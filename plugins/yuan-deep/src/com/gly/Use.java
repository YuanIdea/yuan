package com.gly;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.translate.NoopTranslator;
import com.gly.io.json.Json;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Use {
    /**
     * Make predictions using a pre-trained model.
     *
     * @param modelPath Path to the model directory
     * @param imagePath Path to the image to be recognized
     */
    public static void predictWithModel(String modelPath, String imagePath) throws Exception {
        Path modelDir = Paths.get(modelPath);
        if (!Files.exists(modelDir) || !Files.isDirectory(modelDir)) {
            System.err.println("Model directory does not exist: " + modelDir.toAbsolutePath());
            return;
        }

        String modelName = modelDir.getFileName().toString();
        String metadata = modelPath + "/metadata.json";
        Json json = new Json(metadata);
        Block block = ModelBuilder.buildBlockFromJson(metadata);
        System.out.println("Using " + modelName);
        String engine = json.getSubJson("modelConfig").getString("engine");
        if (engine.isEmpty()) {
            engine = "PyTorch";
        }
        // Create a model instance and set its structure
        try (Model model = Model.newInstance(modelName, engine)) {
            model.setBlock(block);
            // Load parameter file (automatically matches *.params)
            model.load(modelDir, modelName);
            System.out.println("Model loaded successfully: " + modelName);

            Shape inputShape = ModelBuilder.parseShape(json.getJsonNode("modelConfig").get("inputShape"));
            predict(model, imagePath, inputShape, engine);
        }
    }

    private static void predict(Model model, String imagePath, Shape inputShape, String engin) {
        try {
            // Load the image and make a prediction
            BufferedImage original = ImageIO.read(Paths.get(imagePath).toFile());
            // Create a 28x28 grayscale image
            BufferedImage grayImage = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = grayImage.createGraphics();
            g.drawImage(original, 0, 0, 28, 28, null);
            g.dispose();

            byte[] pixelData = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();
            NDArray input;
            try (NDManager manager = NDManager.newBaseManager(engin)) {
                // Convert byte array to float and normalize to [0,1]
                float[] floatData = new float[pixelData.length];
                for (int i = 0; i < pixelData.length; ++i) {
                    floatData[i] = (pixelData[i] & 0xFF) / 255.0f;
                }

                input = manager.create(floatData, ModelBuilder.concatWithBatchSize(1, inputShape));
                if (input == null) {
                    System.out.println("Failed to parse data.");
                }
                try (Predictor<NDList, NDList> predictor = model.newPredictor(new NoopTranslator())) {
                    NDList result = predictor.predict(new NDList(input));
                    NDArray logits = result.singletonOrThrow();          // Shape (1, 10)
                    // Apply softmax along the class dimension (index 1) to obtain the probability distribution.
                    NDArray probabilities = logits.softmax(1).squeeze(0); // Shape (10,).
                    long[] indices = probabilities.argSort(0).toLongArray(); // Sort in ascending order
                    int len = indices.length;
                    System.out.println("Prediction result:");
                    for (int i = 0; i < 3; ++i) {
                        int idx = (int) indices[len - 1 - i];
                        float prob = probabilities.getFloat(idx);
                        System.out.printf("  Class %d: %.4f%n", idx, prob);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
