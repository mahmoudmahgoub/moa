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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;


/**
 * Drift detection method based in SDDM method of <todo add name of author></todo>
 *
 * //todo change this description
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

    public FloatOption driftThresholdOption = new FloatOption("driftThreshold",
            'o', "threshold to accept drift", 0.6);

    public IntOption instanceLimitOption = new IntOption("streamsize", 'i',
            "Maximum number of instances to test/train on  (-1 = no limit).",
            100000000, -1, Integer.MAX_VALUE);

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



   private static int fileReadFlag = 0;
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
    private double driftThreshold = 0.6;
    public ArrayList<Instance> window;
    private  SDDMAlgo sddmObjGui;
    private List < Instance > fileDataGui = new ArrayList<>();
    int maxInstances = 1000;
    int  buffer_size = 960; //drift+time_step 550
    //int predictionOption = this.leafpredictionOption.getChosenIndex();
    int noDriftsGui = 0;

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
        driftThreshold = this.driftThresholdOption.getValue();
        maxInstances = this.instanceLimitOption.getValue();
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
         System.out.print("HELLLO");
    }

    @Override
    public void input(Instance inst) {
       // System.out.println("start");
        fileReadFlag++;
        if (this.isChangeDetected == true || this.isInitialized == false) {
            resetLearning();
            System.out.println("init");
            this.isInitialized = true;
        }
        //int time_step =500,drift =50;
        this.isChangeDetected = false;
        //for(int i = time_step;i<file_data.size();i+=drift){
        // covariatecolumns = cols without target col
        //if(fileReadFlag<maxInstances) {
            if(fileReadFlag<buffer_size) {
            fileDataGui.add(inst);
        }
        else if(fileReadFlag == buffer_size) {
            fileDataGui.add(inst);

            sddmObjGui = new SDDMAlgo(5, 0);
            List<Instance> train;
            List<Instance> test;
            sddmObjGui.setNumClassLabels(fileDataGui.get(0).numClasses());
            sddmObjGui.setTargetColumn(fileDataGui.get(0).numAttributes() - 1);
            int time_step = 900, drift = 60;
            Reader SDDMReader = new Reader();

            System.out.println("Hello Taa7");


            //this.isChangeDetected = false;
            List<Map<String, Double>> results = new ArrayList<>();

            //for (int i : SDDMReader.npRange(time_step, fileDataGui.size(), drift)) { // #time step is window size length drift window slide
                //train = fileDataGui.subList(i - time_step, i);
               // int EndOfArr = Math.min(fileDataGui.size(), i + drift);  // for the final step to avoid out of index
               // test = fileDataGui.subList(i, EndOfArr);
                train = fileDataGui.subList(0, time_step);
                test = fileDataGui.subList(time_step,time_step+drift);
                sddmObjGui.binsIntervalsBuilder(train);
                List<List<Double>> binnedTrain = null;
                try {
                    binnedTrain = sddmObjGui.binData(train);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<List<Double>> binnedTest = null;
                try {
                    binnedTest = sddmObjGui.binData(test);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Set<Integer> covariateColumns = new HashSet<>(Arrays.asList(0, 1));
                Map<String, Double> res = sddmObjGui.getJointResults(binnedTrain, binnedTest, covariateColumns);
             //   System.out.println("Drift" + i+ " "+results.size());
                if (res.get("conceptdrift") > .04) {//.3conceptdriftclassshift
                    //      System.out.println("Drift" + i+ " "+results.size());
                    this.isChangeDetected = true;
                    noDriftsGui++;
                    //og.println(true);
                } else {
                    //og.println(false);
                }
                results.add(res);
                // sddmObj.getJointShift(binnedTrain,binnedTest,covariateColumns);
                //sddmObj.getConditionalCovariateDrift(binnedTrain,binnedTest,covariateColumns);
                //sddmObj.getPosteriorDrift(binnedTrain,binnedTest,covariateColumns);
            //}
                fileReadFlag = 0;
                fileDataGui.clear();
            System.out.println("SDDM drifts:" + noDriftsGui);

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



    static class result_row{
    String col;
        double covariate_shift;
        double class_shift;
        double concept_drift;
        double posterior_drift;
        double conditional_covariate_distribution;
    }


    public static void main(String[] args) throws Exception {
        SDDMAlgo sddmObj = new SDDMAlgo(5, 0);

       // covariatecolumns = cols without target col

        List < Instance > file_data = new ArrayList<>();
        List < Instance > train;
        List < Instance > test;
        //moa.streams.ArffFileStream stream = new ArffFileStream("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\sine1\\sine1_w_50_n_0.1_102.arff", -1);
        //moa.streams.ArffFileStream stream = new ArffFileStream("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\sine full\\sine1_w_50_n_0.1_101.arff", -1);
        //moa.streams.ArffFileStream stream = new ArffFileStream("E:\\offline Thesis work\\originaladwin++\\datasets\\sine1_w_50_n_0.1_101.arff",-1);
        //moa.streams.ArffFileStream stream = new ArffFileStream("E:\\offline Thesis work\\originaladwin++\\datasets\\Incremental datasets\\1554_drifts_dataset.arff",-1);
        //moa.streams.ArffFileStream stream = new ArffFileStream("E:\\offline Thesis work\\originaladwin++\\datasets\\Gradual datasets\\1738_drifts_dataset.arff",-1);
        //moa.streams.ArffFileStream stream = new ArffFileStream("E:\\offline Thesis work\\originaladwin++\\datasets\\Abrupt datasets\\1283_drifts_dataset.arff",-1);
        //moa.streams.ArffFileStream stream = new ArffFileStream("E:\\offline Thesis work\\originaladwin++\\datasets\\Gradual datasets\\10_drifts_dataset.arff",-1);
       // moa.streams.ArffFileStream stream = new ArffFileStream("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\usp-stream-data\\elec.arff",-1);//
        //moa.streams.ArffFileStream stream = new ArffFileStream("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\usp-stream-data\\airlines.arff",-1);//
       moa.streams.ArffFileStream stream = new ArffFileStream("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\usp-stream-data\\INSECTS-abrupt_balanced_norm.arff",-1);//


        while (stream.hasMoreInstances() ) {
             file_data.add(stream.nextInstance().getData());
        }
        sddmObj.setNumClassLabels(file_data.get(0).numClasses());
        sddmObj.setTargetColumn(file_data.get(0).numAttributes()-1);
        int time_step =500,drift =50;
        Reader SDDMReader = new Reader();
       /* List<result_row[]> z = SDDMReader.npRangeStream(time_step, file_data.size(), drift).map(i -> {
            List<Instance> train2 = file_data.subList(i - time_step, i);
            List<Instance> test2 = file_data.subList(i, i + drift);
            return new result_row[5];
        }).collect(Collectors.toList());*/
        List<Map<String, Double>> results = new ArrayList<>();
        int no_drifts=0;
        //PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\SDDMdrifts"+"elec"));
        //PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\SDDMdrifts"+"airlines"));

        long startTime = System.currentTimeMillis();
        for(int i:SDDMReader.npRange(time_step,file_data.size(),drift)){ // #time step is window size length drift window slide
       // for(int i = time_step;i<file_data.size();i=+drift){
            train = file_data.subList(i-time_step,i);
            int EndOfArr = Math.min(file_data.size(), i+drift);  // for the final step to avoid out of index
            test = file_data.subList(i,EndOfArr);
            sddmObj.binsIntervalsBuilder(train);
            List<List<Double>> binnedTrain = sddmObj.binData(train);
            List<List<Double>> binnedTest = sddmObj.binData(test);
            Set<Integer> covariateColumns = new HashSet<>(Arrays.asList(0,1));
            Map<String, Double> res = sddmObj.getJointResults(binnedTrain,binnedTest,covariateColumns);
            //System.out.println("Drift" + i+ " "+results.size());
            boolean detected_drift = (res.get("conceptdrift")>.04);
            PrintWriter og = new PrintWriter(new FileWriter("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\results\\SDDMdrifts"+"2"));
            og.println(detected_drift);
            if(detected_drift) {//.3
                //System.out.println("Drift" + i+ " "+results.size());
                no_drifts++;

            }

            results.add(res);
           // sddmObj.getJointShift(binnedTrain,binnedTest,covariateColumns);
            //sddmObj.getConditionalCovariateDrift(binnedTrain,binnedTest,covariateColumns);
            //sddmObj.getPosteriorDrift(binnedTrain,binnedTest,covariateColumns);
        }
        long endTime   = System.currentTimeMillis();
        NumberFormat formatter = new DecimalFormat("#0.00000");
        System.out.print("Execution time is " + formatter.format((endTime - startTime) / 1000d) + " seconds");
        System.out.println("SDDM drifts:"+no_drifts);

    }
}