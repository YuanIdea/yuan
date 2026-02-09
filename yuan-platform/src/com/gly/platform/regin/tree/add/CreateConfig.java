package com.gly.platform.regin.tree.add;

import com.gly.i18n.I18n;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Create configuration interface.
 */
public class CreateConfig {
    // Title name.
    private final String titleName;

    // Whether OK was clicked.
    private boolean ok = false;

    // Create the name of the file.
    private String fileName;

    // Dialog.
    JDialog dialog;

    // Top panel.
    JPanel fieldPanel;

    public CreateConfig(String titleName) {
        this.titleName = titleName;
    }

    public void initializeUI(Component parent) {
        dialog = new JDialog();
        dialog.setTitle(titleName);
        dialog.setModal(true);
        dialog.setBounds(600, 200, 300, 120);
        dialog.setLayout(new BorderLayout());// Layout manager.

        fieldPanel = new JPanel();
        fieldPanel.setLayout(null);

        int height = 25;
        int y = 10;

        JLabel nameLabel = new JLabel(I18n.get("name") + ":");
        JTextField nameField = new JTextField();

        ActionListener doAction = e -> {
            this.ok = true;
            fileName = nameField.getText().trim();
            dialog.dispose();
        };
        nameField.addActionListener(doAction);
        nameLabel.setBounds(20, y, 100, height);
        fieldPanel.add(nameLabel);
        nameField.setBounds(120, y, 140, height);
        fieldPanel.add(nameField);
        dialog.add(fieldPanel, "Center");

        // Operation buttons.
        JButton okBtn = new JButton(I18n.get("ok"));
        JButton cancelBtn = new JButton(I18n.get("cancel"));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, "South");

        // Button event.
        cancelBtn.addActionListener(
                e -> {
                    this.ok = false;
                    dialog.dispose();
                });
        okBtn.addActionListener(doAction);
        dialog.setLocationRelativeTo(parent);
    }

    /**
     * Show the new file dialog.
     *
     * @param parent Parent container.
     */
    public void showNewFileDialog(Component parent) {
        initializeUI(parent);
        dialog.setVisible(true);
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isOk() {
        return ok;
    }
}
