package com.gly.platform.regin.tree.find;

import com.gly.os.OS;
import com.gly.os.OSUtils;

import java.io.File;
import java.io.IOException;

public class FileExplorerUtil {
    /**
     * Open the file in the operating system's file explorer and highlight the specific file.
     *
     * @param file Target file or directory
     * @throws IOException File opening exception.
     */
    public static void showFileInExplorer(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + (file == null ? "null" : file.getAbsolutePath()));
        }

        String absolutePath = file.getAbsolutePath();
        Runtime runtime = Runtime.getRuntime();
        OS osType = OSUtils.getOSType();
        switch (osType) {
            case WINDOWS:
                // On Windows, use explorer /select, and enclose the path in quotes if it contains spaces.
                String strCmd = "explorer /select,\"" + absolutePath + "\"";
                runtime.exec(strCmd);
                break;
            case MACOS:
                // macOS use open -R
                String[] arrCmd = {"open", "-R", absolutePath};
                runtime.exec(arrCmd);
                break;
            case LINUX:
                // On Linux, attempt to use xdg-open, but it cannot guarantee to highlight
                // the specific file—only the directory can be opened.
                File parentDir = file.isDirectory() ? file : file.getParentFile();
                if (parentDir != null && parentDir.exists()) {
                    String[] arrCmd2 = {"xdg-open", parentDir.getAbsolutePath()};
                    runtime.exec(arrCmd2);
                } else {
                    throw new IOException("Unable to open the parent directory of the file");
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operating system:" + osType.getDisplayName());
        }
    }
}
