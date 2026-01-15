package com.gly.platform.regin.output;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AnsiStream extends OutputStream {
    private static final Pattern ANSI_PATTERN = Pattern.compile("\u001B\\[([\\d;]*)m");
    private static final Color[] BASIC_COLORS = {
            Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW,
            Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE
    };
    private final JTextPane textPane;
    private final boolean isError;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private Color currentFg = null;
    private Color currentBg = null;
    private boolean bold = false;

    AnsiStream(JTextPane textPane, boolean isError) {
        this.textPane = textPane;
        this.isError = isError;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.write(b);
        if (b == '\n') {
            flush();
        }
    }

    @Override
    public void flush() throws IOException {
        if (buffer.size() == 0)
            return;

        String text = buffer.toString(StandardCharsets.UTF_8);
        buffer.reset();
        processText(text);// Process ANSI escape sequences
        super.flush();
    }

    private void processText(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = textPane.getStyledDocument();
                // Parse ANSI escape sequences
                Matcher matcher = ANSI_PATTERN.matcher(text);
                int lastEnd = 0;
                while (matcher.find()) {
                    // Append text before the ANSI sequence
                    if (matcher.start() > lastEnd) {
                        String plain = text.substring(lastEnd, matcher.start());
                        appendWithStyle(doc, plain);
                    }

                    // Process the ANSI escape sequence
                    processAnsiCode(matcher.group(1));
                    lastEnd = matcher.end();
                }

                // Append remaining text
                if (lastEnd < text.length()) {
                    String remaining = text.substring(lastEnd);
                    appendWithStyle(doc, remaining);
                }

                textPane.setCaretPosition(doc.getLength());// Scroll to bottom
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void appendWithStyle(StyledDocument doc, String text) throws BadLocationException {
        SimpleAttributeSet style = createStyle();
        doc.insertString(doc.getLength(), text, style);
    }

    private SimpleAttributeSet createStyle() {
        SimpleAttributeSet style = new SimpleAttributeSet();

        // Set foreground color
        if (currentFg != null) {
            StyleConstants.setForeground(style, currentFg);
        } else if (isError) {
            StyleConstants.setForeground(style, Color.RED);
        } else {
            StyleConstants.setForeground(style, textPane.getForeground());
        }

        // Set background color
        if (currentBg != null) {
            StyleConstants.setBackground(style, currentBg);
        }

        // Set bold style
        if (bold) {
            StyleConstants.setBold(style, true);
        }
        return style;
    }

    private void processAnsiCode(String codes) {
        if (codes.isEmpty()) {
            // Reset all attributes
            currentFg = null;
            currentBg = null;
            bold = false;
            return;
        }

        String[] parts = codes.split(";");
        for (String part : parts) {
            try {
                int code = Integer.parseInt(part);
                applyCode(code);
            } catch (NumberFormatException e) {
                // Ignore invalid codes
            }
        }
    }

    private void applyCode(int code) {
        switch (code) {
            case 0:
                currentFg = null;
                currentBg = null;
                bold = false;
                break;
            case 1:
                bold = true;
                break;
            case 22:
                bold = false;
                break;
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
                currentFg = BASIC_COLORS[code - 30];
                break;
            case 39:
                currentFg = null;
                break;
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
                currentBg = BASIC_COLORS[code - 40];
                break;
            case 49:
                currentBg = null;
                break;
        }
    }
}