package com.gly.platform.view;

import com.gly.event.page.PageInfo;
import com.gly.platform.regin.work.PageDockable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;

public class SaveAction extends AbstractAction {
    private final PageDockable page;
    private File currentFile; // 当前编辑的文件（若为新文件则为 null）

    public SaveAction(PageDockable page) {
        super("保存");
        this.page = page;
        PageInfo pageInfo = page.getPageInfo();
        if (pageInfo != null) {
            this.currentFile = new File(pageInfo.getName());
        }
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));// 绑定快捷键 Ctrl+S
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentFile == null) {
            // 若文件未保存过，触发“另存为”操作
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(page.getContentPane());
            if (result == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
            } else {
                return; // 用户取消保存
            }
        }
        page.save(currentFile.getPath());
    }

    public static void bindSaveShortcut(JComponent component, Action saveAction){
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        KeyStroke ctrlS = SaveAction.getSaveKeyStroke();
        inputMap.put(ctrlS, "saveAction");
        actionMap.put("saveAction", saveAction);
    }

    /**
     * 获取跨平台的保存快捷键（Ctrl+S 或 Cmd+S）
     * @return 快捷热键。
     */
    private static KeyStroke getSaveKeyStroke() {
        int modifiers;
        String osName = System.getProperty("os.name").toLowerCase();
        // 判断操作系统类型
        if (osName.contains("mac")) {
            modifiers = KeyEvent.META_DOWN_MASK; // macOS 使用 Cmd
        } else {
            modifiers = KeyEvent.CTRL_DOWN_MASK;  // 其他系统使用 Ctrl
        }

        return KeyStroke.getKeyStroke(KeyEvent.VK_S, modifiers);
    }
}
