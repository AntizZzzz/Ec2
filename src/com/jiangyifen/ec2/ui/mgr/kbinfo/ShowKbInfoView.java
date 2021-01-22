package com.jiangyifen.ec2.ui.mgr.kbinfo;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.KbInfo;
import com.jiangyifen.ec2.entity.KbInfoType;
import com.jiangyifen.ec2.service.eaoservice.KbInfoService;
import com.jiangyifen.ec2.service.eaoservice.KbInfoTypeService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;


/**
 * 知识查询展示
 * @author lxy
 *
 */
public class ShowKbInfoView extends VerticalLayout implements Property.ValueChangeListener, ClickListener{

	//静态-非业务
	private static final long serialVersionUID = -5040426051485818805L;
	//private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	//静态-业务
	private final Object[] COL_PROPERTIES = new Object[] {"title"};
	private static final String TREE_PROPERTY_NAME = "NAME";
	private static final int PAGE_LENGTH = 5;			//一页数据条数
	//持有UI对象
	private HierarchicalContainer hwContainer = new HierarchicalContainer();
	
	//本控件持有对象	
	//页面布局组件
	private VerticalLayout hl_left;
	private VerticalLayout hl_right;
	//页面组件
	private FlipOverTableComponent<KbInfo>	qflip;	//本页面分页组件
	private Table table;							//表格组件
 
	private TextField tf_query;						//查询文本
 	private Button bt_search_like;					//模糊
	private Button bt_reset;						//重置
	private Label lb_msg;							//系提示信息
	private TextArea ta_content;					//显示知识类型
 	private Tree tree;
	
	// 弹出窗口 只创建一次
	
	//业务必须对象
 	private boolean treeQuery = false;
	//业务本页对象
	private Domain domain;		//当前用户
 	private String searchListJpql;				//检索列表Jpql
	private String searchCountJpql;				//检索总数Jpql
	 
	
	//业务注入对象
	private KbInfoService kbInfoService; 
	private KbInfoTypeService kbInfoTypeService;
	
	public ShowKbInfoView(){
		initThis();
		initSpringContext();
		initCompanent();
	}
	
	/**初始化本控件整体属性 */
	private void initThis() {
		this.setMargin(false,true,false,true);
 		this.setSizeFull();
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
		hl_left.setWidth("150px");
		
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
	
	private void updateTreeData(){
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
	
	private void buildTopToolsLayout(){
		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout1 = new HorizontalLayout();
		searchHLayout1.setSpacing(true);
		tf_query = new TextField();
		tf_query.setInputPrompt("关键字");
		tf_query.setImmediate(true);
		tf_query.addListener(this);
		searchHLayout1.addComponent(tf_query);
		bt_search_like = new Button("模糊搜索", this);
		bt_search_like.setImmediate(true);
		searchHLayout1.addComponent(bt_search_like);
		lb_msg = new Label("",Label.CONTENT_XHTML);
		searchHLayout1.addComponent(lb_msg);
		hl_right.addComponent(searchHLayout1);
	 
	}
	
	/** 创建分机表格显示区组件*/
	private void buildTableLayout(){
		table = createFormatColumnTable();
		table.setWidth("100%");	
		table.setHeight("95px");	
 		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		table.addStyleName("striped");
		table.setPageLength(PAGE_LENGTH);	
		table.setColumnExpandRatio("title", 1.0f);
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
					if(null != kit) {
						KbInfoType kittmp = (KbInfoType)kit;
						if(null != kittmp){
							return kittmp.getName(); 
						}
					}else{
						return "根目录";
					}
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
		initializeSql();	// 初始化搜索语句和分页组件
		qflip = new FlipOverTableComponent<KbInfo>(KbInfo.class, kbInfoService, table, searchListJpql, searchCountJpql, null);
		qflip.setPageLength(PAGE_LENGTH, true);
		table.setVisibleColumns(COL_PROPERTIES);
  		footerHLayout.addComponent(qflip);
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
	
	/** 根据不同的需求，初始化查询语句 */
	private void initializeSql() {
		StringBuffer nativeSqlList = new StringBuffer();
		StringBuffer nativeSqlCount = new StringBuffer();
		StringBuffer nativeSql = new StringBuffer();
		StringBuffer idsSql = new StringBuffer();
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
			if(kitid > 0 ) {//树形
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
			if(!WorkUIUtils.stringIsEmpty(this.tf_query.getValue())){
				nativeSql.append(" and (s.title like '%" + this.tf_query.getValue().toString()+"%' or s.content like '%" + this.tf_query.getValue().toString()+"%')");
			}
		}
		
		nativeSql.append(" and s.domain.id = "+domain.getId());
		nativeSqlList.append(nativeSql);
		nativeSqlList.append(" order by s.level desc ,s.createDate desc");
		nativeSqlCount.append(nativeSql);
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
		if(source == bt_search_like){
			treeQuery = false;
			this.tree.removeListener(this);
			this.tree.select(null);
			this.tree.addListener(this);
			initializeSql();
			updateTable(true);
		}else if(source == bt_reset){
			resetQuery();
		}
	}
	 
	public void refreshComponentInfo(){
		resetQuery();
		updateTreeData();
		initializeSql();
		updateTable(true);
		this.setReadOnlyTa("");
	}
	
	private void resetQuery() {
		this.treeQuery = false;
		this.tree.removeListener(this);
		this.tree.select(null);
		this.tree.addListener(this);
 		this.tf_query.setValue("");
		this.table.removeAllItems();
		this.table.setValue(null);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table) {
			KbInfo target = (KbInfo) table.getValue();
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
	
	private void setReadOnlyTa(String info){
		this.ta_content.setReadOnly(false);
		this.ta_content.setValue(info);
		this.ta_content.setReadOnly(true);
	}
	
	public Table getTable() {
		return table;
	}
}
