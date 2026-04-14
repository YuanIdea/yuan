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

    public static void writeMinMax(String pathName, Coder coder, Coder label) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("dataMin", coder.getMinData());
        jsonMap.put("dataMax", coder.getMaxData());
        jsonMap.put("labelMin", label.getMinData());
        jsonMap.put("labelMax", label.getMaxData());
        JsonUtil.writeJson(pathName, jsonMap);
    }
}
