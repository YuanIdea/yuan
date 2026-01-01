package com.gly.log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class BufferedFileAppender implements Appender {
    private static final int FLUSH_SIZE = 100;
    private final List<String> buffer = new ArrayList<>();
    private final Path logPath;

    BufferedFileAppender(Path path) {
        // 确保路径有效性
        this.logPath = path.toAbsolutePath();
    }

    @Override
    public synchronized void append(Level level, String message) {
        buffer.add(message);
        if (buffer.size() >= FLUSH_SIZE) {
            flushBuffer();
        }
    }

    private void flushBuffer() {
        try {
            Files.write(
                    logPath,
                    buffer,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            buffer.clear();
        } catch (IOException e) {
            System.err.println("缓冲写入失败: " + e.getMessage());
        }
    }
}
