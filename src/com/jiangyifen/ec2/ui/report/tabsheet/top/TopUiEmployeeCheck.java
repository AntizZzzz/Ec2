package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.text.ParseException;
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
import com.jiangyifen.ec2.report.pojo.EmployeeCheckPo;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.EmployeeCheck;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * REPORTPAGE人员考核
 * @author lxy
 *
 */
@SuppressWarnings("serial")
public class TopUiEmployeeCheck extends VerticalLayout {

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;
	private Label fromLabel;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;

	private Button seeButton;
	private Button exportReport;// 导出报表
	private ComboBox usernameBox;

	private EmployeeCheck employeeCheck;

	private TabSheet tabSheet;

	private Tab tab;

	private String[] dateType = new String[] { "本日", "本周", "本月", "本年" };

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private String titleName;// 传入的报表名
	private File file;

	private ReportTableUtil tableUtil;
	private int index;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	// 当前时间
	private Date currentDate;
	private SimpleDateFormat format;
	private ReportService reportService = SpringContextHolder
			.getBean("reportService");

	// 登陆的user
	private User loginUser = (User) SpringContextHolder.getHttpSession()
			.getAttribute("loginUser");

	// private String deptName;
	private String domainId;

	private String sql;

	public TopUiEmployeeCheck(EmployeeCheck employeeCheck) {
		this.employeeCheck = employeeCheck;

		currentDate = new Date();
		format = new SimpleDateFormat("yyyy-MM-dd");

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		this.addComponent(horizontalLayout);

		dateComboBox = new ComboBox();
		for (int i = 0; i < dateType.length; i++) {
			dateComboBox.addItem(dateType[i]);
		}
		dateComboBox.setWidth("55px");
		dateComboBox.setImmediate(true);
		horizontalLayout.setMargin(true,true,false,true);
		dateComboBox.setNullSelectionAllowed(false);
		dateComboBox.setValue(dateType[0]);// 默认是本日
		horizontalLayout.addComponent(dateComboBox);

		fromLabel = new Label("从");
		horizontalLayout.addComponent(fromLabel);

		startDate = new PopupDateField();
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
		endDate.setDateFormat("yyyy-MM-dd");
		endDate.setResolution(PopupDateField.RESOLUTION_DAY);
		endDate.setValue(currentDate);
		horizontalLayout.addComponent(endDate);

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");
		if (SpringContextHolder.getBusinessModel().contains(
				MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_STAFF_CHECK)) {
			horizontalLayout.addComponent(exportReport);
		}

		usernameBox = new ComboBox();
		usernameBox.setWidth("100px");
		usernameBox.setImmediate(true);
		usernameBox.setInputPrompt("------工号------");
		horizontalLayout.addComponent(usernameBox);

		usernameBox.addListener(new ComboBox.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {

				if (event.getProperty().getValue() == null) {
					return;
				}

				String value = event.getProperty().getValue().toString();

				Table table = tableUtil.getTable();
				table.removeAllItems();

				for (Object[] objects : list) {

					if (objects[index].equals(value)) {

						table.addItem(objects, null);
					}
				}

			}
		});

		// dateComboBox的事件
		dateComboBox.addListener(new ComboBox.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {

				try {
					if (event.getProperty().toString().equals("本日")) {

						startDate.setValue(currentDate);
						endDate.setValue(currentDate);
					} else if (event.getProperty().toString().equals("本周")) {

						startDate.setValue(format.parse(DateUtil
								.getFirstDayWeek()));
						endDate.setValue(currentDate);

					} else if (event.getProperty().toString().equals("本月")) {

						startDate.setValue(format.parse(DateUtil
								.getFirstDayMonth()));
						endDate.setValue(currentDate);

					} else if (event.getProperty().toString().equals("本年")) {
						startDate.setValue(format.parse(DateUtil
								.getFirstDayYear()));
						endDate.setValue(currentDate);

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

	}

	public void seeResult(ClickEvent event) {

		tabSheet = employeeCheck.getTabSheet();

		// 获得登陆用户的部门名称
		// deptName = loginUser.getDepartment().getName();

		// 登陆用户所在的域
		domainId = loginUser.getDomain().getId().toString();

		startValue = format.format(startDate.getValue());// 获得startDate的值
		endValue = format.format(endDate.getValue());// 获得endDate的值

		// key=username,value=entity
		Map<String, EmployeeCheckPo> employeeCheckPoMap = new HashMap<String, EmployeeCheckPo>();

		// 登陆时长
		list = getLoginTimeLength(startValue, endValue);

		for (Object[] objects : list) {

			EmployeeCheckPo employeeCheckPo = new EmployeeCheckPo();
			// 部门
			if (objects[0] != null) {
				employeeCheckPo.setDeptName(objects[0].toString());
			}
			// 工号
			if (objects[1] != null) {
				employeeCheckPo.setEmpno(objects[1].toString());
			}
			// 姓名
			if (objects[2] != null) {
				employeeCheckPo.setName(objects[2].toString());
			}
			// 日期
			if (objects[3] != null) {
				try {
					employeeCheckPo.setDate(dateFormat.parse(objects[3]
							.toString()));
				} catch (ParseException e) {

					e.printStackTrace();
				}
			}
			// 登陆时长
			if (objects[4] != null) {
				employeeCheckPo.setLoginTimeLength(objects[4].toString());
			}

			// 保存到map
			employeeCheckPoMap.put(objects[1].toString(), employeeCheckPo);

		}

		// 置忙时长
		list = getBusyTimeLength(startValue, endValue);

		for (Object[] objects : list) {

			if (employeeCheckPoMap.containsKey(objects[1].toString())) {

				EmployeeCheckPo employeeCheckPo = employeeCheckPoMap
						.get(objects[1].toString());

				if (objects[4] != null) {

					employeeCheckPo.setBusyTimeLength(objects[4].toString());
				}

			}
		}

		list = new ArrayList<Object[]>();

		Set<Entry<String, EmployeeCheckPo>> set = employeeCheckPoMap.entrySet();
		Iterator<Entry<String, EmployeeCheckPo>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Entry<String, EmployeeCheckPo> entry = iterator.next();
			EmployeeCheckPo employeeCheckPo = entry.getValue();

			// 设置空闲时长
			if (employeeCheckPo.getLoginTimeLength() == null) {
				employeeCheckPo.setLoginTimeLength("0");
			}
			if (employeeCheckPo.getBusyTimeLength() == null) {
				employeeCheckPo.setBusyTimeLength("0");
			}

			// 设置空闲时长
			employeeCheckPo.setFreeTimeLength(String.valueOf(Integer
					.parseInt(employeeCheckPo.getLoginTimeLength())
					- Integer.parseInt(employeeCheckPo.getBusyTimeLength())));

			// 设置时长格式
			employeeCheckPo.setLoginTimeLength(DateUtil.getTime(Long
					.parseLong(employeeCheckPo.getLoginTimeLength())));
			employeeCheckPo.setBusyTimeLength(DateUtil.getTime(Long
					.parseLong(employeeCheckPo.getBusyTimeLength())));
			employeeCheckPo.setFreeTimeLength(DateUtil.getTime(Long
					.parseLong(employeeCheckPo.getFreeTimeLength())));

			// 存list中
			Object[] objects = new Object[7];
			objects[0] = employeeCheckPo.getDeptName();
			objects[1] = employeeCheckPo.getEmpno();
			objects[2] = employeeCheckPo.getName();
			objects[3] = dateFormat.format(employeeCheckPo.getDate());
			objects[4] = employeeCheckPo.getLoginTimeLength();
			objects[5] = employeeCheckPo.getBusyTimeLength();
			objects[6] = employeeCheckPo.getFreeTimeLength();

			list.add(objects);

		}
		// 设置table列名
		columnNames = new ArrayList<String>();

		columnNames.add("部门");
		columnNames.add("工号");
		columnNames.add("姓名");
		columnNames.add("日期");
		columnNames.add("登陆时长");
		columnNames.add("置忙时长");
		columnNames.add("空闲时长");

		tableUtil = new ReportTableUtil(list, "人员考核报表(按天统计)", columnNames,
				startValue, endValue);
		tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);

		// 获得工号所在列的下标
		List<String> names = tableUtil.getNames();
		index = names.indexOf("工号");

		usernameBox.removeAllItems();

		for (Object[] objects : list) {

			if (objects[index] != null) {

				usernameBox.addItem(objects[index]);
			}

		}

	}

	// 导出报表事件
	public void exportReport(ClickEvent event) {

		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询");
			return;
		}

		// 得到当前tab的组件，把当前tab页的数据导出报表
		ReportTableUtil reportTableUtil = (ReportTableUtil) tabSheet
				.getSelectedTab();

		// 如果reportTableUtil为null提示请点击查询
		if (reportTableUtil == null) {
			NotificationUtil.showWarningNotification(this, "请先查询");
			return;
		}

		// 设置文件名
		titleName = reportTableUtil.getTitleName();
		// 设置数据源
		list = reportTableUtil.getList();
		// 设置列名
		columnNames = reportTableUtil.getNames();
		// 开时时间
		startValue = reportTableUtil.getStartValue();
		// 结束时间
		endValue = reportTableUtil.getEndValue();

		// 生成excel file
		file = ExportExcelUtil.exportExcel(titleName, list, columnNames,
				startValue, endValue);

		// ======================chb 操作日志======================//
		// 记录导出日志
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null)
			operationLog.setFilePath(file.getAbsolutePath());
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(SpringContextHolder.getLoginUser()
				.getUsername());
		operationLog.setRealName(SpringContextHolder.getLoginUser()
				.getRealName());
		WebApplicationContext context = (WebApplicationContext) this
				.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("导出人员考核报表");
		operationLog.setProgrammerSee(sql);
		CommonService commonService = SpringContextHolder
				.getBean("commonService");
		commonService.save(operationLog);
		// ======================chb 操作日志======================//

		// 下载file
		final Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		Resource resource = new FileResource(file, event.getComponent()
				.getApplication());
		downloader.setSource(resource);
		this.addComponent(downloader);

		downloader.addListener(new RepaintRequestListener() {

			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent()))
						.removeComponent(downloader);
			}
		});
	}

	public List<Object[]> getLoginTimeLength(String startValue, String endValue) {

		sql = "select department,username,name,logindate,sum(login_time_length) from ec2_report_employee_login where "

				+ "domainid = "
				+ "'"
				+ domainId
				+ "'"
				+ " and logindate >= "
				+ "'"
				+ startValue
				+ "'"
				+ " and logindate <= "
				+ "'"
				+ endValue
				+ "'"
				+ " group by department,username,name,logindate"
				+ " order by department,username,name,logindate";
		return reportService.getMoreRecord(sql);
	}

	public List<Object[]> getBusyTimeLength(String startValue, String endValue) {
		sql = "select department,username,name,date,sum(busytimelength) from ec2_report_employee_busy where "

				+ "domainid = "
				+ "'"
				+ domainId
				+ "'"
				+ " and date >= "
				+ "'"
				+ startValue
				+ "'"
				+ " and date <= "
				+ "'"
				+ endValue
				+ "'"
				+ " group by department,username,name,date"
				+ " order by department,username,name,date";
		return reportService.getMoreRecord(sql);
	}

}
