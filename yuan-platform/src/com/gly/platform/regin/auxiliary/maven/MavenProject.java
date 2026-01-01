package com.gly.platform.regin.auxiliary.maven;

import java.util.ArrayList;
import java.util.List;

// Maven项目数据类
class MavenProject {
    private String artifactId;
    private String groupId;
    private String version;
    private String pomPath;
    private boolean isRoot;
    private List<MavenProject> modules;

    MavenProject(String artifactId) {
        this.artifactId = artifactId;
        this.modules = new ArrayList<>();
        isRoot = false;
    }

    public void addModule(MavenProject module) {
        modules.add(module);
    }

    @Override
    public String toString() {
        String result = artifactId;
        if (version!=null && !version.isEmpty()) {
            result += "-" + version;
        }

        if (groupId!=null && !groupId.isEmpty()) {
            result += ":" + groupId;
        }
        return result;
    }

    // Getter和Setter方法
    public String getArtifactId() { return artifactId; }
    public String getGroupId() { return groupId; }
    public String getVersion() { return version; }
    String getPomPath() { return pomPath; }
    List<MavenProject> getModules() { return modules; }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setPomPath(String pomPath) {
        this.pomPath = pomPath;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public void setModules(List<MavenProject> modules) {
        this.modules = modules;
    }
}
