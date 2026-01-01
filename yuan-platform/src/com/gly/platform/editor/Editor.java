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
 * 编辑器。
 */
public class Editor extends PageDockable {
    // 页面
    private RSyntaxTextArea textArea;

    // 文件编码
    private String encoding;

    // 加载文件路径。
    private Path path;

    // 是否被编辑过。
    private boolean isModified;

    // 未修改文档内容。
    private String originalContent;

    public Editor(MultipleCDockableFactory<PageDockable,?> factory, PageInfo pageInfo){
        super( factory, pageInfo );
        encoding = StandardCharsets.UTF_8.toString();
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setTabSize(4);
        textArea.setName(pageInfo.getFileName());

        String type = PathUtil.getFileExtension(pageInfo.getName());
        String syntaxStyle = "text/"+type;
        textArea.setSyntaxEditingStyle(syntaxStyle);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAutoIndentEnabled(true); // 设置自动缩进启用
        // 启用抗锯齿获得更好效果
        textArea.setAntiAliasingEnabled(true);
        textArea.setFractionalFontMetricsEnabled(true);
        addPopupMenu();
        RTextScrollPane sp = new RTextScrollPane(textArea);
        getContentPane().add(sp);
        path = Paths.get(pageInfo.getName());
        String checkEncoding = Encoding.detectCharset(path);
        new EditorShortcutManager(textArea); // 快捷键。

        if (!checkEncoding.equalsIgnoreCase(encoding)) {
            Logger.info("检测到可能不是" + encoding + "编码，建议尝试"+checkEncoding+"编码方式打开。");
        }

        setTextNoBack(FileUtil.loadFile(path, encoding));
        SaveAction.bindSaveShortcut(textArea, new SaveAction(this)); // 创建保存操作并绑定
        Drop.setupDragAndDrop(textArea);

        textArea.getDocument().addDocumentListener(generateDocumentListener());// 添加文档修改监听器
        this.addVetoClosingListener(generateCVetoClosingListener());// 页面关闭处理
    }

    /**
     * 重新加载。
     */
    public void reload() {
        setTextNoBack(FileUtil.loadFile(path, encoding));
    }

    /**
     * 重新设置内容，并不允许撤销。
     * @param t 要设置的内容。
     */
    private void setTextNoBack(String t) {
        textArea.beginAtomicEdit(); // 暂停撤销记录
        textArea.setText(t);
        textArea.discardAllEdits();
        textArea.endAtomicEdit();// 恢复撤销记录
        this.originalContent = t;
        setModified(false);
    }

    /**
     * 保存内容。
     * @param pathName 保存路径。
     */
    @Override
    public void save(String pathName) {
        FileUtil.saveToFile(pathName, textArea.getText(), encoding);
        this.originalContent = textArea.getText();
        setModified(false);
    }

    /**
     * 只有文件修改时能保存，否则不做保存。
     */
    public void saveModified() {
        if (isModified) {
            save(getPageInfo().getName());
        }
    }

    /**
     * 创建文件监听器。
     * @return 文件监听器。
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
     * 通过内容检测是否发生改变。
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
     * 创建文件关闭监听处理器。
     * @return 文件关闭监听处理器
     */
    private CVetoClosingListener generateCVetoClosingListener() {
        return new CVetoClosingListener() {
            public void closing( CVetoClosingEvent event ) {
                if (isModified) {
                    int option = JOptionPane.showOptionDialog(textArea,
                            textArea.getName() + "文件已修改，是否保存？", "保存文件",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                            new String[]{"保存", "不保存", "取消"}, "保存");

                    if (option == JOptionPane.YES_OPTION) {
                        save(getPageInfo().getName());
                    } else if (option == JOptionPane.CANCEL_OPTION) {
                        event.cancel();  // 关键：阻止关闭操作
                    }
                }
            }
            public void closed( CVetoClosingEvent event ){ }
        };
    }



    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String value) {
        this.encoding = value;
    }

    /**
     * 设置文档为被修改状态。
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
     * 是否可被撤销。
     * @return 是否可被撤销。
     */
    public boolean canUndo() {
        return textArea.canUndo();
    }

    /**
     * 是否可被重做
     * @return 是否可被重做。
     */
    public boolean canRedo() {
        return textArea.canRedo();
    }

    /**
     * 撤销。
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
     * 重做。
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
     * 添加自定义右键。
     */
    private void addPopupMenu() {
        // 获取默认的弹出菜单
        JPopupMenu popupMenu = textArea.getPopupMenu();
        // 添加分隔符
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
