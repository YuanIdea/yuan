package com.gly.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataUtil {
    public static Pair<float[][], float[][]> readToPairFloat(String filePath, int skip, int[] dataIndex, int[] labelIndex)  {
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

    public static float[][] hstack(Object... arrays) {
        if (arrays != null && arrays.length != 0) {
            Integer rows = null;
            for(Object arr : arrays) {
                if (arr instanceof float[][]) {
                    float[][] a = (float[][])arr;
                    if (rows == null) {
                        rows = a.length;
                    } else if (a.length != rows) {
                        throw new IllegalArgumentException("所有二维数组行数必须相等");
                    }
                } else {
                    if (!(arr instanceof float[])) {
                        throw new IllegalArgumentException("参数必须是 float[][] 或 float[] 类型");
                    }

                    float[] a = (float[])arr;
                    if (rows == null) {
                        rows = a.length;
                    } else if (a.length != rows) {
                        throw new IllegalArgumentException("一维数组长度必须等于二维数组行数");
                    }
                }
            }

            int totalCols = 0;

            for(Object arr : arrays) {
                if (arr instanceof float[][]) {
                    float[][] a = (float[][])arr;
                    if (a.length > 0) {
                        totalCols += a[0].length;
                    }
                } else if (arr instanceof float[]) {
                    ++totalCols;
                }
            }

            float[][] result = new float[rows][totalCols];

            for(int r = 0; r < rows; ++r) {
                int colIndex = 0;

                for(Object arr : arrays) {
                    if (arr instanceof float[][]) {
                        float[][] a = (float[][])arr;
                        System.arraycopy(a[r], 0, result[r], colIndex, a[r].length);
                        colIndex += a[r].length;
                    } else {
                        float[] a = (float[])arr;
                        result[r][colIndex] = a[r];
                        ++colIndex;
                    }
                }
            }

            return result;
        } else {
            return new float[0][0];
        }
    }
}
