package com.gly.event.page;

import java.io.File;

/**
 * 重命名页面信息。
 */
public class RenamePageInfo extends PageInfo {
    // 老文件
    private File oldFile;

    /**
     * 构造函数。
     * @param oldFile 老文件。
     * @param newFile 新文件。
     */
    public RenamePageInfo(File oldFile, File newFile) {
        super(newFile);
        this.oldFile = oldFile;
    }

    /**
     * 获得老文件。
     * @return 老文件。
     */
    public File getOldFile() {
        return oldFile;
    }
}
