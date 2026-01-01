package com.gly.event.page;

import com.gly.event.Event;

/**
 *  重命名事件
 */
public class RenameEvent extends Event {

    /**
     * 构造函数。
     * @param data 重命名信息。
     */
    public RenameEvent(RenamePageInfo data) {
        super(data);
    }

    public RenamePageInfo getRenamePageInfo() {
        return (RenamePageInfo) getData();
    }
}
