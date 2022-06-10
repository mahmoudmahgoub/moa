package moa.classifiers.core.driftdetection.benchmarking;
import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.core.driftdetection.ADWINPlusInterface;
import moa.classifiers.core.driftdetection.ADWINPlusPlusWrapper;
import moa.classifiers.core.driftdetection.RDDM;
import moa.classifiers.core.driftdetection.adwinplus.SequentialADWINImpl;
import moa.learners.ChangeDetectorLearner;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.tinylog.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * This is the benchmark for the evaluation of ADWIN.
 * We Insert 100 batches with 1 mio. elements with a constant value
 * This Micobenchmark makes use of our {@link DataGenerator},
 * which generates concepts drift according to the benchmark parameters.
 *
 * To execute the benchmark we define the following program parameter:
 *
 * AdwinType = ORIGINAL | SERIAL | HALFCUT | SNAPSHOT
 * ChangeType = ABRUPT | INCREMENTAL | GRADUAL | OUTLIER | CONSTANT
 * BatchSize = the number of elements in one batch
 * NumConstant = the number of constant elements between a concept drift
 * NumChange = the length of a concept drift
 * Delta = the delta parameter of ADWIN
 *
 *
 * For example:
 * Snapshot constant 1000000 10 10 0.001
 *
 */
@State(Scope.Benchmark)
public class Microbenchmark {

    final private static int warmUpIterations = 0;
   final private  static int measurmentsIterations = 1;


                            /* NobaseLearner*/
  // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void adwinMOABenchmarking(final ExecutionPlan.AdwinMOAExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;
       // PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\AdwinMOAdrifts"+plan.filename));
        for(int i2 = 0; i2<plan.data.length; i2++) { //data.length() 2000000
           // System.out.println(plan.data.length);
            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
          //  og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
       // System.out.println("ADWIN MOA no of drifts: "+drift);
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void adwinPlusPlus1Benchmarking(final ExecutionPlan.AdwinPlusPlus1ExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;
       // PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\AdwinPlusPlusdrifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
           // og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
       // System.out.println("ADWIN PlusPlus1 no of drifts: "+drift);
    }

   // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void DDMBenchmarking(final ExecutionPlan.DDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;
       // PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\DDMdrifts"+plan.filename));

        for(int i2 = 0; i2<plan.data.length; i2++) { //plan.data.length
            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
          //  og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
       // System.out.println("DDM No of drifts: "+drift);
    }

   //@Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void EDDMBenchmarking(final ExecutionPlan.EDDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;
      //  PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\EDDMdrifts"+plan.filename));
        for(int i2 = 0; i2 < plan.data.length; i2++) {
            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
         //   og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
        //System.out.println("EDDM No of drifts: "+drift);
    }

   // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void RDDMBenchmarking(final ExecutionPlan.RDDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

     //  PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\RDDMdrifts"+plan.filename));
        for(int i2 = 0; i2 < plan.data.length; i2++) { //plan.data.length
            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
          //  og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
       // System.out.println("RDDM No of drifts: "+drift);
    }


   // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void STEPDBenchmarking(final ExecutionPlan.STEPDExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;
       // PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\STEPDdrifts"+plan.filename));
        for(int i2 = 0; i2<plan.data.length; i2++) { //data.length()
            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
         //   og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
        //System.out.println("STEPD no of drifts: "+drift);
    }


    //@Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void SeqDrift2Benchmarking(final ExecutionPlan.SeqDrift2ExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

      //  PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\SeqDrift2drifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
         //   og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
      //  System.out.println("SeqDrift2 no of drifts: "+drift);
    }

  // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void SeqDrift1Benchmarking(final ExecutionPlan.SeqDrift1ExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

       //  PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\SeqDrift1drifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
            //  og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
        //System.out.println("SeqDrift1 no of drifts: "+drift);
    }

  //  @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void SEEDBenchmarking(final ExecutionPlan.SEEDExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        // PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\SEEDdrifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
         //     og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
       // System.out.println("SEED no of drifts: "+drift);
    }


  //  @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void PageHinkleyDMBenchmarking(final ExecutionPlan.PageHinkleyDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        // PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\PageHinkleyDMdrifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
           //   og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
       // System.out.println("PageHinkleyDM no of drifts: "+drift);
    }

   // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void HDDMABenchmarking(final ExecutionPlan.HDDMAExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        // PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\HDDMAdrifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
          //    og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
     //   System.out.println("HDDMA no of drifts: "+drift);
    }


   // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void HDDMWBenchmarking(final ExecutionPlan.HDDMWExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        // PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\HDDMWdrifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
          //    og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
       // System.out.println("HDDMW no of drifts: "+drift);
    }

  //  @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void GeometricMovingAverageDMBenchmarking(final ExecutionPlan.GeometricMovingAverageDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

      //   PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\GeometricMovingAverageDMdrifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
        //      og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
      //  System.out.println("GeometricMovingAverageDM no of drifts: "+drift);
    }

    //@Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void EWMAChartDMBenchmarking(final ExecutionPlan.EWMAChartDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

       //  PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\EWMAChartDMdrifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
         //     og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
    //    System.out.println("EWMAChartDM no of drifts: "+drift);
    }


   // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void CusumDMBenchmarking(final ExecutionPlan.CusumDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\CusumDMdrifts"+plan.filename));
        for(int i2 = 0; i2 <plan.data.length; i2++) { //data.length()

            plan.classifier.input(plan.data[i2]);
            boolean detected_drift = plan.classifier.getChange();
        //      og.println(detected_drift);
            if (detected_drift) {
                drift += 1;
            }
        }
        blackHole.consume(drift);
    //    System.out.println("CusumDM no of drifts: "+drift);
    }

    //@Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void baselearnerAdwinMOABenchmarking(final ExecutionPlan.BL_AdwinMOAExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        for(int i2 = 0; i2 < plan.instancesList.size(); i2++) { //data.length()
            plan.baseLearnerClassifier.trainOnInstanceImpl(plan.instancesList.get(i2));
            if (plan.baseLearnerClassifier.isChangeDetected()) {
                drift += 1;
            }
            //blackHole.consume(drift);
        }
        System.out.println("BL ADWIN MOA no of drifts: "+drift);
    }

    ////@benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void baselearnerAdwinPlusPlus1Benchmarking(final ExecutionPlan.BL_AdwinPlusPlus1ExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        for(int i2 = 0; i2 < plan.instancesList.size(); i2++) { //data.length()
            plan.baseLearnerClassifier.trainOnInstanceImpl(plan.instancesList.get(i2));
            if (plan.baseLearnerClassifier.isChangeDetected()) {
                drift += 1;
            }
            blackHole.consume(drift);
        }
        System.out.println("BL ADWINPlusPlus no of drifts: "+drift);
    }


   // @Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void baselearnerDDMBenchmarking(final ExecutionPlan.BL_DDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        for(int i2 = 0; i2 < plan.instancesList.size(); i2++) { //data.length()
            plan.baseLearnerClassifier.trainOnInstanceImpl(plan.instancesList.get(i2));
            if (plan.baseLearnerClassifier.isChangeDetected()) {
                drift += 1;
            }
            blackHole.consume(drift);
        }
        System.out.println("BL DDM no of drifts: "+drift);
    }

    //@benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void baselearnerEDDMBenchmarking(final ExecutionPlan.BL_EDDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        for(int i2 = 0; i2 < plan.instancesList.size(); i2++) { //data.length()
            plan.baseLearnerClassifier.trainOnInstanceImpl(plan.instancesList.get(i2));
            if (plan.baseLearnerClassifier.isChangeDetected()) {
                drift += 1;
            }
            blackHole.consume(drift);
        }
        System.out.println("BL EDDM no of drifts: "+drift);
    }

    //@Benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void baselearnerRDDMBenchmarking(final ExecutionPlan.BL_RDDMExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        for(int i2 = 0; i2 < plan.instancesList.size(); i2++) { //data.length()
            plan.baseLearnerClassifier.trainOnInstanceImpl(plan.instancesList.get(i2));
            if (plan.baseLearnerClassifier.isChangeDetected()) {
                drift += 1;
            }
            blackHole.consume(drift);
        }
        System.out.println("BL RDDM no of drifts: "+drift);
    }

    //@benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void baselearnerSTEPDBenchmarking(final ExecutionPlan.BL_STEPDExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        for(int i2 = 0; i2 < plan.instancesList.size(); i2++) { //data.length()
            plan.baseLearnerClassifier.trainOnInstanceImpl(plan.instancesList.get(i2));
            if (plan.baseLearnerClassifier.isChangeDetected()) {
                drift += 1;
            }
            blackHole.consume(drift);
        }
        System.out.println("BL STEPD no of drifts: "+drift);
    }

    //@benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void baselearnerSEEDBenchmarking(final ExecutionPlan.BL_SEEDExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        for(int i2 = 0; i2 < plan.instancesList.size(); i2++) { //data.length()
            plan.baseLearnerClassifier.trainOnInstanceImpl(plan.instancesList.get(i2));
            if (plan.baseLearnerClassifier.isChangeDetected()) {
                drift += 1;
            }
            blackHole.consume(drift);
        }
        System.out.println("BL SEED no of drifts: "+drift);
    }

    ////@benchmark
    @Fork(value = 1, warmups = 0)
    @Warmup(iterations = warmUpIterations)
    @Measurement(iterations = measurmentsIterations)
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static void baselearnerSeqDrift2Benchmarking(final ExecutionPlan.BL_SeqDrift2ExecutionPlan plan, Blackhole blackHole) throws Exception {
        int drift = 0;

        for(int i2 = 0; i2 < plan.instancesList.size(); i2++) { //data.length()
            plan.baseLearnerClassifier.trainOnInstanceImpl(plan.instancesList.get(i2));
            if (plan.baseLearnerClassifier.isChangeDetected()) {
                drift += 1;
            }
            blackHole.consume(drift);
        }
        System.out.println("BL SeqDrift no of drifts : "+drift);
    }












   // //@benchmark()
    public boolean benchmarkAdwin(double elem) throws Exception {

       // System.out.println("Item number "+numInvocations);
        //System.out.println("Input data is "+data[numInvocations]); //todo uncomment

       // if (numInvocations % 100 ==0) {
           // System.out.println("Current window size is "+adwin.getSize());/todo uncomment
            /* /todo uncomment
            File file =new File("C:/Users/MahmoudMahgoub/Downloads/MOA/out.txt");
            FileWriter fw = new FileWriter(file,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println(adwin.getSize());
            pw.close();*/
      //  }

        //return adwin.addElement(elem);//data[numInvocations++]);
        return true;
    }

//
   // //@benchmark()
   // public void adaptiveWindowDrop(){
   //     adwin.adaptiveDrop();
   // }

   // @TearDown( Level.Iteration )
    public void teardownIteration() {
     /*   System.out.print("<Number of Adwin cut checks performed: " + (adwin.getAdwinCount() - adwinCount) + "> ");
        adwinCount = adwin.getAdwinCount();
        numTotalInvocations += numInvocations;
        if (warmup && numTotalInvocations == warmupIterations * batchSize) {
            adwin.terminateAdwin();
            warmup = false;
            adwinCount = 0;
            numTotalInvocations = 0;
            adwin = null;
          //  dataGenerator = null;
        }*/
    }

   //@TearDown( Level.Trial )
   // public void teardownTrial() {
      //  adwin.terminateAdwin();
   // }


    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        // String input
        String name = sc.nextLine();
        Main.main(args);
     /*   ChangeDetectorLearner learner = new ChangeDetectorLearner();
        RDDM R;
        //R = new RDDM();
        Instance trainInst;

        Main.main(args);

       // Microbenchmark mc = new Microbenchmark();
        File file =
                new File("E:\\offline Thesis work\\originaladwin++\\datasets\\Gradual datasets\\273_drifts_dataset.txt");

       // double[] data;

       // int i3 = 0;
       // data = new double[(int) file.length()];
       /* while (sc.hasNextLine()) {
            data[i3] = Double.parseDouble(sc.nextLine());
            //System.out.println(data[i]);
            //R.input(data[i3]);
            i3++;
        }*/
        int drift=0;
        long start = System.nanoTime();
       // mc.data=data;
        //mc.setupIteration();
       // for(int i2=0;i2<data.length;i2++){
;
           // method.accept(object);
           // R.input(data[i2]);
         //   boolean flag = mc.benchmarkAdwin();
            //System.out.println(R.getChange());
           // if(R.getChange()){
          //  if(flag) {
            //    drift+=1;
           // }
//vm options: -Dcom.sun.management.jmxremote.port=6789 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false //todo
            //
      //  }
        /*
        long end = System.nanoTime();
        Logger.info("classification runtime: {} millis", (end - start) / 1_000_000.0);
        System.out.println("Number of drifts is: " + drift);
        */
       // return;
        /*
        /////////////////
        long startTime = System.currentTimeMillis();
        //addWinRunner();
        Microbenchmark mc = new Microbenchmark();
        //mc.dataGeneratorRunner();
        mc.setupIteration();

        boolean flag;
        int drift=0;
        PrintWriter og = new PrintWriter(new FileWriter("file"));
        for (int i = 1; i< 2000000; i++)
        {
            flag = mc.benchmarkAdwin();
            System.out.println(flag);
            og.println(flag);
            og.println(adwin.getMinLimit());

            if(flag){
                drift+=1;
            }
            mc.adaptiveWindowDrop();
            //System.out.println(adwin.getMinLimit());
            //System.out.println(adwin.minWindowMovingRate());
            //System.out.println(adwin.waitingAfterDriftElements());
        }
        og.close();
        mc.teardownTrial();
        System.out.println();
        System.out.println("Number of drifts is: " + drift);
        //mc.Generator();
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((endTime - startTime) / 1000d) + " seconds");*/
    }


   /* private  double[] dataGeneratorRunner() throws IOException {
        this.dataGenerator = new DataGenerator(100000, DataGenerator.Type.ABRUPT,5000,DataGenerator.VarianceType.NOVARIANCE);
        double r[] = new double[2000000];
        PrintWriter pw = new PrintWriter(new FileWriter(""));
        for (int i = 1; i< 2000000; i++) {

            //System.out.println(dataGenerator.getNext());
            r[i] = dataGenerator.getNext();
            pw.println(r[i]);

        }
        pw.close();
        return r;

    }
*/

/*
    private static void addWinRunner() throws RunnerException {
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        String argAdwinType = "SERIAL";
        String argChangeType = "INCREMENTAL";
        String argBatchSize = "3";
        String argNumConstant = "20";
        String argNumChange = "20";
        String argDelta = "1";

        String warmupIterations = "20";
        String measurementIterations = "100";

        Options opt = new OptionsBuilder()
                .include(Microbenchmark.class.getName())
                .mode(Mode.SingleShotTime)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupIterations(Integer.valueOf(warmupIterations))
                .warmupBatchSize(Integer.valueOf(argBatchSize))
                .measurementIterations(Integer.valueOf(measurementIterations))
                .measurementBatchSize(Integer.valueOf(argBatchSize))
                .param("adwinType", argAdwinType)
                .param("delta", argDelta)
                .param("changeType", argChangeType)
                .param("numConstant", argNumConstant)
                .param("numChange", argNumChange)
                .param("warmupIterations", warmupIterations)
                .param("measurementIterations", measurementIterations)
                .param("batchSize", argBatchSize)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


*/


  /*  private ADWINInterface newAdwin() throws Exception {
        switch (adwinType) {
            case ORIGINAL:
                return new ADWINWrapperOriginal(delta);
            case SERIAL:
                return new ADWINPlusPlusWrapper()(delta,  Histogram.class, SequentialADWINImpl.class,safe_lim, min_lim, max_lim, theta, omega);
            case HALFCUT:
                return new ADWINWrapper(delta,  Histogram.class, HalfCutCheckThreadExecutorADWINImpl.class,safe_lim, min_lim, max_lim, theta, omega);
            case SNAPSHOT:
                return new SnapshotThreadExecutorADWINWrapper(delta, Histogram.class, SequentialADWINImpl.class,safe_lim, min_lim, max_lim, theta, omega);
        }
        throw new Exception("Unknown ADWIN type");
    }
*/

    /*private DataGenerator newDatagenerator() {
        return new DataGenerator(numConstant, changeType, numChange, DataGenerator.VarianceType.NOVARIANCE);
    }*/
    //private static ADWINPlusInterface adwin;
   /* private int adwinCount;


    // private DataGenerator dataGenerator;

    private double[] data;

    private int numInvocations;

    private int numTotalInvocations;

    private boolean warmup;
*/
    /*
        // Dummy parameter values, because JMH requires default parameter values.
        // The actual parameter values are set in the main method.
        @Param({"SNAPSHOT"})
        public AdwinType adwinType;
        @Param({"0.01"})
        public double delta;
       // @Param({"INCREMENTAL"})
      //  public DataGenerator.Type changeType;
        @Param({"1000"})
        public int numConstant;

        @Param({"200"})
        public int numChange;

        @Param({"20"})
        public int warmupIterations;

        @Param({"100"})
        public int measurementIterations;

        @Param({"100000"})
        public int batchSize;

        @Param({"10000"})
        public int min_lim;

        @Param({"10000"})
        public int safe_lim;

        @Param({"2000"})
        public int max_lim;

        @Param({"5"})
        public int theta;

        @Param({"5"})
        public int omega;

    */
  /*  @Setup( Level.Trial)
    public void setupTrial() throws Exception {
        data = new double[1000];//batchSize];
        warmup = true;
        adwinCount = 0;
        numTotalInvocations = 0;
    }

    @Setup( Level.Iteration )
    public void setupIteration() throws Exception {
        numInvocations = 0;

        //adwin =  new ADWINWrapperOriginal(1); //old

        //adwin = new ADWINPlusPlusWrapper(); //serial
        adwin = new ADWINPlusPlusWrapper(1, SequentialADWINImpl.class,15, 51, 60, 70000, 40000); //serial

        //adwin = new ADWINWrapper(1, Histogram.class, HalfCutCheckThreadExecutorADWINImpl.class,5, 15, 20, 10, 0); //halfcut

         //adwin = new SnapshotThreadExecutorADWINWrapper(1, Histogram.class, SequentialADWINImpl.class, 5, 20, 30, 10, 0); //snapshot (optimisitc adwin)
    }
*/
   // public enum AdwinType { ORIGINAL, SERIAL, HALFCUT, SNAPSHOT }
}
