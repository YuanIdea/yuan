package com.gly.python;

import com.gly.model.BaseExecutable;

import java.io.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PythonRunner extends BaseExecutable {
    // The home of the currently used python.
    private String pythonHome;


    public PythonRunner(String pythonHome) {
        this.pythonHome = pythonHome;
    }

    @Override
    public void start() {
        try {
            ProcessBuilder pb = new ProcessBuilder(pythonHome + "/python.exe", getName());
            pb.directory(new File(getRoot()));

            // Set environment variables.
            Map<String, String> env = pb.environment();
            env.put("PYTHONUNBUFFERED", "1");

            Process process = pb.start();
            CompletableFuture<Void> outputFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.err.println("Output read error:" + e.getMessage());
                }
            });
            process.waitFor(10, TimeUnit.MINUTES);
            outputFuture.cancel(true);
        } catch (Exception e) {
            System.err.println("Python execution failed:: " + e.getMessage());
        }
    }

    /**
     * Set Python path home.
     *
     * @param pythonHome The path home of the currently used python.
     */
    public void setPythonHome(String pythonHome) {
        this.pythonHome = pythonHome;
    }

    @Override
    public void stop() {

    }

    @Override
    public Object getResult() {
        return null;
    }
}