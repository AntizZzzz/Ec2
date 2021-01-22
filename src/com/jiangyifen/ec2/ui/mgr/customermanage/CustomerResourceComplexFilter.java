package com.jiangyifen.ec2.ui.mgr.customermanage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 客户资源复杂过滤器 组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class CustomerResourceComplexFilter extends VerticalLayout implements ClickListener, Content {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private TextField managerEmpNo;			// 经理工号输入文本框
	private TextField customerName;			// 客户姓名输入文本框
	private ComboBox dialDateScope;			// “联系时间”选择框
	private PopupDateField startTime;		// “开始时间”选择框
	private PopupDateField finishTime;		// “截止时间”选择框
	
	private TextField managerName;			// 经理姓名输入文本框
	private TextField customerPhone;		// 客户手机输入文本框
	private TextField expiredDateMoreThan;	// “有效时间搜索起始时间”输入框
	private ComboBox andOrToExpiredDate;	// 关联词选择框，and 或 or
	private TextField expiredDateLessThan;	// “有效时间搜索结束时间”输入框

	private TextField managerUsername;		// 经理用户名输入文本框
	private ComboBox customerLevelSelector; // “客户等级”选择
	private TextField customerId_tf;		// 客户编号输入文本框
	private ComboBox importDateScope;		// “导入时间”选择框
	private TextField companyName;			// 公司名称输入文本框
	
	private TextField dialCountMoreThan;	// “联系次数>=”输入框
	private ComboBox andOrToDialCount;		// 关联词选择框，and 或 or
	private TextField dialCountLessThan;	// “联系次数<=”输入框
	

	private Button searchButton;			// 刷新结果按钮
	private Button clearButton;				// 清空输入内容
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private User loginUser; 				// 当前的登陆用户
	private Domain domain;					// 当前用户所属域
	private String deptSearchSql;			// 部门范围搜索语句

	private FlipOverTableComponent<CustomerResource> customerMemberManageFlip;				// 客户成员管理Tab 页的翻页组件
	
	private CustomerLevelService customerLevelService;	// 客户等级服务类
	private DepartmentService departmentService;		// 部门服务类
	
	public CustomerResourceComplexFilter() {
		this.setSpacing(true);

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		customerLevelService = SpringContextHolder.getBean("customerLevelService");
		departmentService = SpringContextHolder.getBean("departmentService");
		
		gridLayout = new GridLayout(6, 4);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 初始化部门搜索语句
		deptSearchSql = createDeptSearchSql();

		//--------- 第一行  -----------//
		this.createManagerEmpNoHLayout();
		this.createCustomerNameHLayout();
		this.createDialDateScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		
		//--------- 第二行  -----------//
		this.createManagerNameHLayout();
		this.createCustomerLevelHLayout();
		this.createExpiredDateMoreThanHLayout();
		this.createAndOrToExpiredDateHLayout();
		this.createExpiredDateLessThanHLayout();

		//--------- 第三行  -----------//
		this.createManagerUsernameHLayout();
		this.createCustomerPhoneHLayout();
		this.createCustomerIdHLayout();
//		this.createCountMoreThanHLayout();
//		this.createAndOrToCountHLayout();
//		this.createCountLessThanHLayout();
		this.createImportDateHLayout();
		this.createCompanyNameHLayout();

		//--------- 第四行  -----------//
//		this.createDialDateHLayout();
//		this.createCustomerLevelHLayout();
		
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
		String sql = " and c.accountManager.department.id in (" +deptIdSql+ ")";
		
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
	 * 创建 存放“客户姓名” 的布局管理器
	 */
	private void createManagerEmpNoHLayout() {
		HorizontalLayout managerEmpNoHLayout = new HorizontalLayout();
		managerEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(managerEmpNoHLayout, 0, 0);
		
		// 客户姓名输入区
		Label managerEmpNoLabel = new Label("经理工号：");
		managerEmpNoLabel.setWidth("-1px");
		managerEmpNoHLayout.addComponent(managerEmpNoLabel);
		
		managerEmpNo = new TextField();
		managerEmpNo.setWidth("100px");	
		managerEmpNoHLayout.addComponent(managerEmpNo);
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
		customerName.setWidth("100px");	
		customerNameHLayout.addComponent(customerName);
	}

	/**
	 * 创建  存放“时间范围标签和其选择框” 的布局管理器
	 */
	private void createDialDateScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 2, 0);
		
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
		dialDateScope.setWidth("100px");
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
	}

	/**
	 * 创建  存放“开始时间标签和其选择框” 的布局管理器
	 */
	private void createStartTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
	
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 3, 0);
				
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
	}

	/**
	 * 创建  存放“截止时间标签和其选择框” 的布局管理器
	 */
	private void createFinishTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
		
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 4, 0);
		
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
		finishTime.setWidth("154px");
		finishTime.setValue(dates[1]);
		finishTime.addListener(finishTimeListener);
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setParseErrorMessage("时间格式不合法");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTimeHLayout.addComponent(finishTime);
	}
	
	/**
	 * 创建 存放“经理姓名” 的布局管理器
	 */
	private void createManagerNameHLayout() {
		HorizontalLayout managerNameHLayout = new HorizontalLayout();
		managerNameHLayout.setSpacing(true);
		gridLayout.addComponent(managerNameHLayout, 0, 1);
		
		// 客户姓名输入区
		Label managerNameLabel = new Label("经理姓名：");
		managerNameLabel.setWidth("-1px");
		managerNameHLayout.addComponent(managerNameLabel);
		
		managerName = new TextField();
		managerName.setWidth("100px");	
		managerNameHLayout.addComponent(managerName);
	}
	
	/**
	 * 创建 存放“联系电话” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout telephoneHLayout = new HorizontalLayout();
		telephoneHLayout.setSpacing(true);
		gridLayout.addComponent(telephoneHLayout, 1, 1);
		
		Label telephoneLabel = new Label("联系电话：");
		telephoneLabel.setWidth("-1px");
		telephoneHLayout.addComponent(telephoneLabel);
		
		customerPhone = new TextField();
		customerPhone.setWidth("100px");
		telephoneHLayout.addComponent(customerPhone);
	}

	/**
	 * 创建  存放“联系次数>=标签和其选择框” 的布局管理器
	 */
	private void createExpiredDateMoreThanHLayout() {
		HorizontalLayout moreThanHLayout = new HorizontalLayout();
		moreThanHLayout.setSpacing(true);
		gridLayout.addComponent(moreThanHLayout, 2, 1);
		
		Label expiredDateMoreThanLabel = new Label("有效时间>=：");
		expiredDateMoreThanLabel.setWidth("-1px");
		moreThanHLayout.addComponent(expiredDateMoreThanLabel);
		
		expiredDateMoreThan = new TextField();
		expiredDateMoreThan.setWidth("85px");
		expiredDateMoreThan.setInputPrompt("n天内不过期");
		expiredDateMoreThan.addValidator(new RegexpValidator("(-?\\d+)||\\d*", "有效时间只能为负数或正数！"));
		expiredDateMoreThan.setValidationVisible(false);
		moreThanHLayout.addComponent(expiredDateMoreThan);
	}

	/**
	 * 创建  存放“搜索关联词标签和其选择框” 的布局管理器
	 */
	private void createAndOrToExpiredDateHLayout() {
		HorizontalLayout andOrToExpiredHLayout = new HorizontalLayout();
		andOrToExpiredHLayout.setSpacing(true);
		gridLayout.addComponent(andOrToExpiredHLayout, 3, 1);
		
		Label correlativeLabel = new Label("关 联 词 ：");
		correlativeLabel.setWidth("-1px");
		andOrToExpiredHLayout.addComponent(correlativeLabel);
		
		andOrToExpiredDate = new ComboBox();
		andOrToExpiredDate.addItem("AND");
		andOrToExpiredDate.addItem("OR");
		andOrToExpiredDate.setValue("AND");
		andOrToExpiredDate.setWidth("153px");
		andOrToExpiredDate.setNullSelectionAllowed(false);
		andOrToExpiredHLayout.addComponent(andOrToExpiredDate);
	}

	/**
	 * 创建  存放“联系次数<=标签和其选择框” 的布局管理器
	 */
	private void createExpiredDateLessThanHLayout() {
		HorizontalLayout lessThanHLayout = new HorizontalLayout();
		lessThanHLayout.setSpacing(true);
		gridLayout.addComponent(lessThanHLayout, 4, 1);
		
		Label expiredDateLessThanLabel = new Label("有效时间<=：");
		expiredDateLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(expiredDateLessThanLabel);
		
		expiredDateLessThan = new TextField();
		expiredDateLessThan.setWidth("138px");
		expiredDateLessThan.setInputPrompt("n天内不过期");
		expiredDateLessThan.addValidator(new RegexpValidator("(-?\\d+)||\\d*", "有效时间只能为负数或正数！"));
		expiredDateLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(expiredDateLessThan);
	}
	
	/**
	 * 创建 存放“经理用户名” 的布局管理器
	 */
	private void createManagerUsernameHLayout() {
		HorizontalLayout managerUsernameHLayout = new HorizontalLayout();
		managerUsernameHLayout.setSpacing(true);
		gridLayout.addComponent(managerUsernameHLayout, 0, 2);
		
		Label managerUsernameLabel = new Label("经理用户名：");
		managerUsernameLabel.setWidth("-1px");
		managerUsernameHLayout.addComponent(managerUsernameLabel);

		managerUsername = new TextField();
		managerUsername.setWidth("87px");
		managerUsernameHLayout.addComponent(managerUsername);
	}

	/**
	 * 创建  存放“客户等级标签和其选择框” 的布局管理器
	 */
	private void createCustomerLevelHLayout() {
		HorizontalLayout levelHLayout = new HorizontalLayout();
		levelHLayout.setSpacing(true);
		gridLayout.addComponent(levelHLayout, 1, 2);
		
		Label levelLabel = new Label("客户等级：");
		levelLabel.setWidth("-1px");
		levelHLayout.addComponent(levelLabel);
		
		BeanItemContainer<CustomerLevel> levelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
		levelContainer.addAll(customerLevelService.getAll(domain));
		
		customerLevelSelector = new ComboBox();
		customerLevelSelector.setImmediate(true);
		customerLevelSelector.setWidth("100px");
		customerLevelSelector.setItemCaptionPropertyId("levelName");
		customerLevelSelector.setContainerDataSource(levelContainer);
		customerLevelSelector.setNullSelectionAllowed(true);
		levelHLayout.addComponent(customerLevelSelector);
	}
	
	/**
	 * 创建  存放“客户编号输入区” 的布局管理器
	 */
	private void createCustomerIdHLayout() {
		HorizontalLayout customerIdHLayout = new HorizontalLayout();
		customerIdHLayout.setSpacing(true);
		gridLayout.addComponent(customerIdHLayout, 2, 2);

		Label customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		customerIdHLayout.addComponent(customerIdLabel);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("100px");
		customerIdHLayout.addComponent(customerId_tf);
	}

	/**
	 * 创建  存放“联系次数>=标签和其选择框” 的布局管理器
	 */
	@SuppressWarnings("unused")
	private void createCountMoreThanHLayout() {
		HorizontalLayout moreThanHLayout = new HorizontalLayout();
		moreThanHLayout.setSpacing(true);
		gridLayout.addComponent(moreThanHLayout, 1, 2);
		
		Label talkTimeMoreThanLabel = new Label("联系次数>=：");
		talkTimeMoreThanLabel.setWidth("-1px");
		moreThanHLayout.addComponent(talkTimeMoreThanLabel);
		
		dialCountMoreThan = new TextField();
		dialCountMoreThan.setWidth("100px");
		dialCountMoreThan.addValidator(new RegexpValidator("\\d+", "联系次数只能为数字组成！"));
		dialCountMoreThan.setValidationVisible(false);
		moreThanHLayout.addComponent(dialCountMoreThan);
	}

	/**
	 * 创建  存放“搜索关联词标签和其选择框” 的布局管理器
	 */
	@SuppressWarnings("unused")
	private void createAndOrToCountHLayout() {
		HorizontalLayout andOrHLayout = new HorizontalLayout();
		andOrHLayout.setSpacing(true);
		gridLayout.addComponent(andOrHLayout, 2, 2);
		
		Label correlativeLabel = new Label("搜索关联词：");
		correlativeLabel.setWidth("-1px");
		andOrHLayout.addComponent(correlativeLabel);
		
		andOrToDialCount = new ComboBox();
		andOrToDialCount.addItem("AND");
		andOrToDialCount.addItem("OR");
		andOrToDialCount.setValue("AND");
		andOrToDialCount.setWidth("141px");
		andOrToDialCount.setNullSelectionAllowed(false);
		andOrHLayout.addComponent(andOrToDialCount);
	}

	/**
	 * 创建  存放“联系次数<=标签和其选择框” 的布局管理器
	 */
	@SuppressWarnings("unused")
	private void createCountLessThanHLayout() {
		HorizontalLayout lessThanHLayout = new HorizontalLayout();
		lessThanHLayout.setSpacing(true);
		gridLayout.addComponent(lessThanHLayout, 3, 2);
		
		Label talkTimeLessThanLabel = new Label("联系次数<=：");
		talkTimeLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(talkTimeLessThanLabel);
		
		dialCountLessThan = new TextField();
		dialCountLessThan.setWidth("100px");
		dialCountLessThan.addValidator(new RegexpValidator("\\d+", "联系次数只能为数字组成！"));
		dialCountLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(dialCountLessThan);
		
		Label secondsLabel = new Label("秒");
		secondsLabel.setWidth("-1px");
		lessThanHLayout.addComponent(secondsLabel);
	}

	/**
	 * 创建  存放“导入时间标签和其选择框” 的布局管理器
	 */
	private void createImportDateHLayout() {
		HorizontalLayout dialDateScopeHLayout = new HorizontalLayout();
		dialDateScopeHLayout.setSpacing(true);
		gridLayout.addComponent(dialDateScopeHLayout, 3, 2);
		
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
	
	/**
	 * 创建 存放“公司名称” 的布局管理器
	 */
	private void createCompanyNameHLayout() {
		HorizontalLayout companyHLayout = new HorizontalLayout();
		companyHLayout.setSpacing(true);
		gridLayout.addComponent(companyHLayout, 4, 2);
		
		Label campanyLabel = new Label("公司名称：");
		campanyLabel.setWidth("-1px");
		companyHLayout.addComponent(campanyLabel);
		
		companyName = new TextField();
		companyName.setWidth("153px");
		companyHLayout.addComponent(companyName);
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
//				dialCountMoreThan.getApplication().getMainWindow().showNotification("联系次数搜索输入框中只能输入数字！", Notification.TYPE_WARNING_MESSAGE);
//				return;
//			}
			
			if(!customerId_tf.isValid()) {
				customerId_tf.getApplication().getMainWindow().showNotification("客户编号只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(expiredDateMoreThan.isValid() == false || expiredDateMoreThan.isValid() == false) {
				expiredDateMoreThan.getApplication().getMainWindow().showNotification("有效时间搜索输入框只能为负数或正数！", Notification.TYPE_WARNING_MESSAGE);
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
		managerEmpNo.setValue("");
		managerName.setValue("");
		managerUsername.setValue("");
		
		dialDateScope.select("今天");
		
//			dialCountMoreThan.setValue("");
//			andOrToDialCount.setValue("AND");
//			dialCountLessThan.setValue("");
		
		expiredDateMoreThan.setValue("");
		andOrToExpiredDate.setValue("AND");
		expiredDateLessThan.setValue("");
		
		customerName.setValue("");
		customerPhone.setValue("");
		customerId_tf.setValue("");
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
		// 经理工号查询
		String managerEmpNoSql = "";
		String inputEmpNo = managerEmpNo.getValue().toString().trim();
		if(!"".equals(inputEmpNo)) {
			managerEmpNoSql = " and c.accountManager.empNo like '%" + inputEmpNo + "%'";
		}
		
		// 经理工号查询
		String managerNameSql = "";
		String inputName = managerName.getValue().toString().trim();
		if(!"".equals(inputName)) {
			managerNameSql = " and c.accountManager.realName like '%" + inputName + "%'";
		}
		
		// 经理工号查询
		String managerUsernameSql = "";
		String inputUsername = managerUsername.getValue().toString().trim();
		if(!"".equals(inputUsername)) {
			managerUsernameSql = " and c.accountManager.username like '%" + inputUsername + "%'";
		}
		
		// 客户姓名查询
		String customerNameSql = "";
		String inputCustomerName = customerName.getValue().toString().trim();
		if(!"".equals(inputCustomerName)) {
			customerNameSql = " and c.name like '%" + inputCustomerName + "%'";
		}

		// 客户编号查询语句
		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and c.id = " + customerId;
		}
		
		// 客户公司查询
		String companyNameSql = "";
		String inputCompanyName = companyName.getValue().toString().trim();
		if(!"".equals(inputCompanyName)) {
			companyNameSql = " and c.company.name like '%" + inputCompanyName + "%'";
		}
		
		// 客户联系方式查询
		String customerPhoneSql = "";
		String phoneNumber = customerPhone.getValue().toString().trim();
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			customerPhoneSql = " and c in (select p.customerResource from Telephone as p where p.number like '%" + phoneNumber + "%') ";
		}
		
		// 最近联系客户的时间
		String dialDateScopeSql = "";
		String dialDateScopeValue = (String) importDateScope.getValue();
		if(dialDateScopeValue != null) {
			String[] dates = ParseDateSearchScope.parseDateSearchScope(dialDateScopeValue);
			dialDateScopeSql = " and c.importDate >= '" +dates[0]+ "' and c.importDate <= '" +dates[1]+ "'";
		}
		
		// 客户等级查询
		String customerLevelSql = "";
		CustomerLevel customerLevel = (CustomerLevel) customerLevelSelector.getValue();
		if(customerLevel != null) {
			customerLevelSql = " and c.customerLevel.id = " +customerLevel.getId();
		}
		
		// 资源的导入日期查询
		String specificStartImportTimeSql = "";
		if(startTime.getValue() != null) {
			specificStartImportTimeSql = " and c.lastDialDate >= '" + dateFormat.format(startTime.getValue()) +"'";
		} 
		
		String specificFinishImportTimeSql = "";
		if(finishTime.getValue() != null) {
			specificFinishImportTimeSql = " and c.lastDialDate <= '" + dateFormat.format(finishTime.getValue()) +"'";
		}
		
//TODO	add it 	
//		// 客户被呼叫次数查询
//		String dialCountSql = "";
//		String inputDialCountMoreThan = dialCountMoreThan.getValue().toString().trim();
//		String inputDialCountLessThan = dialCountLessThan.getValue().toString().trim();
//		if(!"".equals(inputDialCountMoreThan) && !"".equals(inputDialCountLessThan)) {
//			if(Integer.parseInt(inputDialCountLessThan) < Integer.parseInt(inputDialCountMoreThan)) {
//				andOrToDialCount.setValue("OR");
//				dialCountSql = " and (c.count >= " + inputDialCountMoreThan + " or c.count <= " +inputDialCountLessThan + ")";
//			} else { 
//				andOrToDialCount.setValue("AND");
//				dialCountSql = " and (c.count >= " + inputDialCountMoreThan + " and c.count <= " +inputDialCountLessThan + ")";
//			}
//		} else if(!"".equals(inputDialCountMoreThan) && "".equals(inputDialCountLessThan)) {
//			dialCountSql = " and c.count >= " + inputDialCountMoreThan;
//		} else if("".equals(inputDialCountMoreThan) && !"".equals(inputDialCountLessThan)) {
//			dialCountSql = " and c.count <= " + inputDialCountLessThan;
//		} 
		
		// 客户的有效期查询
		String expiredDateSql = "";
		String inputExpiredMoreThan = expiredDateMoreThan.getValue().toString().trim();
		String inputExpiredLessThan = expiredDateLessThan.getValue().toString().trim();
		if(!"".equals(inputExpiredMoreThan) && !"".equals(inputExpiredLessThan)) {
			int morethan = Integer.parseInt(inputExpiredMoreThan);
			int lessthan = Integer.parseInt(inputExpiredLessThan);
			if(lessthan < morethan) {
				andOrToExpiredDate.setValue("OR");
				expiredDateSql = " and (c.expireDate >= '" + ParseDateSearchScope.getSpecifyDateStr(morethan) + "' or c.expireDate <= '" +ParseDateSearchScope.getSpecifyDateStr(lessthan) + "')";
			} else { 
				andOrToExpiredDate.setValue("AND");
				expiredDateSql = " and (c.expireDate >= '" + ParseDateSearchScope.getSpecifyDateStr(morethan) + "' and c.expireDate <= '" +ParseDateSearchScope.getSpecifyDateStr(lessthan) + "')";
			}
		} else if(!"".equals(inputExpiredMoreThan) && "".equals(inputExpiredLessThan)) {
			int morethan = Integer.parseInt(inputExpiredMoreThan);
			expiredDateSql = " and c.expireDate >= '" + ParseDateSearchScope.getSpecifyDateStr(morethan)+ "'";
		} else if("".equals(inputExpiredMoreThan) && !"".equals(inputExpiredLessThan)) {
			int lessthan = Integer.parseInt(inputExpiredLessThan);
			expiredDateSql = " and c.expireDate <= '" + ParseDateSearchScope.getSpecifyDateStr(lessthan)+ "'";
		} 
		
		// 创建固定的搜索语句
		return managerEmpNoSql + managerNameSql + managerUsernameSql + customerNameSql + customerIdSql + companyNameSql + customerPhoneSql 
				+ customerLevelSql + expiredDateSql + specificStartImportTimeSql + specificFinishImportTimeSql + dialDateScopeSql;
	}

	/**
	 * 设置 CustomerMemberManagement 的界面的翻页组件
	 * @param customerMemberManageFlip
	 */
	public void setCustomerMemberManageFlip(FlipOverTableComponent<CustomerResource> customerMemberManageFlip) {
		this.customerMemberManageFlip = customerMemberManageFlip;
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
