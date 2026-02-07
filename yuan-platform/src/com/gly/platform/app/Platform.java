package com.gly.platform.app;

import bibliothek.gui.dock.common.CControl;
import com.gly.event.Event;
import com.gly.event.page.AddEvent;
import com.gly.i18n.I18n;
import com.gly.i18n.Language;
import com.gly.log.Level;
import com.gly.log.Logger;
import com.gly.event.*;
import com.gly.io.xml.WriteLayout;
import com.gly.platform.thread.AlgorithmExecutor;
import com.gly.platform.editor.Drop;
import com.gly.event.page.PageInfo;
import com.gly.platform.regin.ViewManager;
import com.gly.platform.regin.work.PageDockable;
import com.gly.run.PluginManager;
import com.gly.util.Resources;
import com.gly.platform.view.*;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;


public class Platform extends JFrame {
    private static Platform instance;
    /**
     * the manager managing all the elements of the view
     */
    private ViewManager view;

    private String root = "";

    /**
     * 安全模式
     */
    private boolean secure;

    private CControl control;

    private MenuManager menu;

    private PluginManager pluginManager;

    private String currentProjectRoot;

    /**
     * 构造平台。
     */
    private Platform() {
        I18n.switchLanguage(Language.EN_US);
        //I18n.switchLanguage(Language.ZH_CN);
        this.setTitle(I18n.get("app.title"));
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setIconImage(Resources.toImage(Resources.getIcon("aif")));

        System.setProperty("file.encoding", "UTF-8");
        GlobalBus.register(this); // 注册到事件总线
        Logger.createLogger(Level.DEBUG, YuanConfig.YUAN_PATH.resolve("data/Yuan.log"));
    }

    public static Platform getInstance() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    void startup(String[] args, boolean secure) {
        this.secure = secure;
        control = new CControl();
        getContentPane().add(control.getContentArea());
        view = new ViewManager(control, secure, root);
        menu = new MenuManager(this);
        menu.generateMenu(control);
        ToolManager tool = new ToolManager(this);
        tool.init();

        StatusBar bar = new StatusBar(this);
        bar.init();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });

        if (args.length > 0) {
            for (String f : args) {
                File fileToOpen = new File(f);
                if (fileToOpen.exists() && fileToOpen.isFile()) {
                    PageInfo pageInfo = new PageInfo(fileToOpen);
                    GlobalBus.dispatch(new AddEvent(pageInfo));
                }
            }
        }
        Drop.setupDragAndDrop(this);
        setVisible(true);
        pluginManager = PluginManager.getInstance("com.gly");
    }

    /**
     * 窗口关闭处理。
     */
    public void handleWindowClosing() {
        try {
            if (!secure) {
                WriteLayout.write(control);
            }
            GlobalBus.unregister(this);
            control.destroy();
            Platform.super.dispose();
            System.exit(0); // 这里判断是否需要系统退出
        } catch (Exception ex) {
            Logger.error("关闭窗口错误：" + ex.getMessage());
        }
    }

    /**
     * 通用总线数据处理。
     *
     * @param event 事件。
     */
    @Subscribe
    public void handleEvent(Event event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()) {
                case saveFile: // 保存文件。
                    saveAs((String) event.getData());
                    break;
                case openFold: // 打开方案。
                    open((String) event.getData());
                    break;
            }
        });
    }

    /**
     * 另存为。
     *
     * @param savePath 保存路径。
     */
    private void saveAs(String savePath) {
        PageDockable page = getOpenPage();
        page.save(savePath);
        refreshFiles(root);
    }

    public void netFile() {
        view.getTreeDockable().newFile("新建文件");
    }

    /**
     * 保存。
     */
    public void save() {
        PageDockable page = getOpenPage();
        if (page == null) {
            Logger.info("没有激活页面被选中");
        } else {
            page.save(page.getPageInfo().getName());
        }
    }

    /**
     * 保存所有页面。
     */
    public void saveAll() {
        view.saveAll();
    }

    /**
     * 保存所有修改过的页面。
     */
    public void saveAllModified() {
        view.saveAllModified();
    }

    /**
     * 刷新工程管理目录。
     *
     * @param newRoot 新根目录。
     */
    private void refreshFiles(String newRoot) {
        view.getTreeDockable().refreshRoot(newRoot);
        view.getTreeDockable().setVisible(true);
    }

    public PageDockable getFocusedPage() {
        return view.getFocusedPage();
    }

    public PageDockable getOpenPage() {
        return view.getOpenPage();
    }

    /**
     * 终止当前排产任务。
     */
    public void stopCurSolver() {
        AlgorithmExecutor executor = AlgorithmExecutor.getInstance();
        executor.stopCurSolver();
    }

    /**
     * 打开新目录。
     *
     * @param newRoot 新目录根目录。
     */
    private void open(String newRoot) {
        root = newRoot;
        refreshFiles(root);
        menu.refresh(); // 刷新菜单栏
    }

    /**
     * 获得根目录。
     *
     * @return 当前根目录。
     */
    public String getRoot() {
        return root;
    }

    public ViewManager getView() {
        return view;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public String getCurrentProjectRoot() {
        if (currentProjectRoot != null && !currentProjectRoot.isEmpty()) {
            return currentProjectRoot;
        } else {
            return root;
        }
    }

    public void setCurrentProjectRoot(String currentProjectRoot) {
        this.currentProjectRoot = currentProjectRoot;
    }
}
