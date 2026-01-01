package com.gly.platform.view;

import com.gly.event.*;
import com.gly.util.PathUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

class Dialog  {
    /**
     * 保存对话框。
     * @param parent 父容器。
     * @param defaultPath 默认根目录。
     */
    void Save(Component parent, String defaultPath) {
        JFileChooser jf = createChose(defaultPath);
        int value = jf.showSaveDialog(parent);
        if (value == JFileChooser.APPROVE_OPTION) {
            File file = jf.getSelectedFile();
            GlobalBus.dispatch(EventType.saveFile, file.getPath(), this);
        }
    }

    /**
     * 打开对话框。
     * @param parent 父容器。
     * @param defaultPath 默认根目录。
     */
    void Open(Component parent, String defaultPath) {
        JFileChooser jf = createChose(defaultPath);
        jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int value = jf.showOpenDialog(parent);
        if (value == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jf.getSelectedFile();
            String absolutePath = PathUtil.format(selectedFile.getAbsolutePath());
            GlobalBus.dispatch(EventType.openFold, absolutePath, this);
        }
    }

    private static JFileChooser createChose(String root) {
        JFileChooser jf = new JFileChooser();
        jf.setCurrentDirectory(new File(root));
        jf.setFileHidingEnabled(false);
        return jf;
    }
}
