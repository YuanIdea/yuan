package com.gly.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ArrayUtils {

    /**
     * 支持二维double数组和一维double数组的水平拼接（类似 np.hstack）
     * 要求：
     * - 所有二维数组行数相同
     * - 所有一维数组长度等于行数
     * @param arrays 可传入 double[][] 或 double[] 的 Object
     * @return 拼接后的二维double数组
     */
    public static double[][] hstack(Object... arrays) {
        if (arrays == null || arrays.length == 0) {
            return new double[0][0];
        }

        // 先确定行数
        Integer rows = null;
        for (Object arr : arrays) {
            if (arr instanceof double[][]) {
                double[][] a = (double[][]) arr;
                if (rows == null)
                    rows = a.length;
                else if (a.length != rows)
                    throw new IllegalArgumentException("所有二维数组行数必须相等");
            } else if (arr instanceof double[]) {
                // 一维数组暂时不确定行数，先检查长度是否跟已有rows匹配
                double[] a = (double[]) arr;
                if (rows == null) rows = a.length;
                else if (a.length != rows)
                    throw new IllegalArgumentException("一维数组长度必须等于二维数组行数");
            } else {
                throw new IllegalArgumentException("参数必须是 double[][] 或 double[] 类型");
            }
        }

        // 计算总列数
        int totalCols = 0;
        for (Object arr : arrays) {
            if (arr instanceof double[][]) {
                double[][] a = (double[][]) arr;
                if (a.length > 0) totalCols += a[0].length;
            } else if (arr instanceof double[]) {
                totalCols += 1; // 一维数组每行1列
            }
        }

        double[][] result = new double[rows][totalCols];

        // 进行拼接
        for (int r = 0; r < rows; r++) {
            int colIndex = 0;
            for (Object arr : arrays) {
                if (arr instanceof double[][]) {
                    double[][] a = (double[][]) arr;
                    System.arraycopy(a[r], 0, result[r], colIndex, a[r].length);
                    colIndex += a[r].length;
                } else {  // double[]
                    double[] a = (double[]) arr;
                    result[r][colIndex] = a[r];
                    colIndex += 1;
                }
            }
        }

        return result;
    }

    /**
     * vstack: 垂直拼接，支持二维数组和一维数组混合，
     * 一维数组被当作一行数组处理，列数必须跟二维数组匹配。
     *
     * 要求：
     * - 所有二维数组列数相同
     * - 一维数组长度必须等于二维数组列数
     *
     * @param arrays 多个 double[][] 或 double[] 对象
     * @return 拼接后的二维数组
     */
    public static double[][] vstack(Object... arrays) {
        if (arrays == null || arrays.length == 0) {
            return new double[0][0];
        }
        // 先确定二维数组的列数
        Integer cols = null;
        for (Object arr : arrays) {
            if (arr instanceof double[][]) {
                double[][] a = (double[][]) arr;
                if (a.length > 0) {
                    int thisCols = a[0].length;
                    for (double[] row : a) {
                        if (row.length != thisCols) {
                            throw new IllegalArgumentException("二维数组行列数不一致");
                        }
                    }
                    if (cols == null) {
                        cols = thisCols;
                    } else if (!cols.equals(thisCols)) {
                        throw new IllegalArgumentException("所有二维数组列数必须相同");
                    }
                }
            } else if (arr instanceof double[]) {
                double[] a = (double[]) arr;
                if (cols == null) {
                    cols = a.length;
                } else if (a.length != cols) {
                    throw new IllegalArgumentException("一维数组长度需等于二维数组列数");
                }
            } else {
                throw new IllegalArgumentException("数组类型只支持 double[][] 和 double[]");
            }
        }

        if (cols == null) {
            return new double[0][0];
        }

        // 计算总行数（二维数组行数之和 + 一维数组个数）
        int totalRows = 0;
        for (Object arr : arrays) {
            if (arr instanceof double[][]) {
                totalRows += ((double[][]) arr).length;
            } else { // 一维数组视为一行
                totalRows += 1;
            }
        }

        double[][] result = new double[totalRows][cols];
        int rowIndex = 0;
        for (Object arr : arrays) {
            if (arr instanceof double[][]) {
                double[][] a = (double[][]) arr;
                for (double[] row : a) {
                    System.arraycopy(row, 0, result[rowIndex], 0, cols);
                    ++rowIndex;
                }
            } else {
                double[] a = (double[]) arr;
                System.arraycopy(a, 0, result[rowIndex], 0, cols);
                ++rowIndex;
            }
        }
        return result;
    }

    /**
     * 转置二维double数组
     *
     * @param matrix 原始二维数组
     * @return 转置后的二维数组
     */
    public static double[][] transpose(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            return new double[0][0];
        }
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] transposed = new double[cols][rows];
        for (int r = 0; r < rows; ++r) {
            // 可选：检查是否所有行列数一致
            if (matrix[r].length != cols) {
                throw new IllegalArgumentException("所有行必须有相同列数");
            }
            for (int c = 0; c < cols; ++c) {
                transposed[c][r] = matrix[r][c];
            }
        }
        return transposed;
    }

    /**
     * 按列索引提取二维数组部分列
     * @param data 原始二维数组，大小 m*n
     * @param cols 要提取的列索引数组，如 [0,1]
     * @return 新二维数组，大小 m*cols.length
     */
    public static double[][] selectColumns(double[][] data, int[] cols) {
        if (data == null || data.length == 0 || cols == null || cols.length == 0) {
            return new double[0][0];
        }
        int rows = data.length;
        int totalCols = data[0].length;

        // 校验cols中的索引有效性
        for (int c : cols) {
            if (c < 0 || c >= totalCols) {
                throw new IllegalArgumentException("列索引越界: " + c);
            }
        }

        double[][] result = new double[rows][cols.length];
        for (int r = 0; r < rows; ++r) {
            if (data[r].length != totalCols) {
                System.err.println(String.format("输入数组每行列数不一致,将跳过第%d行", r));
                continue;
            }
            for (int i = 0; i < cols.length; ++i) {
                result[r][i] = data[r][cols[i]];
            }
        }
        return result;
    }

    /**
     * 按列索引提取二维数组部分列
     * @param data 原始二维数组，大小 m*n
     * @param rows 要提取的列索引数组，如 [0,1]
     * @return 新二维数组，大小 m*cols.length
     */
    public static double[][] selectRows(double[][] data, int[] rows) {
        if (data == null || data.length == 0 || rows == null || rows.length == 0) {
            return new double[0][0];
        }

        int totalDataRows = data.length;
        int selectedRowsCount = rows.length;
        int cols = data[0].length;

        // 校验所有要选取的行索引是否有效
        for (int selectIndex : rows) {
            if (selectIndex < 0 || selectIndex >= totalDataRows) {
                throw new IllegalArgumentException("行索引越界: " + selectIndex);
            }
        }

        double[][] result = new double[selectedRowsCount][cols];
        for (int r = 0; r < selectedRowsCount; ++r) {
            System.arraycopy(data[rows[r]], 0, result[r], 0, cols);
        }

        return result;
    }

    public static double[] flatten(double[][] array) {
        // 处理null输入
        if (array == null)
            return new double[0];

        // 计算总元素个数
        int totalElements = 0;
        for (double[] row : array) {
            if (row != null) {
                totalElements += row.length;
            }
        }
        // 创建结果数组
        double[] result = new double[totalElements];
        int index = 0;
        // 填充数据
        for (double[] row : array) {
            if (row != null) {
                for (double num : row) {
                    result[index++] = num;
                }
            }
        }
        return result;
    }

    public static int[] toIntArray(List<Integer> list) {
        int length = list.size();
        int[] arr = new int[length];
        for (int i = 0; i < length; ++i) {
            arr[i] = list.get(i);  // 自动拆箱 Integer -> int
        }
        return arr;
    }

    /**
     * 转换List<double[]>为double[][]
     * @param dataList 数组列表。
     * @return 二维数组。
     */
    public static double[][] toArray(List<double[]> dataList) {
        int rows = dataList.size();
        if (rows == 0)
            return new double[0][0]; // 空数据返回
        int cols = dataList.get(0).length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; ++i) {
            result[i] = dataList.get(i);
        }
        return result;
    }

    public static int[] mergeUniqueArrays(int[] arr1, int[] arr2) {
        Set<Integer> set = new LinkedHashSet<>();
        for (int num : arr1) {
            set.add(num);
        }
        for (int num : arr2) {
            set.add(num);
        }
        // 转回int[]
        int[] result = new int[set.size()];
        int index = 0;
        for (Integer val : set) {
            result[index++] = val;
        }
        return result;
    }

    public static int[] merge(int[] arr1, int[] arr2) {
        int[] result = new int[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

    public static int[] range(int a, int b) {
        if (b <= a) {
            return new int[0]; // 空数组
        }
        int[] result = new int[b - a];
        for (int i = 0; i < result.length; ++i) {
            result[i] = a + i;
        }
        return result;
    }

    public static double[] rangeD(int a, int b) {
        if (b <= a) {
            return new double[0]; // 空数组
        }
        double[] result = new double[b - a];
        for (int i = 0; i < result.length; ++i) {
            result[i] = a + i;
        }
        return result;
    }
}
