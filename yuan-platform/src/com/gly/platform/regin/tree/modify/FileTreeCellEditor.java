package com.gly.platform.regin.tree.modify;

import com.gly.event.GlobalBus;
import com.gly.event.page.RenameEvent;
import com.gly.event.page.RenamePageInfo;
import com.gly.platform.app.YuanConfig;
import com.gly.util.NameValidator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;

/**
 * Custom tree cell editor.
 */
public class FileTreeCellEditor extends DefaultCellEditor {
    private File originalFile;
    private DefaultMutableTreeNode editingNode;
    private final JTree tree;

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
        if (newName.isEmpty()) {// Basic validation (empty name check).
            JOptionPane.showMessageDialog(tree, "文件名不能为空", "错误", JOptionPane.ERROR_MESSAGE);
            return originalFile;
        }

        // The new name is the same as the original name.
        if (newName.equals(originalFile.getName())) {
            return originalFile;
        }

        File newFile = new File(originalFile.getParentFile(), newName);
        // The new name is the same as the original name.
        if (newFile.exists()) {
            JOptionPane.showMessageDialog(tree, "目标名称已存在: " + newFile.getName(),
                    "重命名失败", JOptionPane.ERROR_MESSAGE);
            return originalFile;
        }

        // Permission check.
        if (!originalFile.canWrite()) {
            JOptionPane.showMessageDialog(tree, "没有写入权限: " + originalFile.getName(),
                    "权限错误", JOptionPane.ERROR_MESSAGE);
            return originalFile;
        }

        // Perform the rename operation.
        boolean success = originalFile.renameTo(newFile);
        if (success) {
            GlobalBus.dispatch(new RenameEvent(new RenamePageInfo(originalFile, newFile)));
            editingNode.setUserObject(newFile);
            DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
            treeModel.nodeChanged(editingNode);
            return newFile;
        } else {
            // Advanced error analysis.
            String errorReason = analyzeRenameFailure(originalFile, newFile);
            JOptionPane.showMessageDialog(tree, "重命名失败: " + errorReason,
                    "错误", JOptionPane.ERROR_MESSAGE);
            return originalFile;
        }
    }

    /**
     * Error cause analysis tool.
     *
     * @param src  Original file.
     * @param dest Renamed file.
     * @return Error description information.
     */
    private String analyzeRenameFailure(File src, File dest) {
        // File name is invalid.
        String info = NameValidator.validateFileName(dest.getName());
        if (!info.equals(NameValidator.VALID)) {
            return info;
        }

        // File is locked (Windows system).
        if (YuanConfig.isWin) {
            if (NameValidator.isFileLocked(src)) {
                return "文件被其他程序占用";
            }
        }

        // Other unknown reasons.
        return "未知系统错误";
    }
}
