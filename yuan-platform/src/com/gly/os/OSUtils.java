package com.gly.os;

import java.util.Locale;

/**
 * Operating system utility class.
 */
public final class OSUtils {
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    private static final OS OS_TYPE = detectOS();

    private OSUtils() {
        // Utility class; prevent instantiation.
    }

    /**
     * Detect the current operating system.
     */
    private static OS detectOS() {
        for (OS os : OS.values()) {
            if (os.keywords != null) {
                for (String keyword : os.keywords) {
                    if (OS_NAME.contains(keyword)) {
                        return os;
                    }
                }
            }
        }
        return OS.UNKNOWN;
    }

    /**
     * Get the current operating system type.
     */
    public static OS getOSType() {
        return OS_TYPE;
    }

    /**
     * Get operating system name.
     */
    public static String getOSName() {
        return System.getProperty("os.name");
    }

    /**
     * Get operating system architecture.
     */
    public static String getOSArch() {
        return System.getProperty("os.arch");
    }

    /**
     * Get operating system version.
     */
    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    /**
     * Determine if it is a Windows system.
     */
    public static boolean isWindows() {
        return OS_TYPE == OS.WINDOWS;
    }

    /**
     * Determine if it is a macOS system.
     */
    public static boolean isMac() {
        return OS_TYPE == OS.MACOS;
    }

    /**
     * Determine if it is a Linux system.
     */
    public static boolean isLinux() {
        return OS_TYPE == OS.LINUX;
    }

    /**
     * 判断是否是Unix-like系统（Linux, macOS, Solaris, AIX）
     */
    public static boolean isUnixLike() {
        return OS_TYPE.isUnixLike();
    }

    /**
     * Verify if the operating system is supported.
     */
    public static void validateSupportedOS() {
        if (OS_TYPE == OS.UNKNOWN) {
            throw new UnsupportedOperationException(
                    String.format("不支持的操作系统: %s (名称: %s, 架构: %s, 版本: %s)",
                            OS_NAME, getOSName(), getOSArch(), getOSVersion()));
        }
    }

    /**
     * Get detailed operating system information.
     */
    public static String getOSDetail() {
        return String.format("%s %s (%s) [%s]",
                getOSName(), getOSVersion(), getOSArch(), OS_TYPE.getDisplayName());
    }

    /**
     * Check if the operating system is in a Chinese locale.
     */
    public static boolean isChineseOS() {
        String userLanguage = System.getProperty("user.language");
        return "zh".equals(userLanguage);
    }
}
