package com.gly.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.gly.util.JsonUtil;

public class Data {
    private String inputPathName;
    private String minMaxPathName;
    private int[] inputIndex;      // 数组形式
    private int[] labelIndex;      // 数组形式
    private boolean shuffle;

    public Data() {
        inputPathName = "";
        minMaxPathName = "";
        inputIndex = null;
        labelIndex = null;
        shuffle = true;
    }

    public static Data parse(JsonNode jsonNode) {
        return JsonUtil.decode(jsonNode, Data.class);
    }

    public String getInputPathName() {
        return inputPathName;
    }

    public void setInputPathName(String inputPathName) {
        this.inputPathName = inputPathName;
    }

    public String getMinMaxPathName() {
        return minMaxPathName;
    }

    public void setMinMaxPathName(String minMaxPathName) {
        this.minMaxPathName = minMaxPathName;
    }

    public int[] getInputIndex() {
        return inputIndex;
    }

    public void setInputIndex(int[] inputIndex) {
        this.inputIndex = inputIndex;
    }

    public int[] getLabelIndex() {
        return labelIndex;
    }

    public void setLabelIndex(int[] labelIndex) {
        this.labelIndex = labelIndex;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }
}
