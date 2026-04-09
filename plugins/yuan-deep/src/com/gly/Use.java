package com.gly;

import com.gly.io.json.Json;
import com.gly.model.BaseExecutable;
import com.gly.util.Coder;
import com.gly.util.NetUtil;
import com.gly.util.PathUtil;

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
        float[][] x = json.getFloat2DArray("data");
        String minMaxPath = NetUtil.getMinMaxPath(root, json);
        Json minMax = new Json(minMaxPath);
        Coder coder = Coder.generateCoder(minMax, "dataMin", "dataMax");
        try {
            float[][] encodeResult = NetUtil.batchPredict(NetUtil.getModePath(root, json), coder.encode(x));
            Coder coderL = Coder.generateCoder(minMax, "labelMin", "labelMax");
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
