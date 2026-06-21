package com.gly;

import javax.swing.*;
import java.io.File;

public class Menu {
    private final Platform platform;
    private final JMenuBar menuBar;

    public Menu(Platform platform) {
        this.platform = platform;
        menuBar = new JMenuBar();
        this.platform.canvas.setJMenuBar(menuBar);
    }

    void buildMenus() {
        // File 菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> platform.stopVideo());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Open 菜单
        JMenu openMenu = new JMenu("打开视频");
        JMenuItem cameraItem = new JMenuItem("打开本地摄像头");
        cameraItem.addActionListener(e -> platform.startVideo("0"));
        openMenu.add(cameraItem);

        JMenuItem fileItem = new JMenuItem("打开本地视频");
        fileItem.addActionListener(e -> {
            // Dialog 是自定义的文件选择器，请确保可用
            File file = Dialog.open(platform.canvas, System.getProperty("user.dir"));
            if (file != null && file.exists()) {
                platform.canvas.setTitle(file.getName());
                platform.startVideo(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(platform.canvas,
                        "无法打开视频文件",
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        openMenu.add(fileItem);
        menuBar.add(openMenu);

        JMenuItem openIP = new JMenuItem("打开网络摄像头");
        openMenu.add(openIP);
        openIP.addActionListener(e -> {
            IPCamera ipc = new IPCamera(platform.canvas);
            platform.startVideo(ipc.getCameraUrl());
        });

        operate();
    }

    /**
     * 操作菜单。
     */
    private void operate() {
        JMenu operate = new JMenu("操作");
        menuBar.add(operate);
        JMenuItem trace = new JMenuItem("图像追踪");
        trace.addActionListener(e -> platform.startDetect = ! platform.startDetect);
        operate.add(trace);
    }
}
