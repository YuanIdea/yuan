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
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.djl.modality.Classifications.Classification;

import static org.opencv.videoio.Videoio.CAP_ANY;

/**
 * Real-time object detection using YOLOv8 with DJL (Deep Java Library) and OpenCV.
 * The model is loaded from a TorchScript file and runs on CPU.
 */
public class Main {
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

    static {
        OpenCV.loadShared();
    }

    public static void main(String[] args) {
        // Model directory path (relative to the current working directory)
        String modelDirPath = "src/resources/yolo";
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

            VideoCapture cap = new VideoCapture(CAP_ANY);
            if (!cap.isOpened()) {
                System.err.println("Failed to open camera.");
                return;
            }
            Mat frame = new Mat();
            while (cap.read(frame)) {
                detect(frame, model);
                HighGui.imshow("Real-time Detection", frame);
                if (HighGui.waitKey(20) == 27) break; // ESC to exit
            }

            cap.release();
            HighGui.destroyAllWindows();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Performs object detection on a frame and draws bounding boxes.
     * Handles coordinate mapping from model input size (640x640) to original frame size.
     *
     * @param frame OpenCV Mat (original camera frame)
     * @param model Loaded DJL ZooModel
     */
    static void detect(Mat frame, ZooModel<Image, DetectedObjects> model) throws  TranslateException {
        BufferedImage bufferedImage = matToBufferedImage(frame);
        long startTime = System.currentTimeMillis();

        try (Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
            // Run inference
            DetectedObjects results = predictor.predict(
                    ImageFactory.getInstance().fromImage(bufferedImage)
            );

            // Original frame dimensions
            int imgWidth = frame.width();
            int imgHeight = frame.height();
            // Model input dimensions (must match the translator configuration)
            int modelWidth = 640;
            int modelHeight = 640;

            // Scaling factors because the model input is stretched to 640x640
            float scaleX = (float) imgWidth / modelWidth;
            float scaleY = (float) imgHeight / modelHeight;

            // Iterate over results (each element is a Classification, but we need DetectedObject)
            for (Classification classification : results.items()) {
                // Safely cast to DetectedObject to access bounding box
                if (classification instanceof DetectedObjects.DetectedObject) {
                    DetectedObjects.DetectedObject obj = (DetectedObjects.DetectedObject) classification;
                    Rectangle rect = obj.getBoundingBox().getBounds(); // Coordinates in model space (640x640)

                    // Map coordinates back to original frame space
                    int x = (int) (rect.getX() * scaleX);
                    int y = (int) (rect.getY() * scaleY);
                    int w = (int) (rect.getWidth() * scaleX);
                    int h = (int) (rect.getHeight() * scaleY);

                    // Clamp to image boundaries (optional)
                    x = Math.max(0, Math.min(x, imgWidth - 1));
                    y = Math.max(0, Math.min(y, imgHeight - 1));
                    w = Math.max(1, Math.min(w, imgWidth - x));
                    h = Math.max(1, Math.min(h, imgHeight - y));

                    String label = String.format("%s: %.2f", obj.getClassName(), obj.getProbability());

                    // Draw bounding box
                    Imgproc.rectangle(frame,
                            new Point(x, y),
                            new Point(x + w, y + h),
                            new Scalar(0, 255, 0), 2);

                    // Prepare for text drawing (using baseline array)
                    int[] baseline = new int[1];
                    Size textSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 2, baseline);
                    int baselineY = baseline[0];

                    // Draw label with black background for readability
                    Point textOrg = new Point(x, y - 5);
                    if (textOrg.y - textSize.height < 0) {
                        textOrg.y = y + textSize.height + 5; // move below if above top
                    }
                    Imgproc.rectangle(frame,
                            new Point(textOrg.x, textOrg.y - textSize.height - 2),
                            new Point(textOrg.x + textSize.width, textOrg.y + baselineY + 2),
                            new Scalar(0, 0, 0), -1);
                    Imgproc.putText(frame, label, textOrg,
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 1);
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        double fps = 1000.0 / elapsed;
        //System.out.printf("Inference time: %d ms, FPS: %.2f%n", elapsed, fps);
    }

    /**
     * Converts an OpenCV Mat (BGR) to a BufferedImage (TYPE_3BYTE_BGR).
     *
     * @param mat Input OpenCV Mat
     * @return Corresponding BufferedImage
     */
    static BufferedImage matToBufferedImage(Mat mat) {
        // Convert grayscale to BGR if necessary
        if (mat.channels() == 1) {
            Mat gray = new Mat();
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_GRAY2BGR);
            mat = gray;
        }

        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);

        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);

        return image;
    }
}