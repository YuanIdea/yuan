package com.gly.platform.thread;

import com.gly.event.GlobalBus;
import com.gly.event.StartEvent;
import com.gly.log.Logger;
import com.gly.model.ExecutableUnit;

public class AlgorithmExecutor {
    private static volatile AlgorithmExecutor instance;
    private ExecutionWorker currentWorker;
    private final Object lock = new Object();

    /**
     * 私有构造函数防止外部实例化
     */
    private AlgorithmExecutor() {
    }

    /**
     * 获取单例实例（双重校验锁实现线程安全）
     */
    public static AlgorithmExecutor getInstance() {
        if (instance == null) {
            synchronized (AlgorithmExecutor.class) {
                if (instance == null) {
                    instance = new AlgorithmExecutor();
                }
            }
        }
        return instance;
    }

    /**
     * 同步终止当前算法
     */
    public void stopCurSolver() {
        synchronized (lock) {
            if (currentWorker == null) {
                return;
            }

            Logger.info("退出当前程序...");
            try {
                currentWorker.stop();// 请求算法终止
                waitForTermination(800);// 同步等待算法终止完成
            } finally {
                currentWorker = null;
            }
        }
    }

    /**
     * 同步等待算法终止
     */
    private void waitForTermination(long timeoutMs) {
        if (currentWorker == null) {
            return;
        }
        long startTime = System.currentTimeMillis();
        while (!currentWorker.isDone()) {// 等待算法终止或超时
            if (System.currentTimeMillis() - startTime > timeoutMs) {// 检查超时
                reset();
                break;
            }

            try {// 短暂等待避免忙等
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        if (currentWorker !=null && currentWorker.isDone()) {
            Logger.info("算法正常终止");
        }
    }

    /**
     * 启动新算法
     */
    public void startNewSolver(ExecutableUnit execUnit) {
        if (execUnit == null) {
            return;
        }
        synchronized (lock) {
            // 等待当前算法完全终止
            while (isAlgorithmRunning()) {
                try {
                    lock.wait(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            GlobalBus.dispatch(new StartEvent(execUnit));
            // 创建并启动新算法
            currentWorker = new ExecutionWorker(execUnit);
            currentWorker.execute();
        }
    }

    /**
     * 获取当前算法状态
     */
    private boolean isAlgorithmRunning() {
        synchronized (lock) {
            return currentWorker != null && !currentWorker.isDone();
        }
    }

    /**
     * 重置执行器（用于特殊情况下强制重置状态）
     */
    private void reset() {
        synchronized (lock) {
            if (currentWorker != null && !currentWorker.isDone()) {
                Logger.info("Algorithm force terminated!");
                currentWorker.cancel(true);
            }
            currentWorker = null;
        }
    }
}