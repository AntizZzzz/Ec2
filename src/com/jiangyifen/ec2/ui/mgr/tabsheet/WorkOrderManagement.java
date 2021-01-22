package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.WorkOrder;
import com.jiangyifen.ec2.service.eaoservice.WorkOrderService;
import com.jiangyifen.ec2.ui.mgr.workorder.AddWorkOrderWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 
 * @author lxy
 * 
 * <br>模块内容：后台工单管理主页面
 *
 */
public class WorkOrderManagement extends VerticalLayout implements Property.ValueChangeListener,ClickListener  {
	
	//静态
	private static final long serialVersionUID = 2238419925253504715L;
	//private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	private final Object[] COL_PROPERTIES = new Object[] {"marketingProject.projectName","title","workOrderPriority.name"};
	private final String[] COL_HEADERS = new String[] {"项目名称","工单标题","级别"};
	
	//持有UI对象
	private AddWorkOrderWindow addWorkOrderWindow;
	//本控件持有对象
	private BeanItemContainer<WorkOrder> workOrderContainer;
		
	//页面布局组件
	private HorizontalLayout 	topToolsLayout;		//顶部工具布局
	private HorizontalLayout 	contentLayout;		//中间内容布局
	private VerticalLayout 		leftContentlayout;	//左边内容布局
	private VerticalLayout 		rightContentLayout;	//右边内容布局
	private HorizontalLayout 	bottomToolsLayout;	//底部工具布局
	
	//页面控件
	private Panel leftContentPanel;
	private Panel rightContentPanel;
	
	private TextField tf_keyword_workorder;				//文本输入框
	private Button bt_search_workorder;					//搜索按钮
	private Table tb_my_workorder;						//我的工单
	
	private Button bt_edit_workorder;					//编辑工单
	
	private Button bt_add_workorder;					//添加工单

	
	
	//业务必须对象
	private Domain domain;
	private User loginUser;
	//业务本页对象
	//private WorkOrder workOrder;
	
	//业务注入对象
	private WorkOrderService workOrderService;
 		
 	/** 该类构造函数 */
	public WorkOrderManagement() {
 		initThis();
		initSpringContext();
		initCompanent();
	}
	/**初始化本控件整体属性 */
	private void initThis() {
 		this.setWidth("100%");	
		this.setMargin(false, true, true, true);
		this.setSpacing(true);
	}
	/**初始化Spring上写相关系想你 */
	private void initSpringContext(){
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		 
		workOrderService = SpringContextHolder.getBean("workOrderService");
	}
	
	/**初始化控件布局和组件 */
	private void initCompanent() {
		buildTopToolsLayout();			//顶部工具布局
		buildContentLayout();			//中间内容布局
		buildLeftContentlayout();		//左边内容布局
		buildRightContentLayout();		//右边内容布局
		buildBottomToolsLayout();		//底部工具布局
		 
		this.addComponent(topToolsLayout);
		leftContentlayout.addComponent(leftContentPanel);
		rightContentLayout.addComponent(rightContentPanel);
		contentLayout.addComponent(leftContentlayout);
		contentLayout.addComponent(rightContentLayout);
		this.addComponent(contentLayout);
		this.addComponent(bottomToolsLayout);
		
	}
	/**顶部工具布局 */
	private void buildTopToolsLayout(){
		topToolsLayout = new HorizontalLayout();
		topToolsLayout.setSpacing(true);
		
  		tf_keyword_workorder = new TextField();
  		tf_keyword_workorder.setMaxLength(10);
		topToolsLayout.addComponent(tf_keyword_workorder);
		
		bt_search_workorder = new Button("搜索", this);
		bt_search_workorder.setImmediate(true);
  		topToolsLayout.addComponent(bt_search_workorder);
		
	}	
	
	/**中间内容布局 */
	private void buildContentLayout(){
		contentLayout = new HorizontalLayout();
		contentLayout.setSpacing(true);
		contentLayout.setSizeFull();
		contentLayout.setWidth("100%");
	}
	
	/**左边内容布局 */
	private void buildLeftContentlayout(){
		leftContentlayout = new VerticalLayout();
		leftContentlayout.setSizeFull();
		leftContentlayout.setWidth("100%");
		leftContentPanel = new Panel();
		leftContentPanel.setCaption("最新工单日志");
	}
	
	/**右边内容布局 */
	private void buildRightContentLayout(){
		rightContentLayout = new VerticalLayout();
		rightContentLayout.setSizeFull();
		rightContentLayout.setWidth("100%");
		rightContentPanel = new Panel();
		rightContentPanel.setCaption("我的工单");
		
		tb_my_workorder = new Table();
		tb_my_workorder.setWidth("100%");
		tb_my_workorder.setHeight("100%");
		tb_my_workorder.setSelectable(true);
		tb_my_workorder.setImmediate(true);
		tb_my_workorder.addListener((Property.ValueChangeListener) this);
		workOrderContainer = new BeanItemContainer<WorkOrder>(WorkOrder.class);
		for(int i = 0; i < COL_PROPERTIES.length; i++) {
			workOrderContainer.addNestedContainerProperty(COL_PROPERTIES[i].toString());}
		workOrderContainer.removeAllItems();
		tb_my_workorder.setContainerDataSource(workOrderContainer);
 		tb_my_workorder.setVisibleColumns(COL_PROPERTIES);
		tb_my_workorder.setColumnHeaders(COL_HEADERS);
		rightContentPanel.addComponent(tb_my_workorder);
		
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		
		bt_edit_workorder = new Button("编辑工单",this);					//编辑工单
		hl.addComponent(bt_edit_workorder);
		rightContentPanel.addComponent(hl);
 	}
	
	/**底部工具布局 */
	private void buildBottomToolsLayout(){
		bottomToolsLayout = new HorizontalLayout();
		bt_add_workorder = new Button("添加工单", this);
  		bt_add_workorder.setImmediate(true);
  		bottomToolsLayout.addComponent(bt_add_workorder);
	}
	
	public void refreshCompanent(){
		updateTable();
		
	}

	private void updateTable(){
		List<WorkOrder> workOrderList = new ArrayList<WorkOrder>();
		workOrderList = workOrderService.getWorkOrderListBuDomainAndUser(domain,loginUser);
		workOrderContainer.removeAllItems();
		workOrderContainer.addAll(workOrderList);
	}
	//---------------------需要实现的接口方法------开始-----------------------------------
	/** 表格选择改变的监听器，设置按钮样式，状态信息*/
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source != null){
			
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_add_workorder) {
			showAddWorkOrderWindow();
		}else if(source == bt_edit_workorder){
			
		}
	}
	
	/** 显示添加工单窗口*/
	private void showAddWorkOrderWindow() {
		if(addWorkOrderWindow==null){
			addWorkOrderWindow = new AddWorkOrderWindow(this);
		}
		addWorkOrderWindow.refreshCompanent(this);
		this.getApplication().getMainWindow().addWindow(addWorkOrderWindow);
	}
	  
	//---------------------需要实现的接口方法------结束-----------------------------------
}

/**
			Object rs = source.getData();
			Info info = null;
			if(null != rs && rs instanceof Info){
				info = (Info)rs;
			}
			if(info != null){
				keyword.setValue(info.getName());
			}else{
				keyword.setValue("kkkkkkk");
			}
*/
