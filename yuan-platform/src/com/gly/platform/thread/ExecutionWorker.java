package com.gly.platform.thread;

import com.gly.event.DoneEvent;
import com.gly.event.GlobalBus;
import com.gly.model.ExecutableUnit;

import javax.swing.*;

public class ExecutionWorker extends SwingWorker<Object, String> {
    private ExecutableUnit actuator;

    ExecutionWorker(ExecutableUnit exec) {
        actuator = exec;
    }

    @Override
    protected Object doInBackground() {
        // 初始化算法
        actuator.start();

        if (actuator.isDone()) {
            GlobalBus.dispatch(new DoneEvent(actuator));
        }
        // 返回最终结果
        return actuator.getResult();
    }

    @Override
    protected void done() {

    }

    /**
     * 安全终止程序。
     */
    public void stop() {
        actuator.stop();
    }
}

