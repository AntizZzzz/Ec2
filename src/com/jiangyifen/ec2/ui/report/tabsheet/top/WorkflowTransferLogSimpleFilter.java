package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
 * 工作流迁移日志简单查询组件
 * 
 * @author jrh
 * 
 * 2013-7-30
 */
@SuppressWarnings("serial")
public class WorkflowTransferLogSimpleFilter extends VerticalLayout implements ClickListener {

	private final SimpleDateFormat SDF_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private GridLayout gridLayout;			// 面板中的布局管理器

	//--------------  第一行  --------------// 
	private TextField logId_tf;
	private ComboBox importTimeScope_cb;				// “原始数据导入时间”选择框
	private PopupDateField startImportTime_pdf;			// “开始导入时间”选择框
	private PopupDateField finishImportTime_pdf;		// “截止导入时间”选择框

	//--------------  第二行  --------------// 
	private TextField customerId_tf;					// 客户编号输入文本框
	private TextField customerName_tf;					// 客户姓名输入文本框
	private TextField customerPhone_tf;					// 客户手机输入文本框
	private TextField importorEmpNo_tf;					// 导入者工号输入文本框

	private Button search_bt;							// 刷新结果按钮
	private Button clear_bt;							// 清空输入内容
	private PopupView complexSearchView;				// 复杂搜索界面
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private User loginUser; 							// 当前的登陆用户
	private Domain domain;								// 当前用户所属域
	private String deptSearchSql;						// 初始化部门搜索语句

	private WorkflowTransferLogComplexFilter workflowTransferLogComplexFilter;
	private FlipOverTableComponent<WorkflowTransferLog> workflowTransferLogTableFlip;		// 工作流迁移日志管理Tab 页的翻页组件
	
	private DepartmentService departmentService;	// 部门服务类
	
	public WorkflowTransferLogSimpleFilter() {
		this.setSpacing(true);

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		departmentService = SpringContextHolder.getBean("departmentService");
		
		gridLayout = new GridLayout(5, 2);
		gridLayout.setCaption("搜索条件");
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
		this.createCustomerIdHLayout();
		this.createCustomerNameHLayout();
		this.createCustomerPhoneHLayout();
		this.createOperatorEmpNoHLayout();
		
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
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		gridLayout.addComponent(layout, 4, 0);
		
		search_bt = new Button("查 询");
		search_bt.setDescription("快捷键(Ctrl+Q)");
		search_bt.addStyleName("default");
		search_bt.addListener(this);
		search_bt.setWidth("60px");
		search_bt.setClickShortcut(KeyCode.Q, ModifierKey.CTRL);
		layout.addComponent(search_bt);
		
		clear_bt = new Button("清 空");
		clear_bt.setDescription("快捷键(Ctrl+L)");
		clear_bt.addListener(this);
		clear_bt.setWidth("60px");
		clear_bt.setClickShortcut(KeyCode.L, ModifierKey.CTRL);
		layout.addComponent(clear_bt);
		
		workflowTransferLogComplexFilter = new WorkflowTransferLogComplexFilter();
		workflowTransferLogComplexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(workflowTransferLogComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 4, 1);
		gridLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
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
		logId_tf.setWidth("100px");	
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
		importTimeScope_cb.setWidth("100px");
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
	 * 创建 存放“客户编号” 的布局管理器
	 */
	private void createCustomerIdHLayout() {
		HorizontalLayout customerIdHLayout = new HorizontalLayout();
		customerIdHLayout.setSpacing(true);
		gridLayout.addComponent(customerIdHLayout, 0, 1);
		
		Label customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		customerIdHLayout.addComponent(customerIdLabel);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户编号 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("100px");
		customerIdHLayout.addComponent(customerId_tf);
	}
	
	/**
	 * 创建 存放“客户姓名” 的布局管理器
	 */
	private void createCustomerNameHLayout() {
		HorizontalLayout customerNameHLayout = new HorizontalLayout();
		customerNameHLayout.setSpacing(true);
		gridLayout.addComponent(customerNameHLayout, 1, 1);
		
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		customerNameHLayout.addComponent(customerNameLabel);
		
		customerName_tf = new TextField();
		customerName_tf.setWidth("126px");	
		customerNameHLayout.addComponent(customerName_tf);
	}

	/**
	 * 创建 存放“客户电话” 的布局管理器
	 */
	private void createCustomerPhoneHLayout() {
		HorizontalLayout telephoneHLayout = new HorizontalLayout();
		telephoneHLayout.setSpacing(true);
		gridLayout.addComponent(telephoneHLayout, 2, 1);

		// 客户电话输入区
		Label telephoneLabel = new Label("客户电话：");
		telephoneLabel.setWidth("-1px");
		telephoneHLayout.addComponent(telephoneLabel);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("154px");
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
		gridLayout.addComponent(operatorEmpNoHLayout, 3, 1);
		
		// 导入者工号输入区
		Label operatorEmpNoLabel = new Label("导入者工号：");
		operatorEmpNoLabel.setWidth("-1px");
		operatorEmpNoHLayout.addComponent(operatorEmpNoLabel);
		
		importorEmpNo_tf = new TextField();
		importorEmpNo_tf.setWidth("140px");	
		operatorEmpNoHLayout.addComponent(importorEmpNo_tf);
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

		customerId_tf.setValue("");
		customerName_tf.setValue("");
		customerPhone_tf.setValue("");
		importorEmpNo_tf.setValue("");
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
		
		// 创建固定的搜索语句
		return logIdSql + specificStartImportTimeSql + specificFinishImportTimeSql 
				+ customerIdSql + customerNameSql + customerPhoneSql + importorEmpNoSql;
	}

	public void setWorkflowTransferLogTableFlip(FlipOverTableComponent<WorkflowTransferLog> workflowTransferLogTableFlip) {
		this.workflowTransferLogTableFlip = workflowTransferLogTableFlip;
		workflowTransferLogComplexFilter.setWorkflowTransferLogTableFlip(workflowTransferLogTableFlip);
	}

}
