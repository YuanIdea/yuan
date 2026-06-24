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
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Detection {
    private static final OpenCVFrameConverter.ToMat CONVERTER = new OpenCVFrameConverter.ToMat();
    private static ZooModel<Image, DetectedObjects> model = null;
    // COCO dataset class names (80 classes)
    public static final List<String> COCO_CLASSES = Arrays.asList(
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

    public static ZooModel<Image, DetectedObjects> getModelInstance() {
        if (model == null) {
            // 加载模型（放在 startVideo 里，确保每次切换视频源都用新模型？也可以复用）
            String modelDirPath = "model";
            String modelUrl = Paths.get(modelDirPath).toUri().toString();
            Map<String, Object> arguments = new ConcurrentHashMap<>();
            arguments.put("width", 640);
            arguments.put("height", 640);
            arguments.put("resize", true);
            arguments.put("rescale", true);
            Translator<Image, DetectedObjects> translator =
                    YoloV8Translator.builder(arguments)
                            .optSynset(Detection.COCO_CLASSES)
                            .optNmsThreshold(0.5f)
                            .build();
            Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                    .setTypes(Image.class, DetectedObjects.class)
                    .optDevice(Device.cpu())
                    .optModelUrls(modelUrl)
                    .optModelName("yolov8s.torchscript")
                    .optTranslator(translator)
                    .optProgress(new ProgressBar())
                    .optEngine("PyTorch")
                    .build();
            try {
                model = ModelZoo.loadModel(criteria);
                System.out.println("Model loaded successfully");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return model;
    }

    static void detect(Frame inputFrame, Predictor<Image, DetectedObjects> predictor) throws Exception {
        Mat mat = CONVERTER.convert(inputFrame);
        BufferedImage bufferedImage = matToBufferedImage(mat);
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
