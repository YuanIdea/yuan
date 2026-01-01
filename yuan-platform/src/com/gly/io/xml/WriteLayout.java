package com.gly.io.xml;

import bibliothek.gui.dock.common.CControl;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import com.gly.platform.app.YuanConfig;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class WriteLayout {
    public static void write(CControl control) {
        try{
            XElement element = new XElement( "config" );
            writeXML( control, element );
            OutputStream out = new BufferedOutputStream( new FileOutputStream(YuanConfig.YUAN_PATH.resolve("data/config.xml").toString() ));
            XIO.writeUTF( element, out );
        }
        catch( IOException ex ){
            ex.printStackTrace();
        }
    }

    /**
     * Writes all the settings of this application.
     * @param element the xml element to write into
     */
    private static void writeXML(CControl control, XElement element ){
        control.getResources().writeXML( element.addElement( "resources" ) );
    }
}
