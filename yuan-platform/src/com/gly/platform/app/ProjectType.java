package com.gly.platform.app;

import bibliothek.util.xml.XElement;
import com.gly.log.Logger;
import com.gly.run.Config;

/**
 * Types of projects run on the platform.
 */
public class ProjectType {
    // Type of the current project.
    private static String currentType = "";

    // Java maven project.
    private final static String MAVEN = "maven";

    // Python project
    private final static String PYTHON = "python";

    // Standard model project.
    private final static String MODEL = "model";

    /**
     * Determine if the current project is a maven project.
     *
     * @return if the current project is a maven project.
     */
    public static boolean isMaven() {
        return currentType.equals(MAVEN);
    }

    /**
     * Determine if the current project is a python project.
     *
     * @return if the current project is a python project.
     */
    public static boolean isPython() {
        return currentType.equals(PYTHON);
    }

    /**
     * Determine if the current project is a standard model project.
     *
     * @return if the current project is a standard model.
     */
    public static boolean isModel() {
        return currentType.equals(MODEL);
    }

    /**
     * Read the current project type.
     *
     * @param root Root directory name.
     */
    public static void readProjectType(String root) {
        XElement element = Config.readElement(root);
        if (element != null) {
            currentType = element.getElement("type").getValue(); // 工程类型。
        } else {
            Logger.warn("No valid project file was found in the root directory, " +
                    "so compilation, execution, and other functions cannot be used.");
        }
    }
}
