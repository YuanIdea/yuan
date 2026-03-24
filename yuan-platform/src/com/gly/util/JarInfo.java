package com.gly.util;

/**
 * JAR file information wrapper class.
 *
 * <p>This class encapsulates the file name and version information of a JAR file,
 * providing convenient methods to access and format the data.
 */
public class JarInfo {
    private final String fileName;  // The name of the JAR file, including the .jar extension
    private final String version;   // The version string extracted from the JAR file or its name

    /**
     * Constructs a new JarInfo object.
     *
     * @param fileName The name of the JAR file (including .jar extension)
     * @param version  The version string associated with the JAR file
     */
    public JarInfo(String fileName, String version) {
        this.fileName = fileName;
        this.version = version;
    }

    /**
     * Returns the full file name of the JAR.
     *
     * @return The file name including the .jar extension
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the version information of the JAR.
     *
     * @return The version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the file name without the .jar extension.
     *
     * @return The file name with the .jar suffix removed
     */
    public String getNameWithoutExtension() {
        return fileName.replace(".jar", "");
    }

    /**
     * Gets the full name of the JAR (including version, without the .jar extension).
     * <p>
     * This method returns the base name combined with version information,
     * typically used for display or identification purposes.
     *
     * @return The formatted full name (e.g., "my-library-1.0.0")
     */
    public String getFullName() {
        return getNameWithoutExtension();
    }

    /**
     * Returns a string representation of the JarInfo object.
     *
     * @return A string containing the file name and version
     */
    @Override
    public String toString() {
        return "JarInfo{" +
                "fileName='" + fileName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
