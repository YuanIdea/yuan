package com.gly.platform.regin.work;

import bibliothek.gui.dock.common.MultipleCDockableFactory;

/**
 *
 */
public class PageFactory implements MultipleCDockableFactory<PageDockable, PageLayout> {
    public PageLayout create() {
        return new PageLayout();
    }

    public PageDockable read(PageLayout layout ) {
        return null;
    }

    public PageLayout write(PageDockable dockable ) {
        PageLayout layout = new PageLayout();
        layout.setName( dockable.getPageInfo().getName() );
        return layout;
    }

    public boolean match(PageDockable dockable, PageLayout layout ){
        String name = dockable.getPageInfo().getName();
        return name.equals( layout.getName() );
    }
}
