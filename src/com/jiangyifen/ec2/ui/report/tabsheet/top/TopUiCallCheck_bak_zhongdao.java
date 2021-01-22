//package com.jiangyifen.ec2.ui.report.tabsheet.top;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import org.mortbay.jetty.security.SSORealm;
//
//import com.jiangyifen.ec2.entity.OperationLog;
//import com.jiangyifen.ec2.entity.User;
//import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
//import com.jiangyifen.ec2.report.pojo.CallCheckPo;
//import com.jiangyifen.ec2.report.pojo.CallCheckSuperPo;
//import com.jiangyifen.ec2.service.common.CommonService;
//import com.jiangyifen.ec2.service.mgr.ReportService;
//import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
//
//import com.jiangyifen.ec2.ui.report.tabsheet.CallCheck;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.NumberFormat;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportSum;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
//import com.jiangyifen.ec2.utils.SpringContextHolder;
//import com.vaadin.data.Property.ValueChangeEvent;
//import com.vaadin.data.util.BeanItemContainer;
//import com.vaadin.terminal.FileResource;
//import com.vaadin.terminal.Resource;
//import com.vaadin.terminal.gwt.server.WebApplicationContext;
//import com.vaadin.ui.Button;
//import com.vaadin.ui.Button.ClickEvent;
//import com.vaadin.ui.Alignment;
//import com.vaadin.ui.ComboBox;
//import com.vaadin.ui.Embedded;
//import com.vaadin.ui.HorizontalLayout;
//import com.vaadin.ui.Label;
//import com.vaadin.ui.OptionGroup;
//import com.vaadin.ui.PopupDateField;
//import com.vaadin.ui.TabSheet;
//import com.vaadin.ui.Window;
//import com.vaadin.ui.TabSheet.Tab;
//import com.vaadin.ui.Table;
//import com.vaadin.ui.VerticalLayout;
//
//@SuppressWarnings("serial")
//public class TopUiCallCheck_bak_zhongdao extends VerticalLayout {
//
//	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
//	private ComboBox dateComboBox;
//	private Label fromLabel;
//	private PopupDateField startDate;
//	private Label toLabel;
//	private PopupDateField endDate;
//
//	private ComboBox comboBox;// 呼叫方向
//
//	private Button seeButton;// 查询
//	private Button exportReport;// 导出报表
//	// private ComboBox usernameBox;
//
//	private CallCheck callCheck;
//
//	private TabSheet tabSheet;
//
//	private Tab tab;
//
//	private String[] dateType = new String[] { "本日", "本周", "本月", "本年" };
//
//	private String[] callDirection = new String[] { "呼出", "呼入", "全部" };
//
//	private List<Object[]> list;// 从数据库获得新的数据
//
//	private String startValue;// startDate的值
//	private String endValue;// endDate的值
//
//	private List<String> columnNames;// table的列名
//	private String titleName;// 传入的报表名
//	private File file;
//	private Table table;// 显示数据的table
//	private BeanItemContainer<CallCheckPo> beanItemContainer;// 绑定table的数据源
//	private Map<String, CallCheckPo> map;// 存实体的集合
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
//	private String sql;
//
//	private String[] columnHeaders = new String[] { "部门", "工号", "姓名", "呼叫总数量",
//			"呼叫总时长", "呼叫平均时长", "呼叫接通数量", "呼叫接通时长", "呼叫平均接通时长", "呼叫未接通数量",
//			"呼叫未接通时长", "振铃时长", "平均振铃时长" };
//	private Object[] visibleColumns = new Object[] { "dept", "empno", "name",
//			"callTotalCount", "callTotalTimeLength", "callAvgTimeLength",
//			"callConnectCount", "callConnectTimeLength",
//			"callAvgConnectTimeLength", "callUnconnectCount",
//			"callUnconnectTimeLength", "callRingTimeLength",
//			"callAvgRingTimeLength" };
//
//	public TopUiCallCheck_bak_zhongdao(CallCheck callCheck) {
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
//		comboBox.setValue(callDirection[0]);// 默认是呼出
//		horizontalLayout.addComponent(comboBox);
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
//		// usernameBox = new ComboBox();
//		// usernameBox.setWidth("100px");
//		// usernameBox.setImmediate(true);
//		// usernameBox.setInputPrompt("------工号------");
//		// horizontalLayout.addComponent(usernameBox);
//		//
//		// usernameBox.addListener(new ComboBox.ValueChangeListener() {
//		//
//		// @Override
//		// public void valueChange(ValueChangeEvent event) {
//		//
//		// if (event.getProperty().getValue() == null) {
//		// return;
//		// }
//		//
//		// String value = event.getProperty().getValue().toString();
//		//
//		// Table table = tableUtil.getTable();
//		// table.removeAllItems();
//		//
//		// for (Object[] objects : list) {
//		//
//		// if (objects[index].equals(value)) {
//		//
//		// table.addItem(objects, null);
//		// }
//		// }
//		//
//		// }
//		// });
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
//		String callDirectionValue = comboBox.getValue().toString().trim();
//
//		startValue = format.format(startDate.getValue());// 获得startDate的值
//		endValue = format.format(endDate.getValue());// 获得endDate的值
//
//		if ("呼出".equals(callDirectionValue)) {
//			// TODO 计时开始
//			long start = System.currentTimeMillis();
//
//			Map<String, CallCheckPo> map = getOutgoingCallCheckPo(startValue,
//					endValue);
//
//			table = new Table("话务考核");
//			beanItemContainer = new BeanItemContainer<CallCheckPo>(
//					CallCheckPo.class);
//
//			Set<Entry<String, CallCheckPo>> set = map.entrySet();
//			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
//			while (iterator.hasNext()) {
//				Entry<String, CallCheckPo> entry = iterator.next();
//				CallCheckPo callCheckPo = entry.getValue();
//
//				// 呼叫平均时长
//				if (callCheckPo.getCallTotalCount() != 0) {
//					callCheckPo.setCallAvgTimeLength(callCheckPo
//							.getCallTotalTimeLength()
//							/ callCheckPo.getCallTotalCount());
//				}
//
//				// 呼叫平均接通时长
//				if (callCheckPo.getCallConnectCount() != 0) {
//					callCheckPo.setCallAvgConnectTimeLength(callCheckPo
//							.getCallConnectTimeLength()
//							/ callCheckPo.getCallConnectCount());
//				}
//
//				// 平均振铃时长
//				if (callCheckPo.getCallTotalCount() != 0) {
//					callCheckPo.setCallAvgRingTimeLength(callCheckPo
//							.getCallRingTimeLength()
//							/ callCheckPo.getCallTotalCount());
//				}
//
//				beanItemContainer.addBean(callCheckPo);
//			}
//
//			table.setContainerDataSource(beanItemContainer);
//			table.setSizeFull();
//			table.setImmediate(true);
//			table.setNullSelectionAllowed(true);
//			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
//			table.setStyleName("striped");
//			table.setSelectable(true);
//			table.setVisibleColumns(visibleColumns);
//			table.setColumnHeaders(columnHeaders);
//
//			tab = tabSheet.addTab(table, "呼出统计", null);
//			tab.setClosable(true);
//			tabSheet.setSelectedTab(tab);
//
//			// TODO 计时结束
//			long end = System.currentTimeMillis();
//			System.out.println("spend time: " + (end - start));
//
//		} else if ("呼入".equals(callDirectionValue)) {
//
//			// TODO 计时开始
//			long start = System.currentTimeMillis();
//
//			Map<String, CallCheckPo> map = getIncomingCallCheckPo(startValue,
//					endValue);
//
//			table = new Table("话务考核");
//			beanItemContainer = new BeanItemContainer<CallCheckPo>(
//					CallCheckPo.class);
//
//			Set<Entry<String, CallCheckPo>> set = map.entrySet();
//			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
//			while (iterator.hasNext()) {
//				Entry<String, CallCheckPo> entry = iterator.next();
//				CallCheckPo callCheckPo = entry.getValue();
//
//				// 呼叫平均时长
//				if (callCheckPo.getCallTotalCount() != 0) {
//					callCheckPo.setCallAvgTimeLength(callCheckPo
//							.getCallTotalTimeLength()
//							/ callCheckPo.getCallTotalCount());
//				}
//
//				// 呼叫平均接通时长
//				if (callCheckPo.getCallConnectCount() != 0) {
//					callCheckPo.setCallAvgConnectTimeLength(callCheckPo
//							.getCallConnectTimeLength()
//							/ callCheckPo.getCallConnectCount());
//				}
//
//				// 平均振铃时长
//				if (callCheckPo.getCallTotalCount() != 0) {
//					callCheckPo.setCallAvgRingTimeLength(callCheckPo
//							.getCallRingTimeLength()
//							/ callCheckPo.getCallTotalCount());
//				}
//
//				beanItemContainer.addBean(callCheckPo);
//			}
//
//			table.setContainerDataSource(beanItemContainer);
//			table.setSizeFull();
//			table.setImmediate(true);
//			table.setNullSelectionAllowed(true);
//			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
//			table.setStyleName("striped");
//			table.setSelectable(true);
//			table.setVisibleColumns(visibleColumns);
//			table.setColumnHeaders(columnHeaders);
//
//			tab = tabSheet.addTab(table, "呼入统计", null);
//			tab.setClosable(true);
//			tabSheet.setSelectedTab(tab);
//
//			// TODO 计时结束
//			long end = System.currentTimeMillis();
//			System.out.println("spend time: " + (end - start));
//
//		} else if ("全部".equals(callDirectionValue)) {
//			// TODO 计时开始
//			long start = System.currentTimeMillis();
//
//			Map<String, CallCheckPo> map = getTotalCallCheckPo(startValue,
//					endValue);
//
//			table = new Table("话务考核");
//			beanItemContainer = new BeanItemContainer<CallCheckPo>(
//					CallCheckPo.class);
//
//			Set<Entry<String, CallCheckPo>> set = map.entrySet();
//			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
//			while (iterator.hasNext()) {
//				Entry<String, CallCheckPo> entry = iterator.next();
//				CallCheckPo callCheckPo = entry.getValue();
//
//				// 呼叫平均时长
//				if (callCheckPo.getCallTotalCount() != 0) {
//					callCheckPo.setCallAvgTimeLength(callCheckPo
//							.getCallTotalTimeLength()
//							/ callCheckPo.getCallTotalCount());
//				}
//
//				// 呼叫平均接通时长
//				if (callCheckPo.getCallConnectCount() != 0) {
//					callCheckPo.setCallAvgConnectTimeLength(callCheckPo
//							.getCallConnectTimeLength()
//							/ callCheckPo.getCallConnectCount());
//				}
//
//				// 平均振铃时长
//				if (callCheckPo.getCallTotalCount() != 0) {
//					callCheckPo.setCallAvgRingTimeLength(callCheckPo
//							.getCallRingTimeLength()
//							/ callCheckPo.getCallTotalCount());
//				}
//
//				beanItemContainer.addBean(callCheckPo);
//			}
//
//			table.setContainerDataSource(beanItemContainer);
//			table.setSizeFull();
//			table.setImmediate(true);
//			table.setNullSelectionAllowed(true);
//			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
//			table.setStyleName("striped");
//			table.setSelectable(true);
//			table.setVisibleColumns(visibleColumns);
//			table.setColumnHeaders(columnHeaders);
//
//			tab = tabSheet.addTab(table, "全部统计", null);
//			tab.setClosable(true);
//			tabSheet.setSelectedTab(tab);
//
//			// TODO 计时结束
//			long end = System.currentTimeMillis();
//			System.out.println("spend time: " + (end - start));
//		}
//
//	}
//
//	public Map<String, CallCheckPo> getTotalCallCheckPo(String startValue,
//			String endValue) {
//
//		// 存入实体
//		map = new HashMap<String, CallCheckPo>();
//
//		sql = "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select destusername from cdr where destusername !='' and domainid = "
//				+ domainId
//				+ " group by  destusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.destusername = u.username";
//
//		List<Object[]> destList = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : destList) {
//
//			CallCheckPo callCheckPo = new CallCheckPo();
//
//			// 工号
//			if (objects[0] != null) {
//				callCheckPo.setEmpno(objects[0].toString());
//			}
//			// 部门
//			if (objects[1] != null) {
//				callCheckPo.setDept(objects[1].toString());
//			}
//			// 姓名
//			if (objects[2] != null) {
//				callCheckPo.setName(objects[2].toString());
//			}
//
//			map.put(objects[0].toString(), callCheckPo);
//
//		}
//
//		sql = "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select srcusername from cdr where srcusername !='' and domainid = "
//				+ domainId
//				+ " group by  srcusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.srcusername = u.username";
//
//		List<Object[]> srcList = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : srcList) {
//
//			CallCheckPo callCheckPo = new CallCheckPo();
//
//			// 工号
//			if (objects[0] != null) {
//				callCheckPo.setEmpno(objects[0].toString());
//			}
//			// 部门
//			if (objects[1] != null) {
//				callCheckPo.setDept(objects[1].toString());
//			}
//			// 姓名
//			if (objects[2] != null) {
//				callCheckPo.setName(objects[2].toString());
//			}
//
//			map.put(objects[0].toString(), callCheckPo);
//
//		}
//
//		// 呼叫总数，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//				// 呼叫总数量
//				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
//						.toString()) + callCheckPo.getCallTotalCount());
//				// 呼叫总时长
//				callCheckPo.setCallTotalTimeLength(Integer.parseInt(objects[2]
//						.toString()) + callCheckPo.getCallTotalTimeLength());
//			}
//		}
//
//		// 呼叫总数，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//				// 呼叫总数量
//				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
//						.toString()) + callCheckPo.getCallTotalCount());
//				// 呼叫总时长
//
//				callCheckPo.setCallTotalTimeLength(Integer.parseInt(objects[2]
//						.toString()) + callCheckPo.getCallTotalTimeLength());
//			}
//		}
//
//		// 呼叫接通数量，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and isbridged is not null and isbridged = true and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//				// 呼叫接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1]
//							.toString()) + callCheckPo.getCallConnectCount());
//				}
//				// 呼叫接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallConnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallConnectTimeLength());
//				}
//			}
//		}
//
//		// 呼叫接通数量，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and isbridged is not null and isbridged = true and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//				// 呼叫接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1]
//							.toString()) + callCheckPo.getCallConnectCount());
//				}
//				// 呼叫接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallConnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallConnectTimeLength());
//				}
//			}
//		}
//
//		// 呼叫未接通数量，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and (isbridged is null or isbridged = false) and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallUnconnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallUnconnectCount());
//				}
//				// 呼叫未接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallUnconnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallUnconnectTimeLength());
//				}
//
//			}
//		}
//
//		// 呼叫未接通数量，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and (isbridged is null or isbridged = false) and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallUnconnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallUnconnectCount());
//				}
//				// 呼叫未接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallUnconnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallUnconnectTimeLength());
//				}
//
//			}
//		}
//
//		// 振铃时长
//		sql = "select  destusername as destusername,sum(ringduration) as count "
//				+ "from cdr "
//				+ "where destusername != '' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫振铃时长
//				if (objects[1] != null) {
//					callCheckPo.setCallRingTimeLength(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallRingTimeLength());
//				}
//
//			}
//		}
//
//		// 振铃时长
//		sql = "select  srcusername as srcusername,sum(ringduration) as count "
//				+ "from cdr " + "where srcusername != '' and domainid = "
//				+ domainId + " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'" + startValue + "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= " + "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫振铃时长
//				if (objects[1] != null) {
//					callCheckPo.setCallRingTimeLength(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallRingTimeLength());
//				}
//
//			}
//		}
//
//		return map;
//
//	}
//
//	public Map<String, CallCheckPo> getOutgoingCallCheckPo(String startValue,
//			String endValue) {
//
//		// 存入实体
//		map = new HashMap<String, CallCheckPo>();
//
//		sql = "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select srcusername from cdr where srcusername !='' and domainid = "
//				+ domainId
//				+ " group by  srcusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.srcusername = u.username";
//
//		List<Object[]> srcList = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : srcList) {
//
//			CallCheckPo callCheckPo = new CallCheckPo();
//
//			// 工号
//			if (objects[0] != null) {
//				callCheckPo.setEmpno(objects[0].toString());
//			}
//			// 部门
//			if (objects[1] != null) {
//				callCheckPo.setDept(objects[1].toString());
//			}
//			// 姓名
//			if (objects[2] != null) {
//				callCheckPo.setName(objects[2].toString());
//			}
//
//			map.put(objects[0].toString(), callCheckPo);
//
//		}
//
//		// 呼叫总数，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and cdrdirection = '1' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//				// 呼叫总数量
//				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
//						.toString()) + callCheckPo.getCallTotalCount());
//				// 呼叫总时长
//
//				callCheckPo.setCallTotalTimeLength(Integer.parseInt(objects[2]
//						.toString()) + callCheckPo.getCallTotalTimeLength());
//			}
//		}
//
//		// 呼叫接通数量，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and cdrdirection = '1' and isbridged is not null and isbridged = true and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//				// 呼叫接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1]
//							.toString()) + callCheckPo.getCallConnectCount());
//				}
//				// 呼叫接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallConnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallConnectTimeLength());
//				}
//			}
//		}
//
//		// 呼叫未接通数量，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and cdrdirection = '1' and (isbridged is null or isbridged = false) and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallUnconnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallUnconnectCount());
//				}
//				// 呼叫未接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallUnconnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallUnconnectTimeLength());
//				}
//
//			}
//		}
//
//		// 振铃时长
//		sql = "select  srcusername as srcusername,sum(ringduration) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and cdrdirection = '1' and domainid = "
//				+ domainId + " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'" + startValue + "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= " + "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫振铃时长
//				if (objects[1] != null) {
//					callCheckPo.setCallRingTimeLength(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallRingTimeLength());
//				}
//
//			}
//		}
//
//		return map;
//
//	}
//
//	public Map<String, CallCheckPo> getIncomingCallCheckPo(String startValue,
//			String endValue) {
//
//		// 存入实体
//		map = new HashMap<String, CallCheckPo>();
//
//		sql = "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select destusername from cdr where destusername !='' and domainid = "
//				+ domainId
//				+ " group by  destusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.destusername = u.username";
//
//		List<Object[]> destList = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : destList) {
//
//			CallCheckPo callCheckPo = new CallCheckPo();
//
//			// 工号
//			if (objects[0] != null) {
//				callCheckPo.setEmpno(objects[0].toString());
//			}
//			// 部门
//			if (objects[1] != null) {
//				callCheckPo.setDept(objects[1].toString());
//			}
//			// 姓名
//			if (objects[2] != null) {
//				callCheckPo.setName(objects[2].toString());
//			}
//
//			map.put(objects[0].toString(), callCheckPo);
//
//		}
//
//		// 呼叫总数，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and cdrdirection = '2' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//				// 呼叫总数量
//				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
//						.toString()) + callCheckPo.getCallTotalCount());
//				// 呼叫总时长
//				callCheckPo.setCallTotalTimeLength(Integer.parseInt(objects[2]
//						.toString()) + callCheckPo.getCallTotalTimeLength());
//			}
//		}
//
//		// 呼叫接通数量，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and cdrdirection = '2' and isbridged is not null and isbridged = true and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//				// 呼叫接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1]
//							.toString()) + callCheckPo.getCallConnectCount());
//				}
//				// 呼叫接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallConnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallConnectTimeLength());
//				}
//			}
//		}
//
//		// 呼叫未接通数量，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and cdrdirection = '2' and (isbridged is null or isbridged = false) and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallUnconnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallUnconnectCount());
//				}
//				// 呼叫未接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallUnconnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallUnconnectTimeLength());
//				}
//
//			}
//		}
//
//		// 振铃时长
//		sql = "select  destusername as destusername,sum(ringduration) as count "
//				+ "from cdr "
//				+ "where destusername != '' and cdrdirection = '2' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫振铃时长
//				if (objects[1] != null) {
//					callCheckPo.setCallRingTimeLength(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallRingTimeLength());
//				}
//
//			}
//		}
//
//		return map;
//
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
//		final Window window = new Window();
//		window.setWidth("30%");
//		window.setHeight("40%");
//		window.setCaption("请选择要导出的列名");
//		window.center();
//		table.getApplication().getMainWindow().addWindow(window);
//
//		VerticalLayout verticalLayout = new VerticalLayout();
//		verticalLayout.setSizeFull();
//		verticalLayout.setMargin(true);
//		window.addComponent(verticalLayout);
//
//		HorizontalLayout horizontalLayout = new HorizontalLayout();
//		horizontalLayout.setMargin(true);
//		horizontalLayout.setSizeFull();
//		verticalLayout.addComponent(horizontalLayout);
//
//		final OptionGroup totalGroup = new OptionGroup("全部统计的列名");
//		totalGroup.setMultiSelect(true);
//		horizontalLayout.addComponent(totalGroup);
//		horizontalLayout.setComponentAlignment(totalGroup,
//				Alignment.BOTTOM_LEFT);
//		totalGroup.addItem("getCallTotalCount|呼叫总数量");
//		totalGroup.addItem("getCallTotalTimeLength|呼叫总时长");
//		totalGroup.addItem("getCallAvgTimeLength|呼叫平均时长");
//		totalGroup.addItem("getCallConnectCount|呼叫接通数量");
//		totalGroup.addItem("getCallConnectTimeLength|呼叫接通时长");
//		totalGroup.addItem("getCallAvgConnectTimeLength|呼叫平均接通时长");
//		totalGroup.addItem("getCallUnconnectCount|呼叫未接通数量");
//		totalGroup.addItem("getCallUnconnectTimeLength|呼叫未接通时长");
//		totalGroup.addItem("getCallRingTimeLength|呼叫振铃时长");
//		totalGroup.addItem("getCallAvgRingTimeLength|呼叫平均振铃时长");
//		totalGroup.setItemCaption("getCallTotalCount|呼叫总数量", "呼叫总数量");
//		totalGroup.setItemCaption("getCallTotalTimeLength|呼叫总时长", "呼叫总时长");
//		totalGroup.setItemCaption("getCallAvgTimeLength|呼叫平均时长", "呼叫平均时长");
//		totalGroup.setItemCaption("getCallConnectCount|呼叫接通数量", "呼叫接通数量");
//		totalGroup.setItemCaption("getCallConnectTimeLength|呼叫接通时长", "呼叫接通时长");
//		totalGroup.setItemCaption("getCallAvgConnectTimeLength|呼叫平均接通时长",
//				"呼叫平均接通时长");
//		totalGroup.setItemCaption("getCallUnconnectCount|呼叫未接通数量", "呼叫未接通数量");
//		totalGroup.setItemCaption("getCallUnconnectTimeLength|呼叫未接通时长",
//				"呼叫未接通时长");
//		totalGroup.setItemCaption("getCallRingTimeLength|呼叫振铃时长", "呼叫振铃时长");
//		totalGroup.setItemCaption("getCallAvgRingTimeLength|呼叫平均振铃时长",
//				"呼叫平均振铃时长");
//
//		final OptionGroup outgoingGroup = new OptionGroup("呼出统计的列名");
//		outgoingGroup.setMultiSelect(true);
//		horizontalLayout.addComponent(outgoingGroup);
//		horizontalLayout.setComponentAlignment(outgoingGroup,
//				Alignment.BOTTOM_CENTER);
//		outgoingGroup.addItem("getOutgoingCount|呼出总数量");
//		outgoingGroup.addItem("getOutgoingTimeLength|呼出总时长");
//		outgoingGroup.addItem("getOutgoingAvgTimeLength|呼出平均时长");
//		outgoingGroup.addItem("getOutgoingConnectCount|呼出接通数量");
//		outgoingGroup.addItem("getOutgoingConnectTimeLength|呼出接通时长");
//		outgoingGroup.addItem("getOutgoingAvgConnectTimeLength|呼出平均接通时长");
//		outgoingGroup.addItem("getOutgoingUnconnectCount|呼出未接通数量");
//		outgoingGroup.addItem("getOutgoingUnconnectTimeLength|呼出未接通时长");
//		outgoingGroup.addItem("getOutgoingRingTimeLength|呼出振铃时长");
//		outgoingGroup.addItem("getOutgoingAvgRingTimeLength|呼出平均振铃时长");
//		outgoingGroup.setItemCaption("getOutgoingCount|呼出总数量", "呼出总数量");
//		outgoingGroup.setItemCaption("getOutgoingTimeLength|呼出总时长", "呼出总时长");
//		outgoingGroup.setItemCaption("getOutgoingAvgTimeLength|呼出平均时长",
//				"呼出平均时长");
//		outgoingGroup
//				.setItemCaption("getOutgoingConnectCount|呼出接通数量", "呼出接通数量");
//		outgoingGroup.setItemCaption("getOutgoingConnectTimeLength|呼出接通时长",
//				"呼出接通时长");
//		outgoingGroup.setItemCaption(
//				"getOutgoingAvgConnectTimeLength|呼出平均接通时长", "呼出平均接通时长");
//		outgoingGroup.setItemCaption("getOutgoingUnconnectCount|呼出未接通数量",
//				"呼出未接通数量");
//		outgoingGroup.setItemCaption("getOutgoingUnconnectTimeLength|呼出未接通时长",
//				"呼出未接通时长");
//		outgoingGroup.setItemCaption("getOutgoingRingTimeLength|呼出振铃时长",
//				"呼出振铃时长");
//		outgoingGroup.setItemCaption("getOutgoingAvgRingTimeLength|呼出平均振铃时长",
//				"呼出平均振铃时长");
//
//		final OptionGroup incomingGroup = new OptionGroup("呼入统计的列名");
//		incomingGroup.setMultiSelect(true);
//		horizontalLayout.addComponent(incomingGroup);
//		horizontalLayout.setComponentAlignment(incomingGroup,
//				Alignment.BOTTOM_RIGHT);
//		incomingGroup.addItem("getIncomingCount|呼入总数量");
//		incomingGroup.addItem("getIncomingTimeLength|呼入总时长");
//		incomingGroup.addItem("getIncomingAvgTimeLength|呼入平均时长");
//		incomingGroup.addItem("getIncomingConnectCount|呼入接通数量");
//		incomingGroup.addItem("getIncomingConnectTimeLength|呼入接通时长");
//		incomingGroup.addItem("getIncomingAvgConnectTimeLength|呼入平均接通时长");
//		incomingGroup.addItem("getIncomingUnconnectCount|呼入未接通数量");
//		incomingGroup.addItem("getIncomingUnconnectTimeLength|呼入未接通时长");
//		incomingGroup.addItem("getIncomingRingTimeLength|呼入振铃时长");
//		incomingGroup.addItem("getIncomingAvgRingTimeLength|呼入平均振铃时长");
//		incomingGroup.setItemCaption("getIncomingCount|呼入总数量", "呼入总数量");
//		incomingGroup.setItemCaption("getIncomingTimeLength|呼入总时长", "呼入总时长");
//		incomingGroup.setItemCaption("getIncomingAvgTimeLength|呼入平均时长",
//				"呼入平均时长");
//		incomingGroup
//				.setItemCaption("getIncomingConnectCount|呼入接通数量", "呼入接通数量");
//		incomingGroup.setItemCaption("getIncomingConnectTimeLength|呼入接通时长",
//				"呼入接通时长");
//		incomingGroup.setItemCaption(
//				"getIncomingAvgConnectTimeLength|呼入平均接通时长", "呼入平均接通时长");
//		incomingGroup.setItemCaption("getIncomingUnconnectCount|呼入未接通数量",
//				"呼入未接通数量");
//		incomingGroup.setItemCaption("getIncomingUnconnectTimeLength|呼入未接通时长",
//				"呼入未接通时长");
//		incomingGroup.setItemCaption("getIncomingRingTimeLength|呼入振铃时长",
//				"呼入振铃时长");
//		incomingGroup.setItemCaption("getIncomingAvgRingTimeLength|呼入平均振铃时长",
//				"呼入平均振铃时长");
//
//		HorizontalLayout horizontalLayout2 = new HorizontalLayout();
//		horizontalLayout2.setMargin(true);
//		horizontalLayout2.setSizeFull();
//		verticalLayout.addComponent(horizontalLayout2);
//
//		Button exportButton = new Button("导出");
//		horizontalLayout2.addComponent(exportButton);
//		horizontalLayout2.setComponentAlignment(exportButton,
//				Alignment.BOTTOM_LEFT);
//		exportButton.addListener(new Button.ClickListener() {
//
//			@Override
//			public void buttonClick(ClickEvent event) {
//
//				// TODO
//				System.out
//						.println("--------------3333333333333333333333-----------");
//				// 全部选中的值
//				Set<String> totalSet = (Set<String>) totalGroup.getValue();
//
//				// 呼出选中的值
//				Set<String> outgoingSet = (Set<String>) outgoingGroup
//						.getValue();
//
//				// 呼入选中的值
//				Set<String> incomingSet = (Set<String>) incomingGroup
//						.getValue();
//
//				List<String> methodList = getMethodNameList(totalSet,
//						outgoingSet, incomingSet);
//
//				// TODO
//				System.out.println("1111111111111111111111111");
//				System.out.println(methodList);
//
//				Map<String, CallCheckSuperPo> map = createcCallCheckSuperPoMap();
//
//				analysisCollection(map, methodList);
//
//				System.out
//						.println("------------888888888888888---------------");
//
//			}
//		});
//
//		Button cancelButton = new Button("取消");
//		horizontalLayout2.addComponent(cancelButton);
//		cancelButton.addListener(new Button.ClickListener() {
//
//			@Override
//			public void buttonClick(ClickEvent event) {
//
//				table.getApplication().getMainWindow().removeWindow(window);
//
//			}
//		});
//		horizontalLayout2.setComponentAlignment(cancelButton,
//				Alignment.BOTTOM_RIGHT);
//
//		verticalLayout.addComponent(horizontalLayout2);
//
//	}
//
//	public void analysisCollection(Map<String, CallCheckSuperPo> map,
//			List<String> methodList) {
//
//		// 最终生成file的数据源
//		List<Object[]> toFileList = new ArrayList<Object[]>();
//
//		// 最终生成file列名的数据源
//		List<String> toFileColumnList = new ArrayList<String>();
//		toFileColumnList.add("部门");
//		toFileColumnList.add("工号");
//		toFileColumnList.add("姓名");
//
//		// 存列名
//		for (String method : methodList) {
//
//			// 下标
//			int index = method.indexOf("|");
//
//			// 存入截取的列名
//			toFileColumnList.add(method.substring(index + 1, method.length()));
//		}
//
//		// 遍历map
//		Set<Entry<String, CallCheckSuperPo>> entries = map.entrySet();
//		Iterator<Entry<String, CallCheckSuperPo>> iterator = entries.iterator();
//		while (iterator.hasNext()) {
//			Entry<String, CallCheckSuperPo> entry = iterator.next();
//			CallCheckSuperPo callCheckSuperPo = entry.getValue();
//			Object[] objects = new Object[methodList.size() + 3];
//			objects[0] = callCheckSuperPo.getDept();
//			objects[1] = callCheckSuperPo.getEmpno();
//			objects[2] = callCheckSuperPo.getName();
//
//			try {
//
//				for (int i = 0; i < methodList.size(); i++) {
//
//					// 反射调用方法
//					Class<? extends CallCheckSuperPo> class1 = callCheckSuperPo
//							.getClass();
//
//					// 方法名
//					int index = methodList.get(i).indexOf("|");
//
//					// 截取方法名
//					String methodN = methodList.get(i).substring(0, index);
//
//					// TODO
//
//					System.out.println("methodN-----> " + methodN);
//
//					Method method = class1.getMethod(methodN);
//
//					Object value = method.invoke(callCheckSuperPo);
//
//					// 存列名对应的值
//					objects[i+3] = value;
//				}
//
//				toFileList.add(objects);
//
//			} catch (SecurityException e) {
//
//				e.printStackTrace();
//			} catch (Exception e) {
//
//				e.printStackTrace();
//			}
//
//		}
//
//		downloadFile(toFileList, toFileColumnList);
//
//	}
//
//	public void downloadFile(List<Object[]> toFileList,
//			List<String> toFileNameList) {
//		// 设置文件名
//		titleName = table.getCaption();
//
//		// 设置列名
//		List<String> columnN = new ArrayList<String>();
//
//		for (String str : table.getColumnHeaders()) {
//			columnN.add(str);
//		}
//
//		columnNames = columnN;
//		// 开时时间
//		// startValue = reportTableUtil.getStartValue();
//		// 结束时间
//		// endValue = reportTableUtil.getEndValue();
//
//		// 设置数据源
//		// list = reportTableUtil.getList();
//
//		// 生成excel file
//		file = ExportExcelUtil.exportExcel(titleName, toFileList,
//				toFileNameList, startValue, endValue);
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
//		Resource resource = new FileResource(file, table.getApplication());
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
//	public List<String> getMethodNameList(Set<String> totalSet,
//			Set<String> outgoingSet, Set<String> incomingSet) {
//
//		List<String> list = new ArrayList<String>();
//
//		Iterator<String> iterator = totalSet.iterator();
//		while (iterator.hasNext()) {
//			list.add(iterator.next());
//		}
//
//		Iterator<String> iterator2 = outgoingSet.iterator();
//		while (iterator2.hasNext()) {
//			list.add(iterator2.next());
//		}
//
//		Iterator<String> iterator3 = incomingSet.iterator();
//		while (iterator3.hasNext()) {
//			list.add(iterator3.next());
//		}
//
//		return list;
//	}
//
//	public Map<String, CallCheckSuperPo> createcCallCheckSuperPoMap() {
//
//		Map<String, CallCheckSuperPo> map = new HashMap<String, CallCheckSuperPo>();
//
//		sql = "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select destusername from cdr where destusername !='' and domainid = "
//				+ domainId
//				+ " group by  destusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.destusername = u.username";
//
//		List<Object[]> destList = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : destList) {
//
//			CallCheckSuperPo callCheckPo = new CallCheckSuperPo();
//
//			// 工号
//			if (objects[0] != null) {
//				callCheckPo.setEmpno(objects[0].toString());
//			}
//			// 部门
//			if (objects[1] != null) {
//				callCheckPo.setDept(objects[1].toString());
//			}
//			// 姓名
//			if (objects[2] != null) {
//				callCheckPo.setName(objects[2].toString());
//			}
//
//			map.put(objects[0].toString(), callCheckPo);
//
//		}
//
//		sql = "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select srcusername from cdr where srcusername !='' and domainid = "
//				+ domainId
//				+ " group by  srcusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.srcusername = u.username";
//
//		List<Object[]> srcList = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : srcList) {
//
//			CallCheckSuperPo callCheckPo = new CallCheckSuperPo();
//
//			// 工号
//			if (objects[0] != null) {
//				callCheckPo.setEmpno(objects[0].toString());
//			}
//			// 部门
//			if (objects[1] != null) {
//				callCheckPo.setDept(objects[1].toString());
//			}
//			// 姓名
//			if (objects[2] != null) {
//				callCheckPo.setName(objects[2].toString());
//			}
//
//			map.put(objects[0].toString(), callCheckPo);
//
//		}
//
//		// 呼叫总数，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//				// 呼叫总数量
//				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
//						.toString()) + callCheckPo.getCallTotalCount());
//				// 呼叫总时长
//				callCheckPo.setCallTotalTimeLength(Integer.parseInt(objects[2]
//						.toString()) + callCheckPo.getCallTotalTimeLength());
//			}
//		}
//
//		// 呼叫总数，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//				// 呼叫总数量
//				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
//						.toString()) + callCheckPo.getCallTotalCount());
//				// 呼叫总时长
//
//				callCheckPo.setCallTotalTimeLength(Integer.parseInt(objects[2]
//						.toString()) + callCheckPo.getCallTotalTimeLength());
//			}
//		}
//
//		// 呼叫接通数量，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and isbridged is not null and isbridged = true and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//				// 呼叫接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1]
//							.toString()) + callCheckPo.getCallConnectCount());
//				}
//				// 呼叫接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallConnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallConnectTimeLength());
//				}
//			}
//		}
//
//		// 呼叫接通数量，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and isbridged is not null and isbridged = true and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//				// 呼叫接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1]
//							.toString()) + callCheckPo.getCallConnectCount());
//				}
//				// 呼叫接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallConnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallConnectTimeLength());
//				}
//			}
//		}
//
//		// 呼叫未接通数量，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and (isbridged is null or isbridged = false) and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallUnconnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallUnconnectCount());
//				}
//				// 呼叫未接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallUnconnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallUnconnectTimeLength());
//				}
//
//			}
//		}
//
//		// 呼叫未接通数量，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and (isbridged is null or isbridged = false) and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallUnconnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallUnconnectCount());
//				}
//				// 呼叫未接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setCallUnconnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallUnconnectTimeLength());
//				}
//
//			}
//		}
//
//		// 振铃时长
//		sql = "select  destusername as destusername,sum(ringduration) as count "
//				+ "from cdr "
//				+ "where destusername != '' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫振铃时长
//				if (objects[1] != null) {
//					callCheckPo.setCallRingTimeLength(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallRingTimeLength());
//				}
//
//			}
//		}
//
//		// 振铃时长
//		sql = "select  srcusername as srcusername,sum(ringduration) as count "
//				+ "from cdr " + "where srcusername != '' and domainid = "
//				+ domainId + " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'" + startValue + "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= " + "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫振铃时长
//				if (objects[1] != null) {
//					callCheckPo.setCallRingTimeLength(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallRingTimeLength());
//				}
//
//			}
//		}
//
//		// 呼叫接通数量，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and cdrdirection = '1' and isbridged is not null and isbridged = true and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//				// 呼叫接通数量
//				if (objects[1] != null) {
//					callCheckPo.setOutgoingConnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallConnectCount());
//				}
//				// 呼叫接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setOutgoingConnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallConnectTimeLength());
//				}
//			}
//		}
//
//		// 呼叫未接通数量，时长
//		sql = "select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and cdrdirection = '1' and (isbridged is null or isbridged = false) and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setOutgoingUnconnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallUnconnectCount());
//				}
//				// 呼叫未接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setOutgoingUnconnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallUnconnectTimeLength());
//				}
//
//			}
//		}
//
//		// 振铃时长
//		sql = "select  srcusername as srcusername,sum(ringduration) as count "
//				+ "from cdr "
//				+ "where srcusername != '' and cdrdirection = '1' and domainid = "
//				+ domainId + " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'" + startValue + "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= " + "'"
//				+ endValue + "'" + " group by srcusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫振铃时长
//				if (objects[1] != null) {
//					callCheckPo.setOutgoingRingTimeLength(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallRingTimeLength());
//				}
//
//			}
//		}
//
//		// 呼叫总数，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and cdrdirection = '2' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//				// 呼叫总数量
//				callCheckPo.setIncomingCount(Integer.parseInt(objects[1]
//						.toString()) + callCheckPo.getCallTotalCount());
//				// 呼叫总时长
//				callCheckPo.setIncomingTimeLength(Integer.parseInt(objects[2]
//						.toString()) + callCheckPo.getCallTotalTimeLength());
//			}
//		}
//
//		// 呼叫接通数量，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and cdrdirection = '2' and isbridged is not null and isbridged = true and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//				// 呼叫接通数量
//				if (objects[1] != null) {
//					callCheckPo.setIncomingConnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallConnectCount());
//				}
//				// 呼叫接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setIncomingConnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallConnectTimeLength());
//				}
//			}
//		}
//
//		// 呼叫未接通数量，时长
//		sql = "select  destusername as destusername,count(*) as count,sum(billableseconds) as count "
//				+ "from cdr "
//				+ "where destusername != '' and cdrdirection = '2' and (isbridged is null or isbridged = false) and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setIncomingUnconnectCount(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallUnconnectCount());
//				}
//				// 呼叫未接通时长
//				if (objects[2] != null) {
//
//					callCheckPo.setIncomingUnconnectTimeLength(Integer
//							.parseInt(objects[2].toString())
//							+ callCheckPo.getCallUnconnectTimeLength());
//				}
//
//			}
//		}
//
//		// 振铃时长
//		sql = "select  destusername as destusername,sum(ringduration) as count "
//				+ "from cdr "
//				+ "where destusername != '' and cdrdirection = '2' and domainid = "
//				+ domainId
//				+ " and to_char(starttimedate,'yyyy-MM-dd') >= "
//				+ "'"
//				+ startValue
//				+ "'"
//				+ " and to_char(starttimedate,'yyyy-MM-dd') <= "
//				+ "'"
//				+ endValue + "'" + " group by destusername";
//
//		list = reportService.getMoreRecord(sql);
//
//		for (Object[] objects : list) {
//
//			if (map.containsKey(objects[0].toString().trim())) {
//
//				CallCheckSuperPo callCheckPo = map.get(objects[0]);
//
//				// 呼叫振铃时长
//				if (objects[1] != null) {
//					callCheckPo.setIncomingRingTimeLength(Integer
//							.parseInt(objects[1].toString())
//							+ callCheckPo.getCallRingTimeLength());
//				}
//
//			}
//		}
//
//		// TODO 因为赶时间，所以平均时长暂时未计算，遍历此map，计算即可。
//
//		return map;
//
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
