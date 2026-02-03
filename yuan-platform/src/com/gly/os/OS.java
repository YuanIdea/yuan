package com.gly.os;

/**
 * Operating system type enumeration.
 */
public enum OS {
    WINDOWS("Windows", "win"),
    MACOS("macOS", "mac"),
    LINUX("Linux", "nix", "nux"),
    SOLARIS("Solaris", "sunos"),
    AIX("AIX", "aix"),
    UNKNOWN("Unknown");

    private final String displayName;
    final String[] keywords;

    OS(String displayName, String... keywords) {
        this.displayName = displayName;
        this.keywords = keywords;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isUnixLike() {
        return this == LINUX || this == MACOS || this == SOLARIS || this == AIX;
    }
}
