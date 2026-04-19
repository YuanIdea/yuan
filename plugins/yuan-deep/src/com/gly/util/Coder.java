package com.gly.util;

import com.gly.io.json.Json;

import java.util.Arrays;

public class Coder {
    private float[][] oriData;
    private float[] minData;
    private float[] maxData;
    private float[] range;

    /**
     * 编解码器构造函数。
     *
     * @param oriData 原始数据。
     */
    public Coder(float[][] oriData) {
        this.oriData = oriData;
        float[][] minMax = getMinMax(oriData);
        init(minMax[0], minMax[1]);
    }

    public Coder(float[] minData, float[] maxData) {
        init(minData, maxData);
    }

    /**
     * 根据json串创建编码器。
     *
     * @param minMax minMax的json串。
     * @param min    最小值数组对应key。
     * @param max    最大值数组对应key。
     * @return 编码器。
     */
    public static Coder generateCoder(Json minMax, String min, String max) {
        float[] minBlock = minMax.getFloatArray(min);
        float[] maxBlock = minMax.getFloatArray(max);
        return new Coder(minBlock, maxBlock);
    }

    /**
     * 编解码器构造函数。
     *
     * @param minData 所有列的最小值数组
     * @param maxData 所有列的最大值数组
     */
    private void init(float[] minData, float[] maxData) {
        this.minData = minData.clone();
        this.maxData = maxData.clone();
        this.range = new float[minData.length];
        for (int i = 0; i < minData.length; ++i) {
            this.range[i] = maxData[i] - minData[i];
        }
    }

    /**
     * 归一化编码（全列）。
     *
     * @param data 需要编码的二维数据
     * @return 编码后的二维数据
     */
    public float[][] encode(float[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        float[][] encoded = new float[rows][cols];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                encoded[i][j] = (data[i][j] - minData[j]) / range[j];
            }
        }
        return encoded;
    }

    public float[][] getEncode() {
        if (oriData != null) {
            return encode(oriData);
        } else {
            return null;
        }
    }

    /**
     * 获取数据每列的最小值和最大值。
     *
     * @param data 二维输入数据
     * @return 包含[minData, maxData]的列表
     */
    private static float[][] getMinMax(float[][] data) {
        int cols = data[0].length;
        float[] minData = new float[cols];
        float[] maxData = new float[cols];
        Arrays.fill(minData, Float.POSITIVE_INFINITY);
        Arrays.fill(maxData, Float.NEGATIVE_INFINITY);

        for (float[] row : data) {
            for (int j = 0; j < cols; ++j) {
                if (row[j] < minData[j]) minData[j] = row[j];
                if (row[j] > maxData[j]) maxData[j] = row[j];
            }
        }
        return new float[][]{minData, maxData};
    }

    /**
     * 解码（全列）。
     *
     * @param data 需要解码的二维数据
     * @return 解码后的二维数据
     */
    public float[][] decode(float[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        float[][] decoded = new float[rows][cols];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                decoded[i][j] = data[i][j] * range[j] + minData[j];
            }
        }
        return decoded;
    }

    public float[] getMinData() {
        return minData;
    }

    public float[] getMaxData() {
        return maxData;
    }

    /**
     * 只对选中列进行编码。
     *
     * @param data        原始二维数据
     * @param selectIndex 需要编码的列索引数组
     * @return 仅包含选中列编码后的二维数据
     */
    public float[][] encodePart(float[][] data, int[] selectIndex) {
        int rows = data.length;
        float[][] encoded = new float[rows][selectIndex.length];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < selectIndex.length; j++) {
                int colIdx = selectIndex[j];
                encoded[i][j] = (data[i][colIdx] - minData[colIdx]) / range[colIdx];
            }
        }
        return encoded;
    }

    /**
     * Decodes only the selected columns.
     *
     * @param data        The encoded 2D data (containing only the selected columns).
     * @param selectIndex The array of column indices to decode.
     * @return The decoded 2D data (containing only the selected columns).
     */
    public float[][] decodePart(float[][] data, int[][] selectIndex) {
        int rows = data.length;
        int cols = selectIndex.length;
        float[][] decoded = new float[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                int colIdx = selectIndex[j][1];
                decoded[i][j] = data[i][j] * range[colIdx] + minData[colIdx];
            }
        }
        return decoded;
    }
}