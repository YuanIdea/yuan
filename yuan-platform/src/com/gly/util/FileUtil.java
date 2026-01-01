package com.gly.util;

import com.gly.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class FileUtil {
    /**
     * 将字符串内容保存到指定的 Java 文件
     * @param pathName 保存文件路径名。
     * @param content 保存内容。
     */
    public static void saveToFile(String pathName, String content, String code) {
        try {
            Path path = Paths.get(pathName);
            Files.createDirectories(path.getParent());// 如果父目录不存在，创建目录
            Files.write(path, content.getBytes(code),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);// 写入内容，覆盖原文件
            Logger.info("保存成功:" + pathName);
        }  catch (FileAlreadyExistsException e) {
            System.err.println("路径已存在且不是目录: " + e.getFile());
        } catch (AccessDeniedException e) {
            System.err.println("权限不足");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadFile(Path path, String encoding) {
        try {
            // 假设fileName是完整路径，或相对路径
            if (!Files.exists(path)) {
                Logger.info("文件不存在: " + path.getFileName());
                return "";
            }
            String content = new String(Files.readAllBytes(path), encoding);
            content = removeBOM(content);

            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 处理 UTF-8 BOM 等特殊字符。
     * @param content 文件内容。
     * @return 处理了BOM后的文件内容。
     */
    private static String removeBOM(String content) {
        if (content.startsWith("\uFEFF")) { // UTF-8 BOM
            return content.substring(1);
        }
        return content;
    }

    /**
     * 判断child是否是parent文件夹的子文件（包括子文件夹中的文件）
     * @param parent  目标文件夹
     * @param child   待判断的文件
     * @return 如果child是parent文件夹的子文件，返回true；否则返回false
     */
    public static boolean isChildOf(File parent, File child) {
        try {
            // 获取标准化的绝对路径，便于比较
            String parentPath = parent.getCanonicalPath();
            String childPath = child.getCanonicalPath();

            // 判断child的路径是否以parent路径开头，且child不等于parent本身
            return childPath.startsWith(parentPath + File.separator);
        } catch (IOException e) {
            // 出现路径异常，返回false
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 计算重命名parent文件夹后，child文件的对应新路径
     * @param oldParent 旧的parent文件夹
     * @param newParent 新的parent文件夹（重命名后的文件夹）
     * @param child     原child文件
     * @return  重命名后child对应的新File路径
     * @throws IOException
     */
    public static File getChildParentRename(File oldParent, File newParent, File child) throws IOException {
        // 获取规范化路径
        String oldParentPath = oldParent.getCanonicalPath();
        String newParentPath = newParent.getCanonicalPath();
        String childPath = child.getCanonicalPath();

        // 确保child是oldParent的子路径
        if (!childPath.startsWith(oldParentPath)) {
            throw new IllegalArgumentException("child 不是 oldParent 的子路径");
        }

        // 计算child相对于oldParent的子路径
        String relativePath = childPath.substring(oldParentPath.length());

        // 拼接新的路径
        String newChildPath = newParentPath + relativePath;

        return new File(newChildPath);
    }
}
