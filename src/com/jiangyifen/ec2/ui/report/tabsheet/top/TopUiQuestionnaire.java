package com.jiangyifen.ec2.ui.report.tabsheet.top;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.OperationLog;
import com.jiangyifen.ec2.entity.enumtype.OperationStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.ReportService;
import com.jiangyifen.ec2.ui.mgr.accordion.MgrAccordion;
import com.jiangyifen.ec2.ui.report.tabsheet.QuestionnaireDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ExportExcelUtil;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.ReportTableUtil;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.SpringContextHolder;
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
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class TopUiQuestionnaire extends VerticalLayout {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private HorizontalLayout horizontalLayout;// 存放dateComboBox,startDate,endDate组件的
	private ComboBox reportBox;
	private Button seeButton;
	private Button exportReport;// 导出报表

	private QuestionnaireDetail questionnaire;

	private TabSheet tabSheet;

	private Tab tab;

	private List<Object[]> list;// 从数据库获得新的数据

	private String startValue;// startDate的值
	private String endValue;// endDate的值

	private List<String> columnNames;// table的列名
	private String titleName;// 传入的报表名
	private File file;

	private ReportTableUtil tableUtil;
	// private int index;

	private ReportService reportService = SpringContextHolder
			.getBean("reportService");
	// 登陆的user
	// private User loginUser = (User) SpringContextHolder.getHttpSession()
	// .getAttribute("loginUser");

	// private String deptName;
	// private String domainId;

	private String sql;

	public TopUiQuestionnaire(QuestionnaireDetail questionnaire) {
		this.questionnaire = questionnaire;

		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSpacing(true);
		horizontalLayout.setMargin(true);
		this.addComponent(horizontalLayout);

		reportBox = new ComboBox();
		String[] strings = getQuestionnaireTitle();

		for (int i = 0; i < strings.length; i++) {
			reportBox.addItem(strings[i]);
		}
		reportBox.setWidth("100px");
		reportBox.setImmediate(true);
		reportBox.setInputPrompt("---问卷---");
		reportBox.setNullSelectionAllowed(true);

		horizontalLayout.addComponent(reportBox);

		seeButton = new Button("查询", this, "seeResult");
		horizontalLayout.addComponent(seeButton);

		exportReport = new Button("导出报表", this, "exportReport");
		if (SpringContextHolder.getBusinessModel().contains(
				MgrAccordion.REPORT_MANAGEMENT_DOWNLOAD_STAFF_TIME_SHEET)) {
			horizontalLayout.addComponent(exportReport);
		}

	}

	public void seeResult(ClickEvent event) {

		tabSheet = questionnaire.getTabSheet();

		// 获得登陆用户的部门名称
		// deptName = loginUser.getDepartment().getName();

		// 登陆用户的domainId
		// domainId = loginUser.getDomain().getId().toString();

		if (reportBox.getValue() == null) {
			NotificationUtil.showWarningNotification(this, "请先选择问卷！");
			return;
		}

		// 问卷标题
		String reportRequestionnaire = reportBox.getValue().toString();

		// 获得数据
		list = getData(reportRequestionnaire);

		// TODO 暂时 写如下一个sql用来区分不同项目中问卷的数据
		sql = "select t1.customerresources_id from"
				+ " ("
				+ " select customerresources_id from ec2_customer_resource_ec2_customer_resource_batch as t1,ec2_markering_project_ec2_customer_resource_batch as t2"
				+ " where t1.customerresourcebatches_id = t2.batches_id and t2.marketingproject_id = 54"
				+ " ) as t1,"
				+ " ("
				+ " select customerid from ec2_customer_questionnaire where questionnaire_id = 50"
				+ " ) as t2" + " where t1.customerresources_id = t2.customerid";

		List<Object> objects3 = reportService.getOneRecord(sql);
		List<Object[]> delete = new ArrayList<Object[]>();

		for (Object[] objects2 : list) {
			for (Object object : objects3) {
				if (objects2[0].toString().equals(object.toString())) {
					delete.add(objects2);
				}
			}
		}

		for (Object[] objects : delete) {
			list.remove(objects);
		}

		// 设置table列名
		columnNames = new ArrayList<String>();

		int i = 1;
		columnNames.add("[" + (i++) + "]" + "资源id");
		columnNames.add("[" + (i++) + "]" + "客户手机号码");

		List<Object> objects = getTableHeader(reportRequestionnaire);

		for (Object object : objects) {

			columnNames.add("[" + (i++) + "]" + object.toString());
		}

		columnNames.add("[" + (i++) + "]" + "录音下载IP");

		tableUtil = new ReportTableUtil(list, reportRequestionnaire,
				columnNames, startValue, endValue);
		tab = tabSheet.addTab(tableUtil, "问卷详情报表", null);
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab);

		// 得到当前tab的组件，把当前tab页的数据导出报表
		ReportTableUtil reportTableUtil = (ReportTableUtil) tabSheet
				.getSelectedTab();

		if (reportTableUtil != null) {

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

		// // 设置文件名
		// titleName = reportTableUtil.getTitleName();
		// // 设置数据源
		// list = reportTableUtil.getList();
		// // 设置列名
		// columnNames = reportTableUtil.getNames();
		// // 开时时间
		// startValue = reportTableUtil.getStartValue();
		// // 结束时间
		// endValue = reportTableUtil.getEndValue();

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
		operationLog.setDescription("问卷详情报表");
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

	public String[] getQuestionnaireTitle() {

		sql = "select maintitle from ec2_questionnaire group by maintitle";

		List<Object> list = reportService.getOneRecord(sql);

		String[] strings = new String[list.size()];

		for (int i = 0; i < list.size(); i++) {

			strings[i] = list.get(i).toString();
		}

		return strings;
	}

	// 最终拼凑出来的数据 此做法极差，偶合性极强，几乎不能共用与扩展
	public List<List<Object>> getDescriptions(String questionnaireTitle) {

		List<List<Object>> lists = new ArrayList<List<Object>>();

		// get telephone (number,customerresource_id)
		List<Object[]> telephones = getTelephones(questionnaireTitle);

		// get customer resource description (key,value,customerresource_id)
		List<Object[]> descriptions = getCustomerResourceDescription(questionnaireTitle);

		for (Object[] telephone : telephones) {

			// init save customerresource_id
			List<Object> list = new ArrayList<Object>();
			list.add(telephone[1].toString());
			list.add(telephone[0].toString());

			for (Object[] description : descriptions) {

				// if customerresource_id equals
				if (telephone[1].toString().equals(description[2].toString())) {

					list.add(description[1].toString());

				}

			}

			// 获得资源描述 key value 集合
			lists.add(list);

		}

		logger.info("------------1--------------");
		iteratorData(lists);
		logger.info("------------1--------------");

		return lists;

	}

	public void iteratorData(List<List<Object>> lists) {

		for (List<Object> list : lists) {

			for (Object obj : list) {

				System.out.print(obj.toString() + "|");
			}

		}

	}

	public void showToConsole(List<Object[]> objects) {

		for (Object[] objects2 : objects) {

			for (int i = 0; i < objects2.length; i++) {

				logger.info(objects2[i] + " ");
			}

		}
	}

	// 获得问卷数据集合,此时的数据比较粗糙
	public List<List<Object>> getquestionnaires(String questionnaireTitle) {

		sql = "select t7.customerid,t7.title,t7.name from"
				+ " ("
				+ " select t5.customerid,t6.title,t5.name,t6.questionnaire_id from"
				+ " ("
				+ " select t3.customerid,t4.name,t4.question_id from"
				+ " ("
				+ " select t1.customerid,t2.customerquestionnaire_id,t2.questionoptions_id from  ec2_customer_questionnaire as t1,ec2_customer_question_options as t2"
				+ " where t1.id = t2.customerquestionnaire_id"
				+ " order by t1.customerid"
				+ " ) as t3,ec2_question_options as t4"
				+ " where t3.questionoptions_id = t4.id"
				+ " ) as t5,ec2_question as t6"
				+ " where t5.question_id = t6.id"
				+ " ) as t7,ec2_questionnaire as t8"
				+ " where t7.questionnaire_id = t8.id and t8.maintitle = "
				+ "'" + questionnaireTitle + "'";

		List<Object[]> list = reportService.getMoreRecord(sql);

		sql = "select t7.customerid,t7.title,t7.text from"
				+ " ("
				+ " select t5.customerid,t6.title,t5.text,t6.questionnaire_id from"
				+ " ("
				+ " select t3.customerid,t3.text,t4.question_id from"
				+ " ("
				+ " select t1.customerid,t2.customerquestionnaire_id,t2.questionoptions_id,t2.text from  ec2_customer_questionnaire as t1,ec2_customer_question_options as t2"
				+ " where t1.id = t2.customerquestionnaire_id and text !=''"
				+ " order by t1.customerid"
				+ " ) as t3,ec2_question_options as t4"
				+ " where t3.questionoptions_id = t4.id"
				+ " ) as t5,ec2_question as t6"
				+ " where t5.question_id = t6.id"
				+ " ) as t7,ec2_questionnaire as t8"
				+ " where t7.questionnaire_id = t8.id and t8.maintitle = "
				+ "'" + questionnaireTitle + "'";

		List<Object[]> texts = reportService.getMoreRecord(sql);

		for (Object[] names : list) {

			for (Object[] text : texts) {

				if (text[0].toString().equals(names[0].toString())
						&& text[1].toString().equals(names[1].toString())) {

					names[2] = text[2];
				}
			}
		}

		sql = "select t1.customerid,t2.title from ec2_customer_questionnaire as t1,ec2_question as t2,ec2_questionnaire as t3"
				+ " where t1.questionnaire_id = t3.id and t2.questionnaire_id = t3.id and t3.maintitle = "
				+ "'" + questionnaireTitle + "'" + " order by t1.id";

		List<Object[]> titles = reportService.getMoreRecord(sql);

		List<List<Object>> data = new ArrayList<List<Object>>();

		for (Object[] titleObjects : titles) {

			List<Object> list2 = new ArrayList<Object>();

			// name用来记录多个答案
			String name = "";
			for (Object[] objects : list) {

				if (titleObjects[0].toString().equals(objects[0].toString())
						&& titleObjects[1].toString().equals(
								objects[1].toString())) {

					name = name + objects[2].toString() + ",";
				}
			}

			list2.add(titleObjects[0]);
			list2.add(titleObjects[1]);
			if (name != "") {

				list2.add(name.substring(0, name.length() - 1));
			} else {

				list2.add(name);
			}

			data.add(list2);
		}

		logger.info("-------------2-------------");
		iteratorData(data);
		logger.info("-------------2-------------");
		return data;

	}

	public List<Object> getTableHeader(String questionnaireTitle) {

		sql = "select t1.key from ec2_customer_resource_description as t1,ec2_customer_questionnaire as t2,ec2_questionnaire as t3"
				+ " where t1.customerresource_id = t2.customerid and t2.questionnaire_id = t3.id and t3.maintitle = "
				+ "'" + questionnaireTitle + "'" + " order by t1.id";

		List<Object> objects = reportService.getOneRecord(sql);

		sql = "select t1.title from ec2_question as t1,ec2_questionnaire as t2 where t1.questionnaire_id = t2.id and t2.maintitle = "
				+ "'" + questionnaireTitle + "'";

		List<Object> titleList = reportService.getOneRecord(sql);

		for (Object object : titleList) {

			objects.add(object);
		}

		return objects;
	}

	public List<Object[]> getTelephones(String questionnaireTitle) {

		sql = "select t1.number,t1.customerresource_id from ec2_telephone as t1,ec2_customer_questionnaire as t2,ec2_questionnaire as t3"
				+ " where t1.customerresource_id = t2.customerid and t2.questionnaire_id = t3.id and t3.maintitle = "
				+ "'" + questionnaireTitle + "'" + " order by t1.id";

		return reportService.getMoreRecord(sql);
	}

	public List<Object[]> getCustomerResourceDescription(
			String questionnaireTitle) {

		sql = "select t1.key,t1.value,t1.customerresource_id from ec2_customer_resource_description as t1,ec2_customer_questionnaire as t2,ec2_questionnaire as t3"
				+ " where t1.customerresource_id = t2.customerid and t2.questionnaire_id = t3.id and t3.maintitle = "
				+ "'" + questionnaireTitle + "'" + " order by t1.id";

		return reportService.getMoreRecord(sql);
	}

	public List<Object[]> getRecordFile(String questionnaireTitle) {

		Properties props = new Properties();
		InputStream inputStream = Config.class.getClassLoader()
				.getResourceAsStream("config.properties");
		try {
			props.load(inputStream);
		} catch (IOException e) {

			e.printStackTrace();
		}

		String rec_url_perfix = props.getProperty("rec_url_perfix");

		sql = "select t1.customerid,t1.downloadpath from ec2_record_file as t1,ec2_customer_questionnaire_record_file_link as t2,ec2_customer_questionnaire as t3,ec2_questionnaire as t4"
				+ " where t1.id = t2.record_file_id and t2.customer_questionnaire_id = t3.id and t3.questionnaire_id = t4.id and t4.maintitle = "
				+ "'" + questionnaireTitle + "'" + " order by t1.customerid";

		List<Object[]> list = reportService.getMoreRecord(sql);

		for (Object[] object : list) {
			object[1] = rec_url_perfix + object[1].toString();
		}

		return list;

	}

	// 没有录音
	public List<Object[]> getRecord(String questionnaireTitle) {

		// 获得描述数据
		List<List<Object>> descriptions = getDescriptions(questionnaireTitle);

		// 获得问卷数据
		List<List<Object>> questionnaires = getquestionnaires(questionnaireTitle);

		List<List<Object>> willDeleted = new ArrayList<List<Object>>();

		// 删除匹配不上的数据
		for (List<Object> description : descriptions) {

			boolean b = true;

			for (List<Object> questionnaire : questionnaires) {

				if (description.get(0).toString()
						.equals(questionnaire.get(0).toString())) {

					b = false;
				}
			}

			if (b) {
				willDeleted.add(description);
			}

		}

		for (List<Object> list : willDeleted) {

			descriptions.remove(list);
		}

		// 拼数据
		for (List<Object> description : descriptions) {

			for (List<Object> questionnaire : questionnaires) {

				if (description.get(0).toString()
						.equals(questionnaire.get(0).toString())) {

					description.add(questionnaire.get(2));

				}
			}
		}

		logger.info("------------3--------------");
		iteratorData(descriptions);
		logger.info("------------3-------------");

		// 数据转换
		List<Object[]> objects = new ArrayList<Object[]>();

		for (List<Object> list : descriptions) {

			Object[] objects2 = new Object[list.size()];

			for (int i = 0; i < list.size(); i++) {

				objects2[i] = list.get(i);
			}

			objects.add(objects2);

		}

		return objects;
	}

	// 加上录音
	public List<Object[]> getData(String questionnaireTitle) {

		List<Object[]> list = getRecord(questionnaireTitle);

		// 获得指定问卷的数据
		sql = "select t1.customerid from ec2_customer_questionnaire as t1,ec2_questionnaire as t2 "
				+ " where t1.questionnaire_id = t2.id and t2.maintitle = "
				+ "'" + questionnaireTitle + "'";

		List<Object> customerIds = reportService.getOneRecord(sql);

		// 筛选数据，去掉customerid 不匹配的记录
		List<Object[]> willDeleted = new ArrayList<Object[]>();

		for (Object[] objects : list) {

			boolean b = true;

			for (Object customerId : customerIds) {

				if (objects[0].toString().equals(customerId.toString())) {

					b = false;
				}
			}

			if (b) {
				willDeleted.add(objects);
			}

		}

		for (Object[] objects : willDeleted) {

			list.remove(objects);
		}

		logger.info("---------------4----------------");
		showToConsole(list);
		logger.info("---------------4---------------");

		// 添加录音
		List<Object[]> recordFiles = getRecordFile(questionnaireTitle);

		logger.info("------------5------------");
		showToConsole(recordFiles);
		logger.info("------------5------------");

		List<Object[]> newList = new ArrayList<Object[]>();

		for (Object[] objects : list) {

			Object[] newObjects = new Object[objects.length + 1];

			for (Object[] recordFile : recordFiles) {

				if (objects[0].toString().equals(recordFile[0].toString())) {

					for (int i = 0; i < objects.length; i++) {

						newObjects[i] = objects[i];
					}

					newObjects[objects.length] = recordFile[1];

					newList.add(newObjects);
				}
			}

		}

		logger.info("--------------6----------------");
		showToConsole(newList);
		logger.info("--------------6----------------");

		return newList;
	}
}
