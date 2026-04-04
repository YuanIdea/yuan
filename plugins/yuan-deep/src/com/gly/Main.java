package com.gly;

import com.gly.mnist.Mnist;

public class Main {

    public static void main(String[] args) throws Exception {
        String modelName = "mlp";
        //modelName = "cnn";
        //modelName = "lenet5";
        //modelName = "lstm";

        String modelPathName = "models/mnist/" + modelName + "/metadata.json";
        Mnist.train(modelPathName);
        Use.predictWithModel(modelPathName, "test-digit.png");

        Quick.train("models/quick/mlp/metadata.json");

//        Train train = new Train();
//        train.start();
    }
}