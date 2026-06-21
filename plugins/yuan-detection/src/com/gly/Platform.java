package com.gly;

import org.bytedeco.javacv.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Platform {
    public final CanvasFrame canvas;
    private FrameGrabber grabber;
    private volatile boolean running = false;   // 控制循环是否继续
    private Thread videoThread;
    public boolean startDetect = false;

    public Platform() {
        canvas = new CanvasFrame("Real-time Detection");
        canvas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        canvas.setCanvasSize(960, 720);
        canvas.setLocationRelativeTo(null);
        canvas.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopVideo();
            }
        });
        canvas.setVisible(true);

        Menu menu = new Menu(this);
        menu.buildMenus();
    }


    void startVideo(String source) {
        stopVideo();
        Detection.getModelInstance();
        try {
            if (source.equals("0")) {
                grabber = new OpenCVFrameGrabber(0);
            } else if (source.startsWith("rtsp")){
                System.out.println(source);
                grabber = new FFmpegFrameGrabber(source);
            } else {
                grabber = new OpenCVFrameGrabber(source);
            }
    
            grabber.start();
            canvas.setCanvasSize(grabber.getImageWidth(), grabber.getImageHeight());
            running = true;
            videoThread = new Thread(() -> {
                try {
                    double fps = 30;
                    long frameDelayMs = (long) (1000 / fps);
                    long lastFrameTime = 0;
                    while (running && canvas.isShowing()) {
                        Frame frame = grabber.grab();
                        if (frame == null)
                            break;
                        if (startDetect) {
                            Detection.detect(frame, Detection.getModelInstance());
                        }
                        if (canvas.isShowing()) {
                            canvas.showImage(frame);
                        }
                        lastFrameTime = sleep(frameDelayMs, lastFrameTime);
                    }
                    releaseResources();
                } catch (Exception e) {
                    System.err.println("Video thread error: " + e.getMessage());
                } finally {
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

    private static long sleep(long frameDelayMs, long lastFrameTime) {
        // 计算当前时间
        long currentTime = System.currentTimeMillis();
        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
        } else {
            // 计算应该等待的时间
            long elapsed = currentTime - lastFrameTime;
            long waitTime = frameDelayMs - elapsed;
            // 如果处理时间小于帧间隔，等待剩余时间
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
        running = false;   // 告知线程退出
        if (videoThread != null && videoThread.isAlive()) {
            videoThread.interrupt();  // 打断可能的阻塞
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
