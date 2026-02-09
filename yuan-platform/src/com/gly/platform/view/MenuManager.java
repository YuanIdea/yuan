package com.gly.platform.view;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.menu.*;
import bibliothek.gui.dock.facile.menu.RootMenuPiece;
import bibliothek.gui.dock.facile.menu.SubmenuPiece;
import com.gly.i18n.I18n;
import com.gly.io.OpenFile;
import com.gly.log.Logger;
import com.gly.platform.app.ProjectType;
import com.gly.platform.app.Platform;
import com.gly.platform.app.YuanConfig;
import com.gly.platform.editor.Editor;
import com.gly.platform.editor.FindReplaceDialog;
import com.gly.run.Run;
import com.gly.platform.regin.auxiliary.maven.Executor;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;

/**
 * Menu Manager.
 */
public class MenuManager {
    // Platform's main program.
    private final Platform platform;

    // control
    private CControl control;

    // main menu bar.
    private final JMenuBar menuBar;

    // run menu.
    private JMenu menuRun;

    // undo operator.
    private JMenuItem undo;

    // redo operator.
    private JMenuItem redo;

    // select all
    private JMenuItem selectAllMenuItem;

    // find and replace
    private JMenuItem findAndReplace;

    public MenuManager(Platform platform) {
        this.platform = platform;
        menuBar = new JMenuBar();
        this.platform.setJMenuBar(menuBar);
    }

    /**
     * Generate all menu bars.
     *
     * @param control Current control.
     */
    public void generateMenu(CControl control) {
        this.control = control;

        generateFile();     // Generate the file menu bar.
        generateEdit();     // Generate the edit menu bar.
        generateRun();      // Generate the run menu bar.
        generateView();     // Generate the theme menu bar.
        generateHelp();     // Generate the help menu bar.
    }

    /**
     * Generate the file menu bar.
     */
    private void generateFile() {
        JMenu menuFile = new JMenu(I18n.get("menu.file") + "(F)");
        menuFile.setMnemonic('F');
        menuBar.add(menuFile);
        JMenuItem newFileBtn = new JMenuItem(I18n.get("menuItem.newFile"));

        newFileBtn.addActionListener(e -> platform.netFile());
        menuFile.add(newFileBtn);

        JMenuItem open = new JMenuItem(I18n.get("menuItem.openFolder"));
        menuFile.add(open);
        open.addActionListener(e -> new Dialog().Open(platform, platform.getRoot()));

        JMenuItem save = new JMenuItem(I18n.get("menuItem.saveFile"));
        menuFile.add(save);
        save.addActionListener(e -> platform.save());

        JMenuItem saveAll = new JMenuItem(I18n.get("menuItem.saveAll"));
        menuFile.add(saveAll);
        saveAll.addActionListener(e -> platform.saveAll());

        JMenuItem saveOther = new JMenuItem(I18n.get("menuItem.saveAs"));
        menuFile.add(saveOther);
        saveOther.addActionListener(e -> new Dialog().Save(platform, platform.getRoot()));

        JMenuItem exit = new JMenuItem(I18n.get("menuItem.exit"));
        menuFile.add(exit);
        exit.addActionListener(e -> platform.handleWindowClosing());
    }

    /**
     * Generate the edit menu bar.
     */
    private void generateEdit() {
        JMenu editMenu = new JMenu(I18n.get("menu.edit") + "(E)");
        editMenu.setMnemonic('E');
        menuBar.add(editMenu);

        undo = new JMenuItem(I18n.get("menuItem.undo"));
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        editMenu.add(undo);
        undo.addActionListener(e -> {
            Editor editor = (Editor) platform.getOpenPage();
            if (editor != null) {
                editor.undo();
            }
        });

        redo = new JMenuItem(I18n.get("menuItem.redo"));
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        editMenu.add(redo);
        redo.addActionListener(e -> {
            Editor editor = (Editor) platform.getOpenPage();
            if (editor != null) {
                editor.redo();
            }
        });

        selectAllMenuItem = new JMenuItem(I18n.get("menuItem.selectAll"));
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        selectAllMenuItem.addActionListener(e -> {
            Editor editor = (Editor) platform.getOpenPage();
            if (editor != null) {
                RSyntaxTextArea textArea = editor.getTextArea();
                textArea.selectAll();
                textArea.requestFocusInWindow();
            }
        });
        editMenu.add(selectAllMenuItem);

        findAndReplace = new JMenuItem(I18n.get("menuItem.findAndReplace"));
        findAndReplace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
        findAndReplace.addActionListener(e -> {
            Editor editor = (Editor) platform.getFocusedPage();
            if (editor != null) {
                FindReplaceDialog.showDialog(platform, editor.getTextArea());
            }
        });
        editMenu.add(findAndReplace);

        // Add a listener for the menu expansion event.
        editMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                updateEditItemState(); // Edit menu expanded
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                // Triggered when the menu closes.
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                // Triggered when the menu is canceled.
            }
        });
    }

    /**
     * Generate the run menu bar.
     */
    private void generateRun() {
        menuRun = new JMenu(I18n.get("menu.run") + "(R)");
        menuRun.setEnabled(false);
        menuRun.setMnemonic('R');
        menuBar.add(menuRun);

        JMenuItem run = new JMenuItem(I18n.get("menuItem.run"));
        menuRun.add(run);
        run.addActionListener(e -> Run.run(platform));

        JMenuItem stop = new JMenuItem(I18n.get("menuItem.stop"));
        menuRun.add(stop);
        stop.addActionListener(e -> platform.stopCurSolver());
    }

    /**
     * Generate the theme view menu bar.
     */
    private void generateView() {
        RootMenuPiece settings = new RootMenuPiece(I18n.get("menu.view") + "(V)", false);
        settings.getMenu().setMnemonic('V');
        settings.add(new SingleCDockableListMenuPiece(control));
        menuBar.add(settings.getMenu());

        RootMenuPiece layout = new RootMenuPiece(I18n.get("menu.theme") + "(L)", false);
        layout.getMenu().setMnemonic('L');
        layout.add(new SubmenuPiece(I18n.get("menuItem.appearance"), true, new CLookAndFeelMenuPiece(control)));
        layout.add(new SubmenuPiece(I18n.get("menuItem.style"), true, new CThemeMenuPiece(control)));
        menuBar.add(layout.getMenu());
    }

    /**
     * Generate the help menu bar.
     */
    private void generateHelp() {
        JMenu helpModel = new JMenu(I18n.get("menu.help") + "(H)");
        helpModel.setMnemonic('H');
        menuBar.add(helpModel);

        JMenuItem helpDocFileBtn = new JMenuItem(I18n.get("menuItem.help"));
        helpDocFileBtn.setMnemonic('V');
        helpDocFileBtn.addActionListener(e -> {
            File pdfFile = YuanConfig.YUAN_PATH.resolve("data/Yuan.pdf").toFile();
            if (pdfFile.exists()) {
                OpenFile.open(pdfFile);
            } else {
                Logger.error("Help documentation not found:" + pdfFile);
            }
        });
        helpModel.add(helpDocFileBtn);

        JMenuItem regFileBtn = new JMenuItem(I18n.get("menuItem.register"));
        helpDocFileBtn.setMnemonic('D');
        regFileBtn.addActionListener(e -> new Registration(platform));
        helpModel.add(regFileBtn);

        JMenuItem installCoreBtn = new JMenuItem(I18n.get("menuItem.installCommon"));
        installCoreBtn.setMnemonic('I');
        installCoreBtn.addActionListener(e -> installCore());
        helpModel.add(installCoreBtn);

        JMenuItem aboutFileBtn = new JMenuItem(I18n.get("menuItem.about"));
        aboutFileBtn.addActionListener(e -> AboutDialog.showAboutDialog(platform));
        helpModel.add(aboutFileBtn);
    }

    /**
     * Install yuan-common to the repository.
     */
    private void installCore() {
        Path directory = YuanConfig.YUAN_PATH;
        String jarName = "yuan-common-1.0.7.jar";
        Path coreFile = directory.resolve(jarName);
        boolean installedSuccess = Executor.executeMaven(directory, YuanConfig.DEFAULT_JAVA_HOME,
                "install:install-file", "-Dfile=" + coreFile);
        if (installedSuccess) {
            Logger.info(jarName + " installed successfully.");
        } else {
            Logger.error(jarName + " installation failed.");
        }
    }

    /**
     * Update the state of the edit submenu items.
     */
    private void updateEditItemState() {
        Editor editor = (Editor) platform.getOpenPage(); //Currently open document windows.
        if (editor != null) {
            undo.setEnabled(editor.canUndo());
            redo.setEnabled(editor.canRedo());
            selectAllMenuItem.setEnabled(true);
            findAndReplace.setEnabled(true);
        } else {
            undo.setEnabled(false);
            redo.setEnabled(false);
            selectAllMenuItem.setEnabled(false);
            findAndReplace.setEnabled(false);
        }
    }

    /**
     * Refresh run menu.
     */
    public void refresh() {
        boolean enable = ProjectType.isModel() || ProjectType.isMaven() || ProjectType.isPython();
        menuRun.setEnabled(enable);
    }
}

