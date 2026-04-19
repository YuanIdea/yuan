package com.gly;

import com.gly.log.Logger;
import com.gly.util.ArrayUtils;
import com.gly.util.Coder;
import com.gly.util.Pair;

/**
 * 块数据集
 */
public class Dataset2 {
    // 原始数据 [总样本数][特征数]
    private final float[][] data;

    // 块的总行数。
    private final int inputRowCount;

    private final int[] inputColIndex;

    // 末行输入特征索引列表
    private final int[][] inputMaskIndex;

    // 末行标签索引位置列表
    private final int[][] labelIndex;

    /**
     * 构造函数。
     *
     * @param data      原始数据，不作编码处理，如果使用编码数据，在外部进行编码。
     * @param block 模块庶数据。
     */
    public Dataset2(float[][] data, BlockIndex block) {
        if (data == null || data.length == 0) { // 验证数据有效性
            Logger.error("Data cannot be null or empty");
            throw new IllegalArgumentException("Data cannot be null or empty");
        }
        this.data = data;
        this.inputColIndex = block.getInputColIndex();
        this.inputRowCount = block.getInputRowCount();
        this.inputMaskIndex = block.getInputMaskIndex();
        this.labelIndex = block.getLabelIndex();
    }

    /**
     * 获取所有滑动窗口样本和标签。
     *
     * @return 所有滑动窗口样本和标签。
     */
    public Pair<float[][], float[][]> getAllData() {
        return getData(true);
    }

    /**
     * 获得所有滑动窗口的输入数据。
     *
     * @return 编码数据序列。
     */
    public float[][] getAllInput() {
        return getData(false).first;
    }

    /**
     * 获取所有滑动窗口数据。
     *
     * @param useLabel 是否获得标签数据，true返回数据和标签，false只返回数据，标签为空。
     * @return 所有滑动窗口数据。
     */
    private Pair<float[][], float[][]> getData(boolean useLabel) {
        int numSequences = data.length - inputRowCount + 1;

        float[][] labels = null;
        if (useLabel) {
            labels = new float[numSequences][labelIndex.length];
            for (int i = 0; i < numSequences; ++i) {
                labels[i] = getOneLabel(i);
            }
        }

        float[][] sequences = new float[numSequences][inputRowCount * data[0].length];
        for (int i = 0; i < numSequences; ++i) {
            float[][] window = copy(data, i, i + inputRowCount, this.inputColIndex);// 获取当前窗口的样本
            sequences[i] = getOneInputFlatten(window, inputMaskIndex); // 获取单个输入块，平铺成一维。
        }
        return new Pair<>(sequences, labels);
    }

    private float[][] copy(float[][] data, int beginRow, int endRow, int[] inputColIndex) {
        int row = endRow - beginRow;
        int col = inputColIndex.length;
        float[][] result = new float[row][];
        for (int i=0; i < row; ++i) {
            result[i] = new float[col];
            for (int j=0; j < col; ++j) {
                result[i][j] = data[beginRow+i][inputColIndex[j]];
            }
        }
        return result;
    }

    /**
     * 获取一条块输入，最后一行的占位符号0，不进行F编码。
     *
     * @param window          原始的全信息块。
     * @param inputMaskIndex 末行标签索引位置列表，为空时全使用占位符。
     * @return 一条输入块。
     */
    private static float[][] getOneInput(float[][] window, int[][] inputMaskIndex) {
        if (inputMaskIndex != null) {
            for (int[] rc : inputMaskIndex) {
                window[rc[0]][rc[1]] = 0;
            }
        }
        return window;
    }

    /**
     * 获取一组标签。
     *
     * @param beginIndex 当前起始索引。
     * @return 一组输标签。
     */
    private float[] getOneLabel(int beginIndex) {
        int length = labelIndex.length;
        float[] labels = new float[length];
        for (int i = 0; i < length; ++i) {
            int[] rc = labelIndex[i];
            labels[i] = data[beginIndex + rc[0]][rc[1]];
        }
        return labels;
    }

    /**
     * 获得编码后的块输入集合。
     *
     * @param block          原始的未编码块输入集合。
     * @param inputMaskIndex 末行输入特征索引列表。
     * @param coder          编码器。
     * @return 编码后的块输入集合。
     */
    public static float[][] getEncodeInput(float[][][] block, int[][] inputMaskIndex, Coder coder) {
        int length = block.length;
        float[][] encodeInput = new float[length][block[0].length * block[0][0].length];
        for (int i = 0; i < length; ++i) {
            float[][] encodeOne = coder.encode(block[i]);
            encodeInput[i] = getOneInputFlatten(encodeOne, inputMaskIndex);
        }
        return encodeInput;
    }

    /**
     * 获取一条块输入，并平铺成一维，最后一行的占位符号0，不进行编码。
     *
     * @param block             原始的全信息块。
     * @param inputMaskIndex 末行标签索引位置列表，为空时全使用占位符。
     * @return 一条输入块。
     */
    private static float[] getOneInputFlatten(float[][] block, int[][] inputMaskIndex) {
        float[][] input = getOneInput(block, inputMaskIndex); // 获取单个输入块
        return ArrayUtils.flatten(input);
    }
}

