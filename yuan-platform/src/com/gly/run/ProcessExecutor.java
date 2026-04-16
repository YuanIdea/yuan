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
     * Start the platform program.
     */
    public void run(Path javaHome) {
        try {
            isRunning.set(true);
            shouldStop.set(false);

            // Build the complete command argument list
            List<String> command = getCommand(javaHome);
            String formattedCmd = CommandFormatter.formatCommand(command);
            System.out.println(formattedCmd + "\n");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(pom.projectPath.toFile()); //Redirect to the default path.

            if (YuanConfig.YUAN_PATH.toFile().exists()) {
                // Get environment variables and modify the PATH.
                Map<String, String> env = pb.environment();
                env.compute("Path", (k, currentPath) ->
                        YuanConfig.YUAN_PATH + File.pathSeparator + currentPath);
            }

            currentProcess = pb.start();
            outputThread = new Thread(() -> readStream(currentProcess.getInputStream()));// 读取标准输出流
            outputThread.setDaemon(true);
            outputThread.start();

            errorThread = new Thread(() -> readStream(currentProcess.getErrorStream()));// 读取标准错误流
            errorThread.setDaemon(true);
            errorThread.start();

            int exitCode = waitForProcess(); // Wait for the process to end while checking the stop flag.

            // Wait for the output stream to finish reading, with a maximum wait of 1 second.
            outputThread.join(1000);
            // Ensure the error stream has finished reading, with a maximum wait of 1 second.
            errorThread.join(1000);

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

    private List<String> getCommand(Path javaHome) {
        List<String> command = new ArrayList<>();
        command.add(javaHome.resolve("bin/java.exe").toString()); // Complete path to java.exe.
        // Add a debugging proxy.
        if (enableDebug) {
            int debugPort = 57445;
            command.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=" + debugPort);
        }
        command.add("-Dfile.encoding=" + pom.sourceEncoding); // Specify the encoding method to be used.
        command.add("-classpath");// Add classpath (keep full paths).
        command.add(pom.getClassPath());
        command.add(pom.mainClass);// Add the main class name.
        return command;
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

    @Override
    public Object getResult() {
        return null;
    }
}