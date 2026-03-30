package com.gly;

import java.awt.*;


public class Main {

    public static void main(String[] args) throws Exception {
        String modelName = "mlp-mnist";
        //String modelName = "cnn-mnist";
        //String modelName = "lstm-mnist";
        String modelPath = "models/" + modelName;

        Train train = new Train();
        train.trainAndSaveModel(modelPath);

        Use.predictWithModel(modelPath, "test-digit.png");
    }
}