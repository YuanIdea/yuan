package com.gly.platform.editor;

import bibliothek.gui.dock.common.intern.CDockable;
import com.gly.event.GlobalBus;
import com.gly.event.Subscribe;
import com.gly.event.FocusChangeEvent;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;
import java.awt.*;

import java.awt.event.KeyEvent;

/**
 * 查找和替换功能。
 */
public class FindReplaceDialog extends JDialog {
    private RSyntaxTextArea textArea;
    private JTextField findField;
    private JTextField replaceField;
    private JCheckBox matchCaseCheckBox;
    private JCheckBox wholeWordCheckBox;
    private JCheckBox regexCheckBox;
    private JCheckBox wrapSearchCheckBox;
    private JCheckBox markAllCheckBox;
    private JTextField findPos;

    private JLabel statusLabel;

    FindReplaceDialog(JFrame owner, RSyntaxTextArea textArea) {
        super(owner, "查找和替换", false);
        GlobalBus.register(this); // 注册到事件总线
        this.textArea = textArea;
        initializeUI();
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 查找标签和字段
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("查找:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        findField = new JTextField(30);
        findField.setToolTipText("输入要查找的文本");
        mainPanel.add(findField, gbc);

        // 替换标签和字段
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("替换为:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        replaceField = new JTextField(30);
        replaceField.setToolTipText("输入替换文本");
        mainPanel.add(replaceField, gbc);

        // 查找位置
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("查找位置:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        findPos = new JTextField(30);
        findPos.setText(textArea.getName());
        findPos.setEnabled(false);
        mainPanel.add(findPos, gbc);

        // 选项面板
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        matchCaseCheckBox = new JCheckBox("区分大小写");
        wholeWordCheckBox = new JCheckBox("全字匹配");
        regexCheckBox = new JCheckBox("正则表达式");
        wrapSearchCheckBox = new JCheckBox("循环搜索", true);
        markAllCheckBox = new JCheckBox("标记所有匹配项", true);

        optionsPanel.add(matchCaseCheckBox);
        optionsPanel.add(wholeWordCheckBox);
        optionsPanel.add(regexCheckBox);
        optionsPanel.add(wrapSearchCheckBox);
        optionsPanel.add(markAllCheckBox);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        mainPanel.add(optionsPanel, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        JButton findPrevButton = createButton("上一步", this::findPrevious, "查找上一个匹配项");
        JButton findNextButton = createButton("下一步", this::findNext, "查找下一个匹配项");
        JButton replaceButton = createButton("替换", this::replace, "替换当前匹配项");
        JButton replaceAllButton = createButton("全部替换", this::replaceAll, "替换所有匹配项");

        buttonPanel.add(findPrevButton);
        buttonPanel.add(findNextButton);
        buttonPanel.add(replaceButton);
        buttonPanel.add(replaceAllButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        mainPanel.add(buttonPanel, gbc);

        // 状态栏
        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusLabel.setForeground(Color.GRAY);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        // 设置快捷键
        setupKeyBindings();

        // 初始焦点
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                findField.requestFocus();
                findField.selectAll();
            }
        });
    }

    private JButton createButton(String text, Runnable action, String tooltip) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setMargin(new Insets(3, 8, 3, 8));
        return button;
    }

    private void setupKeyBindings() {
        // 查找字段回车触发查找下一个
        findField.registerKeyboardAction(
                e -> findNext(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_FOCUSED
        );

        // 替换字段回车触发替换
        replaceField.registerKeyboardAction(
                e -> replace(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_FOCUSED
        );

        // ESC键关闭对话框
        getRootPane().registerKeyboardAction(
                e -> setVisible(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private SearchContext createSearchContext() {
        SearchContext context = new SearchContext();
        context.setSearchFor(findField.getText());
        context.setReplaceWith(replaceField.getText());
        context.setMatchCase(matchCaseCheckBox.isSelected());
        context.setWholeWord(wholeWordCheckBox.isSelected());
        context.setRegularExpression(regexCheckBox.isSelected());
        context.setSearchWrap(wrapSearchCheckBox.isSelected());
        context.setMarkAll(markAllCheckBox.isSelected());
        return context;
    }

    /**
     * 查找下一个。
     */
    private void findNext() {
        SearchContext context = createSearchContext();
        context.setSearchForward(true);
        SearchResult result = SearchEngine.find(textArea, context);
        updateStatus(result);
    }

    /**
     * 查找上一个。
     */
    private void findPrevious() {
        SearchContext context = createSearchContext();
        context.setSearchForward(false);
        SearchResult result = SearchEngine.find(textArea, context); // 反向搜索
        updateStatus(result);
    }

    private void replace() {
        SearchContext context = createSearchContext();
        SearchResult result = SearchEngine.replace(textArea, context);
        updateStatus(result);
    }

    private void replaceAll() {
        SearchContext context = createSearchContext();
        SearchResult result = SearchEngine.replaceAll(textArea, context);

        if (result.getCount() == 0) {
            statusLabel.setText("未找到匹配项");
        } else {
            statusLabel.setText("已替换 " + result.getCount() + " 处匹配项");
        }
    }

    private void updateStatus(SearchResult result) {
        if (!result.wasFound()) {
            statusLabel.setText("未找到匹配项");
        } else if (result.isWrapped()) {
            statusLabel.setText("已循环搜索到开头");
        } else {
            statusLabel.setText("找到匹配项");
        }
    }

    public static void showDialog(JFrame owner, RSyntaxTextArea textArea) {
        FindReplaceDialog dialog = new FindReplaceDialog(owner, textArea);
        dialog.setVisible(true);
    }

    void setFindText(String text) {
        findField.setText(text);
        findField.selectAll();
        findField.requestFocusInWindow();
    }

    @Subscribe
    public void handleFocusChange(FocusChangeEvent event) {
        CDockable page = event.getFocusPage();
        if (page instanceof Editor) {
            textArea = ((Editor) page).getTextArea();
            findPos.setText(textArea.getName());
        }
    }
}