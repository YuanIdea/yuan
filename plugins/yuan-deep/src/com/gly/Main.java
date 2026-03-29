package com.gly;

import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.nn.Block;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ai.djl.ndarray.types.Shape;
import ai.djl.translate.NoopTranslator;
import com.gly.io.json.Json;

public class Main {

    public static void main(String[] args) throws Exception {
        // 打印当前引擎
        System.out.println("Default engine: " + Engine.getDefaultEngineName());
        //String modelName = "mlp-mnist";
        String modelName = "cnn-mnist";
        //String modelName = "lstm-mnist";
        String model = "models/" + modelName;
        Train train = new Train();
        train.fit(model + "/metadata.json", modelName);
        predictWithModel(model, "test-digit.png");
    }

    /**
     * 使用已训练好的模型进行预测
     *
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
        String metadata = "models/" + modelName + "/metadata.json";
        Block block = ModelBuilder.buildBlockFromJson(metadata);
        System.out.println("使用" + modelName);

        // 创建模型实例，设置结构
        try (Model model = Model.newInstance(modelName)) {
            model.setBlock(block);
            // 加载参数文件（自动匹配 mlp-mnist-*.params 或 cnn-mnist-*.params）
            model.load(modelDir, modelName);
            System.out.println("模型加载成功: " + modelName);

            Json json = new Json(metadata);
            Shape inputShape = ModelBuilder.parseShape(json.getJsonNode("modelConfig").get("inputShape"));
            NDArray input = getGray(imagePath, inputShape);
            if (input == null) {
                System.out.println("Failed to parse data.");
                return;
            }
            try (Predictor<NDList, NDList> predictor = model.newPredictor(new NoopTranslator())) {
                NDList result = predictor.predict(new NDList(input));
                NDArray logits = result.singletonOrThrow();          // 形状 (1, 10)
                // 对类别维度（索引1）应用softmax，得到概率分布
                NDArray probabilities = logits.softmax(1).squeeze(0); // 形状 (10,)
                long[] indices = probabilities.argSort(0).toLongArray(); // 升序排序
                int len = indices.length;
                System.out.println("预测结果:");
                for (int i = 0; i < 3; ++i) {
                    int idx = (int) indices[len - 1 - i];
                    float prob = probabilities.getFloat(idx);
                    System.out.printf("  类别 %d: %.4f%n", idx, prob);
                }
            }
        }
    }

    private static NDArray getGray(String imagePath, Shape inputShape) {
        try {
            // 加载图片并预测
            BufferedImage original = ImageIO.read(Paths.get(imagePath).toFile());
            // 创建 28x28 灰度图
            BufferedImage grayImage = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = grayImage.createGraphics();
            g.drawImage(original, 0, 0, 28, 28, null);
            g.dispose();

            // 从 BufferedImage 获取像素数据
            byte[] pixelData = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();
            // 构建 NDArray，形状为 (1, 1, 28, 28) 或 (1, 28, 28) 取决于模型期望
            NDManager manager = NDManager.newBaseManager();
            // 将字节数组转为 float 并归一化到 [0,1]
            float[] floatData = new float[pixelData.length];
            for (int i = 0; i < pixelData.length; ++i) {
                floatData[i] = (pixelData[i] & 0xFF) / 255.0f;
            }

            return manager.create(floatData, inputShape);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}