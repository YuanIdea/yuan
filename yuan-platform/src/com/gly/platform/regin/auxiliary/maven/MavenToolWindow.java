package com.gly.platform.regin.auxiliary.maven;

import com.gly.i18n.I18n;
import com.gly.platform.app.Platform;
import com.gly.util.IconUtil;
import org.apache.maven.model.Model;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Maven Tool Window panel.
 */
public class MavenToolWindow extends JPanel {
    private JTree projectTree;
    private MavenProject currentProject;
    private JTree dependenciesTree;
    private DefaultMutableTreeNode rootNode;
    private JTabbedPane lifecycleTabbedPane;
    private final Platform platform;

    public MavenToolWindow() {
        platform = Platform.getInstance();
        initUI();
    }

    /**
     * Initialize the interface.
     */
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create the main split panel.
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(300);
        mainSplitPane.setResizeWeight(0.5);

        JPanel leftPanel = new JPanel(new BorderLayout());

        // project tree.
        refreshProjectTree();
        JScrollPane treeScrollPane = new JScrollPane(projectTree);

        // Create operation tabs.
        createOperationTabs();

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

        currentProject = new MavenProject(model, true);
        currentProject.setPomPath(pomPath.toString());
        DefaultTreeModel projectTreeModel = getProjectTreeModel(currentProject);
        if (projectTree == null) {
            projectTree = new JTree(projectTreeModel);
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
                            currentProject = (MavenProject) userObject;
                            refreshDependencies();
                            System.out.println("selected:" + currentProject.getArtifactId());
                        }
                    }
                }
            });

            // Set the tree renderer.
            Class<?> nativeClass = this.getClass();
            Icon m2Icon = IconUtil.getIcon(nativeClass, "/icons/m2.png");
            Icon closeFolder = IconUtil.getIcon(nativeClass, "/icons/folder_close.png");
            Icon file = IconUtil.getIcon(nativeClass, "/icons/file.png");
            Icon folderIcon = IconUtil.createOverlayIcon(closeFolder, m2Icon, 0.5f, 9, 0);
            Icon fileIcon = IconUtil.createOverlayIcon(file, m2Icon, 0.5f, 9, 0);
            projectTree.setCellRenderer(new MavenTreeCellRenderer(folderIcon, fileIcon));
        } else {
            projectTree.setModel(projectTreeModel);
        }
        expandAll(projectTree);
    }

    /**
     * Get the model of the current project.
     *
     * @param rootProject The root of current project.
     * @return he model of the current project.
     */
    private DefaultTreeModel getProjectTreeModel(MavenProject rootProject) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootProject);
        List<MavenProject> mavenProjects = rootProject.getModules();
        for (MavenProject project : mavenProjects) {
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project);
            // If there are modules, add them to the project tree.
            List<MavenProject> modules = project.getModules();
            if (modules != null) {
                for (MavenProject module : modules) {
                    projectNode.add(new DefaultMutableTreeNode(module));
                }
            }
            rootNode.add(projectNode);
        }
        return new DefaultTreeModel(rootNode);
    }

    /**
     * Get the root directory of the current project.
     *
     * @return The root directory of the current project.
     */
    public String getCurrentProjectRoot() {
        String root = platform.getRoot();
        if (currentProject != null && !currentProject.isRoot()) {
            Path rootPath = Paths.get(root);
            return rootPath.resolve(currentProject.getArtifactId()).toString();
        } else {
            return root;
        }
    }

    /**
     * Create operation tabs.
     */
    private void createOperationTabs() {
        lifecycleTabbedPane = new JTabbedPane();

        // Lifecycle tab.
        JPanel lifecyclePanel = createLifecyclePanel();
        lifecycleTabbedPane.addTab(I18n.get("maven.lifecycle"), lifecyclePanel);

        // Dependencies tab.
        JPanel dependenciesPanel = createDependenciesPanel();
        lifecycleTabbedPane.addTab(I18n.get("maven.dependencies"), dependenciesPanel);
    }

    /**
     * Create a lifecycle panel.
     *
     * @return Lifecycle panel.
     */
    private JPanel createLifecyclePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultMutableTreeNode lifecycle = new DefaultMutableTreeNode(I18n.get("maven.lifecycle"));
        String[] phases = {
                "clean", "validate", "compile", "test", "package",
                "verify", "install", "site", "deploy"
        };

        for (String phase : phases) {
            lifecycle.add(new DefaultMutableTreeNode(phase));
        }

        JTree lifecycleTree = getLifecycleTree(lifecycle);
        panel.add(new JScrollPane(lifecycleTree), BorderLayout.CENTER);
        lifecycleTree.setCellRenderer(simpleMavenCellRender(0.6f, "/icons/m_cmd.png"));
        return panel;
    }

    private JTree getLifecycleTree(DefaultMutableTreeNode lifecycle) {
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
        return lifecycleTree;
    }

    /**
     * Execute the Lifecycle operation command.
     *
     * @param cmd operation command.
     */
    private void executeLifecycleCmd(String cmd) {
        String root = platform.getRoot();
        if (root.isEmpty()) {
            System.err.println(cmd + "Command failed. No open project was found.");
            return;
        }
        Path pomPath = getPomPath();
        System.out.println("execute:" + pomPath);
        Pom pom = new Pom(pomPath);
        pom.parseProjectInfo();
        Executor comp = new Executor(cmd, "-Dfile.encoding=" + pom.sourceEncoding);
        comp.setOutDirectory(pom.getOutputRoot().toFile());
        comp.init(pomPath.getParent().toString(), "", platform);
        comp.start();
    }

    /**
     * Get the path of the pom.xml file for the selected project.
     *
     * @return The path of the pom.xml file for the selected project.
     */
    private Path getPomPath() {
        Path path = Paths.get(platform.getRoot());
        Path pomPath;
        if (currentProject == null || currentProject.isRoot()) {
            pomPath = path.resolve("pom.xml");
        } else {
            pomPath = path.resolve(currentProject.getPomPath());
        }
        return pomPath;
    }

    /**
     * Create the dependency JAR panel.
     *
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
        DefaultMutableTreeNode dependenciesRoot = new DefaultMutableTreeNode(I18n.get("maven.dependencies"));
        List<String> deps = DependencyManager.getAllDependenciesAsString(model);

        for (String dep : deps) {
            dependenciesRoot.add(new DefaultMutableTreeNode(dep));
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(dependenciesRoot);
        if (dependenciesTree == null) {
            dependenciesTree = new JTree(treeModel);
            dependenciesTree.setCellRenderer(simpleMavenCellRender(0.7f, "/icons/depend.png"));
        } else {
            dependenciesTree.setModel(treeModel);
            treeModel.reload();
        }

        expandAll(dependenciesTree);
    }

    /**
     * Generate Simple Renderer includes file icons and a new folder icon
     * synthesized from the file and a standard folder.
     *
     * @param scaleFactor Scale factor for the small icon (e.g., 0.5 = 50% size).
     * @param smallIcon   Small icon pathname.
     * @return MavenTreeCellRenderer
     */
    private MavenTreeCellRenderer simpleMavenCellRender(float scaleFactor, String smallIcon) {
        Class<?> nativeClass = this.getClass();
        Icon closeFolder = IconUtil.getIcon(nativeClass, "/icons/folder_close.png");
        Icon fileIcon = IconUtil.getIcon(nativeClass, smallIcon);
        Icon folderIcon = IconUtil.createOverlayIcon(closeFolder, fileIcon, scaleFactor, 9, 0);
        return new MavenTreeCellRenderer(folderIcon, fileIcon);
    }

    /**
     * Expand all nodes of the tree.
     *
     * @param tree Project tree with nodes that need to be expanded.
     */
    private void expandAll(JTree tree) {
        if (tree != null) {
            int count = tree.getRowCount();
            for (int i = 0; i < count; ++i) {
                tree.expandRow(i);
            }
        }
    }
}
