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
 *  Maven工具窗口面板。
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
     * 初始化界面。
     */
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 创建主分割面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(300);
        mainSplitPane.setResizeWeight(0.5);

        // 创建左侧面板（项目和生命周期）
        JPanel leftPanel = new JPanel(new BorderLayout());

        // 项目树
        refreshProjectTree();
        JScrollPane treeScrollPane = new JScrollPane(projectTree);

        // 生命周期选项卡
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
     * 刷新工程目录。
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
            // 添加选中监听器
            projectTree.addTreeSelectionListener(e -> {
                // 获取当前选中的路径
                TreePath selectedPath = projectTree.getSelectionPath();
                if (selectedPath != null) {
                    // 获取选中的节点
                    Object selectedNode = selectedPath.getLastPathComponent();
                    if (selectedNode instanceof DefaultMutableTreeNode) {
                        Object userObject = ((DefaultMutableTreeNode) selectedNode).getUserObject();
                        if (userObject instanceof MavenProject) {
                            currentProject = (MavenProject)userObject;

                            refreshDependencies();
                            System.out.println("选中了:"+currentProject.getArtifactId());
                        }
                    }
                }
            });
            // 设置树渲染器
            projectTree.setCellRenderer(new MavenTreeCellRenderer());
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
        Collections.sort(modules); //排序
        for (String mName : modules) {
            MavenProject mavenProject = new MavenProject(mName);
            mavenProject.setPomPath(mName + "/pom.xml");
            mavenProjects.add(mavenProject);
        }
    }

    /**
     * 创建生命周期Tab。
     */
    private void createLifecycleTabs() {
        lifecycleTabbedPane = new JTabbedPane();

        // 生命周期选项卡
        JPanel lifecyclePanel = createLifecyclePanel();
        lifecycleTabbedPane.addTab("Lifecycle", lifecyclePanel);

        // Dependencies选项卡
        JPanel dependenciesPanel = createDependenciesPanel();
        lifecycleTabbedPane.addTab("Dependencies", dependenciesPanel);
    }

    /**
     * 创建生命周期面板。
     * @return 生命周期面板。
     */
    private JPanel createLifecyclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        // 生命周期树
        DefaultMutableTreeNode lifecycle = new DefaultMutableTreeNode("Lifecycle");
        // 示例依赖
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
        return panel;
    }

    /**
     * 执行生命周期操作。
     * @param cmd 操作命令。
     */
    private void executeLifecycleCmd(String cmd) {
        String root = platform.getRoot();
        if (root.isEmpty()) {
            System.err.println(cmd+"命令失败,没有找到打开的项目.");
            return;
        }
        Path pomPath = getPomPath();
        System.out.println("执行:"+pomPath);
        Pom pom = new Pom(pomPath);
        pom.parseProjectInfo();
        Executor comp = new Executor(cmd, "-Dfile.encoding=" + pom.sourceEncoding);
        comp.setOutFile(pom.getOutputRoot().toFile());
        comp.init(pomPath.getParent().toString(), "", platform);
        comp.start();
    }

    /**
     * 获取选中工程的pom.xml路径。
     * @return 选中工程的pom.xml路径。
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
     * 创建依赖面板。
     * @return 依赖面板。
     */
    private JPanel createDependenciesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        refreshDependencies();
        // 添加右键菜单
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
     * 刷新依赖面板。
     */
    public void refreshDependencies() {
        Path pomPath = getPomPath();
        if (!Files.exists(pomPath)) {
            return;
        }
        Model model = Pom.readPom(pomPath.toString());
        // 依赖树
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
     * 更新工程树。
     */
    private void updateProjectTree() {
        rootNode.removeAllChildren();
        for (MavenProject project : mavenProjects) {
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
            // 添加模块（如果有）
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
     * 展开树。
     * @param tree 树控件。
     */
    private void expandAll(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

}
