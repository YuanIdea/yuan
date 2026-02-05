package com.gly.run;

import com.gly.event.page.PageInfo;
import com.gly.io.json.Json;
import com.gly.log.Logger;
import com.gly.platform.editor.Editor;
import com.gly.model.ExecutableUnit;
import com.gly.platform.regin.work.PageDockable;
import com.gly.platform.thread.AlgorithmExecutor;
import com.gly.platform.app.Platform;
import com.gly.platform.app.ProjectType;
import com.gly.python.PythonRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;


/**
 * Class for running various types of projects.
 */
public class Run {

    /**
     * Run the program.
     *
     * @param platform Platform's main program.
     */
    public static void run(Platform platform) {
        save(platform);
        AlgorithmExecutor executor = AlgorithmExecutor.getInstance();
        executor.stopCurSolver();
        ExecutableUnit execute = null;
        String root;
        String pathName = "";
        if (ProjectType.isMaven()) {
            root = platform.getView().getPackDockable().getCurrentProjectRoot();
            execute = new ProcessExecutor();
        } else if (ProjectType.isPython()) {
            root = platform.getRoot();
            Path pythonHome = Config.getProjectPythonHome(root);
            if (pythonHome != null) {
                pathName = getPathName(platform);
                if (pathName != null) {
                    String fileName = pathName.toLowerCase();
                    boolean canRuan = fileName.endsWith(".py") || fileName.endsWith(".pyc");
                    if (canRuan) {
                        execute = new PythonRunner(pythonHome.toString(), StandardCharsets.UTF_8);
                    } else {
                        System.err.println("Not an executable python file: " + fileName);
                    }
                }
            } else {
                Logger.error("Python home path not found.");
            }
        } else {
            root = platform.getRoot();
            if (ProjectType.isModel()) {
                PluginManager pluginManager = platform.getPluginManager();
                if (pluginManager != null) {
                    pathName = getPathName(platform);
                    execute = getExecute(pluginManager, pathName);
                }
            }
        }
        if (execute != null) {
            execute.init(root, pathName, platform);
            executor.startNewSolver(execute);
        }
    }

    private static ExecutableUnit getExecute(PluginManager pluginManager, String pathName) {
        Json json = new Json(pathName);
        if (json.getRootNode() != null && json.has("actionType")) {
            String actionType = json.getString("actionType");
            Map<String, ExecutableUnit> executableUnits = pluginManager.getExecutableUnits();
            for (String id : executableUnits.keySet()) {
                if (id.toLowerCase().contains(actionType.toLowerCase())) {
                    return executableUnits.get(id);
                }
            }
        }
        return null;
    }


    /**
     * Save content to disk.
     *
     * @param platform Platform's main program.
     */
    private static void save(Platform platform) {
        if (ProjectType.isMaven()) {
            platform.saveAllModified();
        } else {
            PageDockable page = platform.getOpenPage();
            if (page != null) {
                if (page instanceof Editor) {
                    Editor editor = (Editor) page;
                    editor.saveModified();
                } else {
                    page.save(page.getPageInfo().getName());
                }
            }
        }
    }

    private static String getPathName(Platform platform) {
        PageDockable page = platform.getOpenPage();
        if (page == null) {
            Logger.info("No active page is selected.");
            return null;
        }
        PageInfo pageInfo = page.getPageInfo();
        if (pageInfo == null) {
            Logger.warn("Page information error.");
            return null;
        }

        return pageInfo.getName();
    }
}
