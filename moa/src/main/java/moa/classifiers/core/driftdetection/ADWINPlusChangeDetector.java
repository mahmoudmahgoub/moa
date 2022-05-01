/*
 *    ADWINChangeDetector.java
 *    Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
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
import com.github.javacliparser.MultiChoiceOption;
import moa.core.ObjectRepository;
import moa.tasks.TaskMonitor;

/**
 * Drift detection method based in ADWIN. ADaptive sliding WINdow is a change
 * detector and estimator. It keeps a variable-length window of recently seen
 * items, with the property that the window has the maximal length statistically
 * consistent with the hypothesis "there has been no change in the average value
 * inside the window".
 *
 *
 * @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 * @version $Revision: 7 $
 */
public class ADWINPlusChangeDetector extends AbstractChangeDetector {

    protected ADWINPlusInterface adwin;
    int countDrifts;

    public FloatOption deltaAdwinOption = new FloatOption("deltaAdwin", 'a',
            "Delta of Adwin change detection", 0.002, 0.0, 1.0);

    /*public MultiChoiceOption ADWINPlusAlgorithm = new MultiChoiceOption("ADWINPlusAlgorithm", 'A',
            "choose which algorithm to use", new java.lang.String[]{
            "PlusPlus","Old" , "SnapShot"},
            new String[]{"PlusPlus: tdo",
                    "Old: todo",
                    "SnapShot: todo"}, //todo // and and enable enable ADWINoldWrapper and SnapshotThreadExecutorADWINWrapper
            0);*/ //todo maybe I have to use something likethat
                // public ClassOption driftDetectionMethodOption = new ClassOption("driftDetectionMethod", 'd',
                //"Drift detection method to use.", ChangeDetector.class, "DDM");

    //todo I only can choose from ADWINPlusPLus implementations(ADWIN wrapper in Hassan code)
     public MultiChoiceOption ADWINPlusPlusImplementation = new MultiChoiceOption("Implementation", 'I',
            "choose which Implementation to use", new java.lang.String[]{ "Serial",
            "HalfCut"},
            new String[]{"Serial: SequentialADWINImpl",
                    "HalfCut: HalfCutCheckThreadExecutorADWINImpl"},
            0);

    public ADWINPlusChangeDetector() {
        resetLearning();
    }
    @Override
    public void input(double inputValue) {
       /* if (this.isChangeDetected == true || this.isInitialized == false) {
            //resetLearning();
            this.isInitialized = true;
        }*/
        /*if (this.adwin == null) {
            System.out.println("reset");
            resetLearning();
        }*/
        this.isChangeDetected = false;
        try {
            if(adwin.addElement(inputValue)){
                this.isChangeDetected = true;
                countDrifts++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
      //  if(countDrifts==273)
        //    System.out.println("hola"+countDrifts);
        adwin.adaptiveDrop();
       /* double ErrEstim = this.adwin.getEstimation();
        if(adwin.setInput(inputValue)) {
            if (this.adwin.getEstimation() > ErrEstim) {
                this.isChangeDetected = true;
            }
        }
        this.isWarningZone = false;
        this.delay = 0.0;
        this.estimation = adwin.getEstimation();*/
    }



    @Override
    public void resetLearning() {
        countDrifts = 0;

       try {
            adwin = new ADWINPlusPlusWrapper(1, SequentialADWINImpl.class,15, 51, 60, 70000, 40000); //serial
          //  adwin =  new ADWINPaperOriginalWrapper(1); //old
        } catch (Exception e) {
            e.printStackTrace(); //todo
        }
        // adwin = new ADWIN((double) this.deltaAdwinOption.getValue());
        super.resetLearning();
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
}
