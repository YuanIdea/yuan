package com.gly.util;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.types.Shape;
import com.gly.ModelBuilder;
import com.gly.RegressionTranslator;
import com.gly.io.json.Json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class NetUtil {
    public static float[][] batchPredict(String modelPath, float[][] inputArray) throws Exception {
        Path modelDir = Paths.get(modelPath);
        if (!Files.exists(modelDir) || !Files.isDirectory(modelDir)) {
            throw new IOException("Model directory does not exist: " + modelDir.toAbsolutePath());
        }
        Json json = new Json(modelPath + "/config.json");
        List<float[]> outputs;
        try (Model model = ModelBuilder.load(modelDir, json)) {
            Shape inputShape = ModelBuilder.parseShape(json.getRootNode().get("inputShape"));
            RegressionTranslator translator = new RegressionTranslator(inputShape);
            try (Predictor<float[], float[]> predictor = model.newPredictor(translator)) {
                List<float[]> inputList = Arrays.asList(inputArray);
                outputs = predictor.batchPredict(inputList);
            }
        }
        return outputs.toArray(new float[0][]);
    }
}