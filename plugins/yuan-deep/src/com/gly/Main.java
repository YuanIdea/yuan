package com.gly;

import ai.djl.engine.Engine;

import java.awt.*;


public class Main {

    public static void main(String[] args) throws Exception {
        // Print the current engine.
        System.out.println("Currently used engine: " + Engine.getDefaultEngineName());

        //String modelName = "mlp-mnist";
        //String modelName = "cnn-mnist";
        String modelName = "lstm-mnist";

        String model = "models/" + modelName;
        Train train = new Train();
        train.fit(model + "/metadata.json", modelName);
        Use.predictWithModel(model, "test-digit.png");
    }
}