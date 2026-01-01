package com.gly.platform.regin.tree.delete;

import com.gly.event.GlobalBus;
import com.gly.event.page.RemoveEvent;
import com.gly.log.Logger;
import com.gly.event.page.PageInfo;
import com.gly.platform.regin.tree.FileTreeNode;
import com.gly.platform.regin.tree.find.Select;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class DeleteWorker extends SwingWorker<Void, Void> {
    private final DefaultMutableTreeNode nodeToRemove;
    private final Path deletePath;
    private Exception error;
    private JTree fileTree;
    private DefaultTreeModel treeModel;

    private DeleteWorker(JTree fileTree, FileTreeNode nodeToRemove) {
        this.fileTree = fileTree;
        this.treeModel = (DefaultTreeModel) fileTree.getModel();
        this.nodeToRemove = nodeToRemove;
        this.deletePath = nodeToRemove.getFile().toPath();
    }

    @Override
    protected Void doInBackground() {
        try {
            deleteFileTree(deletePath);
        } catch (IOException e) {
            error = e;
        }
        return null;
    }

    @Override
    protected void done() {
        try {
            if (error == null) {
                removeTreeNode(nodeToRemove);
                Logger.info("删除成功: " + deletePath);
            } else {
                throw error;
            }
        } catch (Exception ex) {
            Logger.error("删除失败: " + deletePath);
            ex.printStackTrace();
        }
    }

    /**
     * 递归删除文件系统内容
     */
    private void deleteFileTree(Path path) throws IOException {
        if (!Files.exists(path)) {
            Logger.warn(path + "不存在");
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                GlobalBus.dispatch(new RemoveEvent(new PageInfo(file.toFile())));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * 从JTree中移除节点
     */
    private void removeTreeNode(DefaultMutableTreeNode node) {
        if (node.isRoot()) {
            return;
        }
        treeModel.removeNodeFromParent(node);
        // 自动展开父节点
        TreeNode parent = node.getParent();
        if (parent != null) {
            TreePath parentPath = new TreePath(treeModel.getPathToRoot(parent));
            fileTree.expandPath(parentPath);
        }

        // 刷新树显示
        treeModel.reload(parent);
    }

    /**
     * 删除选中的节点及其对应的文件系统内容
     */
    public static void deleteSelectedNode(JTree fileTree) {
        List<FileTreeNode> selectedNodes = Select.getSelectedTreeNodes(fileTree);
        String deleteNames = "将永久删除:\n" + selectedNodes.get(0).getFile();
        if (selectedNodes.size() > 1) {
            deleteNames += "等,\n"+selectedNodes.size() + "个所选项.";
        }

        // 确认对话框
        int confirm = JOptionPane.showConfirmDialog(null,
                deleteNames,
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            for (FileTreeNode selectedNode:selectedNodes) {
                new DeleteWorker(fileTree, selectedNode).execute();
            }
        }
    }
}