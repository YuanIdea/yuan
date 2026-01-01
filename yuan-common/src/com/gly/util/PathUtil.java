package com.gly.util;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PathUtil {
    /**
     * 获取无后缀文件名。
     * @param nameSuffix 有后缀的文件名。
     * @return 无后缀文件名。
     */
    public static String getName(String nameSuffix) {
        if (nameSuffix.contains(".")) {
            return nameSuffix.substring(0, nameSuffix.lastIndexOf('.'));
        } else {
            return nameSuffix;
        }
    }

    /**
     * 查找最后一个目录。
     * @param path 路径。
     * @return 最后一个目录。
     */
    public static String getLast(String path) {
        String newPath = path.replace("\\", "/");
        if (newPath.contains("/")) {
            return newPath.substring(newPath.lastIndexOf('/')+1);
        } else {
            return newPath;
        }
    }

    public static String format(String pathName) {
        return pathName.replace("\\", "/");
    }

    /**
     * 获得路径名文件后缀
     * @param path 路径名。
     * @return 不含点的后缀。
     */
    public static String getFileExtension(String path) {
        if (path == null) {
            return "";
        }
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == path.length() - 1) {
            // 没有后缀或者点在最后面
            return "";
        }
        return path.substring(dotIndex + 1);
    }

    public static String getPath(TreeNode[] path) {
        if (path == null || path.length == 0) {
            return "";
        }
        return  Stream.of(path).map(TreeNode::toString).collect(Collectors.joining("/"));
    }

    public static String getPath(String pathName) {
        File file = new File(pathName);
        return file.getParent();
    }

    /**
     * 查找指定目录下与指定后缀名相同的所有文件。
     * @param rootPath 指定要查找的路径。
     * @param suffix 指定后缀。
     * @return 与指定后缀相同的所有文件名列表。
     */
    public static List<String> findFiles(String rootPath, String suffix) {
        List<String> result = new ArrayList<>();
        Queue<File> queue = new LinkedList<>();
        File root = new File(rootPath);
        if (!root.exists() || !root.isDirectory()) {
            return result;
        }

        queue.add(root);
        while (!queue.isEmpty()) {
            File current = queue.poll();
            File[] files = current.listFiles();
            if (files == null) continue;
            for (File f : files) {
                if (f.isDirectory()) {
                    queue.add(f);
                } else if (f.isFile() && f.getName().endsWith(suffix)) {
                    result.add(f.getAbsolutePath());
                }
            }
        }
        return result;
    }

    public static boolean fileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }

    /**
     * 根据基准路径和相对路径计算绝对路径
     * @param basePath 基准路径，可以是当前目录（"."）或其他绝对路径
     * @param relativePath 相对路径字符串
     * @return 绝对路径字符串
     */
    public static String resolveAbsolutePath(String basePath, String relativePath) {
        Path base = Paths.get(basePath);
        Path resolved = base.resolve(relativePath).normalize();
        return resolved.toAbsolutePath().toString();
    }

    /**
     * 从给定 path 开始向上查找第一个存在的父目录（包含自身）。
     * 返回第一个存在的目录的 Path；如果直到根目录都不存在则返回 null。
     *
     * 行为说明：
     * - 如果 path 为 null，返回 null。
     * - 如果 path 指向一个已存在的目录，返回 path。
     * - 如果 path 指向一个已存在的文件，返回其父目录（如果父目录存在），否则继续向上查找。
     * - 如果 path 不存在，则不断上溯 parent，直到找到存在的目录或到达顶层（parent == null）。
     */
    public static Path findExistingParent(Path path) {
        if (path == null) {
            return null;
        }

        // 规范化：将相对路径转换为绝对路径，以便向上查找更直观（可选）
        Path current = path.toAbsolutePath();

        // 如果当前指向存在的路径
        if (Files.exists(current)) {
            if (Files.isDirectory(current)) {
                return current;
            } else {
                // 如果是文件，则尝试返回其父目录（可能为 null）
                Path parent = current.getParent();
                if (parent != null && Files.isDirectory(parent)) {
                    return parent;
                }
                // 若父目录不存在或为 null，则继续从 parent（可能为 null）向上查找
                current = parent;
            }
        }

        // 向上查找父目录，直到找到存在的目录或到达根（parent == null）
        while (current != null) {
            if (Files.exists(current) && Files.isDirectory(current)) {
                return current;
            }
            current = current.getParent();
        }

        // 未找到任何存在的父目录
        return null;
    }

    /**
     * 创建目录文件，目录不存在时自动创建。
     * @param file 路径名。
     * @return 创建的目录文件。
     */
    public static Path createPathFile(Path file) {
        try {
            // 1. 创建父目录（如果存在且不是目录会抛异常）
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent); // 如果已存在则不会报错
            }

            // 2. 创建文件（如果已存在则保持不变）
            try {
                return Files.createFile(file);
            } catch (IOException e) {
                // 文件已存在，直接返回原始 Path
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
