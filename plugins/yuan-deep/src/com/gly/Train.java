package com.gly;

import ai.djl.Model;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import com.fasterxml.jackson.databind.JsonNode;
import com.gly.io.json.Json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Train {
    private String modelName;
    private String metadata;

    public void fit(String metadata, String modelName) {
        this.metadata = metadata;
        this.modelName = modelName;
        try {
            Json json = new Json(metadata);
            JsonNode config = json.getJsonNode("training");
            Json training = new Json();
            training.setRootNode(config);
            int batchSize = training.getInt("batchSize");
            int numEpochs = training.getInt("epochs");
            MnistData mnistData = new MnistData();
            mnistData.loadData(batchSize);
            trainAndSaveModel(json, mnistData.trainDataset, mnistData.testDataset, numEpochs);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Model training and saving methods.
     */
    public void trainAndSaveModel(Json json, Dataset trainDataset, Dataset testDataset, int numEpochs) throws Exception {
        // Parse input shape from configuration
        Shape inputShape = ModelBuilder.parseShape(json.getJsonNode("modelConfig").get("inputShape"));

        // Build the model block (returns a Block, avoid casting to SequentialBlock)
        Block block = ModelBuilder.buildBlockFromJson(metadata);

        // Use try-with-resources to automatically close the model
        try (Model model = Model.newInstance(modelName)) {
            model.setBlock(block);

            // Configure training settings
            DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                    .addEvaluator(new Accuracy())
                    .optOptimizer(Adam.builder().build())
                    .addTrainingListeners(TrainingListener.Defaults.logging());

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.initialize(inputShape);
                System.out.println("Start training " + modelName + "...");
                EasyTrain.fit(trainer, numEpochs, trainDataset, testDataset);
            } // Trainer automatically closed

            // Save the trained model
            Path modelDir = Paths.get("models", modelName);
            Files.createDirectories(modelDir); // Ensure directory exists
            model.save(modelDir, modelName);
            System.out.println("Model saved to: " + modelDir.toAbsolutePath());
        } // Model automatically closed
    }
}
