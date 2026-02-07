package com.gly.i18n;

public enum Language {
    ZH_CN("zh_CN", "简体中文"),
    EN_US("en_US", "English");

    private final String code;
    private final String displayName;

    Language(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Language fromCode(String code) {
        for (Language lang : values()) {
            if (lang.code.equals(code)) {
                return lang;
            }
        }
        return ZH_CN; // Default language used.
    }
}
