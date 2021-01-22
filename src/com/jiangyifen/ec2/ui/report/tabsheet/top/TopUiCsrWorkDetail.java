package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.entity.CsrWorkDetail;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.CsrWork;
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
public class TopUiCsrWorkDetail extends VerticalLayout {

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;
	private Label fromLabel;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;
	/*private ComboBox reportBox;
	private ComboBox comboBox;// 呼叫方向
	private ComboBox seeWay;// 求和或详情*/	
	private Button seeButton;// 查询
	private Button exportReport;// 导出报表

	private CsrWork csrWork;

	private TabSheet tabSheet;

	private Tab tab;

	private String[] dateType = new String[] { "本日", "本周", "本月", "本年" };

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private String titleName;// 传入的报表名
	private File file;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private ReportTableUtil tableUtil;
	/*private int index;*/

	// 当前时间
	private Date currentDate;
	private SimpleDateFormat format;
	// reportService
	private ReportService reportService = SpringContextHolder.getBean("reportService");
	// 登陆的user
	/*private User loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");*/

	// 登陆用户的部门
	// private String deptName;
	// 登陆用户的domainid
	/*private String domainId;*/

	private String sqlByDay;

	public TopUiCsrWorkDetail(CsrWork csrWork) {
		this.csrWork = csrWork;

		// this.setMargin(true);

		currentDate = new Date();
		format = new SimpleDateFormat("yyyy-MM-dd");

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

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");
		if (SpringContextHolder
				.getBusinessModel()
				.contains(
						MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_TELEPHONE_TRAFFIC_CHECK)) {
			horizontalLayout.addComponent(exportReport);
		}

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

		tabSheet = csrWork.getTabSheet();

		// 登陆用户的部门名称
		// deptName = loginUser.getDepartment().getName();

		// 登陆用户domainId
		/*domainId = loginUser.getDomain().getId().toString();*/

		startValue = format.format(startDate.getValue());// 获得startDate的值
		endValue = format.format(endDate.getValue());// 获得endDate的值

		// 获得数据
		List<CsrWorkDetail> csrWorkDetails = new ArrayList<CsrWorkDetail>();
		List<Object[]> list = getBaseInfo(startValue, endValue);
		for (Object[] objects : list) {
			CsrWorkDetail csrWorkDetail = new CsrWorkDetail();

			if (objects[0] != null) {
				csrWorkDetail.setExten(objects[0].toString());
			}
			if (objects[1] != null) {
				csrWorkDetail.setEmpno(objects[1].toString());
			}
			if (objects[2] != null) {
				csrWorkDetail.setName(objects[2].toString());
			}
			if (objects[3] != null) {
				int i = Integer.parseInt(objects[3].toString());
				if (i == 0) {

					csrWorkDetail.setCallDirection("内部");
				} else if (i == 1) {
					csrWorkDetail.setCallDirection("呼出");
				} else if (i == 2) {
					csrWorkDetail.setCallDirection("呼入");
				} else if (i == 3) {
					csrWorkDetail.setCallDirection("外转外");
				}
			}
			if (objects[4] != null) {
				csrWorkDetail.setCallNumber(objects[4].toString());
			}
			if (objects[5] != null) {
				csrWorkDetail.setCalledNumber(objects[5].toString());
			}
			if (objects[6] != null) {
				csrWorkDetail.setRingTimeLength(Integer.parseInt(objects[6]
						.toString()));
			}

			try {
				if (objects[7] != null) {
					csrWorkDetail.setCallDate(dateFormat.format(dateFormat
							.parse(objects[7].toString())));
				}
				if (objects[8] != null) {
					csrWorkDetail.setHungupDate(dateFormat.format(dateFormat
							.parse(objects[8].toString())));
				}

			} catch (ParseException e) {

				e.printStackTrace();
			}

			csrWorkDetails.add(csrWorkDetail);

		}

		List<Object[]> list2 = new ArrayList<Object[]>();
		for (CsrWorkDetail csrWorkDetail : csrWorkDetails) {
			// TODO it is unconvenient to create a constant Object[]
			Object[] objects = new Object[10];
			objects[0] = csrWorkDetail.getExten();
			objects[1] = csrWorkDetail.getEmpno();
			objects[2] = csrWorkDetail.getName();
			objects[3] = csrWorkDetail.getCallDirection();
			objects[4] = csrWorkDetail.getCallNumber();
			objects[5] = csrWorkDetail.getCalledNumber();
			objects[6] = csrWorkDetail.getRingTimeLength();
			objects[7] = csrWorkDetail.getRingDate();
			objects[8] = csrWorkDetail.getCallDate();
			objects[9] = csrWorkDetail.getHungupDate();

			list2.add(objects);
		}

		// list = ReportSum.sum(list, 3, 4, 5, 6, 7);

		// 设置数据格式
		for (Object[] objects : list2) {

			// 时长设置xx:xx:xx格式
			objects[6] = DateUtil
					.getTime(Long.parseLong(objects[6].toString()));

		}

		// 设置table列名
		columnNames = new ArrayList<String>();
		columnNames.add("分机号");
		columnNames.add("员工工号");
		columnNames.add("员工姓名");
		columnNames.add("呼叫方向");
		columnNames.add("主叫号码");
		columnNames.add("被叫号码");
		columnNames.add("振铃时长");
		columnNames.add("振铃时间");
		columnNames.add("通话时间");
		columnNames.add("挂断时间");

		tableUtil = new ReportTableUtil(list2, "座席工作详情", columnNames,
				startValue, endValue);

		tab = tabSheet.addTab(tableUtil, "座席工作详情", null);

		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);

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
		operationLog.setDescription("导出话务考核报表");
		operationLog.setProgrammerSee(sqlByDay);
		CommonService commonService = SpringContextHolder
				.getBean("commonService");
		commonService.save(operationLog);
		// ======================chb 操作日志======================//

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

	public List<Object[]> getBaseInfo(String startValue, String endValue) {

		/*String sql = "select callerid,srcempno,srcrealname,cdrdirection,src,destination,(duration-billableseconds),starttimedate,endtimedate"
				+ " from cdr"
				+ " where to_char(starttimedate,'yyyy-MM-dd') >= "
				+ "'"
				+ startValue
				+ "'"
				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
				+ "'"
				+ endValue + "'";*/
		String sql = "select callerid,srcempno,srcrealname,cdrdirection,src,destination,(duration-billableseconds),starttimedate,endtimedate"
				+ " from cdr"
				+ " where starttimedate >= '"
				+ startValue
				+ "' and starttimedate <= '"
				+ endValue + "'";

		List<Object[]> list = reportService.getMoreRecord(sql);

		return list;

	}

	public String getStartValue() {
		return startValue;
	}

	public String getEndValue() {
		return endValue;
	}

}
