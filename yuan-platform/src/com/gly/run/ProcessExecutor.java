package com.gly.run;

import com.gly.event.AddFileEvent;
import com.gly.event.DoneEvent;
import com.gly.event.GlobalBus;
import com.gly.platform.app.YuanConfig;
import com.gly.model.BaseExecutable;
import com.gly.platform.regin.auxiliary.maven.Executor;
import com.gly.platform.regin.auxiliary.maven.Pom;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Processes that can be run and stopped within the platform.
 */
public class ProcessExecutor extends BaseExecutable {
    private boolean enableDebug = false;

    // Current process.
    private Process currentProcess;
    private Thread outputThread;
    private Thread errorThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);

    private Pom pom;

    ProcessExecutor() {

    }

    @Override
    public void start() {
        String root = getRoot();
        Path rootPath = Paths.get(root);
        Path pomPath = rootPath.resolve("pom.xml");
        pom = new Pom(pomPath);
        pom.parseProjectInfo();
        Path javaHome = Config.getProjectJavaHome(root);
        boolean compileSuccess = Executor.executeMaven(rootPath,
                javaHome,
                "compile",
                "-Dfile.encoding=" + pom.sourceEncoding,
                "-q",
                "dependency:build-classpath",
                "-DincludeScope=compile",
                "-Dmdep.outputFile=" + pom.absoluteClassPath().toString());
        if (compileSuccess) {
            System.out.println("✓ Execution succeeded!");
            GlobalBus.dispatch(new AddFileEvent(pom.getOutputRoot().toFile()));
            run(javaHome);
        } else {
            System.err.println("✗ Execution failed!");
        }
        GlobalBus.dispatch(new DoneEvent(this));
    }

    /**
     * 启动平台程序。
     */
    public void run(Path javaHome) {
        try {
            isRunning.set(true);
            shouldStop.set(false);

            // 构建完整的命令参数列表
            List<String> command = new ArrayList<>();
            command.add(javaHome.resolve("bin/java.exe").toString()); // 完整的 Java 路径
            // 添加调试代理（可选）
            if (enableDebug) { // 可配置的调试开关
                int debugPort = 57445; // 随机端口或固定端口
                command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + debugPort);
            }
            command.add("-Dfile.encoding=" + pom.sourceEncoding); // 编码强制指定
            command.add("-classpath");// 添加 classpath（保留完整路径）
            command.add(pom.getClassPath()); // 不再做路径替换
            command.add(pom.mainClass);// 添加主类名
            String formattedCmd = CommandFormatter.formatCommand(command);
            System.out.println(formattedCmd + "\n");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(pom.projectPath.toFile()); //重定向到默认路径。

            if (YuanConfig.YUAN_PATH.toFile().exists()) {
                // 获取环境变量并修改PATH
                Map<String, String> env = pb.environment();
                env.compute("Path", (k, currentPath) -> YuanConfig.YUAN_PATH + File.pathSeparator + currentPath);
            }

            currentProcess = pb.start();
            outputThread = new Thread(() -> readStream(currentProcess.getInputStream()));// 读取标准输出流
            outputThread.setDaemon(true);
            outputThread.start();

            errorThread = new Thread(() -> readStream(currentProcess.getErrorStream()));// 读取标准错误流
            errorThread.setDaemon(true);
            errorThread.start();

            int exitCode = waitForProcess(); // 等待进程结束，同时检查停止标志

            outputThread.join(1000); // 等待输出流读取完成，最多等待1秒
            errorThread.join(1000);  // 确保错误流读取完成，最多等待1秒

            if (shouldStop.get()) {
                System.out.println("Process has been actively stopped.");
            } else {
                System.out.println("Process finished with exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution error: " + e.getMessage());
        } finally {
            isRunning.set(false);
            currentProcess = null;
            outputThread = null;
            errorThread = null;
        }
    }

    /**
     * Wait for the process to end while supporting interruption.
     */
    private int waitForProcess() {
        while (isRunning.get()) {
            try {
                // Use waitFor with a timeout to periodically check the stop flag.
                if (currentProcess != null) {
                    // Check every 100ms while waiting.
                    Thread.sleep(100);
                    try {
                        // Check if the process has ended in a non-blocking manner.
                        return currentProcess.exitValue();
                    } catch (IllegalThreadStateException e) {
                        // Process is still running; check if it needs to be stopped.
                        if (shouldStop.get()) {
                            stopProcess();
                            return -1;
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stopProcess();
                return -1;
            }
        }
        return -1;
    }

    /**
     * Stop process.
     */
    private void stopProcess() {
        if (currentProcess != null) {
            System.out.println("Stopping process...");
            // First attempt graceful shutdown.
            currentProcess.destroy();
            try {
                // Wait for the process to exit gracefully, with a maximum wait of 3 seconds.
                if (!currentProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    // Force termination.
                    System.out.println("Process did not exit normally, forcing termination...");
                    currentProcess.destroyForcibly();
                    currentProcess.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                currentProcess.destroyForcibly();
            }
        }
    }

    private void readStream(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, pom.sourceEncoding))) {
            String line;
            while ((line = reader.readLine()) != null && !shouldStop.get()) {
                System.out.println(line);
            }
        } catch (IOException e) {
            if (!shouldStop.get()) {
                System.err.println("Stream read error: " + e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        if (isRunning.get() && !shouldStop.get()) {
            shouldStop.set(true);
            System.out.println("Requesting to stop the process...");
            // Stop process.
            stopProcess();
            // Interrupt the read thread.
            if (outputThread != null && outputThread.isAlive()) {
                outputThread.interrupt();
            }
            if (errorThread != null && errorThread.isAlive()) {
                errorThread.interrupt();
            }

            // Wait for status update.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Check if the process is running.
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public Object getResult() {
        return null;
    }
}
