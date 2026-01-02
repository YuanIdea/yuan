package com.gly.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Video file utility class.
 */
public class VideoFileUtil {
    // Common video file extensions.
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(Set.of(
            "mp4", "mkv", "flv", "avi", "mov", "wmv", "mpg", "mpeg", "m4v", "3gp",
            "webm", "ogg", "ogv", "rm", "rmvb", "asf", "divx", "vob", "ts", "m2ts",
            "mts", "f4v", "swf", "mxf", "hevc", "h264", "h265", "av1", "prores",
            "dnxhd", "cineform", "braw", "m2t", "m2v", "m2p", "mpv", "pmp", "qt",
            "tod", "tp", "trp", "3g2", "3gp2", "dv", "dvr-ms", "wtv"
    ));

    /**
     * Is it a video.
     * @param fileName Need to determine whether it is the name of a video file, excluding the path.
     * @return true, it is a video file, false, it is not a video file.
     */
    public static boolean isVideo(String fileName) {
        if (fileName == null) {
            return false;
        }

        String name = fileName.toLowerCase();
        int dotIndex = name.lastIndexOf('.');

        if (dotIndex <= 0 || dotIndex >= name.length() - 1) {
            return false;
        }

        String ext = name.substring(dotIndex + 1);
        return VIDEO_EXTENSIONS.contains(ext);
    }
}
