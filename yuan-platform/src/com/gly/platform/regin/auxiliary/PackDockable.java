package com.gly.platform.regin.auxiliary;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import com.gly.event.GlobalBus;
import com.gly.platform.regin.auxiliary.maven.MavenToolWindow;

/**
 * maven面板
 */
public class PackDockable extends DefaultSingleCDockable{
    private Container content;
    private JScrollPane scrollMaven;
    private MavenToolWindow mavenToolWindow;
    /**
     * Creates a new dockable.
     * dockables.
     */
    public PackDockable(){
        super( "PackDockable" );
        GlobalBus.register(this); // 注册到事件总线
        setCloseable( true );
        setMinimizable( true );
        setMaximizable( true );
        setExternalizable( true );
        setTitleText( "Maven" );
        setTitleIcon(new ImageIcon(getClass().getResource("/icons/m.png")));
        content = getContentPane();
    }

    public void addMaven() {
        if (mavenToolWindow == null) {
            content.setLayout( new GridBagLayout() );
            mavenToolWindow = new MavenToolWindow();
            scrollMaven = new JScrollPane(mavenToolWindow);
            content.add(scrollMaven, new GridBagConstraints(0, 0, 1, 1, 1.0, 100.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(1, 1, 1, 1), 0, 0));
        } else {
            mavenToolWindow.refreshProjectTree();
            mavenToolWindow.refreshDependencies();
        }
    }

    public void removeMaven() {
        if (scrollMaven != null) {
            content.remove(scrollMaven);
            scrollMaven = null;
        }
    }

    public String getCurrentProjectRoot() {
        if (mavenToolWindow != null) {
           return mavenToolWindow.getCurrentProjectRoot();
        }
        return null;
    }
}