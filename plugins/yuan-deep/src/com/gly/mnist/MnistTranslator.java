package com.gly.mnist;

import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.Arrays;

public class MnistTranslator implements Translator<Image, Classifications> {
    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDManager manager = ctx.getNDManager();
        NDArray array = input.toNDArray(manager)
                .toType(DataType.FLOAT32, false)
                .div(255.0f)
                .mean(new int[]{2});              // (H,W,3) -> (H,W)

        Shape targetShape = getTargetShape(ctx);
        array = array.reshape(targetShape);
        return new NDList(array);
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) {
        // Post-process: convert model output NDArray to user-friendly Classifications
        NDArray probabilities = list.singletonOrThrow().softmax(0);
        return new Classifications(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"), probabilities);
    }

    private Shape getTargetShape(TranslatorContext ctx) {
        Shape[] inputShapes = ctx.getModel().getBlock().getInputShapes();
        Shape expectedShape = inputShapes[0];
        return removeBatchDimension(expectedShape);
    }

    /**
     * Removes the first dimension (batch) from a Shape.
     *
     * @param shape the original Shape (may contain batch dimension)
     * @return a new Shape with the batch dimension removed
     */
    public static Shape removeBatchDimension(Shape shape) {
        long[] dims = shape.getShape();
        if (dims.length <= 1) {
            return new Shape(); // empty shape
        }
        long[] newDims = new long[dims.length - 1];
        System.arraycopy(dims, 1, newDims, 0, newDims.length);
        return new Shape(newDims);
    }
}
