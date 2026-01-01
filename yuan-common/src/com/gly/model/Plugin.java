package com.gly.model;

/**
 * 插件接口 - 所有插件必须实现这个接口
 */
public interface Plugin {
    /** 插件ID */
    String getId();

    /** 插件名称 */
    String getName();

    /** 插件版本 */
    String getVersion();

    /** 插件启动方法 - 平台启动时调用 */
    void start();

    /** 插件停止方法 - 平台关闭时调用 */
    void stop();

    /** 执行插件功能 */
    void execute();
}
