package com.gly.platform.regin.auxiliary.maven;

import com.gly.event.AddFileEvent;
import com.gly.event.DoneEvent;
import com.gly.event.GlobalBus;
import com.gly.platform.app.YuanConfig;
import com.gly.model.BaseExecutable;
import com.gly.run.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Maven命令执行器。
 */
public class Executor extends BaseExecutable {
    // 命令集
    private List<String> goals;

    // 输出目录。
    private File outFile;

    /**
     * 构造函数。
     * @param goals 指定执行命令集。
     */
    public Executor(String... goals) {
        this.goals = new ArrayList<>(Arrays.asList(goals));
    }

    @Override
    public void start() {
        String root = getRoot();
        Path javaHome = Config.getProjectJavaHome(root);
        boolean success = Executor.executeMaven(Paths.get(root), javaHome, goals.toArray(new String[0]));
        if (success) {
            System.out.println("✓ " + goals.get(0) + "成功!");
            if (outFile!=null) {
                GlobalBus.dispatch(new AddFileEvent(outFile));
            }
        } else {
            System.err.println("✗ " + goals.get(0) + "失败! ");
        }
        GlobalBus.dispatch(new DoneEvent(this));
    }

    /**
     * 使用系统Maven执行命令的简单封装。
     * @param projectDir 项目根目录。
     * @param goals 其它参数。
     * @return 是否执行成功。
     */
    public static boolean executeMaven(Path projectDir, Path javaHome, String... goals) {
        try {
            String mavenCommand = YuanConfig.YUAN_PATH .resolve("apache-maven-3.9.0/bin/mvn.cmd").toString();
            List<String> command = new ArrayList<>();
            command.add(mavenCommand);
            Collections.addAll(command, goals);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(projectDir.toFile());
            pb.redirectErrorStream(true); // 合并错误流和输出流

            String encoding = getEncode(goals);
            Map<String, String> env = pb.environment();
            if (!encoding.isEmpty()) {
                env.put("MAVEN_OPTS", encoding);
            }
            if (javaHome != null) {
                env.put("JAVA_HOME", javaHome.toString());
            }

            System.out.println("执行 Maven 命令: " + String.join(" ", command));
            Process process = pb.start();

            // 使用 CompletableFuture 处理输出
            CompletableFuture<Void> outputFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Maven] " + line);
                    }
                } catch (IOException e) {
                    System.err.println("输出读取错误: " + e.getMessage());
                }
            });

            // 带超时的等待
            boolean success = process.waitFor(10, TimeUnit.MINUTES);
            outputFuture.cancel(true); // 停止输出线程
            return success && process.exitValue() == 0;
        } catch (Exception e) {
            System.err.println("Maven 执行失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取编码。
     * @param goals 命令集。
     */
    private static String getEncode(String... goals) {
        String encoding = "";
        for (String goal : goals) {
            if (goal.contains("-Dfile.encoding")) {
                encoding = goal;
                break;
            }
        }
        return encoding;
    }

    @Override
    public void stop() {

    }

    @Override
    public Object getResult() {
        return null;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }
}
