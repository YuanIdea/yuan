package com.gly.platform.view;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

/**
 * about dialog.
 */
class AboutDialog {
    private static final String APP_NAME = "yuan-1.0.5";
    private static final String VERSION = "2026.01.08";
    private static final String WEB = "https://github.com/YuanIdea/yuan";

    static void showAboutDialog(JFrame parent) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JDialog aboutDialog = new JDialog(parent, "About", true);
        aboutDialog.setLayout(new BorderLayout(20, 20));
        aboutDialog.setResizable(false);

        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top icon and title area.
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Load the 64x64 application icon file.
        ImageIcon appIcon = new ImageIcon(AboutDialog.class.getResource("/icons/aif.png"));
        JLabel iconLabel = new JLabel(new ImageIcon(appIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        headerPanel.add(iconLabel);

        // Add homepage link information.
        JLabel linkLabel = new JLabel("<html><a href=''>" + WEB + "</a></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    Desktop.getDesktop().browse(new URI(WEB));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Platform name and version.
        JLabel titleLabel = new JLabel(APP_NAME);
        JLabel versionLabel = new JLabel(VERSION);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.add(titleLabel);
        textPanel.add(versionLabel);
        headerPanel.add(textPanel);

        JTextArea infoArea = new JTextArea();
        String base =
                "Java runtime: " + System.getProperty("java.version") + "\n" +
                "JVM version: " + System.getProperty("java.vm.version") + "\n" +
                "Java home: " + System.getProperty("java.home") + "\n";
        infoArea.setText(base);
        infoArea.setEditable(false);
        infoArea.setOpaque(false);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(infoArea, BorderLayout.CENTER);
        contentPanel.add(linkLabel, BorderLayout.SOUTH);
        aboutDialog.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        aboutDialog.add(buttonPanel, BorderLayout.SOUTH);

        aboutDialog.pack();
        aboutDialog.setLocationRelativeTo(parent);
        aboutDialog.setVisible(true);
    }
}
