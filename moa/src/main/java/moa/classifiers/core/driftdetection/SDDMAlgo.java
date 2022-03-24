package moa.classifiers.core.driftdetection;

import com.yahoo.labs.samoa.instances.Instance;

import java.util.List;

public class SDDMAlgo {

    static void GetJointShift(List<Instance> train, List < Instance > test){
       // if len(cols) == 1 and cols[0] == "":  return 0 //todo

    }
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