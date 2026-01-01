package com.gly.platform.regin.tree.add;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 创建配置界面
 */
public class CreateConfig {
    // 标题名称
    private String titleName;

    //是否点击了ok
    private boolean ok = false;

    // 创建文件名称。
    private String fileName;

    // 对话框。
    JDialog dialog;

    // 上部分面板。
    JPanel fieldPanel;

    public CreateConfig(String titleName) {
        this.titleName = titleName;
    }

    public void initializeUI(Component parent) {
        dialog = new JDialog();
        dialog.setTitle(titleName);
        dialog.setModal(true);
        dialog.setBounds(600, 200, 300, 120);
        dialog.setLayout(new BorderLayout());//布局管理器

        fieldPanel = new JPanel();
        fieldPanel.setLayout(null);

        int height = 25;
        int y = 10;

        // 文件名输入
        JLabel nameLabel = new JLabel("名称:");
        JTextField nameField = new JTextField();

        ActionListener doAction = e -> {
            this.ok = true;
            fileName = nameField.getText().trim();
            dialog.dispose();
        };
        nameField.addActionListener(doAction);
        nameLabel.setBounds(20, y, 100, height);
        fieldPanel.add(nameLabel);
        nameField.setBounds(120, y, 140, height);
        fieldPanel.add(nameField);
        dialog.add(fieldPanel, "Center");

        // 操作按钮
        JButton okBtn = new JButton("确定");
        JButton cancelBtn = new JButton("取消");
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, "South");

        // 按钮事件
        cancelBtn.addActionListener(
                e -> {
                    this.ok = false;
                    dialog.dispose();});
        okBtn.addActionListener(doAction);
        dialog.setLocationRelativeTo(parent);
    }

    /**
     * 显示新建文件对话框。
     * @param parent 父容器。
     */
    public void showNewFileDialog(Component parent) {
        initializeUI(parent);
        dialog.setVisible(true);
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isOk() {
        return ok;
    }
}
