package com.gly.platform.app;


import javax.swing.*;

public class Yuan {
    public static void main( String[] args ) {
        SwingUtilities.invokeLater(() -> {
            Platform frame = Platform.getInstance();
            frame.startup(args, true);
        });
    }
}
