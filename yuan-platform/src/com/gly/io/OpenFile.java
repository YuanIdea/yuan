package com.gly.io;

import com.gly.log.Logger;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class OpenFile {
    public static void open(File file) {
        if (!file.exists()) {
            Logger.error("文件不存在: " + file.getAbsolutePath());
        }

        if (!Desktop.isDesktopSupported()) {
            System.err.println("当前平台不支持 java.awt.Desktop");
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            Logger.error("打开操作不受支持");
        }

        try {
            desktop.open(file);
        } catch (IOException e) {
            Logger.error("打开文件出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
