package com.gly.event;

import com.gly.model.ExecutableUnit;

public class StartEvent extends Event {
    public StartEvent(ExecutableUnit data) {
        super(data);
    }

    public ExecutableUnit getExecutable() {
        return (ExecutableUnit) getData();
    }
}
