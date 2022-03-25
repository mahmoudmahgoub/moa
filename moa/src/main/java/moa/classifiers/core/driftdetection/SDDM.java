/*
 *    EDDM.java
 *    Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *    @author Manuel Baena (mbaena@lcc.uma.es)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package moa.classifiers.core.driftdetection;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.MultiChoiceOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.core.driftdetection.sddmutils.Reader;
import moa.core.Example;
import moa.core.ObjectRepository;
import moa.streams.ArffFileStream;
import moa.tasks.TaskMonitor;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;


/**
 * Drift detection method based in EDDM method of Manuel Baena et al.
 *
 * <p>Early Drift Detection Method. Manuel Baena-Garcia, Jose Del Campo-Avila,
 * Raúl Fidalgo, Albert Bifet, Ricard Gavalda, Rafael Morales-Bueno. In Fourth
 * International Workshop on Knowledge Discovery from Data Streams, 2006.</p>
 *
 * @author Manuel Baena (mbaena@lcc.uma.es)
 * @version $Revision: 7 $
 */
public class SDDM extends AbstractChangeDetector {

    /**
     *
     */
    private static final long serialVersionUID = 140980267062162000L; //todo

    public IntOption normalizationCoefficientOption = new IntOption("normalizationCoefficient",
            'n', "//todo",
            1, 0, Integer.MAX_VALUE);
    public FloatOption weightUniqueOption = new FloatOption("weightUnique",
            'u', "//todo",
            0.001, 0, 1);
    public IntOption binsOption = new IntOption("bins",
            'b', "//todo",
            20, 0, Integer.MAX_VALUE);
    public IntOption subSampleFractionOption = new IntOption("subSampleFraction",
            'f', "//todo",
            1, 0, Integer.MAX_VALUE);

    public IntOption widthOption = new IntOption("width",
            'w', "Size of Window", 1000);

    public MultiChoiceOption distanceMeasureOption = new MultiChoiceOption("distanceMeasure", 'd',
            "//todo", new java.lang.String[]{
            "KLD", "Hellinger", "TVD"},
            new String[]{"KLD: Kullback–Leibler divergence.",
                    "hellinger: hellinger distance.",
                    "TVD: total variation distance."},
            0);
    public MultiChoiceOption shiftMetricOption = new MultiChoiceOption("shiftMetric", 'm',
            "//todo", new java.lang.String[]{
            "COV", "Class", "Concept", "POS", "COND"},
            new String[]{"COV: covariate shift.",
                    "Class: class shift.",
                    "Concept: concept drift.",
                    "POS: posterior drift",
                    "COND: conditional covariate drift"},
            2);


    private static final double FDDM_OUTCONTROL = 0.9;

    private static final double FDDM_WARNING = 0.95;

    private static final double FDDM_MINNUMINSTANCES = 30;

    private double m_numErrors;

    private int m_minNumErrors = 30;

    private int m_n;

    private int m_d;

    private int m_lastd;

    private double m_mean;

    private double m_stdTemp;

    private double m_m2smax;

    private int m_lastLevel;

    private int normalizationCoefficient = 1;
    private double weightUnique = 0.001;
    private int bins = 20;
    private int subSampleFraction = 1;
    private int width;
    public ArrayList<Instance> window;
    //int predictionOption = this.leafpredictionOption.getChosenIndex();

    public SDDM() {
        resetLearning();
    }


       //          distance_measure="kld",
         //        shift_metric="concept_drift"):



   /*
       self.train_data = train_data //nodefault value
    self.test_data = test_data //nodefault value
   self.ignore_columns = ignore_columns //ignore_columns = [],
    self.target_column = target_column  //target_column = ""
*/
    @Override
    public void resetLearning() {
        normalizationCoefficient = this.normalizationCoefficientOption.getValue();
        weightUnique = this.weightUniqueOption.getValue();
        bins = this.binsOption.getValue();
        subSampleFraction = this.subSampleFractionOption.getValue();
        width = this.widthOption.getValue();
        /*m_n = 1;
        m_numErrors = 0;
        m_d = 0;
        m_lastd = 0;
        m_mean = 0.0;
        m_stdTemp = 0.0;
        m_m2smax = 0.0;
        //m_lastLevel = DDM_INCONTROL_LEVEL;
        this.estimation = 0.0;*/
    }

    @Override
    public void input(double prediction) {
        // prediction must be 1 or 0
        // It monitors the error rate
        // System.out.print(prediction + " " + m_n + " " + probability + " ");
        if (this.isChangeDetected == true || this.isInitialized == false) {
            resetLearning();
            this.isInitialized = true;
        }

        this.isChangeDetected = false;

        m_n++;
        if (prediction == 1.0) {
            this.isWarningZone = false;
            this.delay = 0;
            m_numErrors += 1;
            m_lastd = m_d;
            m_d = m_n - 1;
            int distance = m_d - m_lastd;
            double oldmean = m_mean;
            m_mean = m_mean + ((double) distance - m_mean) / m_numErrors;
            this.estimation = m_mean;
            m_stdTemp = m_stdTemp + (distance - m_mean) * (distance - oldmean);
            double std = Math.sqrt(m_stdTemp / m_numErrors);
            double m2s = m_mean + 2 * std;

            // System.out.print(m_numErrors + " " + m_mean + " " + std + " " +
            // m2s + " " + m_m2smax + " ");

            if (m2s > m_m2smax) {
                if (m_n > FDDM_MINNUMINSTANCES) {
                    m_m2smax = m2s;
                }
                //m_lastLevel = DDM_INCONTROL_LEVEL;
                // System.out.print(1 + " ");
            } else {
                double p = m2s / m_m2smax;
                // System.out.print(p + " ");
                if (m_n > FDDM_MINNUMINSTANCES && m_numErrors > m_minNumErrors
                        && p < FDDM_OUTCONTROL) {
                    //System.out.println(m_mean + ",D");
                    this.isChangeDetected = true;
                    //resetLearning();
                    //return DDM_OUTCONTROL_LEVEL;
                } else if (m_n > FDDM_MINNUMINSTANCES
                        && m_numErrors > m_minNumErrors && p < FDDM_WARNING) {
                    //System.out.println(m_mean + ",W");
                    //m_lastLevel = DDM_WARNING_LEVEL;
                    this.isWarningZone = true;
                    //return DDM_WARNING_LEVEL;
                } else {
                    this.isWarningZone = false;
                    //System.out.println(m_mean + ",N");
                    //m_lastLevel = DDM_INCONTROL_LEVEL;
                    //return DDM_INCONTROL_LEVEL;
                }
            }
        } else {
            // System.out.print(m_numErrors + " " + m_mean + " " +
            // Math.sqrt(m_stdTemp/m_numErrors) + " " + (m_mean +
            // 2*Math.sqrt(m_stdTemp/m_numErrors)) + " " + m_m2smax + " ");
            // System.out.print(((m_mean +
            // 2*Math.sqrt(m_stdTemp/m_numErrors))/m_m2smax) + " ");
        }
    }

    @Override
    public void input(Instance prediction) {

        System.out.println("Hello Taa7");


}

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
                                     ObjectRepository repository) {
        // TODO Auto-generated method stub
    }
//**********************************Algorithm**************************************//
    private void extractMetadata(ArrayList<Example<Instance>> data){
       //data = self.__remove_ignore_columns(file_to_parse)
       // data = data.select_dtypes(include=[np.number])
        if(data.size()>0) {
            double bin_fraction = 1.0 / bins;
            DescriptiveStatistics stats = new DescriptiveStatistics();
            IntStream.rangeClosed(1, 10).forEach(i->stats.addValue(i));//add 1,2,3,4,5,6,7,8,9,10 to stats
        }
        /*
         type(self.train_data[0]):<class 'pandas.core.frame.DataFrame'>
         type(self.train_data):<class 'list'>
*/
    }
/*    def __extract_metadata(self, file_to_parse):



            bin_vals = np.arange(0.0, 1 + bin_fraction, bin_fraction)
    bin_vals = bin_vals[bin_vals <=1]
    data = data.quantile(bin_vals, axis = 0)

    self.meta_data = {}
        for column in data.columns:
            if column != self.target_column:
    self.meta_data[column] = np.concatenate(([np.NINF], np.sort(data[column].unique()), [np.inf]))*/
//----------------------


//////////////////////


    static class result_row{
    String col;
        double covariate_shift;
        double class_shift;
        double concept_drift;
        double posterior_drift;
        double conditional_covariate_distribution;
    }
    static class grouping_taa7 {
    List<Double> arr;
    int no_elems;

        public grouping_taa7(double arr_2[], int no_elems) {
            this.arr = Arrays.stream(arr_2).boxed().collect(Collectors.toList());
            this.no_elems = no_elems;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            grouping_taa7 that = (grouping_taa7) o;
            return Objects.deepEquals( this.arr.subList(0,no_elems),that.arr.subList(0,that.no_elems));
        }

        @Override
        public int hashCode() {
            return this.arr.subList(0,no_elems).hashCode();
        }
    }

    static NavigableMap<Double, Double> intervals(List<Double> zzz)
    {

        NavigableMap<Double, Double> map = new TreeMap<Double, Double>();
        double i = 0;
        for (double s : zzz) {
            map.put(s, i);
            i++;
        }
        return map;

    }

    static List<BigDecimal> quantiles(BigDecimal start, BigDecimal end, int steps) {
        BigDecimal step = end.subtract( start).divide(BigDecimal.valueOf(steps));
        return IntStream
                .rangeClosed(0, steps)
                .boxed()
                .map(i -> start.add(step.multiply(BigDecimal.valueOf(i))))
                .collect(Collectors.toList());
    }
    public static void main(String[] args) {

        SDDMAlgo sddmObj = new SDDMAlgo(5);


        DescriptiveStatistics stats = new DescriptiveStatistics();
        stats.setPercentileImpl( new Percentile().
                withEstimationType( Percentile.EstimationType.R_7 ) );
        List < Instance > file_data = new ArrayList<>();
        List < Instance > train;
        List < Instance > test;
        moa.streams.ArffFileStream stream = new ArffFileStream("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\sine1\\sine1_w_50_n_0.1_102.arff", -1);
       // Instance newInst;

        while (stream.hasMoreInstances() ) {
            // newInst = stream.nextInstance().getData();
             file_data.add(stream.nextInstance().getData());
             System.out.println(6);
        }

        int time_step =2,drift =1;
        Reader SDDMReader = new Reader();
        List<result_row[]> z = SDDMReader.npRangeStream(time_step, file_data.size(), drift).map(i -> {
            List<Instance> train2 = file_data.subList(i - time_step, i);
            List<Instance> test2 = file_data.subList(i, i + drift);
            return new result_row[5];
        }).collect(Collectors.toList());
        for(int i:SDDMReader.npRange(time_step,file_data.size(),drift)){
        //we can use for loop instead of nprange
        //for(int i = time_step;i<file_data.size();i=+drift){
            train = file_data.subList(i-time_step,i);
            sddmObj.dataToIntervals(train);
            test = file_data.subList(i,i+drift);
            for(int ii=0; ii<train.get(0).numAttributes();ii++){
                int finalIi = ii;
                stats.clear();
                train.forEach(t->stats.addValue(t.value(finalIi)));//add 1,2,3,4,5,6,7,8,9,10 to stats
                List<Double> zzz = quantiles(BigDecimal.ZERO, BigDecimal.valueOf(1), 5).stream().filter(q -> q.compareTo(BigDecimal.ZERO) > 0).map(q -> stats.getPercentile(q.doubleValue() * 100)).collect(Collectors.toList());
                zzz.add(0, train.get(0).value(finalIi));


                NavigableMap<Double, Double> xxx = intervals(zzz);

                List<Double> cat = train.stream().map(z4 -> xxx.floorEntry(z4.value(finalIi)).getValue()).collect(Collectors.toList());
                System.out.println(cat);
            }


            System.out.println(quantiles(BigDecimal.ZERO,BigDecimal.valueOf(1),20));
          //   train.stream().collect(Collectors.groupingBy(instance -> new grouping_taa7(instance.toDoubleArray(),2)));
            //train.stream().collect(Collectors.groupingBy(instance -> instance.value(0)+instance.value(1)));
           // System.out.println(train.stream().collect(Collectors.groupingBy(instance -> new AbstractMap.SimpleEntry<>(instance.value(0),instance.value(1)))));
            Map<grouping_taa7, List<Instance>> zz = train.stream().collect(Collectors.groupingBy(instance -> new grouping_taa7(instance.toDoubleArray(), 2)));;
            Map<grouping_taa7, Long> mtrain = train.stream().collect(Collectors.groupingBy(instance -> new grouping_taa7(instance.toDoubleArray(), 2), Collectors.counting()));
            Map<grouping_taa7, Long> mtest = test.stream().collect(Collectors.groupingBy(instance -> new grouping_taa7(instance.toDoubleArray(), 2), Collectors.counting()));
            Map<grouping_taa7, ArrayList<Long>> mtest_mtrain = new HashMap<grouping_taa7,ArrayList<Long>>();
            mtrain.forEach((key,value)->mtest_mtrain.computeIfAbsent(key,k -> new ArrayList<>()).add(value));
            mtest.forEach((key,value)->mtest_mtrain.computeIfAbsent(key,k -> new ArrayList<>()).add(value));

            System.out.println( mtest_mtrain.values().toArray()); // returns an array of values
            kullback_leibler_divergence(mtest_mtrain);


            System.out.println(  train.stream().collect(Collectors.groupingBy(instance -> new grouping_taa7(instance.toDoubleArray(),2))).size());
        }
            //    train = data.iloc[i-time_step:i] #time step is window size lenght drift window slide
            //    test = data.iloc[i:i+length_drift]
        IntStream.rangeClosed(1, 4).forEach(i->stats.addValue(i));//add 1,2,3,4,5,6,7,8,9,10 to stats
        IntStream.rangeClosed(6, 15).filter(i -> (i-6)%2 == 0).forEach(System.out::println);
        //System.out.println("max value "+stats.getMax());//max value 10.0

        //System.out.println("min value "+stats.getMin());//min value 1.0

        //System.out.println("mean value "+stats.getMean());//mean value 5.5

        System.out.println("50% value "+stats.getPercentile(50));//75% value 8.25

        System.out.println("25% value "+stats.getPercentile(25));//25% value 2.75

        List < Employee > employees = Arrays.asList(
                new Employee(1, 10, "Chandra"), new Employee(1, 20, "Rajesh"),
                new Employee(1, 20, "Rahul"), new Employee(3, 20, "Rajesh"));

        //Map < Integer, List< Employee >> byDept = employees.stream().collect(
         //       Collectors.groupingBy(Employee::getDepartment));

        //Map < Integer, List< String >> byDept = employees.stream().collect(
        //        Collectors.groupingBy(Employee::getDepartment, Collectors.mapping(Employee::getName,Collectors.toList())));

//        Map<Integer, Map<String, Long>> byDept = employees.stream().collect(
  //              Collectors.groupingBy(Employee::getDepartment, Collectors.groupingBy(Employee::getName,Collectors.counting())));//Collectors.mapping(Employee::getName,Collectors.toList())));
      //  Map<String, Map<Integer, List<Person>>> map = people
        //        .collect(Collectors.groupingBy(Person::getName,
              //          Collectors.groupingBy(Person::getAge));
      //  List<String> fields = Arrays.asList(new String[]{"City", "Age"}); //This will be constructed as per user's wish

        Map<Integer, Map<String, Long>> byDept = employees.stream().collect(
                             Collectors.groupingBy(Employee::getDepartment, Collectors.groupingBy(Employee::getName,Collectors.counting())));//Collectors.mapping(Employee::getName,Collectors.toList())));
                (byDept).forEach((k, v) -> System.out.println("DeptId:" +k +"   " +
                 v));


        //System.out.println(stats);
    }
/*
    private static List<String> buildClassificationFunction(Map<String,String> map, List<String> fields) {
        return fields.stream()
                .map(map::get)
                .collect(Collectors.toList());
    }



    Map<List<String>, List<Collection<String>>> city = aList.stream()
            .collect(Collectors.groupingBy(map ->
                            buildClassificationFunction(map, fields), //Call the function to build the list
                    Collectors.mapping(Map::values, Collectors.toList())));
*/
  /*  private getJointShift(){



    }
    def __get_joint_shift(train, test, cols):
          #  if len(cols) == 1 and cols[0] == "":
          #  return 0

    train = tr[cols]
    test = te[cols]

    cols = [c for c in cols]

    grouped = pd.merge(train.groupby(cols).size().reset_index(name = 'count_train'),
                            test.groupby(cols).size().reset_index(name = 'count_test'),
    on = cols, how="outer")
    grouped = grouped[["count_train", "count_test"]]

    grouped['count_train'] = grouped['count_train'].fillna(0)
    grouped['count_test'] = grouped['count_test'].fillna(0)

        return Helper.get_distance(grouped["count_train"], grouped["count_test"], normalization_coeff=self.normalization_coeff,
    method=self.distance_measure)*/
//**********************************HELPER*****************************************//
private static double kullback_leibler_divergence( Map<grouping_taa7, ArrayList<Long>> vals) {

    double kld = 0;
    for( ArrayList<Long> value : vals.values())
    {
        if (value.size()<2)
            continue;
        kld += value.get(0) * Math.log(value.get(0) / value.get(1));
    }


    /*vals.values().stream()
            .filter(i -> i.size() ==2 )
            .mapToDouble(value ->  value.get(0) * Math.log(value.get(0) / value.get(1)))
            .sum();*/
   /* double[] kld2 = new double [1];
vals.forEach((key,value)->
{
    if (value.size() > 1)
       // continue;

        kld2[0] += value.get(0) * Math.log(value.get(0) / value.get(1));
});
    */
        return kld / Math.log(2);


    }

    /*

    private static double kullback_leibler_divergence(double[] arr1, double[] arr2) {

        mtest_mtrain.forEach((key,value)->);
        double kld = 0;
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] == 0 || arr2[i] == 0)
                continue;
            kld += arr1[i] * Math.log(arr1[i] / arr2[i]);

        }
        return kld / Math.log(2);


    }
     */
}