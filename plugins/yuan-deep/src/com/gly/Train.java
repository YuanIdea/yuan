package com.gly;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.loss.SoftmaxCrossEntropyLoss;
import ai.djl.training.optimizer.Adam;

import com.fasterxml.jackson.databind.JsonNode;
import com.gly.config.Data;
import com.gly.config.Training;
import com.gly.io.json.Json;
import com.gly.model.BaseExecutable;
import com.gly.util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Train extends BaseExecutable {
    private final static Device[] maxGpus = new Device[]{Device.cpu()};
    private Training training;

    @Override
    public void start() {
        String root = getRoot();
        String name = getName();

        Json json = new Json(name);
        Data data = Data.parse(json.getJsonNode("data"));
        int[] inputIndex = data.getInputIndex();
        int[] labelIndex = data.getLabelIndex();
        String filePath = PathUtil.resolveAbsolutePath(root, data.getInputPathName());
        Pair<float[][], float[][]> allData = DataUtil.readToPairFloat(filePath, 1, inputIndex, labelIndex);
        if (allData != null) {
            Coder dataCoder = new Coder(allData.first);
            Coder labelCoder = new Coder(allData.second);
            training = Training.parse(json.getJsonNode("training"));
            try {
                Json sequence = json.getSubJson("modelConfig");
                String engine = sequence.getString("engine");
                if (engine.isEmpty()) {
                    engine = "PyTorch";
                }
                try (NDManager manager = NDManager.newBaseManager(engine)) {
                    Dataset dataset = convertToDataset(manager,
                            dataCoder.getEncode(),
                            labelCoder.getEncode(),
                            training.getBatchSize(),
                            data.isShuffle());
                    trainAndSaveModel(name, dataset, dataset);
                    String minMaxPath = PathUtil.resolveAbsolutePath(root, data.getMinMaxPathName());
                    MinMax.writeMinMax(minMaxPath, dataCoder, labelCoder);
                }
            } catch (Exception e) {
                System.out.println("Training error:" + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Model training and saving methods.
     *
     * @param metadataPathName Path to the model directory.
     * @param trainingDataset  Dataset used for training.
     * @param validateDataset  Dataset used for validation.
     */
    public void trainAndSaveModel(String metadataPathName, Dataset trainingDataset, Dataset validateDataset) {
        try {
            Json json = new Json(metadataPathName);
            if (training == null) {
                training = Training.parse(json.getJsonNode("training"));
            }
            Json sequence = json.getSubJson("modelConfig");
            // Parse input shape from configuration
            JsonNode modelConfig = sequence.getRootNode();
            Shape inputShape = ModelBuilder.parseShape(modelConfig.get("inputShape"));
            Shape fullShape = ModelBuilder.concatWithBatchSize(training.getBatchSize(), inputShape);
            String modelName = sequence.getString("name");

            // Use try-with-resources to automatically close the model
            try (Model model = ModelBuilder.generateModel(sequence)) {
                // Configure training settings
                DefaultTrainingConfig lossConfig = setupTrainingConfig(training.getLoss(), training.isAccuracy());
                try (Trainer trainer = model.newTrainer(lossConfig)) {
                    trainer.setMetrics(new Metrics());
                    trainer.initialize(fullShape);
                    System.out.println("Start training " + modelName + "...");
                    EasyTrain.fit(trainer, training.getEpochs(), trainingDataset, validateDataset);
                }

                if (!training.getSaveModelPath().isEmpty()) {
                    saveModel(Paths.get(getRoot()), model, json);
                }
            }
            setDone(true);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveModel(Path rootPath, Model model, Json metadata) throws IOException {
        Json sequence = metadata.getSubJson("modelConfig");
        Json training = metadata.getSubJson("training");
        Path modelDir = rootPath.resolve(training.getString("saveModelPath"));
        // Save the trained model
        Files.createDirectories(modelDir); // Ensure directory exists
        model.save(modelDir, sequence.getString("name"));
        String configPathName = modelDir.resolve("config.json").toString();
        JsonUtil.writeJsonNode(configPathName, metadata.getJsonNode("modelConfig"));
        System.out.println("Model saved to: " + modelDir.toAbsolutePath());
    }

    private static DefaultTrainingConfig setupTrainingConfig(String lossName, boolean addAccuracy) {
        Loss loss;
        lossName = lossName.toLowerCase();
        if ("crossentropy".equals(lossName)) {
            loss = new SoftmaxCrossEntropyLoss("softmax", 1.0f, -1,
                    false, true); // 接受 one‑hot
        } else if ("mse".equals(lossName)) {
            loss = Loss.l2Loss(); // 直接使用 one‑hot
        } else {
            loss = Loss.l2Loss();
        }
        DefaultTrainingConfig config = new DefaultTrainingConfig(loss)
                .optDevices(maxGpus)
                .optOptimizer(Adam.builder().build())
                .addTrainingListeners(TrainingListener.Defaults.logging());
        if (addAccuracy) {
            config.addEvaluator(new Accuracy("accuracy"));
        }
        return config;
    }

    public static Dataset convertToDataset(NDManager manager, float[][] featuresArray,
                                           float[][] labelsArray, int batchSize, boolean shuffle) {
        NDArray features = manager.create(featuresArray);
        NDArray labels = manager.create(labelsArray);
        return new ArrayDataset.Builder()
                .setData(features)
                .optLabels(labels)
                .setSampling(batchSize, shuffle)
                .build();
    }

    @Override
    public void stop() {
    }

    @Override
    public Object getResult() {
        return null;
    }
}