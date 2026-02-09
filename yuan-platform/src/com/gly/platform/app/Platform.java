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
import com.gly.os.OSUtils;
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

/**
 * yuan-platform frame.
 */
public class Platform extends JFrame {
    private static Platform instance;
    /**
     * the manager managing all the elements of the view
     */
    private ViewManager view;

    private String root = "";

    private boolean secure;

    private CControl control;

    private MenuManager menu;

    private PluginManager pluginManager;


    /**
     * Construct platform.
     */
    private Platform() {
        if (OSUtils.isChineseOS()) {
            I18n.switchLanguage(Language.ZH_CN);
        } else {
            I18n.switchLanguage(Language.EN_US);
        }
        this.setTitle(I18n.get("app.title"));
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setIconImage(Resources.toImage(Resources.getIcon("aif")));

        System.setProperty("file.encoding", "UTF-8");
        GlobalBus.register(this);
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

        for (String f : args) {
            File fileToOpen = new File(f);
            if (fileToOpen.exists() && fileToOpen.isFile()) {
                PageInfo pageInfo = new PageInfo(fileToOpen);
                GlobalBus.dispatch(new AddEvent(pageInfo));
            }
        }
        Drop.setupDragAndDrop(this);
        setVisible(true);
        pluginManager = PluginManager.getInstance("com.gly");
    }

    /**
     * Window close handling.
     */
    public void handleWindowClosing() {
        try {
            if (!secure) {
                WriteLayout.write(control);
            }
            GlobalBus.unregister(this);
            control.destroy();
            Platform.super.dispose();
            System.exit(0);
        } catch (Exception ex) {
            Logger.error("Window close error: " + ex.getMessage());
        }
    }

    /**
     * Universal bus data processing.
     *
     * @param event Event
     */
    @Subscribe
    public void handleEvent(Event event) {
        SwingUtilities.invokeLater(() -> {
            switch (event.getType()) {
                case saveFile: // Save file.
                    saveAs((String) event.getData());
                    break;
                case openFold: // Open solution.
                    open((String) event.getData());
                    break;
            }
        });
    }

    /**
     * Save As.
     *
     * @param savePath Save path.
     */
    private void saveAs(String savePath) {
        PageDockable page = getOpenPage();
        page.save(savePath);
        refreshFiles(root);
    }

    public void netFile() {
        view.getTreeDockable().newFile(I18n.get("menuItem.newFile"));
    }

    /**
     * 保存。
     */
    public void save() {
        PageDockable page = getOpenPage();
        if (page == null) {
            Logger.info("No active page is selected.");
        } else {
            page.save(page.getPageInfo().getName());
        }
    }

    /**
     * Save all pages.
     */
    public void saveAll() {
        view.saveAll();
    }

    /**
     * Save all modified pages.
     */
    public void saveAllModified() {
        view.saveAllModified();
    }

    /**
     * Refresh project management directory.
     *
     * @param newRoot New root directory.
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
     * Terminate the current scheduling task.
     */
    public void stopCurSolver() {
        AlgorithmExecutor executor = AlgorithmExecutor.getInstance();
        executor.stopCurSolver();
    }

    /**
     * Open a new directory.
     *
     * @param newRoot New directory root.
     */
    private void open(String newRoot) {
        root = newRoot;
        refreshFiles(root);
        menu.refresh(); // 刷新菜单栏
    }

    /**
     * Get the root directory.
     *
     * @return Current root directory.
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
}
