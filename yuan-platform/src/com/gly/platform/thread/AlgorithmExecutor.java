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
     * Private constructor prevents external instantiation.
     */
    private AlgorithmExecutor() {
    }

    /**
     * Get the singleton instance.
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
     * Synchronously terminate the current algorithm.
     */
    public void stopCurSolver() {
        synchronized (lock) {
            if (currentWorker == null) {
                return;
            }

            Logger.info("Exiting the current program...");
            try {
                currentWorker.stop();// Request algorithm termination.
                waitForTermination(800);// Synchronously wait for the algorithm to complete termination.
            } finally {
                currentWorker = null;
            }
        }
    }

    /**
     * Synchronously wait for algorithm termination.
     */
    private void waitForTermination(long timeoutMs) {
        if (currentWorker == null) {
            return;
        }
        long startTime = System.currentTimeMillis();
        while (!currentWorker.isDone()) {// Wait for algorithm termination or timeout.
            if (System.currentTimeMillis() - startTime > timeoutMs) {// Check for timeout.
                reset();
                break;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
                break;
            }
        }

        if (currentWorker != null && currentWorker.isDone()) {
            Logger.info("Algorithm terminated normally.");
        }
    }

    /**
     * Start a new algorithm.
     */
    public void startNewSolver(ExecutableUnit execUnit) {
        if (execUnit == null) {
            return;
        }
        synchronized (lock) {
            // Wait for the current algorithm to fully terminate.
            while (isAlgorithmRunning()) {
                try {
                    lock.wait(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                    return;
                }
            }

            GlobalBus.dispatch(new StartEvent(execUnit));
            // Create and start a new algorithm.
            currentWorker = new ExecutionWorker(execUnit);
            currentWorker.execute();
        }
    }

    /**
     * Get the current algorithm status.
     */
    private boolean isAlgorithmRunning() {
        synchronized (lock) {
            return currentWorker != null && !currentWorker.isDone();
        }
    }

    /**
     * Used to forcibly reset the state under special circumstances.
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