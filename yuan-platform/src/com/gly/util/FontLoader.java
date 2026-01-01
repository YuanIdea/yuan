package com.gly.util;

import java.awt.*;
import java.io.InputStream;

public class FontLoader {

    public static Font loadSansSerif(int size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, size); // 回退字体
    }

    public static Font loadMonoSpaced(int size) {
        return new Font(Font.MONOSPACED, Font.PLAIN, size); // 回退字体
    }

    public static void loadJetBrainsMonoFamily() {
        String[] fontFiles = {
                "/fonts/JetBrainsMono-Regular.ttf",
                "/fonts/JetBrainsMono-Bold.ttf",
                "/fonts/JetBrainsMono-Italic.ttf",
                "/fonts/JetBrainsMono-BoldItalic.ttf",
        };

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String path : fontFiles) {
            try (InputStream is = FontLoader.class.getResourceAsStream(path)) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, is);
                ge.registerFont(font); // 注册所有变体
            } catch (Exception e) {
                System.err.println("加载字体失败: " + path);
                e.printStackTrace();
            }
        }
    }

    public static Font loadFont(int size) {
        try {
            // 从资源文件夹加载（推荐）
            InputStream is = FontLoader.class.getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont((float) size);

            // 注册到图形环境
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            return font;
        } catch (Exception e) {
            e.printStackTrace();
            return new Font(Font.MONOSPACED, Font.PLAIN, size);// 回退到系统等宽字体
        }
    }
}
