package com.gly.run;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import com.gly.log.Logger;
import com.gly.platform.app.YuanConfig;
import com.gly.util.PathUtil;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    /**
     * 读取xml配置文件。
     * @param root 根目录。
     * @return xml文件内容。
     */
    public static XElement readElement(String root) {
        if (root.isEmpty()) {
            return null;
        }
        String projectPathName = PathUtil.resolveAbsolutePath(root, YuanConfig.PROJECT_CONFIG);
        if (!PathUtil.fileExists(projectPathName)) {
            return null;
        }
        try {
            FileInputStream config = new FileInputStream(projectPathName); // 工程配置文件。
            InputStream in = new BufferedInputStream(config);
            XElement xElement = XIO.readUTF(in);
            config.close();
            return xElement;
        } catch (Exception e) {
            Logger.error("配置文件读取错误:" + projectPathName + ", " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取当前工程配置使用的jdk路径，没有配置时使用默认配置。
     * @param root 工程根目录。
     * @return 当前工程配置使用的jdk路径。
     */
    public static Path getProjectJavaHome(String root) {
        XElement element = readElement(root);
        if (element != null) {
            try {
                XElement jdk = element.getElement("jdk"); // 工程类型。
                if (jdk != null) {
                    return Paths.get(jdk.getValue());
                }
            } catch (Exception e) {
                Logger.error("jdk配置解析错误:" + e.getMessage());
            }
        }
        return YuanConfig.DEFAULT_JAVA_HOME;
    }

    /**
     * 写入maven配置文件。
     * @param projectXml 写入路径。
     */
    public static void writeMavenXml(File projectXml) {
        try {
            XElement project = new XElement("project");
            project.addElement("type").setValue("maven");
            // 将 XElement 保存到文件（会覆盖已有文件）
            FileOutputStream config = new FileOutputStream(projectXml); // 工程配置文件。
            OutputStream out = new BufferedOutputStream(config);
            XIO.writeUTF(project, out);
        } catch (IOException e) {
            System.err.println("写入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
