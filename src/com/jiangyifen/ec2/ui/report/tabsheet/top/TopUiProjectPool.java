package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.report.tabsheet.ProjectPool;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class TopUiProjectPool extends VerticalLayout {

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的

	private Button seeButton;
	private Button exportReport;// 导出报表
	private ComboBox usernameBox;

	private ProjectPool projectPool;

	private TabSheet tabSheet;

	private Tab tab;

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private String titleName;// 传入的报表名
	private File file;

	private ReportTableUtil tableUtil;
	private int index;

	private ReportService reportService = SpringContextHolder.getBean("reportService");
	// 登陆的user
	/*private User loginUser = (User) SpringContextHolder.getHttpSession().getAttribute("loginUser");*/

	// private String deptName;
	/*private String domainId;*/

	private String sql;

	public TopUiProjectPool(ProjectPool projectPool) {
		this.projectPool = projectPool;

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		this.addComponent(horizontalLayout);

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");
		horizontalLayout.addComponent(exportReport);

		usernameBox = new ComboBox();
		usernameBox.setWidth("100px");
		usernameBox.setImmediate(true);
		usernameBox.setInputPrompt("------土豆池------");
		horizontalLayout.addComponent(usernameBox);

		usernameBox.addListener(new ComboBox.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {

				if (event.getProperty().getValue() == null) {
					return;
				}

				String value = event.getProperty().getValue().toString();

				Table table = tableUtil.getTable();
				table.removeAllItems();

				for (Object[] objects : list) {

					if (objects[index].equals(value)) {

						table.addItem(objects, null);
					}
				}

			}
		});

	}

	public void seeResult(ClickEvent event) {

		tabSheet = projectPool.getTabSheet();

		// 获得登陆用户的部门名称
		// deptName = loginUser.getDepartment().getName();

		// 登陆用户的domainId
		/*domainId = loginUser.getDomain().getId().toString();*/

		// 获得数据
		list = getRecordByDay();

		for (Object[] objects : list) {
			objects[4] = objects[4] + "%";
			objects[8] = objects[8] + "%";
		}

		// 设置table列名
		columnNames = new ArrayList<String>();
		columnNames.add("土豆池id");
		columnNames.add("土豆池");
		columnNames.add("任务总数");
		columnNames.add("接通总数");
		columnNames.add("接通率");
		columnNames.add("转移到土豆池id");
		columnNames.add("转移到土豆池");
		columnNames.add("转移数量");
		columnNames.add("转化率");

		tableUtil = new ReportTableUtil(list, "土豆池详情(按天统计,单位:秒)", columnNames,
				startValue, endValue);
		tab = tabSheet.addTab(tableUtil, "(表)按天统计", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);

		// 获得工号所在列的下标
		List<String> names = tableUtil.getNames();
		index = names.indexOf("土豆池");

		usernameBox.removeAllItems();

		for (Object[] objects : list) {
			usernameBox.addItem(objects[index]);
		}

	}

	// 导出报表事件
	public void exportReport(ClickEvent event) {

		if (tabSheet == null) {
			NotificationUtil.showWarningNotification(this, "请先查询");
			return;
		}

		// 得到当前tab的组件，把当前tab页的数据导出报表
		ReportTableUtil reportTableUtil = (ReportTableUtil) tabSheet
				.getSelectedTab();

		// 如果reportTableUtil为null提示请点击查询
		if (reportTableUtil == null) {
			NotificationUtil.showWarningNotification(this, "请先查询");
			return;
		}

		// 设置文件名
		titleName = reportTableUtil.getTitleName();
		// 设置数据源
		list = reportTableUtil.getList();
		// 设置列名
		columnNames = reportTableUtil.getNames();
		// 开时时间
		startValue = reportTableUtil.getStartValue();
		// 结束时间
		endValue = reportTableUtil.getEndValue();

		// 生成excel file
		file = ExportExcelUtil.exportExcel(titleName, list, columnNames,
				startValue, endValue);

		// ======================chb 操作日志======================//
		// 记录导出日志
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
		operationLog.setDescription("导出KPI报表");
		operationLog.setProgrammerSee(sql);
		CommonService commonService = SpringContextHolder
				.getBean("commonService");
		commonService.save(operationLog);
		// ======================chb 操作日志======================//

		// 下载file
		final Embedded downloader = new Embedded();
		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		Resource resource = new FileResource(file, event.getComponent()
				.getApplication());
		downloader.setSource(resource);
		this.addComponent(downloader);

		downloader.addListener(new RepaintRequestListener() {

			public void repaintRequested(RepaintRequestEvent event) {
				((VerticalLayout) (downloader.getParent()))
						.removeComponent(downloader);
			}
		});
	}

	public List<Object[]> getRecordByDay() {
		sql = "select w.fromprojectid,w.fromprojectname,t.task_count,c.connect_count,round(c.connect_count*100.0/t.task_count,2),w.toprojectid,w.toprojectname,w.transfer_count,round(w.transfer_count*100.0/t.task_count,2)"
				+ " from"
				+ " (select fromprojectid,fromprojectname,toprojectid,toprojectname,count(*) as transfer_count from ec2_workflow_transfer_log"
				+ " group by fromprojectid,fromprojectname,toprojectid,toprojectname) as w,"
				+ " (select marketingproject_id, count(*) as task_count from ec2_marketing_project_task"
				+ " group by marketingproject_id"
				+ " ) as t,"
				+ " (select marketingproject_id,count(*) as connect_count"
				+ " from ec2_marketing_project_task"
				+ " where isfinished = true and isanswered = true"
				+ " group by marketingproject_id"
				+ " ) as c"
				+ " where w.fromprojectid = t.marketingproject_id and t.marketingproject_id = c.marketingproject_id";

		return reportService.getMoreRecord(sql);
	}
}
