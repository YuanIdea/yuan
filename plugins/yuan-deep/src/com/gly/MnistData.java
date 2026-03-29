package com.gly;

import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.ndarray.NDManager;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.util.ProgressBar;

public class MnistData {
    public Mnist trainDataset;
    public Mnist testDataset;

    public void loadData(int batchSize, String engine) {
        try {
            // 1. 加载数据集
            trainDataset = Mnist.builder()
                    .optUsage(Dataset.Usage.TRAIN)
                    .optManager(NDManager.newBaseManager(engine))
                    .setSampling(batchSize, true)
                    .optLimit(Long.MAX_VALUE)
                    .build();
            trainDataset.prepare(new ProgressBar());

            testDataset = Mnist.builder()
                    .optUsage(Dataset.Usage.TEST)
                    .optManager(NDManager.newBaseManager(engine))
                    .setSampling(batchSize, false)
                    .optLimit(Long.MAX_VALUE)
                    .build();
            testDataset.prepare(new ProgressBar());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
