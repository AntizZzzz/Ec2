package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.entity.Kpi;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.KPI;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * REPORTPAGE PKI
 * @author lxy
 *
 */
@SuppressWarnings("serial")
public class TopUiKPI extends VerticalLayout {

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;// 日期
	private PopupDateField startDate;// 时间
	private Label toLabel;
	private PopupDateField endDate;// 时间
	private Label deptLabel;
	private ComboBox deptBox;// 部门
	private Label outgoingLabel;
	private ComboBox outgoingDirection;// 呼叫方向
	private Label prjectLabel;
	private ComboBox projectBox;// 项目
	private Label usernameLabel;
	private TextField usernameField;// 用户名
	private Label nameLabel;
	private TextField nameField;// 姓名
	private Label timeLabel;
	private TextField timeField;// 接通时长
	private Button seeButton;
	private Button exportReport;// 导出报表
	private KPI kpi;
	private TabSheet tabSheet;
	private Tab tab;
	private String[] dateType = new String[] { "本日", "本周", "本月", "本年" };
	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private File file;
	private ReportTableUtil tableUtil;

	private User loginUser;
	private Date currentDate;
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private ReportService reportService = SpringContextHolder.getBean("reportService");

	private String sql;
	private Map<String, Kpi> kpiMap;
	/*private DateTransformFactory dtf = new DateTransformFactory();*/

	public TopUiKPI(KPI kpi) {
		this.kpi = kpi;
		currentDate = new Date();
		initSpringContext();
		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		this.addComponent(horizontalLayout);

		dateComboBox = new ComboBox();
		for (int i = 0; i < dateType.length; i++) {
			dateComboBox.addItem(dateType[i]);
		}
		dateComboBox.setWidth("55px");
		dateComboBox.setImmediate(true);
		dateComboBox.setNullSelectionAllowed(false);
		dateComboBox.setValue(dateType[0]);// 默认是本日
		horizontalLayout.addComponent(dateComboBox);

		startDate = new PopupDateField();
		startDate.setWidth("110px");
		startDate.setDateFormat("yyyy-MM-dd");
		startDate.setResolution(PopupDateField.RESOLUTION_DAY);
		try {
			startDate.setValue(new Date());
		} catch (Exception e) {
			e.printStackTrace();
		}

		horizontalLayout.addComponent(startDate);

		toLabel = new Label("至");
		horizontalLayout.addComponent(toLabel);

		endDate = new PopupDateField();
		endDate.setWidth("110px");
		endDate.setDateFormat("yyyy-MM-dd");
		endDate.setResolution(PopupDateField.RESOLUTION_DAY);
		endDate.setValue(currentDate);
		horizontalLayout.addComponent(endDate);

		prjectLabel = new Label("项目:");
		horizontalLayout.addComponent(prjectLabel);

		projectBox = new ComboBox();
		for (Object object : getProjectName()) {

			projectBox.addItem(object.toString());
		}
		projectBox.setWidth("80px");
		horizontalLayout.addComponent(projectBox);

		outgoingLabel = new Label("呼叫方向:");
		horizontalLayout.addComponent(outgoingLabel);

		outgoingDirection = new ComboBox();
		outgoingDirection.addItem("呼出");
		outgoingDirection.addItem("呼入");
		outgoingDirection.setValue("呼出");
		outgoingDirection.setWidth("80px");
		horizontalLayout.addComponent(outgoingDirection);

		deptLabel = new Label("部门:");
		horizontalLayout.addComponent(deptLabel);

		deptBox = new ComboBox();
		for (Object object : getDeptName()) {
			deptBox.addItem(object.toString());
		}
		deptBox.setWidth("80px");
		deptBox.setImmediate(true);

		horizontalLayout.addComponent(deptBox);

		usernameLabel = new Label("工号:");
		horizontalLayout.addComponent(usernameLabel);

		usernameField = new TextField();
		usernameField.setWidth("80px");
		horizontalLayout.addComponent(usernameField);

		nameLabel = new Label("姓名:");
		horizontalLayout.addComponent(nameLabel);

		nameField = new TextField();
		nameField.setWidth("80px");
		horizontalLayout.addComponent(nameField);

		timeLabel = new Label("接通时长:");
		horizontalLayout.addComponent(timeLabel);

		timeField = new TextField();
		timeField.setWidth("80px");
		horizontalLayout.addComponent(timeField);

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");

		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_KPI_DETAIL)) {
			horizontalLayout.addComponent(exportReport);
		}
		// dateComboBox的事件
		dateComboBox.addListener(new ComboBox.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				try {
					if (event.getProperty().toString().equals("本日")) {
						startDate.setValue(currentDate);
						endDate.setValue(currentDate);
					} else if (event.getProperty().toString().equals("本周")) {
						startDate.setValue(format.parse(DateUtil.getFirstDayWeek()));
						endDate.setValue(currentDate);
					} else if (event.getProperty().toString().equals("本月")) {
						startDate.setValue(format.parse(DateUtil.getFirstDayMonth()));
						endDate.setValue(currentDate);
					} else if (event.getProperty().toString().equals("本年")) {
						startDate.setValue(format.parse(DateUtil.getFirstDayYear()));
						endDate.setValue(currentDate);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
	//初始化spring相关业务对象
	private void initSpringContext() {
		loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");
		reportService = SpringContextHolder.getBean("reportService");
	}
	public void seeResult(ClickEvent event) {
		if (outgoingDirection.getValue() == null) {
			NotificationUtil.showWarningNotification(this,"请选择呼叫方向！");
			return;
		}

		String direction = outgoingDirection.getValue().toString().trim();
		String sql = null;
		if (direction.equals("呼出")) {
			sql = "select t1.projectname,t2.deptname,t2.username,t2.realname,sum(t2.outgoing_total_count),sum(t2.outgoing_connect_count),round(sum(t2.outgoing_connect_count)*100.0/greatest(sum(t2.outgoing_total_count),1),2),"
					+ " sum(t2.outgoing_total_time_length),round(sum(t2.outgoing_connect_time_length)*1.0/greatest(sum(t2.outgoing_connect_count),1),0),sum(t2.outgoing_free_time_length)"
					+ " from ec2_markering_project as t1,ec2_report_call_check as t2"
					+ " where t1.id = t2.projectid";
		} else if (direction.equals("呼入")) {
			sql = "select t1.projectname,t2.deptname,t2.username,t2.realname,sum(t2.incoming_total_count),sum(t2.incoming_connect_count),round(sum(t2.incoming_connect_count)*100.0/greatest(sum(t2.incoming_total_count),1),2),"
					+ " sum(t2.incoming_total_time_length),round(sum(t2.incoming_connect_time_length)*1.0/greatest(sum(t2.incoming_connect_count),1),0),sum(t2.incoming_free_time_length)"
					+ " from ec2_markering_project as t1,ec2_report_call_check as t2"
					+ " where t1.id = t2.projectid";
		}

		StringBuffer sqlBuffer = new StringBuffer(sql);
		tabSheet = kpi.getTabSheet();
		if (projectBox.getValue() != null) {// 项目
			sqlBuffer.append(" and t1.projectname = " + "'" + projectBox.getValue().toString() + "'");
		}
		if (deptBox.getValue() != null) {// 部门
			sqlBuffer.append(" and t2.deptname = " + "'" + deptBox.getValue().toString() + "'");
		}
		if (usernameField.getValue() != null && !usernameField.getValue().toString().trim().equals("")) {// 工号
			sqlBuffer.append(" and t2.username = " + "'" + usernameField.getValue().toString() + "'");
		}
		if (nameField.getValue() != null && !nameField.getValue().toString().trim().equals("")) {// 姓名
			sqlBuffer.append(" and t2.realname = " + "'" + nameField.getValue().toString() + "'");
		}
		if (startDate.getValue() != null) {
			startValue = format.format(startDate.getValue());// 获得startDate的值
			sqlBuffer.append(" and t2.date >= " + "'" + startValue + "'");
		}
		if (endDate.getValue() != null) {
			endValue = format.format(endDate.getValue());// 获得endDate的值
			sqlBuffer.append(" and t2.date <= " + "'" + endValue + "'");
		}
		sqlBuffer.append(" group by t1.projectname,t2.deptname,t2.username,t2.realname");//主查询

		list = getRecord(sqlBuffer);
		kpiMap = new HashMap<String, Kpi>();

		for (Object[] objects : list) {
			Kpi kpi = new Kpi();
			if (objects[0] != null) {
				kpi.setProjectName(objects[0].toString());
			}
			if (objects[1] != null) {
				kpi.setDeptName(objects[1].toString());
			}
			if (objects[2] != null) {
				kpi.setUsername(objects[2].toString());
			}
			if (objects[3] != null) {
				kpi.setRealName(objects[3].toString());
			}
			if (objects[4] != null) {
				kpi.setOutgoingTotalCount(Integer.parseInt(objects[4].toString()));
			}
			if (objects[5] != null) {
				kpi.setOutgoingConnectCount(Integer.parseInt(objects[5].toString()));
			}
			if (objects[6] != null) {
				kpi.setOutgoingConnectRate(objects[6].toString());
			}
			if (objects[7] != null) {
				kpi.setOutgoingTotalTimeLength(Integer.parseInt(objects[7].toString()));
			}
			if (objects[8] != null) {
				kpi.setOutgoingAvgTimeLength(Integer.parseInt(objects[8].toString()));
			}
			if (objects[9] != null) {
				kpi.setFreeTotalTimeLength(Integer.parseInt(objects[9].toString()));
			}
			if (kpi.getWorkTotalTimeLengthRate() == null) {
				kpi.setWorkTotalTimeLengthRate("0.00");
			}
			kpiMap.put(objects[2].toString(), kpi);
		}

		// 登入时长
		for (Object[] objects : loginTimeLength(startValue, endValue)) {
			if (kpiMap.containsKey(objects[0].toString())) {
				kpiMap.get(objects[0].toString()).setLoginTotalTimeLength(Integer.parseInt(objects[1].toString()));
			}
		}

		// 置忙时长
		for (Object[] objects : busyTotalTimeLength(startValue, endValue)) {
			if (kpiMap.containsKey(objects[0].toString())) {
				kpiMap.get(objects[0].toString()).setBusyTotalTimeLength(Integer.parseInt(objects[1].toString()));
			}
		}
		// 能聊时长率（通话X秒以上数/接通数）
		/*for (Object[] objects : connectCountByTime(startValue, endValue, time)) {
			if (kpiMap.containsKey(objects[0].toString())) {
				Kpi kpi = kpiMap.get(objects[0].toString());
				if (kpi.getOutgoingConnectCount() != 0) {
					kpi.setCallTimeLengthRate((Integer.parseInt(objects[1].toString()) * 100 / kpi.getOutgoingConnectCount()) + "%");
				}
			}
		}
*/
		List<Object[]> list = new ArrayList<Object[]>();
		Set<Entry<String, Kpi>> entries = kpiMap.entrySet();
		Iterator<Entry<String, Kpi>> iterator = entries.iterator();
		while (iterator.hasNext()) {
			Object[] objects = new Object[12];
			Entry<String, Kpi> entry = iterator.next();
			Kpi kpi = entry.getValue();
			objects[0] = kpi.getProjectName();
			objects[1] = kpi.getDeptName();
			objects[2] = kpi.getUsername();
			objects[3] = kpi.getRealName();
			objects[4] = kpi.getOutgoingTotalCount();
			objects[5] = kpi.getOutgoingConnectCount();
			objects[6] = kpi.getOutgoingConnectRate() + "%";
			objects[7] = kpi.getOutgoingTotalTimeLength();
			objects[8] = kpi.getOutgoingAvgTimeLength();
			objects[9] = kpi.getLoginTotalTimeLength();
			objects[10] = kpi.getBusyTotalTimeLength();
			objects[11] = kpi.getFreeTotalTimeLength();
			//objects[12] = kpi.getRingTotalTimeLength();	//振铃时长
			//objects[12] = kpi.getWorkTotalTimeLengthRate() + "%";
			//objects[14] = kpi.getCallTimeLengthRate();
			list.add(objects);
		}

		for (Object[] objects : list) {
			objects[7] = DateUtil.getTime(Long.parseLong(objects[7].toString()));
			objects[8] = DateUtil.getTime(Long.parseLong(objects[8].toString()));
			objects[9] = DateUtil.getTime(Long.parseLong(objects[9].toString()));
			objects[10] = DateUtil.getTime(Long.parseLong(objects[10].toString()));
			objects[11] = DateUtil.getTime(Long.parseLong(objects[11].toString()));
			//objects[12] = DateUtil.getTime(Long.parseLong(objects[12].toString()));
		}

		// 设置table列名
		columnNames = new ArrayList<String>();
		columnNames.add("项目");			//0
		columnNames.add("部门");			//1
		columnNames.add("工号");			//3
		columnNames.add("姓名");			//4
		columnNames.add("呼叫总数");		//5
		columnNames.add("呼叫接通数");		//6
		columnNames.add("呼叫接通率");		//7
		columnNames.add("呼叫总时长");		//8
		columnNames.add("呼叫平均时长");	//9
		columnNames.add("登入总时长");		//10
		columnNames.add("置忙总时长");		//11
		columnNames.add("置闲时长");		//12
		//columnNames.add("振铃总时长");	//13
		//columnNames.add("工作时长占比");	//14
		//columnNames.add("能聊时长率");

		tableUtil = new ReportTableUtil(list, "", columnNames, startValue, endValue);
		tab = tabSheet.addTab(tableUtil, "统计详情", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);

	}

	// 导出报表事件
	public void exportReport(ClickEvent event) {
		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询");
			return;
		}
		// 得到当前tab的组件，把当前tab页的数据导出报表
		ReportTableUtil reportTableUtil = (ReportTableUtil) tabSheet.getSelectedTab();
		// 如果reportTableUtil为null提示请点击查询
		if (reportTableUtil == null) {
			NotificationUtil.showWarningNotification(this, "请先查询");
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
		file = ExportExcelUtil.exportExcel("KPI报表", list, columnNames,startValue, endValue);
		
		saveOperationLog();// 记录导出日志
		fileDownload(event);// 文件下载
	}

	public List<Object[]> connectCountByTime(String startValue,String endValue, int time) {
		/*sql = "select srcusername,count(*) from cdr"
				+ " where srcusername != '' and cdrdirection = '1' and disposition = 'ANSWERED' and billableseconds >= "
				+ time + " and to_char(starttimedate,'yyyy-MM-dd hh24') >= "
				+ "'" + startValue + "'"
				+ " and to_char(starttimedate,'yyyy-MM-dd hh24') <= " + "'"
				+ endValue + "'" + " group by srcusername";*/
		sql = "select srcusername,count(*) from cdr"
				+ " where srcusername != '' and cdrdirection = '1' and disposition = 'ANSWERED' and billableseconds >= "
				+ time + " and starttimedate >= "
				+ "'" + startValue + "'"
				+ " and starttimedate <= " + "'"
				+ endValue + "'" + " group by srcusername";
		
		return reportService.getMoreRecord(sql);
	}

	public List<Object[]> loginTimeLength(String startValue, String endValue) {
		/*sql = "select username,sum(login_time_length)"
				+ " from ec2_report_employee_login"
				+ " where to_char(logindate,'yyyy-MM-dd') >= " + "'"
				+ startValue + "'" + " and to_char(logindate,'yyyy-MM-dd') <= "
				+ "'" + endValue + "'" + " group by username";*/
		sql = "select username,sum(login_time_length)"
				+ " from ec2_report_employee_login"
				+ " where logindate >= " + "'"
				+ startValue + "'" + " and logindate <= "
				+ "'" + endValue + "'" + " group by username";
		return reportService.getMoreRecord(sql);
	}

	public List<Object[]> busyTotalTimeLength(String startValue, String endValue) {
		sql = "select username,sum(busytimelength)"
				+ " from ec2_report_employee_busy" + " where date >= " + "'"
				+ startValue + "'" + " and date <= " + "'" + endValue + "'"
				+ " group by username";
		return reportService.getMoreRecord(sql);
	}

	public List<Object> getProjectName() {
		sql = "select projectname from ec2_markering_project group by projectname";
		List<Object> projects = reportService.getOneRecord(sql);
		return projects;
	}

	public List<Object> getDeptName() {
		sql = "select name from ec2_department  group by name";
		List<Object> deptNames = reportService.getOneRecord(sql);
		return deptNames;
	}

	public List<Object[]> getRecord(StringBuffer buffer) {
		String sql = buffer.toString();
		return reportService.getMoreRecord(sql);
	}
	
	//记录导出日志
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
		operationLog.setDescription("导出KPI报表");
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
}
