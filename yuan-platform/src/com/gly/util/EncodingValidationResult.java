package com.gly.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储编码验证结果
 */
class EncodingValidationResult {
    private final List<EncodingResult> results = new ArrayList<>();

    void addEncodingResult(String encoding, boolean valid, double chineseRatio) {
        results.add(new EncodingResult(encoding, valid, chineseRatio));
    }

    List<EncodingResult> getValidResults() {
        List<EncodingResult> validResults = new ArrayList<>();
        for (EncodingResult result : results) {
            if (result.isValid()) {
                validResults.add(result);
            }
        }
        return validResults;
    }

    boolean isEncodingValid(String encoding) {
        for (EncodingResult result : results) {
            if (result.getEncoding().equalsIgnoreCase(encoding) && result.isValid()) {
                return true;
            }
        }
        return false;
    }
}
