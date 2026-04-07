package com.gly;

import com.gly.mnist.Mnist;
import com.gly.mnist.MnistUse;
import com.gly.quick.Quick;
import com.gly.quick.RegressionUse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        int type = 1;
        switch (type) {
            case 0:
                mnist("mlp");
                //mnist("cnn");
                //mnist("lenet5");
                //mnist("lstm");
                break;
            case 1:
                quick();
                break;
            case 2:
                Train train = new Train();
                String root = "D:/WorkSpace/github/yuan/yuan-demo/quick/";
                train.init(root, root + "train.json", null);
                train.start();
                break;
        }
    }

    private static void mnist(String modelName) throws Exception {
        String modelPath = "models/mnist/" + modelName;
        Mnist.train(modelPath + "/metadata.json");

        List<String> nameList = new ArrayList<>();
        nameList.add("test/4.png");
        nameList.add("test/6.png");
        nameList.add("test/7.png");
        MnistUse.predictWithModel(modelPath, nameList);
    }

    private static void quick() throws Exception {
        Quick.train("models/quick/mlp/metadata.json");
        float[][] inputArray = new float[][]{{0.1f, 0.2f}, {0.3f, 0.4f}};
        float[][] outputs = RegressionUse.batchPredict("models/quick/mlp", inputArray);
        for (float[] output : outputs) {
            System.out.println("Predicted: " + Arrays.toString(output));
        }
    }
}