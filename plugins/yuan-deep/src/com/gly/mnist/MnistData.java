package com.gly.mnist;

import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public class MnistData {
    public RandomAccessDataset trainDataset;
    public RandomAccessDataset testDataset;

    /**
     * 加载 MNIST 数据集，支持图像重塑和可选 one-hot 标签。
     *
     * @param engine      深度学习引擎（如 "PyTorch"）
     * @param batchSize   批次大小
     * @param targetShape 图像目标形状（不含 batch 维度），为 null 则保持原始形状
     * @param numClasses  类别数（用于 one-hot，若 oneHot 为 false 则忽略）
     * @param oneHot      是否将标签转为 one-hot 格式
     */
    public void loadData(String engine, int batchSize, Shape targetShape, int numClasses, boolean oneHot) {
        try {
            trainDataset = getDataset(Dataset.Usage.TRAIN, batchSize, engine, targetShape, numClasses, oneHot);
            testDataset = getDataset(Dataset.Usage.TEST, batchSize, engine, targetShape, numClasses, oneHot);
        } catch (Exception e) {
            System.err.println("Failed to load dataset: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Dataset loading failed", e); // 终止程序，暴露问题
        }
    }

    /**
     * 兼容旧版：不进行 one-hot 转换。
     */
    public void loadData(String engine, int batchSize, Shape targetShape) {
        loadData(engine, batchSize, targetShape, 0, false);
    }

    private static RandomAccessDataset getDataset(Dataset.Usage usage, int batchSize,
                                                  String engine, Shape targetShape,
                                                  int numClasses, boolean oneHot)
            throws IOException, TranslateException {

        RandomAccessDataset dataset;
        if (oneHot && numClasses > 0) {
            dataset = MnistOneHot.builder()
                    .optUsage(usage)
                    .optManager(NDManager.newBaseManager(engine))
                    .setSampling(batchSize, true)
                    .optLimit(Long.MAX_VALUE)
                    .optTargetShape(targetShape)
                    .optNumClasses(numClasses)
                    .build();
        } else {
            Pipeline pipeline = new Pipeline();
            if (targetShape != null) {
                pipeline.add((NDArray array) -> array.reshape(targetShape));
            }
            pipeline.add((NDArray array) -> array.div(255f));
            dataset = Mnist.builder()
                    .optUsage(usage)
                    .optManager(NDManager.newBaseManager(engine))
                    .setSampling(batchSize, true)
                    .optLimit(Long.MAX_VALUE)
                    .optPipeline(pipeline)
                    .build();
        }
        dataset.prepare(new ProgressBar());
        return dataset;
    }

}