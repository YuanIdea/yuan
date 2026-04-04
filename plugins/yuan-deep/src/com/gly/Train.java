package com.gly;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
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

import com.gly.io.json.Json;
import com.gly.model.BaseExecutable;
import com.gly.util.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class Train extends BaseExecutable {
    private final static Device[] maxGpus = new Device[]{Device.cpu()};
    private String engine;

    @Override
    public void start() {
//        String root = getRoot();
//        String name = getName();

        String root = "D:/WorkSpace/github/yuan/yuan-demo/quick/";
        String name = root + "train.json";

        Json json = new Json(name);
        Json data = json.getSubJson("data");
        int[] inputIndex = data.getIntArray("inputIndex");
        int[] labelIndex = data.getIntArray("labelIndex");
        String filePath = PathUtil.resolveAbsolutePath(root, data.getString("inputPathName"));
        Pair<float[][], float[][]> allData = DataUtil.readToPairFloat(filePath, 1, inputIndex, labelIndex);
        if (allData != null) {
            Coder dataCoder = new Coder(allData.first);
            Coder labelCoder = new Coder(allData.second);
            String minMaxPath = PathUtil.resolveAbsolutePath(root, data.getString("minMaxPathName"));
            writeMinMax(minMaxPath, dataCoder, labelCoder);
            try {
                Json sequence = json.getSubJson("modelConfig");
                engine = sequence.getString("engine");
                if (engine.isEmpty()) {
                    engine = "PyTorch";
                }
                try (NDManager manager = NDManager.newBaseManager(engine)) {
                    Dataset dataset = convertToDataset(manager, dataCoder.getEncode(), labelCoder.getEncode(), 64, true);
                    ;
                    trainAndSaveModel(name, dataset, dataset);
                }
            } catch (Exception e) {
                e.printStackTrace(); // 打印完整堆栈
                throw e; // 或处理
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
        Path modelDir = Paths.get(metadataPathName).getParent();
        ;
        try {
            Json json = new Json(metadataPathName);
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

            Block block = ModelBuilder.buildBlockFromJson(metadataPathName);
            String modelName = extractModelName(metadataPathName, 2);

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

    public static Dataset convertToDataset(NDManager manager, float[][] featuresArray, float[][] labelsArray, int batchSize, boolean shuffle) {

        // 2. 将 float[][] 转换为 NDArray
        // 注意：NDArray 的数据类型默认为 FLOAT32，而 float 是 FLOAT64，转换可能会损失精度。
        NDArray features = manager.create(featuresArray);
        NDArray labels = manager.create(labelsArray);

        // 3. 使用 ArrayDataset.Builder 构建数据集
        ArrayDataset dataset = new ArrayDataset.Builder()
                .setData(features)          // 设置特征
                .optLabels(labels)          // 设置标签
                .setSampling(batchSize, shuffle) // 设置批次大小和是否打乱
                .build();

        return dataset;
    }

    private void writeMinMax(String pathName, Coder data, Coder label) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("dataMin", data.getMinData());
        jsonMap.put("dataMax", data.getMaxData());
        jsonMap.put("labelMin", label.getMinData());
        jsonMap.put("labelMax", label.getMaxData());
        JsonUtil.writeJson(pathName, jsonMap);
    }

    @Override
    public void stop() {
    }

    @Override
    public Object getResult() {
        return null;
    }
}
