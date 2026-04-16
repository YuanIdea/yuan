package com.gly.io.csv;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Writer {

    /**
     * Writes T[][] data to a CSV file, with an optional header (can be null for no header).
     *
     * @param data     The 2D array of type T to be written.
     * @param filePath The path of the CSV file to write to.
     * @param header   The header as an array of strings,
     *                 each element corresponds to a column name; if null, no header is written.
     */
    public static <T> void writeArray(T[][] data, String filePath, String[] header) {
        try {
            Path path = Paths.get(filePath);
            // Create the parent directory (if it does not exist).
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (OutputStream os = new FileOutputStream(filePath);
                 OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                 BufferedWriter bw = new BufferedWriter(osw)) {

                // Write UTF-8 BOM
                os.write(0xEF);
                os.write(0xBB);
                os.write(0xBF);

                // Write the header.
                if (header != null && header.length > 0) {
                    bw.write(String.join(",", header));
                    bw.newLine();
                }

                // Write the data row.
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
            // Create the parent directory (if it does not exist).
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (OutputStream os = new FileOutputStream(filePath);
                 OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                 BufferedWriter bw = new BufferedWriter(osw)) {

                // Write UTF-8 BOM
                os.write(0xEF);
                os.write(0xBB);
                os.write(0xBF);

                // Write the header.
                if (header != null && header.length > 0) {
                    bw.write(String.join(",", header));
                    bw.newLine();
                }

                // Write the data row.
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

    public static void writeFloatArray(float[][] data, String filePath, String[] header) {
        try {
            Path path = Paths.get(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (
                    OutputStream os = new FileOutputStream(filePath);
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
                    BufferedWriter bw = new BufferedWriter(osw);
            ) {
                os.write(239);
                os.write(187);
                os.write(191);
                if (header != null && header.length > 0) {
                    bw.write(String.join(",", header));
                    bw.newLine();
                }

                for (float[] row : data) {
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
