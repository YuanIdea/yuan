package com.gly.platform.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Platform configuration file.
 */
public class YuanConfig {
    /**
     * Root directory of the platform.
     */
    public static final Path YUAN_PATH;

    static {
        String val = System.getenv("YUAN_HOME");
        if (val == null) {
            val = System.getProperty("user.dir");
        }
        YUAN_PATH = Paths.get(val);
    }

    /**
     * Default JDK root directory.
     * If a built-in JDK exists, prioritize its use;
     * otherwise, select the JDK configured in JAVA_HOME.
     */
    public static final Path DEFAULT_JAVA_HOME;

    static {
        Path inlineJavaPath = YUAN_PATH.resolve("jdk-11");
        Path inlineJavaExe = inlineJavaPath.resolve("bin/java.exe");
        if (Files.exists(inlineJavaExe)) {
            DEFAULT_JAVA_HOME = inlineJavaPath;
        } else {
            String javaHome = System.getenv("JAVA_HOME");
            if (javaHome == null) {
                System.err.println("JDK configuration path not found.");
                DEFAULT_JAVA_HOME = null;
            } else {
                DEFAULT_JAVA_HOME = Paths.get(javaHome);
                if (!Files.exists(DEFAULT_JAVA_HOME.resolve("bin/java.exe"))) {
                    System.err.println("JDK configuration path not found:" + DEFAULT_JAVA_HOME);
                }
            }
        }
    }

    /**
     * Project configuration directory.
     */
    public static final String YUAN_PROJECT = ".yuan";

    /**
     * Project configuration file.
     */
    public static final String PROJECT_CONFIG = YUAN_PROJECT + "/project.xml";
}
