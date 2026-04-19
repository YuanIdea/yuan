package com.gly.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataUtil {
    public static Pair<float[][], float[][]> readToPairFloat(String filePath, int skip, int[] dataIndex, int[] labelIndex) {
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

        for (int i = 0; i < length; ++i) {
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
            for (int i = 0; i < rows; ++i) {
                result[i] = dataList.get(i);
            }
            return result;
        }
    }

    public static float[][] hstack(Object... arrays) {
        if (arrays != null && arrays.length != 0) {
            Integer rows = null;
            for (Object arr : arrays) {
                if (arr instanceof float[][]) {
                    float[][] a = (float[][]) arr;
                    if (rows == null) {
                        rows = a.length;
                    } else if (a.length != rows) {
                        throw new IllegalArgumentException("所有二维数组行数必须相等");
                    }
                } else {
                    if (!(arr instanceof float[])) {
                        throw new IllegalArgumentException("参数必须是 float[][] 或 float[] 类型");
                    }

                    float[] a = (float[]) arr;
                    if (rows == null) {
                        rows = a.length;
                    } else if (a.length != rows) {
                        throw new IllegalArgumentException("一维数组长度必须等于二维数组行数");
                    }
                }
            }

            int totalCols = 0;

            for (Object arr : arrays) {
                if (arr instanceof float[][]) {
                    float[][] a = (float[][]) arr;
                    if (a.length > 0) {
                        totalCols += a[0].length;
                    }
                } else if (arr instanceof float[]) {
                    ++totalCols;
                }
            }

            float[][] result = new float[rows][totalCols];

            for (int r = 0; r < rows; ++r) {
                int colIndex = 0;

                for (Object arr : arrays) {
                    if (arr instanceof float[][]) {
                        float[][] a = (float[][]) arr;
                        System.arraycopy(a[r], 0, result[r], colIndex, a[r].length);
                        colIndex += a[r].length;
                    } else {
                        float[] a = (float[]) arr;
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

    /**
     * Extracts specific columns from a 2D array by column index.
     *
     * @param data The original 2D array of size m*n.
     * @param cols The array of column indices to extract, e.g., [0, 1].
     * @return A new 2D array of size m * cols.length.
     */
    public static float[][] selectColumns(float[][] data, int[] cols) {
        if (data == null || data.length == 0 || cols == null || cols.length == 0) {
            return new float[0][0];
        }
        int rows = data.length;
        int totalCols = data[0].length;

        // Validate the validity of indices in the cols array.
        for (int c : cols) {
            if (c < 0 || c >= totalCols) {
                throw new IllegalArgumentException("Column index out of bounds:" + c);
            }
        }

        float[][] result = new float[rows][cols.length];
        for (int r = 0; r < rows; ++r) {
            if (data[r].length != totalCols) {
                System.err.println(String.format("The number of columns in each row of the input array is inconsistent. " +
                        "Row %d will be skipped.", r));
                continue;
            }
            for (int i = 0; i < cols.length; ++i) {
                result[r][i] = data[r][cols[i]];
            }
        }
        return result;
    }

    /**
     * Extracts specific rows from a 2D array by row index.
     *
     * @param data The original 2D array of size m*n.
     * @param rows The array of row indices to extract, e.g., [0, 1].
     * @return A new 2D array of size rows.length * n.
     */
    public static float[][] selectRows(float[][] data, int[] rows) {
        if (data == null || data.length == 0 || rows == null || rows.length == 0) {
            return new float[0][0];
        }

        int totalDataRows = data.length;
        int selectedRowsCount = rows.length;
        int cols = data[0].length;

        // Validate whether all selected row indices are valid.
        for (int selectIndex : rows) {
            if (selectIndex < 0 || selectIndex >= totalDataRows) {
                throw new IllegalArgumentException("Row index out of bounds: " + selectIndex);
            }
        }

        float[][] result = new float[selectedRowsCount][cols];
        for (int r = 0; r < selectedRowsCount; ++r) {
            System.arraycopy(data[rows[r]], 0, result[r], 0, cols);
        }

        return result;
    }
}
