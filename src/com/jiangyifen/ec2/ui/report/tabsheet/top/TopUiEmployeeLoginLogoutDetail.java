package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.EmployeeLoginLogoutDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * REPORTPAGE员工上下线详单
 * @author lxy
 *
 */
@SuppressWarnings("serial")
public class TopUiEmployeeLoginLogoutDetail extends VerticalLayout implements ValueChangeListener  {

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;
	private ComboBox cmbDept;				// 需要查询的部门
	private Button seeButton;
	private Button exportReport;			// 导出报表

	private EmployeeLoginLogoutDetail detail;
	private TabSheet tabSheet;
	private Tab tab;
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private String[] dateType = new String[] { "今天", "昨天", "本周", "上周", "本月", "上月", "精确时间"};

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private File file;
	private ReportTableUtil tableUtil;
 	private ComboBox empNoComboBox;
	
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private ReportService reportService = SpringContextHolder.getBean("reportService");
	// 登陆的user
	private User loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");
	//部分管理
	private DepartmentService departmentService = SpringContextHolder.getBean("departmentService");
	
	private UserService userService = SpringContextHolder.getBean("userService");
	
	private Set<Long> deptidSet = new HashSet<Long>();	//用户管辖的部门编号
	private String domainId;
	private Domain domain;
	private StringBuffer sqlBuffer = new StringBuffer();
	
	// 存放所管理的所有部门的信息
	private List<Department> deptList = new ArrayList<Department>();
	private BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(Department.class);
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();

	public TopUiEmployeeLoginLogoutDetail(EmployeeLoginLogoutDetail detail) {
		this.detail = detail;
		initDepIds();
		domain = loginUser.getDomain();
		domainId = loginUser.getDomain().getId().toString();
		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true,true,false,true);
		this.addComponent(horizontalLayout);

		// 创建存放 时间范围 的布局管理器
		createImportTimeScopeHLayout();
		// 创建 开始时间 的布局管理器
		createStartImportTimeHLayout();

		toLabel = new Label("至");
		horizontalLayout.addComponent(toLabel);

		// 创建 截止时间 的布局管理器
		createFinishImportTimeHLayout();
		
		// 部门选择 -- 这里应该考虑选择多个部门进行查询
		cmbDept = achieveBasicUtil.getCmbDeptReport(cmbDept, loginUser, deptList, deptContainer);
		cmbDept.addListener((ValueChangeListener) this);
		horizontalLayout.addComponent(cmbDept);

		Label lb_1 = new Label("工号：");
		horizontalLayout.addComponent(lb_1);
		
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User showAllCsr = new User();
		showAllCsr.setEmpNo("全部-员工-部门");
		showAllCsr.setUsername("全部-员工-部门");
		userContainer.addBean(showAllCsr);
		List<Long> strList = new ArrayList<Long>();
		strList.addAll(deptidSet);
		List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
		userContainer.addAll(csrs);
		empNoComboBox = new ComboBox();
		empNoComboBox.setContainerDataSource(userContainer);
		empNoComboBox.setValue(showAllCsr);
		empNoComboBox.setWidth("158px");
		empNoComboBox.setNullSelectionAllowed(false);
		empNoComboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		empNoComboBox.setItemCaptionPropertyId("migrateCsr1");
		horizontalLayout.addComponent(empNoComboBox);
		/*for(User csr : userContainer.getItemIds()) {
			String caption = csr.getEmpNo();
			if(csr.getRealName() != null) {
				caption = caption +" - "+ csr.getRealName();
			}
			empNoComboBox.setItemCaption(csr, caption);
		}*/
		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);
		exportReport = new Button("导出报表", this, "exportReport");
		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_STAFF_TIME_SHEET)) {
			horizontalLayout.addComponent(exportReport);
		}

	}

	/**
	 * 创建存放 时间范围 的布局管理器
	 */
	private void createImportTimeScopeHLayout() {
		dateComboBox = new ComboBox();
		for (int i = 0; i < dateType.length; i++) {
			dateComboBox.addItem(dateType[i]);
		}
		dateComboBox.setWidth("80px");
		dateComboBox.setImmediate(true);
		dateComboBox.setNullSelectionAllowed(false);
		dateComboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		dateComboBox.setValue(dateType[0]);// 默认是本日
		horizontalLayout.addComponent(dateComboBox);
		
		timeScopeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String scopeValue = (String) dateComboBox.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startDate.removeListener(startTimeListener);
				endDate.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startDate.setValue(dates[0]);
				endDate.setValue(dates[1]);
				startDate.addListener(startTimeListener);
				endDate.addListener(finishTimeListener);
			}
		};
		dateComboBox.addListener(timeScopeListener);
	}

	/**
	 * 创建 开始时间 的布局管理器
	 */
	private void createStartImportTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		startDate = new PopupDateField();
		startDate.setWidth("100px");
		startDate.setDateFormat("yyyy-MM-dd");
		startDate.setResolution(PopupDateField.RESOLUTION_DAY);
		startDate.setImmediate(true);
		startDate.setValue(dates[0]);
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dateComboBox.removeListener(timeScopeListener);
				dateComboBox.setValue("精确时间");
				dateComboBox.addListener(timeScopeListener);
			}
		};
		startDate.addListener(startTimeListener);
		startDate.setParseErrorMessage("时间格式不合法");
		startDate.setValidationVisible(false);
		horizontalLayout.addComponent(startDate);
	}

	/**
	 * 创建 截止时间 的布局管理器
	 */
	private void createFinishImportTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		endDate = new PopupDateField();
		endDate.setWidth("100px");
		endDate.setDateFormat("yyyy-MM-dd");
		endDate.setResolution(PopupDateField.RESOLUTION_DAY);
		endDate.setImmediate(true);
		endDate.setValue(dates[1]);
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dateComboBox.removeListener(timeScopeListener);
				dateComboBox.setValue("精确时间");
				dateComboBox.addListener(timeScopeListener);
			}
		};
		endDate.addListener(finishTimeListener);
		horizontalLayout.addComponent(endDate);
	}
	
	private void initDepIds(){
		Set<Role> roles = loginUser.getRoles();
		for (Role role : roles) {
			if(role.getType().equals(RoleType.manager)) {
				for(Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					deptidSet.add(dept.getId());
				}
			}
		}
	}
	
	public void seeResult(ClickEvent event) {
		
		tabSheet = detail.getTabSheet();
		// 登陆用户的domainId
		Department dept = (Department) cmbDept.getValue();
		if(dept == null) {
			event.getComponent().getWindow().showNotification("请选择要查询的部门");
			return;
		}
		
		startValue = format.format(startDate.getValue());// 获得startDate的值
		endValue = format.format(endDate.getValue());// 获得endDate的值
		// 获得数据
		list = getRecordByDay(startValue, endValue, dept);

		// 设置table列名
		columnNames = new ArrayList<String>();
		columnNames.add("部门");
		columnNames.add("工号");
		columnNames.add("姓名");
		columnNames.add("分机号");
		columnNames.add("登录IP");
		columnNames.add("登陆时间");
		columnNames.add("登出时间");

		tableUtil = new ReportTableUtil(list, "",columnNames, startValue, endValue);
		tab = tabSheet.addTab(tableUtil, "员工上下线详单", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);
	}

	// 导出报表事件
	public void exportReport(ClickEvent event) {
		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询,再导出!");
			return;
		}
		// 得到当前tab的组件，把当前tab页的数据导出报表
		ReportTableUtil reportTableUtil = (ReportTableUtil) tabSheet.getSelectedTab();
		// 如果reportTableUtil为null提示请点击查询
		if (reportTableUtil == null) {
			NotificationUtil.showWarningNotification(this, "请先查询,再导出!");
			return;
		}
		// 设置数据源
		list = reportTableUtil.getList();
		// 设置列名
		columnNames = reportTableUtil.getNames();
		// 开时时间
		startValue = reportTableUtil.getStartValue();
		// 结束时间
		endValue = reportTableUtil.getEndValue();
		// 生成excel file
		file = ExportExcelUtil.exportExcel("员工上下线详单", list, columnNames,startValue, endValue);

		// ======================报表导出日志======================//
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null)
			operationLog.setFilePath(file.getAbsolutePath());
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(SpringContextHolder.getLoginUser().getUsername());
		operationLog.setRealName(SpringContextHolder.getLoginUser()	.getRealName());
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("导出员工上下线详单报表");
		operationLog.setProgrammerSee(sqlBuffer.toString());
		CommonService commonService = SpringContextHolder.getBean("commonService");
		commonService.save(operationLog);
		// ======================报表导出日志======================//

		// 下载 报表文件
		final Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		Resource resource = new FileResource(file, event.getComponent().getApplication());
		downloader.setSource(resource);
		this.addComponent(downloader);
		downloader.addListener(new RepaintRequestListener() {
			private static final long serialVersionUID = -5870507759227061769L;
			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
			}
		});
		// 下载 报表文件
	}

	//获取查询结果
	public List<Object[]> getRecordByDay(String startValue, String endValue, Department dept) {
		sqlBuffer.setLength(0);
		sqlBuffer.append(" select deptname,username,realname,exten,ip,logindate,logoutdate from ec2_user_login_record where 1=1 and domainid = ");
		sqlBuffer.append(domainId);
		sqlBuffer.append(" ");
		sqlBuffer.append(getSqlByDept(dept, "deptid"));
		/*if(StringUtils.isNotEmpty(deptIds)){
			sqlBuffer.append(" and deptid in(" + deptIds + ") ");
		}else{
			sqlBuffer.append(" and 1 > 2");
		}*/
		if(null != empNoComboBox.getValue()){
			User selectUser =  (User) empNoComboBox.getValue();
			if(StringUtils.isNotEmpty(selectUser.getUsername()) && !"全部-员工-部门".equals(selectUser.getUsername())){
				sqlBuffer.append( " and username = '"+selectUser.getUsername()+"' ");
			}
		}
		sqlBuffer.append(" and logindate >= '");
		sqlBuffer.append(startValue);
		sqlBuffer.append("' and logindate <= '");
		sqlBuffer.append(endValue);
		sqlBuffer.append("' group by deptname,username,realname,exten,ip,logindate,logoutdate ");
		sqlBuffer.append(" order by deptname  desc,username  desc,realname ,exten ,logindate  desc ,logoutdate desc  ");
		return reportService.getMoreRecord(sqlBuffer.toString());
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(cmbDept == source) {
			Department dept = (Department) cmbDept.getValue();
			if(dept != null) {
				empNoComboBox.removeAllItems();
				BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
				User showAllCsr = new User();
				showAllCsr.setEmpNo("全部-员工-部门");
				showAllCsr.setUsername("全部-员工-部门");
				userContainer.addBean(showAllCsr);
				List<Long> strList = new ArrayList<Long>();
				if(dept.getId().equals(0L)) {
					strList.addAll(deptidSet);
					List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
					userContainer.addAll(csrs);
					empNoComboBox.setContainerDataSource(userContainer);
				} else {
					strList.add(dept.getId());
					List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
					userContainer.addAll(csrs);
					empNoComboBox.setContainerDataSource(userContainer);
				}
				empNoComboBox.setValue(showAllCsr);
			}
		}
	}
	
	/**
	 * jinht
	 * 根据部门信息进行判断查询的SQL语句
	 * @param dept      部门
	 * @param str       要查询的字段
	 * @return String   sql 语句
	 */
	private String getSqlByDept(Department dept, String str) {
		StringBuffer sbSql = new StringBuffer();
		if(dept != null) {
			if(dept.getId().equals(0L)) {
				sbSql.append(" and ");
				sbSql.append(str);
				sbSql.append(" in (");
				sbSql.append(achieveBasicUtil.getDeptIds());
				sbSql.append(") ");
			} else {
				sbSql.append(" and ");
				sbSql.append(str);
				sbSql.append("=");
				sbSql.append(dept.getId());
			}
		} else {
			sbSql.append(" and 1>2");
		}
		sbSql.append(" ");
		return sbSql.toString();
	}
}
