package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.callrecordmanage.CallRecordSimpleFilter;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.terminal.ExternalResource;
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
import com.vaadin.ui.Link;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 呼叫记录查看界面
 * 
 * @author jrh
 */
@SuppressWarnings("serial")
public class CallRecordManagement extends VerticalLayout implements
		ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	private final Object[] VISIBLE_PROPERTIES = new Object[] { "startTimeDate",
			"duration", "billableSeconds", "ec2_billableSeconds", "ringDuration", "cdrDirection", "isBridged", "recordFileName",
			"url", "resourceId", "src", "destination", "srcDeptName", "srcEmpNo",
			"srcRealName", "srcUsername", "destDeptName", "destEmpNo",
			"destRealName", "destUsername", "projectName", "isAutoDial",
			"userField"/*, "uniqueId", "channel", "destinationChannel",
			"lastApplication", "lastData"*/ };

	private final String[] COL_HEADERS = new String[] { "开始时间", "呼叫时长", "接通时长", "通话时长", "振铃时长",
			"呼叫方向", "接通情况", "录音文件名称", "试听/下载录音", "客户编号", "主叫", "被叫", "主叫部门", "主叫工号", "主叫姓名",
			"主叫用户名", "被叫部门", "被叫工号", "被叫姓名", "被叫用户名", "项目名称", "呼叫类型", "客户按键" /*,
			"unique-id", "主叫通道", "被叫通道", "last-app", "last-data"*/ };

	private Notification warningNotification; // 错误提示信息

	private Integer[] screenResolution; // 屏幕分辨率
	private Table callRecordTable; // 显示呼叫记录的表格
	private CallRecordSimpleFilter callRecordFilter; // 呼叫记录的查询工具
	private FlipOverTableComponent<Cdr> callRecordTableFlip; // 为Table添加的翻页组件

	private Button exportExcel; // 导出记录的按钮
	private Button exportCustomerInfo; // 导出客户信息
	private Button exportTapeUrl; // 批量导出录音的url
	private Embedded downloader; // 存放数据的组件
	private HorizontalLayout progressLayout; // 进度条组件存放布局管理器
	private ProgressIndicator pi; // 进度条

	private Label player; // 播放器显示标签
	private HorizontalLayout playerLayout; // 存放播放器的布局管理器

	private String exportPath;

	private User loginUser; // 当前登录的用户
	private ArrayList<String> ownBusinessModels;		// 当前用户拥有的权限
	private boolean isEncryptMobile = true; 			// 默认使用加密的电话号码和手机号

	private CdrService cdrService; // 呼叫记录服务类
	private DepartmentService departmentService; // 部门服务类
	private CommonService commonService;
	
	//录音基础路径
	private String baseUrl=BaseUrlUtils.getBaseUrl();

	public CallRecordManagement() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(false, true, false, true);

		loginUser = SpringContextHolder.getLoginUser();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();

		cdrService = SpringContextHolder.getBean("cdrService");
		departmentService = SpringContextHolder.getBean("departmentService");
		commonService = SpringContextHolder.getBean("commonService");

		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);

		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;

		warningNotification = new Notification("",
				Notification.TYPE_WARNING_MESSAGE);
		warningNotification.setDelayMsec(1000);
		warningNotification.setHtmlContentAllowed(true);

		// 创建Table下面的收索组件
		callRecordFilter = new CallRecordSimpleFilter();
		this.addComponent(callRecordFilter);

		// 存放cdr显示Table 和 翻页组件
		VerticalLayout callRecordVLayout = new VerticalLayout();
		callRecordVLayout.setSpacing(true);
		this.addComponent(callRecordVLayout);
		this.setExpandRatio(callRecordVLayout, 1);

		// 创建呼叫记录的查看表格
		createCallRecordTable(callRecordVLayout);

		HorizontalLayout tableFooterLayout = new HorizontalLayout();
		tableFooterLayout.setWidth("100%");
		tableFooterLayout.setSpacing(true);
		callRecordVLayout.addComponent(tableFooterLayout);

		// 创建播放录音组件
		createPlayerLayout(tableFooterLayout);

		HorizontalLayout tableFooterRightLayout = new HorizontalLayout();
		tableFooterRightLayout.setSpacing(true);
		tableFooterLayout.addComponent(tableFooterRightLayout);
		tableFooterLayout.setComponentAlignment(tableFooterRightLayout, Alignment.TOP_RIGHT);

		// 创建导出呼叫记录的组件、导出录音Url 的组件
		createExportComponents(tableFooterRightLayout);

		// 创建翻页组件
		createTableFlipOver(tableFooterRightLayout);
	}

	/**
	 * 创建呼叫记录的查看表格
	 * 
	 * @param callRecordVLayout
	 */
	private void createCallRecordTable(VerticalLayout callRecordVLayout) {
		callRecordTable = createFormatColumnTable();
		callRecordTable.setStyleName("striped");
		callRecordTable.setSelectable(true);
		callRecordTable.setImmediate(true);
		callRecordTable.setSizeFull();
		callRecordTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		callRecordTable.setColumnAlignment("calldate", Table.ALIGN_CENTER);
		callRecordTable.addGeneratedColumn("url", new ListenTypeColumnGenerator());
		callRecordTable.setColumnAlignment("url", Table.ALIGN_CENTER);
		callRecordVLayout.addComponent(callRecordTable);
	}

	/**
	 * 创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				if (property.getValue() == null && !"isAutoDial".equals(colId)) {
					return "";
				} else if (property.getType() == Date.class) {
					return dateFormat.format((Date) property.getValue());
				} else if ("isAutoDial".equals(colId)) {
					Boolean value = (Boolean) property.getValue();
					if (value == null) {
						return "手动呼叫";
					}
					return value ? "自动群呼" : "语音群发";
				} else if ("isBridged".equals(colId)) {
					boolean value = (Boolean) property.getValue();
					return value ? "已接通" : "";
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 创建播放录音组件
	 * 
	 * @param tableFooterLayout
	 */
	private void createPlayerLayout(HorizontalLayout tableFooterLayout) {
		playerLayout = new HorizontalLayout();
		playerLayout.setSpacing(true);
		tableFooterLayout.addComponent(playerLayout);

		String musicPath = "<EMBED src='' hidden=false autostart='false' width=360 height=63 "
				+ "type=audio/x-ms-wma volume='0' loop='-1' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
		player = new Label(musicPath, Label.CONTENT_XHTML);
		playerLayout.addComponent(player);
	}

	/**
	 * 创建导出呼叫记录的组件
	 * 
	 * @param tableFooterRightLayout
	 */
	private void createExportComponents(HorizontalLayout tableFooterRightLayout) {
		HorizontalLayout exportHLayout = new HorizontalLayout();
		exportHLayout.setSpacing(true);
		tableFooterRightLayout.addComponent(exportHLayout);
		tableFooterRightLayout.setComponentAlignment(exportHLayout,
				Alignment.MIDDLE_LEFT);

		// 创建进度组件
		progressLayout = new HorizontalLayout();
		progressLayout.setSpacing(true);
		progressLayout.setVisible(false);
		exportHLayout.addComponent(progressLayout);

		Label piLabel = new Label("下载进度：");
		piLabel.setWidth("-1px");
		progressLayout.addComponent(piLabel);

		pi = new ProgressIndicator();
		pi.setPollingInterval(100);
		progressLayout.addComponent(pi);
		progressLayout.setComponentAlignment(pi, Alignment.MIDDLE_LEFT);

		// 创建导出记录按钮
		if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_CALL_RECORD)) {
			exportExcel = new Button("导出Excel");
			exportExcel.setStyleName("default");
			exportExcel.addListener(this);
			exportExcel.setImmediate(true);
			exportHLayout.addComponent(exportExcel);
		}

		exportCustomerInfo = new Button("导出客户资源");
		exportCustomerInfo.setStyleName("default");
		exportCustomerInfo.addListener(this);
		exportCustomerInfo.setImmediate(true);
		// chb 导出Excel的客户信息蛮复杂
		// exportHLayout.addComponent(exportCustomerInfo);
		// 创建导出记录按钮
		if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_URL_CALL_RECORD)) {
			exportTapeUrl = new Button("批量导出录音URL");
			exportTapeUrl.addListener(this);
			exportTapeUrl.setImmediate(true);
			exportTapeUrl
					.setDescription("<B>导出录音下载地址后，再使用迅雷等下载工具即可直接进行批量下载！</B>");
			exportHLayout.addComponent(exportTapeUrl);
		}

		downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);
	}

	/**
	 * 创建表格的翻页组件
	 */
	private void createTableFlipOver(HorizontalLayout tableFooterRightLayout) {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for (Role role : loginUser.getRoles()) {
			if (role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService
						.getGovernedDeptsByRole(role.getId());
				if (departments.isEmpty()) {
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
		for (int i = 0; i < allGovernedDeptIds.size(); i++) {
			if (i == (allGovernedDeptIds.size() - 1)) {
				deptIdSql += allGovernedDeptIds.get(i);
			} else {
				deptIdSql += allGovernedDeptIds.get(i) + ", ";
			}
		}
		String deptSql = " and ( c.srcDeptId in (" + deptIdSql
				+ ") or c.destDeptId in (" + deptIdSql
				+ ") or (c.srcDeptId is null and c.destDeptId is null) )";

		// 创建初始搜索语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("今天");
		String countSql = "select count(c) from Cdr as c where c.startTimeDate >= '"
				+ dateStrs[0]
				+ "' and c.startTimeDate < '"
				+ dateStrs[1]
				+ "' and c.domainId = "
				+ loginUser.getDomain().getId()
				+ deptSql;
		String searchSql = countSql.replaceFirst("count\\(c\\)", "c")
				+ " order by c.startTimeDate desc";

		// 创建翻页组件
		callRecordTableFlip = new FlipOverTableComponent<Cdr>(Cdr.class,
				cdrService, callRecordTable, searchSql, countSql, null);
		callRecordFilter.setCallRecordTableFlip(callRecordTableFlip);
		callRecordTable.setVisibleColumns(VISIBLE_PROPERTIES);
		callRecordTable.setColumnHeaders(COL_HEADERS);
		if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SOUND_CALL_RECORD)) {
			callRecordTable.setColumnHeader("url", "试听/下载录音");
		} else {
			callRecordTable.setColumnHeader("url", "试听录音");
		} 

		// jrh
		callRecordTable.addGeneratedColumn("src",
				new TelephonesColumnGenerator());
		callRecordTable.addGeneratedColumn("destination",
				new TelephonesColumnGenerator());

		tableFooterRightLayout.addComponent(callRecordTableFlip);

		// 设置表格的显示行数
		setTablePageLength();
	}

	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if (screenResolution[1] == 768) {
			callRecordTable.setPageLength(16);
			callRecordTableFlip.setPageLength(16, false);
		} else if (screenResolution[1] == 900) {
			callRecordTable.setPageLength(23);
			callRecordTableFlip.setPageLength(23, false);
		} else if (screenResolution[1] == 1050) {
			callRecordTable.setPageLength(30);
			callRecordTableFlip.setPageLength(30, false);
		} else if (screenResolution[1] == 1080) {
			callRecordTable.setPageLength(32);
			callRecordTableFlip.setPageLength(32, false);
		}
	}

	/**
	 * jrh 用于自动生成可以拨打电话的列 如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			Cdr cdr = (Cdr) itemId;
			if (columnId.equals("src")) {
				String telephone = cdr.getSrc();
				return new TelephoneNumberInTableLabelStyle(telephone,
						isEncryptMobile);
			} else if (columnId.equals("destination")) {
				String telephone = cdr.getDestination();
				return new TelephoneNumberInTableLabelStyle(telephone,
						isEncryptMobile);
			}
			return null;
		}
	}

	/**
	 * 用于自动生成“试听录音”列
	 */
	private class ListenTypeColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final Cdr cdr = (Cdr) itemId;
			String tmpUrl = null;
			if(isEncryptMobile) {
				tmpUrl = cdr.getRealUrl(baseUrl);
			} else {
				tmpUrl = cdr.getUrl(baseUrl);
			}
			final String url = tmpUrl;

			if (url != null) {
				final Button listen = new Button("试听");
				listen.setStyleName(BaseTheme.BUTTON_LINK);
				listen.setIcon(ResourceDataCsr.listen_type_ico);
				listen.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if (player != null) {
							player.setValue(null);
							playerLayout.removeAllComponents();
						}

						// 判断当前是否对电话进行了加密，如果加密了，则播放器上的录音访问路径也不能显示电话号码，只能显示不带电话的录音名称
						String path = "<EMBED src='" +url+ "' hidden='false' autostart='true' width=360 height=63 " 
									+ "type=audio/x-ms-wma volume='0' loop='0' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
					
						player = new Label(path, Label.CONTENT_XHTML);
						playerLayout.addComponent(player);
					}
				});

				Link typelink = new Link("下载", new ExternalResource(url));
				typelink.setIcon(ResourceDataCsr.down_type_ico);
				typelink.setTargetName("_blank");
				
				//TODO The button with a caption is created by lc,as follows. 
//				final Button download = new Button("下载");
//				download.setIcon(ResourceDataCsr.down_type_ico);
//				download.setStyleName(BaseTheme.BUTTON_LINK);
//				if(cdr.getIsDownloaded()){
//					download.addStyleName("red");
//				}
//				download.addListener(new Button.ClickListener() {
//					
//					@Override
//					public void buttonClick(ClickEvent event) {
//						 
//						download.addStyleName("red");
//					 
//						downloadRecord(url);
//						
//						cdr.setIsDownloaded(true);
//						updateCdr(cdr);
//					}
//				});

				HorizontalLayout layout = new HorizontalLayout();
				layout.setSpacing(true);
				layout.setWidth("100%");
				layout.addComponent(listen);
				layout.setComponentAlignment(listen, Alignment.MIDDLE_LEFT);

				if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SOUND_CALL_RECORD)) {
					layout.addComponent(typelink);
					layout.setComponentAlignment(typelink, Alignment.MIDDLE_RIGHT);
				}

				return layout;
				
			} else {
				return new Label("无录音文件");
			}
		}
	}

	//TODO The function is added by lc,as follows.
	public void downloadRecord(String url) {

		url = "/var/www/html/"+url.substring(url.indexOf("monitor"), url.length());
		
		File file = new File(url);

		 logger.info("file.exists(): "+file.exists());
		 logger.info("url: "+url);
		 
	 
		if (!file.exists()) {
			return;
		}

		Resource resource = new FileResource(file, this.getApplication());

		final Embedded downloader = new Embedded();

		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setSource(resource);
		this.addComponent(downloader);

		downloader.addListener(new RepaintRequestListener() {

			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent()))
						.removeComponent(downloader);
			}
		});
		
		
		recordOperatorLog(file,null);
		 
	}
	
	//Update a cdr entity.
	public void updateCdr(Cdr cdr){
		
		
		cdrService.update(cdr);
	}
	
	//Record the operator log.
	public void recordOperatorLog(File file,String sql){
		
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null)
			operationLog.setFilePath(file.getAbsolutePath());
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(SpringContextHolder.getLoginUser()
				.getUsername());
		operationLog.setRealName(SpringContextHolder.getLoginUser()
				.getRealName());
		WebApplicationContext context = (WebApplicationContext) this
				.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("mgr 呼叫记录");
		operationLog.setProgrammerSee(sql);
		CommonService commonService = SpringContextHolder
				.getBean("commonService");
		commonService.save(operationLog);
	}

	 

	/**
	 * 刷新界面，当Tab页发生变化时调用
	 */
	public void refreshTable() {
		callRecordTableFlip.refreshInCurrentPage();
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		final String downloadCountSql = callRecordTableFlip.getCountSql();
		if (source == exportExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出呼叫记录
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
		} else if (source == exportCustomerInfo) {
			final Domain domain = loginUser.getDomain();
			// 导出呼叫记录
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					exportCustomerInfo.setEnabled(false);
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 执行导出操作
					exportCustomerInfoThreadRun(downloadCountSql, domain,
							loginUser);
					exportCustomerInfo.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		} else if (source == exportTapeUrl) {
			// 批量导出录音下载地址
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					exportTapeUrl.setEnabled(false);
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 批量执行导出url
					exportUrlThreadRun(downloadCountSql);
					exportTapeUrl.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		}
	}

	/**
	 * 批量执行导出url
	 */
	private boolean exportUrlThreadRun(String downloadCountSql) {
		// 把数据存到excel文件中
		File urlfile = null;
		FileOutputStream fos = null;
		BufferedOutputStream bw = null;
		try {
			// 录音下载地址文件
			urlfile = new File(new String(("录音下载地址.txt").getBytes("GBK"),
					"ISO-8859-1"));
			fos = new FileOutputStream(urlfile);
			bw = new BufferedOutputStream(fos);
			
			OperationLogUtil.simpleLog(loginUser, "导出录音url："+urlfile.getName());
			
			// 记录查出来的客服记录Id值
			Cdr firstRecord = (Cdr) callRecordTable.firstItemId();
			if (firstRecord == null) {
				warningNotification.setCaption("选出的结果为空，不能导出录音下载地址！");
				this.getApplication().getMainWindow()
						.showNotification(warningNotification);
				return false;
			}

			// 计数
			Long recordCount = cdrService.getEntityCount(downloadCountSql) + 0L;

			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(c\\)",
					"max\\(c.id\\)");
			Long recordId = (Long) commonService.excuteSql(maxidSql,
					ExecuteType.SINGLE_RESULT);

			String downLoadSearchTmpSql = downloadCountSql.replaceFirst(
					"count\\(c\\)", "c");

			// 每次加载500条
			List<Cdr> cdrs = null;
			Long processCount = 0L;
			int pageStep = 500;

			do {
				StringBuffer urlContent = new StringBuffer();
				// 只下载有录音文件的
				String useSql = downLoadSearchTmpSql + " and c.id <= "
						+ recordId + " order by c.id desc";
				cdrs = cdrService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (cdrs.size() > 0) {
					recordId = cdrs.get(cdrs.size() - 1).getId() - 1;
				}

				for (Cdr cdr : cdrs) {
					String url = cdr.getUrl(baseUrl);
					if (url != null) {
						urlContent.append(url + "\r\n");
					}
				}
				String sipcontentStr = urlContent.toString();
				bw.write(sipcontentStr.getBytes());
			} while (cdrs.size() > 0);
		} catch (Exception e) {
			// 导出完成之后是导出按钮可用
			exportTapeUrl.setEnabled(true);
			progressLayout.setVisible(false);
			logger.error(e.getMessage() + " Manager 批量导出录音下载地址出现异常!", e);
			warningNotification.setCaption("录音下载地址批量导出失败，请重试!");
			this.getApplication().getMainWindow()
					.showNotification(warningNotification);
			return false;
		}

		finally {
			try {
				bw.close();
				fos.close();
				urlfile = zipFile(urlfile);

				// 文件创建好后，下载文件
				downloadFile(urlfile);
			} catch (IOException e) {
				logger.error(
						e.getMessage() + " Manger 批量导出录音下载地址，IO 流关闭时出现异常！", e);
				warningNotification.setCaption("录音下载地址批量导出失败，请重试!");
				this.getApplication().getMainWindow()
						.showNotification(warningNotification);
				return false;
			}
		}
		return true;
	}

	/**
	 * 压缩文件
	 * 
	 * @param sourceFile
	 * @return
	 */
	private File zipFile(File sourceFile) {
		try {
			String filename = new String(sourceFile.getName().getBytes(
					"ISO-8859-1"), "GBK");
			FileOutputStream fos = new FileOutputStream(sourceFile.getName()
					+ ".zip");
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ZipOutputStream zos = new ZipOutputStream(bos);// 压缩包
			ZipEntry ze = new ZipEntry(filename);// 这是压缩包名里的文件名
			zos.putNextEntry(ze);// 写入新的 ZIP 文件条目并将流定位到条目数据的开始处

			FileInputStream fis = new FileInputStream(sourceFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = bis.read(buf)) != -1) {
				zos.write(buf, 0, len);
				zos.flush();
			}
			bis.close();
			zos.close();
			return new File(sourceFile.getAbsolutePath() + ".zip");
		} catch (Exception e) {
			throw new RuntimeException("压缩录音URL 地址文件是出现异常 --> "
					+ e.getMessage());
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

			// ======================chb 操作日志======================//
			file = new File(new String((exportPath + "/呼叫记录_"
					+ new Date().getTime() + ".xls").getBytes("GBK"),
					"ISO-8859-1"));
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException("无法再指定位置创建新Excel文件！");
				}
			} else {
				throw new RuntimeException("Excel文件已经存在，请重新创建！");
			}

//			String filePath = new String(
//					(file.getAbsolutePath()).getBytes("ISO-8859-1"), "GBK");

			OperationLogUtil.simpleLog(loginUser, "导出呼叫记录："+file.getName());

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(Colour.LIGHT_GREEN);					// 设置单元格背景颜色为浅绿色

			// 记录查出来的客服记录Id值
			Cdr firstRecord = (Cdr) callRecordTable.firstItemId();
			if (firstRecord == null) {
				warningNotification.setCaption("选出的结果为空，不能导出呼叫记录！");
				this.getApplication().getMainWindow()
						.showNotification(warningNotification);
				return false;
			}

			// 计数
			Long recordCount = cdrService.getEntityCount(downloadCountSql) + 0L;

			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(c\\)",
					"max\\(c.id\\)");
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst(
					"count\\(c\\)", "c");
			Long recordId = (Long) commonService.excuteSql(maxidSql,
					ExecuteType.SINGLE_RESULT);

			// 每次加载500条
			List<Cdr> records = null;
			Long processCount = 0L;
			int pageStep = 500;
			int index = 0;

			do {
				// 如果当前已经查到超过50000条记录，则新建一个sheet

				if ((processCount % 50000) == 0) {
					++index;

					sheet = writableWorkbook
							.createSheet("Sheet" + index, index);
					// 设置excel列名
					for (int i = 0; i < COL_HEADERS.length; i++) {
						if ("试听/下载录音".equals(COL_HEADERS[i])
								|| "试听录音".equals(COL_HEADERS[i])) {
							sheet.addCell(new jxl.write.Label(i, 0, "录音文件下载路径", cellFormat));
						} else {
							sheet.addCell(new jxl.write.Label(i, 0, COL_HEADERS[i], cellFormat));
						}
					}
				}

				String useSql = downLoadSearchTmpSql + " and c.id <= "
						+ recordId + " order by c.id desc";
				records = cdrService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (records.size() > 0) {
					recordId = records.get(records.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				
				for (int i = 0; i < records.size(); i++) {
					Cdr record = records.get(i);

					String startTimeDate = "";
					if (record.getStartTimeDate() != null) {
						startTimeDate = sdf.format(record.getStartTimeDate());
					}

					String duration = "";
					if (record.getDuration() != null) {
						duration = record.getDuration().toString();
					}

					String billableSeconds = "0";
					if (record.getBillableSeconds() != null) {
						billableSeconds = record.getBillableSeconds()
								.toString();
					}

					String ec2_billableSeconds = "0";
					if (record.getEc2_billableSeconds() != null) {
						ec2_billableSeconds = record.getEc2_billableSeconds()
								.toString();
					}

					String ringDuration = record.getRingDuration().toString();
					
					String cdrDirection = "";
					if (record.getCdrDirection() != null) {
						cdrDirection = record.getCdrDirection().getName();
					}

					String bridged = "未接通";
					Boolean isBridged = record.getIsBridged();
					if (isBridged != null && isBridged) {
						bridged = "已接通";
					}

					// 录音文件名称
					String recordFileName = "";
					if (record.getRecordFileName() != null) {
						recordFileName = record.getRecordFileName();
					}

					String url = "无录音文件";
					if (record.getUrl(baseUrl) != null) {
						url = record.getUrl(baseUrl);
					}

					String destination = "";
					if (record.getDestination() != null) {
						destination = record.getDestination().toString();
					}

					String resourceId = "";
					if(record.getResourceId() != null) {
						resourceId = record.getResourceId().toString();
					}
					
					String src = "";
					if (record.getSrc() != null) {
						src = record.getSrc().toString();
					}

					String srcDeptName = "";
					if (record.getSrcDeptName() != null) {
						srcDeptName = record.getSrcDeptName().toString();
					}

					String srcEmpNo = "";
					if (record.getSrcEmpNo() != null) {
						srcEmpNo = record.getSrcEmpNo().toString();
					}

					String srcRealName = "";
					if (record.getSrcRealName() != null) {
						srcRealName = record.getSrcRealName().toString();
					}

					String srcUsername = "";
					if (record.getSrcUsername() != null) {
						srcUsername = record.getSrcUsername().toString();
					}

					String destDeptName = "";
					if (record.getDestDeptName() != null) {
						destDeptName = record.getDestDeptName().toString();
					}

					String destEmpNo = "";
					if (record.getDestEmpNo() != null) {
						destEmpNo = record.getDestEmpNo().toString();
					}

					String destRealName = "";
					if (record.getDestRealName() != null) {
						destRealName = record.getDestRealName().toString();
					}

					String destUsername = "";
					if (record.getDestUsername() != null) {
						destUsername = record.getDestUsername().toString();
					}

					String projectName = "";
					if (record.getProjectName() != null) {
						projectName = record.getProjectName().toString();
					}

					String isAutoDial = "手动呼叫";
					if (record.getIsAutoDial() != null
							&& record.getIsAutoDial() == true) {
						isAutoDial = "自动群呼";
					} else if (record.getIsAutoDial() != null
							&& record.getIsAutoDial() == false) {
						isAutoDial = "语言群发";
					}

					// TODO 目前只是只是针对 山西焦炭 才这么做到
					String userfield = "";
					if (record.getUserField() != null) {
						userfield = record.getUserField().toString();
					}

//					String uniqueId = "";
//					if (record.getUniqueId() != null) {
//						uniqueId = record.getUniqueId().toString();
//					}
//
//					String channel = "";
//					if (record.getChannel() != null) {
//						channel = record.getChannel().toString();
//					}
//
//					String destinationChannel = "";
//					if (record.getDestinationChannel() != null) {
//						destinationChannel = record.getDestinationChannel()
//								.toString();
//					}
//
//					String lastApplication = "";
//					if (record.getLastApplication() != null) {
//						lastApplication = record.getLastApplication()
//								.toString();
//					}
//
//					String lastData = "";
//					if (record.getLastData() != null) {
//						lastData = record.getLastData().toString();
//					}

					int j = 0;
					sheet.addCell(new jxl.write.Label(j, cursor + i, startTimeDate));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, duration));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, billableSeconds));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, ec2_billableSeconds));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, ringDuration));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, cdrDirection));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, bridged));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, recordFileName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, url));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, resourceId));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, src));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, destination));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, srcDeptName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, srcEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, srcRealName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, srcUsername));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, destDeptName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, destEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, destRealName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, destUsername));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, projectName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, isAutoDial));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, userfield));
					
//					sheet.addCell(new jxl.write.Label(++j, cursor + i, uniqueId));
//					sheet.addCell(new jxl.write.Label(++j, cursor + i, channel));
//					sheet.addCell(new jxl.write.Label(++j, cursor + i, destinationChannel));
//					sheet.addCell(new jxl.write.Label(++j, cursor + i, lastApplication));
//					sheet.addCell(new jxl.write.Label(++j, cursor + i, lastData));
				}
			} while (records.size() > 0);

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
			logger.error(e.getMessage() + " 导出呼叫记录Excel出现异常!", e);
			warningNotification.setCaption("呼叫记录导出失败，请重试!");
			this.getApplication().getMainWindow()
					.showNotification(warningNotification);
			return false;
		}

		return true;
	}

	/**
	 * 导出客户信息
	 */
	@SuppressWarnings("unused")
	private boolean exportCustomerInfoThreadRun(String downloadCountSql,
			Domain domain, User user) {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			// ======================chb 操作日志======================//
			file = new File(new String((exportPath + "/客户信息_"
					+ new Date().getTime() + ".xls").getBytes("GBK"),
					"ISO-8859-1"));
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				try {
					file.createNewFile();
				} catch (IOException e) {
					throw new RuntimeException("无法再指定位置创建新Excel文件！");
				}
			} else {
				throw new RuntimeException("Excel文件已经存在，请重新创建！");
			}

//			String filePath = new String(
//					(file.getAbsolutePath()).getBytes("ISO-8859-1"), "GBK");

			OperationLogUtil.simpleLog(loginUser, "导出客户信息："+file.getName());

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);

			// 记录查出来的客服记录Id值
			Cdr firstRecord = (Cdr) callRecordTable.firstItemId();
			if (firstRecord == null) {
				warningNotification.setCaption("选出的结果为空，不能导出客户信息！");
				this.getApplication().getMainWindow()
						.showNotification(warningNotification);
				return false;
			}

			// 计数
			Long recordCount = cdrService.getEntityCount(downloadCountSql) + 0L;

			// 查询符合条件的记录的最大id值
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst(
					"count\\(c\\)", "c");
			String maxidSql = downloadCountSql.replaceFirst("count\\(c\\)",
					"max\\(c.id\\)");
			Long recordId = (Long) commonService.excuteSql(maxidSql,
					ExecuteType.SINGLE_RESULT);

			// 每次加载500条
			List<Cdr> records = null;
			Long processCount = 0L;
			int pageStep = 500;
			int index = 0;

			do {
				// 如果当前已经查到超过50000条记录，则新建一个sheet

				if ((processCount % 50000) == 0) {
					++index;

					sheet = writableWorkbook
							.createSheet("Sheet" + index, index);
				}

				String useSql = downLoadSearchTmpSql + " and c.id <= "
						+ recordId + " order by c.id desc";
				records = cdrService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (records.size() > 0) {
					recordId = records.get(records.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				for (int i = 0; i < records.size(); i++) {
					Cdr record = records.get(i);
				}
			} while (records.size() > 0);

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
			logger.error(e.getMessage() + " 导出呼叫记录Excel出现异常!", e);
			warningNotification.setCaption("呼叫记录导出失败，请重试!");
			this.getApplication().getMainWindow()
					.showNotification(warningNotification);
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

	/**
	 * 刷新界面
	 * 
	 * @param isToFirst
	 */
	public void updateTable(boolean isToFirst) {
		if (isToFirst) {
			callRecordTableFlip.refreshToFirstPage();
		} else {
			callRecordTableFlip.refreshInCurrentPage();
		}
	}

}
