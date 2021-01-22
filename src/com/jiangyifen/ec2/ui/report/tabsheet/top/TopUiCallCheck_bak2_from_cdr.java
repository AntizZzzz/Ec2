//package com.jiangyifen.ec2.ui.report.tabsheet.top;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//
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
//import com.ibm.icu.impl.Normalizer2Impl.ReorderingBuffer;
//import com.jiangyifen.ec2.entity.Department;
//import com.jiangyifen.ec2.entity.OperationLog;
//import com.jiangyifen.ec2.entity.Role;
//import com.jiangyifen.ec2.entity.User;
//import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
//import com.jiangyifen.ec2.report.pojo.CallCheckPo;
//import com.jiangyifen.ec2.service.common.CommonService;
//import com.jiangyifen.ec2.service.mgr.ReportService;
//import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
//
//import com.jiangyifen.ec2.ui.report.tabsheet.CallCheck;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
//
//import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
//import com.jiangyifen.ec2.utils.SpringContextHolder;
//import com.vaadin.data.Property.ValueChangeEvent;
//import com.vaadin.data.util.BeanItemContainer;
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
//public class TopUiCallCheck_bak2_from_cdr extends VerticalLayout {
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
//	private long deptId;
//	// 登陆用户的domainid
//	private String domainId;
//	private List<Long> deptidList;
//	private String deptIds;
//
//	private String sqlByDay;
//	private String sql;
//	private Table table;// 显示数据的table
//	private Map<String, CallCheckPo> map;// table的数据源
//
//	private String[] columnHeaders = new String[] { "部门", "工号", "姓名", "呼叫总数量",
//			"呼叫总时长", "呼叫接通数量", "呼叫接通时长", "呼叫未接通数量", "呼叫未接通时长" };
//	private Object[] visibleColumns = new Object[] { "dept", "empno", "name",
//			"callTotalCount", "callTotalTimeLength", "callConnectCount",
//			"callConnectTimeLength", "callUnconnectCount",
//			"callUnconnectTimeLength" };
//
//	public TopUiCallCheck_bak2_from_cdr(CallCheck callCheck) {
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
//		deptId = loginUser.getDepartment().getId();
//
//		// 登陆用户domainId
//		domainId = loginUser.getDomain().getId().toString();
//
//		// 登陆用户所属角色所管理的部门id
//		deptidList = new ArrayList<Long>();
//		deptidList.add(deptId);
//		Set<Role> roles = loginUser.getRoles();
//		for (Role role : roles) {
//			Set<Department> departments = role.getDepartments();
//			for (Department department : departments) {
//				deptidList.add(department.getId());
//			}
//		}
//
//		// id拼在一起
//		deptIds = deptidList.toString().substring(1,
//				(deptidList.toString().length() - 1));
//
//		String callDirectionValue = comboBox.getValue().toString().trim();
//
//		startValue = format.format(startDate.getValue());// 获得startDate的值
//		endValue = format.format(endDate.getValue());// 获得endDate的值
//
//		// 呼出
//		if ("呼出".equals(callDirectionValue)) {
//
//			// TODO 以下代码可以封装成一个函数，现在此代码有可能变动，所以暂未封装。
//			// TODO 计时开始
//			long start = System.currentTimeMillis();
//
//			map = getOutgoingCallCheckPo(startValue, endValue);
//
//			table = new Table("话务考核");
//			BeanItemContainer<CallCheckPo> container = new BeanItemContainer<CallCheckPo>(
//					CallCheckPo.class);
//
//			Set<Entry<String, CallCheckPo>> set = map.entrySet();
//			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
//			while (iterator.hasNext()) {
//				Entry<String, CallCheckPo> entry = iterator.next();
//				CallCheckPo callCheckPo = entry.getValue();
//				container.addBean(callCheckPo);
//			}
//
//			table.setContainerDataSource(container);
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
//			map = getIncomingCallCheckPo(startValue, endValue);
//
//			table = new Table("话务考核");
//			BeanItemContainer<CallCheckPo> container = new BeanItemContainer<CallCheckPo>(
//					CallCheckPo.class);
//
//			Set<Entry<String, CallCheckPo>> set = map.entrySet();
//			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
//			while (iterator.hasNext()) {
//				Entry<String, CallCheckPo> entry = iterator.next();
//				CallCheckPo callCheckPo = entry.getValue();
//				container.addBean(callCheckPo);
//			}
//
//			table.setContainerDataSource(container);
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
//
//			// TODO 计时开始
//			long start = System.currentTimeMillis();
//
//			map = getTotalCallCheckPo(startValue, endValue);
//
//			table = new Table("话务考核");
//			BeanItemContainer<CallCheckPo> container = new BeanItemContainer<CallCheckPo>(
//					CallCheckPo.class);
//
//			Set<Entry<String, CallCheckPo>> set = map.entrySet();
//			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
//			while (iterator.hasNext()) {
//				Entry<String, CallCheckPo> entry = iterator.next();
//				CallCheckPo callCheckPo = entry.getValue();
//
//				// 设置时长格式
//				callCheckPo
//						.setCallTotalTimeLength(DateUtil.getTime(Integer
//								.parseInt(callCheckPo.getCallTotalTimeLength() == null ? "0"
//										: callCheckPo.getCallTotalTimeLength())));
//				callCheckPo
//						.setCallConnectTimeLength(DateUtil.getTime(Integer.parseInt(callCheckPo
//								.getCallConnectTimeLength() == null ? "0"
//								: callCheckPo.getCallConnectTimeLength())));
//				callCheckPo
//						.setCallUnconnectTimeLength(DateUtil.getTime(Integer.parseInt(callCheckPo
//								.getCallUnconnectTimeLength() == null ? "0"
//								: callCheckPo.getCallUnconnectTimeLength())));
//				container.addBean(callCheckPo);
//			}
//
//			table.setContainerDataSource(container);
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
//	public Map<String, CallCheckPo> getOutgoingCallCheckPo(String startValue,
//			String endValue) {
//
//		// 查看全部部门
//		// sql =
//		// "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//		// + "("
//		// +
//		// "select srcusername from cdr where srcusername !='' and domainid = "
//		// + domainId
//		// + " group by  srcusername "
//		// + ") as t"
//		// + " where d.id = u.department_id and t.srcusername = u.username";
//		// 仅查看管理员所属部门
//		// sql =
//		// "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//		// + "("
//		// +
//		// "select srcusername from cdr where srcusername !='' and domainid = "
//		// + domainId
//		// + " group by  srcusername "
//		// + ") as t"
//		// +
//		// " where d.id = u.department_id and t.srcusername = u.username and d.id = "
//		// + deptId;
//
//		// 管理员所属部门和管辖部门
//		sql = "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select srcusername from cdr where srcusername !='' and domainid = "
//				+ domainId
//				+ " group by  srcusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.srcusername = u.username and d.id in("
//				+ deptIds + ")";
//
//		List<Object[]> list = reportService.getMoreRecord(sql);
//
//		// 存入实体
//		Map<String, CallCheckPo> srcMap = new HashMap<String, CallCheckPo>();
//
//		for (Object[] objects : list) {
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
//			srcMap.put(objects[0].toString(), callCheckPo);
//
//		}
//
//		// 呼出总数，时长
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
//			if (srcMap.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = srcMap.get(objects[0]);
//				// 呼出总数量
//				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
//						.toString()));
//				// 呼出总时长
//				callCheckPo.setCallTotalTimeLength(DateUtil.getTime(Integer
//						.parseInt(objects[2].toString())));
//			}
//		}
//
//		// 呼出接通数量，时长
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
//			if (srcMap.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = srcMap.get(objects[0]);
//				// 呼出接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1]
//							.toString()));
//				}
//				// 呼出接通时长
//				if (objects[2] != null) {
//					callCheckPo.setCallConnectTimeLength(DateUtil
//							.getTime(Integer.parseInt(objects[2].toString())));
//				}
//			}
//		}
//
//		// 呼出未接通数量，时长
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
//			if (srcMap.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = srcMap.get(objects[0]);
//
//				// 呼出未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallUnconnectCount(Integer
//							.parseInt(objects[1].toString()));
//				}
//				// 呼出未接通时长
//				if (objects[2] != null) {
//					callCheckPo.setCallUnconnectTimeLength(DateUtil
//							.getTime(Integer.parseInt(objects[2].toString())));
//				}
//
//			}
//		}
//
//		return srcMap;
//
//	}
//
//	public Map<String, CallCheckPo> getIncomingCallCheckPo(String startValue,
//			String endValue) {
//
//		// 查看所有部门
//		// sql =
//		// "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//		// + "("
//		// +
//		// "select destusername from cdr where destusername !='' and domainid = "
//		// + domainId
//		// + " group by  destusername "
//		// + ") as t"
//		// + " where d.id = u.department_id and t.destusername = u.username";
//		// 仅查看管理员所属部门
//		// sql =
//		// "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//		// + "("
//		// +
//		// "select destusername from cdr where destusername !='' and domainid = "
//		// + domainId
//		// + " group by  destusername "
//		// + ") as t"
//		// +
//		// " where d.id = u.department_id and t.destusername = u.username and d.id = "
//		// + deptId;
//
//		sql = "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select destusername from cdr where destusername !='' and domainid = "
//				+ domainId
//				+ " group by  destusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.destusername = u.username and d.id in("
//				+ deptIds + ")";
//
//		List<Object[]> list = reportService.getMoreRecord(sql);
//
//		// 存入实体
//		Map<String, CallCheckPo> destMap = new HashMap<String, CallCheckPo>();
//
//		for (Object[] objects : list) {
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
//			destMap.put(objects[0].toString(), callCheckPo);
//
//		}
//
//		// 呼入总数，时长
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
//			if (destMap.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = destMap.get(objects[0]);
//				// 呼入总数量
//				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
//						.toString()));
//				// 呼入总时长
//				callCheckPo.setCallTotalTimeLength(DateUtil.getTime(Integer
//						.parseInt(objects[2].toString())));
//			}
//		}
//
//		// 呼入接通数量，时长
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
//			if (destMap.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = destMap.get(objects[0]);
//				// 呼入接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1]
//							.toString()));
//				}
//				// 呼入接通时长
//				if (objects[2] != null) {
//					callCheckPo.setCallConnectTimeLength(DateUtil
//							.getTime(Integer.parseInt(objects[2].toString())));
//				}
//			}
//		}
//
//		// 呼入未接通数量，时长
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
//			if (destMap.containsKey(objects[0].toString().trim())) {
//
//				CallCheckPo callCheckPo = destMap.get(objects[0]);
//
//				// 呼入未接通数量
//				if (objects[1] != null) {
//					callCheckPo.setCallUnconnectCount(Integer
//							.parseInt(objects[1].toString()));
//				}
//				// 呼入未接通时长
//				if (objects[2] != null) {
//					callCheckPo.setCallUnconnectTimeLength(DateUtil
//							.getTime(Integer.parseInt(objects[2].toString())));
//				}
//
//			}
//		}
//
//		return destMap;
//
//	}
//
//	public Map<String, CallCheckPo> getTotalCallCheckPo(String startValue,
//			String endValue) {
//
//		// 存入实体
//		Map<String, CallCheckPo> map = new HashMap<String, CallCheckPo>();
//
//		// 查看所有部门
//		// sql =
//		// "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//		// + "("
//		// +
//		// "select destusername from cdr where destusername !='' and domainid = "
//		// + domainId
//		// + " group by  destusername "
//		// + ") as t"
//		// + " where d.id = u.department_id and t.destusername = u.username";
//		// 仅查看管理员所属部门
//		// sql =
//		// "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//		// + "("
//		// +
//		// "select destusername from cdr where destusername !='' and domainid = "
//		// + domainId
//		// + " group by  destusername "
//		// + ") as t"
//		// +
//		// " where d.id = u.department_id and t.destusername = u.username and d.id = "
//		// + deptId;
//
//		sql = "select t.destusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select destusername from cdr where destusername !='' and domainid = "
//				+ domainId
//				+ " group by  destusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.destusername = u.username and d.id in("
//				+ deptIds + ")";
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
//		// 查看所有部门
//		// sql =
//		// "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//		// + "("
//		// +
//		// "select srcusername from cdr where srcusername !='' and domainid = "
//		// + domainId
//		// + " group by  srcusername "
//		// + ") as t"
//		// + " where d.id = u.department_id and t.srcusername = u.username";
//
//		// 仅查看管理员所属部门
//		// sql =
//		// "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//		// + "("
//		// +
//		// "select srcusername from cdr where srcusername !='' and domainid = "
//		// + domainId
//		// + " group by  srcusername "
//		// + ") as t"
//		// +
//		// " where d.id = u.department_id and t.srcusername = u.username and d.id = "
//		// + deptId;
//
//		sql = "select t.srcusername,d.name,u.realname from ec2_department as d,ec2_user as u,"
//				+ "("
//				+ "select srcusername from cdr where srcusername !='' and domainid = "
//				+ domainId
//				+ " group by  srcusername "
//				+ ") as t"
//				+ " where d.id = u.department_id and t.srcusername = u.username and d.id in("
//				+ deptIds + ")";
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
//
//				callCheckPo
//						.setCallTotalTimeLength(String.valueOf(Integer
//								.parseInt(objects[2].toString())
//								+ Integer.parseInt(callCheckPo
//										.getCallTotalTimeLength() == null ? "0"
//										: callCheckPo.getCallTotalTimeLength())));
//
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
//				callCheckPo
//						.setCallTotalTimeLength(String.valueOf(Integer
//								.parseInt(objects[2].toString())
//								+ Integer.parseInt(callCheckPo
//										.getCallTotalTimeLength() == null ? "0"
//										: callCheckPo.getCallTotalTimeLength())));
//
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
//
//				callCheckPo.setCallConnectTimeLength(String.valueOf(Integer
//						.parseInt(objects[2].toString())
//						+ Integer.parseInt(callCheckPo
//								.getCallConnectTimeLength() == null ? "0"
//								: callCheckPo.getCallConnectTimeLength())));
//
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
//
//				callCheckPo.setCallConnectTimeLength(String.valueOf(Integer
//						.parseInt(objects[2].toString())
//						+ Integer.parseInt(callCheckPo
//								.getCallConnectTimeLength() == null ? "0"
//								: callCheckPo.getCallConnectTimeLength())));
//
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
//
//				callCheckPo.setCallUnconnectTimeLength(String.valueOf(Integer
//						.parseInt(objects[2].toString())
//						+ Integer.parseInt(callCheckPo
//								.getCallUnconnectTimeLength() == null ? "0"
//								: callCheckPo.getCallUnconnectTimeLength())));
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
//
//				callCheckPo.setCallUnconnectTimeLength(String.valueOf(Integer
//						.parseInt(objects[2].toString())
//						+ Integer.parseInt(callCheckPo
//								.getCallUnconnectTimeLength() == null ? "0"
//								: callCheckPo.getCallUnconnectTimeLength())));
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
//		// 设置文件名
//		titleName = tab.getCaption();
//		// 设置数据源
//		list = getToTableList(map);
//		// 设置列名
//		List<String> columnN = new ArrayList<String>();
//		for (String str : columnHeaders) {
//			columnN.add(str);
//		}
//		columnNames = columnN;
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
//	public List<Object[]> getToTableList(Map<String, CallCheckPo> map) {
//		// 创建list
//		List<Object[]> list = new ArrayList<Object[]>();
//
//		// 编历map
//		Set<Entry<String, CallCheckPo>> set = map.entrySet();
//		Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
//		while (iterator.hasNext()) {
//
//			Entry<String, CallCheckPo> entry = iterator.next();
//			CallCheckPo callCheckPo = entry.getValue();
//
//			// 创建Object[],size固定死了
//			Object[] objects = new Object[9];
//			// 部门
//			objects[0] = callCheckPo.getDept();
//			// 工号
//			objects[1] = callCheckPo.getEmpno();
//			// 姓名
//			objects[2] = callCheckPo.getName();
//			// 呼叫总数量
//			objects[3] = callCheckPo.getCallTotalCount();
//			// 呼叫总时长
//			objects[4] = callCheckPo.getCallTotalTimeLength();
//			// 呼叫接通数量
//			objects[5] = callCheckPo.getCallConnectCount();
//			// 呼叫接通时长
//			objects[6] = callCheckPo.getCallConnectTimeLength();
//			// 呼叫未接通数量
//			objects[7] = callCheckPo.getCallUnconnectCount();
//			// 呼叫未接通时长
//			objects[8] = callCheckPo.getCallUnconnectTimeLength();
//
//			list.add(objects);
//
//		}
//
//		return list;
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
