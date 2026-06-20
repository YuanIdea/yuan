package com.gly;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Dialog {
    static File open(Component parent, String defaultPath) {
        JFileChooser jf = createChose(defaultPath);
        jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int value = jf.showOpenDialog(parent);
        if (value == JFileChooser.APPROVE_OPTION) {
            return jf.getSelectedFile();
        } else {
            return null;
        }
    }

    private static JFileChooser createChose(String root) {
        JFileChooser jf = new JFileChooser();
        jf.setCurrentDirectory(new File(root));
        jf.setFileHidingEnabled(false);
        return jf;
    }
}
