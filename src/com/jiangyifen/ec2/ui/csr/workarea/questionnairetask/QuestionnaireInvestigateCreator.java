package com.jiangyifen.ec2.ui.csr.workarea.questionnairetask;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.entity.CustomerQuestionnaire;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.CustomerQuestionnaireFinishStatus;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerQuestionnaireService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerBaseInfoView;
import com.jiangyifen.ec2.ui.mgr.questionnaire.StartDoQuestionnaire;
import com.jiangyifen.ec2.ui.mgr.questionnaire.StartDoQuestionnaireShowAll;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * 弹屏界面中用于做满意度调查业务的Tab 页
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class QuestionnaireInvestigateCreator extends VerticalLayout implements ClickListener, ValueChangeListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	private Notification notification;			// 提示信息
	private ComboBox questionnaireSelector;		// 问卷选择框
	private Button startInvestigate;			// 开始调查

//	private Table focusTaskTable;				// 当前正在操作的问卷任务表格
	
	private FlipOverTableComponent<MarketingProjectTask> focusTaskTableFlip;		// 当前  正在  操作的问卷任务表格的翻页组件
	private FlipOverTableComponent<MarketingProjectTask> unfocusTaskTableFlip;		// 当前  没有  操作的问卷任务表格的翻页组件

	private CustomerBaseInfoView customerBaseInfoView;								// 客户资源对象的基本信息界面
	
	// 创建确认窗口组件
	private Label noticeLabel;					// 提示标签
	private Button reDo;						// 重做问卷
	private Button continueDo;					// 接着上次完成的地方做
	private Button cancel;						// 取消
	private Window confirmWindow;				// 确认窗口

	private User loginUser;						// 当前登陆用户
	private String exten;						// 当前登陆用户所使用的分机
	private Boolean isbridged2Customer;			// 已经与客户建立了通话
	private Questionnaire questionnaire;		// 话务员选中的问卷对象
	private CustomerResource customerResource;	// 问卷调查的客户对象
	private String recordFilePath;				// 当前通话对应的录音文件的部分路径
	
	private MarketingProjectTask currentTask;				// 用户获取问卷任务表格中被选择的任务项
	private BeanItemContainer<Questionnaire> questionnaireContainer; 	// 问卷调查选择框数据源
	private StartDoQuestionnaire startQuestionnaireQuestion;			// 做题
	private StartDoQuestionnaireShowAll startDoQuestionnaireShowAllWindow;	// 显示所有题目的问卷弹窗
	
	private MarketingProjectTaskService projectTaskService;				// 项目任务服务类
	private CustomerQuestionnaireService customerQuestionnaireService;	// 客户与问卷对应关系的服务类
	
	public QuestionnaireInvestigateCreator(CustomerBaseInfoView customerBaseInfoView) {
		this.setSpacing(true);
		this.setMargin(true);
		this.customerBaseInfoView = customerBaseInfoView;

		loginUser = SpringContextHolder.getLoginUser();
		exten = SpringContextHolder.getExten();
		questionnaireContainer = new BeanItemContainer<Questionnaire>(Questionnaire.class);
		customerQuestionnaireService = SpringContextHolder.getBean("customerQuestionnaireService");
		projectTaskService = SpringContextHolder.getBean("marketingProjectTaskService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 创建操作组件：问卷选择框、开始调查等组件
		createOperatorComponents();
		
		startQuestionnaireQuestion = new StartDoQuestionnaire(loginUser);
		this.addComponent(startQuestionnaireQuestion);
		
		// 创建确认窗口
		createConfirmWindow();
		
		// 创建显示所有题目的问卷弹窗
		startDoQuestionnaireShowAllWindow = new StartDoQuestionnaireShowAll(loginUser);
	}


	/**
	 *  创建确认窗口
	 */
	private void createConfirmWindow() {
		confirmWindow = new Window("确认窗口");
		
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		confirmWindow.setContent(windowContent);
		confirmWindow.center();
		
		noticeLabel = new Label("<font color='red'><B>当前客户已经做过当前问卷，请选择重做，或接着上次完成的地方继续做！</B></font>", Label.CONTENT_XHTML);
		noticeLabel.setWidth("-1px");
		windowContent.addComponent(noticeLabel);
		
		HorizontalLayout operatorLayout = new HorizontalLayout();
		operatorLayout.setSpacing(true);
		operatorLayout.setWidth("100%");
		windowContent.addComponent(operatorLayout);
		
		continueDo = new Button("继续完成", this);
		continueDo.setStyleName("default");
		continueDo.setImmediate(true);
		operatorLayout.addComponent(continueDo);
		
		reDo = new Button("重新开始", this);
		reDo.setImmediate(true);
		operatorLayout.addComponent(reDo);
		
		cancel = new Button("取消", this);
		cancel.setStyleName("default");
		cancel.setImmediate(true);
		operatorLayout.addComponent(cancel);
		operatorLayout.setComponentAlignment(cancel, Alignment.MIDDLE_RIGHT);
		operatorLayout.setExpandRatio(cancel, 1.0f);
	}

	/**
	 *  创建操作组件：问卷选择框、开始调查等组件
	 */
	private void createOperatorComponents() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		this.addComponent(layout);
		
		// 问卷选择框
		Label captionLabel = new Label("<B>选择问卷：</B>", Label.CONTENT_XHTML);
		captionLabel.setWidth("-1px");
		layout.addComponent(captionLabel);
		layout.setComponentAlignment(captionLabel, Alignment.MIDDLE_LEFT);
		
		questionnaireSelector = new ComboBox();
		questionnaireSelector.addListener((ValueChangeListener)this);
		questionnaireSelector.setImmediate(true);
		questionnaireSelector.setInputPrompt("请选择要调查的问卷");
		questionnaireSelector.setWidth("300px");
		questionnaireSelector.setNullSelectionAllowed(false);
		questionnaireSelector.setItemCaptionPropertyId("mainTitle");		
		questionnaireSelector.setContainerDataSource(questionnaireContainer);
		layout.addComponent(questionnaireSelector);
		layout.setComponentAlignment(questionnaireSelector, Alignment.MIDDLE_LEFT);
		
		// 开始调查按钮
		startInvestigate = new Button("开始调查", this);
		startInvestigate.setStyleName("default");
		startInvestigate.setImmediate(true);
		layout.addComponent(startInvestigate);
		layout.setComponentAlignment(startInvestigate, Alignment.MIDDLE_LEFT);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == questionnaireSelector) {
			questionnaire = (Questionnaire) questionnaireSelector.getValue();
			questionnaireSelector.setDescription("<B>"+questionnaire.getMainTitle()+"</B>");
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		try {
			if(source == startInvestigate) {
				// 执行问卷调查
				executeInvestigate();
			} else if(source == continueDo) {
				// 显示做题布局
				if(questionnaire.getShowcode() == 2) {
					this.getApplication().getMainWindow().removeWindow(startDoQuestionnaireShowAllWindow);
					this.getApplication().getMainWindow().addWindow(startDoQuestionnaireShowAllWindow);
					startDoQuestionnaireShowAllWindow.refreshComponentInfo(questionnaire, customerResource.getId(), 3, recordFilePath);
				} else {
					startQuestionnaireQuestion.refreshComponentInfo(questionnaire, customerResource.getId(), 3, recordFilePath);
				}
				this.getApplication().getMainWindow().removeWindow(confirmWindow);
			} else if(source == cancel) {
				this.getApplication().getMainWindow().removeWindow(confirmWindow);
			} else if(source == reDo) {
				// 显示做题布局
				if(questionnaire.getShowcode() == 2) {
					this.getApplication().getMainWindow().removeWindow(startDoQuestionnaireShowAllWindow);
					this.getApplication().getMainWindow().addWindow(startDoQuestionnaireShowAllWindow);
					startDoQuestionnaireShowAllWindow.refreshComponentInfo(questionnaire, customerResource.getId(), 2, recordFilePath);
				} else {
					startQuestionnaireQuestion.refreshComponentInfo(questionnaire, customerResource.getId(), 2, recordFilePath);
				}
				this.getApplication().getMainWindow().removeWindow(confirmWindow);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 执行问卷调查出现异常--》"+e.getMessage(), e);
		}
	}

	/**
	 * 执行问卷调查
	 */
	private void executeInvestigate() {
		// 如果客户基础信息处于编辑状态，则先保存客户基础信息，然后再创建后续记录
		boolean isSaveResourceSuccess = customerBaseInfoView.getCustomerBaseInfoEditorForm().checkAndSaveCustomerResourceInfo();
		if(!isSaveResourceSuccess) {
			this.getApplication().getMainWindow().showNotification("客户基础信息填写有误，保存失败！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
//		if(customerResource.getId() == null) {
//			notification.setCaption("<font color='red'><B>请先保存客户资源！</B></font>");
//			this.getApplication().getMainWindow().showNotification(notification);
//			return;
//		}
		
		if(questionnaire == null) {
			notification.setCaption("<font color='red'><B>请先选择一个要调查的问卷！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return;
		}
		
		// 判断话务员是否已经与客户建立通话
		ChannelSession channelSession = checkBridged2Customer();
		if(channelSession == null) {
			isbridged2Customer = false;
			notification.setCaption("<font color='red'><B>您尚未与客户建立通话，请等客户接通后再试！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return;
		} else {
			String dateStr = dateFormat.format(new Date());
			recordFilePath = dateStr+"/"+dateStr+"-"+channelSession.getChannelUniqueId()+".wav";
			isbridged2Customer = true;
		}
		
		// 检查用户是否已经做过题 TODO
		List<CustomerQuestionnaire> cqs = customerQuestionnaireService.getCustomerQuestionnaireByCustomerId(customerResource.getId(), questionnaire.getId());
		if(cqs != null && cqs.size() > 0) {	// 更新组件显示属性
			if(questionnaire.getShowcode() == 2) {
				continueDo.setVisible(true);
				reDo.setVisible(true);
			} else {
				for(CustomerQuestionnaire cq : cqs) {
					if("start".equals(cq.getFinish())) {
						continueDo.setVisible(true);
						break;
					} else if("end".equals(cq.getFinish())) {
						continueDo.setVisible(false);
						break;
					}
				}
			}
			this.getApplication().getMainWindow().removeWindow(confirmWindow);
			this.getApplication().getMainWindow().addWindow(confirmWindow);
		} else {
			if(questionnaire.getShowcode() == 2) {
				this.getApplication().getMainWindow().removeWindow(startDoQuestionnaireShowAllWindow);
				this.getApplication().getMainWindow().addWindow(startDoQuestionnaireShowAllWindow);
				startDoQuestionnaireShowAllWindow.refreshComponentInfo(questionnaire, customerResource.getId(), 1, recordFilePath);
			} else {
				startQuestionnaireQuestion.refreshComponentInfo(questionnaire, customerResource.getId(), 1, recordFilePath);
			}
		}
	}

	/**
	 * 判断当前分机进行的呼叫，客户是否接听，如果接通了，则放回其对应的ChannelSession，否则返回null
	 * @return ChannelSession
	 */
	private ChannelSession checkBridged2Customer() {
		Set<String> selfChannels = ShareData.peernameAndChannels.get(exten);
		if(selfChannels == null) {
			return null;
		}
		
		for(String channel : selfChannels) {
			ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
			if (channelSession != null) {
				String bridgedChannel = channelSession.getBridgedChannel();
				if (bridgedChannel != null && (!"".equals(bridgedChannel))) {
					return channelSession;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 回显话务员选中的问卷对象
	 */
	public void echoQuestionnaireInfos(CustomerResource customerResource) {
		this.customerResource = customerResource;
		
		try {
			MarketingProject currentProject = currentTask.getMarketingProject();
			if(currentProject != null) {
				List<Questionnaire> questionnaires = new ArrayList<Questionnaire>(currentProject.getQuestionnaires());
				questionnaire = (Questionnaire) questionnaireSelector.getValue();
				questionnaireContainer.removeAllItems();
				questionnaireContainer.addAll(questionnaires);
				if(questionnaireContainer.size() >= 1 && questionnaire == null) {
					questionnaireSelector.setValue(questionnaireContainer.getIdByIndex(0));
				} else {
					for(Questionnaire q : questionnaires) {
						if(q.getId() != null && q.getId().equals(questionnaire.getId())) {
							questionnaireSelector.setValue(q);
							break;
						}
					}
				}
				
				this.removeComponent(startQuestionnaireQuestion);
				startQuestionnaireQuestion = new StartDoQuestionnaire(loginUser);
				this.addComponent(startQuestionnaireQuestion);
			}
		} catch (Exception e) {
			logger.error("JRH--->"+e.getMessage(), e);
		}
	}
	
	/**
	 * 更新问卷任务的显示表格中的内容
	 */
	public void updateQuestionnaireTableSource() {
		try {
			MarketingProject currentProject = currentTask.getMarketingProject();
			if(currentProject != null) {
				currentTask.setIsFinished(true);
				currentTask.setLastUpdateDate(new Date());
				if(isbridged2Customer == null) {
					currentTask.setIsAnswered(false);
				} else {
					currentTask.setIsAnswered(isbridged2Customer);
				}
				
				CustomerQuestionnaireFinishStatus finishedStatus = CustomerQuestionnaireFinishStatus.UN_STARTED;
				if(questionnaire != null) {
					List<CustomerQuestionnaire> cqs = customerQuestionnaireService.getCustomerQuestionnaireByCustomerId(customerResource.getId(), questionnaire.getId());
					if(cqs != null && cqs.size() > 0) {
						for(CustomerQuestionnaire cq : cqs) {
							if("start".equals(cq.getFinish())) {
								finishedStatus = CustomerQuestionnaireFinishStatus.PART_FINISHED;
								break;
							} else if("end".equals(cq.getFinish())) {
								finishedStatus = CustomerQuestionnaireFinishStatus.FINISHED;
								break;
							}
						}
					} 
				}
				
				currentTask.setCustomerQuestionnaireFinishStatus(finishedStatus);
				currentTask = projectTaskService.update(currentTask);
				
				// 更新问卷任务表格显示内容
				focusTaskTableFlip.refreshInCurrentPage();
				unfocusTaskTableFlip.refreshToFirstPage();
			}
		} catch (Exception e) { 
			logger.error("JRH--->"+e.getMessage(), e);
		}
	}
	
	/**
	 * 根据当前正在操作的表格，来刷新响应的组件
	 * @param focusTaskTableFlip
	 * @param unfocusTaskTableFlip
	 */
	public void refreshComponent(FlipOverTableComponent<MarketingProjectTask> focusTaskTableFlip, 
			FlipOverTableComponent<MarketingProjectTask> unfocusTaskTableFlip) {
		this.focusTaskTableFlip = focusTaskTableFlip;
		this.unfocusTaskTableFlip = unfocusTaskTableFlip;
	}

	public void refreshCurrentTask(MarketingProjectTask marketingProjectTask) {
		this.currentTask = marketingProjectTask;
	}
	
}
