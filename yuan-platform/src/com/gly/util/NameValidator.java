package com.gly.util;


import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class NameValidator {
    // 保留名称
    private static final String[] RESERVED_NAMES = {"CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    // 定义非法字符集合
    private static final Set<Character> ILLEGAL_CHARS = new HashSet<Character>() {{
        add('<'); add('>'); add(':'); add('"'); add('/'); add('\\'); add('|'); add('?'); add('*');
    }};

    // 合法名称
    public static final String VALID = "valid";

    private static String validCheck(String name) {
        for (char c : name.toCharArray()) {
            if (ILLEGAL_CHARS.contains(c)) {
                return "包含非法字符：" + c;
            }
            if (c == '/' || c == '\\') {
                return "目录名不能包含路径分隔符";
            }
        }
        return VALID;
    }

    /**
     *  验证文件名合法性
     * @param name 需要验证的名称。
     * @return 文件名称信息描述。
     */
    public static String validateFileName(String name) {
        if (name.isEmpty()) {
            return name + "名称不能为空";
        }

        String result = validCheck(name);
        if (!result.equals(VALID)) {
            return result;
        }

        // 保留名称检查
        for (String reserved : RESERVED_NAMES) {
            if (name.equalsIgnoreCase(reserved)) {
                return "系统保留名称：" + reserved;
            }
        }

        return VALID;
    }

    /**
     *  Windows文件锁定检测（需要JNA依赖）
     * @param file 要检测的文件。
     * @return 是否被锁定。
     */
    public static boolean isFileLocked(File file) {
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            // 如果能获取排他锁说明未被占用
            FileLock lock = channel.tryLock();
            if (lock != null) {
                lock.close();
                return false;
            }
        } catch (IOException e) {
            return true;
        }
        return true;
    }

    public static void validateInputDirectory(Path parentPath, String dirName) throws InvalidPathException {
        // 空值检查
        if (parentPath == null || dirName == null) {
            throw new IllegalArgumentException("参数不能为null");
        }

        // 父目录存在性验证
        if (!Files.exists(parentPath)) {
            throw new InvalidPathException(parentPath.toString(), "父目录不存在");
        }
        if (!Files.isDirectory(parentPath)) {
            throw new InvalidPathException(parentPath.toString(), "父目录路径不是有效目录");
        }

        // 目录名有效性验证
        // 检查非法字符
        String result = validateFileName(dirName);
        if (!result.equals(VALID)) {
            throw new InvalidPathException(dirName, result);
        }

        // 检查结尾字符
        if ((dirName.endsWith(".") || dirName.endsWith(" "))) {
            throw new InvalidPathException(dirName, "目录名不能以空格或点号结尾");
        }
    }
}
