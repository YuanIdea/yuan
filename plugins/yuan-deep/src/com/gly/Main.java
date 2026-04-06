package com.gly;

import com.gly.mnist.Mnist;

public class Main {

    public static void main(String[] args) throws Exception {
        int type = 0;
        switch (type) {
            case 0:
                String modelName = "mlp";
                //modelName = "cnn";
                //modelName = "lenet5";
                //modelName = "lstm";

                String modelPath = "models/mnist/" + modelName;
                Mnist.train(modelPath + "/metadata.json");

                Use.predictWithModel(modelPath, "test-digit.png");
                break;
            case 1:
                Quick.train("models/quick/mlp/metadata.json");
                break;
            case 2:
                Train train = new Train();
                String root = "D:/WorkSpace/github/yuan/yuan-demo/quick/";
                train.init(root, root + "train.json", null);
                train.start();
                break;
        }
    }
}