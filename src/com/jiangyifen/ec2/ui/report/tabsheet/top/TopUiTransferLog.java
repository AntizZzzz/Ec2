package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;

import com.jiangyifen.ec2.ui.report.tabsheet.TransferLog;

import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.DateTransformFactory;
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

@SuppressWarnings("serial")
public class TopUiTransferLog extends VerticalLayout {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的

	private Label fromLabel;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;

	private Button seeButton;
	private Button clearButton;
	private Button exportReport;// 导出报表
	private ComboBox usernameBox;

	private TransferLog transferLog;

	private TabSheet tabSheet;

	private Tab tab;

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private String titleName;// 传入的报表名
	private File file;

	private ReportTableUtil tableUtil;
	private int index;

	// 当前时间
	private Date currentDate;
	private SimpleDateFormat format;
	private DateTransformFactory dtf = new DateTransformFactory();
	private ReportService reportService = SpringContextHolder.getBean("reportService");
	// 登陆的user
	/*private User loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");*/

	// private String deptName;
	/*private String domainId;*/

	private String sql;

	public TopUiTransferLog(TransferLog transferLog) {
		this.transferLog = transferLog;

		currentDate = new Date();
		format = new SimpleDateFormat("yyyy-MM-dd");

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		this.addComponent(horizontalLayout);

		fromLabel = new Label("转移日期：");
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

		toLabel = new Label("预约时间：");
		horizontalLayout.addComponent(toLabel);

		endDate = new PopupDateField();
		endDate.setDateFormat("yyyy-MM-dd");
		endDate.setResolution(PopupDateField.RESOLUTION_DAY);
		endDate.setValue(currentDate);
		horizontalLayout.addComponent(endDate);

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);

		clearButton = new Button("重选");
		clearButton.addListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {

				startDate.setValue(null);
				endDate.setValue(null);

			}
		});
		horizontalLayout.addComponent(clearButton);

		exportReport = new Button("导出报表", this, "exportReport");

		if (SpringContextHolder.getBusinessModel().contains(
				MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_KPI_DETAIL)) {
			horizontalLayout.addComponent(exportReport);
		}

		usernameBox = new ComboBox();
		usernameBox.setWidth("100px");
		usernameBox.setImmediate(true);
		usernameBox.setInputPrompt("源项目名称");
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

	}

	public void seeResult(ClickEvent event) {

		tabSheet = transferLog.getTabSheet();

		// 获得登陆用户的部门名称
		// deptName = loginUser.getDepartment().getName();

		// 登陆用户的domainId
		/*domainId = loginUser.getDomain().getId().toString();*/

		String sql = "";

		StringBuffer sqlBuffer = new StringBuffer(
				"select customerresourceid,customername,phoneno,operatorid,operatorname,operatorusername,operatordeptid,operatordeptname,fromprojectid,fromprojectname,toprojectid,toprojectname,operatortime,description,ordertime,ordernote from ec2_workflow_transfer_log where");

		if (startDate.getValue() != null) {
			/*sqlBuffer.append(" to_char(operatortime,'yyyy-MM-dd') = " + "'"
					+ format.format(startDate.getValue()) + "'" + " and");*/
			sqlBuffer.append(" operatortime >= '"
					+ format.format(startDate.getValue()) + "' and operatortime < '"+dtf.getSpecifiedDayAfter(format.format(startDate.getValue()), "yyyy-MM-dd") + "' and ");
		}

		if (endDate.getValue() != null) {
			/*sqlBuffer.append(" to_char(ordertime,'yyyy-MM-dd') = " + "'"
					+ format.format(endDate.getValue()) + "'" + " and");*/
			sqlBuffer.append(" ordertime >= '"
					+ format.format(endDate.getValue()) + "' and ordertime < '"+dtf.getSpecifiedDayAfter(format.format(endDate.getValue()), "yyyy-MM-dd") + "' and ");
		}

		// get sql
		if (sqlBuffer.toString().endsWith("and")) {
			sql = sqlBuffer.toString().substring(0,
					sqlBuffer.toString().length() - 3);
		} else if (sqlBuffer.toString().endsWith("where")) {
			sql = sqlBuffer.toString().substring(0,
					sqlBuffer.toString().length() - 5);
		}

		// 获得数据
		list = getRecordByDay(sql);

		// 设置table列名
		columnNames = new ArrayList<String>();
		columnNames.add("客户id");
		columnNames.add("客户姓名");
		columnNames.add("客户电话");
		columnNames.add("话务员id");
		columnNames.add("话务员姓名");
		columnNames.add("话务员用户名");
		columnNames.add("操作者部门id");
		columnNames.add("操作者部门名称");
		columnNames.add("源项目id");
		columnNames.add("源项目名称");
		columnNames.add("目标项目id");
		columnNames.add("目标项目名称");
		columnNames.add("转移日期");
		columnNames.add("操作描述");
		columnNames.add("预约时间 ");
		columnNames.add("预约备注");

		tableUtil = new ReportTableUtil(list, "转移日志报表", columnNames,
				startValue, endValue);
		tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);

		// 获得工号所在列的下标
		List<String> names = tableUtil.getNames();
		index = names.indexOf("源项目名称");

		usernameBox.removeAllItems();

		for (Object[] objects : list) {
			usernameBox.addItem(objects[index]);
		}

	}

	// 导出报表事件
	public void exportReport(ClickEvent event) {

		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询");
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
		operationLog.setDescription("转移日志报表");
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

	public List<Object[]> getRecordByDay(String sql) {

		logger.info("sql-----> " + sql);

		return reportService.getMoreRecord(sql);
	}
}
