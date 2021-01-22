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
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.entity.Customer;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.CustomerDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.DateTransformFactory;
import com.jiangyifen.ec2.utils.SpringContextHolder;
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

@SuppressWarnings("serial")
public class TopUiCustomerDetail extends VerticalLayout {

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的

	private Label fromLabel;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;
	private Label deptLabel;
	private ComboBox deptBox;
	private Label prjectLabel;
	private ComboBox projectBox;
	private Label usernameLabel;
	private TextField usernameField;
	private Label nameLabel;
	private TextField nameField;
	private Label teleLabel;
	private TextField teleField;
	private Label customerStatus;
	private ComboBox statusBox;
	private Button seeButton;
	private Button exportReport;// 导出报表

	private CustomerDetail customerDetail;

	private TabSheet tabSheet;

	private Tab tab;

	private List<Object[]> list;// 从数据库获得新的数据

	/*private String[] dateType = new String[] { "本日", "本周", "本月", "本年" };
	private String project;// 项目
	private String dept;// 部门
	private String empno;// 工号
	private String name;// 姓名
*/
	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private String titleName;// 传入的报表名
	private File file;

	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:ss:mm");
	private DateTransformFactory dtf = new DateTransformFactory();

	private ReportTableUtil tableUtil;

	// 当前时间
	private Date currentDate;
	private SimpleDateFormat format;
	private ReportService reportService = SpringContextHolder.getBean("reportService");
	// 登陆的user
	/*private User loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");*/

	// private String deptName;
	/*private String domainId;*/

	private String sql;

	public TopUiCustomerDetail(CustomerDetail customerDetail) {
		this.customerDetail = customerDetail;

		currentDate = new Date();
		format = new SimpleDateFormat("yyyy-MM-dd");

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		this.addComponent(horizontalLayout);

		fromLabel = new Label("创建日期:");
		horizontalLayout.addComponent(fromLabel);

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

		toLabel = new Label("预约日期:");
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

		teleLabel = new Label("电话:");
		horizontalLayout.addComponent(teleLabel);

		teleField = new TextField();
		teleField.setWidth("85px");
		horizontalLayout.addComponent(teleField);

		customerStatus = new Label("客服记录状态");
		customerStatus.setWidth("80px");
		horizontalLayout.addComponent(customerStatus);

		statusBox = new ComboBox();
		for (Object object : getCustomerStatus()) {
			statusBox.addItem(object);
		}
		statusBox.setWidth("80px");
		horizontalLayout.addComponent(statusBox);

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");

		if (SpringContextHolder.getBusinessModel().contains(
				MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_KPI_DETAIL)) {
			horizontalLayout.addComponent(exportReport);
		}

	}

	public void seeResult(ClickEvent event) {

		StringBuffer buffer = new StringBuffer(
				"select t1.customerresource_id,t2.projectname,t3.username,t3.realname,t4.name,t5.number,t1.distributetime,t1.ordertime,t1.laststatus"
						+ " from ec2_marketing_project_task as t1,ec2_markering_project as t2,ec2_user as t3,ec2_department as t4,ec2_telephone as t5"
						+ " where t1.marketingproject_id = t2.id and t1.user_id = t3.id and t3.department_id = t4.id and t5.customerresource_id = t1.customerresource_id");

		tabSheet = customerDetail.getTabSheet();

		// 登陆用户的domainId
		/*domainId = loginUser.getDomain().getId().toString();*/

		// 项目
		if (projectBox.getValue() != null) {
			buffer.append(" and t2.projectname = " + "'"
					+ projectBox.getValue().toString() + "'");
		}

		// 部门
		if (deptBox.getValue() != null) {
			buffer.append(" and t4.name = " + "'"
					+ deptBox.getValue().toString() + "'");
		}

		// 工号
		if (usernameField.getValue() != null
				&& !usernameField.getValue().toString().trim().equals("")) {
			buffer.append(" and t3.username = " + "'"
					+ usernameField.getValue().toString() + "'");
		}

		// 姓名
		if (nameField.getValue() != null
				&& !nameField.getValue().toString().trim().equals("")) {
			buffer.append(" and t3.realname = " + "'"
					+ nameField.getValue().toString() + "'");
		}
		// 电话
		if (teleField.getValue() != null
				&& !teleField.getValue().toString().trim().equals("")) {

			buffer.append(" and t5.number = " + "'"
					+ teleField.getValue().toString() + "'");
		}

		if (startDate.getValue() != null) {

			startValue = format.format(startDate.getValue());// 获得startDate的

			/*buffer.append(" and to_char(t1.distributetime,'yyyy-MM-dd') = "
					+ "'" + startValue + "'");*/
			buffer.append(" and t1.distributetime >= '" + startValue + "' and t1.distributetime < '"+dtf.getSpecifiedDayAfter(startValue, "yyyy-MM-dd")+"'");
		}

		if (endDate.getValue() != null) {

			endValue = format.format(endDate.getValue());// 获得endDate的值

			/*buffer.append(" and to_char(t1.ordertime,'yyyy-MM-dd') = " + "'"
					+ startValue + "'");*/
			buffer.append(" and t1.ordertime >= '"+ startValue + "' and t1.ordertime < '"+dtf.getSpecifiedDayAfter(startValue, "yyyy-MM-dd"));
		}

		// 客服记录状态
		if (statusBox.getValue() != null) {
			buffer.append(" and t1.laststatus = " + "'"
					+ statusBox.getValue().toString() + "'");
		}
		buffer.append(" group by t1.customerresource_id,t2.projectname,t3.username,t3.realname,t4.name,t5.number,t1.distributetime,t1.ordertime,t1.laststatus ");

		// 获得数据
		list = getRecord(buffer);

		Map<String, Customer> customerMap = new HashMap<String, Customer>();

		for (Object[] objects : list) {

			Customer customer = new Customer();
			if (objects[0] != null) {
				customer.setCustomerEmpno(objects[0].toString());
			}
			if (objects[1] != null) {
				customer.setProjectName(objects[1].toString());
			}
			if (objects[2] != null) {
				customer.setUsername(objects[2].toString());
			}
			if (objects[3] != null) {
				customer.setRealName(objects[3].toString());
			}
			if (objects[4] != null) {
				customer.setDept(objects[4].toString());
			}
			if (objects[5] != null) {
				customer.setTelephone(objects[5].toString());
			}
			if (objects[6] != null) {
				try {
					customer.setCreateDate(dateFormat.format(dateFormat
							.parse(objects[6].toString())));
				} catch (ParseException e) {

					e.printStackTrace();
				}
			}

			if (objects[7] != null) {
				try {
					customer.setOrderDate(dateFormat.format(dateFormat
							.parse(objects[7].toString())));
				} catch (ParseException e) {

					e.printStackTrace();
				}
			}
			if (objects[8] != null) {
				customer.setCustomerStatus(objects[8].toString());
			}

			customerMap.put(objects[0].toString(), customer);
		}

		// 土豆来源
		setDescription(customerMap);

		Set<Entry<String, Customer>> set = customerMap.entrySet();
		Iterator<Entry<String, Customer>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Entry<String, Customer> entry = iterator.next();

			Customer customer = entry.getValue();

			setCallDate(entry.getKey(), customer);
		}

		List<Object[]> list = new ArrayList<Object[]>();
		Set<Entry<String, Customer>> entries = customerMap.entrySet();
		Iterator<Entry<String, Customer>> iterator2 = entries.iterator();
		while (iterator2.hasNext()) {
			Object[] objects = new Object[13];
			Entry<String, Customer> entry = iterator2.next();
			Customer customer = entry.getValue();
			objects[0] = customer.getCustomerEmpno();
			objects[1] = customer.getProjectName();
			objects[2] = customer.getPotatoSource();
			objects[3] = customer.getUsername();
			objects[4] = customer.getRealName();
			objects[5] = customer.getDept();
			objects[6] = customer.getTelephone();
			objects[7] = customer.getCreateDate();
			objects[8] = customer.getOrderDate();
			objects[9] = customer.getFirstCallDate();
			objects[10] = customer.getSecondCallDate();
			objects[11] = customer.getThirdCallDate();
			objects[12] = customer.getCustomerStatus();

			list.add(objects);

		}

		// 设置table列名
		columnNames = new ArrayList<String>();
		columnNames.add("客户编号");
		columnNames.add("项目");
		columnNames.add("土豆来源");
		columnNames.add("工号");
		columnNames.add("姓名");
		columnNames.add("部门");
		columnNames.add("电话号码");
		columnNames.add("创建日期");
		columnNames.add("土豆预约日期");
		columnNames.add("首次拨打时间");
		columnNames.add("二次拨打时间");
		columnNames.add("三次拨打时间");
		columnNames.add("客户状态");

		tableUtil = new ReportTableUtil(list, "客户明细报表", columnNames,
				startValue, endValue);
		tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
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
		operationLog.setDescription("客户明细报表");
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

	public void setDescription(Map<String, Customer> map) {

		sql = "select customerresource_id,value from ec2_customer_resource_description"
				+ " where key = '土豆来源'";
		List<Object[]> objects = reportService.getMoreRecord(sql);

		for (Object[] objects2 : objects) {
			if (map.containsKey(objects2[0].toString())) {
				Customer customer = map.get(objects2[0].toString());
				customer.setPotatoSource(objects2[1].toString());
			}
		}

	}

	public void setCallDate(String customerresourceId, Customer customer) {

		sql = "select createdate from ec2_customer_service_record"
				+ " where customerresource_id = " + customerresourceId
				+ " order by createdate" + " limit 3";

		List<Object> list = reportService.getOneRecord(sql);

		if (list.size() >= 1 && list.get(0) != null) {
			try {
				customer.setFirstCallDate(dateFormat.format(dateFormat
						.parse(list.get(0).toString())));
			} catch (ParseException e) {

				e.printStackTrace();
			}
		}

		if (list.size() >= 2 && list.get(1) != null) {
			try {
				customer.setSecondCallDate(dateFormat.format(dateFormat
						.parse(list.get(1).toString())));
			} catch (ParseException e) {

				e.printStackTrace();
			}
		}

		if (list.size() >= 3 && list.get(2) != null) {
			try {
				customer.setThirdCallDate(dateFormat.format(dateFormat
						.parse(list.get(2).toString())));
			} catch (ParseException e) {

				e.printStackTrace();
			}
		}

	}

	public List<Object> getProjectName() {

		sql = "select projectname from ec2_markering_project"
				+ " group by projectname";

		List<Object> projects = reportService.getOneRecord(sql);

		return projects;
	}

	public List<Object> getDeptName() {

		sql = "select name from ec2_department" + " group by name";
		List<Object> deptNames = reportService.getOneRecord(sql);

		return deptNames;
	}

	public List<Object> getCustomerStatus() {

		sql = "select statusname from ec2_customer_service_record_status"
				+ " group by statusname";

		return reportService.getOneRecord(sql);

	}

	public List<Object[]> getRecord(StringBuffer buffer) {
		String sql = buffer.toString();
//		System.out.println("sql--> " + sql);
		return reportService.getMoreRecord(sql);
	}
}
