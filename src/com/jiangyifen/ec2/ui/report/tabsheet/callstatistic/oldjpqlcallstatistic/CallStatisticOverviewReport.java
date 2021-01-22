 package com.jiangyifen.ec2.ui.report.tabsheet.callstatistic.oldjpqlcallstatistic;

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
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.report.entity.CallStatisticOverview;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CallStatisticOverviewService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
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
public class CallStatisticOverviewReport extends VerticalLayout implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat SDF_DAY = new SimpleDateFormat("yyyy-MM-dd");

	private static final String REPORT_MANAGEMENT_DOWNLOAD_CALL_STATISTIC_OVERVIEW = "report_management&download_call_statistic_overview";
	
	private Table csorTable;
	private CallStatisticOverviewSimpleFilter csorSimpleFilter;		// 话务统计总概览的简单搜索组件
	private FlipOverTableComponent<CallStatisticOverview> csorTableFlip;	// 为Table添加的翻页组件

	private Notification notification;							// 提示信息
	
	private User loginUser;										// 当前登录用户
	private ArrayList<String> ownBusinessModels;
	
	private Button exportExcel; 								// 导出记录的按钮
	private Embedded downloader; 								// 存放数据的组件
	private HorizontalLayout progressLayout; 					// 进度条组件存放布局管理器
	private ProgressIndicator pi; 								// 进度条
	
	private String exportPath;
	
	private CallStatisticOverviewService callStatisticOverviewService;
	private CommonService commonService;
	
	public CallStatisticOverviewReport() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		ownBusinessModels = SpringContextHolder.getBusinessModel();

		callStatisticOverviewService = SpringContextHolder.getBean("callStatisticOverviewService");
		commonService = SpringContextHolder.getBean("commonService");

		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 创建客户表格
		createCsorTable();

		csorSimpleFilter = new CallStatisticOverviewSimpleFilter();
		csorSimpleFilter.setHeight("-1px");
		csorSimpleFilter.setCsorTable(csorTable);
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
	 * 创建格式化显示列的表格
	 * @return
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if("hour".equals(colId)) { 
					int hour = (Integer) property.getValue();
					if(hour < 10) {
						return "0"+hour+":00-0"+hour+":59";
					} else {
						return hour+":00-"+hour+":59";
					}
				} else if (property.getType() == Date.class) {
					CallStatisticOverview cso = (CallStatisticOverview) rowId;
					if(cso.getId() == null) {
						return "总计";
					}
            		return dateFormat.format((Date)property.getValue());
                } else if ("bridgedRate".equals(colId) || "bridgedInRate".equals(colId) 
                		|| "bridgedOutRate".equals(colId) || "incomingServiceLevel".equals(colId)) {
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
		final String downloadCountSql = csorTableFlip.getCountSql();
		if (source == exportExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出话务统计总概览
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
			String direction = csorSimpleFilter.getDirection();
			String[] current_col_headers = csorTable.getColumnHeaders();
			
			String fileName = "";
			if("all".equals(direction)) {
				fileName = "/话务统计总概览_(全部方向)_";
			} else if("incoming".equals(direction)) {
				fileName = "/话务统计总概览_(呼入方向)_";
			} else if("outgoing".equals(direction)) {
				fileName = "/话务统计总概览_(呼出方向)_";
			}
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
			operationLog.setProgrammerSee(downloadCountSql.replaceFirst("count\\(wtl\\)", "wtl"));
			commonService.save(operationLog);
			//======================chb 操作日志======================//

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);

			// 记录查出来的客服记录Id值
			CallStatisticOverview firstLog = (CallStatisticOverview) csorTable.firstItemId();
			if (firstLog == null) {
				notification.setCaption("<font color='red'><B>选出的结果为空，不能导出话务统计总概览！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			}

			// 计数
			Long recordCount = callStatisticOverviewService.getEntityCount(downloadCountSql) + 0L;

			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(e\\)", "max\\(e.id\\)");
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(e\\)", "e");
			Long logId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);

			// 每次加载500条
			List<CallStatisticOverview> csos = null;
			Long processCount = 0L;
			int pageStep = 50;
			int index = 0;
			
			do {
				// 如果当前已经查到超过50000条记录，则新建一个sheet

				if ((processCount % 50000) == 0) {
					++index;

					sheet = writableWorkbook.createSheet("Sheet" + index, index);
					// 设置excel列名
					for (int i = 0; i < current_col_headers.length; i++) {
						sheet.addCell(new jxl.write.Label(i, 1,	current_col_headers[i]));
					}

					// 设置excel标题
					sheet.mergeCells(0, 0, current_col_headers.length - 1, 0);

					// 应该有开始和结束时间的显示
					sheet.addCell(new jxl.write.Label(0, 0, "话务统计总概览", cellFormat));
				}

				String useSql = downLoadSearchTmpSql + " and e.id <= " + logId + " order by e.id desc";
				csos = callStatisticOverviewService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (csos.size() > 0) {
					logId = csos.get(csos.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				for (int i = 0; i < csos.size(); i++) {
					CallStatisticOverview cso = csos.get(i);
					this.addCellToExcel(direction, sheet, cursor, i, cso);
				}
				
			} while (csos.size() > 0);

			this.addCellToExcel(direction, sheet, sheet.getRows(), 0, csorSimpleFilter.getSubtotal());
			
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
			notification.setCaption("话务统计总概览导出失败，请重试!");
			this.getApplication().getMainWindow().showNotification(notification);
			return false;
		}

		return true;
	}

	private void addCellToExcel(String direction, WritableSheet sheet,
			int cursor, int i, CallStatisticOverview cso)
			throws WriteException, RowsExceededException {
		String startDate = "";
		String hour = "";
		if(cso.getId() == null) {
			startDate = "总计";
		} else {
			hour = cso.getHour().toString();
			startDate = SDF_DAY.format(cso.getStartDate());
		}
		if("all".equals(direction)) {
			int j = 0;
			sheet.addCell(new jxl.write.Label(j, cursor + i, startDate));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, hour));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getCallAmount().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getBridgedAmount().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getPickupInSpecifiedSec().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getAbandonInSpecifiedSec().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getTotalCallTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getAvgCallTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getAvgAnswerTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, changeToPercent(cso.getBridgedRate())));
		} else if("incoming".equals(direction)) {
			int j = 0;
			sheet.addCell(new jxl.write.Label(j, cursor + i, startDate));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, hour));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getCallInAmount().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getBridgedInAmount().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getCsrPickupInSpecifiedSec().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getCustomerAbandonInSpecifiedSec().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getTotalInCallTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getAvgInCallTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getAvgInAnswerTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, changeToPercent(cso.getBridgedInRate())));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, changeToPercent(cso.getIncomingServiceLevel())));
		} else if("outgoing".equals(direction)) {
			int j = 0;
			sheet.addCell(new jxl.write.Label(j, cursor + i, startDate));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, hour));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getCallOutAmount().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getBridgedOutAmount().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getCustomerPickupInSpecifiedSec().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getCsrAbandonInSpecifiedSec().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getTotalOutCallTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getAvgOutCallTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, cso.getAvgOutAnswerTime().toString()));
			sheet.addCell(new jxl.write.Label(++j, cursor + i, changeToPercent(cso.getBridgedOutRate())));
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
