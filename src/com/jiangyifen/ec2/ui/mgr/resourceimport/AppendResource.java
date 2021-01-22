package com.jiangyifen.ec2.ui.mgr.resourceimport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.mgr.ImportResourceService;
import com.jiangyifen.ec2.service.mgr.impl.ImportResourceServiceImpl;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ResourceImport;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 创建追加资源
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class AppendResource extends Window implements Button.ClickListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 主要组件区域
	 */
	// 导入新批次组件
	private Label batchLabel;
	private TextArea noteArea;
	private CheckBox checkBox;

	// 导入指定路径视图组件
	private HorizontalLayout importBatchPathLayout;
	private Upload upload;
	private Button cancel;
	
	//进度条组件
	private VerticalLayout progressOuterLayout;
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;
	
	/**
	 * Service
	 */
	private User user;
//	jrh 不去重、去重导入可选
//	private boolean ignoreReduplicate;
	private ImportResourceService importResourceService;
	/*private Domain domain;
	private CommonService commonService;*/
	
	/**
	 * 其他组件
	 */
	// 上传的文件
	private File excelFile;
	private ResourceImport resourceImport;
	private CustomerResourceBatch batch;

	/**
	 * 构造器
	 * 
	 * @param resourceImport
	 *            资源导入视图的引用
	 */
	public AppendResource(ResourceImport resourceImport) {
		this.initService();
		this.center();
		this.setModal(true);
		this.resourceImport = resourceImport;
		this.setSizeUndefined();
		this.setResizable(false);
		this.setCaption("追加到现有批次");

		VerticalLayout windowContent = new VerticalLayout();
		this.setContent(windowContent);
		// 添加Window内最大的Layout
		windowContent.setSizeUndefined();// .setSizeFull();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);

		// 批次名称
		windowContent.addComponent(buildBatchNameLayout());
		// 描述信息
		windowContent.addComponent(buildNoteLayout());
		
		//创建进度条组件
		progressOuterLayout=resourceImport.getProgressLayout();
		progressLayout=new HorizontalLayout();
		progressOuterLayout.addComponent(progressLayout);
//				windowContent.addComponent(progressLayout);
		
		pi = new ProgressIndicator();
		pi.setEnabled(false);
		pi.setPollingInterval(1000);
		
		// 上传输出
		HorizontalLayout uploadLayout = buildUploadLayout();
		windowContent.addComponent(uploadLayout);
		windowContent.setComponentAlignment(uploadLayout,
				Alignment.BOTTOM_RIGHT);
	}
	
	/**
	 * 初始化此类中用到的Service
	 */
	private void initService() {
		importResourceService = SpringContextHolder.getBean("importResourceService");
		user=SpringContextHolder.getLoginUser();
		/*domain=SpringContextHolder.getDomain();
		commonService=SpringContextHolder.getBean("commonService");*/
	}
	
	/**
	 * 导入批次的名称
	 * 
	 * @return
	 */
	private HorizontalLayout buildBatchNameLayout() {
		HorizontalLayout importBatchNameLayout = new HorizontalLayout();
		this.addComponent(importBatchNameLayout);

		importBatchNameLayout.addComponent(new Label("批次名称:"));
		batchLabel = new Label("");
		batchLabel.setWidth("7em");
		importBatchNameLayout.addComponent(batchLabel);
		
		//chen added 20140725
		HorizontalLayout tmpLayout=new HorizontalLayout();
		checkBox=new CheckBox("去重");
		checkBox.setImmediate(true);
		checkBox.setValue(true);
		tmpLayout.addComponent(checkBox);
		importBatchNameLayout.addComponent(tmpLayout);
		
		return importBatchNameLayout;
	}

	/**
	 * 描述信息的输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildNoteLayout() {
		HorizontalLayout noteLayout = new HorizontalLayout();
		noteLayout.addComponent(new Label("备注信息:"));
		noteArea = new TextArea();
		noteArea.setColumns(15);
		noteArea.setRows(3);
		noteArea.setWordwrap(true);
		noteArea.setInputPrompt("请输入与批次相关的备注信息！");
		noteLayout.addComponent(noteArea);
		return noteLayout;
	}

	/**
	 * 上传文件组件输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildUploadLayout() {
		importBatchPathLayout = new HorizontalLayout();
		importBatchPathLayout.setSpacing(true);

		// 选择上传文件按钮
		upload = new Upload();
		upload.setImmediate(true);
		upload.setButtonCaption("导入");
		this.setUploadListener();
		this.assignReceiverForUpload(upload);
		importBatchPathLayout.addComponent(upload);

		// 取消按钮
		cancel = new Button("取消");
		cancel.addListener(this);
		importBatchPathLayout.addComponent(cancel);

		return importBatchPathLayout;
	}

	/**
	 * 为Upload组件设置多个监听器
	 */
	private void setUploadListener() {
		upload.addListener(new Upload.SucceededListener() {
			public void uploadSucceeded(SucceededEvent event) {
				WebApplicationContext context = (WebApplicationContext) AppendResource.this.getApplication().getContext();
				final String ip = context.getBrowser().getAddress();
				new Thread(){
					@Override
					public void run() {
						Long startTime=System.currentTimeMillis();
						executeImport(ip);
						Long endTime=System.currentTimeMillis();
						logger.info("追加数据耗时:"+(endTime-startTime)/1000+"秒");
					}
				}.start();
				//Excel 上传完毕移除窗口
				AppendResource.this.getParent().removeWindow(AppendResource.this);
			}
		});
	}

	/**
	 * 由 buildPathLayout 调用，指定上传的Excel文件的存储名称和位置
	 */
	private void assignReceiverForUpload(Upload upload) {
		upload.setReceiver(new Upload.Receiver() {

			public OutputStream receiveUpload(String filename, String mimeType) {
				// Output stream to write to
				FileOutputStream fos = null;
				String userName =user.getUsername();
				String dateStr = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss")
						.format(new Date());

				filename = ConfigProperty.PATH + "/" + dateStr + "_" + userName
						+ "_" + filename;
				excelFile = new File(filename);
				if (!excelFile.exists()) {
					if (!excelFile.getParentFile().exists()) {
						excelFile.getParentFile().mkdirs();
					}
					try {
						excelFile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();

						Logger logger = LoggerFactory.getLogger(this.getClass());
						logger.warn("创建文件失败，无法再指定位置创建新Excel文件！");
						throw new RuntimeException("无法再指定位置创建新Excel文件！");
					}
				} else {
					throw new RuntimeException("Excel文件已经存在，请重新创建！");
				}

				try {
					fos = new FileOutputStream(excelFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					throw new RuntimeException("Excel文件无法读取或不存在！");
				}
				
				// jrh 成功上传文件应该写在这，而不是下载236行这个 try catch 之前
				Logger logger = LoggerFactory.getLogger(this.getClass());
				logger.info("成功上传Excel文件:" + filename);
				return fos;
			}
		});
	}

	@Override
	public void attach() {
		super.attach();
		// 取出选中的批次，此时一定有且仅有一个批次处于选中状态
		batch = (CustomerResourceBatch) resourceImport.getTable().getValue();
		batchLabel.setValue(batch.getBatchName());
		checkBox.setValue(true);
		noteArea.setValue(batch.getNote());
		upload.setEnabled(true);
		cancel.setEnabled(true);
	}

	/**
	 * 执行导入数据库操作
	 */
	public Boolean executeImport(String ip) {
		//初始化进度条
		pi.setEnabled(true);
		pi.setValue(0f);
		progressLayout.addComponent(new Label("上传进度:"));
		progressLayout.addComponent(pi);
		progressOuterLayout.addComponent(progressLayout);
		upload.setEnabled(false);
		cancel.setEnabled(false);
				
		String noteAreaStr = "";
		//备注
		if (noteArea.getValue() != null) {
			noteAreaStr = noteArea.getValue().toString().trim();
			batch.setNote(noteAreaStr);
		}
		//调用导入数据的方法导入数据，更新表格信息
		String importMessage="";
		try {
			OperationLogUtil.simpleLog(user, "追加资源 "+batch.getBatchName()+" Batch_Id:"+batch.getId());
			long timeKey = System.currentTimeMillis();
			logger.info(timeKey+" LXY_ImportData_HHHH: Start "+batch.getBatchName()+" Batch_Id:"+batch.getId());
			
			//isAppend是否是追加资源
//			jrh 不去重、去重导入可选
//			Map<String, Long> importDataResult = importResourceService.importData(excelFile, batch,user,true,pi, ignoreReduplicate);
			Boolean quChong = (Boolean)checkBox.getValue();
			quChong=quChong==null?false:quChong;
			Map<String, Long> importDataResult = importResourceService.importData(excelFile, batch,user,true,pi,quChong);
			OperationLogUtil.simpleLog(user, timeKey+" 追加资源完成:" + excelFile.getName()+" Import Success "+(new Date())+" ");
			logger.info(timeKey+" LXY_ImportData_HHHH: End " + excelFile.getName()+" Import Success "+(new Date())+" ");
			
			Long successNum=importDataResult.get(ImportResourceServiceImpl.IMPORT_SUCCESS);
			Long hasResourceUpdateNum=importDataResult.get(ImportResourceServiceImpl.HAS_RESOURCE_UPDATE);
			Long hasCustomerResourceNum=importDataResult.get(ImportResourceServiceImpl.HAS_CUSTOMER_IGNORE);
			Long invalidNum=importDataResult.get(ImportResourceServiceImpl.INVALID_NUMBER);
			Long elapsedTime = importDataResult.get(ImportResourceServiceImpl.ELAPSED_TIME);
			importMessage="成功导入"+successNum+"条,</br>";
			
			//chenhb：20140728
			if(quChong){
				importMessage+="重复数据"+hasResourceUpdateNum+"条,</br>";
			}else{
				importMessage+="更新数据"+hasResourceUpdateNum+"条,</br>";
			}
			
//			if(ignoreReduplicate == false) {
				importMessage+="客户资源"+hasCustomerResourceNum+"条,</br>";
//			}
			importMessage+="无效号码"+invalidNum+"条,</br>";
			importMessage+="总共耗时"+elapsedTime+"秒.";
			resourceImport.updateTable(true);
			//导入完成后进度条的处理进度条
			pi.setEnabled(false);
			progressLayout.removeAllComponents();
		} catch (Exception e) {
			// jrh 注意，下面只能用resourceImport 来获取主窗口，因为在用户选择文件，确认上传后，窗口就会关闭，所以不能用this.getWindow来处理
			NotificationUtil.showWarningNotification(resourceImport.getApplication(), e.getMessage());
			//导入完成后进度条的处理进度条
			pi.setEnabled(false);
			progressLayout.removeAllComponents();
			e.printStackTrace();
			return false;
		}
		progressOuterLayout.removeComponent(progressLayout);
		
//		Notification notification=new Notification(importMessage, Notification.TYPE_HUMANIZED_MESSAGE);
//		notification.setDelayMsec(Notification.DELAY_FOREVER);
//		notification.setPosition(Notification.POSITION_CENTERED_TOP);
		
		////chenhb 20140728
		ArrayList<String> noteContentList=new ArrayList<String>();
		noteContentList.add(importMessage);
		NoticeWindow noticeWindow=new NoticeWindow("追加结果", noteContentList);
		resourceImport.getApplication().getMainWindow().addWindow(noticeWindow);
		logger.info("jinht -->> ("+batch.getId()+")"+batch.getBatchName()+"追加资源成功: " + importMessage);
		return true;
	}

	/**
	 * 点击导入按钮后的操作
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == cancel)
			this.getParent().removeWindow(this);
	}

//	jrh 不去重、去重导入可选
//	public void setIgnoreReduplicate(boolean ignoreReduplicate) {
//		this.ignoreReduplicate = ignoreReduplicate;
//	}
	
}
