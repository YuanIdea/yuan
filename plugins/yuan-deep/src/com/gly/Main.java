package com.gly;

import com.gly.mnist.Mnist;

public class Main {

    public static void main(String[] args) throws Exception {
        String modelName = "mlp";
        //modelName = "cnn";
        //modelName = "lenet5";
        //modelName = "lstm";

        String modelPath = "models/mnist/" + modelName;
        Mnist.train(modelPath+"/metadata.json");
        Use.predictWithModel(modelPath, "test-digit.png");

//        Quick.train("models/quick/mlp");
        Train train = new Train();
        train.start();
    }
}