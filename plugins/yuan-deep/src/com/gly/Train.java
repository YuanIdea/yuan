package com.gly;

import ai.djl.Model;
import ai.djl.engine.Engine;
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
import java.io.File;

public class Train {
    private String metadataPath;
    private String engine;

    public void fit(String metadataPath) {
        this.metadataPath = metadataPath;
        try {
            Json json = new Json(metadataPath);
            JsonNode config = json.getJsonNode("training");
            Json training = new Json();
            training.setRootNode(config);
            int batchSize = training.getInt("batchSize");
            int numEpochs = training.getInt("epochs");
            MnistData mnistData = new MnistData();
            engine = json.getSubJson("modelConfig").getString("engine");
            if (engine.isEmpty()) {
                engine = "PyTorch";
            }
            mnistData.loadData(batchSize, engine);
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
        Block block = ModelBuilder.buildBlockFromJson(metadataPath);
        String modelName = extractModelName(metadataPath);
        // Use try-with-resources to automatically close the model
        try (Model model = Model.newInstance(modelName, engine)) {
            // Print the current engine.
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

    /**
     * Extracts the model name by splitting the path with the system file separator.
     *
     * @param metadataPath The full path to the metadata.json file
     * @return The model name (the directory name right before "metadata.json")
     */
    public static String extractModelName(String metadataPath) {
        // Normalize separators to the system default
        String normalized = metadataPath.replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);
        String[] parts = normalized.split(File.separator.equals("\\") ? "\\\\" : File.separator);
        // The last part is "metadata.json", the one before is the model name
        if (parts.length >= 2) {
            return parts[parts.length - 2];
        }
        throw new IllegalArgumentException("Invalid path format: " + metadataPath);
    }
}
