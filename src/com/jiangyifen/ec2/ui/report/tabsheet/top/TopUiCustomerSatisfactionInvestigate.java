package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.PushNumStatus;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.pojo.SatisfactionInvestigation;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.CustomerSatisfactionInvestigate;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
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
 * REPORTPAGE满意度调查
 * @author lxy
 *
 */
@SuppressWarnings("serial")
public class TopUiCustomerSatisfactionInvestigate extends VerticalLayout implements ValueChangeListener {

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;
	private ComboBox cmbDept;		// 需要查询的部门
	private ComboBox reportBox;

	private Button seeButton;// 查询
	private Button exportReport;// 导出报表
	private ComboBox empNoComboBox;
	
	private CustomerSatisfactionInvestigate customerSatisfactionInvestigate;
	private TabSheet tabSheet;
	private Tab tab;
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private String[] dateType = new String[] { "今天", "昨天", "本周", "上周", "本月", "上月", "精确时间"};
	private String[] callDirection = new String[] { "呼出", "呼入" };

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private File file;

	private ReportTableUtil tableUtil;
	private Map<String, SatisfactionInvestigation> map;

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	private ReportService reportService ;
	private CommonService commonService;
	private UserService userService;
	private User loginUser;
	private Domain domain;
	
	//部分管理
	private DepartmentService departmentService;
	// 登陆用户的编号
	private String domainId;
	private Set<Long> deptidSet = new HashSet<Long>();	//用户管辖的部门编号
	
	// 存放所管理的所有部门的信息
	private List<Department> deptList = new ArrayList<Department>();
	private BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(Department.class);
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
	
	private String sql;

	public TopUiCustomerSatisfactionInvestigate(CustomerSatisfactionInvestigate customerSatisfactionInvestigate) {
		this.customerSatisfactionInvestigate = customerSatisfactionInvestigate;
		initSpringContext();
		initDepIds();

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
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

		reportBox = new ComboBox();
		for (int i = 0; i < callDirection.length; i++) {
			reportBox.addItem(callDirection[i]);
		}
		reportBox.setWidth("100px");
		reportBox.setImmediate(true);
		reportBox.setNullSelectionAllowed(false);
		reportBox.setValue(callDirection[0]);// 默认是呼出
		horizontalLayout.addComponent(reportBox);

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);
		exportReport = new Button("导出报表", this, "exportReport");
		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_SATISFACTION_INVESTIGATE)) {
			horizontalLayout.addComponent(exportReport);
		}
		
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
		
		horizontalLayout.addComponent(dateComboBox);
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
		deptidSet = new HashSet<Long>();
		Set<Role> roles = loginUser.getRoles();
		for (Role role : roles) {
			if(role.getType().equals(RoleType.manager)) {
				for(Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					deptidSet.add(dept.getId());
				}
			}
		}
	}
	//初始化spring相关业务对象
	private void initSpringContext() {
		loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");
		domain = loginUser.getDomain();
		userService = SpringContextHolder.getBean("userService");
		commonService = SpringContextHolder.getBean("commonService");
		reportService = SpringContextHolder.getBean("reportService");
		departmentService = SpringContextHolder.getBean("departmentService");
		Set<Role> roles = loginUser.getRoles(); // 获取登陆者所有的角色
		for (Role role : roles) {
			if (role.getType().equals(RoleType.manager)) {
				for (Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					deptidSet.add(dept.getId());
				}
			}
		}
	}
	public void seeResult(ClickEvent event) {
		tabSheet = customerSatisfactionInvestigate.getTabSheet();
		domainId = loginUser.getDomain().getId().toString();
		
		Department dept = (Department) cmbDept.getValue();
		if(dept == null) {
			event.getComponent().getWindow().showNotification("请选择要查询的部门");
			return;
		}
		
		String directionValue = reportBox.getValue().toString();
		startValue = format.format(startDate.getValue());// 获得startDate的值
		endValue = format.format(endDate.getValue());// 获得endDate的值
		if ("呼出".equals(directionValue)) {//呼叫方式-呼出
			initEntityHashMap(dept);	
			getTotalGrade(startValue, endValue, "outgoing");
			getAvgGrade_and_countMarked(startValue, endValue, "outgoing");
			countUnmarked(startValue, endValue, "1");
			countNum0Pushed(startValue, endValue, "outgoing");
			countNum1Pushed(startValue, endValue, "outgoing");
			countNum2Pushed(startValue, endValue, "outgoing");
			countNum3Pushed(startValue, endValue, "outgoing");
			countNum4Pushed(startValue, endValue, "outgoing");
			countNum5Pushed(startValue, endValue, "outgoing");
			countNum6Pushed(startValue, endValue, "outgoing");
			countNum7Pushed(startValue, endValue, "outgoing");
			countNum8Pushed(startValue, endValue, "outgoing");
			countNum9Pushed(startValue, endValue, "outgoing");

		} else if ("呼入".equals(directionValue)) {//呼叫方式-呼入
			initEntityHashMap(dept);
			getTotalGrade(startValue, endValue, "incoming");
			getAvgGrade_and_countMarked(startValue, endValue, "incoming");
			countUnmarked(startValue, endValue, "2");
			countNum0Pushed(startValue, endValue, "incoming");
			countNum1Pushed(startValue, endValue, "incoming");
			countNum2Pushed(startValue, endValue, "incoming");
			countNum3Pushed(startValue, endValue, "incoming");
			countNum4Pushed(startValue, endValue, "incoming");
			countNum5Pushed(startValue, endValue, "incoming");
			countNum6Pushed(startValue, endValue, "incoming");
			countNum7Pushed(startValue, endValue, "incoming");
			countNum8Pushed(startValue, endValue, "incoming");
			countNum9Pushed(startValue, endValue, "incoming");
		}
		Set<Entry<String, SatisfactionInvestigation>> set = map.entrySet();
		Iterator<Entry<String, SatisfactionInvestigation>> iterator = set.iterator();
		list = new ArrayList<Object[]>();
		while (iterator.hasNext()) {
			Entry<String, SatisfactionInvestigation> entry = iterator.next();
			SatisfactionInvestigation investigation = entry.getValue();
			Object[] objects = new Object[16];
			objects[0] = investigation.getDept();
			objects[1] = investigation.getEmpno();
			objects[2] = investigation.getName();
			objects[3] = investigation.getTotalGrade();
			objects[4] = investigation.getAvgGrade();
			objects[5] = investigation.getCountMarked();
			objects[6] = investigation.getCountNum0Pushed();
			objects[7] = investigation.getCountNum1Pushed();
			objects[8] = investigation.getCountNum2Pushed();
			objects[9] = investigation.getCountNum3Pushed();
			objects[10] = investigation.getCountNum4Pushed();
			objects[11] = investigation.getCountNum5Pushed();
			objects[12] = investigation.getCountNum6Pushed();
			objects[13] = investigation.getCountNum7Pushed();
			objects[14] = investigation.getCountNum8Pushed();
			objects[15] = investigation.getCountNum9Pushed();
			list.add(objects);
		}
		// 设置table列名
		columnNames = new ArrayList<String>();
		columnNames.add("部门");
		columnNames.add("工号");
		columnNames.add("姓名");
		columnNames.add("总成绩");
		columnNames.add("平均成绩");
		columnNames.add("评分次数");//columnNames.add("未评分次数");

		List<PushNumStatus> numStatus = commonService.getEntitiesByJpql("select p from PushNumStatus as p order by p.number");
		for (PushNumStatus pushNumStatus : numStatus) {
			columnNames.add("按" + pushNumStatus.getNumber() + "(" + pushNumStatus.getStatusName() + ")" + "数量");
		}
		tableUtil = new ReportTableUtil(list, "", columnNames, startValue, endValue);
		tab = tabSheet.addTab(tableUtil, "统计结果", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);

	}

	/**
	 * 初始化实体Map
	 */
	public void initEntityHashMap(Department dept) {
		map = new HashMap<String, SatisfactionInvestigation>();//查询Map结合
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,d.name,u.realname from ec2_user as u,ec2_department as d where u.department_id = d.id and u.domain_id = ");
		sbQuery.append(domainId);
		sbQuery.append(getSqlByDept(dept, "d.id"));
		if (null != empNoComboBox.getValue()) {
			User selectUser = (User) empNoComboBox.getValue();
			if (StringUtils.isNotEmpty(selectUser.getEmpNo()) && !"全部-员工-部门".equals(selectUser.getEmpNo())) {
				sbQuery.append(" and u.empno = '");
				sbQuery.append(selectUser.getEmpNo());
				sbQuery.append("' ");
			}
		}
		sql =  sbQuery.toString();
		List<Object[]> list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			SatisfactionInvestigation investigation = new SatisfactionInvestigation();
			if (objects[0] != null) {// 工号
				investigation.setEmpno(objects[0].toString());
			}
			if (objects[1] != null) {// 部门
				investigation.setDept(objects[1].toString());
			}
			if (objects[2] != null) {// 姓名
				investigation.setName(objects[2].toString());
			}
			map.put(objects[0].toString().trim(), investigation);// key=empno value=entity
		}
	}

	/**
	 * @param startValue
	 *            开始时间
	 * @param endValue
	 *            结束时间
	 * @param direction
	 *            呼叫方向
	 */
	public void getTotalGrade(String startValue, String endValue,String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,sum(cast(s.grade as bigint)) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setTotalGrade(Integer.parseInt(objects[1].toString()));// 设置总成绩
			}
		}
	}

	// 平均成绩和评分次数
	public void getAvgGrade_and_countMarked(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				// 设置平均成绩
				investigation.setAvgGrade(investigation.getTotalGrade() / Integer.parseInt(objects[1].toString()));
				// 评分次数
				investigation.setCountMarked(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	/**
	 * 未评分次数
	 * @param startValue
	 * @param endValue
	 * @param direction
	 */
	public void countUnmarked(String startValue, String endValue,String direction) {
		StringBuffer sbQuery = new StringBuffer();
		if ("1".equals(direction)) {// 呼出
			sbQuery.append("select srcempno,count(*) from cdr where starttimedate >= '");
			sbQuery.append(startValue);
			sbQuery.append("' and starttimedate <= '");
			sbQuery.append(endValue);
			sbQuery.append("' and cdrdirection = '");
			sbQuery.append(direction);
			sbQuery.append("' and domainid = '");
			sbQuery.append(domainId);
			sbQuery.append("' group by srcempno");
			sql = sbQuery.toString();
			list = reportService.getMoreRecord(sql);
			
			for (Object[] objects : list) {
				if (objects[0] != null && map.containsKey(objects[0].toString())) {
					SatisfactionInvestigation investigation = map.get(objects[0].toString());
					// 未评分次数
					investigation.setCountUnmarked(Integer.parseInt(objects[1].toString()) - investigation.getCountMarked());
				}
			}
		} else if ("2".equals(direction)) {
			sbQuery.setLength(0);
			sbQuery.append("select destempno,count(*) from cdr where starttimedate >= '");
			sbQuery.append(startValue);
			sbQuery.append("' and starttimedate <= '");
			sbQuery.append(endValue);
			sbQuery.append("' and cdrdirection = '");
			sbQuery.append(direction);
			sbQuery.append("' and domainid = '");
			sbQuery.append(domainId);
			sbQuery.append("' group by destempno");;
			sql =sbQuery.toString();
			list = reportService.getMoreRecord(sql);
			
			for (Object[] objects : list) {
				if (map.containsKey(objects[0].toString())) {
					SatisfactionInvestigation investigation = map.get(objects[0].toString());
					// 未评分次数
					investigation.setCountUnmarked(Integer.parseInt(objects[1].toString()) - investigation.getCountMarked());
				}
			}
		}
		sbQuery = null;
	}

	// 按0数量
	public void countNum0Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '0' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql =  sbQuery.toString(); 
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum0Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按1数量
	public void countNum1Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '1'");
		sbQuery.append(" and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum1Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按2数量
	public void countNum2Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '2' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum2Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按3数量
	public void countNum3Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '3' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum3Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按4数量
	public void countNum4Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '4' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum4Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按5数量
	public void countNum5Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '5' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum5Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按6数量
	public void countNum6Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '6' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum6Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按7数量
	public void countNum7Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '7' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum7Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按8数量
	public void countNum8Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '8' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql =sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum8Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 按9数量
	public void countNum9Pushed(String startValue, String endValue, String direction) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,count(*) from ec2_customer_satisfaction_investigation_log as s,ec2_user as u where u.id = s.csrid and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and direction = '");
		sbQuery.append(direction);
		sbQuery.append("' and grade = '9' and date >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and date <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by u.empno");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		sbQuery = null;
		
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString())) {
				SatisfactionInvestigation investigation = map.get(objects[0].toString());
				investigation.setCountNum9Pushed(Integer.parseInt(objects[1].toString()));
			}
		}
	}

	// 导出报表事件
	public void exportReport(ClickEvent event) throws FileNotFoundException {
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
		file = ExportExcelUtil.exportExcel("满意度调查", list, columnNames, startValue, endValue);
		saveOperationLog();// 记录导出日志
		fileDownload(event);// 文件下载

	}
	
	// 记录导出日志
	private void saveOperationLog() {
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null)
			operationLog.setFilePath(file.getAbsolutePath());
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(loginUser.getUsername());
		operationLog.setRealName(loginUser.getRealName());
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("满意度调查");
		operationLog.setProgrammerSee(sql);
		CommonService commonService = SpringContextHolder.getBean("commonService");
		commonService.save(operationLog);
	}

	// 文件下载
	private void fileDownload(ClickEvent event) {
		Resource resource = new FileResource(file, event.getComponent().getApplication());
		final Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setSource(resource);
		this.addComponent(downloader);
		downloader.addListener(new RepaintRequestListener() {
			private static final long serialVersionUID = -9200720669070381303L;
			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
			}
		});
	}

	public String getStartValue() {
		return startValue;
	}

	public String getEndValue() {
		return endValue;
	}

	/**
	 * 值发生修改时，触发的事件 
	 * @param event
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		if(cmbDept == event.getProperty()) {
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
