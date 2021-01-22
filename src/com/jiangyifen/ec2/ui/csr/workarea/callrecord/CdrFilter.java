package com.jiangyifen.ec2.ui.csr.workarea.callrecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.CdrDirection;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.utils.CustomComboBox;
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
 * 话务员界面，呼叫记录过滤器
 * 
 * @author jrh
 */
@SuppressWarnings("serial")
public class CdrFilter extends VerticalLayout implements ClickListener {

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	private TextField dialingNo;			// “主叫”输入框
	private TextField calledNo;				// “被叫”输入框
	private TextField bridgedTimeMoreThan;	// “通话时长>=”输入框
	private ComboBox bridgedTimeAndOr;		// 关联词选择框，and 或 or
	private TextField bridgedTimeLessThan;	// “通话时长<=”输入框

	private TextField resourceId_tf;		// “客户编号”输入框
	private TextField projectName;			// “项目名称”输入框
	private CustomComboBox dialType;		// “呼叫类型”输入框
	private CustomComboBox callDirection;	// “呼叫方向”选择框
	private ComboBox bridgedResult;			// “是否接通”选择框

	private ComboBox timeScope;				// “联系时间”选择框
	private PopupDateField startTime;		// “开始时间”选择框
	private PopupDateField finishTime;		// “截止时间”选择框
	private TextField empNo;				// “主/被叫工号”输入框
	
	private Button searchButton;			// 搜索按钮
	private Button clearButton;				// 搜索按钮
	private PopupView complexSearchView;	// 复杂搜索界面
	
	private User loginUser;					// 当前登录用户
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private CdrTabView cdrTabView;
	private CdrComplexFilter cdrComplexFilter;			// Cdr 的高级过滤器组件
	private FlipOverTableComponent<Cdr> tableFlipOver;	// Cdr 信息Table显示区的翻页组件
	
	public CdrFilter() {
		this.setSpacing(true);
		
		loginUser = SpringContextHolder.getLoginUser();
		
		gridLayout = new GridLayout(6, 3);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 创建各水平布局管理器，并将组件加入 gridLayout 中
		//--------- 第一行  ----------//
		this.createDalingNoHLayout();
		this.createCalledNoHLayout();
		this.createBridgedTimeMoreThanHLayout();
		this.createBridgedTimeAndOrHLayout();
		this.createBridgedTimeLessThanHLayout();

		//--------- 第二行  ----------//
		this.createResourceIdHLayout();
		this.createProjectHLayout();
		this.createDialTypeHLayout();
		this.createCallDirHLayout();
		this.createBridgedResultHLayout();
		
		//--------- 第三行  ----------//
		this.createDialDateScopeHLayout();
		this.createStartTimeHLayout();
		this.createFinishTimeHLayout();
		this.createEmpnoHLayout();

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
		gridLayout.addComponent(searchButton, 5, 0);
		
		clearButton = new Button("清 空");
		clearButton.setDescription("快捷键(Ctrl+L)");
		clearButton.addListener(this);
		clearButton.setWidth("60px");
		clearButton.setClickShortcut(KeyCode.L, ModifierKey.CTRL);
		gridLayout.addComponent(clearButton, 5, 1);

		cdrComplexFilter = new CdrComplexFilter();
		cdrComplexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(cdrComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 5, 2);
		gridLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_CENTER);
	}

	/**
	 * 创建 存放“主叫号码标签和其选择框” 的布局管理器
	 */
	private void createDalingNoHLayout() {
		HorizontalLayout dalingNoHLayout = new HorizontalLayout();
		dalingNoHLayout.setSpacing(true);
		gridLayout.addComponent(dalingNoHLayout, 0, 0);
		
		Label dialingNoLabel = new Label("主 &nbsp; &nbsp; 叫 ：", Label.CONTENT_XHTML);
		dialingNoLabel.setWidth("-1px");
		dalingNoHLayout.addComponent(dialingNoLabel);
		
		dialingNo = new TextField();
		dialingNo.setWidth("105px");
		dalingNoHLayout.addComponent(dialingNo);
	}
	
	/**
	 * 创建 存放“被叫号码标签和其选择框” 的布局管理器
	 */
	private void createCalledNoHLayout() {
		HorizontalLayout calledNoHLayout = new HorizontalLayout();
		calledNoHLayout.setSpacing(true);
		gridLayout.addComponent(calledNoHLayout, 1, 0);
		
		Label calledNoLabel = new Label("被 &nbsp; &nbsp; 叫 ：", Label.CONTENT_XHTML);
		calledNoLabel.setWidth("-1px");
		calledNoHLayout.addComponent(calledNoLabel);
		
		calledNo = new TextField();
		calledNo.setWidth("154px");
		calledNoHLayout.addComponent(calledNo);
	}
	
	/**
	 * 创建  存放“通话时长>=标签和其选择框” 的布局管理器
	 */
	private void createBridgedTimeMoreThanHLayout() {
		HorizontalLayout moreThanHLayout = new HorizontalLayout();
		moreThanHLayout.setSpacing(true);
		gridLayout.addComponent(moreThanHLayout, 2, 0);
		
		Label talkTimeMoreThanLabel = new Label("通话时长>=：");
		talkTimeMoreThanLabel.setWidth("-1px");
		moreThanHLayout.addComponent(talkTimeMoreThanLabel);
		
		bridgedTimeMoreThan = new TextField();
		bridgedTimeMoreThan.setWidth("138px");
		bridgedTimeMoreThan.setInputPrompt("秒");
		bridgedTimeMoreThan.addValidator(new RegexpValidator("\\d+", "通话时长只能为数字组成！"));
		bridgedTimeMoreThan.setValidationVisible(false);
		moreThanHLayout.addComponent(bridgedTimeMoreThan);
	}
	
	/**
	 * 创建  存放“关联词标签和其选择框” 的布局管理器
	 */
	private void createBridgedTimeAndOrHLayout() {
		HorizontalLayout andOrHLayout = new HorizontalLayout();
		andOrHLayout.setSpacing(true);
		gridLayout.addComponent(andOrHLayout, 3, 0);
		
		Label correlativeLabel = new Label("关 联 词 ：");
		correlativeLabel.setWidth("-1px");
		andOrHLayout.addComponent(correlativeLabel);
		
		bridgedTimeAndOr = new ComboBox();
		bridgedTimeAndOr.addItem("AND");
		bridgedTimeAndOr.addItem("OR");
		bridgedTimeAndOr.setValue("AND");
		bridgedTimeAndOr.setWidth("90px");
		bridgedTimeAndOr.setNullSelectionAllowed(false);
		andOrHLayout.addComponent(bridgedTimeAndOr);
	}
	
	/**
	 * 创建  存放“通话时长<=标签和其选择框” 的布局管理器
	 */
	private void createBridgedTimeLessThanHLayout() {
		HorizontalLayout lessThanHLayout = new HorizontalLayout();
		lessThanHLayout.setSpacing(true);
		gridLayout.addComponent(lessThanHLayout, 4, 0);
		
		Label talkTimeLessThanLabel = new Label("通话时长<=：");
		talkTimeLessThanLabel.setWidth("-1px");
		lessThanHLayout.addComponent(talkTimeLessThanLabel);
		
		bridgedTimeLessThan = new TextField();
		bridgedTimeLessThan.setWidth("75px");
		bridgedTimeLessThan.setInputPrompt("秒");
		bridgedTimeLessThan.addValidator(new RegexpValidator("\\d+", "通话时长只能为数字组成！"));
		bridgedTimeLessThan.setValidationVisible(false);
		lessThanHLayout.addComponent(bridgedTimeLessThan);
	}
	

	/**
	 * 创建  存放“呼叫方向标签和其选择框” 的布局管理器
	 */
	private void createCallDirHLayout() {
		HorizontalLayout callDirHLayout = new HorizontalLayout();
		callDirHLayout.setSpacing(true);
		gridLayout.addComponent(callDirHLayout, 0, 1);
	
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
		callDirection.setWidth("105px");
		callDirection.setValue("all");
		callDirection.setNullSelectionAllowed(false);
		callDirHLayout.addComponent(callDirection);
	}

	/**
	 * 创建 存放“客户编号” 的布局管理器
	 */
	private void createResourceIdHLayout() {
		HorizontalLayout resourceIdHLayout = new HorizontalLayout();
		resourceIdHLayout.setSpacing(true);
		gridLayout.addComponent(resourceIdHLayout, 1, 1);
		
		Label resourceId_lb = new Label("客户编号：");
		resourceId_lb.setWidth("-1px");
		resourceIdHLayout.addComponent(resourceId_lb);
		
		resourceId_tf = new TextField();
		resourceId_tf.setWidth("154px");
		resourceId_tf.addValidator(new RegexpValidator("\\d+", "客户编号只能由数字组成！"));
		resourceId_tf.setValidationVisible(false);
		resourceIdHLayout.addComponent(resourceId_tf);
	}

	/**
	 * 创建 存放“项目名称” 的布局管理器
	 */
	private void createProjectHLayout() {
		HorizontalLayout projectHLayout = new HorizontalLayout();
		projectHLayout.setSpacing(true);
		gridLayout.addComponent(projectHLayout, 2, 1);
		
		Label projectLabel = new Label("项目名称：");
		projectLabel.setWidth("-1px");
		projectHLayout.addComponent(projectLabel);
		
		projectName = new TextField();
		projectName.setWidth("154px");
		projectHLayout.addComponent(projectName);
	}

	private void createBridgedResultHLayout() {
		HorizontalLayout callResultHLayout = new HorizontalLayout();
		callResultHLayout.setSpacing(true);
		gridLayout.addComponent(callResultHLayout, 3, 1);
		
		Label bridgedResultLabel = new Label("接通情况：");
		bridgedResultLabel.setWidth("-1px");
		callResultHLayout.addComponent(bridgedResultLabel);
		
		bridgedResult = new ComboBox();
		bridgedResult.addItem("全部");
		bridgedResult.addItem("已接通");
		bridgedResult.addItem("未接通");
		bridgedResult.setValue("全部");
		bridgedResult.setWidth("90px");
		bridgedResult.setNullSelectionAllowed(false);
		callResultHLayout.addComponent(bridgedResult);
	}

	/**
	 * 创建 存放“主/被叫工号” 的布局管理器
	 */
	private void createEmpnoHLayout() {
		HorizontalLayout extenHLayout = new HorizontalLayout();
		extenHLayout.setSpacing(true);
		gridLayout.addComponent(extenHLayout, 4, 1);
		
		Label extenLabel = new Label("主/被叫工号：");
		extenLabel.setWidth("-1px");
		extenHLayout.addComponent(extenLabel);
		
		empNo = new TextField();
		empNo.setWidth("75px");
		extenHLayout.addComponent(empNo);
	}

	/**
	 * 创建  存放“联系时间标签和其选择框” 的布局管理器
	 */
	private void createDialDateScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 0, 2);
		
		Label timeScopeLabel = new Label("联系时间：");
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
		timeScope.setWidth("105px");
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
		gridLayout.addComponent(startTimeHLayout, 1, 2);
				
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
		gridLayout.addComponent(finishTimeHLayout, 2, 2);
		
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
	 * 创建 存放“呼叫类型” 的布局管理器
	 */
	private void createDialTypeHLayout() {
		HorizontalLayout dialTypeHLayout = new HorizontalLayout();
		dialTypeHLayout.setSpacing(true);
		
		Label dialTypeLabel = new Label("呼叫类型：");
		dialTypeLabel.setWidth("-1px");
		dialTypeHLayout.addComponent(dialTypeLabel);
		
		TreeMap<String, String> treeMap = new TreeMap<String, String>();
		treeMap.put("all", "全部");
		treeMap.put("manual", "手动呼叫");
		treeMap.put("autodial", "自动群呼");
		treeMap.put("soundsdial", "语音群发");
		
		dialType = new CustomComboBox();
		dialType.setWidth("90px");
		dialType.setNullSelectionAllowed(false);
		dialType.setDataSource(treeMap);
		dialType.setValue("all");
		dialTypeHLayout.addComponent(dialType);
		gridLayout.addComponent(dialTypeHLayout, 3, 2);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			Date startDate = (Date) startTime.getValue();
			Date endDate = (Date) finishTime.getValue();
			if(startDate == null || endDate == null) {
				searchButton.getApplication().getMainWindow().showNotification("开始时间间和截止时间都不能为空！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(bridgedTimeMoreThan.isValid() == false || bridgedTimeLessThan.isValid() == false) {
				bridgedTimeMoreThan.getApplication().getMainWindow().showNotification("通话时长 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!resourceId_tf.isValid()) {
				resourceId_tf.getApplication().getMainWindow().showNotification("客户编号 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			handleSearchEvent();
			
			// 更新坐席个人话务情况统计信息
			cdrTabView.initializeStatisticInfo(startDate, endDate);
		} else if(source == clearButton) {
			timeScope.select("今天");
			
			dialingNo.setValue("");
			calledNo.setValue("");
			bridgedTimeMoreThan.setValue("");
			bridgedTimeAndOr.setValue("AND");
			bridgedTimeLessThan.setValue("");
			callDirection.setValue("all");
			empNo.setValue("");
			projectName.setValue("");
			dialType.setValue("all");
			resourceId_tf.setValue("");
		}
	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(c) from Cdr as c where" + createFixedSql();
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.startTimeDate desc";
		tableFlipOver.setSearchSql(searchSql);
		tableFlipOver.setCountSql(countSql);
		tableFlipOver.refreshToFirstPage();
	}

	/**
	 * 动态生成搜索语句
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String  createFixedSql() {
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
		
		String bridgedResultSql = "";
		String inputBridgedResultSql = bridgedResult.getValue().toString().trim();
		if("已接通".equals(inputBridgedResultSql)) {
			bridgedResultSql = " and c.isBridged is not null and c.isBridged is true";
		} else if("未接通".equals(inputBridgedResultSql)) {
			bridgedResultSql = " and (c.isBridged is null or c.isBridged = false)";
		}
		
		String extenSql = "";
		String inputExten = empNo.getValue().toString().trim();
		if(!"".equals(inputExten)) {
			extenSql = " and (c.srcEmpNo like '%" + inputExten + "%' or c.destEmpNo like '%" + inputExten + "%')";
		}
		
		String projectSql = "";
		String inputProject = projectName.getValue().toString().trim();
		if(!"".equals(inputProject)) {
			projectSql = " and c.projectName like '%" + inputProject + "%'";
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
		
		String resourceIdSql = "";
		String inputResourceId = StringUtils.trimToEmpty((String) resourceId_tf.getValue());
		if(!"".equals(inputResourceId)) {
			resourceIdSql = " and c.resourceId = " + inputResourceId;
		}
		
		return " (c.srcUsername = '"+loginUser.getUsername()+"' or c.destUsername = '"+loginUser.getUsername()+"') " + bridgedTimeScopeSql + specificStartTimeSql 
				+ specificFinishTimeSql + dailingNoSql + calledNoSql + callDirSql + bridgedResultSql + extenSql + projectSql + dialTypeSql + resourceIdSql;
	}
	
	/**
	 * 设置 cdrTabView 的界面的翻页组件
	 * @param tableFlipOver
	 */
	public void setTableFlipOver(FlipOverTableComponent<Cdr> tableFlipOver) {
		this.tableFlipOver = tableFlipOver;
		cdrComplexFilter.setTableFlipOver(tableFlipOver);
	}

	public void setCdrTabView(CdrTabView cdrTabView) {
		this.cdrTabView = cdrTabView;
		cdrComplexFilter.setCdrTabView(cdrTabView);
	}
	
}
