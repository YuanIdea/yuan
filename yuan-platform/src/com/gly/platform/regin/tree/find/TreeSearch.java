package com.gly.platform.regin.tree.find;

import com.gly.log.Logger;
import com.gly.platform.regin.tree.FileTreeNode;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * 树节点查找。
 */
public class TreeSearch {
    /**
     * 在已排序的 JTree 中按层快速查找目标路径对应节点。
     * @param rootNode 根目录节点。
     * @param targetPath 目标文件路径。
     * @return 在文件Tree中对应节点。
     */
    public static FileTreeNode findNodeByPathSorted(FileTreeNode rootNode, String targetPath) {
        if (targetPath == null) {
            Logger.error("findNodeByPathSorted的目标文件为空");
            return null;
        }
        File target = new File(targetPath).getAbsoluteFile();

        Object rootObj = rootNode.getUserObject();
        if (!(rootObj instanceof File)) return null;
        File rootFile = ((File) rootObj).getAbsoluteFile();

        String rootPath = rootFile.getAbsolutePath();
        String targetAbs = target.getAbsolutePath();
        if (!targetAbs.startsWith(rootPath)) {
            return null; // 不在 root 子树下
        }
        if (rootPath.equals(targetAbs)) return rootNode;

        String rel = targetAbs.substring(rootPath.length());
        if (rel.startsWith(File.separator)) rel = rel.substring(1);
        if (rel.isEmpty()) return rootNode;

        String[] parts = rel.split(java.util.regex.Pattern.quote(File.separator));
        FileTreeNode current = rootNode;
        for (String part : parts) {
            current = findChildByNameBinary(current, part);
            if (current == null) return null;
        }
        return current;
    }

    /**
     * 对当前节点的直接子节点（按 name 已排序）进行二分查找。
     * @param parent 当前父节点。
     * @param name 目标节点的子目录或子文件。
     * @return 在文件Tree中对应节点。
     */
    private static FileTreeNode findChildByNameBinary(FileTreeNode parent, String name) {
        int childCount = parent.getChildCount();
        if (childCount == 0) return null;

        // 将子节点名字抽取到数组（避免每次调用 getChildAt 产生重复开销）
        List<String> names = new ArrayList<>(childCount);
        List<FileTreeNode> nodes = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; ++i) {
            FileTreeNode child = (FileTreeNode) parent.getChildAt(i);
            nodes.add(child);
            Object uo = child.getUserObject();
            String n = (uo instanceof File) ? ((File) uo).getName() : (uo == null ? "" : uo.toString());
            names.add(n);
        }

        // 二分查找
        int low = 0, high = names.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = names.get(mid).compareTo(name);
            if (cmp == 0) return nodes.get(mid);
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return null;
    }
}
