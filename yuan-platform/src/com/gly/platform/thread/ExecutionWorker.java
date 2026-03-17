package com.gly.platform.thread;

import com.gly.event.DoneEvent;
import com.gly.event.GlobalBus;
import com.gly.model.ExecutableUnit;

import javax.swing.*;

public class ExecutionWorker extends SwingWorker<Object, String> {
    private final ExecutableUnit actuator;

    ExecutionWorker(ExecutableUnit exec) {
        actuator = exec;
    }

    @Override
    protected Object doInBackground() {
        actuator.start(); // Initialize algorithm
        if (actuator.isDone()) {
            GlobalBus.dispatch(new DoneEvent(actuator));
        }
        return actuator.getResult(); // Return final result
    }

    @Override
    protected void done() {
        super.done();
    }

    /**
     * Terminate the program safely.
     */
    public void stop() {
        actuator.stop();
    }
}

