package com.gly.platform.regin.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

public class FileTreeNode extends DefaultMutableTreeNode {
    private final boolean isDirectory; // 标记是否为文件夹
    private boolean isEntry = false;

    public FileTreeNode(File name, boolean isDirectory) {
        super(name);
        this.isDirectory = isDirectory;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public boolean isFile() {
        return !isDirectory;
    }

    @Override
    public String toString() {
        File root = getFile();
        return root.getName();
    }

    public File getFile() {
        Object obj = getUserObject();
        if (obj instanceof File) {
            return (File) obj;
        }
        // 处理意外情况
        return new File(obj.toString());
    }

    public boolean isEntry() {
        return isEntry;
    }

    public void setEntry(boolean entry) {
        isEntry = entry;
    }
}
