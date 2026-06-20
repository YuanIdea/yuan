package com.gly;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
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

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;


import javax.swing.*;

/**
 * Real-time object detection using YOLOv8 with DJL (Deep Java Library) and OpenCV.
 * The model is loaded from a TorchScript file and runs on CPU.
 */
public class Main {
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
                .optSynset(Detection.COCO_CLASSES)
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
                Frame result = Detection.detect(frame, model);
                canvas.showImage(result);
            }

            grabber.stop();
            canvas.dispose();
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}