package com.gly.io.csv;

import com.gly.log.Logger;
import com.gly.util.ArrayUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV读取器。
 */
public class Reader {

    /**
     * 读取CSV文件中的数据为double[][]，跳过前skip行。
     *
     * @param filePath CSV文件路径名。
     * @param skip 跳过的行数（一般用于跳过表头）。
     * @param cols 先择的列索引。
     * @return double[][] 数据数组。
     */
    public static double[][] readToDoubleArray2(String filePath, int skip, int[] cols)  {
        try {
            List<double[]> dataList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = -1;

                while ((line = br.readLine()) != null) {
                    ++lineNumber;
                    if (lineNumber < skip) {
                        continue; // 跳过
                    }
                    String[] tokens = line.split(",");// 分割
                    dataList.add(getRow(tokens, cols));
                }
            }
            return ArrayUtils.toArray(dataList);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 读取CSV文件中的数据为double[][]，读取[begin, end)行， index列。
     * @param filePath CSV文件路径。
     * @param begin 起始行索引。
     * @param end 终止行索引。
     * @param cols 选择列。
     * @return 二维数据。
     */
    public static double[][] readToDoubleArray2(String filePath, int begin, int end, int[] cols)  {
        try {
            List<double[]> dataList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = -1;
                while ((line = br.readLine()) != null && ++lineNumber < end) {
                    if (lineNumber < begin) {
                        continue; // 跳过
                    }
                    String[] tokens = line.split(",");// 分割
                    dataList.add(getRow(tokens, cols));
                }
            }
            return ArrayUtils.toArray(dataList);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 将指定索引的数据String[]转换成double[]。
     * @param tokens 需要挑选列的原始字符串数据。
     * @param dataIndex 挑选数据索引值。
     * @return 挑选列对应的double[]。
     */
    public static double[] getRow(String[] tokens, int[] dataIndex) {
        int length = dataIndex.length;
        double[] row = new double[length];// 每行数据转换为double[]
        for (int i = 0; i < length; ++i) {
            row[i] = Double.parseDouble(tokens[dataIndex[i]].trim());// 去除字符串两端空白后转换
        }
        return row;
    }

    /**
     * 读取CSV文件中的数据为String[][]，跳过前skip行。
     *
     * @param filePath CSV文件路径名。
     * @param skip 跳过的行数（一般用于跳过表头）。
     * @param cols 先择的列索引。
     * @return String[][] 数据数组。
     */
    public static String[][] readToStringArray2(String filePath, int skip, int[] cols) {
        try {
            List<String[]> dataList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = -1;

                while ((line = br.readLine()) != null) {
                    ++lineNumber;
                    if (lineNumber < skip) {
                        continue; // 跳过前几行
                    }
                    if (lineNumber == 0) {
                        line = removeBom(line);
                    }
                    String[] tokens = line.split(","); // 直接拆成字符串数组
                    int length = cols.length;
                    String[] row = new String[length];
                    for (int i = 0; i < length; ++i) {
                        row[i] = tokens[cols[i]].trim();
                    }
                    dataList.add(row);
                }
            }
            int rowCount = dataList.size();
            if (rowCount == 0)
                return new String[0][0]; // 空数据返回
            int colCount = dataList.get(0).length;

            // 构造二维数组
            String[][] result = new String[rowCount][colCount];
            for (int i = 0; i < rowCount; ++i) {
                result[i] = dataList.get(i);
            }
            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 读取CSV文件中的数据为double[][]，读取[begin, end)行， index列。
     * @param filePath CSV文件路径。
     * @param begin 起始行索引。
     * @param end 终止行索引。
     * @param cols 选择列。
     * @return 二维数据。
     */
    public static String[][] readToStringArray2(String filePath, int begin, int end, int[] cols) {
        try {
            List<String[]> dataList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = -1;
                while ((line = br.readLine()) != null && ++lineNumber < end) {
                    if (lineNumber < begin) {
                        continue; // 跳过前几行
                    }
                    if (lineNumber == 0) {
                        line = removeBom(line);
                    }
                    String[] tokens = line.split(","); // 直接拆成字符串数组
                    int length = cols.length;
                    String[] row = new String[length];
                    for (int i = 0; i < length; ++i) {
                        row[i] = tokens[cols[i]].trim();
                    }
                    dataList.add(row);
                }
            }
            int rowCount = dataList.size();
            if (rowCount == 0)
                return new String[0][0]; // 空数据返回
            int colCount = dataList.get(0).length;

            // 构造二维数组
            String[][] result = new String[rowCount][colCount];
            for (int i = 0; i < rowCount; ++i) {
                result[i] = dataList.get(i);
            }
            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 将String[][]数据中指定的列索引选择出来，并转化为double[][]。
     * @param data 原始数据。
     * @param cols 选中的列。
     * @return 选中数据。
     */
    public static double[][] selectToDoubleColumns(String[][] data, int[] cols) {
        if (data == null || data.length == 0 || cols == null || cols.length == 0) {
            return new double[0][0];
        }

        int rows = data.length;
        int totalCols = data[0].length;

        // 校验索引有效性
        for (int c : cols) {
            if (c < 0 || c >= totalCols) {
                throw new IllegalArgumentException("列索引越界: " + c);
            }
        }

        double[][] result = new double[rows][cols.length];
        for (int r = 0; r < rows; ++r) {
            if (data[r].length != totalCols) {
                System.err.println(String.format("输入数组每行列数不一致，将跳过第%d行", r));
                continue;
            }
            for (int i = 0; i < cols.length; ++i) {
                String val = data[r][cols[i]];
                try {
                    result[r][i] = Double.parseDouble(val.trim());
                } catch (NumberFormatException e) {
                    System.err.println(String.format("第%d行第%d列转换double出错，内容：'%s'，默认赋值0", r, cols[i], val));
                    result[r][i] = 0d; // 出错时默认值
                }
            }
        }
        return result;
    }

    /**
     * 读取 CSV 文件指定行，自动处理 BOM 和不可见字符。
     * @param filePath 文件路径名。
     * @param targetLine 目标行号 (从0开始)。
     * @return 清理后的字符串数组，若行不存在返回null。
     */
    public static String[] readLine(String filePath, int targetLine, String encode) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), encode))) {
            String line;
            int effectiveLine = -1;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // 首行处理 BOM
                if (isFirstLine) {
                    line = removeBom(line);
                    isFirstLine = false;
                }
                effectiveLine++;
                if (effectiveLine == targetLine) {
                    return cleanAndSplitLine(line);
                }
            }
            Logger.error("错误：目标行 " + targetLine + " 超出文件范围");
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 读取一行数据存储在字符串数组中。
     * @param filePath CSV文件路径名。
     * @param targetLine 行索引从0开始。
     * @param cols 选中的列索引。
     * @return 第rowIndex行索引。
     */
    public static String[] readLine(String filePath, int targetLine, int[] cols) {
        String[] mat = readLine(filePath, targetLine, "UTF-8");
        if (mat != null) {
            int length = cols.length;
            String[] row = new String[length];
            for (int i = 0; i < length; ++i) {
                row[i] = mat[cols[i]].trim();// 去除字符串两端空白后转换
            }
            return row;
        } else {
            return null;
        }
    }

    /**
     * 去除 UTF-8 BOM 字符
     * @param line 要清理的字符串。
     * @return 清理后的字符串。
     */
    private static String removeBom(String line) {
        if (line.startsWith("\uFEFF")) {
            return line.substring(1);
        }
        return line;
    }

    /**
     * 清理不可见字符并分割字段。
     * @param line 要清理的字符串。
     * @return 清理后的字符串。
     */
    private static String[] cleanAndSplitLine(String line) {
        // 移除所有控制字符和BOM残留
        String cleaned = line.replaceAll("[\\p{C}]", "");
        // 处理带引号的字段 (可选)
        cleaned = cleaned.replaceAll("\"", "");
        return cleaned.split("\\s*,\\s*"); // 允许逗号前后有空格
    }
}
