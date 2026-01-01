package com.gly.platform.regin.auxiliary.maven;

import org.apache.maven.model.Model;
import org.apache.maven.model.Dependency;
import java.util.*;

class DependencyManager {

    /**
     * 获取所有依赖的字符串表示（groupId:artifactId:version）
     * 自动处理变量引用 ${project.groupId}、${project.version} 等
     */
    public static List<String> getAllDependenciesAsString(Model model) {
        List<String> result = new ArrayList<>();
        if (model == null || model.getDependencies() == null) {
            return result;
        }

        for (Dependency dep : model.getDependencies()) {
            String formatted = formatDependency(dep, model);
            if (formatted != null && !formatted.trim().isEmpty()) {
                result.add(formatted);
            }
        }

        return result;
    }

    /**
     * 格式化单个依赖，自动解析变量
     */
    private static String formatDependency(Dependency dep, Model model) {
        // 解析变量：将 ${project.groupId} 等替换为实际值
        String groupId = resolveVariable(dep.getGroupId(), model);
        String artifactId = dep.getArtifactId(); // artifactId 通常不包含变量

        // 处理 version（可能为 null 或包含变量）
        String version = "";
        if (dep.getVersion() != null) {
            version = resolveVariable(dep.getVersion(), model);
        }

        // 处理 system scope 依赖（特殊的本地依赖）
        if ("system".equals(dep.getScope()) && dep.getSystemPath() != null) {
            return String.format("%s:%s:%s [system: %s]",
                    groupId, artifactId, version, dep.getSystemPath());
        }

        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    /**
     * 简单的变量解析
     */
    private static String resolveVariable(String value, Model model) {
        if (value == null) return "";

        // 替换常见的项目变量
        String result = value;

        // ${project.groupId}
        if (result.contains("${project.groupId}")) {
            String groupId = model.getGroupId() != null ? model.getGroupId() : "";
            // 如果是多模块项目，子模块可能没有 groupId，需要检查父模块
            if (groupId.isEmpty() && model.getParent() != null) {
                groupId = model.getParent().getGroupId();
            }
            result = result.replace("${project.groupId}", groupId);
        }

        // ${project.version}
        if (result.contains("${project.version}")) {
            String version = model.getVersion() != null ? model.getVersion() : "";
            // 如果是多模块项目，子模块可能没有 version，需要检查父模块
            if (version.isEmpty() && model.getParent() != null) {
                version = model.getParent().getVersion();
            }
            result = result.replace("${project.version}", version);
        }

        // ${project.artifactId} - 这个通常不用于依赖坐标
        if (result.contains("${project.artifactId}")) {
            String artifactId = model.getArtifactId() != null ? model.getArtifactId() : "";
            result = result.replace("${project.artifactId}", artifactId);
        }

        // 移除未解析的变量占位符（避免输出 ${xxx}）
        if (result.contains("${") && result.contains("}")) {
            // 简单移除变量占位符
            result = result.replaceAll("\\$\\{[^}]*\\}", "");
        }

        return result.trim();
    }

    /**
     * 更简单的版本：只获取基础坐标，忽略变量和特殊处理
     */
    public static List<String> getSimpleDependencyList(Model model) {
        List<String> result = new ArrayList<>();

        if (model == null || model.getDependencies() == null) {
            return result;
        }

        for (Dependency dep : model.getDependencies()) {
            String coord = dep.getGroupId() + ":" + dep.getArtifactId();
            if (dep.getVersion() != null && !dep.getVersion().trim().isEmpty()) {
                coord += ":" + dep.getVersion();
            }
            result.add(coord);
        }

        return result;
    }

    /**
     * 获取依赖数量统计
     */
    public static int getDependencyCount(Model model) {
        return model != null && model.getDependencies() != null ?
                model.getDependencies().size() : 0;
    }

    /**
     * 快速检查是否包含特定依赖（模糊匹配）
     */
    public static boolean containsDependency(Model model, String keyword) {
        if (model == null || model.getDependencies() == null || keyword == null) {
            return false;
        }

        String lowerKeyword = keyword.toLowerCase();
        for (Dependency dep : model.getDependencies()) {
            if (dep.getGroupId().toLowerCase().contains(lowerKeyword) ||
                    dep.getArtifactId().toLowerCase().contains(lowerKeyword)) {
                return true;
            }
        }

        return false;
    }
}
