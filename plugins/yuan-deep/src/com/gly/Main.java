package com.gly;

import ai.djl.ndarray.types.Shape;
import com.gly.io.json.Json;

import java.awt.*;


public class Main {

    public static void main(String[] args) throws Exception {
        String modelName = "mlp";
        //modelName = "cnn";
        //modelName = "lenet5";
        //modelName = "lstm";

        String modelPath = "models/mnist/" + modelName;

        Train train = new Train();
        MnistData mnistData = getMnistData(modelPath);
        train.trainAndSaveModel(modelPath, mnistData.trainDataset, mnistData.testDataset);

        Use.predictWithModel(modelPath, "test-digit.png");
    }

    /**
     * Generate the MNIST dataset.
     * @param modelPath modelPath Path to the model directory.
     * @return MNIST dataset.
     */
    public static MnistData getMnistData(String modelPath) {
        String metadataPath = modelPath + "/metadata.json";
        Json json = new Json(metadataPath);
        Json training = json.getSubJson("training");
        int batchSize = training.getInt("batchSize");
        Json sequence = json.getSubJson("modelConfig");
        String engine = sequence.getString("engine");
        Shape inputShape = ModelBuilder.parseShape(json.getJsonNode("modelConfig").get("inputShape"));
        MnistData mnistData = new MnistData();
        mnistData.loadData(engine, batchSize, inputShape, 10, true);
        return mnistData;
    }
}