package com.gly;

import ai.djl.nn.Block;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.nn.Activation;
import com.gly.io.json.Json;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ModelBuilder {
    /**
     * 创建网络结构
     */
    public static SequentialBlock createBlock(String config) {
        String modelName = "mlp-mnist";                      // 模型标识符
        Path modelDir = Paths.get("models", modelName);      // 指向 models/mlp-mnist 目录
        Path metadataFile = modelDir.resolve(config);
        return buildBlockFromMetadata(metadataFile);
    }

    public static SequentialBlock buildBlockFromMetadata(Path metadataPath) {
        try {
            // 读取 JSON
            Json json = new Json(metadataPath.toString());
            Json sequential = new Json();
            sequential.setRootNode(json.getJsonNode("sequential"));
            String modelType = sequential.getString("modelType");

            if ("mlp".equalsIgnoreCase(modelType)) {
                int inputDim = sequential.getInt("inputDim");
                int numClasses = sequential.getInt("numClasses");
                int[] hiddenSizes = sequential.getIntArray("hiddenSizes");

                // 动态构建 MLP
                boolean flattenInput = sequential.has("flattenInput") && sequential.getBoolean("flattenInput");
                SequentialBlock block = new SequentialBlock();
                if (flattenInput) {
                    block.add(Blocks.batchFlattenBlock(inputDim));
                }
                for (int size : hiddenSizes) {
                    block.add(Linear.builder().setUnits(size).build());
                    block.add(Activation::relu);
                }
                block.add(Linear.builder().setUnits(numClasses).build());
                return block;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
