package com.gly.platform.regin.tree.add;

import com.gly.event.page.FileType;

import javax.swing.*;
import java.awt.*;

public class CreateJavaConfig extends CreateConfig {
    // 选中的Java的类型。
    private FileType fileType = FileType.Class;

    public CreateJavaConfig(String name) {
        super(name);
    }

    // 显示新建文件对话框
    @Override
    public void initializeUI(Component parent) {
        super.initializeUI(parent);

        int height = 25;
        int y = height + 20; // 下移一行

        // 文件类型选择
        JLabel typeLabel = new JLabel("类型:");
        FileType[] fileTypes = {FileType.Class, FileType.Interface, FileType.Enum};
        JComboBox<FileType> typeComboBox = new JComboBox<>(fileTypes);
        typeComboBox.setSelectedItem(fileType);
        typeComboBox.addActionListener(e -> fileType = (FileType) typeComboBox.getSelectedItem());

        typeLabel.setBounds(20, y, 100, height);
        fieldPanel.add(typeLabel);
        typeComboBox.setBounds(120, y, 140, height);
        fieldPanel.add(typeComboBox);
        dialog.setSize(300, 160);
    }

    public FileType getFileType() {
        return fileType;
    }
}
