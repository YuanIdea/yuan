package com.gly;

import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;

import java.io.IOException;

public class MnistData {
    public RandomAccessDataset trainDataset;
    public RandomAccessDataset testDataset;

    /**
     * Loads the MNIST dataset, optionally reshaping the image data to a target shape.
     *
     * @param engine      the deep learning engine (e.g., "PyTorch")
     * @param batchSize   the batch size
     * @param targetShape the target shape (without the batch dimension)
     */
    public void loadData(String engine, int batchSize, Shape targetShape) {
        try {
            trainDataset = getDataset(Dataset.Usage.TRAIN, batchSize, engine, targetShape);
            testDataset = getDataset(Dataset.Usage.TEST, batchSize, engine, targetShape);
        } catch (Exception e) {
            System.err.println("Failed to load dataset: " + e.getMessage());
        }
    }

    private static RandomAccessDataset getDataset(Dataset.Usage usage, int batchSize,
                                                  String engine, Shape targetShape)
            throws IOException {
        // Build the data preprocessing pipeline
        Pipeline pipeline = new Pipeline();
        if (targetShape != null) {
            // Add a custom transformation: reshape the image to the target shape
            pipeline.add((NDArray array) -> {
                Shape shape = ModelBuilder.concatWithBatchSize(array.getShape().get(0), targetShape);
                return array.reshape(shape);
            });
        }

        Mnist mnist = Mnist.builder()
                .optUsage(usage)
                .optManager(NDManager.newBaseManager(engine))
                .setSampling(batchSize, true)
                .optLimit(Long.MAX_VALUE)
                .optPipeline(pipeline) // Apply the preprocessing pipeline
                .build();

        mnist.prepare(new ProgressBar());
        return mnist;
    }
}