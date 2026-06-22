package com.gly;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class IPCamera {
    private JDialog configDialog;
    private JTextField ipField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField passField;
    private JComboBox<String> protocolCombo;
    private String cameraUrl;

    IPCamera(Frame parent) {
        createConfigGUI(parent);
    }

    private void createConfigGUI(Frame parent) {
        configDialog = new JDialog(parent, "IP摄像头配置", true);

        // 创建主面板，使用 GridBagLayout 并结合 EmptyBorder 留白
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25)); // 上左下右的间距

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // 组件之间的间距
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- 表单字段 ---
        // IP 地址
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("摄像头 IP 地址:"), gbc);

        ipField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(ipField, gbc);

        // 端口
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("端口:"), gbc);

        portField = new JTextField("554");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(portField, gbc);

        // 用户名
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("用户名:"), gbc);

        userField = new JTextField("admin");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(userField, gbc);

        // 密码
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("密码:"), gbc);

        passField = new JPasswordField("");
        passField.setEchoChar('*'); // 设置密码掩码
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(passField, gbc);

        // 协议选择
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("流媒体协议:"), gbc);

        protocolCombo = new JComboBox<>(new String[]{"RTSP", "自定义URL"});
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(protocolCombo, gbc);

        // --- 底部按钮区域 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton connectButton = new JButton("连接");
        JButton cancelButton = new JButton("取消");

        // 按钮样式
        connectButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);

        // 将按钮面板添加到主面板底部
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 0, 0, 0); // 按钮和上面表单拉开一点距离
        mainPanel.add(buttonPanel, gbc);

        // --- 业务逻辑 ---
        connectButton.addActionListener(e -> {
            cameraUrl = streamUrl();
            configDialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            cameraUrl = "";
            configDialog.dispose();
        });

        // 按回车键直接触发“连接”
        configDialog.getRootPane().setDefaultButton(connectButton);

        // 配置最终窗口
        configDialog.add(mainPanel);
        configDialog.pack();
        configDialog.setLocationRelativeTo(null); // 居中显示
        configDialog.setResizable(false); // 禁止用户随意拉伸，保持整洁
        configDialog.setVisible(true);
    }

    private String streamUrl() {
        String ip = ipField.getText();
        String port = portField.getText();
        String user = userField.getText();
        String password = new String(passField.getPassword());
        String protocol = (String) protocolCombo.getSelectedItem();
        return buildCameraURL(ip, port, user, password, protocol);
    }

    private String buildCameraURL(String ip, String port, String user, String password,
                                  String protocol) {
        if ("RTSP".equals(protocol)) {
            // 海康威视格式
            return String.format("rtsp://%s:%s@%s:%s/Streaming/Channels/101", user, password, ip, port);
        } else {
            // 自定义URL
            return String.format("rtsp://%s:%s@%s:%s/custom/path", user, password, ip, port);
        }
    }

    public String getCameraUrl() {
        return cameraUrl;
    }
}