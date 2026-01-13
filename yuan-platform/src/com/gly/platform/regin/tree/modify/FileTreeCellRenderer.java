package com.gly.platform.regin.tree.modify;

import com.gly.platform.regin.tree.FileTreeNode;
import com.gly.util.IconUtil;
import com.gly.util.VideoFileUtil;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.io.File;

/**
 * Document node renderer.
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
    // open folder.
    private final Icon openFolderIcon = getIcon("/icons/folder_open.png");

    // close folder.
    private final Icon folderIcon = getIcon("/icons/folder_close.png");

    // icon of a regular file.
    private final Icon fileIcon = getIcon("/icons/file.png");

    // word
    private final Icon wordIcon = getIcon("/icons/word.png");

    // text
    private final Icon textIcon = getIcon("/icons/text.png");

    // excel
    private final Icon excelIcon = getIcon("/icons/excel.png");

    // powerpoint
    private final Icon powerpointIcon = getIcon("/icons/powerpoint.png");

    // code
    private final Icon codeIcon = getIcon("/icons/code.png");

    // m
    private final Icon mIcon = getIcon("/icons/m.png");

    // python
    private final Icon pythonIcon = getIcon("/icons/python.png");

    // json
    private final Icon jsonIcon = getIcon("/icons/json.png");

    // xml
    private final Icon xmlIcon = getIcon("/icons/xml.png");

    // pdf
    private final Icon pdfIcon = getIcon("/icons/pdf.png");

    // c
    private final Icon cIcon = getIcon("/icons/c.png");

    // h
    private final Icon hIcon = getIcon("/icons/h.png");

    // c++
    private final Icon cppIcon = getIcon("/icons/cplusplus.png");

    // c#
    private final Icon cSharpIcon = getIcon("/icons/csharp.png");

    // visual studio
    private final Icon vsIcon = getIcon("/icons/visualstudio.png");

    // php
    private final Icon phpIcon =getIcon("/icons/php.png");

    // picture
    private final Icon pictureIcon = getIcon("/icons/picture.png");

    // zip
    private final Icon zipIcon = getIcon("/icons/zip.png");

    // video
    private final Icon videoIcon = getIcon("/icons/video.png");

    // model
    private final Icon modelIcon = getIcon("/icons/model.png");

    // The small icon on the entry file.
    private final Icon entryBadge = getIcon("/icons/badge.png");

    // The entry file icon.
    private final Icon entryIcon = IconUtil.createOverlayIcon(codeIcon, entryBadge);

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof FileTreeNode) {
            FileTreeNode node = (FileTreeNode) value;
            if (node.isRoot()) {
                setText(node.getFile().getAbsolutePath());
            }
            if (node.isFile()) {
                File file = node.getFile();
                String name = file.getName().toLowerCase();
                if (name.endsWith(".doc") || name.endsWith(".docx")) {
                    setIcon(wordIcon);
                } else if(name.endsWith(".txt")) {
                    setIcon(textIcon);
                } else if(name.endsWith(".csv") || name.endsWith(".xlsx")) {
                    setIcon(excelIcon);
                } else if(name.endsWith(".ppt") || name.endsWith(".pptx")) {
                    setIcon(powerpointIcon);
                } else if(name.endsWith(".java")) {
                    if (node.isEntry()) {
                        setIcon(entryIcon);
                    } else {
                        setIcon(codeIcon);
                    }
                } else if(name.endsWith(".sln")) {
                    setIcon(vsIcon);
                } else if(name.endsWith(".c")) {
                    setIcon(cIcon);
                } else if(isImage(name)) {
                    setIcon(pictureIcon);
                } else if(name.endsWith(".h")) {
                    setIcon(hIcon);
                } else if(name.endsWith(".cpp")) {
                    setIcon(cppIcon);
                } else if(name.endsWith(".php")) {
                    setIcon(phpIcon);
                } else if(name.endsWith(".cs")) {
                    setIcon(cSharpIcon);
                }  else if(name.endsWith(".pdf")) {
                    setIcon(pdfIcon);
                } else if (name.endsWith(".xml")) {
                    if (name.equals("pom.xml")) {
                        setIcon(mIcon);
                    } else {
                        setIcon(xmlIcon);
                    }
                } else if (name.endsWith(".json")) {
                    setIcon(jsonIcon);
                } else if (name.endsWith(".py")) {
                    setIcon(pythonIcon);
                }else if (name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".jar")) {
                    setIcon(zipIcon);
                } else if (name.endsWith(".pt")) {
                    setIcon(modelIcon);
                } else if (VideoFileUtil.isVideo(name)) {
                    setIcon(videoIcon);
                } else {
                    setIcon(fileIcon);// file icon.
                }
            } else {
                // When the directory is expanded, set the icon for the directory node.
                setIcon(expanded ? openFolderIcon : folderIcon);
            }
        }
        return this;
    }

    /**
     * Determine whether the file is an image based on its extension.
     * @param fileName Pathname of the file with extension.
     * @return Return true if it is an image, false otherwise.
     */
    private boolean isImage(String fileName) {
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif")|| fileName.endsWith(".bmp");
    }

    /**
     * Obtain the icon resource based on the path name.
     * @param file Pathname of the file with extension.
     * @return ImageIcon.
     */
    private ImageIcon getIcon(String file) {
        return IconUtil.getIcon(getClass(), file);
    }
}
