package com.gly.model;

import javax.swing.*;

/**
 * 基础可执行单元。
 */
public abstract class BaseExecutable implements ExecutableUnit {
    private String root;
    private String name;
    private JFrame owner;
    private boolean isDone;

    @Override
    public void init(String root, String name, JFrame owner) {
        this.root = root;
        this.name = name;
        this.owner = owner;
        setDone(false);
    }

    public String getRoot() {
        if (root == null || root.isEmpty()) {
            System.err.println("根目录root错误：" + root);
        }
        return root;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public JFrame getOwner() {
        return owner;
    }
}
