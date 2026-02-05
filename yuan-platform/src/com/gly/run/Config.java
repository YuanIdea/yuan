package com.gly.run;

import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import com.gly.log.Logger;
import com.gly.platform.app.YuanConfig;
import com.gly.python.PythonHomeFinder;
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
     * @param root Root directory of the current project.
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
     * Get the JDK path used in the current project configuration.
     * use the default configuration when no configuration is set.
     *
     * @param root Root directory of the current project.
     * @return JDK path used in the current project configuration.
     */
    public static Path getProjectJavaHome(String root) {
        Path javaHome = getPath(root, "jdk");
        if (javaHome == null) {
            return YuanConfig.DEFAULT_JAVA_HOME;
        } else {
            return javaHome;
        }
    }

    /**
     * Get the python path used in the current project configuration.
     *
     * @param root Root directory of the current project.
     * @return Path used in the current project configuration.
     */
    public static Path getProjectPythonHome(String root) {
        Path pythonHome = getPath(root, "pythonHome");
        if (pythonHome == null) {
            String strPath = PythonHomeFinder.findPythonHome();
            if (strPath != null) {
                return Paths.get(strPath);
            } else {
                return null;
            }
        } else {
            return pythonHome;
        }
    }

    /**
     * Parse the path based on the node.
     *
     * @param root Root directory of the current project.
     * @param node A node in XML that configures a path.
     * @return Path corresponding to the node.
     */
    private static Path getPath(String root, String node) {
        XElement element = readElement(root);
        if (element != null) {
            try {
                XElement python = element.getElement(node);
                if (python != null) {
                    return Paths.get(python.getValue());
                }
            } catch (Exception e) {
                Logger.error("Path parsing error:" + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Write to the project configuration file.
     *
     * @param projectXml  Write the path name of the XML file.
     * @param projectType Project type.
     */
    public static void writeProjectXml(File projectXml, String projectType) {
        try {
            XElement project = new XElement("project");
            project.addElement("type").setValue(projectType);
            //Saving XElement to a file will overwrite any existing file.
            FileOutputStream config = new FileOutputStream(projectXml); // the project configuration file
            OutputStream out = new BufferedOutputStream(config);
            XIO.writeUTF(project, out);
        } catch (IOException e) {
            System.err.println("Write failed: " + e.getMessage());
        }
    }
}
