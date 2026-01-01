package com.gly.platform.regin.tree.find;

import java.io.File;
import java.io.IOException;

public class FileExplorerUtil {

    /**
     * 在操作系统资源管理器中显示文件，定位到具体文件。
     * @param file 目标文件或目录
     * @throws IOException 文件打开异常。
     */
    public static void showFileInExplorer(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + (file == null ? "null" : file.getAbsolutePath()));
        }

        String absolutePath = file.getAbsolutePath();
        String os = System.getProperty("os.name").toLowerCase();
        Runtime runtime = Runtime.getRuntime();

        if (os.contains("win")) {
            // Windows 通过explorer /select, 路径带空格要用引号
            String cmd = "explorer /select,\"" + absolutePath + "\"";
            runtime.exec(cmd);

        } else if (os.contains("mac")) {
            // macOS 通过 open -R
            String[] cmd = {"open", "-R", absolutePath};
            runtime.exec(cmd);

        } else if (os.contains("nix") || os.contains("nux")) {
            // Linux 这里尝试 xdg-open，无法保证一定定位到文件，只能打开目录
            File parentDir = file.isDirectory() ? file : file.getParentFile();
            if (parentDir != null && parentDir.exists()) {
                String[] cmd = {"xdg-open", parentDir.getAbsolutePath()};
                runtime.exec(cmd);
            } else {
                throw new IOException("无法打开文件的父目录");
            }
        } else {
            throw new UnsupportedOperationException("不支持的操作系统: " + os);
        }
    }
}
