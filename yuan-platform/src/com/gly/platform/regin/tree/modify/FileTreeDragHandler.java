package com.gly.platform.regin.tree.modify;

import com.gly.log.Logger;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;

/**
 * 文件拖动处理。
 */
public class FileTreeDragHandler extends TransferHandler {
    private final JTree tree;
    private final DefaultTreeModel treeModel;

    public FileTreeDragHandler(JTree tree) {
        this.tree = tree;
        this.treeModel = (DefaultTreeModel) tree.getModel();
    }

    // 1. 启用拖放操作
    public void enableDragAndDrop() {
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(this);
    }

    // 2. 创建可传输对象（被拖动的节点）
    @Override
    protected Transferable createTransferable(JComponent c) {
        TreePath[] selectionPaths = tree.getSelectionPaths();
        if (selectionPaths == null || selectionPaths.length == 0)
            return null;

        return new NodesTransferable(selectionPaths);
    }

    // 3. 指定支持的拖放操作（移动）
    @Override
    public int getSourceActions(JComponent c) {
        return MOVE; // 只允许移动操作
    }

    // 4. 处理拖放目标验证
    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDrop())
            return false;

        // 检查拖放目标是否有效
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath destPath = dl.getPath();
        if (destPath == null)
            return false;

        DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) destPath.getLastPathComponent();
        File targetFile = (File) targetNode.getUserObject();

        // 目标必须是目录
        return targetFile.isDirectory();
    }

    // 5. 执行实际的拖放操作（文件移动+树更新）
    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support))
            return false;

        try {
            // 获取拖放目标
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            TreePath destPath = dl.getPath();
            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) destPath.getLastPathComponent();
            File targetDir = (File) targetNode.getUserObject();

            // 获取被拖动的节点
            Transferable t = support.getTransferable();
            TreePath[] sourcePaths = (TreePath[]) t.getTransferData(NodesTransferable.TREE_PATH_FLAVOR);

            // 执行文件移动和树更新
            return moveNodes(sourcePaths, targetNode, targetDir);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. 移动节点和对应文件
    private boolean moveNodes(TreePath[] sourcePaths, DefaultMutableTreeNode targetNode, File targetDir) {
        boolean allSuccess = true;
        List<DefaultMutableTreeNode> movedNodes = new ArrayList<>();
        Map<DefaultMutableTreeNode, DefaultMutableTreeNode> originalParents = new HashMap<>();

        try {
            // 第一阶段：移动所有文件
            for (TreePath path : sourcePaths) {
                DefaultMutableTreeNode sourceNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                File sourceFile = (File) sourceNode.getUserObject();
                File destFile = new File(targetDir, sourceFile.getName());

                // 执行文件系统移动
                if (sourceFile.renameTo(destFile)) {
                    // 记录原始父节点（用于错误恢复）
                    originalParents.put(sourceNode, (DefaultMutableTreeNode) sourceNode.getParent());
                    // 更新节点关联的文件对象
                    sourceNode.setUserObject(destFile);
                    movedNodes.add(sourceNode);
                    Logger.info("已移动: " + sourceFile + " -> " + destFile);
                } else {
                    JOptionPane.showMessageDialog(tree, "移动失败: " + sourceFile.getName(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                    allSuccess = false;
                    break;
                }
            }

            // 第二阶段：更新树结构（仅在文件移动成功后）
            if (allSuccess) {
                for (DefaultMutableTreeNode node : movedNodes) {
                    // 从原位置移除
                    treeModel.removeNodeFromParent(node);
                    // 添加到新位置
                    treeModel.insertNodeInto(node, targetNode, targetNode.getChildCount());
                }
                tree.expandPath(new TreePath(targetNode.getPath()));
            }
        } catch (Exception e) {
            // 错误处理：恢复移动的文件
            recoverMovedFiles(movedNodes, originalParents);
            allSuccess = false;
        }

        return allSuccess;
    }

    // 7. 错误恢复：回滚文件移动
    private void recoverMovedFiles(List<DefaultMutableTreeNode> movedNodes,
                                   Map<DefaultMutableTreeNode, DefaultMutableTreeNode> originalParents) {
        for (DefaultMutableTreeNode node : movedNodes) {
            try {
                File movedFile = (File) node.getUserObject();
                File originalParentDir = ((File) originalParents.get(node).getUserObject());
                File originalFile = new File(originalParentDir, movedFile.getName());

                // 将文件移回原位置
                if (movedFile.renameTo(originalFile)) {
                    node.setUserObject(originalFile);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
