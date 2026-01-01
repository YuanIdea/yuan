package com.gly.log;

import com.gly.util.PathUtil;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * 多级日志输出工具（支持DEBUG/INFO/WARN/ERROR）
 */
public class Logger {
    // 单例实例（可按需扩展为多实例）
    private static Logger logger;
    private volatile Level level = Level.DEBUG;// 线程安全配置
    private final CopyOnWriteArrayList<Appender> appenders = new CopyOnWriteArrayList<>();
    private volatile Formatter formatter = new DefaultFormatter();

    // 建造者模式配置
    public static class Builder {
        Builder() {
            logger = new Logger();
        }

        Builder level(Level level) {
            logger.level = level;
            return this;
        }

        Builder addAppender(Appender appender) {
            logger.appenders.add(appender);
            return this;
        }

        public Builder formatter(Formatter formatter) {
            logger.formatter = formatter;
            return this;
        }

        Logger build() {
            return logger;
        }
    }

    // 日志格式化接口
    public interface Formatter {
        String format(Level level, String message, Object... args);
    }

    // 默认控制台输出
    private static class ConsoleAppender implements Appender {
        @Override
        public void append(Level level, String formattedMessage) {
            if (level == Level.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }
        }
    }

    // 默认格式化实现
    private static class DefaultFormatter implements Formatter {
        private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        @Override
        public String format(Level level, String message, Object... args) {
            return String.format("[%s] [%s] %s",
                    dtf.format(LocalDateTime.now()),
                    level,
                    (args.length > 0) ? String.format(message, args) : message);
        }
    }

    // 核心日志方法
    public void log(Level level, Supplier<String> messageSupplier) {
        if (this.level.enabled(level)) {
            String formatted = formatter.format(level, messageSupplier.get());
            appenders.forEach(a -> a.append(level, formatted));
        }
    }

    // 调试方法
    public static void debug(String format, Object... args) {
        logger.log(Level.DEBUG, () -> String.format(format, args));
    }

    public static void info(String format, Object... args) {
        logger.log(Level.INFO, () -> String.format(format, args));
    }

    public static void warn(String format, Object... args) {
        logger.log(Level.WARN, () -> String.format(format, args));
    }

    public static void error(String format, Object... args) {
        logger.log(Level.ERROR, () -> String.format(format, args));
    }

    public static void createLogger(Level level, Path logPathName) {
        Builder builder = new Logger.Builder();
        builder.level(level).addAppender(new ConsoleAppender()); // 控制台输出
        if (logPathName != null) {
            PathUtil.createPathFile(logPathName);
            builder.addAppender(new AsyncAppender(new BufferedFileAppender(logPathName))); // 异步文件输出
        }
        builder.build();
    }
}
