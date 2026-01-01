package com.gly.platform.regin.work;

import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.util.xml.XElement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 */
public class PageLayout implements MultipleCDockableLayout {
    /** the name of the picture */
    private String name;

    /**
     * Sets the name of the picture that is shown.
     * @param name the name of the picture
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Gets the name of the picture that is shown.
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void readStream( DataInputStream in ) throws IOException {
        name = in.readUTF();
    }

    public void readXML( XElement element ) {
        name = element.getString();
    }

    public void writeStream( DataOutputStream out ) throws IOException {
        out.writeUTF( name );
    }

    public void writeXML( XElement element ) {
        element.setString( name );
    }
}
