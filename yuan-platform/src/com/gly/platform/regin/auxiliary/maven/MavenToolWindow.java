package com.gly.platform.regin.auxiliary.maven;

import com.gly.platform.app.Platform;
import com.gly.util.IconUtil;
import org.apache.maven.model.Model;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Maven工具窗口面板�?
 */
public class MavenToolWindow extends JPanel {
    // 组件
    private JTree projectTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JTabbedPane lifecycleTabbedPane;
    private MavenProject currentProject;
    // 数据模型
    private List<MavenProject> mavenProjects;
    private Platform platform;
    private JTree dependenciesTree;
    public MavenToolWindow() {
        platform = Platform.getInstance();
        initUI();
    }

    /**
     * 初��化界面�?
     */
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 创建主分割面�?
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(300);
        mainSplitPane.setResizeWeight(0.5);

        // 创建左侧面板（项�?和生命周期）
        JPanel leftPanel = new JPanel(new BorderLayout());

        // 项目�?
        refreshProjectTree();
        JScrollPane treeScrollPane = new JScrollPane(projectTree);

        // 生命周期选项�?
        createLifecycleTabs();

        // 左侧使用分割面板
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setDividerLocation(200);
        leftSplitPane.setResizeWeight(0.5);
        leftSplitPane.setTopComponent(treeScrollPane);
        leftSplitPane.setBottomComponent(lifecycleTabbedPane);

        leftPanel.add(leftSplitPane, BorderLayout.CENTER);
        mainSplitPane.setTopComponent(leftPanel);
        add(mainSplitPane, BorderLayout.CENTER);
    }

    /**
     * Refresh project folder.
     */
    public void refreshProjectTree() {
        String root = platform.getRoot();
        Path rootPath = Paths.get(root);

        Path pomPath = rootPath.resolve("pom.xml");
        if (!Files.exists(pomPath)) {
            return;
        }
        Model model = Pom.readPom(pomPath.toString());
        if (model == null) {
            return;
        }

        MavenProject rootProject = new MavenProject(model.getArtifactId());
        rootProject.setRoot(true);
        rootProject.setGroupId(model.getGroupId());
        rootProject.setVersion(model.getVersion());
        rootProject.setPomPath(pomPath.toString());
        currentProject = rootProject;
        addModulesProject(model);
        rootProject.setModules(mavenProjects);

        rootNode = new DefaultMutableTreeNode(rootProject);
        treeModel = new DefaultTreeModel(rootNode);
        if (projectTree == null) {
            projectTree = new JTree(treeModel);
            // Add selection listener.
            projectTree.addTreeSelectionListener(e -> {
                // Get the currently selected path.
                TreePath selectedPath = projectTree.getSelectionPath();
                if (selectedPath != null) {
                    // Get the selected node.
                    Object selectedNode = selectedPath.getLastPathComponent();
                    if (selectedNode instanceof DefaultMutableTreeNode) {
                        Object userObject = ((DefaultMutableTreeNode) selectedNode).getUserObject();
                        if (userObject instanceof MavenProject) {
                            currentProject = (MavenProject)userObject;

                            refreshDependencies();
                            System.out.println("selected:"+currentProject.getArtifactId());
                        }
                    }
                }
            });

            // Set the tree renderer.
            Class<?> nativeClass = this.getClass();
            Icon m2Icon = IconUtil.getIcon(nativeClass,"/icons/m2.png");
            Icon closeFolder = IconUtil.getIcon(nativeClass,"/icons/folder_close.png");
            Icon file = IconUtil.getIcon(nativeClass,"/icons/file.png");
            Icon folderIcon = IconUtil.createOverlayIcon(closeFolder, m2Icon, 0.5, 9, 0);
            Icon fileIcon = IconUtil.createOverlayIcon(file, m2Icon, 0.5, 9, 0);
            projectTree.setCellRenderer(new MavenTreeCellRenderer(folderIcon, fileIcon));
        } else {
            projectTree.setModel(treeModel);
        }
        updateProjectTree();
    }

    public String getCurrentProjectRoot() {
        String root = platform.getRoot();
        if (currentProject != null && !currentProject.isRoot()) {
            Path rootPath = Paths.get(root);
            return rootPath.resolve(currentProject.getArtifactId()).toString();
        } else {
            return root;
        }
    }

    private void addModulesProject(Model model) {
        mavenProjects = new ArrayList<>();
        List<String> modules = model.getModules();
        Collections.sort(modules);
        for (String mName : modules) {
            MavenProject mavenProject = new MavenProject(mName);
            mavenProject.setPomPath(mName + "/pom.xml");
            mavenProjects.add(mavenProject);
        }
    }

    /**
     * Create a tab at the bottom of the panel.
     */
    private void createLifecycleTabs() {
        lifecycleTabbedPane = new JTabbedPane();

        // Lifecycle tab.
        JPanel lifecyclePanel = createLifecyclePanel();
        lifecycleTabbedPane.addTab("Lifecycle", lifecyclePanel);

        // Dependencies tab.
        JPanel dependenciesPanel = createDependenciesPanel();
        lifecycleTabbedPane.addTab("Dependencies", dependenciesPanel);
    }

    /**
     * 创建生命周期面板。
     * @return 生命周期面板。
     */
    private JPanel createLifecyclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultMutableTreeNode lifecycle = new DefaultMutableTreeNode("Lifecycle");
        String[] phases = {
                "clean", "validate", "compile", "test", "package",
                "verify", "install", "site", "deploy"
        };

        for (String phase : phases) {
            lifecycle.add(new DefaultMutableTreeNode(phase));
        }

        JTree lifecycleTree = new JTree(lifecycle);
        lifecycleTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selRow = lifecycleTree.getRowForLocation(e.getX(), e.getY());
                if (selRow != -1 && e.getClickCount() == 2) {
                    TreePath path = lifecycleTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        executeLifecycleCmd(path.getLastPathComponent().toString());
                    }
                }
            }
        });

        panel.add(new JScrollPane(lifecycleTree), BorderLayout.CENTER);

        // Set the tree renderer.
        Class<?> nativeClass = this.getClass();
        Icon closeFolder = IconUtil.getIcon(nativeClass,"/icons/folder_close.png");
        Icon fileIcon = IconUtil.getIcon(nativeClass,"/icons/m_cmd.png");
        Icon folderIcon = IconUtil.createOverlayIcon(closeFolder, fileIcon, 0.6, 9, 0);
        lifecycleTree.setCellRenderer(new MavenTreeCellRenderer(folderIcon, fileIcon));
        return panel;
    }

    /**
     * Execute the Lifecycle operation command.
     * @param cmd operation command.
     */
    private void executeLifecycleCmd(String cmd) {
        String root = platform.getRoot();
        if (root.isEmpty()) {
            System.err.println(cmd+"Command failed. No open project was found.");
            return;
        }
        Path pomPath = getPomPath();
        System.out.println("execute:"+pomPath);
        Pom pom = new Pom(pomPath);
        pom.parseProjectInfo();
        Executor comp = new Executor(cmd, "-Dfile.encoding=" + pom.sourceEncoding);
        comp.setOutFile(pom.getOutputRoot().toFile());
        comp.init(pomPath.getParent().toString(), "", platform);
        comp.start();
    }

    /**
     * Get the path of the pom.xml file for the selected project.
     * @return The path of the pom.xml file for the selected project.
     */
    private Path getPomPath() {
        String root = platform.getRoot();
        Path pomPath;
        if (currentProject == null || currentProject.isRoot()) {
            pomPath = Paths.get(root).resolve("pom.xml");
        } else {
            pomPath = Paths.get(root).resolve(currentProject.getPomPath());
        }
        return pomPath;
    }

    /**
     * Create the dependency JAR panel.
     * @return The dependency JAR panel.
     */
    private JPanel createDependenciesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        refreshDependencies();
        // Add a right-click context menu.
        JPopupMenu depPopup = new JPopupMenu();
        JMenuItem findUsagesItem = new JMenuItem("Find Usages");
        depPopup.add(findUsagesItem);

        JMenuItem excludeItem = new JMenuItem("Exclude");
        depPopup.add(excludeItem);

        dependenciesTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    depPopup.show(dependenciesTree, e.getX(), e.getY());
                }
            }
        });

        panel.add(new JScrollPane(dependenciesTree), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Refresh the dependency panel.
     */
    public void refreshDependencies() {
        Path pomPath = getPomPath();
        if (!Files.exists(pomPath)) {
            return;
        }
        Model model = Pom.readPom(pomPath.toString());
        DefaultMutableTreeNode dependenciesRoot = new DefaultMutableTreeNode("Dependencies");
        List<String> deps = DependencyManager.getAllDependenciesAsString(model);

        for (String dep : deps) {
            dependenciesRoot.add(new DefaultMutableTreeNode(dep));
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(dependenciesRoot);
        if (dependenciesTree == null) {
            dependenciesTree = new JTree(treeModel);
        } else {
            dependenciesTree.setModel(treeModel);
            treeModel.reload();
        }

        expandAll(dependenciesTree);
    }

    /**
     * 更新工程树�?
     */
    private void updateProjectTree() {
        rootNode.removeAllChildren();
        for (MavenProject project : mavenProjects) {
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
            // 添加模块（�?�果有）
            if (project.getModules() != null) {
                for (MavenProject module : project.getModules()) {
                    projectNode.add(new DefaultMutableTreeNode(module));
                }
            }

            rootNode.add(projectNode);
        }

        treeModel.reload();
        expandAll(projectTree);
    }

    /**
     * Expand all nodes of the tree.
     * @param tree Project tree with nodes that need to be expanded.
     */
    private void expandAll(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

}
