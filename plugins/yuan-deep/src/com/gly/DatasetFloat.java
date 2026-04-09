package com.gly;

import com.gly.util.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatasetFloat {
    private float[][] data;
    private float[][] label;

    public void read(String filePath, int skip, int[] dataIndex, int[] labelIndex)  {
        Pair<float[][], float[][]> pair = readToPair(filePath, skip, dataIndex, labelIndex);
        if (pair != null) {
            data = pair.first;
            label = pair.second;
        }
    }

    public static Pair<float[][], float[][]> readToPair(String filePath, int skip, int[] dataIndex, int[] labelIndex)  {
        try {
            List<float[]> dataList = new ArrayList<>();
            List<float[]> labelList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = -1;

                while ((line = br.readLine()) != null) {
                    ++lineNumber;
                    if (lineNumber < skip) {
                        continue; // 跳过
                    }

                    String[] tokens = line.split(",");// 分割
                    dataList.add(getRow(tokens, dataIndex));
                    labelList.add(getRow(tokens, labelIndex));
                }
            }
            return new Pair<>(toArray(dataList), toArray(labelList));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public float[][] getData() {
        return data;
    }

    public float[][] getTransposeData() {
        return transpose(data);
    }

    public float[][] getLabel() {
        return label;
    }

    public float[] getFlattenLabel() {
        return flatten(label);
    }

    public static float[] getRow(String[] tokens, int[] dataIndex) {
        int length = dataIndex.length;
        float[] row = new float[length];

        for(int i = 0; i < length; ++i) {
            row[i] = Float.parseFloat(tokens[dataIndex[i]].trim());
        }

        return row;
    }

    public static float[][] toArray(List<float[]> dataList) {
        int rows = dataList.size();
        if (rows == 0) {
            return new float[0][0];
        } else {
            int cols = dataList.get(0).length;
            float[][] result = new float[rows][cols];

            for(int i = 0; i < rows; ++i) {
                result[i] = dataList.get(i);
            }

            return result;
        }
    }

    public static float[][] transpose(float[][] matrix) {
        if (matrix != null && matrix.length != 0) {
            int rows = matrix.length;
            int cols = matrix[0].length;
            float[][] transposed = new float[cols][rows];

            for(int r = 0; r < rows; ++r) {
                if (matrix[r].length != cols) {
                    throw new IllegalArgumentException("所有行必须有相同列数");
                }

                for(int c = 0; c < cols; ++c) {
                    transposed[c][r] = matrix[r][c];
                }
            }

            return transposed;
        } else {
            return new float[0][0];
        }
    }

    public static float[] flatten(float[][] array) {
        if (array == null) {
            return new float[0];
        } else {
            int totalElements = 0;

            for(float[] row : array) {
                if (row != null) {
                    totalElements += row.length;
                }
            }

            float[] result = new float[totalElements];
            int index = 0;

            for(float[] row : array) {
                if (row != null) {
                    for(float num : row) {
                        result[index++] = num;
                    }
                }
            }

            return result;
        }
    }
}
