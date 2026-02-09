package com.gly.platform.view;

import bibliothek.gui.dock.common.intern.CDockable;
import com.gly.event.GlobalBus;
import com.gly.event.Subscribe;
import com.gly.event.FocusChangeEvent;
import com.gly.i18n.I18n;
import com.gly.platform.app.Platform;
import com.gly.platform.editor.Editor;
import com.gly.util.MenuScroller;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.nio.charset.Charset;
import java.util.SortedMap;

/**
 * Status bar at the bottom of the platform.
 */
public class StatusBar {
    private final Platform platform;
    private final String[] common = {"UTF-8", "GBK", "ISO-8859-1", "US-ASCII", "UTF-16"};
    private JButton leftInfo;
    private JButton encodingButton;
    private final String ready = I18n.get("status.ready");

    public StatusBar(Platform platform) {
        this.platform = platform;
        GlobalBus.register(this); // Register to event bus.
    }

    public void init() {
        platform.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());

        // Information area on the left.
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftInfo = new JButton(ready);
        styleEncodingButton(leftInfo);// Set the button appearance to resemble a label.
        leftPanel.add(leftInfo);

        // Status information on the right.
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        encodingButton = new JButton("");
        styleEncodingButton(encodingButton);// Set the button appearance to resemble a label.
        rightPanel.add(encodingButton);

        // Add the ability to select encoding types.
        encodingButton.addActionListener(e -> showEncodingMenu());

        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(rightPanel, BorderLayout.EAST);

        return statusBar;
    }

    @Subscribe
    public void handleFocusChange(FocusChangeEvent event) {
        CDockable page = event.getFocusPage();
        if (page instanceof Editor) {
            updateEncoding("", ((Editor) page).getEncoding());
        } else {
            updateEncoding(ready, "");
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
        button.setBorderPainted(false);          // Remove border.
        button.setFocusPainted(false);           // Remove focus outline.
        button.setContentAreaFilled(false);      // Transparent background.
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // 手型光标
    }

    private void showEncodingMenu() {
        JPopupMenu menu = new JPopupMenu();
        for (String e : common) {
            if (!e.equals(getCurrentEncoding())) {
                addEncodingMenu(menu, e);// Add common encodings to the top.
            }
        }

        JMenu moreItem = new JMenu("more");
        menu.add(moreItem);
        MenuScroller.setScrollerFor(moreItem, 15); // 最多显示15项

        moreItem.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
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

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });

        menu.show(encodingButton, 0, encodingButton.getHeight());// Display menu.
    }

    /**
     * Get the encoding method of the current text.
     *
     * @return The encoding method of the current text.
     */
    private String getCurrentEncoding() {
        return encodingButton.getText();
    }

    /**
     * Determine if the current encoding method is among the common encodings.
     *
     * @param charset Currently used encoding.
     * @return Return true if it is among the common encodings; return false if it is not.
     */
    private boolean isCommonEncoding(String charset) {
        for (String enc : common) {
            if (enc.equalsIgnoreCase(charset))
                return true;
        }
        return false;
    }

    /**
     * Add encoding menu.
     *
     * @param menu    Parent menu.
     * @param charset Encoding to add to the menu.
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
     * Reload the editor text content according to the specified encoding method.
     *
     * @param charset Specified encoding method.
     */
    private void updateEditor(String charset) {
        Editor curPage = (Editor) platform.getOpenPage();
        if (curPage != null) {
            curPage.setEncoding(charset);
            curPage.reload();
        }
    }

}
