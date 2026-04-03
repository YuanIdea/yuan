package com.gly.mnist;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.nn.recurrent.LSTM;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingResult;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.SaveModelTrainingListener;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.IOException;

public final class TrainMnistWithLSTM {

    private final static int epoch = 5;
    private final static int batchSize = 64;
    private final static String engine = "PyTorch";
    private final static String outputDir = "list-mnist";
    private final static Device[] maxGpus = new Device[]{Device.cpu()};
    private final static int limit = 1000;

    private TrainMnistWithLSTM() {
    }

    public static void main(String[] args) throws IOException, TranslateException {
        TrainMnistWithLSTM.runExample(args);
    }

    public static TrainingResult runExample(String[] args) throws IOException, TranslateException {


        try (Model model = Model.newInstance("lstm", engine)) {
            model.setBlock(getLSTMModel());

            // get training and validation dataset
            RandomAccessDataset trainingSet = getDataset(Dataset.Usage.TRAIN);
            RandomAccessDataset validateSet = getDataset(Dataset.Usage.TEST);

            // setup training configuration
            DefaultTrainingConfig config = setupTrainingConfig();

            try (Trainer trainer = model.newTrainer(config)) {
                trainer.setMetrics(new Metrics());

                /*
                 * MNIST is 28x28 grayscale image and pre processed into 28 * 28 NDArray.
                 * 1st axis is batch axis, we can use 1 for initialization.
                 */
                Shape inputShape = new Shape(32, 1, 28, 28);

                // initialize trainer with proper input shape
                trainer.initialize(inputShape);

                EasyTrain.fit(trainer, epoch, trainingSet, validateSet);

                return trainer.getTrainingResult();
            }
        }
    }

    private static Block getLSTMModel() {
        SequentialBlock block = new SequentialBlock();
        block.addSingleton(
                input -> {
                    Shape inputShape = input.getShape();
                    long batchSize = inputShape.get(0);
                    long channel = inputShape.get(3);
                    long time = inputShape.size() / (batchSize * channel);
                    return input.reshape(new Shape(batchSize, time, channel));
                });
        block.add(
                new LSTM.Builder()
                        .setStateSize(64)
                        .setNumLayers(1)
                        .optDropRate(0)
                        .optReturnState(false)
                        .build());
        block.add(BatchNorm.builder().optEpsilon(1e-5f).optMomentum(0.9f).build());
        block.add(Blocks.batchFlattenBlock());
        block.add(Linear.builder().setUnits(10).build());
        return block;
    }

    private static DefaultTrainingConfig setupTrainingConfig() {
        SaveModelTrainingListener listener = new SaveModelTrainingListener(outputDir);
        listener.setSaveModelCallback(
                trainer -> {
                    TrainingResult result = trainer.getTrainingResult();
                    Model model = trainer.getModel();
                    float accuracy = result.getValidateEvaluation("Accuracy");
                    model.setProperty("Accuracy", String.format("%.5f", accuracy));
                    model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));
                });

        return new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .optDevices(maxGpus)
                .addTrainingListeners(TrainingListener.Defaults.logging(outputDir))
                .addTrainingListeners(listener);
    }

    private static RandomAccessDataset getDataset(Dataset.Usage usage)
            throws IOException {
        Mnist mnist =
                Mnist.builder()
                        .optUsage(usage)
                        .optManager(NDManager.newBaseManager(engine))
                        .setSampling(batchSize, false, true)
                        .optLimit(limit)
                        .build();
        mnist.prepare(new ProgressBar());
        return mnist;
    }
}