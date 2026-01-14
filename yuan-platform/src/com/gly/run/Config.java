package com.gly.run;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import com.gly.log.Logger;
import com.gly.platform.app.YuanConfig;
import com.gly.util.PathUtil;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Project configuration class.
 */
public class Config {
    /**
     * Read the XML configuration file.
     *
     * @param root Root directory.
     * @return XML file content.
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
            FileInputStream config = new FileInputStream(projectPathName);
            InputStream in = new BufferedInputStream(config);
            XElement xElement = XIO.readUTF(in);
            config.close();
            return xElement;
        } catch (Exception e) {
            Logger.error("Configuration file read error:" + projectPathName + ", " + e.getMessage());
        }
        return null;
    }

    /**
     * Get the JDK path used in the current project configuration;
     * use the default configuration when no configuration is set.
     *
     * @param root Root directory.
     * @return JDK path used in the current project configuration.
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
                Logger.error("JDK configuration parsing error:" + e.getMessage());
            }
        }
        return YuanConfig.DEFAULT_JAVA_HOME;
    }

    public static Path getProjectPythonHome(String root) {
        XElement element = readElement(root);
        if (element != null) {
            try {
                XElement python = element.getElement("pythonHome"); // 工程类型。
                if (python != null) {
                    return Paths.get(python.getValue());
                }
            } catch (Exception e) {
                Logger.error("Python configuration parsing error:" + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Write to the Maven configuration file.
     *
     * @param projectXml Write the path name of the XML file.
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
            System.err.println("Write failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
