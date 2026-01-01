package com.gly;

/**
 * 在平台中定义注册接口
 */
public interface PluginRegister {
    /** 插件注册方法 */
    void register();

    /** 插件注销方法 */
    void unregister();
}
