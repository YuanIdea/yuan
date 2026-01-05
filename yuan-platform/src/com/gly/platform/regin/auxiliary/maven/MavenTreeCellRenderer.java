package com.gly.platform.regin.auxiliary.maven;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

/**
 * Custom tree renderer.
 */
class MavenTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon folderIcon;
    private Icon fileIcon;

    public MavenTreeCellRenderer(Icon folderIcon, Icon fileIcon) {
        this.folderIcon = folderIcon;
        this.fileIcon = fileIcon;
    }
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            setText(userObject.toString());

            if (node == tree.getModel().getRoot() && !node.isLeaf()) {
                setIcon(folderIcon);
            } else {
                setIcon(fileIcon);
            }
        }

        return this;
    }
}
