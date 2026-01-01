package com.gly.platform.regin.tree.add;

import com.gly.platform.regin.tree.FileTreeNode;
import com.gly.platform.regin.auxiliary.maven.Pom;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TreeInitializer {

    /**
     * 初始化树节点（广度优先遍历 + 实时排序）
     * @param rootNode   根节点（需为 FileTreeNode 类型）
     */
    public static void init(FileTreeNode rootNode) {
        File rootFile = rootNode.getFile();
        List<File> entryList = new ArrayList<>();
        // 使用队列进行 BFS 遍历
        Queue<Map.Entry<FileTreeNode, File>> queue = new LinkedList<>();
        queue.add(new AbstractMap.SimpleEntry<>(rootNode, rootFile));
        Comparator<File> fileComparator = generateComparator(); // 创建比较器
        while (!queue.isEmpty()) {
            Map.Entry<FileTreeNode, File> entry = queue.poll();
            FileTreeNode currentNode = entry.getKey();
            File currentFile = entry.getValue();
            File[] children = currentFile.listFiles();

            if (children != null) {
                List<File> sortedChildren = Arrays.asList(children);// 对子文件排序：文件夹在前，按名称升序
                sortedChildren.sort(fileComparator);
                File entryName = getEntry(currentFile.toString());
                if (entryName != null) {
                    entryList.add(entryName);
                }
                for (File childFile : sortedChildren) {// 添加子节点（已排序）
                    // 创建节点（标记是否为目录）
                    FileTreeNode childNode = new FileTreeNode(childFile, childFile.isDirectory());
                    for (File oneEntryName:entryList) {
                        if (oneEntryName.equals(childFile)) {
                            childNode.setEntry(true);
                            entryList.remove(oneEntryName);
                            break;
                        }
                    }
                    currentNode.add(childNode);

                    if (childFile.isDirectory()) {// 如果是目录，加入队列继续遍历
                        queue.add(new AbstractMap.SimpleEntry<>(childNode, childFile));
                    }
                }
            }
        }
    }

    /**
     * 创建比较器（与 SortedTreeModel 一致）
     * @return 比较器。
     */
    private static Comparator<File> generateComparator() {
       return  (f1, f2) -> {
            // 文件夹优先
            if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else if (!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            }
            // 按名称排序（不区分大小写）
            return f1.getName().compareToIgnoreCase(f2.getName());
        };
    }

    /**
     * 入口类路径名
     * @param root 根目录。
     * @return 入口类路径名。
     */
    private static File getEntry(String root) {
        Path rootPath = Paths.get(root);
        Path pomPath = rootPath.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            if (Pom.hasEntry(pomPath)) {
                File entry = Pom.quickGetEntryFile(rootPath, pomPath.toString());
                if (entry != null && entry.exists()) {
                    System.out.println("快速入口:"+entry.getPath());
                    return entry;
                } else {
                    Pom pom = new Pom(pomPath);
                    pom.parseProjectInfo();
                    entry = pom.getEntryFile();
                    if (entry != null && entry.exists()) {
                        System.out.println("解析入口:"+entry.getPath());
                        return entry;
                    }
                }
            }
        }
        return null;
    }
}