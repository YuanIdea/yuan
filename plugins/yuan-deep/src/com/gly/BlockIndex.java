package com.gly;

import com.gly.io.json.Json;
import com.gly.util.JsonUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 块数据处理。
 */
public class BlockIndex {
    private final int[] useColIndex;
    private final int[] inputColIndex;
    private final int inputRowCount;
    private final int[][] inputMaskIndex;
    private final int[][] labelIndex;

    public BlockIndex(Json block) {
        useColIndex = block.getIntArray("useColIndex");
        inputRowCount = block.getInt("inputRowCount");
        inputColIndex = block.getIntArray("inputColIndex");
        inputMaskIndex = block.getInt2DArray("inputMaskIndex");
        labelIndex = block.getInt2DArray("labelIndex");
    }

    public void writeMinMaxJson(String minMaxPath, float[] minData, float[] maxData) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("blockMin", minData);
        jsonMap.put("blockMax", maxData);
        if (inputMaskIndex != null) {
            jsonMap.put("maskIndex", inputMaskIndex);
        }
        jsonMap.put("labelIndex", labelIndex);
        JsonUtil.writeJson(minMaxPath, jsonMap);
    }

    public int[] getUseColIndex() {
        return useColIndex;
    }

    int[][] getInputMaskIndex() {
        return inputMaskIndex;
    }

    public int[] getInputColIndex() {
        return inputColIndex;
    }

    public int getInputRowCount() {
        return inputRowCount;
    }

    public int[][] getLabelIndex() {
        return labelIndex;
    }
}
