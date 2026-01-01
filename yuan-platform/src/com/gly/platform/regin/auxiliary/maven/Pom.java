package com.gly.platform.regin.auxiliary.maven;

import com.gly.platform.app.YuanConfig;
import com.gly.run.Config;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pom {
    private static final Pattern SOURCE_DIRECTORY_PATTERN = Pattern.compile("<sourceDirectory>([^<]+)</sourceDirectory>");
    private static final Pattern OUTPUT_DIRECTORY_PATTERN = Pattern.compile("<outputDirectory>([^<]+)</outputDirectory>");
    private static final Pattern[] MAIN_CLASS_PATTERNS = new Pattern[]{
            Pattern.compile("<main\\.class>([^<]+)</main\\.class>"),
            Pattern.compile("<mainClass>([^<]+)</mainClass>"),
            Pattern.compile("<exec\\.mainClass>([^<]+)</exec\\.mainClass>"),
            Pattern.compile("<start-class>([^<]+)</start-class>")
    };
    private static final Pattern[] ENCODING_PATTERNS = new Pattern[]{
            Pattern.compile("<project\\.build\\.sourceEncoding>([^<]+)</project\\.build\\.sourceEncoding>"),
            Pattern.compile("<encoding>([^<]+)</encoding>"),
            Pattern.compile("<maven\\.compiler\\.encoding>([^<]+)</maven\\.compiler\\.encoding>")
    };

    // 编译源文件根目录。
    private String sourceDirectory;

    // 编译输出目录。
    private String outputDirectory;

    // 工程根根目录。
    public Path projectPath;

    // 主类。
    public String mainClass;

    // 编码信息。
    public String sourceEncoding;

    // 是否需要生成新的effective-pom。
    private boolean needGenerate = true;

    /**
     * 构造函数。
     * @param pomPath pom.xml文件路径名。
     */
    public Pom(Path pomPath) {
        this.projectPath = pomPath.getParent();
    }

    /**
     * 解析pom.xml
     */
    public void parseProjectInfo() {
        // 检查是否已经有缓存的解析结果
        String name = YuanConfig.YUAN_PROJECT + "/effective-pom.xml";
        Path effectivePomPath = projectPath.resolve(name);
        if (!needGenerate) {
            needGenerate = !Files.exists(effectivePomPath);
        }

        if (needGenerate) {
            // 只生成一次 effective-pom.xml
            boolean success = Executor.executeMaven(projectPath, Config.getProjectJavaHome(projectPath.toString()),
                    "help:effective-pom", "-Doutput="+name, "-q");
            if (!success) {
                System.err.println("生成" + name + "失败");
            }
        }

        try {
            String content = Files.readString(effectivePomPath, StandardCharsets.UTF_8);
            // 一次性解析所有信息
            sourceDirectory = extractValue(content, SOURCE_DIRECTORY_PATTERN, "src/main/java");
            outputDirectory = extractValue(content, OUTPUT_DIRECTORY_PATTERN, "target/classes");
            mainClass = extractFirstMatch(content, MAIN_CLASS_PATTERNS, "Main");
            sourceEncoding = extractFirstMatch(content, ENCODING_PATTERNS, "UTF-8");
        } catch (Exception e) {
            System.err.println("解析项目信息失败: " + e.getMessage());
            // 确保临时文件被清理
            if (needGenerate) {
                try {
                    Files.deleteIfExists(effectivePomPath);
                } catch (IOException ioException) {
                    // 忽略清理错误
                }
            }
        }
    }

    public static boolean hasEntry(Path pomPath) {
        try {
            String content = Files.readString(pomPath, StandardCharsets.UTF_8);
            if (content != null) {
                return content.contains("mainClass") || content.contains("main.class");
            }
        } catch (Exception e) {
            System.err.println("解析项目信息失败: " + e.getMessage());
        }
        return false;
    }

    private static String extractValue(String content, Pattern pattern, String defaultValue) {
        String value = extractFirst(content, pattern);
        if (value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    private static String extractFirstMatch(String content, Pattern[] patterns, String defaultValue) {
        for (Pattern pattern : patterns) {
            String value = extractFirst(content, pattern);
            if (!value.isEmpty()) {
                return value;
            }
        }
        return defaultValue;
    }

    private static String extractFirst(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            if (!value.isEmpty() && !value.contains("${")) {
                return value;
            }
        }
        return "";
    }

    /**
     * 获得输出目录中第一级目录的绝对路径。
     * @return 得输出目录中第一级目录的绝对路径。
     */
    public Path getOutputRoot() {
        return resolveFirstSegment(projectPath, outputDirectory);
    }

    /**
     * 获得子目录中第一级目录的绝对路径。
     * @param root 根目录。
     * @param subPath 子目录。
     * @return 目录中第一级目录的绝对路径。
     */
    private static Path resolveFirstSegment(Path root, String subPath) {
        if (subPath != null && !subPath.trim().isEmpty()) {
            // 统一替换反斜杠为斜杠，避免不同分隔符的问题
            String normalized = subPath.replace(root.toString(), "").replace('\\', '/');
            String[] parts = normalized.split("/+"); // 连续分隔符也能处理
            // 找到第一个非空的段
            for (String p : parts) {
                if (!p.isEmpty()) {
                    return root.resolve(p);
                }
            }
        }
        return root;
    }

    /**
     * 编译类路径名。
     * @return 获得编译路径名。
     */
    public String getClassPath() {
        List<String> classpathElements = new ArrayList<>();
        classpathElements.add(outputDirectory);
        classpathElements.addAll(parseDependenciesWithTransitive());
        return String.join(File.pathSeparator, classpathElements);
    }

    public Path absoluteClassPath() {
        return Paths.get(outputDirectory).resolve("classpath.txt");
    }

    private List<String> parseDependenciesWithTransitive() {
        List<String> dependencies = new ArrayList<>();
        try {
            // 读取生成的类路径文件
            Path classpathFile = absoluteClassPath();
            if (Files.exists(classpathFile)) {
                String classpathContent = Files.readString(classpathFile);
                String[] paths = classpathContent.split(File.pathSeparator);// 拆分类路径（Windows用; Linux/Mac用:）
                dependencies.addAll(Arrays.asList(paths));
                Files.deleteIfExists(classpathFile);// 清理临时文件
            } else {
                System.err.println("生成类路径失败");
            }
        } catch (Exception e) {
            System.err.println("解析传递依赖失败: " + e.getMessage());

        }
        return dependencies;
    }

    /**
     * 获得入口文件。
     * @return 入口文件路径名。
     */
    public File getEntryFile() {
        if (mainClass == null || mainClass.isEmpty()) {
            return null;
        }
        return getEntryFile(sourceDirectory, mainClass);
    }

    public void setNeedGenerate(boolean needGenerate) {
        this.needGenerate = needGenerate;
    }

    public String getSourceDirectory() {
        return sourceDirectory;
    }

    static Model readPom(String pomFilePath) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader fileReader = new FileReader(pomFilePath)) {
            return reader.read(fileReader);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 快速获得入口文件。
     * @param pomFilePath pom.xml的路径名。
     * @return 入口文件名。
     */
    public static File quickGetEntryFile(Path root, String pomFilePath) {
        Model model = readPom(pomFilePath);
        if (model == null) {
            return null;
        }
        Properties properties = model.getProperties();
        if (properties == null) {
            return null;
        }

        String mainClass = properties.getProperty("main.class");
        if (mainClass == null || mainClass.isEmpty()) {
            mainClass = properties.getProperty("exec.mainClass");
            if (mainClass == null || mainClass.isEmpty()) {
                return null;
            }
        }

        String source = null;
        Build build = model.getBuild();
        if (build != null) {
            source = build.getSourceDirectory();
        }
        File file = getEntryFile(root, source, mainClass).toFile();
        if (file.exists()) {
            return file;
        } else {
            file = getEntryFile(root,"src", mainClass).toFile();
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private static File getEntryFile(String sourceDirectory, String mainClass) {
        String entry = mainClass.replace(".", "\\");
        if (!entry.contains(".java")) {
            entry += ".java";
        }
        if (sourceDirectory == null) {
            return new File(entry);
        } else {
            return new File(sourceDirectory + "\\" + entry);
        }
    }

    private static Path getEntryFile(Path root, String sourceDirectory, String mainClass) {
        String entry = mainClass.replace(".", "\\");
        if (!entry.contains(".java")) {
            entry += ".java";
        }
        if (sourceDirectory == null) {
            return root.resolve(entry);
        } else {
            return root.resolve(sourceDirectory + "\\" + entry);
        }
    }
}