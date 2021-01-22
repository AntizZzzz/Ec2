 package com.jiangyifen.ec2.ui.report.tabsheet.callstatistic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CallStatisticOverviewService;
import com.jiangyifen.ec2.ui.FlipOverTableUseNativeSql;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.utils.Pagination;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 话务统计总概览查看界面
 * 
 * @author jrh
 * 
 * 2013-7-30
 */
@SuppressWarnings("serial")
public class CallStatisticNativeSqlOverviewReport extends VerticalLayout implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String REPORT_MANAGEMENT_DOWNLOAD_CALL_STATISTIC_OVERVIEW = "report_management&download_call_statistic_overview";
	
	private Table csorTable;
	private CallStatisticNativeSqlOverviewSimpleFilter csorSimpleFilter;			// 话务统计总概览的简单搜索组件
	private FlipOverTableUseNativeSql csorTableFlip;			// 为Table添加的翻页组件

	private User loginUser;										// 当前登录用户
	private ArrayList<String> ownBusinessModels;
	
	private Button exportExcel; 								// 导出记录的按钮
	private Embedded downloader; 								// 存放数据的组件
	private HorizontalLayout progressLayout; 					// 进度条组件存放布局管理器
	private ProgressIndicator pi; 								// 进度条
	
	private String exportPath;
	
	private CallStatisticOverviewService callStatisticOverviewService;
	private CommonService commonService;
	
	public CallStatisticNativeSqlOverviewReport() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		ownBusinessModels = SpringContextHolder.getBusinessModel();

		callStatisticOverviewService = SpringContextHolder.getBean("callStatisticOverviewService");
		commonService = SpringContextHolder.getBean("commonService");

		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;
		
		// 创建客户表格
		createCsorTable();

		csorSimpleFilter = new CallStatisticNativeSqlOverviewSimpleFilter(csorTable);
		csorSimpleFilter.setHeight("-1px");
		csorSimpleFilter.getSearch_bt().click();
		this.addComponent(csorSimpleFilter);

		this.addComponent(csorTable);
		
		HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setSpacing(true);
		tableFooter.setWidth("100%");
		this.addComponent(tableFooter);

		// 创建导出日志按钮
		createExportComponents(tableFooter);
		
		// 创建翻页组件
		csorTableFlip = csorSimpleFilter.getCsorTableFlip();
		tableFooter.addComponent(csorTableFlip);
		tableFooter.setComponentAlignment(csorTableFlip, Alignment.MIDDLE_RIGHT);
		
	}

	/**
	 * 创建表格
	 */
	private void createCsorTable() {
		csorTable = createFormatColumnTable();
		csorTable.setWidth("100%");
		csorTable.setHeight("-1px");
		csorTable.setSelectable(true);
		csorTable.setStyleName("striped");
		csorTable.setNullSelectionAllowed(false);
		csorTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
	}

	/**
	 * 创建格式化显示列的表格“注意：这个表格中的数据时数组Object[]”
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if("小时".equals(colId)) { 
					int hour = (Integer) property.getValue();
					if(hour < 10) {
						return "0"+hour+":00-0"+hour+":59";
					} else {
						return hour+":00-"+hour+":59";
					}
                } else if ("接通率".equals(colId) || "呼入接通率".equals(colId) 
                		|| "呼出接通率".equals(colId) || "呼入服务水平".equals(colId)) {
                	return changeToPercent((Double) property.getValue());
                }
            	return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 创建导出话务统计总概览的组件
	 * 
	 * @param tableFooterRightLayout
	 */
	private void createExportComponents(HorizontalLayout tableFooter) {
		HorizontalLayout exportHLayout = new HorizontalLayout();
		exportHLayout.setSpacing(true);
		tableFooter.addComponent(exportHLayout);
		tableFooter.setComponentAlignment(exportHLayout, Alignment.MIDDLE_LEFT);
		
		// 创建导出记录按钮
		exportExcel = new Button("导出Excel", this);
		exportExcel.setStyleName("default");
		exportExcel.setImmediate(true);
		if (ownBusinessModels.contains(REPORT_MANAGEMENT_DOWNLOAD_CALL_STATISTIC_OVERVIEW)) {
			exportHLayout.addComponent(exportExcel);
		}

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
	}
	
	/**
	 * 刷新话务统计总概览显示界面，当Tab 页切换的时候调用
	 */
	public void updateTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			csorTableFlip.refreshToFirstPage();
		} else {
			csorTableFlip.refreshInCurrentPage();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == exportExcel) {
			// 导出话务统计总概览
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 初始化进度条
					exportExcel.setEnabled(false);
					pi.setValue(0f);
					progressLayout.setVisible(true);
					// 执行导出操作
					exportExcelThreadRun(loginUser);
					exportExcel.setEnabled(true);
					progressLayout.setVisible(false);
				}
			}).start();
		}
	}

	/**
	 * 制作表格
	 */
	private boolean exportExcelThreadRun(User user) {
		// 把数据存到excel文件中
		File file = null;
		WritableWorkbook writableWorkbook = null;
		try {
			String callDirection = csorSimpleFilter.getCallDirection();
			String statisticType = csorSimpleFilter.getStatisticType();
			String[] current_col_headers = csorTable.getColumnHeaders();
			int colLen = current_col_headers.length;
			
			String directionText = "(全部方向)_";
			if("incoming".equals(callDirection)) {
				directionText = "(呼入方向)_";
			} else if("outgoing".equals(callDirection)) {
				directionText = "(呼出方向)_";
			}
			
			String statisticStr = "按天、时统计_";
			if("byDay".equals(statisticType)) {
				statisticStr = "按天统计_";
			} else if("byMonth".equals(statisticType)) {
				statisticStr = "按月统计_";
			}
			
			String fileName = "/话务统计总概览_"+directionText+statisticStr;
			String downloadCountSql = csorTableFlip.getCountSql();
			String downloadSearchSql = csorTableFlip.getSearchSql();
			Domain domain = user.getDomain();
			
			//======================chb 操作日志======================//
			file = new File(new String((exportPath + fileName
					+ new Date().getTime() + ".xls").getBytes("UTF-8"),	"ISO-8859-1"));
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

			String filePath = new String((file.getAbsolutePath()).getBytes("ISO-8859-1"), "UTF-8");

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
			operationLog.setDescription("导出话务统计总概览");
			operationLog.setProgrammerSee(downloadSearchSql);
			commonService.save(operationLog);
			//======================chb 操作日志======================//

			// 记录查出来的客服记录Id值
			Object[] firstLog = (Object[]) csorTable.firstItemId();
			if (firstLog == null) {
				this.getApplication().getMainWindow().showNotification("选出的结果为空，不能导出话务统计总概览！", Notification.TYPE_WARNING_MESSAGE);
				return false;
			}

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(Colour.LIGHT_GREEN);					// 设置单元格背景颜色为浅绿色

			// 计数
			int totalRecordCount = callStatisticOverviewService.getInfoCountByNativeSql(downloadCountSql);

			// 每次加载500条
			Long processCount = 0L;
			int pageLen = 50;
			int sheetIndex = 0;

			Pagination pagination = new Pagination(totalRecordCount);
			pagination.setPageRecords(pageLen);	
			
			for(int i = 1; i <= pagination.getTotalPage(); i++) { 
				int startIndex = pagination.getStartIndex();

				// 如果当前已经查到超过50000条记录，则新建一个sheet
				if ((processCount % 50000) == 0) {
					++sheetIndex;
					sheet = writableWorkbook.createSheet("Sheet" + sheetIndex, sheetIndex);
					
					for (int ch = 0; ch < colLen; ch++) {	// 设置excel列名
						sheet.addCell(new jxl.write.Label(ch, 0, current_col_headers[ch], cellFormat));
					}
				}

				List<Object[]> crsIndb1 = callStatisticOverviewService.loadPageInfosByNativeSql(startIndex, pageLen, downloadSearchSql);
				if(!"byDayHour".equals(statisticType)) {
					crsIndb1 = csorSimpleFilter.rebuildTableSourceObjArr(crsIndb1, callDirection);
				}
				
				processCount += pageLen;
				pi.setValue(processCount / (float) totalRecordCount);

				int cursor = sheet.getRows();
				for (int r = 0; r < crsIndb1.size(); r++) {
					this.addCellToExcel(callDirection, sheet, colLen, cursor, r, crsIndb1.get(r), null);
				}
				
				pagination.setCurrentPage(i+1);
			}
			
			// 添加总计信息到TabSheet 的最后
			this.addCellToExcel(callDirection, sheet, colLen, sheet.getRows(), 0, csorSimpleFilter.getSubtotalDstObjs(), cellFormat);
			
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
			logger.error(e.getMessage() + " 导出话务统计总概览Excel出现异常!", e);
			this.getApplication().getMainWindow().showNotification("话务统计总概览导出失败，请重试!", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	/**
	 * 将数据写入Excel中
	 * @param direction			查看记录的呼叫方向
	 * @param sheet				当前操作的Excel 的Sheet对象
	 * @param colLen			要插入的列数
	 * @param cursor			当前已经插入到的行数
	 * @param row				当前要插入的行数增量
	 * @param statisticsInfo	要插入的信息
	 * @param cellFormat		单元格样式
	 * @throws WriteException
	 * @throws RowsExceededException
	 */
	private void addCellToExcel(String callDirection, WritableSheet sheet, int colLen, int cursor, int row, Object[] statisticsInfo, WritableCellFormat cellFormat)
			throws WriteException, RowsExceededException {
		for(int col = 0; col < colLen; col++) {
			Object info = statisticsInfo[col];
			String value = "";
			if(info != null) {	// 如果info 不为空，则判断但求要处理的统计信息是否为“接通率、或者服务水平”等，这些需要转换成百分比的形式，否则直接转换成字符串就行
				if(("incoming".equals(callDirection) && col == (colLen-2)) || col == (colLen-1)) {
					value = changeToPercent((Double) info);
				} else {
					value = info.toString();
				}
			}
			if(cellFormat != null) {	// 单元格样式不为空，加样式
				sheet.addCell(new jxl.write.Label(col, cursor + row, value, cellFormat));
			} else {
				sheet.addCell(new jxl.write.Label(col, cursor + row, value));
			}
		}
	}

	/**
	 * 将小数改成百分比
	 * @param number
	 * @return
	 */
	private String changeToPercent(Double number) {
		String numberStr = (number * 100)+"%";
    	if(numberStr.length() > 6) {
    		return numberStr.substring(0, 5) +"%";
    	} 
    	return numberStr;
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
