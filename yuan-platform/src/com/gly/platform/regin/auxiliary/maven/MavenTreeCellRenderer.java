package com.gly.platform.regin.auxiliary.maven;

import com.gly.util.IconUtil;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;

/**
 * Custom tree renderer.
 */
class MavenTreeCellRenderer extends DefaultTreeCellRenderer {
    private final Icon m2Icon = getIcon("/icons/m2.png");
    private final Icon projectIcon = IconUtil.createOverlayIcon(getIcon("/icons/folder_close.png"), m2Icon, 0.5, 9, 0);
    private final Icon moduleIcon = IconUtil.createOverlayIcon(getIcon("/icons/file.png"), m2Icon, 0.5, 9, 0);

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
            }
        }

        return this;
    }

    private ImageIcon getIcon(String file) {
        return new ImageIcon(getClass().getResource(file));
    }
}
