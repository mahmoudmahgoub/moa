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

import java.lang.reflect.Array;
import java.math.BigDecimal;
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


        DescriptiveStatistics stats = new DescriptiveStatistics();
        stats.setPercentileImpl( new Percentile().
                withEstimationType( Percentile.EstimationType.R_7 ) );
        List < Instance > file_data = new ArrayList<>();
        List < Instance > train;
        List < Instance > test;
        moa.streams.ArffFileStream stream = new ArffFileStream("C:\\Users\\MahmoudMahgoub\\Desktop\\thesis python\\data\\sine1\\sine1_w_50_n_0.1_102.arff", -1);

        while (stream.hasMoreInstances() ) {
             file_data.add(stream.nextInstance().getData());
        }
        sddmObj.setNumClassLabels(file_data.get(0).numClasses());
        sddmObj.setTargetColumn(file_data.get(0).numAttributes()-1);
        int time_step =2,drift =1;
        Reader SDDMReader = new Reader();
        List<result_row[]> z = SDDMReader.npRangeStream(time_step, file_data.size(), drift).map(i -> {
            List<Instance> train2 = file_data.subList(i - time_step, i);
            List<Instance> test2 = file_data.subList(i, i + drift);
            return new result_row[5];
        }).collect(Collectors.toList());
        for(int i:SDDMReader.npRange(time_step,file_data.size(),drift)){ // #time step is window size length drift window slide
            train = file_data.subList(i-time_step,i);
            test = file_data.subList(i,i+drift);
            sddmObj.binsIntervalsBuilder(train);
            System.out.println("test data:"+test);
            List<List<Double>> binnedTrain = sddmObj.binData(train);
            List<List<Double>> binnedTest = sddmObj.binData(test);
            Set<Integer> columns = new HashSet<>(Arrays.asList(0,1));
            sddmObj.getJointShift(binnedTrain,binnedTest,columns);
            sddmObj.getConditionalCovariateDrift(binnedTrain,binnedTest,columns);
            System.out.println("fd");
        }
    }
}