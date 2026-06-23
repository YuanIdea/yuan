package com.gly;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class Platform {
    public final JFrame frame;

    // JavaFX 的视频渲染面板
    private final JFXPanel jfxPanel;
    private ImageView imageView;

    private FrameGrabber grabber;
    private volatile boolean running = false;
    private Thread videoThread;
    public boolean startDetect = false;

    public Platform() {
        frame = new JFrame("Real-time Detection");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopVideo();
            }
        });

        // 初始化 JavaFX 嵌入面板（放在窗口正中间）
        jfxPanel = new JFXPanel();
        frame.add(jfxPanel, BorderLayout.CENTER);

        // 在 JavaFX 线程里初始化视频渲染组件
        javafx.application.Platform.runLater(() -> {
            imageView = new ImageView();
            StackPane root = new StackPane(imageView);
            jfxPanel.setScene(new Scene(root));

            // 绑定到 root 的属性
            imageView.fitWidthProperty().bind(root.widthProperty());
            imageView.fitHeightProperty().bind(root.heightProperty());

            imageView.setPreserveRatio(true);
        });

        frame.setVisible(true);

        // 构建菜单
        Menu menu = new Menu(this);
        menu.buildMenus();
    }

    void startVideo(String source) {
        avutil.av_log_set_level(avutil.AV_LOG_ERROR);
        stopVideo();
        Detection.getModelInstance();
        try {
            if (source.equals("0")) {
                grabber = new OpenCVFrameGrabber(0);
            } else {
                grabber = new FFmpegFrameGrabber(source);
                grabber.setAudioStream(0);
                grabber.setImageMode(FrameGrabber.ImageMode.COLOR);
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
                fps = 33;
            }
            long frameDelayMs = (long) (1000 / fps);
            long lastFrameTime = 0;

            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                while (running && frame.isShowing()) {
                    Frame grabFrame = grabber.grab();
                    if (grabFrame == null)
                        break;

                    if (startDetect) {
                        Detection.detect(grabFrame, Detection.getModelInstance());
                    }

                    BufferedImage bufImg = converter.convert(grabFrame);
                    if (bufImg != null) {
                        WritableImage fxImage = SwingFXUtils.toFXImage(bufImg, null);
                        // 安全地将渲染推送到 JavaFX 的 UI 线程
                        javafx.application.Platform.runLater(() -> {
                            if (imageView != null && running) {
                                imageView.setImage(fxImage);
                            }
                        });
                    }
                    try {
                        lastFrameTime = sleep(frameDelayMs, lastFrameTime);
                    } catch (InterruptedException e) {
                        running = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Video thread error: " + e.getMessage());
        } finally {
            stopVideo();
        }
    }

    private long sleep(long frameDelayMs, long lastFrameTime) throws Exception {
        long currentTime = System.currentTimeMillis();
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
        } else {
            long elapsed = currentTime - lastFrameTime;
            long waitTime = frameDelayMs - elapsed;
            if (waitTime > 0) {
                Thread.sleep(waitTime);
            }
            lastFrameTime = System.currentTimeMillis();
        }
        return lastFrameTime;
    }

    void stopVideo() {
        if (!running)
            return;

        running = false;
        // 先中断线程
        if (videoThread != null) {
            videoThread.interrupt();
        }

        // 释放采集器资源
        releaseResources();

        // 清空画面
        javafx.application.Platform.runLater(() -> {
            if (imageView != null) {
                imageView.setImage(null);
            }
        });

        // 等待线程结束
        try {
            if (videoThread != null) {
                videoThread.join(500);
            }
        } catch (InterruptedException ignored) {
        }

        videoThread = null;
    }

    private void releaseResources() {
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                System.err.println("Error stopping grabber: " + e.getMessage());
            } finally {
                grabber = null;
            }
        }
    }
}