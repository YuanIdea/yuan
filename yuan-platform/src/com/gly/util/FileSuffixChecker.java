package com.gly.util;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * File suffix checking utility class - Non-recursive implementation
 */
public class FileSuffixChecker {

    /**
     * Check if any file with specified suffixes exists in the directory (including subdirectories)
     *
     * @param directory The directory to search
     * @param suffixes  Array of file suffixes to check (e.g., ".py", ".java")
     * @return true if at least one file with any of the specified suffixes is found
     * @throws IllegalArgumentException if directory is invalid or suffixes array is empty
     */
    public static boolean hasFileWithAnySuffix(File directory, String... suffixes) {
        validateInputs(directory, suffixes);

        // Normalize all suffixes to lowercase and ensure they start with a dot
        String[] normalizedSuffixes = new String[suffixes.length];
        for (int i = 0; i < suffixes.length; ++i) {
            normalizedSuffixes[i] = normalizeSuffix(suffixes[i]);
        }

        // Use BFS (Breadth-First Search) to traverse directory structure
        Queue<File> queue = new LinkedList<>();
        queue.offer(directory);

        while (!queue.isEmpty()) {
            File currentDir = queue.poll();
            File[] files = currentDir.listFiles();

            // Skip directories that cannot be read (permission issues, etc.)
            if (files == null) continue;

            for (File file : files) {
                if (file.isDirectory()) {
                    // Add subdirectory to queue for further exploration
                    queue.offer(file);
                } else {
                    // Check if file has any of the specified suffixes
                    String fileName = file.getName().toLowerCase();
                    for (String suffix : normalizedSuffixes) {
                        if (fileName.endsWith(suffix)) {
                            return true; // File found, exit early
                        }
                    }
                }
            }
        }

        return false; // No matching files found
    }


    /**
     * Validate input parameters for the file search operation
     *
     * @param directory Directory to validate
     * @param suffixes  Suffixes array to validate
     * @throws IllegalArgumentException if directory is invalid or suffixes array is empty
     */
    private static void validateInputs(File directory, String[] suffixes) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Provided path is not a valid directory: " + directory);
        }

        if (suffixes == null || suffixes.length == 0) {
            throw new IllegalArgumentException("File suffixes array cannot be null or empty");
        }
    }

    /**
     * Normalize a file suffix to ensure consistent format
     * Converts suffix to lowercase and ensures it starts with a dot
     *
     * @param suffix The suffix to normalize (e.g., "py", ".PY", ".py")
     * @return Normalized suffix starting with dot and in lowercase (e.g., ".py")
     */
    private static String normalizeSuffix(String suffix) {
        return suffix.startsWith(".") ? suffix.toLowerCase() : "." + suffix.toLowerCase();
    }
}
