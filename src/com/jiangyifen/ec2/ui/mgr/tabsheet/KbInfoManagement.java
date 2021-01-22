package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.KbInfo;
import com.jiangyifen.ec2.entity.KbInfoType;
import com.jiangyifen.ec2.service.eaoservice.KbInfoService;
import com.jiangyifen.ec2.service.eaoservice.KbInfoTypeService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.kbinfo.AddKbInfoWinodw;
import com.jiangyifen.ec2.ui.mgr.kbinfo.DeleteKbInfoWinodw;
import com.jiangyifen.ec2.ui.mgr.kbinfo.EditKbInfoWinodw;
import com.jiangyifen.ec2.ui.mgr.kbinfo.KbInfoTypeManagement;
import com.jiangyifen.ec2.ui.mgr.kbinfo.ShowKbInfoViewWindow;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 知识库管理
 * @author lxy
 *
 */
public class KbInfoManagement extends VerticalLayout implements Property.ValueChangeListener, ClickListener {

	//静态-非业务
	private static final long serialVersionUID = 5463589052066440013L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	
	//静态-业务
	private final Object[] COL_PROPERTIES = new Object[] {"title","kbInfoType","level","createDate","lastUpdateDate"};
	private final String[] COL_HEADERS = new String[] {"标题","类型","级别","创建时间","最后更新时间"};
	private static final int PAGE_LENGTH = 15;			//一页数据条数
	private static final String TREE_PROPERTY_NAME = "NAME";
	//持有UI对象	
	//本控件持有对象
 	//页面布局组件
	//private HorizontalLayout 	topToolsLayout;		//顶部工具布局
 	//private HorizontalLayout 	bottomToolsLayout;	//底部工具布局
	private VerticalLayout hl_left;
	private VerticalLayout hl_right;
	
	//页面组件
	private FlipOverTableComponent<KbInfo>	qflip;	//本页面分页组件
	private Table table;							//表格组件
	
 	private ComboBox cb_level;						//级别
	private TextField tf_query;						//查询文本
	private Button bt_search_title;					//标题搜索
	private Button bt_search_like;					//模糊
	private Button bt_reset;						//重置
	private PopupDateField createStartTime;			//创建开始时间
	private PopupDateField createEndTime;			//创建结束时间
	private PopupDateField lasetUpdateStartTime;	//最后更新开始时间
	private PopupDateField lasetUpdateEndTime;		//最后更新结束时间
	private Label lb_msg;							//系提示信息
	private Button bt_add_kbinfo;					//添加知识
	private Button bt_edit_kbinfo;					//编辑知识
	private Button bt_delete_kbinfo;				//删除知识
	private Button bt_manage_type;					//知识类型管理
	private TextArea ta_content;					//显示知识类型
	private Button bt_show;							//客户显示
 	private Tree tree;
	
	// 弹出窗口 只创建一次
	private HierarchicalContainer hwContainer = new HierarchicalContainer();
	private KbInfoTypeManagement infoTypeManagement;
	private EditKbInfoWinodw editKbInfoWinodw;
	private DeleteKbInfoWinodw deleteKbInfoWinodw;
	private ShowKbInfoViewWindow showKbInfoViewWindow;
	
	//业务必须对象
	 
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**搜索类型   1:标题搜索 2:模糊搜素 默认：1 标题搜索**/
	private int query_tp = 1 ;					
	private boolean treeQuery = false;
	
	//业务本页对象
	private Domain domain;		//当前用户
 	private String searchListJpql;				//检索列表Jpql
	private String searchCountJpql;				//检索总数Jpql
	 
	
	//业务注入对象
	private KbInfoService kbInfoService; 
 	private KbInfoTypeService kbInfoTypeService;
 	
 	public KbInfoManagement(){
		initThis();
		initSpringContext();
		initCompanent();
	}
	/**初始化本控件整体属性 */
	private void initThis() {
 		this.setWidth("100%");	
 		this.setHeight("100%");	
		this.setMargin(false, true, true, true);
		this.setSpacing(true);
 	}
	/**初始化Spring上写相关系想你 */
	private void initSpringContext(){
		domain = SpringContextHolder.getDomain();
		kbInfoService = SpringContextHolder.getBean("kbInfoService");
		kbInfoTypeService = SpringContextHolder.getBean("kbInfoTypeService");
 	}
	
	/**初始化控件布局和组件 */
	private void initCompanent() {
		buildMainLayout();
 		buildTopToolsLayout();			//顶部工具布局
		buildTableLayout();				//中间内容布局
		buildBottomToolsLayout();		//底部工具布局
		buildBottomInfosLayout();
	}
	
	private void buildMainLayout(){
		HorizontalLayout hl_main = new HorizontalLayout();
		hl_main.setSizeFull();
		
		hl_main.setSpacing(true);
		this.addComponent(hl_main);
		
		hl_left = new VerticalLayout();
		hl_left.setWidth("300px");
		
		tree = new Tree();
		tree.setImmediate(true);
 		tree.setItemCaptionPropertyId(TREE_PROPERTY_NAME);
  		long domainid = domain.getId();
 		List<KbInfoType> list =  kbInfoTypeService.getKbInfoTypeListByDomain(domainid);
  		if(list.size()>0){
 			for (KbInfoType kit : list) {
 				hwContainer.addContainerProperty(TREE_PROPERTY_NAME, String.class, null);
 		 		Item item = hwContainer.addItem(kit.getId());
 		  		item.getItemProperty(TREE_PROPERTY_NAME).setValue(kit.getName());
 		  		hwContainer.setChildrenAllowed(kit.getId(), true);
 		  		initTreeData(kit,domainid);
			}
 		}
  		tree.setContainerDataSource(hwContainer);//ExampleUtil.getHardwareContainer()
 		tree.addListener(this);
		hl_left.addComponent(tree);
		hl_main.addComponent(hl_left);
 		
		hl_right = new VerticalLayout();
		hl_right.setSpacing(true);
		hl_right.setSizeFull();
		hl_main.addComponent(hl_right);
		hl_main.setExpandRatio(hl_right, 1.0f);
	}
	
	public void updateTreeData(){
		hwContainer.removeAllItems();
		long domainid = domain.getId();
 		List<KbInfoType> list =  kbInfoTypeService.getKbInfoTypeListByDomain(domainid);
  		if(list.size()>0){
 			for (KbInfoType kit : list) {
 				hwContainer.addContainerProperty(TREE_PROPERTY_NAME, String.class, null);
 		 		Item item = hwContainer.addItem(kit.getId());
 		  		item.getItemProperty(TREE_PROPERTY_NAME).setValue(kit.getName());
 		  		hwContainer.setChildrenAllowed(kit.getId(), true);
 		  		initTreeData(kit,domainid);
			}
 		}
  		tree.setContainerDataSource(hwContainer);
	}
	
	private void initTreeData(KbInfoType infoType,long domainid){
		if(null != infoType){ 
			List<KbInfoType> lsitem = kbInfoTypeService.getKbInfoTypeListByParenetId(infoType.getId(),domainid);
		  	if(lsitem.size()>0){
		 			for (KbInfoType kititem : lsitem) {
		 				Item itemitem = hwContainer.addItem(kititem.getId());
		 				itemitem.getItemProperty(TREE_PROPERTY_NAME).setValue(kititem.getName());
		 				hwContainer.setParent(kititem.getId(), infoType.getId());
		 					int count = kbInfoTypeService.getKbInfoTypeCountByParenetId(kititem.getId(),domainid);
		 					if(count>0){
		 						hwContainer.setChildrenAllowed(kititem.getId(), true);
		 						initTreeData(kititem,domainid);
		 					}else{
		 						hwContainer.setChildrenAllowed(kititem.getId(), false);
		 					}
		 			}
		  	}
		}
	}
	
	private void buildBottomInfosLayout() {
		ta_content = new TextArea();
		ta_content.setWidth("100%");
		ta_content.setHeight("100%");
		ta_content.setMaxLength(10000);
		ta_content.setReadOnly(true);
		hl_right.addComponent(ta_content);
 		hl_right.setExpandRatio(ta_content, 1.0f);
	}
	
	private void buildTopToolsLayout(){
		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout1 = new HorizontalLayout();
		searchHLayout1.setSpacing(true);
		searchHLayout1.setMargin(true, false, false, false);
		hl_right.addComponent(searchHLayout1);
		
		tf_query = new TextField();
		tf_query.setWidth("350px");
		tf_query.setInputPrompt("关键字");
		searchHLayout1.addComponent(tf_query);
		
		bt_search_title = new Button("搜索标题", this);
		bt_search_title.setImmediate(true);
		searchHLayout1.addComponent(bt_search_title);
		
		bt_search_like = new Button("搜索标题+内容", this);
		bt_search_like.setImmediate(true);
		searchHLayout1.addComponent(bt_search_like);
		
		bt_reset = new Button("重置搜索", this);
		bt_reset.setImmediate(true);
		searchHLayout1.addComponent(bt_reset);
		
		lb_msg = new Label("",Label.CONTENT_XHTML);
		searchHLayout1.addComponent(lb_msg);
		
		HorizontalLayout searchHLayout2 = new HorizontalLayout();
		searchHLayout2.setSpacing(true);
		Label lb_2_1 = new Label("创建时间&nbsp",Label.CONTENT_XHTML);
		lb_2_1.setWidth("-1px");
		searchHLayout2.addComponent(lb_2_1);
		createStartTime = new PopupDateField();
		createStartTime.setWidth("153px");
		createStartTime.setImmediate(true);
		createStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		createStartTime.setParseErrorMessage("时间格式不合法");
		createStartTime.setResolution(PopupDateField.RESOLUTION_SEC);
		searchHLayout2.addComponent(createStartTime);
		Label lb_2_2 = new Label("-");
		lb_2_2.setWidth("-1px");
		searchHLayout2.addComponent(lb_2_2);
		createEndTime = new PopupDateField();
		createEndTime.setWidth("153px");
		createEndTime.setImmediate(true);
		createEndTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		createEndTime.setParseErrorMessage("时间格式不合法");
		createEndTime.setResolution(PopupDateField.RESOLUTION_SEC);
		searchHLayout2.addComponent(createEndTime);
		Label lb_2_3 = new Label("最后更新时间");
		lb_2_3.setWidth("-1px");
		searchHLayout2.addComponent(lb_2_3);
		lasetUpdateStartTime = new PopupDateField();
		lasetUpdateStartTime.setWidth("153px");
		lasetUpdateStartTime.setImmediate(true);
		lasetUpdateStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		lasetUpdateStartTime.setParseErrorMessage("时间格式不合法");
		lasetUpdateStartTime.setResolution(PopupDateField.RESOLUTION_SEC);
		searchHLayout2.addComponent(lasetUpdateStartTime);
		Label lb_2_4 = new Label("-");
		lb_2_4.setWidth("-1px");
		searchHLayout2.addComponent(lb_2_4);
		lasetUpdateEndTime = new PopupDateField();
		lasetUpdateEndTime.setWidth("153px");
		lasetUpdateEndTime.setImmediate(true);
		lasetUpdateEndTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		lasetUpdateEndTime.setParseErrorMessage("时间格式不合法");
		lasetUpdateEndTime.setResolution(PopupDateField.RESOLUTION_SEC);
		searchHLayout2.addComponent(lasetUpdateEndTime);
		searchHLayout2.addComponent(new Label("级别"));
		cb_level = new ComboBox();
		for (int i = 1; i < 10; i++) {
			cb_level.addItem(i);
		}
		cb_level.setWidth("40px");
		cb_level.setNullSelectionAllowed(true);
		cb_level.setImmediate(true);
		searchHLayout2.addComponent(cb_level);
		
		hl_right.addComponent(searchHLayout2);
	}
	
	/** 创建分机表格显示区组件*/
	private void buildTableLayout(){
		table = createFormatColumnTable();
		table.setWidth("100%");	
		table.setHeight("300px");	
 		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.setPageLength(PAGE_LENGTH);
		hl_right.addComponent(table);
	}

	/** 创建格式化 了 显示内容的 Table对象 */
	private Table createFormatColumnTable() {
		return new Table() {
			private static final long serialVersionUID = 4347052160778120067L;
			@Override
			protected String formatPropertyValue(Object rowId, Object columnId,
					Property property) {
				if(columnId.equals("kbInfoType")){
					Object kit =  property.getValue();
					if(null != kit) {	//不为空说明存根目录
						KbInfoType kittmp = (KbInfoType)kit;
						if(null != kittmp){
							return kittmp.getName(); 
						}
					}else{				//为空说明是根目录
						return "根目录";
					}
				}
				if(columnId.equals("createDate")) {
					String st = "";
					try {
						if (null != property.getValue()) {
							Date d = (Date) property.getValue();
							st = sdf.format(d);
						}
					} catch (Exception e) {
						logger.error("日期转换异常_createFormatColumnTable_LLXXYY", e);
					}
					return st;
				}
				if(columnId.equals("lastUpdateDate")) {
					String st = "";
					try {
						if(null!=property.getValue()){
							Date d = (Date) property.getValue();
							st = sdf.format(d);
						}
					} catch (Exception e) {
						logger.error("日期转换异常_createFormatColumnTable_LLXXYY", e);
					}
					return st;
				}
				
				return super.formatPropertyValue(rowId, columnId, property);
			}
		};
	}
	
	
	private void buildBottomToolsLayout(){
		HorizontalLayout footerHLayout = new HorizontalLayout();
		footerHLayout.setSpacing(true);
		footerHLayout.setWidth("100%");
		hl_right.addComponent(footerHLayout);
		
		HorizontalLayout leftFooter = new HorizontalLayout();
		leftFooter.setSpacing(true);
		footerHLayout.addComponent(leftFooter);
		
		bt_add_kbinfo = new Button("添加知识", this);
		bt_add_kbinfo.setImmediate(true);
		leftFooter.addComponent(bt_add_kbinfo);
		
		bt_edit_kbinfo = new Button("编辑知识", this);
		bt_edit_kbinfo.setImmediate(true);
		bt_edit_kbinfo.setEnabled(false);
		leftFooter.addComponent(bt_edit_kbinfo);
		
		bt_delete_kbinfo = new Button("删除知识", this);
		bt_delete_kbinfo.setImmediate(true);
		bt_delete_kbinfo.setEnabled(false);
		leftFooter.addComponent(bt_delete_kbinfo);
		
		bt_manage_type = new Button("管理知识类型",this);
		bt_manage_type.setImmediate(true);
		leftFooter.addComponent(bt_manage_type);
		
		bt_show = new Button("知识库",this);
		bt_show.setImmediate(true);
		leftFooter.addComponent(bt_show);
		
		initializeSql();	// 初始化搜索语句和分页组件
		
		qflip = new FlipOverTableComponent<KbInfo>(KbInfo.class, kbInfoService, table, searchListJpql, searchCountJpql, null);
		qflip.setPageLength(PAGE_LENGTH, false);
  		table.setVisibleColumns(COL_PROPERTIES);
		table.setColumnHeaders(COL_HEADERS);
 		footerHLayout.addComponent(qflip);
	}
	
	/** 根据不同的需求，初始化查询语句 */
	private void initializeSql() {
		StringBuffer nativeSqlList = new StringBuffer();		//查询列表语句
		StringBuffer nativeSqlCount = new StringBuffer();		//查询总数语句
		StringBuffer nativeSql = new StringBuffer();			//语句where语句
		StringBuffer idsSql = new StringBuffer();				//子类型时用到的id列表语句
		
		nativeSqlList.append("select s  ");//
 		nativeSqlCount.append(" select ");
		nativeSqlCount.append(" count(s) ");
		
		nativeSql.append(" from KbInfo as s ");	//知识
		nativeSql.append(" where 1 = 1 ");
		
		if(treeQuery){
			long kitid = -1;
			if(!WorkUIUtils.stringIsEmpty(this.tree.getValue())){				
				kitid = Long.valueOf(this.tree.getValue().toString());
			}
			if(kitid > 0 ) {//开始创建时间
				List<Long> ids = new ArrayList<Long>();
				ids.add(kitid);
				ids = kbInfoService.getTypeIds(kitid,ids);
				if(ids.size() < 1){
					ids.add(kitid);
				}
				for (int i = 0; i < ids.size(); i++) {
					if(i == 0){
						idsSql.append(ids.get(i));
					}else{
						idsSql.append(","+ids.get(i));
					}
				}
				nativeSql.append(" and s.kbInfoType.id in ("+idsSql.toString()+")");
			}
		}else{
			if(2 == query_tp){			//模糊搜索		都是like 知识标题最好做索引
				if(!WorkUIUtils.stringIsEmpty(this.tf_query.getValue())){
					nativeSql.append(" and (s.title like '%" + this.tf_query.getValue().toString()+"%' or s.content like '%" + this.tf_query.getValue().toString()+"%')");
				}
			}else if(1 == query_tp){	//标题搜索
				if(!WorkUIUtils.stringIsEmpty(this.tf_query.getValue())){
					nativeSql.append(" and s.title like '%" + this.tf_query.getValue().toString()+"%'");
				}
			}
		}
		
		if(!WorkUIUtils.stringIsEmpty(this.cb_level.getValue())){
			int level = Integer.valueOf(this.cb_level.getValue().toString());
			nativeSql.append(" and s.level="+level);
		}
		
		if(null != createStartTime.getValue()) {//开始创建时间
			nativeSql.append(" and s.createDate >= '" + sdf.format(createStartTime.getValue()) +"'");
		} 
		if(null !=createEndTime.getValue()) {//开始创建时间
			nativeSql.append(" and s.createDate <= '" + sdf.format(createEndTime.getValue()) +"'");
		} 
		if(null != lasetUpdateStartTime.getValue()) {//最新更新时间
			nativeSql.append(" and s.lastUpdateDate >= '" + sdf.format(lasetUpdateStartTime.getValue()) +"'");
		}
		if(null != lasetUpdateEndTime.getValue()) {	//最新更新时间
			nativeSql.append(" and s.lastUpdateDate <= '" + sdf.format(lasetUpdateEndTime.getValue()) +"'");
		}
		nativeSql.append(" and s.domain.id = "+domain.getId());		//域非常重要,必须有
		nativeSqlList.append(nativeSql);
		nativeSqlList.append(" order by s.createDate desc,s.level desc ");
		nativeSqlCount.append(nativeSql);
		
		//System.out.println(nativeSqlList.toString());	System.out.println(nativeSqlCount.toString());
		searchListJpql  = nativeSqlList.toString();
		searchCountJpql = nativeSqlCount.toString();
	}
	
	/**
	 * 刷新显示分机的表格
	 * @param isToFirst 刷新后是否跳转至第一页   true 第一页 ；false 当前页 */
	public void updateTable(Boolean isToFirst) {
		if (qflip != null) {
			qflip.setSearchSql(searchListJpql);
			qflip.setCountSql(searchCountJpql);
			if (isToFirst) {
				qflip.refreshToFirstPage();
			} else {
				qflip.refreshInCurrentPage();
			}
		} 
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_search_title) {
			treeQuery = false;
			this.tree.removeListener(this);
			this.tree.select(null);
			this.tree.addListener(this);
			query_tp = 1;				//1:标题搜索
			initializeSql();
			updateTable(true);
		}else if(source == bt_search_like){
			treeQuery = false;
			this.tree.removeListener(this);
			this.tree.select(null);
			this.tree.addListener(this);
			query_tp = 2;				//2:模糊搜素
			initializeSql();
			updateTable(true);
		}else if(source == bt_reset){
			resetQuery();				//重置查询
		}else if(source == bt_add_kbinfo){
			this.getApplication().getMainWindow().addWindow(new AddKbInfoWinodw(this));
		}else if(source == bt_manage_type){ //知识类型管理
			if(null == infoTypeManagement){
				infoTypeManagement = new KbInfoTypeManagement(this);
			}
			infoTypeManagement.refreshComponentInfo();
			this.getApplication().getMainWindow().addWindow(infoTypeManagement);
		}else if(source == bt_edit_kbinfo){
			showEditWindow();
		}else if(source == bt_delete_kbinfo){
			showDeleteWindow();
		}else if(source == bt_show){	//显示知识库
			if(null == showKbInfoViewWindow){
				showKbInfoViewWindow = new ShowKbInfoViewWindow(this.getApplication().getMainWindow().getBrowserWindowWidth());
			}
			showKbInfoViewWindow.refreshComponentInfo(this.getApplication().getMainWindow().getBrowserWindowWidth());
			this.getApplication().getMainWindow().removeWindow(showKbInfoViewWindow);
			this.getApplication().getMainWindow().addWindow(showKbInfoViewWindow);
		}
	}

	private void showEditWindow() {
		KbInfo target = (KbInfo) table.getValue();
		if(null == editKbInfoWinodw){
			editKbInfoWinodw = new EditKbInfoWinodw(this);
		}
		editKbInfoWinodw.refreshComponentInfo(target);
		this.getApplication().getMainWindow().addWindow(editKbInfoWinodw);
	}
	
	private void showDeleteWindow(){
		if(null == deleteKbInfoWinodw){
			deleteKbInfoWinodw = new DeleteKbInfoWinodw(this);
		}
		KbInfo target = (KbInfo) table.getValue();
		if(null != target){
			deleteKbInfoWinodw.refreshComponentInfo(target);
			this.getApplication().getMainWindow().addWindow(deleteKbInfoWinodw);
		}else{
			showWindowMsgInfo("请选中知识,再点击删除");
		}
	}
	
	/**刷新页面组件，表单**/
	public void refreshComponentInfo(){
		resetQuery();
		updateTreeData();
		initializeSql();
		updateTable(true);
	}
	
	/**重置表单**/
	private void resetQuery() {
		this.treeQuery = false;
		this.query_tp = 1;			//默认：标题查询
		this.tree.removeListener(this);
		this.tree.select(null);
		this.tree.addListener(this);
		this.tf_query.setValue("");
		this.createStartTime.setValue(null);
		this.createEndTime.setValue(null);
		this.lasetUpdateStartTime.setValue(null);
		this.lasetUpdateEndTime.setValue(null);
		this.table.removeAllItems();
		this.table.setValue(null);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table) {
			KbInfo target = (KbInfo) table.getValue();
 			bt_edit_kbinfo.setEnabled(target != null);		//编辑知识按钮
 			bt_delete_kbinfo.setEnabled(target != null);	//删除知识按钮
			if(null != target){								//填充知识内容	因ta是只读需要先设置只读为false在设置值
				setReadOnlyTa(target.getContent());
 			}else{
				setReadOnlyTa("");
			}
		}else if(source == tree){
			this.tf_query.setValue("");
			this.treeQuery = true;
			initializeSql();
			updateTable(true);
 			this.setReadOnlyTa("");
 		}
	}
	
	/**获得查询页面的表格组件**/
	public Table getTable() {
		return table;
	}
	
	private void setReadOnlyTa(String info){
		this.ta_content.setReadOnly(false);
		this.ta_content.setValue(info);
		this.ta_content.setReadOnly(true);
	}
	/**
	 * 显示提示信息 Notification
	 * @param msg
	 */
	private void showWindowMsgInfo(String msg){
		this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_HUMANIZED_MESSAGE);
	}
}
