package com.gly.platform.regin.output;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;


/**
 * Supports ANSI color output.
 */
class Redirect {

    static void systemOutToTextArea(JTextPane textPane) {
        System.setOut(createPrintStream(textPane, false));
    }

    static void systemErrorStream(JTextPane textPane) {
        System.setErr(createPrintStream(textPane, true));
    }

    private static PrintStream createPrintStream(JTextPane textPane, boolean isError) {
        try {
            return new PrintStream(
                    new AnsiStream(textPane, isError), true, StandardCharsets.UTF_8.name()
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}