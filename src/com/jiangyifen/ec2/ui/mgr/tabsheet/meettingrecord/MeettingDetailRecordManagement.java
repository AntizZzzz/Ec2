package com.jiangyifen.ec2.ui.mgr.tabsheet.meettingrecord;

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
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MeettingDetailRecord;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.MeettingDetailRecordService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
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
import com.vaadin.ui.themes.BaseTheme;

/**
 * 
 * @Description 描述：多方通话记录详情查看界面
 * 
 * @author  jrh
 * @date    2013年12月25日 上午9:33:25
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class MeettingDetailRecordManagement extends VerticalLayout implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "originatorId", "meettingRoom", "meettingUniqueId", "recordFileUrl", "memberIndex", 
			"joinMemberNum", "joinMemberId", "duration", "joinDate", "leaveDate", "channel", "channelUniqueId"};
	
	private final String[] COL_HEADERS = new String[] {"记录编号", "发起人", "会议室号码", "会议唯一标识", "试听/下载录音", "与会者序号", 
			"与会者号码", "与会坐席", "与会时长", "与会时间", "离会时间", "与会通道", "与会通道标识"};
	
	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);

	private Table detailRecordTable;
	private MeettingRecordSimpleFilter meettingRecordSimpleFilter;		// 迁移日志的简单搜索组件
	private FlipOverTableComponent<MeettingDetailRecord> mettingRecordTableFlip;	// 为Table添加的翻页组件

	private TabSheet customerInfoTabSheet;						// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;		// 客户所有相关信息查看窗口 

	private Notification notification;							// 提示信息

	private Label player; 										// 播放器显示标签
	private HorizontalLayout playerLayout; 						// 存放播放器的布局管理器
	
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
	private MeettingDetailRecordService meettingDetailRecordService;
	private UserService userService;
	private CommonService commonService;
	
	//录音基础路径
		private String baseUrl=BaseUrlUtils.getBaseUrl();
		
	
	public MeettingDetailRecordManagement() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		screenResolution = SpringContextHolder.getScreenResolution();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		meettingDetailRecordService = SpringContextHolder.getBean("meettingDetailRecordService");
		userService = SpringContextHolder.getBean("userService");
		commonService = SpringContextHolder.getBean("commonService");

		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);

		meettingRecordSimpleFilter = new MeettingRecordSimpleFilter();
		meettingRecordSimpleFilter.setHeight("-1px");
		this.addComponent(meettingRecordSimpleFilter);

		// 创建客户表格
		createMigrateLogTable();

		// 为表格添加右键单击事件
		addActionToTable(detailRecordTable);
		
		HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setSpacing(true);
		tableFooter.setWidth("100%");
		this.addComponent(tableFooter);

		// 创建播放录音组件
		createPlayerLayout(tableFooter);

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
		meettingRecordSimpleFilter.setMeettingRecordTableFlip(mettingRecordTableFlip);
		
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
		detailRecordTable.addGeneratedColumn("recordFileUrl", new ListenTypeColumnGenerator());
		detailRecordTable.setColumnAlignment("recordFileUrl", Table.ALIGN_CENTER);
		detailRecordTable.addGeneratedColumn("joinMemberNum", new TelephonesColumnGenerator());
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
                } else if ("originatorId".equals(colId)) {
                	User originator = userService.get(property.getValue());
                	if(originator != null) {
                		return originator.getMigrateCsr();
                	} 
                	return property.getValue()+"";
                } else if ("joinMemberId".equals(colId)) {
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
	 * 用于自动生成“试听录音”列
	 */
	private class ListenTypeColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final MeettingDetailRecord record = (MeettingDetailRecord) itemId;
			final String url = record.getRecordFileUrl(baseUrl);

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
						String path = "<EMBED src='"+ url + "' hidden=false autostart='true' width=360 height=63 "
								+ "type=audio/x-ms-wma volume='0' loop='0' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
						player = new Label(path, Label.CONTENT_XHTML);
						playerLayout.addComponent(player);
					}
				});

//				Link typelink = new Link("下载", new ExternalResource(url));
//				typelink.setIcon(ResourceDataCsr.down_type_ico);
//				typelink.setTargetName("_blank");
				
				//TODO The button with a caption is created by lc,as follows. 
				final Button download = new Button("下载");
				download.setIcon(ResourceDataCsr.down_type_ico);
				download.setStyleName(BaseTheme.BUTTON_LINK);
				if(record.getIsDownloaded()){
					download.addStyleName("red");
				}
				download.addListener(new Button.ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						download.addStyleName("red");
						downloadRecord(url);
						record.setIsDownloaded(true);
						meettingDetailRecordService.updateMeettingDetailRecord(record);
					}
				});

				HorizontalLayout layout = new HorizontalLayout();
				layout.setSpacing(true);
				layout.setWidth("100%");
				layout.addComponent(listen);
				layout.setComponentAlignment(listen, Alignment.MIDDLE_LEFT);

				if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SOUND_MEETTING_DETAIL_RECORD)) {
					layout.addComponent(download);
					layout.setComponentAlignment(download, Alignment.MIDDLE_RIGHT);
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
				((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
			}
		});
		
		recordOperatorLog(file,null);
	}

	//Record the operator log.
	public void recordOperatorLog(File file,String sql){
		OperationLog operationLog = new OperationLog();
		operationLog.setDomain(SpringContextHolder.getDomain());
		if (file != null) {
			operationLog.setFilePath(file.getAbsolutePath());
		}
		operationLog.setOperateDate(new Date());
		operationLog.setOperationStatus(OperationStatus.EXPORT);
		operationLog.setUsername(SpringContextHolder.getLoginUser().getUsername());
		operationLog.setRealName(SpringContextHolder.getLoginUser().getRealName());
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		String ip = context.getBrowser().getAddress();
		operationLog.setIp(ip);
		operationLog.setDescription("管理员 在多方通话记录详情界面下载了录音");
		operationLog.setProgrammerSee(sql);
		commonService.save(operationLog);
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
				MeettingDetailRecord record = (MeettingDetailRecord) table.getValue();
				Long domainId = domain.getId();
				String number = record.getJoinMemberNum();
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
	 * 创建导出多方通话记录详情的组件
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
		if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_MEETTING_DETAIL_RECORD)) {
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
		// 创建初始搜索语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("本周");
		String countSql = "select count(m) from MeettingDetailRecord as m where m.joinDate >= '"+dateStrs[0]+"' and m.joinDate <= '"+dateStrs[1]+"' and m.domainId = " +domain.getId();
		String searchSql = "select m from MeettingDetailRecord as m group by m.meettingUniqueId,m.id,m.memberIndex having m.joinDate >= '"
		+dateStrs[0]+"' and m.joinDate <= '"+dateStrs[1]+"' and m.domainId = " +domain.getId()+" order by m.meettingUniqueId desc,m.memberIndex asc,m.id asc";
		
		mettingRecordTableFlip = new FlipOverTableComponent<MeettingDetailRecord>(MeettingDetailRecord.class, 
				meettingDetailRecordService, detailRecordTable, searchSql , countSql, null);

		detailRecordTable.setVisibleColumns(VISIBLE_PROPERTIES);
		detailRecordTable.setColumnHeaders(COL_HEADERS);
		
		tableFooter.addComponent(mettingRecordTableFlip);
		tableFooter.setComponentAlignment(mettingRecordTableFlip, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 根据屏幕分辨率的 垂直像素px 来设置表格的行数
	 */
	private void setTablePageLength() {
		if(screenResolution[1] >= 1080) {
			detailRecordTable.setPageLength(32);
			mettingRecordTableFlip.setPageLength(32, false);
		} else if(screenResolution[1] >= 1050) {
			detailRecordTable.setPageLength(30);
			mettingRecordTableFlip.setPageLength(30, false);
		} else if(screenResolution[1] >= 900) {
			detailRecordTable.setPageLength(23);
			mettingRecordTableFlip.setPageLength(23, false);
		} else if(screenResolution[1] >= 768) {
			detailRecordTable.setPageLength(16);
			mettingRecordTableFlip.setPageLength(16, false);
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
			if(columnId.equals("joinMemberNum")) {
				MeettingDetailRecord record = (MeettingDetailRecord) itemId;
				String number = record.getJoinMemberNum();
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
	 * 刷新多方通话记录详情显示界面，当Tab 页切换的时候调用
	 */
	public void updateTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			mettingRecordTableFlip.refreshToFirstPage();
		} else {
			mettingRecordTableFlip.refreshInCurrentPage();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == exportExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出多方通话记录详情
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
			String searchSql = mettingRecordTableFlip.getSearchSql();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

			file = new File(new String((exportPath + "/多方通话记录详情_"
					+ new Date().getTime() + ".xls").getBytes("GBK"),	"ISO-8859-1"));
			OperationLogUtil.simpleLog(loginUser, "导出多方通话记录："+file.getName());
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

//			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"), "GBK");

			
			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(jxl.format.Colour.LIGHT_GREEN);					// 设置单元格背景颜色为浅绿色

			// 记录查出来的客服记录Id值
			MeettingDetailRecord firstLog = (MeettingDetailRecord) detailRecordTable.firstItemId();
			if (firstLog == null) {
				notification.setCaption("<font color='red'><B>选出的结果为空，不能导出多方通话记录详情！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			}


			// 每次加载500条
			int pageLen = 500;
			Long processCount = 0L;
			int index = 0;
			List<MeettingDetailRecord> migrateCustomerLogs = null;
			// 计数
			Long recordTotalCount = mettingRecordTableFlip.getTotalRecord() + 0L;
			int totalPage = (mettingRecordTableFlip.getTotalRecord() + pageLen - 1) / pageLen;
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
				migrateCustomerLogs = meettingDetailRecordService.loadPageEntities(startIndex, pageLen, searchSql);
				
				processCount += pageLen;
				pi.setValue(processCount / (float) recordTotalCount);
				
				int cursor = sheet.getRows();
				for (int row = 0; row < migrateCustomerLogs.size(); row++) {
					MeettingDetailRecord mdr = migrateCustomerLogs.get(row);
					String originatorInfo = trimObjectToEmpty(mdr.getOriginatorId());
					User originator = userService.get(mdr.getOriginatorId());
                	if(originator != null) {
                		originatorInfo = originator.getMigrateCsr();
                	} 
                	
                	String joinMemberInfo = trimObjectToEmpty(mdr.getJoinMemberId());
                	User joinMember = userService.get(mdr.getJoinMemberId());
                	if(joinMember != null) {
                		joinMemberInfo = joinMember.getMigrateCsr();
                	}
                	
					int col = 0;
					sheet.addCell(new jxl.write.Label(col, cursor + row, trimObjectToEmpty(mdr.getId())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, originatorInfo));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getMeettingRoom())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getMeettingUniqueId())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getRecordFileUrl(baseUrl))));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getMemberIndex())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getJoinMemberNum())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, joinMemberInfo));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getDuration())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, sdf.format(mdr.getJoinDate())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, sdf.format(mdr.getLeaveDate())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getChannel())));
					sheet.addCell(new jxl.write.Label(++col, cursor + row, trimObjectToEmpty(mdr.getChannelUniqueId())));
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
			logger.error(e.getMessage() + " 导出多方通话记录详情Excel出现异常!", e);
			notification.setCaption("多方通话记录详情导出失败，请重试!");
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
