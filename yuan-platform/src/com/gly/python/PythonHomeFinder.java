package com.gly.python;

import com.gly.os.OSUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Python Home Path Finder.
 * This class provides multiple methods to find the default Python Home on the local system.
 * It attempts various approaches and returns the first valid path found.
 */
public class PythonHomeFinder {

    /**
     * Finds the Python Home.
     * Tries multiple methods in order and returns the first valid path found.
     *
     * @return The Python Home directory path, or null if not found.
     */
    public static String findPythonHome() {
        // Try multiple methods in sequence
        String[] methods = {
                byCmd(),
                whichPython(),
                envPythonHome(),
        };

        for (String path : methods) {
            if (path != null && !path.isEmpty()) {
                return path;
            }
        }
        return null;
    }

    /**
     * Uses Python command to get the directory containing the Python executable.
     * Executes a Python one-liner that prints the directory of sys.executable.
     * This method is reliable as it uses Python's own knowledge of its location.
     *
     * @return The directory path containing the Python executable, or null if command fails.
     */
    private static String byCmd() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "-c",
                    "import sys, os; print(os.path.dirname(sys.executable))");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Uses system commands to find Python executable and extract its directory.
     * On Windows, uses "where python" command; on Unix-like systems, uses "which python3" or "which python".
     *
     * @return The directory path containing the Python executable, or null if not found or command fails.
     */
    private static String whichPython() {
        try {
            String line = getLine();
            if (line != null) {
                // Extract the directory part (remove the executable filename)
                int lastSeparator = Math.max(line.lastIndexOf("\\"), line.lastIndexOf("/"));
                if (lastSeparator > 0) {
                    return line.substring(0, lastSeparator);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Get the command to find Python on different operating systems.
     * @return The command to find Python on different operating systems.
     * @throws IOException Get command IO exception.
     */
    private static String getLine() throws IOException {
        ProcessBuilder pb;
        if (OSUtils.isWindows()) {
            // Windows: use "where" command to locate python.exe
            pb = new ProcessBuilder("cmd.exe", "/c", "where python");
        } else {
            // Unix-like systems: try "which python3" first, then "which python"
            pb = new ProcessBuilder("bash", "-c", "which python3 || which python");
        }

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        return reader.readLine();
    }

    /**
     * Retrieves Python Home directory from the PYTHON_HOME environment variable.
     * This is a common environment variable used to specify the Python installation directory.
     *
     * @return The value of PYTHON_HOME environment variable, or null if not set or empty.
     */
    private static String envPythonHome() {
        String pythonHome = System.getenv("PYTHON_HOME");
        if (pythonHome != null && !pythonHome.isEmpty()) {
            return pythonHome;
        }
        return null;
    }
}
