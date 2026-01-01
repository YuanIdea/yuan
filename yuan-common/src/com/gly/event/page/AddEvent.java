package com.gly.event.page;

import com.gly.event.Event;


public class AddEvent extends Event {
    public AddEvent(PageInfo data) {
        super(data);
    }

    public PageInfo getPageInfo() {
        return (PageInfo) getData();
    }
}


