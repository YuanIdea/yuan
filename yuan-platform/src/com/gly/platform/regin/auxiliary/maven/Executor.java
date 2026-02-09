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
 * Maven command executor.
 */
public class Executor extends BaseExecutable {
    // List of commands to be executed.
    private final List<String> goals;

    // Output directory.
    private File outDirectory;

    /**
     * Constructor.
     *
     * @param goals Specify the command set to execute.
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
            System.out.println("✓ " + goals.get(0) + " succeeded!");
            if (outDirectory != null) {
                GlobalBus.dispatch(new AddFileEvent(outDirectory));
            }
        } else {
            System.err.println("✗ " + goals.get(0) + " failed!");
        }
        GlobalBus.dispatch(new DoneEvent(this));
    }

    /**
     * Execute commands using Maven.
     *
     * @param projectDir Project root directory.
     * @param goals      Other parameters.
     * @return Whether the execution was successful.
     */
    public static boolean executeMaven(Path projectDir, Path javaHome, String... goals) {
        try {
            String mavenCommand = YuanConfig.YUAN_PATH.resolve("apache-maven-3.9.0/bin/mvn.cmd").toString();
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

            System.out.println("Execute Maven commands: " + String.join(" ", command));
            Process process = pb.start();

            // 使用 CompletableFuture 处理输出
            CompletableFuture<Void> outputFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.err.println("Read error: " + e.getMessage());
                }
            });

            // 带超时的等待
            boolean success = process.waitFor(10, TimeUnit.MINUTES);
            outputFuture.cancel(true); // 停止输出线程
            return success && process.exitValue() == 0;
        } catch (Exception e) {
            System.err.println("Maven execution failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve encoding information from the command set.
     *
     * @param goals Command set.
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

    public void setOutDirectory(File outDirectory) {
        this.outDirectory = outDirectory;
    }
}
