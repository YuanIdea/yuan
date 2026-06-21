package com.gly;

import javax.swing.*;
import java.awt.*;

public class IPCamera {
    private JDialog configDialog;
    private JTextField ipField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField passField;
    private JComboBox<String> protocolCombo;
    private JTextField channelField;
    private String cameraUrl;

    IPCamera(Frame parent) {
        createConfigGUI(parent);
    }

    private void createConfigGUI(Frame parent) {
        configDialog = new JDialog(parent, "IP摄像头配置", true);
        configDialog.setLayout(new GridLayout(0, 2, 10, 10));

        // IP 地址
        JLabel ipLabel = new JLabel("摄像头IP地址:");
        ipField = new JTextField("169.254.105.154");

        // 端口
        JLabel portLabel = new JLabel("端口:");
        portField = new JTextField("554");

        // 用户名
        JLabel userLabel = new JLabel("用户名:");
        userField = new JTextField("admin");

        // 密码
        JLabel passLabel = new JLabel("密码:");
        passField = new JPasswordField("");

        // 协议选择
        JLabel protocolLabel = new JLabel("协议:");
        protocolCombo = new JComboBox<>(new String[]{
                "RTSP", "自定义URL"
        });

        // 通道选择（用于多通道摄像头）
        JLabel channelLabel = new JLabel("通道:");
        channelField = new JTextField("1");

        JButton connectButton = new JButton("连接");
        JButton cancelButton = new JButton("取消");

        configDialog.add(ipLabel);
        configDialog.add(ipField);
        configDialog.add(portLabel);
        configDialog.add(portField);
        configDialog.add(userLabel);
        configDialog.add(userField);
        configDialog.add(passLabel);
        configDialog.add(passField);
        configDialog.add(protocolLabel);
        configDialog.add(protocolCombo);
        configDialog.add(channelLabel);
        configDialog.add(channelField);
        configDialog.add(connectButton);
        configDialog.add(cancelButton);

        connectButton.addActionListener(e -> {
            cameraUrl = streamUrl();
            configDialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            cameraUrl = "";
            configDialog.dispose();
        });

        configDialog.pack();
        configDialog.setLocationRelativeTo(null);
        configDialog.setSize(400, 300);
        configDialog.setVisible(true);
    }

    private String streamUrl() {
        String ip = ipField.getText();
        String port = portField.getText();
        String user = userField.getText();
        String password = new String(passField.getPassword());
        String protocol = (String) protocolCombo.getSelectedItem();
        String channel = channelField.getText();
        return buildCameraURL(ip, port, user, password, protocol, channel);
    }

    private String buildCameraURL(String ip, String port, String user, String password,
                                  String protocol, String channel) {
        if ("RTSP".equals(protocol)) {
            // 海康威视格式
            return String.format("rtsp://%s:%s@%s:%s/Streaming/Channels/101", user, password, ip, port, channel);
        } else {
            // 自定义URL
            return String.format("rtsp://%s:%s@%s:%s/custom/path", user, password, ip, port);
        }
    }

    public String getCameraUrl() {
        return cameraUrl;
    }
}

