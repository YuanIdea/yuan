package com.gly;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.loss.SoftmaxCrossEntropyLoss;
import ai.djl.training.optimizer.Adam;
import com.gly.io.json.Json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

public class Train {
    private final static Device[] maxGpus = new Device[]{Device.cpu()};

    /**
     * Model training and saving methods.
     *
     * @param modelPath Path to the model directory.
     * @param trainingDataset Dataset used for training.
     * @param validateDataset Dataset used for validation.
     */
    public void trainAndSaveModel(String modelPath, Dataset trainingDataset, Dataset validateDataset) {
        String metadataPath = modelPath + "/metadata.json";
        try {
            Json json = new Json(metadataPath);
            Json training = json.getSubJson("training");
            int batchSize = training.getInt("batchSize");
            int numEpochs = training.getInt("epochs");
            Json sequence = json.getSubJson("modelConfig");
            String engine = sequence.getString("engine");
            if (engine.isEmpty()) {
                engine = "PyTorch";
            }
            // Parse input shape from configuration
            Shape inputShape = ModelBuilder.parseShape(json.getJsonNode("modelConfig").get("inputShape"));
            Shape fullShape = ModelBuilder.concatWithBatchSize(batchSize, inputShape);

            Block block = ModelBuilder.buildBlockFromJson(metadataPath);
            String modelName = extractModelName(metadataPath, 2);

            // Use try-with-resources to automatically close the model
            try (Model model = Model.newInstance(modelName, engine)) {
                // Print the current engine.
                model.setBlock(block);
                // Configure training settings
                DefaultTrainingConfig config = setupTrainingConfig(training.getString("loss"));
                try (Trainer trainer = model.newTrainer(config)) {
                    trainer.setMetrics(new Metrics());
                    trainer.initialize(fullShape);
                    System.out.println("Start training " + modelName + "...");

                    EasyTrain.fit(trainer, numEpochs, trainingDataset, validateDataset);
                }

                // Save the trained model
                Path modelDir = Paths.get(modelPath);
                Files.createDirectories(modelDir); // Ensure directory exists
                model.save(modelDir, modelName);
                System.out.println("Model saved to: " + modelDir.toAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static DefaultTrainingConfig setupTrainingConfig(String lossName) {
        Loss loss;
        lossName = lossName.toLowerCase();
        if ("crossentropy".equals(lossName)) {
            loss = new SoftmaxCrossEntropyLoss("softmax", 1.0f, -1, false, true); // 接受 one‑hot
        } else if ("mse".equals(lossName)) {
            loss = Loss.l2Loss(); // 直接使用 one‑hot
        } else {
            loss = Loss.l2Loss();
        }
        return new DefaultTrainingConfig(loss)
                .addEvaluator(new Accuracy("accuracy"))
                .optDevices(maxGpus)
                .optOptimizer(Adam.builder().build())
                .addTrainingListeners(TrainingListener.Defaults.logging());
    }

    /**
     * Extracts the model name by splitting the path with the system file separator.
     *
     * @param metadataPath The full path to the metadata.json file
     * @param index        Get the index value of the truncated directory
     * @return The model name (the directory name right before "metadata.json")
     */
    public static String extractModelName(String metadataPath, int index) {
        // Normalize separators to the system default
        String normalized = metadataPath.replace('/', File.separatorChar)
                .replace('\\', File.separatorChar);
        String[] parts = normalized.split(File.separator.equals("\\") ? "\\\\" : File.separator);
        // The last part is "metadata.json", the one before is the model name
        if (parts.length > index) {
            return parts[index];
        }
        throw new IllegalArgumentException("Invalid path format: " + metadataPath);
    }
}
