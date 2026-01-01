package com.gly.platform.regin.output;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TextPaneOutputStream extends OutputStream {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final StyledDocument doc;
    private final JTextPane textPane;
    private final SimpleAttributeSet style;

    TextPaneOutputStream(JTextPane textPane, SimpleAttributeSet style) {
        this.textPane = textPane;
        this.doc = textPane.getStyledDocument();
        this.style = style;
    }

    @Override
    public void write(int b) {
        buffer.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.write(b, off, len);
    }

    @Override
    public void flush() {
        // 关键修复：显式转换字节数组为 UTF-8 字符串
        String content = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        buffer.reset();

        SwingUtilities.invokeLater(() -> {
            try {
                doc.insertString(doc.getLength(), content, style);
                textPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                System.err.println(e.getMessage());
            }
        });
    }
}
