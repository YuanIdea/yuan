package com.gly.platform.regin.output;

import java.awt.*;
import javax.swing.*;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import com.gly.event.GlobalBus;
import com.gly.event.RefreshEvent;
import com.gly.event.Subscribe;
import com.gly.util.Resources;
import com.gly.platform.view.JPopupTextField;
import com.gly.util.FontLoader;

/**
 * 平台信息输出窗口。
 * @author Guoliang Yang
 */
public class OutputDockable extends DefaultSingleCDockable{

    // 控制台输出。
    private JPopupTextField output;
    /**
     * 调试信息输出窗口。
     */
    public OutputDockable(){
        super( "OutputDockable" );
        GlobalBus.register(this); // 注册到事件总线
        setCloseable( true );
        setMinimizable( true );
        setMaximizable( true );
        setExternalizable( true );
        setTitleText( "输出" );
        setTitleIcon( Resources.getIcon( "dockable.out" ) );

        output = new JPopupTextField(); // 输出调试信息。
        output.setFont(FontLoader.loadSansSerif(13));
        Redirect.systemOutToTextArea(output); // 重定向系统信息到控制台。
        Redirect.systemErrorStream(output);// 重定向系统错误到控制台。
        Container content = getContentPane();
        content.add(new JScrollPane(output), BorderLayout.CENTER);

//        Font font = output.getFont();
//        System.out.println("1当前字体: " + font.getFontName() + ", 大小: " + font.getSize() + ",支持中文：" + font.canDisplay('中'));
    }

    @Subscribe
    public void handleClearStagePage(RefreshEvent event) {
        output.setText("");
    }
}


