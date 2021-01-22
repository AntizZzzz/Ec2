package com.jiangyifen.ec2.ui.mgr.callrecordmanage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.CdrDirection;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.utils.CustomComboBox;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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
 * 呼叫记录的搜索组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class CallRecordComplexFilter extends VerticalLayout implements ClickListener, Content {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private ComboBox timeScope;				// “时间范围”选择框
	private PopupDateField startTime;		// “开始时间”选择框
	private PopupDateField finishTime;		// “截止时间”选择框
	private CustomComboBox callDirection;	// “呼叫方向”选择框
	
	private TextField projectName;			// “项目名称”输入框
	private TextField bridgedTimeMoreThan;	// “通话时长大于等于”输入框
	private ComboBox bridgedTimeAndOr;		// 通话时长关联词选择框，and 或 or
	private TextField bridgedTimeLessThan;	// “通话时长小于等于”输入框
	
	private TextField dialingNo;			// “主叫”输入框
	private TextField pickupTimeMoreThan;	// “接通时长大于等于”输入框
	private ComboBox pickupTimeAndOr;		// 接通时长关联词选择框，and 或 or
	private TextField pickupTimeLessThan;	// “接通时长小于等于”输入框
	
	private TextField calledNo;				// “被叫”输入框
	private TextField callTimeMoreThan;		// “呼叫时长大于等于”输入框
	private ComboBox callTimeAndOr;			// 呼叫时长关联词选择框，and 或 or
	private TextField callTimeLessThan;		// “呼叫时长小于等于”输入框
	
	private TextField department;			// “主/被叫部门”输入框
	private TextField username;				// “主/被叫用户名”输入框
	private TextField realName;				// “主/被叫姓名”输入框
	private CustomComboBox dialType;		// “呼叫类型”输入框
	
	private TextField empNo;				// “主/被叫工号”输入框
	private ComboBox bridgedResult;			// “是否接通”选择框
	private TextField userFieldInput;		// “客户按键”输入框
//	private ComboBox callResult;			// “是否应答”选择框
	private TextField resourceId_tf;		// “客户编号”输入框
	
	private Button searchButton;			// 搜索按钮
	private Button clearButton;				// 搜索按钮
	
	private User loginUser;					// 当前登录用户
	private Domain domain;					// 当前登录用户所属域
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private DepartmentService departmentService;
	private FlipOverTableComponent<Cdr> callRecordTableFlip;	// Cdr 信息Table显示区的翻页组件
	
	public CallRecordComplexFilter() {
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		departmentService=SpringContextHolder.getBean("departmentService");
		
		gridLayout = new GridLayout(5, 6);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		//--------- 第一行  -----------//
		this.createTimeScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		this.createCallDirHLayout();
		
		//--------- 第二行  -----------//
		this.createProjectHLayout();
		this.createBridgedTimeMoreThanHLayout();
		this.createBridgedTimeAndOrHLayout();
		this.createBridgedTimeLessThanHLayout();

		//--------- 第三行  -----------//
		this.createDalingNoHLayout();
		this.createPickupTimeMoreThanHLayout();
		this.createPickupTimeAndOrHLayout();
		this.createPickupTimeLessThanHLayout();
		
		
		//--------- 第四行  -----------//
		this.createCalledNoHLayout();
		this.createCallTimeMoreThanHLayout();
		this.createCallTimeAndOrHLayout();
		this.createCallTimeLessThanHLayout();
		
		//--------- 第五行  -----------//
		this.createDepartmentHLayout();
		this.createUsernameHLayout();
		this.createRealNameHLayout();
		this.createDialTypeHLayout();
		
		//--------- 第五行  -----------//
		this.createEmpNoLayout();
		this.createBridgedResultHLayout();
		this.createCustomerInputHLayout();
//		this.createCallStatusHLayout();
		this.createResourceIdHLayout();

		//--------- 创建操作按钮 -----------//
		this.createOperateButtons();
	}

	/**
	 * 创建操作按钮
	 */
	private void createOperateButtons() {
		searchButton = new Button("查 询");
		searchButton.addStyleName("default");
		searchButton.addListener(this);
		searchButton.setWidth("60px");
		gridLayout.addComponent(searchButton, 4, 0);
		
		clearButton = new Button("清 空");
		clearButton.addListener(this);
		clearButton.setWidth("60px");
		gridLayout.addComponent(clearButton, 4, 1);
	}
	
	/**
	 * 创建  存放“时间范围标签和其选择框” 的布局管理器
	 */
	private void createTimeScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 0, 0);
		
		Label timeScopeLabel = new Label("时间范围：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		timeScope = new ComboBox();
		timeScope.setImmediate(true);
		timeScope.addItem("今天");
		timeScope.addItem("昨天");
		timeScope.addItem("本周");
		timeScope.addItem("上周");
		timeScope.addItem("本月");
		timeScope.addItem("上月");
		timeScope.addItem("精确时间");
		timeScope.setValue("今天");
		timeScope.setWidth("120px");
		timeScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(timeScope);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)timeScope.getValue();
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
		timeScope.addListener(timeScopeListener);
	}

	/**
	 * 创建  存放“开始时间标签和其选择框” 的布局管理器
	 */
	private void createStartTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
	
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 1, 0);
				
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		startTimeHLayout.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("精确时间");
				timeScope.addListener(timeScopeListener);
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
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 2, 0);
		
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		finishTimeHLayout.addComponent(finishTimeLabel);
		
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(finishTimeListener);
				timeScope.setValue("精确时间");
				timeScope.addListener(timeScopeListener);
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
	 * 创建  存放“呼叫方向标签和其选择框” 的布局管理器
	 */
	private void createCallDirHLayout() {
		HorizontalLayout callDirHLayout = new HorizontalLayout();
		callDirHLayout.setSpacing(true);
		gridLayout.addComponent(callDirHLayout, 3, 0);
	
		TreeMap<String, String> treeMap = new TreeMap<String, String>();
		treeMap.put("all", "全部");
		treeMap.put("e2e", "内部");
		treeMap.put("e2o", "呼出");
		treeMap.put("o2e", "呼入");
		treeMap.put("o2o", "外转外");
		
		Label callDirectionLabel = new Label("呼叫方向：");
		callDirectionLabel.setWidth("-1px");
		callDirHLayout.addComponent(callDirectionLabel);
		
		callDirection = new CustomComboBox();
		callDirection.setDataSource(treeMap);
		callDirection.setWidth("174px");
		callDirection.setValue("all");
		callDirection.setNullSelectionAllowed(false);
		callDirHLayout.addComponent(callDirection);
	}

	/**
	 * 创建 存放“项目名称” 的布局管理器
	 */
	private void createProjectHLayout() {
		HorizontalLayout projectHLayout = new HorizontalLayout();
		projectHLayout.setSpacing(true);
		gridLayout.addComponent(projectHLayout, 0, 1);
		
		Label projectLabel = new Label("项目名称：");
		projectLabel.setWidth("-1px");
		projectHLayout.addComponent(projectLabel);
		
		projectName = new TextField();
		projectName.setWidth("120px");
		projectHLayout.addComponent(projectName);
	}

	/**
	 * 创建  存放“通话时长大于等于标签和其选择框” 的布局管理器
	 */
	private void createBridgedTimeMoreThanHLayout() {
		HorizontalLayout moreThanHLayout = new HorizontalLayout();
		moreThanHLayout.setSpacing(true);
		gridLayout.addComponent(moreThanHLayout, 1, 1);
		
		Label talkTimeMoreThanLabel = new Label("通话时长大于等于：");
		talkTimeMoreThanLabel.setWidth("-1px");
		moreThanHLayout.addComponent(talkTimeMoreThanLabel);
		
		bridgedTimeMoreThan = new TextField();
		bridgedTimeMoreThan.setWidth("100px");
		bridgedTimeMoreThan.addValidator(new RegexpValidator("\\d*", "通话时长只能为数字组成！"));
		bridgedTimeMoreThan.setValidationVisible(false);
		moreThanHLayout.addComponent(bridgedTimeMoreThan);
	}

	/**
	 * 创建  存放“搜索关联词标签和其选择框” 的布局管理器
	 */
	private void createBridgedTimeAndOrHLayout() {
		HorizontalLayout andOrHLayout = new HorizontalLayout();
		andOrHLayout.setSpacing(true);
		gridLayout.addComponent(andOrHLayout, 2, 1);
		
		Label correlativeLabel = new Label("通话时长关联词：");
		correlativeLabel.setWidth("-1px");
		andOrHLayout.addComponent(correlativeLabel);
		
		bridgedTimeAndOr = new ComboBox();
		bridgedTimeAndOr.addItem("AND");
		bridgedTimeAndOr.addItem("OR");
		bridgedTimeAndOr.setValue("AND");
		bridgedTimeAndOr.setWidth("115px");
		bridgedTimeAndOr.setNullSelectionAllowed(false);
		andOrHLayout.addComponent(bridgedTimeAndOr);
	}

	/**
	 * 创建  存放“通话时长小于等于标签和其选择框” 的布局管理器
	 */
	private void createBridgedTimeLessThanHLayout() {
		HorizontalLayout lessThanHLayout = new HorizontalLayout();
		lessThanHLayout.setSpacing(true);
		gridLayout.addComponent(lessThanHLayout, 3, 1);
		
		Label talkTimeLessThanLabel = new Label("通话时长小于等于：");
		talkTimeLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(talkTimeLessThanLabel);
		
		bridgedTimeLessThan = new TextField();
		bridgedTimeLessThan.setWidth("100px");
		bridgedTimeLessThan.addValidator(new RegexpValidator("\\d*", "通话时长只能为数字组成！"));
		bridgedTimeLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(bridgedTimeLessThan);
		
		Label secondsLabel = new Label("秒");
		secondsLabel.setWidth("-1px");
		lessThanHLayout.addComponent(secondsLabel);
	}

	/**
	 * 创建 存放“主叫号码标签和其选择框” 的布局管理器
	 */
	private void createDalingNoHLayout() {
		HorizontalLayout dalingNoHLayout = new HorizontalLayout();
		dalingNoHLayout.setSpacing(true);
		gridLayout.addComponent(dalingNoHLayout, 0, 2);
		
		Label dialingNoLabel = new Label("主叫：");
		dialingNoLabel.setWidth("-1px");
		dalingNoHLayout.addComponent(dialingNoLabel);
		
		dialingNo = new TextField();
		dialingNo.setWidth("146px");
		dalingNoHLayout.addComponent(dialingNo);
	}

	/**
	 * 创建  存放“接通时长大于等于标签和其选择框” 的布局管理器
	 */
	private void createPickupTimeMoreThanHLayout() {
		HorizontalLayout pickupTimeMoreThanHLayout = new HorizontalLayout();
		pickupTimeMoreThanHLayout.setSpacing(true);
		gridLayout.addComponent(pickupTimeMoreThanHLayout, 1, 2);
		
		Label pickupTimeMoreThanLabel = new Label("接通时长大于等于：");
		pickupTimeMoreThanLabel.setWidth("-1px");
		pickupTimeMoreThanHLayout.addComponent(pickupTimeMoreThanLabel);
		
		pickupTimeMoreThan = new TextField();
		pickupTimeMoreThan.setWidth("100px");
		pickupTimeMoreThan.addValidator(new RegexpValidator("\\d+", "接通时长只能为数字组成！"));
		pickupTimeMoreThan.setValidationVisible(false);
		pickupTimeMoreThanHLayout.addComponent(pickupTimeMoreThan);
	}
	
	/**
	 * 创建  存放“接通时长关联词标签和其选择框” 的布局管理器
	 */
	private void createPickupTimeAndOrHLayout() {
		HorizontalLayout andOrHLayout = new HorizontalLayout();
		andOrHLayout.setSpacing(true);
		gridLayout.addComponent(andOrHLayout, 2, 2);
		
		Label correlativeLabel = new Label("接通时长关联词：");
		correlativeLabel.setWidth("-1px");
		andOrHLayout.addComponent(correlativeLabel);
		
		pickupTimeAndOr = new ComboBox();
		pickupTimeAndOr.addItem("AND");
		pickupTimeAndOr.addItem("OR");
		pickupTimeAndOr.setValue("AND");
		pickupTimeAndOr.setWidth("115px");
		pickupTimeAndOr.setNullSelectionAllowed(false);
		andOrHLayout.addComponent(pickupTimeAndOr);
	}
	
	/**
	 * 创建  存放“接通时长小于等于标签和其选择框” 的布局管理器
	 */
	private void createPickupTimeLessThanHLayout() {
		HorizontalLayout lessThanHLayout = new HorizontalLayout();
		lessThanHLayout.setSpacing(true);
		gridLayout.addComponent(lessThanHLayout, 3, 2);
		
		Label pickupTimeLessThanLabel = new Label("接通时长小于等于：");
		pickupTimeLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(pickupTimeLessThanLabel);
		
		pickupTimeLessThan = new TextField();
		pickupTimeLessThan.setWidth("100px");
		pickupTimeLessThan.addValidator(new RegexpValidator("\\d+", "接通时长只能为数字组成！"));
		pickupTimeLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(pickupTimeLessThan);
		
		Label secondsLabel = new Label("秒");
		secondsLabel.setWidth("-1px");
		lessThanHLayout.addComponent(secondsLabel);
	}
	
	/**
	 * 创建 存放“被叫号码标签和其选择框” 的布局管理器
	 */
	private void createCalledNoHLayout() {
		HorizontalLayout calledNoHLayout = new HorizontalLayout();
		calledNoHLayout.setSpacing(true);
		gridLayout.addComponent(calledNoHLayout, 0, 3);
		
		Label calledNoLabel = new Label("被叫：");
		calledNoLabel.setWidth("-1px");
		calledNoHLayout.addComponent(calledNoLabel);
		
		calledNo = new TextField();
		calledNo.setWidth("146px");
		calledNoHLayout.addComponent(calledNo);
	}

	/**
	 * 创建  存放“呼叫时长大于等于标签和其选择框” 的布局管理器
	 */
	private void createCallTimeMoreThanHLayout() {
		HorizontalLayout moreThanHLayout = new HorizontalLayout();
		moreThanHLayout.setSpacing(true);
		gridLayout.addComponent(moreThanHLayout, 1, 3);
		
		Label talkTimeMoreThanLabel = new Label("呼叫时长大于等于：");
		talkTimeMoreThanLabel.setWidth("-1px");
		moreThanHLayout.addComponent(talkTimeMoreThanLabel);
		
		callTimeMoreThan = new TextField();
		callTimeMoreThan.setWidth("100px");
		callTimeMoreThan.addValidator(new RegexpValidator("\\d+", "呼叫时长只能为数字组成！"));
		callTimeMoreThan.setValidationVisible(false);
		moreThanHLayout.addComponent(callTimeMoreThan);
	}
	
	/**
	 * 创建  存放“呼叫时长关联词标签和其选择框” 的布局管理器
	 */
	private void createCallTimeAndOrHLayout() {
		HorizontalLayout andOrHLayout = new HorizontalLayout();
		andOrHLayout.setSpacing(true);
		gridLayout.addComponent(andOrHLayout, 2, 3);
		
		Label correlativeLabel = new Label("呼叫时长关联词：");
		correlativeLabel.setWidth("-1px");
		andOrHLayout.addComponent(correlativeLabel);
		
		callTimeAndOr = new ComboBox();
		callTimeAndOr.addItem("AND");
		callTimeAndOr.addItem("OR");
		callTimeAndOr.setValue("AND");
		callTimeAndOr.setWidth("115px");
		callTimeAndOr.setNullSelectionAllowed(false);
		andOrHLayout.addComponent(callTimeAndOr);
	}
	
	/**
	 * 创建  存放“呼叫时长小于等于标签和其选择框” 的布局管理器
	 */
	private void createCallTimeLessThanHLayout() {
		HorizontalLayout lessThanHLayout = new HorizontalLayout();
		lessThanHLayout.setSpacing(true);
		gridLayout.addComponent(lessThanHLayout, 3, 3);
		
		Label talkTimeLessThanLabel = new Label("呼叫时长小于等于：");
		talkTimeLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(talkTimeLessThanLabel);
		
		callTimeLessThan = new TextField();
		callTimeLessThan.setWidth("100px");
		callTimeLessThan.addValidator(new RegexpValidator("\\d+", "呼叫时长只能为数字组成！"));
		callTimeLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(callTimeLessThan);
		
		Label secondsLabel = new Label("秒");
		secondsLabel.setWidth("-1px");
		lessThanHLayout.addComponent(secondsLabel);
	}

	/**
	 * 创建 存放“主/被叫部门” 的布局管理器
	 */
	private void createDepartmentHLayout() {
		HorizontalLayout deptHLayout = new HorizontalLayout();
		deptHLayout.setSpacing(true);
		gridLayout.addComponent(deptHLayout, 0, 4);
		
		Label deptLabel = new Label("主/被叫部门：");
		deptLabel.setWidth("-1px");
		deptHLayout.addComponent(deptLabel);
		
		department = new TextField();
		department.setWidth("102px");
		deptHLayout.addComponent(department);
	}
	
	/**
	 * 创建 存放“主/被叫用户名” 的布局管理器
	 */
	private void createUsernameHLayout() {
		HorizontalLayout usernameHLayout = new HorizontalLayout();
		usernameHLayout.setSpacing(true);
		gridLayout.addComponent(usernameHLayout, 1, 4);
		
		Label usernameLabel = new Label("主/被叫用户名：");
		usernameLabel.setWidth("-1px");
		usernameHLayout.addComponent(usernameLabel);
		
		username = new TextField();
		username.setWidth("123px");
		usernameHLayout.addComponent(username);
	}

	/**
	 * 创建 存放“主/被叫姓名” 的布局管理器
	 */
	private void createRealNameHLayout() {
		HorizontalLayout realNameHLayout = new HorizontalLayout();
		realNameHLayout.setSpacing(true);
		gridLayout.addComponent(realNameHLayout, 2, 4);
		
		Label realNameLabel = new Label("主/被叫姓名：");
		realNameLabel.setWidth("-1px");
		realNameHLayout.addComponent(realNameLabel);
		
		realName = new TextField();
		realName.setWidth("138px");
		realNameHLayout.addComponent(realName);
	}
	
	/**
	 * 创建 存放“呼叫类型” 的布局管理器
	 */
	private void createDialTypeHLayout() {
		HorizontalLayout dialTypeHLayout = new HorizontalLayout();
		dialTypeHLayout.setSpacing(true);
		gridLayout.addComponent(dialTypeHLayout, 3, 4);
		
		Label dialTypeLabel = new Label("呼叫类型：");
		dialTypeLabel.setWidth("-1px");
		dialTypeHLayout.addComponent(dialTypeLabel);
		
		TreeMap<String, String> treeMap = new TreeMap<String, String>();
		treeMap.put("all", "全部");
		treeMap.put("manual", "手动呼叫");
		treeMap.put("autodial", "自动群呼");
		treeMap.put("soundsdial", "语音群发");
		
		dialType = new CustomComboBox();
		dialType.setWidth("174px");
		dialType.setNullSelectionAllowed(false);
		dialType.setDataSource(treeMap);
		dialType.setValue("all");
		dialTypeHLayout.addComponent(dialType);
	}
	
	/**
	 * 创建 存放“主/被叫工号” 的布局管理器
	 */
	private void createEmpNoLayout() {
		HorizontalLayout empNoHLayout = new HorizontalLayout();
		empNoHLayout.setSpacing(true);
		gridLayout.addComponent(empNoHLayout, 0, 5);
		
		Label extenLabel = new Label("主/被叫工号：");
		extenLabel.setWidth("-1px");
		empNoHLayout.addComponent(extenLabel);
		
		empNo = new TextField();
		empNo.setWidth("102px");
		empNoHLayout.addComponent(empNo);
	}

	/**
	 * 创建“接通情况”组件
	 */
	private void createBridgedResultHLayout() {
		HorizontalLayout callResultHLayout = new HorizontalLayout();
		callResultHLayout.setSpacing(true);
		gridLayout.addComponent(callResultHLayout, 1, 5);
		
		Label bridgedResultLabel = new Label("接通情况：");
		bridgedResultLabel.setWidth("-1px");
		callResultHLayout.addComponent(bridgedResultLabel);
		
		bridgedResult = new ComboBox();
		bridgedResult.addItem("全部");
		bridgedResult.addItem("已接通");
		bridgedResult.addItem("未接通");
		bridgedResult.setValue("全部");
		bridgedResult.setWidth("152px");
		bridgedResult.setNullSelectionAllowed(false);
		callResultHLayout.addComponent(bridgedResult);
	}

	/**
	 * 创建客户输入信息输入过滤框
	 */
	private void createCustomerInputHLayout() {
		HorizontalLayout userFieldHLayout = new HorizontalLayout();
		userFieldHLayout.setSpacing(true);
		gridLayout.addComponent(userFieldHLayout, 2, 5);
		
		Label userFieldLabel = new Label("客户按键：");
		userFieldLabel.setWidth("-1px");
		userFieldHLayout.addComponent(userFieldLabel);
		
		userFieldInput = new TextField();
		userFieldInput.setWidth("155px");
		userFieldHLayout.addComponent(userFieldInput);
	}
//
//	/**
//	 * 创建  存放“是否接通标签和其选择框” 的布局管理器
//	 */
//	private void createCallStatusHLayout() {
//		HorizontalLayout callResultHLayout = new HorizontalLayout();
//		callResultHLayout.setSpacing(true);
//		gridLayout.addComponent(callResultHLayout, 0, 4);
//		
//		Label callStatusLabel = new Label("应答情况：");
//		callStatusLabel.setWidth("-1px");
//		callResultHLayout.addComponent(callStatusLabel);
//		
//		callResult = new ComboBox();
//		callResult.addItem("全部");
//		callResult.addItem("已应答");
//		callResult.addItem("未应答");
//		callResult.setValue("全部");
//		callResult.setWidth("120px");
//		callResult.setNullSelectionAllowed(false);
//		callResultHLayout.addComponent(callResult);
//	}
	
	/**
	 * 创建 存放“客户编号” 的布局管理器
	 */
	private void createResourceIdHLayout() {
		HorizontalLayout resourceIdHLayout = new HorizontalLayout();
		resourceIdHLayout.setSpacing(true);
		gridLayout.addComponent(resourceIdHLayout, 3, 5);
		
		Label resourceId_lb = new Label("客户编号：");
		resourceId_lb.setWidth("-1px");
		resourceIdHLayout.addComponent(resourceId_lb);
		
		resourceId_tf = new TextField();
		resourceId_tf.setWidth("174px");
		resourceId_tf.addValidator(new RegexpValidator("\\d+", "客户编号只能由数字组成！"));
		resourceId_tf.setValidationVisible(false);
		resourceIdHLayout.addComponent(resourceId_tf);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(startTime.getValue() == null || finishTime.getValue() == null) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间和截止时间都不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(bridgedTimeMoreThan.isValid() == false || bridgedTimeLessThan.isValid() == false) {
				this.getApplication().getMainWindow().showNotification("通话时长 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!resourceId_tf.isValid()) {
				resourceId_tf.getApplication().getMainWindow().showNotification("客户编号 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			handleSearchEvent();
		} else if(source == clearButton) {
			timeScope.select("今天");
			dialingNo.setValue("");
			calledNo.setValue("");
			bridgedTimeMoreThan.setValue("");
			bridgedTimeAndOr.setValue("AND");
			bridgedTimeLessThan.setValue("");
			callDirection.setValue("all");
//			callResult.setValue("全部");
			empNo.setValue("");
			department.setValue("");
			username.setValue("");
			realName.setValue("");
			projectName.setValue("");
			dialType.setValue("all");
			userFieldInput.setValue("");
			bridgedResult.setValue("全部");
			resourceId_tf.setValue("");
		}
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(c) from Cdr as c where" + createFixedSql();
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.startTimeDate desc";
		
		// 通过翻页组件进行搜索
		callRecordTableFlip.setSearchSql(searchSql);
		callRecordTableFlip.setCountSql(countSql);
		callRecordTableFlip.refreshToFirstPage();
	}

	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createFixedSql() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String specificStartTimeSql = "";
		if(startTime.getValue() != null) {
			specificStartTimeSql = " and c.startTimeDate >= '" + dateFormat.format(startTime.getValue()) +"'";
		} 
		
		String specificFinishTimeSql = "";
		if(finishTime.getValue() != null) {
			specificFinishTimeSql = " and c.startTimeDate <= '" + dateFormat.format(finishTime.getValue()) +"'";
		}
		
		String dailingNoSql = "";
		String inputDailingNo = dialingNo.getValue().toString().trim();
		if(!"".equals(inputDailingNo)) {
			dailingNoSql = " and c.src like '%" + inputDailingNo + "%'";
		}
		
		String calledNoSql = "";
		String inputCalledNo = calledNo.getValue().toString().trim();
		if(!"".equals(inputCalledNo)) {
			calledNoSql = " and c.destination like '%" + inputCalledNo + "%'";
		}
		
		// 通话时长查询
		String bridgedTimeScopeSql = "";
		String inputBridgedMoreThanValue = bridgedTimeMoreThan.getValue().toString().trim();
		String inputBridgedLessThanValue = bridgedTimeLessThan.getValue().toString().trim();
		if(!"".equals(inputBridgedMoreThanValue) && !"".equals(inputBridgedLessThanValue)) {
			if(Integer.parseInt(inputBridgedLessThanValue) < Integer.parseInt(inputBridgedMoreThanValue)) {
				bridgedTimeAndOr.setValue("OR");
				bridgedTimeScopeSql = " and (c.ec2_billableSeconds >= " + inputBridgedMoreThanValue + " or c.ec2_billableSeconds <= " +inputBridgedLessThanValue + ")";
			} else { 
				bridgedTimeAndOr.setValue("AND");
				bridgedTimeScopeSql = " and (c.ec2_billableSeconds >= " + inputBridgedMoreThanValue + " and c.ec2_billableSeconds <= " +inputBridgedLessThanValue + ")";
			}
		} else if(!"".equals(inputBridgedMoreThanValue) && "".equals(inputBridgedLessThanValue)) {
			bridgedTimeScopeSql = " and c.ec2_billableSeconds >= " + inputBridgedMoreThanValue;
		} else if("".equals(inputBridgedMoreThanValue) && !"".equals(inputBridgedLessThanValue)) {
			bridgedTimeScopeSql = " and c.ec2_billableSeconds <= " + inputBridgedLessThanValue;
		} 
			
		// 呼叫时长查询
		String callTimeScopeSql = "";
		String inputCallMoreThanValue = callTimeMoreThan.getValue().toString().trim();
		String inputCallLessThanValue = callTimeLessThan.getValue().toString().trim();
		if(!"".equals(inputCallMoreThanValue) && !"".equals(inputCallLessThanValue)) {
			if(Integer.parseInt(inputCallLessThanValue) < Integer.parseInt(inputCallMoreThanValue)) {
				callTimeAndOr.setValue("OR");
				callTimeScopeSql = " and (c.duration >= " + inputCallMoreThanValue + " or c.duration <= " +inputCallLessThanValue + ")";
			} else { 
				callTimeAndOr.setValue("AND");
				callTimeScopeSql = " and (c.duration >= " + inputCallMoreThanValue + " and c.duration <= " +inputCallLessThanValue + ")";
			}
		} else if(!"".equals(inputCallMoreThanValue) && "".equals(inputCallLessThanValue)) {
			callTimeScopeSql = " and c.duration >= " + inputCallMoreThanValue;
		} else if("".equals(inputCallMoreThanValue) && !"".equals(inputCallLessThanValue)) {
			callTimeScopeSql = " and c.duration <= " + inputCallLessThanValue;
		} 
		
		// 接通时长查询
		String pickupTimeScopeSql = "";
		String inputPickupMoreThanValue = pickupTimeMoreThan.getValue().toString().trim();
		String inputPickupLessThanValue = pickupTimeLessThan.getValue().toString().trim();
		if(!"".equals(inputPickupMoreThanValue) && !"".equals(inputPickupLessThanValue)) {
			if(Integer.parseInt(inputPickupLessThanValue) < Integer.parseInt(inputPickupMoreThanValue)) {
				pickupTimeAndOr.setValue("OR");
				pickupTimeScopeSql = " and (c.billableSeconds >= " + inputPickupMoreThanValue + " or c.billableSeconds <= " +inputPickupLessThanValue + ")";
			} else { 
				pickupTimeAndOr.setValue("AND");
				pickupTimeScopeSql = " and (c.billableSeconds >= " + inputPickupMoreThanValue + " and c.billableSeconds <= " +inputPickupLessThanValue + ")";
			}
		} else if(!"".equals(inputPickupMoreThanValue) && "".equals(inputPickupLessThanValue)) {
			pickupTimeScopeSql = " and c.billableSeconds >= " + inputPickupMoreThanValue;
		} else if("".equals(inputPickupMoreThanValue) && !"".equals(inputPickupLessThanValue)) {
			pickupTimeScopeSql = " and c.billableSeconds <= " + inputPickupLessThanValue;
		} 
	
		String callDirSql = "";
		String direction = (String) callDirection.getValue();
		if("e2e".equals(direction)) {
			callDirSql = CdrDirection.e2e.getClass().getName() +"."+ "e2e";
		} else if("e2o".equals(direction)) {
			callDirSql = CdrDirection.e2o.getClass().getName() +"."+ "e2o";
		} else if("o2e".equals(direction)) {
			callDirSql = CdrDirection.o2e.getClass().getName() +"."+ "o2e";
		} else if("o2o".equals(direction)) {
			callDirSql = CdrDirection.o2o.getClass().getName() +"."+ "o2o";
		}
		if(!"all".equals(direction)) {
			callDirSql = " and c.cdrDirection = " +callDirSql;
		}
		
//		String callResultSql = "";
//		String inputCallResult = callResult.getValue().toString().trim();
//		if("已应答".equals(inputCallResult)) {
//			callResultSql = " and c.disposition = 'ANSWERED'";
//		} else if("未应答".equals(inputCallResult)) {
//			callResultSql = " and c.disposition != 'ANSWERED'";
//		}
		
		String empNoSql = "";
		String inputExten = empNo.getValue().toString().trim();
		if(!"".equals(inputExten)) {
			empNoSql = " and (c.srcEmpNo like '%" + inputExten + "%' or c.destEmpNo like '%" + inputExten + "%')";
		}

		String deptSql = "";
		String inputDept= department.getValue().toString().trim();
		if(!"".equals(inputDept)) {
			deptSql = " and (c.srcDeptName like '%" + inputDept + "%' or c.destDeptName like '%" + inputDept + "%')";
		}
		
		String usernameSql = "";
		String inputUsername = username.getValue().toString().trim();
		if(!"".equals(inputUsername)) {
			usernameSql = " and (c.srcUsername like '%" + inputUsername + "%' or c.destUsername like '%" + inputUsername + "%')";
		}
		
		String realNameSql = "";
		String inputRealName = realName.getValue().toString().trim();
		if(!"".equals(inputRealName)) {
			realNameSql = " and (c.srcRealName like '%" + inputRealName + "%' or c.srcRealName like '%" + inputRealName + "%')";
		}
		
		String projectSql = "";
		String inputProject = projectName.getValue().toString().trim();
		if(!"".equals(inputProject)) {
			projectSql = " and c.projectName like '%" + inputProject + "%'";
		}
		
		String userFieldSql = "";
		String inputUserField = userFieldInput.getValue().toString().trim();
		if(!"".equals(inputUserField)) {
			userFieldSql = " and (c.userField like '%" + inputUserField + "%' or c.userField like '%" + inputUserField + "%')";
		}
		
		String dialTypeSql = "";
		String inputDialType = (String) dialType.getValue();
		if("manual".equals(inputDialType)) {
			dialTypeSql = " and c.isAutoDial is null";
		} else if("autodial".equals(inputDialType)) {
			dialTypeSql = " and c.isAutoDial = true";
		} else if("soundsdial".equals(inputDialType)) {
			dialTypeSql = " and c.isAutoDial = false";
		}

		String bridgedResultSql = "";
		String inputBridgedResultSql = bridgedResult.getValue().toString().trim();
		if("已接通".equals(inputBridgedResultSql)) {
			bridgedResultSql = " and c.isBridged is not null and c.isBridged = true";
		} else if("未接通".equals(inputBridgedResultSql)) {
			bridgedResultSql = " and (c.isBridged is null or c.isBridged = false)";
		}

		String resourceIdSql = "";
		String inputResourceId = StringUtils.trimToEmpty((String) resourceId_tf.getValue());
		if(!"".equals(inputResourceId)) {
			resourceIdSql = " and c.resourceId = " + inputResourceId;
		}
		
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
		String allDeptSql = " and ( c.srcDeptId in (" +deptIdSql+ ") or c.destDeptId in (" +deptIdSql+ ") or (c.srcDeptId is null and c.destDeptId is null) )";
		
		// 创建固定的搜索语句
		return " c.domainId = " + domain.getId() + allDeptSql + bridgedTimeScopeSql + specificStartTimeSql + specificFinishTimeSql + dailingNoSql + calledNoSql + callTimeScopeSql 
				+ pickupTimeScopeSql + callDirSql + empNoSql + deptSql + usernameSql + realNameSql + projectSql + userFieldSql + dialTypeSql + bridgedResultSql + resourceIdSql;
	}
	
	/**
	 * 设置 CallRecordManagement 的界面的翻页组件
	 * @param callRecordTableFlip
	 */
	public void setCallRecordTableFlip(FlipOverTableComponent<Cdr> callRecordTableFlip) {
		this.callRecordTableFlip = callRecordTableFlip;
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
