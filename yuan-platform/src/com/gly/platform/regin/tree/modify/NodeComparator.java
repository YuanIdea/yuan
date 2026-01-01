package com.gly.platform.regin.tree.modify;

import com.gly.platform.regin.tree.FileTreeNode;

import javax.swing.tree.TreeNode;
import java.util.Comparator;

/**
 * 节点比较器。
 */
public class NodeComparator implements Comparator<TreeNode> {
    @Override
    public int compare(TreeNode node1, TreeNode node2) {
        FileTreeNode n1 = (FileTreeNode) node1;
        FileTreeNode n2 = (FileTreeNode) node2;

        // 文件夹优先
        if (n1.isDirectory() && !n2.isDirectory()) {
            return -1;
        } else if (!n1.isDirectory() && n2.isDirectory()) {
            return 1;
        }

        // 按名称排序（不区分大小写）
        return n1.getUserObject().toString()
                .compareToIgnoreCase(n2.getUserObject().toString());
    }
}
