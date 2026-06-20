package com.gly;

import ai.djl.Device;
import ai.djl.translate.Translator;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.djl.*;
import ai.djl.modality.cv.*;
import ai.djl.modality.cv.output.*;
import ai.djl.modality.cv.translator.YoloV8Translator;
import ai.djl.repository.zoo.*;
import ai.djl.training.util.ProgressBar;

public class Main {
    private static CanvasFrame canvas;
    private static OpenCVFrameGrabber grabber;
    private static volatile boolean running = false;   // 控制循环是否继续
    private static Thread videoThread;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            canvas = new CanvasFrame("Real-time Detection");
            canvas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            canvas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            canvas.setCanvasSize(960, 720);           // 设置画布尺寸
            canvas.setLocationRelativeTo(null);
            canvas.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    stopVideo();  // 窗口关闭时安全停止
                }
            });

            JMenuBar menuBar = new JMenuBar();
            buildMenus(menuBar);
            canvas.setJMenuBar(menuBar);
            canvas.setVisible(true);
        });
    }

    private static void buildMenus(JMenuBar menuBar) {
        // File 菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> stopVideo());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Open 菜单
        JMenu openMenu = new JMenu("打开视频");
        JMenuItem cameraItem = new JMenuItem("打开本地摄像头");
        cameraItem.addActionListener(e -> startVideo("0"));
        openMenu.add(cameraItem);

        JMenuItem fileItem = new JMenuItem("打开本地视频");
        fileItem.addActionListener(e -> {
            // Dialog 是自定义的文件选择器，请确保可用
            File file = Dialog.open(canvas, System.getProperty("user.dir"));
            if (file != null && file.exists()) {
                canvas.setTitle(file.getName());
                startVideo(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(canvas,
                        "无法打开视频文件",
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        openMenu.add(fileItem);
        menuBar.add(openMenu);
    }

    private static void startVideo(String source) {
        // 先停掉之前的视频
        stopVideo();

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
            ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);
            System.out.println("Model loaded successfully. Starting video source: " + source);

            if (source.equals("0")) {
                grabber = new OpenCVFrameGrabber(0);
            } else {
                grabber = new OpenCVFrameGrabber(source);
            }
            grabber.start();

            running = true;
            videoThread = new Thread(() -> {
                try {
                    while (running && canvas.isShowing()) {
                        Frame frame = grabber.grab();
                        if (frame == null) break;
                        Frame result = Detection.detect(frame, model);
                        if (canvas.isShowing()) {
                            canvas.showImage(result);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Video thread error: " + e.getMessage());
                } finally {
                    // 循环退出后释放资源
                    releaseResources();
                }
            });
            videoThread.setDaemon(true);
            videoThread.start();

        } catch (Exception e) {
            System.err.println("Failed to start video: " + e.getMessage());
            JOptionPane.showMessageDialog(canvas,
                    "无法打开视频源: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void stopVideo() {
        running = false;   // 告知线程退出
        if (videoThread != null && videoThread.isAlive()) {
            videoThread.interrupt();  // 打断可能的阻塞
        }
        // 资源释放会在 video thread 的 finally 中执行，
        // 但为保证立即停止，也可以在这里尝试调用 grabber.stop()
        // 注意线程安全，一般由同一线程关闭 grabber 更稳妥
    }

    private static void releaseResources() {
        // 确保在视频线程中调用，避免 grabber 被多线程操作
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                System.err.println("Error stopping grabber: " + e.getMessage());
            }
            grabber = null;
        }
        // 模型可以在这里关闭，但 ZooModel 是 try-with-resources 在 startVideo 里？注意模型生命周期
        // 为了简化，模型在 startVideo 的 try 块结束时已关闭，如果要在 stopVideo 里显式关闭，需保存引用
    }
}