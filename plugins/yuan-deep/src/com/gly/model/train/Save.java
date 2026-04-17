package com.gly.model.train;

import ai.djl.Model;
import com.gly.io.json.Json;
import com.gly.util.JsonUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Save {
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
}
