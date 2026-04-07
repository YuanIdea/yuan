package com.gly;

import com.gly.mnist.Mnist;

import java.util.ArrayList;
import java.util.List;

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

                List<String> nameList = new ArrayList<>();
                nameList.add("test/4.png");
                nameList.add("test/6.png");
                nameList.add("test/7.png");
                Use.predictWithModel(modelPath, nameList);
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