package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.pojo.AutoDialDetailPo;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.AutodialDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
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
 * REPORTPAGE自动外呼详情
 * @author lxy
 *
 */
@SuppressWarnings("serial")
public class TopUiAutodialDetail extends VerticalLayout implements ValueChangeListener {
	
	private String[] columnHeaders = new String[] { "部门", "工号", "姓名", "接通数量", "通话时长", "平均通话时长", "接电话用时", "平均接电话用时" };
	private Object[] visibleColumns = new Object[] { "dept", "empno", "name", "connectCount", "callTimeLength", "avgCallTimeLength", "answerTimeLength", "avgAnswerTimeLength" };
	private String[] dateType = new String[] { "今天", "昨天", "本周", "上周", "本月", "上月", "精确时间"};
	
	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox dateComboBox;
	private PopupDateField startDate;
	private Label toLabel;
	private PopupDateField endDate;
	private ComboBox cmbDept;		// 需要查询的部门
	private Button seeButton;		// 查询
	private Button exportReport;	// 导出报表
	
	private ComboBox empNoComboBox;
	private AutodialDetail autodialDetail;
	private TabSheet tabSheet;
	private Tab tab;
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
	
	private File file;
	
	private List<Object[]> list;		// 从数据库获得新的数据
	private String startValue;			// startDate的值
	private String endValue;			// endDate的值
	private List<String> columnNames;	// table的列名
	private String titleName;			// 传入的报表名
	private String sqlQuery;
    private List<AutoDialDetailPo> autoDialDetailPos;
    
    private Table table;
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private UserService userService;
	private ReportService reportService;
	private DepartmentService departmentService;
	
	// 登陆的user
	private User loginUser;
	private Domain domain;
	// 登陆用户的
	private String domainId;
	private Set<Long> deptidSet = new HashSet<Long>(); 	//该管理员管辖的部门编号列表
	private List<String> names = new ArrayList<String>();              //列名
	// 登陆用户的部门
	private Map<String, AutoDialDetailPo> addpMap;
	
	// 存放所管理的所有部门的信息
	private List<Department> deptList = new ArrayList<Department>();
	private BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(Department.class);
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
	
	public TopUiAutodialDetail(AutodialDetail autodialDetail) {
		this.autodialDetail = autodialDetail;
		initSpringContext();
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
		cmbDept.addListener((ValueChangeListener) this);

		Label lb_1 = new Label("工号：");
		horizontalLayout.addComponent(lb_1);
		//工号选择
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User showAllCsr = new User();
		showAllCsr.setEmpNo("全部-员工-部门");
		showAllCsr.setUsername("全部-员工-部门");
		userContainer.addBean(showAllCsr);
		List<Long> strList = new ArrayList<Long>();
		strList.addAll(deptidSet);
		List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
		userContainer.addAll(csrs);
		empNoComboBox = new ComboBox();
		empNoComboBox.setContainerDataSource(userContainer);
		empNoComboBox.setValue(showAllCsr);
		empNoComboBox.setWidth("158px");
		empNoComboBox.setNullSelectionAllowed(false);
		empNoComboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		empNoComboBox.setItemCaptionPropertyId("migrateCsr1");
		horizontalLayout.addComponent(empNoComboBox);
		/*for(User csr : userContainer.getItemIds()) {
			String caption = csr.getEmpNo();
			if(csr.getRealName() != null) {
				caption = caption +" - "+ csr.getRealName();
			}
			empNoComboBox.setItemCaption(csr, caption);
		}*/
		
		
		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);
		exportReport = new Button("导出报表", this, "exportReport");
		if (SpringContextHolder.getBusinessModel().contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_SEE_AUTODIAL_DETAIL)) {
			horizontalLayout.addComponent(exportReport);
		}
		
	}

	/**
	 * 创建存放 时间范围 的布局管理器
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
	
	//初始化spring相关业务对象
	private void initSpringContext() {
		loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");
		domain = loginUser.getDomain();
		userService = SpringContextHolder.getBean("userService");
		reportService = SpringContextHolder.getBean("reportService");
		departmentService = SpringContextHolder.getBean("departmentService");

		Set<Role> roles = loginUser.getRoles(); // 获取登陆者所有的角色
		for (Role role : roles) {
			if (role.getType().equals(RoleType.manager)) {
				for (Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					deptidSet.add(dept.getId());
				}
			}
		}
	}

	public void seeResult(ClickEvent event) {
		autoDialDetailPos = new ArrayList<AutoDialDetailPo>();
		tabSheet = autodialDetail.getTabSheet();
		domainId = loginUser.getDomain().getId().toString();// 登陆用户domainId	
		
		Department dept = (Department) cmbDept.getValue();
		if(dept == null) {
			event.getComponent().getWindow().showNotification("请选择要查询的部门");
			return;
		}
		
		startValue = format.format(startDate.getValue());
		endValue = format.format(endDate.getValue());
		//给map中的实体赋值
		addpMap = setValueForEntityToMap(startValue, endValue, dept);
		table = new Table("自动外呼详情");
		BeanItemContainer<AutoDialDetailPo> container = new BeanItemContainer<AutoDialDetailPo>(AutoDialDetailPo.class);

		Set<Entry<String, AutoDialDetailPo>> set = addpMap.entrySet();
		Iterator<Entry<String, AutoDialDetailPo>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Entry<String, AutoDialDetailPo> entry = iterator.next();
			AutoDialDetailPo autoDialDetailPo = entry.getValue();
            //设置通话时长格式  
			autoDialDetailPo.setCallTimeLength(DateUtil.getTime(Integer.parseInt(autoDialDetailPo.getCallTimeLength()==null ? "0":autoDialDetailPo.getCallTimeLength())));
			//设置平均通话时长格式
			autoDialDetailPo.setAvgCallTimeLength(DateUtil.getTime(Integer.parseInt(autoDialDetailPo.getAvgCallTimeLength()==null ? "0":autoDialDetailPo.getAvgCallTimeLength())));
			//设置接电话用时格式
			autoDialDetailPo.setAnswerTimeLength(DateUtil.getTime(Integer.parseInt(autoDialDetailPo.getAnswerTimeLength()==null ? "0":autoDialDetailPo.getAnswerTimeLength())));
			//设置平均接电话用时格式
			autoDialDetailPo.setAvgAnswerTimeLength(DateUtil.getTime(Integer.parseInt(autoDialDetailPo.getAvgAnswerTimeLength()==null ? "0":autoDialDetailPo.getAvgAnswerTimeLength())));
			container.addBean(autoDialDetailPo);
			//存入集合
			autoDialDetailPos.add(autoDialDetailPo);
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

		tab = tabSheet.addTab(table, "自动外呼详情", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);
	}
	
	/**                      给map中的实体赋值
	 * @param startValue     开始时间
	 * @param endValue       结束时间
	 * @return
	 */
	public Map<String, AutoDialDetailPo> setValueForEntityToMap(String startValue,String endValue, Department dept){
		addpMap = getAutoDialDetailPoMap(startValue, endValue, dept);
		connectCount(startValue, endValue, addpMap);	//接通数量
		callTimeLength(startValue, endValue, addpMap);	//通话时长
		pickupTimeLength(startValue, endValue, addpMap);//接电话用时
		avgCallTimeLength(addpMap);		//平均通话时长
		avgAnswerTimeLength(addpMap);	//平均接电话用时
		return addpMap;
	}
	
    /**                      初始化自动外呼详情集合
     * @param startValue     开始时间
     * @param endValue       结束时间
     * @return               实体集合
     */
	public Map<String, AutoDialDetailPo> getAutoDialDetailPoMap(String startValue, String endValue, Department dept) {
		StringBuffer sbQuery = new StringBuffer();
		sbQuery.append("select u.empno,d.name,u.realname from ec2_user as u,ec2_department as d where u.department_id = d.id and u.domain_id = ");
		sbQuery.append(domainId);
		sbQuery.append(getSqlByDept(dept, "d.id"));

		if (null != empNoComboBox.getValue()) {
			User selectUser = (User) empNoComboBox.getValue();
			if (StringUtils.isNotEmpty(selectUser.getEmpNo()) && !"全部-员工-部门".equals(selectUser.getEmpNo())) {
				sbQuery.append(" and u.empno = '");
				sbQuery.append(selectUser.getEmpNo());
				sbQuery.append("' ");
			}
		}
		
		sqlQuery = sbQuery.toString();
		// 查询结果
		list = reportService.getMoreRecord(sqlQuery);
		sbQuery = null;
		
		Map<String, AutoDialDetailPo> autoDialDPMap = new HashMap<String, AutoDialDetailPo>();
		// 初始化map
		for (Object[] objects : list) {
			AutoDialDetailPo autoDialDetailPo = new AutoDialDetailPo();
			if (objects[0] != null) {// 工号
				autoDialDetailPo.setEmpno(objects[0].toString());
			}
			if (objects[1] != null) {// 部门
				autoDialDetailPo.setDept(objects[1].toString());
			}
			if (objects[2] != null) {// 姓名
				autoDialDetailPo.setName(objects[2].toString());
			}
			// 存入map
			autoDialDPMap.put(objects[0].toString(), autoDialDetailPo);
			if (objects[0] != null) {// 存工号
				names.add(objects[0].toString());
			}
		}
		return autoDialDPMap;
	}
    
    /**                          接通数量
     * @param startValue         开始时间
     * @param endValue           结束时间
     * @param map                要被赋值的实体集合
     */
    public void connectCount(String startValue,String endValue,Map<String,AutoDialDetailPo> map){
    	// 自动外呼接通数量
    	StringBuffer sbQuery = new StringBuffer();
    	sbQuery.append("select  destusername as destusername,count(*) as count from cdr where destusername != '' and cdrdirection = '2' and isautodial = true and isbridged is not null and isbridged = true and domainid = ");
    	sbQuery.append(domainId);
    	sbQuery.append(" and starttimedate >= '");
    	sbQuery.append(startValue);
    	sbQuery.append("' and starttimedate <= '");
    	sbQuery.append(endValue);
    	sbQuery.append("' group by destusername");
    	sqlQuery = sbQuery.toString(); 
    	//查询结果
    	list = reportService.getMoreRecord(sqlQuery);
    	sbQuery = null;
    	
    	//遍历实体并赋值
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				AutoDialDetailPo autoDialDetailPo = map.get(objects[0].toString().trim());
				// 自动外呼接通数量
				if (objects[1] != null) {
					autoDialDetailPo.setConnectCount(Integer.parseInt(objects[1].toString()));
				}
			}
		}
    }
    
    /**                          通话时长
     * @param startValue         开始时间
     * @param endValue           结束时间
     * @param map                要被赋值的实体集合
     */
    public void callTimeLength(String startValue,String endValue,Map<String,AutoDialDetailPo> map){
    	//自动外呼通话时长
    	StringBuffer sbQuery = new StringBuffer();
    	sbQuery.append("select  destusername as destusername,sum(billableseconds) as count from cdr where destusername != '' and cdrdirection = '2' and isautodial = true and domainid = ");
    	sbQuery.append(domainId);
    	sbQuery.append(" and starttimedate >= '");
    	sbQuery.append(startValue);
    	sbQuery.append("' and starttimedate <= '");
    	sbQuery.append(endValue);
    	sbQuery.append("' group by destusername");
    	//查询结果
    	sqlQuery = sbQuery.toString();
    	list = reportService.getMoreRecord(sqlQuery);
    	sbQuery = null;
    	
    	//遍历实体并赋值
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				AutoDialDetailPo autoDialDetailPo = map.get(objects[0].toString().trim());
				// 自动外呼通话时长
				if (objects[1] != null) {
					autoDialDetailPo.setCallTimeLength(objects[1].toString());
				}
			}
		}
        	
    }
    
    /**                          接电话用时
     * @param startValue         开始时间
     * @param endValue           结束时间
     * @param map                要被赋值的实体集合
     */
    public void pickupTimeLength(String startValue,String endValue,Map<String,AutoDialDetailPo> map){
    	//接电话用时
    	StringBuffer sbQuery = new StringBuffer();
    	sbQuery.append("select u.username,cast(sum(extract(epoch from t.autodialpickuptime)-extract(epoch from t.autodialansweredtime)) as bigint) from ec2_marketing_project_task as t,ec2_user as u ");
    	sbQuery.append("where t.autodialisanswered = 'TRUE' and t.autodialiscsrpickup = 'TRUE' and t.user_id !='1' and t.user_id = u.id and autodialtime >= '");
    	sbQuery.append(startValue);
    	sbQuery.append("' and autodialtime <= '");
    	sbQuery.append(endValue);
    	sbQuery.append("' and t.domain_id =");
    	sbQuery.append(domainId);
    	sbQuery.append(" group by u.username");
    	sqlQuery = sbQuery.toString();
     	//查询结果
    	list = reportService.getMoreRecord(sqlQuery);
    	sbQuery = null;
    	
    	//遍历实体并赋值
		for (Object[] objects : list) {
			if (map.containsKey(objects[0].toString().trim())) {
				AutoDialDetailPo autoDialDetailPo = map.get(objects[0].toString().trim());
				if (objects[1] != null) {// 接电话用时
					autoDialDetailPo.setAnswerTimeLength(objects[1].toString());
				}
			}
		}
    	
    }
    
    /**                   平均通话时长
     * @param map         要被赋值的实体集合
     */
    public void avgCallTimeLength(Map<String,AutoDialDetailPo> map){
    	Iterator<Entry<String, AutoDialDetailPo>> iterator = map.entrySet().iterator();
    	while(iterator.hasNext()){
    		Entry<String, AutoDialDetailPo> entry = iterator.next();
    		AutoDialDetailPo autoDialDetailPo = entry.getValue();
    		//通话时长
    		if(autoDialDetailPo.getCallTimeLength()!=null){
    			int callTimeLength = Integer.parseInt(autoDialDetailPo.getCallTimeLength());    			
    			//接通数量
    			int connectCount = autoDialDetailPo.getConnectCount();
    			//平均通话时长
    			autoDialDetailPo.setAvgCallTimeLength(String.valueOf(callTimeLength/connectCount));
    		}
    		
    	}
    }
    
    /**                   平均接电话用时
     * @param map         要被赋值的实体集合
     */
    public void avgAnswerTimeLength(Map<String,AutoDialDetailPo> map){    	
    	Iterator<Entry<String, AutoDialDetailPo>> iterator = map.entrySet().iterator();
    	while(iterator.hasNext()){
    		Entry<String, AutoDialDetailPo> entry = iterator.next();
    		AutoDialDetailPo autoDialDetailPo = entry.getValue();
    		if(autoDialDetailPo.getAnswerTimeLength()!=null){
    			//接电话用时
    			int answerTimeLength = Integer.parseInt(autoDialDetailPo.getAnswerTimeLength());
    			//接通数量
    			int connectCount = autoDialDetailPo.getConnectCount();
    			//平均接电话用时
    			autoDialDetailPo.setAvgAnswerTimeLength(String.valueOf(answerTimeLength/connectCount));    			
    		}
    		
    	}
    }

	// 导出报表事件
	public void exportReport(ClickEvent event) throws FileNotFoundException {
		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询,再导出!");
			return;
		}
		titleName = tab.getCaption();// 设置文件名
		list = getToTableList(addpMap);// 设置数据源
		columnNames = new ArrayList<String>();// 设置列名
		for(String columnName:columnHeaders){
			columnNames.add(columnName);
		}
		file = ExportExcelUtil.exportExcel(titleName, list, columnNames, startValue, endValue);// 生成excel file
		saveOperationLog();// 记录导出日志
		fileDownload(event);// 文件下载

	}
	
	public List<Object[]> getToTableList(Map<String, AutoDialDetailPo> map){
		//创建list
		List<Object[]> list = new ArrayList<Object[]>();
		//遍历map
        Iterator<Entry<String, AutoDialDetailPo>> iterator =map.entrySet().iterator();
        while(iterator.hasNext()){
        	Entry<String, AutoDialDetailPo> entry = iterator.next();
        	AutoDialDetailPo autoDialDetailPo = entry.getValue();
        	//创建一个size定死的object[]
        	Object[] objects = new Object[8];
        	//部门
        	objects[0] = autoDialDetailPo.getDept();
        	//工号
        	objects[1] = autoDialDetailPo.getEmpno();
        	//姓名
        	objects[2] = autoDialDetailPo.getName();
        	//接通数量
        	objects[3] = autoDialDetailPo.getConnectCount();
        	//通话时长
        	objects[4] = autoDialDetailPo.getCallTimeLength();
        	//平均通话时长
        	objects[5] = autoDialDetailPo.getAvgCallTimeLength();
        	//接电话用时
        	objects[6] = autoDialDetailPo.getAnswerTimeLength();
        	//平均接电话用时
        	objects[7] = autoDialDetailPo.getAvgAnswerTimeLength();
        	
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

	//记录导出日志
	private void saveOperationLog() {
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null)
			operationLog.setFilePath(file.getAbsolutePath());
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

	// 文件下载
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

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(cmbDept == source) {
			Department dept = (Department) cmbDept.getValue();
			if(dept != null) {
				empNoComboBox.removeAllItems();
				BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
				User showAllCsr = new User();
				showAllCsr.setEmpNo("全部-员工-部门");
				showAllCsr.setUsername("全部-员工-部门");
				userContainer.addBean(showAllCsr);
				List<Long> strList = new ArrayList<Long>();
				if(dept.getId().equals(0L)) {
					strList.addAll(deptidSet);
					List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
					userContainer.addAll(csrs);
					empNoComboBox.setContainerDataSource(userContainer);
				} else {
					strList.add(dept.getId());
					List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
					userContainer.addAll(csrs);
					empNoComboBox.setContainerDataSource(userContainer);
				}
				empNoComboBox.setValue(showAllCsr);
			}
		}
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
