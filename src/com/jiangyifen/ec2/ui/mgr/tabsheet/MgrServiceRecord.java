package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import jxl.Cell;
import jxl.Workbook;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.Cdr;
import com.jiangyifen.ec2.entity.City;
import com.jiangyifen.ec2.entity.County;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Province;
import com.jiangyifen.ec2.entity.TableKeyword;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceDescriptionService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.service.eaoservice.TableKeywordService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.mgr.common.TelephoneNumberInTableLabelStyle;
import com.jiangyifen.ec2.ui.mgr.servicerecord.MgrCdrTable;
import com.jiangyifen.ec2.ui.mgr.servicerecord.MgrServiceRecordSimpleFilter;
import com.jiangyifen.ec2.ui.mgr.servicerecord.QcWindow;
import com.jiangyifen.ec2.ui.mgr.servicerecord.ServiceRecordRecycleToBatch;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer.ComponentAttachListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 数据整理--客服记录查看
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class MgrServiceRecord extends VerticalLayout implements
		ComponentAttachListener, Button.ClickListener {
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";

	/**
	 * 主要组件
	 */
	// 搜索组件
	private MgrServiceRecordSimpleFilter simpleFilter;

	// 项目表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private boolean isEncryptMobile = true; // 默认使用加密的电话号码和手机号

	@SuppressWarnings("unused")
	private int entityCount;
	private CustomerServiceRecordService customerServiceRecordService;
	private FlipOverTableComponent<CustomerServiceRecord> flip;
	private Button exportExcel;
	private Button recycleToBatch;
	private ServiceRecordRecycleToBatch recycleToBatchWindow;

	// 弹出窗口显示的Table组件
	private QcWindow qcWindow;
	private MgrCdrTable mgrCdrTable;

	// 进度条组件
	private VerticalLayout progressOuterLayout;
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;

	private Button switchToHsrView_bt;					// jrh 切换查看客服记录视角的按钮
	private MgrServiceRecordAllView mgrServiceRecordAllView;	// 用于显示客服记录的组界面，也是当前组件的上级界面
	
	/**
	 * 其他组件
	 */
	private CdrService cdrService;
	private CommonService commonService;
	private CustomerResourceDescriptionService descriptionService;		// 描述信息服务类
	private TableKeywordService tableKeywordService;
	
	// 导出的文件名
	@SuppressWarnings("unused")
	private String fileName;

	private String exportPath;

	
	/**
	 * 构造器
	 */
	public MgrServiceRecord(MgrServiceRecordAllView mgrServiceRecordAllView) {
		initService();
		this.setWidth("100%");
		this.setMargin(false, true, false, true);
		this.mgrServiceRecordAllView = mgrServiceRecordAllView;
		
		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 搜索组件
		simpleFilter = new MgrServiceRecordSimpleFilter(this);
		constrantLayout.addComponent(simpleFilter);
		// 初始化Sql语句
		simpleFilter.getSearchButton().click();
		
		// 表格和按钮
		constrantLayout.addComponent(buildTabelAndButtonsLayout());

		// 初始化
		mgrCdrTable = new MgrCdrTable(isEncryptMobile);
	}

	/**
	 * 初始化所有的Service
	 */
	private void initService() {
		customerServiceRecordService = SpringContextHolder.getBean("customerServiceRecordService");
		cdrService = SpringContextHolder.getBean("cdrService");
		descriptionService = SpringContextHolder.getBean("customerResourceDescriptionService");
		commonService = SpringContextHolder.getBean("commonService");
		tableKeywordService = SpringContextHolder.getBean("tableKeywordService");
		exportPath = ConfigProperty.PATH_EXPORT;
	}

	/**
	 * 创建表格和按钮输出（Table）
	 * 
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSpacing(true);
		// 创建表格
		table = new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				if (property.getValue() == null) {
					return "";
				} else if (property.getType() == Date.class) {
					return dateFormat.format((Date) property.getValue());
				} else if ("marketingProject".equals(colId)) {
					MarketingProject mp = (MarketingProject) property
							.getValue();
					return mp.getProjectName();
				} else if ("serviceRecordStatus".equals(colId)) {
					CustomerServiceRecordStatus status = (CustomerServiceRecordStatus) property.getValue();
					String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
					return status.getStatusName() + " - " + direction;
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
		table.setWidth("100%");
		table.setHeight("-1px");
		table.setImmediate(true);
		table.setSelectable(true);
		table.setColumnWidth("id", 50);
		table.setNullSelectionAllowed(false);
		table.setColumnAlignment("id", Table.ALIGN_CENTER);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		tabelAndButtonsLayout.addComponent(table);
		
		// 创建按钮
		HorizontalLayout tableBottom_hlo = buildTableButtons();
		tabelAndButtonsLayout.addComponent(tableBottom_hlo);
		tabelAndButtonsLayout.setComponentAlignment(tableBottom_hlo, Alignment.TOP_RIGHT);
		return tabelAndButtonsLayout;
	}
	
	/**
	 * 由buildTabelAndButtonsLayout调用，创建按钮的输出，为Table设置数据源
	 * 
	 * @return
	 */
	private HorizontalLayout buildTableButtons() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setSpacing(true);

		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<CustomerServiceRecord>(
				CustomerServiceRecord.class, customerServiceRecordService,
				table, sqlSelect, sqlCount, this);
		entityCount = flip.getTotalRecord();
		table.setPageLength(18);
		flip.setPageLength(18, false);

		// 设置表格头部显示
		Object[] visibleColumns = new Object[] { "id", "createDate",
				"serviceRecordStatus", "customerResource.id", "customerResource.name",
				"customerResource.telephones", "marketingProject",
				"creator.empNo", "creator.realName","qcMgr", "orderTime", "orderNote" };
		String[] columnHeaders = new String[] { "记录编号", "联系时间", "联系结果", "客户编号", "客户姓名",
				"电话", "所属项目", "记录员工号", "记录员姓名","管理员质检结果", "预约时间", "预约内容" };
		for (int i = 0; i < visibleColumns.length; i++)
			flip.getEntityContainer().addNestedContainerProperty(
					visibleColumns[i].toString());
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);
		// jrh
		table.addGeneratedColumn("customerResource.telephones", new TelephonesColumnGenerator());
		table.addGeneratedColumn("orderNote", new OrderNoteColumnGenerator());

		// 生成附加列
		this.addColumn1(table);
		this.addColumn3(table);
		this.addColumn2(table);
		this.addColumn(table);

		HorizontalLayout leftButtons=new HorizontalLayout();
		leftButtons.setSpacing(true);
		tableButtons.addComponent(leftButtons);
		tableButtons.setComponentAlignment(leftButtons, Alignment.TOP_RIGHT);

		// 创建进度条组件
		progressLayout=new HorizontalLayout();
		progressOuterLayout = new VerticalLayout();
		progressOuterLayout.addComponent(progressLayout);
		leftButtons.addComponent(progressOuterLayout);
		
		exportExcel = new Button("导出客服记录");
		exportExcel.addListener(this);
		if (SpringContextHolder.getBusinessModel().contains(
				MgrAccordion.DATA_MANAGEMENT_DOWNLOAD_SERVICE_RECORD)) {
			leftButtons.addComponent(exportExcel);
		}

		recycleToBatch = new Button("回收到批次");
		recycleToBatch.addListener(this);
		leftButtons.addComponent(recycleToBatch);

		// jrh 添加切换查看客服记录视角的按钮
		switchToHsrView_bt = new Button(MgrServiceRecordAllView.LAST_SR_CAPTION, this);
		switchToHsrView_bt.addStyleName("default");
		switchToHsrView_bt.setImmediate(true);
		leftButtons.addComponent(switchToHsrView_bt);

		pi = new ProgressIndicator();
		pi.setEnabled(false);
		pi.setPollingInterval(500);

		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
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
	 * 由 buildButtonsAndFlipLayout调用，为Table 添加一列
	 * 
	 * @param table
	 */
	private void addColumn(final Table table) {
		table.addGeneratedColumn("通话查看", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId, Object columnId) {
				// 使Popup创建多个,但Table只创建一个
				PopupView popup = new PopupView(new Content() {
					@Override
					public String getMinimizedValueAsHTML() {
						return "通话查看";
					}

					@Override
					public Component getPopupComponent() {
						return mgrCdrTable;
					}
				});
				popup.setData(itemId);
				popup.setHideOnMouseOut(false);
				popup.addListener(MgrServiceRecord.this);
				return popup;
			}
		});
	}

	/**
	 * 由buildTableButtons调用，为Table添加列
	 * 
	 * @param table
	 */
	private void addColumn2(final Table table) {
		table.addGeneratedColumn("质检操作", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				// 创建备注显示组件
				final CustomerServiceRecord serviceRecord=(CustomerServiceRecord)itemId;
	
				Button button=new Button("质检");
				button.setStyleName(BaseTheme.BUTTON_LINK);
				button.setData(serviceRecord);
				button.addListener(MgrServiceRecord.this);
				return button;
			}
		});
	}
	/**
	 * 由 buildLeftTableAndFlip 和 buildRightTable 调用，为Table 添加一列
	 * 
	 * @param table
	 */
	private void addColumn1(final Table table) {
		table.addGeneratedColumn("记录内容", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				Object note = table.getContainerDataSource()
						.getContainerProperty(itemId, "recordContent");
				String longNote = "";
				if (note != null && note.toString() != null) {
					longNote = note.toString();
				}
				String shortNote = longNote;
				if (shortNote.length() < 5) {
					shortNote += "...";
				} else {
					shortNote = shortNote.substring(0, 5) + "...";
				}
				Label label = new Label(shortNote);
				label.setDescription(longNote);
				return label;
			}
		});
	}
	/**
	 * 由 buildLeftTableAndFlip 和 buildRightTable 调用，为Table 添加一列
	 * 
	 * @param table
	 */
	private void addColumn3(final Table table) {
		table.addGeneratedColumn("质检原因", new Table.ColumnGenerator() {
			public Component generateCell(Table source, Object itemId,
					Object columnId) {
				Object note = table.getContainerDataSource()
						.getContainerProperty(itemId, "qcReason");
				String longNote = "";
				if (note != null && note.toString() != null) {
					longNote = note.toString();
				}
				String shortNote = longNote;
				if (shortNote.length() < 5) {
					shortNote += "...";
				} else {
					shortNote = shortNote.substring(0, 5) + "...";
				}
				Label label = new Label(shortNote);
				label.setDescription(longNote);
				return label;
			}
		});
	}

	/**
	 * 导出客服记录表格
	 */
	private void exportExcel() {
		final User user = SpringContextHolder.getLoginUser();
		final Domain domain = user.getDomain();

		new Thread(new Runnable() {
			@Override
			public void run() {
				// 初始化进度条
				pi.setEnabled(true);
				pi.setValue(0f);
				progressLayout.addComponent(new Label("正在导出:"));
				progressLayout.addComponent(pi);
				exportExcel.setEnabled(false);
				exportExcelThreadRun(domain, user);// TODO chb 有点问题
				exportExcel.setEnabled(true);
				// 导入完成后进度条的处理进度条
				pi.setEnabled(false);
				progressLayout.removeAllComponents();
			}
		}).start();
	}

	/**
	 * 由buttonClick调用 显示回收到批次窗口
	 */
	private void showRecycleToBatchWindow() {
		if (recycleToBatchWindow == null) {
			try {
				recycleToBatchWindow = new ServiceRecordRecycleToBatch(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
			}
		}
		this.getApplication().getMainWindow().removeWindow(recycleToBatchWindow);
		this.getApplication().getMainWindow().addWindow(recycleToBatchWindow);
	}

	private void exportExcelThreadRun(Domain domain, User user) {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {

			//======================chb 操作日志======================//
			file = new File(new String((exportPath + "/客服记录[记录视角]_"
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

			// 记录查出来的客服记录Id值
			CustomerServiceRecord firstRecord = (CustomerServiceRecord) table.firstItemId();
			if (firstRecord == null) {
				NotificationUtil.showWarningNotification(this,"选出的结果为空，不能导出客服记录！");
				return;
			}
			
			OperationLogUtil.simpleLog(user, "导出客服记录"+file.getName());

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;
			
			ArrayList<String> colNameStrLs = new ArrayList<String>();
			
			String[] baseNames = new String[] { "记录编号", "联系时间", "联系结果", "客户编号", "客户姓名",
					"电话", "所属项目", "记录员工号", "记录员姓名", "管理员质检结果", "记录内容", "质检原因", 
					"客户性别", "客户生日", "客户等级" , "客户地址", "客户公司", "客户描述信息"};
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
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			// 每次加载500条
			List<CustomerServiceRecord> records = null;
			String sqlSelectTemp = sqlSelect.substring(0, sqlSelect.length() - 19); // 去掉Order by的SQL

			// jrh 查询符合条件的记录的最大id值
			String maxidSql = sqlSelectTemp.replaceFirst(" e ", " max\\(e.id\\) ");
			Long recordId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);
			
			// 计数
			Long recordCount = customerServiceRecordService.getEntityCount(sqlCount) + 0L;
			Long processCount = 0L;
			int pageStep = 1000;
			int iterator = 0;

			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(jxl.format.Colour.LIGHT_GREEN);					// 设置单元格背景颜色为浅绿色
			
			do {

				if ((processCount % 50000) == 0) {
					++iterator;
					sheet = writableWorkbook.createSheet("Sheet" + iterator, iterator);

					// 设置excel列名
					for (int i = 0; i < namesall.length; i++) {
						sheet.addCell(new jxl.write.Label(i, 0, namesall[i], cellFormat));
					}
				}

				String useSql = sqlSelectTemp + " and e.id<=" + recordId
						+ " order by e.id desc";
				records = customerServiceRecordService.loadPageEntities(0,
						pageStep, useSql);
				processCount += pageStep;

				pi.setValue(processCount / (float) recordCount);

				if (records.size() > 0) {
					recordId = records.get(records.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				for (int i = 0; i < records.size(); i++) {
					CustomerServiceRecord record = records.get(i);
					CustomerResource customer = record.getCustomerResource();

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
					
					String customerId = "";
					if (customer != null) {
						customerId = customer.getId().toString();
					}

					String customerName = "";
					if (customer != null
							&& customer.getName() != null) {
						customerName = customer.getName();
					}

					String phonesStr = "";
					if (customer != null
							&& customer.getTelephones() != null) {
						// 调用Size方法，使之加载电话实体
						customer.getTelephones().size();
						phonesStr = StringUtils.join(customer.getTelephones(), ",");
					}

					String projectName = "";
					if (record.getMarketingProject() != null
							&& record.getMarketingProject().getProjectName() != null) {
						projectName = record.getMarketingProject()
								.getProjectName();
					}

					String creatorEmpNo = "";
					if (record.getCreator() != null
							&& record.getCreator().getEmpNo() != null) {
						creatorEmpNo = record.getCreator().getEmpNo();
					}

					String creatorName = "";
					if (record.getCreator() != null
							&& record.getCreator().getRealName() != null) {
						creatorName = record.getCreator().getRealName();
					}
					
					String mgrQcResult = "";
					if(record.getQcMgr() != null) {
						mgrQcResult = record.getQcMgr().getName();
					}

					String recordContent = "";
					if (record.getCreator() != null
							&& record.getRecordContent() != null) {
						recordContent = record.getRecordContent();
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
					
					String companyName = "";
					if (customer != null
							&& customer.getCompany() != null) {
						companyName = customer.getCompany().getName();
					}

					int index = 0;
					sheet.addCell(new jxl.write.Label(index, cursor + i, id));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, createDate));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, recordStatus));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, customerId));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, customerName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, phonesStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, projectName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, creatorEmpNo));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, creatorName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, mgrQcResult));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, recordContent));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, qcReason));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, sexStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, birthdayStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, levelStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, addressAllStr));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, companyName));

					// 客户所有描述信息
					String descriptionsStr = "";
					if(customer != null) {
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

			// 下载报表
			final Embedded downloader = new Embedded();
			downloader.setType(Embedded.TYPE_BROWSER);
			downloader.setWidth("0px");
			downloader.setHeight("0px");
			Resource resource = new FileResource(file, this.getApplication());
			downloader.setSource(resource);
			this.addComponent(downloader);

			downloader.addListener(new RepaintRequestListener() {
				@Override
				public void repaintRequested(RepaintRequestEvent event) {
					((VerticalLayout) (downloader.getParent()))
							.removeComponent(downloader);
				}
			});

		} catch (Exception e) {
			exportExcel.setEnabled(true);
			e.printStackTrace();
			LoggerFactory.getLogger(this.getClass()).error("导出客服记录报表出现异常!");
		}
		exportExcel.setEnabled(true);
	}
	
	/**
	 * 用来供回调的方法
	 * 
	 * @param records
	 */
	public void flipOverCallBack(List<CustomerServiceRecord> records) {
		for (CustomerServiceRecord record : records) {
			record.getCustomerResource().getTelephones().size();
		}
	}

	/**
	 * 显示质检窗口
	 * @param obj
	 */
	private void showQcWindow(CustomerServiceRecord obj) {
		//刷新Window的内容
		if(qcWindow==null){
			qcWindow=new QcWindow(this);
		}
		
		qcWindow.updateInfo(obj);
		this.getApplication().getMainWindow().removeWindow(qcWindow);
		this.getApplication().getMainWindow().addWindow(qcWindow);
	}
	
	/**
	 * 导出文件时的文件名
	 * 
	 * @param fileName
	 */
	public void setExportFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 由executeSearch调用更新表格内容
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 * @param isToFirst
	 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(sqlSelect);
			flip.setCountSql(sqlCount);
			
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
			entityCount = flip.getTotalRecord();
		}
	}

	/**
	 * 取得Flip 为了取得结果总数
	 * 
	 * @return
	 */
	public FlipOverTableComponent<CustomerServiceRecord> getFlip() {
		return flip;
	}

	/**
	 * 返回Table的一个引用
	 * 
	 * @return
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 设置sql
	 * 
	 * @param sqlSelect
	 */
	public void setSqlSelect(String sqlSelect) {
		this.sqlSelect = sqlSelect;
	}
	
	public String getSqlSelect() {
		return sqlSelect;
	}

	//取得进度条组件输出区
	public VerticalLayout getProgressLayout() {
		return progressOuterLayout;
	}
	
	/**
	 * 设置sql
	 * 
	 * @param sqlCount
	 */
	public void setSqlCount(String sqlCount) {
		this.sqlCount = sqlCount;
	}

	public String getSqlCount() {
		return sqlCount;
	}
	
	/**
	 * 由Popup组件的弹出事件attach调用
	 * 
	 * @param event
	 */
	@Override
	public void componentAttachedToContainer(ComponentAttachEvent event) {
		PopupView popupView = (PopupView) event.getComponent();
		CustomerServiceRecord serviceRecord = (CustomerServiceRecord) popupView
				.getData();
		table.setValue(serviceRecord);
		// 根据ServiceRecord查询，没查到，查到一个，查到多个
		List<Cdr> cdrs = cdrService.getRecordByServiceRecord(serviceRecord);
		mgrCdrTable.setTableItems(cdrs);
	}

	/**
	 * 导出Excel事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton().equals(exportExcel)) {
			exportExcel();
		}else if(event.getButton().equals(recycleToBatch)){
			showRecycleToBatchWindow();
		}else if(event.getButton().equals(switchToHsrView_bt)){
			mgrServiceRecordAllView.switchView(MgrServiceRecordAllView.LAST_VIEW_TYPE);
		}else{ 
			//确认点击的是质检按钮
			Object obj=event.getButton().getData();
			if(obj!=null&&obj instanceof CustomerServiceRecord){
				showQcWindow((CustomerServiceRecord)obj);
			}
		}
	}

}
