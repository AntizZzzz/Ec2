package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.BatchStatus;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.mgr.DistributeResourceToProjecService;
import com.jiangyifen.ec2.service.mgr.ImportResourceService;
import com.jiangyifen.ec2.service.mgr.impl.ImportResourceServiceImpl;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.ConfigProperty;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 创建导入新资源（Upload）
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class ImportResourceWindow extends Window implements Button.ClickListener,TextChangeListener,ValueChangeListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * 主要组件区域
	 */
	// 导入新批次组件
	private OptionGroup operateType_og;
	private HorizontalLayout batchSelect_hlo;
	private ComboBox batchSelect_cb;
	private HorizontalLayout batchNameLayout;
	private TextField batchName;
	private TextArea note;

	// 导入指定路径视图组件
	private HorizontalLayout importBatchLayout;
	private Upload upload;
	private Button saveEmptyBatch;		// jrh 创建一个没有导入资源的空批次
	private Button cancel;
	
	//进度条组件
	private VerticalLayout progressOuterLayout;
	private HorizontalLayout progressLayout;
	private ProgressIndicator pi;
	
	/**
	 * Service
	 */
	private Domain domain;
	private User user;
//	jrh 不去重、去重导入可选
//	private boolean ignoreReduplicate;
	private BeanItemContainer<CustomerResourceBatch> batchOptionsContainer;
	private ImportResourceService importResourceService;
	private DistributeResourceToProjecService distributeResourceToProjecService;
	private CommonService commonService;
	
	/**
	 * 其他组件
	 */
	// 上传的文件
	private File excelFile;
	//父组件的引用
	private ProjectControl projectControl;

	/**
	 * 构造器
	 * @param resourceImport
	 *            资源导入视图的引用
	 */
	public ImportResourceWindow(ProjectControl projectControl) {
		this.initService();
		this.center();
		this.setModal(true);
		this.setSizeUndefined();
		this.setResizable(false);
		this.setCaption("导入/追加资源");
		this.projectControl = projectControl;

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		this.setContent(windowContent);
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);

		// 操作类型
		windowContent.addComponent(buildOperateTypeLayout());
		
		// 批次选择
		windowContent.addComponent(buildBatchSelectLayout());
		
		// 批次名称
		windowContent.addComponent(buildBatchNameLayout());
		// 描述信息
		windowContent.addComponent(buildNoteLayout());
		//创建进度条组件
		progressOuterLayout=projectControl.getProgressLayout();
		progressLayout=new HorizontalLayout();
		progressOuterLayout.addComponent(progressLayout);
//		windowContent.addComponent(progressLayout);
		
		pi = new ProgressIndicator();
		pi.setEnabled(false);
		pi.setPollingInterval(1000);
		// 上传输出
		HorizontalLayout uploadLayout = buildUploadLayout();
		windowContent.addComponent(uploadLayout);
		windowContent.setComponentAlignment(uploadLayout,Alignment.BOTTOM_RIGHT);
	}
	
	/**
	 * 初始化此类中用到的Service
	 */
	private void initService() {
		user=SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		batchOptionsContainer = new BeanItemContainer<CustomerResourceBatch>(CustomerResourceBatch.class);
		
		importResourceService = SpringContextHolder.getBean("importResourceService");
		distributeResourceToProjecService = SpringContextHolder.getBean("distributeResourceToProjecService");
		commonService=SpringContextHolder.getBean("commonService");
	}
	
	/**
	 * jrh 操作类型的组件【导入、追加】
	 * 
	 * @return
	 */
	private HorizontalLayout buildOperateTypeLayout() {
		HorizontalLayout operateType_hlo = new HorizontalLayout();
		this.addComponent(operateType_hlo);
		
		Label operateType_lb = new Label("操作类型：");
		operateType_lb.setWidth("-1px");
		operateType_hlo.addComponent(operateType_lb);
		
		operateType_og = new OptionGroup();
		operateType_og.addListener((ValueChangeListener) this);
		operateType_og.addItem("import");
		operateType_og.addItem("append");
		operateType_og.setItemCaption("import", "导入");
		operateType_og.setItemCaption("append", "追加");
		operateType_og.setWidth("200px");
		operateType_og.setImmediate(true);
		operateType_og.setStyleName("twocol200");
		operateType_hlo.addComponent(operateType_og);
		
		return operateType_hlo;
	}

	/**
	 * jrh 当管理员选择追加资源，并且当前项目已经有与之关联的批次时，显示批次选择组件
	 * 
	 * @return
	 */
	private HorizontalLayout buildBatchSelectLayout() {
		batchSelect_hlo = new HorizontalLayout();
		this.addComponent(batchSelect_hlo);
		
		Label batchSelect_lb = new Label("选择批次：");
		batchSelect_lb.setWidth("-1px");
		batchSelect_hlo.addComponent(batchSelect_lb);
		
		batchSelect_cb = new ComboBox();
		batchSelect_cb.setWidth("200px");
		batchSelect_cb.setNullSelectionAllowed(false);
		batchSelect_cb.setContainerDataSource(batchOptionsContainer);
		batchSelect_hlo.addComponent(batchSelect_cb);
		
		return batchSelect_hlo;
	}

	/**
	 * 导入批次的名称
	 * 
	 * @return
	 */
	private HorizontalLayout buildBatchNameLayout() {
		batchNameLayout = new HorizontalLayout();
		this.addComponent(batchNameLayout);

		batchNameLayout.addComponent(new Label("批次名称："));
		batchName = new TextField();
		batchName.addListener((TextChangeListener) this);
		batchName.setImmediate(true);
		batchName.setWriteThrough(true);
		batchName.setInputPrompt("批次名称");
		batchName.setWidth("200px");
		batchNameLayout.addComponent(batchName);
		return batchNameLayout;
	}

	/**
	 * 描述信息的输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildNoteLayout() {
		HorizontalLayout noteLayout = new HorizontalLayout();
		noteLayout.addComponent(new Label("备注信息："));
		note = new TextArea();
		note.setWidth("200px");
		note.setRows(3);
		note.setWordwrap(true);
		note.setInputPrompt("请输入与批次相关的备注信息！");
		noteLayout.addComponent(note);
		return noteLayout;
	}

	/**
	 * 上传文件组件输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildUploadLayout() {
		importBatchLayout = new HorizontalLayout();
		importBatchLayout.setSpacing(true);

		// 选择上传文件按钮
		upload = new Upload();
		upload.setImmediate(true);
		upload.setButtonCaption("导入并保存");
		upload.setEnabled(false);
		this.setUploadListener();
		this.assignReceiverForUpload(upload);
		importBatchLayout.addComponent(upload);
		
		// 取消按钮
		saveEmptyBatch= new Button("保存空批次", this);
		importBatchLayout.addComponent(saveEmptyBatch);

		// 取消按钮
		cancel= new Button("取消", this);
		importBatchLayout.addComponent(cancel);
		return importBatchLayout;
	}

	/**
	 * 为Upload组件设置多个监听器
	 */
	private void setUploadListener() {
		upload.addListener(new Upload.SucceededListener() {
			public void uploadSucceeded(SucceededEvent event) {
				WebApplicationContext context = (WebApplicationContext) ImportResourceWindow.this.getApplication().getContext();
				final String ip = context.getBrowser().getAddress();
				new Thread(){
					@Override
					public void run() {
						Long startTime=System.currentTimeMillis();
						executeImport(ip);
						Long endTime=System.currentTimeMillis();
						logger.info("导入数据耗时:"+(endTime-startTime)/1000+"秒");
					}
				}.start();
				//Excel 上传完毕移除窗口
				ImportResourceWindow.this.getParent().removeWindow(ImportResourceWindow.this);
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
				String userName = user.getUsername();
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
						logger.error("创建文件失败:"+filename,e);
						throw new RuntimeException("无法再指定位置创建新Excel文件！");
					}
				} else {
					throw new RuntimeException("Excel文件已经存在，请重新创建！");
				}
				try {
					fos = new FileOutputStream(excelFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();//应该不会出现
				}
				return fos;
			}
		});
	}

	@Override
	public void attach() {
		super.attach();
		batchName.setValue("");
		note.setValue("");
		upload.setEnabled(false);
		saveEmptyBatch.setEnabled(true);
		cancel.setEnabled(true);
		operateType_og.setValue("import");
		
//		progressLayout=new HorizontalLayout();
//		pi = new ProgressIndicator();
//		pi.setEnabled(false);
//		pi.setPollingInterval(1000);
//		progressOuterLayout.addComponent(progressLayout);
	}

	/**
	 * 执行导入数据库操作
	 * 导入成功返回true，导入失败返回false
	 */
	public Boolean executeImport(String ip) {
		//初始化进度条
		pi.setEnabled(true);
		pi.setValue(0f);
		progressLayout.addComponent(new Label("导入进度:"));
		progressLayout.addComponent(pi);
		upload.setEnabled(false);
		cancel.setEnabled(false);
		
		// 备注
		String noteStr = "";
		if (note.getValue() != null) {
			noteStr = note.getValue().toString().trim();
		}

		CustomerResourceBatch batch = null;
		String type = (String) operateType_og.getValue();
		/*String logType = "导入资源到新批次-->";*/
		boolean isAppend = false;
		if("import".equals(type)) {	//创建一个新的批次
			String batchNameStr = batchName.getValue().toString().trim();
			batch = new CustomerResourceBatch();
			batch.setBatchName(batchNameStr);
			batch.setNote(noteStr);
			batch.setUser(user);
			batch.setBatchStatus(BatchStatus.USEABLE);
			batch.setDomain(domain);
			batch.setCreateDate(new Date());
			batch=(CustomerResourceBatch)commonService.update(batch);
		} else {
			batch = (CustomerResourceBatch) batchSelect_cb.getValue();
			batch.setNote(noteStr);
			/*logType = "追加资源到老批次-->";*/
			isAppend = true;
		}
		
		//调用导入数据的方法导入数据，更新表格信息
		String importMessage="";
		try {
			
			OperationLogUtil.simpleLog(user, "项目控制-导入/追加资源："+batch.getBatchName());
			
			//isAppend是否是追加资源
			Map<String, Long> importDataResult = importResourceService.importData(excelFile, batch,user,isAppend,pi,true);
			
			if(!isAppend) {		// 如果是导入资源到新建的批次，则还需要为项目分配资源，以及创建批次与项目的关联
				//按照批次给项目指派资源
				List<CustomerResourceBatch> toAddBatches=new ArrayList<CustomerResourceBatch>();
				toAddBatches.add(batch);
				MarketingProject marketingProject=projectControl.getCurrentSelect();
				distributeResourceToProjecService.assignProjectResourceByBatch(marketingProject, toAddBatches, domain,pi);
				
				//添加项目批次的关联关系
				Set<CustomerResourceBatch>  associateBatches=marketingProject.getBatches();
				associateBatches.add(batch);
				marketingProject=(MarketingProject)commonService.update(marketingProject);
			}
			
			//显示的数据信息
			Long successNum=importDataResult.get(ImportResourceServiceImpl.IMPORT_SUCCESS);
			Long hasResourceUpdateNum=importDataResult.get(ImportResourceServiceImpl.HAS_RESOURCE_UPDATE);
			Long hasCustomerResourceNum=importDataResult.get(ImportResourceServiceImpl.HAS_CUSTOMER_IGNORE);
			Long invalidNum=importDataResult.get(ImportResourceServiceImpl.INVALID_NUMBER);
			Long elapsedTime = importDataResult.get(ImportResourceServiceImpl.ELAPSED_TIME);
			importMessage="成功导入"+successNum+"条,</br>";
//			if(ignoreReduplicate == false) {
				importMessage+="更新数据"+hasResourceUpdateNum+"条,</br>";
				importMessage+="客户资源"+hasCustomerResourceNum+"条,</br>";
//			}
			importMessage+="无效号码"+invalidNum+"条,</br>";
			importMessage+="总共耗时"+elapsedTime+"秒.";
			//导入完成后进度条的处理进度条
			pi.setEnabled(false);
			progressLayout.removeAllComponents();
			projectControl.updateProjectResourceInfo();
		} catch (Exception e) {
			//此处应该合理处理提示信息
			// jrh 注意，下面只能用resourceImport 来获取主窗口，因为在用户选择文件，确认上传后，窗口就会关闭，所以不能用this.getWindow来处理
			projectControl.getApplication().getMainWindow().showNotification(e.getMessage(), Notification.TYPE_WARNING_MESSAGE);
			//导入完成后进度条的处理进度条
			pi.setEnabled(false);
			progressLayout.removeAllComponents();
			NotificationUtil.showWarningNotification(this, e.getMessage());
			e.printStackTrace();
			return false;
		}
		progressOuterLayout.removeComponent(progressLayout);
		NotificationUtil.showWarningNotification(ImportResourceWindow.this.getApplication(), importMessage);
		logger.info("jinht -->> ("+batch.getId()+")"+batch.getBatchName()+"导入资源成功: " + importMessage);
		return true;
	}

	/**
	 * jrh
	 * 保存一个没有关联资源的空批次
	 */
	private void executeSaveEmptyBatch() {
		// 批次名
		String batchNameStr = StringUtils.trimToEmpty((String) batchName.getValue());
		if("".equals(batchNameStr)) {
			this.getApplication().getMainWindow().showNotification("批次名称不能为空！");
			return;
		}
		
		// 备注
		String noteStr = StringUtils.trimToEmpty((String) note.getValue());
		
		//创建一个空批次
		CustomerResourceBatch batch=new CustomerResourceBatch();
		batch.setBatchName(batchNameStr);
		batch.setNote(noteStr);
		batch.setUser(user);
		batch.setDomain(domain);
		batch.setBatchStatus(BatchStatus.USEABLE);
		batch.setCreateDate(new Date());
		batch.setCount(0L);
		commonService.update(batch);
		
		this.getParent().removeWindow(this);
	}

	/**
	 * 点击导入按钮后的操作
	 * 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == cancel) {
			this.getParent().removeWindow(this);
		} else if(source == saveEmptyBatch) {
			executeSaveEmptyBatch();
		}
	}
	
	/**
	 * 当批次名称框的内容改变时触发
	 * @param event
	 */
	@Override
	public void textChange(TextChangeEvent event) {
		if(!event.getText().equals("")){
			batchName.setValue(event.getText());
			upload.setEnabled(true);
		}else{
			batchName.setValue("");
			upload.setEnabled(false);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == operateType_og) {
			String type = (String) operateType_og.getValue();
			if("import".equals(type)) {
				batchSelect_hlo.setVisible(false);
				batchNameLayout.setVisible(true);
				saveEmptyBatch.setVisible(true);
				upload.setEnabled(false);
				upload.setButtonCaption("导入并保存");
			} else {
				MarketingProject marketingProject = projectControl.getCurrentSelect();
				Set<CustomerResourceBatch> resourceBatchs = marketingProject.getBatches();
				if(resourceBatchs.size() == 0) {
					this.getApplication().getMainWindow().showNotification("对不起，当前项目没有与之关联的批次，只能直接导入新资源", Notification.TYPE_WARNING_MESSAGE);
					operateType_og.setValue("import");
				} else {
					upload.setButtonCaption("追加并保存");
					upload.setEnabled(true);
					batchSelect_hlo.setVisible(true); 
					batchNameLayout.setVisible(false);
					saveEmptyBatch.setVisible(false);
					
					batchOptionsContainer.removeAllItems();
					batchOptionsContainer.addAll(resourceBatchs);
					for(CustomerResourceBatch batch : resourceBatchs) {
						batchSelect_cb.setValue(batch); break;
					}
				}
			}
		}
	}
	
//	jrh
//	public void setIgnoreReduplicate(boolean ignoreReduplicate) {
//		this.ignoreReduplicate = ignoreReduplicate;
//	}
	
}
