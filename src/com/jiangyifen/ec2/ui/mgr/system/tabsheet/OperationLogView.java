package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

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

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 操作日志 管理页面
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class OperationLogView extends VerticalLayout implements
		Button.ClickListener,Property.ValueChangeListener {

	public final String SYSTEM_MANAGEMENT_DOWNLOAD_OPERATION_LOG = "system_management&download_operation_log";

	// 表格中语音文件夹的各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] { "username", "realName","operateDate","description"};

	private final String[] COL_HEADERS = new String[] { "用户名", "真实名","操作日期", "描述信息"};
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// 搜索组件
	private TextField keyWord;
	private Button search;

	// 表格组件
	private Table table;

	private Embedded downloader; 				// 存放数据的组件
	private Button exportLogExcel;				// 导出日志Excel
	private ProgressIndicator pi; 				// 进度条
	private HorizontalLayout progressLayout; 	// 进度条组件存放布局管理器
	
	//Sql语句
	private String sqlSelect;
	private String sqlCount;

	private User loginUser;						// 当前登录用户
	private String exportPath;					// 导出路径
	private ArrayList<String> ownBusinessModels;						// 当前用户所有用的所有管理角色的权限

	// 当前选中的知识
//	private OperationLog selectedOperationLog;
	private FlipOverTableComponent<OperationLog> flip;
//	private BeanItemContainer<OperationLog> container;

	private CommonService commonService;
	private Domain domain;

	public OperationLogView() {
		commonService = SpringContextHolder.getBean("commonService");
		this.domain = SpringContextHolder.getDomain();
		this.loginUser = SpringContextHolder.getLoginUser();
		// 记录导出日志
		this.exportPath = ConfigProperty.PATH_EXPORT;

		this.ownBusinessModels = SpringContextHolder.getBusinessModel();
		
		this.setSizeFull();
		this.setMargin(true);

		// 设置最大的layout
		VerticalLayout contentLayout = new VerticalLayout();
		contentLayout.setSpacing(true);
		this.addComponent(contentLayout);
		// 添加搜索组件
		contentLayout.addComponent(buildSearchLayout());
		// 初始化container
		search.click();
		// 添加表格和按钮组件
		contentLayout.addComponent(buildTableAndButtonsLayout());

	}

	// 创建搜索组件
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		// searchLayout.setWidth("100%");


		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.addComponent(new Label("关键字:"));// 关键字
		keyWord = new TextField();// 输入区域
		keyWord.setWidth("6em");
		keyWord.setStyleName("search");
		keyWord.setInputPrompt("用户名");
		constrantLayout.addComponent(keyWord);
		searchLayout.addComponent(constrantLayout);

		//搜索按钮
		search = new Button("查询");
		search.addListener((ClickListener) this);
		searchLayout.addComponent(search);
		return searchLayout;
	}

	// 创建表格和按钮组件
	private VerticalLayout buildTableAndButtonsLayout() {
		VerticalLayout tableAndButtonsLayout = new VerticalLayout();
		tableAndButtonsLayout.setSpacing(true);

		table = new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId,
					Property property) {
				if (property.getType() == Date.class) {
					return SDF.format((Date) property.getValue());
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};

		table.setStyleName("striped");
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addListener((Property.ValueChangeListener) this);

		tableAndButtonsLayout.addComponent(table);

		// 创建按钮
		tableAndButtonsLayout.addComponent(buildButtons());

		return tableAndButtonsLayout;
	}

	// 创建按钮组件
	@SuppressWarnings("unchecked")
	private HorizontalLayout buildButtons() {

		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth("100%");
		buttonsLayout.setSpacing(true);
		
		HorizontalLayout flp_lo = new HorizontalLayout();
		flp_lo.setSpacing(true);
		buttonsLayout.addComponent(flp_lo);
		buttonsLayout.setComponentAlignment(flp_lo, Alignment.MIDDLE_RIGHT);

		// 创建下载组件
		createExportComponents(flp_lo);
		
		// 右侧按钮（翻页组件）
		flip = new FlipOverTableComponent<OperationLog>(
				OperationLog.class, commonService, table,
				sqlSelect, sqlCount, null);
		table.setPageLength(10);
		flip.setPageLength(10, false);

		// 设置表格头部显示

		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(COL_HEADERS);
		flp_lo.addComponent(flip);
		flp_lo.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return buttonsLayout;
	}

	/**
	 * jrh
	 *	 创建导出操作日志的工具
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
		if(ownBusinessModels.contains(SYSTEM_MANAGEMENT_DOWNLOAD_OPERATION_LOG)) {
			exportLogExcel = new Button("导出日志", this);
			exportLogExcel.setStyleName("default");
			exportLogExcel.setImmediate(true);
			tableFooterRightLayout.addComponent(exportLogExcel);
		}
	}
	
	/**
	 * 更新表格显示的内容
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
		}
	}

	// 执行搜索
	private void executeSearch() {
		//读取搜索关键字
		String keyWordStr=StringUtils.trimToEmpty((String)keyWord.getValue());
		
		//拼装Sql语句
		sqlSelect="select o from OperationLog o where o.username like "+"'%"+keyWordStr+"%' and o.domain.id="+domain.getId()+" order by o.id desc";
		sqlCount="select count(o) from OperationLog o where o.domain.id="+domain.getId()+" and  o.username like "+"'%"+keyWordStr+"%'";
		
		//更新表格
		updateTable(true);
	}

	// 表格valueChange事件
	@Override
	public void valueChange(ValueChangeEvent event) {
//		this.selectedOperationLog = (OperationLog) table.getValue();
//
//		// 改变按钮
//		if (table.getValue() != null) {
//			// 应该是可以通过设置父组件来使之全部为true;
//			edit.setEnabled(true);
//			delete.setEnabled(true);
//		} else {
//			// 应该是可以通过设置父组件来使之全部为false;
//			edit.setEnabled(false);
//			delete.setEnabled(false);
//		}
	}

	// 按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == search) {
			executeSearch();
		} else if (source == exportLogExcel) {
			final String downloadCountSql = flip.getCountSql();
			final Domain domain = loginUser.getDomain();
			// 导出记录
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					exportLogExcel.setEnabled(false);
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 执行导出操作
					exportExcelThreadRun(downloadCountSql, domain, loginUser);
					exportLogExcel.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		}
	}


	/**
	 * 制作表格
	 */
	@SuppressWarnings("unchecked")
	private boolean exportExcelThreadRun(String downloadCountSql,
			Domain domain, User user) {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {

			//======================chb 操作日志======================//
			file = new File(new String((exportPath + "/操作日志_"
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
			operationLog.setDescription("导出操作日志");
			operationLog.setProgrammerSee(downloadCountSql.replaceFirst("count\\(c\\)", "c"));
			commonService.save(operationLog);
			//======================chb 操作日志======================//

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);

			// 记录查出来的操作日志Id值
			OperationLog firstRecord = (OperationLog) table.firstItemId();
			if (firstRecord == null) {
				this.getApplication().getMainWindow()
						.showNotification("选出的结果为空，不能导出操作日志！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}

			// 计数
			Long recordCount = commonService.getEntityCount(downloadCountSql) + 0L;
			
			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(o\\)", "max\\(o.id\\)");
			Long recordId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(o\\)", "o");

			// 每次加载500条
			List<OperationLog> records = null;
			Long processCount = 0L;
			int pageStep = 500;
			int pageIndex = 0;

			do {
				// 如果当前已经查到超过50000条记录，则新建一个sheet
				if ((processCount % 50000) == 0) {
					++pageIndex;

					sheet = writableWorkbook
							.createSheet("Sheet" + pageIndex, pageIndex);
					// 设置excel列名
					for (int i = 0; i < COL_HEADERS.length; i++) {
						sheet.addCell(new jxl.write.Label(i, 1, COL_HEADERS[i]));
					}

					// 设置excel标题
					sheet.mergeCells(0, 0, COL_HEADERS.length - 1, 0);

					// 应该有开始和结束时间的显示
					sheet.addCell(new jxl.write.Label(0, 0, "操作日志", cellFormat));
				}

				String useSql = downLoadSearchTmpSql + " and o.id <= " + recordId + " order by o.id desc";
				records = commonService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (records.size() > 0) {
					recordId = records.get(records.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				for (int i = 0; i < records.size(); i++) {
					OperationLog record = records.get(i);

					String username = "";
					if(record.getUsername() != null) {
						username = record.getUsername();
					}

					String realName = "";
					if(record.getRealName() != null) {
						realName = record.getRealName();
					}
					
					String operateDate = "";
					if(record.getOperateDate() != null) {
						operateDate = SDF.format(record.getOperateDate());
					}
					
					String description = "";
					if(record.getDescription() != null) {
						description = record.getDescription();
					}

					int index = 0;
					sheet.addCell(new jxl.write.Label(index, cursor + i, username));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, realName));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, operateDate));
					sheet.addCell(new jxl.write.Label(++index, cursor + i, description));
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
			exportLogExcel.setEnabled(true);
			progressLayout.setVisible(false);
			logger.error("jrh 管理员导出操作日志出现异常!" + e.getMessage(), e);
			NotificationUtil.showWarningNotification(OperationLogView.this.getApplication(), "操作日志导出失败，请重试!");
			return false;
		}

		return true;
	}

	/**
	 * jrh
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
	
	// *****************setter和getter*******************//
	

	public Table getTable() {
		return table;
	}

	public Button getSearch() {
		return search;
	}

	public void setSearch(Button search) {
		this.search = search;
	}

}
