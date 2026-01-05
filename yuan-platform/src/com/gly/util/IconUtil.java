package com.gly.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Icon processing utility class.
 */
public class IconUtil {
    /**
     * Overlay the small icon onto the bottom right corner of the base icon to return a new icon.
     * @param baseIcon Under the small icon is the base icon.
     * @param smallIcon The small icon above the base icon.
     * @return Base icon and small icon combined icon.
     */
    public static Icon createOverlayIcon(Icon baseIcon, Icon smallIcon) {
        int w = Math.max(baseIcon.getIconWidth(), smallIcon.getIconWidth());
        int h = Math.max(baseIcon.getIconHeight(), smallIcon.getIconHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        baseIcon.paintIcon(null, g, 0, 0);
        // The small icon is positioned at location x in the top-right corner of the base icon.
        int bx = baseIcon.getIconWidth() - smallIcon.getIconWidth();
        if (bx < 0) {
            bx = 0;
        }
        int by = 0;
        smallIcon.paintIcon(null, g, bx, by);
        g.dispose();
        return new ImageIcon(img);
    }

    /**
     * Overlay the scaled small icon onto the base icon with customizable position and scaling.
     * @param baseIcon The base icon.
     * @param smallIcon The small icon to overlay.
     * @param scaleFactor Scale factor for the small icon (e.g., 0.5 = 50% size).
     * @param position Position of the small icon (1-9 for grid positions, like numpad).
     *                  1=top-left,         2=top-center,       3=top-right
     *                  4=middle-left,      5=middle-center,    6=middle-right,
     *                  7=bottom-left,      8=bottom-center,    9=bottom-right,
     *
     * @param margin Margin in pixels around the small icon.
     * @return Combined icon.
     */
    public static Icon createOverlayIcon(Icon baseIcon, Icon smallIcon, double scaleFactor, int position, int margin) {
        // Parameter validation.
        if (scaleFactor <= 0) {
            scaleFactor = 1.0;
        }
        if (position < 1 || position > 9) {
            position = 9; // bottom-right
        }

        int baseWidth = baseIcon.getIconWidth();
        int baseHeight = baseIcon.getIconHeight();

        // Calculate the scaled dimensions.
        int scaledSmallWidth = (int) (smallIcon.getIconWidth() * scaleFactor);
        int scaledSmallHeight = (int) (smallIcon.getIconHeight() * scaleFactor);

        // Ensure the minimum size.
        scaledSmallWidth = Math.max(1, scaledSmallWidth);
        scaledSmallHeight = Math.max(1, scaledSmallHeight);

        // Create a composite image.
        int compositeWidth = Math.max(baseWidth, scaledSmallWidth);
        int compositeHeight = Math.max(baseHeight, scaledSmallHeight);

        BufferedImage img = new BufferedImage(compositeWidth, compositeHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // set high-quality rendering.
        setupHighQualityGraphics(g);

        // draw the base icon.
        baseIcon.paintIcon(null, g, 0, 0);

        // calculate coordinates based on position.
        int x = calculateX(baseWidth, scaledSmallWidth, position, margin);
        int y = calculateY(baseHeight, scaledSmallHeight, position, margin);

        //  draw the scaled small icon.
        drawScaledIcon(g, smallIcon, x, y, scaleFactor);

        g.dispose();

        return new ImageIcon(img);
    }

    /**
     * set high-quality rendering.
     */
    private static void setupHighQualityGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }

    /**
     * Calculate the X-coordinate of the small icon.
     */
    private static int calculateX(int baseWidth, int smallWidth, int position, int margin) {
        int x;
        switch (position) {
            case 1: case 4: case 7: // Left-aligned
                x = margin;
                break;
            case 2: case 5: case 8: // Horizontal centering
                x = (baseWidth - smallWidth) / 2;
                break;
            case 3: case 6: case 9: // Right-aligned
            default:
                x = baseWidth - smallWidth - margin;
                if (x < margin) x = margin;
                break;
        }
        return x;
    }

    /**
     * Calculate the Y-coordinate of the small icon.
     */
    private static int calculateY(int baseHeight, int smallHeight, int position, int margin) {
        int y;
        switch (position) {
            case 1: case 2: case 3: // Top-aligned

            case 4: case 5: case 6: // vertical centering
                y = (baseHeight - smallHeight) / 2;
                break;
            case 7: case 8: case 9: // Bottom-aligned
                y = baseHeight - smallHeight - margin;
                if (y < margin) y = margin;
                break;
            default:
                y = margin;
                break;
        }
        return y;
    }

    /**
     * Draw the scaled icon.
     */
    private static void drawScaledIcon(Graphics2D g, Icon icon, int x, int y, double scaleFactor) {
        if (Math.abs(scaleFactor - 1.0) < 0.001) {
            // If the scaling factor is close to 1, draw it directly.
            icon.paintIcon(null, g, x, y);
        } else {
            // Use AffineTransform for scaling.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(x, y);
            g2.scale(scaleFactor, scaleFactor);
            icon.paintIcon(null, g2, 0, 0);
            g2.dispose();
        }
    }

    /**
     * Obtain the icon resource based on the path name.
     * @param nativeClass the runtime class of this code Object.
     * @param file Pathname of the file.
     * @return ImageIcon.
     */
    public static ImageIcon getIcon(Class<?> nativeClass, String file) {
        return new ImageIcon(nativeClass.getResource(file));
    }
}
