package com.gly.platform.regin.auxiliary.maven;

import org.apache.maven.model.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maven project data class.
 */
class MavenProject {
    private final List<MavenProject> modules;
    private String artifactId;
    private String groupId;
    private String version;
    private String pomPath;
    private boolean isRoot;

    MavenProject(String artifactId) {
        this.artifactId = artifactId;
        this.modules = new ArrayList<>();
        isRoot = false;
    }

    MavenProject(Model model, boolean isRoot) {
        this.artifactId = model.getArtifactId();
        this.groupId = model.getGroupId();
        this.version = model.getVersion();
        this.modules = getModulesProject(model);
        this.isRoot = isRoot;
    }

    /**
     * Add modules of the project.
     *
     * @param model The model of the project.
     * @return List of MavenProject objects parsed from pom.xml.
     */
    private List<MavenProject> getModulesProject(Model model) {
        List<MavenProject> mavenProjects = new ArrayList<>();
        List<String> modules = model.getModules();
        Collections.sort(modules);
        for (String mName : modules) {
            MavenProject mavenProject = new MavenProject(mName);
            mavenProject.setPomPath(mName + "/pom.xml");
            mavenProjects.add(mavenProject);
        }
        return mavenProjects;
    }

    @Override
    public String toString() {
        String result = artifactId;
        if (version != null && !version.isEmpty()) {
            result += "-" + version;
        }

        if (groupId != null && !groupId.isEmpty()) {
            result += ":" + groupId;
        }
        return result;
    }

    // Getter和Setter方法
    public String getArtifactId() {
        return artifactId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getVersion() {
        return version;
    }

    String getPomPath() {
        return pomPath;
    }

    List<MavenProject> getModules() {
        return modules;
    }

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
}
