package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Workbook;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MigrateCustomerLog;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MigrateCustomerLogService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.customermigratelog.CustomerMigrateSimpleFilter;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.event.Action;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 客户迁移日志
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class CustomerMigrateLogView extends VerticalLayout implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"migratedDate", "customerId", "customerName", "customerDefaultPhone", "customerCompanyName", "operatorEmpNo", "operatorRealName", 
			"operatorDeptName", "oldManagerEmpNo", "oldManagerRealName", "oldManagerDeptName", "newManagerEmpNo", "newManagerRealName", "newManagerDeptName"};
	
	private final String[] COL_HEADERS = new String[] {"迁移时间", "客户编号", "客户姓名", "客户电话", "客户公司", "迁移者工号", "迁移者姓名", "迁移者部门", "原经理工号", "原经理姓名", "原经理部门", "新经理工号", "新经理姓名", "新经理部门"};
	
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);

	private Table migrateLogTable;
	private CustomerMigrateSimpleFilter customerMigrateSimpleFilter;		// 迁移日志的简单搜索组件
	private FlipOverTableComponent<MigrateCustomerLog> migrateLogTableFlip;	// 为Table添加的翻页组件

	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户所有相关信息查看窗口 

	private Notification notification;							// 提示信息
	
	private Domain domain;										// 当前登录用户所属域
	private User loginUser;										// 当前登录用户
	private Integer[] screenResolution;							// 屏幕分辨率
	private boolean isEncryptMobile = true;						// 默认使用加密的电话号码和手机号
	
	private Button exportExcel; 								// 导出记录的按钮
	private Embedded downloader; 								// 存放数据的组件
	private HorizontalLayout progressLayout; 					// 进度条组件存放布局管理器
	private ProgressIndicator pi; 								// 进度条
	
	private String exportPath;
	
	private CustomerResourceService customerResourceService;
	private DepartmentService departmentService;				// 部门服务类
	private MigrateCustomerLogService migrateCustomerLogService;
	private CommonService commonService;
	
	public CustomerMigrateLogView() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		screenResolution = SpringContextHolder.getScreenResolution();
		
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		departmentService = SpringContextHolder.getBean("departmentService");
		migrateCustomerLogService = SpringContextHolder.getBean("migrateCustomerLogService");
		commonService = SpringContextHolder.getBean("commonService");

		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);

		customerMigrateSimpleFilter = new CustomerMigrateSimpleFilter();
		customerMigrateSimpleFilter.setHeight("-1px");
		this.addComponent(customerMigrateSimpleFilter);

		// 创建客户表格
		createMigrateLogTable();

		// 为表格添加右键单击事件
		addActionToTable(migrateLogTable);
		
		HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setSpacing(true);
		tableFooter.setWidth("100%");
		this.addComponent(tableFooter);

		// 创建导出日志按钮
		createExportComponents(tableFooter);
		
		// 创建翻页组件
		createTableFlipComponent(tableFooter);

		// 根据分辨率设置表格行数
		setTablePageLength();

		// 为我的资源Tab 页上方的搜索组件传递翻页组件对象
		customerMigrateSimpleFilter.setMigrateLogTableFlip(migrateLogTableFlip);
	}

	private void createMigrateLogTable() {
		migrateLogTable = createFormatColumnTable();
		migrateLogTable.setWidth("100%");
		migrateLogTable.setHeight("-1px");
		migrateLogTable.setSelectable(true);
		migrateLogTable.setStyleName("striped");
		migrateLogTable.setNullSelectionAllowed(false);
		migrateLogTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(migrateLogTable);
	}

	/**
	 * 创建格式化显示列的表格
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if (property.getType() == Date.class) {
            		return dateFormat.format((Date)property.getValue());
                } 
            	return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 为指定任务表格 添加右键单击事件
	 */
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(customerAllInfoWindow == null) {			// 只有单击右键时才创建客户信息查看窗口及相关组件
					createCustomerAllInfoWindow();
				}
				
				table.select(target);
				CustomerResource customerResource = getCustomerResource(table);
				boolean success = confirmPopupWindow(table, customerResource);
				if(success == true) {
					if(action == BASEINFO) {
						customerAllInfoWindow.echoCustomerBaseInfo(customerResource);
						customerInfoTabSheet.setSelectedTab(0);
					} else if(action == DESCRIPTIONINFO) {
						customerAllInfoWindow.echoCustomerDescription(customerResource);
						customerInfoTabSheet.setSelectedTab(1);
					} else if(action == ADDRESSINFO) {
						customerAllInfoWindow.echoCustomerAddress(customerResource);
						customerInfoTabSheet.setSelectedTab(2);
					} else if(action == HISTORYRECORD) {
						customerAllInfoWindow.echoHistoryRecord(customerResource);
						customerInfoTabSheet.setSelectedTab(3);
					} 
				}
			}

			/**
			 * 根据电话号码从数据空中查询客户对象
			 * @param table
			 * @return
			 */
			private CustomerResource getCustomerResource(final Table table) {
				// 按拨打号码进行查询资源
				MigrateCustomerLog migrateCustomerLog = (MigrateCustomerLog) table.getValue();
				String phoneNo = migrateCustomerLog.getCustomerDefaultPhone();
				Long domainId = domain.getId();
				CustomerResource customerResource = customerResourceService.getCustomerResourceByPhoneNumber(phoneNo, domainId);
				
				// 如若资源不存在，并且来电显示号码带 '0'，则去'0'查询； 如过来电显示号码不带'0'，则加'0'查询
				if(customerResource == null && phoneNo.startsWith("0")) {
					while(phoneNo.startsWith("0")) {
						phoneNo = phoneNo.substring(1);
					}
					customerResource = customerResourceService.getCustomerResourceByPhoneNumber(phoneNo, domainId);  
				} else if(customerResource == null && !phoneNo.startsWith("0")) {
					phoneNo = "0" + phoneNo;
					customerResource = customerResourceService.getCustomerResourceByPhoneNumber(phoneNo, domainId);  
				} 

				return customerResource;
			}
			
			/**
			 * 判断客户是否存在，如果存在则弹屏，否则显示提示信息
			 * @param table
			 * @param customerResource
			 */
			private boolean confirmPopupWindow(final Table table, CustomerResource customerResource) {
				if(customerResource == null) {
					notification.setCaption("<font color='red'><B>在数据库中查找该客户失败，可能已被删除！</B></font>");
					table.getApplication().getMainWindow().showNotification(notification);
					return false;
				} else {
					table.getApplication().getMainWindow().removeWindow(customerAllInfoWindow);
					table.getApplication().getMainWindow().addWindow(customerAllInfoWindow);
					customerAllInfoWindow.initCustomerResource(customerResource);
					return true;
				}
			}
			
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target != null) {
					return new Action[] {BASEINFO, DESCRIPTIONINFO, ADDRESSINFO, HISTORYRECORD};
				}
				return null;
			}
		});
	}

	/**
	 * 创建导出客户迁移日志的组件
	 * 
	 * @param tableFooterRightLayout
	 */
	private void createExportComponents(HorizontalLayout tableFooter) {
		HorizontalLayout exportHLayout = new HorizontalLayout();
		exportHLayout.setSpacing(true);
		tableFooter.addComponent(exportHLayout);
		tableFooter.setComponentAlignment(exportHLayout, Alignment.MIDDLE_LEFT);
		
		// 创建导出记录按钮
		exportExcel = new Button("导出Excel");
		exportExcel.setStyleName("default");
		exportExcel.addListener(this);
		exportExcel.setImmediate(true);
		if (SpringContextHolder.getBusinessModel().contains(
				MgrAccordion.CUSTOMER_MANAGEMENT_DOWNLOAD_CUSTOMER_MIGRATE_LOG_MANAGE)) {
			exportHLayout.addComponent(exportExcel);
		}

		// 创建进度组件
		progressLayout = new HorizontalLayout();
		progressLayout.setSpacing(true);
		progressLayout.setVisible(false);
		exportHLayout.addComponent(progressLayout);

		Label piLabel = new Label("上传进度：");
		piLabel.setWidth("-1px");
		progressLayout.addComponent(piLabel);

		pi = new ProgressIndicator();
		pi.setPollingInterval(100);
		progressLayout.addComponent(pi);
		progressLayout.setComponentAlignment(pi, Alignment.MIDDLE_LEFT);

		downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);
	}
	
	/**
	 * 创建翻页组件
	 */
	private void createTableFlipComponent(HorizontalLayout tableFooter) {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for (Department dept : departments) {
						Long deptId = dept.getId();
						if (!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
						}
					}
				}
			}
		}
		// 根据当前用户所属部门，及其下属部门，创建动态查询语句
		String deptIdSql = "";
		for(int i = 0; i < allGovernedDeptIds.size(); i++) {
			if( i == (allGovernedDeptIds.size() - 1) ) {
				deptIdSql += allGovernedDeptIds.get(i);
			} else {
				deptIdSql += allGovernedDeptIds.get(i) +", ";
			}
		}
		String deptSql = " and m.operatorDeptId in (" +deptIdSql+ ")";

		// 创建初始搜索语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("本周");
		String countSql = "select count(m) from MigrateCustomerLog as m where m.domainId = " +domain.getId() +" and m.migratedDate >= '" +dateStrs[0]+ "' and m.migratedDate < '" +dateStrs[1]+"'"+ deptSql;
		String searchSql = countSql.replaceFirst("count\\(m\\)", "m") + " order by m.migratedDate desc";
		
		migrateLogTableFlip = new FlipOverTableComponent<MigrateCustomerLog>(MigrateCustomerLog.class, 
				migrateCustomerLogService, migrateLogTable, searchSql , countSql, null);

		// jrh
		migrateLogTable.addGeneratedColumn("customerDefaultPhone", new TelephonesColumnGenerator());
		migrateLogTable.setVisibleColumns(VISIBLE_PROPERTIES);
		migrateLogTable.setColumnHeaders(COL_HEADERS);
		
		tableFooter.addComponent(migrateLogTableFlip);
		tableFooter.setComponentAlignment(migrateLogTableFlip, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			migrateLogTable.setPageLength(32);
			migrateLogTableFlip.setPageLength(32, false);
		} else if(screenResolution[1] >= 1050) {
			migrateLogTable.setPageLength(30);
			migrateLogTableFlip.setPageLength(30, false);
		} else if(screenResolution[1] >= 900) {
			migrateLogTable.setPageLength(23);
			migrateLogTableFlip.setPageLength(23, false);
		} else if(screenResolution[1] >= 768) {
			migrateLogTable.setPageLength(16);
			migrateLogTableFlip.setPageLength(16, false);
		}
	}
	
	/**
	 *  创建客户信息查看窗口及相关组件
	 */
	private void createCustomerAllInfoWindow() {
		customerAllInfoWindow = new CustomerAllInfoWindow(RoleType.manager);
		customerAllInfoWindow.setResizable(false);
//		TODO 需不需要让其修改客户信息，改完之后是否需要修改日志对应的客户信息
//		customerAllInfoWindow.setEchoModifyByReflect(this);
		
		customerInfoTabSheet = customerAllInfoWindow.getCustomerInfoTabSheet();
	}

	/**
	 * jrh 
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("customerDefaultPhone")) {
				MigrateCustomerLog customerResource = (MigrateCustomerLog) itemId;
				String telephone = customerResource.getCustomerDefaultPhone();
				return new TelephoneNumberInTableLabelStyle(telephone, isEncryptMobile);
			} 
			return null;
		}
	}
	
	/**
	 * 刷新客户迁移日志显示界面，当Tab 页切换的时候调用
	 */
	public void updateTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			migrateLogTableFlip.refreshToFirstPage();
		} else {
			migrateLogTableFlip.refreshInCurrentPage();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		final String downloadCountSql = migrateLogTableFlip.getCountSql();
		if (source == exportExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出客户迁移日志
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					exportExcel.setEnabled(false);
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 执行导出操作
					exportExcelThreadRun(downloadCountSql, domain, loginUser);
					exportExcel.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		}
	}

	/**
	 * 制作表格
	 */
	private boolean exportExcelThreadRun(String downloadCountSql,
			Domain domain, User user) {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

			//======================chb 操作日志======================//
			file = new File(new String((exportPath + "/客户迁移日志_"
					+ new Date().getTime() + ".xls").getBytes("GBK"),	"ISO-8859-1"));
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException("无法在指定位置创建新Excel文件！");
				}
			} else {
				throw new RuntimeException("Excel文件已经存在，请重新创建！");
			}

			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"), "GBK");

			// 记录导出日志
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setFilePath(filePath);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(user.getUsername());
			operationLog.setRealName(user.getRealName());
			WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
			String ip = context.getBrowser().getAddress();
			operationLog.setIp(ip);
			operationLog.setDescription("导出客户迁移日志");
			operationLog.setProgrammerSee(downloadCountSql.replaceFirst("count\\(m\\)", "m"));
			commonService.save(operationLog);
			//======================chb 操作日志======================//

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(jxl.format.Colour.LIGHT_GREEN);					// 设置单元格背景颜色为浅绿色

			// 记录查出来的客服记录Id值
			MigrateCustomerLog firstLog = (MigrateCustomerLog) migrateLogTable.firstItemId();
			if (firstLog == null) {
				notification.setCaption("<font color='red'><B>选出的结果为空，不能导出客户迁移日志！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			}

			// 计数
			Long recordCount = migrateCustomerLogService.getEntityCount(downloadCountSql) + 0L;
			
			// 查询符合条件的记录的最大id值
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(m\\)", "m");
			String maxidSql = downloadCountSql.replaceFirst("count\\(m\\)", "max\\(m.id\\)");
			Long logId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);

			// 每次加载500条
			List<MigrateCustomerLog> migrateCustomerLogs = null;
			Long processCount = 0L;
			int pageStep = 500;
			int index = 0;

			do {
				// 如果当前已经查到超过50000条记录，则新建一个sheet

				if ((processCount % 50000) == 0) {
					++index;

					sheet = writableWorkbook.createSheet("Sheet" + index, index);
					// 设置excel列名
					for (int i = 0; i < COL_HEADERS.length; i++) {
						sheet.addCell(new jxl.write.Label(i, 0,	COL_HEADERS[i], cellFormat));
					}
				}

				String useSql = downLoadSearchTmpSql + " and m.id <= " + logId + " order by m.id desc";
				migrateCustomerLogs = migrateCustomerLogService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (migrateCustomerLogs.size() > 0) {
					logId = migrateCustomerLogs.get(migrateCustomerLogs.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				for (int i = 0; i < migrateCustomerLogs.size(); i++) {
					MigrateCustomerLog migrateCustomerLog = migrateCustomerLogs.get(i);

					String migratedDate = "";
					if (migrateCustomerLog.getMigratedDate() != null) {
						migratedDate = sdf.format(migrateCustomerLog.getMigratedDate());
					}

					String customerId = "";
					if (migrateCustomerLog.getCustomerId() != null) {
						customerId = migrateCustomerLog.getCustomerId().toString();
					}
					
					String customerName = "";
					if (migrateCustomerLog.getCustomerName() != null) {
						customerName = migrateCustomerLog.getCustomerName();
					}

					String customerDefaultPhone = "";
					if (migrateCustomerLog.getCustomerDefaultPhone() != null) {
						customerDefaultPhone = migrateCustomerLog.getCustomerDefaultPhone();
					}

					String customerCompanyName = "";
					if (migrateCustomerLog.getCustomerCompanyName() != null) {
						customerCompanyName = migrateCustomerLog.getCustomerCompanyName();
					}

					String operatorEmpNo = "";
					if (migrateCustomerLog.getOperatorEmpNo() != null) {
						operatorEmpNo = migrateCustomerLog.getOperatorEmpNo();
					}

					String operatorRealName = "";
					if (migrateCustomerLog.getOperatorRealName() != null) {
						operatorRealName = migrateCustomerLog.getOperatorRealName();
					}

					String operatorDeptName = "";
					if (migrateCustomerLog.getOperatorDeptName() != null) {
						operatorDeptName = migrateCustomerLog.getOperatorDeptName();
					}

					String oldManagerEmpNo = "";
					if (migrateCustomerLog.getOldManagerEmpNo() != null) {
						oldManagerEmpNo = migrateCustomerLog.getOldManagerEmpNo();
					}

					String oldManagerRealName = "";
					if (migrateCustomerLog.getOldManagerRealName() != null) {
						oldManagerRealName = migrateCustomerLog.getOldManagerRealName();
					}

					String oldManagerDeptName = "";
					if (migrateCustomerLog.getOldManagerDeptName() != null) {
						oldManagerDeptName = migrateCustomerLog.getOldManagerDeptName();
					}

					String newManagerEmpNo = "";
					if (migrateCustomerLog.getNewManagerEmpNo() != null) {
						newManagerEmpNo = migrateCustomerLog.getNewManagerEmpNo();
					}

					String newManagerRealName = "";
					if (migrateCustomerLog.getNewManagerRealName() != null) {
						newManagerRealName = migrateCustomerLog.getNewManagerRealName();
					}

					String newManagerDeptName = "";
					if (migrateCustomerLog.getNewManagerDeptName() != null) {
						newManagerDeptName = migrateCustomerLog.getNewManagerDeptName();
					}

					int j = 0;
					sheet.addCell(new jxl.write.Label(j, cursor + i, migratedDate));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, customerId));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, customerName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, customerDefaultPhone));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, customerCompanyName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, operatorEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, operatorRealName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, operatorDeptName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, oldManagerEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, oldManagerRealName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, oldManagerDeptName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, newManagerEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, newManagerRealName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, newManagerDeptName));
				}
			} while (migrateCustomerLogs.size() > 0);

			// JXL 只能写一次,不然出问题
			writableWorkbook.write();
			writableWorkbook.close();

			// 文件创建好后，下载文件
			downloadFile(file);
		} catch (Exception e) {
			// 导出完成之后是导出按钮可用
			exportExcel.setEnabled(true);
			progressLayout.setVisible(false);
			e.printStackTrace();
			logger.error(e.getMessage() + " 导出客户迁移日志Excel出现异常!", e);
			notification.setCaption("客户迁移日志导出失败，请重试!");
			this.getApplication().getMainWindow().showNotification(notification);
			return false;
		}

		return true;
	}

	/**
	 * 文件创建好之后，创建文件
	 * 
	 * @param file
	 */
	private void downloadFile(File file) {
		// 下载报表
		Resource resource = new FileResource(file, this.getApplication());
		downloader.setSource(resource);
		this.getApplication().getMainWindow().addComponent(downloader);

		downloader.addListener(new RepaintRequestListener() {
			@Override
			public void repaintRequested(RepaintRequestEvent event) {
				if (downloader.getParent() != null) {
					((VerticalLayout) (downloader.getParent()))
							.removeComponent(downloader);
				}
			}
		});
	}

}
