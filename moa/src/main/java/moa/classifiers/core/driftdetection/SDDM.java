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
import com.github.javacliparser.StringOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.core.Example;
import moa.core.ObjectRepository;
import moa.tasks.TaskMonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;



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

    public static void main(String[] args) {
        System.out.println("hji");
        DescriptiveStatistics stats = new DescriptiveStatistics();

        IntStream.rangeClosed(1, 4).forEach(i->stats.addValue(i));//add 1,2,3,4,5,6,7,8,9,10 to stats

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
    private static double kullback_leibler_divergence(double[] arr1, double[] arr2) {
        double kld = 0;
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i] == 0 || arr2[i] == 0)
                continue;
            kld += arr1[i] * Math.log(arr1[i] / arr2[i]);

        }
        return kld / Math.log(2);


    }
}