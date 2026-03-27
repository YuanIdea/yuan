package com.gly;

import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.convolutional.Conv2d;
import ai.djl.nn.core.Linear;
import ai.djl.nn.pooling.Pool;

public class Cnn {
    /**
     * 创建 CNN 网络结构（LeNet-5 风格）
     */
    public static SequentialBlock createCnnBlock() {
        SequentialBlock block = new SequentialBlock();
        block.add(Conv2d.builder()
                        .setKernelShape(new Shape(5, 5))
                        .optPadding(new Shape(2, 2))
                        .setFilters(6)
                        .build())
                .add(Activation::sigmoid)
                .add(Pool.avgPool2dBlock(new Shape(2, 2), new Shape(2, 2)))
                .add(Conv2d.builder()
                        .setKernelShape(new Shape(5, 5))
                        .setFilters(16)
                        .build())
                .add(Activation::sigmoid)
                .add(Pool.avgPool2dBlock(new Shape(2, 2), new Shape(2, 2)))
                .add(Blocks.batchFlattenBlock())
                .add(Linear.builder().setUnits(120).build())
                .add(Activation::sigmoid)
                .add(Linear.builder().setUnits(84).build())
                .add(Activation::sigmoid)
                .add(Linear.builder().setUnits(10).build());
        return block;
    }
}
