package com.gly.platform.editor;

import com.gly.platform.app.Platform;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * 编辑器快捷键管理。
 */
class EditorShortcutManager {
    private final JFrame mainFrame;
    private final RSyntaxTextArea textArea;
    private FindReplaceDialog findDialog;

    EditorShortcutManager(RSyntaxTextArea textArea) {
        this.mainFrame = Platform.getInstance();
        this.textArea = textArea;
        setupShortcuts();
    }

    private void setupShortcuts() {
        // 1. 确保编辑器可以接收快捷键
        textArea.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK),
                "openFindDialog"
        );

        textArea.getActionMap().put("openFindDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFindDialog();
            }
        });

        // 2. 为整个应用程序添加后备快捷键
        JRootPane rootPane = mainFrame.getRootPane();
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK),
                "globalFind"
        );

        rootPane.getActionMap().put("globalFind", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFindDialog();
            }
        });
    }

    private void showFindDialog() {
        if (findDialog == null) {
            findDialog = new FindReplaceDialog(mainFrame, textArea);
        }

        // 确保焦点正确设置
        findDialog.setVisible(true);
        findDialog.toFront();

        // 如果编辑器有选中文本，自动填充到查找框
        String selectedText = textArea.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            findDialog.setFindText(selectedText);
        }
    }
}
