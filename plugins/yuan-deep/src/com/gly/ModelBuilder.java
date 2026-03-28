package com.gly;

import ai.djl.nn.*;
import ai.djl.nn.convolutional.Conv2d;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.Dropout;
import ai.djl.nn.pooling.Pool;
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
 * Builds a DJL Block (model structure) from a JSON configuration file.
 */
public class ModelBuilder {
    public static Block buildBlockFromJson(Path configPath) throws IOException {
        return buildBlockFromJson(configPath.toString());
    }

    /**
     * Builds a Block from a JSON file.
     *
     * @param configPath Path to the JSON configuration file
     * @return The constructed SequentialBlock
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
     * Builds a Block from the modelConfig node.
     *
     * @param modelConfig The modelConfig JSON node
     * @return The constructed SequentialBlock
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
                    block.add(Blocks.batchFlattenBlock());
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
                case "conv2d":
                    int filters = layer.get("filters").asInt();
                    Shape kernelSize = parseShape(layer.get("kernelSize"));
                    Conv2d.Builder convBuilder = Conv2d.builder()
                            .setFilters(filters)
                            .setKernelShape(kernelSize);
                    if (layer.has("stride")) {
                        convBuilder.optStride(parseShape(layer.get("stride")));
                    }
                    if (layer.has("padding")) {
                        JsonNode padNode = layer.get("padding");
                        if (padNode.isTextual()) {
                            String padStr = padNode.asText().toLowerCase();
                            // Parse "same" or "valid" to specific padding values.
                            Shape paddingShape = parsePadding(padStr, kernelSize);
                            if (paddingShape != null) {
                                convBuilder.optPadding(paddingShape);
                            }
                        } else if (padNode.isArray()) {
                            // Use array directly as padding value.
                            convBuilder.optPadding(parseShape(padNode));
                        }
                    }
                    block.add(convBuilder.build());
                    if (layer.has("activation") && !layer.get("activation").isNull()) {
                        Block actBlock = parseActivation(layer.get("activation").asText());
                        if (actBlock != null) {
                            block.add(actBlock);
                        }
                    }
                    break;
                case "maxpool2d":
                    Shape poolSize = parseShape(layer.get("poolSize"));
                    Shape stride = layer.has("stride") ? parseShape(layer.get("stride")) : poolSize;
                    block.add(Pool.maxPool2dBlock(poolSize, stride));
                    break;
                case "avgpool2d":
                    Shape avgPoolSize = parseShape(layer.get("poolSize"));
                    Shape avgStride = layer.has("stride") ? parseShape(layer.get("stride")) : avgPoolSize;
                    block.add(Pool.avgPool2dBlock(avgPoolSize, avgStride));
                    break;
                case "dropout":
                    float rate = (float) layer.get("rate").asDouble();
                    block.add(Dropout.builder().optRate(rate).build());
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported layer type: " + layerType);
            }
        }
        return block;
    }

    /**
     * Parses the activation function name and returns the corresponding Block (activation layer).
     *
     * @param name The name of the activation function
     * @return The activation layer Block, or null for linear
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
                return null; // Do not add layer.
            default:
                throw new IllegalArgumentException("Unsupported activation: " + name);
        }
    }

    /**
     * Parses a JSON node into a DJL Shape.
     *
     * @param node JSON node, which can be a number (scalar) or an array
     * @return Shape object
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

    private static Shape parsePadding(String padding, Shape kernelSize) {
        long[] ks = kernelSize.getShape();
        if ("same".equals(padding)) {
            // Keep input and output dimensions the same: padding = (kernelSize - 1) / 2
            return new Shape((ks[0] - 1) / 2, (ks[1] - 1) / 2);
        } else if ("valid".equals(padding)) {
            return new Shape(0, 0); // No padding.
        }
        return null;
    }
}