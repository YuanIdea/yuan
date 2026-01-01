package com.gly.event;

/**
 * 刷新工程
 */
public class RefreshEvent extends Event {
    public RefreshEvent(boolean rootChange) {
        super(rootChange);
    }

    public boolean isRootChange() {
        return (boolean) getData();
    }
}

