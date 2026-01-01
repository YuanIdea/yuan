package com.gly.platform.editor;

import com.gly.event.GlobalBus;
import com.gly.event.page.AddEvent;
import com.gly.event.page.PageInfo;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Drop {
    /**
     * 启用拖放支持。
     */
    public static void setupDragAndDrop(Container panel) {
        new DropTarget(panel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                try {
                    event.acceptDrop(DnDConstants.ACTION_COPY);// 接受拖拽操作
                    Object data = event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    List<File> droppedFiles = getDropFile(data);
                    if (droppedFiles!= null && !droppedFiles.isEmpty()) {
                        for (File file : droppedFiles){
                            PageInfo pageInfo = new PageInfo(file);
                            GlobalBus.dispatch(new AddEvent(pageInfo));
                        }
                    }
                    event.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    event.dropComplete(false);
                }
            }
        });
    }

    /**
     * 获取拖放的文件列表
     * @param data 拖拽数据。
     * @return 文件列表。
     */
    private static List<File> getDropFile(Object data) {
        List<File> droppedFiles = null;
        if (data instanceof List<?>) {
            List<?> rawList = (List<?>) data;
            droppedFiles = new ArrayList<>();
            for (Object o : rawList) {
                if (o instanceof File) {
                    droppedFiles.add((File) o);
                }
            }
        }
        return droppedFiles;
    }
}
