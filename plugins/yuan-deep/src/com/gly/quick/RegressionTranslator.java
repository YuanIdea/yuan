package com.gly.quick;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

/**
 * A translator for regression tasks that processes float[] inputs and outputs.
 * Assumes the model expects a 1D input (features) and outputs a 1D array (predictions).
 */
public class RegressionTranslator implements Translator<float[], float[]> {

    private final Shape inputShape;   // e.g., new Shape(featureDim)
    private final Shape outputShape;  // e.g., new Shape(outputDim)

    /**
     * Constructs a RegressionTranslator with explicit input and output shapes.
     *
     * @param inputShape  shape of a single input sample (without batch dimension)
     * @param outputShape shape of a single output sample (without batch dimension)
     */
    public RegressionTranslator(Shape inputShape, Shape outputShape) {
        this.inputShape = inputShape;
        this.outputShape = outputShape;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, float[] input) {
        NDManager manager = ctx.getNDManager();
        // Convert float[] to NDArray with the specified shape
        NDArray array = manager.create(input, inputShape);
        // Ensure data type is float32 (manager.create defaults to float32 for float[])
        return new NDList(array);
    }

    @Override
    public float[] processOutput(TranslatorContext ctx, NDList list) {
        NDArray array = list.singletonOrThrow();
        // Reshape to outputShape if necessary (e.g., if batch dimension is included)
        long[] actualShape = array.getShape().getShape();
        if (actualShape.length == 2 && actualShape[0] == 1) {
            // Remove batch dimension: (1, outputDim) -> (outputDim)
            array = array.squeeze(0);
        }
        // If shape doesn't match outputShape, attempt to reshape
        if (!array.getShape().equals(outputShape)) {
            array = array.reshape(outputShape);
        }
        // Convert to float[]
        return array.toFloatArray();
    }
}
