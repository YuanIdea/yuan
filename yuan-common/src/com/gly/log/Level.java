package com.gly.log;

// 日志级别枚举
public enum Level {
    DEBUG, INFO, WARN, ERROR;

    /**
     * 是否打印日志，只有当前的信息等级大于等于指定层级的信息才被打印。
     * @param current 当前信息的等级。
     * @return 是否打钱当前日志。
     */
    public boolean enabled(Level current) {
        return current.ordinal() >= this.ordinal();
    }
}
