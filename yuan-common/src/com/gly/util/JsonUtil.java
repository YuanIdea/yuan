package com.gly.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 对象转JSON字符串
     * @param params 需要转json的对象。
     * @return 对象对应的json字符串。
     */
    public static String encode(Object params) {
        try {
            return mapper.writeValueAsString(params);
        } catch (Exception ex) {
            throw new RuntimeException("JSON编码失败", ex);
        }
    }

    /**
     * JSON字符串转对象
     * @param json json字符串。
     * @param valueType 需要转化的类。
     * @param <T> 类的class
     * @return 指定类的实例。
     */
    public static <T> T decode(String json, Class<T> valueType) {
        try {
            return mapper.readValue(json, valueType);
        } catch (Exception ex) {
            throw new RuntimeException("JSON解码失败", ex);
        }
    }

    /**
     * 加载文件转成对象。
     * @param filePath 文件路径名。
     * @param valueType 需要转化的类。
     * @param <T> 类的class。
     * @return 指定类的实例。
     */
    public static <T> T load(String filePath, Class<T> valueType) {
        return decode(new File(filePath), valueType);
    }

    /**
     * 加载文件转换成对像
     * @param file 文件。
     * @param valueType 需要转化的类。
     * @param <T> 类的class
     * @return 指定类的实例。
     */
    public static <T> T decode(File file, Class<T> valueType) {
        try {
            return mapper.readValue(file, valueType);
        } catch (Exception ex) {
            throw new RuntimeException("JSON解码失败", ex);
        }
    }

    public static void writeJson(String pathName, Map<String, Object> jsonMap) {
        try {
            Path father = Paths.get(pathName).getParent();
            if (father != null) {
                Files.createDirectories(father);// 如果父目录不存在，创建目录
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT); // 启用美化打印（缩进和换行）
            mapper.writeValue(new File(pathName), jsonMap);
        } catch (Exception e) {
            System.err.println("写数据错误:" + e.getMessage());
        }
    }
}