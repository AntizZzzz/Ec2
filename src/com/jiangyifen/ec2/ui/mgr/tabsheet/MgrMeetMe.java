package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.OriginateAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 管理员会议室查看页面
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class MgrMeetMe extends VerticalLayout implements
		Button.ClickListener {
	// 分机输入
	private TextField extenInputField;
	private Button confirm;
	private Button reset;

	// 手机输入
	private TextField phoneNumberInputField;
	private Button invitePhoneNumber;
	
	// 坐席选择
	private ComboBox csrSelectComboBox;
	private BeanItemContainer<User> csrSelectContainer;
	private Button inviteCsr;

	// 分机选择
	private ComboBox extenComboBox;
	private BeanItemContainer<String> extenSelectContainer;
	private Button inviteExten;
	
	private CommonService commonService;
	private Domain domain;
	private String inputExten;

	public MgrMeetMe() {
		commonService=SpringContextHolder.getBean("commonService");
		domain=SpringContextHolder.getDomain();
		this.setSizeFull();
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 分机输入区域
		constrantLayout.addComponent(buildExtenInputLayout());
		
		//外部手机号码输入区
		constrantLayout.addComponent(buildPhoneNumberInputLayout());

		// 坐席选择区域
		constrantLayout.addComponent(buildInviteCsrLayout());

		// 分机选择区域
		constrantLayout.addComponent(buildInviteExtenLayout());
	}

	@Override
	public void attach() {
		update();
	}
	
	//更新用户显示
	public void update(){
		//===================将组件空，将field置为可用，其它不可用
		// 分机输入
		extenInputField.setValue(null);
		extenInputField .setEnabled(true);
		confirm.setEnabled(true);
		reset.setEnabled(true);

		// 手机输入
		phoneNumberInputField.setValue(null);
		phoneNumberInputField.setEnabled(false);
		invitePhoneNumber.setEnabled(false);
		
		// 坐席选择
		csrSelectComboBox.setValue(null);
		csrSelectComboBox.setEnabled(false);
		inviteCsr.setEnabled(false);

		// 分机选择
		extenComboBox.setValue(null);
		extenComboBox.setEnabled(false);
		inviteExten.setEnabled(false);
		
		//=====================================更新分机和用户的container
		//更新分机
		extenSelectContainer.removeAllItems();
		List<String> extens = ShareData.domainToExts.get(domain.getId());
		extenSelectContainer.addAll(extens);
		
		//更新在线用户
		csrSelectContainer.removeAllItems();
		for(String exten:extens){
			Long userId=ShareData.extenToUser.get(exten);
			if(userId!=null){
				User user=commonService.get(User.class, userId);
				csrSelectContainer.addItem(user);
			}
		}
		inputExten="";
	}
	
	/**
	 * 创建分机输入区域
	 * 
	 * @return
	 */
	private HorizontalLayout buildExtenInputLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();

		// 关键字Label
		mainLayout.addComponent(new Label("分机号："));

		// 关键字组件
		extenInputField = new TextField();
		extenInputField.setNullRepresentation("");
		mainLayout.addComponent(extenInputField);

		// 约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);

		// 创建会议室按钮
		confirm = new Button("创建会议室");
		confirm.addListener(this);
		buttonConstraintLayout.addComponent(confirm);

		// 搜索按钮
		reset = new Button("重置");
		reset.addListener(this);
		buttonConstraintLayout.addComponent(reset);
		return mainLayout;
	}

	/**
	 * 外部手机号码输入区
	 * @return
	 */
	private HorizontalLayout buildPhoneNumberInputLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		
		// 关键字Label
		mainLayout.addComponent(new Label("手机号："));
		
		// 关键字组件
		phoneNumberInputField = new TextField();
		phoneNumberInputField.setNullRepresentation("");
		mainLayout.addComponent(phoneNumberInputField);
		
		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);
		
		// 搜索按钮
		invitePhoneNumber = new Button("邀请");
		invitePhoneNumber.addListener(this);
		buttonConstraintLayout.addComponent(invitePhoneNumber);
		return mainLayout;
	}

	/**
	 * 坐席选择区域输入区
	 * @return
	 */
	private HorizontalLayout buildInviteCsrLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		
		// 关键字Label
		mainLayout.addComponent(new Label("坐席号："));
		
		// 关键字组件
		csrSelectComboBox = new ComboBox();
		csrSelectComboBox.setTextInputAllowed(true);
		csrSelectContainer=new BeanItemContainer<User>(User.class);
		csrSelectComboBox.setContainerDataSource(csrSelectContainer);
		mainLayout.addComponent(csrSelectComboBox);
		
		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);
		
		// 搜索按钮
		inviteCsr = new Button("邀请");
		inviteCsr.addListener(this);
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
		mainLayout.addComponent(new Label("分机号："));
		
		// 关键字组件
		extenComboBox = new ComboBox();
		extenComboBox.setTextInputAllowed(true);
		extenSelectContainer=new BeanItemContainer<String>(String.class);
		extenComboBox.setContainerDataSource(extenSelectContainer);
		mainLayout.addComponent(extenComboBox);
		
		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);
		
		// 搜索按钮
		inviteExten = new Button("邀请");
		inviteExten.addListener(this);
		buttonConstraintLayout.addComponent(inviteExten);
		return mainLayout;
	}
	

	//加入会议室
	private void executeConfirm() {
		//检查输入是否合法  jrh 这里没必要这么处理，直接给TextField 加一个验证器即可 new RegexpValidator("\\d+"),判断是否符合
		inputExten=StringUtils.trimToEmpty((String)extenInputField.getValue());
		if(inputExten.equals("")|| !StringUtils.isNumeric(inputExten)){
			NotificationUtil.showWarningNotification(this,"分机号不能为空，必须全部是数字");
			return;
		}
		
		//检查分机的域
		List<String> extens = ShareData.domainToExts.get(domain.getId());
		if(!extens.contains(inputExten)){
			NotificationUtil.showWarningNotification(this,"分机号不存在或者您无权使用此分机");
			return;
		}
		
		//检查是否被占用
		if(ShareData.extenToUser.get(inputExten)!=null){
			NotificationUtil.showWarningNotification(this,"分机正在被坐席被使用");
			return;
		}
		
		//发起action定位到会议室
		// 呼叫Action
		OriginateAction originateAction = new OriginateAction();
		originateAction.setChannel("SIP/"+inputExten);
		originateAction.setCallerId(inputExten);
		originateAction.setExten("998"+inputExten);
		originateAction.setContext("outgoing");
		originateAction.setPriority(1);
		originateAction.setAsync(true);
		AmiManagerThread.sendAction(originateAction);
		
		//=============================设置锁定与解锁
		extenInputField.setEnabled(false);
		confirm.setEnabled(false);
		
		// 手机输入
		phoneNumberInputField.setValue(null);
		phoneNumberInputField.setEnabled(true);
		invitePhoneNumber.setEnabled(true);
		
		// 坐席选择
		csrSelectComboBox.setValue(null);
		csrSelectComboBox.setEnabled(true);
		inviteCsr.setEnabled(true);

		// 分机选择
		extenComboBox.setValue(null);
		extenComboBox.setEnabled(true);
		inviteExten.setEnabled(true);
	}

	//向手机发出加入会议室的邀请
	private void executePhoneNumber() {
		//检查输入是否合法
		String phoneNumber=StringUtils.trimToEmpty((String)phoneNumberInputField.getValue());
		//TODO  没有做号码的正则匹配
		if(phoneNumber.equals("")|| !StringUtils.isNumeric(phoneNumber)||phoneNumber.length()<7||phoneNumber.length()>13){
			NotificationUtil.showWarningNotification(this,"号码不能为空，必须全部是数字，位数必须正确");
			return;
		}
		
		//发起action定位到会议室
		String outline=ShareData.domainToDefaultOutline.get(domain.getId());
		invite("SIP/"+phoneNumber+"@"+outline,phoneNumber);
	}

	//邀请分机 
	private void executeInviteExten() {
		//检查输入是否合法
		String extenNumber=StringUtils.trimToEmpty((String)extenComboBox.getValue());
		if(extenNumber.equals("")){
			NotificationUtil.showWarningNotification(this,"请选择分机");
			return;
		}
		
		//发起action定位到会议室
		invite("SIP/"+extenNumber,extenNumber);
	}

	//邀请坐席
	private void executeInviteCsr() {
		User user=(User)csrSelectComboBox.getValue();
		if(user==null){
			NotificationUtil.showWarningNotification(this,"请选择坐席");
			return;
		}
		
		//检查输入是否合法
		String extenNumber=ShareData.userToExten.get(user.getId());
		if(extenNumber.equals("")){
			NotificationUtil.showWarningNotification(this,"选择的坐席没有登陆分机");
			return;
		}
		
		//发起action定位到会议室
		invite("SIP/"+extenNumber,extenNumber);
		
	}

	/**
	 * 发起邀请
	 * @return
	 */
	private Boolean invite(String channel,String callerId){
		try {
			// 呼叫Action
			OriginateAction originateAction = new OriginateAction();
			originateAction.setChannel(channel);
			originateAction.setCallerId(callerId);
			originateAction.setExten("998"+inputExten);
			originateAction.setContext("outgoing");
			originateAction.setPriority(1);
			originateAction.setAsync(true);
			AmiManagerThread.sendAction(originateAction);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// 监听按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == confirm) {
			//判断可用性，加入会议室
			try {
				executeConfirm();
			} catch (Exception e) {
				NotificationUtil.showWarningNotification(this,"创建会议室出现异常！");
				e.printStackTrace();
			}
		}else if (event.getButton() == reset) {
			//重置信息
			this.update();
		}else if (event.getButton() == invitePhoneNumber) {
			//向手机发出加入会议室的邀请
			executePhoneNumber();
			NotificationUtil.showWarningNotification(this,"邀请已经发出");
		}else if (event.getButton() == inviteCsr) {
			//邀请坐席
			executeInviteCsr();
			NotificationUtil.showWarningNotification(this,"邀请已经发出");
		}else if (event.getButton() == inviteExten) {
			//邀请分机 
			executeInviteExten();
			NotificationUtil.showWarningNotification(this,"邀请已经发出");
		}
	}

}
