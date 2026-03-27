package com.gly;

import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.util.ProgressBar;

public class MnistData {
    public Mnist trainDataset;
    public Mnist testDataset;

    public void loadData(int batchSize) {
        try {
            // 1. 加载数据集
            trainDataset = Mnist.builder()
                    .optUsage(Dataset.Usage.TRAIN)
                    .setSampling(batchSize, true)
                    .optLimit(Long.MAX_VALUE)
                    .build();
            trainDataset.prepare(new ProgressBar());

            testDataset = Mnist.builder()
                    .optUsage(Dataset.Usage.TEST)
                    .setSampling(batchSize, false)
                    .optLimit(Long.MAX_VALUE)
                    .build();
            testDataset.prepare(new ProgressBar());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
