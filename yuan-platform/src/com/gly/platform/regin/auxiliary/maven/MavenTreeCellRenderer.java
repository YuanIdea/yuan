package com.gly.platform.regin.auxiliary.maven;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

// 自定义树渲染器
class MavenTreeCellRenderer extends DefaultTreeCellRenderer {
    private final Icon projectIcon = new ImageIcon(getClass().getResource("/icons/folder_close.png"));
    private final Icon moduleIcon = new ImageIcon(getClass().getResource("/icons/file.png"));

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof MavenProject) {
                MavenProject project = (MavenProject) userObject;
                setText(project.toString());

                if (node == tree.getModel().getRoot()) {
                    setIcon(projectIcon);
                } else {
                    setIcon(moduleIcon);
                }
            } else if ("Maven Projects".equals(userObject)) {
                setIcon(new ImageIcon(getClass().getResource("/icons/file.png")));
            }
        }

        return this;
    }
}
