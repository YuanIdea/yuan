package com.gly.util;

/**
 * 单个编码验证结果
 */
class EncodingResult {
    private final String encoding;
    private final boolean valid;
    private final double chineseRatio;

    EncodingResult(String encoding, boolean valid, double chineseRatio) {
        this.encoding = encoding;
        this.valid = valid;
        this.chineseRatio = chineseRatio;
    }

    public String getEncoding() { return encoding; }
    boolean isValid() { return valid; }
    double getChineseRatio() { return chineseRatio; }
}
