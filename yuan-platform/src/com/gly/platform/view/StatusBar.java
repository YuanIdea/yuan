package com.gly.platform.view;

import bibliothek.gui.dock.common.intern.CDockable;
import com.gly.event.GlobalBus;
import com.gly.event.Subscribe;
import com.gly.event.FocusChangeEvent;
import com.gly.platform.app.Platform;
import com.gly.platform.editor.Editor;
import com.gly.util.MenuScroller;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.nio.charset.Charset;
import java.util.SortedMap;

public class StatusBar {
    private JButton leftInfo;
    private Platform platform;

    private JButton encodingButton;
    private String[] common = {"UTF-8", "GBK", "ISO-8859-1", "US-ASCII", "UTF-16"};

    public StatusBar(Platform platform) {
        this.platform = platform;
        GlobalBus.register(this); // 注册到事件总线
    }
    public void init() {
        platform.add(createStatusBar(), BorderLayout.SOUTH);
    }
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());

        // 左侧信息区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftInfo = new JButton("Ready");
        styleEncodingButton(leftInfo);// 设置按钮外观（类似标签）
        leftPanel.add(leftInfo);

        // 右侧状态信息
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        encodingButton = new JButton("");
        styleEncodingButton(encodingButton);// 设置按钮外观（类似标签）
        rightPanel.add(encodingButton);

        // 添加编码选择功能
        encodingButton.addActionListener(e -> showEncodingMenu());

        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(rightPanel, BorderLayout.EAST);

        return statusBar;
    }

    @Subscribe
    public void handleFocusChange(FocusChangeEvent event) {
        CDockable page = event.getFocusPage();
        if (page instanceof Editor) {
            updateEncoding("", ((Editor)page).getEncoding());
        } else {
            updateEncoding("Ready", "");
        }
    }

    private void updateEncoding(String leftInfo, String encoding) {
        SwingUtilities.invokeLater(() -> {
            this.leftInfo.setText(leftInfo);
            this.leftInfo.revalidate();
            this.leftInfo.repaint();

            encodingButton.setText(encoding);
            encodingButton.revalidate();
            encodingButton.repaint();
        });
    }

    private void styleEncodingButton(JButton button) {
        button.setBorderPainted(false);          // 移除边框
        button.setFocusPainted(false);           // 移除焦点框
        button.setContentAreaFilled(false);      // 透明背景
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 手型光标
    }

    private void showEncodingMenu() {
        JPopupMenu menu = new JPopupMenu();
        for (String e:common) {
            if (!e.equals(getCurrentEncoding())) {
                addEncodingMenu(menu, e);// 添加常用编码到顶部
            }
        }

        JMenu moreItem = new JMenu("more");
        menu.add(moreItem);
        MenuScroller.setScrollerFor(moreItem, 15); // 最多显示15项

        moreItem.addMenuListener(new MenuListener() {
            @Override public void menuSelected(MenuEvent e) {
                if (moreItem.getItemCount() == 0) {
                    SortedMap<String, Charset> availableCharsets = Charset.availableCharsets();// 获取系统支持的所有编码
                    for (String charsetName : availableCharsets.keySet()) {
                        if (!isCommonEncoding(charsetName) && !charsetName.equals(getCurrentEncoding())) {
                            addEncodingMenu(moreItem, charsetName);
                        }
                    }
                    moreItem.getPopupMenu().setPopupSize(200, 400); // 设置合适的大小，弹出方向为右侧
                }
            }
            @Override public void menuDeselected(MenuEvent e) {}
            @Override public void menuCanceled(MenuEvent e) {}
        });

        menu.show(encodingButton, 0, encodingButton.getHeight());// 显示菜单
    }

    /**
     * 获取当前编码方式。
     * @return 当前编码方式。
     */
    private String getCurrentEncoding() {
        return encodingButton.getText();
    }

    /**
     * 当前编码是否在常用编码中。
     * @param charset 当前编码。
     * @return true是在常用码中，false不在常用编码中。
     */
    private boolean isCommonEncoding(String charset) {
        for (String enc : common) {
            if (enc.equalsIgnoreCase(charset))
                return true;
        }
        return false;
    }

    /**
     * 添加编码菜单。
     * @param menu 父级菜单。
     * @param charset 要添加的菜单的编码。
     */
    private void addEncodingMenu(JComponent menu, String charset) {
        JMenuItem item = new JMenuItem(charset);
        if (charset.equals(getCurrentEncoding())) {
            item.setFont(item.getFont().deriveFont(Font.BOLD)); // 当前编码加粗
        }
        item.addActionListener(e -> {
            encodingButton.setText(charset);
            System.out.println("Encoding changed to: " + charset);
            updateEditor(charset);
        });
        menu.add(item);
    }

    /**
     * 按指定编码方式重新加载编辑器。
     * @param charset 指定的编码方式。
     */
    private void updateEditor(String charset) {
        Editor curPage = (Editor)platform.getOpenPage();
        if (curPage != null) {
            curPage.setEncoding(charset);
            curPage.reload();
        }
    }

}
