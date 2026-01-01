package com.gly.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Encoding {

    public static String detectCharset(Path path) {
        // 首先检查BOM标记
        String bomEncoding = detectBOM(path);
        if (bomEncoding != null) {
            return bomEncoding;
        }

        // 2读取文件内容到字节数组
        byte[] fileContent = readFileBytes(path);
        if (fileContent == null || fileContent.length == 0) {
            return "UTF-8"; // 默认值
        }

        // 3. 使用UniversalDetector检测
        String detectedEncoding = detectWithJava(fileContent);

        // 4. 结果后处理
        return postProcessDetection(fileContent, detectedEncoding);
    }

    // 检查BOM标记（字节顺序标记）
    private static String detectBOM(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            byte[] bom = new byte[4];
            int bytesRead = is.read(bom);

            if (bytesRead >= 3 &&
                    bom[0] == (byte) 0xEF &&
                    bom[1] == (byte) 0xBB &&
                    bom[2] == (byte) 0xBF) {
                return "UTF-8"; // UTF-8 BOM
            }
            if (bytesRead >= 4 &&
                    bom[0] == (byte) 0x00 &&
                    bom[1] == (byte) 0x00 &&
                    bom[2] == (byte) 0xFE &&
                    bom[3] == (byte) 0xFF) {
                return "UTF-32BE";
            }
        } catch (IOException e) {
            // 忽略错误，继续其他检测方式
        }
        return null;
    }

    // 读取文件内容到字节数组
    private static byte[] readFileBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 使用Java内置的简单检测（实际还是需要启发式）
     */
    public static String detectWithJava(byte[] content) {
        // 方法1：尝试常见编码，选择第一个成功的
        for (String encoding : new String[]{"UTF-8", "GBK", "ISO-8859-1"}) {
            try {
                String decoded = new String(content, encoding);
                // 简单验证：检查是否有过多替换字符
                if (!hasTooManyReplacementChars(decoded)) {
                    return encoding;
                }
            } catch (Exception e) {
                // 继续尝试下一个编码
            }
        }

        // 方法2：使用系统默认编码（不推荐，但简单）
        return Charset.defaultCharset().name();
    }

    private static boolean hasTooManyReplacementChars(String str) {
        if (str.length() == 0) return false;

        int checkLen = Math.min(str.length(), 200);
        int replacementCount = 0;

        for (int i = 0; i < checkLen; i++) {
            if (str.charAt(i) == '\uFFFD') {
                replacementCount++;
            }
        }
        // 如果超过10%是替换字符，认为编码错误
        return replacementCount > checkLen / 10;
    }

    // 检测结果后处理
    private static String postProcessDetection(byte[] content, String detectedEncoding) {
        // 优先验证UTF-8（严格模式）
        if (isValidUTF8(content)) {
            return "UTF-8";
        }
        // 最后回退到检测结果或默认值
        return detectedEncoding != null ? detectedEncoding : "UTF-8";
    }

    // 严格验证UTF-8有效性
    private static boolean isValidUTF8(byte[] data) {
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            decoder.decode(buffer);
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }

    public static String convertToUTF8(String content, String charset) {
        try {
            byte[] bytes = content.getBytes(charset);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}