package com.jiangyifen.ec2.ui.report.tabsheet;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 置忙详情统计报表
 *  目的: 为了统计出每一个置忙状态的数量
 * @author jht
 *
 */
@SuppressWarnings("serial")
public class PauseDetailStatisticsReport extends VerticalLayout implements ClickListener, ValueChangeListener {

	// 日志工具类
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 界面布局
	private static int gridLayoutColumns = 4;					// Grid 布局的行数
	private static int gridLayoutRows = 3;					// Grid 布局的列数
	private GridLayout gridLayout;								// Grid 布局管理器
	private ComboBox cmbTimeScope;								// 时间范围
	private PopupDateField pdfStartTime;						// 开始时间
	private PopupDateField pdfFinishTime;						// 截止时间
	private ComboBox cmbDept;									// 部门选择
	private ComboBox cmbUser;									// 用户选择
	private Button btnSearch;									// 搜索按钮
	private Button btnClear;									// 清空按钮
	private Button btnExportExcel;								// 导出 Excel 按钮
	private Embedded downloader;								// 存放数据的组件
	
	private Table table;										// 表格组件
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
	
	// 集合类
	private List<String> pauseReasonsList;								// 置忙状态类型集合
	private List<Long> deptIdsList = new ArrayList<Long>();			// 该管理员管辖的部门编号列表
	private List<Department> deptsList = new ArrayList<Department>();
	private BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(Department.class);
	private List<String> headersList = new ArrayList<String>();		// 表格头部显示的列的名称
	private List<Object[]> allTableList = new ArrayList<Object[]>();	// 获取表格里的所有数据
	
	// 业务注入对象
	private UserService userService;
	private CommonService commonService;
	private DepartmentService departmentService;
	
	// 其他
	private User loginUser;									// 当前登录的用户
	private Domain domain;										// 当前用户所属的域
	private String exportPath;									// 导出路径
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
	
	/** 构造方法 */
	public PauseDetailStatisticsReport() {
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		initSpringContext();
		
		/******************************************** UI 构建 ****************************************************/
		gridLayout = new GridLayout(gridLayoutColumns, gridLayoutRows);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(false);
		this.addComponent(gridLayout);
		
		createFirstHLayout();
		createSecondHLayout();
		
		createTableAndButtonsLayout();
	}
	
	/** 初始化 Spring 相关的业务对象 */
	private void initSpringContext() {
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		
		userService = SpringContextHolder.getBean("userService");
		commonService = SpringContextHolder.getBean("commonService");
		departmentService = SpringContextHolder.getBean("departmentService");
		
		Set<Role> roles = loginUser.getRoles();					// 获取当前登录用户的所有角色
		for(Role role : roles) {
			if(role.getType().equals(RoleType.manager)) {
				for(Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					deptIdsList.add(dept.getId());
				}
			}
		}
		
		exportPath = ConfigProperty.PATH_EXPORT;
		
		pauseReasonsList = commonService.getEntitiesByJpql("select s.reason from PauseReason as s where s.enabled = true and s.domain.id = " + domain.getId());
	}

	/** 创建 GridLayout 布局第一行组件 */
	private void createFirstHLayout() {
		// 时间范围
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 0, 0);
		
		Label lblTimeScope = new Label("时间范围：");
		lblTimeScope.setWidth("-1px");
		timeScopeHLayout.addComponent(lblTimeScope);
		
		cmbTimeScope = new ComboBox();
		cmbTimeScope.setImmediate(true);
		cmbTimeScope.addItem("今天");
		cmbTimeScope.addItem("昨天");
		cmbTimeScope.addItem("本周");
		cmbTimeScope.addItem("上周");
		cmbTimeScope.addItem("本月");
		cmbTimeScope.addItem("上月");
		cmbTimeScope.addItem("精确时间");
		cmbTimeScope.setValue("今天");
		cmbTimeScope.setWidth("160px");
		cmbTimeScope.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cmbTimeScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(cmbTimeScope);
		timeScopeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String scopeValue = (String) cmbTimeScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				pdfStartTime.removeListener(startTimeListener);
				pdfFinishTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				pdfStartTime.setValue(dates[0]);
				pdfFinishTime.setValue(dates[1]);
				pdfStartTime.addListener(startTimeListener);
				pdfFinishTime.addListener(finishTimeListener);
			}
		};
		cmbTimeScope.addListener(timeScopeListener);
		
		// 开始时间
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 1, 0);
		
		Label lblStartTime = new Label("开始时间：");
		lblStartTime.setWidth("-1px");
		startTimeHLayout.addComponent(lblStartTime);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				cmbTimeScope.removeListener(timeScopeListener);
				cmbTimeScope.setValue("精确时间");
				cmbTimeScope.addListener(timeScopeListener);
			}
		};
		
		pdfStartTime = new PopupDateField();
		pdfStartTime.setWidth("160px");
		pdfStartTime.setImmediate(true);
		pdfStartTime.setValue(dates[0]);
		pdfStartTime.addListener(startTimeListener);
		pdfStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pdfStartTime.setParseErrorMessage("时间格式不合法");
		pdfStartTime.setValidationVisible(false);
		pdfStartTime.setResolution(PopupDateField.RESOLUTION_HOUR);
		startTimeHLayout.addComponent(pdfStartTime);
		
		// 截止时间
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 2, 0);
		
		Label lblFinishTime = new Label("截止时间：");
		lblFinishTime.setWidth("-1px");
		finishTimeHLayout.addComponent(lblFinishTime);
		
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				cmbTimeScope.removeListener(timeScopeListener);
				cmbTimeScope.setValue("精确时间");
				cmbTimeScope.addListener(timeScopeListener);
			}
		};
		
		pdfFinishTime = new PopupDateField();
		pdfFinishTime.setImmediate(true);
		pdfFinishTime.setWidth("160px");
		pdfFinishTime.setValue(dates[1]);
		pdfFinishTime.addListener(finishTimeListener);
		pdfFinishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pdfFinishTime.setParseErrorMessage("时间格式不合法");
		pdfFinishTime.setValidationVisible(false);
		pdfFinishTime.setResolution(PopupDateField.RESOLUTION_HOUR);
		finishTimeHLayout.addComponent(pdfFinishTime);
		
		btnSearch = new Button("查 询", this);
		btnSearch.setStyleName("default");
		btnSearch.setImmediate(true);
		gridLayout.addComponent(btnSearch, 3, 0);
	}
	
	/** 创建 GridLayout 布局第二行组件 */
	private void createSecondHLayout() {
		// 部门选择
		HorizontalLayout deptHLayout = new HorizontalLayout();
		deptHLayout.setSpacing(true);
		gridLayout.addComponent(deptHLayout, 0, 1);
		
		Label lblDept = new Label("部门选择：");
		lblDept.setWidth("-1px");
		deptHLayout.addComponent(lblDept);
		
		/*cmbDept = new ComboBox();
		cmbDept.setImmediate(true);
		cmbDept.setWidth("160px");
		cmbDept.addListener((ValueChangeListener)this);
		cmbDept.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cmbDept.setNullSelectionAllowed(false);*/
		cmbDept = achieveBasicUtil.getCmbDeptReport(cmbDept, loginUser, deptsList, deptContainer);
		cmbDept.setWidth("160px");
		cmbDept.addListener((ValueChangeListener)this);
		deptHLayout.addComponent(cmbDept);
		
		// 用户选择
		HorizontalLayout userHLayout = new HorizontalLayout();
		userHLayout.setSpacing(true);
		gridLayout.addComponent(userHLayout, 1, 1);
		
		Label lblUser = new Label("用户选择：");
		lblUser.setWidth("-1px");
		userHLayout.addComponent(lblUser);
		
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User showAllCsr = new User();
		showAllCsr.setEmpNo("全部-员工-部门");
		showAllCsr.setUsername("全部-员工-部门");
		showAllCsr.setId(0L);
		userContainer.addBean(showAllCsr);
		List<User> csrs = userService.getCsrsByDepartment(deptIdsList, domain.getId());
		userContainer.addAll(csrs);
		cmbUser = new ComboBox();
		cmbUser.setImmediate(true);
		cmbUser.setWidth("160px");
		cmbUser.addListener(this);
		cmbUser.setContainerDataSource(userContainer);
		cmbUser.setItemCaptionPropertyId("migrateCsr1");
		cmbUser.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cmbUser.setNullSelectionAllowed(false);
		cmbUser.setValue(showAllCsr);
		userHLayout.addComponent(cmbUser);
		
		HorizontalLayout clearHLayout = new HorizontalLayout();
		clearHLayout.setSpacing(true);
		clearHLayout.setWidth("100%");
		gridLayout.addComponent(clearHLayout, 2, 1);
		
		btnClear = new Button("清 空", this);
		btnClear.setImmediate(true);
		clearHLayout.addComponent(btnClear);
		clearHLayout.setComponentAlignment(btnClear, Alignment.MIDDLE_RIGHT);
		
		btnExportExcel = new Button("导 出", this);
		btnExportExcel.setImmediate(true);
		gridLayout.addComponent(btnExportExcel, 3, 1);
		
		downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);
	}
	
	/** 创建 表格和按钮布局组件 */
	private void createTableAndButtonsLayout() {
		table = new Table();
		table.setWidth("100%");
		table.setHeight("-1px");
		table.setHeight("100%");
		table.setSelectable(true);
		table.setNullSelectionAllowed(false);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		
		this.addComponent(table);
		this.setComponentAlignment(table, Alignment.MIDDLE_CENTER);
		this.setExpandRatio(table, 1.0f);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == cmbDept) {
			setCmbUserValue();
		}
	}
	
	/** 设置用户选择ComboBox里面的内容 */
	private void setCmbUserValue() {
		if(cmbDept.getValue() != null){
			Department dept = (Department) cmbDept.getValue();
			if(dept == null) {
				this.getApplication().getMainWindow().getWindow().showNotification("请选择要查询的部门");
				return;
			}
			
			BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
			User showAllCsr = new User();
			showAllCsr.setEmpNo("全部-员工-部门");
			showAllCsr.setUsername("全部-员工-部门");
			showAllCsr.setId(0L);
			userContainer.addBean(showAllCsr);
			List<Long> strList = new ArrayList<Long>();
			if(dept.getId().equals(0L)) {
				strList.addAll(deptIdsList);
				List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
				userContainer.addAll(csrs);
				cmbUser.setContainerDataSource(userContainer);
			} else {
				strList.add(dept.getId());
				List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
				userContainer.addAll(csrs);
				cmbUser.setContainerDataSource(userContainer);
			}
			cmbUser.setValue(showAllCsr);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnSearch) {
			btnSearch.setEnabled(false);
			if(!executeSearch()) {
				btnSearch.setEnabled(true);
				return;
			}
			
			btnSearch.setEnabled(true);
		} else if(source == btnClear) {
			cmbTimeScope.setValue("今天");
		} else if(source == btnExportExcel) {
			// 导出报表
			new Thread(new Runnable() {
				@Override
				public void run() {
					btnExportExcel.setEnabled(false);
					executeExportExcel();
					btnExportExcel.setEnabled(true);
				}
			}).start();
		}
	}
	
	/** 点击查询按钮执行的方法 */
	private boolean executeSearch() {
		if(headersList != null) headersList.clear();
		if(allTableList != null) allTableList.clear();
		
		table.setValue(null);
		table.removeAllItems();
		
		if(pauseReasonsList == null || pauseReasonsList.size() == 0) {
			this.getApplication().getMainWindow().showNotification("对不起, 当前系统中没有置忙状态信息这个功能项, 无法查询到结果! ", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		StringBuffer sbWhereQuery = new StringBuffer(" where reason in ('"+StringUtils.join(pauseReasonsList, "','")+"')");
		
		if(pdfStartTime.getValue() == null || "".equals(pdfStartTime.getValue().toString()) || pdfFinishTime.getValue() == null || "".equals(pdfFinishTime.getValue().toString())) {
			this.getApplication().getMainWindow().showNotification("对不起, 请选择一个查询的时间范围, 开始时间和结束时间不能为空!", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		Date start_time = (Date) pdfStartTime.getValue();
		if(start_time.after(new Date())) {
			this.getApplication().getMainWindow().showNotification("对不起, 开始时间不能超过当前时间！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		String startTime = sdf.format((Date)pdfStartTime.getValue());
		String finishTime = sdf.format((Date)pdfFinishTime.getValue());
		sbWhereQuery.append(" and pausedate >= '");
		sbWhereQuery.append(startTime);
		sbWhereQuery.append("' and pausedate <= '");
		sbWhereQuery.append(finishTime);
		sbWhereQuery.append("' ");
		
		if(cmbDept.getValue() == null) {
			this.getApplication().getMainWindow().showNotification("请选择要查询的部门! ", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		if(cmbUser.getValue() == null) {
			this.getApplication().getMainWindow().showNotification("请选择要查询的用户! ", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		User queryUser = (User) cmbUser.getValue();
		Department queryDept = (Department) cmbDept.getValue();
		if(queryUser.getId().equals(0L)) {
			if(queryDept.getId().equals(0L)) {
				sbWhereQuery.append(" and deptid in(");
				sbWhereQuery.append(StringUtils.join(deptIdsList, ","));
				sbWhereQuery.append(")");
			} else {
				sbWhereQuery.append(" and deptid = ");
				sbWhereQuery.append(queryDept.getId());
			}
		} else {
			sbWhereQuery.append(" and username = '");
			sbWhereQuery.append(queryUser.getUsername());
			sbWhereQuery.append("' ");
		}
		
		/**
		 * 统计字段: 
		 *  部门, 用户名, 队列, 置忙总次数, 置忙总时间, 置忙原因(次数), 置忙原因(时长),
		 */
		headersList.add("部门");
		headersList.add("用户名");
		headersList.add("队列");
		headersList.add("置忙总次数");
		headersList.add("置忙总时长");
		
		for(String reason : pauseReasonsList) {
			headersList.add(reason + "(次数)");
			headersList.add(reason + "(时长)");
		}
		
		allTableList.add(headersList.toArray());
		
		// 所管辖的员工
		/*List<Long> strList = new ArrayList<Long>();
		strList.addAll(deptIdsList);
		List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());	
		Map<Long, User> userMap = new HashMap<Long, User>();
		for(User user : csrs) {
			userMap.put(user.getId(), user);
		}*/
		
		for(String header: headersList) {
			table.addContainerProperty(header, String.class, " ");
		}
		table.setColumnHeaders(headersList.toArray(new String[headersList.size()]));
		
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select deptname, username, queue, count(id), reason, ");
		/*sbQuery.append("sum(case when (pausedate >= '");
		sbQuery.append(startTime);
		sbQuery.append("' and pausedate < '");
		sbQuery.append(finishTime);
		sbQuery.append("') then 1 else 0 end) as pausedateCT,");*/
		sbQuery.append(" sum(case when unpausedate is null then ((case when (now() >= '");
		sbQuery.append(finishTime);
		sbQuery.append("') then '");
		sbQuery.append(finishTime);
		sbQuery.append("' else now() end) - (case when pausedate >= '");
		sbQuery.append(startTime);
		sbQuery.append("' then pausedate else '");
		sbQuery.append(startTime);
		sbQuery.append("' end)) else ((case when (unpausedate >= '");
		sbQuery.append(finishTime);
		sbQuery.append("') then '");
		sbQuery.append(finishTime);
		sbQuery.append("' else unpausedate end) - (case when pausedate >= '");
		sbQuery.append(startTime);
		sbQuery.append("' then pausedate else '");
		sbQuery.append(startTime);
		sbQuery.append("' end)) end) as pausedateDate from ec2_queue_member_pause_event_log ");
		sbQuery.append(sbWhereQuery);
		sbQuery.append(" group by deptname, domainid, queue, username,reason order by username");
		
		HashMap<String, List<Object>> recordRowsMap = new HashMap<String, List<Object>>();		// 用来存储 table 显示的每一列的值
		List<String> existsList = new ArrayList<String>();										// 用来存储当前 table 显示行的信息是否已经存在, 也就是去除重复
		List<Object> rowsList = new ArrayList<Object>();										// table 显示行的值用 list 来存储, 最后转换为数组
		
		@SuppressWarnings("unchecked")
		List<Object[]> objectsList = (List<Object[]>) commonService.excuteNativeSql(sbQuery.toString(), ExecuteType.RESULT_LIST);
		/*System.out.println(sbQuery.toString());
		for(Object[] objs : objectsList) {
			for(int i = 0; i < objs.length; i++) {
				System.out.print(objs[i] + "\t\t\t");
			}
			System.out.println();
		}*/
		
		for(Object[] objects : objectsList) {	// 遍历查询出来的结果集合
			if(!existsList.contains(objects[0]+"-"+objects[1]+"-"+objects[2])) {	// 判断当前行是否已经添加过
				rowsList = new ArrayList<Object>();
				
				for(int i = 0; i < (pauseReasonsList.size() * 2 + 5); i ++) {	// 填充这个显示列, 为防止后面报索引异常
					rowsList.add(i, 0);
				}
				rowsList.set(0, objects[0]);
				rowsList.set(1, objects[1]);
				rowsList.set(2, objects[2]);
				existsList.add(objects[0]+"-"+objects[1]+"-"+objects[2]);
			} else {
				rowsList = recordRowsMap.get(objects[0]+"-"+objects[1]+"-"+objects[2]);	// 如果这个要填充的 table 行信息已经添加过了, 则这里进行对其列进行赋值
			}
			
			int count = 5;
			for(String reason : pauseReasonsList) {	// 这里对其对应的表头信息列名称和值保持一致
				if(reason.equals(objects[4])) {
					rowsList.set(count, objects[3]);
					count += 1;
					rowsList.set(count, buildDateStr(buildDurationParams(objects[5]+""))[1]);
				} else {
					count += 1;
				}
				count ++;
			}
			
			recordRowsMap.put(objects[0]+"-"+objects[1]+"-"+objects[2], rowsList);
		}
		
		StringBuffer sbTotalQuery = new StringBuffer();
		sbTotalQuery.append("select deptname, username, queue, count(id), ");
		sbTotalQuery.append(" sum(case when unpausedate is null then ((case when (now() >= '");
		sbTotalQuery.append(finishTime);
		sbTotalQuery.append("') then '");
		sbTotalQuery.append(finishTime);
		sbTotalQuery.append("' else now() end) - (case when pausedate >= '");
		sbTotalQuery.append(startTime);
		sbTotalQuery.append("' then pausedate else '");
		sbTotalQuery.append(startTime);
		sbTotalQuery.append("' end)) else ((case when (unpausedate >= '");
		sbTotalQuery.append(finishTime);
		sbTotalQuery.append("') then '");
		sbTotalQuery.append(finishTime);
		sbTotalQuery.append("' else unpausedate end) - (case when pausedate >= '");
		sbTotalQuery.append(startTime);
		sbTotalQuery.append("' then pausedate else '");
		sbTotalQuery.append(startTime);
		sbTotalQuery.append("' end)) end) as pausedateDate from ec2_queue_member_pause_event_log ");
		sbTotalQuery.append(sbWhereQuery);
		sbTotalQuery.append(" group by deptname, domainid, queue, username order by username");
		
		@SuppressWarnings("unchecked")
		List<Object[]> objectsTotalList = (List<Object[]>) commonService.excuteNativeSql(sbTotalQuery.toString(), ExecuteType.RESULT_LIST);
		
		for(Object[] objects : objectsTotalList) {
			List<Object> objectList = recordRowsMap.get(objects[0]+"-"+objects[1]+"-"+objects[2]);
			if(objectList != null && objectList.size() > 0) {
				objectList.set(3, objects[3]);
				objectList.set(4, buildDateStr(buildDurationParams(objects[4]+""))[1]);
			}
		}
		
		for(Entry<String, List<Object>> entryRows : recordRowsMap.entrySet()) {
			table.addItem(entryRows.getValue().toArray(), null);
			allTableList.add(entryRows.getValue().toArray());
		}
		
		return true;
	}
	
	/**
	 * 导出 Excel 报表
	 */
	private boolean executeExportExcel() {
		// 把数据存放到 Excel 文件中
		File file = null;
		FileOutputStream fos = null;
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = null;
		HSSFRow row = null;
		HSSFCell cell = null;
		
		if(allTableList.size() < 2) {
			this.getApplication().getMainWindow().showNotification("查询结果为空, 无法进行到处数据!", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		try {
			file = new File(new String((exportPath + "/置忙详情统计报表_" + new Date().getTime() + ".xls").getBytes("UTF-8"), "ISO-8859-1"));
			if(!file.exists()) {	// 如果目录不存在
				if(!file.getParentFile().exists()) {	// 如果父目录不存在
					file.getParentFile().mkdirs();
				}
				try {
					file.createNewFile();	// 创建一个空目录
				} catch (Exception e) {
					throw new RuntimeException("无法在指定的位置创建新的Excel文件!");
				}
			} else {
				throw new RuntimeException("Excel 文件已经存在, 请重新创建!");
			}
			
			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"), "UTF8");
			// 记录导出日志
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setFilePath(filePath);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(loginUser.getUsername());
			operationLog.setRealName(loginUser.getRealName());
			WebApplicationContext context = (WebApplicationContext)this.getApplication().getContext();
			String ip = context.getBrowser().getAddress();
			operationLog.setIp(ip);
			operationLog.setDescription("导出置忙详情统计报表");
			operationLog.setProgrammerSee("");
			commonService.save(operationLog);
			
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			style.setFillBackgroundColor(HSSFColor.GREEN.index);
			style.setFillForegroundColor(HSSFColor.GREEN.index);
			HSSFFont font = wb.createFont();
			style.setFont(font);
			
			// 设置文本格式单元格
			HSSFCellStyle styleString = wb.createCellStyle();
			HSSFDataFormat formatString = wb.createDataFormat();
			styleString.setDataFormat(formatString.getFormat("@"));
			
			HSSFCellStyle styleNumber = wb.createCellStyle();
			HSSFDataFormat formatNumber = wb.createDataFormat();
			styleNumber.setDataFormat(formatNumber.getFormat("#,##0"));
			
			HSSFCellStyle styleDouble = wb.createCellStyle();
			HSSFDataFormat formatDouble = wb.createDataFormat();
			styleDouble.setDataFormat(formatDouble.getFormat("0.00%"));
			
			sheet = wb.createSheet("置忙详情统计报表");
			row = sheet.createRow(0);
			for(int i = 0; i < allTableList.get(0).length; i++) {
				cell = row.createCell(i);
				cell.setCellValue(allTableList.get(0)[i]+"");
				cell.setCellStyle(style);
			}
			
			for(int i = 1; i < allTableList.size(); i++) {
				row = sheet.createRow(i);
				Object[] columns = allTableList.get(i);
				for(int j = 0; j < columns.length; j++) {
					HSSFCell cell2 = row.createCell(j);
					if(j > 2 && j%2 == 1) {
						int value = 0;
						if(columns[j] != null) {
							value = ((Number)columns[j]).intValue();
						}
						cell2.setCellValue(value);
						cell2.setCellStyle(styleNumber);
					} else {
						if(columns[j] == null) {
							cell2.setCellValue("");
						} else {
							cell2.setCellValue(columns[j].toString());
						}
						cell2.setCellStyle(styleString);
					}
				}
			}
			
			fos = new FileOutputStream(file);
			wb.write(fos);
			downloadFile(file);
			
		} catch (Exception e) {
			btnExportExcel.setEnabled(true);
			this.getApplication().getMainWindow().showNotification("导出置忙详情统计报表出现异常! ", Notification.TYPE_WARNING_MESSAGE);
			logger.error("jinht -->> 导出置忙详情统计报表出现异常 : " + e.getMessage(), e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 文件创建好之后, 下载文件
	 */
	private void downloadFile(File file) {
		Resource resource = new FileResource(file, this.getApplication());
		downloader.setSource(resource);
		this.getApplication().getMainWindow().addComponent(downloader);
		downloader.addListener(new RepaintRequestListener() {
			@Override
			public void repaintRequested(RepaintRequestEvent event) {
				if(downloader.getParent() != null) {
					((VerticalLayout)(downloader.getParent())).removeComponent(downloader);
				}
			}
		});
	}
	
	/**
	 * 解析由NativeSql 查出来的时间差，获取其中的天、小时、分钟、秒，对应的值
	 * @param dateInfoStr
	 * @return
	 */
	private Integer[] buildDurationParams(String dateInfoStr) {
		Integer[] params = new Integer[]{0,0,0,0};
		String[] dts = dateInfoStr.split(" ");
		if(dts != null && dts.length > 10) {
			dts[10] = dts[10].substring(0, dts[10].indexOf("."));
			params[0] = Integer.parseInt(dts[4]);
			params[1] = Integer.parseInt(dts[6]);
			params[2] = Integer.parseInt(dts[8]);
			params[3] = Integer.parseInt(dts[10]);
		}
		return params;
	}
	
	/**
	 * 根据天、小时、分钟、秒，对应的值，组装成 7天 10:10:00 这样的字符串
	 * @param dateParams
	 * @return Object[] [3600, '01:00:00']
	 */
	private Object[] buildDateStr(Integer[] dateParams) {
		int day = dateParams[0];
		int hour = dateParams[1];
		int minute = dateParams[2];
		int second = dateParams[3];
		
		Long duration_sec_long = (long) (day*24*3600 + hour*3600 + minute*60 + second);
		
		int addMin = second / 60;				// 如果秒数大于60，则算出多余的分钟数
		int addHour = (minute + addMin) / 60;	// 如果分钟大于60，则算出多余的小时数
		int addDay = (hour + addHour) / 24;		// 如果小时大于24，则算出多余的天数
		second = second % 60;
		minute = (minute + addMin) % 60;
		hour = (hour + addHour) % 24;
		day = day + addDay;
		
		String onlineTimeStr = "";
		if(day > 0) {
			onlineTimeStr = day+"天 ";
		}
		if(hour >= 0 && hour < 10) {
			onlineTimeStr += "0"+hour+":";
		} else {
			onlineTimeStr += hour+":";
		}
		if(minute >= 0 && minute < 10) {
			onlineTimeStr += "0"+minute+":";
		} else {
			onlineTimeStr += minute+":";
		}
		if(second >= 0 && second < 10) {
			onlineTimeStr += "0"+second;
		} else {
			onlineTimeStr += second;
		}
		
		return new Object[]{duration_sec_long, onlineTimeStr};
	}
	
	@Override
	public void attach() {
		this.btnSearch.click();
	}
	
}
