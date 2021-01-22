//package com.jiangyifen.ec2.ui.report.tabsheet.top;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import com.jiangyifen.ec2.entity.OperationLog;
//import com.jiangyifen.ec2.entity.User;
//import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
//import com.jiangyifen.ec2.service.common.CommonService;
//import com.jiangyifen.ec2.service.mgr.ReportService;
//import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
//import com.jiangyifen.ec2.ui.report.tabsheet.CallCheck;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.NumberFormat;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportSum;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
//import com.jiangyifen.ec2.utils.SpringContextHolder;
//import com.vaadin.data.Property.ValueChangeEvent;
//import com.vaadin.terminal.FileResource;
//import com.vaadin.terminal.Resource;
//import com.vaadin.terminal.gwt.server.WebApplicationContext;
//import com.vaadin.ui.Button;
//import com.vaadin.ui.Button.ClickEvent;
//import com.vaadin.ui.ComboBox;
//import com.vaadin.ui.Embedded;
//import com.vaadin.ui.HorizontalLayout;
//import com.vaadin.ui.Label;
//import com.vaadin.ui.PopupDateField;
//import com.vaadin.ui.TabSheet;
//import com.vaadin.ui.TabSheet.Tab;
//import com.vaadin.ui.Table;
//import com.vaadin.ui.VerticalLayout;
//
//@SuppressWarnings("serial")
//public class TopUiCallCheck_bak_old extends VerticalLayout {
//
//	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
//	private ComboBox dateComboBox;
//	private Label fromLabel;
//	private PopupDateField startDate;
//	private Label toLabel;
//	private PopupDateField endDate;
//	private ComboBox reportBox;
//	private ComboBox comboBox;// 呼叫方向
//	private ComboBox seeWay;// 求和或详情
//	private Button seeButton;// 查询
//	private Button exportReport;// 导出报表
//	private ComboBox usernameBox;
//
//	private CallCheck callCheck;
//
//	private TabSheet tabSheet;
//
//	private Tab tab;
//
//	private String[] dateType = new String[] { "本日", "本周", "本月", "本年" };
//
//	private String[] reportType = new String[] { "按天和时统计", "按天统计", "按月统计" };
//
//	private String[] callDirection = new String[] { "内部", "呼出", "呼入", "外传外",
//			"全部" };
//
//	private String[] sumAndDetail = new String[] { "求和", "详情" };
//
//	private List<Object[]> list;// 从数据库获得新的数据
//
//	private String startValue;// startDate的值
//	private String endValue;// endDate的值
//
//	private List<String> columnNames;// table的列名
//	private String titleName;// 传入的报表名
//	private File file;
//
//	private ReportTableUtil tableUtil;
//	private int index;
//
//	// 当前时间
//	private Date currentDate;
//	private SimpleDateFormat format;
//	// reportService
//	private ReportService reportService = SpringContextHolder
//			.getBean("reportService");
//	// 登陆的user
//	private User loginUser = (User) SpringContextHolder.getHttpSession()
//			.getAttribute("loginUser");
//
//	// 登陆用户的部门
//	// private String deptName;
//	// 登陆用户的domainid
//	private String domainId;
//
//	private String sqlByDay;
//
//	public TopUiCallCheck_bak_old(CallCheck callCheck) {
//		this.callCheck = callCheck;
//
//		// this.setMargin(true);
//
//		currentDate = new Date();
//		format = new SimpleDateFormat("yyyy-MM-dd");
//
//		horizontalLayout = new HorizontalLayout();
//		horizontalLayout.setSpacing(true);
//		horizontalLayout.setMargin(true);
//		this.addComponent(horizontalLayout);
//
//		dateComboBox = new ComboBox();
//		for (int i = 0; i < dateType.length; i++) {
//			dateComboBox.addItem(dateType[i]);
//		}
//		dateComboBox.setWidth("55px");
//		dateComboBox.setImmediate(true);
//		dateComboBox.setNullSelectionAllowed(false);
//		dateComboBox.setValue(dateType[0]);// 默认是本日
//		horizontalLayout.addComponent(dateComboBox);
//
//		fromLabel = new Label("从");
//		horizontalLayout.addComponent(fromLabel);
//
//		startDate = new PopupDateField();
//		startDate.setDateFormat("yyyy-MM-dd");
//		startDate.setResolution(PopupDateField.RESOLUTION_DAY);
//		try {
//			startDate.setValue(currentDate);// 默认为本日
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		horizontalLayout.addComponent(startDate);
//
//		toLabel = new Label("至");
//		horizontalLayout.addComponent(toLabel);
//
//		endDate = new PopupDateField();
//		endDate.setDateFormat("yyyy-MM-dd");
//		endDate.setResolution(PopupDateField.RESOLUTION_DAY);
//		endDate.setValue(currentDate);
//		horizontalLayout.addComponent(endDate);
//
//		comboBox = new ComboBox();
//		for (int i = 0; i < callDirection.length; i++) {
//			comboBox.addItem(callDirection[i]);
//		}
//		comboBox.setWidth("55px");
//		comboBox.setImmediate(true);
//		comboBox.setNullSelectionAllowed(false);
//		comboBox.setValue(callDirection[1]);// 默认是呼出
//		horizontalLayout.addComponent(comboBox);
//
//		seeWay = new ComboBox();
//		for (int i = 0; i < sumAndDetail.length; i++) {
//			seeWay.addItem(sumAndDetail[i]);
//		}
//		seeWay.setWidth("55px");
//		seeWay.setImmediate(true);
//		seeWay.setNullSelectionAllowed(false);
//		seeWay.setValue(sumAndDetail[0]);// 默认是求和
//		horizontalLayout.addComponent(seeWay);
//
//		reportBox = new ComboBox();
//		for (int i = 0; i < reportType.length; i++) {
//			reportBox.addItem(reportType[i]);
//		}
//		reportBox.setVisible(false);
//
//		reportBox.setWidth("100px");
//		reportBox.setImmediate(true);
//		reportBox.setNullSelectionAllowed(false);
//		reportBox.setValue(reportType[1]);// 默认是按天统计
//		horizontalLayout.addComponent(reportBox);
//
//		seeButton = new Button("查询", this, "seeResult");
//		horizontalLayout.addComponent(seeButton);
//
//		exportReport = new Button("导出报表", this, "exportReport");
//		if (SpringContextHolder
//				.getBusinessModel()
//				.contains(
//						MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_TELEPHONE_TRAFFIC_CHECK)) {
//			horizontalLayout.addComponent(exportReport);
//		}
//
//		usernameBox = new ComboBox();
//		usernameBox.setWidth("100px");
//		usernameBox.setImmediate(true);
//		usernameBox.setInputPrompt("------工号------");
//		horizontalLayout.addComponent(usernameBox);
//
//		usernameBox.addListener(new ComboBox.ValueChangeListener() {
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//
//				if (event.getProperty().getValue() == null) {
//					return;
//				}
//
//				String value = event.getProperty().getValue().toString();
//
//				Table table = tableUtil.getTable();
//				table.removeAllItems();
//
//				for (Object[] objects : list) {
//
//					if (objects[index].equals(value)) {
//
//						table.addItem(objects, null);
//					}
//				}
//
//			}
//		});
//
//		// dateComboBox的事件
//		dateComboBox.addListener(new ComboBox.ValueChangeListener() {
//
//			public void valueChange(ValueChangeEvent event) {
//
//				try {
//					if (event.getProperty().toString().equals("本日")) {
//
//						startDate.setValue(currentDate);
//						endDate.setValue(currentDate);
//					} else if (event.getProperty().toString().equals("本周")) {
//
//						startDate.setValue(format.parse(DateUtil
//								.getFirstDayWeek()));
//						endDate.setValue(currentDate);
//
//					} else if (event.getProperty().toString().equals("本月")) {
//
//						startDate.setValue(format.parse(DateUtil
//								.getFirstDayMonth()));
//						endDate.setValue(currentDate);
//
//					} else if (event.getProperty().toString().equals("本年")) {
//						startDate.setValue(format.parse(DateUtil
//								.getFirstDayYear()));
//						endDate.setValue(currentDate);
//
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		});
//
//		seeWay.addListener(new ComboBox.ValueChangeListener() {
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//
//				String str = event.getProperty().toString();
//
//				if (str.equals("详情")) {
//
//					reportBox.setVisible(true);
//				} else {
//					reportBox.setVisible(false);
//				}
//			}
//		});
//
//	}
//
//	public void seeResult(ClickEvent event) {
//
//		tabSheet = callCheck.getTabSheet();
//
//		// 登陆用户的部门名称
//		// deptName = loginUser.getDepartment().getName();
//
//		// 登陆用户domainId
//		domainId = loginUser.getDomain().getId().toString();
//
//		String reportBoxValue = reportBox.getValue().toString();
//
//		String callDirectionValue = comboBox.getValue().toString();
//
//		String sumAndDetail = seeWay.getValue().toString();
//
//		startValue = format.format(startDate.getValue());// 获得startDate的值
//		endValue = format.format(endDate.getValue());// 获得endDate的值
//
//		if (sumAndDetail.equals("求和")) {
//
//			// 呼入
//			if (callDirectionValue.equals("内部")) {
//
//				// 获得数据
//				list = get_e_to_e_Sum_RecordByDay(startValue, endValue);
//
//				list = ReportSum.sum(list, 3, 4, 5, 6, 7);
//
//				// 设置数据格式
//				for (Object[] objects : list) {
//
//					// 时长设置xx:xx:xx格式
//					objects[5] = DateUtil.getTime(Long.parseLong(objects[5]
//							.toString()));
//
//					// 时长设置xx:xx:xx格式
//					objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//							.toString()));
//
//				}
//
//				// 设置table列名
//				columnNames = new ArrayList<String>();
//				columnNames.add("部门");
//				columnNames.add("工号");
//				columnNames.add("姓名");
//				columnNames.add("呼叫总数(内部)");
//				columnNames.add("接通数量(内部)");
//				columnNames.add("接通时长(内部)");
//				columnNames.add("未接通数量(内部)");
//				columnNames.add("未接通时长(内部)");
//
//				tableUtil = new ReportTableUtil(list, "话务考核报表(内部:按天统计)",
//						columnNames, startValue, endValue);
//
//				tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//				tab.setClosable(true);
//				tabSheet.setSelectedTab(tab);
//
//			} else if (callDirectionValue.equals("呼出")) {
//
//				// 获得数据
//				list = getOutgoingSumRecordByDay(startValue, endValue);
//
//				list = ReportSum.sum(list, 3, 4, 5, 6, 7);
//
//				// 设置数据格式
//				for (Object[] objects : list) {
//
//					objects[3] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[3].toString()));
//					objects[4] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[4].toString()));
//					objects[6] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[6].toString()));
//
//					// 时长设置xxx:xx:xx格式
//					objects[5] = DateUtil.getTime(Long.parseLong(objects[5]
//							.toString()));
//
//					// 时长设置xxx:xx:xx格式
//					objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//							.toString()));
//
//				}
//
//				// 设置table列名
//				columnNames = new ArrayList<String>();
//				columnNames.add("部门");
//				columnNames.add("工号");
//				columnNames.add("姓名");
//				columnNames.add("呼叫总数(呼出)");
//				columnNames.add("接通数量(呼出)");
//				columnNames.add("接通时长(呼出)");
//				columnNames.add("未接通数量(呼出)");
//				columnNames.add("未接通时长(呼出)");
//
//				tableUtil = new ReportTableUtil(list, "话务考核报表(呼出:按天统计)",
//						columnNames, startValue, endValue);
//				// 增加一个table
//				tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//				tab.setClosable(true);
//				tabSheet.setSelectedTab(tab);
//
//			} else if (callDirectionValue.equals("呼入")) {
//
//				// 获得数据
//				list = getIncomingSumRecordByDay(startValue, endValue);
//
//				list = ReportSum.sum(list, 3, 4, 5, 6, 7);
//
//				// 设置数据格式
//				for (Object[] objects : list) {
//
//					objects[3] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[3].toString()));
//					objects[4] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[4].toString()));
//					objects[6] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[6].toString()));
//
//					// 时长设置xxx:xx:xx格式
//					objects[5] = DateUtil.getTime(Long.parseLong(objects[5]
//							.toString()));
//
//					// 时长设置xxx:xx:xx格式
//					objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//							.toString()));
//
//				}
//
//				// 设置table列名
//				columnNames = new ArrayList<String>();
//				columnNames.add("部门");
//				columnNames.add("工号");
//				columnNames.add("姓名");
//				columnNames.add("呼叫总数(呼入)");
//				columnNames.add("接通数量(呼入)");
//				columnNames.add("接通时长(呼入)");
//				columnNames.add("未接通数量(呼入)");
//				columnNames.add("未接通时长(呼入)");
//
//				tableUtil = new ReportTableUtil(list, "话务考核报表(呼入:按天统计)",
//						columnNames, startValue, endValue);
//
//				tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//				tab.setClosable(true);
//				tabSheet.setSelectedTab(tab);
//
//			} else if (callDirectionValue.equals("外传外")) {
//
//				// 获得数据
//				list = get_o_to_o_sum_RecordByDay(startValue, endValue);
//				list = ReportSum.sum(list, 3, 4, 5, 6, 7);
//
//				// 设置数据格式
//				for (Object[] objects : list) {
//
//					// 时长设置xxx:xx:xx格式
//					objects[5] = DateUtil.getTime(Long.parseLong(objects[5]
//							.toString()));
//
//					// 时长设置xxx:xx:xx格式
//					objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//							.toString()));
//
//				}
//
//				// 设置table列名
//				columnNames = new ArrayList<String>();
//				columnNames.add("部门");
//				columnNames.add("工号");
//				columnNames.add("姓名");
//				columnNames.add("呼叫总数(外传外)");
//				columnNames.add("接通数量(外传外)");
//				columnNames.add("接通时长(外传外)");
//				columnNames.add("未接通数量(外传外)");
//				columnNames.add("未接通时长(外传外)");
//
//				tableUtil = new ReportTableUtil(list, "话务考核报表(外传外:按天统计)",
//						columnNames, startValue, endValue);
//
//				tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//				tab.setClosable(true);
//				tabSheet.setSelectedTab(tab);
//
//			} else if (callDirectionValue.equals("全部")) {
//				// 获得数据
//				list = get_call_sum_RecordByDay(startValue, endValue);
//
//				// TODO
//				for (Object[] objects : list) {
//					objects[3] = Integer.parseInt(objects[3].toString());
//				}
//				list = ReportSum.sum(list, 3, 4, 5, 6, 7);
//
//				// 设置数据格式
//				for (Object[] objects : list) {
//
//					objects[3] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[3].toString()));
//					objects[4] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[4].toString()));
//					objects[6] = NumberFormat.numberFormat(Integer
//							.parseInt(objects[6].toString()));
//					// 时长设置xxx:xx:xx格式
//					objects[5] = DateUtil.getTime(Long.parseLong(objects[5]
//							.toString()));
//
//					// 时长设置xxx:xx:xx格式
//					objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//							.toString()));
//
//				}
//
//				// 设置table列名
//				columnNames = new ArrayList<String>();
//				columnNames.add("部门");
//				columnNames.add("工号");
//				columnNames.add("姓名");
//				columnNames.add("呼叫总数(全部)");
//				columnNames.add("接通数量(全部)");
//				columnNames.add("接通时长(全部)");
//				columnNames.add("未接通数量(全部)");
//				columnNames.add("未接通时长(全部)");
//
//				tableUtil = new ReportTableUtil(list, "话务考核报表(全部:按天统计)",
//						columnNames, startValue, endValue);
//
//				tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//				tab.setClosable(true);
//				tabSheet.setSelectedTab(tab);
//			}
//
//			// TODO
//
//		} else if (sumAndDetail.equals("详情")) {
//
//			// 呼入
//			if (callDirectionValue.equals("内部")) {
//
//				if (reportBoxValue == "按天统计") {
//
//					// 获得数据
//					list = get_e_to_e_RecordByDay(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						// 时长设置xx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					// 设置table列名
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("呼叫总数(内部)");
//					columnNames.add("接通数量(内部)");
//					columnNames.add("接通时长(内部)");
//					columnNames.add("未接通数量(内部)");
//					columnNames.add("未接通时长(内部)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(内部:按天统计)",
//							columnNames, startValue, endValue);
//
//					tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按天和时统计") {
//					list = get_e_to_e_RecordByDayHour(startValue, endValue);
//
//					// list = ReportSum.sum(list, 5, 6, 7, 8, 9);
//
//					// 把list中的时段格式，改为xx:00~xx:00,如：02:00~03:00
//					for (Object[] objects : list) {
//						objects[4] = objects[4] + ":00" + "~"
//								+ (Integer.parseInt(objects[4].toString()) + 1)
//								+ ":00";
//					}
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						// 时长设置xxx:xx:xx格式
//						objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[9] = DateUtil.getTime(Long.parseLong(objects[9]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("时段");
//					columnNames.add("呼叫总数(内部)");
//					columnNames.add("接通数量(内部)");
//					columnNames.add("接通时长(内部)");
//					columnNames.add("未接通数量(内部)");
//					columnNames.add("未接通时长(内部)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(内部:按天和时统计)",
//							columnNames, startValue, endValue);
//
//					tab = tabSheet.addTab(tableUtil, "(表)按天和时统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按月统计") {
//					// 日期精确到月
//					SimpleDateFormat dateFormat = new SimpleDateFormat(
//							"yyyy-MM");
//					try {
//						Date date = dateFormat.parse(startValue);
//						startValue = dateFormat.format(date);
//						date = dateFormat.parse(endValue);
//						endValue = dateFormat.format(date);
//					} catch (ParseException e) {
//
//						e.printStackTrace();
//					}
//
//					list = get_e_to_e_RecordByMonth(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("月份");
//					columnNames.add("呼叫总数(内部)");
//					columnNames.add("接通数量(内部)");
//					columnNames.add("接通时长(内部)");
//					columnNames.add("未接通数量(内部)");
//					columnNames.add("未接通时长(内部)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(内部:按月统计)",
//							columnNames, startValue, endValue);
//
//					tab = tabSheet.addTab(tableUtil, "(表)按月统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				}
//
//			} else if (callDirectionValue.equals("呼出")) {
//
//				// 呼出
//				if (reportBoxValue == "按天统计") {
//
//					// 获得数据
//					list = getOutgoingRecordByDay(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[4] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[4].toString()));
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[7] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[7].toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					// 设置table列名
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("呼叫总数(呼出)");
//					columnNames.add("接通数量(呼出)");
//					columnNames.add("接通时长(呼出)");
//					columnNames.add("未接通数量(呼出)");
//					columnNames.add("未接通时长(呼出)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(呼出:按天统计)",
//							columnNames, startValue, endValue);
//
//					tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按天和时统计") {
//					list = getOutgoingRecordByDayHour(startValue, endValue);
//
//					// list = ReportSum.sum(list, 5, 6, 7, 8, 9);
//
//					// 把list中的时段格式，改为xx:00~xx:00,如：02:00~03:00
//
//					for (int i = 0; i < list.size() - 1; i++) {
//
//						Object[] objects = list.get(i);
//
//						objects[4] = objects[4] + ":00" + "~"
//								+ (Integer.parseInt(objects[4].toString()) + 1)
//								+ ":00";
//
//					}
//					// for (Object[] objects : list) {
//					// objects[4] = objects[4] + ":00" + "~"
//					// + (Integer.parseInt(objects[4].toString()) + 1)
//					// + ":00";
//					// }
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[6] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[6].toString()));
//						objects[8] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[8].toString()));
//						// 时长设置xxx:xx:xx格式
//						objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[9] = DateUtil.getTime(Long.parseLong(objects[9]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("时段");
//					columnNames.add("呼叫总数(呼出)");
//					columnNames.add("接通数量(呼出)");
//					columnNames.add("接通时长(呼出)");
//					columnNames.add("未接通数量(呼出)");
//					columnNames.add("未接通时长(呼出)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(呼出:按天和时统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按天和时统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按月统计") {
//
//					// 日期精确到月
//					SimpleDateFormat dateFormat = new SimpleDateFormat(
//							"yyyy-MM");
//					try {
//						Date date = dateFormat.parse(startValue);
//						startValue = dateFormat.format(date);
//						date = dateFormat.parse(endValue);
//						endValue = dateFormat.format(date);
//					} catch (ParseException e) {
//
//						e.printStackTrace();
//					}
//					list = getOutgoingRecordByMonth(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[4] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[4].toString()));
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[7] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[7].toString()));
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("月份");
//					columnNames.add("呼叫总数(呼出)");
//					columnNames.add("接通数量(呼出)");
//					columnNames.add("接通时长(呼出)");
//					columnNames.add("未接通数量(呼出)");
//					columnNames.add("未接通时长(呼出)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(呼出:按月统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按月统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				}
//
//			} else if (callDirectionValue.equals("呼入")) {
//
//				if (reportBoxValue == "按天统计") {
//
//					// 获得数据
//					list = getIncomingRecordByDay(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[4] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[4].toString()));
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[7] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[7].toString()));
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					// 设置table列名
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("呼叫总数(呼入)");
//					columnNames.add("接通数量(呼入)");
//					columnNames.add("接通时长(呼入)");
//					columnNames.add("未接通数量(呼入)");
//					columnNames.add("未接通时长(呼入)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(呼入:按天统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按天和时统计") {
//					list = getIncomingRecordByDayHour(startValue, endValue);
//
//					// list = ReportSum.sum(list, 5, 6, 7, 8, 9);
//
//					// 把list中的时段格式，改为xx:00~xx:00,如：02:00~03:00
//					for (Object[] objects : list) {
//						objects[4] = objects[4] + ":00" + "~"
//								+ (Integer.parseInt(objects[4].toString()) + 1)
//								+ ":00";
//					}
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[6] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[6].toString()));
//						objects[8] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[8].toString()));
//						// 时长设置xxx:xx:xx格式
//						objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[9] = DateUtil.getTime(Long.parseLong(objects[9]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("时段");
//					columnNames.add("呼叫总数(呼入)");
//					columnNames.add("接通数量(呼入)");
//					columnNames.add("接通时长(呼入)");
//					columnNames.add("未接通数量(呼入)");
//					columnNames.add("未接通时长(呼入)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(呼入:按天和时统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按天和时统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按月统计") {
//
//					// 日期精确到月
//					SimpleDateFormat dateFormat = new SimpleDateFormat(
//							"yyyy-MM");
//					try {
//						Date date = dateFormat.parse(startValue);
//						startValue = dateFormat.format(date);
//						date = dateFormat.parse(endValue);
//						endValue = dateFormat.format(date);
//					} catch (ParseException e) {
//
//						e.printStackTrace();
//					}
//					list = getIncomingRecordByMonth(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[4] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[4].toString()));
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[7] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[7].toString()));
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("月份");
//					columnNames.add("呼叫总数(呼入)");
//					columnNames.add("接通数量(呼入)");
//					columnNames.add("接通时长(呼入)");
//					columnNames.add("未接通数量(呼入)");
//					columnNames.add("未接通时长(呼入)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(呼入:按月统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按月统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				}
//
//			} else if (callDirectionValue.equals("全部")) {
//				// TODO
//				if (reportBoxValue == "按天统计") {
//
//					// 获得数据
//					list = get_call_RecordByDay(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[4] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[4].toString()));
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[7] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[7].toString()));
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					// 设置table列名
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("呼叫总数(全部)");
//					columnNames.add("接通数量(全部)");
//					columnNames.add("接通时长(全部)");
//					columnNames.add("未接通数量(全部)");
//					columnNames.add("未接通时长(全部)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(全部:按天统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按天和时统计") {
//					list = getCallRecordByDayHour(startValue, endValue);
//
//					// list = ReportSum.sum(list, 5, 6, 7, 8, 9);
//
//					// 把list中的时段格式，改为xx:00~xx:00,如：02:00~03:00
//					for (Object[] objects : list) {
//						objects[4] = objects[4] + ":00" + "~"
//								+ (Integer.parseInt(objects[4].toString()) + 1)
//								+ ":00";
//
//					}
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[6] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[6].toString()));
//						objects[8] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[8].toString()));
//						// 时长设置xxx:xx:xx格式
//						objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[9] = DateUtil.getTime(Long.parseLong(objects[9]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("时段");
//					columnNames.add("呼叫总数(全部)");
//					columnNames.add("接通数量(全部)");
//					columnNames.add("接通时长(全部)");
//					columnNames.add("未接通数量(全部)");
//					columnNames.add("未接通时长(全部)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(全部:按天和时统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按天和时统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按月统计") {
//					// 日期精确到月
//					SimpleDateFormat dateFormat = new SimpleDateFormat(
//							"yyyy-MM");
//					try {
//						Date date = dateFormat.parse(startValue);
//						startValue = dateFormat.format(date);
//						date = dateFormat.parse(endValue);
//						endValue = dateFormat.format(date);
//					} catch (ParseException e) {
//
//						e.printStackTrace();
//					}
//
//					list = getCallRecordByMonth(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						objects[4] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[4].toString()));
//						objects[5] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[5].toString()));
//						objects[7] = NumberFormat.numberFormat(Integer
//								.parseInt(objects[7].toString()));
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("月份");
//					columnNames.add("呼叫总数(全部)");
//					columnNames.add("接通数量(全部)");
//					columnNames.add("接通时长(全部)");
//					columnNames.add("未接通数量(全部)");
//					columnNames.add("未接通时长(全部)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(外传外:按月统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按月统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				}
//
//			} else if (callDirectionValue.equals("外传外")) {
//
//				if (reportBoxValue == "按天统计") {
//
//					// 获得数据
//					list = get_o_to_o_RecordByDay(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					// 设置table列名
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("呼叫总数(外传外)");
//					columnNames.add("接通数量(外传外)");
//					columnNames.add("接通时长(外传外)");
//					columnNames.add("未接通数量(外传外)");
//					columnNames.add("未接通时长(外传外)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(外传外:按天统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按天和时统计") {
//					list = get_o_to_o_RecordByDayHour(startValue, endValue);
//
//					// list = ReportSum.sum(list, 5, 6, 7, 8, 9);
//
//					// 把list中的时段格式，改为xx:00~xx:00,如：02:00~03:00
//					for (Object[] objects : list) {
//						objects[4] = objects[4] + ":00" + "~"
//								+ (Integer.parseInt(objects[4].toString()) + 1)
//								+ ":00";
//					}
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						// 时长设置xxx:xx:xx格式
//						objects[7] = DateUtil.getTime(Long.parseLong(objects[7]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[9] = DateUtil.getTime(Long.parseLong(objects[9]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("日期");
//					columnNames.add("时段");
//					columnNames.add("呼叫总数(外传外)");
//					columnNames.add("接通数量(外传外)");
//					columnNames.add("接通时长(外传外)");
//					columnNames.add("未接通数量(外传外)");
//					columnNames.add("未接通时长(外传外)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(外传外:按天和时统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按天和时统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				} else if (reportBoxValue == "按月统计") {
//					// 日期精确到月
//					SimpleDateFormat dateFormat = new SimpleDateFormat(
//							"yyyy-MM");
//					try {
//						Date date = dateFormat.parse(startValue);
//						startValue = dateFormat.format(date);
//						date = dateFormat.parse(endValue);
//						endValue = dateFormat.format(date);
//					} catch (ParseException e) {
//
//						e.printStackTrace();
//					}
//
//					list = get_o_to_o_RecordByMonth(startValue, endValue);
//
//					list = ReportSum.sum(list, 4, 5, 6, 7, 8);
//
//					// 设置数据格式
//					for (Object[] objects : list) {
//
//						// 时长设置xxx:xx:xx格式
//						objects[6] = DateUtil.getTime(Long.parseLong(objects[6]
//								.toString()));
//
//						// 时长设置xxx:xx:xx格式
//						objects[8] = DateUtil.getTime(Long.parseLong(objects[8]
//								.toString()));
//
//					}
//
//					columnNames = new ArrayList<String>();
//					columnNames.add("部门");
//					columnNames.add("工号");
//					columnNames.add("姓名");
//					columnNames.add("月份");
//					columnNames.add("呼叫总数(外传外)");
//					columnNames.add("接通数量(外传外)");
//					columnNames.add("接通时长(外传外)");
//					columnNames.add("未接通数量(外传外)");
//					columnNames.add("未接通时长(外传外)");
//
//					tableUtil = new ReportTableUtil(list, "话务考核报表(外传外:按月统计)",
//							columnNames, startValue, endValue);
//					tab = tabSheet.addTab(tableUtil, "(表)按月统计", null);
//
//					tab.setClosable(true);
//					tabSheet.setSelectedTab(tab);
//
//				}
//			}
//		}
//
//		// 获得工号所在列的下标
//		List<String> names = tableUtil.getNames();
//		index = names.indexOf("工号");
//
//		usernameBox.removeAllItems();
//
//		for (Object[] objects : list) {
//
//			if (objects[index] != null) {
//
//				usernameBox.addItem(objects[index]);
//			}
//
//		}
//	}
//
//	// 导出报表事件
//	public void exportReport(ClickEvent event) throws FileNotFoundException {
//
//		if (tabSheet == null) {
//			NotificationUtil.showWarningNotification(this, "请先查询");
//			return;
//		}
//
//		// 得到当前tab的组件，把当前tab页的数据导出报表
//		ReportTableUtil reportTableUtil = (ReportTableUtil) tabSheet
//				.getSelectedTab();
//
//		// 如果reportTableUtil为null提示请点击查询
//		if (reportTableUtil == null) {
//			NotificationUtil.showWarningNotification(this, "请先查询");
//			return;
//		}
//
//		// 设置文件名
//		titleName = reportTableUtil.getTitleName();
//		// 设置数据源
//		list = reportTableUtil.getList();
//		// 设置列名
//		columnNames = reportTableUtil.getNames();
//		// 开时时间
//		startValue = reportTableUtil.getStartValue();
//		// 结束时间
//		endValue = reportTableUtil.getEndValue();
//
//		// 生成excel file
//		file = ExportExcelUtil.exportExcel(titleName, list, columnNames,
//				startValue, endValue);
//
//		// ======================chb 操作日志======================//
//		// 记录导出日志
//		OperationLog operationLog = new OperationLog();
//		operationLog.setDomain(SpringContextHolder.getDomain());
//		if (file != null)
//			operationLog.setFilePath(file.getAbsolutePath());
//		operationLog.setOperateDate(new Date());
//		operationLog.setOperationStatus(OperationStatus.EXPORT);
//		operationLog.setUsername(SpringContextHolder.getLoginUser()
//				.getUsername());
//		operationLog.setRealName(SpringContextHolder.getLoginUser()
//				.getRealName());
//		WebApplicationContext context = (WebApplicationContext) this
//				.getApplication().getContext();
//		String ip = context.getBrowser().getAddress();
//		operationLog.setIp(ip);
//		operationLog.setDescription("导出话务考核报表");
//		operationLog.setProgrammerSee(sqlByDay);
//		CommonService commonService = SpringContextHolder
//				.getBean("commonService");
//		commonService.save(operationLog);
//		// ======================chb 操作日志======================//
//
//		// 下载file
//		Resource resource = new FileResource(file, event.getComponent()
//				.getApplication());
//
//		final Embedded downloader = new Embedded();
//
//		downloader.setType(Embedded.TYPE_BROWSER);
//		downloader.setWidth("0px");
//		downloader.setHeight("0px");
//		downloader.setSource(resource);
//		this.addComponent(downloader);
//
//		downloader.addListener(new RepaintRequestListener() {
//
//			public void repaintRequested(RepaintRequestEvent event) {
//				((VerticalLayout) (downloader.getParent()))
//						.removeComponent(downloader);
//			}
//		});
//
//	}
//
//	// 内部:按天统计
//	public List<Object[]> get_e_to_e_RecordByDay(String startValue,
//			String endValue) {
//		sqlByDay = "select deptname,username,realname,date,sum(e_to_e_total_count),sum(e_to_e_connect_count),sum(e_to_e_connect_time_length),sum(e_to_e_unconnect_count),sum(e_to_e_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date "
//				+ "order by deptname,username,realname,date ";
//
//		return reportService.getMoreRecord(sqlByDay);
//	}
//
//	public List<Object[]> get_e_to_e_Sum_RecordByDay(String startValue,
//			String endValue) {
//		String sqlByDay = "select deptname,username,realname,sum(e_to_e_total_count),sum(e_to_e_connect_count),sum(e_to_e_connect_time_length),sum(e_to_e_unconnect_count),sum(e_to_e_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname "
//				+ "order by deptname,username,realname ";
//
//		return reportService.getMoreRecord(sqlByDay);
//	}
//
//	// 内部：按天和时统计
//	public List<Object[]> get_e_to_e_RecordByDayHour(String startValue,
//			String endValue) {
//		String sqlByDayHour = "select deptname,username,realname,date,hour,sum(e_to_e_total_count),sum(e_to_e_connect_count),sum(e_to_e_connect_time_length),sum(e_to_e_unconnect_count),sum(e_to_e_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date,hour "
//				+ "order by deptname,username,realname,date,hour ";
//		return reportService.getMoreRecord(sqlByDayHour);
//	}
//
//	// 内部：按月统计
//	public List<Object[]> get_e_to_e_RecordByMonth(String startValue,
//			String endValue) {
//		String sqlByHour = "select deptname,username,realname,to_char(date,'yyyy-MM'),sum(e_to_e_total_count),sum(e_to_e_connect_count),sum(e_to_e_connect_time_length),sum(e_to_e_unconnect_count),sum(e_to_e_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,to_char(date,'yyyy-MM') "
//				+ "order by deptname,username,realname,to_char(date,'yyyy-MM') ";
//
//		return reportService.getMoreRecord(sqlByHour);
//	}
//
//	// 呼出：按天统计
//	public List<Object[]> getOutgoingRecordByDay(String startValue,
//			String endValue) {
//		String sql = "select deptname,username,realname,date,sum(outgoing_total_count),sum(outgoing_connect_count),sum(outgoing_connect_time_length),sum(outgoing_unconnect_count),sum(outgoing_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date "
//				+ " order by deptname,username,realname,date ";
//		return reportService.getMoreRecord(sql);
//	};
//
//	public List<Object[]> getOutgoingSumRecordByDay(String startValue,
//			String endValue) {
//		String sql = "select deptname,username,realname,sum(outgoing_total_count),sum(outgoing_connect_count),sum(outgoing_connect_time_length),sum(outgoing_unconnect_count),sum(outgoing_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname "
//				+ " order by  deptname,username,realname ";
//		return reportService.getMoreRecord(sql);
//	};
//
//	// 呼出：按天和时统计
//	public List<Object[]> getOutgoingRecordByDayHour(String startValue,
//			String endValue) {
//
//		String sql = "select deptname,username,realname,date,hour,sum(outgoing_total_count),sum(outgoing_connect_count),sum(outgoing_connect_time_length),sum(outgoing_unconnect_count),sum(outgoing_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date,hour "
//				+ "order by deptname,username,realname,date,hour ";
//		return reportService.getMoreRecord(sql);
//
//	};
//
//	// 呼出：按月统计
//	public List<Object[]> getOutgoingRecordByMonth(String startValue,
//			String endValue) {
//
//		String sql = "select deptname,username,realname,to_char(date,'yyyy-MM'),sum(outgoing_total_count),sum(outgoing_connect_count),sum(outgoing_connect_time_length),sum(outgoing_unconnect_count),sum(outgoing_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,to_char(date,'yyyy-MM') "
//				+ "order by deptname,username,realname,to_char(date,'yyyy-MM') ";
//
//		return reportService.getMoreRecord(sql);
//	};
//
//	// 呼入：按天统计
//	public List<Object[]> getIncomingRecordByDay(String startValue,
//			String endValue) {
//		String sql = "select deptname,username,realname,date,sum(incoming_total_count),sum(incoming_connect_count),sum(incoming_connect_time_length),sum(incoming_unconnect_count),sum(incoming_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date "
//				+ " order by deptname,username,realname,date ";
//		return reportService.getMoreRecord(sql);
//	};
//
//	public List<Object[]> getIncomingSumRecordByDay(String startValue,
//			String endValue) {
//		String sql = "select deptname,username,realname,sum(incoming_total_count),sum(incoming_connect_count),sum(incoming_connect_time_length),sum(incoming_unconnect_count),sum(incoming_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname "
//				+ " order by deptname,username,realname ";
//		return reportService.getMoreRecord(sql);
//	};
//
//	// 呼入：按天和时统计
//	public List<Object[]> getIncomingRecordByDayHour(String startValue,
//			String endValue) {
//
//		String sql = "select deptname,username,realname,date,hour,sum(incoming_total_count),sum(incoming_connect_count),sum(incoming_connect_time_length),sum(incoming_unconnect_count),sum(incoming_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date,hour "
//				+ "order by deptname,username,realname,date,hour ";
//		return reportService.getMoreRecord(sql);
//
//	};
//
//	// 呼入：按月统计
//	public List<Object[]> getIncomingRecordByMonth(String startValue,
//			String endValue) {
//
//		String sql = "select deptname,username,realname,to_char(date,'yyyy-MM'),sum(incoming_total_count),sum(incoming_connect_count),sum(incoming_connect_time_length),sum(incoming_unconnect_count),sum(incoming_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,to_char(date,'yyyy-MM') "
//				+ "order by deptname,username,realname,to_char(date,'yyyy-MM') ";
//
//		return reportService.getMoreRecord(sql);
//	};
//
//	// 外传外:按天统计
//	public List<Object[]> get_o_to_o_RecordByDay(String startValue,
//			String endValue) {
//		String sqlByDay = "select deptname,username,realname,date,sum(o_to_o_total_count),sum(o_to_o_connect_count),sum(o_to_o_connect_time_length),sum(o_to_o_unconnect_count),sum(o_to_o_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date "
//				+ "order by deptname,username,realname,date ";
//
//		return reportService.getMoreRecord(sqlByDay);
//	}
//
//	public List<Object[]> get_o_to_o_sum_RecordByDay(String startValue,
//			String endValue) {
//		String sqlByDay = "select deptname,username,realname,sum(o_to_o_total_count),sum(o_to_o_connect_count),sum(o_to_o_connect_time_length),sum(o_to_o_unconnect_count),sum(o_to_o_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname "
//				+ "order by deptname,username,realname ";
//
//		return reportService.getMoreRecord(sqlByDay);
//	}
//
//	// TODO 求和 全部 按天
//	public List<Object[]> get_call_sum_RecordByDay(String startValue,
//			String endValue) {
//		String sqlByDay = "select deptname,username,realname,sum(call_total_count),sum(call_connect_count),sum(call_connect_time_length),sum(call_unconnect_count),sum(call_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname "
//				+ "order by deptname,username,realname ";
//
//		return reportService.getMoreRecord(sqlByDay);
//	}
//
//	// 详情 全部 按天
//	public List<Object[]> get_call_RecordByDay(String startValue,
//			String endValue) {
//		String sqlByDay = "select deptname,username,realname,date,sum(call_total_count),sum(call_connect_count),sum(call_connect_time_length),sum(call_unconnect_count),sum(call_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date "
//				+ "order by deptname,username,realname,date ";
//
//		return reportService.getMoreRecord(sqlByDay);
//
//	}
//
//	// 详情 全部 按天和时
//	public List<Object[]> getCallRecordByDayHour(String startValue,
//			String endValue) {
//
//		String sql = "select deptname,username,realname,date,hour,sum(call_total_count),sum(call_connect_count),sum(call_connect_time_length),sum(call_unconnect_count),sum(call_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date,hour "
//				+ "order by deptname,username,realname,date,hour ";
//		return reportService.getMoreRecord(sql);
//
//	};
//
//	// 详情 全部 按月
//	public List<Object[]> getCallRecordByMonth(String startValue,
//			String endValue) {
//
//		String sql = "select deptname,username,realname,to_char(date,'yyyy-MM'),sum(call_total_count),sum(call_connect_count),sum(call_connect_time_length),sum(call_unconnect_count),sum(call_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,to_char(date,'yyyy-MM') "
//				+ "order by deptname,username,realname,to_char(date,'yyyy-MM') ";
//
//		return reportService.getMoreRecord(sql);
//	};
//
//	// 外传外：按天和时统计
//	public List<Object[]> get_o_to_o_RecordByDayHour(String startValue,
//			String endValue) {
//		String sqlByDayHour = "select deptname,username,realname,date,hour,sum(o_to_o_total_count),sum(o_to_o_connect_count),sum(o_to_o_connect_time_length),sum(o_to_o_unconnect_count),sum(o_to_o_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and date >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and date <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,date,hour "
//				+ "order by deptname,username,realname,date,hour ";
//		return reportService.getMoreRecord(sqlByDayHour);
//	}
//
//	// 外传外：按月统计
//	public List<Object[]> get_o_to_o_RecordByMonth(String startValue,
//			String endValue) {
//		String sqlByHour = "select deptname,username,realname,to_char(date,'yyyy-MM'),sum(o_to_o_total_count),sum(o_to_o_connect_count),sum(o_to_o_connect_time_length),sum(o_to_o_unconnect_count),sum(o_to_o_unconnect_time_length) "
//
//				+ "from ec2_report_call_check "
//				+ "where "
//				+ "domainid = "
//				+ "'"
//				+ domainId
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(date,'yyyy-MM') <= "
//				+ "'"
//				+ endValue
//				+ "'"
//				+ " group by deptname,username,realname,to_char(date,'yyyy-MM') "
//				+ "order by deptname,username,realname,to_char(date,'yyyy-MM') ";
//
//		return reportService.getMoreRecord(sqlByHour);
//	}
//
//	public String getStartValue() {
//		return startValue;
//	}
//
//	public String getEndValue() {
//		return endValue;
//	}
//
//}
