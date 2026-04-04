package com.gly.mnist;

import ai.djl.ndarray.types.Shape;
import com.gly.ModelBuilder;
import com.gly.Train;
import com.gly.io.json.Json;

public class Mnist {
    public static void train(String modelPath) {
        Train train = new Train();
        MnistData mnistData = getMnistData(modelPath);
        train.trainAndSaveModel(modelPath, mnistData.trainDataset, mnistData.testDataset);
    }

    /**
     * Generate the MNIST dataset.
     *
     * @param metadataPath modelPath Path to the model directory.
     * @return MNIST dataset.
     */
    public static MnistData getMnistData(String metadataPath) {
        Json json = new Json(metadataPath);
        Json training = json.getSubJson("training");
        int batchSize = training.getInt("batchSize");
        Json sequence = json.getSubJson("modelConfig");
        String engine = "PyTorch";
        if (sequence.has("engine")) {
            engine = sequence.getString("engine");
        }
        Shape inputShape = ModelBuilder.parseShape(json.getJsonNode("modelConfig").get("inputShape"));
        MnistData mnistData = new MnistData();
        mnistData.loadData(engine, batchSize, inputShape, 10, true);
        return mnistData;
    }
}
