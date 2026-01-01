package com.gly.platform.regin.tree;

import com.gly.platform.regin.tree.delete.DeleteWorker;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;


class RightMenu {
    // 树形资源管理器
    private TreeDockable treeDockable;

    // 弹出菜单
    private JPopupMenu popupMenu;

    // 添加
    private JMenu addItem;

    // 添加文件
    private JMenuItem openItem;

    // 设计文件
    private JMenuItem designItem;

    // 剪切
    private JMenuItem cutItem;

    // 复制
    private JMenuItem copyItem;

    // 重命名文件
    private JMenuItem renameItem;

    // 删除
    private JMenuItem deleteItem;

    RightMenu(TreeDockable treeDockable) {
        this.treeDockable = treeDockable;
    }

    /**
     * 添加右键菜单
     */
    void createRightMenu() {
        JTree tree = treeDockable.getTree();
        popupMenu = new JPopupMenu();
        addItem = new JMenu("添加");

        JMenuItem addJavaClassItem = new JMenuItem("Java Class");
        addJavaClassItem.addActionListener(e -> treeDockable.newJava()); // 新建Java类
        addItem.add(addJavaClassItem);

        JMenuItem addFileItem = new JMenuItem("新建文件");
        addFileItem.addActionListener(e -> treeDockable.newFile("新建文件")); // 新建文件
        addItem.add(addFileItem);

        JMenuItem addFoldItem = new JMenuItem("新建文件夹");
        addFoldItem.addActionListener(e -> treeDockable.newFolder()); // 新建件夹
        addItem.add(addFoldItem);
        popupMenu.add(addItem);

        openItem = new JMenuItem("打开文件");
        openItem.addActionListener(e ->  treeDockable.openFile(false)); // 打开文件

        designItem = new JMenuItem("设计文件");
        designItem.addActionListener(e -> treeDockable.openFile(true)); // 设计设计

        popupMenu.addSeparator();
        cutItem = new JMenuItem("剪切");
        cutItem.addActionListener(e -> treeDockable.cut());

        copyItem = new JMenuItem("复制");
        popupMenu.add(copyItem);
        copyItem.addActionListener(e -> treeDockable.copy());

        JMenuItem pasteItem = new JMenuItem("粘贴");
        pasteItem.addActionListener(e -> treeDockable.paste());
        popupMenu.add(pasteItem);
        popupMenu.addSeparator();

        // 删除功能。
        // 定义一个 Action（可以在外部类或内部类）
        Action deleteAction = new AbstractAction("删除") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DeleteWorker.deleteSelectedNode(tree);
            }
        };
        deleteItem = new JMenuItem(deleteAction);
        popupMenu.add(deleteItem);
        // 绑定 Delete 键
        tree.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        tree.getActionMap().put("delete", deleteAction);

        renameItem = new JMenuItem("重命名");
        renameItem.addActionListener(e -> treeDockable.renameNode()); // 重命名。
        popupMenu.addSeparator();

        JMenuItem openOutItem = new JMenuItem("在资源管理器中显示");
        openOutItem.addActionListener(e -> treeDockable.openDiskFile()); // 外部打开。
        popupMenu.add(openOutItem);

        JMenuItem refresh = new JMenuItem("刷新");
        popupMenu.add(refresh);
        refresh.addActionListener(e -> treeDockable.refresh());
    }

    /**
     * 点击事件为右键处理
     * @param e 点击事件。
     */
    void rightHandler(MouseEvent e) {
        JTree tree = treeDockable.getTree();
        if (SwingUtilities.isRightMouseButton(e)) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            FileTreeNode selectedNode;
            if (path != null) {
                selectedNode = (FileTreeNode) path.getLastPathComponent();
                popupMenu.remove(cutItem); // 先移除，避免重复
                popupMenu.remove(openItem);
                popupMenu.remove(designItem);
                popupMenu.remove(renameItem);

                if (!selectedNode.isRoot()) { // 不是根目录
                    popupMenu.add(cutItem, getMenuIndex(copyItem));
                    popupMenu.add(renameItem, getMenuIndex(deleteItem) + 1);
                    if (selectedNode.isFile()) {
                        popupMenu.add(openItem, getMenuIndex(addItem) + 1);
                    }
                }

                // 如果点击的节点不在当前选择中，则重新选择
                if (!tree.getSelectionModel().isPathSelected(path)) {
                    tree.getSelectionModel().setSelectionPath(path);
                }

                // 显示弹出菜单
                if (tree.getSelectionCount() > 0) {
                    popupMenu.show(tree, e.getX(), e.getY());
                }
            }
        }
    }

    /**
     * 返回指定 JMenuItem 在 JPopupMenu 中的索引，如果找不到返回 -1。
     * @param menuItem 需要查找的 JMenuItem
     * @return 索引位置，找不到就返回 -1
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
