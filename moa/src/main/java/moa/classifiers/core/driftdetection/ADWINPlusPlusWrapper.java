package moa.classifiers.core.driftdetection;

//import de.tub.bdapro.adwin.core.ADWIN;
//import de.tub.bdapro.adwin.core.histogram.Histogram;
import moa.classifiers.core.driftdetection.adwinplus.ADWINPlusPlus;
import moa.classifiers.core.driftdetection.adwinplusutils.Histogram;
import moa.core.ObjectRepository;
import moa.tasks.TaskMonitor;

import java.lang.reflect.InvocationTargetException;


/**
 * The {@link ADWINPlusPlusWrapper} is used to combine a given {@link Histogram} with a given implementation of {@link ADWINPlusPlus}
 */
public class ADWINPlusPlusWrapper implements ADWINPlusInterface  {


    private  ADWINPlusPlus adwin;
    private  Histogram histogram;
    private int numElementsProcessed;
    private int adwinCount;

    /*
    public ADWINPlusPlusWrapper(double delta, Class<? extends Histogram> histogramClass, Class<? extends ADWINPlus> adwinClass,
                                int safe_lim, int min_lim, int max_lim, int theta, int omega) throws Exception {

        this.histogram = (Histogram) histogramClass.getConstructors()[0].newInstance(6);
        adwin = (ADWINPlus) adwinClass.getConstructors()[0].newInstance(delta, safe_lim, min_lim, max_lim, theta, omega);
        this.numElementsProcessed = 0;
        //adwin.minWindowLimit = minWindowLimit;
    }
*/
    public ADWINPlusPlusWrapper()  {
    }

    public ADWINPlusPlusWrapper (double delta , Class<? extends ADWINPlusPlus> adwinClass,
        int safe_lim, int min_lim, int max_lim, int theta, int omega) throws Exception {
///1,Histogram.class, SequentialADWINImpl.class,15, 51, 60, 70000, 40000

       // int delta = 1,safe_lim = 15, min_lim = 51, max_lim = 60, theta = 70000, omega = 40000;

        this.histogram = (Histogram) Histogram.class.getConstructors()[0].newInstance(6);
        this.numElementsProcessed = 0;
        //adwin.minWindowLimit = minWindowLimit;
        adwin = (ADWINPlusPlus) adwinClass.getConstructors()[0].newInstance(delta, safe_lim, min_lim, max_lim, theta, omega);


    }
    @Override
    public boolean addElement(final double element) throws Exception {
        this.numElementsProcessed++;
        this.histogram.addElement(element);
        adwinCount++;

        return this.adwin.execute(histogram);

    }


    @Override
    public void adaptiveDrop(){ this.adwin.drop(histogram);
    }


    @Override
    public int getAdwinCount() {
        return adwinCount;
    }

    @Override
    public int resetAdwinCount() {
        return adwinCount = 0;
    }

    @Override
    public void terminateAdwin() {
        this.adwin.terminate();
    }


    @Override
    public int getNumElementsProcessed() {
        return this.numElementsProcessed;
    }

    public int getSize() {
        return histogram.getNumBuckets();
        //return histogram.getNumElements();
    }
    @Override
    public int getMinLimit(){
        return this.adwin.min_lim;
    }

    @Override
    public int minWindowMovingRate(){
        return this.adwin.theta;
    }

    @Override
    public int waitingAfterDriftElements(){
        return this.adwin.omega;
    }


   /* @Override
    public void input(double inputValue) {
        //todo
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        //todo
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor, ObjectRepository repository) {
        //todo

    }*/
}
