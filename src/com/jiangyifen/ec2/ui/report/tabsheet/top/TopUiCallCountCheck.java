 package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.pojo.CallCountCheckPo;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.CallCheckByCallTimeLength;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * REPORTPAGE通次考核
 * @author lxy
 *
 */
@SuppressWarnings("serial")
public class TopUiCallCountCheck extends VerticalLayout {
	
	private HorizontalLayout horizontalLayout;       // 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;                   // 日期组件
 	private PopupDateField startDate;                // 开始日期
	private Label toLabel;                           // 结束日期label              
	private PopupDateField endDate;                  // 结束日期
	private ComboBox comboBox;                       // 呼入，呼出选项
	private TextField fromField;                     // 开始的秒数
	private Label label;;                            // 至
	private TextField toField;                       // 结束的秒数
	private Button seeButton;                        // 查看按钮
	private Button exportReport;                     // 导出报表
	private ComboBox cmbDept;		// 需要查询的部门
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
       
	private File file = null;                        // 下载的文件

	private CallCheckByCallTimeLength byCallTimeLength;                              // 实体
	private TabSheet tabSheet;                                                       // 存放tab的tabSheet
	private Tab tab;                                                                 // tab
	private String[] dateType = new String[] { "今天", "昨天", "本周", "上周", "本月", "上月", "精确时间"};          // 日期范围
	private String[] callDirection = new String[] { "呼出", "呼入", "全部" };            // 查询范围
	
	private String[] visibleColumns = new String[] { "dept", "empno", "name","callTotalCount", "callConnectCount", "callUnconnectCount"};  // table的列名
	private String[] columnHeaders = new String[] { "部门", "工号", "姓名", "呼叫总数量","呼叫接通数量", "呼叫未接通数量"};                                // table的列名

	private List<Object[]> list;           // 从数据库获得新的数据
	private String startValue;             // startDate的值
	private String endValue;               // endDate的值
	private List<String> columnNames;      // table的列名
	private String titleName;              // 传入的报表名
	private SimpleDateFormat format;       // yyyy-MM-dd
	
	private ReportService reportService = SpringContextHolder.getBean("reportService");               // service实例
		
	private User loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");   // 登陆的user

	private Map<String, CallCountCheckPo> map;  // 实体集合
	private Table table;                        // 要展示的table
	private StringBuffer buffer;                // 创建一个StringBuffer
	private String domainId;                    // 登陆用户domainId
	private String callDirectionValue;          // 呼叫方向
	private String fromStr;                     // 大于等于的秒数
	private String toStr;                       // 小于等于的秒数
	private String sql = "";                    // 要拼接的sql
	
	// 存放所管理的所有部门的信息
	private List<Department> deptList = new ArrayList<Department>();
	private BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(Department.class);
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();

	public TopUiCallCountCheck(CallCheckByCallTimeLength byCallTimeLength) {
		this.byCallTimeLength = byCallTimeLength;

		format = new SimpleDateFormat("yyyy-MM-dd");   // 创建一个SimpleDateFormat
		horizontalLayout = new HorizontalLayout();     // 创建一个水平布局
		horizontalLayout.setSpacing(true);   
		horizontalLayout.setMargin(true,true,false,true);
		this.addComponent(horizontalLayout);

		// 创建存放 时间范围 的布局管理器
		createImportTimeScopeHLayout();
		// 创建 开始时间 的布局管理器
		createStartImportTimeHLayout();

		toLabel = new Label("至");                   // 至
		horizontalLayout.addComponent(toLabel);
		// 创建 截止时间 的布局管理器
		createFinishImportTimeHLayout();
		
		// 部门选择 -- 这里应该考虑选择多个部门进行查询
		cmbDept = achieveBasicUtil.getCmbDeptReport(cmbDept, loginUser, deptList, deptContainer);
		horizontalLayout.addComponent(cmbDept);

		comboBox = new ComboBox();                 // 方向
		for (int i = 0; i < callDirection.length; i++) {
			comboBox.addItem(callDirection[i]);
		}
		comboBox.setWidth("55px");
		comboBox.setImmediate(true);
		comboBox.setNullSelectionAllowed(false);
		comboBox.setValue(callDirection[0]);      // 默认是呼出
		horizontalLayout.addComponent(comboBox);

		fromField = new TextField();              // 开始秒数
		fromField.setWidth("50px");
		fromField.setInputPrompt("秒数");
		horizontalLayout.addComponent(fromField);

		label = new Label("至");                  // 至
		horizontalLayout.addComponent(label);

		toField = new TextField();               // 结束秒数
		toField.setWidth("50px");
		toField.setInputPrompt("秒数");
		horizontalLayout.addComponent(toField);

		seeButton = new Button("查询", this, "seeResult");             // 查询按钮
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");    // 导出按钮
		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_TELEPHONE_TRAFFIC_CHECK_BY_CALL_TIME)) {  //是否有导出权限
			horizontalLayout.addComponent(exportReport);
		}

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

	/**
	 * 创建存放 时间范围 的布局管理器
	 */
	private void createImportTimeScopeHLayout() {
		dateComboBox = new ComboBox();                  // 日期combobox
		for (int i = 0; i < dateType.length; i++) {     // 结日期combobx赋值
			dateComboBox.addItem(dateType[i]);
		}
		dateComboBox.setWidth("80px");
		dateComboBox.setImmediate(true);
		dateComboBox.setNullSelectionAllowed(false);
		dateComboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		dateComboBox.setValue(dateType[0]);            // 默认是本日
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
		startDate = new PopupDateField();             // 开始日期
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

	public void seeResult(ClickEvent event) {

		tabSheet = byCallTimeLength.getTabSheet();                 // 获取tabSheet
		domainId = loginUser.getDomain().getId().toString();       // 登陆用户domainId
		Department dept = (Department) cmbDept.getValue();
		if(dept == null) {
			event.getComponent().getWindow().showNotification("请选择要查询的部门");
			return;
		}
		
 		startValue = format.format(startDate.getValue());      // 获得startDate的值
		endValue = format.format(endDate.getValue());          // 获得endDate的值
		callDirectionValue = comboBox.getValue().toString();   // 呼叫方向
		fromStr = fromField.getValue().toString();             // 时长范围的起始点
		toStr = toField.getValue().toString();                 // 时长范围的终点
		if (callDirectionValue.equals("呼出")) {
			map = getOutgoingList(startValue, endValue, fromStr, toStr, dept);                                                             // 获取统计的集合
			table = new Table("通次考核");                                                                                              // 创建一个要展示的表格
			BeanItemContainer<CallCountCheckPo> beanItemContainer = new BeanItemContainer<CallCountCheckPo>(CallCountCheckPo.class); // 创建一个beanItemContainer
			Set<Entry<String, CallCountCheckPo>> entries = map.entrySet();                                                           // 遍历map把实体与beanItemContainer绑定
			Iterator<Entry<String, CallCountCheckPo>> iterator = entries.iterator();
			while (iterator.hasNext()) {
				Entry<String, CallCountCheckPo> entry = iterator.next();
				CallCountCheckPo callCountCheckPo = entry.getValue();
				beanItemContainer.addBean(callCountCheckPo);              // 实休添加到beanItemContainer中
			}
			table.setContainerDataSource(beanItemContainer);              // beanItemContainer添加到table中  
			table.setSizeFull();
			table.setImmediate(true);
			table.setNullSelectionAllowed(true);
			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			table.setStyleName("striped");
			table.setSelectable(true);
			table.setVisibleColumns(visibleColumns);
			table.setColumnHeaders(columnHeaders);

		    tab = tabSheet.getTab(0);                                //获取第一个tab
		    if(tab==null){                                           //如果为空。表示新建
			   tab = tabSheet.addTab(table, "通次统计", null);
			   tab.setClosable(true);
			   tabSheet.setSelectedTab(tab);			   
		    }else {                                                  //如果不为空，获取其中的table
			   table = (Table) tab.getComponent();
			   table.setContainerDataSource(beanItemContainer);
			   table.setSizeFull();
			   table.setImmediate(true);
			   table.setNullSelectionAllowed(true);
			   table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			   table.setStyleName("striped");
			   table.setSelectable(true);
			   table.setVisibleColumns(visibleColumns);
			   table.setColumnHeaders(columnHeaders);
		   }

		} else if (callDirectionValue.equals("呼入")) {

			map = getIncomingList(startValue, endValue, fromStr, toStr, dept);
			table = new Table("通次考核");
			BeanItemContainer<CallCountCheckPo> beanItemContainer = new BeanItemContainer<CallCountCheckPo>(CallCountCheckPo.class);
			Set<Entry<String, CallCountCheckPo>> entries = map.entrySet();
			Iterator<Entry<String, CallCountCheckPo>> iterator = entries.iterator();
			while (iterator.hasNext()) {
				Entry<String, CallCountCheckPo> entry = iterator.next();
				CallCountCheckPo callCountCheckPo = entry.getValue();
				beanItemContainer.addBean(callCountCheckPo);
			}
			table.setContainerDataSource(beanItemContainer);
			table.setSizeFull();
			table.setImmediate(true);
			table.setNullSelectionAllowed(true);
			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			table.setStyleName("striped");
			table.setSelectable(true);
			table.setVisibleColumns(visibleColumns);
			table.setColumnHeaders(columnHeaders);

			tab = tabSheet.getTab(0);                                //获取第一个tab
			if(tab==null){                                           //如果为空。表示新建
	  	      tab = tabSheet.addTab(table, "通次统计", null);
	  	      tab.setClosable(true);
	 	      tabSheet.setSelectedTab(tab);			   
		   }else {                                                  //如果不为空，获取其中的table
		      table = (Table) tab.getComponent();
			  table.setContainerDataSource(beanItemContainer);
		      table.setSizeFull();
			  table.setImmediate(true);
		      table.setNullSelectionAllowed(true);
			  table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			  table.setStyleName("striped");
		      table.setSelectable(true);
		      table.setVisibleColumns(visibleColumns);
		      table.setColumnHeaders(columnHeaders);
		   }

		} else if (callDirectionValue.equals("全部")) {

			map = getTotalList(startValue, endValue, fromStr, toStr, dept);

			table = new Table("通次考核");

			BeanItemContainer<CallCountCheckPo> beanItemContainer = new BeanItemContainer<CallCountCheckPo>(
					CallCountCheckPo.class);

			Set<Entry<String, CallCountCheckPo>> entries = map.entrySet();
			Iterator<Entry<String, CallCountCheckPo>> iterator = entries
					.iterator();
			while (iterator.hasNext()) {
				Entry<String, CallCountCheckPo> entry = iterator.next();
				CallCountCheckPo callCountCheckPo = entry.getValue();

				// add bean to container
				beanItemContainer.addBean(callCountCheckPo);
			}

			table.setContainerDataSource(beanItemContainer);
			table.setSizeFull();
			table.setImmediate(true);
			table.setNullSelectionAllowed(true);
			table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			table.setStyleName("striped");
			table.setSelectable(true);
			table.setVisibleColumns(visibleColumns);
			table.setColumnHeaders(columnHeaders);

			tab = tabSheet.getTab(0);                                //获取第一个tab
			if(tab==null){                                           //如果为空。表示新建
	  	      tab = tabSheet.addTab(table, "通次统计", null);
	  	      tab.setClosable(true);
	 	      tabSheet.setSelectedTab(tab);			   
		   }else {                                                  //如果不为空，获取其中的table
		      table = (Table) tab.getComponent();
			  table.setContainerDataSource(beanItemContainer);
		      table.setSizeFull();
			  table.setImmediate(true);
		      table.setNullSelectionAllowed(true);
			  table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
			  table.setStyleName("striped");
		      table.setSelectable(true);
		      table.setVisibleColumns(visibleColumns);
		      table.setColumnHeaders(columnHeaders);
			   }
		}

	}

	// 呼出
	public Map<String, CallCountCheckPo> getOutgoingList(String startValue, String endValue, String fromStr, String toStr, Department dept) {

		// 管理员所属部门和管辖部门
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,d.name,u.realname from ec2_user as u,ec2_department as d where u.department_id = d.id and u.domain_id = ");
		sbQuery.append(domainId);
		sbQuery.append(getSqlByDept(dept, "d.id"));
		sql = sbQuery.toString();
		List<Object[]> list = reportService.getMoreRecord(sql);

		// 存入实体
		Map<String, CallCountCheckPo> srcMap = new HashMap<String, CallCountCheckPo>();

		for (Object[] objects : list) {

			CallCountCheckPo callCountCheckPo = new CallCountCheckPo();

			// 工号
			if (objects[0] != null) {
				callCountCheckPo.setEmpno(objects[0].toString());
			}
			// 部门
			if (objects[1] != null) {
				callCountCheckPo.setDept(objects[1].toString());
			}
			// 姓名
			if (objects[2] != null) {
				callCountCheckPo.setName(objects[2].toString());
			}

			srcMap.put(objects[0].toString(), callCountCheckPo);

		}

		// 呼出总数量
		buffer = new StringBuffer();

		buffer.append("select  srcempno as srcempno,count(*) as count from cdr where srcusername != '' and cdrdirection = '1' and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");
		
		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}
		
		buffer.append(" group by srcempno");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {

			if (srcMap.containsKey(objects[0].toString().trim())) {

				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0]
						.toString());

				// 呼出总数量
				callCountCheckPo.setCallTotalCount(Integer.parseInt(objects[1]
						.toString()));

			}
		}
		// 呼出接通数量
		buffer = new StringBuffer();
		buffer.append("select  srcusername as srcusername,count(*) as count from cdr where srcusername != '' and cdrdirection = '1' and isbridged is not null and isbridged = true and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");
		
		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}
		
		buffer.append(" group by srcusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				callCountCheckPo.setCallConnectCount(Integer.parseInt(objects[1].toString()));	// 呼出接通数量
			}
		}

		// 呼出未接通数量
		buffer = new StringBuffer();
		buffer.append("select  srcusername as srcusername,count(*) as count from cdr where srcusername != '' and cdrdirection = '1' and (isbridged is null or isbridged = false) and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by srcusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);
		
		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				callCountCheckPo.setCallUnconnectCount(Integer.parseInt(objects[1].toString()));	// 呼出未接通数量
			}
		}

		return srcMap;
	}

	// 呼入
	public Map<String, CallCountCheckPo> getIncomingList(String startValue, String endValue, String fromStr, String toStr, Department dept) {

		// 查看全部部门
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,d.name,u.realname from ec2_user as u,ec2_department as d where u.department_id = d.id and u.domain_id = ");
		sbQuery.append(domainId);
		sbQuery.append(getSqlByDept(dept, "d.id"));
		
		sql = sbQuery.toString();
		List<Object[]> list = reportService.getMoreRecord(sql);
		// 存入实体
		Map<String, CallCountCheckPo> srcMap = new HashMap<String, CallCountCheckPo>();

		for (Object[] objects : list) {
			CallCountCheckPo callCountCheckPo = new CallCountCheckPo();

			// 工号
			if (objects[0] != null) {
				callCountCheckPo.setEmpno(objects[0].toString());
			}
			// 部门
			if (objects[1] != null) {
				callCountCheckPo.setDept(objects[1].toString());
			}
			// 姓名
			if (objects[2] != null) {
				callCountCheckPo.setName(objects[2].toString());
			}

			srcMap.put(objects[0].toString(), callCountCheckPo);
		}

		// 呼入总数量
		buffer = new StringBuffer();
		buffer.append("select  destusername as destusername,count(*) as count from cdr where destusername != '' and cdrdirection = '2' and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("'");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by destusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				callCountCheckPo.setCallTotalCount(Integer.parseInt(objects[1].toString()));	// 呼入总数量
			}
		}
		// 呼入接通数量
		buffer = new StringBuffer();
		buffer.append("select  destusername as destusername,count(*) as count from cdr where destusername != '' and cdrdirection = '2' and isbridged is not null and isbridged = true and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by destusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				callCountCheckPo.setCallConnectCount(Integer.parseInt(objects[1].toString()));	// 呼入接通数量
			}
		}

		// 呼入未接通数量
		buffer = new StringBuffer();
		buffer.append("select  destusername as destusername,count(*) as count from cdr where destusername != '' and cdrdirection = '2' and (isbridged is null or isbridged = false) and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by destusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				// 呼入未接通数量
				callCountCheckPo.setCallUnconnectCount(Integer.parseInt(objects[1].toString()));
			}
		}

		return srcMap;
	}

	// 全部
	public Map<String, CallCountCheckPo> getTotalList(String startValue, String endValue, String fromStr, String toStr, Department dept) {

		// 查看全部部门
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,d.name,u.realname from ec2_user as u,ec2_department as d  where u.department_id = d.id and u.domain_id = ");
		sbQuery.append(domainId);
		sbQuery.append(getSqlByDept(dept, "d.id"));
		sql = sbQuery.toString();

		List<Object[]> list = reportService.getMoreRecord(sql);
		// 存入实体
		Map<String, CallCountCheckPo> srcMap = new HashMap<String, CallCountCheckPo>();

		for (Object[] objects : list) {
			CallCountCheckPo callCountCheckPo = new CallCountCheckPo();
			
			// 工号
			if (objects[0] != null) {
				callCountCheckPo.setEmpno(objects[0].toString());
			}
			// 部门
			if (objects[1] != null) {
				callCountCheckPo.setDept(objects[1].toString());
			}
			// 姓名
			if (objects[2] != null) {
				callCountCheckPo.setName(objects[2].toString());
			}

			srcMap.put(objects[0].toString(), callCountCheckPo);
		}

		// 呼叫总数量
		buffer = new StringBuffer();

		buffer.append("select  destusername as destusername,count(*) as count from cdr where destusername != '' and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by destusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);
		
		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				// 呼叫总数量
				callCountCheckPo.setCallTotalCount(callCountCheckPo.getCallTotalCount()+ Integer.parseInt(objects[1].toString()));
			}
		}
		// 呼叫接通数量
		buffer = new StringBuffer("select  destusername as destusername,count(*) as count from cdr where destusername != '' and isbridged is not null and isbridged = true and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by destusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				// 呼叫接通数量
				callCountCheckPo.setCallConnectCount(callCountCheckPo.getCallConnectCount() + Integer.parseInt(objects[1].toString()));
			}
		}

		// 呼叫未接通数量
		buffer = new StringBuffer();

		buffer.append("select  destusername as destusername,count(*) as count from cdr where destusername != '' and (isbridged is null or isbridged = false) and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by destusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				// 呼叫未接通数量
				callCountCheckPo.setCallUnconnectCount(callCountCheckPo.getCallUnconnectCount() + Integer.parseInt(objects[1].toString()));
			}
		}

		// 呼叫总数量
		buffer = new StringBuffer();
		buffer.append("select  srcusername as srcusername,count(*) as count from cdr where srcusername != '' and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by srcusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				// 呼叫总数量
				callCountCheckPo.setCallTotalCount(callCountCheckPo.getCallTotalCount() + Integer.parseInt(objects[1].toString()));
			}
		}
		// 呼叫接通数量
		buffer = new StringBuffer("select  srcusername as srcusername,count(*) as count from cdr where srcusername != '' and isbridged is not null and isbridged = true and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}

		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by srcusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				// 呼叫接通数量
				callCountCheckPo.setCallConnectCount(callCountCheckPo.getCallConnectCount() + Integer.parseInt(objects[1].toString()));
			}
		}

		// 呼叫未接通数量
		buffer = new StringBuffer();
		buffer.append("select  srcusername as srcusername,count(*) as count from cdr where srcusername != '' and (isbridged is null or isbridged = false) and domainid = ");
		buffer.append(domainId);
		buffer.append(" and starttimedate >= '");
		buffer.append(startValue);
		buffer.append("' and starttimedate <= '");
		buffer.append(endValue);
		buffer.append("' ");

		if (!fromStr.equals("")) {
			buffer.append(" and billableseconds >= ");
			buffer.append(fromStr);
		}
		if (!toStr.equals("")) {
			buffer.append(" and billableseconds <= ");
			buffer.append(toStr);
		}

		buffer.append(" group by srcusername");
		sql = buffer.toString();
		list = reportService.getMoreRecord(sql);

		for (Object[] objects : list) {
			if (srcMap.containsKey(objects[0].toString().trim())) {
				CallCountCheckPo callCountCheckPo = srcMap.get(objects[0].toString());
				// 呼叫未接通数量
				callCountCheckPo.setCallUnconnectCount(callCountCheckPo.getCallUnconnectCount() + Integer.parseInt(objects[1].toString()));
			}
		}

		return srcMap;
	}

	// 导出报表事件
	public void exportReport(ClickEvent event) {
		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询,再导出!");
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
		// 生成excel file
		file = ExportExcelUtil.exportExcel(titleName, list, columnNames,startValue, endValue);

		// ======================chb 操作日志======================//
		// 记录导出日志
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null)
			operationLog.setFilePath(file.getAbsolutePath());
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(SpringContextHolder.getLoginUser().getUsername());
		operationLog.setRealName(SpringContextHolder.getLoginUser().getRealName());
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("导出通次考核报表["+startValue+"]["+endValue+"]");
		 
		CommonService commonService = SpringContextHolder.getBean("commonService");
		commonService.save(operationLog);
		// ======================chb 操作日志======================//

		// 下载file
		Resource resource = new FileResource(file, event.getComponent().getApplication());
		final Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setSource(resource);
		this.addComponent(downloader);
		downloader.addListener(new RepaintRequestListener() {
			private static final long serialVersionUID = 626612329245010818L;
			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
			}
		});
	}

	public List<Object[]> getToTableList(Map<String, CallCountCheckPo> map) {
		// 创建list
		List<Object[]> list = new ArrayList<Object[]>();

		// 编历map
		Set<Entry<String, CallCountCheckPo>> set = map.entrySet();
		Iterator<Entry<String, CallCountCheckPo>> iterator = set.iterator();
		while (iterator.hasNext()) {

			Entry<String, CallCountCheckPo> entry = iterator.next();
			CallCountCheckPo callCheckPo = entry.getValue();

			// 创建Object[],size固定死了
			Object[] objects = new Object[6];
			// 部门
			objects[0] = callCheckPo.getDept();
			// 工号
			objects[1] = callCheckPo.getEmpno();
			// 姓名
			objects[2] = callCheckPo.getName();
			// 呼叫总数量
			objects[3] = callCheckPo.getCallTotalCount();
			// 呼叫接通数量
			objects[4] = callCheckPo.getCallConnectCount();
			// 呼叫未接通数量
			objects[5] = callCheckPo.getCallUnconnectCount();

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
