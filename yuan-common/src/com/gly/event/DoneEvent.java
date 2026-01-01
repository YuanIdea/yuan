package com.gly.event;

import com.gly.model.ExecutableUnit;


public class DoneEvent extends Event {
    public DoneEvent(ExecutableUnit data) {
        super(data);
    }

    public ExecutableUnit getExecutable() {
        return (ExecutableUnit) getData();
    }
}


