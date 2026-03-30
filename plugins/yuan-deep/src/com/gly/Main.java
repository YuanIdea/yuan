package com.gly;

import java.awt.*;


public class Main {

    public static void main(String[] args) throws Exception {
        String modelName = "mlp";
        //modelName = "cnn";
        //modelName = "lenet5";
        //modelName = "lstm";

        String modelPath = "models/mnist/" + modelName;

        Train train = new Train();
        train.trainAndSaveModel(modelPath);

        Use.predictWithModel(modelPath, "test-digit.png");
    }
}