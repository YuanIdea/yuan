package com.gly.platform.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;

public class JPopupTextField extends JTextPane implements MouseListener, ActionListener {
    private JPopupMenu popupMenu;
    private JMenuItem copyMenu;
    private JMenuItem selectAllMenu;
    private JMenuItem clearAllMenu;

    public JPopupTextField() {
        super();
        popupMenu = new JPopupMenu();

        copyMenu = new JMenuItem("拷贝");
        selectAllMenu = new JMenuItem("全选");
        clearAllMenu = new JMenuItem("清空");

        copyMenu.setAccelerator(KeyStroke.getKeyStroke('C'));
        selectAllMenu.setAccelerator(KeyStroke.getKeyStroke('A'));

        copyMenu.addActionListener(this);
        selectAllMenu.addActionListener(this);
        clearAllMenu.addActionListener(this);

        popupMenu.add(copyMenu);
        popupMenu.add(new JSeparator());
        popupMenu.add(selectAllMenu);
        popupMenu.add(clearAllMenu);

        this.add(popupMenu);
        this.addMouseListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == copyMenu) {
            this.copy();
        }
        if (e.getSource() == selectAllMenu) {
            this.selectAll();
        }
        if (e.getSource() == clearAllMenu) {
            this.setText("");
        }
    }

    public void mousePressed(MouseEvent e) {
        popupMenuTrigger(e);
    }

    public void mouseReleased(MouseEvent e) {
        popupMenuTrigger(e);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private void popupMenuTrigger(MouseEvent e) {
        if (e.isPopupTrigger()) {
            this.requestFocusInWindow();
            boolean r = isAbleToCopyAndCut();
            copyMenu.setEnabled(r);
            selectAllMenu.setEnabled(isAbleToSelectAll());

            popupMenu.show(this, e.getX() + 3, e.getY() + 3);
        }
    }

    /**
     * 是否能够全选。
     *
     * @return 是否能够全选
     */
    private boolean isAbleToSelectAll() {
        return !("".equalsIgnoreCase(this.getText()) || (null == this.getText()));
    }

    /**
     * 是否允许拷贝和剪切。
     *
     * @return 是否允许拷贝和剪切
     */
    private boolean isAbleToCopyAndCut() {
        return this.getSelectionStart() != this.getSelectionEnd();
    }
}

