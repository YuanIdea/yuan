package com.gly;

import java.awt.*;


public class Main {

    public static void main(String[] args) throws Exception {
        String modelName = "mlp-mnist";
        //String modelName = "cnn-mnist";
        //String modelName = "lstm-mnist";

        String model = "models/" + modelName;
        Train train = new Train();
        train.fit(model + "/metadata.json");
        Use.predictWithModel(model, "test-digit.png");
    }
}