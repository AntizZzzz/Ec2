package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.WorkflowTransferLog;
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
 * 工作流迁移日志高级查询组件
 * 
 * @author jrh
 * 
 * 2013-7-30
 */
@SuppressWarnings("serial")
public class WorkflowTransferLogComplexFilter extends VerticalLayout implements ClickListener, Content {
	
	private final SimpleDateFormat SDF_DAY = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat SDF_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final Calendar CALENDAR = Calendar.getInstance();

	private GridLayout gridLayout;			// 面板中的布局管理器
	
	//--------------  第一行  --------------// 
	private TextField logId_tf;
	private ComboBox importTimeScope_cb;			// “原始数据导入时间”选择框
	private PopupDateField startImportTime_pdf;			// “开始导入时间”选择框
	private PopupDateField finishImportTime_pdf;		// “截止导入时间”选择框

	//--------------  第二行  --------------// 
	private TextField workflow1CsrEmpNo_tf;				// 土豆获取人工号
	private TextField workflow1CsrDeptName_tf;			// 土豆获取人部门
	private PopupDateField migrateToWorkflow1Time_pdf;	// 转入土豆池时间
	private PopupDateField workflow1PickTaskTime_pdf;	// 土豆获取时间
	
	//--------------  第三行  --------------// 
	private TextField workflow2CsrEmpNo_tf;				// 一销获取人工号
	private TextField workflow2CsrDeptName_tf;			// 一销获取人部门
	private PopupDateField migrateToWorkflow2Time_pdf;	// 一销获取时间
	private PopupDateField workflow2PickTaskTime_pdf;	// 一销获取时间
	
	//--------------  第四行  --------------// 
	private TextField workflow3CsrEmpNo_tf;				// 二销获取人工号
	private TextField workflow3CsrDeptName_tf;			// 二销获取人部门
	private PopupDateField migrateToWorkflow3Time_pdf;	// 二销获取时间
	private PopupDateField workflow3PickTaskTime_pdf;	// 二销获取时间
	
	//--------------  第五行  --------------// 
	private TextField customerId_tf;					// 客户编号输入文本框
	private TextField customerName_tf;					// 客户姓名输入文本框
	private TextField customerPhone_tf;					// 客户手机输入文本框
	private TextField importorEmpNo_tf;					// 导入者工号输入文本框
	
	//--------------  第六行  --------------// 
	private TextField importorDept_tf;					// 导入者部门输入文本框
	
	private Button search_bt;							// 查询结果按钮
	private Button clear_bt;							// 清空输入内容
	private Label count_lb;								// 显示查询结果数
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private User loginUser; 							// 当前的登陆用户
	private Domain domain;								// 当前用户所属域
	private String deptSearchSql;						// 部门范围搜索语句

	private FlipOverTableComponent<WorkflowTransferLog> workflowTransferLogTableFlip;	// 为Table添加的翻页组件
	
	private DepartmentService departmentService;		// 部门服务类
	
	public WorkflowTransferLogComplexFilter() {
		this.setSpacing(true);

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		departmentService = SpringContextHolder.getBean("departmentService");
		
		gridLayout = new GridLayout(6, 6);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 初始化部门搜索语句
		deptSearchSql = createDeptSearchSql();

		//--------- 第一行  -----------//
		this.createLogIdHLayout();
		this.createImportTimeScopeHLayout();
		this.createStartImportTimeHLayout();
		this.createFinishImportTimeHLayout();


		//--------- 第二行  -----------//
		this.createWorkflow1CsrEmpNoHLayout();
		this.createWorkflow1CsrDeptNameHLayout();
		this.createMigrateToWorkflow1TimeHLayout();
		this.createWorkflow1PickTaskTimeHLayout();
		
		//--------- 第三行  -----------//
		this.createWorkflow2CsrEmpNoHLayout();
		this.createWorkflow2CsrDeptNameHLayout();
		this.createMigrateToWorkflow2TimeHLayout();
		this.createWorkflow2PickTaskTimeHLayout();
		
		//--------- 第四行  -----------//
		this.createWorkflow3CsrEmpNoHLayout();
		this.createWorkflow3CsrDeptNameHLayout();
		this.createMigrateToWorkflow3TimeHLayout();
		this.createWorkflow3PickTaskTimeHLayout();
		
		//--------- 第五行  -----------//
		this.createCustomerIdHLayout();
		this.createCustomerNameHLayout();
		this.createCustomerPhoneHLayout();
		this.createOperatorEmpNoHLayout();

		//--------- 第六行  -----------//
		this.createimportorDeptHLayout();
		
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
		String sql = " and wtl.importorDeptId in (" +deptIdSql+ ")";
		
		return sql;
	}

	/**
	 * 创建操作按钮
	 */
	private void createOperateButtons() {
		search_bt = new Button("查 询");
		search_bt.addStyleName("default");
		search_bt.addListener(this);
		search_bt.setWidth("60px");
		gridLayout.addComponent(search_bt, 5, 0);
		
		clear_bt = new Button("清 空");
		clear_bt.addListener(this);
		clear_bt.setWidth("60px");
		gridLayout.addComponent(clear_bt, 5, 1);
		
		HorizontalLayout countHLayout = new HorizontalLayout();
		gridLayout.addComponent(countHLayout, 5, 3);
		
		Label countCaption = new Label("总数：");
		countCaption.setWidth("-1px");
		countHLayout.addComponent(countCaption);
		
		count_lb = new Label("");
		countHLayout.addComponent(count_lb);
	}

	/**
	 * 创建 存放“日志编号” 的布局管理器
	 */
	private void createLogIdHLayout() {
		HorizontalLayout logIdHLayout = new HorizontalLayout();
		logIdHLayout.setSpacing(true);
		gridLayout.addComponent(logIdHLayout, 0, 0);
		
		// 客户姓名输入区
		Label logIdLabel = new Label("日志编号：");
		logIdLabel.setWidth("-1px");
		logIdHLayout.addComponent(logIdLabel);
		
		logId_tf = new TextField();
		logId_tf.setWidth("120px");	
		logId_tf.addValidator(new RegexpValidator("\\d+", "导入者工号 只能由数字组成！"));
		logId_tf.setValidationVisible(false);
		logIdHLayout.addComponent(logId_tf);
	}
	
	/**
	 * 创建  存放“原始数据导入时间范围标签和其选择框” 的布局管理器
	 */
	private void createImportTimeScopeHLayout() {
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 1, 0);
		
		Label timeScopeLabel = new Label("数据导入时间：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		importTimeScope_cb = new ComboBox();
		importTimeScope_cb.setImmediate(true);
		importTimeScope_cb.addItem("今天");
		importTimeScope_cb.addItem("昨天");
		importTimeScope_cb.addItem("本周");
		importTimeScope_cb.addItem("上周");
		importTimeScope_cb.addItem("本月");
		importTimeScope_cb.addItem("上月");
		importTimeScope_cb.addItem("精确时间");
		importTimeScope_cb.setValue("本周");
		importTimeScope_cb.setWidth("132px");
		importTimeScope_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		importTimeScope_cb.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(importTimeScope_cb);
		
		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)importTimeScope_cb.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startImportTime_pdf.removeListener(startTimeListener);
				finishImportTime_pdf.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startImportTime_pdf.setValue(dates[0]);
				finishImportTime_pdf.setValue(dates[1]);
				startImportTime_pdf.addListener(startTimeListener);
				finishImportTime_pdf.addListener(finishTimeListener);
			}
		};
		importTimeScope_cb.addListener(timeScopeListener);
	}

	/**
	 * 创建  存放“开始时间标签和其选择框” 的布局管理器
	 */
	private void createStartImportTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
	
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 2, 0);
				
		Label startTimeLabel = new Label("开始导入时间：");
		startTimeLabel.setWidth("-1px");
		startTimeHLayout.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				importTimeScope_cb.removeListener(timeScopeListener);
				importTimeScope_cb.setValue("精确时间");
				importTimeScope_cb.addListener(timeScopeListener);
			}
		};
		
		startImportTime_pdf = new PopupDateField();
		startImportTime_pdf.setWidth("153px");
		startImportTime_pdf.setImmediate(true);
		startImportTime_pdf.setValue(dates[0]);
		startImportTime_pdf.addListener(startTimeListener);
		startImportTime_pdf.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startImportTime_pdf.setParseErrorMessage("时间格式不合法");
		startImportTime_pdf.setValidationVisible(false);
		startImportTime_pdf.setResolution(PopupDateField.RESOLUTION_SEC);
		startTimeHLayout.addComponent(startImportTime_pdf);
	}

	/**
	 * 创建  存放“截止时间标签和其选择框” 的布局管理器
	 */
	private void createFinishImportTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("本月");
		
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 3, 0);
		
		Label finishTimeLabel = new Label("截止导入时间：");
		finishTimeLabel.setWidth("-1px");
		finishTimeHLayout.addComponent(finishTimeLabel);
		
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				importTimeScope_cb.removeListener(finishTimeListener);
				importTimeScope_cb.setValue("精确时间");
				importTimeScope_cb.addListener(timeScopeListener);
			}
		};
		
		finishImportTime_pdf = new PopupDateField();
		finishImportTime_pdf.setImmediate(true);
		finishImportTime_pdf.setWidth("154px");
		finishImportTime_pdf.setValue(dates[1]);
		finishImportTime_pdf.addListener(finishTimeListener);
		finishImportTime_pdf.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishImportTime_pdf.setParseErrorMessage("时间格式不合法");
		finishImportTime_pdf.setValidationVisible(false);
		finishImportTime_pdf.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTimeHLayout.addComponent(finishImportTime_pdf);
	}

	/**
	 * 创建 存放“土豆获取人工号” 的布局管理器
	 */
	private void createWorkflow1CsrEmpNoHLayout() {
		HorizontalLayout workflow1CsrEmpNoHLayout = new HorizontalLayout();
		workflow1CsrEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(workflow1CsrEmpNoHLayout, 0, 1);
		
		// 土豆获取人工号
		Label workflow1CsrEmpNoLabel = new Label("土豆获取人工号：");
		workflow1CsrEmpNoLabel.setWidth("-1px");
		workflow1CsrEmpNoHLayout.addComponent(workflow1CsrEmpNoLabel);
		
		workflow1CsrEmpNo_tf = new TextField();
		workflow1CsrEmpNo_tf.setWidth("81px");	
		workflow1CsrEmpNoHLayout.addComponent(workflow1CsrEmpNo_tf);
	}
	
	/**
	 * 创建 存放“土豆获取人部门” 的布局管理器
	 */
	private void createWorkflow1CsrDeptNameHLayout() {
		HorizontalLayout workflow1CsrDeptNameHLayout = new HorizontalLayout();
		workflow1CsrDeptNameHLayout.setSpacing(true);
		gridLayout.addComponent(workflow1CsrDeptNameHLayout, 1, 1);
		
		// 土豆获取人部门
		Label workflow1CsrDeptNameLabel = new Label("土豆获取人部门：");
		workflow1CsrDeptNameLabel.setWidth("-1px");
		workflow1CsrDeptNameHLayout.addComponent(workflow1CsrDeptNameLabel);
		
		workflow1CsrDeptName_tf = new TextField();
		workflow1CsrDeptName_tf.setWidth("120px");	
		workflow1CsrDeptNameHLayout.addComponent(workflow1CsrDeptName_tf);
	}
	
	/**
	 * 创建  存放“转入土豆池时间” 的布局管理器
	 */
	private void createMigrateToWorkflow1TimeHLayout() {
		HorizontalLayout migrateToWorkflow1HLayout = new HorizontalLayout();
		migrateToWorkflow1HLayout.setSpacing(true);
		gridLayout.addComponent(migrateToWorkflow1HLayout, 2, 1);
		
		Label migrateToWorkflow1Label = new Label("转入土豆池时间：");
		migrateToWorkflow1Label.setWidth("-1px");
		migrateToWorkflow1HLayout.addComponent(migrateToWorkflow1Label);
		
		migrateToWorkflow1Time_pdf = new PopupDateField();
		migrateToWorkflow1Time_pdf.setWidth("153px");
		migrateToWorkflow1Time_pdf.setImmediate(true);
		migrateToWorkflow1Time_pdf.setValidationVisible(false);
		migrateToWorkflow1Time_pdf.setDateFormat("yyyy-MM-dd");
		migrateToWorkflow1Time_pdf.setParseErrorMessage("时间格式不合法");
		migrateToWorkflow1Time_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
		migrateToWorkflow1HLayout.addComponent(migrateToWorkflow1Time_pdf);
	}
	
	/**
	 * 创建  存放“转入土豆池时间” 的布局管理器
	 */
	private void createWorkflow1PickTaskTimeHLayout() {
		HorizontalLayout workflow1PickTaskHLayout = new HorizontalLayout();
		workflow1PickTaskHLayout.setSpacing(true);
		gridLayout.addComponent(workflow1PickTaskHLayout, 3, 1);
		
		Label workflow1PickTaskLabel = new Label("土豆获取时间：");
		workflow1PickTaskLabel.setWidth("-1px");
		workflow1PickTaskHLayout.addComponent(workflow1PickTaskLabel);
		
		workflow1PickTaskTime_pdf = new PopupDateField();
		workflow1PickTaskTime_pdf.setWidth("153px");
		workflow1PickTaskTime_pdf.setImmediate(true);
		workflow1PickTaskTime_pdf.setValidationVisible(false);
		workflow1PickTaskTime_pdf.setDateFormat("yyyy-MM-dd");
		workflow1PickTaskTime_pdf.setParseErrorMessage("时间格式不合法");
		workflow1PickTaskTime_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
		workflow1PickTaskHLayout.addComponent(workflow1PickTaskTime_pdf);
	}
	

	/**
	 * 创建 存放“一销获取人工号” 的布局管理器
	 */
	private void createWorkflow2CsrEmpNoHLayout() {
		HorizontalLayout workflow2CsrEmpNoHLayout = new HorizontalLayout();
		workflow2CsrEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(workflow2CsrEmpNoHLayout, 0, 2);
		
		// 一销获取人工号
		Label workflow2CsrEmpNoLabel = new Label("一销获取人工号：");
		workflow2CsrEmpNoLabel.setWidth("-1px");
		workflow2CsrEmpNoHLayout.addComponent(workflow2CsrEmpNoLabel);
		
		workflow2CsrEmpNo_tf = new TextField();
		workflow2CsrEmpNo_tf.setWidth("81px");	
		workflow2CsrEmpNoHLayout.addComponent(workflow2CsrEmpNo_tf);
	}
	
	/**
	 * 创建 存放“一销获取人部门” 的布局管理器
	 */
	private void createWorkflow2CsrDeptNameHLayout() {
		HorizontalLayout workflow2CsrDeptNameHLayout = new HorizontalLayout();
		workflow2CsrDeptNameHLayout.setSpacing(true);
		gridLayout.addComponent(workflow2CsrDeptNameHLayout, 1, 2);
		
		// 一销获取人部门
		Label workflow2CsrDeptNameLabel = new Label("一销获取人部门：");
		workflow2CsrDeptNameLabel.setWidth("-1px");
		workflow2CsrDeptNameHLayout.addComponent(workflow2CsrDeptNameLabel);
		
		workflow2CsrDeptName_tf = new TextField();
		workflow2CsrDeptName_tf.setWidth("120px");	
		workflow2CsrDeptNameHLayout.addComponent(workflow2CsrDeptName_tf);
	}
	
	/**
	 * 创建  存放“转入一销池时间” 的布局管理器
	 */
	private void createMigrateToWorkflow2TimeHLayout() {
		HorizontalLayout migrateToWorkflow2HLayout = new HorizontalLayout();
		migrateToWorkflow2HLayout.setSpacing(true);
		gridLayout.addComponent(migrateToWorkflow2HLayout, 2, 2);
		
		Label migrateToWorkflow2Label = new Label("转入一销池时间：");
		migrateToWorkflow2Label.setWidth("-1px");
		migrateToWorkflow2HLayout.addComponent(migrateToWorkflow2Label);
		
		migrateToWorkflow2Time_pdf = new PopupDateField();
		migrateToWorkflow2Time_pdf.setWidth("153px");
		migrateToWorkflow2Time_pdf.setImmediate(true);
		migrateToWorkflow2Time_pdf.setValidationVisible(false);
		migrateToWorkflow2Time_pdf.setDateFormat("yyyy-MM-dd");
		migrateToWorkflow2Time_pdf.setParseErrorMessage("时间格式不合法");
		migrateToWorkflow2Time_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
		migrateToWorkflow2HLayout.addComponent(migrateToWorkflow2Time_pdf);
	}
	
	/**
	 * 创建  存放“转入一销池时间” 的布局管理器
	 */
	private void createWorkflow2PickTaskTimeHLayout() {
		HorizontalLayout workflow2PickTaskHLayout = new HorizontalLayout();
		workflow2PickTaskHLayout.setSpacing(true);
		gridLayout.addComponent(workflow2PickTaskHLayout, 3, 2);
		
		Label workflow2PickTaskLabel = new Label("一销获取时间：");
		workflow2PickTaskLabel.setWidth("-1px");
		workflow2PickTaskHLayout.addComponent(workflow2PickTaskLabel);
		
		workflow2PickTaskTime_pdf = new PopupDateField();
		workflow2PickTaskTime_pdf.setWidth("153px");
		workflow2PickTaskTime_pdf.setImmediate(true);
		workflow2PickTaskTime_pdf.setValidationVisible(false);
		workflow2PickTaskTime_pdf.setDateFormat("yyyy-MM-dd");
		workflow2PickTaskTime_pdf.setParseErrorMessage("时间格式不合法");
		workflow2PickTaskTime_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
		workflow2PickTaskHLayout.addComponent(workflow2PickTaskTime_pdf);
	}
	
	

	/**
	 * 创建 存放“二销获取人工号” 的布局管理器
	 */
	private void createWorkflow3CsrEmpNoHLayout() {
		HorizontalLayout workflow3CsrEmpNoHLayout = new HorizontalLayout();
		workflow3CsrEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(workflow3CsrEmpNoHLayout, 0, 3);
		
		// 二销获取人工号
		Label workflow3CsrEmpNoLabel = new Label("二销获取人工号：");
		workflow3CsrEmpNoLabel.setWidth("-1px");
		workflow3CsrEmpNoHLayout.addComponent(workflow3CsrEmpNoLabel);
		
		workflow3CsrEmpNo_tf = new TextField();
		workflow3CsrEmpNo_tf.setWidth("81px");	
		workflow3CsrEmpNoHLayout.addComponent(workflow3CsrEmpNo_tf);
	}
	
	/**
	 * 创建 存放“二销获取人部门” 的布局管理器
	 */
	private void createWorkflow3CsrDeptNameHLayout() {
		HorizontalLayout workflow3CsrDeptNameHLayout = new HorizontalLayout();
		workflow3CsrDeptNameHLayout.setSpacing(true);
		gridLayout.addComponent(workflow3CsrDeptNameHLayout, 1, 3);
		
		// 二销获取人部门
		Label workflow3CsrDeptNameLabel = new Label("二销获取人部门：");
		workflow3CsrDeptNameLabel.setWidth("-1px");
		workflow3CsrDeptNameHLayout.addComponent(workflow3CsrDeptNameLabel);
		
		workflow3CsrDeptName_tf = new TextField();
		workflow3CsrDeptName_tf.setWidth("120px");	
		workflow3CsrDeptNameHLayout.addComponent(workflow3CsrDeptName_tf);
	}
	
	/**
	 * 创建  存放“转入二销池时间” 的布局管理器
	 */
	private void createMigrateToWorkflow3TimeHLayout() {
		HorizontalLayout migrateToWorkflow3HLayout = new HorizontalLayout();
		migrateToWorkflow3HLayout.setSpacing(true);
		gridLayout.addComponent(migrateToWorkflow3HLayout, 2, 3);
		
		Label migrateToWorkflow3Label = new Label("转入二销池时间：");
		migrateToWorkflow3Label.setWidth("-1px");
		migrateToWorkflow3HLayout.addComponent(migrateToWorkflow3Label);
		
		migrateToWorkflow3Time_pdf = new PopupDateField();
		migrateToWorkflow3Time_pdf.setWidth("153px");
		migrateToWorkflow3Time_pdf.setImmediate(true);
		migrateToWorkflow3Time_pdf.setValidationVisible(false);
		migrateToWorkflow3Time_pdf.setDateFormat("yyyy-MM-dd");
		migrateToWorkflow3Time_pdf.setParseErrorMessage("时间格式不合法");
		migrateToWorkflow3Time_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
		migrateToWorkflow3HLayout.addComponent(migrateToWorkflow3Time_pdf);
	}
	
	/**
	 * 创建  存放“转入二销池时间” 的布局管理器
	 */
	private void createWorkflow3PickTaskTimeHLayout() {
		HorizontalLayout workflow3PickTaskHLayout = new HorizontalLayout();
		workflow3PickTaskHLayout.setSpacing(true);
		gridLayout.addComponent(workflow3PickTaskHLayout, 3, 3);
		
		Label workflow3PickTaskLabel = new Label("二销获取时间：");
		workflow3PickTaskLabel.setWidth("-1px");
		workflow3PickTaskHLayout.addComponent(workflow3PickTaskLabel);
		
		workflow3PickTaskTime_pdf = new PopupDateField();
		workflow3PickTaskTime_pdf.setWidth("153px");
		workflow3PickTaskTime_pdf.setImmediate(true);
		workflow3PickTaskTime_pdf.setValidationVisible(false);
		workflow3PickTaskTime_pdf.setDateFormat("yyyy-MM-dd");
		workflow3PickTaskTime_pdf.setParseErrorMessage("时间格式不合法");
		workflow3PickTaskTime_pdf.setResolution(PopupDateField.RESOLUTION_DAY);
		workflow3PickTaskHLayout.addComponent(workflow3PickTaskTime_pdf);
	}
	
	/**
	 * 创建 存放“客户编号” 的布局管理器
	 */
	private void createCustomerIdHLayout() {
		HorizontalLayout customerIdHLayout = new HorizontalLayout();
		customerIdHLayout.setSpacing(true);
		gridLayout.addComponent(customerIdHLayout, 0, 4);
		
		Label customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		customerIdHLayout.addComponent(customerIdLabel);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户编号 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("120px");
		customerIdHLayout.addComponent(customerId_tf);
	}
	
	/**
	 * 创建 存放“客户姓名” 的布局管理器
	 */
	private void createCustomerNameHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 1, 4);
		
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		customerName_tf = new TextField();
		customerName_tf.setWidth("158px");	
		customerNameHLayout.addComponent(customerName_tf);
	}

	/**
	 * 创建 存放“客户电话” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout telephoneHLayout = new HorizontalLayout();
		telephoneHLayout.setSpacing(true);
		gridLayout.addComponent(telephoneHLayout, 2, 4);

		// 客户电话输入区
		Label telephoneLabel = new Label("客户电话：");
		telephoneLabel.setWidth("-1px");
		telephoneHLayout.addComponent(telephoneLabel);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("166px");
		customerPhone_tf.addValidator(new RegexpValidator("\\d+", "客户电话 只能由数字组成！"));
		customerPhone_tf.setValidationVisible(false);
		telephoneHLayout.addComponent(customerPhone_tf);
	}
	
	/**
	 * 创建 存放“导入者工号” 的布局管理器
	 */
	private void createOperatorEmpNoHLayout() {
		HorizontalLayout operatorEmpNoHLayout = new HorizontalLayout();
		operatorEmpNoHLayout.setSpacing(true);
		gridLayout.addComponent(operatorEmpNoHLayout, 3, 4);
		
		// 导入者工号输入区
		Label operatorEmpNoLabel = new Label("导入者工号：");
		operatorEmpNoLabel.setWidth("-1px");
		operatorEmpNoHLayout.addComponent(operatorEmpNoLabel);
		
		importorEmpNo_tf = new TextField();
		importorEmpNo_tf.setWidth("140px");	
		operatorEmpNoHLayout.addComponent(importorEmpNo_tf);
	}
	
	/**
	 * 创建 存放“导入者部门” 的布局管理器
	 */
	private void createimportorDeptHLayout() {
		HorizontalLayout deptHLayout = new HorizontalLayout();
		deptHLayout.setSpacing(true);
		gridLayout.addComponent(deptHLayout, 0, 5);
		
		Label deptLabel = new Label("导入者部门：");
		deptLabel.setWidth("-1px");
		deptHLayout.addComponent(deptLabel);

		importorDept_tf = new TextField();
		importorDept_tf.setWidth("107px");
		deptHLayout.addComponent(importorDept_tf);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search_bt) {
			boolean validValue = comfirmValues();
			if(validValue) {	// 数据校验成功
				handleSearchEvent();
			}
		} else if(source == clear_bt) {
			excuteClearValues();
		}
	}

	/**
	 * 校验输入信息
	 * @return
	 */
	private boolean comfirmValues() {
		if(!logId_tf.isValid()) {
			logId_tf.getApplication().getMainWindow().showNotification("日志编号 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(startImportTime_pdf.getValue() == null || finishImportTime_pdf.getValue() == null) {
			startImportTime_pdf.getApplication().getMainWindow().showNotification("开始导入时间和截止导入时间 都不能为空, 并且格式要正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(!migrateToWorkflow1Time_pdf.isValid()) {
			migrateToWorkflow1Time_pdf.getApplication().getMainWindow().showNotification("转入土豆池时间 格式不正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(!workflow1PickTaskTime_pdf.isValid()) {
			workflow1PickTaskTime_pdf.getApplication().getMainWindow().showNotification("土豆获取时间 格式不正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(!migrateToWorkflow2Time_pdf.isValid()) {
			migrateToWorkflow2Time_pdf.getApplication().getMainWindow().showNotification("转入土豆池时间 格式不正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(!workflow2PickTaskTime_pdf.isValid()) {
			workflow2PickTaskTime_pdf.getApplication().getMainWindow().showNotification("土豆获取时间 格式不正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(!migrateToWorkflow3Time_pdf.isValid()) {
			migrateToWorkflow3Time_pdf.getApplication().getMainWindow().showNotification("转入土豆池时间 格式不正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(!workflow3PickTaskTime_pdf.isValid()) {
			workflow3PickTaskTime_pdf.getApplication().getMainWindow().showNotification("土豆获取时间 格式不正确！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		if(!customerId_tf.isValid()) {
			customerId_tf.getApplication().getMainWindow().showNotification("客户编号 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(!customerPhone_tf.isValid()) {
			customerPhone_tf.getApplication().getMainWindow().showNotification("客户电话 只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 执行清空操作
	 */
	private void excuteClearValues() {
		logId_tf.setValue("");
		importTimeScope_cb.select("本周");

		workflow1CsrEmpNo_tf.setValue("");
		workflow1CsrDeptName_tf.setValue("");
		migrateToWorkflow1Time_pdf.setValue(null);
		workflow1PickTaskTime_pdf.setValue(null);
		
		workflow2CsrEmpNo_tf.setValue("");
		workflow2CsrDeptName_tf.setValue("");
		migrateToWorkflow2Time_pdf.setValue(null);
		workflow2PickTaskTime_pdf.setValue(null);
		
		workflow3CsrEmpNo_tf.setValue("");
		workflow3CsrDeptName_tf.setValue("");
		migrateToWorkflow3Time_pdf.setValue(null);
		workflow3PickTaskTime_pdf.setValue(null);
		
		customerId_tf.setValue("");
		customerName_tf.setValue("");
		customerPhone_tf.setValue("");
		importorEmpNo_tf.setValue("");
		
		importorDept_tf.setValue("");

	}

	/**
	 *  处理搜索事件
	 */
	private void handleSearchEvent() {
		String countSql = "select count(wtl) from WorkflowTransferLog as wtl where wtl.domainId = " +domain.getId() + deptSearchSql + createDynamicSql();
		String searchSql = countSql.replaceFirst("count\\(wtl\\)", "wtl") + " order by wtl.id desc";
		workflowTransferLogTableFlip.setSearchSql(searchSql);
		workflowTransferLogTableFlip.setCountSql(countSql);
		workflowTransferLogTableFlip.refreshToFirstPage();
		
		int count = workflowTransferLogTableFlip.getTotalRecord();
		count_lb.setValue(count);
	}

	/**
	 * 动态生成搜索语句 
	 * 		查询组件中生成的固定查询语句
	 * @return
	 */
	private String createDynamicSql() {
		// 日志编号
		String logIdSql = "";
		String inputLogId = StringUtils.trimToEmpty((String) logId_tf.getValue());
		if(!"".equals(inputLogId)) {
			logIdSql = " and wtl.id = " + inputLogId;
		}
		
		// 资源的导入日期查询
		String specificStartImportTimeSql = "";
		Date startImportTime = (Date) startImportTime_pdf.getValue();
		if(startImportTime != null) {
			specificStartImportTimeSql = " and wtl.importCustomerTime >= '" + SDF_SEC.format(startImportTime) +"'";
		} 
		
		String specificFinishImportTimeSql = "";
		Date finishImportTime = (Date) finishImportTime_pdf.getValue();
		if(finishImportTime != null) {
			specificFinishImportTimeSql = " and wtl.importCustomerTime <= '" + SDF_SEC.format(finishImportTime) +"'";
		}

		// 土豆获取人工号
		String workflow1CsrEmpNoSql = "";
		String inputWorkflow1CsrEmpNo = StringUtils.trimToEmpty((String) workflow1CsrEmpNo_tf.getValue());
		if(!"".equals(inputWorkflow1CsrEmpNo)) {
			logIdSql = " and wtl.workflow1CsrEmpNo = '" + inputWorkflow1CsrEmpNo+"'";
		}
		
		// 土豆获取人部门
		String workflow1CsrDeptNameSql = "";
		String inputWorkflow1CsrDeptName = StringUtils.trimToEmpty((String) workflow1CsrDeptName_tf.getValue());
		if(!"".equals(inputWorkflow1CsrDeptName)) {
			logIdSql = " and wtl.workflow1CsrDeptName like '%" + inputWorkflow1CsrDeptName+"%'";
		}
		
		// 转入土豆池的时间
		String migrateWorkflow1TimeSql = "";
		Date migrateWorkflow1Time = (Date) migrateToWorkflow1Time_pdf.getValue();
		if(migrateWorkflow1Time != null) {	// 统计这一天范围内的数据
			CALENDAR.setTime(migrateWorkflow1Time);
			CALENDAR.add(Calendar.DAY_OF_YEAR, +1);
			specificStartImportTimeSql = " and wtl.migrateWorkflow1Time >= '" + SDF_DAY.format(migrateWorkflow1Time) 
					+"' and wtl.migrateWorkflow1Time <= '" +SDF_DAY.format(CALENDAR.getTime())+ "'";
		} 
		
		// 土豆获取时间
		String workflow1PickTaskTimeSql = "";
		Date workflow1PickTaskTime = (Date) workflow1PickTaskTime_pdf.getValue();
		if(workflow1PickTaskTime != null) {	// 统计这一天范围内的数据
			CALENDAR.setTime(workflow1PickTaskTime);
			CALENDAR.add(Calendar.DAY_OF_YEAR, +1);
			specificStartImportTimeSql = " and wtl.workflow1PickTaskTime >= '" + SDF_DAY.format(workflow1PickTaskTime) 
					+"' and wtl.workflow1PickTaskTime <= '" +SDF_DAY.format(CALENDAR.getTime())+ "'";
		} 

		// 一销获取人工号
		String workflow2CsrEmpNoSql = "";
		String inputWorkflow2CsrEmpNo = StringUtils.trimToEmpty((String) workflow2CsrEmpNo_tf.getValue());
		if(!"".equals(inputWorkflow2CsrEmpNo)) {
			logIdSql = " and wtl.workflow2CsrEmpNo = '" + inputWorkflow2CsrEmpNo+"'";
		}
		
		// 一销获取人部门
		String workflow2CsrDeptNameSql = "";
		String inputWorkflow2CsrDeptName = StringUtils.trimToEmpty((String) workflow2CsrDeptName_tf.getValue());
		if(!"".equals(inputWorkflow2CsrDeptName)) {
			logIdSql = " and wtl.workflow2CsrDeptName like '%" + inputWorkflow2CsrDeptName+"%'";
		}
		
		// 转入一销池的时间
		String migrateWorkflow2TimeSql = "";
		Date migrateWorkflow2Time = (Date) migrateToWorkflow2Time_pdf.getValue();
		if(migrateWorkflow2Time != null) {	// 统计这一天范围内的数据
			CALENDAR.setTime(migrateWorkflow2Time);
			CALENDAR.add(Calendar.DAY_OF_YEAR, +1);
			specificStartImportTimeSql = " and wtl.migrateWorkflow2Time >= '" + SDF_DAY.format(migrateWorkflow2Time) 
					+"' and wtl.migrateWorkflow2Time <= '" +SDF_DAY.format(CALENDAR.getTime())+ "'";
		} 
		
		// 一销获取时间
		String workflow2PickTaskTimeSql = "";
		Date workflow2PickTaskTime = (Date) workflow2PickTaskTime_pdf.getValue();
		if(workflow2PickTaskTime != null) {	// 统计这一天范围内的数据
			CALENDAR.setTime(workflow2PickTaskTime);
			CALENDAR.add(Calendar.DAY_OF_YEAR, +1);
			specificStartImportTimeSql = " and wtl.workflow2PickTaskTime >= '" + SDF_DAY.format(workflow2PickTaskTime) 
					+"' and wtl.workflow2PickTaskTime <= '" +SDF_DAY.format(CALENDAR.getTime())+ "'";
		} 

		// 二销获取人工号
		String workflow3CsrEmpNoSql = "";
		String inputWorkflow3CsrEmpNo = StringUtils.trimToEmpty((String) workflow3CsrEmpNo_tf.getValue());
		if(!"".equals(inputWorkflow3CsrEmpNo)) {
			logIdSql = " and wtl.workflow3CsrEmpNo = '" + inputWorkflow3CsrEmpNo+"'";
		}
		
		// 二销获取人部门
		String workflow3CsrDeptNameSql = "";
		String inputWorkflow3CsrDeptName = StringUtils.trimToEmpty((String) workflow3CsrDeptName_tf.getValue());
		if(!"".equals(inputWorkflow3CsrDeptName)) {
			logIdSql = " and wtl.workflow3CsrDeptName like '%" + inputWorkflow3CsrDeptName+"%'";
		}
		
		// 转入二销池的时间
		String migrateWorkflow3TimeSql = "";
		Date migrateWorkflow3Time = (Date) migrateToWorkflow3Time_pdf.getValue();
		if(migrateWorkflow3Time != null) {	// 统计这一天范围内的数据
			CALENDAR.setTime(migrateWorkflow3Time);
			CALENDAR.add(Calendar.DAY_OF_YEAR, +1);
			specificStartImportTimeSql = " and wtl.migrateWorkflow3Time >= '" + SDF_DAY.format(migrateWorkflow3Time) 
					+"' and wtl.migrateWorkflow3Time <= '" +SDF_DAY.format(CALENDAR.getTime())+ "'";
		} 
		
		// 二销获取时间
		String workflow3PickTaskTimeSql = "";
		Date workflow3PickTaskTime = (Date) workflow3PickTaskTime_pdf.getValue();
		if(workflow3PickTaskTime != null) {	// 统计这一天范围内的数据
			CALENDAR.setTime(workflow3PickTaskTime);
			CALENDAR.add(Calendar.DAY_OF_YEAR, +1);
			specificStartImportTimeSql = " and wtl.workflow3PickTaskTime >= '" + SDF_DAY.format(workflow3PickTaskTime) 
					+"' and wtl.workflow3PickTaskTime <= '" +SDF_DAY.format(CALENDAR.getTime())+ "'";
		}
		
		// 客户编号
		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and wtl.customerResourceId = " + customerId;
		}
		
		// 客户姓名查询
		String customerNameSql = "";
		String inputCustomerName = StringUtils.trimToEmpty((String) customerName_tf.getValue());
		if(!"".equals(inputCustomerName)) {
			customerNameSql = " and wtl.customerName like '%" + inputCustomerName + "%'";
		}
		
		// 客户电话查询
		String customerPhoneSql = "";
		String phoneNo = StringUtils.trimToEmpty((String) customerPhone_tf.getValue());
		if(!"".equals(phoneNo)) {
			customerPhoneSql = " and wtl.phoneNo like '%" + phoneNo + "%'";
		}

		// 导入者工号查询
		String importorEmpNoSql = "";
		String importorEmpNo = StringUtils.trimToEmpty((String) importorEmpNo_tf.getValue());
		if(!"".equals(importorEmpNo)) {
			importorEmpNoSql = " and wtl.importorEmpNo = '" + importorEmpNo + "'";
		}
		
		// 导入者部门查询
		String importorDeptNameSql = "";
		String importorDeptName = StringUtils.trimToEmpty((String) importorDept_tf.getValue());
		if(!"".equals(importorDeptName)) {
			importorDeptNameSql = " and wtl.importorDeptName like '%" + importorDeptName + "%'";
		}
		
		// 创建固定的搜索语句
		return logIdSql + specificStartImportTimeSql + specificFinishImportTimeSql 
				+ workflow1CsrEmpNoSql + workflow1CsrDeptNameSql + migrateWorkflow1TimeSql + workflow1PickTaskTimeSql 
				+ workflow2CsrEmpNoSql + workflow2CsrDeptNameSql + migrateWorkflow2TimeSql + workflow2PickTaskTimeSql 
				+ workflow3CsrEmpNoSql + workflow3CsrDeptNameSql + migrateWorkflow3TimeSql + workflow3PickTaskTimeSql 
				+ customerIdSql + customerNameSql + customerPhoneSql + importorEmpNoSql + importorDeptNameSql;
	}

	/**
	 * 设置 WorkflowTransferLogLogView 的界面的翻页组件
	 * @param workflowTransferLogTableFlip
	 */
	public void setWorkflowTransferLogTableFlip(FlipOverTableComponent<WorkflowTransferLog> workflowTransferLogTableFlip) {
		this.workflowTransferLogTableFlip = workflowTransferLogTableFlip;
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
