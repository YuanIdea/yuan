package com.gly.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JAR file finder utility for locating JAR files with a specific prefix in a directory and extracting version numbers.
 */
public class JarFinder {

    /**
     * Finds a JAR file with the specified prefix in the given directory.
     *
     * @param directoryPath The directory path to search
     * @param prefix        The file prefix (e.g., "yuan-platform" or "yuan-common")
     * @return JarInfo object containing the file name and version; returns null if not found
     */
    public static JarInfo findJarByPrefix(String directoryPath, String prefix) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Directory does not exist or is not a valid directory: " + directoryPath);
            return null;
        }

        // Compile regular expression: match prefix + version number (digits and dots) + .jar
        // Version format: digits(dot digits)* e.g., 1.0.8, 2.1.3, 1.0.0-SNAPSHOT
        String regex = "^" + Pattern.quote(prefix) + "-(\\d+(?:\\.\\d+)*(?:-SNAPSHOT)?)\\.jar$";
        Pattern pattern = Pattern.compile(regex);

        // Filter matching files
        File[] matchingFiles = directory.listFiles((dir, name) -> {
            Matcher matcher = pattern.matcher(name);
            return matcher.matches();
        });

        if (matchingFiles == null || matchingFiles.length == 0) {
            return null;
        }

        // If there are multiple matches, return the first one (can be customized to select the latest version)
        File selectedFile = matchingFiles[0];

        // Extract version number
        Matcher matcher = pattern.matcher(selectedFile.getName());
        if (matcher.matches()) {
            String version = matcher.group(1);
            return new JarInfo(selectedFile.getName(), version);
        }

        return null;
    }

    /**
     * Finds all JAR files matching the prefix (sorted by version).
     *
     * @param directoryPath The directory path
     * @param prefix        The file prefix
     * @return A list of all matching JarInfo objects, sorted in descending order by version
     */
    public static List<JarInfo> findAllJarsByPrefix(String directoryPath, String prefix) {
        File directory = new File(directoryPath);
        List<JarInfo> result = new ArrayList<>();

        if (!directory.exists() || !directory.isDirectory()) {
            return result;
        }

        String regex = "^" + Pattern.quote(prefix) + "-(\\d+(?:\\.\\d+)*(?:-SNAPSHOT)?)\\.jar$";
        Pattern pattern = Pattern.compile(regex);

        File[] files = directory.listFiles((dir, name) -> {
            Matcher matcher = pattern.matcher(name);
            return matcher.matches();
        });

        if (files != null) {
            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    result.add(new JarInfo(file.getName(), matcher.group(1)));
                }
            }

            // Sort by version number (descending, latest first)
            result.sort((a, b) -> compareVersions(b.getVersion(), a.getVersion()));
        }

        return result;
    }

    /**
     * Compares two version numbers (supports numeric versions and SNAPSHOT).
     *
     * @param v1 Version 1
     * @param v2 Version 2
     * @return Positive if v1 > v2, negative if v1 < v2, 0 if equal
     */
    public static int compareVersions(String v1, String v2) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return -1;
        if (v2 == null) return 1;

        // Handle SNAPSHOT versions
        boolean isV1Snapshot = v1.endsWith("-SNAPSHOT");
        boolean isV2Snapshot = v2.endsWith("-SNAPSHOT");

        String cleanV1 = v1.replace("-SNAPSHOT", "");
        String cleanV2 = v2.replace("-SNAPSHOT", "");

        String[] parts1 = cleanV1.split("\\.");
        String[] parts2 = cleanV2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }

        // If numeric parts are the same, compare SNAPSHOT flag (non-SNAPSHOT versions are considered larger)
        if (isV1Snapshot && !isV2Snapshot) return -1;
        if (!isV1Snapshot && isV2Snapshot) return 1;
        return 0;
    }

    /**
     * Convenience method: finds and returns the file name combined with version.
     *
     * @param directoryPath The directory path
     * @param prefix        The file prefix
     * @return A string in the format "filename-version", e.g., "yuan-platform-1.0.8"; returns null if not found
     */
    public static String findJarNameWithVersion(String directoryPath, String prefix) {
        JarInfo info = findJarByPrefix(directoryPath, prefix);
        return info != null ? info.getFullName() : null;
    }
}
