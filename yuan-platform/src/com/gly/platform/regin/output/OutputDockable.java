package com.gly.platform.regin.output;

import java.awt.*;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import com.gly.event.GlobalBus;
import com.gly.event.RefreshEvent;
import com.gly.event.Subscribe;
import com.gly.i18n.I18n;
import com.gly.util.Resources;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

/**
 * Platform information output window.
 *
 * @author Guoliang Yang
 */
public class OutputDockable extends DefaultSingleCDockable {
    // Console output.
    private final JediTermWidget console;

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

        DefaultSettingsProvider settingsProvider = new DefaultSettingsProvider() {
            @Override
            public Font getTerminalFont() {
                return new Font("DialogInput", Font.PLAIN, 13);
            }
        };
        console = new JediTermWidget(settingsProvider);
        try {
            DummyTtyConnector dummyConnector = new DummyTtyConnector();
            console.setTtyConnector(dummyConnector);
            console.start();

            Redirect.systemOutToTextArea(dummyConnector.getOutputStream());
            Redirect.systemErrorStream(dummyConnector.getOutputStream());

            Container content = getContentPane();
            content.add(console, BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void handleClearStagePage(RefreshEvent event) {
        console.getTerminal().clearScreen();
    }
}


