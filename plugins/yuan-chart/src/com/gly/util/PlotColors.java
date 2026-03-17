package com.gly.util;

import java.awt.Color;
import java.util.List;
import java.util.Map;

/**
 * Provides standard color cycles and character-to-color mappings for plotting.
 * The color cycle imitates the default MATLAB R2014b+ color order and is extended
 * to a 15-color cycle.
 * The character map supports common color shortcuts (e.g., 'r' for red).
 */
public final class PlotColors {

    private PlotColors() {
        // Utility class, prevent instantiation
    }

    /**
     * A 15-color cycle list, intended to be used sequentially.
     * The first 7 colors match the default MATLAB color order; the remaining 8 are extended.
     */
    public static final List<Color> CYCLE = List.of(
            new Color(0, 114, 189), // 1. Blue (MATLAB first default color)
            new Color(217, 83, 25), // 2. Orange-red
            new Color(237, 177, 32), // 3. Bright yellow
            new Color(126, 47, 142), // 4. Purple
            new Color(119, 172, 48), // 5. Grass green
            new Color(77, 190, 238), // 6. Sky blue
            new Color(162, 20, 47), // 7. Crimson
            new Color(255, 128, 64), // 8. Orange
            new Color(166, 86, 40), // 9. Brown
            new Color(91, 155, 213), // 10. Cobalt blue
            new Color(112, 48, 160), // 11. Violet
            new Color(61, 174, 118), // 12. Emerald green
            new Color(255, 204, 153), // 13. Light orange
            new Color(147, 196, 125), // 14. Grayish green
            new Color(208, 201, 192)  // 15. Silver gray
    );

    /**
     * A map from a character to a predefined color, supporting shortcuts similar to MATLAB.
     * Supported characters: r(red), g(green), b(blue), c(cyan), m(magenta), y(yellow),
     * k(black), w(white), o(orange), p(purple)
     */
    public static final Map<Character, Color> CHAR_MAP = Map.ofEntries(
            Map.entry('r', Color.RED),
            Map.entry('g', Color.GREEN),
            Map.entry('b', Color.BLUE),
            Map.entry('c', Color.CYAN),
            Map.entry('m', Color.MAGENTA),
            Map.entry('y', Color.YELLOW),
            Map.entry('k', Color.BLACK),
            Map.entry('w', Color.WHITE),
            Map.entry('o', new Color(255, 128, 0)),    // Orange
            Map.entry('p', new Color(125, 46, 141))   // Purple
    );
}