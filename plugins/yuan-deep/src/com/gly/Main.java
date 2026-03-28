package com.gly;

import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.SequentialBlock;
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

public class Main {

    public static void main(String[] args) throws Exception {
        // 打印当前引擎
        System.out.println("Default engine: " + Engine.getDefaultEngineName());
        String metadata = "models/mlp-mnist/metadata.json";
        Json json = new Json(metadata);
        JsonNode config = json.getJsonNode("training");
        Json training = new Json();
        training.setRootNode(config);
        int batchSize = training.getInt("batchSize");
        int numEpochs = training.getInt("epochs");
        Shape shape = ModelBuilder.parseShape(json.getJsonNode("modelConfig").get("inputShape"));
        MnistData mnistData = new MnistData();
        mnistData.loadData(batchSize);
        // 2. 训练 MLP 模型
        try {
            SequentialBlock block = (SequentialBlock)ModelBuilder.buildBlockFromJson(metadata);
            trainAndSaveModel("mlp-mnist", block, shape,
                    mnistData.trainDataset, mnistData.testDataset, numEpochs);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // 3. 训练 CNN 模型
        trainAndSaveModel("cnn-mnist", Cnn.createCnnBlock(), new Shape(1, 1, 28, 28),
                mnistData.trainDataset, mnistData.testDataset, numEpochs);

        // 4. 预测示例（加载模型）
        predictWithModel("models/mlp-mnist", "test-digit.png");
    }

    /**
     * 通用模型训练与保存方法
     */
    private static void trainAndSaveModel(String modelName,
                                          SequentialBlock block,
                                          Shape inputShape,
                                          Dataset trainDataset,
                                          Dataset testDataset,
                                          int numEpochs) throws Exception {
        // 创建模型并设置网络结构
        Model model = Model.newInstance(modelName);
        model.setBlock(block);

        // 配置训练器
        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .optOptimizer(Adam.builder().build())
                .addTrainingListeners(TrainingListener.Defaults.logging());

        try (Trainer trainer = model.newTrainer(config)) {
            trainer.initialize(inputShape);
            System.out.println("开始训练 " + modelName + "...");
            EasyTrain.fit(trainer, numEpochs, trainDataset, testDataset);
        }

        // 保存模型到 models/{modelName} 目录
        Path modelDir = Paths.get("models", modelName);
        model.save(modelDir, modelName);
        // 检查最终模型文件是否存在
        System.out.println("模型已保存至: " + modelDir.toAbsolutePath());
    }

    /**
     * 使用已训练好的模型进行预测
     * @param modelPath 模型目录路径（例如 "models/cnn-mnist"）
     * @param imagePath 待识别图片路径（28x28 灰度图）
     */
    private static void predictWithModel(String modelPath, String imagePath) throws Exception {
        Path modelDir = Paths.get(modelPath);
        if (!Files.exists(modelDir) || !Files.isDirectory(modelDir)) {
            System.err.println("模型目录不存在: " + modelDir.toAbsolutePath());
            return;
        }

        String modelName = modelDir.getFileName().toString();
        Block block = null;

        // 1. 根据模型名称重建相同的网络结构
        if ("mlp-mnist".equals(modelName)) {
            block = ModelBuilder.buildBlockFromJson("models/mlp-mnist/metadata.json");
            System.out.println("使用 MLP 结构");
        } else if ("cnn-mnist".equals(modelName)) {
            block = Cnn.createCnnBlock();
            System.out.println("使用 CNN 结构");
        } else {
            System.err.println("未知模型类型: " + modelName);
            return;
        }

        // 2. 创建模型实例，设置结构
        try (Model model = Model.newInstance(modelName)) {
            model.setBlock(block);
            // 3. 加载参数文件（自动匹配 mlp-mnist-*.params 或 cnn-mnist-*.params）
            model.load(modelDir, modelName);
            System.out.println("模型加载成功: " + modelName);
            // 4. 创建 Translator（图像预处理）
            ImageClassificationTranslator translator = ImageClassificationTranslator.builder()
                    .addTransform(new ToTensor())
                    .optApplySoftmax(true)
                    .build();

            // 5. 加载图片并预测
            Path imageFile = Paths.get(imagePath);
            if (!Files.exists(imageFile)) {
                System.err.println("图片文件不存在: " + imageFile.toAbsolutePath());
                return;
            }
            Image image = ImageFactory.getInstance().fromFile(imageFile);

            try (Predictor<Image, Classifications> predictor = model.newPredictor(translator)) {
                Classifications result = predictor.predict(image);
                System.out.println("预测结果: " + result.topK(3));
            }
        }
    }
}