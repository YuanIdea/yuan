package com.gly.platform.regin.tree.add;

import com.gly.util.NameValidator;

import java.io.IOException;
import java.nio.file.*;

/**
 * 目录创建器。
 */
public class DirectoryCreator {

    /**
     * 在指定父目录下创建新目录
     * @param parentPath 父目录绝对路径（必须存在且为目录）
     * @param dirName 新目录名称
     * @return 创建结果对象
     */
    public static CreateResult createDirectory(Path parentPath, String dirName) {
        try {
            // 1. 输入参数验证
            NameValidator.validateInputDirectory(parentPath, dirName);

            // 2. 构建完整路径
            Path parent = parentPath.normalize().toAbsolutePath();
            Path fullPath = parent.resolve(dirName).normalize();

            // 3. 安全验证：防止目录遍历攻击
            if (!fullPath.startsWith(parent)) {
                return CreateResult.failure("非法路径：尝试创建超出父目录范围的目录");
            }

            // 4. 创建目录（自动创建中间目录）
            Files.createDirectories(fullPath);

            // 5. 验证目录是否成功创建
            if (Files.exists(fullPath) && Files.isDirectory(fullPath)) {
                return CreateResult.success(fullPath.toString());
            }
            return CreateResult.failure("目录创建失败，未知错误");
        } catch (InvalidPathException e) {
            return CreateResult.failure(e.getReason());
        } catch (FileAlreadyExistsException e) {
            return CreateResult.failure("同名文件/目录已存在");
        } catch (AccessDeniedException e) {
            return CreateResult.failure("无权限在此位置创建目录");
        } catch (IOException e) {
            return CreateResult.failure("IO错误：" + e.getMessage());
        } catch (SecurityException e) {
            return CreateResult.failure("安全限制：" + e.getMessage());
        }
    }

}
