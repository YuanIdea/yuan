package com.gly.platform.app;

import bibliothek.util.xml.XElement;
import com.gly.log.Logger;
import com.gly.run.Config;

/**
 * 工程类型类
 */
public class ProjectType {
    // 工程类型
    private static String currentType = "";

    // maven程序
    private final static String MAVEN = "maven";

    // 模型
    private final static String MODEL = "model";

    /**
     * 当前工程类型是否为Maven程序。
     * @return 是否为Maven程序。
     */
    public static boolean isMaven() {
        return currentType.equals(MAVEN);
    }

    /**
     * 当前工程是否为模型。
     * @return 是否为模型。
     */
    public static boolean isModel() {
        return currentType.equals(MODEL);
    }

    /**
     * 读取当前工程类型。
     * @param root 根目录名
     */
    public static void readProjectType(String root) {
        XElement element = Config.readElement(root);
        if (element != null) {
            currentType = element.getElement("type").getValue(); // 工程类型。
        } else {
            Logger.warn("根目录中没有找到有效的工程文件，无法使用编译运行等功能。");
        }
    }
}
