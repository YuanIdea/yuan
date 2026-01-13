package com.gly.platform.editor;

import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import com.gly.log.Logger;
import com.gly.event.page.PageInfo;
import com.gly.platform.regin.tree.find.FileExplorerUtil;
import com.gly.platform.regin.work.PageDockable;
import com.gly.platform.view.SaveAction;
import com.gly.util.Encoding;
import com.gly.util.FileUtil;
import com.gly.util.PathUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Text editor supporting code highlighting effects.
 */
public class Editor extends PageDockable {
    // 页面
    private RSyntaxTextArea textArea;

    // Encoding method used by the current text.
    private String encoding;

    // Path name where the current text is stored.
    private Path path;

    // Used to indicate whether the content has been edited.
    private boolean isModified;

    // Original unmodified document content.
    private String originalContent;

    public Editor(MultipleCDockableFactory<PageDockable, ?> factory, PageInfo pageInfo) {
        super(factory, pageInfo);
        encoding = StandardCharsets.UTF_8.toString();
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setTabSize(4);
        textArea.setName(pageInfo.getFileName());

        String syntaxStyle = getStyleKey(pageInfo.getName());
        textArea.setSyntaxEditingStyle(syntaxStyle);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setAntiAliasingEnabled(true);// Enable anti-aliasing for better results.
        textArea.setFractionalFontMetricsEnabled(true);
        addPopupMenu();
        RTextScrollPane sp = new RTextScrollPane(textArea);
        getContentPane().add(sp);
        path = Paths.get(pageInfo.getName());
        String checkEncoding = Encoding.detectCharset(path);
        new EditorShortcutManager(textArea); // Implement shortcut key functionality.

        if (!checkEncoding.equalsIgnoreCase(encoding)) {
            Logger.info("检测到可能不是" + encoding + "编码，建议尝试" + checkEncoding + "编码方式打开。");
        }

        setTextNoBack(FileUtil.loadFile(path, encoding));
        SaveAction.bindSaveShortcut(textArea, new SaveAction(this)); // Create and bind the save operation.
        Drop.setupDragAndDrop(textArea);

        textArea.getDocument().addDocumentListener(generateDocumentListener());// Add a document modification listener.
        this.addVetoClosingListener(generateCVetoClosingListener());// Page close handling.
    }

    /**
     * Get the Style Key to achieve code highlighting.
     *
     * @param pathName Path name of the file.
     * @return the Style Key.
     */
    private String getStyleKey(String pathName) {
        String type = PathUtil.getFileExtension(pathName);
        if (type.equals("py")) {
            type = "python";
        }
        return "text/" + type;
    }

    /**
     * Reload the text content.
     */
    public void reload() {
        setTextNoBack(FileUtil.loadFile(path, encoding));
    }

    /**
     * Reset the content without allowing undo.
     *
     * @param text Content to be set.
     */
    private void setTextNoBack(String text) {
        textArea.beginAtomicEdit(); // Pause undo recording.
        textArea.setText(text);
        textArea.discardAllEdits();
        textArea.endAtomicEdit();// Resume undo recording.
        this.originalContent = text;
        setModified(false);
    }

    /**
     * Save the document content.
     *
     * @param pathName Path name for saving the document.
     */
    @Override
    public void save(String pathName) {
        FileUtil.saveToFile(pathName, textArea.getText(), encoding);
        this.originalContent = textArea.getText();
        setModified(false);
    }

    /**
     * Save only when the file is modified; otherwise, no action is taken.
     */
    public void saveModified() {
        if (isModified) {
            save(getPageInfo().getName());
        }
    }

    /**
     * Generate a file listener.
     *
     * @return Generated file listener.
     */
    private DocumentListener generateDocumentListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
            }
        };
    }

    /**
     * Detect changes through content comparison.
     */
    private void checkModifyByContent() {
        if (originalContent != null) {
            if (originalContent.equals(textArea.getText())) {
                setModified(false);
            } else {
                setModified(true);
            }
        }
    }

    /**
     * Generate a handler for the file close listener.
     *
     * @return A handler for the file close listener.
     */
    private CVetoClosingListener generateCVetoClosingListener() {
        return new CVetoClosingListener() {
            public void closing(CVetoClosingEvent event) {
                if (isModified) {
                    int option = JOptionPane.showOptionDialog(textArea,
                            textArea.getName() + "文件已修改，是否保存？", "保存文件",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                            new String[]{"保存", "不保存", "取消"}, "保存");

                    if (option == JOptionPane.YES_OPTION) {
                        save(getPageInfo().getName());
                    } else if (option == JOptionPane.CANCEL_OPTION) {
                        event.cancel();
                    }
                }
            }

            public void closed(CVetoClosingEvent event) {
            }
        };
    }


    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String value) {
        this.encoding = value;
    }

    /**
     * Set the document's status to modified.
     */
    private void setModified(boolean modified) {
        isModified = modified;
        updateTitle(getPageInfo().getFileName());
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }

    public Path getPath() {
        return path;
    }

    /**
     * Determine if the operation can be undone.
     *
     * @return if the operation can be undone.
     */
    public boolean canUndo() {
        return textArea.canUndo();
    }

    /**
     * Determine if it can be redone.
     *
     * @return if it can be redone.
     */
    public boolean canRedo() {
        return textArea.canRedo();
    }

    /**
     * Undo the most recent operation.
     */
    public void undo() {
        if (textArea.canUndo()) {
            textArea.undoLastAction();
            SwingUtilities.invokeLater(this::checkModifyByContent);// 异步检查是否回到了原始状态
        } else {
            Logger.warn("无法撤销。");
        }
    }

    /**
     * Redo the most recent operation.
     */
    public void redo() {
        if (textArea.canRedo()) {
            textArea.redoLastAction();
            SwingUtilities.invokeLater(this::checkModifyByContent);// 异步检查是否回到了原始状态
        } else {
            Logger.warn("无法重做。");
        }
    }

    public void updateTitle(String text) {
        if (isModified) {
            setTitleText(text + "*");
        } else {
            setTitleText(text);
        }
    }

    /**
     * Add a custom right-click context menu bar.
     */
    private void addPopupMenu() {
        // Get the default popup menu.
        JPopupMenu popupMenu = textArea.getPopupMenu();
        // Add a separator to the menu bar.
        popupMenu.addSeparator();
        JMenuItem open = new JMenuItem("在资源管理器中显示");
        open.addActionListener(e -> {
            try {
                FileExplorerUtil.showFileInExplorer(path.toFile());
            } catch (Exception exc) {
                Logger.error(exc.getMessage());
            }
        });
        popupMenu.add(open);
    }

    public boolean isModified() {
        return isModified;
    }

    public void setText(String content) {
        if (textArea != null) {
            textArea.setText(content);
        }
    }

}
