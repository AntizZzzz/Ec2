package com.jiangyifen.ec2.ui.csr.workarea.othermodules;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.OriginateAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.SipConfigService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 话务员的会议室管理模块
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class CsrMeetMeView extends VerticalLayout implements ClickListener {

	private Notification notification;		// 提示信息
	
	// 坐席选择
	private ComboBox csrSelectComboBox;
	private BeanItemContainer<User> csrSelectContainer;
	private Button inviteCsr;

	// 分机选择
	private ComboBox extenComboBox;
	private BeanItemContainer<String> extenSelectContainer;
	private Button inviteExten;
	
	// 手机输入
	private TextField phoneNumberInputField;
	private Button invitePhoneNumber;
	
	// 终止会议室
	private Button stopMeetingRoom_bt;
	
	private CsrMeetMeDetailSupervise csrMeetMeDetailSupervise;	// 会议室监控界面

	private User loginUser;		// 当前登陆用户
	private String exten;		// 当前用户使用的分机
	private Domain domain;		// 当前用户所属域
	
	private UserService userService;
	private SipConfigService sipConfigService;
	
	public CsrMeetMeView() {
		this.setSizeFull();
		this.setMargin(true);
		this.setSpacing(true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		exten = ShareData.userToExten.get(loginUser.getId());
		
		userService = SpringContextHolder.getBean("userService");
		sipConfigService = SpringContextHolder.getBean("sipConfigService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);
		
		// 会议室提示窗口
		Label noticeLabel = new Label("<font color='blue'>您当前的会议室为："+exten+"</font>&nbsp;&nbsp;&nbsp;", Label.CONTENT_XHTML);
		noticeLabel.setWidth("-1px");
		constrantLayout.addComponent(noticeLabel);
		constrantLayout.setComponentAlignment(noticeLabel, Alignment.MIDDLE_LEFT);

		// 坐席选择区域
		constrantLayout.addComponent(buildInviteCsrLayout());

		// 分机选择区域
		constrantLayout.addComponent(buildInviteExtenLayout());
		
		//外部手机号码输入区
		constrantLayout.addComponent(buildPhoneNumberInputLayout());
		
		stopMeetingRoom_bt = new Button("终止会议", this);
		stopMeetingRoom_bt.setStyleName("default");
		constrantLayout.addComponent(stopMeetingRoom_bt);
		
		csrMeetMeDetailSupervise = new CsrMeetMeDetailSupervise();
		this.addComponent(csrMeetMeDetailSupervise);
		this.setExpandRatio(csrMeetMeDetailSupervise, 1.0f);
	}

	/**
	 * 坐席选择区域输入区
	 * @return
	 */
	private HorizontalLayout buildInviteCsrLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		
		// 关键字Label
		Label caption = new Label("坐席号：");
		caption.setWidth("-1px");
		mainLayout.addComponent(caption);
		mainLayout.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
		
		csrSelectContainer = new BeanItemContainer<User>(User.class);
		csrSelectContainer.addAll(userService.getCsrsByDomain(domain));
		
		// 关键字组件
		csrSelectComboBox = new ComboBox();
		csrSelectComboBox.setWidth("120px");
		csrSelectComboBox.setItemCaptionPropertyId("username");
		csrSelectComboBox.setContainerDataSource(csrSelectContainer);
		mainLayout.addComponent(csrSelectComboBox);
		mainLayout.setComponentAlignment(csrSelectComboBox, Alignment.MIDDLE_LEFT);
		
		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);
		
		// 搜索按钮
		inviteCsr = new Button("邀请", this);
		buttonConstraintLayout.addComponent(inviteCsr);
		return mainLayout;
	}
	
	/**
	 * 分机选择区域输入区
	 * @return
	 */
	private HorizontalLayout buildInviteExtenLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		
		// 关键字Label
		Label caption = new Label("分机号：");
		caption.setWidth("-1px");
		mainLayout.addComponent(caption);
		mainLayout.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
		
		extenSelectContainer = new BeanItemContainer<String>(String.class);
		for(SipConfig sip : sipConfigService.getAllExtsByDomain(domain)) {
			extenSelectContainer.addBean(sip.getName());
		}
		
		// 关键字组件
		extenComboBox = new ComboBox();
		extenComboBox.setWidth("120px");
		extenComboBox.setContainerDataSource(extenSelectContainer);
		mainLayout.addComponent(extenComboBox);
		mainLayout.setComponentAlignment(extenComboBox, Alignment.MIDDLE_LEFT);
		
		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);
		
		// 搜索按钮
		inviteExten = new Button("邀请", this);
		buttonConstraintLayout.addComponent(inviteExten);
		return mainLayout;
	}
	
	/**
	 * 外部手机号码输入区
	 * @return
	 */
	private HorizontalLayout buildPhoneNumberInputLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		
		// 关键字Label
		Label caption = new Label("手机号：");
		caption.setWidth("-1px");
		mainLayout.addComponent(caption);
		mainLayout.setComponentAlignment(caption, Alignment.MIDDLE_LEFT);
		
		// 关键字组件
		phoneNumberInputField = new TextField();
		phoneNumberInputField.setWidth("120px");
		phoneNumberInputField.setNullRepresentation("");
		mainLayout.addComponent(phoneNumberInputField);
		mainLayout.setComponentAlignment(phoneNumberInputField, Alignment.MIDDLE_LEFT);
		
		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);
		
		// 搜索按钮
		invitePhoneNumber = new Button("邀请", this);
		buttonConstraintLayout.addComponent(invitePhoneNumber);
		return mainLayout;
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == inviteCsr) {
			//邀请坐席
			executeInviteCsr();
		} else if (source == inviteExten) {
			//邀请分机 
			executeInviteExten();
		} else if (source == invitePhoneNumber) {
			//向手机发出加入会议室的邀请
			executePhoneNumber();
		} else if (source == stopMeetingRoom_bt) {
			CommandAction commandAction = new CommandAction("meetme kick "+exten+" all");
			AmiManagerThread.sendAction(commandAction);
		}
	}

	//邀请分机 
	private void executeInviteExten() {
		//检查输入是否合法
		String extenNumber=StringUtils.trimToEmpty((String)extenComboBox.getValue());
		if(extenNumber.equals("")){
			notification.setCaption("<font color='red'><B>请选择分机</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return;
		}
		
		//发起action定位到会议室
		invite("SIP/"+extenNumber,extenNumber);
	}

	//邀请坐席
	private void executeInviteCsr() {
		User user=(User)csrSelectComboBox.getValue();
		if(user==null){
			notification.setCaption("<font color='red'><B>请选择坐席</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return;
		}
		
		//检查输入是否合法
		String extenNumber=ShareData.userToExten.get(user.getId());
		if(extenNumber == null){
			notification.setCaption("<font color='red'><B>当前选中的坐席不在线</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return;
		}
		
		//发起action定位到会议室
		invite("SIP/"+extenNumber,extenNumber);
		
	}

	//向手机发出加入会议室的邀请
	private void executePhoneNumber() {
		//检查输入是否合法
		String phoneNumber=StringUtils.trimToEmpty((String)phoneNumberInputField.getValue());
		if(phoneNumber.equals("") || !StringUtils.isNumeric(phoneNumber) 
				|| phoneNumber.length() < 7 || phoneNumber.length() > 13){
			notification.setCaption("<font color='red'><B>号码不能为空，并且只能是数字，长度为7-12位</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return;
		}
		
		//发起action定位到会议室
		String outline=ShareData.domainToDefaultOutline.get(domain.getId());
		invite("SIP/"+phoneNumber+"@"+outline,phoneNumber);
	}
	
	/**
	 * 发起邀请
	 * @return
	 */
	private Boolean invite(String channel,String callerId){
		try {
			// 呼叫Action
			OriginateAction originateAction = new OriginateAction();
			originateAction.setTimeout(5*60*1000L);
			originateAction.setChannel(channel);
			originateAction.setCallerId(callerId);
			originateAction.setExten("998"+exten);
			originateAction.setContext("outgoing");
			originateAction.setPriority(1);
			originateAction.setAsync(true);
			AmiManagerThread.sendAction(originateAction);
			
			notification.setCaption("对 "+callerId+" 的邀请已发出！");
			this.getApplication().getMainWindow().showNotification(notification);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 启动监控线程
	 */
	public void startSuperviseThread() {
		csrMeetMeDetailSupervise.update();
	}
	
	/**
	 * 停止监控线程
	 */
	public void stopSuperviseThread() {
		csrMeetMeDetailSupervise.setGotoRun(false);
	}
}
