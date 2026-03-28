package com.gly;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.Mnist;
import ai.djl.basicmodelzoo.basic.Mlp;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
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

/**
 * An example of training an image classification (MNIST) model.
 *
 * <p>See this <a
 * href="https://github.com/deepjavalibrary/djl/blob/master/examples/docs/train_mnist_mlp.md">doc</a>
 * for information about this example.
 */
public final class TrainMnist {
    private final static int epoch = 5;
    private final static int batchSize = 64;
    private final static String engine = "PyTorch";
    private final static String outputDir = "mlp-mnist";
    private final static Device[] maxGpus = new Device[]{Device.cpu()};
    private final static int limit = 1000;

    private TrainMnist() {}

    public static void main(String[] args) throws IOException, TranslateException {
        TrainMnist.runExample(args);
    }

    public static TrainingResult runExample(String[] args) throws IOException, TranslateException {


        // Construct neural network
        Block block =
                new Mlp(
                        Mnist.IMAGE_HEIGHT * Mnist.IMAGE_WIDTH,
                        Mnist.NUM_CLASSES,
                        new int[] {128, 64});

        try (Model model = Model.newInstance("mlp", engine)) {
            model.setBlock(block);

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
                Shape inputShape = new Shape(1, Mnist.IMAGE_HEIGHT * Mnist.IMAGE_WIDTH);

                // initialize trainer with proper input shape
                trainer.initialize(inputShape);

                EasyTrain.fit(trainer, epoch, trainingSet, validateSet);

                return trainer.getTrainingResult();
            }
        }
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
                        .setSampling(batchSize, true)
                        .optLimit(limit)
                        .build();
        mnist.prepare(new ProgressBar());
        return mnist;
    }
}
