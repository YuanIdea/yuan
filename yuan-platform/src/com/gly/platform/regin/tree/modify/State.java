package com.gly.platform.regin.tree.modify;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.*;

/**
 * 保存恢复树形资源管理的展开状态。
 */
public class State {
    // 树形资源管理控件。
    private JTree tree;

    // 树形控件各点是否展开的的状态存储。
    private Map<String, Boolean> state;

    /**
     * 初始化树形资源管理控件。
     * @param tree 树形资源管理控件。
     */
    public void init(JTree tree) {
        this.tree = tree;
    }

    /**
     * 保存展开状态（使用文件路径作为唯一标识）
     */
    public void saveExpansionState() {
        state = new HashMap<>();
        if (tree != null) {
            TreeNode root = (TreeNode) tree.getModel().getRoot();
            if (root != null) {
                // 使用栈数据结构
                Stack<Object[]> stack = new Stack<>();
                stack.push(new Object[]{root, new TreePath(root)});

                while (!stack.isEmpty()) {
                    Object[] current = stack.pop();
                    TreeNode currentNode = (TreeNode) current[0];
                    TreePath currentPath = (TreePath) current[1];

                    // 检查当前节点是否展开
                    if (tree.isExpanded(currentPath)) {
                        File file = (File) ((DefaultMutableTreeNode) currentNode).getUserObject();
                        state.put(file.getAbsolutePath(), true);
                    }

                    // 处理子节点（从后向前入栈，保证处理顺序与递归一致）
                    int childCount = currentNode.getChildCount();
                    for (int i = childCount - 1; i >= 0; --i) {
                        TreeNode child = currentNode.getChildAt(i);
                        TreePath childPath = currentPath.pathByAddingChild(child);
                        stack.push(new Object[]{child, childPath});
                    }
                }
            }
        }
    }

    /**
     * 恢复展开状态。
     */
    public void restoreExpansionState() {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        // 使用队列实现广度优先遍历
        Queue<Object[]> queue = new LinkedList<>();
        queue.offer(new Object[]{root, new TreePath(root)});

        while (!queue.isEmpty()) {
            Object[] current = queue.poll();
            TreeNode currentNode = (TreeNode) current[0];
            TreePath currentPath = (TreePath) current[1];

            // 获取节点对应的文件
            File file = (File) ((DefaultMutableTreeNode) currentNode).getUserObject();
            String filePath = file.getAbsolutePath();

            // 如果之前是展开状态，则展开当前节点
            if (state.containsKey(filePath)) {
                tree.expandPath(currentPath);
            }

            // 将子节点添加到队列
            int childCount = currentNode.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                TreeNode child = currentNode.getChildAt(i);
                TreePath childPath = currentPath.pathByAddingChild(child);
                queue.offer(new Object[]{child, childPath});
            }
        }
    }
}
