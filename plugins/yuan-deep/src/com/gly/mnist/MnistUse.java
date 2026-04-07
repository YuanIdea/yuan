package com.gly.mnist;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import com.gly.ModelBuilder;
import com.gly.io.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MnistUse {
    /**
     * Make predictions using a pre-trained model.
     *
     * @param modelPath Path to the model directory
     * @param imagePaths        Path to the image to be recognized
     */
    public static void predictWithModel(String modelPath, List<String> imagePaths) throws Exception {
        Path modelDir = Paths.get(modelPath);
        if (!Files.exists(modelDir) || !Files.isDirectory(modelDir)) {
            throw new IOException("Model directory does not exist: " + modelDir.toAbsolutePath());
        }
        Json json = new Json(modelPath+"/config.json");
        try (Model model = ModelBuilder.load(modelDir, json)) {
            List<Image> images = new ArrayList<>();
            for (String imagePath:imagePaths) {
                Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
                images.add(img);
            }
            predict(model, images);
        }
    }

    private static void predict(Model model, List<Image> images) throws Exception {
        try (Predictor<Image, Classifications> predictor = model.newPredictor(new MnistTranslator())) {
            List<Classifications> results = predictor.batchPredict(images);
            for (Classifications result : results) {
                Classifications.Classification best = result.best(); // 获取最高概率的类别
                System.out.printf("Predicted: %s (%.5f)%n", best.getClassName(), best.getProbability());
            }
        }
    }
}
