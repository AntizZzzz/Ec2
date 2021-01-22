package com.jiangyifen.ec2.ui.csr.workarea.incoming;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.ProjectCustomer;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.WorkflowTransferLog;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectTaskType;
import com.jiangyifen.ec2.entity.enumtype.QcCsr;
import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.SatisfactionInvestigationService;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceBatchService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.service.eaoservice.ProjectCustomerService;
import com.jiangyifen.ec2.service.eaoservice.TimersService;
import com.jiangyifen.ec2.service.eaoservice.WorkflowTransferLogService;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.ShanXiJiaoTanWorkOrderConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 呼入窗口的客服记录构造器
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class IncomingDialRecordInfoCreator extends VerticalLayout implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Notification notification;					// 提示信息
	
	private OptionGroup answeredResultOptions;			// 任务完成情况单选项(已接通)

	private PopupDateField orderTime_pdf;				// 预约提醒时间
	private TextArea orderNoteArea;						// 提醒内容
	private VerticalLayout orderVLayout;				// 预约组件
	
	private ComboBox customerLevelSelector; 			// “客户等级”选择
	private ComboBox removeCustomerSelector;			// "移除客户"选择框
	private HorizontalLayout removeCustomerLayout;		// 存放"移除客户"选择框及以标题标签的布局管理器
	private TextArea recordContent;						// 职员根据情况添加其他描述信息

	private Button saveClose_button;					// 保存并关闭按钮
	private Button save_button;							// 保存按钮
	private Button close_button;						// 关闭按钮
	private Button satisfictionInvestigationButton;		// 执行满意度调查

	private Domain domain;								// 当前用户所属域
	private User loginUser; 							// 当前登录用户
	private Long departmentId;							// 当前用户所属部门的id
	private String exten;								// 当前用户使用的分机
	
	private CustomerResource customerResource;
	private TabSheet tabSheet;							// 呼入客户信息的TabSheet
	
	private BeanItemContainer<CustomerLevel> customerLevelContainer;				// 客户级别容器
	private BeanItemContainer<CustomerServiceRecordStatus> incomingStatusContainer;	// 呼入任务完成状态（代表已接通）
	private String shanXiJiaoTanPcMac;												// 山西焦炭的服务器Mac 地址【山西焦炭】
	private boolean isMakeOrderForSelf = false;										// 是否偶需要给自己创建预约提醒
	private boolean isMakeOrderForOthers = false;									// 是否需要为下一个工作流话务员创建预约时间【山西焦炭】

	private IncomingDialTabView incomingDialTabView;								// 呼入弹屏的Tab 页
	
	private CustomerServiceRecordStatusService serviceRecordStatusService;			// 呼入记录完成状态服务类
	private CustomerServiceRecordService serviceRecordService;						// 呼入记录服务类
	private CustomerResourceService customerResourceService;						// 客户资源服务类
	private ProjectCustomerService projectCustomerService;							// 项目与客户
	private MarketingProjectService marketingProjectService;						// 项目服务类
	private CustomerLevelService customerLevelService;								// 客户级别服务类
	private SatisfactionInvestigationService satisfactionInvestigationService;		// 客户满意度调查服务类
	private MarketingProjectTaskService projectTaskService;							// 项目任务服务类
	private CustomerResourceBatchService customerResourceBatchService;				// 批次服务类
	private WorkflowTransferLogService workflowTransferLogService;					// 工作流迁移日志服务类
	private TimersService timersService;											// 定时器服务类
	
	public IncomingDialRecordInfoCreator(User loginUser, IncomingDialTabView incomingDialTabView) {
		this.setSpacing(true);
		this.loginUser = loginUser;
		this.incomingDialTabView = incomingDialTabView;
		
		domain = loginUser.getDomain();
		departmentId = loginUser.getDepartment().getId();
		exten = ShareData.userToExten.get(loginUser.getId());

		shanXiJiaoTanPcMac = (String) ShanXiJiaoTanWorkOrderConfig.props.
				get(ShanXiJiaoTanWorkOrderConfig.SANXIJIAOTAN_PC_MAC);
		
		serviceRecordService = SpringContextHolder.getBean("customerServiceRecordService");
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		serviceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		projectCustomerService = SpringContextHolder.getBean("projectCustomerService");
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		customerLevelService = SpringContextHolder.getBean("customerLevelService");
		satisfactionInvestigationService = SpringContextHolder.getBean("satisfactionInvestigationService");
		projectTaskService = SpringContextHolder.getBean("marketingProjectTaskService");
		customerResourceBatchService = SpringContextHolder.getBean("customerResourceBatchService");
		workflowTransferLogService = SpringContextHolder.getBean("workflowTransferLogService");
		timersService = SpringContextHolder.getBean("timersService");

		customerLevelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
		incomingStatusContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 创建主要的相关组件
		createMainComponents();

		// 设置组件的原始状态
		setComponentDefaultValue();
	}
	
	// 创建该界面的主要组件
	private void createMainComponents() {
		answeredResultOptions = new OptionGroup("联系结果：", incomingStatusContainer);
		answeredResultOptions.addStyleName("threecol");			// 按三列排版
		answeredResultOptions.addStyleName("boldcaption");
		answeredResultOptions.addListener(this);
		answeredResultOptions.setImmediate(true);
		this.addComponent(answeredResultOptions);

		// 创建预约组件
		this.createOrderComponents();
		
		// 创建客户级别选择框、移除客户选择框
		this.createCustomerBaseInfoHLayout();
		
		recordContent = new TextArea("联系内容：");
		recordContent.addStyleName("boldcaption");
		recordContent.setWidth("100%");
		recordContent.setHeight("46px");
		this.addComponent(recordContent);

		HorizontalLayout operator_hl = new HorizontalLayout();
		operator_hl.setSpacing(true);
		
		saveClose_button = new Button("保存并关闭", this);
		saveClose_button.setStyleName("default");
		saveClose_button.setImmediate(true);
		operator_hl.addComponent(saveClose_button);
		
		save_button = new Button("保存", this);
		save_button.setImmediate(true);
		operator_hl.addComponent(save_button);
		
		close_button = new Button("关闭", this);
		close_button.setImmediate(true);
		operator_hl.addComponent(close_button);

		// 满意度调查按钮，默认为不可见
		satisfictionInvestigationButton = new Button("满意度调查", this);
		satisfictionInvestigationButton.setImmediate(true);
		satisfictionInvestigationButton.setVisible(false);

		// 存放操作按钮的水平布局管理器
		HorizontalLayout operateButtonHLayout = new HorizontalLayout();
		operateButtonHLayout.setWidth("100%");
		operateButtonHLayout.setSpacing(true);
		operateButtonHLayout.addComponent(operator_hl);
		operateButtonHLayout.addComponent(satisfictionInvestigationButton);
		operateButtonHLayout.setMargin(false, true, false, true);
		operateButtonHLayout.setComponentAlignment(operator_hl, Alignment.MIDDLE_LEFT);
		operateButtonHLayout.setComponentAlignment(satisfictionInvestigationButton, Alignment.MIDDLE_RIGHT);
		this.addComponent(operateButtonHLayout);
	}

	/**
	 *  创建预约组件 
	 */
	private void createOrderComponents() {
		orderVLayout = new VerticalLayout();
		orderVLayout.setSpacing(true);
		orderVLayout.setWidth("100%");
		orderVLayout.setDescription("<B>当前设置的预约是给下一个跟单的话务员使用的！</B>");
		orderVLayout.setVisible(false);
		this.addComponent(orderVLayout);
		
		HorizontalLayout orderTime_hl = new HorizontalLayout();
		orderTime_hl.setSpacing(true);
		orderVLayout.addComponent(orderTime_hl);
		
		Label orderTimeCaption = new Label("<B>预约时间：<B>", Label.CONTENT_XHTML);
		orderTimeCaption.setWidth("-1px");
		orderTime_hl.addComponent(orderTimeCaption);
		orderTime_hl.setComponentAlignment(orderTimeCaption, Alignment.MIDDLE_LEFT);

		orderTime_pdf = new PopupDateField();
		orderTime_pdf.setWidth("180px");
		orderTime_pdf.setInputPrompt("预约时间不能为空！");
		orderTime_pdf.setDescription("<B>预约时间不能为空！</B>");
		orderTime_pdf.setDateFormat("yyyy-MM-dd HH:mm");
		orderTime_pdf.setValidationVisible(false);
		orderTime_pdf.setResolution(PopupDateField.RESOLUTION_MIN);
		orderTime_hl.addComponent(orderTime_pdf);

		HorizontalLayout orderNote_hl = new HorizontalLayout();
		orderNote_hl.setSpacing(true);
		orderNote_hl.setWidth("100%");
		orderVLayout.addComponent(orderNote_hl);
		
		Label orderNoteCaption = new Label("<B>预约内容：<B>", Label.CONTENT_XHTML);
		orderNoteCaption.setWidth("-1px");
		orderNote_hl.addComponent(orderNoteCaption);
		orderNote_hl.setComponentAlignment(orderNoteCaption, Alignment.MIDDLE_LEFT);
		
		orderNoteArea = new TextArea();
		orderNoteArea.setWidth("100%");
		orderNoteArea.setRows(2);
		orderNoteArea.setMaxLength(1024);
		orderNote_hl.addComponent(orderNoteArea);
		orderNote_hl.setExpandRatio(orderNoteArea, 1.0f);
	}

	/**
	 * 创建  存放“客户级别标签和其选择框”，以及“移除客户”组件 的布局管理器
	 */
	private void createCustomerBaseInfoHLayout() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		this.addComponent(layout);
		
		Label levelLabel = new Label("<B>客户级别：<B>", Label.CONTENT_XHTML);
		levelLabel.setWidth("-1px");
		layout.addComponent(levelLabel);
		
		customerLevelSelector = new ComboBox();
		customerLevelSelector.setImmediate(true);
		customerLevelSelector.setWidth("152px");
		customerLevelSelector.setInputPrompt("请选择客户级别");
		customerLevelSelector.setNullSelectionAllowed(false);
		customerLevelSelector.setItemCaptionPropertyId("levelName");
		customerLevelSelector.setContainerDataSource(customerLevelContainer);
		layout.addComponent(customerLevelSelector);

		removeCustomerLayout = new HorizontalLayout();
		removeCustomerLayout.setSpacing(true);
		layout.addComponent(removeCustomerLayout);
		
		Label removeCustomerLabel = new Label("<B>移除客户：<B>", Label.CONTENT_XHTML);
		removeCustomerLabel.setDescription("<B>是否将已经与自己成交的客户从‘我的客户’中移除</B>");
		removeCustomerLabel.setWidth("-1px");
		removeCustomerLayout.addComponent(removeCustomerLabel);
		
		removeCustomerSelector = new ComboBox();
		removeCustomerSelector.addItem(false);
		removeCustomerSelector.addItem(true);
		removeCustomerSelector.setItemCaption(false, "否");
		removeCustomerSelector.setItemCaption(true, "是");
		removeCustomerSelector.setImmediate(true);
		removeCustomerSelector.setWidth("100px");
		removeCustomerSelector.setDescription("<B>是否将已经与自己成交的客户从‘我的客户’中移除</B>");
		removeCustomerSelector.setNullSelectionAllowed(false);
		removeCustomerLayout.addComponent(removeCustomerSelector);
	}

	private void setComponentDefaultValue() {
		answeredResultOptions.setValue(null);
		recordContent.setValue("");
		orderNoteArea.setValue("");
		orderTime_pdf.setValue(null);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == answeredResultOptions) {
			CustomerServiceRecordStatus serviceRecordStatus = (CustomerServiceRecordStatus) answeredResultOptions.getValue();
			if(serviceRecordStatus != null) {
				if(serviceRecordStatus.getStatusName().contains("预约")) {
					isMakeOrderForSelf = true;
					isMakeOrderForOthers = false;
					orderVLayout.setVisible(true);
				} else if(GlobalData.MAC_ADDRESS.equals(shanXiJiaoTanPcMac)){	// 【该判定是为山西焦炭而作】
					String statusName = serviceRecordStatus.getStatusName();
					// 获取需要扭转到工作流2的状态
					ArrayList<String> transfer2Order2Names = new ArrayList<String>();
					String transfer_to_work_order2 = (String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.TRANSFER_TO_WORK_ORDER2);
					for(String singleCondition1 : transfer_to_work_order2.split(",")) {
						transfer2Order2Names.add(singleCondition1);
					}
					
					// 获取需要扭转到工作流3的状态
					ArrayList<String> transfer2Order3Names = new ArrayList<String>();
					String transfer_to_work_order3 = (String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.TRANSFER_TO_WORK_ORDER3);
					for(String singleCondition2 : transfer_to_work_order3.split(",")) {
						transfer2Order2Names.add(singleCondition2);
					}
					if(transfer2Order2Names.contains(statusName) || transfer2Order3Names.contains(statusName)) {
						isMakeOrderForSelf = false;
						isMakeOrderForOthers = true;
						orderVLayout.setVisible(true);
					} else {
						isMakeOrderForSelf = false;
						isMakeOrderForOthers = false;
						orderVLayout.setVisible(false);
					}
				} else {
					isMakeOrderForSelf = false;
					isMakeOrderForOthers = false;
					orderVLayout.setVisible(false);
				}
			}
		} 
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button sourceButton = event.getButton();
		if(sourceButton == saveClose_button) {
			boolean success = saveChanges();
			if(success) { // 如果成功，移除窗口
				executeClose();
			}
		} else if(sourceButton == save_button) {
			saveChanges();
		} else if(sourceButton == close_button) {
			executeClose();
		} else if(sourceButton == satisfictionInvestigationButton) {
			String result = satisfactionInvestigationService.investigation(loginUser.getId(), "incoming");
			if("success".equals(result)) {
				notification.setCaption("满意度调查转接成功！");
			} else if("unbridge".equals(result)) {
				notification.setCaption("<font color='red'><B>您当前还没有建立通话，请与客户建立通话后再做满意度调查！</B></font>");
			} else if("fail".equals(result)) {
				notification.setCaption("<font color='red'><B>对不起，调查失败</B></font>");
			}
			this.getApplication().getMainWindow().showNotification(notification);
		}
	}

	/**
	 * 保存记录
	 */
	private boolean saveChanges() {
		// 如果客户基础信息处于编辑状态，则先保存客户基础信息，然后再创建后续记录
		boolean isSaveResourceSuccess = incomingDialTabView.checkAndSaveCustomerResourceInfo();
		if(!isSaveResourceSuccess) {
			this.getApplication().getMainWindow().showNotification("客户基础信息填写有误，保存失败！", Notification.TYPE_WARNING_MESSAGE);
			return false;
		}
//		if(customerResource.getId() == null) {
//			notification.setCaption("<font color='red'><B>请先保存客户资源！</B></font>");
//			this.getApplication().getMainWindow().showNotification(notification);
//			return false;
//		}
		
		CustomerServiceRecordStatus serviceRecordStatus = (CustomerServiceRecordStatus) answeredResultOptions.getValue();
		if(serviceRecordStatus == null) {
			notification.setCaption("<font color='red'><B>外呼结果不能为空！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return false;
		}

		if(orderVLayout.isVisible()) {
			if(!orderTime_pdf.isValid()) {
				notification.setCaption("<font color='red'><B>预约时间格式错误！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			} else if(orderTime_pdf.getValue() == null) {
				notification.setCaption("<font color='red'><B>预约时间不能为空！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			} else {
				Date now = new Date();
				Date specifiedTime = (Date) orderTime_pdf.getValue();
				if(now.after(specifiedTime)) {
					notification.setCaption("<font color='red'><B>预约时间不能小于当前时间！</B></font>");
					this.getApplication().getMainWindow().showNotification(notification);
					return false;
				}
			}
		}

		// 预约信息
		Date orderTime = null;
		String orderNote = null;
		
		try {
			// 当前用户可能没有项目
			MarketingProject currentProject = null;
			IncomingDialInfo incomingDialInfo = incomingDialTabView.getIncomingDialInfo();
			if(incomingDialInfo != null && !"".equals(incomingDialInfo.getVasOutline())) {		// 优先根据外线判断项目	20140711
				if(incomingDialInfo.getProjectId() != null) {
					currentProject = marketingProjectService.get(incomingDialInfo.getProjectId());
				} else {
					currentProject = marketingProjectService.getAllByOutlineName(incomingDialInfo.getVasOutline(), domain.getId());
				}
			} 

			if(currentProject == null) {	// 如果根据外线找不到项目，则按坐席当前工作的项目处理
				Long projectId = ShareData.extenToProject.get(exten); 
				if(projectId != null) {
					currentProject = marketingProjectService.get(projectId);
				}
			}

			// 判断是否需要给自己创建预约时间
			if(orderVLayout.isVisible()) {
				orderTime = (Date) orderTime_pdf.getValue();
				orderNote = StringUtils.trimToEmpty((String) orderNoteArea.getValue());
				if(isMakeOrderForSelf) { // 为自己创建定时提醒
					if("".equals(orderNote)) {
						orderNote = "用户名："+loginUser.getUsername()+"您好，您有一个预约客户需要联系";
					}
					createNewTimeNotices(orderTime, orderNote);
				}
			} 
			
			// 创建一个新的呼入记录
			createOutgoingRecord(orderTime, orderNote, serviceRecordStatus, currentProject);
			
			// 设置客户为 VIP 资源
			createVipCustomerToProject(serviceRecordStatus, currentProject);

			// 更新客户级别（客户等级、客户经理）
			updateCustomerBaseInfo(customerResource, serviceRecordStatus);
			
			// TODO (为山西焦炭而实现) 自动完成任务迁移由土豆池1 转向 土豆池 2
			if(currentProject != null && GlobalData.MAC_ADDRESS.equals(shanXiJiaoTanPcMac)) {
				transferTask2NextWorkflow(serviceRecordStatus, currentProject);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 弹屏保存客服记录出现异常--》"+e.getMessage(), e);
			// 这里不做返回 false , 原因：一般创建都会成功，只有刷新界面信息时可能出现空指针异常，可以通过操作避免
		}
		
		notification.setCaption("客服记录创建成功！");
		this.getApplication().getMainWindow().showNotification(notification);
		
		return true;
	}

	/**
	 *  移除Tab页
	 */
	private void executeClose() {
		// 从TabSheet中移除已经保存呼叫记录的Tab页
		tabSheet.removeTab(tabSheet.getTab(incomingDialTabView));
		// 移除后，如果TabSheet 中已经没有其他的Tab 页了，那么就将Tabsheet 所在的Window 关闭
		if(tabSheet.getComponentCount() == 0) {
			tabSheet.getApplication().getMainWindow().removeWindow(tabSheet.getWindow());
		}
	}

	/**
	 * 根据预约时间，为话务员自己创建定时提醒任务
	 * @param projectTasks
	 */
	private void createNewTimeNotices(Date orderTime, String orderNote) {
		if(orderTime == null) {
			return;
		}
		// 标题
		String title = "联系客户[编号："+customerResource.getId()+"]";
		String customerPhoneNum = "";
		for(Telephone phone : customerResource.getTelephones()) {
			customerPhoneNum = phone.getNumber();
			break;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(orderTime);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Timers timers = new Timers();
		timers.setCreator(loginUser);
		timers.setDomain(loginUser.getDomain());
		timers.setResponseTime(orderTime);
		timers.setType("一次");
		timers.setContent(orderNote);
		timers.setTitle(title);
		timers.setCreateTime(new Date());
		timers.setFirstRespTime(orderTime);
		timers.setCustomerId(customerResource.getId());
		timers.setCustomerPhoneNum(customerPhoneNum);
		timersService.save(timers);
		
		// 更新定时任务
		timersService.refreshSchedule(false, timers);
	}

	/**
	 *  创建一个新的呼入记录
	 * 
	 * @param orderTime 给自己创建的预约时间
	 * @param orderNote 预约内容
	 */
	private void createOutgoingRecord(Date orderTime, String orderNote, CustomerServiceRecordStatus serviceRecordStatus, MarketingProject project) {
		CustomerServiceRecord serviceRecord = new CustomerServiceRecord();
		serviceRecord.setOrderTime(orderTime);
		serviceRecord.setOrderNote(orderNote);
		serviceRecord.setCreator(loginUser);
		serviceRecord.setCreateDate(new Date());
		serviceRecord.setDomain(loginUser.getDomain());
		serviceRecord.setCustomerResource(customerResource);
		serviceRecord.setMarketingProject(project);
		serviceRecord.setRecordContent(recordContent.getValue().toString());
		serviceRecord.setServiceRecordStatus(serviceRecordStatus);
		serviceRecord.setDirection("incoming");
		serviceRecord.setQcCsr(QcCsr.QUALIFIED);
		serviceRecordService.save(serviceRecord);
	}
	
	/**
	 * 只有当前项目不为空的情况，才需要添加ProjectCustomer 对象
	 * 如果呼叫记录的结果是“成功客户”, 并且当前Csr拥有要呼叫的项目
	 * 		则将该客户设置成当前项目的 VIP 客户资源
	 */
	private void createVipCustomerToProject(CustomerServiceRecordStatus serviceRecordStatus, MarketingProject project) {
		boolean isVipStatus = serviceRecordStatus.getIsMeanVipCustomer();
		if(isVipStatus == false) {
			return;
		}
		
		if(project != null) {
			// 判断是否已经是VIP 客户
			boolean isVipInProject = false;
			for(ProjectCustomer projectCustomer : projectCustomerService.getAllByProject(loginUser, project)) {
				MarketingProject marketingProject = projectCustomer.getAccountProject();
				if(marketingProject.getId() != null && marketingProject.getId().equals(marketingProject.getId())) {
					isVipInProject = true;
					break;
				}
			}
			
			if(isVipInProject == false) {
				ProjectCustomer projectCustomer = new ProjectCustomer();
				projectCustomer.setAccountManager(loginUser);
				projectCustomer.setSignDate(new Date());
				projectCustomer.setAccountProject(project);
				projectCustomer.setCustomerResource(customerResource);
				projectCustomer.setDomain(loginUser.getDomain());
				projectCustomerService.save(projectCustomer);
				
				customerResource.getProjectCustomers().add(projectCustomer);
				customerResource = customerResourceService.update(customerResource);
			}
		}
		
		// 将资源置为自己的专有客户
		User accountManager = customerResource.getAccountManager();
		if(accountManager == null) {
			customerResource.setAccountManager(loginUser);
			customerResource = customerResourceService.update(customerResource);
		} else if(!accountManager.getId().equals(loginUser.getId())) {
			notification.setCaption("该资源已经成为其他人的客户！");
			this.getApplication().getMainWindow().showNotification(notification);
		}
	}

	/** 
	 * 更新客户级别及客户经理
	 * @param customerResource
	 */
	private void updateCustomerBaseInfo(CustomerResource customerResource, CustomerServiceRecordStatus serviceRecordStatus) {
		CustomerLevel level = (CustomerLevel) customerLevelSelector.getValue();
		if(level != null) {
			customerResource.setCustomerLevel(level);
			Date protectDate = ParseDateSearchScope.getSpecifyDate(level.getProtectDay());
			customerResource.setExpireDate(protectDate);
		}
		
		// 如果“移除客户”组件可见，并且话务员选择了移除，则将客户经理置空
		Boolean isRemoveCustomer = (Boolean) removeCustomerSelector.getValue();
		if(removeCustomerLayout.isVisible() && isRemoveCustomer) {
			customerResource.setAccountManager(null);
		}

		customerResource.setService_record_status_id(serviceRecordStatus.getId());	// JRH 更新客户的最近一次联系结果
		this.customerResource = customerResourceService.update(customerResource);
	}

	/**
	 *  TODO (为山西焦炭而实现) 自动完成任务迁移由土豆池1 转向 土豆池 2
	 */
	private void transferTask2NextWorkflow(CustomerServiceRecordStatus serviceRecordStatus, MarketingProject currentProject) {
		Long currentProjectId = currentProject.getId();
		String statusName = serviceRecordStatus.getStatusName();

		Long workOrder1ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER1_PROJECT_ID));
		Long workOrder2ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER2_PROJECT_ID));
		Long workOrder3ProjectId = Long.parseLong((String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.WORK_ORDER3_PROJECT_ID));

		if(currentProjectId.equals(workOrder1ProjectId)) {
			// 获取需要扭转到工作流2的状态
			ArrayList<String> transferToOrder2Names = new ArrayList<String>();
			String transfer_to_work_order2 = (String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.TRANSFER_TO_WORK_ORDER2);
			for(String singleCondition1 : transfer_to_work_order2.split(",")) {
				transferToOrder2Names.add(singleCondition1);
			}
			if(transferToOrder2Names.contains(statusName)) {	// 转移到工作流2 下
				executeTransfer(currentProject, workOrder2ProjectId, "workflow2");
			}
		} else if(currentProjectId.equals(workOrder2ProjectId)) {
			// 获取需要扭转到工作流3的状态
			ArrayList<String> transferToOrder3Names = new ArrayList<String>();
			String transfer_to_work_order3 = (String) ShanXiJiaoTanWorkOrderConfig.props.get(ShanXiJiaoTanWorkOrderConfig.TRANSFER_TO_WORK_ORDER3);
			for(String singleCondition2 : transfer_to_work_order3.split(",")) {
				transferToOrder3Names.add(singleCondition2);
			}
			if(transferToOrder3Names.contains(statusName)) {	// 转移到工作流3 下
				executeTransfer(currentProject, workOrder3ProjectId, "workflow3");
			}
		}
	}

	/**
	 * 行将当前任务转移到下一个项目中，需要更新或创建工作流迁移日志
	 * @param currentProject		当前工作项目
	 * @param nextOrderProjectId	下一个工作项目Id
	 * @param workflow				要转向哪一个工作流   workflow1 ： 一销； workflow2 ： 二销
	 */
	private void executeTransfer(MarketingProject currentProject, Long nextOrderProjectId, String workflow) {
		MarketingProject nextOrderProject = marketingProjectService.get(nextOrderProjectId);
		
		if(nextOrderProject != null) {
			// 第一步建立批次与资源的关联关系，这样项目将某个批次从项目中删除时，才能将相应的任务也删除
			CustomerResourceBatch resourceBatch = null;
			Set<CustomerResourceBatch> batches = nextOrderProject.getBatches();
			if(batches != null && batches.size() > 0) {
				for(CustomerResourceBatch batch : batches) {
					resourceBatch = batch;
					break;
				}
			}
			
			if(resourceBatch != null) {
				// 检查当前资源是否已经在项目中的某个批次中了
				boolean existedInBatch = false;
				for(CustomerResourceBatch batch : batches) {
					existedInBatch = customerResourceBatchService.checkResourceExistedInBatch(customerResource.getId(), batch.getId());
					if(existedInBatch == true) {
						break;
					}
				}
				
				// 如果资源没有存在于项目下的任何一个批次中，则将其与一个批次建立关联
				if(existedInBatch == false) {
					// 预约信息
					Date orderTime = null;
					String orderNote = null;
					// 判断是否需要给下游话务员创建预约时间
					if(isMakeOrderForOthers) {
						orderTime = (Date) orderTime_pdf.getValue();
						orderNote = StringUtils.trimToEmpty((String) orderNoteArea.getValue());
					}
					
					// 因为是客户资源对象控制级联关系的，所以此处用更新 客户对象来 往 ec2_customer_resource_ec2_customer_resource_batch 中间表中插入数据
					Set<CustomerResourceBatch> resourceBatches = customerResource.getCustomerResourceBatches();
					resourceBatches.add(resourceBatch);
					customerResource = customerResourceService.update(customerResource);

					// 第二步创建工作流迁移日志
					//---------------------  工作流迁移日志对应关联的资源的各种基础信息    ------------------//
					WorkflowTransferLog transferLog = new WorkflowTransferLog();
					transferLog.setCustomerName(customerResource.getName());
					transferLog.setCustomerResourceId(customerResource.getId());
					String phoneNo = "";
					for(Telephone telephone : customerResource.getTelephones()) {
						phoneNo = telephone.getNumber();
						break;
					}
					transferLog.setPhoneNo(phoneNo);
					
					User importor = customerResource.getOwner();
					if(importor != null) {
						transferLog.setImportorId(importor.getId());
						transferLog.setImportorEmpNo(importor.getEmpNo());
						
						Department importorDept = importor.getDepartment();
						transferLog.setImportorDeptId(importorDept.getId());
						transferLog.setImportorDeptName(importorDept.getName());
					}
					
					transferLog.setImportCustomerTime(customerResource.getImportDate());
					transferLog.setDomainId(domain.getId());

					if("workflow2".equals(workflow)) {
						transferLog.setWorkflow1ProjectId(currentProject.getId());
						transferLog.setWorkflow1ProjectName(currentProject.getProjectName());
						transferLog.setWorkflow1CsrId(loginUser.getId());
						transferLog.setWorkflow1CsrEmpNo(loginUser.getEmpNo());
						transferLog.setWorkflow1CsrName(loginUser.getRealName());
						transferLog.setWorkflow1CsrUsername(loginUser.getUsername());
						Department loginUserDept = loginUser.getDepartment();
						transferLog.setWorkflow1CsrDeptId(loginUserDept.getId());
						transferLog.setWorkflow1CsrDeptName(loginUserDept.getName());
						transferLog.setMigrateWorkflow2Time(new Date());
					} else if("workflow3".equals(workflow)) {
						transferLog.setWorkflow2ProjectId(currentProject.getId());
						transferLog.setWorkflow2ProjectName(currentProject.getProjectName());
						transferLog.setWorkflow2CsrId(loginUser.getId());
						transferLog.setWorkflow2CsrEmpNo(loginUser.getEmpNo());
						transferLog.setWorkflow2CsrName(loginUser.getRealName());
						transferLog.setWorkflow2CsrUsername(loginUser.getUsername());
						Department loginUserDept = loginUser.getDepartment();
						transferLog.setWorkflow2CsrDeptId(loginUserDept.getId());
						transferLog.setWorkflow2CsrDeptName(loginUserDept.getName());
						transferLog.setMigrateWorkflow3Time(new Date());
					}

					if(transferLog.getId() == null) {
						workflowTransferLogService.saveWorkflowTransferLog(transferLog);
					} else {
						transferLog = workflowTransferLogService.updateWorkflowTransferLog(transferLog);
					}
					logger.info("transferLog-->"+transferLog);

					// 第三步 创建新的任务到指定的项目中去
					MarketingProjectTask newTask = new MarketingProjectTask();
					newTask.setCustomerResource(customerResource);
					newTask.setMarketingProject(nextOrderProject);
					newTask.setDomain(domain);
					// 这里是‘我的营销任务’模块，所以直接设置任务类型
					newTask.setMarketingProjectTaskType(MarketingProjectTaskType.MARKETING);
					newTask.setOrderTime(orderTime);
					newTask.setOrderNote(orderNote);
					newTask.setIsUseable(true);
					newTask.setCreateTime(new Date());
					newTask.setWorkflowTransferLogId(transferLog.getId());
					newTask.setBatchId(resourceBatch.getId());
					projectTaskService.update(newTask);
				}
			}
		}
	}
	
	/**
	 * 回显客户对象及其级别
	 * @param customerResource
	 */
	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

	/**
	 * 在关闭呼入Tab页时调用，清空组件的值
	 */
	public void clearComponentsValue() {
		answeredResultOptions.setValue(null);
		recordContent.setValue("");
		orderNoteArea.setValue("");
		orderTime_pdf.setValue(null);
	}
	
	public void setTabSheet(TabSheet tabSheet) {
		this.tabSheet = tabSheet;
	}

	/**
	 * 自行以的加载方法
	 */
	public void myAttach() {
		// 全局配置项
		Map<String, Boolean> configs = ShareData.domainToConfigs.get(domain.getId());

		// 新加的：设置客户等级
		CustomerLevel customerLevel = customerResource.getCustomerLevel();
		List<CustomerLevel> customerLevels = new ArrayList<CustomerLevel>();
		Boolean downable = configs.get("customer_level_down_able");
		// 如果客户级别为空，或者全局配置客户可降级对象存在，且配置为可降级，则等级可选项为所有等级，否则为不高于自己的等级
		if(customerLevel == null || (downable != null && downable == true)) {
			customerLevels.addAll(customerLevelService.getAll(domain));
		} else {
			customerLevels.addAll(customerLevelService.getAllSuperiorLevel(customerLevel, domain));
		}
		customerLevelContainer.removeAllItems();
		customerLevelContainer.addAll(customerLevels);

		// 回显客户等级值
		if(customerLevel == null) {
			customerLevelSelector.setValue(null);
		} else {
			for(CustomerLevel level : customerLevels) {
				if(customerLevel.getId() != null && customerLevel.getId().equals(level.getId())) {
					customerLevelSelector.setValue(level);
					break;
				}
			}
		}
		
		// 更新客户等级降级选择框的描述信息
		Boolean levelDownAble = configs.get("customer_level_down_able");
		if(levelDownAble == null || levelDownAble == false) {
			customerLevelSelector.setDescription("<B>客户等级只能持平或者升级，不能降低</B>");
		} else {
			customerLevelSelector.setDescription(null);
		}
		
		// 更新“移除客户”组件的可视属性，以及选择框的默认值
		// 判断资源是否为当前CSR 的客户，如果不是，则不显示“移除客户组件”
		User accountManager = customerResource.getAccountManager();
		removeCustomerSelector.setValue(false);
		if(accountManager == null || !accountManager.getId().equals(loginUser.getId())) {
			removeCustomerLayout.setVisible(false);
		} else {	// 如果是自己的客户，则判断管理员是否允许话务员移除自己的客户
			Boolean customerRemoveAble = configs.get("customer_remove_able_by_csr");
			if(customerRemoveAble == null || customerRemoveAble == false) {
				removeCustomerLayout.setVisible(false);
			} else {
				removeCustomerLayout.setVisible(true);
			}
		}
		
		// 判断全局配置是否开启了呼入客户满意度调查
		Boolean isOutgoingSati = configs.get("incoming_sati_config");
		if(isOutgoingSati == null || isOutgoingSati == false) {
			satisfictionInvestigationButton.setVisible(false);
		} else {
			satisfictionInvestigationButton.setVisible(true);
		}
		
		incomingStatusContainer.addAll(serviceRecordStatusService.getAllByDeptIdAndDirection(departmentId, "incoming", true, domain.getId()));
		
		// 将预约组件置为不可见
		orderVLayout.setVisible(false);
		
	}
	
}
