package com.gly.run;

import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class CompileUtils {
    /**
     * 动态编译多个 Java 源文件
     * @param sourcePath 源码根目录
     * @param dependencies 依赖文件列表
     * @param outputDir 编译输出目录
     * @param code 编码方式
     * @return 是否编译成功
     */
    static boolean compile(String sourcePath, List<String> dependencies, String outputDir, String code) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("找不到 Java 编译器");
            return false;
        }

        // 配置编译参数
        List<String> command = new ArrayList<>();
        command.add("-d");
        command.add(outputDir);
        command.add("-encoding");
        command.add(code);
        command.add("-cp");
        command.add(String.join(File.pathSeparator, dependencies));
        command.add("-sourcepath");
        command.add(sourcePath);
        command.add("-implicit:class");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            // 获取所有需要编译的文件对象
            List<String> sourceFiles = getFileList(sourcePath);
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(sourceFiles);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, command, null, compilationUnits);
            boolean success = task.call();
            // 处理诊断信息
            diagnostics.getDiagnostics().forEach(d -> {
                String message = formatDiagnostic(d);
                if (d.getKind() == Diagnostic.Kind.ERROR) {
                    System.err.println(message);
                } else {
                    System.out.println("警告：" + message);
                }
            });
            return success;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String formatDiagnostic(Diagnostic<?> diagnostic) {
        return String.format("行 %d: %s%n", diagnostic.getLineNumber(), diagnostic.getMessage(null));
    }

    /**
     * 获得当前目录下的java文件列表。
     * @param sourcePath 指定的目录。
     * @return 目录下的java文件列表。
     */
    private static List<String> getFileList(String sourcePath) {
        try {
            Path sourceDir = Paths.get(sourcePath);
            List<Path> javaFiles = Files.walk(sourceDir)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            if (javaFiles.isEmpty()) {
                System.err.println("错误: 在 " + sourceDir + " 中未找到Java文件");
                return null;
            }

            // 构建javac命令
            List<String> command = new ArrayList<>();
            // 添加所有Java文件
            for (Path javaFile : javaFiles) {
                command.add(javaFile.toString());
            }
            return command;
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
        return null;
    }

}