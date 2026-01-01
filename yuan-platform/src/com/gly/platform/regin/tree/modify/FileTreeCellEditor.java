package com.gly.platform.regin.tree.modify;

import com.gly.event.GlobalBus;
import com.gly.event.page.RenameEvent;
import com.gly.event.page.RenamePageInfo;
import com.gly.util.NameValidator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;

/**
 * 自定义树单元格编辑器
 */
public class FileTreeCellEditor extends DefaultCellEditor {
    private File originalFile;
    private DefaultMutableTreeNode editingNode;
    private JTree tree;

    public FileTreeCellEditor(JTree tree) {
        super(new JTextField());
        this.tree = tree;
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                boolean isSelected, boolean expanded, boolean leaf, int row) {
        editingNode = (DefaultMutableTreeNode) value;
        originalFile = (File) editingNode.getUserObject();
        JTextField textField = (JTextField) editorComponent;
        textField.setText(originalFile.getName());
        return editorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        String newName = ((JTextField) editorComponent).getText().trim();
        if (newName.isEmpty()) {// 基础验证（空名称检查）
            JOptionPane.showMessageDialog(tree, "文件名不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return originalFile;
        }

        // 情况1: 新名称与原名称相同
        if (newName.equals(originalFile.getName())) {
            return originalFile; // 无需操作
        }

        File newFile = new File(originalFile.getParentFile(), newName);
        // 情况2: 目标文件已存在
        if (newFile.exists()) {
            JOptionPane.showMessageDialog(tree, "目标名称已存在: " + newFile.getName(),
                    "重命名失败", JOptionPane.ERROR_MESSAGE);
            return originalFile;
        }

        // 情况3: 权限检查
        if (!originalFile.canWrite()) {
            JOptionPane.showMessageDialog(tree, "没有写入权限: " + originalFile.getName(),
                    "权限错误", JOptionPane.ERROR_MESSAGE);
            return originalFile;
        }

        // 执行重命名
        boolean success = originalFile.renameTo(newFile);
        if (success) {
            GlobalBus.dispatch(new RenameEvent(new RenamePageInfo(originalFile, newFile)));
            editingNode.setUserObject(newFile);
            DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
            treeModel.nodeChanged(editingNode);
            return newFile;
        } else {
            // 高级错误分析
            String errorReason = analyzeRenameFailure(originalFile, newFile);
            JOptionPane.showMessageDialog(tree, "重命名失败: " + errorReason,
                    "错误", JOptionPane.ERROR_MESSAGE);
            return originalFile;
        }
    }

    /**
     * 错误原因分析工具方法
     * @param src 原始文件。
     * @param dest 重名名文件。
     * @return 错误描述。
     */
    private String analyzeRenameFailure(File src, File dest) {
        // 原因1: 包含文件名称不合法
        String info = NameValidator.validateFileName(dest.getName());
        if (!info.equals(NameValidator.VALID)) {
            return info;
        }

        // 原因2: 文件被锁定（Windows系统）
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            if (NameValidator.isFileLocked(src)) {
                return "文件被其他程序占用";
            }
        }

        // 其他未知原因
        return "未知系统错误";
    }
}
