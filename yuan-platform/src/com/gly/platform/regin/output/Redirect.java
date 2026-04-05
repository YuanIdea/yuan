package com.gly.platform.regin.output;

import java.io.*;
import java.nio.charset.StandardCharsets;


/**
 * Supports ANSI color output.
 */
class Redirect {
    static void systemOutToTextArea(OutputStream outputStream) {
        System.setOut(createPrintStream(outputStream));
    }

    static void systemErrorStream(OutputStream outputStream) {
        ErrorAnsiOutputStream errorStream = new ErrorAnsiOutputStream(outputStream);
        System.setErr(createPrintStream(errorStream));
    }

    private static PrintStream createPrintStream(OutputStream outputStream) {
        try {
            // Redirect System output directly to the pipe stream;
            // the terminal will automatically read and parse ANSI colors.
            return new PrintStream(outputStream, true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}