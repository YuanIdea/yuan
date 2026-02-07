package com.gly.platform.regin.output;

import java.awt.*;
import javax.swing.*;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import com.gly.event.GlobalBus;
import com.gly.event.RefreshEvent;
import com.gly.event.Subscribe;
import com.gly.i18n.I18n;
import com.gly.util.Resources;
import com.gly.platform.view.JPopupTextField;
import com.gly.util.FontLoader;

/**
 * Platform information output window.
 *
 * @author Guoliang Yang
 */
public class OutputDockable extends DefaultSingleCDockable {

    // Console output.
    private final JPopupTextField output;

    /**
     * Constructor.
     */
    public OutputDockable() {
        super("OutputDockable");
        GlobalBus.register(this); // Register to event bus.
        setCloseable(true);
        setMinimizable(true);
        setMaximizable(true);
        setExternalizable(true);
        setTitleText(I18n.get("output"));
        setTitleIcon(Resources.getIcon("dockable.out"));

        output = new JPopupTextField(); // Output debugging information.
        output.setFont(FontLoader.loadSansSerif(13));
        Redirect.systemOutToTextArea(output); // Redirect system information to console.
        Redirect.systemErrorStream(output);// Redirect system errors to console.
        Container content = getContentPane();
        content.add(new JScrollPane(output), BorderLayout.CENTER);
    }

    @Subscribe
    public void handleClearStagePage(RefreshEvent event) {
        output.setText("");
    }
}


