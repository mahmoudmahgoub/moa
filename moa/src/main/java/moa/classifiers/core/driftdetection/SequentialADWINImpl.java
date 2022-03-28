package moa.classifiers.core.driftdetection;


//import de.tub.bdapro.adwin.core.histogram.Bucket;
//import de.tub.bdapro.adwin.core.histogram.Histogram;


import moa.classifiers.core.driftdetection.adwinplus.ADWINPlusPlus;
import moa.classifiers.core.driftdetection.adwinplusutils.Bucket;
import moa.classifiers.core.driftdetection.adwinplusutils.Histogram;

/**
 * This is the serial implementation of ADWIN.
 * It basically executes a full cut detection in the main thread.
 */
public class SequentialADWINImpl extends ADWINPlusPlus {


    private int dropNum;
    Bucket[] removedBuckets;
    private int num;

    public SequentialADWINImpl(double delta, int safe_lim, int min_lim, int max_lim, int theta, int omega) {
        super(delta, safe_lim, min_lim, max_lim, theta, omega);
    }

    @Override
    public boolean execute(Histogram histogram) {

        boolean tryToFindCut = true;
        boolean cutFound = false;
        while (tryToFindCut) {
            tryToFindCut = false;
            if (checkHistogramForCut(histogram, histogram.reverseBucketIterable(), histogram.getNumBuckets() - 1)) {
                removedBuckets = histogram.removeBucketsCheck(1);
                histogram.removeBuckets(removedBuckets);

                tryToFindCut = true;
                cutFound = true;

            }
        }

        if (cutFound) {
            num = omega;
        }
        return cutFound;
    }



    @Override
    public void drop(Histogram histogram) {

        if (num == 0) {
            if (histogram.getNumBuckets() > safe_lim && histogram.getNumBuckets() > min_lim && histogram.getNumBuckets() < max_lim) {
                double originalVariance = histogram.getVariance();
                removedBuckets = histogram.removeBucketsCheck(1);
                histogram.removeBuckets(removedBuckets);
                if (checkHistogramForCut(histogram, histogram.reverseBucketIterable(), histogram.getNumBuckets() - 1)) {
                    histogram.returnDroppedBuckets(removedBuckets, originalVariance);
                    min_lim += 3;
                    dropNum = 0;
                } else {
                    dropNum++;
                    if (dropNum % theta == 0 && min_lim > safe_lim)
                        min_lim--;
                }
            }
        } else
            num--;

        if (histogram.getNumBuckets() > max_lim) {
            removedBuckets = histogram.removeBucketsCheck(1);
            histogram.removeBuckets(removedBuckets);
        }
    }


    @Override
    public void terminate() {}




}


