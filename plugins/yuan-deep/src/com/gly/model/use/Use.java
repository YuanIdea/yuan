package com.gly.model.use;

import com.gly.Dataset2;
import com.gly.io.json.Json;
import com.gly.model.BaseExecutable;
import com.gly.util.Coder;
import com.gly.util.MinMax;
import com.gly.util.NetUtil;

import java.util.Arrays;

public class Use extends BaseExecutable {
    private float[][] result;

    public Use() {

    }

    @Override
    public void start() {
        String jsonName = getName();
        String root = getRoot();
        Json json = new Json(jsonName);
        String minMaxPath = MinMax.getMinMaxPath(root, json);
        Json minMax = new Json(minMaxPath);
        if (json.has("block")) {
            Coder coder = Coder.generateCoder(minMax, "blockMin", "blockMax");
            float[][][] block = json.getFloat3DArray("block");
            float[][] encodeX = Dataset2.getEncodeInput(block, minMax.getInt2DArray("maskIndex"), coder);
            try {
                float[][] encodeResult = NetUtil.batchPredict(MinMax.getModePath(root, json), encodeX);
                int[][] labelIndex = minMax.getInt2DArray("labelIndex");
                result = coder.decodePart(encodeResult, labelIndex);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else {
            float[][] x = json.getFloat2DArray("data");
            Coder coder = Coder.generateCoder(minMax, "dataMin", "dataMax");
            try {
                float[][] encodeResult = NetUtil.batchPredict(MinMax.getModePath(root, json), coder.encode(x));
                Coder coderL = Coder.generateCoder(minMax, "labelMin", "labelMax");
                result = coderL.decode(encodeResult);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
        if (result != null) {
            StringBuilder strResult = new StringBuilder(json.getString("modelPath") + " predict:");
            for (float[] one : result) {
                strResult.append(Arrays.toString(one));
            }
            System.out.println(strResult);
        }
        setDone(true);
    }

    @Override
    public void stop() {

    }

    /**
     * Get the prediction result.
     *
     * @return The prediction result.
     */
    @Override
    public Object getResult() {
        return result;
    }
}
