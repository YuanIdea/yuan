package com.gly.platform.regin.tree.add;

import com.gly.log.Logger;
import com.gly.platform.regin.tree.InvalidFileNameException;
import com.gly.util.NameValidator;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileCreator {
    /** 项目根目录  */
    private Path projectRoot;

    private Path filePath;

    public FileCreator(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public void createFile(String fileName, JDialog dialog) {
        try {
            // 验证输入
            String info = NameValidator.validateFileName(fileName);
            if (!info.equals(NameValidator.VALID)) {
                throw new InvalidFileNameException(info);
            }
            // 构建完整路径
            filePath = projectRoot.resolve(fileName);
            // 创建文件
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Files.createFile(filePath);
                    return null;
                }

                @Override
                protected void done() {
                    Logger.info(filePath + "创建成功");
                }
            }.execute();

        } catch (InvalidFileNameException ex) {
            JOptionPane.showMessageDialog(dialog, ex.getMessage(), "输入错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public File getFile() {
        if (filePath == null) {
            return null;
        } else {
            return filePath.toFile();
        }
    }
}
