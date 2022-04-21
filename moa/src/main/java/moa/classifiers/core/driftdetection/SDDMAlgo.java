package moa.classifiers.core.driftdetection;

import com.yahoo.labs.samoa.instances.Instance;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class SDDMAlgo {


    private ArrayList<Integer> numeric_columns = new ArrayList<>();  // the columns that I will use in  binData and __extract_metadata
    private int numsBins;
    private double normalizationCoeff;
    private List <NavigableMap<Double, Double>> IntervalsMappers;
    private int numClassLabels;
    private int targetColumn;

    public SDDMAlgo(int numsBins, long normalizationCoeff) {
        this.numsBins = numsBins;
        this.normalizationCoeff = normalizationCoeff;
        IntervalsMappers =new ArrayList<>();
        numClassLabels = 0;
        targetColumn = 0;
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
    void binsIntervalsBuilder(List<Instance> data) //metadata
    {
        IntervalsMappers.clear();
        List<List<Double>> intervalsLimitsLists = dataQuantiles(data);

        /*(-inf,min] is already here because of using function ceilingEntry when I use the Mapper*/
        for (List<Double> intervalsLimits : intervalsLimitsLists) {
            double i = 0;
            NavigableMap<Double, Double> mapper = new TreeMap<>();
            for(double Limit:intervalsLimits) {

                mapper.putIfAbsent(Limit, i); //todo check if putIfAbsent is correct or only put
                i++;
            }
            mapper.put(Double.MAX_VALUE,i); //for range (max:inf] //todo check
            IntervalsMappers.add(mapper);

        }
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

    List<List<Double>> binData(List<Instance> data) throws Exception { //todo make it only for numeric columns

        if (IntervalsMappers.size() == 0) {
            throw new Exception("IntervalMappers are not build yet, please build them using intervalsBuilder");
        }
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

    public void setNumClassLabels(int numClassLabels) {
        this.numClassLabels = numClassLabels;
    }

    public void setTargetColumn(int targetColumn) {
        this.targetColumn = targetColumn;
    }

    public static class InstancesGrouping {
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

    private Map<InstancesGrouping, List<Double>> dataNormalize(Map<InstancesGrouping, List<Long>> data){
        Map<InstancesGrouping, List<Double>> normalizedData = new HashMap<>();
        double [][] arrnormalizedData = new double[data.size()][2];

        for(int j = 0; j<2;j++) {
            double arr_sum = 0 ;
            for (List<Long> value : data.values()) {
                arr_sum +=value.get(j);
            }
            double sumFactor = arr_sum + normalizationCoeff * data.size();
            int i = 0;
            for (InstancesGrouping key:data.keySet()) {
                arrnormalizedData[i][j] = (data.get(key).get(j) + normalizationCoeff) / (sumFactor + 2);
                i++;
            }

        }
         int i = 0;
        for (InstancesGrouping key:data.keySet()){
            ArrayList<Double> temp = new ArrayList<>();
            for (int j = 0; j<arrnormalizedData[i].length; j++){
                temp.add(arrnormalizedData[i][j]);
            }
            normalizedData.put(key, temp);
            i++;
        }

        return normalizedData;
    }



    double get_distance(Map<InstancesGrouping, List<Long>> data) //todo add method in params list: method = "kld"
    {
        return kullback_leibler_divergence(dataNormalize(data));
    }
    private double kullback_leibler_divergence( Map<InstancesGrouping, List<Double>> vals) {

        double kld = 0, kld1 = 0, kld2 = 0;
        for( List<Double> value : vals.values())
        {
            if (value.get(0) == 0 || value.get(1) == 0)
                continue;
            kld1 += value.get(0) * Math.log(value.get(0) / value.get(1));
            kld2 += value.get(1) * Math.log(value.get(1) / value.get(0));
        }
        kld = (kld1 / Math.log(2) + kld2 / Math.log(2))/2;
        return kld;

    }

    double getJointShift(List<List<Double>> trainData,List<List<Double>>  testData,Set<Integer> cols){
        // if len(cols) == 1 and cols[0] == "":  return 0 //todo add this check which is found in python
        //todo check target_column
        /*
        List<List<Double>> modifiedTrainData = new ArrayList<>(trainData.stream().map(x -> new ArrayList<>(x)).collect(Collectors.toList()));
        List<List<Double>> modifiedTestData = new ArrayList<>(testData.stream().map(x -> new ArrayList<>(x)).collect(Collectors.toList()));

        for(List<Double> Instance:modifiedTrainData) {
            for (int i = 0; i < Instance.size(); i++) {
                if (!cols.contains(i))
                    Instance.remove(i);
            }
        }
            for(List<Double> Instance:modifiedTestData){
                for(int i = 0; i<Instance.size();i++){
                    if (!cols.contains(i))
                        Instance.remove(i);
                }
        }*/
        List<List<Double>> modifiedTrainData = new ArrayList<>();
        List<List<Double>> modifiedTestData = new ArrayList<>();

        for(List<Double> Instance:trainData) {
            List<Double> tempInstance =  new ArrayList<>();
            for (int i = 0; i < Instance.size(); i++) {
                if (cols.contains(i))
                    tempInstance.add(Instance.get(i));
            }
            modifiedTrainData.add(tempInstance);
        }

        for(List<Double> Instance:testData) {
            List<Double> tempInstance =  new ArrayList<>();
            for (int i = 0; i < Instance.size(); i++) {
                if (cols.contains(i))
                    tempInstance.add(Instance.get(i));
            }
            modifiedTestData.add(tempInstance);
        }

        int colsSize = cols.size();
        Map<InstancesGrouping, Long> groupedTrain = modifiedTrainData.stream().collect(Collectors.groupingBy(instance -> new InstancesGrouping(instance, colsSize), Collectors.counting()));
        Map<InstancesGrouping, Long> groupedTest = modifiedTestData.stream().collect(Collectors.groupingBy(instance -> new InstancesGrouping(instance, colsSize), Collectors.counting()));
        Map<InstancesGrouping, List<Long>> groupedTestTrain = new HashMap<>();
        groupedTrain.forEach((key,value)->groupedTestTrain.computeIfAbsent(key,k -> new ArrayList<>()).add(value));
        groupedTest.forEach((key,value)->groupedTestTrain.computeIfAbsent(key,k -> new ArrayList<>(Arrays.asList(Long.valueOf(0)))).add(value)); //add 0 for missed train data to make pairs
        for(List<Long> val: groupedTestTrain.values()) {
            if (val.size() <2 )
                val.add((long) 0); //add 0 for missed test data to make pairs
        }
        System.out.println( groupedTestTrain.values().toArray()); // returns an array of values

       return get_distance(groupedTestTrain);
    }


    private double[][] dataNormalize2(long[][] data){ //todo merge with dataNormalize
        double [][] arrnormalizedData = new double[data.length][2];

        for(int j = 0; j<2;j++) {
            double arr_sum = 0 ;
            for (long[] value : data) {
                arr_sum +=value[j];
            }
            double sumFactor = arr_sum + normalizationCoeff * data.length;

            for (int i = 0;i<data.length;i++) {
                arrnormalizedData[i][j] = (data[i][j] + normalizationCoeff) / (sumFactor + 2);
            }

        }

        return arrnormalizedData;
    }



    double get_distance2(long[][] data) //todo merge with get_distance
    {
        return kullback_leibler_divergence2(dataNormalize2(data));
    }
    private double kullback_leibler_divergence2( double[][] vals) { //todo merge with kullback_leibler_divergence

        double kld = 0, kld1 = 0, kld2 = 0;
        for( double[] value : vals)
        {
            if (value[0] == 0 || value[1] == 0)
                continue;
            kld1 += value[0] * Math.log(value[0]/ value[1]);
            kld2 += value[1] * Math.log(value[1] / value[0]);
        }
        kld = (kld1 / Math.log(2) + kld2 / Math.log(2))/2;
        return kld;

    }

    double getPosteriorDrift(List<List<Double>> trainData,List<List<Double>>  testData,Set<Integer> cols){
        //if self.target_column == "": return 0 //todo
        List<List<Double>> modifiedTrainData = trainData.stream().map(ArrayList::new).collect(Collectors.toList());
        List<List<Double>> modifiedTestData = testData.stream().map(ArrayList::new).collect(Collectors.toList());

       /* for(List<Double> Instance:modifiedTrainData) {
            for (int i = 0; i < Instance.size(); i++) {
                if (!cols.contains(i))
                    Instance.remove(i);
            }
        }
        for(List<Double> Instance:modifiedTestData){
            for(int i = 0; i<Instance.size();i++){
                if (!cols.contains(i))
                    Instance.remove(i);
            }
        }*/
        int colsSize = cols.size();
        Map<InstancesGrouping, Double> weightsGroupedTrain = modifiedTrainData.stream().collect(Collectors.groupingBy(
                instance -> new InstancesGrouping(instance, colsSize), Collectors.collectingAndThen(Collectors.counting(),aLong -> 1.0*aLong /modifiedTrainData.size())));
        Map<InstancesGrouping, Double> weightsGroupedTest = modifiedTestData.stream().collect(Collectors.groupingBy(
                instance -> new InstancesGrouping(instance, colsSize), Collectors.collectingAndThen(Collectors.counting(),aLong ->1.0*aLong/modifiedTestData.size())));

        Map<InstancesGrouping, Map<Double, Long>>  groupedTrain = modifiedTrainData.stream().collect(Collectors.groupingBy(
                instance -> new InstancesGrouping(instance, colsSize),
                Collectors.groupingBy(instance -> instance.get(targetColumn),Collectors.counting())
        ));
        Map<InstancesGrouping, Map<Double, Long>>  groupedTest = modifiedTestData.stream().collect(Collectors.groupingBy(
                instance -> new InstancesGrouping(instance, colsSize),
                Collectors.groupingBy(instance -> instance.get(targetColumn),Collectors.counting())
        ));

             //   Map<InstancesGrouping, ArrayList<Long>> groupedTestTrain = new HashMap<>();
       // groupedTrain.forEach((key,value)->groupedTestTrain.computeIfAbsent(key,k -> new ArrayList<>()).add(value.));
        Map<InstancesGrouping, long[][]> groupedclasses= new HashMap<>();

        groupedTrain.forEach((iG,m)->{
            long[][] longs = new long[2][2]; //todo make it generic numClassLabels instead of the second 2 Long[2][numClassLabels]
               longs[0] = new long[]{m.getOrDefault(0.0,0L),0L};
               longs[1] = new long[]{m.getOrDefault(1.0,0L),0L};
            groupedclasses.put(iG,longs);

        });

        groupedTest.forEach((iG,m)->{
            if(groupedclasses.containsKey(iG))
            {
                groupedclasses.get(iG)[0][1]= m.getOrDefault(0.0,0L);
                groupedclasses.get(iG)[1][1]= m.getOrDefault(1.0,0L);

            } else {
                long[][] longs = new long[2][2]; //todo make it generic numClassLabels instead of the second 2 Long[2][numClassLabels]
                longs[0] = new long[]{0L,m.getOrDefault(0.0,0L)};
                longs[1] = new long[]{0L,m.getOrDefault(1.0,0L)};
                groupedclasses.put(iG,longs);
            }
        });

        double posterior_drift = groupedclasses.entrySet().stream().mapToDouble(e -> get_distance2(e.getValue()) *
                (weightsGroupedTrain.getOrDefault(e.getKey(), 0.0) + weightsGroupedTest.getOrDefault(e.getKey(), 0.0)) / 2).sum();
        System.out.println("hola"+groupedclasses);
        return posterior_drift;
    }

  double getConditionalCovariateDrift(List<List<Double>> trainData,List<List<Double>>  testData,Set<Integer> covariateCols) {
      //if self.target_column == "": return 0 todo
      //todo targetclass need to be added

      double ccd = 0;
      for (int classLabel=0;classLabel< numClassLabels;classLabel++) {
          int finalClassLabel = classLabel;
          List<List<Double>> sliceTrain = trainData.stream().filter(x -> x.get(targetColumn) == finalClassLabel).collect(Collectors.toList());
          List<List<Double>> sliceTest = testData.stream().filter(x -> x.get(targetColumn) == finalClassLabel).collect(Collectors.toList());
          double kld = getJointShift(sliceTrain, sliceTest, covariateCols);
          ccd = ccd + kld * ((double)sliceTest.size() /testData.size() + (double)sliceTrain.size() / trainData.size()) / 2;
      }
      return ccd;
  }

   Map<String, Double> getJointResults(List<List<Double>> trainData, List<List<Double>>  testData, Set<Integer> covariateColumns) {
       Map<String,Double> results = new HashMap<>();
       int normalize = 1;
       Set<Integer> targetColumn = new HashSet<>(Arrays.asList(2)); //todo extract from here
       Set<Integer> testColumns = new HashSet<>(Arrays.asList(0,1,2)); // todo extract from here
       results.put("covariateshift", (getJointShift(trainData, testData, covariateColumns) / normalize));
       results.put("classshift",     (getJointShift(trainData, testData, targetColumn) / normalize));
       results.put("conceptdrift",   (getJointShift(trainData, testData, testColumns) / normalize));
       results.put("posteriordrift", (getPosteriorDrift(trainData, testData, covariateColumns) / normalize));
       results.put("conditionalcovariatedistribution", (getConditionalCovariateDrift(trainData, testData, covariateColumns) / normalize));
       System.out.println("TTTTTTTTTTTTTTTTTTTTTTTest");
       return results;
   }
}

