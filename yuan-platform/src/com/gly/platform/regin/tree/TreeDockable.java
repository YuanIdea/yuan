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
 * 工程目录资源管理的树形停靠窗口。
 *
 * @author Guoliang Yang
 */
public class TreeDockable extends DefaultSingleCDockable {
    // 树形资源管理器
    private JTree tree;

    // 右键处理
    private RightMenu rightMenu;

    // 根目录
    private String root;

    // 可排序树模型
    private SortedTreeModel model;

    // 树的状态
    private State state;

    // 当前剪切板文件
    private List<File> currentClipboardFileList;

    // 是否为剪切操作
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
     * 刷新选中功能。
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
     * 刷新工程根目录。
     *
     * @param root 工程根路径。
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
            if (change) { // 根目录发生变化。
                rootTreeNode = new FileTreeNode(new File(root), true);
                TreeInitializer.init(rootTreeNode); // 将File数组中的元素增加到节点上
                model = new SortedTreeModel(rootTreeNode, new NodeComparator());// 创建排序模型
                if (tree == null) {
                    createTree(model);  // 创建树和目录
                } else {
                    tree.setModel(model); // 变更到新目录
                }
            } else { // 刷新当前目录
                rootTreeNode = (FileTreeNode) model.getRoot();
                refreshNode(rootTreeNode);
            }
        }
    }

    /**
     * 创建工程配置文件目录。
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
     * 创建树形控件。
     *
     * @param rootNode 默认根节点。
     */
    private void createTree(SortedTreeModel rootNode) {
        tree = new JTree(rootNode);// 使用节点创建树控件
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION); // 设置多选模式
        tree.setEditable(false);
        tree.setCellRenderer(new FileTreeCellRenderer());
        tree.setCellEditor(new FileTreeCellEditor(tree));
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doubleHandler(e); // 双击事件。
                rightMenu.rightHandler(e);  // 右击事件。
            }
        });
        rightMenu.createRightMenu(); // 创建右键菜单
        getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);

        state.init(tree); // 展开状态初始化指定树。

        // 节点拖动功能。
        FileTreeDragHandler dragHandler = new FileTreeDragHandler(tree);
        dragHandler.enableDragAndDrop();
    }

    /**
     * 创建文件节点。
     *
     * @param title 创建标题。
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
            node.createFile(nfc.getFileName(), null);
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
     * 创建目录节点。
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
     * 添加新文件节点。
     *
     * @param file        文件名称。
     * @param isDirectory 是否是目录。
     */
    private void addNewNode(File file, boolean isDirectory) {
        FileTreeNode parent = (FileTreeNode) tree.getLastSelectedPathComponent();
        if (parent == null) {
            parent = (FileTreeNode) model.getRoot();
        } else if (parent.isFile()) {
            parent = (FileTreeNode) parent.getParent();
        }

        FileTreeNode newNode = new FileTreeNode(file, isDirectory);// 创建新节点
        model.insertNodeInto(newNode, parent, parent.getChildCount());// 通过模型插入节点
        tree.expandPath(new TreePath(parent.getPath()));// 自动展开父节点（可选）
    }

    /**
     * 点击为双击时处理。
     *
     * @param e 点击事件。
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
     * 开文件。
     *
     * @param isDesign 是否以设计模式打开。
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
     * 从磁盘中打开文件。
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
     * 节点重命名
     */
    void renameNode() {
        SwingUtilities.invokeLater(() -> {
            final FileTreeNode fileNode = Select.getSelectedTreeNode(tree);
            tree.setEditable(true);// 临时启用编辑功能
            tree.startEditingAtPath(tree.getSelectionPath());// 启动编辑
            // 添加编辑器监听器，在编辑完成后禁用编辑功能
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
     * 刷新所有子目录和文件。
     *
     * @param foldNode 需要重新加载的当前目录节点。
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
                TreeInitializer.init(reloadNode); // 将File数组中的元素增加到节点上
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
     * 复制
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
     * 剪切
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
     * 粘贴
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
     * 粘贴一个文件到目标目录。
     *
     * @param currentClipboardFile 当前文件。
     * @param targetDir            目标目录。
     */
    private void pasteOne(File currentClipboardFile, Path targetDir) {
        try {
            Path destination = targetDir.resolve(currentClipboardFile.getName());
            if (isCutOperation) {
                // 剪切操作（移动文件）
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
     * 生成带编号的文件路径
     *
     * @param originalPath 原始文件路径。
     * @return 用编号修正后的路径。
     */
    private Path getNumberedPath(Path originalPath) {
        if (!Files.exists(originalPath)) {
            return originalPath;
        }

        String fileName = originalPath.getFileName().toString();
        String baseName = fileName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');

        // 分离文件名和扩展名
        if (dotIndex > 0) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        Path parent = originalPath.getParent();
        int count = 2;
        // 寻找可用的编号
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
        // 检查目标路径是否在源路径内（防止无限复制）
        if (target.startsWith(source)) {
            Logger.error("目标文件夹是源文件夹的子文件夹，操作失败: " + source + " -> " + target);
            return false;
        }

        // 使用队列实现非递归复制
        Queue<Path[]> copyQueue = new LinkedList<>();
        copyQueue.add(new Path[]{source, target});

        while (!copyQueue.isEmpty()) {
            Path[] paths = copyQueue.poll();
            Path currentSource = paths[0];
            Path currentTarget = paths[1];

            // 如果是目录，创建目标目录并添加子项到队列
            if (Files.isDirectory(currentSource)) {
                if (!Files.exists(currentTarget)) {
                    Files.createDirectories(currentTarget);
                }

                // 遍历目录内容
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentSource)) {
                    for (Path entry : stream) {
                        Path newTarget = currentTarget.resolve(entry.getFileName());
                        copyQueue.add(new Path[]{entry, newTarget});
                    }
                }
            }
            // 如果是文件，直接复制
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