package com.gly.event;

import bibliothek.gui.dock.common.intern.CDockable;

public class FocusChangeEvent extends Event {
    public FocusChangeEvent(CDockable data) {
        super(data);
    }
    public CDockable getFocusPage() {
        return (CDockable)getData();
    }
}
