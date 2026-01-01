package com.gly.platform.regin.tree.modify;

import com.gly.platform.regin.tree.FileTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;

/**
 * 排序模型。
 */
public class SortedTreeModel extends DefaultTreeModel {
    private final Comparator<TreeNode> comparator;

    public SortedTreeModel(FileTreeNode root, Comparator<TreeNode> comparator) {
        super(root);
        this.comparator = comparator;
    }

    @Override
    public void insertNodeInto(MutableTreeNode newChild, MutableTreeNode parent, int index) {
        if (newChild == null || parent == null)
            return;
        // 忽略传入的 index，使用自定义排序逻辑计算插入位置
        int sortedIndex = findInsertionIndex(newChild, parent);
        super.insertNodeInto(newChild, parent, sortedIndex);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        super.valueForPathChanged(path, newValue);
        
        // 延迟排序避免事件冲突
        SwingUtilities.invokeLater(() -> {
            MutableTreeNode node = (MutableTreeNode) path.getLastPathComponent();
            sortChildren(node);
        });
    }

    private void sortChildren(MutableTreeNode parent) {
        if (parent.getChildCount() == 0)
            return;
        // 步骤1: 将子节点转换为 List<MutableTreeNode>（安全过滤非 MutableTreeNode 的节点）
        List<MutableTreeNode> children = new ArrayList<>();
        Enumeration<? extends TreeNode> childrenEnum = parent.children();
        while (childrenEnum.hasMoreElements()) {
            TreeNode node = childrenEnum.nextElement();
            if (node instanceof MutableTreeNode) {
                children.add((MutableTreeNode) node);
            }
        }

        // 步骤2: 使用泛型兼容的比较器排序
        children.sort(comparator);
        // 步骤3: 移除并重新插入节点（保持排序）
        for (MutableTreeNode child : children) {
            super.removeNodeFromParent(child);
        }
        for (MutableTreeNode child : children) {
            super.insertNodeInto(child, parent, findInsertionIndex(child, parent));
        }
    }

    /**
     * 使用二分查找代替线性遍历，提升插入位置计算效率
     * @param newChild 父节点。
     * @param parent 新节点。
     * @return 插入位置。
     */
    private int findInsertionIndex(MutableTreeNode newChild, MutableTreeNode parent) {
        int low = 0;
        int high = parent.getChildCount() - 1;
        // 处理空子节点情况
        if (high < 0) return 0;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            TreeNode midChild = parent.getChildAt(mid);
            // 添加空节点检查
            if (midChild == null) {
                return parent.getChildCount();// 遇到无效节点，中断排序
            }

            int cmp = comparator.compare(newChild, midChild);
            if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }
}
