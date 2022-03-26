package moa.classifiers.core.driftdetection;

import com.yahoo.labs.samoa.instances.Instance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import sun.plugin.javascript.navig.Array;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SDDMAlgo {


    private ArrayList<Integer> numeric_columns = new ArrayList<>();  // the columns that I will use in  binData and __extract_metadata
    private int numsBins;
    void detect_concept_drift (){

    }

    void extract_metadata (){
      //remove_ignore_columns todo


    }

    public SDDMAlgo( int numsBins) {
        this.numsBins = numsBins;
    }

    List<BigDecimal> binsGenerator(int numBins) {
        BigDecimal start = BigDecimal.ZERO;
        BigDecimal end =  BigDecimal.ONE;

        BigDecimal step = end.subtract( start).divide(BigDecimal.valueOf(numBins));
        return IntStream
                .rangeClosed(0, numBins)
                .boxed()
                .map(i -> start.add(step.multiply(BigDecimal.valueOf(i))))
                .collect(Collectors.toList());
        //maybe need check for that bins list values <=1 todo
    }

    //assign number for every interval using intervalsLimits
    List<NavigableMap<Double, Double>> intervalsBuilder(List<List<Double>> intervalsLimitsLists)
    {
        /*
        todo if I used sample like in python "bin" method:  data.sample(frac = <>),
         I will need to add inf value to adjust the last interval: (max:inf] as I don't guarantee that I have the min and max values in the sample
         (-inf,min] is already here because of using function ceilingEntry when I use the Mapper
        */
        List<NavigableMap<Double, Double>> mappers = new ArrayList<>();

        for (List<Double> intervalsLimits : intervalsLimitsLists) {
            double i = 0;
            NavigableMap<Double, Double> mapper = new TreeMap<Double, Double>();
            for(double Limit:intervalsLimits) {
                mapper.put(Limit, i);
                i++;
            }
            mappers.add(mapper);

        }
        return mappers;
    }

    List<List<Double>> dataQuantiles( List<Instance> data){ //todo we can make this function work only on 1D and making the 2D in the calling Context
        DescriptiveStatistics stats = new DescriptiveStatistics();

        stats.setPercentileImpl( new Percentile().
                withEstimationType( Percentile.EstimationType.R_7 ) );


        int features = data.get(0).numAttributes();
        List<List<Double>> quantilesOfData = new ArrayList<>();

        for(int i=0; i<features -1 ;i++){ // need to make a filter to choose only numeric cols, now I assume all the cols except last one is numeric todo
            int finalI = i; //to use within Lambda expression
            stats.clear();
            data.forEach(inst->stats.addValue(inst.value(finalI)));
            List<Double> temp = binsGenerator(numsBins).stream().filter(q -> q.compareTo(BigDecimal.ZERO) > 0)
                    .map(q -> stats.getPercentile(q.doubleValue() * 100)).collect(Collectors.toList());
            double min =data.get(0).value(finalI);
            for(Instance val:data){
                if(val.value(finalI)<min){
                    min = val.value(finalI);
                }
            }
            temp.add(0,min);
            quantilesOfData.add(temp);
        }
     return quantilesOfData;
    }

    List<List<Double>> binData(List<Instance> data){

        List <NavigableMap<Double, Double>> IntervalsMappers = intervalsBuilder(dataQuantiles(data));
        int data_size =data.size();
        int features = data.get(0).numAttributes();

        List<List<Double>> dataBinned = new ArrayList<>();
        double [][] arrDataBinned = new double[data_size][features];
        for(int i=0; i<features -1 ;i++){
            NavigableMap<Double, Double> mapper = IntervalsMappers.get(i);
            int finalI = i;
            List<Double> mappedData = data.stream().map(inst -> mapper.ceilingEntry(inst.value(finalI)).getValue()).collect(Collectors.toList());
            int row = 0;
            for(double val:mappedData){
                arrDataBinned[row][finalI] = val;
                row++;
            }
        }
         //last col data
        int row = 0;
        for(Instance inst:data){
            arrDataBinned[row][features-1] = inst.value(features-1);
            row++;
        }


        for (int i = 0; i<arrDataBinned.length; i++){
            List<Double> temp = new ArrayList<>();
            for (int j = 0; j<arrDataBinned[i].length; j++){
                temp.add(arrDataBinned[i][j]);
            }
            dataBinned.add(temp);
        }
        System.out.println(arrDataBinned);
        System.out.println(dataBinned);
        return dataBinned;
}

    static class InstancesGrouping {
        List<Double> arr;
        int noElems;

        public InstancesGrouping(List<Double> arr, int noElems) {
            this.arr = arr;
            this.noElems = noElems;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InstancesGrouping that = (InstancesGrouping) o;
            return Objects.deepEquals( this.arr.subList(0,noElems),that.arr.subList(0,that.noElems));
        }

        @Override
        public int hashCode() {
            return this.arr.subList(0,noElems).hashCode();
        }
    }

    private static double kullback_leibler_divergence( Map<InstancesGrouping, ArrayList<Long>> vals) {

        double kld = 0;
        for( ArrayList<Long> value : vals.values())
        {
            if (value.size()<2)
                continue;
            kld += value.get(0) * Math.log(value.get(0) / value.get(1));
        }
        return kld / Math.log(2);

    }

    void getJointShift(List<List<Double>> trainData,List<List<Double>>  testData){ //todo add cols to method params
        // if len(cols) == 1 and cols[0] == "":  return 0 //todo add this check which is found in python

        Map<InstancesGrouping, Long> groupedTrain = trainData.stream().collect(Collectors.groupingBy(instance -> new InstancesGrouping(instance, 2), Collectors.counting()));
        Map<InstancesGrouping, Long> groupedTest = testData.stream().collect(Collectors.groupingBy(instance -> new InstancesGrouping(instance, 1), Collectors.counting()));
        Map<InstancesGrouping, ArrayList<Long>> groupedTestTrain = new HashMap<InstancesGrouping,ArrayList<Long>>();
        groupedTrain.forEach((key,value)->groupedTestTrain.computeIfAbsent(key,k -> new ArrayList<>()).add(value));
        groupedTest.forEach((key,value)->groupedTestTrain.computeIfAbsent(key,k -> new ArrayList<>()).add(value));

        System.out.println( groupedTestTrain.values().toArray()); // returns an array of values
        kullback_leibler_divergence(groupedTestTrain);

    }

    /*        if len(cols) == 1 and cols[0] == "":
            return 0

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
    method=self.distance_measure)

    def __getJointResults(train, test, covariate_columns):
    results = {}
    normalize = 1

    results["covariate_shift"] = self.__get_joint_shift(train, test, covariate_columns)/normalize #covariate_columns
    results["class_shift"] = self.__get_joint_shift(train, test, [self.target_column])/normalize
    results["concept_drift"] = self.__get_joint_shift(train, test, test.columns)/normalize #test.columns

    results["posterior_drift"] = self.__get_posterior_drift(train, test, covariate_columns)/normalize
    results["conditional_covariate_distribution"] = self.__get_conditional_covariate_drift(train, test, covariate_columns)/normalize
   // private:
   // train_data = train_data
   // self.test_data = test_data
  /*  def detect_concept_drift(self):
            self.__extract_metadata(self.train_data[0])

    test_binned_data = self.__bin_data(self.test_data[0].copy(), subsample = False)

    covariate_columns = np.array(test_binned_data.columns)
            if(self.target_column != ""):
    covariate_columns = np.delete(covariate_columns, np.where(covariate_columns == self.target_column))

*/
   /* def __get_joint_shift(self, tr, te, cols):



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
}
///*
// *    EDDM.java
// *    Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
// *    @author Manuel Baena (mbaena@lcc.uma.es)
// *
// *    This program is free software; you can redistribute it and/or modify
// *    it under the terms of the GNU General Public License as published by
// *    the Free Software Foundation; either version 3 of the License, or
// *    (at your option) any later version.
// *
// *    This program is distributed in the hope that it will be useful,
// *    but WITHOUT ANY WARRANTY; without even the implied warranty of
// *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *    GNU General Public License for more details.
// *
// *    You should have received a copy of the GNU General Public License
// *    along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package moa.classifiers.core.driftdetection;
//
//import com.github.javacliparser.FloatOption;
//import com.github.javacliparser.IntOption;
//import com.github.javacliparser.MultiChoiceOption;
//import com.yahoo.labs.samoa.instances.Instance;
//import moa.AbstractMOAObject;
//import moa.core.Example;
//import moa.core.ObjectRepository;
//import moa.tasks.TaskMonitor;
//import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//
///**
// * Drift detection method based in EDDM method of Manuel Baena et al.
// *
// * <p>Early Drift Detection Method. Manuel Baena-Garcia, Jose Del Campo-Avila,
// * Raúl Fidalgo, Albert Bifet, Ricard Gavalda, Rafael Morales-Bueno. In Fourth
// * International Workshop on Knowledge Discovery from Data Streams, 2006.</p>
// *
// * @author Manuel Baena (mbaena@lcc.uma.es)
// * @version $Revision: 7 $
// */
///
//public class SDDMAlgorithm extends AbstractMOAObject {
//
//    public void getJointShift(ArrayList<Example<Instance>> data){
//    def __get_joint_shift(self, tr, te, cols):
//            if len(cols) == 1 and cols[0] == "":
//            return 0
//
//    train = tr[cols]
//    test = te[cols]
//
//    cols = [c for c in cols]
//
//    grouped = pd.merge(train.groupby(cols).size().reset_index(name = 'count_train'),
//                            test.groupby(cols).size().reset_index(name = 'count_test'),
//    on = cols, how="outer")
//    grouped = grouped[["count_train", "count_test"]]
//
//    grouped['count_train'] = grouped['count_train'].fillna(0)
//    grouped['count_test'] = grouped['count_test'].fillna(0)
//
//        return Helper.get_distance(grouped["count_train"], grouped["count_test"], normalization_coeff=self.normalization_coeff,
//    method=self.distance_measure)
//
////**********************************Algorithm**************************************//
//    private void extractMetadata(ArrayList<Example<Instance>> data){
//       //data = self.__remove_ignore_columns(file_to_parse)
//       // data = data.select_dtypes(include=[np.number])
//        if(data.size()>0) {
//            double bin_fraction = 1.0 / bins;
//            DescriptiveStatistics stats = new DescriptiveStatistics();
//            IntStream.rangeClosed(1, 10).forEach(i->stats.addValue(i));//add 1,2,3,4,5,6,7,8,9,10 to stats
//        }
//        /*
//         type(self.train_data[0]):<class 'pandas.core.frame.DataFrame'>
//         type(self.train_data):<class 'list'>
//*/
//    }
///*    def __extract_metadata(self, file_to_parse):
//
//
//
//            bin_vals = np.arange(0.0, 1 + bin_fraction, bin_fraction)
//    bin_vals = bin_vals[bin_vals <=1]
//    data = data.quantile(bin_vals, axis = 0)
//
//    self.meta_data = {}
//        for column in data.columns:
//            if column != self.target_column:
//    self.meta_data[column] = np.concatenate(([np.NINF], np.sort(data[column].unique()), [np.inf]))*/
//
//    public static void main(String[] args) {
//        System.out.println("hji");
//        DescriptiveStatistics stats = new DescriptiveStatistics();
//
//        IntStream.rangeClosed(1, 4).forEach(i->stats.addValue(i));//add 1,2,3,4,5,6,7,8,9,10 to stats
//
//        //System.out.println("max value "+stats.getMax());//max value 10.0
//
//        //System.out.println("min value "+stats.getMin());//min value 1.0
//
//        //System.out.println("mean value "+stats.getMean());//mean value 5.5
//
//        System.out.println("50% value "+stats.getPercentile(50));//75% value 8.25
//
//        System.out.println("25% value "+stats.getPercentile(25));//25% value 2.75
//
//        List < Employee > employees = Arrays.asList(
//                new Employee(1, 10, "Chandra"), new Employee(1, 20, "Rajesh"),
//                new Employee(1, 20, "Rahul"), new Employee(3, 20, "Rajesh"));
//
//        //Map < Integer, List< Employee >> byDept = employees.stream().collect(
//         //       Collectors.groupingBy(Employee::getDepartment));
//
//        //Map < Integer, List< String >> byDept = employees.stream().collect(
//        //        Collectors.groupingBy(Employee::getDepartment, Collectors.mapping(Employee::getName,Collectors.toList())));
//
////        Map<Integer, Map<String, Long>> byDept = employees.stream().collect(
//  //              Collectors.groupingBy(Employee::getDepartment, Collectors.groupingBy(Employee::getName,Collectors.counting())));//Collectors.mapping(Employee::getName,Collectors.toList())));
//      //  Map<String, Map<Integer, List<Person>>> map = people
//        //        .collect(Collectors.groupingBy(Person::getName,
//              //          Collectors.groupingBy(Person::getAge));
//      //  List<String> fields = Arrays.asList(new String[]{"City", "Age"}); //This will be constructed as per user's wish
//
//        Map<Integer, Map<String, Long>> byDept = employees.stream().collect(
//                             Collectors.groupingBy(Employee::getDepartment, Collectors.groupingBy(Employee::getName,Collectors.counting())));//Collectors.mapping(Employee::getName,Collectors.toList())));
//                (byDept).forEach((k, v) -> System.out.println("DeptId:" +k +"   " +
//                 v));
//
//
//        //System.out.println(stats);
//    }
//
//    @Override
//    public void getDescription(StringBuilder sb, int indent) {
//
//    }
///*
//    private static List<String> buildClassificationFunction(Map<String,String> map, List<String> fields) {
//        return fields.stream()
//                .map(map::get)
//                .collect(Collectors.toList());
//    }
//
//
//
//    Map<List<String>, List<Collection<String>>> city = aList.stream()
//            .collect(Collectors.groupingBy(map ->
//                            buildClassificationFunction(map, fields), //Call the function to build the list
//                    Collectors.mapping(Map::values, Collectors.toList())));
//*/
//  /*  private getJointShift(){
//
//
//
//    }
//    def __get_joint_shift(train, test, cols):
//          #  if len(cols) == 1 and cols[0] == "":
//          #  return 0
//
//    train = tr[cols]
//    test = te[cols]
//
//    cols = [c for c in cols]
//
//    grouped = pd.merge(train.groupby(cols).size().reset_index(name = 'count_train'),
//                            test.groupby(cols).size().reset_index(name = 'count_test'),
//    on = cols, how="outer")
//    grouped = grouped[["count_train", "count_test"]]
//
//    grouped['count_train'] = grouped['count_train'].fillna(0)
//    grouped['count_test'] = grouped['count_test'].fillna(0)
//
//        return Helper.get_distance(grouped["count_train"], grouped["count_test"], normalization_coeff=self.normalization_coeff,
//    method=self.distance_measure)*/
//
//}