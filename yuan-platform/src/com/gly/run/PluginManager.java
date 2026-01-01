package com.gly.run;


import com.gly.PluginRegister;
import com.gly.model.ExecutableUnit;
import com.gly.platform.app.YuanConfig;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 插件管理器 - 负责加载和管理插件
 */
public class PluginManager {
    // 插件中的所有可执行单元。
    private final Map<String, ExecutableUnit> executableUnits = new HashMap<>();

    private final Map<String, PluginRegister> registers = new HashMap<>();
    // 插件路径。
    private Path pluginDir;
    private static PluginManager pluginManager;
    private String start;

    public static PluginManager getInstance(String start){
        if (pluginManager == null) {
            pluginManager = new PluginManager(start);
        }
        return pluginManager;
    }

    private PluginManager(String start) {
        this.pluginDir = YuanConfig.YUAN_PATH.resolve("plugins");
        this.start = start;
        loadPlugins();
    }

    /**
     * 加载所有插件
     */
    private void loadPlugins() {
        File dir = pluginDir.toFile();
        if (!dir.exists()) {
            System.out.println("插件目录不存在: " + pluginDir);
            return;
        }

        File[] jarFiles = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (jarFiles == null) {
            return;
        }

        for (File jarFile : jarFiles) {
            try {
                loadExecutableUnits(jarFile);
            } catch (Exception e) {
                System.err.println("❌ 加载插件失败: " + jarFile.getName());
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载单个插件JAR中的多个执行单元。
     */
    private void loadExecutableUnits(File jarFile) throws Exception {
        // 1. 创建类加载器
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                getClass().getClassLoader()  // 使用父类加载器
        );

        // 2. 读取JAR包，查找实现Plugin接口的类
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                // 只处理.class文件
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");

                    // 跳过内部类
                    if (className.contains("$")) {
                        continue;
                    }

                    // 3. 只加载你期望的包名下的类（例如 com. 开头的包）
                    // 这样可以避免加载第三方库的内部类
                    if (!start.isEmpty() && !className.startsWith(start)) {
                        continue;
                    }

                    try {
                        // 3. 加载类并检查是否实现了Plugin接口
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                            continue;
                        }
                        if (ExecutableUnit.class.isAssignableFrom(clazz)) {
                            // 4. 实例化插件
                            ExecutableUnit executableUnit = (ExecutableUnit) clazz.getDeclaredConstructor().newInstance();
                            this.executableUnits.put(className, executableUnit);
                            System.out.println("✅ 插件加载成功: " + className);
                        }

                        // 2. 查找注册类（实现PluginRegister接口）
                        if (PluginRegister.class.isAssignableFrom(clazz)) {
                            PluginRegister register = (PluginRegister) clazz.getDeclaredConstructor().newInstance();
                            registers.put(className, register);
                        }
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        System.err.println("❌ 加载插件失败: " + className);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 获取所有插件
     */
    Map<String, ExecutableUnit> getExecutableUnits() {
        return executableUnits;
    }

    public void register(String id) {
        PluginRegister pluginRegister = registers.get(id);
        if (pluginRegister != null) {
            pluginRegister.register();
        }
    }
}