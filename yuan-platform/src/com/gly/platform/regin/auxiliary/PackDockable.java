package com.gly.platform.regin.auxiliary;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import com.gly.event.GlobalBus;
import com.gly.platform.regin.auxiliary.maven.MavenToolWindow;

/**
 * Maven panel
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
        GlobalBus.register(this); // Registered to the event bus.
        setCloseable( true );
        setMinimizable( true );
        setMaximizable( true );
        setExternalizable( true );
        setTitleText( "Maven" );
        setTitleIcon(new ImageIcon(getClass().getResource("/icons/m2.png")));
        content = getContentPane();
    }

    /**
     * Add maven.
     */
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

    /**
     * Remove maven.
     */
    public void removeMaven() {
        if (scrollMaven != null) {
            content.remove(scrollMaven);
            scrollMaven = null;
        }
    }

    /**
     * Get the root directory of the current project.
     * @return the root directory of the current project.
     */
    public String getCurrentProjectRoot() {
        if (mavenToolWindow != null) {
           return mavenToolWindow.getCurrentProjectRoot();
        }
        return null;
    }
}
