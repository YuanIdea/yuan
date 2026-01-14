package com.gly.platform.view;

import com.gly.event.*;
import com.gly.platform.app.Platform;
import com.gly.platform.app.YuanConfig;
import com.gly.platform.editor.Editor;
import com.gly.run.Run;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Toolbar Manager Class.
 */
public class ToolManager {
    private Platform platform;
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
        ImageIcon newFile = new ImageIcon(getClass().getResource("/tools/add.png"));
        JButton btnNew = new JButton(newFile);
        btnNew.setToolTipText("新建文件");
        btnNew.addActionListener(e -> platform.netFile());
        toolBar.add(btnNew);

        // Open the selected directory.
        ImageIcon open = new ImageIcon(getClass().getResource("/tools/open.png"));
        JButton btnOpen = new JButton(open);
        btnOpen.setToolTipText("打开目录");
        btnOpen.addActionListener(e -> new Dialog().Open(platform, platform.getRoot()));
        toolBar.add(btnOpen);

        // Save the currently opened single file.
        ImageIcon save = new ImageIcon(getClass().getResource("/tools/save.png"));
        JButton btnSave = new JButton(save);
        btnSave.setToolTipText("保存 (Ctrl + S)");
        btnSave.addActionListener(e -> platform.save());
        toolBar.add(btnSave);

        // Save all currently opened files.
        ImageIcon saveAll = new ImageIcon(getClass().getResource("/tools/save_all.png"));
        JButton btnSaveAll = new JButton(saveAll);
        btnSaveAll.setToolTipText("保存全部 (Ctrl + Shift + S)");
        btnSaveAll.addActionListener(e -> platform.saveAll());
        toolBar.add(btnSaveAll);

        // Save the currently opened file as a different file.
        ImageIcon saveAs = new ImageIcon(getClass().getResource("/tools/save_as.png"));
        JButton btnSaveAS = new JButton(saveAs);
        btnSaveAS.setToolTipText("另存为");
        btnSaveAS.addActionListener(e -> new Dialog().Save(platform, platform.getRoot()));
        toolBar.add(btnSaveAS);
        toolBar.addSeparator(); // Add a separator to the menu bar.

        // Undo the most recent operation.
        ImageIcon undo = new ImageIcon(getClass().getResource("/tools/undo.png"));
        JButton btnUndo = new JButton(undo);
        btnUndo.setToolTipText("撤销 (Ctrl + Z)");
        btnUndo.addActionListener(e -> {
            Editor editor = (Editor) platform.getOpenPage();
            if (editor != null) {
                editor.undo();
            }
        });
        toolBar.add(btnUndo);

        // Redo the most recent operation.
        ImageIcon redo = new ImageIcon(getClass().getResource("/tools/redo.png"));
        JButton btnRedo = new JButton(redo);
        btnRedo.setToolTipText("重做 (Ctrl + Y)");
        btnRedo.addActionListener(e -> {
            Editor editor = (Editor) platform.getOpenPage();
            if (editor != null) {
                editor.redo();
            }
        });
        toolBar.add(btnRedo);
        toolBar.addSeparator(); // Add a separator to the menu bar.

        // Run the current content.
        ImageIcon run = new ImageIcon(getClass().getResource("/tools/run.png"));
        btnRun = new JButton(run);
        btnRun.setToolTipText("运行");
        btnRun.setEnabled(false);
        btnRun.addActionListener(e -> Run.run(platform));
        toolBar.add(btnRun);

        // Terminate the currently running content.
        ImageIcon stop = new ImageIcon(getClass().getResource("/tools/stop.png"));
        btnStop = new JButton(stop);
        btnStop.setToolTipText("终止运行");
        btnStop.addActionListener(e -> {
            btnStop.setEnabled(false);
            platform.stopCurSolver();
        });
        btnStop.setEnabled(false);
        toolBar.add(btnStop);
        platform.add(toolBar, BorderLayout.PAGE_START);
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
