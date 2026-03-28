package com.gly;

import ai.djl.nn.*;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.Dropout;
import com.fasterxml.jackson.databind.JsonNode;
import ai.djl.nn.Activation;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

import ai.djl.nn.*;
import ai.djl.nn.core.*;
import com.gly.io.json.Json;
import ai.djl.ndarray.types.Shape;

/**
 * 从 JSON 配置文件构建 DJL 的 Block（模型结构）
 */
public class ModelBuilder {
    public static Block buildBlockFromJson(Path configPath) throws IOException {
        return buildBlockFromJson(configPath.toString());
    }

    /**
     * 从 JSON 文件构建 Block
     *
     * @param configPath JSON 配置文件路径
     * @return 构建好的 SequentialBlock
     */
    public static Block buildBlockFromJson(String configPath) throws IOException {
        Json json = new Json(configPath);
        JsonNode modelConfig = json.getJsonNode("modelConfig");
        if (modelConfig == null) {
            throw new IllegalArgumentException("Missing 'modelConfig' in JSON");
        }
        return buildBlockFromModelConfig(modelConfig);
    }

    /**
     * 从 modelConfig 节点构建 Block
     *
     * @param modelConfig modelConfig JSON 节点
     * @return 构建好的 SequentialBlock
     */
    public static Block buildBlockFromModelConfig(JsonNode modelConfig) {
        JsonNode layersNode = modelConfig.get("layers");
        Json json = new Json();
        json.setRootNode(modelConfig);
        if (layersNode == null || !layersNode.isArray()) {
            throw new IllegalArgumentException("Missing or invalid 'layers' array");
        }

        SequentialBlock block = new SequentialBlock();
        for (JsonNode layer : layersNode) {
            String layerType = layer.get("type").asText().toLowerCase();
            switch (layerType) {
                case "flatten":
                    Shape inputShape = ModelBuilder.parseShape(modelConfig.get("inputShape"));
                    block.add(Blocks.batchFlattenBlock(inputShape.size()));
                    break;
                case "dense":
                    int units = layer.get("units").asInt();
                    block.add(Linear.builder().setUnits(units).build());
                    if (layer.has("activation") && !layer.get("activation").isNull()) {
                        Block actBlock = parseActivation(layer.get("activation").asText());
                        if (actBlock != null) {
                            block.add(actBlock);
                        }
                    }
                    break;
                case "dropout":
                    float rate =  (float)layer.get("rate").asDouble();
                    block.add(Dropout.builder().optRate(rate).build());
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported layer type: " + layerType);
            }
        }
        return block;
    }

    /**
     * 解析激活函数名称，返回对应的 Block（激活层）
     *
     * @param name 激活函数名称，如 relu, sigmoid, tanh, linear
     * @return 激活层 Block，若为 linear 则返回 null
     */
    private static Block parseActivation(String name) {
        switch (name.toLowerCase()) {
            case "relu":
                return Activation.reluBlock();
            case "sigmoid":
                return Activation.sigmoidBlock();
            case "tanh":
                return Activation.tanhBlock();
            case "linear":
                return null; // 恒等变换，不添加层
            default:
                throw new IllegalArgumentException("Unsupported activation: " + name);
        }
    }

    /**
     * 将 JSON 节点解析为 DJL 的 Shape
     *
     * @param node JSON 节点，可以是数字（标量）或数组
     * @return Shape 对象
     */
    public static Shape parseShape(JsonNode node) {
        if (node == null) return null;
        if (node.isInt()) {
            return new Shape(node.asLong());
        } else if (node.isArray()) {
            long[] dims = new long[node.size()];
            for (int i = 0; i < node.size(); i++) {
                dims[i] = node.get(i).asLong();
            }
            return new Shape(dims);
        } else {
            throw new IllegalArgumentException("Invalid shape node: " + node);
        }
    }
}