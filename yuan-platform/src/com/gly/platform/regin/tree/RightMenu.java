package com.gly.platform.regin.tree;

import com.gly.i18n.I18n;
import com.gly.platform.regin.tree.delete.DeleteWorker;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

class RightMenu {
    // Tree Resource Manager.
    private final TreeDockable treeDockable;

    // Pop-up menu.
    private JPopupMenu popupMenu;

    // Add menu.
    private JMenu addItem;

    // Open file menu.
    private JMenuItem openItem;

    // Design file menu.
    private JMenuItem designItem;

    // Cut menu.
    private JMenuItem cutItem;

    // Copy menu.
    private JMenuItem copyItem;

    // Rename file menu.
    private JMenuItem renameItem;

    // Delete menu.
    private JMenuItem deleteItem;

    RightMenu(TreeDockable treeDockable) {
        this.treeDockable = treeDockable;
    }

    /**
     * Add right-click context menu.
     */
    void createRightMenu() {
        JTree tree = treeDockable.getTree();
        popupMenu = new JPopupMenu();
        addItem = new JMenu(I18n.get("add"));

        JMenuItem addJavaClassItem = new JMenuItem(I18n.get("newJava"));
        addJavaClassItem.addActionListener(e -> treeDockable.newJava()); // New Java Class.
        addItem.add(addJavaClassItem);

        String newFile = I18n.get("newFile");
        JMenuItem addFileItem = new JMenuItem(newFile);
        addFileItem.addActionListener(e -> treeDockable.newFile(newFile)); // New File
        addItem.add(addFileItem);

        JMenuItem addFoldItem = new JMenuItem(I18n.get("newFolder"));
        addFoldItem.addActionListener(e -> treeDockable.newFolder()); // New Folder
        addItem.add(addFoldItem);
        popupMenu.add(addItem);

        openItem = new JMenuItem(I18n.get("openFile"));
        openItem.addActionListener(e -> treeDockable.openFile(false)); // Open file

        designItem = new JMenuItem(I18n.get("designFile"));
        designItem.addActionListener(e -> treeDockable.openFile(true)); // Design file

        popupMenu.addSeparator();
        cutItem = new JMenuItem(I18n.get("cut"));
        cutItem.addActionListener(e -> treeDockable.cut());

        copyItem = new JMenuItem(I18n.get("copy"));
        popupMenu.add(copyItem);
        copyItem.addActionListener(e -> treeDockable.copy());

        JMenuItem pasteItem = new JMenuItem(I18n.get("paste"));
        pasteItem.addActionListener(e -> treeDockable.paste());
        popupMenu.add(pasteItem);
        popupMenu.addSeparator();

        // Delete functionality.
        // Define an Action (can be in an outer class or inner class)
        Action deleteAction = new AbstractAction(I18n.get("delete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DeleteWorker.deleteSelectedNode(tree);
            }
        };
        deleteItem = new JMenuItem(deleteAction);
        popupMenu.add(deleteItem);
        // Bind to the Delete key.
        tree.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        tree.getActionMap().put("delete", deleteAction);

        renameItem = new JMenuItem(I18n.get("rename"));
        renameItem.addActionListener(e -> treeDockable.renameNode());
        popupMenu.addSeparator();

        JMenuItem openOutItem = new JMenuItem(I18n.get("showInExplorer"));
        openOutItem.addActionListener(e -> treeDockable.openDiskFile()); // Show in Explorer.
        popupMenu.add(openOutItem);

        JMenuItem refresh = new JMenuItem(I18n.get("refresh"));
        popupMenu.add(refresh);
        refresh.addActionListener(e -> treeDockable.refresh());
    }

    /**
     * Handle the event as a right-click.
     *
     * @param e The click event.
     */
    void rightHandler(MouseEvent e) {
        JTree tree = treeDockable.getTree();
        if (SwingUtilities.isRightMouseButton(e)) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            FileTreeNode selectedNode;
            if (path != null) {
                selectedNode = (FileTreeNode) path.getLastPathComponent();
                // Remove first to avoid duplication.
                popupMenu.remove(cutItem);
                popupMenu.remove(openItem);
                popupMenu.remove(designItem);
                popupMenu.remove(renameItem);

                if (!selectedNode.isRoot()) { // Not the root directory.
                    popupMenu.add(cutItem, getMenuIndex(copyItem));
                    popupMenu.add(renameItem, getMenuIndex(deleteItem) + 1);
                    if (selectedNode.isFile()) {
                        popupMenu.add(openItem, getMenuIndex(addItem) + 1);
                    }
                }

                // If the clicked node is not in the current selection, reselect it.
                if (!tree.getSelectionModel().isPathSelected(path)) {
                    tree.getSelectionModel().setSelectionPath(path);
                }

                // Show the popup menu.
                if (tree.getSelectionCount() > 0) {
                    popupMenu.show(tree, e.getX(), e.getY());
                }
            }
        }
    }

    /**
     * Returns the index of the specified JMenuItem within the JPopupMenu, or -1 if not found.
     *
     * @param menuItem The JMenuItem to search for.
     * @return The index position, or -1 if not found.
     */
    private int getMenuIndex(JMenuItem menuItem) {
        int itemCount = popupMenu.getComponentCount();
        for (int i = 0; i < itemCount; ++i) {
            if (popupMenu.getComponent(i) == menuItem) {
                return i;
            }
        }
        return -1;
    }
}
