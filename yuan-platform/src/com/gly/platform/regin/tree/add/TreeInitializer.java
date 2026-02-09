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
     * Initialize tree nodes (breadth-first traversal + real-time sorting)
     *
     * @param rootNode Root node (must be of type FileTreeNode)
     */
    public static void init(FileTreeNode rootNode) {
        File rootFile = rootNode.getFile();
        List<File> entryList = new ArrayList<>();
        // Use a queue for BFS traversal.
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
                    // Create node (mark whether it's a directory).
                    FileTreeNode childNode = new FileTreeNode(childFile, childFile.isDirectory());
                    for (File oneEntryName : entryList) {
                        if (oneEntryName.equals(childFile)) {
                            childNode.setEntry(true);
                            entryList.remove(oneEntryName);
                            break;
                        }
                    }
                    currentNode.add(childNode);

                    if (childFile.isDirectory()) {
                        // If it is a directory, add it to the queue for continued traversal.
                        queue.add(new AbstractMap.SimpleEntry<>(childNode, childFile));
                    }
                }
            }
        }
    }

    /**
     * Create comparator (consistent with SortedTreeModel)
     *
     * @return Comparator.
     */
    private static Comparator<File> generateComparator() {
        return (f1, f2) -> {
            // Folders first.
            if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else if (!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            }
            // Sort by name (case-insensitive).
            return f1.getName().compareToIgnoreCase(f2.getName());
        };
    }

    /**
     * Entry class path name
     *
     * @param root Root directory.
     * @return Entry class path name.
     */
    private static File getEntry(String root) {
        Path rootPath = Paths.get(root);
        Path pomPath = rootPath.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            if (Pom.hasEntry(pomPath)) {
                File entry = Pom.quickGetEntryFile(rootPath, pomPath.toString());
                if (entry != null && entry.exists()) {
                    System.out.println("Quick access:" + entry.getPath());
                    return entry;
                } else {
                    Pom pom = new Pom(pomPath);
                    pom.parseProjectInfo();
                    entry = pom.getEntryFile();
                    if (entry != null && entry.exists()) {
                        System.out.println("Parse entry:" + entry.getPath());
                        return entry;
                    }
                }
            }
        }
        return null;
    }
}