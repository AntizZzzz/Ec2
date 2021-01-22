package com.jiangyifen.ec2.ui.mgr.tabsheet.mgrhistoryservicerecord;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jxl.Cell;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.Company;
import com.jiangyifen.ec2.entity.County;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Province;
import com.jiangyifen.ec2.entity.TableKeyword;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceDescriptionService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.service.eaoservice.TableKeywordService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAllInfoWindow;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrServiceRecordAllView;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 历史客服记录显示界面
 * 
 * @author jrh 2013-7-18
 */
@SuppressWarnings("serial")
public class MgrHistoryServiceRecordTabView extends VerticalLayout implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	private final Object[] VISIBLE_PROPERTIES = new Object[] { "id", "createDate", "serviceRecordStatus", "recordFileDownloadUrl", "customerResource.id", "customerResource.name", 
			"customerResource.telephones", "customerResource.customerLevel", "customerResource.company", "creator", "creator.department", "orderTime", "orderNote", "recordContent", 
			"marketingProject", "mgrQcCreator", "qcMgr", "qcReason" };

	private final String[] COL_HEADERS = new String[] { "记录编号", "联系时间", "联系结果", "试听/下载录音", "客户编号", "客户姓名", "客户电话", "客户等级", "公司名称", "创建人", "所属部门", "预约时间", "预约内容", "记录内容", "所属项目", "质检人", "质检结果", "质检原因" };

	// 右键单击 action
	private final Action BASEINFO = new Action("查看客户基础信息", ResourceDataCsr.customer_info_16_ico);
	private final Action DESCRIPTIONINFO = new Action("查看客户描述信息", ResourceDataCsr.customer_description_16_ico);
	private final Action ADDRESSINFO = new Action("查看客户地址信息", ResourceDataCsr.address_16_ico);
	private final Action HISTORYRECORD = new Action("查看客户历史记录", ResourceDataCsr.customer_history_record_16_ico);

	private Notification warningNotification; 												// 错误提示信息

	private Table mgrHistoryServiceRecordTable; 											// 存放历史客服记录查询结果的表格
	private MgrHistoryServiceRecordSimpleFilter mgrSimpleHistoryServiceRecordFilter; 		// 简单搜索条件组件
	private FlipOverTableComponent<CustomerServiceRecord> historyServiceRecordTableFlip; 	// 为Table添加的翻页组件

	private Label player; 								// 播放器显示标签
	private HorizontalLayout playerLayout; 				// 存放播放器的布局管理器

	private Embedded downloader;  						// 存放数据的组件
	private Button exportServiceRecordExcel;			// 导出客服记录Excel
	private Button exportTapeUrl;  						// 批量导出录音的url
	private ProgressIndicator pi;  						// 进度条
	private HorizontalLayout progressLayout; 			// 进度条组件存放布局管理器
	private Button switchToDetailView_bt; 				// 切换查看客服记录视角的按钮

	private TabSheet customerInfoTabSheet; 				// 存放客户信息的TabSheet
	private CustomerAllInfoWindow customerAllInfoWindow;// 客户所有相关信息查看窗口
	private MgrQcWindow mgrQcWindow; 					// 管理员质检窗口
	private MgrDetailSingleCustomerHistoryRecordView detailSingleCustomerHistoryRecordView; // 单个客户的历史服务记录详细信息显示窗口

	private MgrServiceRecordAllView mgrServiceRecordAllView; // 用于显示客服记录的组界面，也是当前组件的上级界面

	private User loginUser; 						// 当前登录用户
	private boolean isEncryptMobile = true; 		// 默认使用加密的电话号码和手机号
	private ArrayList<String> ownBusinessModels; 	// 当前用户所有用的所有管理角色的权限
	private String exportPath;						// 导出路径

	private CustomerServiceRecordService customerServiceRecordService;  // 客服记录服务类
	private CdrService cdrService; 										// 历史客服记录服务类
	private CustomerResourceDescriptionService descriptionService;		// 描述信息服务类
	private CommonService commonService;
	private TableKeywordService tableKeywordService;
	
	//录音基础路径
	private String baseUrl=BaseUrlUtils.getBaseUrl();

	public MgrHistoryServiceRecordTabView(MgrServiceRecordAllView mgrServiceRecordAllView) {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);

		this.mgrServiceRecordAllView = mgrServiceRecordAllView;

		loginUser = SpringContextHolder.getLoginUser();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;

		customerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService");
		cdrService = SpringContextHolder.getBean("cdrService");
		descriptionService = SpringContextHolder.getBean("customerResourceDescriptionService");
		commonService = SpringContextHolder.getBean("commonService");
		tableKeywordService = SpringContextHolder.getBean("tableKeywordService");

		warningNotification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warningNotification.setDelayMsec(1000);
		warningNotification.setHtmlContentAllowed(true);

		// 创建搜索组件
		mgrSimpleHistoryServiceRecordFilter = new MgrHistoryServiceRecordSimpleFilter();
		mgrSimpleHistoryServiceRecordFilter.setHeight("-1px");
		this.addComponent(mgrSimpleHistoryServiceRecordFilter);

		// 创建历史客服记录显示表格
		createServiceRecordTable();

		// 为表格添加右键单击事件
		addActionToTable(mgrHistoryServiceRecordTable);

		// 表格下方布局管理器
		HorizontalLayout tableFooterLayout = new HorizontalLayout();
		tableFooterLayout.setWidth("100%");
		tableFooterLayout.setSpacing(true);
		this.addComponent(tableFooterLayout);

		// 创建播放录音组件
		createPlayerLayout(tableFooterLayout);

		// 表格右下方布局管理器
		HorizontalLayout tableFooterRightLayout = new HorizontalLayout();
		tableFooterRightLayout.setSpacing(true);
		tableFooterLayout.addComponent(tableFooterRightLayout);
		tableFooterLayout.setComponentAlignment(tableFooterRightLayout, Alignment.TOP_RIGHT);

		// 创建导出历史客服记录的工具
		createExportComponents(tableFooterRightLayout);

		// 创建历史客服记录表格的翻页组件
		createTableFlipComponent(tableFooterRightLayout);

		// 为历史客服记录Tab 页的 上侧搜索组件 传递 “翻页组件”和“Table组件”
		mgrSimpleHistoryServiceRecordFilter.setTableFlipOver(historyServiceRecordTableFlip);

		// 创建单个客户的历史服务记录详细信息显示窗口及各种组件
		detailSingleCustomerHistoryRecordView = new MgrDetailSingleCustomerHistoryRecordView();
		
		mgrSimpleHistoryServiceRecordFilter.getSearchButton().click();
	}

	/**
	 * 创建历史客服记录显示表格
	 */
	private void createServiceRecordTable() {
		mgrHistoryServiceRecordTable = createFormatColumnTable();
		mgrHistoryServiceRecordTable.setWidth("100%");
		mgrHistoryServiceRecordTable.setHeight("-1px");
		mgrHistoryServiceRecordTable.setImmediate(true);
		mgrHistoryServiceRecordTable.setSelectable(true);
		mgrHistoryServiceRecordTable.setStyleName("striped");
		mgrHistoryServiceRecordTable.setNullSelectionAllowed(false);
		mgrHistoryServiceRecordTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(mgrHistoryServiceRecordTable);

		mgrHistoryServiceRecordTable.addGeneratedColumn("recordFileDownloadUrl", new ListenTypeColumnGenerator());
		mgrHistoryServiceRecordTable.addGeneratedColumn("customerResource.telephones", new TelephonesColumnGenerator());
		mgrHistoryServiceRecordTable.addGeneratedColumn("recordContent", new ContentColumnGenerator());
		mgrHistoryServiceRecordTable.addGeneratedColumn("orderNote", new OrderNoteColumnGenerator());
		mgrHistoryServiceRecordTable.addGeneratedColumn("qcReason", new QcReasonColumnGenerator());
	}

	/**
	 * 创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if (property.getValue() == null) {
					return "";
				} else if (property.getType() == Date.class) {
					return dateFormat.format((Date) property.getValue());
				} else if ("marketingProject".equals(colId)) {
					MarketingProject mp = (MarketingProject) property.getValue();
					return mp.getProjectName();
				} else if ("serviceRecordStatus".equals(colId)) {
					CustomerServiceRecordStatus status = (CustomerServiceRecordStatus) property.getValue();
					String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
					return status.getStatusName() + " - " + direction;
				} else if ("customerResource.company".equals(colId)) {
					Company company = (Company) property.getValue();
					return company.getName();
				} else if ("creator".equals(colId)) {
					User csr = (User) property.getValue();
					String creatorInfo = csr.getEmpNo();
					if (csr.getRealName() != null) {
						creatorInfo = creatorInfo + " - " + csr.getRealName();
					}
					return creatorInfo;
				} else if ("creator.department".equals(colId)) {
					Department dept = (Department) property.getValue();
					return dept.getName();
				} else if ("mgrQcCreator".equals(colId)) {
					User qcCreator = (User) property.getValue();
					String qcCreatorInfo = qcCreator.getEmpNo();
					if (qcCreator.getRealName() != null) {
						qcCreatorInfo = qcCreatorInfo + " - " + qcCreator.getRealName();
					}
					return qcCreatorInfo;
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
			try {
				CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
				// lc 注释开始 2013-09-25
				// // 获取指定客服记录的录音下载路径
				// String tmpUrl = serviceRecord.getRecordFileDownloadUrl();
				// Cdr cdr = null;
				//
				// // TODO 为了解决老版本的录音文件查询方式，老版本的 Ec2 中 recordFileName 一定为null
				// if(tmpUrl == null) {
				// // 根据ServiceRecord查询，没查到，查到一个，查到多个
				// List<Cdr> cdrs =
				// cdrService.getRecordByServiceRecord(serviceRecord);
				// if(cdrs.size() > 0) {
				// cdr = cdrs.get(0);
				// tmpUrl = cdr.getUrl();
				// }
				// }
				//
				// lc 注释结束 2013-09-25
				List<Cdr> cdrs = cdrService.getRecordByServiceRecord(serviceRecord);
				String tmpUrl = null;
				Cdr cdr = null;
				if (isEncryptMobile) {
					for (Cdr c : cdrs) {
						cdr = c;
						if (cdr.getRealUrl(baseUrl) != null) {
							tmpUrl = cdr.getRealUrl(baseUrl);
							break;
						}
					}
				} else {
					for (Cdr c : cdrs) {
						cdr = c;
						if (cdr.getUrl(baseUrl) != null) {
							tmpUrl = cdr.getUrl(baseUrl);
							break;
						}
					}
				}

				// 最终下载路径
				final String downloadPath = tmpUrl;
				final Cdr updateCdr = cdr;
				if (downloadPath != null) {
					Button listen = new Button("试听");
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
							String path = "<EMBED src='" + downloadPath + "' hidden='false' autostart='true' width=360 height=63 " + "type=audio/x-ms-wma volume='0' loop='0' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";

							player = new Label(path, Label.CONTENT_XHTML);
							playerLayout.addComponent(player);
						}
					});

					// Link typelink = new Link("下载", new
					// ExternalResource(tmpUrl));
					// typelink.setIcon(ResourceDataCsr.down_type_ico);
					// typelink.setTargetName("_blank");

					// TODO The button with a caption is created by lc,as
					// follows.
					final Button download = new Button("下载");
					download.setIcon(ResourceDataCsr.down_type_ico);
					download.setStyleName(BaseTheme.BUTTON_LINK);

					if (cdr.getIsDownloaded()) {
						download.addStyleName("red");
					}

					download.addListener(new Button.ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							download.addStyleName("red");
							downloadRecord(downloadPath);
							updateCdr(updateCdr);
						}
					});

					HorizontalLayout layout = new HorizontalLayout();
					layout.setSpacing(true);
					layout.setWidth("100%");
					layout.addComponent(listen);
					layout.setComponentAlignment(listen, Alignment.MIDDLE_LEFT);

					if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SOUND_SERVICE_RECORD)) {
						layout.addComponent(download);
						layout.setComponentAlignment(download, Alignment.MIDDLE_RIGHT);
						source.setColumnHeader(columnId, "试听/下载录音");
					} else {
						source.setColumnHeader(columnId, "试听录音");
					}

					return layout;
				} else {
					return new Label("无录音文件");
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("jrh 管理员历史客服记录界面自动生成试听或下载录音文件列，发生异常--》", e);
				return new Label("无录音文件");
			}
		}
	}

	// TODO The function is added by lc,as follows.
	public void downloadRecord(String url) {

		url = "/var/www/html/" + url.substring(url.indexOf("monitor"), url.length());

		File file = new File(url);

		logger.info("file.exists(): " + file.exists());
		logger.info("url: " + url);

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

		recordOperatorLog(file, null);

	}

	// Update a cdr entity.
	public void updateCdr(Cdr cdr) {

		cdr.setIsDownloaded(true);
		cdrService.update(cdr);
	}

	// Record the operator log.
	public void recordOperatorLog(File file, String sql) {

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
		operationLog.setDescription("mgr 历史客服记录");
		operationLog.setProgrammerSee(sql);
		CommonService commonService = SpringContextHolder.getBean("commonService");
		commonService.save(operationLog);
	}

	/**
	 * jrh 用于自动生成可以拨打电话的列 如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if (columnId.equals("customerResource.telephones")) {
				CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
				Set<Telephone> telephones = serviceRecord.getCustomerResource().getTelephones();
				return new TelephoneNumberInTableLabelStyle(telephones, isEncryptMobile);
			}
			return null;
		}
	}

	/**
	 * 用于自动生成记录能容列 如果记录内容的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class ContentColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			if (columnId.equals("recordContent")) {
				String content = serviceRecord.getRecordContent();
				if (content == null) {
					return null;
				} else if (!"".equals(content.trim())) {
					Label contentLabel = new Label();
					String trimedContent = content.trim();
					if (trimedContent.length() > 8) {
						contentLabel.setValue(trimedContent.substring(0, 8) + "...");
						contentLabel.setDescription(trimedContent);
					} else {
						contentLabel.setValue(trimedContent);
					}
					return contentLabel;
				}
			}
			return null;
		}
	}

	/**
	 * 用于自动生成预约信息显示组件 如果预约信息的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class OrderNoteColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			if (columnId.equals("orderNote")) {
				String orderNote = serviceRecord.getOrderNote();
				if (orderNote == null) {
					return null;
				} else if (!"".equals(orderNote.trim())) {
					Label orderNoteLabel = new Label();
					String trimedOrderNote = orderNote.trim();
					if (trimedOrderNote.length() > 8) {
						orderNoteLabel.setValue(trimedOrderNote.substring(0, 8) + "...");
						orderNoteLabel.setDescription(trimedOrderNote);
					} else {
						orderNoteLabel.setValue(trimedOrderNote);
					}
					return orderNoteLabel;
				}
			}
			return null;
		}
	}

	/**
	 * 用于自动生成质检原因显示组件 如果质检原因的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class QcReasonColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			if (columnId.equals("qcReason")) {
				String qcReason = serviceRecord.getQcReason();
				if (qcReason == null) {
					return null;
				} else if (!"".equals(qcReason.trim())) {
					Label qcReasonLabel = new Label();
					String trimedOrderNote = qcReason.trim();
					if (trimedOrderNote.length() > 8) {
						qcReasonLabel.setValue(trimedOrderNote.substring(0, 8) + "...");
						qcReasonLabel.setDescription(trimedOrderNote);
					} else {
						qcReasonLabel.setValue(trimedOrderNote);
					}
					return qcReasonLabel;
				}
			}
			return null;
		}
	}

	/**
	 * 用于自动生成各种操作按钮显示组件
	 */
	private class OperatorColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(final Table source, final Object itemId, Object columnId) {
			final CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			final CustomerResource customerResource = serviceRecord.getCustomerResource();

			Button qc_bt = new Button("质检");
			qc_bt.setStyleName(BaseTheme.BUTTON_LINK);
			qc_bt.setImmediate(true);
			qc_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					showMgrQcWindow(serviceRecord);
				}
			});

			PopupView detail = new PopupView(new Content() {
				@Override
				public Component getPopupComponent() {
					return detailSingleCustomerHistoryRecordView;
				}

				@Override
				public String getMinimizedValueAsHTML() {
					return "联系详情";
				}
			});

			detail.setHideOnMouseOut(false);
			detail.addListener(new ComponentAttachListener() {
				@Override
				public void componentAttachedToContainer(ComponentAttachEvent event) {
					detailSingleCustomerHistoryRecordView.echoHistoryRecord(customerResource);
					source.select(itemId);
				}
			});

			HorizontalLayout opeartorLayout = new HorizontalLayout();
			opeartorLayout.setSpacing(true);
			opeartorLayout.addComponent(qc_bt);
			opeartorLayout.addComponent(detail);
			return opeartorLayout;
		}
	}

	/**
	 * 显示质检窗口
	 * 
	 * @param obj
	 */
	private void showMgrQcWindow(CustomerServiceRecord serviceRecord) {
		// 刷新Window的内容
		if (mgrQcWindow == null) {
			mgrQcWindow = new MgrQcWindow(this);
		}

		mgrQcWindow.updateInfo(serviceRecord);
		this.getApplication().getMainWindow().removeWindow(mgrQcWindow);
		this.getApplication().getMainWindow().addWindow(mgrQcWindow);
	}

	/**
	 * 为指定任务表格 添加右键单击事件
	 */
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if (customerAllInfoWindow == null) { // 只有单击右键时才创建客户信息查看窗口及相关组件
					createCustomerAllInfoWindow();
				}

				table.select(target);
				table.getApplication().getMainWindow().removeWindow(customerAllInfoWindow);
				table.getApplication().getMainWindow().addWindow(customerAllInfoWindow);
				CustomerResource customerResource = ((CustomerServiceRecord) table.getValue()).getCustomerResource();
				customerAllInfoWindow.initCustomerResource(customerResource);

				if (action == BASEINFO) {
					customerAllInfoWindow.echoCustomerBaseInfo(customerResource);
					customerInfoTabSheet.setSelectedTab(0);
				} else if (action == DESCRIPTIONINFO) {
					customerAllInfoWindow.echoCustomerDescription(customerResource);
					customerInfoTabSheet.setSelectedTab(1);
				} else if (action == ADDRESSINFO) {
					customerAllInfoWindow.echoCustomerAddress(customerResource);
					customerInfoTabSheet.setSelectedTab(2);
				} else if (action == HISTORYRECORD) {
					customerAllInfoWindow.echoHistoryRecord(customerResource);
					customerInfoTabSheet.setSelectedTab(3);
				}
			}

			@Override
			public Action[] getActions(Object target, Object sender) {
				if (target != null) {
					return new Action[] { BASEINFO, DESCRIPTIONINFO, ADDRESSINFO, HISTORYRECORD };
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

		String musicPath = "<EMBED src='' hidden=false autostart='false' width=360 height=63 " + "type=audio/x-ms-wma volume='0' loop='-1' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
		player = new Label(musicPath, Label.CONTENT_XHTML);
		playerLayout.addComponent(player);
	}

	/**
	 * 创建导出历史客服记录的工具
	 * 
	 * @param tableFooterRightLayout
	 */
	private void createExportComponents(HorizontalLayout tableFooterRightLayout) {
		HorizontalLayout exportHLayout = new HorizontalLayout();
		exportHLayout.setSpacing(true);
		tableFooterRightLayout.addComponent(exportHLayout);
		tableFooterRightLayout.setComponentAlignment(exportHLayout, Alignment.MIDDLE_LEFT);

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

		downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);

		// 如果拥有下载权限才创建相关下载组件
		if (ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SERVICE_RECORD)) {
			exportServiceRecordExcel = new Button("导出客服记录", this);
			exportServiceRecordExcel.setImmediate(true);
			tableFooterRightLayout.addComponent(exportServiceRecordExcel);

			exportTapeUrl = new Button("批量导出录音URL", this);
			exportTapeUrl.setImmediate(true);
			exportTapeUrl.setDescription("<B>导出录音下载地址后，再使用迅雷等下载工具即可直接进行批量下载！</B>");
			tableFooterRightLayout.addComponent(exportTapeUrl);
		}

		switchToDetailView_bt = new Button(MgrServiceRecordAllView.DETAIL_SR_CAPTION, this);
		switchToDetailView_bt.addStyleName("default");
		switchToDetailView_bt.setImmediate(true);
		tableFooterRightLayout.addComponent(switchToDetailView_bt); // jrh 添加切换查看客服记录视角的按钮
	}

	// 创建翻页组件
	private void createTableFlipComponent(HorizontalLayout tableFooterRightLayout) {
		historyServiceRecordTableFlip = new FlipOverTableComponent<CustomerServiceRecord>(CustomerServiceRecord.class, 
				customerServiceRecordService, mgrHistoryServiceRecordTable, "", "", null);

		for (int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			historyServiceRecordTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		
		mgrHistoryServiceRecordTable.setVisibleColumns(VISIBLE_PROPERTIES);
		mgrHistoryServiceRecordTable.setColumnHeaders(COL_HEADERS);
		
		mgrHistoryServiceRecordTable.addGeneratedColumn("operators", new OperatorColumnGenerator());
		mgrHistoryServiceRecordTable.setColumnAlignment("operators", Table.ALIGN_CENTER);
		mgrHistoryServiceRecordTable.setColumnHeader("operators", "操作");

		mgrHistoryServiceRecordTable.setPageLength(18);
		historyServiceRecordTableFlip.setPageLength(18, false);

		tableFooterRightLayout.addComponent(historyServiceRecordTableFlip);
		tableFooterRightLayout.setComponentAlignment(historyServiceRecordTableFlip, Alignment.TOP_RIGHT);
	}

	/**
	 * 创建客户信息查看窗口及相关组件
	 */
	private void createCustomerAllInfoWindow() {
		customerAllInfoWindow = new CustomerAllInfoWindow(RoleType.manager);
		customerAllInfoWindow.setResizable(false);
		customerAllInfoWindow.setEchoModifyByReflect(this);

		customerInfoTabSheet = customerAllInfoWindow.getCustomerInfoTabSheet();
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		final String downloadCountSql = historyServiceRecordTableFlip.getCountSql();
		if (source == exportServiceRecordExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出呼叫记录
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 执行导出操作
					exportServiceRecordExcel.setEnabled(false);
					exportExcelThreadRun(downloadCountSql, domain, loginUser);
					exportServiceRecordExcel.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		} else if (source == exportTapeUrl) {
			// 批量导出录音下载地址
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 批量执行导出url
					exportTapeUrl.setEnabled(false);
					exportUrlThreadRun(downloadCountSql);
					exportTapeUrl.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		} else if (source == switchToDetailView_bt) {
			mgrServiceRecordAllView.switchView(MgrServiceRecordAllView.DETAIL_VIEW_TYPE);
		}
	}

	/**
	 * 制作表格
	 */
	private boolean exportExcelThreadRun(String downloadCountSql, Domain domain, User user) {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

			// ======================chb 操作日志======================//
			file = new File(new String((exportPath + "/客服记录[客户视角]_" + new Date().getTime() + ".xls").getBytes("GBK"), "ISO-8859-1"));
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

			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"), "GBK");

			// 记录查出来的客服记录Id值
			CustomerServiceRecord firstRecord = (CustomerServiceRecord) mgrHistoryServiceRecordTable.firstItemId();
			if (firstRecord == null) {
				warningNotification.setCaption("选出的结果为空，不能导出历史客服记录！");
				mgrHistoryServiceRecordTable.getApplication().getMainWindow().showNotification(warningNotification);
				return false;
			}

			// 记录导出日志
			WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
			String ip = context.getBrowser().getAddress();
			OperationLog operationLog = new OperationLog();
			operationLog.setDomain(domain);
			operationLog.setFilePath(filePath);
			operationLog.setOperateDate(new Date());
			operationLog.setOperationStatus(OperationStatus.EXPORT);
			operationLog.setUsername(user.getUsername());
			operationLog.setRealName(user.getRealName());
			operationLog.setIp(ip);
			operationLog.setDescription("导出历史客服记录");
			operationLog.setProgrammerSee(downloadCountSql.replaceFirst("count\\(c\\)", "c"));
			commonService.save(operationLog);
			// ======================chb 操作日志======================//

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			ArrayList<String> colNameStrLs = new ArrayList<String>();
			
			String[] baseNames = new String[] { "记录编号", "联系时间", "联系结果", "录音文件下载路径", "客户编号", "客户姓名", "客户电话", 
					"客户等级", "公司名称", "创建人", "所属部门", "预约时间", "预约内容", "记录内容", "所属项目", "质检人", "质检结果", "质检原因", 
					"客户性别", "客户生日", "客户地址", "客户描述信息"};
			for(String baseName : baseNames) {	// 添加基础字段信息
				colNameStrLs.add(baseName);
			}
			
			List<TableKeyword> tableKeywordLs = tableKeywordService.getAllByDomain(domain);
			ArrayList<String> tableKeywordStrLs = new ArrayList<String>();
			
			for(TableKeyword tk : tableKeywordLs) {	// 添加描述字段信息
				if(!colNameStrLs.contains(tk.getColumnName()) ) {
					colNameStrLs.add(tk.getColumnName());
					tableKeywordStrLs.add(tk.getColumnName());
				}
			}

			HashMap<String, Integer> colIndexMap = new HashMap<String, Integer>();		// 记录各列名对应列的index
			int namesLen = colNameStrLs.size();
			String[] namesall = new String[namesLen];
			for(int i = 0; i < namesLen; i++) {
				namesall[i] = colNameStrLs.get(i);
				colIndexMap.put(namesall[i], i);
			}
			
			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(Colour.LIGHT_GREEN); // 设置单元格背景颜色为浅绿色

			// 计数
			Long recordCount = customerServiceRecordService.getEntityCount(downloadCountSql) + 0L;

			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(c\\)", "max\\(c.id\\)");
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(c\\)", "c");
			Long recordId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);

			// 每次加载500条
			List<CustomerServiceRecord> records = null;
			Long processCount = 0L;
			int pageStep = 500;
			int pageIndex = 0;

			do {
				// 如果当前已经查到超过50000条记录，则新建一个sheet
				if ((processCount % 50000) == 0) {
					++pageIndex;

					sheet = writableWorkbook.createSheet("Sheet" + pageIndex, pageIndex);
					// 设置excel列名
					for (int i = 0; i < namesall.length; i++) {
						sheet.addCell(new jxl.write.Label(i, 0, namesall[i], cellFormat));
					}
				}

				String useSql = downLoadSearchTmpSql + " and c.id <= " + recordId + " order by c.id desc";
				records = customerServiceRecordService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (records.size() > 0) {
					recordId = records.get(records.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				for (int i = 0; i < records.size(); i++) {
					CustomerServiceRecord record = records.get(i);

					String id = "";
					if (record.getId() != null) {
						id = record.getId().toString();
					}

					String createDate = "";
					if (record.getCreateDate() != null) {
						createDate = sdf.format(record.getCreateDate());
					}

					String recordStatus = "";
					if (record.getServiceRecordStatus() != null) {
						CustomerServiceRecordStatus status = record.getServiceRecordStatus();
						String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
						recordStatus = status.getStatusName() + " - " + direction;
					}

					// 录音下载路径
					// String recordFileName = "";

					// 录音下载路径 优先从cdr中找录音文件，如果没找到，再到客服记录上直接找
					String tmpUrl = null;

					// 根据ServiceRecord查询，没查到，查到一个，查到多个
					List<Cdr> cdrs = cdrService.getRecordByServiceRecord(record);
					for (Cdr cdr : cdrs) {
						tmpUrl = cdr.getUrl(baseUrl);
						// if(tmpUrl != null) {
						// recordFileName = cdr.getRecordFileName();
						// break;
						// }
					}

					if (tmpUrl == null) {
						tmpUrl = record.getRecordFileDownloadUrl(baseUrl);
						// if(tmpUrl != null) {
						// recordFileName = record.getRecordFileName();
						// }
					}

					String url = "无录音文件";
					if (tmpUrl != null) {
						url = tmpUrl;
					}

					// 客服记录对应的客户资源
					CustomerResource customer = record.getCustomerResource();

					String customerId = "";
					if (customer != null) {
						customerId = customer.getId().toString();
					}

					String customerName = "";
					if (customer != null && customer.getName() != null) {
						customerName = customer.getName();
					}

					String phonesStr = "";
					if (customer != null && customer.getTelephones() != null) {
						// 调用Size方法，使之加载电话实体
						customer.getTelephones().size();
						phonesStr = StringUtils.join(customer.getTelephones(), ",");
					}

					// 客户等级
					String levelStr = "";
					if (customer != null && customer.getCustomerLevel() != null) {
						levelStr = customer.getCustomerLevel().getLevelName();
					}
					
					String companyName = "";
					if (customer != null && customer.getCompany() != null) {
						companyName = customer.getCompany().getName();
					}

					String creatorInfo = "";
					if (record.getCreator() != null && record.getCreator().getEmpNo() != null) {
						creatorInfo = record.getCreator().getEmpNo();
						String realName = record.getCreator().getRealName();
						if (realName != null) {
							creatorInfo = creatorInfo + " - " + realName;
						}
					}

					String deptInfo = "";
					if (record.getCreator() != null) {
						Department dept = record.getCreator().getDepartment();
						deptInfo = dept.getName();
					}

					String orderTime = "";
					if (record.getOrderTime() != null) {
						orderTime = sdf.format(record.getOrderTime());
					}

					String orderNote = "";
					if (record.getOrderNote() != null) {
						orderNote = record.getOrderNote();
					}

					String recordContent = "";
					if (record.getRecordContent() != null) {
						recordContent = record.getRecordContent();
					}

					String projectName = "";
					if (record.getMarketingProject() != null && record.getMarketingProject().getProjectName() != null) {
						projectName = record.getMarketingProject().getProjectName();
					}

					String mgrQcCreatorInfo = "";
					if (record.getMgrQcCreator() != null && record.getMgrQcCreator().getEmpNo() != null) {
						mgrQcCreatorInfo = record.getMgrQcCreator().getEmpNo();
						String realName = record.getMgrQcCreator().getRealName();
						if (realName != null) {
							mgrQcCreatorInfo = mgrQcCreatorInfo + " - " + realName;
						}
					}

					String mgrQcResult = "";
					if (record.getQcMgr() != null) {
						mgrQcResult = record.getQcMgr().getName();
					}

					String qcReason = "";
					if (record.getQcReason() != null) {
						qcReason = record.getQcReason();
					}

					// 客户性别
					String sexStr = "";
					if (customer != null && customer.getSex() != null) {
						sexStr = customer.getSex();
					}

					// 客户生日
					String birthdayStr = "";
					if (customer != null && customer.getBirthdayStr() != null) {
						birthdayStr = customer.getBirthdayStr();
					}

					// 客户所有地址
					String addressAllStr = "";
					if (customer != null) {
						for (Address address : customer.getAddresses()) {
							Province province = address.getProvince();
							City city = address.getCity();
							County county = address.getCounty();
							addressAllStr += "[";
							if (province != null) {
								addressAllStr += province.getName();
							}
							if (city != null) {
								addressAllStr += " " + city.getName();
							}
							if (county != null) {
								addressAllStr += " " + county.getName();
							}
							addressAllStr += " " + address.getStreet() + "] ";
						}
					}

					int index = 0;
					sheet.addCell(new jxl.write.Label(index, cursor + i, id));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, createDate));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, recordStatus));
					// sheet.addCell(new jxl.write.Label(++index, cursor + i,
					// recordFileName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, url));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, customerId));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, customerName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, phonesStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, levelStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, companyName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, creatorInfo));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, deptInfo));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, orderTime));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, orderNote));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, recordContent));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, projectName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, mgrQcCreatorInfo));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, mgrQcResult));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, qcReason));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, sexStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, birthdayStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, addressAllStr));

					// 客户所有描述信息
					String descriptionsStr = "";
					if (customer != null) {
						List<CustomerResourceDescription> customerDescriptions = descriptionService.getAllByCustomerId(customer.getId());
						for(CustomerResourceDescription crd : customerDescriptions) {
							String keyname = crd.getKey();
							if(tableKeywordStrLs.contains(keyname)) {
								Cell cell = sheet.getCell(colIndexMap.get(keyname), cursor + i);
								String cellValue = "";
								if(cell != null && !"".equals(cell.getContents())) {
									cellValue = cell.getContents()+", "+crd.getValue();
								} else {
									cellValue = crd.getValue();
								}
								sheet.addCell(new jxl.write.Label(colIndexMap.get(keyname), cursor + i, cellValue));
							} else {
								descriptionsStr += "["+keyname +":"+crd.getValue()+"] ";
							}
						}
					}
					
					sheet.addCell(new jxl.write.Label(++index, cursor + i, descriptionsStr));
				}
			} while (records.size() > 0);

			// JXL 只能写一次,不然出问题
			writableWorkbook.write();
			writableWorkbook.close();

			// 文件创建好后，下载文件
			downloadFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			// 导出完成之后是导出按钮可用
			exportServiceRecordExcel.setEnabled(true);
			progressLayout.setVisible(false);
			logger.error("jrh 管理员导出历史客服记录出现异常!" + e.getMessage(), e);
			warningNotification.setCaption("历史客服记录导出失败，请重试!");
			this.getApplication().getMainWindow().showNotification(warningNotification);
			return false;
		}

		return true;
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
			urlfile = new File(new String(("客服记录对应的录音下载地址.txt").getBytes("GBK"), "ISO-8859-1"));
			fos = new FileOutputStream(urlfile);
			bw = new BufferedOutputStream(fos);

			// 记录查出来的客服记录Id值
			CustomerServiceRecord firstRecord = (CustomerServiceRecord) mgrHistoryServiceRecordTable.firstItemId();
			if (firstRecord == null) {
				warningNotification.setCaption("选出的结果为空，不能导出录音下载地址！");
				mgrHistoryServiceRecordTable.getApplication().getMainWindow().showNotification(warningNotification);
				return false;
			}

			// 计数
			Long recordCount = customerServiceRecordService.getEntityCount(downloadCountSql) + 0L;

			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(c\\)", "max\\(c.id\\)");
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(c\\)", "c");
			Long recordId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);

			// 每次加载500条
			List<CustomerServiceRecord> records = null;
			Long processCount = 0L;
			int pageStep = 500;

			do {
				StringBuffer urlContent = new StringBuffer();
				// 只下载有录音文件的
				String useSql = downLoadSearchTmpSql + " and c.id <= " + recordId + " order by c.id desc";
				records = customerServiceRecordService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (records.size() > 0) {
					recordId = records.get(records.size() - 1).getId() - 1;
				}

				for (CustomerServiceRecord record : records) {
					String tmpUrl = null;
					// 根据ServiceRecord查询，没查到，查到一个，查到多个
					List<Cdr> cdrs = cdrService.getRecordByServiceRecord(record);
					for (Cdr cdr : cdrs) {
						tmpUrl = cdr.getUrl(baseUrl);
						if (tmpUrl != null) {
							urlContent.append(tmpUrl + "\r\n");
							break;
						}
					}
					if (tmpUrl == null) {
						tmpUrl = record.getRecordFileDownloadUrl(baseUrl);
						if (tmpUrl != null) {
							urlContent.append(tmpUrl + "\r\n");
						}
					}
				}
				String sipcontentStr = urlContent.toString();
				bw.write(sipcontentStr.getBytes());
			} while (records.size() > 0);
		} catch (Exception e) {
			// 导出完成之后是导出按钮可用
			exportTapeUrl.setEnabled(true);
			progressLayout.setVisible(false);
			logger.error(e.getMessage() + "jrh 管理员导出历史客服记录对应的录音下载地址时出现异常!", e);
			warningNotification.setCaption("录音下载地址批量导出失败，请重试!");
			exportTapeUrl.getApplication().getMainWindow().showNotification(warningNotification);
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
				logger.error(e.getMessage() + "jrh 管理员导出历史客服记录对应的录音下载地址，IO 流关闭时出现异常！", e);
				warningNotification.setCaption("录音下载地址批量导出失败，请重试!");
				exportTapeUrl.getApplication().getMainWindow().showNotification(warningNotification);
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
			String filename = new String(sourceFile.getName().getBytes("ISO-8859-1"), "GBK");
			FileOutputStream fos = new FileOutputStream(sourceFile.getName() + ".zip");
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
			throw new RuntimeException("压缩录音URL 地址文件是出现异常 --> " + e.getMessage());
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

	/**
	 * 供 CustomerBaseInfoEditorForm 调用 当其他组件中改变了客户信息后，则刷新Table的内容信息
	 */
	public void echoTableInfoByReflect() {
		historyServiceRecordTableFlip.refreshInCurrentPage();
	}

	/**
	 * 刷新历史客服记录显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if (refreshToFirstPage == true) {
			historyServiceRecordTableFlip.refreshToFirstPage();
		} else {
			historyServiceRecordTableFlip.refreshInCurrentPage();
		}
	}

}
