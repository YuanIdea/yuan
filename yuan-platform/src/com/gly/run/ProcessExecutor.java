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
 * 平台启动入口类。
 */
public class ProcessExecutor extends BaseExecutable {
    private boolean enableDebug = false;
    
    // 添加控制变量
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
                "-Dfile.encoding="+pom.sourceEncoding,
                "-q",
                "dependency:build-classpath",
                "-DincludeScope=compile",
                "-Dmdep.outputFile=" + pom.absoluteClassPath().toString());
        if (compileSuccess) {
            System.out.println("✓ 编译成功!");
            GlobalBus.dispatch(new AddFileEvent(pom.getOutputRoot().toFile()));
            run(javaHome);
        } else {
            System.err.println("✗ 编译失败! ");
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
            command.add("-Dfile.encoding="+pom.sourceEncoding); // 编码强制指定
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
                String currentPath = env.get("Path");
                env.put("Path", YuanConfig.YUAN_PATH.toString() + File.pathSeparator + currentPath);
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
                System.out.println("进程已被主动停止");
            } else {
                System.out.println("Process finished with exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("执行错误: " + e.getMessage());
        } finally {
            isRunning.set(false);
            currentProcess = null;
            outputThread = null;
            errorThread = null;
        }
    }

    /**
     * 等待进程结束，同时支持中断
     */
    private int waitForProcess() {
        while (isRunning.get()) {
            try {
                // 使用带超时的waitFor，以便定期检查停止标志
                if (currentProcess != null) {
                    // 等待100ms检查一次
                    Thread.sleep(100);
                    try {
                        // 非阻塞方式检查进程是否结束
                        return currentProcess.exitValue();
                    } catch (IllegalThreadStateException e) {
                        // 进程还在运行，检查是否需要停止
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
     * 停止进程
     */
    private void stopProcess() {
        if (currentProcess != null) {
            System.out.println("正在停止进程...");
            // 先尝试优雅关闭
            currentProcess.destroy();
            try {
                // 等待进程优雅退出，最多等待3秒
                if (!currentProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS)) {
                    // 强制终止
                    System.out.println("进程未正常退出，强制终止...");
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
                System.err.println("流读取错误: " + e.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        if (isRunning.get() && !shouldStop.get()) {
            shouldStop.set(true);
            System.out.println("正在请求停止进程...");
            // 停止进程
            stopProcess();
            // 中断读取线程
            if (outputThread != null && outputThread.isAlive()) {
                outputThread.interrupt();
            }
            if (errorThread != null && errorThread.isAlive()) {
                errorThread.interrupt();
            }
            
            // 等待状态更新
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 检查进程是否正在运行
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public Object getResult() {
        return null;
    }
}
