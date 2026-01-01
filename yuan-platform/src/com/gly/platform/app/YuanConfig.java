package com.gly.platform.app;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 平台配置文件
 */
public class YuanConfig {
    /**
     * 平台根目录。
     */
    public static final Path YUAN_PATH;
    static {
        String val = System.getenv("YUAN_HOME");
        if (val == null) {
            val = System.getProperty("user.dir");
        }
        YUAN_PATH = Paths.get(val); // 给类的静态 final 字段赋值
    }

    /**
     * 默认JDK根目录。
     */
    public static final Path DEFAULT_JAVA_HOME = YUAN_PATH.resolve("jdk-11");

    /**
     * 工程配置目录
     */
    public static final String YUAN_PROJECT = ".yuan";

    /**
     * 工程配置文件。
     */
    public static final String PROJECT_CONFIG = YUAN_PROJECT + "/project.xml";
}
