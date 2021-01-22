package com.jiangyifen.ec2.ui.mgr.customermigratelog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MigrateCustomerLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 客户资源迁移日志简单过滤器
 * @author jrh
 */
@SuppressWarnings("serial")
public class CustomerMigrateSimpleFilter extends VerticalLayout implements ClickListener {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private TextField customerName;			// 客户姓名输入文本框
	private NativeSelect migrateScope;		// “迁移时间”选择框
	private PopupDateField startTime;		// “开始时间”选择框
	private PopupDateField finishTime;		// “截止时间”选择框

	private TextField customerPhone;		// 客户电话输入文本框
	private TextField operatorEmpNo;		// 迁移者工号输入文本框
	private TextField oldManagerEmpNo;		// 原经理工号输入文本框
	private TextField newManagerEmpNo;		// 新经理工号输入文本框

	private Button searchButton;			// 刷新结果按钮
	private Button clearButton;				// 清空输入内容
	private PopupView complexSearchView;	// 复杂搜索界面
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private User loginUser; 				// 当前的登陆用户
	private Domain domain;					// 当前用户所属域
	private String deptSearchSql;			// 初始化部门搜索语句

	private CustomerMigrateComplexFilter customerMigrateComplexFilter;
	private FlipOverTableComponent<MigrateCustomerLog> migrateLogTableFlip;		// 客户迁移日志管理Tab 页的翻页组件
	
	private DepartmentService departmentService;	// 部门服务类
	
	public CustomerMigrateSimpleFilter() {
		this.setSpacing(true);

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		departmentService = SpringContextHolder.getBean("departmentService");
		
		gridLayout = new GridLayout(5, 2);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 初始化部门搜索语句
		deptSearchSql = createDeptSearchSql();

		//--------- 第一行  -----------//
		this.createCustomerNameHLayout();
		this.createMigrateDateScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		
		//--------- 第二行  -----------//
		this.createCustomerPhoneHLayout();
		this.createOperatorEmpNoHLayout();
		this.createOldManagerEmpNoHLayout();
		this.createNewManagerEmpNoHLayout();
		
		//--------- 创建操作按钮 -----------//
		this.createOperateButtons();
	}

	/**
	 * 初始化部门搜索范围语句
	 * @return
	 */
	private String createDeptSearchSql() {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
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
		// 根据当前用户所属部门，及其下属部门，创建动态查询语句
		String deptIdSql = "";
		for(int i = 0; i < allGovernedDeptIds.size(); i++) {
			if( i == (allGovernedDeptIds.size() - 1) ) {
				deptIdSql += allGovernedDeptIds.get(i);
			} else {
				deptIdSql += allGovernedDeptIds.get(i) +", ";
			}
		}
		String sql = " and m.operatorDeptId in (" +deptIdSql+ ")";
		
		return sql;
	}

	/**
	 * 创建操作按钮
	 */
	private void createOperateButtons() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		gridLayout.addComponent(layout, 4, 0);
		
		searchButton = new Button("查 询");
		searchButton.setDescription("快捷键(Ctrl+Q)");
		searchButton.addStyleName("default");
		searchButton.addListener(this);
		searchButton.setWidth("60px");
		searchButton.setClickShortcut(KeyCode.Q, ModifierKey.CTRL);
		layout.addComponent(searchButton);
		
		clearButton = new Button("清 空");
		clearButton.setDescription("快捷键(Ctrl+L)");
		clearButton.addListener(this);
		clearButton.setWidth("60px");
		clearButton.setClickShortcut(KeyCode.L, ModifierKey.CTRL);
		layout.addComponent(clearButton);
		
		customerMigrateComplexFilter = new CustomerMigrateComplexFilter();
		customerMigrateComplexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(customerMigrateComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 4, 1);
		gridLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 创建 存放“客户姓名” 的布局管理器
	 */
	private void createCustomerNameHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 0, 0);
		
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		customerName = new TextField();
		customerName.setWidth("120px");	
		customerNameHLayout.addComponent(customerName);
	}

	/**
	 * 创建  存放“时间范围标签和其选择框” 的布局管理器
	 */
	private void createMigrateDateScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 1, 0);
		
		Label timeScopeLabel = new Label("迁移时间：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		migrateScope = new NativeSelect();
		migrateScope.setImmediate(true);
		migrateScope.addItem("今天");
		migrateScope.addItem("昨天");
		migrateScope.addItem("本周");
		migrateScope.addItem("上周");
		migrateScope.addItem("本月");
		migrateScope.addItem("上月");
		migrateScope.addItem("精确时间");
		migrateScope.setValue("本月");
		migrateScope.setWidth("133px");
		migrateScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(migrateScope);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)migrateScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startTime.removeListener(startTimeListener);
				finishTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startTime.setValue(dates[0]);
				finishTime.setValue(dates[1]);
				startTime.addListener(startTimeListener);
				finishTime.addListener(finishTimeListener);
			}
		};
		migrateScope.addListener(timeScopeListener);
	}

	/**
	 * 创建  存放“开始时间标签和其选择框” 的布局管理器
	 */
	private void createStartTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
	
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 2, 0);
				
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		startTimeHLayout.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				migrateScope.removeListener(timeScopeListener);
				migrateScope.setValue("精确时间");
				migrateScope.addListener(timeScopeListener);
			}
		};
		
		startTime = new PopupDateField();
		startTime.setWidth("153px");
		startTime.setImmediate(true);
		startTime.setValue(dates[0]);
		startTime.addListener(startTimeListener);
		startTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime.setParseErrorMessage("时间格式不合法");
		startTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startTimeHLayout.addComponent(startTime);
	}

	/**
	 * 创建  存放“截止时间标签和其选择框” 的布局管理器
	 */
	private void createFinishTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
		
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 3, 0);
		
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		finishTimeHLayout.addComponent(finishTimeLabel);
		
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				migrateScope.removeListener(finishTimeListener);
				migrateScope.setValue("精确时间");
				migrateScope.addListener(timeScopeListener);
			}
		};
		
		finishTime = new PopupDateField();
		finishTime.setImmediate(true);
		finishTime.setWidth("153px");
		finishTime.setValue(dates[1]);
		finishTime.addListener(finishTimeListener);
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setParseErrorMessage("时间格式不合法");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTimeHLayout.addComponent(finishTime);
	}

	/**
	 * 创建 存放“联系电话” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout telephoneHLayout = new HorizontalLayout();
		telephoneHLayout.setSpacing(true);
		gridLayout.addComponent(telephoneHLayout, 0, 1);
		
		Label telephoneLabel = new Label("客户电话：");
		telephoneLabel.setWidth("-1px");
		telephoneHLayout.addComponent(telephoneLabel);
		
		customerPhone = new TextField();
		customerPhone.setWidth("120px");
		telephoneHLayout.addComponent(customerPhone);
	}

	/**
	 * 创建 存放“客户姓名” 的布局管理器
	 */
	private void createOperatorEmpNoHLayout() {
		HorizontalLayout managerEmpNoHLayout = new HorizontalLayout();
		managerEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(managerEmpNoHLayout, 1, 1);
		
		// 客户姓名输入区
		Label managerEmpNoLabel = new Label("迁移者工号：");
		managerEmpNoLabel.setWidth("-1px");
		managerEmpNoHLayout.addComponent(managerEmpNoLabel);
		
		operatorEmpNo = new TextField();
		operatorEmpNo.setWidth("120px");	
		managerEmpNoHLayout.addComponent(operatorEmpNo);
	}
	
	/**
	 * 创建 存放“经理姓名” 的布局管理器
	 */
	private void createOldManagerEmpNoHLayout() {
		HorizontalLayout managerNameHLayout = new HorizontalLayout();
		managerNameHLayout.setSpacing(true);
		gridLayout.addComponent(managerNameHLayout, 2, 1);
		
		// 客户姓名输入区
		Label managerNameLabel = new Label("原经理工号：");
		managerNameLabel.setWidth("-1px");
		managerNameHLayout.addComponent(managerNameLabel);
		
		oldManagerEmpNo = new TextField();
		oldManagerEmpNo.setWidth("140px");	
		managerNameHLayout.addComponent(oldManagerEmpNo);
	}
	
	/**
	 * 创建 存放“公司名称” 的布局管理器
	 */
	private void createNewManagerEmpNoHLayout() {
		HorizontalLayout companyHLayout = new HorizontalLayout();
		companyHLayout.setSpacing(true);
		gridLayout.addComponent(companyHLayout, 3, 1);
		
		Label campanyLabel = new Label("新经理工号：");
		campanyLabel.setWidth("-1px");
		companyHLayout.addComponent(campanyLabel);
		
		newManagerEmpNo = new TextField();
		newManagerEmpNo.setWidth("140px");
		companyHLayout.addComponent(newManagerEmpNo);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(startTime.getValue() == null || finishTime.getValue() == null) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间和截止时间都不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
//			if(dialCountMoreThan.isValid() == false || dialCountLessThan.isValid() == false) {
//				this.getApplication().getMainWindow().showNotification("联系次数搜索输入框中只能输入数字！", Notification.TYPE_WARNING_MESSAGE);
//				return;
//			}
			
			// 处理搜索事件
			handleSearchEvent();
		} else if(source == clearButton) {
			excuteClearValues();
		}
	}
	
	/**
	 * 执行清空操作
	 */
	private void excuteClearValues() {
		operatorEmpNo.setValue("");
		oldManagerEmpNo.setValue("");
		migrateScope.select("本月");
		customerName.setValue("");
		customerPhone.setValue("");
		newManagerEmpNo.setValue("");
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(m) from MigrateCustomerLog as m where m.domainId = " +domain.getId() + deptSearchSql + createDynamicSql();
		String searchSql = countSql.replaceFirst("count\\(m\\)", "m") + " order by m.migratedDate desc";
		migrateLogTableFlip.setSearchSql(searchSql);
		migrateLogTableFlip.setCountSql(countSql);
		migrateLogTableFlip.refreshToFirstPage();
	}

	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createDynamicSql() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 客户姓名查询
		String customerNameSql = "";
		String inputCustomerName = customerName.getValue().toString().trim();
		if(!"".equals(inputCustomerName)) {
			customerNameSql = " and m.customerName like '%" + inputCustomerName + "%'";
		}
		
		// 客户联系方式查询
		String customerPhoneSql = "";
		String phoneNumber = customerPhone.getValue().toString().trim();
		if(!"".equals(phoneNumber)) {
			customerPhoneSql = " and m.customerDefaultPhone like '%" + phoneNumber + "%'";
		}

		// 迁移者工号查询
		String operatorEmpNoSql = "";
		String inputEmpNo = operatorEmpNo.getValue().toString().trim();
		if(!"".equals(inputEmpNo)) {
			operatorEmpNoSql = " and m.operatorEmpNo like '%" + inputEmpNo + "%'";
		}
		
		// 原经理工号查询
		String oldManagerEmpNoSql = "";
		String inputOldEmpNo = oldManagerEmpNo.getValue().toString().trim();
		if(!"".equals(inputOldEmpNo)) {
			oldManagerEmpNoSql = " and m.oldManagerEmpNo like '%" + inputOldEmpNo + "%'";
		}
		
		// 新经理工号查询
		String newManagerEmpNoSql = "";
		String inputNewEmpNo = newManagerEmpNo.getValue().toString().trim();
		if(!"".equals(inputNewEmpNo)) {
			newManagerEmpNoSql = " and m.newManagerEmpNo like '%" + inputNewEmpNo + "%'";
		}
		
		// 资源的导入日期查询
		String specificStartImportTimeSql = "";
		if(startTime.getValue() != null) {
			specificStartImportTimeSql = " and m.migratedDate >= '" + dateFormat.format(startTime.getValue()) +"'";
		} 
		
		String specificFinishImportTimeSql = "";
		if(finishTime.getValue() != null) {
			specificFinishImportTimeSql = " and m.migratedDate <= '" + dateFormat.format(finishTime.getValue()) +"'";
		}
		
		// 创建固定的搜索语句
		return customerNameSql + customerPhoneSql + operatorEmpNoSql + oldManagerEmpNoSql + newManagerEmpNoSql  
				+ specificStartImportTimeSql + specificFinishImportTimeSql;
	}

	public void setMigrateLogTableFlip(FlipOverTableComponent<MigrateCustomerLog> migrateLogTableFlip) {
		this.migrateLogTableFlip = migrateLogTableFlip;
		customerMigrateComplexFilter.setMigrateLogTableFlip(migrateLogTableFlip);
	}
	
}
