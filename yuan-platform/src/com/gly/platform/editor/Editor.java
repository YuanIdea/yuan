package com.gly.platform.editor;

import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import com.gly.i18n.I18n;
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
    // Text editing area.
    private final RSyntaxTextArea textArea;

    // Encoding method used by the current text.
    private String encoding;

    // Path name where the current text is stored.
    private final Path path;

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
            Logger.info("It was detected that the file may not be encoded in " + encoding +
                    ". It is recommended to try opening it with the " + checkEncoding + " encoding.");
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
            setModified(!originalContent.equals(textArea.getText()));
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
                            textArea.getName() + I18n.get("wantSave"), I18n.get("saveFile"),
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                            new String[]{I18n.get("saveFile"), I18n.get("doNotSave"), I18n.get("cancel")},
                            I18n.get("saveFile"));

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
            // Asynchronously check whether it has returned to the original state.
            SwingUtilities.invokeLater(this::checkModifyByContent);
        } else {
            Logger.warn("Cannot perform undo operation.");
        }
    }

    /**
     * Redo the most recent operation.
     */
    public void redo() {
        if (textArea.canRedo()) {
            textArea.redoLastAction();
            // Asynchronously check whether it has returned to the original state.
            SwingUtilities.invokeLater(this::checkModifyByContent);
        } else {
            Logger.warn("Cannot perform redo operation.");
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
        JMenuItem open = new JMenuItem(I18n.get("showInExplorer"));
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
