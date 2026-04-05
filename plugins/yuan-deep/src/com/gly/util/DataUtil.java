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
            int cols = ((float[])dataList.get(0)).length;
            float[][] result = new float[rows][cols];

            for(int i = 0; i < rows; ++i) {
                result[i] = (float[])dataList.get(i);
            }

            return result;
        }
    }
}
