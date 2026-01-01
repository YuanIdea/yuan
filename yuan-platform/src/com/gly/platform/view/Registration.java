package com.gly.platform.view;

import com.gly.util.JsonUtil;
import com.gly.log.Logger;
import com.gly.platform.app.YuanConfig;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

class Registration extends JDialog {
    Registration(Frame parent) {
        super(parent);
        setTitle( "平台注册");
        setSize(350, 120);
        setLocationRelativeTo(parent);
        setResizable(false);
        showFrame();
    }

    // 显示新建文件对话框
    private void showFrame() {
        this.setLayout(new BorderLayout());//布局管理器
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(null);

        int height = 25;
        int y = 10;

        JTextField codeField = new JTextField(); // 注册码文本框。
        // 文件名输入
        JLabel nameLabel = new JLabel("注册码:");

        ActionListener doAction = e -> {
            register(codeField.getText());
            this.dispose();
        };

        JPopupMenu popup = new JPopupMenu();
        Action pasteAction = new DefaultEditorKit.PasteAction();
        pasteAction.putValue(Action.NAME, "粘贴");
        JMenuItem pasteItem = new JMenuItem(pasteAction);
        popup.add(pasteItem);
        codeField.addMouseListener(new MouseAdapter() {
            private void showIfPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    // 可根据当前选择/剪贴板状态启用/禁用菜单项
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) { showIfPopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { showIfPopup(e); }
        });
        codeField.addActionListener(doAction);

        nameLabel.setBounds(20, y, 100, height);
        fieldPanel.add(nameLabel);
        codeField.setBounds(70, y, 250, height);
        fieldPanel.add(codeField);
        this.add(fieldPanel, "Center");

        // 操作按钮
        JButton okBtn = new JButton("注册");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(okBtn);
        this.add(buttonPanel, "South");
        okBtn.addActionListener(doAction);
        this.setVisible(true);
    }

    /**
     * 注册
     * @param code 注册码。
     */
    private void register(String code) {
        if (code.length() != 32 || containsNonHexChar(code)) {
            Logger.error("注册码错误,无法注册平台.");
        } else {
            Map<String, Object> jsonMap = new LinkedHashMap<>();
            jsonMap.put("code", code);
            JsonUtil.writeJson(YuanConfig.YUAN_PATH.resolve("data/ygl.json").toString(), jsonMap);
            Logger.info("写入注册码,请重新打开后可使用平台.");
        }
    }

    /**
     * 判断字符串中是否有非十六进制字符。
     * 返回 true 表示存在非十六进制字符；false 表示全是十六进制字符（0-9, A-F, a-f）。
     */
    private static boolean containsNonHexChar(String s) {
        if (s == null || s.isEmpty()) {
            return false; // 空串视为不含非十六进制字符（可根据需要改为 true）
        }

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            boolean ok = (c >= '0' && c <= '9')
                    || (c >= 'A' && c <= 'F')
                    || (c >= 'a' && c <= 'f');
            if (!ok) return true; // 存在非十六进制字符
        }
        return false; // 全部是十六进制字符
    }
}
