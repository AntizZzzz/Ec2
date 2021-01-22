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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.entity.Business;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.BusinessDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.ShanXiJiaoTanWorkOrderConfig;
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
public class TopUiBusinessDetail extends VerticalLayout {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

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
	private Button seeButton;
	private Button exportReport;// 导出报表

	private BusinessDetail businessDetail;

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

	private ReportTableUtil tableUtil;

	private Map<String, Business> businesses;

	// 当前时间
	private Date currentDate;
	private SimpleDateFormat format;
	private ReportService reportService = SpringContextHolder
			.getBean("reportService");
	// 登陆的user
	/*private User loginUser = (User) SpringContextHolder.getHttpSession()
			.getAttribute("loginUser");*/

	// private String deptName;
	/*private String domainId;*/

	private String sql;

	public TopUiBusinessDetail(BusinessDetail businessDetail) {
		this.businessDetail = businessDetail;

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd 00:00:00");
		try {
			currentDate = dateFormat.parse(dateFormat.format(new Date()));
		} catch (ParseException e) {

		}
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		this.addComponent(horizontalLayout);

		fromLabel = new Label("从");
		horizontalLayout.addComponent(fromLabel);

		startDate = new PopupDateField();
		startDate.setWidth("110px");
		startDate.setImmediate(true);
		startDate.setValidationVisible(false);
		startDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startDate.setResolution(PopupDateField.RESOLUTION_SEC);
		startDate.setValue(currentDate);

		horizontalLayout.addComponent(startDate);

		toLabel = new Label("至");
		horizontalLayout.addComponent(toLabel);

		endDate = new PopupDateField();
		endDate.setWidth("110px");
		endDate.setValidationVisible(false);
		endDate.setDateFormat("yyyy-MM-dd HH:mm:ss");
		endDate.setResolution(PopupDateField.RESOLUTION_SEC);
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
				"select t2.projectname,t2.id,t3.username,t3.realname,t4.name"
						+ " from ec2_marketing_project_task as t1,ec2_markering_project as t2,ec2_user as t3,ec2_department as t4"
						+ " where t1.marketingproject_id = t2.id and t1.user_id = t3.id and t3.department_id = t4.id");

		tabSheet = businessDetail.getTabSheet();

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

		if (startDate.getValue() != null) {

			startValue = format.format(startDate.getValue());// 获得startDate的值

			/*buffer.append(" and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') >= "
					+ "'" + startValue + "'");*/
			buffer.append(" and t1.lastupdatedate >= "
					+ "'" + startValue + "'");
		}

		if (endDate.getValue() != null) {

			endValue = format.format(endDate.getValue());// 获得endDate的值

			/*buffer.append(" and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') <= "
					+ "'" + endValue + "'");*/
			buffer.append(" and t1.lastupdatedate <= "
					+ "'" + endValue + "'");
		}

		buffer.append(" group by t2.projectname,t2.id,t3.username,t3.realname,t4.name");

		// 获得数据
		list = getRecord(buffer);
		businesses = new HashMap<String, Business>();

		for (Object[] objects : list) {

			Business business = new Business();
			if (objects[0] != null) {

				business.setProjectName(objects[0].toString());
			}
			if (objects[2] != null) {
				business.setUsername(objects[2].toString());
			}
			if (objects[3] != null) {
				business.setName(objects[3].toString());
			}
			if (objects[4] != null) {
				business.setDept(objects[4].toString());
			}

			// projectId+username 唯一
			String key = objects[1].toString() + objects[2].toString();

			businesses.put(key, business);

		}

		// 接收土豆数
		for (Object[] objects : projectTaskCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setProjectTaskCount(
								Integer.parseInt(objects[2].toString()));
			}
		}

		// 呼出土豆数
		for (Object[] objects : finishedTaskCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setFinishedTaskCount(
								Integer.parseInt(objects[2].toString()));
			}
		}

		// 接通土豆数
		for (Object[] objects : answeredTaskCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setAnsweredTaskCount(
								Integer.parseInt(objects[2].toString()));
			}
		}

		// 未接通土豆数
		for (Object[] objects : unansweredTaskCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setUnansweredTaskCount(
								Integer.parseInt(objects[2].toString()));
			}
		}

		// 未处理土豆数
		for (Object[] objects : unfinishedTaskCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setUnfinishedTaskCount(
								Integer.parseInt(objects[2].toString()));
			}
		}

		// 拒绝数
		for (Object[] objects : refuseTaskCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setRefuseTaskCount(
								Integer.parseInt(objects[2].toString()));
			}
		}
		// 预约跟踪数
		for (Object[] objects : orderTrackCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setOrderTrackCount(
								Integer.parseInt(objects[2].toString()));
			}
		}

		// 预约开户数
		for (Object[] objects : orderCustomerCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setOrderCustomerCount(
								Integer.parseInt(objects[2].toString()));
			}
		}

		// 成功开户数
		for (Object[] objects : successCustomerCount(startValue, endValue)) {

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setSuccessCustomerCount(
								Integer.parseInt(objects[2].toString()));
			}
		}

		// 成功土豆数
		for (Object[] objects : successPotatoCount(startValue, endValue)) {

			// TODO
			logger.info("-----------------------");
			for (Object object : objects) {
				logger.info(object.toString() + "  ");
			}
			logger.info("-----------------------");

			if (businesses.containsKey(objects[0].toString()
					+ objects[1].toString())) {
				businesses.get(objects[0].toString() + objects[1].toString())
						.setSuccessPotatoCount(
								Integer.parseInt(objects[2].toString()));
			}
		}
		// cdr中接通的数量
		try {
			for (Object[] objects : cdrConnectCount(startValue, endValue)) {

				if (businesses.containsKey(objects[0].toString()
						+ objects[1].toString())) {

					businesses.get(
							objects[0].toString() + objects[1].toString())
							.setCdrConnectCount(
									Integer.parseInt(objects[2].toString()));
				}
			}
		} catch (NumberFormatException e) {

			e.printStackTrace();
		} catch (ParseException e) {

			e.printStackTrace();
		}

		String id = ShanXiJiaoTanWorkOrderConfig.props
				.getProperty("work_order1_project_id");

		List<Object[]> list = new ArrayList<Object[]>();

		// 如果是指定项目（土豆），则把一些数据设为0
		sql = "select projectname from ec2_markering_project where id = " + "'"
				+ id + "'";
		List<Object> list2 = reportService.getOneRecord(sql);

		Set<Entry<String, Business>> set = businesses.entrySet();
		Iterator<Entry<String, Business>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Object[] objects = new Object[16];
			Entry<String, Business> entry = iterator.next();
			Business business = entry.getValue();
			if (business.getFinishedTaskCount() != 0) {

				business.setAnsweredTaskRate(business.getAnsweredTaskCount()
						* 100 / business.getFinishedTaskCount() + "%");
			}

			if (list2.size() != 0
					&& business.getProjectName()
							.equals(list2.get(0).toString())) {

				objects[0] = business.getProjectName();
				objects[1] = business.getUsername();
				objects[2] = business.getName();
				objects[3] = business.getDept();
				objects[4] = "0";
				objects[5] = "0";
				objects[6] = "0";
				objects[7] = "0";
				objects[8] = "0";
				objects[9] = "0";
				objects[10] = "0";
				objects[11] = "0";
				objects[12] = "0";
				objects[13] = "0";
				objects[14] = business.getSuccessPotatoCount();

				// TODO 土豆转化率（土豆池中的客服记录状态为成功土豆的任务数/接通的任务数） 原始数据= 座席点击获取的数据
				// 土豆转化率 = 成功土豆数/cdr接通数
				if (business.getCdrConnectCount() != 0) {

					objects[15] = (business.getSuccessPotatoCount() * 100 / business
							.getCdrConnectCount()) + "%";
				}

			} else {
				objects[0] = business.getProjectName();
				objects[1] = business.getUsername();
				objects[2] = business.getName();
				objects[3] = business.getDept();
				objects[4] = business.getProjectTaskCount();
				objects[5] = business.getFinishedTaskCount();
				objects[6] = business.getAnsweredTaskCount();
				objects[7] = business.getUnansweredTaskCount();
				objects[8] = business.getAnsweredTaskRate();
				objects[9] = business.getUnfinishedTaskCount();
				objects[10] = business.getRefuseTaskCount();
				objects[11] = business.getOrderTrackCount();
				objects[12] = business.getOrderCustomerCount();
				objects[13] = business.getSuccessCustomerCount();
				objects[14] = business.getSuccessPotatoCount();
				objects[15] = "0";
			}

			list.add(objects);

		}

		// 设置table列名
		columnNames = new ArrayList<String>();
		columnNames.add("项目");
		columnNames.add("工号");
		columnNames.add("姓名");
		columnNames.add("部门");
		columnNames.add("收接土豆数");
		columnNames.add("呼出土豆数");
		columnNames.add("接通土豆数");
		columnNames.add("未接通土豆数");
		columnNames.add("土豆接通率");
		columnNames.add("未处理土豆数");
		columnNames.add("拒绝数");
		columnNames.add("预约跟踪数");
		columnNames.add("预约开户数");
		columnNames.add("成功开户数");
		columnNames.add("成功土豆数");
		columnNames.add("转化率");

		tableUtil = new ReportTableUtil(list, "业务报表", columnNames, startValue,
				endValue);
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
		operationLog.setDescription("导出业务报表");
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

	// 接收土豆数
	public List<Object[]> projectTaskCount(String start, String end) {

		/*sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and to_char(t1.distributetime,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and to_char(t1.distributetime,'yyyy-MM-dd hh24:mi:ss') <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";*/
		sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.distributetime >= "
				+ "'" + start + "'"
				+ " and t1.distributetime <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";

		return reportService.getMoreRecord(sql);
	}

	// 呼出土豆数
	public List<Object[]> finishedTaskCount(String start, String end) {

		/*sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.isfinished = true and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";*/
		sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.isfinished = true and t1.lastupdatedate >= "
				+ "'" + start + "'"
				+ " and t1.lastupdatedate <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";

		return reportService.getMoreRecord(sql);

	}

	// 接通土豆数
	public List<Object[]> answeredTaskCount(String start, String end) {

		/*sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.isfinished = true and t1.isanswered = true and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";*/
		sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.isfinished = true and t1.isanswered = true and t1.lastupdatedate >= "
				+ "'" + start + "'"
				+ " and t1.lastupdatedate <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";

		return reportService.getMoreRecord(sql);

	}

	// 未接通土豆数
	public List<Object[]> unansweredTaskCount(String start, String end) {

		/*sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.isfinished = true and t1.isanswered = false and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";*/
		sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.isfinished = true and t1.isanswered = false and t1.lastupdatedate >= "
				+ "'" + start + "'"
				+ " and t1.lastupdatedate <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";
		

		return reportService.getMoreRecord(sql);
	}

	// 未处理土豆数
	public List<Object[]> unfinishedTaskCount(String start, String end) {

		/*sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.isfinished = false and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and to_char(t1.lastupdatedate,'yyyy-MM-dd hh24:mi:ss') <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";*/
		sql = "select t1.marketingproject_id,t2.username,count(*)"
				+ " from ec2_marketing_project_task as t1,ec2_user as t2"
				+ " where t1.user_id = t2.id and t1.isfinished = false and t1.lastupdatedate >= "
				+ "'" + start + "'"
				+ " and t1.lastupdatedate <= "
				+ "'" + end + "'"
				+ " group by t1.marketingproject_id,t2.username";

		return reportService.getMoreRecord(sql);
	}

	// 拒绝数
	public List<Object[]> refuseTaskCount(String start, String end) {

		/*sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '拒绝' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where to_char(t2.createdate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and  to_char(t2.createdate,'yyyy-MM-dd hh24') <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";*/
		sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '拒绝' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where t2.createdate >= "
				+ "'" + start + "'"
				+ " and  t2.createdate <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";

		return reportService.getMoreRecord(sql);
	}

	// 预约跟踪数
	public List<Object[]> orderTrackCount(String start, String end) {

		/*sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '预约跟踪' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where to_char(t2.createdate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and  to_char(t2.createdate,'yyyy-MM-dd hh24') <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";*/
		sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '预约跟踪' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where t2.createdate >= "
				+ "'" + start + "'"
				+ " and  t2.createdate <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";

		return reportService.getMoreRecord(sql);
	}

	// 预约开户数
	public List<Object[]> orderCustomerCount(String start, String end) {

		/*sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '预约开户' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where to_char(t2.createdate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and  to_char(t2.createdate,'yyyy-MM-dd hh24') <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";*/
		sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '预约开户' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where t2.createdate >= "
				+ "'" + start + "'"
				+ " and  t2.createdate <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";

		return reportService.getMoreRecord(sql);
	}

	// 成功开户数
	public List<Object[]> successCustomerCount(String start, String end) {
		/*sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '成功开户' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where to_char(t2.createdate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and  to_char(t2.createdate,'yyyy-MM-dd hh24') <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";*/
		sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '成功开户' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where t2.createdate >= "
				+ "'" + start + "'"
				+ " and  t2.createdate <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";

		return reportService.getMoreRecord(sql);
	}

	// 成功土豆数
	public List<Object[]> successPotatoCount(String start, String end) {
		/*sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '成功土豆' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where to_char(t2.createdate,'yyyy-MM-dd hh24:mi:ss') >= "
				+ "'" + start + "'"
				+ " and  to_char(t2.createdate,'yyyy-MM-dd hh24') <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";*/
		sql = "select  t1.marketingproject_id,t3.username,count(*)"
				+ " from ec2_customer_service_record as t1,ec2_customer_service_record_status as t2,ec2_user as t3"
				+ " where t1.servicerecordstatus_id = t2.id and t1.creator_id = t3.id and t2.statusname = '成功土豆' and t1.id in"
				+ " (select max(t2.id) from ec2_customer_service_record as t2 where t2.createdate >= "
				+ "'" + start + "'"
				+ " and  t2.createdate <= " + "'"
				+ end + "'"
				+ " group by t2.marketingproject_id,t2.customerresource_id)"
				+ " group by t1.marketingproject_id,t3.username";

		return reportService.getMoreRecord(sql);
	}

	// 土豆池中的客服记录状态为成功土豆的任务数
	public List<Object[]> cdrConnectCount(String start, String end)
			throws ParseException {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		sql = "select projectid,username,sum(outgoing_connect_count)"
				+ " from ec2_report_call_check" + " where date >= " + "'"
				+ dateFormat.format(dateFormat.parse(start)) + "'"
				+ " and date <= " + "'"
				+ dateFormat.format(dateFormat.parse(end)) + "'"
				+ " group by projectid,username";

		return reportService.getMoreRecord(sql);

	}

	public List<Object[]> getRecord(StringBuffer buffer) {
		String sql = buffer.toString();
		logger.info("sql--> " + sql);
		return reportService.getMoreRecord(sql);
	}
}
