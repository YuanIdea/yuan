package com.gly.platform.view;

import com.gly.event.*;
import com.gly.i18n.I18n;
import com.gly.platform.app.Platform;
import com.gly.platform.app.YuanConfig;
import com.gly.platform.editor.Editor;
import com.gly.run.Run;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Toolbar Manager Class.
 */
public class ToolManager {
    private final Platform platform;
    private JButton btnRun;
    private JButton btnStop;

    public ToolManager(Platform platform) {
        this.platform = platform;
        GlobalBus.register(this);
        btnRun = null;
        btnStop = null;
    }

    public void init() {
        JToolBar toolBar = new JToolBar();

        // Add a new file.
        URL urlAdd = getClass().getResource("/tools/add.png");
        if (urlAdd != null) {
            ImageIcon newFile = new ImageIcon(urlAdd);
            JButton btnNew = new JButton(newFile);
            btnNew.setToolTipText(I18n.get("menuItem.newFile"));
            btnNew.addActionListener(e -> platform.netFile());
            toolBar.add(btnNew);
        }

        // Open the selected directory.
        URL urlOpen = getClass().getResource("/tools/open.png");
        if (urlOpen != null) {
            ImageIcon open = new ImageIcon(urlOpen);
            JButton btnOpen = new JButton(open);
            btnOpen.setToolTipText(I18n.get("menuItem.openFolder"));
            btnOpen.addActionListener(e -> new Dialog().Open(platform, platform.getRoot()));
            toolBar.add(btnOpen);
        }

        // Save the currently opened single file.
        URL urlSave = getClass().getResource("/tools/save.png");
        if (urlSave != null) {
            ImageIcon save = new ImageIcon(urlSave);
            JButton btnSave = new JButton(save);
            btnSave.setToolTipText(I18n.get("menuItem.saveFile") + " (Ctrl + S)");
            btnSave.addActionListener(e -> platform.save());
            toolBar.add(btnSave);
        }

        // Save all currently opened files.
        URL urlSaveAll = getClass().getResource("/tools/save_all.png");
        if (urlSaveAll != null) {
            ImageIcon saveAll = new ImageIcon(urlSaveAll);
            JButton btnSaveAll = new JButton(saveAll);
            btnSaveAll.setToolTipText(I18n.get("menuItem.saveAll") + " (Ctrl + Shift + S)");
            btnSaveAll.addActionListener(e -> platform.saveAll());
            toolBar.add(btnSaveAll);
        }

        // Save the currently opened file as a different file.
        URL urlSaveAs = getClass().getResource("/tools/save_as.png");
        if (urlSaveAs != null) {
            ImageIcon saveAs = new ImageIcon(urlSaveAs);
            JButton btnSaveAS = new JButton(saveAs);
            btnSaveAS.setToolTipText(I18n.get("menuItem.saveAs"));
            btnSaveAS.addActionListener(e -> new Dialog().Save(platform, platform.getRoot()));
            toolBar.add(btnSaveAS);
            toolBar.addSeparator(); // Add a separator to the menu bar.
        }

        // Undo the most recent operation.
        URL urlUndo = getClass().getResource("/tools/undo.png");
        if (urlUndo != null) {
            ImageIcon undo = new ImageIcon(urlUndo);
            JButton btnUndo = new JButton(undo);
            btnUndo.setToolTipText(I18n.get("menuItem.undo") + " (Ctrl + Z)");
            btnUndo.addActionListener(e -> {
                Editor editor = (Editor) platform.getOpenPage();
                if (editor != null) {
                    editor.undo();
                }
            });
            toolBar.add(btnUndo);
        }

        // Redo the most recent operation.
        URL urlRedo = getClass().getResource("/tools/redo.png");
        if (urlRedo != null) {
            ImageIcon redo = new ImageIcon(urlRedo);
            JButton btnRedo = new JButton(redo);
            btnRedo.setToolTipText(I18n.get("menuItem.redo") + " (Ctrl + Y)");
            btnRedo.addActionListener(e -> {
                Editor editor = (Editor) platform.getOpenPage();
                if (editor != null) {
                    editor.redo();
                }
            });
            toolBar.add(btnRedo);
            toolBar.addSeparator(); // Add a separator to the menu bar.
        }

        // Run the current content.
        URL urlRun = getClass().getResource("/tools/run.png");
        if (urlRun != null) {
            ImageIcon run = new ImageIcon(urlRun);
            btnRun = new JButton(run);
            btnRun.setToolTipText(I18n.get("menu.run"));
            btnRun.setEnabled(false);
            btnRun.addActionListener(e -> Run.run(platform));
            toolBar.add(btnRun);
        }

        // Terminate the currently running content.
        URL urlStop = getClass().getResource("/tools/stop.png");
        if (urlStop != null) {
            ImageIcon stop = new ImageIcon(urlStop);
            btnStop = new JButton(stop);
            btnStop.setToolTipText(I18n.get("menuItem.stop"));
            btnStop.addActionListener(e -> {
                btnStop.setEnabled(false);
                platform.stopCurSolver();
            });
            btnStop.setEnabled(false);
            toolBar.add(btnStop);
            platform.add(toolBar, BorderLayout.PAGE_START);
        }
    }

    @Subscribe
    public void handleRefreshProject(RefreshEvent event) {
        Path root = Paths.get(platform.getRoot());
        Path projectPathName = root.resolve(YuanConfig.PROJECT_CONFIG);
        if (Files.exists(projectPathName)) {
            btnRun.setEnabled(true);
            btnStop.setEnabled(false);
        } else {
            btnRun.setEnabled(false);
            btnStop.setEnabled(false);
        }
    }

    @Subscribe
    public void handleStart(StartEvent event) {
        btnStop.setEnabled(true);
    }

    @Subscribe
    public void handleDone(DoneEvent event) {
        btnStop.setEnabled(false);
    }
}
