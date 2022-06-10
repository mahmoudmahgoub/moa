package moa.classifiers.core.driftdetection.adwinplusutils;

import java.util.Arrays;

/**
 * This {@link BucketContainer} data structure is used in the Histogram and represents an array buckets.
 */
public class BucketContainer {

    private BucketContainer prev;
    private BucketContainer next;

    private final int maxBuckets;
    private final Bucket[] buckets;
    private int firstBucket;
    private final int numElementsPerBucket;
    private int numBuckets;

    BucketContainer(final BucketContainer prev, final BucketContainer next, final int maxBuckets, final int elementsPerBucket) {
        this.prev = prev;
        this.next = next;
        this.maxBuckets = maxBuckets;
        this.numElementsPerBucket = elementsPerBucket;
        this.buckets = new Bucket[maxBuckets];
        this.firstBucket = 0;
        this.numBuckets = 0;
    }

    private BucketContainer(final BucketContainer originalContainer) {
        this.maxBuckets = originalContainer.maxBuckets;
        this.buckets = Arrays.copyOf(originalContainer.buckets, originalContainer.buckets.length);
        this.firstBucket = originalContainer.firstBucket;
        this.numElementsPerBucket = originalContainer.numElementsPerBucket;
        this.numBuckets = originalContainer.numBuckets;
    }

    BucketContainer getPrev() {
        return this.prev;
    }

    BucketContainer getNext() {
        return this.next;
    }

    public int getNumBuckets() {
        return this.numBuckets;
    }
    int getFirstBucket(){
        return firstBucket;
    }

    int getNumElementsPerBucket() {
        return this.numElementsPerBucket;
    }

    int getMaxBuckets() {
        return this.maxBuckets;
    }

    void setNext(final BucketContainer next) {
        this.next = next;
    }

    void addBucket(Bucket newBucket) {
        assert newBucket.getNumElements() == numElementsPerBucket;
        buckets[(firstBucket + numBuckets) % maxBuckets] = newBucket;
        numBuckets++;
    }

    void addBucketBack(Bucket newBucket) {
        assert newBucket.getNumElements() == numElementsPerBucket;
        buckets[firstBucket % maxBuckets] = newBucket;
        numBuckets++;
    }

    public Bucket[] removeBuckets(int num) {
        Bucket[] resultBuckets = new Bucket[num];
        if (firstBucket <= ((firstBucket + num) % maxBuckets)) {
            System.arraycopy(buckets, firstBucket, resultBuckets, 0, num);
        } else {
            System.arraycopy(buckets, firstBucket, resultBuckets, 0, maxBuckets - firstBucket);
            System.arraycopy(buckets, 0, resultBuckets, maxBuckets - firstBucket, num - maxBuckets + firstBucket);
        }
        firstBucket = (firstBucket + num) % maxBuckets;
        numBuckets -= num;
        return resultBuckets;
    }

    public void returnFirstBucket (int num) {

        int originalFirstBucket = firstBucket - num;
        if (originalFirstBucket < 0)
            originalFirstBucket += maxBuckets;

        firstBucket = originalFirstBucket;
    }

    Bucket getBucket(int position) {
        return buckets[(firstBucket + (numBuckets - position - 1)) % maxBuckets];
    }


    BucketContainer deepCopy() {
        final BucketContainer copyThis = new BucketContainer(this);
        if(this.next!=null){
            final BucketContainer nextCopy = this.next.deepCopy();
            copyThis.next = nextCopy;
            nextCopy.prev = copyThis;
        }
        return copyThis;
    }


}
