package com.gly.event.page;

import java.io.File;

/**
 * 页面信息。
 */
public class PageInfo {
    // 文件。
    private File file;

    // 是否是设计模式。
    private boolean isDesign;

    private FileType fileType;
    
    /**
     * 页面名称采用全路径名，保持唯一性。
     * @param file 页面名称。
     */
    public PageInfo(File file){
        this.file = file;
        isDesign = false;
        fileType = FileType.Blank;
    }

    public File getFile() {
        return file;
    }

    public boolean nameEqual(PageInfo pageInfo) {
        return  pageInfo.getFile().equals(file);
    }
    
    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    /**
     * Gets the name of this picture.
     * @return the name
     */
    public String getName() {
        return file.getAbsolutePath();
    }

    /**
     * 获取带后缀的名称。
     * @return 后缀名称。
     */
    public String getFileName() {
        return file.getName();
    }

    public boolean isDesign() {
        return isDesign;
    }

    public void setIsDesign(boolean isDesign) {
        this.isDesign = isDesign;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public FileType getFileType() {
        return fileType;
    }
}
