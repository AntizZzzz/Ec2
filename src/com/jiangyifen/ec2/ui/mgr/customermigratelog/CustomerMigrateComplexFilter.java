package com.jiangyifen.ec2.ui.mgr.customermigratelog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 客户资源迁移日志复杂过滤器
 * @author jrh
 */
@SuppressWarnings("serial")
public class CustomerMigrateComplexFilter extends VerticalLayout implements ClickListener, Content {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	//--------------  第一行  --------------// 
	private NativeSelect migrateScope;		// “迁移时间”选择框
	private TextField customerName;			// 客户姓名输入文本框
	private TextField operatorEmpNo;		// 迁移者工号输入文本框
	private TextField oldManagerEmpNo;		// 原经理工号输入文本框
	private TextField newManagerEmpNo;		// 新经理工号输入文本框

	//--------------  第二行  --------------// 
	private PopupDateField startTime;		// “开始时间”选择框
	private TextField customerPhone;		// 客户手机输入文本框
	private TextField operatorName;			// 迁移者姓名输入文本框
	private TextField oldManagerName;		// 原经理姓名输入文本框
	private TextField newManagerName;		// 新经理姓名输入文本框
	
	//--------------  第三行  --------------// 
	private PopupDateField finishTime;		// “截止时间”选择框
	private TextField customerId_tf;		// 客户编号输入文本框
	private TextField operatorDept;			// 迁移者用户名输入文本框
	private TextField oldManagerDept;		// 原经理用户名输入文本框
	private TextField newManagerDept;		// 新经理用户名输入文本框

	//--------------  第四行  --------------// 
	private TextField companyName;			// 公司名称输入文本框

	private Button searchButton;			// 刷新结果按钮
	private Button clearButton;				// 清空输入内容
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private User loginUser; 				// 当前的登陆用户
	private Domain domain;					// 当前用户所属域
	private String deptSearchSql;			// 部门范围搜索语句

	private FlipOverTableComponent<MigrateCustomerLog> migrateLogTableFlip;		// 客户迁移日志管理Tab 页的翻页组件
	
	private DepartmentService departmentService;		// 部门服务类
	
	public CustomerMigrateComplexFilter() {
		this.setSpacing(true);

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		departmentService = SpringContextHolder.getBean("departmentService");
		
		gridLayout = new GridLayout(6, 4);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 初始化部门搜索语句
		deptSearchSql = createDeptSearchSql();

		//--------- 第一行  -----------//
		this.createMigrateScopeHLayout();
		this.createCustomerNameHLayout();
		this.createOperatorEmpNoHLayout();
		this.createOldManagerEmpNoHLayout();
		this.createNewManagerEmpNoHLayout();

		//--------- 第二行  -----------//
		this.createStartTimeHLayout();
		this.createCustomerPhoneHLayout();
		this.createOperatorNameHLayout();
		this.createOldManagerNameHLayout();
		this.createNewManagerNameHLayout();

		//--------- 第三行  -----------//
		this.createFinishTimeHLayout();
		this.createCustomerIdHLayout();
		this.createOperatorDeptHLayout();
		this.createOldManagerDeptHLayout();
		this.createNewManagerDeptHLayout();
		
		//--------- 第四行  -----------//
		this.createCompanyNameHLayout();

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
		searchButton = new Button("查 询");
		searchButton.addStyleName("default");
		searchButton.addListener(this);
		searchButton.setWidth("60px");
		gridLayout.addComponent(searchButton, 5, 0);
		
		clearButton = new Button("清 空");
		clearButton.addListener(this);
		clearButton.setWidth("60px");
		gridLayout.addComponent(clearButton, 5, 1);
	}
	
	/**
	 * 创建  存放“迁移时间范围标签和其选择框” 的布局管理器
	 */
	private void createMigrateScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 0, 0);
		
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
		migrateScope.setWidth("153px");
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
	 * 创建 存放“客户姓名” 的布局管理器
	 */
	private void createCustomerNameHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 1, 0);
		
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		customerName = new TextField();
		customerName.setWidth("110px");	
		customerNameHLayout.addComponent(customerName);
	}

	/**
	 * 创建 存放“迁移者工号” 的布局管理器
	 */
	private void createOperatorEmpNoHLayout() {
		HorizontalLayout operatorEmpNoHLayout = new HorizontalLayout();
		operatorEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(operatorEmpNoHLayout, 2, 0);
		
		// 迁移者工号输入区
		Label operatorEmpNoLabel = new Label("迁移者工号：");
		operatorEmpNoLabel.setWidth("-1px");
		operatorEmpNoHLayout.addComponent(operatorEmpNoLabel);
		
		operatorEmpNo = new TextField();
		operatorEmpNo.setWidth("110px");	
		operatorEmpNoHLayout.addComponent(operatorEmpNo);
	}
	
	/**
	 * 创建 存放“原经理工号” 的布局管理器
	 */
	private void createOldManagerEmpNoHLayout() {
		HorizontalLayout oldManagerEmpNoHLayout = new HorizontalLayout();
		oldManagerEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(oldManagerEmpNoHLayout, 3, 0);
		
		// 原经理工号输入区
		Label oldManagerEmpNoLabel = new Label("原经理工号：");
		oldManagerEmpNoLabel.setWidth("-1px");
		oldManagerEmpNoHLayout.addComponent(oldManagerEmpNoLabel);
		
		oldManagerEmpNo = new TextField();
		oldManagerEmpNo.setWidth("110px");	
		oldManagerEmpNoHLayout.addComponent(oldManagerEmpNo);
	}

	/**
	 * 创建 存放“新经理工号” 的布局管理器
	 */
	private void createNewManagerEmpNoHLayout() {
		HorizontalLayout newManagerEmpNoHLayout = new HorizontalLayout();
		newManagerEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(newManagerEmpNoHLayout, 4, 0);
		
		// 新经理工号输入区
		Label newManagerEmpNoLabel = new Label("新经理工号：");
		newManagerEmpNoLabel.setWidth("-1px");
		newManagerEmpNoHLayout.addComponent(newManagerEmpNoLabel);
		
		newManagerEmpNo = new TextField();
		newManagerEmpNo.setWidth("110px");	
		newManagerEmpNoHLayout.addComponent(newManagerEmpNo);
	}

	/**
	 * 创建  存放“开始时间标签和其选择框” 的布局管理器
	 */
	private void createStartTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
	
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 0, 1);
				
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
	 * 创建 存放“客户电话” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout telephoneHLayout = new HorizontalLayout();
		telephoneHLayout.setSpacing(true);
		gridLayout.addComponent(telephoneHLayout, 1, 1);

		// 客户电话输入区
		Label telephoneLabel = new Label("客户电话：");
		telephoneLabel.setWidth("-1px");
		telephoneHLayout.addComponent(telephoneLabel);
		
		customerPhone = new TextField();
		customerPhone.setWidth("110px");
		telephoneHLayout.addComponent(customerPhone);
	}

	/**
	 * 创建 存放“迁移者姓名” 的布局管理器
	 */
	private void createOperatorNameHLayout() {
		HorizontalLayout operatorNameHLayout = new HorizontalLayout();
		operatorNameHLayout.setSpacing(true);
		gridLayout.addComponent(operatorNameHLayout, 2, 1);
		
		// 迁移者姓名输入区
		Label operatorNameLabel = new Label("迁移者姓名：");
		operatorNameLabel.setWidth("-1px");
		operatorNameHLayout.addComponent(operatorNameLabel);
		
		operatorName = new TextField();
		operatorName.setWidth("110px");	
		operatorNameHLayout.addComponent(operatorName);
	}
	
	/**
	 * 创建 存放“原经理姓名” 的布局管理器
	 */
	private void createOldManagerNameHLayout() {
		HorizontalLayout oldManagerNameHLayout = new HorizontalLayout();
		oldManagerNameHLayout.setSpacing(true);
		gridLayout.addComponent(oldManagerNameHLayout, 3, 1);
		
		// 原经理姓名输入区
		Label oldManagerNameLabel = new Label("原经理姓名：");
		oldManagerNameLabel.setWidth("-1px");
		oldManagerNameHLayout.addComponent(oldManagerNameLabel);
		
		oldManagerName = new TextField();
		oldManagerName.setWidth("110px");	
		oldManagerNameHLayout.addComponent(oldManagerName);
	}
	
	/**
	 * 创建 存放“新经理姓名” 的布局管理器
	 */
	private void createNewManagerNameHLayout() {
		HorizontalLayout newManagerNameHLayout = new HorizontalLayout();
		newManagerNameHLayout.setSpacing(true);
		gridLayout.addComponent(newManagerNameHLayout, 4, 1);
		
		// 新经理姓名输入区
		Label newManagerNameLabel = new Label("新经理姓名：");
		newManagerNameLabel.setWidth("-1px");
		newManagerNameHLayout.addComponent(newManagerNameLabel);
		
		newManagerName = new TextField();
		newManagerName.setWidth("110px");	
		newManagerNameHLayout.addComponent(newManagerName);
	}
	
	/**
	 * 创建  存放“截止时间标签和其选择框” 的布局管理器
	 */
	private void createFinishTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
		
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 0, 2);
		
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
		finishTime.setWidth("154px");
		finishTime.setValue(dates[1]);
		finishTime.addListener(finishTimeListener);
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setParseErrorMessage("时间格式不合法");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTimeHLayout.addComponent(finishTime);
	}
	
	/**
	 * 创建 存放“公司名称” 的布局管理器
	 */
	private void createCustomerIdHLayout() {
		HorizontalLayout customerIdHLayout = new HorizontalLayout();
		customerIdHLayout.setSpacing(true);
		gridLayout.addComponent(customerIdHLayout, 1, 2);
		
		Label customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		customerIdHLayout.addComponent(customerIdLabel);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("110px");
		customerIdHLayout.addComponent(customerId_tf);
	}

	/**
	 * 创建 存放“迁移者部门” 的布局管理器
	 */
	private void createOperatorDeptHLayout() {
		HorizontalLayout deptHLayout = new HorizontalLayout();
		deptHLayout.setSpacing(true);
		gridLayout.addComponent(deptHLayout, 2, 2);
		
		Label deptLabel = new Label("迁移者部门：");
		deptLabel.setWidth("-1px");
		deptHLayout.addComponent(deptLabel);

		operatorDept = new TextField();
		operatorDept.setWidth("110px");
		deptHLayout.addComponent(operatorDept);
	}
	
	/**
	 * 创建 存放“原经理部门” 的布局管理器
	 */
	private void createOldManagerDeptHLayout() {
		HorizontalLayout deptHLayout = new HorizontalLayout();
		deptHLayout.setSpacing(true);
		gridLayout.addComponent(deptHLayout, 3, 2);
		
		Label deptLabel = new Label("原经理部门：");
		deptLabel.setWidth("-1px");
		deptHLayout.addComponent(deptLabel);
		
		oldManagerDept = new TextField();
		oldManagerDept.setWidth("110px");
		deptHLayout.addComponent(oldManagerDept);
	}
	
	/**
	 * 创建 存放“新经理部门” 的布局管理器
	 */
	private void createNewManagerDeptHLayout() {
		HorizontalLayout deptHLayout = new HorizontalLayout();
		deptHLayout.setSpacing(true);
		gridLayout.addComponent(deptHLayout, 4, 2);
		
		Label deptLabel = new Label("新经理部门：");
		deptLabel.setWidth("-1px");
		deptHLayout.addComponent(deptLabel);
		
		newManagerDept = new TextField();
		newManagerDept.setWidth("110px");
		deptHLayout.addComponent(newManagerDept);
	}

	/**
	 * 创建 存放“公司名称” 的布局管理器
	 */
	private void createCompanyNameHLayout() {
		HorizontalLayout companyHLayout = new HorizontalLayout();
		companyHLayout.setSpacing(true);
		companyHLayout.setWidth("100%");
		gridLayout.addComponent(companyHLayout, 0, 3, 1, 3);
		
		Label campanyLabel = new Label("公司名称：");
		campanyLabel.setWidth("-1px");
		companyHLayout.addComponent(campanyLabel);
		
		companyName = new TextField();
		companyName.setWidth("100%");
		companyHLayout.addComponent(companyName);
		companyHLayout.setExpandRatio(companyName, 1.0f);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(startTime.getValue() == null || finishTime.getValue() == null) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间和截止时间都不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			if(!customerId_tf.isValid()) {
				customerId_tf.getApplication().getMainWindow().showNotification("客户编号只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
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
		migrateScope.select("本月");
		companyName.setValue("");
		
		customerName.setValue("");
		customerPhone.setValue("");
		customerId_tf.setValue("");
		
		operatorEmpNo.setValue("");
		operatorName.setValue("");
		operatorDept.setValue("");

		oldManagerEmpNo.setValue("");
		oldManagerName.setValue("");
		oldManagerDept.setValue("");
		
		newManagerEmpNo.setValue("");
		newManagerName.setValue("");
		newManagerDept.setValue("");
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

		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and m.customerId = " + customerId;
		}
		
		// 客户公司查询
		String companyNameSql = "";
		String inputCompanyName = companyName.getValue().toString().trim();
		if(!"".equals(inputCompanyName)) {
			companyNameSql = " and m.customerCompanyName like '%" + inputCompanyName + "%'";
		}

		// 迁移者工号查询
		String operatorEmpNoSql = "";
		String inputOperatorEmpNo = operatorEmpNo.getValue().toString().trim();
		if(!"".equals(inputOperatorEmpNo)) {
			operatorEmpNoSql = " and m.operatorEmpNo like '%" + inputOperatorEmpNo + "%'";
		}
		
		// 迁移者姓名查询
		String operatorNameSql = "";
		String inputOperatorName = operatorName.getValue().toString().trim();
		if(!"".equals(inputOperatorName)) {
			operatorNameSql = " and m.operatorRealName like '%" + inputOperatorName + "%'";
		}
		
		// 迁移者部门查询
		String operatorDeptSql = "";
		String inputOperatorDept = operatorDept.getValue().toString().trim();
		if(!"".equals(inputOperatorDept)) {
			operatorDeptSql = " and m.operatorDept like '%" + inputOperatorDept + "%'";
		}
		
		// 原经理工号查询
		String oldManagerEmpNoSql = "";
		String inputOldManagerEmpNo = oldManagerEmpNo.getValue().toString().trim();
		if(!"".equals(inputOldManagerEmpNo)) {
			oldManagerEmpNoSql = " and m.oldManagerEmpNo like '%" + inputOldManagerEmpNo + "%'";
		}
		
		// 原经理姓名查询
		String oldManagerNameSql = "";
		String inputOldManagerName = oldManagerName.getValue().toString().trim();
		if(!"".equals(inputOldManagerName)) {
			oldManagerNameSql = " and m.oldManagerRealName like '%" + inputOldManagerName + "%'";
		}
		
		// 原经理部门查询
		String oldManagerDeptSql = "";
		String inputOldManagerDept = oldManagerDept.getValue().toString().trim();
		if(!"".equals(inputOldManagerDept)) {
			oldManagerDeptSql = " and m.oldManagerDept like '%" + inputOldManagerDept + "%'";
		}
		
		// 新经理工号查询
		String newManagerEmpNoSql = "";
		String inputNewManagerEmpNo = newManagerEmpNo.getValue().toString().trim();
		if(!"".equals(inputNewManagerEmpNo)) {
			newManagerEmpNoSql = " and m.newManagerEmpNo like '%" + inputNewManagerEmpNo + "%'";
		}
		
		// 新经理姓名查询
		String newManagerNameSql = "";
		String inputNewManagerName = newManagerName.getValue().toString().trim();
		if(!"".equals(inputNewManagerName)) {
			newManagerNameSql = " and m.newManagerRealName like '%" + inputNewManagerName + "%'";
		}
		
		// 新经理部门查询
		String newManagerDeptSql = "";
		String inputNewManagerDept = newManagerDept.getValue().toString().trim();
		if(!"".equals(inputNewManagerDept)) {
			newManagerDeptSql = " and m.newManagerDept like '%" + inputNewManagerDept + "%'";
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
		return customerNameSql + customerPhoneSql + customerIdSql + companyNameSql + operatorEmpNoSql 
				 + operatorNameSql + operatorDeptSql + oldManagerEmpNoSql + oldManagerNameSql 
				 + oldManagerDeptSql + newManagerEmpNoSql + newManagerNameSql + newManagerDeptSql
				 + specificStartImportTimeSql + specificFinishImportTimeSql;
	}

	/**
	 * 设置 CustomerMigrateLogView 的界面的翻页组件
	 * @param migrateLogTableFlip
	 */
	public void setMigrateLogTableFlip(FlipOverTableComponent<MigrateCustomerLog> migrateLogTableFlip) {
		this.migrateLogTableFlip = migrateLogTableFlip;
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级搜索";
	}

	@Override
	public Component getPopupComponent() {
		return gridLayout;
	}
	
}
