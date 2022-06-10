package moa.classifiers.core.driftdetection.adwinplusutils;



import java.util.Iterator;

/**
 * Datastructure for the exponential histogram used in ADWIN.
 *
 */
public class Histogram {

    private final int maxBucketsPerBucketContainer;
    private BucketContainer head;
    private BucketContainer tail;

    private int numBucketContainers;
    private int numBuckets;
    private int numElements;
    ////////////////////////////////////////////////
    private int numElementsProcessed;
    //////////////////////////////////////////////


    private double total;
    private double variance;



    /**
     * Creates a new empty Histogram
     * @param maxBucketsPerBucketContainer max number of Buckets in one bucket container. default is 5
     */
    public Histogram(int maxBucketsPerBucketContainer) {
        if (maxBucketsPerBucketContainer < 2) {
            throw new IllegalArgumentException("maxBucketsPerBucketContainer must be at least 2.");
        }

        this.head = new BucketContainer(null, null, maxBucketsPerBucketContainer, 1);
        this.tail = this.head;
        this.numBucketContainers = 1;
        this.maxBucketsPerBucketContainer = maxBucketsPerBucketContainer;

    }

    /**
     * Private copy constructor. Used in the copy method.
     * @param original
     */
    private Histogram(final Histogram original) {
        this.maxBucketsPerBucketContainer = original.maxBucketsPerBucketContainer;

        this.numBucketContainers = original.numBucketContainers;
        this.numBuckets = original.numBuckets;
        this.numElements = original.numElements;

        this.total = original.total;
        this.variance = original.variance;


        this.head = original.head.deepCopy();
        BucketContainer currentBucketContainer = this.head;
        while (currentBucketContainer.getNext() != null){
            currentBucketContainer = currentBucketContainer.getNext();
        }
        // next == null;
        this.tail = currentBucketContainer;
    }

    public void addElement(double element) {
        numElementsProcessed++;
        Bucket newBucket = new Bucket(element, 0, 1);
        head.addBucket(newBucket);
        numBuckets++;
        numElements++;
        if (numElements > 1) {
            variance += (numElements - 1) * Math.pow(element - total / (numElements - 1), 2) / numElements;
        }
        total += element;
        compress();
    }

    private void addEmptyTailBucketContainer() {
        BucketContainer newTail = new BucketContainer(tail, null, maxBucketsPerBucketContainer, tail.getNumElementsPerBucket() * 2);
        tail.setNext(newTail);
        tail = newTail;
        numBucketContainers++;
    }

    private void compress() {
        BucketContainer pointer = head;
        while (pointer != null) {
            if (pointer.getNumBuckets() == pointer.getMaxBuckets()) {
                if (pointer.getNext() == null) {
                    addEmptyTailBucketContainer();
                }
                Bucket[] removedBuckets = pointer.removeBuckets(2);
                Bucket newBucket = compressBuckets(removedBuckets[0], removedBuckets[1]);
                pointer.getNext().addBucket(newBucket);
                numBuckets -= 1;
            }
            pointer = pointer.getNext();
        }
    }

    private Bucket compressBuckets(Bucket firstBucket, Bucket secondBucket) {
        assert firstBucket.getNumElements() == secondBucket.getNumElements();
        int elementsPerBucket = firstBucket.getNumElements();
        double newTotal = firstBucket.getTotal() + secondBucket.getTotal();
        double varianceIncrease = Math.pow(firstBucket.getTotal() - secondBucket.getTotal(), 2) / (2 * elementsPerBucket);
        double newVariance = firstBucket.getVariance() + secondBucket.getVariance() + varianceIncrease;
        return new Bucket(newTotal, newVariance, 2 * elementsPerBucket);
    }

    public Bucket[] removeBucketsCheck(int num) {
        Bucket[] removedBuckets = new Bucket[num];
        int lastPosition = removedBuckets.length;
        Bucket[] addedBuckets;

        while (num > 0) {
            if (num >= tail.getNumBuckets()) {
                num -= tail.getNumBuckets();
                addedBuckets = tail.removeBuckets(tail.getNumBuckets());
                lastPosition -= addedBuckets.length;
                System.arraycopy(addedBuckets, 0, removedBuckets, lastPosition, addedBuckets.length );
                tail = tail.getPrev();
                tail.setNext(null);
                numBucketContainers--;
            } else {
                addedBuckets = tail.removeBuckets(num);
                lastPosition -= addedBuckets.length;
                System.arraycopy(addedBuckets, 0, removedBuckets, lastPosition, addedBuckets.length );
                num = 0;
            }
        }
        return removedBuckets;
    }
    public void removeBuckets(Bucket[] removedBuckets){
        for (Bucket bucket : removedBuckets) {
            numElements -= bucket.getNumElements();
            numBuckets--;
            total -= bucket.getTotal();
            variance -= bucket.getVariance() + bucket.getNumElements() * numElements * Math.pow(bucket.getMean() - total / numElements, 2) / (bucket.getNumElements() + numElements);

        }

    }

    public void returnDroppedBuckets (Bucket[] removedBuckets, double originalVariance) {

            BucketContainer pointer = head;
            while (pointer.getNext() != null)
                pointer = pointer.getNext();
            Bucket bucket = removedBuckets[0];
            if (bucket.getNumElements() == pointer.getNumElementsPerBucket()) {
                pointer.returnFirstBucket(1);
                pointer.addBucketBack(bucket);
            }
            else{
                addEmptyTailBucketContainer();
                pointer = pointer.getNext();
                pointer.addBucket(bucket);

            }
            numElements += bucket.getNumElements();
            numBuckets++;
            total += bucket.getTotal();
            variance = originalVariance;
    }

    public double getTotal() {
        return total;
    }

    public double getVariance() {
        return variance;
    }

    public int getNumElements() {
        return numElements;
    }


    public int getNumElementsProcessed() {
        return numElementsProcessed;
    }


    public BucketContainer returnTail(){
        return tail.deepCopy();
    }

    public Iterable<Bucket> reverseBucketIterable() {
        return new Iterable<Bucket>() {
            @Override
            public Iterator<Bucket> iterator() {
                return new Iterator<Bucket>() {

                    BucketContainer currentBucketContainer = tail;
                    int currentBucketIndex = tail.getNumBuckets();

                    @Override
                    public boolean hasNext() {
                        return (currentBucketIndex > 0) || (currentBucketContainer.getPrev() != null);
                    }

                    @Override
                    public Bucket next() {
                        currentBucketIndex--;
                        if (currentBucketIndex < 0) {
                            currentBucketContainer = currentBucketContainer.getPrev();
                            currentBucketIndex = currentBucketContainer.getNumBuckets() - 1;
                        }
                        return currentBucketContainer.getBucket(currentBucketIndex);
                    }
                };
            }
        };
    }


    public Iterable<Bucket> forwardBucketIterable() {
        return new Iterable<Bucket>() {
            @Override
            public Iterator<Bucket> iterator() {
                return new Iterator<Bucket>() {

                    BucketContainer currentBucketContainer = head;
                    int currentBucketIndex = -1;

                    @Override
                    public boolean hasNext() {
                        return (currentBucketIndex < currentBucketContainer.getNumBuckets() - 1) || (currentBucketContainer.getNext() != null);
                    }

                    @Override
                    public Bucket next() {
                        currentBucketIndex++;
                        if (currentBucketIndex > currentBucketContainer.getNumBuckets() - 1) {
                            currentBucketContainer = currentBucketContainer.getNext();
                            currentBucketIndex = 0;
                        }
                        return currentBucketContainer.getBucket(currentBucketIndex);
                    }
                };
            }
        };
    }

    public int getNumBuckets() {
        return numBuckets;
    }

    /**
     * Creates a deep copy of the histogram.
     * @return new Histogram.
     */
    public Histogram copy() {
        return new Histogram(this);
    }

}
