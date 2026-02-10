package com.gly.platform.regin.tree.add;

/**
 * Create result wrapper class.
 */
public class CreateResult {
    private final boolean success;
    private final String path;
    private final String error;

    private CreateResult(boolean success, String path, String error) {
        this.success = success;
        this.path = path;
        this.error = error;
    }

    static CreateResult success(String path) {
        return new CreateResult(true, path, null);
    }

    static CreateResult failure(String error) {
        return new CreateResult(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getPath() {
        return path;
    }

    public String getError() {
        return error;
    }
}
