package com.gly.platform.regin;

import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.*;
import bibliothek.gui.dock.common.event.CDockableAdapter;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import com.gly.event.*;
import com.gly.event.page.*;
import com.gly.log.Logger;
import com.gly.platform.app.ProjectType;
import com.gly.platform.app.YuanConfig;
import com.gly.platform.editor.Editor;
import com.gly.io.xml.ReadLayout;
import com.gly.platform.editor.CodeGenerator;
import com.gly.platform.regin.auxiliary.PackDockable;
import com.gly.platform.regin.output.OutputDockable;
import com.gly.platform.regin.tree.TreeDockable;
import com.gly.platform.regin.work.PageDockable;
import com.gly.platform.regin.work.PageFactory;
import com.gly.util.FileUtil;


/**
 * 可视化管理。
 */
public class ViewManager {
    /** the controller of the whole framework */
    private CControl control;

    /** 创建的所有页面信息列表 */
    private List<PageDockable> pageDockables = new LinkedList<>();

    private PageFactory pageFactory;
    
    /** 工作区 */
    private CWorkingArea workingArea;

    /** 树形工程管理 */
    private TreeDockable treeDockable;

    private PackDockable packDockable;
    /**
     * Creates a new manager.
     * @param control the center of the Docking-Framework
     */
    public ViewManager(CControl control, boolean secure, String root){
        GlobalBus.register(this); // 注册到事件总线
        this.control = control;
        control.getContentArea().setBackground(Color.LIGHT_GRAY);
        control.getController().addDockableFocusListener(event -> handleFocusedChange(event.getNewFocusOwner()));

        packDockable = new PackDockable();
        control.addDockable(packDockable);
        packDockable.setLocation(CLocation.base().normalEast(0.2));

        pageFactory = new PageFactory();
        control.addMultipleDockableFactory( "page", pageFactory );

        workingArea = control.createWorkingArea( "WorkingArea" );
        workingArea.setLocation( CLocation.base().normalRectangle( 0, 0, 1, 1 ) );

        treeDockable = new TreeDockable();
        control.addDockable(treeDockable);
        treeDockable.refreshRoot(root);
        treeDockable.setLocation( CLocation.base().normalWest( 0.2 ) );

        OutputDockable outDock = new OutputDockable();
        control.addDockable(outDock);
        outDock.setLocation(CLocation.base().normalSouth( 0.2 ));

        readLayout(secure);
    }

    /**
     * 焦点变化处理。
     * @param newFocused 获得焦点的新停靠窗口。
     */
    private void handleFocusedChange(Dockable newFocused) {
        if( newFocused instanceof CommonDockable ){
            CDockable newC = ((CommonDockable)newFocused).getDockable();
            GlobalBus.dispatch(new FocusChangeEvent(newC));
        } else {
            GlobalBus.dispatch(new FocusChangeEvent(null));
        }
    }

    /**
     * 添加页面事件处理。
     * @param event 添加事件。
     */
    @Subscribe
    public void handleAddEvent(AddEvent event) {
        PageInfo pageInfo = event.getPageInfo();
        PageDockable exist = find(pageInfo);
        if (exist != null) {
            if (exist.getPageInfo().isDesign() == pageInfo.isDesign()) {
                reload(exist);// 如果存在且模式相同重新加载。
                return;
            } else {
                closeAll(pageInfo); // 模式不同，将存在的关闭。
            }
        }
        if (!pageInfo.isDesign()) {
            Editor editor = new Editor(pageFactory, pageInfo);
            if (pageInfo.getFileType() != FileType.Blank) {
                String init = CodeGenerator.generateJava(treeDockable.getRoot(), pageInfo.getFile(), pageInfo.getFileType());
                editor.setText(init);
            }
            open(editor) ;
        }
    }

    /**
     * 移除页面事件处理。
     * @param event 移除事件。
     */
    @Subscribe
    public void handleAddEvent(RemoveEvent event) {
        PageInfo pageInfo = event.getPageInfo();
        closeAll(pageInfo);
    }

    /**
     * 重命名
     * @param event 重命名事件。
     */
    @Subscribe
    public void handleRenameEvent(RenameEvent event) {
        RenamePageInfo rpi = event.getRenamePageInfo();
        //原文件在磁盘中不存在了，需要用新文件判断
        if (rpi.getFile().isFile()) {
            PageInfo old = new PageInfo(rpi.getOldFile());
            Editor page = (Editor)find(old);
            if (page != null) {
                page.setPageInfo(rpi);
                page.updateTitle(rpi.getFileName());
            }
        } else {
            for(PageDockable pageDockable : pageDockables){
                File oldParent = rpi.getOldFile();
                File openFile = pageDockable.getPageInfo().getFile();
                if (FileUtil.isChildOf(oldParent, openFile)){
                    try {
                        File newFile = FileUtil.getChildParentRename(oldParent, rpi.getFile(), openFile);
                        pageDockable.updateName(new PageInfo(newFile));
                        Logger.info("新文件：" + newFile.getAbsolutePath());
                    } catch (Exception e) {
                        Logger.error("文件夹子文件重命名失败：" + e.getMessage());
                    }
                }
            }
        }
    }

    @Subscribe
    public void handleRefreshEvent(RefreshEvent event) {
        if (event.isRootChange()) {
            // 清空舞台
            List<PageDockable> pagesToProcess = new ArrayList<>(pageDockables);// 创建副本,避免直接遍历原集合,造成错误。
            for (PageDockable page : pagesToProcess) {
                page.setVisible(false);
                control.removeDockable(page); // 确保此操作会从 pageDockables 中移除元素
            }
            pageDockables.clear();

            //  初始化maven
            if (ProjectType.isMaven() && packDockable != null) {
                packDockable.addMaven();
            } else {
                packDockable.removeMaven();
            }
        }
    }

    private void open(PageDockable pageDockable) {
        pageDockable.addCDockableStateListener(generateCDockableAdapter(pageDockable));// 页面变化处理。
        pageDockable.setLocation(CLocation.working( workingArea ).rectangle( 0, 0, 1, 1 ));
        workingArea.add(pageDockable);
        pageDockable.setVisible(true);
    }

    /**
     * 页面可视化变化处理。
     * @param pageDockable 要处理的页面。
     * @return 页面可视化变化处理器。
     */
    private CDockableAdapter generateCDockableAdapter(PageDockable pageDockable) {
        return new CDockableAdapter(){
            @Override
            public void visibilityChanged(CDockable dockable) {
                if(dockable.isVisible()){
                    pageDockables.add(pageDockable);
                    setFocused(pageDockable);
                } else {
                    pageDockables.remove(pageDockable);
                    if (pageDockables.size() > 1) {
                        setFocused(pageDockables.get(pageDockables.size() - 1));
                    }
                }
            }
        };
    }

    /**
     * 重新加载内容，并获得焦点。
     * @param pageDockable 要加载激活的停靠窗口。
     */
    private void reload(DefaultMultipleCDockable pageDockable) {
        if (pageDockable instanceof Editor) {
            Editor editor = (Editor) pageDockable;
            editor.reload();
        }
        setFocused(pageDockable);
    }

    /**
     * 指定停靠窗口获得焦点。
     * @param pageDockable 要指定的停靠窗口。
     */
    private void setFocused(DefaultMultipleCDockable pageDockable) {
        control.getController().setFocusedDockable(pageDockable.intern(), true);
    }

    /**
     * 获得当前被激活的页面。
     * @return 当前激活页。
     */
    public PageDockable getFocusedPage() {
        CDockable focusedDock = control.getFocusedCDockable();
        return getPage(focusedDock);
    }

    /**
     * 获得当前工作区打开的页面。
     * @return 当前打开的页面。
     */
    public PageDockable getOpenPage() {
        CDockable openedDock = getOpenDock();
        return getPage(openedDock);
    }

    private PageDockable getPage(CDockable dock ) {
        if (dock instanceof PageDockable) {
            PageDockable pageDockable = (PageDockable)dock;
            Logger.info("当前页为："+pageDockable.getPageInfo().getName());
            return pageDockable;
        }
        return null;
    }

    /**
     * 读取外部布局配置信息。
     * @param secure 是否是安全状态。
     */
    private void readLayout(boolean secure) {
        try{
            InputStream in;
            if(secure){
                in = getClass().getResourceAsStream( "/config.xml" );
            } else {
                in = new BufferedInputStream(new FileInputStream(YuanConfig.YUAN_PATH.resolve("data/config.xml").toString()));
            }
            ReadLayout.read(control, in);
        } catch( IOException ex ){
            ex.printStackTrace();
        }
    }

    public TreeDockable getTreeDockable() {
        return treeDockable;
    }

    /**
     * Ensures that no view shows <code>PageInfo</code> anymore.
     * @param pageInfo the PageInfo which should not be painted anywhere
     */
    private void closeAll( PageInfo pageInfo ){
        List<PageDockable> pagesToProcess = new ArrayList<>(pageDockables);
        for(PageDockable pageDockable : pagesToProcess){
            if (pageInfo.nameEqual(pageDockable.getPageInfo())){
                pageDockable.setVisible( false );
                control.removeDockable(pageDockable);
            }
        }
    }

    /**
     * 保存所有页面。
     */
    public void saveAll() {
        List<PageDockable> pagesToProcess = new ArrayList<>(pageDockables);
        for(PageDockable pageDockable : pagesToProcess){
            pageDockable.save(pageDockable.getPageInfo().getName());
        }
    }

    /**
     * 保存所有修改过的页面。
     */
    public void saveAllModified() {
        List<PageDockable> pagesToProcess = new ArrayList<>(pageDockables);
        for(PageDockable pageDockable : pagesToProcess){
            if (pageDockable instanceof Editor) {
                Editor editor = (Editor) pageDockable;
                editor.saveModified();
            }
        }
    }

    /**
     * 查找工作区中存在的页面停靠。
     * @param pageInfo 页面信息。
     * @return 如果存在与页面信息一致的停靠空口，返回这个窗口，否则返回空。
     */
    private PageDockable find(PageInfo pageInfo){
        for(PageDockable pageDockable : pageDockables){
            if (pageInfo.nameEqual(pageDockable.getPageInfo())){
                return pageDockable;
            }
        }
        return null;
    }

    private CDockable getOpenDock() {
        List<PageDockable> allDocks = new ArrayList<>(pageDockables);
        for (CDockable dock : allDocks) {
            if (dock.isVisible() && dock.isShowing()) {
                return dock;
            }
        }
        return null;
    }

    public PackDockable getPackDockable() {
        return packDockable;
    }
}
