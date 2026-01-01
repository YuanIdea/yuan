package com.gly.log;

// 日志输出接口
public interface Appender {
    void append(Level level, String formattedMessage);
}
