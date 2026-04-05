package com.gly.platform.regin.output;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Wrapper stream for error output that automatically adds red ANSI color to each line of error output.
 */
class ErrorAnsiOutputStream extends FilterOutputStream {
    // Flag indicating whether a red prefix needs to be added before the next write
    private boolean needRedPrefix = true;

    public ErrorAnsiOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        if (needRedPrefix) {
            // Write the red ANSI escape code: \033[91m sets the foreground color to red
            out.write("\033[91m".getBytes(StandardCharsets.UTF_8));
            needRedPrefix = false;
        }

        // Write the actual content
        out.write(b);

        // If a newline character is encountered, this line of error output has ended
        if (b == '\n') {
            // Write the reset ANSI escape code to restore the default color
            out.write("\033[0m".getBytes(StandardCharsets.UTF_8));
            needRedPrefix = true;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        // Process byte by byte during bulk writes to ensure newline flags are correctly handled
        for (int i = off; i < off + len; ++i) {
            write(b[i]);
        }
    }
}
