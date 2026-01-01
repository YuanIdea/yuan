package com.gly.platform.regin.tree.find;

import com.gly.log.Logger;
import com.gly.platform.regin.tree.FileTreeNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 树形选中工具类。
 */
public class Select {

    /**
     * 获取当前选中的树节点
     * @param fileTree 文件树组件
     * @return 选中的节点，如果没有选中则返回空节点。
     */
    public static FileTreeNode getSelectedTreeNode(JTree fileTree) {
        if (fileTree == null) {
            Logger.warn("根目录不存在");
            return null;
        }
        TreePath selectionPath = fileTree.getSelectionPath();
        if (selectionPath == null)
            return null;
        return (FileTreeNode) selectionPath.getLastPathComponent();
    }

    /**
     * 获取所有选中的树节点（支持多选）
     * @param fileTree 文件树组件
     * @return 选中的节点列表，如果没有选中则返回空列表
     */
    public static List<FileTreeNode> getSelectedTreeNodes(JTree fileTree) {
        List<FileTreeNode> selectedNodes = new ArrayList<>();

        // 获取所有选中的路径
        TreePath[] selectionPaths = fileTree.getSelectionPaths();
        if (selectionPaths == null || selectionPaths.length == 0) {
            return selectedNodes;
        }

        // 遍历所有选中的路径
        for (TreePath path : selectionPaths) {
            Object lastComponent = path.getLastPathComponent();
            if (lastComponent instanceof FileTreeNode) {
                selectedNodes.add((FileTreeNode) lastComponent);
            }
        }

        return selectedNodes;
    }

    /**
     * 获得选择的路径。
     * @param tree 文件树组件。
     * @return 被选中的路径。
     */
    public static Path getSelectedPath(JTree tree) {
        FileTreeNode fileNode = getSelectedTreeNode(tree);
        if (fileNode != null) {
            // 直接返回文件对应的路径
            return fileNode.getFile().toPath();
        } else {
            return null;
        }
    }

    /**
     * 获得选择的路径。
     * @param tree 文件树组件。
     * @return 被选中的路径。
     */
    public static List<File> getSelectedFiles(JTree tree) {
        List<FileTreeNode> selectedNodes = getSelectedTreeNodes(tree);
        if (selectedNodes != null) {
            // 直接返回文件对应的路径
            List<File> paths = new LinkedList<>();
            for (FileTreeNode fileNode:selectedNodes) {
                paths.add(fileNode.getFile());
            }
            return paths;
        } else {
            return null;
        }
    }
}
