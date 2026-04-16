package com.gly;

import com.gly.event.AddFileEvent;
import com.gly.event.GlobalBus;
import com.gly.io.csv.Reader;
import com.gly.io.json.Json;
import com.gly.model.BaseExecutable;
import com.gly.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Test extends BaseExecutable {
    @Override
    public void start() {
        String root = getRoot();
        String name = getName();

        Path rootPath = Paths.get(root);
        Json json = new Json(name);
        Path filePath = rootPath.resolve(json.getString("inputPathName"));
        int[] inputIndex = json.getIntArray("inputIndex");
        int[] labelIndex = json.getIntArray("labelIndex");
        Pair<float[][], float[][]> pair = DataUtil.readToPairFloat(filePath.toString(), 1, inputIndex, labelIndex);
        if (pair != null) {
            String minMaxPath = MinMax.getMinMaxPath(root, json);
            Json minMax = new Json(minMaxPath);
            float[][] data = pair.first;
            Coder coder = Coder.generateCoder(minMax, "dataMin", "dataMax");
            float[][] testData = coder.encode(data);
            try {
                float[][] encodeResult = NetUtil.batchPredict(MinMax.getModePath(root, json), testData);
                float[][] labels = pair.second;
                Coder coderL = Coder.generateCoder(minMax, "labelMin", "labelMax");
                float[][] result = coderL.decode(encodeResult);
                float[][] allData = DataUtil.hstack(DataUtil.hstack(data, labels), result);
                Path absoluteSavePath = rootPath.resolve(json.getString("outputPathName"));
                String[] newHeader = getHeader(filePath, inputIndex, labelIndex);

                writeFloatArray(allData, absoluteSavePath.toString(), newHeader);
                System.out.println("Generate test data:" + absoluteSavePath);
                Path parent = PathUtil.findExistingParent(absoluteSavePath);
                if (parent != null) {
                    GlobalBus.dispatch(new AddFileEvent((parent.toFile())));
                }
                setDone(true);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the output header file.
     *
     * @param path       The original file path name.
     * @param inputIndex The input index.
     * @param labelIndex The label index.
     * @return The output header file.
     */
    private String[] getHeader(Path path, int[] inputIndex, int[] labelIndex) {
        String encoding = Encoding.detectCharset(path);
        String[] header = Reader.readLine(path.toString(), 0, encoding);
        String[] newHeader = null;
        if (header != null) {
            int inputLen = inputIndex.length;
            int labelLen = labelIndex.length;
            int totalLen = inputLen + labelLen;
            newHeader = new String[totalLen + labelLen];
            for (int i = 0; i < totalLen; ++i) {
                if (i < inputLen) {
                    newHeader[i] = header[inputIndex[i]];
                } else {
                    newHeader[i] = header[labelIndex[i - inputLen]];
                }
            }
            for (int j = 0; j < labelLen; ++j) {
                newHeader[j + totalLen] = "预测" + header[labelIndex[j]];
            }
        }
        return newHeader;
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

    @Override
    public void stop() {

    }

    @Override
    public Object getResult() {
        return null;
    }
}
