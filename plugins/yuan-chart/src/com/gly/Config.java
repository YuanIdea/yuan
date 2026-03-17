package com.gly;

import com.gly.util.JsonUtil;

import java.io.File;

public class Config {
    private String actionType;
    private int[] inputIndex;
    private int[] rangeRows;
    private String inputPathName;
    private String labelX;
    private String labelY;
    private String title;
    private String[] legend;
    private float[] lineWidth;
    private String[] style;


    public Config() {
        rangeRows = null;
    }

    public int[] getInputIndex() {
        return inputIndex;
    }

    public void setInputIndex(int[] inputIndex) {
        this.inputIndex = inputIndex;
    }

    public int[] getRangeRows() {
        return rangeRows;
    }

    public void setRangeRows(int[] rangeRows) {
        this.rangeRows = rangeRows;
    }

    public String getInputPathName() {
        return inputPathName;
    }

    public void setInputPathName(String inputPathName) {
        this.inputPathName = inputPathName;
    }

    public String getLabelX() {
        return labelX;
    }

    public void setLabelX(String labelX) {
        this.labelX = labelX;
    }

    public String getLabelY() {
        return labelY;
    }

    public void setLabelY(String labelY) {
        this.labelY = labelY;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getLegend() {
        return legend;
    }

    public void setLegend(String[] legend) {
        this.legend = legend;
    }

    public float[] getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float[] lineWidth) {
        this.lineWidth = lineWidth;
    }

    public static Config loadFromJson(String filePath) {
        return JsonUtil.decode(new File(filePath), Config.class);
    }

    public String[] getStyle() {
        return style;
    }

    public void setStyle(String[] color) {
        this.style = color;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }
}

