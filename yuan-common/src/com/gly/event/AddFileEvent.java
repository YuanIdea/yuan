package com.gly.event;


import java.io.File;

public class AddFileEvent extends Event {
    public AddFileEvent(File data) {
        super(data);
    }

    public File getFile() {
        return (File) getData();
    }
}
