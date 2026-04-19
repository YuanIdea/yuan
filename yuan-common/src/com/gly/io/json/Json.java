package com.gly.io.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

/**
 * JSON processing class.
 */
public class Json {
    private JsonNode rootNode;
    private ObjectMapper mapper;

    public Json() {
        mapper = new ObjectMapper();
    }

    public Json(String filePath) {
        try {
            // Check if the file exists
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File does not exist:" + filePath);
                return;
            }

            // Check file extension.
            if (!isJsonExtension(filePath)) {
                System.err.println("File extension is not in standard JSON format:" + filePath);
                return;
            }

            // Attempt to parse JSON content.
            mapper = new ObjectMapper();
            rootNode = mapper.readTree(file);
        } catch (IOException e) {
            System.err.println("Parsing failed:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check file extension.
     *
     * @param pathName Path name of the file.
     * @return Whether it is a JSON file extension.
     */
    private boolean isJsonExtension(String pathName) {
        String lowerPath = pathName.toLowerCase();
        return lowerPath.endsWith(".json") || lowerPath.endsWith(".jsonc");
    }

    /**
     * Parses the string value for the specified key.
     *
     * @param key The field to parse.
     * @return The parsed value.
     */
    public String getString(String key) {
        return getString(rootNode, key);
    }

    public boolean getBoolean(String key) {
        if (rootNode != null && rootNode.has(key)) {
            return rootNode.get(key).asBoolean();
        } else {
            return false;
        }
    }

    public int getInt(String key) {
        if (rootNode != null && rootNode.has(key)) {
            return rootNode.get(key).asInt();
        } else {
            return 0;
        }
    }

    public float getFloat(String key) {
        if (rootNode != null && rootNode.has(key)) {
            return (float) rootNode.get(key).asDouble();
        } else {
            return 0f;
        }
    }

    public double getDouble(String key) {
        if (rootNode != null && rootNode.has(key)) {
            return rootNode.get(key).asDouble();
        } else {
            return 0;
        }
    }

    /**
     * Parses the JSON string for the specified key.
     *
     * @param key The field to parse.
     * @return The parsed JSON string.
     */
    public String getJsonString(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null) {
                return node.toString();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public int[] getIntArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                int[] result = new int[node.size()];
                for (int i = 0; i < node.size(); ++i) {
                    result[i] = node.get(i).asInt();
                }
                return result;
            }
        }
        return null;
    }

    public double[] getDoubleArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                double[] result = new double[node.size()];
                for (int i = 0; i < node.size(); ++i) {
                    result[i] = node.get(i).asDouble();
                }
                return result;
            }
        }
        return null;
    }

    public float[] getFloatArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                float[] result = new float[node.size()];
                for (int i = 0; i < node.size(); ++i) {
                    result[i] = node.get(i).floatValue();
                }
                return result;
            }
        }
        return null;
    }

    public int[][] getInt2DArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                int outerSize = node.size();
                // Get the number of rows in a 2D array
                if (outerSize == 0) {
                    return null;
                }
                // Assume each inner element is also an array, and all have the same length
                JsonNode firstInnerNode = node.get(0);
                if (!firstInnerNode.isArray()) {
                    return null;  // The structure does not conform to a 2D array
                }
                int innerSize = firstInnerNode.size();
                int[][] result = new int[outerSize][innerSize];
                for (int i = 0; i < outerSize; ++i) {
                    JsonNode innerNode = node.get(i);
                    if (!innerNode.isArray() || innerNode.size() != innerSize) {
                        // The inner arrays have inconsistent lengths or are not arrays;
                        // conversion is not possible.
                        return null;
                    }
                    for (int j = 0; j < innerSize; ++j) {
                        result[i][j] = innerNode.get(j).asInt();
                    }
                }
                return result;
            }
        }
        return null;
    }

    public double[][] getDouble2DArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                int outerSize = node.size();
                // Get the number of rows in a 2D array
                if (outerSize == 0) {
                    return null;
                }
                // Assume each inner element is also an array, and all have the same length
                JsonNode firstInnerNode = node.get(0);
                if (!firstInnerNode.isArray()) {
                    return null;  // The structure does not conform to a 2D array
                }
                int innerSize = firstInnerNode.size();
                double[][] result = new double[outerSize][innerSize];
                for (int i = 0; i < outerSize; ++i) {
                    JsonNode innerNode = node.get(i);
                    if (!innerNode.isArray() || innerNode.size() != innerSize) {
                        // The inner arrays have inconsistent lengths or are not arrays;
                        // conversion is not possible.
                        return null;
                    }
                    for (int j = 0; j < innerSize; ++j) {
                        result[i][j] = innerNode.get(j).asDouble();
                    }
                }
                return result;
            }
        }
        return null;
    }

    public float[][] getFloat2DArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                int outerSize = node.size();
                // Get the number of rows in a 2D array
                if (outerSize == 0) {
                    return null;
                }
                // Assume each inner element is also an array, and all have the same length
                JsonNode firstInnerNode = node.get(0);
                if (!firstInnerNode.isArray()) {
                    return null;  // The structure does not conform to a 2D array
                }
                int innerSize = firstInnerNode.size();
                float[][] result = new float[outerSize][innerSize];
                for (int i = 0; i < outerSize; ++i) {
                    JsonNode innerNode = node.get(i);
                    if (!innerNode.isArray() || innerNode.size() != innerSize) {
                        // The inner arrays have inconsistent lengths or are not arrays;
                        // conversion is not possible.
                        return null;
                    }
                    for (int j = 0; j < innerSize; ++j) {
                        result[i][j] = innerNode.get(j).floatValue();
                    }
                }
                return result;
            }
        }
        return null;
    }

    /**
     * Gets a 3-dimensional array. The dimensions of each array must be consistent.
     *
     * @param key The JSON key.
     * @return A 3-dimensional array.
     */
    public double[][][] getDouble3DArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                int dim1 = node.size();  // 第1维长度
                if (dim1 == 0) {
                    System.err.println("Empty 3-dimensional array.");
                    return new double[0][0][0];
                }

                // Check the second dimension: require each element to be an array,
                // and the lengths of the second dimension to be consistent.
                int dim2 = -1;
                for (int i = 0; i < dim1; ++i) {
                    JsonNode midNode = node.get(i);
                    if (!midNode.isArray()) {
                        System.err.println("The second dimension is not an array structure.");
                        return null;
                    }
                    if (i == 0) {
                        //Use the first element to determine the length of the second dimension.
                        dim2 = midNode.size();
                    } else if (midNode.size() != dim2) {
                        System.err.println("The lengths of the second dimension are inconsistent." +
                                "\n Length of row 1 is:" + dim2 + ", Length of row " + (i + 1) + "is:"
                                + midNode.size());
                        return null;
                    }
                }

                // Empty two-dimensional array check.
                if (dim2 == 0) {
                    return new double[dim1][0][0];
                }

                // Check the third dimension: require each element to be an array,
                // and the lengths of the third dimension to be consistent.
                int dim3 = -1;
                for (int i = 0; i < dim1; ++i) {
                    JsonNode midNode = node.get(i);
                    for (int j = 0; j < dim2; ++j) {
                        JsonNode innerNode = midNode.get(j);
                        if (!innerNode.isArray()) {
                            System.err.println("The third dimension is not an array structure.");
                            return null;
                        }
                        if (i == 0 && j == 0) {
                            // Use the first element to determine the length of the third dimension.
                            dim3 = innerNode.size();
                        } else if (innerNode.size() != dim3) {
                            System.err.println("The lengths of the third dimension are inconsistent." +
                                    "\n Length of row 1 is:" + dim3 + ", Length of row " + (j + 1) + "is:"
                                    + innerNode.size());
                            return null;
                        }
                    }
                }

                // Create and populate a 3-dimensional array.
                double[][][] result = new double[dim1][dim2][dim3];
                for (int i = 0; i < dim1; ++i) {
                    JsonNode midNode = node.get(i);
                    for (int j = 0; j < dim2; ++j) {
                        JsonNode innerNode = midNode.get(j);
                        for (int k = 0; k < dim3; ++k) {
                            result[i][j][k] = innerNode.get(k).asDouble();
                        }
                    }
                }
                return result;
            }
        }
        return null;
    }

    /**
     * Gets a 3-dimensional array. The dimensions of each array must be consistent.
     *
     * @param key The JSON key.
     * @return A 3-dimensional array.
     */
    public float[][][] getFloat3DArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                int dim1 = node.size();  // 第1维长度
                if (dim1 == 0) {
                    System.err.println("Empty 3-dimensional array.");
                    return new float[0][0][0];
                }

                // Check the second dimension: require each element to be an array,
                // and the lengths of the second dimension to be consistent.
                int dim2 = -1;
                for (int i = 0; i < dim1; ++i) {
                    JsonNode midNode = node.get(i);
                    if (!midNode.isArray()) {
                        System.err.println("The second dimension is not an array structure.");
                        return null;
                    }
                    if (i == 0) {
                        //Use the first element to determine the length of the second dimension.
                        dim2 = midNode.size();
                    } else if (midNode.size() != dim2) {
                        System.err.println("The lengths of the second dimension are inconsistent." +
                                "\n Length of row 1 is:" + dim2 + ", Length of row " + (i + 1) + "is:"
                                + midNode.size());
                        return null;
                    }
                }

                // Empty two-dimensional array check.
                if (dim2 == 0) {
                    return new float[dim1][0][0];
                }

                // Check the third dimension: require each element to be an array,
                // and the lengths of the third dimension to be consistent.
                int dim3 = -1;
                for (int i = 0; i < dim1; ++i) {
                    JsonNode midNode = node.get(i);
                    for (int j = 0; j < dim2; ++j) {
                        JsonNode innerNode = midNode.get(j);
                        if (!innerNode.isArray()) {
                            System.err.println("The third dimension is not an array structure.");
                            return null;
                        }
                        if (i == 0 && j == 0) {
                            // Use the first element to determine the length of the third dimension.
                            dim3 = innerNode.size();
                        } else if (innerNode.size() != dim3) {
                            System.err.println("The lengths of the third dimension are inconsistent." +
                                    "\n Length of row 1 is:" + dim3 + ", Length of row " + (j + 1) + "is:"
                                    + innerNode.size());
                            return null;
                        }
                    }
                }

                // Create and populate a 3-dimensional array.
                float[][][] result = new float[dim1][dim2][dim3];
                for (int i = 0; i < dim1; ++i) {
                    JsonNode midNode = node.get(i);
                    for (int j = 0; j < dim2; ++j) {
                        JsonNode innerNode = midNode.get(j);
                        for (int k = 0; k < dim3; ++k) {
                            result[i][j][k] = innerNode.get(k).floatValue();
                        }
                    }
                }
                return result;
            }
        }
        return null;
    }

    /**
     * Checks whether the JSON string contains the specified field.
     *
     * @param key The field to check.
     * @return true if exists, false if not exists or parsing exception occurs.
     */
    public boolean has(String key) {
        if (rootNode != null) {
            return rootNode.has(key);
        } else {
            return false;// Return false if parsing fails
        }
    }

    public JsonNode getJsonNode(String id) {
        return rootNode.path(id);
    }


    public void updateField(String targetField, String newValue) {
        try {
            ObjectNode sequentialObj = (ObjectNode) rootNode;// 转换为可修改的ObjectNode
            sequentialObj.put(targetField, newValue);// 更新目标字段
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);// 修改完整JSON字符串
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void addField(String targetField, ArrayNode value) {
        try {
            ObjectNode sequentialObj = (ObjectNode) rootNode;// 转换为可修改的ObjectNode
            sequentialObj.set(targetField, value);
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);// 修改完整JSON字符串
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public JsonNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(JsonNode rootNode) {
        this.rootNode = rootNode;
    }

    public void setJsonNode(String json) {
        try {
            mapper = new ObjectMapper();
            rootNode = mapper.readTree(json);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Parses the string value for the specified key.
     *
     * @param rootNode The root node.
     * @param key      The field to parse.
     * @return The parsed value.
     */
    public static String getString(JsonNode rootNode, String key) {
        if (rootNode != null && rootNode.has(key)) {
            return rootNode.get(key).asText();
        } else {
            return "";
        }
    }

    public Json getSubJson(String key) {
        if (rootNode != null && rootNode.has(key)) {
            Json subJson = new Json();
            subJson.setRootNode(rootNode.get(key));
            return subJson;
        } else {
            return null;
        }
    }

    /**
     * Writes to disk.
     *
     * @param filePath Specifies the location to write to.
     */
    public void writeJsonNode(String filePath) {
        try {
            File file = new File(filePath);
            // 确保目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(file, rootNode);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}