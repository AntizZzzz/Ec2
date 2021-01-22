package com.jiangyifen.ec2.ui.mgr.callrecordmanage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
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
 * 呼叫记录的搜索组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class CallRecordSimpleFilter extends VerticalLayout implements ClickListener {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private ComboBox timeScope;			// “时间范围”选择框
	private PopupDateField startTime;		// “开始时间”选择框
	private PopupDateField finishTime;		// “截止时间”选择框
	private TextField empNo;				// “主/被叫工号”输入框

	private TextField projectName;			// “项目名称”输入框
	private TextField bridgedTimeMoreThan;	// “通话时长大于等于”输入框
	private ComboBox bridgedTimeAndOr;	// 通话时长关联词选择框，and 或 or
	private TextField bridgedTimeLessThan;	// “通话时长小于等于”输入框

	private TextField dialingNo;			// “主叫”输入框
	private TextField calledNo;				// “被叫”输入框
//	private ComboBox callResult;			// “是否应答”选择框
	private ComboBox bridgedResult;			// “是否接通”选择框
	private TextField resourceId_tf;		// “客户编号”输入框

	private Button searchButton;			// 搜索按钮
	private Button clearButton;				// 搜索按钮
	private PopupView complexSearchView;	// 复杂搜索界面
	
	private Domain domain;					// 当前用户所属域
	private User loginUser;					// 当前登录用户
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private DepartmentService departmentService;
	private CallRecordComplexFilter callRecordComplexFilter;	// Cdr 的高级过滤器组件
	private FlipOverTableComponent<Cdr> callRecordTableFlip;	// Cdr 信息Table显示区的翻页组件
	
	public CallRecordSimpleFilter() {
		this.setSpacing(true);

		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		departmentService=SpringContextHolder.getBean("departmentService");
		
		gridLayout = new GridLayout(5, 4);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 创建各水平布局管理器，并将组件加入 gridLayout 中
		//--------- 第一行  -----------//
		this.createTimeScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		this.createExtenHLayout();
		
		//--------- 第二行  -----------//
		this.createProjectHLayout();
		this.createBridgedTimeMoreThanHLayout();
		this.createBridgedTimeAndOrHLayout();
		this.createBridgedTimeLessThanHLayout();

		//--------- 第三行  -----------//
		this.createDalingNoHLayout();
		this.createCalledNoHLayout();
		this.createBridgedResultHLayout();
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
		
		callRecordComplexFilter = new CallRecordComplexFilter();
		callRecordComplexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(callRecordComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 4, 2);
		gridLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_CENTER);
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
		timeScope.setWidth("150px");
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
	 * 创建 存放“主/被叫分机” 的布局管理器
	 */
	private void createExtenHLayout() {
		HorizontalLayout extenHLayout = new HorizontalLayout();
		extenHLayout.setSpacing(true);
		gridLayout.addComponent(extenHLayout, 3, 0);
		
		Label extenLabel = new Label("主/被叫工号：");
		extenLabel.setWidth("-1px");
		extenHLayout.addComponent(extenLabel);
		
		empNo = new TextField();
		empNo.setWidth("150px");
		extenHLayout.addComponent(empNo);
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
		projectName.setWidth("150px");
		projectHLayout.addComponent(projectName);
	}

	/**
	 * 创建  存放“通话时长大于等于标签和其选择框” 的布局管理器
	 */
	private void createBridgedTimeMoreThanHLayout() {
		HorizontalLayout moreThanHLayout = new HorizontalLayout();
		moreThanHLayout.setSpacing(true);
		gridLayout.addComponent(moreThanHLayout, 1, 1);
		
		Label talkTimeMoreThanLabel = new Label("通话时长>=：");
		talkTimeMoreThanLabel.setWidth("-1px");
		moreThanHLayout.addComponent(talkTimeMoreThanLabel);
		
		bridgedTimeMoreThan = new TextField();
		bridgedTimeMoreThan.setWidth("137px");
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
		
		Label correlativeLabel = new Label("关&nbsp;&nbsp;联&nbsp;&nbsp;词：",Label.CONTENT_XHTML);
		correlativeLabel.setWidth("-1px");
		andOrHLayout.addComponent(correlativeLabel);
		
		bridgedTimeAndOr = new ComboBox();
		bridgedTimeAndOr.addItem("AND");
		bridgedTimeAndOr.addItem("OR");
		bridgedTimeAndOr.setValue("AND");
		bridgedTimeAndOr.setWidth("152px");
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
		
		Label talkTimeLessThanLabel = new Label("通话时长<=：");
		talkTimeLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(talkTimeLessThanLabel);
		
		bridgedTimeLessThan = new TextField();
		bridgedTimeLessThan.setWidth("150px");
		bridgedTimeLessThan.setInputPrompt("秒");
		bridgedTimeLessThan.addValidator(new RegexpValidator("\\d*", "通话时长只能为数字组成！"));
		bridgedTimeLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(bridgedTimeLessThan);
		
		/*Label secondsLabel = new Label("秒");
		secondsLabel.setWidth("-1px");
		lessThanHLayout.addComponent(secondsLabel);*/
	}

	/**
	 * 创建 存放“主叫号码标签和其选择框” 的布局管理器
	 */
	private void createDalingNoHLayout() {
		HorizontalLayout dalingNoHLayout = new HorizontalLayout();
		dalingNoHLayout.setSpacing(true);
		gridLayout.addComponent(dalingNoHLayout, 0, 2);
		
		Label dialingNoLabel = new Label("主&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;叫：",Label.CONTENT_XHTML);
		dialingNoLabel.setWidth("-1px");
		dalingNoHLayout.addComponent(dialingNoLabel);
		
		dialingNo = new TextField();
		dialingNo.setWidth("149px");
		dalingNoHLayout.addComponent(dialingNo);
	}
	
	/**
	 * 创建 存放“被叫号码标签和其选择框” 的布局管理器
	 */
	private void createCalledNoHLayout() {
		HorizontalLayout calledNoHLayout = new HorizontalLayout();
		calledNoHLayout.setSpacing(true);
		gridLayout.addComponent(calledNoHLayout, 1, 2);
		
		Label calledNoLabel = new Label("被&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;叫：",Label.CONTENT_XHTML);
		calledNoLabel.setWidth("-1px");
		calledNoHLayout.addComponent(calledNoLabel);
		
		calledNo = new TextField();
		calledNo.setWidth("151px");
		calledNoHLayout.addComponent(calledNo);
	}
	
	private void createBridgedResultHLayout() {
		HorizontalLayout callResultHLayout = new HorizontalLayout();
		callResultHLayout.setSpacing(true);
		gridLayout.addComponent(callResultHLayout, 2, 2);
		
		Label bridgedResultLabel = new Label("接通情况：");
		bridgedResultLabel.setWidth("-1px");
		callResultHLayout.addComponent(bridgedResultLabel);
		
		bridgedResult = new ComboBox();
		bridgedResult.addItem("全部");
		bridgedResult.addItem("已接通");
		bridgedResult.addItem("未接通");
		bridgedResult.setValue("全部");
		bridgedResult.setWidth("155px");
		bridgedResult.setNullSelectionAllowed(false);
		callResultHLayout.addComponent(bridgedResult);
	}

//	/**
//	 * 创建  存放“是否接通标签和其选择框” 的布局管理器
//	 */
//	private void createCallStatusHLayout() {
//		HorizontalLayout callResultHLayout = new HorizontalLayout();
//		callResultHLayout.setSpacing(true);
//		gridLayout.addComponent(callResultHLayout, 3, 2);
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
//		callResult.setWidth("170px");
//		callResult.setNullSelectionAllowed(false);
//		callResultHLayout.addComponent(callResult);
//	}
	
	/**
	 * 创建 存放“客户编号” 的布局管理器
	 */
	private void createResourceIdHLayout() {
		HorizontalLayout resourceIdHLayout = new HorizontalLayout();
		resourceIdHLayout.setSpacing(true);
		gridLayout.addComponent(resourceIdHLayout, 3, 2);
		
		Label resourceId_lb = new Label("客&nbsp;户&nbsp;&nbsp;编&nbsp;号：",Label.CONTENT_XHTML);
		resourceId_lb.setWidth("-1px");
		resourceIdHLayout.addComponent(resourceId_lb);
		
		resourceId_tf = new TextField();
		resourceId_tf.setWidth("150px");
		resourceId_tf.addValidator(new RegexpValidator("\\d+", "客户编号只能由数字组成！"));
		resourceId_tf.setValidationVisible(false);
		resourceIdHLayout.addComponent(resourceId_tf);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(startTime.getValue() == null || finishTime.getValue() == null) {
				this.getApplication().getMainWindow().showNotification("开始时间间和截止时间都不能为空！", Notification.TYPE_WARNING_MESSAGE);
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
			empNo.setValue("");
			projectName.setValue("");
			bridgedResult.setValue("全部");
//			callResult.setValue("全部");
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
		
		String empNoSql = "";
		String inputExten = empNo.getValue().toString().trim();
		if(!"".equals(inputExten)) {
			empNoSql = " and (c.srcEmpNo like '%" + inputExten + "%' or c.destEmpNo like '%" + inputExten + "%')";
		}
		
//		String callResultSql = "";
//		String inputCallResult = callResult.getValue().toString().trim();
//		if("已应答".equals(inputCallResult)) {
//			callResultSql = " and c.disposition = 'ANSWERED'";
//		} else if("未应答".equals(inputCallResult)) {
//			callResultSql = " and c.disposition != 'ANSWERED'";
//		}
		
		String bridgedResultSql = "";
		String inputBridgedResultSql = bridgedResult.getValue().toString().trim();
		if("已接通".equals(inputBridgedResultSql)) {
			bridgedResultSql = " and c.isBridged is not null and c.isBridged = true";
		} else if("未接通".equals(inputBridgedResultSql)) {
			bridgedResultSql = " and (c.isBridged is null or c.isBridged = false)";
		}
		
		String projectSql = "";
		String inputProject = projectName.getValue().toString().trim();
		if(!"".equals(inputProject)) {
			projectSql = " and c.projectName like '%" + inputProject + "%'";
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
		return " c.domainId = " + domain.getId() + allDeptSql + bridgedTimeScopeSql + specificStartTimeSql 
				+ specificFinishTimeSql + dailingNoSql + calledNoSql + empNoSql + projectSql + bridgedResultSql + resourceIdSql;
	}
	
	/**
	 * 设置 CallRecordManagement 的界面的翻页组件
	 * @param callRecordTableFlip
	 */
	public void setCallRecordTableFlip(FlipOverTableComponent<Cdr> callRecordTableFlip) {
		this.callRecordTableFlip = callRecordTableFlip;
		callRecordComplexFilter.setCallRecordTableFlip(callRecordTableFlip);
	}
	
}
