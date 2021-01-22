package com.jiangyifen.ec2.ui.mgr.tabsheet.csrtimers;

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
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.TimersService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
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
 * 
 * @Description 描述：坐席定时预约详情查看界面
 * 
 * @author  jrh
 * @date    2013年12月27日 上午9:33:25
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class CsrTimersOrderManagement extends VerticalLayout implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"responseTime", "customerId", "customerPhoneNum", "dialCount", 
			"lastDialTime", "firstRespTime", "popCount", "creator", "isCsrForbidPop", "title", "content", "forbidRespTime", "type"};
	
	private final String[] COL_HEADERS = new String[] {"下次响应时间", "预约客户编号", "预约客户电话", "回访客户次数", 
			"最近回访时间", "首次响应时间", "累计提醒次数", "创建坐席", "是否禁止提醒", "标题", "提醒内容", "禁止提醒时间", "响应类型"};
	
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);

	private Table csrTimersOrderTable;
	private CsrTimersOrderSimpleFilter csrTimersOrderSimpleFilter;		// 迁移日志的简单搜索组件
	private FlipOverTableComponent<Timers> csrTimersOrderTableFlip;	// 为Table添加的翻页组件

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
	private TimersService timersService;
	private DepartmentService departmentService;				// 部门服务类
	private CommonService commonService;
	
	public CsrTimersOrderManagement() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		timersService = SpringContextHolder.getBean("timersService");
		departmentService = SpringContextHolder.getBean("departmentService");
		commonService = SpringContextHolder.getBean("commonService");

		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);

		csrTimersOrderSimpleFilter = new CsrTimersOrderSimpleFilter();
		csrTimersOrderSimpleFilter.setHeight("-1px");
		this.addComponent(csrTimersOrderSimpleFilter);

		// 创建客户表格
		createCsrTimersOrderTable();

		// 为表格添加右键单击事件
		addActionToTable(csrTimersOrderTable);
		
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
		csrTimersOrderSimpleFilter.setCsrTimersOrderTableFlip(csrTimersOrderTableFlip);
		
		// 创建客户信息查看窗口及相关组件
		createCustomerInfoWindow();
	}

	private void createCsrTimersOrderTable() {
		csrTimersOrderTable = createFormatColumnTable();
		csrTimersOrderTable.setWidth("100%");
		csrTimersOrderTable.setHeight("-1px");
		csrTimersOrderTable.setSelectable(true);
		csrTimersOrderTable.setStyleName("striped");
		csrTimersOrderTable.setNullSelectionAllowed(false);
		csrTimersOrderTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		csrTimersOrderTable.addGeneratedColumn("customerPhoneNum", new TelephonesColumnGenerator());
		this.addComponent(csrTimersOrderTable);
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
                } else if ("creator".equals(colId)) {
                	User creator = (User) property.getValue();
            		return creator.getMigrateCsr();
                } else if ("isCsrForbidPop".equals(colId)) {
                	Boolean isforbid = (Boolean) property.getValue();
                	return isforbid ? "是" : "";
                } else if(colId.equals("content")) {
	            	String content = (String) property.getValue();
	            	return content.replaceAll("<.+?>", "");	// 去掉HTML 语句
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
				Timers record = (Timers) table.getValue();
				Long customerId = record.getCustomerId();
				if(customerId == null) {
					return null;
				} 
				return customerResourceService.get(customerId);
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
	 * 创建导出坐席定时预约详情的组件
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
		String countSql = "select count(t) from Timers as t where t.responseTime >= '"+dateStrs[0]+"' and t.responseTime <= '"+dateStrs[1]
				+"' and t.domain.id = " +domain.getId()+" and t.customerId is not null and t.creator.department.id in ("+deptIdSql+")";
		String searchSql = countSql.replaceFirst("count\\(t\\)", "t")+" order by t.creator.id asc, t.responseTime desc";
		
		csrTimersOrderTableFlip = new FlipOverTableComponent<Timers>(Timers.class, 
				timersService, csrTimersOrderTable, searchSql , countSql, null);

		csrTimersOrderTable.setVisibleColumns(VISIBLE_PROPERTIES);
		csrTimersOrderTable.setColumnHeaders(COL_HEADERS);
		
		tableFooter.addComponent(csrTimersOrderTableFlip);
		tableFooter.setComponentAlignment(csrTimersOrderTableFlip, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			csrTimersOrderTable.setPageLength(32);
			csrTimersOrderTableFlip.setPageLength(32, false);
		} else if(screenResolution[1] >= 1050) {
			csrTimersOrderTable.setPageLength(30);
			csrTimersOrderTableFlip.setPageLength(30, false);
		} else if(screenResolution[1] >= 900) {
			csrTimersOrderTable.setPageLength(23);
			csrTimersOrderTableFlip.setPageLength(23, false);
		} else if(screenResolution[1] >= 768) {
			csrTimersOrderTable.setPageLength(16);
			csrTimersOrderTableFlip.setPageLength(16, false);
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
	 * 用于自动生成电话的列
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("customerPhoneNum")) {
				Timers record = (Timers) itemId;
				String number = record.getCustomerPhoneNum();
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
	 * 刷新坐席定时预约详情显示界面，当Tab 页切换的时候调用
	 */
	public void updateTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			csrTimersOrderTableFlip.refreshToFirstPage();
		} else {
			csrTimersOrderTableFlip.refreshInCurrentPage();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == exportExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出坐席定时预约详情
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
			String searchSql = csrTimersOrderTableFlip.getSearchSql();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

			file = new File(new String((exportPath + "/坐席定时预约详情_"
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
			operationLog.setDescription("管理员导出了坐席定时预约详情");
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
			Timers firstLog = (Timers) csrTimersOrderTable.firstItemId();
			if (firstLog == null) {
				notification.setCaption("<font color='red'><B>选出的结果为空，不能导出坐席定时预约详情！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			}


			// 每次加载500条
			int pageLen = 500;
			Long processCount = 0L;
			int index = 0;
			List<Timers> migrateCustomerLogs = null;
			// 计数
			Long recordTotalCount = csrTimersOrderTableFlip.getTotalRecord() + 0L;
			int totalPage = (csrTimersOrderTableFlip.getTotalRecord() + pageLen - 1) / pageLen;
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
				migrateCustomerLogs = timersService.loadPageEntities(startIndex, pageLen, searchSql);
				
				processCount += pageLen;
				pi.setValue(processCount / (float) recordTotalCount);
				
				int cursor = sheet.getRows();
				for (int row = 0; row < migrateCustomerLogs.size(); row++) {
					Timers mdr = migrateCustomerLogs.get(row);
					String destUserInfo = "";
					User destUser = mdr.getCreator();
                	if(destUser != null) {
                		destUserInfo = destUser.getMigrateCsr();
                	} 

                	String lastDialTime = "";
                	if(mdr.getLastDialTime() != null) {
                		lastDialTime = sdf.format(mdr.getLastDialTime());
                	}
                	
                	String forbidInfo = "";
                	Boolean isforbid = mdr.getIsCsrForbidPop();
                	if(isforbid != null && isforbid) {
                		forbidInfo = "是";
                	}
                	
                	String content = "";
                	if(mdr.getContent() != null) {
                		content = mdr.getContent().replaceAll("<.+?>", "");	// 去掉HTML 语句
                	}
                	
                	String forbidTime = "";
                	if(mdr.getForbidRespTime() != null) {
                		forbidTime = sdf.format(mdr.getForbidRespTime());
                	}
                	
					int col = 0;
					sheet.addCell(new jxl.write.Label(col, cursor + row, sdf.format(mdr.getResponseTime())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getCustomerId())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getCustomerPhoneNum())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getDialCount())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, lastDialTime));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, sdf.format(mdr.getFirstRespTime())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getPopCount())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, destUserInfo));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, forbidInfo));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getTitle())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, content));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, forbidTime));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getType())));
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
			logger.error(e.getMessage() + " 导出坐席定时预约详情Excel出现异常!", e);
			notification.setCaption("坐席定时预约详情导出失败，请重试!");
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
