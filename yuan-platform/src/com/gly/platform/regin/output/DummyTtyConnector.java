package com.gly.platform.regin.output;

import com.jediterm.terminal.TtyConnector;

import java.io.IOException;// 虚拟 TtyConnector 类，仅用于满足终端启动要求
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

class DummyTtyConnector implements TtyConnector {
    private final InputStreamReader reader;
    private final PipedOutputStream writer;

    public DummyTtyConnector() throws IOException {
        PipedInputStream in = new PipedInputStream();
        writer = new PipedOutputStream(in);
        reader = new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    public PipedOutputStream getOutputStream() {
        return writer;
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return reader.read(buf, offset, length);
    }

    @Override
    public void write(byte[] bytes) {
    }

    @Override
    public void write(String string) {
    }

    @Override
    public void close() {
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "dummy-output";
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }

    @Override
    public int waitFor() {
        return 0;
    }
}