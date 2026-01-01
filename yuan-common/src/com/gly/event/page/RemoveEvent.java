package com.gly.event.page;

import com.gly.event.Event;

public class RemoveEvent extends Event {
    public RemoveEvent(PageInfo data) {
        super(data);
    }

    public PageInfo getPageInfo() {
        return (PageInfo) getData();
    }
}
