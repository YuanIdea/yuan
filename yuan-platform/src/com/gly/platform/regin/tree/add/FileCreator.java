package com.gly.platform.regin.tree.add;

import com.gly.log.Logger;
import com.gly.platform.regin.tree.InvalidFileNameException;
import com.gly.util.NameValidator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File creator.
 */
public class FileCreator {
    /**
     * The project's root directory.
     */
    private Path projectRoot;

    /**
     * Path name of the created file.
     */
    private Path filePath;

    public FileCreator(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    /**
     * Create a file.
     *
     * @param fileName        Name of the file to be created.
     * @param parentComponent determines the <code>Frame</code>
     *                        in which the dialog is displayed; if <code>null</code>,
     *                        or if the <code>parentComponent</code> has no
     *                        code>Frame</code>, a default <code>Frame</code> is used
     * @return Whether the file was created successfully.
     */
    public boolean createFile(String fileName, Component parentComponent) {
        try {
            // Construct the full path name based on the file name and root directory.
            filePath = projectRoot.resolve(fileName);
            if (Files.exists(filePath)) {
                JOptionPane.showMessageDialog(
                        parentComponent,
                        "文件 '" + fileName + "' 已存在！",
                        "文件存在",
                        JOptionPane.WARNING_MESSAGE
                );
                return false;
            }
            // Validate the legality of the file name.
            String info = NameValidator.validateFileName(fileName);
            if (!info.equals(NameValidator.VALID)) {
                throw new InvalidFileNameException(info);
            }

            // Create a file.
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
            JOptionPane.showMessageDialog(parentComponent, ex.getMessage(), "输入错误", JOptionPane.ERROR_MESSAGE);
        }
        return true;
    }

    /**
     * Get path name of the created file.
     *
     * @return The path name of the created file.
     */
    public File getFile() {
        if (filePath == null) {
            return null;
        } else {
            return filePath.toFile();
        }
    }
}
