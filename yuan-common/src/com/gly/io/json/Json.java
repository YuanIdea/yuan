package com.gly.io.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

/**
 * Json处理类。
 */
public class Json {
    private JsonNode rootNode;
    private ObjectMapper mapper;

    public Json() {
        mapper = new ObjectMapper();
    }

    public Json(String filePath) {
        try {
            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("文件不存在: " + filePath);
                return;
            }

            // 检查文件扩展名
            if (!isJsonExtension(filePath)) {
                System.err.println("文件扩展名非标准json格式: " + filePath);
                return;
            }

            // 尝试解析JSON内容
            mapper = new ObjectMapper();
            rootNode = mapper.readTree(file);
        } catch (IOException e) {
            System.err.println("解析"+ filePath + "失败：" + e.getMessage());
        }
    }

    // 辅助方法：检查文件扩展名
    private boolean isJsonExtension(String path) {
        String lowerPath = path.toLowerCase();
        return lowerPath.endsWith(".json") || lowerPath.endsWith(".jsonc");
    }

    /**
     * 解析指定名称的字符串值。
     *
     * @param key 解析的字段。
     * @return 解析出来的值。
     */
    public String getString(String key) {
        return getString(rootNode, key);
    }

    public boolean getBoolean(String key) {
        if (rootNode != null && rootNode.has(key)) {
            return rootNode.get(key).asBoolean();// 只读取"name"字段
        }  else {
            return false;
        }
    }

    public int getInt(String key) {
        if (rootNode != null && rootNode.has(key)) {
            return rootNode.get(key).asInt();// 只读取"name"字段
        }  else {
            return 0;
        }
    }

    public float getFloat(String key) {
        if (rootNode != null && rootNode.has(key)) {
            return (float) rootNode.get(key).asDouble();// 只读取"name"字段
        }  else {
            return 0f;
        }
    }

    public double getDouble(String key) {
        if (rootNode != null && rootNode.has(key)) {
            return rootNode.get(key).asDouble();// 只读取"name"字段
        }  else {
            return 0;
        }
    }

    /**
     * 解析指定名称的json串。
     *
     * @param key 解析的字段。
     * @return 解析出来的json字符串。
     */
    public String getJsonString(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null) {
                return node.toString(); // 将JsonNode转成字符串
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

    public double[][] getDouble2DArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                int outerSize = node.size();
                // 判断二维数组的行数
                if (outerSize == 0) {
                    return new double[0][0];
                }
                // 假设每个内部元素也是数组，且长度相同
                JsonNode firstInnerNode = node.get(0);
                if (!firstInnerNode.isArray()) {
                    return null;  // 结构不符合二维数组
                }
                int innerSize = firstInnerNode.size();
                double[][] result = new double[outerSize][innerSize];
                for (int i = 0; i < outerSize; ++i) {
                    JsonNode innerNode = node.get(i);
                    if (!innerNode.isArray() || innerNode.size() != innerSize) {
                        return null; // 内部数组长度不一致或不是数组，无法转换
                    }
                    for (int j = 0; j < innerSize; j++) {
                        result[i][j] = innerNode.get(j).asDouble();
                    }
                }
                return result;
            }
        }
        return null;
    }

    /**
     * 获得3维数组,各数组维度要求一致。
     * @param key json串键值。
     * @return 3维数组。
     */
    public double[][][] getDouble3DArray(String key) {
        if (rootNode != null) {
            JsonNode node = rootNode.get(key);
            if (node != null && node.isArray()) {
                int dim1 = node.size();  // 第1维长度
                if (dim1 == 0) {
                    System.err.println("空3维数组");
                    return new double[0][0][0];
                }

                // 检查第2维：要求每个元素都是数组，且第2维长度一致
                int dim2 = -1;
                for (int i = 0; i < dim1; ++i) {
                    JsonNode midNode = node.get(i);
                    if (!midNode.isArray()) {
                        System.err.println("第2维非数组结构");
                        return null;
                    }
                    if (i == 0) {
                        dim2 = midNode.size();  // 用第一个元素确定第二维长度
                    } else if (midNode.size() != dim2) {
                        System.err.println("第2维长度不一致.\n第1组长度为:" + dim2 + ",第" +(i+1)+"组长度为:" + midNode.size());
                        return null;
                    }
                }

                // 空二维数组检查（dim2可能是0）
                if (dim2 == 0) {
                    return new double[dim1][0][0];
                }

                // 检查第3维：要求每个元素都是数组，且第3维长度一致
                int dim3 = -1;
                for (int i = 0; i < dim1; ++i) {
                    JsonNode midNode = node.get(i);
                    for (int j = 0; j < dim2; ++j) {
                        JsonNode innerNode = midNode.get(j);
                        if (!innerNode.isArray()) {
                            System.err.println("第3维非数组结构");
                            return null;
                        }
                        if (i == 0 && j == 0) {
                            dim3 = innerNode.size();  // 用第一个元素确定第三维长度
                        } else if (innerNode.size() != dim3) {
                            System.err.println("第3维长度不一致.\n第1行长度为:" + dim3 + ",第" +(j+1)+"行长度为:" + innerNode.size());
                            return null;
                        }
                    }
                }

                // 创建并填充3维数组
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
     * 判断json字符串中是否包含指定字段
     *
     * @param key      解析的字段。
     * @return true-存在，false-不存在或解析异常
     */
    public boolean has(String key) {
        if (rootNode != null) {
            return rootNode.has(key);
        } else {
            return false;// 解析失败返回false
        }
    }

    public JsonNode getJsonNode(String id) {
        return  rootNode.path(id);
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
     * 解析指定名称的字符串值。
     * @param rootNode 根节点。
     * @param key 解析的字段。
     * @return 解析出来的值。
     */
    public static String getString(JsonNode rootNode, String key) {
        if (rootNode != null) {
            return rootNode.get(key).asText();// 只读取"name"字段
        } else {
            return "";
        }
    }

    /**
     * 写到磁盘中。
     * @param filePath 指定写入的位置。
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
