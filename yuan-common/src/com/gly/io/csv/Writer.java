package com.gly.io.csv;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Writer {

    /**
     * 将double[][]数据写入CSV文件，可设置表头（可为null表示无表头）
     *
     * @param data    要写入的二维double数组
     * @param filePath 写入的CSV文件路径
     * @param header  表头，字符串数组，每个元素对应一列列名；如果为null表示不写表头
     */
    public static <T> void writeArray(T[][] data, String filePath, String[] header) {
        try {
            Path path = Paths.get(filePath);
            // 创建父目录（如果不存在）
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (OutputStream os = new FileOutputStream(filePath);
                 OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                 BufferedWriter bw = new BufferedWriter(osw)) {

                // 写入UTF-8 BOM
                os.write(0xEF);
                os.write(0xBB);
                os.write(0xBF);

                // 写表头
                if (header != null && header.length > 0) {
                    bw.write(String.join(",", header));
                    bw.newLine();
                }

                // 写数据行
                for (T[] row : data) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < row.length; ++i) {
                        sb.append(row[i]);
                        if (i < row.length - 1) {
                            sb.append(",");
                        }
                    }
                    bw.write(sb.toString());
                    bw.newLine();
                }
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeDoubleArray(double[][] data, String filePath, String[] header) {
        try {
            Path path = Paths.get(filePath);
            // 创建父目录（如果不存在）
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (OutputStream os = new FileOutputStream(filePath);
                 OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                 BufferedWriter bw = new BufferedWriter(osw)) {

                // 写入UTF-8 BOM
                os.write(0xEF);
                os.write(0xBB);
                os.write(0xBF);

                // 写表头
                if (header != null && header.length > 0) {
                    bw.write(String.join(",", header));
                    bw.newLine();
                }

                // 写数据行
                for (double[] row : data) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < row.length; ++i) {
                        sb.append(row[i]);
                        if (i < row.length - 1) {
                            sb.append(",");
                        }
                    }
                    bw.write(sb.toString());
                    bw.newLine();
                }
                bw.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
