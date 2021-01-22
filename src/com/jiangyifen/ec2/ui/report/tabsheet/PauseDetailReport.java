package com.jiangyifen.ec2.ui.report.tabsheet;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import jxl.Workbook;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.PauseReason;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.pojo.PauseDetailPo;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.PauseReasonService;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
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
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 查看置忙报表详情
 * @author xkp
 *
 */
public class PauseDetailReport extends VerticalLayout implements ClickListener, ValueChangeListener {

	private static final long serialVersionUID = -3766214473616594622L;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private String[] columnHeaders = new String[] { "部门", "用户名", "队列", "置忙总时间" };
	private Object[] visibleColumns = new Object[] { "dept", "username", "queue", "pauseTimeTotal" };

	private Button btnQuery;                // 查询按钮
	private Button btnExportReport;         // 导出报表按钮
	private ComboBox cbStatisticType;		// "统计方式"按天、时；按天；按月
	private PopupDateField pdfPause;		// “查询时间”选择框
	
	public static final String VIEW_TYPE_BY_MON = "by_mon";// 查看记录的统计方式
	public static final String VIEW_TYPE_BY_DAY = "by_day";
	private final String VIEW_TYPE_DEFAULT = "by_day";
	
	private ComboBox cbCsr;
	private Table table;                     // 表格组件
	private List<String> columnNames;	     // table的列名
	private File file;
	private Embedded downloader;			 // 下载文件组件
	
	private List<Object []> obList;          // 从数据库获得新的数据
	private StringBuffer sqlBuffer;
	private String queryDate;				  //查询时间
	private String lastDay;					  //如果按月查询时，当前选择月的最后一天
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private List<PauseReason> reasons;        // 置忙原因
	private Map<String, PauseDetailPo> pauseDetailPoMap;
	private List<PauseDetailPo> pauseDetailPoList;
	private PauseDetailPo po;				  //新置忙记录对象
	
	// 业务注入对象
	private UserService userService;
	private DepartmentService departmentService;
	private PauseReasonService pauseReasonService;
	private QueuePauseRecordService queuePauseRecordService;
	private ArrayList<String> ownBusinessModels;	   // 当前用户所有用的所有管理角色的权限
	private BeanItemContainer<PauseDetailPo> pauseDetailContainer = new BeanItemContainer<PauseDetailPo>(PauseDetailPo.class);

	private User loginUser;                            // 登陆的user
	private Domain domain;
	private String deptIds;                            // 登陆用户的
	private Set<Long> deptidSet = new HashSet<Long>(); // 该管理员管辖的部门编号列表
	
	public PauseDetailReport() {
		
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		initSpringContext();
		
		pauseDetailPoMap = new TreeMap<String, PauseDetailPo>();
		this.createTableTopComponents();                 // 创建表格上方的组件
		this.createTable();                              // 创建表格
		this.addColumn(table);
	}

	// 初始化spring相关业务对象
	private void initSpringContext() {

		loginUser = (User) SpringContextHolder.getLoginUser();
		domain = loginUser.getDomain();
		userService = SpringContextHolder.getBean("userService");
		pauseReasonService = SpringContextHolder.getBean("pauseReasonService");
		departmentService = SpringContextHolder.getBean("departmentService");
		queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");
		ownBusinessModels = SpringContextHolder.getBusinessModel();
 		
		Set<Role> roles = loginUser.getRoles();          // 获取登陆者所有的角色
		for (Role role : roles) {
			if (role.getType().equals(RoleType.manager)) {
				for (Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					deptidSet.add(dept.getId());
				}
			}
		}
		deptIds = StringUtils.join(deptidSet, ",");
	}

	// 创建表格上方的组件
	private void createTableTopComponents() {
		
		HorizontalLayout tableTopHLayout = new HorizontalLayout();
		tableTopHLayout.setSpacing(true);
		this.addComponent(tableTopHLayout);
		GridLayout gridLayout=new GridLayout(10,1);
		gridLayout.setSpacing(true);
		tableTopHLayout.addComponent(gridLayout);
		
		Label viewTypeLabel = new Label("统计方式：");
		viewTypeLabel.setWidth("-1px");
		gridLayout.addComponent(viewTypeLabel, 0, 0);
		
		Label startTimeLabel = new Label("查询时间：");
		startTimeLabel.setWidth("-1px");
		gridLayout.addComponent(startTimeLabel, 2, 0);
		
		pdfPause = new PopupDateField();
		pdfPause.setWidth("120px");
		pdfPause.setImmediate(true);
		pdfPause.setValue(new Date());
		pdfPause.setDateFormat("yyyy-MM-dd");
		pdfPause.setParseErrorMessage("时间格式不合法");
		pdfPause.setValidationVisible(false);
		pdfPause.setResolution(PopupDateField.RESOLUTION_DAY);
		gridLayout.addComponent(pdfPause, 3, 0);

		cbStatisticType = new ComboBox();	
		cbStatisticType.addListener((ValueChangeListener)this);
		cbStatisticType.setImmediate(true);
		cbStatisticType.addItem(VIEW_TYPE_BY_DAY);
		cbStatisticType.addItem(VIEW_TYPE_BY_MON);
		cbStatisticType.setItemCaption(VIEW_TYPE_BY_DAY, "按天");
		cbStatisticType.setItemCaption(VIEW_TYPE_BY_MON, "按月");
		cbStatisticType.setValue(VIEW_TYPE_DEFAULT);
		cbStatisticType.setWidth("100px");
		cbStatisticType.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		cbStatisticType.setNullSelectionAllowed(false);
		gridLayout.addComponent(cbStatisticType, 1, 0);
		
		Label lb_1 = new Label("用户名：");// 用户名选择
		gridLayout.addComponent(lb_1, 4, 0);

		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User showAllCsr = new User();	//设置ComboBox数据源
		showAllCsr.setEmpNo("全部 - 员工");
		showAllCsr.setUsername("全部 - 员工");
		List<Long> strList = new ArrayList<Long>(deptidSet);                       //部门编号集合
		List<User> csrs = userService.getCsrsByDepartment(strList, domain.getId());//登录用户管辖部门下的所有用户
		userContainer.removeAllItems();
		userContainer.addBean(showAllCsr);
		userContainer.addAll(csrs);
		
		cbCsr = new ComboBox();       //创建ComboBox 存放mgr所管理部门的全部用户
		cbCsr.setContainerDataSource(userContainer);
		cbCsr.setValue(showAllCsr);
		cbCsr.setWidth("158px");
		cbCsr.setNullSelectionAllowed(false);
		cbCsr.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		gridLayout.addComponent(cbCsr, 5, 0);
		
		for (User csr : userContainer.getItemIds()) {//更改ComboBox中caption名
			String caption = csr.getUsername();
			if (csr.getRealName() != null) {
				caption = caption + " - " + csr.getRealName();
			}
			cbCsr.setItemCaption(csr, caption);
		}
		btnQuery = new Button("查询", this);		// 按钮输出
		btnQuery.setStyleName("default");
		gridLayout.addComponent(btnQuery, 6, 0);
		
		btnExportReport = new Button("导出报表", this);
		if (ownBusinessModels.contains(MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_PAUSE_DETAIL)) {
			gridLayout.addComponent(btnExportReport, 7, 0);
		}
		
		downloader = new Embedded();          //导出组件
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);
	}

	// 创建表格显示区组件 
	private void createTable() {
		table = new Table() {
			private static final long serialVersionUID = -1739881779849126639L;
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if (colId.equals("pauseTimeTotal")) {
					String st = "";
					try {
						if (null != property.getValue()) {
							
							long millisecondTime = Long.valueOf(property.getValue().toString())/1000;
							st = DateUtil.getTime(millisecondTime);
						}
					} catch (Exception e) {
						logger.error("日期格式异常", e);
					}
					return st;
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};

		table.setSizeFull();
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);// 行号
		table.addStyleName("striped");
		table.setContainerDataSource(pauseDetailContainer);//设置数据源
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		this.addComponent(table);
		this.setExpandRatio(table, 1.0f);
	}
	
	//为Table添加详情
	private void addColumn(final Table table) {
		table.addGeneratedColumn("操作", new Table.ColumnGenerator() {
			private static final long serialVersionUID = 192515402563249806L;

			public Component generateCell(Table source, Object itemId, Object columnId) { 
				Button edit = new Button("详情");// 详情按钮
				edit.setStyleName(BaseTheme.BUTTON_LINK);
				edit.setData(itemId);
				edit.addListener((Button.ClickListener)PauseDetailReport.this);
				return edit;
			}
		});
	}
	
	//  执行查询 
	public void executeQuery() {
		
		sqlBuffer = new StringBuffer("select s.deptname,s.username,s.queue,s.pausedate,s.unpausedate from ec2_queue_member_pause_event_log as s  where 1=1  and s.unpauseDate is not null "); // sql语句生成
		if (pdfPause.getResolution() == PopupDateField.RESOLUTION_DAY) {// 是按天查询
			
			if (null != pdfPause.getValue()) { // 获取查询时间
				queryDate = format.format(pdfPause.getValue());
			} else {
				queryDate = format.format(new Date());
			}
			sqlBuffer.append(" and s.pauseDate >= '" + queryDate + " 00:00:00' ");
			sqlBuffer.append(" and s.pauseDate <= '" + queryDate + " 23:59:59' ");
		} else {	// 是按月查询

			SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM");
			if (null != pdfPause.getValue()) { // 获取查询时间
				queryDate = format1.format(pdfPause.getValue());
			} else {
				queryDate = format1.format(new Date());
			}

			Calendar cal = Calendar.getInstance();
			cal.setTime((Date) pdfPause.getValue());
			int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			cal.set(Calendar.DAY_OF_MONTH, value);
			Date lastDate = cal.getTime();
			lastDay = format.format(lastDate);// 获取当月的最后一天

			sqlBuffer.append(" and s.pauseDate >= '" + queryDate + "-01 00:00:00' ");
			sqlBuffer.append(" and s.pauseDate <= '" + lastDay + " 23:59:59' ");
		}

		reasons = pauseReasonService.getAllByEnabled(loginUser.getDomain(), true); // 置忙原因查询
		if (reasons.size() > 0) {
			sqlBuffer.append(" and s.reason in ('" + StringUtils.join(reasons, "','") + "') ");
		}
		if (null != cbCsr.getValue()) {           // 根据用户名查询
			User selectUser = (User) cbCsr.getValue();
			if (StringUtils.isNotEmpty(selectUser.getUsername()) && !"全部 - 员工".equals(selectUser.getUsername())) {
				sqlBuffer.append(" and s.username = '" + selectUser.getUsername() + "' ");
			}
		}
		sqlBuffer.append(" and s.deptId in(" + deptIds + ") ");
		sqlBuffer.append(" order by s.deptName ,s.username ,s.queue ");
		obList = queuePauseRecordService.getRecordsByNativeSql(sqlBuffer.toString());//执行查询,获取符合条件的所有置忙记录
		
		pauseDetailPoMap.clear();
	
		for (Object[] object : obList) {	       //将查询结果放入Map中，去掉重复，并计算置忙总时间
			String deptname = object[0].toString();//获取数组对象
			String username = object[1].toString();
			String queue = object[2].toString();
			Date pausedate = null;
			Date unpausedate = null;
			long cutTime = 0;                      //置忙时间
			
		    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				pausedate = format1.parse(object[3].toString());
				unpausedate = format1.parse(object[4].toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if (pausedate != null && unpausedate != null) {//计算置忙时间(置忙开始时间减置忙结束时间)
				cutTime = unpausedate.getTime() - pausedate.getTime();
			}
			
			String key = deptname +"_"+  username + "_"+ queue  ; //pauseDetailPoMap的Key值
			if (pauseDetailPoMap.containsKey(key)) {
				long totalTime = (pauseDetailPoMap.get(key).getPauseTimeTotal() + cutTime);
				pauseDetailPoMap.get(key).setPauseTimeTotal(totalTime);
			} else {
				po = new PauseDetailPo();
				po.setDept(deptname);
				po.setUsername(username);
				po.setQueue(queue);
				po.setPauseTimeTotal(cutTime);
				pauseDetailPoMap.put(key, po);
			}
		}
		
		pauseDetailPoList = new ArrayList<PauseDetailPo>();// map转list用于展示
		for (Entry<String, PauseDetailPo> entry : pauseDetailPoMap.entrySet()) {
			pauseDetailPoList.add(entry.getValue());
		}

		pauseDetailContainer.removeAllItems();//清空数据
		pauseDetailContainer.addAll(pauseDetailPoList);	  //填充数据
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == cbStatisticType) {	// 当查看类型发生变化时，修改时间范围组件的格式化类型
			String type = (String) cbStatisticType.getValue();
			if(VIEW_TYPE_BY_DAY.equals(type)) {
				pdfPause.setDateFormat("yyyy-MM-dd");
				pdfPause.setResolution(PopupDateField.RESOLUTION_DAY);
			} else if(VIEW_TYPE_BY_MON.equals(type)) {
				pdfPause.setDateFormat("yyyy-MM");
				pdfPause.setResolution(PopupDateField.RESOLUTION_MONTH);
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == btnQuery) {
			if (!pdfPause.isValid()) {
				this.getApplication().getMainWindow().showNotification("查询时间格式不正确，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			try {
				executeQuery();	//执行查询
			} catch (Exception e) {
				logger.error("执行置忙记录查询出现异常,可能输入的参数有误！-->" + e.getMessage(), e);
			}
			if (pauseDetailPoMap.size() <= 0) {
				NotificationUtil.showWarningNotification(this, "查询结果为空,请重新搜索!");
			}
			
		} else if (source == btnExportReport) {
			if (pauseDetailPoMap.size()<=0) {
				NotificationUtil.showWarningNotification(this, "请先查询,再导出!");
				return;
			} else {
				new Thread(new Runnable() {
					@Override
					public void run() {
						exportPauseReport();

					}
				}).start();
			}

		} else if (source.getCaption().equals("详情")) {
			po = (PauseDetailPo) event.getButton().getData();
			table.select(po);
			
			showPauseDetailView();//查看置忙记录详情
		}
	}
	
	//导出方法
	public void exportPauseReport() {

		columnNames = new ArrayList<String>(); // 设置列名
		for (String columnName : columnHeaders) {
			columnNames.add(columnName);
		}
		file = createExcleFile("置忙报表",pauseDetailPoList, columnNames, queryDate); // 生成excel
																							
		saveOperationLog();    // 记录导出日志
		fileDownload(file);    // 文件下载
	}
	
	//生成excle文件
	public File createExcleFile(String titleName, List<PauseDetailPo> list, List<String> columnNames, String startValue) {
		
		Logger logger = LoggerFactory.getLogger(PauseDetailReport.class);
		WritableWorkbook writableWorkbook = null;
		WritableSheet sheet = null;
		File file = null;
		Properties properties;
		String path;
		
		try {
			properties = new Properties();
			properties.load(PauseDetailReport.class.getClassLoader().getResourceAsStream("report.properties"));

			path = properties.getProperty("report_path");

			String filename = path + titleName + "(" + startValue + ").xls";
			file = new File(new String((filename).getBytes("GBK"), "ISO-8859-1"));

			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
				logger.info("----------报表文件创建成功----------");
			}

			writableWorkbook = Workbook.createWorkbook(file);
			sheet = writableWorkbook.createSheet("Sheet0", 0);
			sheet.setColumnView(3, 20);

			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(jxl.format.Colour.LIGHT_GREEN); // 设置单元格背景颜色为浅绿色

			WritableCellFormat cellFormat1 = new WritableCellFormat();
			cellFormat1.setAlignment(jxl.format.Alignment.CENTRE);
			// 设置excel列名
			for (int i = 0; i < columnNames.size(); i++) {
				sheet.addCell(new jxl.write.Label(i, 0, columnNames.get(i), cellFormat));
			}
			
			// 向excel中导入数据
			for (int i = 0; i < list.size(); i++) {
				PauseDetailPo pauseDetailPo = list.get(i);
				long pauseTimeTotal = (pauseDetailPo.getPauseTimeTotal())/1000;
				String pauseTime = DateUtil.getTime(pauseTimeTotal);
				sheet.addCell(new jxl.write.Label(0, i + 1, pauseDetailPo.getDept().toString(), cellFormat1));
				sheet.addCell(new jxl.write.Label(1, i + 1, pauseDetailPo.getUsername().toString(), cellFormat1));
				sheet.addCell(new jxl.write.Label(2, i + 1, pauseDetailPo.getQueue().toString(), cellFormat1));
				sheet.addCell(new jxl.write.Label(3, i + 1, pauseTime, cellFormat1));
			}

			writableWorkbook.write();
			writableWorkbook.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);

		}
		return file;
	}
	
	//保存导出日志 
	private void saveOperationLog() {
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(domain);
		if (file != null){
			operationLog.setFilePath(file.getAbsolutePath());
		}
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(loginUser.getUsername());
		operationLog.setRealName(loginUser.getRealName());
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("导出置忙报表");
		operationLog.setProgrammerSee(sqlBuffer.toString());
		CommonService commonService = SpringContextHolder.getBean("commonService");
		commonService.save(operationLog);
	}

	// 文件下载
	private void fileDownload(File file) {
		Resource resource = new FileResource(file, this.getApplication());
		downloader.setSource(resource);
		this.getApplication().getMainWindow().addComponent(downloader);
		downloader.addListener(new RepaintRequestListener() {
			private static final long serialVersionUID = 6802307542525747358L;
			@Override
			public void repaintRequested(RepaintRequestEvent event) {
				if (downloader.getParent() != null) {
					((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
				}
			}
		});
	}
	
	// 由buttonClick调用
	private void showPauseDetailView() {
		PauseDetaiRecordViewWindow pauseDetaiRecordViewWindow = new PauseDetaiRecordViewWindow(this, queryDate,lastDay, reasons);
		this.getApplication().getMainWindow().removeWindow(pauseDetaiRecordViewWindow);
		this.getApplication().getMainWindow().addWindow(pauseDetaiRecordViewWindow);
	}

	/**
	 * 返回Table的一个引用
	 * @return
	 */
	public Table getTable() {
		return table;
	}
	
	//置忙记录详情window调用
	public PopupDateField getPopupDate(){
		
		return pdfPause;
	}
}
