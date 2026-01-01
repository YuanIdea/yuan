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

import java.util.Map;


/**
 * 运行类。
 */
public class Run {

    /**
     * 运行程序。
     * @param platform 平台主程。
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
        } else {
            root = platform.getRoot();
            if (ProjectType.isModel()) {
                PluginManager pluginManager = platform.getPluginManager();
                pluginManager.register("com.gly.PluginEntry");
                pathName = getPathName(platform);
                execute = getExecute(pluginManager, pathName);
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
        System.err.println("未找到对应的插件实现");
        return null;
    }


    /**
     * 保存内容到磁盘。
     * @param platform 平台主程。
     */
    private static void save(Platform platform) {
        if (ProjectType.isMaven()) {
            platform.saveAllModified();
        } else {
            PageDockable page = platform.getOpenPage();
            if (page != null) {
                if (page instanceof Editor) {
                    Editor editor = (Editor)page;
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
            Logger.info("没有激活页面被选中");
            return null;
        }
        PageInfo pageInfo = page.getPageInfo();
        if (pageInfo == null) {
            Logger.warn("页面信息错误");
            return null;
        }

        return pageInfo.getName();
    }
}
