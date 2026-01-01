package com.gly.platform.regin.tree.modify;

import com.gly.platform.regin.tree.FileTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;


/**
 * 文件节点渲染器。
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
    // 目录打开
    private final Icon openFolderIcon = getIcon("/icons/folder_open.png");

    // 目录关闭
    private final Icon folderIcon = getIcon("/icons/folder_close.png");

    // 普通文件图标。
    private final Icon fileIcon = getIcon("/icons/file.png");

    // word图标
    private final Icon wordIcon = getIcon("/icons/word.png");

    // text
    private final Icon textIcon = getIcon("/icons/text.png");

    // excel
    private final Icon excelIcon = getIcon("/icons/excel.png");

    // powerpoint
    private final Icon powerpointIcon = getIcon("/icons/powerpoint.png");

    // code
    private final Icon codeIcon = getIcon("/icons/code.png");

    // m图标
    private final Icon mIcon = getIcon("/icons/m.png");

     // json
    private final Icon jsonIcon = getIcon("/icons/json.png");

    // json
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

    // model
    private final Icon modelIcon =getIcon("/icons/model.png");

    // 入口文件上标
    private final Icon entryBadge = getIcon("/icons/badge.png");

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof FileTreeNode) {// 仅处理 FileTreeNode 实例
            FileTreeNode node = (FileTreeNode) value;
            if (node.isRoot()) {
                setText(node.getFile().getAbsolutePath()); //根目录显示完整路径
            }
            if (node.isFile()) {
                File file = node.getFile();
                String name = file.getName().toLowerCase();
                if (name.endsWith(".doc") || name.endsWith(".docx")) {
                    setIcon(wordIcon);// 文件图标
                } else if(name.endsWith(".txt")) {
                    setIcon(textIcon);
                } else if(name.endsWith(".csv") || name.endsWith(".xlsx")) {
                    setIcon(excelIcon);
                } else if(name.endsWith(".ppt") || name.endsWith(".pptx")) {
                    setIcon(powerpointIcon);
                } else if(name.endsWith(".java")) {
                    if (node.isEntry()) {
                        setIcon(createOverlayIcon(codeIcon, entryBadge));
                    } else {
                        setIcon(codeIcon);
                    }
                } else if(name.endsWith(".sln")) {
                    setIcon(vsIcon);
                } else if(name.endsWith(".c")) {
                    setIcon(cIcon);
                } else if(isPicture(name)) {
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
                } else if (name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".jar")) {
                    setIcon(zipIcon);
                } else if (name.endsWith(".pt")) {
                    setIcon(modelIcon);
                } else {
                    setIcon(fileIcon);// 文件图标
                }
            } else {
                setIcon(expanded ? openFolderIcon : folderIcon);// 目录展开状态不同图标
            }
        }
        return this;
    }

    private boolean isPicture(String name) {
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".gif")|| name.endsWith(".bmp");
    }

    private ImageIcon getIcon(String file) {
        return new ImageIcon(getClass().getResource(file));
    }

    // 将 badge 叠加到 baseIcon 的右下角，返回新的 Icon
    private static Icon createOverlayIcon(Icon baseIcon, Icon badge) {
        int w = Math.max(baseIcon.getIconWidth(), badge.getIconWidth());
        int h = Math.max(baseIcon.getIconHeight(), badge.getIconHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        baseIcon.paintIcon(null, g, 0, 0);
        int bx = baseIcon.getIconWidth() - badge.getIconWidth(); // 右上角
        int by = 0;
        if (bx < 0) bx = 0;
        badge.paintIcon(null, g, bx, by);
        g.dispose();
        return new ImageIcon(img);
    }
}
