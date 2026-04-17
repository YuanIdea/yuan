package com.gly.quick;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Dataset;
import com.gly.model.train.Train;
import com.gly.io.json.Json;

public class Quick {
    public static void train(String metadataPath) {
        try (NDManager manager = NDManager.newBaseManager("PyTorch")) {
            Json json = new Json(metadataPath);
            Json training = json.getSubJson("training");
            int batchSize = training.getInt("batchSize");
            Dataset trainDs = getQuadraticDataset(manager, 5000, batchSize);
            Dataset validDs = getQuadraticDataset(manager, 500, batchSize);
            Train train = new Train();
            train.init("", "metadata.json", null);
            train.trainAndSaveModel(metadataPath, trainDs, validDs);
        }
    }

    private static Dataset getQuadraticDataset(NDManager manager, int numExamples, int batchSize) {
        NDArray X = manager.randomUniform(-1, 1, new Shape(numExamples, 2));

        NDArray x1 = X.get(":, 0");
        NDArray x2 = X.get(":, 1");
        NDArray y = x1.pow(2).add(x2.pow(2)).reshape(new Shape(numExamples, 1));

        return new ArrayDataset.Builder()
                .setData(X)          // 特征
                .optLabels(y)        // 标签
                .setSampling(batchSize, true)  // 批次大小 32，是否随机打乱
                .build();
    }
}
