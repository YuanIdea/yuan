package com.gly.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
     * Converts an object to a JSON string.
     *
     * @param params The object to be converted to JSON.
     * @return The JSON string corresponding to the object.
     */
    public static String encode(Object params) {
        try {
            return mapper.writeValueAsString(params);
        } catch (Exception ex) {
            throw new RuntimeException("JSON encoding failed", ex);
        }
    }

    /**
     * Converts a JSON string to an object.
     *
     * @param json      The JSON string.
     * @param valueType The class to be converted to.
     * @param <T>       The class type.
     * @return An instance of the specified class.
     */
    public static <T> T decode(String json, Class<T> valueType) {
        try {
            return mapper.readValue(json, valueType);
        } catch (Exception ex) {
            throw new RuntimeException("JSON encoding failed", ex);
        }
    }

    /**
     * Converts a JsonNode object into an instance of the specified Java class.
     * <p>
     * This method uses Jackson's tree-to-value conversion internally and is useful
     * when a JSON tree node has already been parsed.
     *
     * @param node      the JsonNode to convert (must not be null)
     * @param valueType the target class to convert to
     * @param <T>       the type of the target object
     * @return the deserialized object of type T
     * @throws RuntimeException if conversion fails (wraps Jackson JsonProcessingException)
     */
    public static <T> T decode(JsonNode node, Class<T> valueType) {
        try {
            return mapper.treeToValue(node, valueType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JsonNode to " + valueType, e);
        }
    }

    /**
     * Loads a file and converts it to an object.
     *
     * @param filePath  The path name of the file.
     * @param valueType The class to be converted to.
     * @param <T>       The class type.
     * @return An instance of the specified class.
     */
    public static <T> T load(String filePath, Class<T> valueType) {
        return decode(new File(filePath), valueType);
    }

    /**
     * Loads a file and converts it to an object.
     *
     * @param file      The file.
     * @param valueType The class to be converted to.
     * @param <T>       The class type.
     * @return An instance of the specified class.
     */
    public static <T> T decode(File file, Class<T> valueType) {
        try {
            return mapper.readValue(file, valueType);
        } catch (Exception ex) {
            throw new RuntimeException("JSON encoding failed", ex);
        }
    }

    /**
     * Writes a Map object to a JSON file with pretty formatting.
     *
     * @param pathName the file path where the JSON data will be written
     * @param jsonMap  the Map containing key-value pairs to be serialized to JSON
     */
    public static void writeJson(String pathName, Map<String, Object> jsonMap) {
        try {
            Path father = Paths.get(pathName).getParent();
            if (father != null) {
                // If the parent directory does not exist, create it.
                Files.createDirectories(father);
            }
            ObjectMapper mapper = new ObjectMapper();
            // Enable pretty printing (indentation and line breaks)
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(new File(pathName), jsonMap);
        } catch (Exception e) {
            System.err.println("Write data error:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Writes a JsonNode object to a JSON file with pretty formatting.
     * Creates parent directories automatically if they don't exist.
     *
     * @param pathName the destination file path
     * @param jsonNode the JsonNode to be serialized to JSON
     */
    public static void writeJsonNode(String pathName, JsonNode jsonNode) {
        try {
            // Get the parent directory of the target file
            Path father = Paths.get(pathName).getParent();
            if (father != null) {
                // If the parent directory does not exist, create it along with any missing parent directories
                Files.createDirectories(father);
            }

            // Create ObjectMapper instance for JSON processing
            ObjectMapper mapper = new ObjectMapper();

            // Enable pretty printing (indentation and line breaks) for readable JSON output
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            // Write the JsonNode to the specified file
            mapper.writeValue(new File(pathName), jsonNode);
        } catch (Exception e) {
            // Print error message and stack trace if JSON writing fails
            System.err.println("Write JSON node error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}