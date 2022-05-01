package moa.classifiers.core.driftdetection;


//import de.tub.bdapro.adwin.core.ADWINOld;
import moa.classifiers.core.driftdetection.adwinplus.ADWINPaperOriginal;

/**
 * This is the implementation of the {@link ADWINInterface} for the original ADWIN implementation {@link ADWINOld}
 */
public class ADWINPaperOriginalWrapper implements ADWINPlusInterface {


    private final ADWINPaperOriginal adwin;

    private int adwinCount;
    private int numElementsProcessed;



    public ADWINPaperOriginalWrapper(double delta) throws Exception {


        this.adwin = new ADWINPaperOriginal(delta);
    }

    @Override
    public boolean addElement(final double element) throws Exception {
        this.numElementsProcessed++;
        adwinCount++;
        return this.adwin.setInput(element);
    }

    @Override
    public void adaptiveDrop() {
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



    @Override
    public int getAdwinCount() {
        return adwinCount;
    }

    @Override
    public int resetAdwinCount() {
        return adwinCount = 0;
    }

    @Override
    public int getNumElementsProcessed() {
        return this.numElementsProcessed;
    }

    @Override
    public void terminateAdwin() { return; }

    public int getSize() {
        //return 0;
        return this.adwin.getWidth();
    }


}
