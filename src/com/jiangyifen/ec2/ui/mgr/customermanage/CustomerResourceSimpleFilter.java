package com.jiangyifen.ec2.ui.mgr.customermanage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 客户资源简单过滤器 组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class CustomerResourceSimpleFilter extends VerticalLayout implements ClickListener {

	private GridLayout gridLayout;				// 面板中的布局管理器
	
	private ComboBox dialDateScope;			// “联系时间”选择框
	private TextField customerName;			// 客户姓名输入文本框
	private TextField companyName;				// 公司名称输入文本框
	
	private PopupDateField startTime;		// “开始时间”选择框
	private TextField customerPhone;		// 客户手机输入文本框
	private ComboBox importDateScope;		// “导入时间”选择框
	
	private PopupDateField finishTime;			// “截止时间”选择框
	private ComboBox customerLevelSelector;	// “客户等级”选择
	private ComboBox cmbManager;				// 客户经理

	private Button searchButton;			// 刷新结果按钮
	private Button clearButton;			// 清空输入内容
	private PopupView complexSearchView;	// 复杂搜索界面
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private Domain domain;					// 当前用户所属域
	private String deptSearchSql;			// 初始化部门搜索语句

	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
	private BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
	private CustomerResourceComplexFilter customerResourceComplexFilter;
	private FlipOverTableComponent<CustomerResource> customerMemberManageFlip;				// 客户成员管理Tab 页的翻页组件
	
	private CustomerLevelService customerLevelService;	// 客户等级服务类
	private UserService userService;					// 用户服务类
	
	/** 构造方法 */
	public CustomerResourceSimpleFilter() {
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		customerLevelService = SpringContextHolder.getBean("customerLevelService");
		userService = SpringContextHolder.getBean("userService");
		
		gridLayout = new GridLayout(5, 3);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 初始化部门搜索语句
		deptSearchSql = createDeptSearchSql();

		//--------- 第一行  -----------//
		this.createFirstSearchLayout();
		
		//--------- 第二行  -----------//
		this.createTwoSearchLayout();
		
		//--------- 第三行  -----------//
		this.createThreeSearchLayout();
		
		//--------- 创建操作按钮 -----------//
		this.createOperateButtons();
	}

	/**
	 * 初始化部门搜索范围语句
	 * @return
	 */
	private String createDeptSearchSql() {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		String sql = " and c.accountManager.department.id in (" + achieveBasicUtil.getDeptIds() + ")";
		return sql;
	}

	/**
	 * 创建操作按钮
	 */
	private void createOperateButtons() {
		searchButton = new Button("查 询");
		searchButton.setDescription("快捷键(Ctrl+Q)");
		searchButton.addStyleName("default");
		searchButton.addListener(this);
		searchButton.setWidth("60px");
		searchButton.setClickShortcut(KeyCode.Q, ModifierKey.CTRL);
		gridLayout.addComponent(searchButton, 4, 0);
		
		clearButton = new Button("清 空");
		clearButton.setDescription("快捷键(Ctrl+L)");
		clearButton.addListener(this);
		clearButton.setWidth("60px");
		clearButton.setClickShortcut(KeyCode.L, ModifierKey.CTRL);
		gridLayout.addComponent(clearButton, 4, 1);
		
		customerResourceComplexFilter = new CustomerResourceComplexFilter();
		customerResourceComplexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(customerResourceComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 4, 2);
		gridLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_CENTER);
	}
	
	/** 表格查询组件第一行 */
	private void createFirstSearchLayout() {
		/** 创建  存放“时间范围标签和其选择框” 的布局管理器 */
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 0, 0);
		
		Label timeScopeLabel = new Label("联系时间：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		dialDateScope = new ComboBox();
		dialDateScope.setImmediate(true);
		dialDateScope.addItem("今天");
		dialDateScope.addItem("昨天");
		dialDateScope.addItem("本周");
		dialDateScope.addItem("上周");
		dialDateScope.addItem("本月");
		dialDateScope.addItem("上月");
		dialDateScope.addItem("精确时间");
		dialDateScope.setValue("本月");
		dialDateScope.setWidth("120px");
		dialDateScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(dialDateScope);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)dialDateScope.getValue();
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
		dialDateScope.addListener(timeScopeListener);
		
		/** 创建  存放“开始时间标签和其选择框” 的布局管理器 */
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
		
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 1, 0);
				
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		startTimeHLayout.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dialDateScope.removeListener(timeScopeListener);
				dialDateScope.setValue("精确时间");
				dialDateScope.addListener(timeScopeListener);
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
		
		/** 创建  存放“截止时间标签和其选择框” 的布局管理器 */
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 2, 0);
		
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		finishTimeHLayout.addComponent(finishTimeLabel);
		
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dialDateScope.removeListener(finishTimeListener);
				dialDateScope.setValue("精确时间");
				dialDateScope.addListener(timeScopeListener);
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
	
	/** 表格查询组件第二行 */
	private void createTwoSearchLayout() {
		/** 创建 存放“客户姓名” 的布局管理器 */
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 0, 1);
		
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		customerName = new TextField();
		customerName.setWidth("120px");	
		customerNameHLayout.addComponent(customerName);
		
		/** 创建 存放“联系方式” 的布局管理器 */
		HorizontalLayout telephoneHLayout = new HorizontalLayout();
		telephoneHLayout.setSpacing(true);
		gridLayout.addComponent(telephoneHLayout, 1, 1);
		
		Label telephoneLabel = new Label("联系方式：");
		telephoneLabel.setWidth("-1px");
		telephoneHLayout.addComponent(telephoneLabel);
		
		customerPhone = new TextField();
		customerPhone.setWidth("153px");
		telephoneHLayout.addComponent(customerPhone);
		
		/** 创建 存放“公司名称” 的布局管理器 */
		HorizontalLayout companyHLayout = new HorizontalLayout();
		companyHLayout.setSpacing(true);
		gridLayout.addComponent(companyHLayout, 2, 1);
		
		Label campanyLabel = new Label("公司名称：");
		campanyLabel.setWidth("-1px");
		companyHLayout.addComponent(campanyLabel);
		
		companyName = new TextField();
		companyName.setWidth("153px");
		companyHLayout.addComponent(companyName);
		
	}

	/** 表格查询组件第三行 */
	private void createThreeSearchLayout() {
		/** 创建  存放“客户等级标签和其选择框” 的布局管理器 */
		HorizontalLayout levelHLayout = new HorizontalLayout();
		levelHLayout.setSpacing(true);
		gridLayout.addComponent(levelHLayout, 0, 2);
		
		Label levelLabel = new Label("客户等级：");
		levelLabel.setWidth("-1px");
		levelHLayout.addComponent(levelLabel);
		
		BeanItemContainer<CustomerLevel> levelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
		levelContainer.addAll(customerLevelService.getAll(domain));
		
		customerLevelSelector = new ComboBox();
		customerLevelSelector.setImmediate(true);
		customerLevelSelector.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
		customerLevelSelector.setWidth("120px");
		customerLevelSelector.setItemCaptionPropertyId("levelName");
		customerLevelSelector.setContainerDataSource(levelContainer);
		customerLevelSelector.setNullSelectionAllowed(true);
		levelHLayout.addComponent(customerLevelSelector);
		
		/** 创建 存放“客户经理” 的布局管理器 */
		HorizontalLayout managerHLayout = new HorizontalLayout();
		managerHLayout.setSpacing(true);
		gridLayout.addComponent(managerHLayout, 1, 2);
		
		Label lblManager = new Label("客户经理：");
		lblManager.setWidth("-1px");
		managerHLayout.addComponent(lblManager);
		
		cmbManager = new ComboBox();
		cmbManager.setImmediate(true);
		cmbManager.setFilteringMode(Filtering.FILTERINGMODE_CONTAINS);
		cmbManager.setWidth("153px");
		cmbManager.setNullSelectionAllowed(true);
		cmbManager.setItemCaptionPropertyId("migrateCsr");
		managerHLayout.addComponent(cmbManager);
		
		String deptIds = achieveBasicUtil.getDeptIds();
		if(deptIds != null && deptIds.length() >= 1) {
			List<User> userList = userService.getAllUsersByJpql("select u from User u where u.domain.id = " + domain.getId() + " and u.department.id in("+deptIds+") order by u.empNo asc");
			if(userList != null && userList.size() >= 1) {
				userContainer.removeAllItems();
				userContainer.addAll(userList);
				cmbManager.setContainerDataSource(userContainer);
			}
		}
		
		/** 创建  存放“导入时间标签和其选择框” 的布局管理器 */
		HorizontalLayout dialDateScopeHLayout = new HorizontalLayout();
		dialDateScopeHLayout.setSpacing(true);
		gridLayout.addComponent(dialDateScopeHLayout, 2, 2);
		
		Label dialDateScopeLabel = new Label("导入时间：");
		dialDateScopeLabel.setWidth("-1px");
		dialDateScopeHLayout.addComponent(dialDateScopeLabel);
		
		importDateScope = new ComboBox();
		importDateScope.setImmediate(true);
		importDateScope.addItem("今天");
		importDateScope.addItem("昨天");
		importDateScope.addItem("本周");
		importDateScope.addItem("上周");
		importDateScope.addItem("本月");
		importDateScope.addItem("上月");
		importDateScope.setWidth("153px");
		importDateScope.setNullSelectionAllowed(true);
		dialDateScopeHLayout.addComponent(importDateScope);
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
		/*managerEmpNo.setValue("");
		managerName.setValue("");
		managerUsername.setValue("");*/
		dialDateScope.select("本月");
		customerName.setValue("");
		customerPhone.setValue("");
		companyName.setValue("");
		importDateScope.setValue(null);
		customerLevelSelector.setValue(null);
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(c) from CustomerResource as c where c.domain.id = " +domain.getId() + deptSearchSql + createDynamicSql();
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.accountManager.id asc, c.lastDialDate desc";
		
		customerMemberManageFlip.setSearchSql(searchSql);
		customerMemberManageFlip.setCountSql(countSql);
		customerMemberManageFlip.refreshToFirstPage();
	}

	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createDynamicSql() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer sbJpql = new StringBuffer();
		
		// 客户经理查询
		if(cmbManager.getValue() != null) {
			User user = (User) cmbManager.getValue();
			if(user != null && user.getId() != null) {
				sbJpql.append(" and c.accountManager.id = ");
				sbJpql.append(user.getId());
				sbJpql.append(" ");
			}
		}
		
		// 客户姓名查询
		String inputCustomerName = customerName.getValue().toString().trim();
		if(!"".equals(inputCustomerName)) {
			sbJpql.append(" and c.name like '%");
			sbJpql.append(inputCustomerName);
			sbJpql.append("%' ");
		}
		
		// 客户公司查询
		String inputCompanyName = companyName.getValue().toString().trim();
		if(!"".equals(inputCompanyName)) {
			sbJpql.append(" and c.company.name like '%");
			sbJpql.append(inputCompanyName);
			sbJpql.append("%' ");
		}
		
		// 客户联系方式查询
		String phoneNumber = customerPhone.getValue().toString().trim();
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			sbJpql.append(" and c in (select p.customerResource from Telephone as p where p.number like '%");
			sbJpql.append(phoneNumber);
			sbJpql.append("%') ");
		}
		
		// 导入时间查询
		String dialDateScopeValue = (String) importDateScope.getValue();
		if(dialDateScopeValue != null) {
			String[] dates = ParseDateSearchScope.parseDateSearchScope(dialDateScopeValue);
			sbJpql.append(" and c.importDate >= '");
			sbJpql.append(dates[0]);
			sbJpql.append("' and c.importDate <= '");
			sbJpql.append(dates[1]);
			sbJpql.append("' ");
		}
		
		// 客户等级查询
		CustomerLevel customerLevel = (CustomerLevel) customerLevelSelector.getValue();
		if(customerLevel != null) {
			sbJpql.append(" and c.customerLevel.id = ");
			sbJpql.append(customerLevel.getId());
		}
		
		// 最近联系日期查询
		if(startTime.getValue() != null) {
			sbJpql.append(" and c.lastDialDate >= '");
			sbJpql.append(dateFormat.format(startTime.getValue()));
			sbJpql.append("' ");
		} 
		
		if(finishTime.getValue() != null) {
			sbJpql.append(" and c.lastDialDate <= '");
			sbJpql.append(dateFormat.format(finishTime.getValue()));
			sbJpql.append("' ");
		}
		
		// 创建固定的搜索语句
		return sbJpql.toString();
	}

	/**
	 * 设置翻页组件
	 * @param customerMemberManageFlip
	 */
	public void setCustomerMemberManageFlip(FlipOverTableComponent<CustomerResource> customerMemberManageFlip) {
		this.customerMemberManageFlip = customerMemberManageFlip;
		customerResourceComplexFilter.setCustomerMemberManageFlip(customerMemberManageFlip);
	}

}
