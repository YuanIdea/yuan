package com.gly.run;

import com.gly.PluginRegister;
import com.gly.model.ExecutableUnit;
import com.gly.platform.app.YuanConfig;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Plugin Manager, responsible for loading and managing plugins.
 */
public class PluginManager {
    // All executable units within the plugin.
    private final Map<String, ExecutableUnit> executableUnits = new HashMap<>();

    // All units within the plugin that can be registered.
    private final Map<String, PluginRegister> registers = new HashMap<>();
    // Plugin path.
    private final Path pluginDir;
    private static PluginManager pluginManager;
    private final String start;

    public static PluginManager getInstance(String start) {
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
     *
     * Load all plugins.
     */
    private void loadPlugins() {
        File pluginsRoot = pluginDir.toFile();
        if (!pluginsRoot.exists() || !pluginsRoot.isDirectory()) {
            return;
        }

        // First, load the JARs placed directly in the plugins root directory.
        File[] rootJars = pluginsRoot.listFiles((d, name) -> name.endsWith(".jar"));
        if (rootJars != null) {
            for (File jar : rootJars) {
                try {
                    loadExecutableUnits(jar);
                } catch (Exception e) {
                    System.err.println("Failed to load root plugin jar: " + jar.getName());
                    e.printStackTrace();
                }
            }
        }

        // Traverse each subdirectory (plugin folder) under the plugins directory.
        File[] pluginDirs = pluginsRoot.listFiles(File::isDirectory);
        if (pluginDirs == null)
            return;

        for (File pluginDir : pluginDirs) {
            // Skip hidden directories.
            if (pluginDir.isHidden()) continue;

            // Navigate to the lib subdirectory.
            File libDir = new File(pluginDir, "lib");
            if (!libDir.exists() || !libDir.isDirectory()) {
                // If there is no lib directory, attempt to load JARs directly from the plugin's root directory.
                File[] directJars = pluginDir.listFiles((d, name) -> name.endsWith(".jar"));
                if (directJars != null && directJars.length > 0) {
                    System.out.println(pluginDir.getName() + " No lib directory, directly load the JAR from its root directory.");
                    for (File jar : directJars) {
                        try {
                            loadExecutableUnits(jar);
                        } catch (Exception e) {
                            System.err.println("Failed to load plugin jar: " + jar.getName() + " from " + pluginDir.getName());
                            e.printStackTrace();
                        }
                    }
                }
                continue;
            }

            // 加载 lib 目录下的所有 jar 文件
            File[] libJars = libDir.listFiles((d, name) -> name.endsWith(".jar"));
            if (libJars == null || libJars.length == 0) {
                System.err.println("⚠️ 插件 " + pluginDir.getName() + " 的 lib 目录为空");
                continue;
            }

            for (File jarFile : libJars) {
                try {
                    loadExecutableUnits(jarFile);
                    // 你可以在此记录插件名称，用于后续管理
                } catch (Exception e) {
                    System.err.println("Failed to load plugin jar: " + jarFile.getName() + " from plugin " + pluginDir.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load multiple execution units from a single plugin JAR.
     */
    private void loadExecutableUnits(File jarFile) throws Exception {
        // Create a class loader.
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                getClass().getClassLoader()
        );

        // Read the JAR file and find classes that implement the Plugin interface.
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                // Only process .class files.
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");

                    // Skip inner classes.
                    if (className.contains("$")) {
                        continue;
                    }

                    // Only load classes under the expected package names.
                    if (!start.isEmpty() && !className.startsWith(start)) {
                        continue;
                    }

                    try {
                        // Load the class and check if it implements the Plugin interface.
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                            continue;
                        }
                        if (ExecutableUnit.class.isAssignableFrom(clazz)) {
                            // Instantiate the plugin.
                            ExecutableUnit executableUnit = (ExecutableUnit) clazz.getDeclaredConstructor().newInstance();
                            this.executableUnits.put(className, executableUnit);
                            System.out.println("Plugin loaded successfully:" + className);
                        }

                        // Find registration classes that implement the PluginRegister interface.
                        if (PluginRegister.class.isAssignableFrom(clazz)) {
                            PluginRegister register = (PluginRegister) clazz.getDeclaredConstructor().newInstance();
                            registers.put(className, register);
                        }
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        System.err.println("Failed to load plugin: " + className);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Get all plugins.
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