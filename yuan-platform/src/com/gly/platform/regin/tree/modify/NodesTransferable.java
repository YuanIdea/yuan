package com.gly.platform.regin.tree.modify;

import com.gly.log.Logger;

import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * 自定义Transferable实现
 */
public class NodesTransferable implements Transferable {
    static final DataFlavor TREE_PATH_FLAVOR = getDataFlavor();
    private final TreePath[] paths;

    private static DataFlavor getDataFlavor() {
        try{
            return new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException e) {
            Logger.error("创建DataFlavor失败:" + e.getMessage());
        }
        return null;
    }

    NodesTransferable(TreePath[] paths) {
        this.paths = paths;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{TREE_PATH_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(TREE_PATH_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
            return paths;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
