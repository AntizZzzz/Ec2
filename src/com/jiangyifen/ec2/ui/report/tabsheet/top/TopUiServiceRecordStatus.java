package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.ServiceRecordStatus;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportSum;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
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
import com.vaadin.ui.VerticalLayout;

/**
 * REPORTPAGE客服记录情况
 * @author lxy
 *
 */
public class TopUiServiceRecordStatus extends VerticalLayout {

	private static final long serialVersionUID = -6179929953006817269L;
	
	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;
	private ComboBox directionBox;
	private ComboBox statusName;
	private ComboBox seeWay;
	private ComboBox cmbDept;		// 需要查询的部门
	
	private Button seeButton;	// 查询
	private Button exportReport;// 导出报表
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private ServiceRecordStatus recordStatus;
	private TabSheet tabSheet;
	private Tab tab;
	private String[] dateType = new String[] { "今天", "昨天", "本周", "上周", "本月", "上月", "精确时间"};
	private String[] direction = new String[] { "呼出", "呼入" };
	private String[] sumAndDetail = new String[] { "记录求和", "记录详情" };
	private List<Object[]> list;// 从数据库获得新的数据
	private String startValue;	// startDate的值
	private String endValue;	// endDate的值
	private String statusNameValue;
	private List<String> columnNames;//Arrays.asList(new String[] { "部门", "工号", "姓名", "日期" ,"客服记录状态","数量"});
	private File file;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private User loginUser ;
	private Long domainId;
	private String sqlQuery;
	private ReportService reportService ;
	
	// 存放所管理的所有部门的信息
	private List<Department> deptList = new ArrayList<Department>();
	private BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(Department.class);
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
	
	public TopUiServiceRecordStatus(ServiceRecordStatus recordStatus) {
		this.recordStatus = recordStatus;
		initSpringContext();
		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true,true,false,true);
		this.addComponent(horizontalLayout);

		// 创建存放 时间范围选择 的布局管理器
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
		
		directionBox = new ComboBox();
		for (int i = 0; i < direction.length; i++) {
			directionBox.addItem(direction[i]);
		}
		directionBox.setWidth("100px");
		directionBox.setInputPrompt("呼叫方向");
		directionBox.setImmediate(true);
		directionBox.setNullSelectionAllowed(false);
		directionBox.setValue(direction[0]);
		directionBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		horizontalLayout.addComponent(directionBox);

		statusName = new ComboBox();
		statusName.setWidth("100px");
 		statusName.setImmediate(true);
		statusName.setNullSelectionAllowed(false);
		statusName.setInputPrompt("客服记录状态");
		statusName.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		horizontalLayout.addComponent(statusName);
		initStatusName();
		
		seeWay = new ComboBox();
		for (int i = 0; i < sumAndDetail.length; i++) {
			seeWay.addItem(sumAndDetail[i]);
		}
		seeWay.setWidth("100px");
		seeWay.setImmediate(true);
		seeWay.setNullSelectionAllowed(false);
		seeWay.select(sumAndDetail[0]);
		seeWay.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		horizontalLayout.addComponent(seeWay);
		
		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");
		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_SERVICE_RECORD_STATUS)) {
			horizontalLayout.addComponent(exportReport);
		}
		directionBox.addListener(new ComboBox.ValueChangeListener() {
			private static final long serialVersionUID = 6395956976748263881L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				String direction = event.getProperty().toString();
				if (direction.equals("呼出")) {
					statusName.removeAllItems();
					List<String> statusNames = getStatusname(domainId.toString(), "outgoing");
					for (String sn : statusNames) {
						statusName.addItem(sn);
					}
					if(null != statusNames && statusNames.size() > 0){
						statusName.select(statusNames.get(0));
					}
				} else if (direction.equals("呼入")) {
					statusName.removeAllItems();
					List<String> statusNames = getStatusname(domainId.toString(), "incoming");
					for (String sn : statusNames) {
						statusName.addItem(sn);
					}
					if(null != statusNames && statusNames.size() > 0){
						statusName.select(statusNames.get(0));
					}
				}
			}
		});

	}

	/**
	 * 创建存放 时间范围选择 的布局管理器
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
			private static final long serialVersionUID = 6395956976748263881L;
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
	 * 创建开始时间的布局管理器
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
			private static final long serialVersionUID = 4470721897480740545L;
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
			private static final long serialVersionUID = -8161300638844173890L;
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

	//初始化spring相关业务对象
	private void initSpringContext() {
		loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");
		domainId = loginUser.getDomain().getId();
		reportService = SpringContextHolder.getBean("reportService");
	}

	private void initStatusName(){
		List<String> statusNames = getStatusname(domainId.toString(), "outgoing");
		statusName.setInputPrompt("客服记录状态");
		statusName.removeAllItems();
		for (String sn : statusNames) {
			statusName.addItem(sn);
		}
		if(null != statusNames && statusNames.size() > 0){
			statusName.select(statusNames.get(0));
		}
	}
	
	public void seeResult(ClickEvent event) {
		tabSheet = recordStatus.getTabSheet();
		
		// 获取当前选中的部门
		Department dept = (Department) cmbDept.getValue();
		if(dept == null) {
			NotificationUtil.showWarningNotification(this, "请选择要查询的部门");
			return;
		}
		
		startValue = dateFormat.format(startDate.getValue());	// 获得startDate的值
		endValue = dateFormat.format(endDate.getValue());		// 获得endDate的值
		if (directionBox.getValue() == null) {
			NotificationUtil.showWarningNotification(this, "请选择呼叫方向");
			return;
		}
		if (statusName.getValue() == null) {
			NotificationUtil.showWarningNotification(this, "请选择客服记录状态");
			return;
		}
		String direction = directionBox.getValue().toString();
		String seeWayValue = seeWay.getValue()==null? "":seeWay.getValue().toString();
		statusNameValue = statusName.getValue().toString();
		if (direction.equals("呼出")) {
			direction = "outgoing";
		} else if (direction.equals("呼入")) {
			direction = "incoming";
		}
		if (seeWayValue.equals("记录求和")) {
			columnNames = Arrays.asList(new String[] { "部门", "工号", "姓名", "客服记录状态","数量"});
			list = getSumList(domainId.toString(), startValue, endValue, statusNameValue, direction, dept);
			list = ReportSum.sum(list, 4);
		} else if (seeWayValue.equals("记录详情")) {
			columnNames = Arrays.asList(new String[] { "部门", "工号", "姓名", "日期" ,"客服记录状态","数量"});
			list = getList(domainId.toString(), startValue, endValue, statusNameValue, direction, dept);
			list = ReportSum.sum(list, 5);
		}
		
		tab = tabSheet.addTab(new ReportTableUtil(list, "", columnNames, startValue, endValue), "客服记录报表", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);
	}

	// 导出报表事件
	public void exportReport(ClickEvent event) throws FileNotFoundException {
		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询,在导出");
			return;
		}
		ReportTableUtil reportTableUtil = (ReportTableUtil) tabSheet.getSelectedTab();// 得到当前tab的组件，把当前tab页的数据导出报表
		if (reportTableUtil == null) {//如果reportTableUtil为null提示请点击查询
			NotificationUtil.showWarningNotification(this, "请先查询,在导出");
			return;
		}
		list = reportTableUtil.getList();				// 设置数据源
		columnNames = reportTableUtil.getNames();		// 设置列名
		startValue = reportTableUtil.getStartValue();	// 开时时间
		endValue = reportTableUtil.getEndValue();		// 结束时间
		file = ExportExcelUtil.exportExcel("客服记录报表", list, columnNames, startValue, endValue);// 生成excel file
		saveOperationLog();// 记录导出日志
		fileDownload(event);// 文件下载
	}

	//记录导出日志
	private void saveOperationLog() {
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null) {
			operationLog.setFilePath(file.getAbsolutePath());
		}
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(loginUser.getUsername());
		operationLog.setRealName(loginUser.getRealName());
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("导出客服记录报表");
		operationLog.setProgrammerSee(sqlQuery);
		CommonService commonService = SpringContextHolder.getBean("commonService");
		commonService.save(operationLog);
	}
	
	//文件下载
	private void fileDownload(ClickEvent event) {
		Resource resource = new FileResource(file, event.getComponent().getApplication());
		final Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setSource(resource);
		this.addComponent(downloader);
		downloader.addListener(new RepaintRequestListener() {
			private static final long serialVersionUID = -9200720669070381303L;
			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
			}
		});
	}

	/**
	 * 
	 * @param domainId
	 * @param startValue
	 * @param endValue
	 * @param statusNameValue
	 * @param direction
	 * @return
	 */
	public List<Object[]> getList(String domainId, String startValue,String endValue, String statusNameValue, String direction, Department dept) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select d.name,u.username,u.realname,t.date,t.statusname,t.count from ec2_user as u,ec2_department as d, ");
		sbQuery.append("(select createdate as date,creator_id as creator_id,s.statusname as statusname,count(*) ");
		sbQuery.append("from ec2_customer_service_record as r,ec2_customer_service_record_status as s ");
		sbQuery.append("where r.servicerecordstatus_id = s.id and createdate >= '");
		sbQuery.append(startValue);
		sbQuery.append("' and createdate <=  '");
		sbQuery.append(endValue);
		sbQuery.append("' and r.domain_id = '");
		sbQuery.append(domainId);
		sbQuery.append("' and r.direction = '");
		sbQuery.append(direction);
		sbQuery.append("' ");
		
		if(!"全部".equals(statusNameValue)){
			sbQuery.append(" and s.statusname = '");
			sbQuery.append(statusNameValue);
			sbQuery.append("' ");
		}
		sbQuery.append(" group by date,creator_id,statusname ) as t ");
		sbQuery.append(" where t.creator_id = u.id and u.department_id = d.id ");
		if(dept != null) {
			if(dept.getId().equals(0L)) {
				sbQuery.append(" and d.id in (");
				sbQuery.append(achieveBasicUtil.getDeptIds());
				sbQuery.append(")");
			} else {
				sbQuery.append(" and d.id = ");
				sbQuery.append(dept.getId());
			}
		} else {
			sbQuery.append(" and 1 > 2 ");
		}
		
		sqlQuery = sbQuery.toString();
		List<Object[]> list = reportService.getMoreRecord(sqlQuery);
		return list;
	}

	/**
	 * 
	 * @param domainId
	 * @param startValue
	 * @param endValue
	 * @param statusNameValue
	 * @param direction
	 * @return
	 */
	public List<Object[]> getSumList(String domainId, String startValue,String endValue, String statusNameValue, String direction, Department dept) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select d.name,u.username,u.realname,t.statusname,t.count from ec2_user as u,ec2_department as d, ");
		sbQuery.append("( select creator_id as creator_id,s.statusname as statusname,count(*) ");
		sbQuery.append(" from ec2_customer_service_record as r,ec2_customer_service_record_status as s ");
		sbQuery.append(" where r.servicerecordstatus_id = s.id and createdate >= '" );
		sbQuery.append(startValue);
		sbQuery.append("' and createdate <= '");
		sbQuery.append(endValue);
		sbQuery.append("' ");
		sbQuery.append(" and r.domain_id = '");
		sbQuery.append(domainId);
		sbQuery.append("' ");
		sbQuery.append(" and r.direction = '");
		sbQuery.append(direction);
		sbQuery.append("' ");
		if(!"全部".equals(statusNameValue)){
			sbQuery.append(" and s.statusname = '");
			sbQuery.append(statusNameValue);
			sbQuery.append("' ");
		}
		sbQuery.append(" group by creator_id,statusname) as t ");
		sbQuery.append(" where t.creator_id = u.id and u.department_id = d.id ");
		if(dept != null) {
			if(dept.getId().equals(0L)) {
				sbQuery.append(" and d.id in (");
				sbQuery.append(achieveBasicUtil.getDeptIds());
				sbQuery.append(") ");
			} else {
				sbQuery.append(" and d.id = ");
				sbQuery.append(dept.getId());
			}
		} else{
			sbQuery.append(" and 1 > 2 ");
		}
		
		sqlQuery = sbQuery.toString();
		List<Object[]> list = reportService.getMoreRecord(sqlQuery);
		return list;
	}

	/**
	 * 
	 * @param domain_id
	 * @param direction
	 * @return
	 */
	public List<String> getStatusname(String domain_id, String direction) {
		List<String> rtls = new ArrayList<String>();
		rtls.add("全部");
		sqlQuery = "select statusname from ec2_customer_service_record_status where domain_id = " + "'" + domain_id + "'  and direction = " + "'" + direction + "'";
		List<Object> list = reportService.getOneRecord(sqlQuery);
		for (Object statusName : list) {
			rtls.add(String.valueOf(statusName));
		}
		return rtls;
	}

}
