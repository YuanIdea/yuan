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
     * Load all plugins.
     */
    private void loadPlugins() {
        File dir = pluginDir.toFile();
        if (!dir.exists()) {
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
                System.err.println("❌ Failed to load plugin: " + jarFile.getName());
                e.printStackTrace();
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
                            System.out.println("✅ Plugin loaded successfully:" + className);
                        }

                        // Find registration classes that implement the PluginRegister interface.
                        if (PluginRegister.class.isAssignableFrom(clazz)) {
                            PluginRegister register = (PluginRegister) clazz.getDeclaredConstructor().newInstance();
                            registers.put(className, register);
                        }
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        System.err.println("❌ Failed to load plugin: " + className);
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