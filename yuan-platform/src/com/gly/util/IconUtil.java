package com.gly.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
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
}
