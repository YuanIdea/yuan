package com.gly.platform.regin.tree.find;

import com.gly.log.Logger;
import com.gly.platform.regin.tree.FileTreeNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Tree selection utility class.
 */
public class Select {

    /**
     * Get the currently selected tree node
     *
     * @param fileTree File tree component
     * @return Selected node; returns an empty node if none is selected.
     */
    public static FileTreeNode getSelectedTreeNode(JTree fileTree) {
        if (fileTree == null) {
            Logger.warn("Root directory does not exist.");
            return null;
        }
        TreePath selectionPath = fileTree.getSelectionPath();
        if (selectionPath == null)
            return null;
        return (FileTreeNode) selectionPath.getLastPathComponent();
    }

    /**
     * Get all selected tree nodes (supports multiple selection)
     *
     * @param fileTree File tree component
     * @return List of selected nodes; returns an empty list if none are selected
     */
    public static List<FileTreeNode> getSelectedTreeNodes(JTree fileTree) {
        List<FileTreeNode> selectedNodes = new ArrayList<>();

        // Get all selected paths.
        TreePath[] selectionPaths = fileTree.getSelectionPaths();
        if (selectionPaths == null) {
            return selectedNodes;
        }

        // Traverse all selected paths.
        for (TreePath path : selectionPaths) {
            Object lastComponent = path.getLastPathComponent();
            if (lastComponent instanceof FileTreeNode) {
                selectedNodes.add((FileTreeNode) lastComponent);
            }
        }

        return selectedNodes;
    }

    /**
     * Get the selected path.
     *
     * @param tree File tree component.
     * @return The selected path.
     */
    public static Path getSelectedPath(JTree tree) {
        FileTreeNode fileNode = getSelectedTreeNode(tree);
        if (fileNode != null) {
            // Directly return the path corresponding to the file.
            return fileNode.getFile().toPath();
        } else {
            return null;
        }
    }

    /**
     * Get the selected path.
     *
     * @param tree File tree component.
     * @return The selected path.
     */
    public static List<File> getSelectedFiles(JTree tree) {
        List<FileTreeNode> selectedNodes = getSelectedTreeNodes(tree);
        // Directly return the path corresponding to the file.
        List<File> paths = new LinkedList<>();
        for (FileTreeNode fileNode : selectedNodes) {
            paths.add(fileNode.getFile());
        }
        return paths;
    }
}
