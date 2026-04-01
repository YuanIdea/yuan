package com.gly;

import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.Record;
import ai.djl.util.Progress;

import java.io.IOException;

/**
 * A wrapper around MNIST that returns one-hot encoded labels.
 * Extends RandomAccessDataset and only needs to implement:
 * - prepare()
 * - get()
 * - availableSize()
 * <p>
 * Based on DJL official CSVDataset example:
 * <a href="https://docs.djl.ai/master/docs/demos/malicious-url-detector/docs/dataset_creation.html">...</a>
 */
public class MnistOneHot extends RandomAccessDataset {
    private final Mnist base;           // Underlying MNIST dataset
    private final int numClasses;       // Number of classes for one-hot encoding

    private MnistOneHot(Builder builder) {
        super(builder);  // Must call super constructor with builder
        this.numClasses = builder.numClasses;
        this.pipeline = builder.pipeline;
        this.base = Mnist.builder()
                .optUsage(builder.usage)
                .optManager(builder.manager)
                .setSampling(builder.batchSize, builder.sampling)
                .optLimit(builder.limit)
                .optPipeline(null)
                .build();
        try {
            this.base.prepare(new ProgressBar());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Prepares the dataset. Must be implemented; delegates to the underlying dataset.
     */
    @Override
    public void prepare(Progress progress) throws IOException {
        base.prepare(progress);
    }

    /**
     * Core method: returns a single sample by index.
     * Retrieves the original Record from the underlying dataset and converts
     * the integer label to one-hot encoding.
     */
    @Override
    public Record get(NDManager manager, long index) {
        Record original = base.get(manager, index);
        NDList labels = original.getLabels();
        NDArray labelArray = labels.singletonOrThrow();
        NDArray oneHot = labelArray.oneHot(numClasses);
        return new Record(original.getData(), new NDList(oneHot));
    }

    /**
     * Returns the size of the dataset. Must be implemented.
     */
    @Override
    protected long availableSize() {
        return base.size();
    }

    // ==================== Builder pattern ====================

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends BaseBuilder<Builder> {
        private int numClasses;
        private Pipeline pipeline;
        private NDManager manager;
        private int batchSize;
        private boolean sampling;
        private long limit;
        private Dataset.Usage usage;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder optNumClasses(int numClasses) {
            this.numClasses = numClasses;
            return this;
        }

        public Builder optPipeline(Pipeline pipeline) {
            this.pipeline = pipeline;
            return this;
        }

        public Builder optManager(NDManager manager) {
            this.manager = manager;
            return this;
        }

        public Builder optUsage(Dataset.Usage usage) {
            this.usage = usage;
            return this;
        }

        // Override setSampling to call super and store parameters
        @Override
        public Builder setSampling(int batchSize, boolean random) {
            super.setSampling(batchSize, random);
            this.batchSize = batchSize;
            this.sampling = random;
            return this;
        }

        public Builder optLimit(long limit) {
            this.limit = limit;
            return this;
        }

        public MnistOneHot build() {
            if (manager == null) {
                manager = NDManager.newBaseManager();
            }
            // Ensure sampler is set; if not, set with default values
            if (getSampler() == null) {
                setSampling(batchSize, sampling);
            }
            return new MnistOneHot(this);
        }
    }
}