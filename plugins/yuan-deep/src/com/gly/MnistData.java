package com.gly;

import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.ndarray.NDManager;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.util.ProgressBar;

import java.io.IOException;

public class MnistData {
    public RandomAccessDataset trainDataset;
    public RandomAccessDataset testDataset;

    public void loadData(int batchSize, String engine) {
        try {
            // Load the dataset
            trainDataset = getDataset(Dataset.Usage.TRAIN, batchSize, engine);
            testDataset = getDataset(Dataset.Usage.TEST, batchSize, engine);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static RandomAccessDataset getDataset(Dataset.Usage usage, int batchSize, String engine)
            throws IOException {
        Mnist mnist =
                Mnist.builder()
                        .optUsage(usage)
                        .optManager(NDManager.newBaseManager(engine))
                        .setSampling(batchSize, true)
                        .optLimit(Long.MAX_VALUE)
                        .build();
        mnist.prepare(new ProgressBar());
        return mnist;
    }
}
