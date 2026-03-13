package com.gly.platform.editor;

import com.gly.event.page.FileType;
import com.gly.platform.regin.auxiliary.maven.Pom;
import com.gly.util.PathUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Code generator.
 */
public class CodeGenerator {

    /**
     * Generate default Java code.
     *
     * @param root Project root directory.
     * @param file Newly added file.
     * @param type Newly added type.
     * @return Default class implementation code.
     */
    public static String generateJava(String root, File file, FileType type) {
        Path rootPath = Paths.get(root);
        Path pomPath = rootPath.resolve("pom.xml");
        String packageInfo = "";
        if (Files.exists(pomPath)) {
            Pom pom = new Pom(pomPath);
            pom.setNeedGenerate(false);
            pom.parseProjectInfo();
            String src = pom.getSourceDirectory();
            packageInfo = CodeGenerator.packageNameFromFile(file.toString(), src);
        }
        String strType = "class";
        if (type == FileType.Class) {
            strType = "class";
        } else if (type == FileType.Interface) {
            strType = "interface";
        } else if (type == FileType.Enum) {
            strType = "enum";
        }
        return CodeGenerator.generateJava(packageInfo, strType, PathUtil.getName(file.getName()));
    }

    /**
     * Generate default Java code.
     *
     * @param packageInfo Package information.
     * @param type        added type.
     * @param className   Class name.
     * @return Default class implementation code.
     */
    private static String generateJava(String packageInfo, String type, String className) {
        if (packageInfo == null || packageInfo.isEmpty()) {
            return String.format("public %s %s {\n" + "}", type, className);
        } else {
            return String.format("package %s;\n" + "\n" + "public %s %s {\n" + "}", packageInfo, type, className);
        }
    }

    /**
     * Generate Java package name from the full file path and source root directory path.
     *
     * @param filePathName Full path of the file.
     * @param srcRoot      Source root directory path
     * @return Package name; returns an empty string if the file is not under srcRoot (or could throw an exception)
     * @throws IllegalArgumentException If input is null or the path is invalid.
     */
    private static String packageNameFromFile(String filePathName, String srcRoot) {
        if (filePathName == null || srcRoot == null) {
            throw new IllegalArgumentException("filePathName and srcRoot must not be null");
        }

        try {
            // 使用 Path 来标准化并处理分隔符
            Path filePath = Paths.get(filePathName).normalize();
            Path srcPath = Paths.get(srcRoot).normalize();

            // 如果 srcPath 是文件而不是目录，也尝试取其父目录（容错）
            // 例如 srcRoot 可能传入 ".../src/com" 之类的情况，这里我们假定传入的 srcRoot 是源码根路径
            // 保留这一行以便更严格的检查：如果你希望 srcRoot 必须是目录并存在，可以额外检查 Files.isDirectory(srcPath)
            // srcPath = srcPath;

            // 计算相对路径：filePath 相对于 srcPath
            Path relative;
            try {
                relative = srcPath.relativize(filePath);
            } catch (IllegalArgumentException e) {
                // 当 filePath 与 srcPath 不在同一个根（不同盘符）时，会抛出 IllegalArgumentException
                // 尝试把两者都转换为绝对路径后再 relativize
                Path absSrc = srcPath.toAbsolutePath().normalize();
                Path absFile = filePath.toAbsolutePath().normalize();
                try {
                    relative = absSrc.relativize(absFile);
                } catch (IllegalArgumentException ex) {
                    // 不在同一个根（例如不同驱动器），认为文件不在 src 下
                    return "";
                }
            }

            // relative 现在是像 "com/gly/model/test.java"
            // 如果相对路径以 ".." 开头，说明 filePath 不在 srcPath 目录下
            if (relative.startsWith("..") || relative.toString().isEmpty()) {
                return "";
            }

            // 去掉最后一个元素（文件名）
            Path parent = relative.getParent();
            if (parent == null) {
                // 文件正好位于 src 根目录下，没有包
                return "";
            }

            // 将目录分隔符替换为点，返回包名
            String pkg = parent.toString().replace(File.separatorChar, '.');

            // Windows Path#toString 可能使用 '\'，替换所有 '/' 也安全
            pkg = pkg.replace('/', '.').replace('\\', '.');

            // 去掉可能的重复点（防御性）：将连续多个点合并为一个，并去除首尾点
            pkg = pkg.replaceAll("\\.+", ".").replaceAll("^\\.|\\.$", "");

            return pkg;
        } catch (InvalidPathException ipe) {
            throw new IllegalArgumentException("Invalid path: " + ipe.getMessage(), ipe);
        }
    }
}
