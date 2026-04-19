package com.gly.model.test;

import com.gly.BlockIndex;
import com.gly.Dataset2;
import com.gly.event.AddFileEvent;
import com.gly.event.GlobalBus;
import com.gly.io.csv.Reader;
import com.gly.io.csv.Writer;
import com.gly.io.json.Json;
import com.gly.model.BaseExecutable;
import com.gly.util.*;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Test extends BaseExecutable {
    @Override
    public void start() {
        String root = getRoot();
        String name = getName();

        Path rootPath = Paths.get(root);
        Json json = new Json(name);
        float[][] allData;
        Path absoluteSavePath;
        String[] newHeader;
        Path filePath = rootPath.resolve(json.getString("inputPathName"));
        if (json.has("block")) {
            BlockIndex block = new BlockIndex(json.getSubJson("block"));
            float[][] testAllData = Reader.readToFloatArray2(filePath.toString(), 1, block.getUseColIndex());
            String minMaxPath = MinMax.getMinMaxPath(root, json);

            if (testAllData != null) {
                Json minMax = new Json(minMaxPath);
                Coder coder = Coder.generateCoder(minMax, "blockMin", "blockMax");

                Dataset2 sd = new Dataset2(coder.encode(testAllData), block);
                float[][] encodeX = sd.getAllInput();
                try {
                    float[][] result = NetUtil.batchPredict(MinMax.getModePath(root, json), encodeX);
                    float[][] decodeResult = coder.decodePart(result, block.getLabelIndex());
                    int[] rows = ArrayUtils.range(block.getInputRowCount() - 1, testAllData.length);
                    float[][] inputData = DataUtil.selectRows(testAllData, rows);
                    allData = DataUtil.hstack(inputData, decodeResult);
                    absoluteSavePath = rootPath.resolve(json.getString("outputPathName"));
                    newHeader = getHeader(filePath, block.getUseColIndex(), block.getUseLabelIndex());
                    Writer.writeFloatArray(allData, absoluteSavePath.toString(), newHeader);
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

        } else {
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
                    allData = DataUtil.hstack(DataUtil.hstack(data, labels), result);
                    absoluteSavePath = rootPath.resolve(json.getString("outputPathName"));
                    int[] useIndex = ArrayUtils.merge(inputIndex, labelIndex);
                    newHeader = getHeader(filePath, useIndex, labelIndex);

                    Writer.writeFloatArray(allData, absoluteSavePath.toString(), newHeader);
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
    }

    /**
     * Get the output header file.
     *
     * @param path       The original file path name.
     * @param useIndex The input index.
     * @param labelIndex The label index.
     * @return The output header file.
     */
    private String[] getHeader(Path path, int[] useIndex, int[] labelIndex) {
        String encoding = Encoding.detectCharset(path);
        String[] header = Reader.readLine(path.toString(), 0, encoding);
        String[] newHeader = null;
        if (header != null) {
            int totalLen = useIndex.length;
            int labelLen = labelIndex.length;
            int notLabel = totalLen - labelLen;
            newHeader = new String[totalLen + labelLen];
            for (int i = 0; i < totalLen; ++i) {
                if (i < notLabel) {
                    newHeader[i] = header[useIndex[i]];
                } else {
                    newHeader[i] = header[labelIndex[i - notLabel]];
                }
            }
            for (int j = 0; j < labelLen; ++j) {
                newHeader[j + totalLen] = "预测" + header[labelIndex[j]];
            }
        }
        return newHeader;
    }

    @Override
    public void stop() {

    }

    @Override
    public Object getResult() {
        return null;
    }
}
