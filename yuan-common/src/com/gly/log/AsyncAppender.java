package com.gly.log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncAppender implements Appender {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Appender target;

    AsyncAppender(Appender target) {
        this.target = target;
    }

    @Override
    public void append(Level level, String message) {
        executor.submit(() -> target.append(level, message));
    }
}