package com.gly.i18n;

import java.util.*;
import java.util.prefs.Preferences;

public class I18n {
    private static final String BUNDLE_PATH = "messages";
    private static Locale currentLocale = Locale.SIMPLIFIED_CHINESE;
    private static ResourceBundle bundle;
    private static final Preferences prefs = Preferences.userNodeForPackage(I18n.class);

    static {
        loadSavedLanguage();
        loadBundle();
    }

    public static void switchLanguage(Language language) {
        switch (language) {
            case ZH_CN:
                currentLocale = Locale.SIMPLIFIED_CHINESE;
                break;
            case EN_US:
                currentLocale = Locale.US;
                break;
            default:
                currentLocale = Locale.US;
        }
        saveLanguagePreference(language);
        loadBundle();
    }

    public static String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }


    private static void loadBundle() {
        bundle = ResourceBundle.getBundle(BUNDLE_PATH, currentLocale);
    }

    private static void loadSavedLanguage() {
        String savedLang = prefs.get("language", "zh_CN");
        currentLocale = Language.fromCode(savedLang).equals(Language.EN_US)
                ? Locale.US : Locale.SIMPLIFIED_CHINESE;
    }

    private static void saveLanguagePreference(Language language) {
        prefs.put("language", language.getCode());
    }
}