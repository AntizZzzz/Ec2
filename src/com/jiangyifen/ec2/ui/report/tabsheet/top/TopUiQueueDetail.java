package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.QueueDetail;
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
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class TopUiQueueDetail extends VerticalLayout {

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;
	private Label fromLabel;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;
	private ComboBox reportBox;

	private Button seeButton;// 查询
	private Button exportReport;// 导出报表

	private QueueDetail queueDetail;

	private TabSheet tabSheet;

	private Tab tab;

	private String[] dateType = new String[] { "本日", "本周", "本月", "本年" };

	private String[] reportType = new String[] { "按天和时统计", "按天统计", "按月统计" };

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private String titleName;// 传入的报表名
	private File file;

	// 当前时间
	private Date currentDate;
	private SimpleDateFormat format;
	// reportService
	private ReportService reportService = SpringContextHolder
			.getBean("reportService");
	// 登陆的user
	private User loginUser = (User) SpringContextHolder.getHttpSession()
			.getAttribute("loginUser");

	// 登陆用户的domainid
	private String domainId;
	
	private String sql;

	public TopUiQueueDetail(QueueDetail queueDetail) {
		this.queueDetail = queueDetail;

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
		dateComboBox.setNullSelectionAllowed(false);
		dateComboBox.setValue(dateType[0]);// 默认是本日
		horizontalLayout.addComponent(dateComboBox);

		fromLabel = new Label("从");
		horizontalLayout.addComponent(fromLabel);

		startDate = new PopupDateField();
		startDate.setDateFormat("yyyy-MM-dd");
		startDate.setResolution(PopupDateField.RESOLUTION_DAY);
		try {
			startDate.setValue(currentDate);// 默认为本日
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

		reportBox = new ComboBox();
		for (int i = 0; i < reportType.length; i++) {
			reportBox.addItem(reportType[i]);
		}
		reportBox.setWidth("100px");
		reportBox.setImmediate(true);
		reportBox.setNullSelectionAllowed(false);
		reportBox.setValue(reportType[1]);// 默认是按天统计
		horizontalLayout.addComponent(reportBox);

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);
		if (SpringContextHolder.getBusinessModel().contains(
				MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_SEE_QUEUE_DETAIL)) {
			exportReport = new Button("导出报表", this, "exportReport");
		}
		
		horizontalLayout.addComponent(exportReport);

		// dateComboBox的事件
		dateComboBox.addListener(new ComboBox.ValueChangeListener() {

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

		tabSheet = queueDetail.getTabSheet();

		// 登陆用户domainId
		domainId = loginUser.getDomain().getId().toString();

		String reportBoxValue = reportBox.getValue().toString();

		startValue = format.format(startDate.getValue());// 获得startDate的值
		endValue = format.format(endDate.getValue());// 获得endDate的值

		if (reportBoxValue == "按天统计") {

			// 获得数据
			list = getRecordByDay(startValue, endValue);

			// 设置数据格式
			for (Object[] objects : list) {

				// 时长设置xx:xx:xx格式
				objects[4] = DateUtil.getTime(Long.parseLong(objects[4]
						.toString()));

				// 时长设置xx:xx:xx格式
				objects[5] = DateUtil.getTime(Long.parseLong(objects[5]
						.toString()));

			}

			// 设置table列名
			columnNames = new ArrayList<String>();

			columnNames.add("日期");
			columnNames.add("队列名");
			columnNames.add("队列进入次数");
			columnNames.add("队列放弃次数");
			columnNames.add("等待时长(接通)");
			columnNames.add("等待时长(未接通)");

			// 增加一个table
			tab = tabSheet.addTab(new ReportTableUtil(list, "队列详情报表(按天统计)",
					columnNames, startValue, endValue), "(表)按天统计", null);

			tab.setClosable(true);
			tabSheet.setSelectedTab(tab);

		} else if (reportBoxValue == "按天和时统计") {
			list = getRecordByDayHour(startValue, endValue);

			// 把list中的时段格式，改为xx:00~xx:00,如：02:00~03:00
			for (Object[] objects : list) {
				objects[1] = objects[1] + ":00" + "~"
						+ (Integer.parseInt(objects[1].toString()) + 1) + ":00";
			}

			// 设置数据格式
			for (Object[] objects : list) {

				// 时长设置xxx:xx:xx格式
				objects[5] = DateUtil.getTime(Long.parseLong(objects[5]
						.toString()));

				// 时长设置xxx:xx:xx格式
				objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
						.toString()));

			}

			columnNames = new ArrayList<String>();
			columnNames.add("日期");
			columnNames.add("时段");
			columnNames.add("队列名");
			columnNames.add("队列进入次数");
			columnNames.add("队列放弃次数");
			columnNames.add("等待时长(接通)");
			columnNames.add("等待时长(未接通)");

			// 添加一个table
			tab = tabSheet.addTab(new ReportTableUtil(list, "队列详情报表(按天和时统计)",
					columnNames, startValue, endValue), "(表)按天和时统计", null);

			tab.setClosable(true);
			tabSheet.setSelectedTab(tab);

		} else if (reportBoxValue == "按月统计") {
			// 日期精确到月
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
			try {
				Date date = dateFormat.parse(startValue);
				startValue = dateFormat.format(date);
				date = dateFormat.parse(endValue);
				endValue = dateFormat.format(date);
			} catch (ParseException e) {

				e.printStackTrace();
			}

			list = getRecordByMonth(startValue, endValue);

			// 设置数据格式
			for (Object[] objects : list) {

				// 时长设置xxx:xx:xx格式
				objects[4] = DateUtil.getTime(Long.parseLong(objects[4]
						.toString()));

				// 时长设置xxx:xx:xx格式
				objects[5] = DateUtil.getTime(Long.parseLong(objects[5]
						.toString()));

			}

			columnNames = new ArrayList<String>();
			columnNames.add("月份");
			columnNames.add("队列名");
			columnNames.add("队列进入次数");
			columnNames.add("队列放弃次数");
			columnNames.add("等待时长(接通)");
			columnNames.add("等待时长(未接通)");

			tab = tabSheet.addTab(new ReportTableUtil(list, "队列详情报表(按月统计)",
					columnNames, startValue, endValue), "(表)按月统计", null);

			tab.setClosable(true);
			tabSheet.setSelectedTab(tab);

		}

	}

	// 导出报表事件
	public void exportReport(ClickEvent event) throws FileNotFoundException {

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


		//======================chb 操作日志======================//
		// 记录导出日志
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if(file!=null)
			operationLog.setFilePath(file.getAbsolutePath());
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(SpringContextHolder.getLoginUser().getUsername());
		operationLog.setRealName(SpringContextHolder.getLoginUser().getRealName());
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("导出队列详情报表");
		operationLog.setProgrammerSee(sql);
		CommonService commonService=SpringContextHolder.getBean("commonService");
		commonService.save(operationLog);
		//======================chb 操作日志======================//
		
		// 下载file
		Resource resource = new FileResource(file, event.getComponent()
				.getApplication());

		final Embedded downloader = new Embedded();

		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setSource(resource);
		this.addComponent(downloader);

		downloader.addListener(new RepaintRequestListener() {

			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent()))
						.removeComponent(downloader);
			}
		});

	}

	/**
	 * 按天统计
	 * 
	 * @param startValue
	 * @param endValue
	 * @return
	 */
	public List<Object[]> getRecordByDay(String startValue, String endValue) {
		sql= "select date,queuename,into_queue_count,abandon_queue_count,connect_wait_time_length,unconnect_wait_time_length "

				+ "from ec2_report_queue_detail "
				+ "where domainid = "
				+ "'"
				+ domainId
				+ "'"
				+ " and date >= "
				+ "'"
				+ startValue
				+ "'"
				+ " and date <= " + "'" + endValue + "'" + " order by date ";

		return reportService.getMoreRecord(sql);
	}

	public List<Object[]> getRecordByDayHour(String startValue, String endValue) {
		sql = "select date,hour,queuename,into_queue_count,abandon_queue_count,connect_wait_time_length,unconnect_wait_time_length "

				+ "from ec2_report_queue_detail "
				+ "where domainid = "
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
				+ " order by date,hour ";
		return reportService.getMoreRecord(sql);
	}

	public List<Object[]> getRecordByMonth(String startValue, String endValue) {
		/*sql = "select to_char(date,'yyyy-MM'), queuename,into_queue_count,abandon_queue_count,connect_wait_time_length,unconnect_wait_time_length "

				+ "from ec2_report_queue_detail "
				+ "where domainid = "
				+ "'"
				+ domainId
				+ "'"
				+ " and to_char(date,'yyyy-MM') >= "
				+ "'"
				+ startValue
				+ "'"
				+ " and to_char(date,'yyyy-MM') <= "
				+ "'"
				+ endValue + "'" + " order by to_char(date,'yyyy-MM')";*/
		sql = "select to_char(date,'yyyy-MM'), queuename,into_queue_count,abandon_queue_count,connect_wait_time_length,unconnect_wait_time_length from ec2_report_queue_detail where domainid ='"
				+ domainId
				+ "' and date >= '"
				+ startValue
				+ "' and date <= '"
				+ endValue 
				+ "' order by to_char(date,'yyyy-MM')";

		return reportService.getMoreRecord(sql);
	}

	public String getStartValue() {
		return startValue;
	}

	public String getEndValue() {
		return endValue;
	}

}
