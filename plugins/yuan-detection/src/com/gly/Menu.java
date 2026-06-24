package com.gly;

import javax.swing.*;
import java.io.File;

public class Menu {
    private final VideoPlatform platform;
    private final JMenuBar menuBar;

    public Menu(VideoPlatform platform) {
        this.platform = platform;
        menuBar = new JMenuBar();
        this.platform.frame.setJMenuBar(menuBar);
    }

    void buildMenus() {
        // File 菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem chineseItem = new JMenuItem("中文标签");
        chineseItem.addActionListener(e -> platform.chinese = !platform.chinese);
        fileMenu.add(chineseItem);
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> platform.stopVideo());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Open 菜单
        JMenu openMenu = new JMenu("打开视频");
        JMenuItem cameraItem = new JMenuItem("本地摄像头");
        cameraItem.addActionListener(e -> platform.startVideo("0"));
        openMenu.add(cameraItem);

        JMenuItem fileItem = getJMenuItem();
        openMenu.add(fileItem);
        menuBar.add(openMenu);

        JMenuItem openIP = new JMenuItem("网络摄像头");
        openMenu.add(openIP);
        openIP.addActionListener(e -> {
            IPCamera ipc = new IPCamera(platform.frame);
            String url = ipc.getCameraUrl();
            if (!url.isEmpty()) {
                platform.startVideo(url);
            }
        });

        operate();
    }

    private JMenuItem getJMenuItem() {
        JMenuItem fileItem = new JMenuItem("本地视频");
        fileItem.addActionListener(e -> {
            File file = Dialog.open(platform.frame, System.getProperty("user.dir"));
            if (file != null && file.exists()) {
                platform.frame.setTitle(file.getName());
                platform.startVideo(file.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(platform.frame,
                        "无法打开视频文件",
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        return fileItem;
    }

    /**
     * 操作菜单。
     */
    private void operate() {
        JMenu operate = new JMenu("操作");
        menuBar.add(operate);
        JMenuItem trace = new JMenuItem("目标追踪");
        trace.addActionListener(e -> platform.startDetect = !platform.startDetect);
        operate.add(trace);
    }
}