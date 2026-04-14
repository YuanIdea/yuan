package com.gly.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.gly.util.JsonUtil;

public class Training {
    private String loss;
    private boolean accuracy;
    private int batchSize;
    private int epochs;
    private String saveModelPath;

    public Training() {
        loss = "";
        batchSize = 0;
        epochs = 0;
        accuracy = false;
        saveModelPath = "";
    }

    public static Training parse(JsonNode jsonNode) {
        return JsonUtil.decode(jsonNode, Training.class);
    }

    // getter / setter
    public String getLoss() {
        return loss;
    }

    public void setLoss(String loss) {
        this.loss = loss;
    }

    public boolean isAccuracy() {
        return accuracy;
    }

    public void setAccuracy(boolean accuracy) {
        this.accuracy = accuracy;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public String getSaveModelPath() {
        return saveModelPath;
    }

    public void setSaveModelPath(String saveModelPath) {
        this.saveModelPath = saveModelPath;
    }
}
