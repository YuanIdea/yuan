package com.gly.platform.regin.output;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 重定向类。
 */
class Redirect {
    private static final SimpleAttributeSet ERROR_STYLE = new SimpleAttributeSet();
    static {
        StyleConstants.setForeground(ERROR_STYLE, Color.RED);
    }

    static void systemOutToTextArea(JTextPane jText)   {
        PrintStream out = createPrintStream(jText, null);
        System.setOut(out);
    }

    static void systemErrorStream(JTextPane jText)  {
        PrintStream out = createPrintStream(jText, ERROR_STYLE);
        System.setErr(out);
    }

    private static PrintStream createPrintStream(JTextPane textPane, SimpleAttributeSet style)  {
        try {
            return new PrintStream(new BufferedOutputStream(new TextPaneOutputStream(textPane, style)),
                    true,
                    StandardCharsets.UTF_8.name()
            );
        } catch (UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}