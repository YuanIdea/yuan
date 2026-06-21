package com.gly;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class Platform {
    // 【替换】主窗口改为标准的 Swing JFrame，为了兼容你原有的 Menu 菜单
    public final JFrame frame;

    // 【新增】JavaFX 的视频渲染面板
    private final JFXPanel jfxPanel;
    private ImageView imageView;

    private FrameGrabber grabber;
    private volatile boolean running = false;
    private Thread videoThread;
    public boolean startDetect = false;

    public Platform() {
        // 1. 初始化主窗口（保留 Swing 框架，为菜单做准备）
        frame = new JFrame("Real-time Detection");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopVideo();
            }
        });

        // 2. 初始化 JavaFX 嵌入面板（放在窗口正中间）
        jfxPanel = new JFXPanel();
        frame.add(jfxPanel, BorderLayout.CENTER);

        // 3. 在 JavaFX 线程里初始化视频渲染组件
        javafx.application.Platform.runLater(() -> {
            imageView = new ImageView();
            StackPane root = new StackPane(imageView); // 把 imageView 放进去
            jfxPanel.setScene(new Scene(root));

            // 【修正点】：绑定到 root 的属性，而不是 jfxPanel
            imageView.fitWidthProperty().bind(root.widthProperty());
            imageView.fitHeightProperty().bind(root.heightProperty());

            imageView.setPreserveRatio(true);
        });

        frame.setVisible(true);

        // 4. 构建菜单（需要到 Menu 类里微调，见下文提示）
        Menu menu = new Menu(this);
        menu.buildMenus();
    }

    void startVideo(String source) {
        stopVideo();
        Detection.getModelInstance();
        try {
            if (source.equals("0")) {
                grabber = new OpenCVFrameGrabber(0);
            } else {
                grabber = new FFmpegFrameGrabber(source);
                grabber.setFrameRate(0);
                grabber.setAudioStream(0);
                grabber.setImageMode(FrameGrabber.ImageMode.COLOR); // 保持 BGR 格式给 OpenCV 检测用
            }

            grabber.start();
            running = true;
            videoThread = new Thread(this::cameraLoop);
            videoThread.setDaemon(true);
            videoThread.start();
        } catch (Exception e) {
            System.err.println("Failed to start video: " + e.getMessage());
            JOptionPane.showMessageDialog(frame, // 消息框绑到主 frame
                    "无法打开视频源: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cameraLoop() {
        try {
            double fps = grabber.getFrameRate();
            if (fps <= 0) {
                fps = 30;
            }
            long frameDelayMs = (long) (1000 / fps);
            long lastFrameTime = 0;

            // 【核心修正】：把 OpenCVFrameConverter.ToImage 替换为 Java2DFrameConverter
            Java2DFrameConverter converter = new Java2DFrameConverter();

            while (running && frame.isShowing()) {
                Frame grabFrame = grabber.grab();
                if (grabFrame == null) break;

                if (startDetect) {
                    Detection.detect(grabFrame, Detection.getModelInstance());
                }

                // 【核心升级】：这里把 BGR 转成 BufferedImage，再转成 JavaFX 专用的 WritableImage
                BufferedImage bufImg = converter.convert(grabFrame);
                if (bufImg != null) {
                    WritableImage fxImage = SwingFXUtils.toFXImage(bufImg, null);
                    // 安全地将渲染推送到 JavaFX 的 UI 线程
                    javafx.application.Platform.runLater(() -> {
                        if (imageView != null) {
                            imageView.setImage(fxImage);
                        }
                    });
                }

                lastFrameTime = sleep(frameDelayMs, lastFrameTime);
            }
            releaseResources();
        } catch (Exception e) {
            System.err.println("Video thread error: " + e.getMessage());
        } finally {
            releaseResources();
        }
    }

    private static long sleep(long frameDelayMs, long lastFrameTime) {
        long currentTime = System.currentTimeMillis();
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
        } else {
            long elapsed = currentTime - lastFrameTime;
            long waitTime = frameDelayMs - elapsed;
            if (waitTime > 0) {
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
            lastFrameTime = System.currentTimeMillis();
        }
        return lastFrameTime;
    }

    void stopVideo() {
        running = false;
        if (videoThread != null && videoThread.isAlive()) {
            videoThread.interrupt();
            releaseResources();
        }
    }

    private void releaseResources() {
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                System.err.println("Error stopping grabber: " + e.getMessage());
            }
            grabber = null;
        }
    }
}