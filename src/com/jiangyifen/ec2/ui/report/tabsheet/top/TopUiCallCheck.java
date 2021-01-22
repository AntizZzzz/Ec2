package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.pojo.CallCheckPo;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.CallCheck;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
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
 * REPORTPAGE话务考核
 * @author lxy
 *
 */
@SuppressWarnings("serial")
public class TopUiCallCheck extends VerticalLayout {

	private static final long serialVersionUID = 976911166705646758L;

	/*private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具*/
	
	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;
	private ComboBox cmbDept;		// 需要查询的部门

	private ComboBox comboBox;// 呼叫方向

	private Button seeButton;// 查询
	private Button exportReport;// 导出报表
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private CallCheck callCheck;

	private TabSheet tabSheet;

	private Tab tab;

	private String[] dateType = new String[] { "今天", "昨天", "本周", "上周", "本月", "上月", "精确时间"};//日期自动提示选择

	private String[] callDirection = new String[] { "呼出", "呼入", "全部" }; //呼入呼出选择

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private String titleName;// 传入的报表名
	private File file;

	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	// reportService
	private ReportService reportService = SpringContextHolder.getBean("reportService");
	// 登陆的user
	private User loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");

	// 登陆用户的
	private String domainId;

	private String sqlByDay;
	private String sql;
	private Table table;// 显示数据的table
	private Map<String, CallCheckPo> map;// table的数据源

	private String[] columnHeaders = new String[] { "部门", "工号", "姓名", "呼叫总数量","呼叫总时长", "呼叫接通数量", "呼叫接通时长", "呼叫未接通数量", "呼叫未接通时长", "漏接数量", "漏接平均振铃时长" };
	private Object[] visibleColumns = new Object[] { "dept", "empno", "name","callTotalCount", "callTotalTimeLength", "callConnectCount","callConnectTimeLength", "callUnconnectCount","callUnconnectTimeLength", "missCount", "callAvgRingTimeLength" };

	// 存放所管理的所有部门的信息
	private List<Department> deptList = new ArrayList<Department>();
	private BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(Department.class);
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
	
	public TopUiCallCheck(CallCheck callCheck) {
		this.callCheck = callCheck;

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true,true,false,true);
		this.addComponent(horizontalLayout);
		
		// 创建存放 时间范围 的布局管理器
		createImportTimeScopeHLayout();
		
		// 创建 开始时间 的布局管理器
		createStartImportTimeHLayout();

		toLabel = new Label("至");
		horizontalLayout.addComponent(toLabel);
		// 创建 截止时间 的布局管理器
		createFinishImportTimeHLayout();
		
		// 部门选择 -- 这里应该考虑选择多个部门进行查询
		cmbDept = achieveBasicUtil.getCmbDeptReport(cmbDept, loginUser, deptList, deptContainer);
		horizontalLayout.addComponent(cmbDept);

		comboBox = new ComboBox();
		for (int i = 0; i < callDirection.length; i++) {
			comboBox.addItem(callDirection[i]);
		}
		comboBox.setWidth("55px");
		comboBox.setImmediate(true);
		comboBox.setNullSelectionAllowed(false);
		comboBox.setValue(callDirection[0]);// 默认是呼出
		horizontalLayout.addComponent(comboBox);

		seeButton = new Button("查询", this, "seeResult");//查询按钮
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");
		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_TELEPHONE_TRAFFIC_CHECK)) {//导出按钮权限
			horizontalLayout.addComponent(exportReport);
		}

	}

	/**
	 *  创建存放 时间范围 的布局管理器
	 */
	private void createImportTimeScopeHLayout() {
		dateComboBox = new ComboBox();
		for (int i = 0; i < dateType.length; i++) {
			dateComboBox.addItem(dateType[i]);
		}
		dateComboBox.setWidth("80px");
		dateComboBox.setImmediate(true);
		dateComboBox.setNullSelectionAllowed(false);
		dateComboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		dateComboBox.setValue(dateType[0]);// 默认是本日
		horizontalLayout.addComponent(dateComboBox);
		
		timeScopeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String scopeValue = (String) dateComboBox.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startDate.removeListener(startTimeListener);
				endDate.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startDate.setValue(dates[0]);
				endDate.setValue(dates[1]);
				startDate.addListener(startTimeListener);
				endDate.addListener(finishTimeListener);
			}
		};
		dateComboBox.addListener(timeScopeListener);
	}
	
	/**
	 * 创建 开始时间 的布局管理器
	 */
	private void createStartImportTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		startDate = new PopupDateField();
		startDate.setWidth("100px");
		startDate.setDateFormat("yyyy-MM-dd");
		startDate.setResolution(PopupDateField.RESOLUTION_DAY);
		startDate.setImmediate(true);
		startDate.setValue(dates[0]);
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dateComboBox.removeListener(timeScopeListener);
				dateComboBox.setValue("精确时间");
				dateComboBox.addListener(timeScopeListener);
			}
		};
		startDate.addListener(startTimeListener);
		startDate.setParseErrorMessage("时间格式不合法");
		startDate.setValidationVisible(false);
		horizontalLayout.addComponent(startDate);
	}
	
	/**
	 * 创建 截止时间 的布局管理器
	 */
	private void createFinishImportTimeHLayout() {
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		endDate = new PopupDateField();
		endDate.setWidth("100px");
		endDate.setDateFormat("yyyy-MM-dd");
		endDate.setResolution(PopupDateField.RESOLUTION_DAY);
		endDate.setImmediate(true);
		endDate.setValue(dates[1]);
		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				dateComboBox.removeListener(timeScopeListener);
				dateComboBox.setValue("精确时间");
				dateComboBox.addListener(timeScopeListener);
			}
		};
		endDate.addListener(finishTimeListener);
		horizontalLayout.addComponent(endDate);
	}
	

	public void seeResult(ClickEvent event) {//获取结果
		tabSheet = callCheck.getTabSheet();
		domainId = loginUser.getDomain().getId().toString();// 登陆用户域
		Department dept = (Department) cmbDept.getValue();
		if(dept == null) {
			event.getComponent().getWindow().showNotification("请选择要查询的部门");
			return;
		}
		
		String callDirectionValue = comboBox.getValue().toString().trim();
		startValue = format.format(startDate.getValue());// 获得startDate的值
		endValue = format.format(endDate.getValue());// 获得endDate的值
		// 
		if ("呼出".equals(callDirectionValue)) {
			map = getOutgoingCallCheckPo(startValue, endValue, dept);
			table = new Table("话务考核");
			BeanItemContainer<CallCheckPo> container = new BeanItemContainer<CallCheckPo>(CallCheckPo.class);
			Set<Entry<String, CallCheckPo>> set = map.entrySet();
			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
			while (iterator.hasNext()) {
				Entry<String, CallCheckPo> entry = iterator.next();
				CallCheckPo callCheckPo = entry.getValue();
				container.addBean(callCheckPo);
			}
			table.setContainerDataSource(container);
			table.setSizeFull();
			table.setImmediate(true);
			table.setNullSelectionAllowed(true);
			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			table.setStyleName("striped");
			table.setSelectable(true);
			table.setVisibleColumns(visibleColumns);
			table.setColumnHeaders(columnHeaders);
			tab = tabSheet.addTab(table, "呼出统计["+startValue+"] - ["+endValue+"]", null);
			tab.setClosable(true);
			tabSheet.setSelectedTab(tab);
		} else if ("呼入".equals(callDirectionValue)) {
			map = getIncomingCallCheckPo(startValue, endValue, dept);
			table = new Table("话务考核");
			BeanItemContainer<CallCheckPo> container = new BeanItemContainer<CallCheckPo>(CallCheckPo.class);
			Set<Entry<String, CallCheckPo>> set = map.entrySet();
			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
			while (iterator.hasNext()) {
				Entry<String, CallCheckPo> entry = iterator.next();
				CallCheckPo callCheckPo = entry.getValue();
				container.addBean(callCheckPo);
			}
			table.setContainerDataSource(container);
			table.setSizeFull();
			table.setImmediate(true);
			table.setNullSelectionAllowed(true);
			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			table.setStyleName("striped");
			table.setSelectable(true);
			table.setVisibleColumns(visibleColumns);
			table.setColumnHeaders(columnHeaders);
			tab = tabSheet.addTab(table, "呼入统计["+startValue+"] - ["+endValue+"]", null);
			tab.setClosable(true);
			tabSheet.setSelectedTab(tab);

		} else if ("全部".equals(callDirectionValue)) {
			map = getTotalCallCheckPo(startValue, endValue, dept);
			table = new Table("话务考核");
			BeanItemContainer<CallCheckPo> container = new BeanItemContainer<CallCheckPo>(CallCheckPo.class);
			Set<Entry<String, CallCheckPo>> set = map.entrySet();
			Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
			while (iterator.hasNext()) {
				Entry<String, CallCheckPo> entry = iterator.next();
				CallCheckPo callCheckPo = entry.getValue();
				// 设置时长格式
				callCheckPo.setCallTotalTimeLength(DateUtil.getTime(Integer.parseInt(callCheckPo.getCallTotalTimeLength() == null ? "0": callCheckPo.getCallTotalTimeLength())));
				callCheckPo.setCallConnectTimeLength(DateUtil.getTime(Integer.parseInt(callCheckPo.getCallConnectTimeLength() == null ? "0": callCheckPo.getCallConnectTimeLength())));
				callCheckPo.setCallUnconnectTimeLength(DateUtil.getTime(Integer.parseInt(callCheckPo.getCallUnconnectTimeLength() == null ? "0": callCheckPo.getCallUnconnectTimeLength())));
				container.addBean(callCheckPo);
			}
			table.setContainerDataSource(container);
			table.setSizeFull();
			table.setImmediate(true);
			table.setNullSelectionAllowed(true);
			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			table.setStyleName("striped");
			table.setSelectable(true);
			table.setVisibleColumns(visibleColumns);
			table.setColumnHeaders(columnHeaders);
			tab = tabSheet.addTab(table, "全部统计["+startValue+"] - ["+endValue+"]", null);
			tab.setClosable(true);
			tabSheet.setSelectedTab(tab);
		}
	}

	/**
	 * 呼出统计
	 * @param startValue
	 * @param endValue
	 * @return
	 */
	public Map<String, CallCheckPo> getOutgoingCallCheckPo(String startValue,String endValue, Department dept) {// 查看所属部门
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.username,d.name,u.realname from ec2_user as u,ec2_department as d where u.department_id = d.id and u.domain_id = ");
		sbQuery.append(domainId);
		sbQuery.append(" ");
		sbQuery.append(getSqlByDept(dept, "d.id"));
		sql = sbQuery.toString();
		List<Object[]> list = reportService.getMoreRecord(sql);

		Map<String, CallCheckPo> srcMap = new HashMap<String, CallCheckPo>();
		for (Object[] objects : list) {
			CallCheckPo callCheckPo = new CallCheckPo();
			if (objects[0] != null) {// 用户名
				callCheckPo.setEmpno(objects[0].toString());
			}
			if (objects[1] != null) {// 部门
				callCheckPo.setDept(objects[1].toString());
			}
			if (objects[2] != null) {// 姓名
				callCheckPo.setName(objects[2].toString());
			}
			srcMap.put(objects[0].toString(), callCheckPo);
		}
		
		// 呼出总数，时长
		sbQuery.setLength(0);
		sbQuery.append("select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count from cdr where srcusername != '' and cdrdirection = '1' and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by srcusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = srcMap.get(objects[0]);
				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1].toString()));// 呼出总数量
				callCheckPo.setCallTotalTimeLength(DateUtil.getTime(Integer.parseInt(objects[2].toString())));// 呼出总时长
			}
		}
		// 呼出接通数量，时长
		sbQuery.setLength(0);
		sbQuery.append("select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count from cdr where srcusername != '' and cdrdirection = '1' and isbridged is not null and isbridged = true and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by srcusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = srcMap.get(objects[0]);
				if (objects[1] != null) {// 呼出接通数量
					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1].toString()));
				}
				if (objects[2] != null) {// 呼出接通时长
					callCheckPo.setCallConnectTimeLength(DateUtil.getTime(Integer.parseInt(objects[2].toString())));
				}
			}
		}

		// 呼出未接通数量，时长
		sbQuery.setLength(0);
		/*sbQuery.append("select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count from cdr where srcusername != '' and cdrdirection = '1' and (isbridged is null or isbridged = false) and domainid = ");*/
		sbQuery.append("select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count from cdr where srcusername != '' and cdrdirection = '1' and (isbridged is null or isbridged = false) and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by srcusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = srcMap.get(objects[0]);
				if (objects[1] != null) {// 呼出未接通数量
					callCheckPo.setCallUnconnectCount(Integer.parseInt(objects[1].toString()));
				}
				if (objects[2] != null) {// 呼出未接通时长
					callCheckPo.setCallUnconnectTimeLength(DateUtil.getTime(Integer.parseInt(objects[2].toString())));
				}
			}
		}
		return srcMap;
	}

	/**
	 * 呼入使用destdeptid
	 * @param startValue
	 * @param endValue
	 * @return
	 */
	public Map<String, CallCheckPo> getIncomingCallCheckPo(String startValue,String endValue, Department dept) {
		// 查看全部部门
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.username,d.name,u.realname from ec2_user as u,ec2_department as d where u.department_id = d.id and u.domain_id = ");
		sbQuery.append(domainId);
		sbQuery.append(getSqlByDept(dept, "d.id"));
		sql = sbQuery.toString();
		List<Object[]> list = reportService.getMoreRecord(sql);
		Map<String, CallCheckPo> destMap = new HashMap<String, CallCheckPo>();
		for (Object[] objects : list) {
			CallCheckPo callCheckPo = new CallCheckPo();
			if (objects[0] != null) {// 工号
				callCheckPo.setEmpno(objects[0].toString());
			}
			if (objects[1] != null) {// 部门
				callCheckPo.setDept(objects[1].toString());
			}
			if (objects[2] != null) {// 姓名
				callCheckPo.setName(objects[2].toString());
			}
			destMap.put(objects[0].toString(), callCheckPo);
		}
		// 呼入总数，时长
		sbQuery.setLength(0);
		sbQuery.append("select  destusername as destusername,count(*) as count,sum(billableseconds) as count from cdr where destusername != '' and cdrdirection = '2' and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by destusername");
		sql = sbQuery.toString(); 
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (destMap.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = destMap.get(objects[0]);
				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1].toString()));// 呼入总数量
				callCheckPo.setCallTotalTimeLength(DateUtil.getTime(Integer.parseInt(objects[2].toString())));// 呼入总时长
			}
		}
		// 呼入接通数量，时长
		sbQuery.setLength(0);
		sbQuery.append("select  destusername as destusername,count(*) as count,sum(billableseconds) as count from cdr where destusername != '' and cdrdirection = '2' and isbridged is not null and isbridged = true and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by destusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (destMap.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = destMap.get(objects[0]);
				if (objects[1] != null) {// 呼入接通数量
					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1].toString()));
				}
				if (objects[2] != null) {// 呼入接通时长
					callCheckPo.setCallConnectTimeLength(DateUtil.getTime(Integer.parseInt(objects[2].toString())));
				}
			}
		}

		// 呼入未接通数量，时长
		sbQuery.setLength(0);
		sbQuery.append("select  destusername as destusername,count(*) as count,sum(billableseconds) as count from cdr where destusername != '' and cdrdirection = '2' and (isbridged is null or isbridged = false) and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by destusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (destMap.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = destMap.get(objects[0]);
				if (objects[1] != null) {// 呼入未接通数量
					callCheckPo.setCallUnconnectCount(Integer.parseInt(objects[1].toString()));
				}
				if (objects[2] != null) {// 呼入未接通时长
					callCheckPo.setCallUnconnectTimeLength(DateUtil.getTime(Integer.parseInt(objects[2].toString())));
				}
			}
		}

		// 呼入漏接数量，平均振铃时长
		sbQuery.setLength(0);
		sbQuery.append("select u.username,count(*),round(avg(ringingduration),2) from ec2_miss_call_log as m,ec2_user as u where u.id = m.destuserid and ringingstatetime >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and ringingstatetime <= '");
		sbQuery.append(endValue);
		sbQuery.append("'  group by u.username");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (destMap.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = destMap.get(objects[0]);
				callCheckPo.setMissCount(Integer.parseInt(objects[1].toString()));// 设置呼入漏接数
				callCheckPo.setCallAvgRingTimeLength(objects[2].toString());// 设置呼入平均振铃时长
			}
		}
		sbQuery = null;
		return destMap;
	}

	/**
	 * 总的
	 * @param startValue
	 * @param endValue
	 * @return
	 */
	public Map<String, CallCheckPo> getTotalCallCheckPo(String startValue, String endValue, Department dept) {
		// 查看全部部门
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.username,d.name,u.realname from ec2_user as u,ec2_department as d  where u.department_id = d.id and u.domain_id = ");
		sbQuery.append(domainId);
		sbQuery.append(getSqlByDept(dept, "d.id"));
		sql = sbQuery.toString();
		Map<String, CallCheckPo> map = new HashMap<String, CallCheckPo>();
		List<Object[]> destList = reportService.getMoreRecord(sql);
		for (Object[] objects : destList) {
			CallCheckPo callCheckPo = new CallCheckPo();
			if (objects[0] != null) {// 工号
				callCheckPo.setEmpno(objects[0].toString());
			}
			if (objects[1] != null) {// 部门
				callCheckPo.setDept(objects[1].toString());
			}
			if (objects[2] != null) {// 姓名
				callCheckPo.setName(objects[2].toString());
			}
			map.put(objects[0].toString(), callCheckPo);
		}

		// 呼叫总数，时长
		sbQuery.setLength(0);
		sbQuery.append("select  destusername as destusername,count(*) as count,sum(billableseconds) as count from cdr where destusername != '' and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by destusername");
		sql = sbQuery.toString(); 
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = map.get(objects[0]);
				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1].toString()) + callCheckPo.getCallTotalCount());// 呼叫总数量
				// 呼叫总时长
				callCheckPo.setCallTotalTimeLength(String.valueOf(Integer.parseInt(objects[2].toString())+ Integer.parseInt(callCheckPo.getCallTotalTimeLength() == null ? "0": callCheckPo.getCallTotalTimeLength())));

			}
		}

		// 呼叫总数，时长
		sbQuery.setLength(0);
		sbQuery.append("select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count from cdr where srcusername != '' and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by srcusername");
		sql = sbQuery.toString(); 
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = map.get(objects[0]);
				// 呼叫总数量
				callCheckPo.setCallTotalCount(Integer.parseInt(objects[1].toString()) + callCheckPo.getCallTotalCount());
				// 呼叫总时长
				callCheckPo.setCallTotalTimeLength(String.valueOf(Integer.parseInt(objects[2].toString())+ Integer.parseInt(callCheckPo.getCallTotalTimeLength() == null ? "0": callCheckPo.getCallTotalTimeLength())));
			}
		}

		// 呼叫接通数量，时长
		sbQuery.setLength(0);
		sbQuery.append("select  destusername as destusername,count(*) as count,sum(billableseconds) as count from cdr where destusername != '' and isbridged is not null and isbridged = true and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by destusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = map.get(objects[0]);
				// 呼叫接通数量
				if (objects[1] != null) {
					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1].toString()) + callCheckPo.getCallConnectCount());
				}
				// 呼叫接通时长
				callCheckPo.setCallConnectTimeLength(String.valueOf(Integer.parseInt(objects[2].toString())+ Integer.parseInt(callCheckPo.getCallConnectTimeLength() == null ? "0": callCheckPo.getCallConnectTimeLength())));
			}
		}

		// 呼叫接通数量，时长
		sbQuery.setLength(0);
		sbQuery.append("select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count from cdr where srcusername != '' and isbridged is not null and isbridged = true and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by srcusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = map.get(objects[0]);
				// 呼叫接通数量
				if (objects[1] != null) {
					callCheckPo.setCallConnectCount(Integer.parseInt(objects[1].toString()) + callCheckPo.getCallConnectCount());
				}
				// 呼叫接通时长
				callCheckPo.setCallConnectTimeLength(String.valueOf(Integer.parseInt(objects[2].toString())	+ Integer.parseInt(callCheckPo.getCallConnectTimeLength() == null ? "0": callCheckPo.getCallConnectTimeLength())));
			}
		}

		// 呼叫未接通数量，时长
		sbQuery.setLength(0);
		sbQuery.append("select  destusername as destusername,count(*) as count,sum(billableseconds) as count from cdr where destusername != '' and (isbridged is null or isbridged = false) and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by destusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = map.get(objects[0]);
				// 呼叫未接通数量
				if (objects[1] != null) {
					callCheckPo.setCallUnconnectCount(Integer.parseInt(objects[1].toString())+ callCheckPo.getCallUnconnectCount());
				}
				// 呼叫未接通时长
				callCheckPo.setCallUnconnectTimeLength(String.valueOf(Integer.parseInt(objects[2].toString())+ Integer.parseInt(callCheckPo.getCallUnconnectTimeLength() == null ? "0": callCheckPo.getCallUnconnectTimeLength())));
			}
		}

		// 呼叫未接通数量，时长
		sbQuery.setLength(0);
		sbQuery.append("select  srcusername as srcusername,count(*) as count,sum(billableseconds) as count from cdr where srcusername != '' and (isbridged is null or isbridged = false) and domainid = ");
		sbQuery.append(domainId);
		sbQuery.append(" and starttimedate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and starttimedate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' group by srcusername");
		sql = sbQuery.toString();
		list = reportService.getMoreRecord(sql);
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				CallCheckPo callCheckPo = map.get(objects[0]);
				if (objects[1] != null) {// 呼叫未接通数量
					callCheckPo.setCallUnconnectCount(Integer.parseInt(objects[1].toString()) + callCheckPo.getCallUnconnectCount());
				}// 呼叫未接通时长
				callCheckPo.setCallUnconnectTimeLength(String.valueOf(Integer.parseInt(objects[2].toString()) + Integer.parseInt(callCheckPo.getCallUnconnectTimeLength() == null ? "0"	: callCheckPo.getCallUnconnectTimeLength())));
			}
		}
		return map;
	}

	// 导出报表事件
	public void exportReport(ClickEvent event) throws FileNotFoundException {
		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询");
			return;
		}
		// 设置文件名
		titleName = tab.getCaption();
		// 设置数据源
		list = getToTableList(map);
		// 设置列名
		List<String> columnN = new ArrayList<String>();
		for (String str : columnHeaders) {
			columnN.add(str);
		}
		columnNames = columnN;
		try{
			if(StringUtils.isNotEmpty(titleName)){
				if(titleName.indexOf("[") > 1){ 
					titleName = titleName.substring(0,titleName.indexOf("["));
				}
			}
		}catch (Exception e) {
			titleName = tab.getCaption();
		}
		// 生成excel file
		file = ExportExcelUtil.exportExcel(titleName, list, columnNames,startValue, endValue);

		// ======================chb 操作日志======================//
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null){operationLog.setFilePath(file.getAbsolutePath());}
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(SpringContextHolder.getLoginUser().getUsername());
		operationLog.setRealName(SpringContextHolder.getLoginUser().getRealName());
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("导出话务考核报表");
		operationLog.setProgrammerSee(sqlByDay);
		CommonService commonService = SpringContextHolder.getBean("commonService");
		commonService.save(operationLog);
		// ======================chb 操作日志======================//
		Resource resource = new FileResource(file, event.getComponent().getApplication());
		final Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setSource(resource);
		this.addComponent(downloader);

		downloader.addListener(new RepaintRequestListener() {
			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
			}
		});

	}

	public List<Object[]> getToTableList(Map<String, CallCheckPo> map) {
		List<Object[]> list = new ArrayList<Object[]>();
		Set<Entry<String, CallCheckPo>> set = map.entrySet();
		Iterator<Entry<String, CallCheckPo>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Entry<String, CallCheckPo> entry = iterator.next();
			CallCheckPo callCheckPo = entry.getValue();
			Object[] objects = new Object[11];	// 创建Object[],size固定死了
			objects[0] = callCheckPo.getDept();	// 部门
			objects[1] = callCheckPo.getEmpno();// 工号
			objects[2] = callCheckPo.getName();	// 姓名
			objects[3] = callCheckPo.getCallTotalCount();		// 呼叫总数量
			objects[4] = callCheckPo.getCallTotalTimeLength();	// 呼叫总时长
			objects[5] = callCheckPo.getCallConnectCount();		// 呼叫接通数量
			objects[6] = callCheckPo.getCallConnectTimeLength();// 呼叫接通时长
			objects[7] = callCheckPo.getCallUnconnectCount();	// 呼叫未接通数量
			objects[8] = callCheckPo.getCallUnconnectTimeLength();// 呼叫未接通时长
			objects[9] = callCheckPo.getMissCount();			//漏接数量
			objects[10] = callCheckPo.getCallAvgRingTimeLength();//漏接平均振铃时长
			list.add(objects);
		}
		return list;
	}

	public String getStartValue() {
		return startValue;
	}

	public String getEndValue() {
		return endValue;
	}
	
	/**
	 * jinht
	 * 根据部门信息进行判断查询的SQL语句
	 * @param dept      部门
	 * @param str       要查询的字段
	 * @return String   sql 语句
	 */
	private String getSqlByDept(Department dept, String str) {
		StringBuffer sbSql = new StringBuffer();
		if(dept != null) {
			if(dept.getId().equals(0L)) {
				sbSql.append(" and ");
				sbSql.append(str);
				sbSql.append(" in (");
				sbSql.append(achieveBasicUtil.getDeptIds());
				sbSql.append(") ");
			} else {
				sbSql.append(" and ");
				sbSql.append(str);
				sbSql.append("=");
				sbSql.append(dept.getId());
			}
		} else {
			sbSql.append(" and 1>2");
		}
		sbSql.append(" ");
		return sbSql.toString();
	}
	
}