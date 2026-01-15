package com.gly.util;

public class AnsiUtil {
    /**
     * ANSI sequence removal method.
     */
    public static String removeAnsiEscapeCodes(String input) {
        if (input == null) return "";

        // Match all ANSI control sequences.
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '\033' || c == '\u001b') {
                // Locate the ESC character, then skip until the command character is found.
                i++;
                if (i < input.length() && input.charAt(i) == '[') {
                    i++;
                    // Skip all parameters and intermediate characters until the command character is found.
                    while (i < input.length() &&
                            !Character.isLetter(input.charAt(i))) {
                        i++;
                    }
                    if (i < input.length()) {
                        i++; // Skip the command character.
                    }
                }
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }
}
