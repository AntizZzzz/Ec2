package com.jiangyifen.ec2.ui.mgr.workorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.WorkOrder;
import com.jiangyifen.ec2.entity.WorkOrderFile;
import com.jiangyifen.ec2.entity.WorkOrderPriority;
import com.jiangyifen.ec2.entity.WorkOrderType;
 import com.jiangyifen.ec2.service.eaoservice.FileTypeService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.service.eaoservice.WorkOrderPriorityService;
import com.jiangyifen.ec2.service.eaoservice.WorkOrderService;
import com.jiangyifen.ec2.service.eaoservice.WorkOrderTypeService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.ui.mgr.tabsheet.WorkOrderManagement;
import com.jiangyifen.ec2.ui.mgr.workorder.pojo.OrderFilesPojo;
import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
 
public class AddWorkOrderWindow extends Window implements ClickListener, ValueChangeListener{
	//---------------------------页面初始化开始---------------------------------------
	//静态
	private static final long serialVersionUID = -1400372952758952826L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	private final Object[] COL_PROPERTIES = new Object[] {"fileName"};
	
	//持有UI对象
	private WorkOrderManagement workOrderManagement;
	//本控件持有对象
	private BeanItemContainer<OrderFilesPojo> orderFilesPojoContainer;
	private BeanItemContainer<MarketingProject> marketingProjectContainer;
	private BeanItemContainer<User> userContainer;
	private BeanItemContainer<WorkOrderType> typeContainer;
	private BeanItemContainer<WorkOrderPriority> priorityContainer;
 
	//页面布局组件
	  
	private	VerticalLayout windowContent;
	private GridLayout 	formLayout;
	private HorizontalLayout buttonsLayout;
	
	//页面控件
	private Label lb_project;
	private ComboBox cb_project;
	private Label lb_type;
	private ComboBox cb_type;
	private Label lb_title;
	private TextField tf_title;
	private Label lb_priority;
	private ComboBox cb_priority;
	private Label lb_due_time;
	private PopupDateField df_due_time;
	private Label lb_h_user;
	private ComboBox cb_h_user;
	private Label lb_c_user;
	private Label lb_t_c_user;
	private Label lb_content;
	private TextArea ta_content;
	private Label lb_labels;
	private TextField tf_labels;
	private Table tb_files_table = new Table();
	private Upload ud_files;
 	private Button bt_delete_files;
 	private Label lb_msg = new Label("", Label.CONTENT_XHTML);
	private Button bt_save_workorder;
	private Button bt_cancel;
	
	//业务必须对象
	private Domain domain;
	private User loginUser;
	
	//业务本页对象
			
	//业务注入对象
	private MarketingProjectService marketingProjectService;
	private UserService userService;
	private WorkOrderService workOrderService;
	private WorkOrderTypeService workOrderTypeService;
 	private WorkOrderPriorityService workOrderPriorityService;
	private FileTypeService fileTypeService;
	
	public AddWorkOrderWindow(WorkOrderManagement workOrderManagement){
		this.workOrderManagement = workOrderManagement;
		initPath();
		initThis();
		initSpringContext();
		initCompanent();
	}
	/**初始化本控件整体属性 */
	private void initThis() {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		
		this.setCaption("添加工单!");
		this.setWidth("600px");
		this.setHeight("480px");
	}
	/**初始化Spring上写相关系想你 */
	private void initSpringContext(){
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		userService = SpringContextHolder.getBean("userService");
		workOrderService = SpringContextHolder.getBean("workOrderService");
		workOrderTypeService = SpringContextHolder.getBean("workOrderTypeService");
		//workOrderStatusService = SpringContextHolder.getBean("workOrderStatusService");
		workOrderPriorityService = SpringContextHolder.getBean("workOrderPriorityService");
		fileTypeService = SpringContextHolder.getBean("fileTypeService");
	}
	/**初始化控件布局和组件 */
	private void initCompanent() {
		buildWindowContent();	//主窗体层
		buildFormLayout();		//中间Form层
		buildBottomLayout();	//底部按钮层
		windowContent.addComponent(formLayout);
		windowContent.addComponent(buttonsLayout);
		this.addComponent(windowContent);
	}
	/**主窗体层 */
	private void buildWindowContent(){
		windowContent = new VerticalLayout();
		windowContent.setSpacing(true);
		windowContent.setWidth("100%");
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
	}
	/**中间Form层 */
	private void buildFormLayout(){
		formLayout = new GridLayout(3,10);
		formLayout.setSpacing(true);
		
		lb_project = new Label("所属项目");
		cb_project = new ComboBox();
		cb_project.setWidth("460px");
		cb_project.setNullSelectionAllowed(false);
 		List<MarketingProject>  marketingProjectList = marketingProjectService.getAll(domain);
		marketingProjectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		marketingProjectContainer.addAll(marketingProjectList);
		cb_project.setContainerDataSource(marketingProjectContainer);
		cb_project.setItemCaptionPropertyId("projectName");
		
		formLayout.addComponent(lb_project);
 		formLayout.addComponent(cb_project);
 		formLayout.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
		
 		lb_type = new Label("工单类型");
		cb_type = new ComboBox();
		cb_type.setWidth("460px");
		cb_type.setNullSelectionAllowed(false);
		List<WorkOrderType> workorderTypeList = workOrderTypeService.getAllByDomain(domain);
		typeContainer = new BeanItemContainer<WorkOrderType>(WorkOrderType.class);
		typeContainer.addAll(workorderTypeList);
		cb_type.setContainerDataSource(typeContainer);
		cb_type.setItemCaptionPropertyId("name");
		
		formLayout.addComponent(lb_type);
 		formLayout.addComponent(cb_type);
 		formLayout.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
 		
 		lb_title = new Label("工单标题");
		tf_title = new TextField();
		tf_title.setWidth("460px");
		tf_title.setMaxLength(200);
 		tf_title.setInputPrompt("请填写工单标题");
 		formLayout.addComponent(lb_title);
 		formLayout.addComponent(tf_title);
 		formLayout.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
 		
 		
 		lb_priority = new Label("工单优先级");
		cb_priority = new ComboBox();
		cb_priority.setWidth("460px");
		cb_priority.setNullSelectionAllowed(false);
		List<WorkOrderPriority> workOrderPriorityList = workOrderPriorityService.getAllByDomain(domain);
		priorityContainer = new BeanItemContainer<WorkOrderPriority>(WorkOrderPriority.class);
		priorityContainer.addAll(workOrderPriorityList);
		cb_priority.setContainerDataSource(priorityContainer);
		cb_priority.setItemCaptionPropertyId("name");
		formLayout.addComponent(lb_priority);
 		formLayout.addComponent(cb_priority);
 		formLayout.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
 		
 		lb_due_time = new Label("预计日期");
		df_due_time = new PopupDateField();
		df_due_time.setDateFormat("yyyy-MM-dd HH:mm");
 		formLayout.addComponent(lb_due_time);
 		formLayout.addComponent(df_due_time);
 		formLayout.addComponent(new Label("<font color='red'></font>",Label.CONTENT_XHTML));

 		lb_h_user = new Label("处理人");
		cb_h_user = new ComboBox();
		cb_h_user.setWidth("460px");
		cb_h_user.setNullSelectionAllowed(false);
		List<User> userList = userService.getAllByDomain(domain);
		userContainer = new BeanItemContainer<User>(User.class);
		userContainer.addAll(userList);
		cb_h_user.setContainerDataSource(userContainer);
		cb_h_user.setItemCaptionPropertyId("username");
		formLayout.addComponent(lb_h_user);
 		formLayout.addComponent(cb_h_user);
 		formLayout.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
 		
 		lb_c_user = new Label("创建人");
 		lb_t_c_user = new Label("ADMIN");
 		lb_t_c_user.setWidth("460px");
		formLayout.addComponent(lb_c_user);
 		formLayout.addComponent(lb_t_c_user);
 		formLayout.addComponent(new Label("<font color='red'></font>",Label.CONTENT_XHTML));
 		
 		lb_content = new Label("工单内容");
		ta_content = new TextArea();
		ta_content.setWidth("460px");
		ta_content.setHeight("70px");
		ta_content.setMaxLength(2000);
 		ta_content.setInputPrompt("请填写工单内容");
 		formLayout.addComponent(lb_content);
 		formLayout.addComponent(ta_content);
 		formLayout.addComponent(new Label("<font color='red'></font>",Label.CONTENT_XHTML));
 		
 		lb_labels = new Label("工单标签");
		tf_labels = new TextField();
		tf_labels.setWidth("460px");
		tf_labels.setMaxLength(200);
		tf_labels.setInputPrompt("标签以逗号分割");
		tf_labels.addListener(new TextChangeListener(){
			private static final long serialVersionUID = 4532308236663593010L;
			@Override
			public void textChange(TextChangeEvent event) {
				event.getText();
				
			}
			
		});
		formLayout.addComponent(lb_labels);
 		formLayout.addComponent(tf_labels);
 		formLayout.addComponent(new Label("<font color='red'></font>",Label.CONTENT_XHTML));
 	 
 		HorizontalLayout h_table = new HorizontalLayout();
 		tb_files_table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
  		tb_files_table.setWidth("405px");
 		tb_files_table.setHeight("40px");
		tb_files_table.setSelectable(true);
		tb_files_table.setImmediate(true);
		tb_files_table.addListener((Property.ValueChangeListener) this);
		orderFilesPojoContainer = new BeanItemContainer<OrderFilesPojo>(OrderFilesPojo.class);
		orderFilesPojoContainer.removeAllItems();
  		tb_files_table.setContainerDataSource(orderFilesPojoContainer);
		tb_files_table.setVisibleColumns(COL_PROPERTIES);
		h_table.addComponent(tb_files_table);
		
		bt_delete_files = new Button("删除",this);
		bt_delete_files.setHeight("40px");
		bt_delete_files.setImmediate(true);
		bt_delete_files.setEnabled(false);
		h_table.addComponent(bt_delete_files);
		
		ud_files = new Upload();
		ud_files.setImmediate(true);
		ud_files.setButtonCaption("浏览文件");
		this.setUploadListener();
 		this.assignReceiverForUpload(ud_files);
		
		//bt_files_browse = new Button("浏览文件");
 		formLayout.addComponent(ud_files);
 		formLayout.addComponent(h_table);
 		formLayout.addComponent(new Label("<font color='red'></font>",Label.CONTENT_XHTML));
	}
	
	private File uploadFile;
	private String path;
	private String filePath;
	//private String rtnType = "";
	private String rtnFileName = "";
	
	private void setUploadListener() {
		ud_files.addListener(new Upload.StartedListener() {
			private static final long serialVersionUID = 5597451872163671291L;
			@Override
			public void uploadStarted(StartedEvent event) {
				lb_msg.setValue("<font color='green'>文件长传中...</font>");
			}
		});
		
		ud_files.addListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = 860586921493472076L;
			public void uploadSucceeded(SucceededEvent event) {
				if ((null != rtnFileName) && (rtnFileName.trim().length() > 0)) {
					lb_msg.setValue("<font color='green'>文件长传成功!..</font>");
					OrderFilesPojo r = new OrderFilesPojo();
					r.setFileName(rtnFileName);
					orderFilesPojoContainer.addItem(r);
					tb_files_table.setContainerDataSource(orderFilesPojoContainer);
					rtnFileName = "";
				}
			}
		});
		
	}
	
	/**
	 * 由 buildPathLayout 调用，指定上传的Excel文件的存储名称和位置
	 */
 
	@SuppressWarnings("serial")
	private void assignReceiverForUpload(final Upload upload) {
		upload.setReceiver(new Upload.Receiver() {
			private OutputStream fos = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					// 其实这样做没有对上传的文件做任何处理
				}
			};

			private OutputStream fosNull = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					// 其实这样做没有对上传的文件做任何处理
				}
			};
			
			public OutputStream receiveUpload(String filename, String mimeType) {
				String type = "";
				if ((null != filename) && (filename.trim().length() > 0)) {
					type = filename.substring(filename.lastIndexOf("."));
					type = type.toUpperCase();
					if(!fileTypeService.validateFileType(type)){//不存在该文件类型不可以上传
						lb_msg.setValue("<font color='red'>文件类型不在允许范围内!</font>");
						return fosNull;
					}
				}else{
					lb_msg.setValue("<font color='red'>文件后缀不能为空!</font>");
					return fosNull;
				}
				
				filePath =  path + "/" + domain.getId() + "/workorder/" + filename ;
				uploadFile = new File(filePath);
			 
				rtnFileName = filename;
				//rtnType = type;
				
				if (!uploadFile.exists()) {
					if (!uploadFile.getParentFile().exists()) {
						uploadFile.getParentFile().mkdirs();
					}
					try {
						uploadFile.createNewFile();
					} catch (IOException e) {
						logger.error("创建文件失败:" + path, e);
						throw new RuntimeException("无法再指定位置创建新文件！");
					}
				} else {
					try {
						uploadFile.delete();
					} catch (Exception e) {
						logger.error("文件被替换:" + filePath, e);
					}
				}
				try {
					fos = new FileOutputStream(uploadFile);
				} catch (FileNotFoundException e) {
					e.printStackTrace();// 应该不会出现
				}
				return fos;
			}

		});

	}
	
	/**底部按钮层 */
	private void buildBottomLayout(){
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		
		bt_save_workorder = new Button("保存工单",this);
		bt_save_workorder.setStyleName("default");
		buttonsLayout.addComponent(bt_save_workorder);
		
		bt_cancel = new Button("取消",this);
		buttonsLayout.addComponent(bt_cancel);
		
		buttonsLayout.addComponent(lb_msg);
	}
 	//---------------------------页面初始化结束---------------------------------------
	
	//---------------------------综合其他开始---------------------------------------
	public void refreshCompanent(WorkOrderManagement workOrderManagement){//回复控件原始状态
		this.workOrderManagement = workOrderManagement;
		this.cb_project.setValue(null);
		this.cb_type.setValue(null);
		this.tf_title.setValue("");
		this.cb_priority.setValue(null);
		this.df_due_time.setValue(null);
		this.cb_h_user.setValue(null);
		this.lb_t_c_user.setValue(null);
		this.ta_content.setValue("");
		this.tf_labels.setValue("");
		
		orderFilesPojoContainer.removeAllItems();
		tb_files_table.setContainerDataSource(orderFilesPojoContainer);
		this.lb_msg.setValue("");
		this.lb_t_c_user.setValue(loginUser.getEmpNo());
	}
	//---------------------------综合其他结束---------------------------------------
	//---------------------------页面事件开始---------------------------------------
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == tb_files_table) {
			bt_delete_files.setEnabled(source != null);
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_save_workorder) {
			if(checkWorkOrderForm()){
				excute_SaveWorkOrder();
			}
		}else if(source == bt_cancel){
			 
		 
		}else if(source == bt_delete_files) {
			OrderFilesPojo target = (OrderFilesPojo)tb_files_table.getValue();
			orderFilesPojoContainer.removeItem(target);
			tb_files_table.setContainerDataSource(orderFilesPojoContainer);
		}
	}
	
	private boolean checkWorkOrderForm() {
		if(WorkUIUtils.stringIsEmpty(cb_project.getValue())){
			lb_msg.setValue("<font color='red'>所属项目不能为空!</font>");
			return false;
		}else if(WorkUIUtils.stringIsEmpty(cb_type.getValue())){
			lb_msg.setValue("<font color='red'>工单类型不能为空!</font>");
			return false;
		}else if(WorkUIUtils.stringIsEmpty(tf_title.getValue())){
			lb_msg.setValue("<font color='red'>工单标题不能为空!</font>");
			return false;
		}else if(WorkUIUtils.stringIsEmpty(cb_priority.getValue())){
			lb_msg.setValue("<font color='red'>工单优先级不能为空!</font>");
			return false;
		}else if(WorkUIUtils.stringIsEmpty(cb_h_user.getValue())){
			lb_msg.setValue("<font color='red'>工单处理人不能为空!</font>");
			return false;
		}
		return true;
	}
	
	private void excute_SaveWorkOrder() {
		try {
			  	WorkOrder workOrder = new WorkOrder();
			  	workOrder.setMarketingProject((MarketingProject)cb_project.getValue());
			  	workOrder.setWorkOrderType((WorkOrderType)cb_type.getValue());
			  	workOrder.setTitle(this.tf_title.getValue().toString());
			  	workOrder.setWorkOrderPriority((WorkOrderPriority)cb_priority.getValue());
			  	Date dueTime = getDueTimeValue_NoExP();
			  	if(null != dueTime){
			  		workOrder.setDueTime(dueTime);}
			  	workOrder.setHandleUser((User)cb_h_user.getValue());
			  	workOrder.setCreateUser(loginUser);
			  	workOrder.setContent(ta_content.getValue().toString());
			  	workOrder.setLabels(tf_labels.getValue().toString());
			  	workOrder.setDomain(domain);
			  	
			  	List<WorkOrderFile> wofList = new ArrayList<WorkOrderFile>();
			  	wofList = getWorkOrderFileVlue_NoExP();
			  	workOrderService.saveWorkOrderAndWorkOrderFileList(workOrder,wofList);
			  	workOrderManagement.refreshCompanent();
			  	this.getApplication().getMainWindow().showNotification("保存工单成功!",Notification.TYPE_HUMANIZED_MESSAGE);
			  	this.getApplication().getMainWindow().removeWindow(this);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("保存工单异常_excute_SaveWorkOrder_LLXXYY", e);
			this.getApplication()
					.getMainWindow()
					.showNotification("保存工单异常!",
							Notification.TYPE_ERROR_MESSAGE);
		}
	}
	
	//获得预计日期
	private Date getDueTimeValue_NoExP(){
		Date dt_dut_time = null;
		try {
			Object obj_due_time = df_due_time.getValue();
			if(obj_due_time!=null){
				dt_dut_time = (Date)obj_due_time;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	  	return dt_dut_time;
	}
	
	private List<WorkOrderFile> getWorkOrderFileVlue_NoExP(){
		List<WorkOrderFile> rt = new ArrayList<WorkOrderFile>();
		WorkOrderFile wof = null;
		String nameSuffix = null;
		if(orderFilesPojoContainer.size() > 0){
			for(OrderFilesPojo orderFilesPojo : orderFilesPojoContainer.getItemIds()) {
				wof = new WorkOrderFile();
				wof.setDomain(domain);
				wof.setName(orderFilesPojo.getFileName());
				nameSuffix = getFileSuffix(orderFilesPojo.getFileName());
				if (null != nameSuffix && nameSuffix.trim().length() > 0) {
					wof.setType(nameSuffix.toUpperCase());
				}
				rt.add(wof);
			}
		}
		
		return rt;
	}
	
	private static String getFileSuffix(String fname){
		if (null != fname && fname.trim().length() > 0) {
			int index = fname.lastIndexOf(".");
			if(index > 0){
				fname = fname.substring(index);
			}else{
				fname = "";
			}
		}
		return fname;
	}
	
	public void initPath() {
		path = Config.props.getProperty(Config.WORK_ORDER_PATH);
 	}
	//---------------------------页面事件结束---------------------------------------
	
	 
	
	
	
	
	
	
	
	
	
	 

	/**
	 * 用于自动生成强制挂断列
	 * 	管理员可以强制挂断职员正在进行的通话
	 */
	/*private class ChannelHangupColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			try {
				final WorkOrderFile workOrderFile = (WorkOrderFile) itemId;
				if(columnId.equals("channelHangup")) {
					Button hangup = new Button("删除");
					hangup.setIcon(ResourceDataMgr.hangup_12_ico);
					hangup.addStyleName("borderless");
					hangup.addStyleName(BaseTheme.BUTTON_LINK);
					hangup.setImmediate(true);
					hangup.addListener(new ClickListener() {
						@Override
						public void buttonClick(ClickEvent event) {
							String exten = workOrderFile.getName();
							if(exten != null && !"".equals(exten)) {
								 
							} else {
							 
							}
						}
					});
					return hangup;
				}
			} catch (Exception e) {
				logger.error("附件上传时表格出现异常 --> " + e.getMessage(), e);
			} 
			return null;
		}
	}*/
	
}
