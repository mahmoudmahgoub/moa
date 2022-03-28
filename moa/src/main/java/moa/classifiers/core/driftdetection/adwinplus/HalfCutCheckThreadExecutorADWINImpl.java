package moa.classifiers.core.driftdetection.adwinplus;

import moa.classifiers.core.driftdetection.adwinplusutils.Bucket;
import moa.classifiers.core.driftdetection.adwinplusutils.Histogram;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This implementation of ADWIN contains the implementation of the HalfCutCheck approach.
 * It uses a ExecuterService to create new Threads.
 */
public class HalfCutCheckThreadExecutorADWINImpl extends ADWINPlusPlus {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private int dropNum;
    Bucket[] removedBuckets;
    private int num;


    public HalfCutCheckThreadExecutorADWINImpl(double delta, int safe_lim, int min_lim, int max_lim, int theta, int omega) {
        super(delta, safe_lim, min_lim, max_lim, theta, omega);
    }

    @Override
    public boolean execute(Histogram histogram) throws  Exception {

        boolean tryToFindCut = true;
        boolean cutFound = false;
        while (tryToFindCut) {
            tryToFindCut = false;
            // create forward thread
            final CompletableFuture<Boolean> checkForwardHalf = CompletableFuture. supplyAsync( () ->
                    (checkHistogramForCut(histogram, histogram.forwardBucketIterable(), histogram.getNumBuckets()/2)), executor);


            // use main thread as backward thread
            final boolean checkBackwardHalf = checkHistogramForCut(histogram, histogram.reverseBucketIterable(), (histogram.getNumBuckets()-1)/2);

            if (checkForwardHalf.get() || checkBackwardHalf) {
                removedBuckets = histogram.removeBucketsCheck(1);
                histogram.removeBuckets(removedBuckets);

                tryToFindCut = true;
                cutFound = true;
            }
        }
        if (cutFound)
            num = omega;
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
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////




    public void terminate() {
        this.executor.shutdownNow();
    }

}
