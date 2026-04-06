package com.gly;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.ndarray.types.Shape;
import com.gly.io.json.Json;
import com.gly.mnist.MnistTranslator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Use {
    /**
     * Make predictions using a pre-trained model.
     *
     * @param modelPath Path to the model directory
     * @param imagePath        Path to the image to be recognized
     */
    public static void predictWithModel(String modelPath, String imagePath) throws Exception {
        Path modelDir = Paths.get(modelPath);
        if (!Files.exists(modelDir) || !Files.isDirectory(modelDir)) {
            throw new IOException("Model directory does not exist: " + modelDir.toAbsolutePath());
        }
        Json json = new Json(modelPath+"/config.json");
        try (Model model = ModelBuilder.generateModel(json)) {
            // Load parameter file (automatically matches *.params)
            String prefix = json.getString("name");
            model.load(modelDir, prefix);
            System.out.println("Using " + prefix);
            Image img = ImageFactory.getInstance().fromFile(Paths.get(imagePath));
            predict(model, img);
        }
    }

    private static void predict(Model model, Image img) throws Exception {
        try (Predictor<Image, Classifications> predictor = model.newPredictor(new MnistTranslator())) {
            Classifications result = predictor.predict(img);
            System.out.println(result);
        }
    }
}
