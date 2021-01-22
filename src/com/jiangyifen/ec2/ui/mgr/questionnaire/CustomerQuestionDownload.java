package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jxl.Workbook;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.service.eaoservice.CustomerQuestionnaireEditService;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagementEdit;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class CustomerQuestionDownload extends Window implements ClickListener {
	

	//静态
	private static final long serialVersionUID = 3725723949927474961L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass()); // 日志工具

	private final String httpPath =BaseUrlUtils.getBaseUrl();		//http路径	老的录音文件名存储方式
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
	
	// 持有UI对象
 	// 本控件持有对象

	// 页面布局组件

	// 页面控件
	private Label lb_msg;
	private Button bt_excute;
	private Button bt_cancel;
 	
	private Label lb_info;
	private Embedded downloader; 										// 存放数据的组件
	private ProgressIndicator pi; 										// 进度条
	
	// 业务必须对象
	private String searchCountJpql;
	private String searchListJpql;
	private Long questionnaireId;
	
	// 业务本页对象

	// 业务注入对象
 	private Domain domain;									//EC域
  	private QuestionnaireService questionnaireService;	
  	private CustomerQuestionnaireEditService customerQuestionnaireEditService;
  	private QuestionService questionService;
			
	public CustomerQuestionDownload(){
		initMainWindow();
		initlayout();
		initBusinessAndSpring();
	}

	private void initMainWindow() {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("导出数据");
		this.setWidth("420px");
		this.setHeight("220px");
	}

	private void initlayout() {
		lb_msg = new Label("<table><tr><td><b><font color='red'>1.</b></font></td><td><b><font color='red'>数据查询和组装需要一些时间，请耐心等候.</font></b></td></tr><tr><td><b><font color='red'>2.</b></font></td><td><b><font color='red'>请勿关闭当前浏览器和刷新该页面.</font></b></td></tr></table>",Label.CONTENT_XHTML);
		this.addComponent(lb_msg);
		
		HorizontalLayout hl_buttons = new HorizontalLayout();
		hl_buttons.setSizeFull();
		hl_buttons.setSpacing(true);
		hl_buttons.setMargin(true);
		
		bt_excute = new Button("确定导出",this);
		bt_excute.setStyleName("default");
		bt_excute.setWidth("310px");
		hl_buttons.addComponent(bt_excute);
		
		bt_cancel = new Button("取消",this);
		hl_buttons.addComponent(bt_cancel);
		this.addComponent(hl_buttons);
		
		VerticalLayout vl_info = new VerticalLayout();
		lb_info = new Label("...",Label.CONTENT_XHTML);
		vl_info.addComponent(lb_info);
		
		pi = new ProgressIndicator();
		pi.setWidth("100%");
		pi.setCaption("导出进度");
		pi.setPollingInterval(100);
		vl_info.addComponent(pi);
		vl_info.setComponentAlignment(pi, Alignment.MIDDLE_LEFT);
		
		downloader = new Embedded();
 		downloader.setType(Embedded.TYPE_BROWSER);
		downloader.setWidth("0px");
		downloader.setHeight("0px");
		downloader.setImmediate(true);
		this.addComponent(vl_info);
		
	}

	/** 初始化业务与Spring组件 */
	private void initBusinessAndSpring(){
		domain = SpringContextHolder.getDomain();
  		questionnaireService = SpringContextHolder.getBean("questionnaireService");
 		questionService = SpringContextHolder.getBean("questionService");
 		customerQuestionnaireEditService = SpringContextHolder.getBean("customerQuestionnaireEditService");
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_excute) {
			new Thread(new Runnable(){
				@Override
				public void run() {
					bt_excute.setCaption("数据导出中...");
					bt_cancel.setEnabled(false);
 					bt_excute.setEnabled(false);
 					
					try {
						WritableWorkbook writableWorkbook = null;
						int listCount = customerQuestionnaireEditService.getEntityCount(searchCountJpql);
						Questionnaire questionnaire = questionnaireService.getQuestionnaireById(questionnaireId);
						List<Question>  lqQuestions = questionService.getQuestionListByQuestionnaireId(questionnaireId);
						Long processCount = 0L;//进度
						int pageStep = 100;	//步长
						int pageIndex = 0;	//sheetindex
						int listIndex = 0;
						List<Object[]> lsnull  = null;// customerQuestionnaireEditService.loadPageEntitiesArr(0, listCount,searchListJpql,questionnaireId);	
						String downloadFile = getReportFilePath() +"QA_" + questionnaire.getId() + "_"+ listCount + "_" + sdf.format(new Date()) + ".xls";
						File file = new File(downloadFile);
						if (!file.getParentFile().exists()) {
							file.getParentFile().mkdirs();
						}
						if (!file.exists()) {
							file.createNewFile();
						}
						writableWorkbook = Workbook.createWorkbook(file);
						WritableSheet sheet = null;
						
						List<String> columnNames = new ArrayList<String>();
				 		columnNames.add("问卷编号");
						columnNames.add("话务员编号");
						columnNames.add("话务员账户");
						columnNames.add("话务员真实姓名");
						columnNames.add("话务员工号");
						columnNames.add("客户编号");
						columnNames.add("客户姓名");
						columnNames.add("客户号码");
						columnNames.add("问卷名称");
						columnNames.add("开始时间");
						columnNames.add("结束时间");
						columnNames.add("完成状态");
						for (int i = 0; i < lqQuestions.size(); i++) {
							columnNames.add(lqQuestions.get(i).getTitle());
						}
						columnNames.add("录音目录");
						int indexCount = 0;
						do {
							if ((processCount % 50000) == 0) {
								indexCount=0;
								++pageIndex;
								sheet = writableWorkbook.createSheet("Sheet" + pageIndex, pageIndex);
								for (int i = 0; i < columnNames.size(); i++) {// 设置excel列名
									sheet.addCell(new jxl.write.Label(i, 1, columnNames.get(i)));
								}
								sheet.mergeCells(0, 0, columnNames.size(), 0);				// 设置excel标题
								WritableCellFormat cellFormat = new WritableCellFormat();// 设置excel 的 列样式居中
								cellFormat.setAlignment(jxl.format.Alignment.LEFT);
		 						sheet.addCell(new jxl.write.Label(0, 0, questionnaire.getMainTitle(), cellFormat));
							}
							
							processCount += pageStep;
							pi.setValue(processCount / (float) listCount);
							
							lsnull  = customerQuestionnaireEditService.loadPageEntitiesArr(listIndex, pageStep,searchListJpql,questionnaireId);	
							// 向excel中导入数据
							for (int i = 0; i < lsnull.size(); i++) {
								Object[] objects = lsnull.get(i);
								for (int j = 0; j < objects.length; j++) {
									sheet.addCell(new jxl.write.Label(j, indexCount + 2,objects[j] == null ? null : objects[j].toString()));
								}
								Long index = (Long)objects[0];
								String musicFile = findMusicFile(index);
								
								sheet.addCell(new jxl.write.Label(objects.length, indexCount + 2,musicFile));	
								indexCount ++;
 							}	
							listIndex = (listIndex+1 * pageStep);
						}while(lsnull.size() > 0);
						writableWorkbook.write();
						writableWorkbook.close();
						downloadFile(file);
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("问卷导出失败_buttonClick_LLXXYY", e);
						getApplication()
								.getMainWindow()
								.showNotification("保存问卷失败!",
										Notification.TYPE_ERROR_MESSAGE);
					}
					bt_excute.setCaption("数据导出完成，注意文件下载");
 					bt_cancel.setEnabled(false);
 					bt_excute.setEnabled(false);
				}
			}).start();
		}else{
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	public void refreshComponentInfo(QuestionnaireManagementEdit questionnaireManagementEdit,Long questionnaireId,String searchCountJpql,String searchListJpql) {
 		this.questionnaireId = questionnaireId;
		this.searchCountJpql = searchCountJpql;
		this.searchListJpql = searchListJpql;
		this.lb_info.setValue("");
		bt_excute.setCaption("确定导出");
		this.bt_cancel.setEnabled(true);
		this.bt_excute.setEnabled(true);
		pi.setValue(0.0f);
	}
	
	 
	private String findMusicFile(Long index) {
		String musicFile = "";
		List<RecordFile> rfl = customerQuestionnaireEditService.getRecordFileList(index, domain);
		for (int iq = 0; iq < rfl.size(); iq++) {
			if(iq == 0){
				musicFile += httpPath+rfl.get(iq).findOriginalDownloadPath();
			}else{
				musicFile += " " +httpPath+ rfl.get(iq).findOriginalDownloadPath();
			}
		}
		return musicFile;
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
			private static final long serialVersionUID = -5797846183977367424L;
			@Override
			public void repaintRequested(RepaintRequestEvent event) {
				if (downloader.getParent() != null) {
					((VerticalLayout) (downloader.getParent())).removeComponent(downloader);
				}
			}
		});
	}
	
	private String getReportFilePath(){//TODO 放到服务器上时打开
		try {
			Properties properties = new Properties();
			properties.load(CustomerQuestionDownload.class.getClassLoader().getResourceAsStream("report.properties"));
			return properties.getProperty("report_path");
		} catch (IOException e) {
			e.printStackTrace();
			return "/opt/apache-tomcat-7.0.32/report_file/";
		}
	}		
}
