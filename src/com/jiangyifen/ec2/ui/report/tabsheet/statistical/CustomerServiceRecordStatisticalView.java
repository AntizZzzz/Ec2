package com.jiangyifen.ec2.ui.report.tabsheet.statistical;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.AchieveBasicUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;

/**
 * 客服记录统计详情界面
 * @author JHT
 * @date 2014-11-4 上午9:52:32
 */
public class CustomerServiceRecordStatisticalView extends VerticalLayout implements ClickListener, ValueChangeListener {

	private static final long serialVersionUID = 6533978966608409352L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 搜索组件
	private GridLayout gridLayout;					// 画板中布局管理器
	private ComboBox cmbStatisticType;				// 统计方式
	private ComboBox cmbTimeScope;					// 时间范围
	private PopupDateField pdfStartTime;			// 开始时间
	private PopupDateField pdfFinishTime;			// 截止时间
	private ComboBox cmbProject;					// 项目选择
	private ComboBox cmbDept;						// 部门选择
	private ComboBox cmbUser;						// 用户选择
	private ComboBox cmbCallDirection;				// 呼叫方向
	private Button btnSearch;						// 搜索按钮
	private Button btnClear;						// 清空输入内容按钮
	private Button btnExportExcel;					// 导出Excel按钮
	private Embedded downloader;					// 存放数据的组件

	// 表格组件
	private Table table;
	
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
	
	private final SimpleDateFormat SDF_MON = new SimpleDateFormat("yyyy-MM");
	private final SimpleDateFormat SDF_DAY = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat SDF_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final DecimalFormat  PERCENT_FMT = new DecimalFormat("##.##%");	// 百分比格式化工具
	
	// 查看记录的统计方式
	public static final String VIEW_TYPE_BY_MON = "by_mon";
	public static final String VIEW_TYPE_BY_DAY = "by_day";
	public static final String VIEW_TYPE_BY_HOUR = "by_hour";
	private final String VIEW_TYPE_DEFAULT = "by_day";
	
	// 其他参数
	private User loginUser;						// 当前登录用户
	private Domain domain;							// 当前用户所属域
	private String exportPath;						// 导出路径
	private String viewType;						// 获取查看的统计方式 ："by_mon"、"by_day"、"by_hour"
	private ArrayList<String> ownBusinessModels;	// 当前用户所拥有的管理角色权限
	private DepartmentService departmentService;	// 部门业务类
	private UserService userService;				// 座席业务类
	private MarketingProjectService marketingProjectService;	// 项目业务类
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;	// 座席记录状态业务类
	
	private CommonService commonService;
	
	private Map<Long, MarketingProject> projectMap = new HashMap<Long, MarketingProject>();
	private Map<Long, CustomerServiceRecordStatus> recordStatusMap = new HashMap<Long, CustomerServiceRecordStatus>();					// 用来存储记录状态的集合
	
	private Set<Long> deptidSet = new HashSet<Long>(); 				//该管理员管辖的部门编号列表
	private List<Object[]> allTableList = new ArrayList<Object[]>();	// 获取表格里的所有数据
	private List<String> headerShowList = new ArrayList<String>();		// 表格头部需要显示的信息
	private List<String> headerList = new ArrayList<String>();			// 表格头部显示信息，不带比率字段
	private List<Object> footerTotalList = new ArrayList<Object>();	// 表格底部的统计结果列
	private HashMap<String ,HashMap<Object, Object[]>> columnsMap = new HashMap<String, HashMap<Object, Object[]>>();	// 表格里的数据
	
	// 存放所管理的所有部门的信息
	private List<Department> deptList = new ArrayList<Department>();
	private BeanItemContainer<Department> deptContainer = new BeanItemContainer<Department>(Department.class);
	private AchieveBasicUtil achieveBasicUtil = new AchieveBasicUtil();
	
	// 构造函数
	public CustomerServiceRecordStatisticalView(){
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		initSpringContext();
		
		exportPath = ConfigProperty.PATH_EXPORT;
		
		/******************************************** UI 构建 ****************************************************/
		gridLayout = new GridLayout(4, 3);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(false);
		this.addComponent(gridLayout);
		
		createFirstHLayout();
		createSecondHLayout();
		createThiredHLayout();
		createTableAndButtonsLayout();
	}

	/**
	 * 初始化 Spring 相关业务对象
	 */
	private void initSpringContext() {
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		departmentService = SpringContextHolder.getBean("departmentService");
		userService = SpringContextHolder.getBean("userService");
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		customerServiceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		commonService = SpringContextHolder.getBean("commonService");
		
		Set<Role> roles = loginUser.getRoles(); // 获取登陆者所有的角色
		for (Role role : roles) {
			if (role.getType().equals(RoleType.manager)) {
				for (Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					deptidSet.add(dept.getId());
				}
			}
		}
	}
	
	// 创建  GridLayout第一行组件
	private void createFirstHLayout(){
		// 时间范围
		HorizontalLayout timeScopeHLayout = new HorizontalLayout();
		timeScopeHLayout.setSpacing(true);
		gridLayout.addComponent(timeScopeHLayout, 0, 0);
		
		Label timeScopeLabel = new Label("时间范围：");
		timeScopeLabel.setWidth("-1px");
		timeScopeHLayout.addComponent(timeScopeLabel);
		
		cmbTimeScope = new ComboBox();
		cmbTimeScope.setImmediate(true);
		cmbTimeScope.addItem("今天");
		cmbTimeScope.addItem("昨天");
		cmbTimeScope.addItem("本周");
		cmbTimeScope.addItem("上周");
		cmbTimeScope.addItem("本月");
		cmbTimeScope.addItem("上月");
		cmbTimeScope.addItem("精确时间");
		cmbTimeScope.setValue("今天");
		cmbTimeScope.setWidth("160px");
		cmbTimeScope.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cmbTimeScope.setNullSelectionAllowed(false);
		timeScopeHLayout.addComponent(cmbTimeScope);
		timeScopeListener = new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)cmbTimeScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				pdfStartTime.removeListener(startTimeListener);
				pdfFinishTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				pdfStartTime.setValue(dates[0]);
				pdfFinishTime.setValue(dates[1]);
				pdfStartTime.addListener(startTimeListener);
				pdfFinishTime.addListener(finishTimeListener);
			}
		};
		cmbTimeScope.addListener(timeScopeListener);
		
		// 开始时间
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		HorizontalLayout startTimeHLayout = new HorizontalLayout();
		startTimeHLayout.setSpacing(true);
		gridLayout.addComponent(startTimeHLayout, 1, 0);
				
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		startTimeHLayout.addComponent(startTimeLabel);
		
		startTimeListener = new ValueChangeListener() {
			private static final long serialVersionUID = 4178661219200611853L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				cmbTimeScope.removeListener(timeScopeListener);
				cmbTimeScope.setValue("精确时间");
				cmbTimeScope.addListener(timeScopeListener);
			}
		};
		
		pdfStartTime = new PopupDateField();
		pdfStartTime.setWidth("160px");
		pdfStartTime.setImmediate(true);
		pdfStartTime.setValue(dates[0]);
		pdfStartTime.addListener(startTimeListener);
		pdfStartTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pdfStartTime.setParseErrorMessage("时间格式不合法");
		pdfStartTime.setValidationVisible(false);
		pdfStartTime.setResolution(PopupDateField.RESOLUTION_HOUR);
		startTimeHLayout.addComponent(pdfStartTime);
		
		// 截止时间
		HorizontalLayout finishTimeHLayout = new HorizontalLayout();
		finishTimeHLayout.setSpacing(true);
		gridLayout.addComponent(finishTimeHLayout, 2, 0);
		
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		finishTimeHLayout.addComponent(finishTimeLabel);
		
		finishTimeListener = new ValueChangeListener() {
			private static final long serialVersionUID = 4531732042208014630L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				cmbTimeScope.removeListener(timeScopeListener);
				cmbTimeScope.setValue("精确时间");
				cmbTimeScope.addListener(timeScopeListener);
			}
		};
		
		pdfFinishTime = new PopupDateField();
		pdfFinishTime.setImmediate(true);
		pdfFinishTime.setWidth("160px");
		pdfFinishTime.setValue(dates[1]);
		pdfFinishTime.addListener(finishTimeListener);
		pdfFinishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		pdfFinishTime.setParseErrorMessage("时间格式不合法");
		pdfFinishTime.setValidationVisible(false);
		pdfFinishTime.setResolution(PopupDateField.RESOLUTION_HOUR);
		finishTimeHLayout.addComponent(pdfFinishTime);
		
		// 按钮组件
		/*HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		gridLayout.addComponent(layout, 3, 0);*/
		
		btnSearch = new Button("查 询", this);
		btnSearch.addStyleName("default");
		btnSearch.setWidth("60px");
		btnSearch.setImmediate(true);
		gridLayout.addComponent(btnSearch, 3, 0);
	}
	
	// 创建 GridLayout第二行组件
	private void createSecondHLayout(){
		// 项目选择
		HorizontalLayout projectLayout = new HorizontalLayout();
		projectLayout.setSpacing(true);
		gridLayout.addComponent(projectLayout, 0, 1);
		
		Label projectLabel = new Label("项目选择：");
		projectLabel.setWidth("-1px");
		projectLayout.addComponent(projectLabel);
		
		cmbProject = new ComboBox();
		cmbProject.setImmediate(true);
		cmbProject.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cmbProject.setWidth("160px");
		cmbProject.setNullSelectionAllowed(false);
		projectLayout.addComponent(cmbProject);
		
		// 部门选择
		HorizontalLayout deptLayout = new HorizontalLayout();
		deptLayout.setSpacing(true);
		gridLayout.addComponent(deptLayout, 1, 1);
		
		Label deptLabel = new Label("部门选择：");
		deptLabel.setWidth("-1px");
		deptLayout.addComponent(deptLabel);
		
		cmbDept = achieveBasicUtil.getCmbDeptReport(cmbDept, loginUser, deptList, deptContainer);
		cmbDept.setWidth("160px");
		deptLayout.addComponent(cmbDept);
		cmbDept.addListener(this);
		
		// 用户选择
		HorizontalLayout userLayout = new HorizontalLayout();
		userLayout.setSpacing(true);
		gridLayout.addComponent(userLayout, 2, 1);
		
		Label userLabel = new Label("用户选择：");
		userLabel.setWidth("-1px");
		userLayout.addComponent(userLabel);
		
		//工号选择
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User showAllCsr = new User();
		showAllCsr.setEmpNo("全部-员工-部门");
		showAllCsr.setUsername("全部-员工-部门");
		showAllCsr.setId(0L);
		userContainer.addBean(showAllCsr);
		List<Long> strList = new ArrayList<Long>();
		strList.addAll(deptidSet);
		List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
		userContainer.addAll(csrs);
		cmbUser = new ComboBox();
		cmbUser.setContainerDataSource(userContainer);
		cmbUser.setWidth("160px");
		cmbUser.setImmediate(true);
		cmbUser.setItemCaptionPropertyId("migrateCsr1");
		cmbUser.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cmbUser.setNullSelectionAllowed(false);
		cmbUser.setValue(showAllCsr);
		userLayout.addComponent(cmbUser);
		
		btnClear = new Button("清 空", this);
		btnClear.setWidth("60px");
		btnClear.setImmediate(true);
		gridLayout.addComponent(btnClear, 3, 1);
	}
	
	// 创建 GridLayout第三行组件
	private void createThiredHLayout(){
		// 统计方式
		HorizontalLayout viewTypeHLayout = new HorizontalLayout();
		viewTypeHLayout.setSpacing(true);
		gridLayout.addComponent(viewTypeHLayout, 1, 2);
		
		Label viewTypeLabel = new Label("统计方式：");
		viewTypeLabel.setWidth("-1px");
		viewTypeHLayout.addComponent(viewTypeLabel);
		
		cmbStatisticType = new ComboBox();
		cmbStatisticType.addListener((ValueChangeListener) this);
		cmbStatisticType.setImmediate(true);
//		cmbStatisticType.addItem(VIEW_TYPE_BY_HOUR);
		cmbStatisticType.addItem(VIEW_TYPE_BY_DAY);
		cmbStatisticType.addItem(VIEW_TYPE_BY_MON);
//		cmbStatisticType.setItemCaption(VIEW_TYPE_BY_HOUR, "按天、时统计");
		cmbStatisticType.setItemCaption(VIEW_TYPE_BY_DAY, "按天");
		cmbStatisticType.setItemCaption(VIEW_TYPE_BY_MON, "按月");
		cmbStatisticType.setValue(VIEW_TYPE_DEFAULT);
		cmbStatisticType.setWidth("160px");
		cmbStatisticType.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cmbStatisticType.setNullSelectionAllowed(false);
		viewTypeHLayout.addComponent(cmbStatisticType);
		
		// 呼叫方向
		HorizontalLayout directionLayout = new HorizontalLayout(); 
		directionLayout.setSpacing(true);
		gridLayout.addComponent(directionLayout, 0, 2);
		
		Label directionLabel = new Label("呼叫方向：");
		directionLabel.setWidth("-1px");
		directionLayout.addComponent(directionLabel);
		
		cmbCallDirection = new ComboBox();
		cmbCallDirection.setWidth("160px");
		cmbCallDirection.setImmediate(true);
		cmbCallDirection.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cmbCallDirection.setNullSelectionAllowed(false);
		cmbCallDirection.addItem("all");
		cmbCallDirection.addItem("outgoing");
		cmbCallDirection.addItem("incoming");
		cmbCallDirection.setItemCaption("all", "全部");
		cmbCallDirection.setItemCaption("outgoing", "呼出");
		cmbCallDirection.setItemCaption("incoming", "呼入");
		cmbCallDirection.setValue("all");
		directionLayout.addComponent(cmbCallDirection);
		
		
		btnExportExcel = new Button("导出Excel", this);
		btnExportExcel.setStyleName("default");
		btnExportExcel.setImmediate(true);
		if (ownBusinessModels.contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_CUSTOMER_SERVICE_RECORD_STATISTICAL)) {
			gridLayout.addComponent(btnExportExcel, 2, 2);
		}

		downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);
	}
	
	// 创建 表格和按钮组件
	private void createTableAndButtonsLayout(){
		table = new Table();
		table.setWidth("100%");
		table.setHeight("-1px");
		table.setHeight("100%");
		table.setSelectable(true);
		table.setNullSelectionAllowed(false);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(table);
		this.setComponentAlignment(table, Alignment.MIDDLE_CENTER);
		this.setExpandRatio(table, 1.0f);
		
	}
	

	// 按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnSearch){
			btnSearch.setEnabled(false);
			// 搜索组件输出的判断的判断
			if(!executeSearch()){
				btnSearch.setEnabled(true);
				return;
			}
			
			// 设置table表格头部信息
			for(String header:headerShowList){	
				table.addContainerProperty(header, String.class, " ");
			}
			table.setColumnHeaders(headerList.toArray(new String[headerList.size()]));
			
			allTableList.add(headerList.toArray());
			
			List<Object[]> columnsList = new ArrayList<Object[]>();
			// 设置表格数据显示
			for(Entry<String, HashMap<Object, Object[]>> entryRows:columnsMap.entrySet()){
				for(Entry<Object, Object[]> entry:entryRows.getValue().entrySet()){
					columnsList.add(entry.getValue());
				}
			}
			
			// 设置表格底部显示统计信息
			// 循环遍历出所有的列
			int columnsTotal = 0;	// 计算出来总数
			for(int i = 0; i < columnsList.size(); i++){	// 设置每一行的所有状态列的所占总数比率
				Object[] columns = columnsList.get(i);	// 获取每一列的内容
				if(columns.length > 5){
					int rowsTotal = ((Number)columns[5]).intValue();			// 第五列是客服记录总量
					columnsTotal += rowsTotal;
					for(int j = 6; j < columns.length; j++){
						if(j%2==1){
							if(rowsTotal ==0){
								columns[j] = PERCENT_FMT.format(0);
							} else {
								columns[j] = PERCENT_FMT.format(((Number)columns[j-1]).doubleValue()/rowsTotal);
							}
						}
					}
				}
				table.addItem(columns, null);
				allTableList.add(columns);
			}
			
			if(columnsList.size()<1){	// 判断表格内的数据是否大于1
//				this.getApplication().getMainWindow().showNotification("查询结果为空！", Notification.TYPE_WARNING_MESSAGE);
			} else{
				// 添加表格底部统计咧
				footerTotalList.set(5, columnsTotal);
				for(int i = 0; i < footerTotalList.size(); i++){	// 表格底部的统计行
					
					if(i > 5 && i%2 == 1){
						int total = ((Number)footerTotalList.get(5)).intValue();
						if(total == 0){
							footerTotalList.set(i, PERCENT_FMT.format(0));
						} else {
 							double colsTotal = ((Number)footerTotalList.get(i-1)).doubleValue()/total;
							footerTotalList.set(i, PERCENT_FMT.format(colsTotal));
						}
					}
				}
				
				table.addItem(footerTotalList.toArray(), null);
				allTableList.add(footerTotalList.toArray());
			}
			
			// 设置表格按照"统计时间"列进行降序排序
			table.setSortContainerPropertyId("统计时间");
			/**
			 * 由于第一次排序是没有完全按照规则显示，
			 * 所以需要再次进行排序，而第二次如果排序和第一次排序是一样的话，
			 * 会等同于没有执行任何操作，所以第二次排序吆喝第一次不一致
			 */
			table.setSortAscending(false);	// 如果为真为升序，为假是降序
			table.setSortAscending(true);	// 如果为真为升序，为假是降序
			btnSearch.setEnabled(true);
		} else if(btnClear == source){	// 清空选项
			cmbTimeScope.setValue("今天");
			cmbCallDirection.setValue("all");
			cmbStatisticType.setValue(VIEW_TYPE_DEFAULT);
		} else if(btnExportExcel == source){
			// 导出客服记录统计结果
			new Thread(new Runnable() {
				@Override
				public void run() {
					btnExportExcel.setEnabled(false);
					executeExportExcel();
					btnExportExcel.setEnabled(true);
				}
			}).start();
		}
	}
	
	/**
	 * 点击查询按钮执行的方法
	 */
	private boolean executeSearch(){
		// 每次点击搜索按钮时，清空表格里的数据
		if(columnsMap != null) columnsMap.clear();
		if(headerShowList != null) headerShowList.clear();
		if(footerTotalList != null) footerTotalList.clear();
		if(headerList != null) headerList.clear();
		if(allTableList != null) allTableList.clear();
		table.setValue(null);
		table.removeAllItems();
		
		MarketingProject inputProject = (MarketingProject) cmbProject.getValue();	// 项目选择
		Department inputDept = (Department) cmbDept.getValue();						// 部门选择
		User inputUser = (User) cmbUser.getValue();									// 用户选择
		String inputCallDirection = (String) cmbCallDirection.getValue();			// 呼叫方向	
		viewType = (String) cmbStatisticType.getValue();							// 统计方式
		
		Date start_time = (Date) pdfStartTime.getValue();
		if(start_time.after(new Date())) {
			this.getApplication().getMainWindow().showNotification("对不起, 开始时间不能超过当前时间！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		Date[] fmt_input_ts = executeRebuildTimeScope(viewType);	// 根据查看的统计方式，来重新配置时间范围值
		Date fmt_in_start_time = fmt_input_ts[0];
		Date fmt_in_end_time = fmt_input_ts[1];
		if(!fmt_in_end_time.after(fmt_in_start_time)) {
			this.getApplication().getMainWindow().showNotification("对不起，截止时间必须大于开始时间，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		} 
		pdfStartTime.setValue(fmt_in_start_time);
		pdfFinishTime.setValue(fmt_in_end_time);
		
		String warning = "对不起，单次只能查询一个月范围内的数据，请修改后重试！";
		Calendar cal = Calendar.getInstance();
		cal.setTime(fmt_in_end_time);
		if(VIEW_TYPE_BY_HOUR.equals(viewType)){
			cal.add(Calendar.DAY_OF_YEAR, -1);
			warning = "对不起，按天、时统计查询时，单次只能查询一天范围内的数据，请修改后重试！";
		} else {
			cal.add(Calendar.MONTH, -1);
		}
		
		Date minStartDate = cal.getTime();
		
		if(start_time.before(minStartDate)){
			this.getApplication().getMainWindow().showNotification(warning, Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		
		/**
		 * 统计视角
		 * 	1.根据各个座席进行统计
		 *  2.根据各个部门进行统计
		 *  3.根据各个项目进行统计  
		 *  4.根据整个租户进行统计
		 */
		
		// 根据各个项目进行统计
		try {
			accordingMarketingProjectCount(inputProject, inputDept, inputUser, inputCallDirection);
		} catch (Exception e) {
			btnSearch.setEnabled(true);
			logger.error("jinht -->> 查询客服记录统计报表时出现异常："+e.getMessage(), e);
		}
		return true;
	}
	
	/**
	 * 根据各个项目进行统计
	 * @param inputStartTime	开始时间
	 * @param inputFinishTime	截止时间
	 * @param inputProject		项目选择
	 * @param inputDept			部门选择
	 * @param inputUser			用户选择
	 * @param inputCallDirection	呼叫方向
	 */
	@SuppressWarnings("unchecked")
	private void accordingMarketingProjectCount(MarketingProject inputProject, Department inputDept, User inputUser, String inputCallDirection) {
		
		// 清空上次的查询数据
		Date input_start_time = (Date) pdfStartTime.getValue();
		Date input_finish_time = (Date) pdfFinishTime.getValue();
		
		Calendar calc_cal = Calendar.getInstance();
		calc_cal.setTime(input_start_time);
		Date startTime = calc_cal.getTime();
		
		String userIds = buildUserInfos();
		String callDirection = (String) cmbCallDirection.getValue();	// 呼叫方向
		
		if(userIds.trim().equals("")){	// 判断是否有用户
			this.getApplication().getMainWindow().showNotification("用户为空，无法进行统计客服记录状态！", Notification.TYPE_WARNING_MESSAGE);
			btnSearch.setEnabled(true);
			return;
		} else{
			
			// 设置表格头部显示标题
			headerShowList.add("统计时间");
			headerShowList.add("座席工号");
			headerShowList.add("座席姓名");
			headerShowList.add("部门名称");
			headerShowList.add("项目名称");
			headerShowList.add("客服记录总量");
			// 设置标题头部显示
			List<CustomerServiceRecordStatus> headsList = customerServiceRecordStatusService.getAllByEnable(true, domain.getId());;
			for(CustomerServiceRecordStatus recordStatus:headsList){
				headerShowList.add(recordStatus.getStatusName());
				headerShowList.add(recordStatus.getStatusName()+"比率");
			}
			
			// 对表格头部显示的列去除重名列
			List<String> listTemp = new ArrayList<String>();
			Iterator<String> iterator = headerShowList.iterator();
			while(iterator.hasNext()){
				String header = iterator.next();
				if(listTemp.contains(header)){
					iterator.remove();
				} else{
					listTemp.add(header);
				}
			}
			
			for(String name : headerShowList){
				if(!name.endsWith("比率")){
					headerList.add(name);
				} else{
					headerList.add(" ");
				}
			}
			
			// 底部统计显示列
			footerTotalList.add(0,"小计：");
			for(int i = 1; i < headerList.size(); i++){
				if(i<5){
					footerTotalList.add(i,"");
				} else{
					footerTotalList.add(i, 0);
				}
			}
			
			// 所管辖的员工
			List<Long> strList = new ArrayList<Long>();
			strList.addAll(deptidSet);
			List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());	
			Map<Long, User> userMap = new HashMap<Long, User>();
			for(User user : csrs) {
				userMap.put(user.getId(), user);
			}
			
			while(startTime.before(input_finish_time)){
				
				if(VIEW_TYPE_BY_HOUR.equals(viewType)){
					calc_cal.add(Calendar.HOUR_OF_DAY, +1);
				} else if(VIEW_TYPE_BY_DAY.equals(viewType)){
					calc_cal.add(Calendar.DAY_OF_YEAR, +1);
				} else if(VIEW_TYPE_BY_MON.equals(viewType)){
					calc_cal.add(Calendar.MONTH, +1);
				}
				Date finishTime = calc_cal.getTime();
				
				String start_time_str = SDF_SEC.format(startTime);
				String finish_time_str = SDF_SEC.format(finishTime);
			
				StringBuffer resultSqlBuffer = new StringBuffer("select r.marketingproject_id, r.creator_id, r.servicerecordstatus_id, count(r.id) from ec2_customer_service_record as r inner join ");
				resultSqlBuffer.append(" ( ");
				resultSqlBuffer.append(" select s.id as tid from ec2_customer_service_record as s left join ec2_user as u on s.creator_id=u.id ");
				resultSqlBuffer.append(" where s.createdate between '");
				resultSqlBuffer.append(start_time_str);
				resultSqlBuffer.append("' and '");
				resultSqlBuffer.append(finish_time_str);
				resultSqlBuffer.append("' ");
				
				if(inputUser != null && inputUser.getId() != 0L){	// 如果座席不为空
					resultSqlBuffer.append(" and s.creator_id=");
					resultSqlBuffer.append(inputUser.getId());
					resultSqlBuffer.append(" ");
				} else if(inputDept != null && inputDept.getId() != 0L){	// 如果座席为空，并且部门不为空
					/*resultSqlBuffer.append(" and u.department_id="+inputDept.getId()+" ");*/
					resultSqlBuffer.append(" and s.creator_id in(");
					resultSqlBuffer.append(userIds);
					resultSqlBuffer.append(") ");
				} else {	// 如果部门和用户选择框都没有选择
					resultSqlBuffer.append(" and s.creator_id in(");
					resultSqlBuffer.append(userIds);
					resultSqlBuffer.append(")");
				}
				if(inputProject != null && inputProject.getId() != 0L){	// 如果项目不为空
					resultSqlBuffer.append(" and s.marketingproject_id=");
					resultSqlBuffer.append(inputProject.getId());
					resultSqlBuffer.append(" ");
				}
				if(!callDirection.equals("all")){
					resultSqlBuffer.append(" and s.direction='");
					resultSqlBuffer.append(callDirection);
					resultSqlBuffer.append("' ");
				}
				resultSqlBuffer.append(" and s.domain_id=");
				resultSqlBuffer.append(domain.getId());
				resultSqlBuffer.append(" and s.domain_id=u.domain_id and s.marketingproject_id is not null ");
				resultSqlBuffer.append(" ) as t on r.id = t.tid ");
				resultSqlBuffer.append(" group by r.marketingproject_id, r.creator_id, r.servicerecordstatus_id ");
				resultSqlBuffer.append(" order by r.marketingproject_id, r.creator_id, r.servicerecordstatus_id");
				
				// 客服记录统计结果集合
				List<Object[]> objList = (List<Object[]>) commonService.excuteNativeSql(resultSqlBuffer.toString(), ExecuteType.RESULT_LIST);
				
				// 用来存储一个用户的数据
				HashMap<Object, Object[]> recordRowsMap = new HashMap<Object, Object[]>();		
				List<String> userProjectIdList = new ArrayList<String>();		// 用来存放用户 ID 和 项目 ID 拼接的字符串，以保证只添加同一条记录只显示一次
				List<Object> rowsList = new ArrayList<Object>();
				
				int recordTotalCount = 0;										// 用来存放客服记录总量
				
				
				for(Object[] objects : objList) {
					User user = userMap.get(objects[1]);						// 获取座席的实体类信息
					MarketingProject project = projectMap.get(objects[0]);		// 获取项目的实体类信息
					if(user != null) {											// 现在显示出来的结果是： 座席、 项目、 客服记录总数
						if(project == null ? !userProjectIdList.contains(user.getId() + "") : !userProjectIdList.contains((user.getId() + "-" + project.getId()))) {
							rowsList = new ArrayList<Object>();
							recordTotalCount = 0;
							for(int i = 0; i < headerList.size(); i++) {
								if(i < 5) {
									rowsList.add(i, "");
								} else {
									rowsList.add(i, 0);
								}
							}
							
							// 按月、按天、按时 查询时，显示的时间格式
							if(VIEW_TYPE_BY_MON.equals(viewType)){
								rowsList.set(0,SDF_MON.format(startTime));
							} else if(VIEW_TYPE_BY_DAY.equals(viewType)){
								rowsList.set(0,SDF_DAY.format(startTime));
							} else{
								rowsList.set(0,SDF_SEC.format(startTime));
							}
							
							rowsList.set(1, user.getEmpNo());		// 员工工号
							rowsList.set(2, user.getRealName());	// 员工真实姓名
							if(user.getDepartment() != null) {		// 员工所在部门名称
								rowsList.set(3, user.getDepartment().getName());
							}
						}
						
						/* 如果出问题的话，就应该在这里面，这种代码太麻烦不利于后期维护 */
						for(int i = 6; i < headerList.size(); i++) {
							CustomerServiceRecordStatus recordStatus = recordStatusMap.get(objects[2]);			// 获取客服记录状态信息
							if(project != null) {
								if(!userProjectIdList.contains(user.getId() + "-" + project.getId())) {			// 如果这个项目没有被添加过
									userProjectIdList.add(user.getId() + "-" + project.getId());
									rowsList.set(4, project.getProjectName());
								}
							} else {
								if(!userProjectIdList.contains(user.getId())) {
									userProjectIdList.add(user.getId()+"");
									rowsList.set(4, user.getId() + "");
								} 
							}
							if(recordStatus != null) {
								if(recordStatus.getStatusName().equals(headerList.get(i))) {	// 比较两个表头是否相等
									rowsList.set(i, objects[3]);								// 统计数量
									footerTotalList.set(i, ((Number)footerTotalList.get(i)).intValue()+((Number)objects[3]).intValue());
									recordTotalCount += ((Number)objects[3]).intValue();
									rowsList.set(5, recordTotalCount);							// 统计总数量
								}
							}
						}
						
						if(project != null) {													// 项目名称
							rowsList.set(4, project.getProjectName());
						}
						
						if(project != null) {
							recordRowsMap.put(user.getId() + "-" + project.getId(), rowsList.toArray());
						} else {
							recordRowsMap.put(user.getId() + "", rowsList.toArray()); 
						}
					}
				}
				
				if(recordRowsMap.size() > 0){
					columnsMap.put(SDF_SEC.format(startTime), recordRowsMap);
				}
				startTime = finishTime;					// 开始时间 赋值为上次的结束时间
			}
		}
	}
	
	// 返回座席编号
	private String buildUserInfos(){
		List<Long> strList = new ArrayList<Long>();
		Department dept = (Department) cmbDept.getValue();
		if(dept == null) {
			return "";
		}
		
		List<User> csrs = null;
		
		if(dept.getId().equals(0L)) {
			strList.addAll(deptidSet);
			csrs = userService.getCsrsByDepartment(strList, domain.getId());
		} else {
			strList.add(dept.getId());
			csrs = userService.getCsrsByDepartment(strList, domain.getId());
		}
		if(csrs == null) {
			return "";
		}
		
		List<Long> userIds = new ArrayList<Long>();
		for(User us : csrs){
			if(us.getId() != 0L){
				userIds.add(us.getId());
			}
		}
		return StringUtils.join(userIds,", ");
	}
	
	// 值修改监听事件
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(cmbDept == source){
			setCmbUserValue();
		} else if(cmbStatisticType == source){
			String type = (String) cmbStatisticType.getValue();
			if(VIEW_TYPE_BY_HOUR.equals(type)){
				pdfStartTime.setDateFormat("yyyy-MM-dd H");
				pdfStartTime.setResolution(PopupDateField.RESOLUTION_HOUR);
				pdfFinishTime.setDateFormat("yyyy-MM-dd H");
				pdfFinishTime.setResolution(PopupDateField.RESOLUTION_HOUR);
			} else if(VIEW_TYPE_BY_DAY.equals(type)){
				pdfStartTime.setDateFormat("yyyy-MM-dd");
				pdfStartTime.setResolution(PopupDateField.RESOLUTION_DAY);
				pdfFinishTime.setDateFormat("yyyy-MM-dd");
				pdfFinishTime.setResolution(PopupDateField.RESOLUTION_DAY);
			} else if(VIEW_TYPE_BY_MON.equals(type)){
				pdfStartTime.setDateFormat("yyyy-MM");
				pdfStartTime.setResolution(PopupDateField.RESOLUTION_MONTH);
				pdfFinishTime.setDateFormat("yyyy-MM");
				pdfFinishTime.setResolution(PopupDateField.RESOLUTION_MONTH);
			}
		}
	}
	
	// #region 设置用户选择ComboBox里面的内容
	private void setCmbUserValue(){
		if(cmbDept.getValue() != null){
			Department dept = (Department) cmbDept.getValue();
			if(dept == null) {
				this.getApplication().getMainWindow().getWindow().showNotification("请选择要查询的部门");
				return;
			}
			
			BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
			User showAllCsr = new User();
			showAllCsr.setEmpNo("全部-员工-部门");
			showAllCsr.setUsername("全部-员工-部门");
			showAllCsr.setId(0L);
			userContainer.addBean(showAllCsr);
			List<Long> strList = new ArrayList<Long>();
			if(dept.getId().equals(0L)) {
				strList.addAll(deptidSet);
				List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
				userContainer.addAll(csrs);
				cmbUser.setContainerDataSource(userContainer);
			} else {
				strList.add(dept.getId());
				List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());
				userContainer.addAll(csrs);
				cmbUser.setContainerDataSource(userContainer);
			}
			cmbUser.setValue(showAllCsr);
		}
	}
	// #end
	
	
	/**
	 * @Description 描述：根据查看的统计方式，来重新配置时间范围值
	 *
	 * @author  JRH
	 * @date    2014年5月7日 下午7:41:49
	 * @param viewType	查看的统计方式 ："by_mon"、"by_day"、"by_hour"	
	 * @return Date[]
	 */
	private Date[] executeRebuildTimeScope(String viewType) {
		Date input_start_time = (Date) pdfStartTime.getValue();
		Date input_end_time = (Date) pdfFinishTime.getValue();
		
		Date now_dt = new Date();
		if(now_dt.before(input_end_time)) {	// 如果当前时间比格式按指定要求格式化后的时间还要小，则进一步处理
			input_end_time = executeFormatTime(viewType, now_dt);
		}
		pdfFinishTime.setValue(input_end_time); 
		
		Calendar ini_cal = Calendar.getInstance();
		if(VIEW_TYPE_BY_HOUR.equals(viewType)) {		// 根据查看类型：修改开始时间，以及终止统计的时间
			ini_cal.setTime(input_start_time);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_start_time = ini_cal.getTime();

			ini_cal.setTime(input_end_time);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_end_time = ini_cal.getTime();
		} else if(VIEW_TYPE_BY_DAY.equals(viewType)) {
			ini_cal.setTime(input_start_time);
			ini_cal.set(Calendar.HOUR_OF_DAY, 0);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_start_time = ini_cal.getTime();

			ini_cal.setTime(input_end_time);
			ini_cal.set(Calendar.HOUR_OF_DAY, 0);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_end_time = ini_cal.getTime();
		} else if(VIEW_TYPE_BY_MON.equals(viewType)) {
			ini_cal.setTime(input_start_time);
			ini_cal.set(Calendar.DAY_OF_MONTH, 1);
			ini_cal.set(Calendar.HOUR_OF_DAY, 0);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_start_time = ini_cal.getTime();

			ini_cal.setTime(input_end_time);
			ini_cal.set(Calendar.DAY_OF_MONTH, 1);
			ini_cal.set(Calendar.HOUR_OF_DAY, 0);
			ini_cal.set(Calendar.MINUTE, 0);
			ini_cal.set(Calendar.SECOND, 0);
			ini_cal.set(Calendar.MILLISECOND, 0);
			input_end_time = ini_cal.getTime();
		}
		
		return new  Date[]{input_start_time, input_end_time};
	}

	/**
	 * @Description 描述：格式化截止时间
	 *		规则：
	 *		            分、秒、毫秒直接置为0
	 *			按小时：分钟  > 0 , 小时+1
	 *			按天：    小时  > 0 , 天数+1
	 *  		按月：    天数  > 0 , 月份+1
	 *  
	 * @author  JRH
	 * @date    2014年5月7日 下午8:15:45
	 * @param viewType	查看的统计方式 ："by_mon"、"by_day"、"by_hour"	
	 * @param datetime	带格式化的时间
	 * @return Date
	 */
	private Date executeFormatTime(String viewType, Date datetime) {
		Calendar cal_end = Calendar.getInstance();
		cal_end.setTime(datetime);
		if(VIEW_TYPE_BY_DAY.equals(viewType)) {		// 根据查看类型：修改截止时间
			if(cal_end.get(Calendar.HOUR_OF_DAY) > 0) {
				cal_end.add(Calendar.DAY_OF_YEAR, +1);
			}
			cal_end.set(Calendar.HOUR_OF_DAY, 0);
		} else if(VIEW_TYPE_BY_MON.equals(viewType)) {
			if(cal_end.get(Calendar.DAY_OF_MONTH) > 0) {
				cal_end.add(Calendar.MONTH, +1);
			}
			cal_end.set(Calendar.DAY_OF_MONTH, 1);
		} else if(VIEW_TYPE_BY_HOUR.equals(viewType)) {
			if(cal_end.get(Calendar.MINUTE) > 0) {
				cal_end.add(Calendar.HOUR_OF_DAY, +1);
			}
		}
	
		cal_end.set(Calendar.MINUTE, 0);
		cal_end.set(Calendar.SECOND, 0);
		cal_end.set(Calendar.MILLISECOND, 0);
		Date input_end_time = cal_end.getTime();
		return input_end_time;
	}

	@Override
	public void attach() {
		List<MarketingProject> projectList = marketingProjectService.getAll(domain); 
		List<CustomerServiceRecordStatus> recordStatusList = customerServiceRecordStatusService.getAll(domain); 
		
		// 初始化项目选项值
		BeanItemContainer<MarketingProject> projectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		MarketingProject mp = new MarketingProject();
		mp.setProjectName("全部");
		mp.setId(0L);
		projectList.add(0, mp);
		projectContainer.addAll(projectList);
		cmbProject.setContainerDataSource(projectContainer);
		cmbProject.setItemCaptionPropertyId("projectName");
		cmbProject.setValue(mp);
		
		if(projectList != null){
			for(MarketingProject project: projectList){
				projectMap.put(project.getId(), project);
			}
		}
		if(recordStatusList != null){
			for(CustomerServiceRecordStatus recordstatus:recordStatusList){
				recordStatusMap.put(recordstatus.getId(),recordstatus);
			}
		}
		
	}
	
	/**
	 * 制作Excel表格
	 */
	private boolean executeExportExcel(){
		// 把数据存到Excel文件中
		File file = null;
		FileOutputStream fos = null;
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = null;
		HSSFRow row = null;
		HSSFCell cell = null;
		
		if(allTableList.size() < 3){
			this.getApplication().getMainWindow().showNotification("查询结果为空，无法进行导出数据！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
		try {
			file = new File(new String((exportPath+"/客服记录统计_"+new Date().getTime()+".xls").getBytes("UTF-8"),"ISO-8859-1"));
//			file = new File(new String(("E:/excel/客服记录统计_"+new Date().getTime()+".xlsx").getBytes("UTF-8"),"ISO-8859-1"));
			if(!file.exists()){	// 如果目录不存在 
				if(!file.getParentFile().exists()){	// 如果父目录不存在
					file.getParentFile().mkdirs();	// 创建目录
				}
				try {
					file.createNewFile();	// 创建一个空目录
				} catch (Exception e) {
					throw new RuntimeException("无法在指定位置创建新Excel文件！");
				}
			} else{
				throw new RuntimeException("Excel文件已经存在，请重新创建！");
			}
			
			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"),"UTF-8");
			// 记录导出日志
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setFilePath(filePath);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(loginUser.getUsername());
			operationLog.setRealName(loginUser.getRealName());
			WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
			String ip = context.getBrowser().getAddress();
			operationLog.setIp(ip);
			operationLog.setDescription("导出客服记录统计");
			operationLog.setProgrammerSee("");
			commonService.save(operationLog);
			
			HSSFCellStyle style = wb.createCellStyle();
			style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
			style.setFillBackgroundColor(HSSFColor.GREEN.index);
			style.setFillForegroundColor(HSSFColor.GREEN.index);
			HSSFFont font = wb.createFont();
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			style.setFont(font);
			
			// 设置文本格式的单元格
			HSSFCellStyle styleString = wb.createCellStyle();
			HSSFDataFormat formatString = wb.createDataFormat();
			styleString.setDataFormat(formatString.getFormat("@"));
			
			HSSFCellStyle styleNumber = wb.createCellStyle();
			HSSFDataFormat formatNumber = wb.createDataFormat();
			styleNumber.setDataFormat(formatNumber.getFormat("#,##0"));
			
			HSSFCellStyle styleDouble = wb.createCellStyle();
			HSSFDataFormat formatDouble = wb.createDataFormat();
			styleDouble.setDataFormat(formatDouble.getFormat("0.00%"));
			
			sheet = wb.createSheet("客服记录统计");
			row = sheet.createRow(0);
			for(int i = 0; i < allTableList.get(0).length; i++){
				cell = row.createCell(i);
				cell.setCellValue(allTableList.get(0)[i]+"");
				cell.setCellStyle(style);
			}
			
			for(int i = 1; i < allTableList.size(); i++){
				row = sheet.createRow(i);
				Object[] columns = allTableList.get(i);
				for(int j = 0; j < columns.length; j++){
					HSSFCell cell2 = row.createCell(j);
					if(j<5){
						if(columns[j] == null) {
							cell2.setCellValue("");
						} else {
							cell2.setCellValue(columns[j].toString());
						}
						cell2.setCellStyle(styleString);
					} else if(j == 5){	// 设置为整数
						int value = 0;
						if(columns[j] != null) {
							value = ((Number)columns[j]).intValue();
						}
						cell2.setCellValue(value);
						cell2.setCellStyle(styleNumber);
					} else if(j%2==0){	// 设置为整数
						int value = 0;
						if(columns[j] != null) {
							value = ((Number)columns[j]).intValue();
						}
						cell2.setCellValue(value);
						cell2.setCellStyle(styleNumber);
					} else {	// 设置为百分比
						String str = "0";
						if(columns[j] != null) {
							str = columns[j].toString().substring(0, columns[j].toString().length()-1);
						}
						double value = 0;
						try {
							value = Double.parseDouble(str);
						} catch (Exception e) {}
						if(value>0){
							value=value/100;
						}
						cell2.setCellValue(value);
						cell2.setCellStyle(styleDouble);
					}
				}
			}
			
			fos = new FileOutputStream(file);
			wb.write(fos);
			downloadFile(file);
			
		} catch (Exception e) {
			btnExportExcel.setEnabled(true);
			this.getApplication().getMainWindow().showNotification("导出客服记录统计报表出现异常！", Notification.TYPE_WARNING_MESSAGE);
			logger.error("JHT 客服记录统计导出报表，出现异常-->>"+e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	// 文件创建好后，下载文件
	private void downloadFile(File file){
		Resource resource = new FileResource(file, this.getApplication());
		downloader.setSource(resource);
		this.getApplication().getMainWindow().addComponent(downloader);
		downloader.addListener(new RepaintRequestListener() {
			private static final long serialVersionUID = 6340697875298789625L;
			@Override
			public void repaintRequested(RepaintRequestEvent event) {
				if(downloader.getParent() != null){
					((VerticalLayout)(downloader.getParent())).removeComponent(downloader);
				}
			}
		});
	}
	
	
}
