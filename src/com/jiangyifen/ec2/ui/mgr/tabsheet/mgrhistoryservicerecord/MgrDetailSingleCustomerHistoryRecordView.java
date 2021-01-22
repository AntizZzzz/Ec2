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
import com.jiangyifen.ec2.entity.Role;
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
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.TableKeywordService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
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
import com.vaadin.ui.PopupView;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 单个客户的历史服务记录详细信息显示界面
 * @author jrh
 *  2013-7-17
 */
@SuppressWarnings("serial")
public class MgrDetailSingleCustomerHistoryRecordView extends VerticalLayout implements ValueChangeListener, ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "createDate", "serviceRecordStatus", "recordFileDownloadUrl", 
			"customerResource.id", "customerResource.name", "customerResource.telephones", "customerResource.company", "creator", 
			"creator.department", "orderTime", "orderNote", "recordContent", "marketingProject", "mgrQcCreator", "qcMgr", "qcReason"};
	
	private final String[] COL_HEADERS = new String[] {"记录编号", "联系时间", "联系结果", "试听/下载录音", "客户编号", "客户姓名", 
			"客户电话", "公司名称", "创建人", "所属部门", "预约时间", "预约内容", "记录内容", "所属项目", "质检人", "质检结果", "质检原因"};
	
	private Notification warningNotification;	// 错误提示信息
	
	private TextField searchField;				// 按项目或联系结果搜索
	private PopupView complexSearchView;		// 复杂搜索界面
	private HorizontalLayout searchHLayout;		// 存放搜索组件的布局管理器

	private Table historyOutgoingRecordTable;	// 历史记录显示表格
	private MgrDetailSingleCustomerHistoryRecordFilter recordFilter;			// 存放高级收索条件的组件
	private FlipOverTableComponent<CustomerServiceRecord> historyTableFlip;		// 历史记录Table的翻页组件

	private Label player; 						// 播放器显示标签
	private HorizontalLayout playerLayout;		// 存放播放器的布局管理器
	
	private MgrQcWindow mgrQcWindow;			// 管理员质检窗口

	private Embedded downloader; 				// 存放数据的组件
	private Button exportServiceRecordExcel;	// 导出客服记录Excel
	private Button exportTapeUrl;				// 批量导出录音的url
	private ProgressIndicator pi; 				// 进度条
	private HorizontalLayout progressLayout; 	// 进度条组件存放布局管理器
	
	private User loginUser;						// 当前登录用户
	private String searchSql = "";				// 查询语句
	private String countSql = "";				// 统计语句
	private String governedDeptIdSql = "";		// 按记录创建人所属部门的id查询的语句
	private CustomerResource customerResource;	// 当前选中任务对应的客户资源
	private List<Long> allGovernedDeptIds;		// 当前用户所有管辖部门的id号
	private ArrayList<String> ownBusinessModels;// 当前登陆用户目前使用的角色类型所对应的所有权限
	private boolean isEncryptMobile = true;		// 判断电话是否需要加密
	private String exportPath;					// 导出路径

	private CustomerServiceRecordService customerServiceRecordService;	// 客服记录服务类
	private DepartmentService departmentService;						// 部门服务类
	private CdrService cdrService;										// 呼叫记录服务类
	private CustomerResourceDescriptionService descriptionService;		// 描述信息服务类
	private CommonService commonService;
	private TableKeywordService tableKeywordService;
	

	//录音基础路径
		private String baseUrl=BaseUrlUtils.getBaseUrl();
		
	public MgrDetailSingleCustomerHistoryRecordView() {
		this.setSpacing(true);
		this.setWidth("90%");
		
		loginUser = SpringContextHolder.getLoginUser();
		ownBusinessModels = SpringContextHolder.getBusinessModel();
		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;
		
		customerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService");
		departmentService = SpringContextHolder.getBean("departmentService");
		cdrService = SpringContextHolder.getBean("cdrService");
		descriptionService = SpringContextHolder.getBean("customerResourceDescriptionService");
		commonService = SpringContextHolder.getBean("commonService");
		tableKeywordService = SpringContextHolder.getBean("tableKeywordService");
		
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		allGovernedDeptIds = new ArrayList<Long>();
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

		// 根据当前用户所属部门，及其下属部门，创建动态查询语句, allGovernedDeptIds 至少会含有一个 id 为 0 的元素
		String deptIdsStr = "";
		for (int i = 0; i < allGovernedDeptIds.size(); i++) {
			if (i == (allGovernedDeptIds.size() - 1)) {
				deptIdsStr += allGovernedDeptIds.get(i);
			} else {
				deptIdsStr += allGovernedDeptIds.get(i) + ",";
			}
		}
		governedDeptIdSql = " c.creator.department.id in (" + deptIdsStr+ ")";

		warningNotification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warningNotification.setDelayMsec(1000);
		warningNotification.setHtmlContentAllowed(true);
		
		// 创建历史记录表格
		createHistoryOutgoingRecordTable();

		// 表格下方布局管理器
		HorizontalLayout tableFooterLayout = new HorizontalLayout();
		tableFooterLayout.setWidth("100%");
		tableFooterLayout.setSpacing(true);

		// 创建播放录音组件
		createPlayerLayout(tableFooterLayout);

		// 表格右下方布局管理器
		HorizontalLayout tableFooterRightLayout = new HorizontalLayout();
		tableFooterRightLayout.setSpacing(true);
		tableFooterLayout.addComponent(tableFooterRightLayout);
		tableFooterLayout.setComponentAlignment(tableFooterRightLayout, Alignment.TOP_RIGHT);

		// 创建导出历史客服记录的工具
		createExportComponents(tableFooterRightLayout);
		
		// 创建并添加翻页组件
		createTableFlipOver(tableFooterRightLayout);
		
		// 创建搜索组件
		createSearchComponent();
		
		// 将个组将加入总界面
		this.addComponent(searchHLayout);
		this.addComponent(historyOutgoingRecordTable);
		this.addComponent(tableFooterLayout);
	}

	/**
	 * 创建历史记录表格
	 */
	private void createHistoryOutgoingRecordTable() {
		historyOutgoingRecordTable = createFormatColumnTable();
		historyOutgoingRecordTable.setWidth("100%");
		historyOutgoingRecordTable.addListener(this);
		historyOutgoingRecordTable.setImmediate(true);
		historyOutgoingRecordTable.setSelectable(true);
		historyOutgoingRecordTable.setStyleName("striped");
		historyOutgoingRecordTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);

		historyOutgoingRecordTable.addGeneratedColumn("recordFileDownloadUrl", new ListenTypeColumnGenerator());
		historyOutgoingRecordTable.addGeneratedColumn("customerResource.telephones", new TelephonesColumnGenerator());
		historyOutgoingRecordTable.addGeneratedColumn("recordContent", new ContentColumnGenerator());
		historyOutgoingRecordTable.addGeneratedColumn("orderNote", new OrderNoteColumnGenerator());		
		historyOutgoingRecordTable.addGeneratedColumn("qcReason", new QcReasonColumnGenerator());	
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
	 * 创建导出指定客户的历史客服记录的工具
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
		if(ownBusinessModels.contains(MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SERVICE_RECORD)) {
			exportServiceRecordExcel = new Button("导出历史客服记录", this);
			exportServiceRecordExcel.setStyleName("default");
			exportServiceRecordExcel.setImmediate(true);
			tableFooterRightLayout.addComponent(exportServiceRecordExcel);

			exportTapeUrl = new Button("批量导出录音URL", this);
			exportTapeUrl.setImmediate(true);
			exportTapeUrl.setDescription("<B>导出录音下载地址后，再使用迅雷等下载工具即可直接进行批量下载！</B>");
			tableFooterRightLayout.addComponent(exportTapeUrl);
		}
	}

	/**
	 * 创建并添加翻页组件
	 */
	private void createTableFlipOver(HorizontalLayout tableFooterRightLayout) {
		historyTableFlip = new FlipOverTableComponent<CustomerServiceRecord>(CustomerServiceRecord.class, 
				customerServiceRecordService, historyOutgoingRecordTable, searchSql, countSql, null);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++) {
			historyTableFlip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		historyOutgoingRecordTable.setVisibleColumns(VISIBLE_PROPERTIES);
		historyOutgoingRecordTable.setColumnHeaders(COL_HEADERS);
		
		historyOutgoingRecordTable.addGeneratedColumn("operators", new OperatorColumnGenerator());
		historyOutgoingRecordTable.setColumnAlignment("operators", Table.ALIGN_CENTER);
		historyOutgoingRecordTable.setColumnHeader("operators", "操作");

		historyOutgoingRecordTable.setPageLength(5);
		historyTableFlip.setPageLength(5, false);
		tableFooterRightLayout.addComponent(historyTableFlip);
		tableFooterRightLayout.setComponentAlignment(historyTableFlip, Alignment.TOP_RIGHT);
	}
	
	/**
	 * 创建搜索组件
	 * 		高级搜索组件需要以翻页组件为基础
	 */
	private void createSearchComponent() {
		searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		searchHLayout.setWidth("100%");
		
		Label tableTitle = new Label("单个客户记录详情", Label.CONTENT_XHTML);
		tableTitle.setWidth("-1px");
		searchHLayout.addComponent(tableTitle);
		
		recordFilter = new MgrDetailSingleCustomerHistoryRecordFilter();
		recordFilter.setHeight("-1px");
		recordFilter.setTableFlipOver(historyTableFlip);
		
		complexSearchView = new PopupView(recordFilter);
		complexSearchView.setHideOnMouseOut(false);
		searchHLayout.addComponent(complexSearchView);
		searchHLayout.setExpandRatio(complexSearchView, 1.0f);
		searchHLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
		
		searchField = new TextField();
		searchField.setWidth("140px");
		searchField.addListener(this);
		searchField.setImmediate(true);
		searchField.setStyleName("search");
		searchField.setInputPrompt("请输入联系结果");
		searchField.setDescription("可按联系结果名称检索");
		searchHLayout.addComponent(searchField);
		searchHLayout.setComponentAlignment(searchField, Alignment.TOP_RIGHT);
	}
	
	/**
	 *  创建格式化 了 日期列的 Table对象
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
				} else if("marketingProject".equals(colId)) {
					MarketingProject mp = (MarketingProject) property.getValue();
					return mp.getProjectName();
				} else if("serviceRecordStatus".equals(colId)) {
					CustomerServiceRecordStatus status = (CustomerServiceRecordStatus) property.getValue();
					String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
					return status.getStatusName() + " - " + direction;
				} else if("customerResource.company".equals(colId)) {
					Company company = (Company) property.getValue();
					return company.getName();
				} else if("creator".equals(colId)) {
					User csr = (User) property.getValue();
					String creatorInfo = csr.getEmpNo();
					if(csr.getRealName() != null) {
						creatorInfo = creatorInfo + " - " + csr.getRealName();
					}
					return creatorInfo;
				} else if("creator.department".equals(colId)) {
					Department dept = (Department) property.getValue();
					return dept.getName();
				} else if("mgrQcCreator".equals(colId)) {
					User qcCreator = (User) property.getValue();
					String qcCreatorInfo = qcCreator.getEmpNo();
					if(qcCreator.getRealName() != null) {
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
//				lc 注释开始 2013-09-25
//				//  获取指定客服记录的录音下载路径
//				String tmpUrl = serviceRecord.getRecordFileDownloadUrl();
//				Cdr cdr = null;
//				
//				// TODO 为了解决老版本的录音文件查询方式，老版本的 Ec2 中 recordFileName 一定为null
//				if(tmpUrl == null) {
//					// 根据ServiceRecord查询，没查到，查到一个，查到多个
//					List<Cdr> cdrs = cdrService.getRecordByServiceRecord(serviceRecord);
//					if(cdrs.size() > 0) {
//						cdr = cdrs.get(0);
//						tmpUrl = cdr.getUrl();
//					}
//				} 
//				
//				lc 注释结束 2013-09-25
				
				// 获取指定客服记录的录音下载路径
				List<Cdr> cdrs = cdrService.getRecordByServiceRecord(serviceRecord);
				String tmpUrl = null;
				Cdr cdr = null;
				if(isEncryptMobile) {
					for(Cdr c : cdrs) {
						cdr = c;
						if(cdr.getRealUrl(baseUrl) != null) {
							tmpUrl = cdr.getRealUrl(baseUrl);
							break;
						}
					}
				} else {
					for(Cdr c : cdrs) {
						cdr = c;
						if(cdr.getUrl(baseUrl) != null) {
							tmpUrl = cdr.getUrl(baseUrl);
							break;
						}
					}
				}
				
				// 最终下载路径
				final String downloadPath = tmpUrl;
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
							String path = "<EMBED src='" +downloadPath+ "' hidden='false' autostart='true' width=360 height=63 " 
										+ "type=audio/x-ms-wma volume='0' loop='0' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";

							player = new Label(path, Label.CONTENT_XHTML);
							playerLayout.addComponent(player);
						}
					});

					Link typelink = new Link("下载", new ExternalResource(tmpUrl));
					typelink.setIcon(ResourceDataCsr.down_type_ico);
					typelink.setTargetName("_blank");

					HorizontalLayout layout = new HorizontalLayout();
					layout.setSpacing(true);
					layout.setWidth("100%");
					layout.addComponent(listen);
					layout.setComponentAlignment(listen, Alignment.MIDDLE_LEFT);
					
					if (ownBusinessModels.contains(
							MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SOUND_SERVICE_RECORD)) {
						layout.addComponent(typelink);
						layout.setComponentAlignment(typelink, Alignment.MIDDLE_RIGHT);
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

	/**
	 * jrh 
	 * 用于自动生成可以拨打电话的列
	 * 	如果客户只有一个电话，则直接呼叫，否则，使用菜单呼叫
	 */
	private class TelephonesColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if(columnId.equals("customerResource.telephones")) {
				CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
				Set<Telephone> telephones = serviceRecord.getCustomerResource().getTelephones();
				return new TelephoneNumberInTableLabelStyle(telephones, isEncryptMobile);
			} 
			return null;
		}
	}
	
	/**
	 * 用于自动生成记录能容列
	 * 	如果记录内容的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class ContentColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			if(columnId.equals("recordContent")) {
				String content = serviceRecord.getRecordContent();
				if(content == null) {
					return null;
				} else if(!"".equals(content.trim())) {
					Label contentLabel = new Label();
					String trimedContent = content.trim();
					if(trimedContent.length() > 8) {
						contentLabel.setValue(trimedContent.substring(0, 8)+"...");
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
	 * 用于自动生成预约信息显示组件
	 * 	如果预约信息的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class OrderNoteColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			if(columnId.equals("orderNote")) {
				String orderNote = serviceRecord.getOrderNote();
				if(orderNote == null) {
					return null;
				} else if(!"".equals(orderNote.trim())) {
					Label orderNoteLabel = new Label();
					String trimedOrderNote = orderNote.trim();
					if(trimedOrderNote.length() > 8) {
						orderNoteLabel.setValue(trimedOrderNote.substring(0, 8)+"...");
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
	 * 用于自动生成质检原因显示组件
	 * 	如果质检原因的长度大于8位，则显示前八位加上省略号，否则显示全部内容
	 */
	private class QcReasonColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			CustomerServiceRecord serviceRecord = (CustomerServiceRecord) itemId;
			if(columnId.equals("qcReason")) {
				String qcReason = serviceRecord.getQcReason();
				if(qcReason == null) {
					return null;
				} else if(!"".equals(qcReason.trim())) {
					Label qcReasonLabel = new Label();
					String trimedOrderNote = qcReason.trim();
					if(trimedOrderNote.length() > 8) {
						qcReasonLabel.setValue(trimedOrderNote.substring(0, 8)+"...");
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

			Button qc_bt = new Button("质检");
			qc_bt.setStyleName(BaseTheme.BUTTON_LINK);
			qc_bt.setImmediate(true);
			qc_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					showMgrQcWindow(serviceRecord);
				}
			});
			
			return qc_bt;
		}
	}

	/**
	 * 显示质检窗口
	 * @param obj
	 */
	private void showMgrQcWindow(CustomerServiceRecord serviceRecord) {
		//刷新Window的内容
		if(mgrQcWindow == null){
			mgrQcWindow = new MgrQcWindow(this);
			mgrQcWindow.setModal(false);
		}
		
		mgrQcWindow.updateInfo(serviceRecord);
		this.getApplication().getMainWindow().removeWindow(mgrQcWindow);
		this.getApplication().getMainWindow().addWindow(mgrQcWindow);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == searchField) {
			String searchStr = StringUtils.trimToEmpty((String)searchField.getValue());

			this.countSql = "select count(c) from CustomerServiceRecord as c where " + governedDeptIdSql
					+ " and c.customerResource.id = " + customerResource.getId()
					+ " and c.serviceRecordStatus.statusName like '%" + searchStr +"%'" ;
			this.searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.id desc ";
			
			historyTableFlip.setSearchSql(searchSql);
			historyTableFlip.setCountSql(countSql);
			historyTableFlip.refreshToFirstPage();
			// 默认选中第一项
			selectFirstItemInTable();
		}
	}

	/**
	 * 在查询到结果后，默认选择 Table 的第一条记录
	 */
	private void selectFirstItemInTable() {
		BeanItemContainer<CustomerServiceRecord> taskBeanItemContainer = historyTableFlip.getEntityContainer();
		if(taskBeanItemContainer.size() > 0) {
			Object firstItemId = taskBeanItemContainer.getIdByIndex(0);
			historyOutgoingRecordTable.setValue(taskBeanItemContainer.getItem(firstItemId).getBean());
		} else {
			historyOutgoingRecordTable.setValue(null);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		final String downloadCountSql = historyTableFlip.getCountSql();
		if (source == exportServiceRecordExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出呼叫记录
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					exportServiceRecordExcel.setEnabled(false);
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 执行导出操作
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
			file = new File(new String((exportPath + "/指定客户的历史客服记录_"
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

			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"), "GBK");

			// 记录查出来的客服记录Id值
			CustomerServiceRecord firstRecord = (CustomerServiceRecord) historyOutgoingRecordTable.firstItemId();
			if (firstRecord == null) {
				warningNotification.setCaption("选出的结果为空，不能导出历史客服记录！");
				this.getApplication().getMainWindow().showNotification(warningNotification);
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
			operationLog.setDescription("指定客户的导出历史客服记录");
			operationLog.setProgrammerSee(downloadCountSql.replaceFirst("count\\(c\\)", "c"));
			commonService.save(operationLog);
			//======================chb 操作日志======================//

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			ArrayList<String> colNameStrLs = new ArrayList<String>();
			
			String[] baseNames = new String[] {"记录编号", "联系时间", "联系结果", "试听/下载录音", "客户编号", "客户姓名", 
					"客户电话", "公司名称", "创建人", "所属部门", "预约时间", "预约内容", "记录内容", "所属项目", "质检人", "质检结果", "质检原因", 
					"客户性别", "客户生日", "客户等级" , "客户地址", "客户公司", "客户描述信息" };
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
			cellFormat.setBackground(Colour.LIGHT_GREEN);					// 设置单元格背景颜色为浅绿色

			// 计数
			Long recordCount = customerServiceRecordService.getEntityCount(downloadCountSql) + 0L;
			
			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(c\\)", "max\\(c.id\\)");
			Long recordId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(c\\)", "c");

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
					
//					// 录音文件名称
//					String recordFileName = "";
					
					// 录音下载路径	  优先从cdr中找录音文件，如果没找到，再到客服记录上直接找
					String tmpUrl = null;
					
					// 根据ServiceRecord查询，没查到，查到一个，查到多个
					List<Cdr> cdrs = cdrService.getRecordByServiceRecord(record);
					for(Cdr cdr : cdrs) {
						tmpUrl = cdr.getUrl(baseUrl);
//						if(tmpUrl != null) {
//							recordFileName = cdr.getRecordFileName();
//							break;
//						}
					}

					if(tmpUrl == null) {
						tmpUrl = record.getRecordFileDownloadUrl(baseUrl);
//						if(tmpUrl != null) {
//							recordFileName = record.getRecordFileName();
//						}
					}
					
					String url = "无录音文件";
					if(tmpUrl != null) {
						url = tmpUrl;
					}

					// 客服记录对应的客户资源
					CustomerResource customer = record.getCustomerResource();
					
					String customerId = "";
					if(customer != null) {
						customerId = customer.getId().toString();
					}
					
					String customerName = "";
					if(customer != null	&& customer.getName() != null) {
						customerName = customer.getName();
					}

					String phonesStr = "";
					if(customer != null && customer.getTelephones() != null) {
						// 调用Size方法，使之加载电话实体
						customer.getTelephones().size();
						phonesStr = StringUtils.join(customer.getTelephones(), ",");
					}

					String companyName = "";
					if(customer != null && customer.getCompany() != null) {
						companyName = customer.getCompany().getName();
					}

					String creatorInfo = "";
					if(record.getCreator() != null && record.getCreator().getEmpNo() != null) {
						creatorInfo = record.getCreator().getEmpNo();
						String realName = record.getCreator().getRealName();
						if(realName != null) {
							creatorInfo = creatorInfo + " - " + realName;
						}
					}
					
					String deptInfo = "";
					if(record.getCreator() != null) {
						Department dept = record.getCreator().getDepartment();
						deptInfo = dept.getName();
					}
					
					String orderTime = "";
					if(record.getOrderTime() != null) {
						orderTime = sdf.format(record.getOrderTime());
					}
					
					String orderNote = "";
					if(record.getOrderNote() != null) {
						orderNote = record.getOrderNote();
					}
					
					String recordContent = "";
					if(record.getRecordContent() != null) {
						recordContent = record.getRecordContent();
					}
					
					String projectName = "";
					if(record.getMarketingProject() != null && record.getMarketingProject().getProjectName() != null) {
						projectName = record.getMarketingProject().getProjectName();
					}
					
					String mgrQcCreatorInfo = "";
					if(record.getMgrQcCreator() != null && record.getMgrQcCreator().getEmpNo() != null) {
						mgrQcCreatorInfo = record.getMgrQcCreator().getEmpNo();
						String realName = record.getMgrQcCreator().getRealName();
						if(realName != null) {
							mgrQcCreatorInfo = mgrQcCreatorInfo + " - " + realName;
						}
					}
					
					String mgrQcResult = "";
					if(record.getQcMgr() != null) {
						mgrQcResult = record.getQcMgr().getName();
					}

					String qcReason = "";
					if(record.getQcReason() != null) {
						qcReason = record.getQcReason();
					}
					
					// 客户性别
					String sexStr =""; 
					if(customer != null && customer.getSex() != null) {
						sexStr = customer.getSex();
					}
					
					// 客户生日
					String birthdayStr =""; 
					if(customer != null && customer.getBirthdayStr() != null) {
						birthdayStr = customer.getBirthdayStr();
					}
					
					// 客户等级
					String levelStr = "";
					if(customer != null && customer.getCustomerLevel() != null) {
						levelStr = customer.getCustomerLevel().getLevelName();
					}
					
					// 客户所有地址
					String addressAllStr = "";
					if(customer != null) {
						for(Address address : customer.getAddresses()) {
							Province province = address.getProvince();
							City city = address.getCity();
							County county = address.getCounty();
							addressAllStr += "[";
							if(province != null) {
								addressAllStr += province.getName();
							}
							if(city != null) {
								addressAllStr += " "+ city.getName();
							}
							if(county != null) {
								addressAllStr += " "+ county.getName();
							}
							addressAllStr += " "+ address.getStreet() +"] "; 
						}
					}
					
					int index = 0;
					sheet.addCell(new jxl.write.Label(index, cursor + i, id));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, createDate));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, recordStatus));
//					sheet.addCell(new jxl.write.Label(++index, cursor + i, recordFileName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, url));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, customerId));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, customerName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, phonesStr));
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
					sheet.addCell(new jxl.write.Label(++index, cursor + i, levelStr));
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
			logger.error("jrh 管理员导出指定客户的历史客服记录出现异常!" + e.getMessage(), e);
			warningNotification.setCaption("指定客户的历史客服记录导出失败，请重试!");
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
			urlfile = new File(new String(("指定客户的客服记录对应的录音下载地址.txt").getBytes("GBK"), "ISO-8859-1"));
			fos = new FileOutputStream(urlfile);
			bw = new BufferedOutputStream(fos);

			// 记录查出来的客服记录Id值
			CustomerServiceRecord firstRecord = (CustomerServiceRecord) historyOutgoingRecordTable.firstItemId();
			if (firstRecord == null) {
				warningNotification.setCaption("选出的结果为空，不能导出录音下载地址！");
				historyOutgoingRecordTable.getApplication().getMainWindow().showNotification(warningNotification);
				return false;
			}

			// 计数
			Long recordCount = customerServiceRecordService.getEntityCount(downloadCountSql) + 0L;
			
			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(c\\)", "max\\(c.id\\)");
			Long recordId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(c\\)", "c");

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

				// 优先从cdr中找录音文件，如果没找到，再到客服记录上直接找
				for (CustomerServiceRecord record : records) {
					String tmpUrl = null;
					// 根据ServiceRecord查询，没查到，查到一个，查到多个
					List<Cdr> cdrs = cdrService.getRecordByServiceRecord(record);
					for(Cdr cdr : cdrs) {
						tmpUrl = cdr.getUrl(baseUrl);
						if(tmpUrl != null) {
							urlContent.append(tmpUrl + "\r\n");
							break;
						}
					}
					if(tmpUrl == null) {
						tmpUrl = record.getRecordFileDownloadUrl(baseUrl);
						if(tmpUrl != null) {
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
			logger.error(e.getMessage() + "jrh 管理员导出指定客户的历史客服记录对应的录音下载地址时出现异常!", e);
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
				logger.error(e.getMessage() + "jrh 管理员导出指定客户的历史客服记录对应的录音下载地址，IO 流关闭时出现异常！", e);
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
			FileOutputStream fos = new FileOutputStream(sourceFile.getName()+ ".zip");
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
			throw new RuntimeException("压缩录音URL 地址文件是出现异常 --> "+ e.getMessage());
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
	 *  根据MyTaskLeftView 中的任务表格中选中的任务项，显示该条任务对应客户相关的所有历史外呼记录
	 * @param countSql 查询语句
	 */
	public void echoHistoryRecord(CustomerResource customerResource) {
		this.customerResource = customerResource;
		this.countSql = "select count(c) from CustomerServiceRecord as c where " + governedDeptIdSql
				+ " and c.customerResource.id = " + customerResource.getId();

		recordFilter.setCustomerResource(customerResource);
		if(customerResource != null && historyTableFlip != null) {
			searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.createDate desc ";
			historyTableFlip.setSearchSql(searchSql);
			historyTableFlip.setCountSql(countSql);
			historyTableFlip.refreshToFirstPage();
			
			// 默认选中第一项
			selectFirstItemInTable();
		} else {
			historyOutgoingRecordTable.getContainerDataSource().removeAllItems();
		}
		
		// 停止音乐播放器
		stopMusicPlayer();
	}

	/**
	 * 停止音乐播放器
	 */
	private void stopMusicPlayer() {
		player.setValue(null);
		playerLayout.removeAllComponents();
		String musicPath = "<EMBED src='' hidden=false autostart='false' width=360 height=63 "
				+ "type=audio/x-ms-wma volume='0' loop='-1' ShowDisplay='0' ShowStatusBar='1' PlayCount='1'>";
		player = new Label(musicPath, Label.CONTENT_XHTML);
		playerLayout.addComponent(player);
	}

}
