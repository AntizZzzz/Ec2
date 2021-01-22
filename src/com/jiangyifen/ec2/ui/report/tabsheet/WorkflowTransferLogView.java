package com.jiangyifen.ec2.ui.report.tabsheet;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.WorkflowTransferLog;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.service.eaoservice.WorkflowTransferLogService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.report.tabsheet.top.WorkflowTransferLogSimpleFilter;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
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
 * 工作流迁移日志查看界面
 * 
 * @author jrh
 * 
 * 2013-7-30
 */
@SuppressWarnings("serial")
public class WorkflowTransferLogView extends VerticalLayout implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String REPORT_MANAGEMENT_DOWNLOAD_WORKFLOW_TRANSFER_LOG = "report_management&download_workflow_transfer_log";

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"id", "customerResourceId", "customerName", "phoneNo", "importCustomerTime", "importorEmpNo", "importorDeptName", 
			"migrateWorkflow1Time", "workflow1PickTaskTime", "workflow1CsrEmpNo", "workflow1CsrDeptName", 
			"migrateWorkflow2Time", "workflow2PickTaskTime", "workflow2CsrEmpNo", "workflow2CsrDeptName", 
			"migrateWorkflow3Time", "workflow3PickTaskTime", "workflow3CsrEmpNo", "workflow3CsrDeptName"};
	
	private final String[] COL_HEADERS = new String[] {"日志编号", "客户编号", "客户姓名", "客户电话", "原始数据导入时间", "导入者工号", "导入者部门名称", 
			"转入土豆池时间", "土豆获取时间", "土豆获取人工号", "土豆获取人部门名称", 
			"转入一销池时间", "一销获取时间", "一销获取人工号", "一销获取人部门名称", 
			"转入二销池时间", "二销获取时间", "二销获取人工号", "二销获取人部门名称"};
	
	private Table workflowTransferLogTable;
	private WorkflowTransferLogSimpleFilter workflowTransferLogSimpleFilter;		// 工作流迁移日志的简单搜索组件
	private FlipOverTableComponent<WorkflowTransferLog> workflowTransferLogTableFlip;	// 为Table添加的翻页组件

	private Notification notification;							// 提示信息
	
	private Domain domain;										// 当前登录用户所属域
	private User loginUser;										// 当前登录用户
	private boolean isEncryptMobile = true;						// 默认使用加密的电话号码和手机号
	private ArrayList<String> ownBusinessModels;
	
	private Button exportExcel; 								// 导出记录的按钮
	private Embedded downloader; 								// 存放数据的组件
	private HorizontalLayout progressLayout; 					// 进度条组件存放布局管理器
	private ProgressIndicator pi; 								// 进度条
	
	private String exportPath;
	
	private DepartmentService departmentService;				// 部门服务类
	private WorkflowTransferLogService workflowTransferLogService;
	private TelephoneService telephoneService;
	private CommonService commonService;
	
	public WorkflowTransferLogView() {
		this.setSpacing(true);
		this.setMargin(false, true, false, true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		ownBusinessModels = SpringContextHolder.getBusinessModel();

		// 判断是否需要加密
		isEncryptMobile = SpringContextHolder.getBusinessModel().contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		departmentService = SpringContextHolder.getBean("departmentService");
		workflowTransferLogService = SpringContextHolder.getBean("workflowTransferLogService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		commonService = SpringContextHolder.getBean("commonService");

		// 记录导出日志
		exportPath = ConfigProperty.PATH_EXPORT;
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);

		workflowTransferLogSimpleFilter = new WorkflowTransferLogSimpleFilter();
		workflowTransferLogSimpleFilter.setHeight("-1px");
		this.addComponent(workflowTransferLogSimpleFilter);

		// 创建客户表格
		createMigrateLogTable();

		HorizontalLayout tableFooter = new HorizontalLayout();
		tableFooter.setSpacing(true);
		tableFooter.setWidth("100%");
		this.addComponent(tableFooter);

		// 创建导出日志按钮
		createExportComponents(tableFooter);
		
		// 创建翻页组件
		createTableFlipComponent(tableFooter);

		// 为工作流迁移日志Tab 页上方的搜索组件传递翻页组件对象
		workflowTransferLogSimpleFilter.setWorkflowTransferLogTableFlip(workflowTransferLogTableFlip);
		
	}

	private void createMigrateLogTable() {
		workflowTransferLogTable = createFormatColumnTable();
		workflowTransferLogTable.setWidth("100%");
		workflowTransferLogTable.setHeight("-1px");
		workflowTransferLogTable.setSelectable(true);
		workflowTransferLogTable.setStyleName("striped");
		workflowTransferLogTable.setNullSelectionAllowed(false);
		workflowTransferLogTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(workflowTransferLogTable);
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
                } else if("phoneNo".equals(colId)) {
                	String phoneNo = (String) property.getValue();
                	if(isEncryptMobile) {
                		return telephoneService.encryptMobileNo(phoneNo);
                	}
                	return phoneNo;
                }
            	return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * 创建导出工作流迁移日志的组件
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
		if (ownBusinessModels.contains(REPORT_MANAGEMENT_DOWNLOAD_WORKFLOW_TRANSFER_LOG)) {
			exportHLayout.addComponent(exportExcel);
		}

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
		// 根据当前用户所属部门，及其下属部门，创建动态查询语句
		String deptIdSql = "";
		for(int i = 0; i < allGovernedDeptIds.size(); i++) {
			if( i == (allGovernedDeptIds.size() - 1) ) {
				deptIdSql += allGovernedDeptIds.get(i);
			} else {
				deptIdSql += allGovernedDeptIds.get(i) +", ";
			}
		}
		String deptSql = " and wtl.importorDeptId in (" +deptIdSql+ ")";

		// 创建初始搜索语句
		String[] dateStrs = ParseDateSearchScope.parseDateSearchScope("本周");
		String countSql = "select count(wtl) from WorkflowTransferLog as wtl where wtl.domainId = " +domain.getId() +" and wtl.importCustomerTime >= '" +dateStrs[0]+ "' and wtl.importCustomerTime < '" +dateStrs[1]+"'"+ deptSql;
		String searchSql = countSql.replaceFirst("count\\(wtl\\)", "wtl") + " order by wtl.id desc";
		
		workflowTransferLogTableFlip = new FlipOverTableComponent<WorkflowTransferLog>(WorkflowTransferLog.class, 
				workflowTransferLogService, workflowTransferLogTable, searchSql , countSql, null);
		workflowTransferLogTable.setVisibleColumns(VISIBLE_PROPERTIES);
		workflowTransferLogTable.setColumnHeaders(COL_HEADERS);

		workflowTransferLogTable.setPageLength(15);
		workflowTransferLogTableFlip.setPageLength(15, true);
		
		tableFooter.addComponent(workflowTransferLogTableFlip);
		tableFooter.setComponentAlignment(workflowTransferLogTableFlip, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 刷新工作流迁移日志显示界面，当Tab 页切换的时候调用
	 */
	public void updateTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			workflowTransferLogTableFlip.refreshToFirstPage();
		} else {
			workflowTransferLogTableFlip.refreshInCurrentPage();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		final String downloadCountSql = workflowTransferLogTableFlip.getCountSql();
		if (source == exportExcel) {
			final Domain domain = loginUser.getDomain();
			// 导出工作流迁移日志
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
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");

			//======================chb 操作日志======================//
			file = new File(new String((exportPath + "/工作流迁移日志_"
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
			operationLog.setDescription("导出工作流迁移日志");
			operationLog.setProgrammerSee(downloadCountSql.replaceFirst("count\\(wtl\\)", "wtl"));
			commonService.save(operationLog);
			//======================chb 操作日志======================//

			writableWorkbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;

			// 设置excel 的 列样式居中
			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);

			// 记录查出来的客服记录Id值
			WorkflowTransferLog firstLog = (WorkflowTransferLog) workflowTransferLogTable.firstItemId();
			if (firstLog == null) {
				notification.setCaption("<font color='red'><B>选出的结果为空，不能导出工作流迁移日志！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			}

			// 计数
			Long recordCount = workflowTransferLogService.getEntityCount(downloadCountSql) + 0L;

			// 查询符合条件的记录的最大id值
			String maxidSql = downloadCountSql.replaceFirst("count\\(wtl\\)", "max\\(wtl.id\\)");
			String downLoadSearchTmpSql = downloadCountSql.replaceFirst("count\\(wtl\\)", "wtl");
			Long logId = (Long) commonService.excuteSql(maxidSql, ExecuteType.SINGLE_RESULT);

			// 每次加载500条
			List<WorkflowTransferLog> workflowTransferLogs = null;
			Long processCount = 0L;
			int pageStep = 500;
			int index = 0;

			do {
				// 如果当前已经查到超过50000条记录，则新建一个sheet

				if ((processCount % 50000) == 0) {
					++index;

					sheet = writableWorkbook.createSheet("Sheet" + index, index);
					// 设置excel列名
					for (int i = 0; i < COL_HEADERS.length; i++) {
						sheet.addCell(new jxl.write.Label(i, 1,	COL_HEADERS[i]));
					}

					// 设置excel标题
					sheet.mergeCells(0, 0, COL_HEADERS.length - 1, 0);

					// 应该有开始和结束时间的显示
					sheet.addCell(new jxl.write.Label(0, 0, "工作流迁移日志", cellFormat));
				}

				String useSql = downLoadSearchTmpSql + " and wtl.id <= " + logId + " order by wtl.id desc";
				workflowTransferLogs = workflowTransferLogService.loadPageEntities(0, pageStep, useSql);

				processCount += pageStep;
				pi.setValue(processCount / (float) recordCount);

				// 设置下一次查询的最大id 号
				if (workflowTransferLogs.size() > 0) {
					logId = workflowTransferLogs.get(workflowTransferLogs.size() - 1).getId() - 1;
				}
				int cursor = sheet.getRows();
				for (int i = 0; i < workflowTransferLogs.size(); i++) {
					WorkflowTransferLog workflowTransferLog = workflowTransferLogs.get(i);
					
					String idStr = workflowTransferLog.getId().toString();
					
					String customerResourceIdStr = "";
					Long customerResourceId = workflowTransferLog.getCustomerResourceId();
					if(customerResourceId != null) {
						customerResourceIdStr = customerResourceId.toString();
					}

					String customerName = workflowTransferLog.getCustomerName();
					
					String phoneNo = workflowTransferLog.getPhoneNo();
					
					String importCustomerTimeStr = "";
					Date importCustomerTime = workflowTransferLog.getImportCustomerTime();
					if(importCustomerTime != null) {
						importCustomerTimeStr = sdf.format(importCustomerTime);
					}
					
					String importorEmpNo = workflowTransferLog.getImportorEmpNo();
					
					String importorDeptName = workflowTransferLog.getImportorDeptName();
					
					String migrateWorkflow1TimeStr = "";
					Date migrateWorkflow1Time = workflowTransferLog.getMigrateWorkflow1Time();
					if(migrateWorkflow1Time != null) {
						migrateWorkflow1TimeStr = sdf.format(migrateWorkflow1Time);
					}
					
					String workflow1PickTaskTimeStr = "";
					Date workflow1PickTaskTime = workflowTransferLog.getWorkflow1PickTaskTime();
					if(workflow1PickTaskTime != null) {
						workflow1PickTaskTimeStr = sdf.format(workflow1PickTaskTime);
					}
					
					String workflow1CsrEmpNo = workflowTransferLog.getWorkflow1CsrEmpNo();
					
					String workflow1CsrDeptName = workflowTransferLog.getWorkflow1CsrDeptName();
					
					String migrateWorkflow2TimeStr = "";
					Date migrateWorkflow2Time = workflowTransferLog.getMigrateWorkflow2Time();
					if(migrateWorkflow2Time != null) {
						migrateWorkflow2TimeStr = sdf.format(migrateWorkflow2Time);
					}
					
					String workflow2PickTaskTimeStr = "";
					Date workflow2PickTaskTime = workflowTransferLog.getWorkflow2PickTaskTime();
					if(workflow2PickTaskTime != null) {
						workflow2PickTaskTimeStr = sdf.format(workflow2PickTaskTime);
					}
					
					String workflow2CsrEmpNo = workflowTransferLog.getWorkflow2CsrEmpNo();
					
					String workflow2CsrDeptName = workflowTransferLog.getWorkflow2CsrDeptName();
					
					String migrateWorkflow3TimeStr = "";
					Date migrateWorkflow3Time = workflowTransferLog.getMigrateWorkflow3Time();
					if(migrateWorkflow3Time != null) {
						migrateWorkflow3TimeStr = sdf.format(migrateWorkflow3Time);
					}
					
					String workflow3PickTaskTimeStr = "";
					Date workflow3PickTaskTime = workflowTransferLog.getWorkflow3PickTaskTime();
					if(workflow3PickTaskTime != null) {
						workflow3PickTaskTimeStr = sdf.format(workflow3PickTaskTime);
					}
					
					String workflow3CsrEmpNo = workflowTransferLog.getWorkflow3CsrEmpNo();
					
					String workflow3CsrDeptName = workflowTransferLog.getWorkflow3CsrDeptName();

					int j = 0;
					sheet.addCell(new jxl.write.Label(j, cursor + i, idStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, customerResourceIdStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, customerName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, phoneNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, importCustomerTimeStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, importorEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, importorDeptName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, migrateWorkflow1TimeStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow1PickTaskTimeStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow1CsrEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow1CsrDeptName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, migrateWorkflow2TimeStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow2PickTaskTimeStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow2CsrEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow2CsrDeptName));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, migrateWorkflow3TimeStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow3PickTaskTimeStr));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow3CsrEmpNo));
					sheet.addCell(new jxl.write.Label(++j, cursor + i, workflow3CsrDeptName));
				}
			} while (workflowTransferLogs.size() > 0);

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
			logger.error(e.getMessage() + " 导出工作流迁移日志Excel出现异常!", e);
			notification.setCaption("工作流迁移日志导出失败，请重试!");
			this.getApplication().getMainWindow().showNotification(notification);
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
					((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
				}
			}
		});
	}

}
