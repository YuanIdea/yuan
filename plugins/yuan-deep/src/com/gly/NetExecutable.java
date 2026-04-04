package com.gly;

import com.gly.io.json.Json;
import com.gly.model.BaseExecutable;
import com.gly.util.PathUtil;

import java.io.File;

public abstract class NetExecutable extends BaseExecutable {
//    /**
//     * 加载数据集。
//     * @param inputPathName 输入数据集名称。
//     * @param inputIndex 输入数据索引值。
//     * @param labelIndex 标签索引值。
//     * @return 加载的数据集。
//     */
//    public Dataset loadDataSet(String inputPathName, int[] inputIndex, int[] labelIndex) {
//        Dataset dataSet = new Dataset();
//        dataSet.read(PathUtil.resolveAbsolutePath(getRoot(), inputPathName), 1, inputIndex, labelIndex);
//        return dataSet;
//    }

    public String getMinMaxPath(Json json, File model) {
        String minMaxPath;
        if (json.has("minMaxPathName")) {
            minMaxPath = PathUtil.resolveAbsolutePath(getRoot(), json.getString("minMaxPathName"));
        } else {
            minMaxPath = model.getParent() + "/minMax.json";
        }
        return minMaxPath;
    }


}
