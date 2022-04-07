package moa.classifiers.core.driftdetection.benchmarking;

import com.yahoo.labs.samoa.instances.Instance;
import moa.capabilities.CapabilitiesHandler;
import moa.classifiers.MultiClassClassifier;
import moa.classifiers.core.driftdetection.*;
import moa.classifiers.drift.DriftDetectionMethodClassifier;
import moa.learners.Learner;
import moa.options.ClassOption;
import moa.options.OptionsHandler;
import moa.streams.ArffFileStream;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@State(Scope.Benchmark)
public class ExecutionPlan {

    public AbstractChangeDetector classifier;
    public  double[] data;
    static List<Instance> dataInstances = new ArrayList<>();
    static Map<String, String> filenames = createMap();

    private static Map<String, String> createMap() {
        Map<String,String> myMap = new HashMap<String, String>();
        myMap.put("Inc1554","E:\\offline Thesis work\\originaladwin++\\datasets\\Incremental datasets\\1554_drifts_dataset.txt");
        myMap.put("Inc202","E:\\offline Thesis work\\originaladwin++\\datasets\\Incremental datasets\\202_drifts_dataset.txt");
        myMap.put("Inc3","E:\\offline Thesis work\\originaladwin++\\datasets\\Incremental datasets\\3_drifts_dataset.txt");
        myMap.put("Grad1738","E:\\offline Thesis work\\originaladwin++\\datasets\\Gradual datasets\\1738_drifts_dataset.txt");
        myMap.put("Grad273", "E:\\offline Thesis work\\originaladwin++\\datasets\\Gradual datasets\\273_drifts_dataset.txt");
        myMap.put("Grad10","E:\\offline Thesis work\\originaladwin++\\datasets\\Gradual datasets\\10_drifts_dataset.txt");
        myMap.put("Ab1283","E:\\offline Thesis work\\originaladwin++\\datasets\\Abrupt datasets\\1283_drifts_dataset.txt");
        myMap.put("Ab162","E:\\offline Thesis work\\originaladwin++\\datasets\\Abrupt datasets\\162_drifts_dataset.txt");
        myMap.put("Ab9","E:\\offline Thesis work\\originaladwin++\\datasets\\Abrupt datasets\\9_drifts_dataset.txt");
        myMap.put("Steady","E:\\offline Thesis work\\originaladwin++\\datasets\\Steady dataset\\Steady dataset.txt");
        myMap.put("Sine1","E:\\offline Thesis work\\originaladwin++\\datasets\\sine1_w_50_n_0.1_101.arff");
        myMap.put("elec","C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\usp-stream-data\\INSECTS-abrupt_balanced_norm.arff");
        return myMap;
    }

    static private double[] dataReader(String fileName) throws FileNotFoundException {

        File file = new File(filenames.get(fileName));
        Scanner sc = new Scanner(file);

        int i = 0;
        double[] data =new double[2000000];// new double[(int) file.length()];
        while (sc.hasNextLine()&&i<2000000) {
            data[i] = Double.parseDouble(sc.nextLine());
            i++;
        }
        //note: to convert 1 col data to 3 cols by duplicate use this regex in replace and find of notebad++
        //find ^(\d\.\d)$
        //and replace with: $1,$1,$1

        // E:\offline Thesis work\originaladwin++\datasets\Gradual datasets\10_drifts_dataset.arff
        return data;
    }

   static private List<Instance> arffDataReader(String fileName){ //static private double[]
        dataInstances.clear();
        //C:\Users\MahmoudMahgoub\Desktop\thesis python\data\sine1\sine1_w_50_n_0.1_102.arff
        moa.streams.ArffFileStream stream = new ArffFileStream(filenames.get(fileName), -1);
        //moa.streams.ArffFileStream stream = new ArffFileStream( "C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\usp-stream-data\\airlines.arff",-1);
       //moa.streams.ArffFileStream stream = new ArffFileStream(,-1)
       // new double[(int) file.length()];
        //plan.classifier.input(plan.dataInstances.get(i2).classValue());
        //   boolean flag = mc.benchmarkAdwin(plan.data[i2]);//mc.benchmarkAdwin(plan.dataInstances.get(i2).classValue());
        while (stream.hasMoreInstances() ) {
            dataInstances.add(stream.nextInstance().getData());
        }
       // double[] data =new double[dataInstances.size()];
        //for(int i =0 ;i<dataInstances.size();i++)
          //  data[i] =dataInstances.get(i).value(7);

       // System.out.println("hola: "+data.length);
        return dataInstances;
    }



    @State(Scope.Benchmark)
    public static class AdwinMOAExecutionPlan{

        public AbstractChangeDetector classifier;
        public  double[] data;
       // @Param({"Inc1554", "Inc202","Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283" , "Ab162", "Ab9", "Steady"})
        @Param({"Inc1554"})
        String filename;

        //@Param({".0002","0.002","0.25",".99"})
        double deltaAdwin;
        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new ADWINChangeDetector();

            data = dataReader(filename);
            ((ADWINChangeDetector) classifier).deltaAdwinOption.setValue(1);
          //  ((ADWINChangeDetector) classifier).deltaAdwinOption.setValue(deltaAdwin);

        }
    }

    @State(Scope.Benchmark)
    public static class AdwinPlusPlus1ExecutionPlan{ //SequentialADWINImpl
        public AbstractChangeDetector classifier;
        public  double[] data;
        //@Param({"Inc1554", "Inc202","Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283" , "Ab162", "Ab9", "Steady"})
        @Param({"Sine1"})
        String filename;

        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new ADWINPlusChangeDetector();
            if(filename.equals("Sine1") || filename.equals("Circles")) {
                System.out.println("HoLA");
               // data = arffDataReader(filename);
            }
            else
            data = dataReader(filename);
            //classifier = new ADWINPlusPlusWrapper(1, SequentialADWINImpl.class,15, 51, 60, 70000, 40000); ///serial

        }
    }



    @State(Scope.Benchmark)
    public static class DDMExecutionPlan{
        public AbstractChangeDetector classifier;
        public  double[] data;
        //@Param({"Inc1554", "Inc202","Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283" , "Ab162", "Ab9", "Steady"})
        @Param({"Sine1"})
        String filename;

       /* @Param({"10","30","100"})
        int minInstances;

        @Param({"1","2","4"})
        double warningLevel;

        @Param({"1","2.5","5"})
        double outcontrolLevel;
*/
        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new DDM();
            if(filename.equals("Sine1") || filename.equals("Circles")) {
                System.out.println("HoLA");
               // data = arffDataReader(filename);
            }
            else
            data = dataReader(filename);
          //  ((DDM) classifier).minNumInstancesOption.setValue(minInstances);
           // ((DDM) classifier).warningLevelOption.setValue(warningLevel);
           // ((DDM) classifier).outcontrolLevelOption.setValue(outcontrolLevel);
        }
    }


    @State(Scope.Benchmark)
    public static class EDDMExecutionPlan{
        public AbstractChangeDetector classifier;
        public  double[] data;
        @Param({"Inc1554", "Inc202","Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283" , "Ab162", "Ab9", "Steady"})
        String filename;
        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new EDDM();
            data = dataReader(filename);

        }
    }

    @State(Scope.Benchmark)
    public static class RDDMExecutionPlan{
        public AbstractChangeDetector classifier;
        public  double[] data;
        @Param({"Inc1554", "Inc202","Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283" , "Ab162", "Ab9", "Steady"})
        String filename;

        @Param({"1", "2.5", "5"})
        double driftlevel;

      /*  @Param({"10","129","300"})
        int minInstances;

        @Param({"1","2","4"})
        double warningLevel;

        @Param({"1000","40000","80000"})
        int maxSizeConcept ;

        @Param({"3000","7000","20000"})
        int minSizeStableConcept;*/

        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new RDDM();
            data = dataReader(filename);
            ((RDDM) classifier).driftLevelOption.setValue(driftlevel);
            //((RDDM) classifier).minNumInstancesOption.setValue(minInstances);
           // ((RDDM) classifier).warningLevelOption.setValue(warningLevel);
           //((RDDM) classifier).maxSizeConceptOption.setValue(maxSizeConcept);
            //((RDDM) classifier).minSizeStableConceptOption.setValue(minSizeStableConcept);
        }
    }


    @State(Scope.Benchmark)
    public static class STEPDExecutionPlan {
        public AbstractChangeDetector classifier;
        public double[] data;
        @Param({"Inc1554", "Inc202", "Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283", "Ab162", "Ab9", "Steady"})
        String filename;

        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new STEPD();
            data = dataReader(filename);
        }
    }

    @State(Scope.Benchmark)
    public static class SEEDExecutionPlan {
        public AbstractChangeDetector classifier;
        public double[] data;
        @Param({"Inc1554", "Inc202", "Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283", "Ab162", "Ab9", "Steady"})
        String filename;

        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new SEEDChangeDetector();
            data = dataReader(filename);
        }
    }


    @State(Scope.Benchmark)
    public static class SeqDrift2ExecutionPlan {
        public AbstractChangeDetector classifier;
        public double[] data;
        @Param({"Inc1554", "Inc202", "Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283", "Ab162", "Ab9", "Steady"})
        String filename;

        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new SeqDrift2ChangeDetector();
            data = dataReader(filename);
        }
    }



    @State(Scope.Benchmark)
    public static class baselearnerExecutionPlan implements CapabilitiesHandler {
        public DriftDetectionMethodClassifier detectionClassifier;
        public AbstractChangeDetector classifier;
        //public AbstractClassifier baselearner;
        public double[] data;
        Learner learner;
        List<Instance>instancesList;
       // @Param({"Inc1554", "Inc202", "Inc3", "Grad1738", "Grad273", "Grad10", "Ab1283", "Ab162", "Ab9", "Steady"})
       @Param({"Sine1","elec"})
        String filename;

        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            detectionClassifier  = new DriftDetectionMethodClassifier();
            detectionClassifier.driftDetectionMethodOption.setValueViaCLIString("ADWINChangeDetector");


           // baseLearnerOption
             //learnerOption.setValueViaCLIString("");
            detectionClassifier.baseLearnerOption.setValueViaCLIString("moa.classifiers.bayes.NaiveBayes");
           // //detectionClassifier.resetLearningImpl();
           // OptionsHandler q = new OptionsHandler(learnerOption,"");

            // learner = (Learner) q.getPreparedClassOption(this.learnerOption);
            System.out.println(learner);
            //classifier = new SeqDrift2ChangeDetector();
            //baselearner = new NaiveBayes();
            //baselearner.resetLearning();
            detectionClassifier.prepareForUse();
            instancesList = arffDataReader(filename);
            System.out.println(instancesList.get(1));

        }
    }


/*
    @State(Scope.Benchmark)
    public static class SDDMExecutionPlan{
        public AbstractChangeDetector classifier;
        public  double[] data;


        @Setup(Level.Invocation)
        public void setUp() throws Exception {
            classifier = new ADWINPlusChangeDetector();
            classifier.resetLearning();
            //classifier = new ADWINPlusPlusWrapper(1, SequentialADWINImpl.class,15, 51, 60, 70000, 40000); ///serial
         //   dataInstances.clear();
            String fileName =// "E:\\offline Thesis work\\originaladwin++\\datasets\\Incremental datasets\\1554_drifts_dataset.txt";
                    // "E:\\offline Thesis work\\originaladwin++\\datasets\\Gradual datasets\\273_drifts_dataset.txt";
                    "E:\\offline Thesis work\\originaladwin++\\datasets\\Gradual datasets\\10_drifts_dataset.txt";
            File file = new File(fileName);
            Scanner sc = new Scanner(file);
            int i = 0;
            data = new double[(int) file.length()];
            while (sc.hasNextLine()) {
                data[i] = Double.parseDouble(sc.nextLine());
                i++;
            }

        }
    }*/
}

