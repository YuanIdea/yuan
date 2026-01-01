package com.gly.io.xml;

import bibliothek.gui.dock.common.CControl;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;

import java.io.IOException;
import java.io.InputStream;

public class ReadLayout {

    public static void read(CControl control, InputStream in) {
        if (in != null) {
            try {
                readXML(control, XIO.readUTF(in));
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Reads all the settings of this application.
     * @param control the element to read from
     * @param element the element to read from
     */
    private static void  readXML(CControl control, XElement element) {
        control.getResources().readXML( element.getElement( "resources" ) );
    }
}
