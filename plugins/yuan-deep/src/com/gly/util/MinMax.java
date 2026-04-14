package com.gly.util;

import com.gly.io.json.Json;

import java.util.LinkedHashMap;
import java.util.Map;

public class MinMax {
    public static String getMinMaxPath(String root, Json json) {
        String modelPath = getModePath(root, json);
        return modelPath + "/minMax.json";
    }

    public static String getModePath (String root, Json json) {
        String modelPath = json.getString("modelPath");
        return PathUtil.resolveAbsolutePath(root, modelPath);
    }

    /**
     * Write the maximum and minimum values to the configuration file.
     * @param pathName The file path name for the JSON file containing the maximum and minimum values.
     * @param data The maximum and minimum values in the data.
     * @param label The maximum and minimum values in the labels.
     */
    public static void writeMinMax(String pathName, Coder data, Coder label) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("dataMin", data.getMinData());
        jsonMap.put("dataMax", data.getMaxData());
        jsonMap.put("labelMin", label.getMinData());
        jsonMap.put("labelMax", label.getMaxData());
        JsonUtil.writeJson(pathName, jsonMap);
    }
}
