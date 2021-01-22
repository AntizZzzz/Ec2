package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class HistoryOrderDetailFilter extends VerticalLayout implements ClickListener, Content {
	
	// 用户是否有权限查看历史订单记录
	private final String TASK_MANAGEMENT_HISTORY_ORDER = "task_management&history_order";
	
	private GridLayout gridLayout;				// 面板中的布局管理器
	
	private ComboBox createTimeScope;			// “下单时间”选择框
	private PopupDateField startCreateTime;		// “起始联系”选择框
	private PopupDateField finishCreateTime;	// “截止联系”选择框

	private TextField commodityName_tf;			// 按商品名称搜索
	private TextField orderId_tf;				// 按定当编号搜索
	
	private Button searchButton;				// 刷新结果按钮
	private Button clearButton;					// 清空输入内容

	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
	
	private FlipOverTableComponent<Orderdetails> tableFlipOver;			// 历史订单详情Table的翻页组件
	
	private CustomerResource customerResource;								// 当前选中任务对应的客户资源
	private DepartmentService departmentService;							// 部门服务类
	
	private User loginUser; 												// 当前的登陆用户
	private RoleType roleType;												// 角色类型
	private ArrayList<String> ownBusinessModels;							// 当前登陆用户目前使用的角色类型所对应的所有权限
	private String searchByCreatorSpanSql = "";								// 按创建者所属部门进行查询客服记录[设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史客服记录，默认为可以]

	public HistoryOrderDetailFilter(User loginUser, RoleType roleType) {
		// 初始化各种参数
		this.setSpacing(true);
		this.loginUser = loginUser;
		this.roleType = roleType;

		departmentService = SpringContextHolder.getBean("departmentService");
		
		// 获取当前登陆用户所有权限
		ownBusinessModels = new ArrayList<String>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.csr)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			} else if(role.getType().equals(RoleType.manager)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			}
		}
		
		// 初始化客服记录按部门查询的语句
		initializeSearchByCreatorDeptIdSql(roleType);
		
		// 创建主要组件
		gridLayout = new GridLayout(7, 2);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true);
		this.addComponent(gridLayout);
		
		this.createFilterHLayout1();
		this.createFilterHLayout2();
		this.createButtons();
	}

	/**
	 * 初始化客服记录按部门查询的语句
	 * @param roleType	当前登陆用户目前正使用的角色类型【csr、manager】
	 */
	private void initializeSearchByCreatorDeptIdSql(RoleType roleType) {
		// 获取全局配置，看当前话务员是否能够看到其他部门的坐席针对同一资源做的客服记录
		Map<String,Boolean> domainConfigs = ShareData.domainToConfigs.get(loginUser.getDomain().getId());
		Boolean viewRecordSpanDeptAble = domainConfigs.get("csr_view_order_span_department");
		if(roleType.equals(RoleType.csr)) {
			if(viewRecordSpanDeptAble == null) {	// 查看整个域针对该客户做的记录
				searchByCreatorSpanSql = "";
			} else if(!viewRecordSpanDeptAble) {	// 查看当前坐席针对该客户做的记录
				searchByCreatorSpanSql = " and o.order.csrUserId = "+loginUser.getId();
			} else {	// 查看当前坐席所属部门，整个部门下针对该客户做的记录
				searchByCreatorSpanSql = " and EXISTS ( select u.id from User as u where u.id = o.order.csrUserId and u.department.id = "+loginUser.getDepartment().getId()+" ) ";
			}
		} else if(roleType.equals(RoleType.manager)) {	// 如果是管理员调用，则只能看到自己管辖部门下的坐席创建的客服记录
			// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
			List<Long> allGovernedDeptIds = new ArrayList<Long>();
			for (Role role : loginUser.getRoles()) {
				if (role.getType().equals(RoleType.manager)) {
					List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
					if(departments.isEmpty()) {
						allGovernedDeptIds.add(0L);
					} else {
						for (Department dept : departments) {
							Long deptId = dept.getId();
							if (!allGovernedDeptIds.contains(deptId)) {
								allGovernedDeptIds.add(deptId);
							}
						}
					}
				}
			}
			// 根据当前用户所属部门，及其下属部门，创建动态查询语句, allGovernedDeptIds 至少会含有一个 id 为 0 的元素
			String deptIdsStr = "";
			for (int i = 0; i < allGovernedDeptIds.size(); i++) {
				if (i == (allGovernedDeptIds.size() - 1)) {
					deptIdsStr += allGovernedDeptIds.get(i);
				} else {
					deptIdsStr += allGovernedDeptIds.get(i) + ",";
				}
			}
			searchByCreatorSpanSql = " and o.order.csrUserId in ( select id from User as u where u.department.id in (" + deptIdsStr+ ") )";
		}
	}

	/**
	 * 创建第一行搜索组件
	 */
	private void createFilterHLayout1() {
		// 时间范围选中框
		Label timeScopeLabel = new Label("下单时间：");
		timeScopeLabel.setWidth("-1px");
		gridLayout.addComponent(timeScopeLabel, 0, 0);
		
		createTimeScope = new ComboBox();
		createTimeScope.addItem("今天");
		createTimeScope.addItem("昨天");
		createTimeScope.addItem("本周");
		createTimeScope.addItem("上周");
		createTimeScope.addItem("本月");
		createTimeScope.addItem("上月");
		createTimeScope.addItem("精确时间");
		createTimeScope.setValue("今天");
		createTimeScope.setWidth("110px");
		createTimeScope.setImmediate(true);
		createTimeScope.setNullSelectionAllowed(false);
		gridLayout.addComponent(createTimeScope, 1, 0);

		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)createTimeScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startCreateTime.removeListener(startTimeListener);
				finishCreateTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startCreateTime.setValue(dates[0]);
				finishCreateTime.setValue(dates[1]);
				startCreateTime.addListener(startTimeListener);
				finishCreateTime.addListener(finishTimeListener);
			}
		};
		createTimeScope.addListener(timeScopeListener);

		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		// 开始时间选中框
		Label startTimeLabel = new Label("起始联系：");
		startTimeLabel.setWidth("-1px");
		gridLayout.addComponent(startTimeLabel, 2, 0);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				createTimeScope.removeListener(timeScopeListener);
				createTimeScope.setValue("精确时间");
				createTimeScope.addListener(timeScopeListener);
			}
		};

		startCreateTime = new PopupDateField();
		startCreateTime.setImmediate(true);
		startCreateTime.setWidth("150px");
		startCreateTime.setValue(dates[0]);
		startCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startCreateTime.setParseErrorMessage("时间格式不合法");
		startCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startCreateTime.addListener(startTimeListener);
		gridLayout.addComponent(startCreateTime, 3, 0);
		
		// 截止时间选中框
		Label finishTimeLabel = new Label("截止联系：");
		finishTimeLabel.setWidth("-1px");
		gridLayout.addComponent(finishTimeLabel, 4, 0);

		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				createTimeScope.removeListener(finishTimeListener);
				createTimeScope.setValue("精确时间");
				createTimeScope.addListener(timeScopeListener);
			}
		};
		
		finishCreateTime = new PopupDateField();
		finishCreateTime.setImmediate(true);
		finishCreateTime.setWidth("150px");
		finishCreateTime.setValue(dates[1]);
		finishCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishCreateTime.setParseErrorMessage("时间格式不合法");
		finishCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishCreateTime.addListener(finishTimeListener);
		gridLayout.addComponent(finishCreateTime, 5, 0);
	}

	/**
	 * 创建第一行搜索组件
	 */
	private void createFilterHLayout2() {
		Label commodityName_lb = new Label("商品名称：");
		commodityName_lb.setWidth("-1px");
		gridLayout.addComponent(commodityName_lb, 0, 1);
		
		commodityName_tf = new TextField();
		commodityName_tf.setWidth("110px");
		gridLayout.addComponent(commodityName_tf, 1, 1);

		Label orderId_lb = new Label("订单编号：");
		orderId_lb.setWidth("-1px");
		gridLayout.addComponent(orderId_lb, 2, 1);
		
		orderId_tf = new TextField();
		orderId_tf.setWidth("124px");
		orderId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		orderId_tf.setValidationVisible(false);
		gridLayout.addComponent(orderId_tf, 3, 1);
	}
	
	/**
	 * 创建操作按钮
	 */
	private void createButtons() {
		// 查询按钮
		searchButton = new Button("查 询", (ClickListener)this);
		searchButton.setStyleName("default");
		gridLayout.addComponent(searchButton, 6, 0);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
		
		// 清空按钮
		clearButton = new Button("清 空");
		clearButton.addListener(this);
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
	}

	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			// 如果是坐席角色类型，则只有当其拥有相应的权限的时候，才能查看历史服务记录
			if(roleType.equals(RoleType.csr) && !ownBusinessModels.contains(TASK_MANAGEMENT_HISTORY_ORDER)) {
				searchButton.getApplication().getMainWindow().showNotification("对不起，您无权查看客户订单详情", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			if(!orderId_tf.isValid()) {
				orderId_tf.getApplication().getMainWindow().showNotification("订单编号只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			String countSql = createCountSql();
			String searchSql = countSql.replaceFirst("count\\(o\\)", "o") + " order by o.id desc ";
			tableFlipOver.setSearchSql(searchSql);
			tableFlipOver.setCountSql(countSql);
			tableFlipOver.refreshToFirstPage();
		} else if(source == clearButton) {
			startCreateTime.setValue(null);
			finishCreateTime.setValue(null);
			commodityName_tf.setValue("");
			orderId_tf.setValue("");
		}
	}

	private String  createCountSql() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String specificStartTimeSql = "";
		if(startCreateTime.getValue() != null) {
			specificStartTimeSql = " and o.order.generateDate >= '" + dateFormat.format(startCreateTime.getValue()) +"' ";
		}

		String specificFinishTimeSql = "";
		if(finishCreateTime.getValue() != null) {
			specificFinishTimeSql = " and o.order.generateDate <= '" + dateFormat.format(finishCreateTime.getValue()) +"' ";
		}

		String commodityNameSql = "";
		String commodityName = StringUtils.trimToEmpty((String) commodityName_tf.getValue());
		if(!"".equals(commodityName)) {
			commodityNameSql = " and o.commodityName like '%"+commodityName+"%' ";
		}
		
		String orderIdSql = "";
		String orderId = StringUtils.trimToEmpty((String) orderId_tf.getValue());
		if(!"".equals(orderId)) {
			orderIdSql = " and o.order.id = "+orderId;
		}
		
		String sql = orderIdSql + commodityNameSql + specificStartTimeSql + specificFinishTimeSql;
		return "select count(o) from Orderdetails as o where o.order.customerResource.id = " + customerResource.getId() + sql + searchByCreatorSpanSql;
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级搜索";
	}

	@Override
	public Component getPopupComponent() {
		return gridLayout;
	}
	
	public void setTableFlipOver(FlipOverTableComponent<Orderdetails> tableFlipOver) {
		this.tableFlipOver = tableFlipOver;
	}
	
	// 我的任务左侧任务显示表格中选中项，对应的资源对象
	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

}
