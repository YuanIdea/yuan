package com.gly.python;

import com.gly.log.Logger;
import com.gly.model.BaseExecutable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PythonRunner extends BaseExecutable {
    private final String pythonHome;

    private final boolean isWin;

    private final Charset encode;

    private Process process;

    /**
     * Constructor.
     *
     * @param pythonHome Python home.
     * @param encode     The encoding method used.
     */
    public PythonRunner(String pythonHome, Charset encode) {
        this.pythonHome = pythonHome;
        isWin = System.getProperty("os.name").toLowerCase().contains("win");
        this.encode = encode;
    }

    @Override
    public void start() {
        try {
            String pythonExecutable = pythonHome + (isWin ? "/python.exe" : "/bin/python");
            if (!Files.exists(Paths.get(pythonExecutable))) {
                System.err.println(pythonExecutable + " not found.");
            }
            ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-X", "utf8", getName());
            pb.directory(new File(getRoot()));

            // Set environment variables.
            Map<String, String> env = pb.environment();
            env.put("PYTHONUNBUFFERED", "1");
            env.put("PYTHONIOENCODING", encode.toString());

            process = pb.start();

            CompletableFuture<Void> outputFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(process.getInputStream(), encode))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.err.println("Output read error:" + e.getMessage());
                }
            });

            CompletableFuture<Void> errorFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(process.getErrorStream(), encode))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    System.err.println("Error stream read error:" + e.getMessage());
                }
            });

            boolean exitCode = process.waitFor(10, TimeUnit.MINUTES);
            outputFuture.get(5, TimeUnit.SECONDS);
            errorFuture.get(5, TimeUnit.SECONDS);
            System.out.println("Process exited with code: " + exitCode);
        } catch (Exception e) {
            System.err.println("Python execution failed: " + e.getMessage());

        }
    }

    @Override
    public void stop() {
        if (process != null && process.isAlive()) {
            try {
                // Attempt graceful termination.
                Logger.info("Sending interrupt signal...");
                // For Python processes, send a Ctrl+C signal.
                if (isWin) {
                    // Windows: send taskkill
                    Runtime.getRuntime().exec("taskkill /PID " + process.pid() + " /T /F");
                } else {
                    // Unix-like: send SIGTERM
                    process.destroy();
                }

                // Wait for a period of time.
                if (!process.waitFor(3, TimeUnit.SECONDS)) {
                    Logger.warn("Process did not exit gracefully, forcing termination...");
                    process.destroyForcibly();
                    process.waitFor(2, TimeUnit.SECONDS);
                }
                Logger.info("Process terminated.");
            } catch (Exception e) {
                Logger.error("Error stopping process: " + e.getMessage());
                if (process != null && process.isAlive()) {
                    process.destroyForcibly();
                }
            }
        }
    }

    @Override
    public Object getResult() {
        return null;
    }
}