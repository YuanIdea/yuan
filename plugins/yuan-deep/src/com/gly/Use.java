package com.gly;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.types.Shape;
import com.gly.io.json.Json;
import com.gly.model.BaseExecutable;
import com.gly.util.Coder;
import com.gly.util.PathUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Use extends BaseExecutable {
    private float[][] result;

    public Use() {

    }

    @Override
    public void start() {
        String jsonName = getName();
        Json json = new Json(jsonName);
        float[][] x = json.getFloat2DArray("data");
        String minMaxPath = getMinMaxPath(json);
        Json minMax = new Json(minMaxPath);
        Coder coder = generateCoder(minMax, "dataMin", "dataMax");
        try {
            float[][] encodeResult = batchPredict(getModePath(json), coder.encode(x));
            Coder coderL = generateCoder(minMax, "labelMin", "labelMax");
            result = coderL.decode(encodeResult);
            if (result != null) {
                StringBuilder strResult = new StringBuilder(json.getString("modelPath") + " predict:");
                for (float[] one : result) {
                    strResult.append(Arrays.toString(one));
                }
                System.out.println(strResult);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static float[][] batchPredict(String modelPath, float[][] inputArray) throws Exception {
        Path modelDir = Paths.get(modelPath);
        if (!Files.exists(modelDir) || !Files.isDirectory(modelDir)) {
            throw new IOException("Model directory does not exist: " + modelDir.toAbsolutePath());
        }
        Json json = new Json(modelPath + "/config.json");
        List<float[]> outputs;
        try (Model model = ModelBuilder.load(modelDir, json)) {
            Shape inputShape = ModelBuilder.parseShape(json.getRootNode().get("inputShape"));
            RegressionTranslator translator = new RegressionTranslator(inputShape);
            try (Predictor<float[], float[]> predictor = model.newPredictor(translator)) {
                List<float[]> inputList = Arrays.asList(inputArray);
                outputs = predictor.batchPredict(inputList);
            }
        }
        return outputs.toArray(new float[0][]);
    }

    /**
     * 根据json串创建编码器。
     *
     * @param minMax minMax的json串。
     * @param min    最小值数组对应key。
     * @param max    最大值数组对应key。
     * @return 编码器。
     */
    public static Coder generateCoder(Json minMax, String min, String max) {
        float[] minBlock = minMax.getFloatArray(min);
        float[] maxBlock = minMax.getFloatArray(max);
        return new Coder(minBlock, maxBlock);
    }

    public String getMinMaxPath(Json json) {
        String modelPath = getModePath(json);
        return modelPath + "/minMax.json";
    }

    public String getModePath (Json json) {
        String root = getRoot();
        String modelPath = json.getString("modelPath");
        return PathUtil.resolveAbsolutePath(root, modelPath);
    }

    @Override
    public void stop() {

    }

    /**
     * 获得预测结果。
     *
     * @return 预测结果。
     */
    @Override
    public Object getResult() {
        return result;
    }
}
