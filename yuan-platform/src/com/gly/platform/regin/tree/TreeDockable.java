package com.gly.platform.regin.tree;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import com.gly.event.AddFileEvent;
import com.gly.event.GlobalBus;
import com.gly.event.RefreshEvent;
import com.gly.event.Subscribe;
import com.gly.event.page.AddEvent;
import com.gly.log.Logger;
import com.gly.platform.app.Platform;
import com.gly.platform.app.ProjectType;
import com.gly.platform.app.YuanConfig;
import com.gly.event.page.PageInfo;
import com.gly.platform.regin.tree.add.*;
import com.gly.platform.regin.tree.find.FileExplorerUtil;
import com.gly.platform.regin.tree.find.Select;
import com.gly.platform.regin.tree.find.TreeSearch;
import com.gly.platform.regin.tree.modify.*;
import com.gly.run.Config;
import com.gly.run.PluginManager;
import com.gly.util.Resources;

import java.util.LinkedList;
import java.util.List;
import java.util.*;

/**
 * Tree dock window for project directory resource management.
 *
 * @author Guoliang Yang
 */
public class TreeDockable extends DefaultSingleCDockable {
    // Tree resource manager.
    private JTree tree;

    // Right-click functionality.
    private RightMenu rightMenu;

    // Root directory.
    private String root;

    // Sorted tree model.
    private SortedTreeModel model;

    // Tree state.
    private State state;

    // Current clipboard file.
    private List<File> currentClipboardFileList;

    // Is it a cut operation?
    private boolean isCutOperation;

    public TreeDockable() {
        super("TreeDockable");
        GlobalBus.register(this); // 注册到事件总线
        setCloseable(true);
        setMinimizable(true);
        setMaximizable(true);
        setExternalizable(true);
        setTitleText("工程");
        setTitleIcon(Resources.getIcon("dockable.hierarchy"));
        root = "";
        state = new State();
        rightMenu = new RightMenu(this);
    }

    /**
     * Refresh selected nodes.
     */
    void refresh() {
        final FileTreeNode fileNode = Select.getSelectedTreeNode(tree);
        if (fileNode != null) {
            if (fileNode.isRoot()) {
                refreshRoot(root);
            } else {
                refreshNode(fileNode);
            }
        }
    }

    /**
     * Refresh the project's root directory.
     *
     * @param root Root directory。
     */
    public void refreshRoot(String root) {
        boolean change = !this.root.equals(root);
        this.root = root;
        generateProjectConfig();
        if (!root.isEmpty()) {
            ProjectType.readProjectType(root);
        }
        GlobalBus.dispatch(new RefreshEvent(change));

        if (!root.isEmpty()) {
            if (ProjectType.isModel()) {
                PluginManager pluginManager = Platform.getInstance().getPluginManager();
                if (pluginManager != null) {
                    pluginManager.register("com.gly.PluginEntry");
                }
            }
            FileTreeNode rootTreeNode;
            if (change) { // Root directory has changed.
                rootTreeNode = new FileTreeNode(new File(root), true);
                TreeInitializer.init(rootTreeNode); // Add elements from the File array to the nodes.
                model = new SortedTreeModel(rootTreeNode, new NodeComparator());// Create a sorting model.
                if (tree == null) {
                    createTree(model);  // Create tree and directory.
                } else {
                    tree.setModel(model); // Change to new directory.
                }
            } else { // Refresh current directory.
                rootTreeNode = (FileTreeNode) model.getRoot();
                refreshNode(rootTreeNode);
            }
        }
    }

    /**
     * Create project configuration file directory.
     */
    private void generateProjectConfig() {
        if (root.isEmpty()) {
            return;
        }

        Path projectPathName = Paths.get(root).resolve(YuanConfig.PROJECT_CONFIG);
        if (!Files.exists(projectPathName)) {
            Path pom = Paths.get(root).resolve("pom.xml");
            if (Files.exists(pom)) {
                Path projectPath = Paths.get(root).resolve(YuanConfig.YUAN_PROJECT);
                if (!Files.exists(projectPath)) {
                    try {
                        Files.createDirectories(projectPath);
                    } catch (Exception e) {
                        Logger.error("工程配置创建失败: " + e.getMessage());
                    }
                }
                Config.writeMavenXml(projectPathName.toFile());
            }
        }
    }

    /**
     * Create tree control.
     *
     * @param rootNode Default root node.
     */
    private void createTree(SortedTreeModel rootNode) {
        tree = new JTree(rootNode);// Use nodes to create tree control.
        // Set tree nodes to multiple selection mode.
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setEditable(false);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.setCellEditor(new FileTreeCellEditor(tree));
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doubleHandler(e); // Double-click event.
                rightMenu.rightHandler(e);  // Right-click event.
            }
        });
        rightMenu.createRightMenu(); // Create a right-click context menu.
        getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);

        state.init(tree); // Initialize the tree control in an expanded state.

        // 节点拖动功能。
        FileTreeDragHandler dragHandler = new FileTreeDragHandler(tree);
        dragHandler.enableDragAndDrop();
    }

    /**
     * Create file nodes.
     *
     * @param title Create title.
     */
    public void newFile(String title) {
        final Path selectFolder = getSelectedFolder();
        if (selectFolder == null) {
            Logger.warn("创建失败");
            return;
        }
        CreateConfig nfc = new CreateConfig(title);
        nfc.showNewFileDialog(null);
        if (nfc.isOk()) {
            FileCreator node = new FileCreator(selectFolder);
            boolean success = node.createFile(nfc.getFileName(), null);
            if (success) {
                File file = node.getFile();
                if (file != null) {
                    PageInfo pageInfo = new PageInfo(file);
                    addNewNode(file, false);
                    GlobalBus.dispatch(new AddEvent(pageInfo));
                } else {
                    Logger.error("创建文件失败：" + nfc.getFileName());
                }
            }
        }
    }

    void newJava() {
        final Path selectFolder = getSelectedFolder();
        if (selectFolder == null) {
            Logger.warn("创建失败");
            return;
        }
        CreateJavaConfig nfc = new CreateJavaConfig("新建Java类型");
        nfc.showNewFileDialog(null);
        if (nfc.isOk()) {
            FileCreator node = new FileCreator(selectFolder);
            String fileName = nfc.getFileName();
            if (!fileName.contains(".java")) {
                fileName += ".java";
            }
            node.createFile(fileName, null);
            File file = node.getFile();
            if (file != null) {
                PageInfo pageInfo = new PageInfo(file);
                pageInfo.setFileType(nfc.getFileType());
                addNewNode(file, false);
                GlobalBus.dispatch(new AddEvent(pageInfo));
            } else {
                Logger.error("创建文件失败：" + nfc.getFileName());
            }
        }
    }

    /**
     * Create directory nodes.
     */
    void newFolder() {
        final Path selectFolder = getSelectedFolder();
        CreateConfig nfc = new CreateConfig("新建目录");
        nfc.showNewFileDialog(null);
        if (nfc.isOk()) {
            CreateResult result = DirectoryCreator.createDirectory(selectFolder, nfc.getFileName());
            if (result.isSuccess()) {
                FileCreator node = new FileCreator(selectFolder);
                node.createFile(nfc.getFileName(), null);
                File folder = node.getFile();
                if (folder != null) {
                    addNewNode(folder, true);
                    Logger.info("目录创建成功：" + result.getPath());
                } else {
                    Logger.error("创建失败：" + nfc.getFileName());
                }
            } else {
                Logger.error("创建失败：" + result.getError());
            }
        }
    }

    /**
     * Add new file nodes.
     *
     * @param file        File name.
     * @param isDirectory Is it a directory?
     */
    private void addNewNode(File file, boolean isDirectory) {
        FileTreeNode parent = (FileTreeNode) tree.getLastSelectedPathComponent();
        if (parent == null) {
            parent = (FileTreeNode) model.getRoot();
        } else if (parent.isFile()) {
            parent = (FileTreeNode) parent.getParent();
        }

        FileTreeNode newNode = new FileTreeNode(file, isDirectory);// Create new node.
        model.insertNodeInto(newNode, parent, parent.getChildCount());// Insert nodes through the model.
        tree.expandPath(new TreePath(parent.getPath()));// Automatically expand parent nodes.
    }

    /**
     * Handle when it is a double-click.
     *
     * @param e Click event.
     */
    private void doubleHandler(MouseEvent e) {
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        if (selRow != -1 && e.getClickCount() == 2) {
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path != null) {
                openFile(false);
            }
        }
    }

    /**
     * Open file.
     *
     * @param isDesign Whether to open in design mode.
     */
    void openFile(boolean isDesign) {
        List<FileTreeNode> selectedNodes = Select.getSelectedTreeNodes(tree);
        for (FileTreeNode selectedNode : selectedNodes) {
            if (selectedNode != null && selectedNode.isFile()) {
                File file = selectedNode.getFile();
                if (file.exists()) {
                    PageInfo pageInfo = new PageInfo(file);
                    pageInfo.setIsDesign(isDesign);
                    GlobalBus.dispatch(new AddEvent(pageInfo));
                } else {
                    Logger.error(file + "不存在");
                }
            }
        }
    }

    /**
     * Open file from disk.
     */
    void openDiskFile() {
        FileTreeNode selectedNode = Select.getSelectedTreeNode(tree);
        if (selectedNode != null) {
            try {
                FileExplorerUtil.showFileInExplorer(selectedNode.getFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Node rename.
     */
    void renameNode() {
        SwingUtilities.invokeLater(() -> {
            final FileTreeNode fileNode = Select.getSelectedTreeNode(tree);
            tree.setEditable(true);// Temporarily enable editing functionality.
            tree.startEditingAtPath(tree.getSelectionPath());// Start editing functionality.
            // Add an editor listener to disable editing functionality after editing is completed.
            tree.getCellEditor().addCellEditorListener(new CellEditorListener() {
                @Override
                public void editingStopped(ChangeEvent e) {
                    tree.setEditable(false);
                    refreshNode(fileNode);
                }

                @Override
                public void editingCanceled(ChangeEvent e) {
                    tree.setEditable(false);
                }
            });
        });
    }

    /**
     * Refresh all subdirectories and files.
     *
     * @param foldNode Current directory node that needs to be reloaded.
     */
    private void refreshNode(FileTreeNode foldNode) {
        if (foldNode != null) {
            FileTreeNode reloadNode;
            if (foldNode.isDirectory()) {
                reloadNode = foldNode;
            } else {
                reloadNode = (FileTreeNode) foldNode.getParent();
            }
            if (reloadNode != null) {
                state.saveExpansionState();
                reloadNode.removeAllChildren();
                TreeInitializer.init(reloadNode); // Add elements from the File array to the nodes.
                model.reload(reloadNode);
                state.restoreExpansionState();
            }
        }
    }

    @Subscribe
    public void reloadPath(AddFileEvent event) {
        FileTreeNode rootNode = (FileTreeNode) model.getRoot();
        String fileParent = event.getFile().getParent();
        FileTreeNode pathTreeNode = TreeSearch.findNodeByPathSorted(rootNode, fileParent);
        if (pathTreeNode != null) {
            refreshNode(pathTreeNode);
        }
    }

    /**
     * Copy.
     */
    void copy() {
        currentClipboardFileList = Select.getSelectedFiles(tree);
        isCutOperation = false;
        if (currentClipboardFileList != null) {
            String info = "已复制: " + currentClipboardFileList.get(0);
            if (currentClipboardFileList.size() > 1) {
                info += "等";
            }
            Logger.info(info);
        } else {
            Logger.info("请先选择一个文件或文件夹");
        }
    }

    /**
     * Cut.
     */
    void cut() {
        currentClipboardFileList = Select.getSelectedFiles(tree);
        isCutOperation = true;
        if (currentClipboardFileList != null) {
            String info = "已剪切: " + currentClipboardFileList.get(0);
            if (currentClipboardFileList.size() > 1) {
                info += "等";
            }
            Logger.info(info);
        } else {
            Logger.info("请先选择一个文件或文件夹");
        }
    }

    /**
     * Paste.
     */
    void paste() {
        if (currentClipboardFileList == null) {
            Logger.info("剪贴板为空");
            return;
        }

        Path targetDir = getSelectedFolder();
        if (targetDir == null || !Files.isDirectory(targetDir)) {
            Logger.info("请选择一个目标文件夹");
            return;
        }

        for (File currentClipboardFile : currentClipboardFileList) {
            pasteOne(currentClipboardFile, targetDir);
        }
        refreshRoot(root);
    }

    /**
     * Paste a file to the target directory.
     *
     * @param currentClipboardFile Current file.
     * @param targetDir            Target directory.
     */
    private void pasteOne(File currentClipboardFile, Path targetDir) {
        try {
            Path destination = targetDir.resolve(currentClipboardFile.getName());
            if (isCutOperation) {
                // Cut operation (move file).
                Files.move(currentClipboardFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                Logger.info("已移动: " + currentClipboardFile + " -> " + destination);
            } else {
                // 复制操作
                if (currentClipboardFile.isDirectory()) {
                    if (copyFolder(currentClipboardFile.toPath(), destination)) {
                        Logger.info("已复制文件夹: " + currentClipboardFile + " -> " + destination);
                    }
                } else {
                    Path numberedPath = getNumberedPath(destination);
                    Files.copy(currentClipboardFile.toPath(), numberedPath);
                    Logger.info("已复制文件: " + currentClipboardFile + " -> " + numberedPath);
                }
            }
        } catch (IOException ex) {
            Logger.error("操作失败: " + ex.getMessage());
        }
    }

    /**
     * Generate a file path with a number.
     *
     * @param originalPath Original file path.
     * @return Path corrected with numbering.
     */
    private Path getNumberedPath(Path originalPath) {
        if (!Files.exists(originalPath)) {
            return originalPath;
        }

        String fileName = originalPath.getFileName().toString();
        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');

        // Separate file name and extension.
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        Path parent = originalPath.getParent();
        int count = 2;
        // Find available numbering.
        while (true) {
            String newName = baseName + "(" + count + ")" + extension;
            Path newPath = parent.resolve(newName);

            if (!Files.exists(newPath)) {
                return newPath;
            }
            count++;
        }
    }

    private boolean copyFolder(Path source, Path target) throws IOException {
        // Check if the target path is within the source path (to prevent infinite copying).
        if (target.startsWith(source)) {
            Logger.error("目标文件夹是源文件夹的子文件夹，操作失败: " + source + " -> " + target);
            return false;
        }

        // Use a queue to implement non-recursive copying.
        Queue<Path[]> copyQueue = new LinkedList<>();
        copyQueue.add(new Path[]{source, target});

        while (!copyQueue.isEmpty()) {
            Path[] paths = copyQueue.poll();
            Path currentSource = paths[0];
            Path currentTarget = paths[1];

            // If it is a directory, create the target directory and add its sub-items to the queue.
            if (Files.isDirectory(currentSource)) {
                if (!Files.exists(currentTarget)) {
                    Files.createDirectories(currentTarget);
                }

                // Traverse the directory contents.
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentSource)) {
                    for (Path entry : stream) {
                        Path newTarget = currentTarget.resolve(entry.getFileName());
                        copyQueue.add(new Path[]{entry, newTarget});
                    }
                }
            }
            // If it is a file, copy it directly.
            else {
                Files.copy(currentSource, currentTarget, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return true;
    }

    private Path getSelectedFolder() {
        Path path = Select.getSelectedPath(tree);
        if (path != null) {
            if (Files.isDirectory(path)) {
                return path;
            } else {
                return path.getParent();
            }
        } else {
            return Paths.get(root);
        }
    }

    public String getRoot() {
        return root;
    }

    public JTree getTree() {
        return tree;
    }
}