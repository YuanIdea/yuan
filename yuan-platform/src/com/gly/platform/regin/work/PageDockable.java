package com.gly.platform.regin.work;

import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import com.gly.event.page.PageInfo;
import com.gly.util.Resources;

/**
 * 页面停靠窗口。
 */
public class PageDockable extends DefaultMultipleCDockable {
    /** 当前页面信息 */
    private PageInfo pageInfo;

    public PageDockable(MultipleCDockableFactory<PageDockable,?> factory, PageInfo pageInfo){
        super( factory );

        setCloseable( true );
        setMinimizable( true );
        setMaximizable( true );
        setExternalizable( false );
        setRemoveOnClose( true );
        setTitleIcon( Resources.getIcon( "dockable.page" ) );
        updateName(pageInfo);
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public void updateName(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
        setTitleText(pageInfo.getFileName() );
        setTitleToolTip(pageInfo.getName());
    }

    /**
     * 获得页面信息。
     * @return 页面信息。
     */
    public PageInfo getPageInfo(){
        return pageInfo;
    }

    /**
     * 在子类中覆盖。
     */
    public void save(String pathName) {}
}
