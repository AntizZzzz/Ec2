package com.jiangyifen.ec2.ui.mgr.tabsheet.misscalllog;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MissCallLog;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MissCallLogService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
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
 * 
 * @Description 描述：电话漏接详情查看界面
 * 
 * @author  jrh
 * @date    2013年12月27日 上午9:33:25
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class MissCallLogManagement extends VerticalLayout implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "ringingStateTime", "destUserId", "destNum", "ringingDuration", "srcNum", "srcUniqueId", 
			"srcChannel", "srcUserId", "hangupTime", "destUniqueId", "destChannel"};
	
	private final String[] COL_HEADERS = new String[] {"记录编号", "振铃时间", "被叫坐席", "被叫分机", "振铃时长", "主叫号码", 
			"主叫标识", "主叫通道", "主叫坐席", "挂断时间", "被叫标识", "被叫通道"};
	
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);

	private Table detailRecordTable;
	private MissCallLogSimpleFilter missCallLogSimpleFilter;		// 迁移日志的简单搜索组件
	private FlipOverTableComponent<MissCallLog> missCallLogTableFlip;	// 为Table添加的翻页组件

	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户所有相关信息查看窗口 

	private Notification notification;							// 提示信息

	private Button exportExcel; 								// 导出记录的按钮
	private Embedded downloader; 								// 存放数据的组件
	private HorizontalLayout progressLayout; 					// 进度条组件存放布局管理器
	private ProgressIndicator pi; 								// 进度条

	private Domain domain;										// 当前登录用户所属域
	private User loginUser;										// 当前登录用户
	private Integer[] screenResolution;							// 屏幕分辨率
	private boolean isEncryptMobile = true;						// 默认使用加密的电话号码和手机号
	private ArrayList<String> ownBusinessModels;				// 当前用户拥有的权限
	private String exportPath;
	
	private CustomerResourceService customerResourceService;
	private MissCallLogService missCallLogService;
	private DepartmentService departmentService;				// 部门服务类
	private UserService userService;
	private CommonService commonService;
	
	public MissCallLogManagement() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		missCallLogService = SpringContextHolder.getBean("missCallLogService");
		departmentService = SpringContextHolder.getBean("departmentService");
		userService = SpringContextHolder.getBean("userService");
		commonService = SpringContextHolder.getBean("commonService");

		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);

		missCallLogSimpleFilter = new MissCallLogSimpleFilter();
		missCallLogSimpleFilter.setHeight("-1px");
		this.addComponent(missCallLogSimpleFilter);

		// 创建客户表格
		createMigrateLogTable();

		// 为表格添加右键单击事件
		addActionToTable(detailRecordTable);
		
		HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setSpacing(true);
		tableFooter.setWidth("100%");
		this.addComponent(tableFooter);

		HorizontalLayout tableFooterRightLayout = new HorizontalLayout();
		tableFooterRightLayout.setSpacing(true);
		tableFooter.addComponent(tableFooterRightLayout);
		tableFooter.setComponentAlignment(tableFooterRightLayout, Alignment.TOP_RIGHT);
		
		// 创建导出日志按钮
		createExportComponents(tableFooterRightLayout);
		
		// 创建翻页组件
		createTableFlipComponent(tableFooterRightLayout);

		// 根据分辨率设置表格行数
		setTablePageLength();

		// 为我的资源Tab 页上方的搜索组件传递翻页组件对象
		missCallLogSimpleFilter.setMeettingRecordTableFlip(missCallLogTableFlip);
		
		// 创建客户信息查看窗口及相关组件
		createCustomerInfoWindow();
	}

	private void createMigrateLogTable() {
		detailRecordTable = createFormatColumnTable();
		detailRecordTable.setWidth("100%");
		detailRecordTable.setHeight("-1px");
		detailRecordTable.setSelectable(true);
		detailRecordTable.setStyleName("striped");
		detailRecordTable.setNullSelectionAllowed(false);
		detailRecordTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		detailRecordTable.addGeneratedColumn("srcNum", new TelephonesColumnGenerator());
		this.addComponent(detailRecordTable);
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
                } else if ("destUserId".equals(colId)) {
                	User originator = userService.get(property.getValue());
                	if(originator != null) {
                		return originator.getMigrateCsr();
                	} 
                	return property.getValue()+"";
                } else if ("srcUserId".equals(colId)) {
                	User member = userService.get(property.getValue());
                	if(member != null) {
                		return member.getMigrateCsr();
                	} 
                	return property.getValue()+"";
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
				MissCallLog record = (MissCallLog) table.getValue();
				Long domainId = domain.getId();
				String number = record.getSrcNum();
				if(number == null || "".equals(number)) {
					return null;
				} 
				return customerResourceService.getCustomerResourceByPhoneNumber(number, domainId);
			}
			
			/**
			 * 判断客户是否存在，如果存在则弹屏，否则显示提示信息
			 * @param table
			 * @param customerResource
			 */
			private boolean confirmPopupWindow(final Table table, CustomerResource customerResource) {
				if(customerResource == null) {
					notification.setCaption("<font color='red'><B>未找到该客户，可能已被删除！</B></font>");
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
	 * 创建导出电话漏接详情的组件
	 * 
	 * @param tableFooterRightLayout
	 */
	private void createExportComponents(HorizontalLayout tableFooter) {
		HorizontalLayout exportHLayout = new HorizontalLayout();
		exportHLayout.setSpacing(true);
		tableFooter.addComponent(exportHLayout);
		tableFooter.setComponentAlignment(exportHLayout, Alignment.MIDDLE_LEFT);

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

		// 创建导出记录按钮
		exportExcel = new Button("导出Excel");
		exportExcel.setStyleName("default");
		exportExcel.addListener(this);
		exportExcel.setImmediate(true);
		if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_MISS_CALL_LOG)) {
			exportHLayout.addComponent(exportExcel);
		}
		
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
		for (Role role : loginUser.getRoles()) {
			if (role.getType().equals(RoleType.manager)) {
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
		
		// 根据当前用户所属部门，及其下属部门，创建动态查询语句, allGovernedDeptIds 至少会含有一个 id 为 0 的元素
		String deptIdSql = StringUtils.join(allGovernedDeptIds, ",");
				
		// 创建初始搜索语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("今天");
		String countSql = "select count(m) from MissCallLog as m where m.ringingStateTime >= '"+dateStrs[0]+"' and m.ringingStateTime <= '"+dateStrs[1]+"' and m.domainId = " +domain.getId()+" and m.destUserDeptId in ("+deptIdSql+")";
		String searchSql = countSql.replaceFirst("count\\(m\\)", "m")+" order by m.ringingStateTime desc";
		
		missCallLogTableFlip = new FlipOverTableComponent<MissCallLog>(MissCallLog.class, 
				missCallLogService, detailRecordTable, searchSql , countSql, null);

		detailRecordTable.setVisibleColumns(VISIBLE_PROPERTIES);
		detailRecordTable.setColumnHeaders(COL_HEADERS);
		
		tableFooter.addComponent(missCallLogTableFlip);
		tableFooter.setComponentAlignment(missCallLogTableFlip, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			detailRecordTable.setPageLength(32);
			missCallLogTableFlip.setPageLength(32, false);
		} else if(screenResolution[1] >= 1050) {
			detailRecordTable.setPageLength(30);
			missCallLogTableFlip.setPageLength(30, false);
		} else if(screenResolution[1] >= 900) {
			detailRecordTable.setPageLength(23);
			missCallLogTableFlip.setPageLength(23, false);
		} else if(screenResolution[1] >= 768) {
			detailRecordTable.setPageLength(16);
			missCallLogTableFlip.setPageLength(16, false);
		}
	}
	
	/**
	 *  创建客户信息查看窗口及相关组件
	 */
	private void createCustomerInfoWindow() {
		customerAllInfoWindow = new CustomerAllInfoWindow(RoleType.manager);
		customerAllInfoWindow.setResizable(false);
		customerInfoTabSheet = customerAllInfoWindow.getCustomerInfoTabSheet();
	}

	/**
	 * jrh 
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("srcNum")) {
				MissCallLog record = (MissCallLog) itemId;
				String number = record.getSrcNum();
				if(number == null || "".equals(number) || number.length() < 7) {
					return number;
				} else {
					return new TelephoneNumberInTableLabelStyle(number, isEncryptMobile);
				}
			} 
			return null;
		}
	}
	
	/**
	 * 刷新电话漏接详情显示界面，当Tab 页切换的时候调用
	 */
	public void updateTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			missCallLogTableFlip.refreshToFirstPage();
		} else {
			missCallLogTableFlip.refreshInCurrentPage();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == exportExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出电话漏接详情
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					exportExcel.setEnabled(false);
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 执行导出操作
					exportExcelThreadRun(domain, loginUser);
					exportExcel.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		}
	}

	/**
	 * 制作表格
	 */
	private boolean exportExcelThreadRun(Domain domain, User user) {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			String searchSql = missCallLogTableFlip.getSearchSql();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

			file = new File(new String((exportPath + "/电话漏接详情_"
					+ new Date().getTime() + ".xls").getBytes("GBK"),	"ISO-8859-1"));
			
			OperationLogUtil.simpleLog(loginUser, "导出电话漏接记录："+file.getName());
			
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
			operationLog.setDescription("管理员导出了电话漏接详情");
			operationLog.setProgrammerSee(searchSql);
			commonService.save(operationLog);
			//======================chb 操作日志======================//

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(jxl.format.Colour.LIGHT_GREEN);					// 设置单元格背景颜色为浅绿色

			// 记录查出来的客服记录Id值
			MissCallLog firstLog = (MissCallLog) detailRecordTable.firstItemId();
			if (firstLog == null) {
				notification.setCaption("<font color='red'><B>选出的结果为空，不能导出电话漏接详情！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			}


			// 每次加载500条
			int pageLen = 500;
			Long processCount = 0L;
			int index = 0;
			List<MissCallLog> migrateCustomerLogs = null;
			// 计数
			Long recordTotalCount = missCallLogTableFlip.getTotalRecord() + 0L;
			int totalPage = (missCallLogTableFlip.getTotalRecord() + pageLen - 1) / pageLen;
			if(totalPage == 0) totalPage++;
			
			for(int page = 1; page <= totalPage; page++) {
				// 如果当前已经查到超过50000条记录，则新建一个sheet
				if ((processCount % 50000) == 0) {
					++index;

					sheet = writableWorkbook.createSheet("Sheet" + index, index);
					// 设置excel列名
					for (int c = 0; c < COL_HEADERS.length; c++) {
						sheet.addCell(new jxl.write.Label(c, 0,	COL_HEADERS[c], cellFormat));
					}
				}
				int startIndex = (page-1) * pageLen;
				migrateCustomerLogs = missCallLogService.loadPageEntities(startIndex, pageLen, searchSql);
				
				processCount += pageLen;
				pi.setValue(processCount / (float) recordTotalCount);
				
				int cursor = sheet.getRows();
				for (int row = 0; row < migrateCustomerLogs.size(); row++) {
					MissCallLog mdr = migrateCustomerLogs.get(row);
					String destUserInfo = trimObjectToEmpty(mdr.getDestUserId());
					User destUser = userService.get(mdr.getDestUserId());
                	if(destUser != null) {
                		destUserInfo = destUser.getMigrateCsr();
                	} 
                	
                	String srcUserInfo = trimObjectToEmpty(mdr.getSrcUserId());
                	User srcUser = userService.get(mdr.getSrcUserId());
                	if(srcUser != null) {
                		srcUserInfo = srcUser.getMigrateCsr();
                	}
                	
					int col = 0;
					sheet.addCell(new jxl.write.Label(col, cursor + row, trimObjectToEmpty(mdr.getId())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, sdf.format(mdr.getRingingStateTime())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, destUserInfo));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getDestNum())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getRingingDuration())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getSrcNum())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getSrcUniqueId())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getSrcChannel())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, srcUserInfo));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, sdf.format(mdr.getHangupTime())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getDestUniqueId())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getDestChannel())));
				}
			}

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
			logger.error(e.getMessage() + " 导出电话漏接详情Excel出现异常!", e);
			notification.setCaption("电话漏接详情导出失败，请重试!");
			this.getApplication().getMainWindow().showNotification(notification);
			return false;
		}

		return true;
	}
	
	/**
	 * 将对象转换成字符串返回，如果对象为空，则返回空字符串
	 * @param obj
	 * @return
	 */
	private String trimObjectToEmpty(Object obj) {
		if(obj == null) {
			return "";
		} else {
			return StringUtils.trimToEmpty(obj.toString());
		}
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
					((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
				}
			}
		});
	}

}
