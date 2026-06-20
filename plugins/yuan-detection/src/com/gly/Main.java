package com.gly;

import ai.djl.Device;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.translator.YoloV8Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.bytedeco.javacv.OpenCVFrameConverter;

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;

import java.awt.image.DataBufferByte;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgproc;   // 所有 imgproc 函数都在这里
import org.bytedeco.opencv.opencv_core.*;


import javax.swing.*;

/**
 * Real-time object detection using YOLOv8 with DJL (Deep Java Library) and OpenCV.
 * The model is loaded from a TorchScript file and runs on CPU.
 */
public class Main {
    private static final OpenCVFrameConverter.ToMat CONVERTER = new OpenCVFrameConverter.ToMat();
    // COCO dataset class names (80 classes)
    private static final List<String> COCO_CLASSES = Arrays.asList(
            "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat",
            "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat",
            "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack",
            "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball",
            "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket",
            "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair",
            "couch", "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse",
            "remote", "keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator",
            "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"
    );


    public static void main(String[] args) {
        // Model directory path (relative to the current working directory)
        String modelDirPath = "model";
        String modelUrl = Paths.get(modelDirPath).toUri().toString();
        System.out.println("Model directory URL: " + modelUrl);

        Map<String, Object> arguments = new ConcurrentHashMap<>();
        arguments.put("width", 640);
        arguments.put("height", 640);
        arguments.put("resize", true);   // Resize input to 640x640
        arguments.put("rescale", true);  // Normalize pixel values to [0,1]

        Translator<Image, DetectedObjects> translator = YoloV8Translator.builder(arguments)
                .optSynset(COCO_CLASSES)
                .optNmsThreshold(0.5f)
                .build();

        // Criteria for loading the PyTorch model
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optDevice(Device.cpu())
                .optModelUrls(modelUrl)
                .optModelName("yolov8s.torchscript")
                .optTranslator(translator)
                .optProgress(new ProgressBar())
                .optEngine("PyTorch")
                .build();

        // Load the model and start video capture
        try (ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria)) {
            System.out.println("Model loaded successfully. Starting camera...");
            OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
            grabber.start();
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            CanvasFrame canvas = new CanvasFrame("Real-time Detection");
            canvas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            while (canvas.isShowing()) {
                Frame frame = grabber.grab();
                if (frame == null)
                    break;
                Frame result = detect(frame, model);
                canvas.showImage(result);
            }

            grabber.stop();
            canvas.dispose();
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    static Frame detect(Frame inputFrame, ZooModel<Image, DetectedObjects> model) throws Exception {
        Mat mat = CONVERTER.convert(inputFrame);

        BufferedImage bufferedImage = matToBufferedImage(mat);

        try (Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
            DetectedObjects results = predictor.predict(
                    ImageFactory.getInstance().fromImage(bufferedImage)
            );

            int imgWidth = inputFrame.imageWidth;
            int imgHeight = inputFrame.imageHeight;
            int modelWidth = 640;
            int modelHeight = 640;
            float scaleX = (float) imgWidth / modelWidth;
            float scaleY = (float) imgHeight / modelHeight;

            for (var item : results.items()) {
                if (item instanceof DetectedObjects.DetectedObject) {
                    DetectedObjects.DetectedObject obj = (DetectedObjects.DetectedObject) item;
                    Rectangle rect = obj.getBoundingBox().getBounds();
                    int x = (int) (rect.getX() * scaleX);
                    int y = (int) (rect.getY() * scaleY);
                    int w = (int) (rect.getWidth() * scaleX);
                    int h = (int) (rect.getHeight() * scaleY);
                    x = Math.max(0, Math.min(x, imgWidth - 1));
                    y = Math.max(0, Math.min(y, imgHeight - 1));
                    w = Math.max(1, Math.min(w, imgWidth - x));
                    h = Math.max(1, Math.min(h, imgHeight - y));

                    String label = String.format("%s: %.2f", obj.getClassName(), obj.getProbability());

                    opencv_imgproc.rectangle(mat,
                            new Point(x, y),
                            new Point(x + w, y + h),
                            new Scalar(0, 255, 0, 0), 2,
                            opencv_imgproc.LINE_8, 0);

                    int[] baseline = new int[1];
                    Size textSize = opencv_imgproc.getTextSize(label,
                            opencv_imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 2, baseline);

                    Point textOrg = new Point(x, y - 5);
                    if (textOrg.y() - textSize.height() < 0) {
                        textOrg = new Point(x, y + (int) textSize.height() + 5);
                    }

                    opencv_imgproc.rectangle(mat,
                            new Point(textOrg.x(), textOrg.y() - textSize.height() - 2),
                            new Point(textOrg.x() + textSize.width(), textOrg.y() + baseline[0] + 2),
                            new Scalar(0, 0, 0, 0),
                            -1,
                            opencv_imgproc.LINE_8, 0);
                    
                    opencv_imgproc.putText(mat, label, textOrg,
                            opencv_imgproc.FONT_HERSHEY_SIMPLEX, 0.5,
                            new Scalar(0, 255, 0, 0),
                            1,
                            opencv_imgproc.LINE_AA, false);

                }
            }
        }

        return CONVERTER.convert(mat);
    }

    static BufferedImage matToBufferedImage(Mat mat) {
        if (!mat.isContinuous()) {
            mat = mat.clone();
        }

        int channels = mat.channels();
        if (channels == 1) {
            Mat bgrMat = new Mat();
            opencv_imgproc.cvtColor(mat, bgrMat, opencv_imgproc.COLOR_GRAY2BGR);
            mat = bgrMat;
        }

        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.data().get(buffer);

        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}