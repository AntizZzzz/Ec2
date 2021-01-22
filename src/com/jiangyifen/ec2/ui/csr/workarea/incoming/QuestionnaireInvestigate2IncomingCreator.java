package com.jiangyifen.ec2.ui.csr.workarea.incoming;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.CustomerQuestionnaire;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CustomerQuestionnaireService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
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
 * 针对呼入的问卷调查创建界面
 * @author jrh
 *  2013-7-5
 */
@SuppressWarnings("serial")
public class QuestionnaireInvestigate2IncomingCreator extends VerticalLayout implements ClickListener, ValueChangeListener {
	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

	private Notification success_notification;	// 成功过提示信息
	private Notification warning_notification;	// 错误警告提示信息
	
	private ComboBox questionnaireSelector;		// 问卷选择框
	private Button startInvestigate;			// 开始调查

	// 创建确认窗口组件
	private Label noticeLabel;					// 提示标签
	private Button reDo;						// 重做问卷
	private Button continueDo;					// 接着上次完成的地方做
	private Button cancel;						// 取消
	private Window confirmWindow;				// 确认窗口
	
	private User loginUser;						// 当前登陆用户
	private String exten;						// 当前登陆用户所使用的分机
	private Questionnaire questionnaire;		// 话务员选中的问卷对象
	private CustomerResource customerResource;	// 问卷调查的客户对象
	private String recordFilePath;				// 当前通话对应的录音文件的部分路径
	private ChannelSession bridgedExtenChannelSession;	// 被叫分机接通后的通道信息

	private IncomingDialTabView incomingDialTabView;	// 呼入弹屏的Tab 页
	private boolean isEncryptMobile = true;				// 电话号码默认加密
	
	private BeanItemContainer<Questionnaire> questionnaireContainer; 	// 问卷调查选择框数据源
	private StartDoQuestionnaire startQuestionnaireQuestion;			// 做题
	private StartDoQuestionnaireShowAll startDoQuestionnaireShowAllWindow;	// 显示所有题目的问卷弹窗

	private CustomerQuestionnaireService customerQuestionnaireService;	// 客户与问卷对应关系的服务类
	private TelephoneService telephoneService;							// 电话号码服务类
	
	public QuestionnaireInvestigate2IncomingCreator(User loginUser, IncomingDialTabView incomingDialTabView) {
		this.setSpacing(true);
		this.setMargin(true);
		this.loginUser = loginUser;
		this.incomingDialTabView = incomingDialTabView;
		
		exten = ShareData.userToExten.get(loginUser.getId());

		// 获取当前登陆话务员所有权限
		ArrayList<String> ownBusinessModels = new ArrayList<String>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.csr)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownBusinessModels.add(bm.toString());
				} 
			}
		}
		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		questionnaireContainer = new BeanItemContainer<Questionnaire>(Questionnaire.class);
		customerQuestionnaireService = SpringContextHolder.getBean("customerQuestionnaireService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
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
		if(source == startInvestigate) {
			// 执行问卷调查
			executeInvestigate();
		} else if(source == continueDo) {
			// 显示做题布局
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
	}

	/**
	 * 执行问卷调查
	 */
	private void executeInvestigate() {
		if(questionnaire == null) {
			warning_notification.setCaption("请先选择一个要调查的问卷！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		if(customerResource.getId() == null) {
			warning_notification.setCaption("请先保存客户资源后，再为其做问卷调查！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		
		// 判断话务员是否已经与客户建立通话
		int bridgedStatus = checkBridged2Customer();
		if(bridgedStatus == -1) {
			warning_notification.setCaption("您尚未与当前客户建立通话，请建立通话后再试！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		} else if(bridgedStatus == 0) {
				String callerNum = StringUtils.trimToEmpty(bridgedExtenChannelSession.getConnectedlinenum());
				if(isEncryptMobile == true) {
					callerNum = telephoneService.encryptMobileNo(callerNum);
				}
				warning_notification.setCaption("您当前的通话对象是"+callerNum+",请切换到对应Tab页后再试！</B></font>");
				this.getApplication().getMainWindow().showNotification(warning_notification);
				return;
		} else if(bridgedStatus == 1) {
			String dateStr = dateFormat.format(new Date());
			// 呼入时去主叫的uniqueid值
			recordFilePath = dateStr+"/"+dateStr+"-"+bridgedExtenChannelSession.getBridgedUniqueId()+".wav";
		}

		// 检查用户是否已经做过题
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
	 * 需要确认该分机已经有接通的电话，并且联系的人正好是当前操作的客户对象[需要检查振铃时的通道信息 跟建立某个通话后的通道信息uniqueid是否相同]
	 * @return Integer 
	 * 			<br/> -1: 表示当前分机没有任何接通的通话; 
	 *			<br/> 0: 表示当前分机有接通的通话，但是该通话对象不是当前操作的客户对象 ; 
	 * 			<br/> 1: 表示当前分机有接通的通话，并且该通话对象是当前操作的客户对象 ;
	 */
	private Integer checkBridged2Customer() {
		Set<String> selfChannels = ShareData.peernameAndChannels.get(exten);
		if(selfChannels == null) {
			return -1;
		}
		
		for(String channel : selfChannels) {
			ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
			if (channelSession != null) {
				// 被叫分机振铃时的通道信息
				ChannelSession ringingExtenChannelSession = incomingDialTabView.getRingingExtenChannelSession();	
				String bridgedChannel = channelSession.getBridgedChannel();
				String bridgedUniqueid = channelSession.getChannelUniqueId();
				String ringingUniqueid = ringingExtenChannelSession.getChannelUniqueId();
				// 需要检查振铃时的通道信息 跟建立某个通话后的通道信息uniqueid是否相同
				if(bridgedChannel != null && (!"".equals(bridgedChannel))) {
					this.bridgedExtenChannelSession = channelSession;
					if(ringingUniqueid.equals(bridgedUniqueid)) {
						return 1;
					} 
					return 0;
				}
			}
		}
		return -1;
	}
	
	/**
	 * 回显话务员选中的问卷对象
	 */
	public void echoQuestionnaireInfos(MarketingProject currentProject, CustomerResource customerResource) {
		this.customerResource = customerResource;
		List<Questionnaire> questionnaires = new ArrayList<Questionnaire>(currentProject.getQuestionnaires());
		
		questionnaire = (Questionnaire) questionnaireSelector.getValue();
		questionnaireContainer.removeAllItems();
		questionnaireContainer.addAll(questionnaires);
		if(questionnaireContainer.size() == 1 || questionnaire == null) {
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
	
}
