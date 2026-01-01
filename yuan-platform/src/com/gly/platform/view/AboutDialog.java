package com.gly.platform.view;

import javax.swing.*;
import java.awt.*;

/**
 * 关于对话框
 */
class AboutDialog {
    private static final String APP_NAME = "元智能平台1.05";
    private static final String VERSION = "2026.01.01";
    private static final String COPYRIGHT = "Guoliang-Yang@hotmail.com\nyoucongguo@126.com";

    static void showAboutDialog(JFrame parent) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 创建自定义对话框
        JDialog aboutDialog = new JDialog(parent, "关于", true);
        aboutDialog.setLayout(new BorderLayout(20, 20));
        aboutDialog.setResizable(false);

        // 主内容面板
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 顶部图标和标题区域
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // 加载应用程序图标（需准备一个 64x64 的图标文件）
        ImageIcon appIcon = new ImageIcon(AboutDialog.class.getResource("/icons/aif.png"));
        JLabel iconLabel = new JLabel(new ImageIcon(appIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        headerPanel.add(iconLabel);

        // 应用名称和版本
        JLabel titleLabel = new JLabel(APP_NAME);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        JLabel versionLabel = new JLabel(VERSION);
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.add(titleLabel);
        textPanel.add(versionLabel);
        headerPanel.add(textPanel);

        // 中间版权信息
        JTextArea infoArea = new JTextArea();
        String base = COPYRIGHT + "\n\n" +
                "Java runtime: " + System.getProperty("java.version") + "\n" +
                "JVM version: " + System.getProperty("java.vm.version") + "\n" +
                "Java home: " + System.getProperty("java.home") + "\n";
        infoArea.setText(base);
        infoArea.setEditable(false);
        infoArea.setOpaque(false);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(infoArea, BorderLayout.CENTER);
        aboutDialog.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        aboutDialog.add(buttonPanel, BorderLayout.SOUTH);

        // 设置对话框属性
        aboutDialog.pack();
        aboutDialog.setLocationRelativeTo(parent);
        aboutDialog.setVisible(true);
    }
}
