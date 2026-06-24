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
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Detection {
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

    private static final List<String> COCO_CLASSES_C = Arrays.asList(
            "人", "自行车", "汽车", "摩托车", "飞机", "公共汽车", "火车", "卡车", "船", "交通信号灯",
            "消防栓", "停止标志", "停车计时器", "长椅", "鸟", "猫", "狗", "马", "羊", "牛",
            "大象", "熊", "斑马", "长颈鹿", "背包", "雨伞", "手提包", "领带", "手提箱", "飞盘",
            "滑雪板", "滑雪单板", "运动球", "风筝", "棒球棒", "棒球手套", "滑板", "冲浪板",
            "网球拍", "瓶子", "酒杯", "杯子", "叉子", "刀", "勺子", "碗", "香蕉", "苹果",
            "三明治", "橙子", "西兰花", "胡萝卜", "热狗", "披萨", "甜甜圈", "蛋糕", "椅子", "沙发",
            "盆栽植物", "床", "餐桌", "马桶", "电视", "笔记本电脑", "鼠标", "遥控器", "键盘", "手机",
            "微波炉", "烤箱", "烤面包机", "水槽", "冰箱", "书", "时钟", "花瓶", "剪刀", "泰迪熊",
            "吹风机", "牙刷"
    );

    private static final Map<String, String> EN_TO_CN = new HashMap<>();

    static {
        for (int i = 0; i < COCO_CLASSES.size(); i++) {
            EN_TO_CN.put(COCO_CLASSES.get(i), COCO_CLASSES_C.get(i));
        }
    }

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
                            .optNmsThreshold(0.5f)
                            .optSynset(COCO_CLASSES)
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

    public static BufferedImage detect(Frame inputFrame, Predictor<Image, DetectedObjects> predictor, boolean chinese) throws Exception {
        BufferedImage image;
        // Frame → BufferedImage（推理用）
        try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
            image = converter.convert(inputFrame);
            if (image == null) return null;

            int imgWidth = inputFrame.imageWidth;
            int imgHeight = inputFrame.imageHeight;

            // DJL 推理
            DetectedObjects results = predictor.predict(ImageFactory.getInstance().fromImage(image));

            // 坐标缩放
            float scaleX = (float) imgWidth / 640;
            float scaleY = (float) imgHeight / 640;

            // 用 Graphics2D 绘制框和中文标签
            Graphics2D g = image.createGraphics();
            // 抗锯齿设置
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics();

            for (var item : results.items()) {
                if (!(item instanceof DetectedObjects.DetectedObject)) continue;
                DetectedObjects.DetectedObject obj = (DetectedObjects.DetectedObject) item;
                Rectangle rect = obj.getBoundingBox().getBounds();

                int x = Math.max(0, Math.min((int) (rect.getX() * scaleX), imgWidth - 1));
                int y = Math.max(0, Math.min((int) (rect.getY() * scaleY), imgHeight - 1));
                int w = Math.max(1, Math.min((int) (rect.getWidth() * scaleX), imgWidth - x));
                int h = Math.max(1, Math.min((int) (rect.getHeight() * scaleY), imgHeight - y));

                // 绿色矩形框
                g.setColor(Color.GREEN);
                g.setStroke(new BasicStroke(2));
                g.drawRect(x, y, w, h);

                String className = obj.getClassName();          // "class-3"
                if (chinese) {
                    className = EN_TO_CN.get(className);
                }
                // 中文标签
                String label = String.format("%s: %.2f", className, obj.getProbability());
                int textWidth = fm.stringWidth(label);
                int textHeight = fm.getHeight();

                int by = y - textHeight - 2;
                if (by < 0) by = y + h + 2;   // 上方放不下就放下方

                // 背景
                g.setColor(Color.white);
                g.fillRect(x, by, textWidth + 4, textHeight + 2);
                // 文字
                g.setColor(Color.black);
                g.drawString(label, x + 2, by + fm.getAscent() + 1);
            }

            g.dispose();
        }
        return image;
    }
}
