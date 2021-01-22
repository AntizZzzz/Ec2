package com.jiangyifen.ec2.ui.mgr.tabsheet.email;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.TransactionSystemException;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.email.entity.MailContact;
import com.jiangyifen.ec2.email.service.MailContactService;
import com.jiangyifen.ec2.email.service.MailService;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrTabSheet;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 邮件发送 UI 界面 
 */
@SuppressWarnings("serial")
public class EmailSendView extends VerticalLayout implements Button.ClickListener{
	
/**
 * 主要组件
 */
	//消息组件
	private TextField tfReceiver;		// 收件人
	private TextField tfCopied;		// 抄送
	private TextField tfTitle;			// 主题
    private RichTextArea rtaContent;
    private Button btnRemoveAll;
    private Button btnViewHistory;
    private Button btnSendEmail;
    
	/**
	 *  其它组件   
	 */
    private MailConfig mailConfig;
    private User loginUser;
	private DepartmentService departmentService;
	private MailService mailService;
    private MgrTabSheet mgrTabSheet;
    
	/**
	 * 构造器
	 */
	public EmailSendView() {
		this.setSizeFull();
		
		loginUser = SpringContextHolder.getLoginUser();
		mailService = SpringContextHolder.getBean("mailService");
		departmentService = SpringContextHolder.getBean("departmentService");
		
		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setHeight("100%");
		constrantLayout.setMargin(true);
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		//创建新建通知组件
		VerticalLayout tempNoticeLayout = buildNewNoticeLayout();
		constrantLayout.addComponent(tempNoticeLayout);
		tempNoticeLayout.setHeight("100%");
		constrantLayout.setExpandRatio(tempNoticeLayout, 10.0f);
		// TODO
		MailContactView mailContactView = new MailContactView(this);
		mailContactView.setHeight("100%");
		constrantLayout.addComponent(mailContactView);
		constrantLayout.setExpandRatio(mailContactView, 3.0f);
		//让allCsrs中有数据
		this.updateCsrInfo();
	}
	
	/**
	 * 显示新建消息输出的页面
	 * @return
	 */
	private VerticalLayout buildNewNoticeLayout() {
		VerticalLayout newNoticeLayout=new VerticalLayout();
		newNoticeLayout.setSpacing(true);
		newNoticeLayout.setWidth("100%");
		/* 收件人 */
		HorizontalLayout constraintReceiverLayout=new HorizontalLayout();
		constraintReceiverLayout.setWidth("100%");
		
		Label lblReceiver = new Label("收件人：");
		lblReceiver.setWidth("-1px");
		lblReceiver.setSizeUndefined();
		constraintReceiverLayout.addComponent(lblReceiver);
		
		tfReceiver=new TextField();
		tfReceiver.setImmediate(true);
		tfReceiver.setWidth("100%");
		constraintReceiverLayout.addComponent(tfReceiver);
		constraintReceiverLayout.setExpandRatio(tfReceiver, 1.0f);
		
		btnRemoveAll=new Button("清除全部");
		btnRemoveAll.setStyleName("small");
		btnRemoveAll.addListener((Button.ClickListener)this);
		constraintReceiverLayout.addComponent(btnRemoveAll);
		newNoticeLayout.addComponent(constraintReceiverLayout);
		
		/* 抄送人 */
		HorizontalLayout constraintCopiedLayout = new HorizontalLayout();
		constraintCopiedLayout.setWidth("100%");
		
		Label lblCopied = new Label("抄送人：", Label.CONTENT_XHTML);
		lblCopied.setWidth("-1px");
		lblCopied.setSizeUndefined();
		constraintCopiedLayout.addComponent(lblCopied);
		
		tfCopied = new TextField(); 
		tfCopied.setImmediate(true);
		tfCopied.setWidth("100%");
		constraintCopiedLayout.addComponent(tfCopied);
		constraintCopiedLayout.setExpandRatio(tfCopied, 1.0f);
		newNoticeLayout.addComponent(constraintCopiedLayout);
		
		/* 标题栏 */
		HorizontalLayout constraintTitleLayout=new HorizontalLayout();
		constraintTitleLayout.setWidth("100%");
		
		Label lblTitle = new Label("主　题：", Label.CONTENT_XHTML);
		lblTitle.setWidth("-1px");
		lblTitle.setSizeUndefined();
		constraintTitleLayout.addComponent(lblTitle);
		
		tfTitle = new TextField();
		tfTitle.setImmediate(true);
		tfTitle.setWidth("100%");
		constraintTitleLayout.addComponent(tfTitle);
		constraintTitleLayout.setExpandRatio(tfTitle, 1.0f);
		newNoticeLayout.addComponent(constraintTitleLayout);
		
		//消息内容
		rtaContent = new RichTextArea();
		rtaContent.setImmediate(true);
		rtaContent.setSizeFull();
		rtaContent.setValue("");
		newNoticeLayout.addComponent(rtaContent);
		newNoticeLayout.setExpandRatio(rtaContent, 1.0f);
		
		//按钮输出
		HorizontalLayout constraintButtonsLayout=new HorizontalLayout();
		constraintButtonsLayout.setSpacing(true);
		newNoticeLayout.addComponent(constraintButtonsLayout);
		newNoticeLayout.setComponentAlignment(constraintButtonsLayout, Alignment.BOTTOM_LEFT);
		
		//按钮
		btnSendEmail=new Button("发 送");
		btnSendEmail.setStyleName("default");
		btnSendEmail.addListener((Button.ClickListener)this);
		constraintButtonsLayout.addComponent(btnSendEmail);
		
		return newNoticeLayout;
	}
	
	
	/**
	 * 发送邮件
	 */
	private void executeSend() {
		//对接收人栏的处理
		String toAddresses="";
		if(tfReceiver.getValue()!=null){
			toAddresses=tfReceiver.getValue().toString().trim();
		}
		if(toAddresses.equals("")){
			this.getApplication().getMainWindow().showNotification("请先添加收件人", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		// 抄送人
		String ccAddresses = "";
		if(tfCopied.getValue() != null) {
			ccAddresses = tfCopied.getValue().toString().trim();
		}
		
		//标题
		String titleStr="";
		if(tfTitle.getValue()!=null){
			titleStr=tfTitle.getValue().toString();
		}
		if(titleStr.trim().equals("")){
			this.getApplication().getMainWindow().showNotification("邮件主题不能为空", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		//内容
		String contentStr = StringUtils.trimToEmpty((String) rtaContent.getValue());
		if("".equals(contentStr)){
			this.getApplication().getMainWindow().showNotification("邮件内容不能为空", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		//发送邮件并提示消息
		try {
			mailConfig = mailService.findMailConfigByUserId(loginUser.getId());
			
			// 如果发件人密码没有填写，则需要在这里进行做一个判断输入邮箱密码
			if(mailConfig != null) {
				
				if(mailConfig.getSenderPassword() == null || "".equals(mailConfig.getSenderPassword())) {
					FillEmailConfig fillEmailConfig = new FillEmailConfig(this);
					this.getApplication().getMainWindow().addWindow(fillEmailConfig);
				} else {
					Boolean isSuccess = mailService.sendEmail(loginUser, loginUser, titleStr, contentStr, null, toAddresses, ccAddresses, null);
					if(isSuccess) {
						NotificationUtil.showWarningNotification(this,"发送邮件成功!");
						/**
						 * 清空收件人，标题，内容
						 * 如果发送成功清除这些信息，发送失败就没比较进行清空咯
						 */
						tfReceiver.setValue("");
						tfCopied.setValue("");
						tfTitle.setValue("");
						rtaContent.setValue("");
					} else {
						NotificationUtil.showWarningNotification(this,"发送邮件失败!");
					}
				}
				
			}
			
		} catch (TransactionSystemException e) {
			NotificationUtil.showWarningNotification(this, "文本内容不得超过 4096 个字符！");
		} catch (Exception e) {
			NotificationUtil.showWarningNotification(this,e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 由弹出的输入邮箱密码窗口调用
	 * @param password		邮箱密码
	 * @param titleStr		邮件主题
	 * @param contentStr	邮件内容
	 * @param attachFiles	邮件附件
	 * @param toAddresses	收件人地址
	 * @param ccAddresses	抄送人地址
	 */
	public void sendAddPasswordEmail(String password) {
		// 收件人
		String toAddresses="";
		if(tfReceiver.getValue()!=null){
			toAddresses=tfReceiver.getValue().toString().trim();
		}
		if(toAddresses.equals("")){
			this.getApplication().getMainWindow().showNotification("请先添加收件人", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		// 抄送人
		String ccAddresses = "";
		if(tfCopied.getValue() != null) {
			ccAddresses = tfCopied.getValue().toString().trim();
		}
		
		//标题
		String titleStr="";
		if(tfTitle.getValue()!=null){
			titleStr=tfTitle.getValue().toString();
		}
		if(titleStr.trim().equals("")){
			this.getApplication().getMainWindow().showNotification("邮件主题不能为空", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		//内容
		String contentStr = StringUtils.trimToEmpty((String) rtaContent.getValue());
		if("".equals(contentStr)){
			this.getApplication().getMainWindow().showNotification("邮件内容不能为空", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		Boolean isSuccess = mailService.sendEmail(loginUser, loginUser, titleStr, contentStr, null, toAddresses, ccAddresses, password);
		if(isSuccess) {
			NotificationUtil.showWarningNotification(this,"发送邮件成功!");
			/**
			 * 将Csr全部置空,并清空收件人，标题，内容
			 * 如果发送成功清除这些信息，发送失败就没比较进行清空咯
			 */
			tfReceiver.setValue("");
			tfCopied.setValue("");
			tfTitle.setValue("");
			rtaContent.setValue("");
		} else {
			NotificationUtil.showWarningNotification(this, "发送邮件失败！");
		}
	}
	
	/**
	 * 有可能新添了Csr，更新Csr的信息
	 */
	public void updateCsrInfo() {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for (Department dept : departments) {
						Long deptId = dept.getId();
						if (!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 添加一个CSR
	 */
	public void addSelectedCsr(String empNo) {
		//获取已经选中的Csr的字符串
		String receiverStr="";
		if(tfReceiver.getValue()!=null){
			receiverStr=tfReceiver.getValue().toString().trim();
		}
		//添加Csr
		if(receiverStr.equals("")){
			receiverStr+=empNo;
		}else{
			receiverStr+=","+empNo;
		}
		tfReceiver.setValue(receiverStr);
	}
	/**
	 * 添加一个部门的CSR
	 */
	public void addSelectedCsrs(List<User> selectedCsrs) {
		//获取已经选中的Csr的字符串
		String receiverStr="";
		if(tfReceiver.getValue()!=null){
			receiverStr=tfReceiver.getValue().toString().trim();
		}
		//判断是否已经存在，如果存在，不处理，否则添加新的EmpNo
		for(User user:selectedCsrs){
			if(receiverStr.indexOf(user.getEmpNo())==-1){
				if(receiverStr.equals("")){
					receiverStr+=user.getEmpNo();
				}else{
					receiverStr+=","+user.getEmpNo();
				}
			}
			//否则不做任何处理
		}
		tfReceiver.setValue(receiverStr);
	}
	
	/**
	 * 添加Csr按钮单击事件
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnRemoveAll){
			tfReceiver.setValue("");
		}else if(source == btnSendEmail){
			executeSend();
		}else if(source == btnViewHistory){
			mgrTabSheet.showHistoryNotice(false);
		}
	}

	// 添加联系人
	public void addToReceiverComponent(String buttonData) {
		String receiverStr=tfReceiver.getValue()==null?"":(String)tfReceiver.getValue();
		receiverStr=receiverStr.trim();
		
		if(receiverStr.contains(buttonData)){
			// already exist
		}else if("".equals(receiverStr)){
			receiverStr+=buttonData;
		}else{
			receiverStr+=","+buttonData;
		}
		tfReceiver.setValue(receiverStr);
	}

	public void update() {
		// TODO Auto-generated method stub
		
	}

	public MailConfig getMailConfig() {
		return mailConfig;
	}

}

//右侧视图
@SuppressWarnings("serial")
class MailContactView  extends HorizontalLayout implements Button.ClickListener {
	private EmailSendView emailSendView;
	private Panel panel;
	
	public MailContactView(EmailSendView emailSendView) {
		this.emailSendView=emailSendView;
		this.setWidth("100%");
		this.setSpacing(true);

		panel=new Panel();
		panel.setSizeFull();
		refresh();
		this.addComponent(panel);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		String buttonData=(String)event.getButton().getData();
		if("addContact".equals(buttonData)){
			AddMailContact addMailContact=new AddMailContact(this);
			this.getWindow().addWindow(addMailContact);
			return;
		}
		//其它为添加联系人
		if(!StringUtils.isBlank(buttonData)){
			emailSendView.addToReceiverComponent(buttonData);
		}
	}
	
	public void refresh() {
		panel.removeAllComponents();
		
		MailContactService mailContactService=SpringContextHolder.getBean("mailContactService");
		List<MailContact> contacts = mailContactService.getMailContactsByUserId(SpringContextHolder.getLoginUser().getId());
		if(contacts.size()==0){
			Button addContact=new Button("添加联系人",this);
			addContact.setStyleName(BaseTheme.BUTTON_LINK);
			addContact.setData("addContact");
			panel.addComponent(addContact);
			panel.addComponent(new Label("----------------------"));
			panel.addComponent(new Label("<b>无联系人</b>",Label.CONTENT_XHTML));
		}else{
			Button addContact=new Button("添加联系人",this);
			addContact.setStyleName(BaseTheme.BUTTON_LINK);
			addContact.setData("addContact");
			panel.addComponent(addContact);
			panel.addComponent(new Label("----------------------"));
			for(MailContact c:contacts){
				HorizontalLayout hl=new HorizontalLayout();
				panel.addComponent(hl);
				
				String caption=StringUtils.isBlank(c.getEmailAddress())?c.getEmailAddress():c.getEmailAddress(); //+"-"+c.getDescription();
				Button contactButton=new Button(caption,this);
				String emailAddress = "";
				if(c.getDescription() != null && !"".equals(c.getDescription())) {
					emailAddress = c.getDescription()+"<"+c.getEmailAddress()+">";
					contactButton.setDescription(c.getDescription());
				} else {
					emailAddress = c.getEmailAddress();
				}
				contactButton.setData(emailAddress);
				contactButton.setStyleName(BaseTheme.BUTTON_LINK);
				hl.addComponent(contactButton);
				
				hl.addComponent(new Label("&nbsp;/&nbsp;",Label.CONTENT_XHTML));
				
				Button delButton=new Button("删除",new Button.ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						Long contactId=(Long)event.getButton().getData();
						MailContactService mailContactService=SpringContextHolder.getBean("mailContactService");
						mailContactService.deleteById(contactId);
						refresh();
					}
				});
				delButton.setData(c.getId());
				delButton.setStyleName(BaseTheme.BUTTON_LINK);
				hl.addComponent(delButton);
				
			}
		}
	}
	
}

@SuppressWarnings("serial")
class AddMailContact extends Window implements Button.ClickListener {

	private TextField txtEmailAddress;
	private TextField txtDescription;
	
	private VerticalLayout errorLayout;	
	private VerticalLayout customViewLayout;
	
	// 按钮
	private Button btnSave;												// 保存按钮
	private Button btnCancel;											// 取消按钮
	
	private MailContactView mailContactView;
	
	public AddMailContact(MailContactView mailContactView) {
		this.mailContactView=mailContactView;
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setSizeUndefined();
		this.setCaption("添加联系人");
		
		//custom view
		// 添加Widnow内最大的 Layout
		customViewLayout = new VerticalLayout();
		customViewLayout.setSpacing(true);
		customViewLayout.setSizeUndefined();
		customViewLayout.setMargin(false, true, true, true);
		this.setContent(customViewLayout);
		// 创建各组件
		customViewLayout.addComponent(createGridLayout());
		customViewLayout.addComponent(createErrorLayout());
		customViewLayout.addComponent(createButtonsLayout());

	}
	
	// GridLayout
	private GridLayout createGridLayout(){
		GridLayout gridLayout = new GridLayout(2,1);
		gridLayout.setSpacing(true);
		
		int column=-1;
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("邮箱地址："), 0, column);
		
		txtEmailAddress = new TextField();
		txtEmailAddress.setWidth("160px");
		txtEmailAddress.setRequired(true);
		txtEmailAddress.setValue("");
		txtEmailAddress.setNullRepresentation("");
		gridLayout.addComponent(txtEmailAddress, 1, column);
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("描述信息："), 0, column);
		
		txtDescription = new TextField();
		txtDescription.setWidth("160px");
		//txtDescription.setRequired(true);
		txtDescription.setValue("");
		txtDescription.setNullRepresentation("");
		gridLayout.addComponent(txtDescription, 1, column);
		return gridLayout;
	}
	
	private VerticalLayout createErrorLayout() {
		errorLayout=new VerticalLayout();
		return errorLayout;
	}
	
	private HorizontalLayout createButtonsLayout() {
		HorizontalLayout fullWidthLayout=new HorizontalLayout();
		fullWidthLayout.setWidth("100%");

		// LeftLayout
		HorizontalLayout leftLayout = new HorizontalLayout();
		leftLayout.setSpacing(true);
		fullWidthLayout.addComponent(leftLayout);
		
		// RightLayout
		HorizontalLayout rightLayout = new HorizontalLayout();
		rightLayout.setSpacing(true);
		btnSave = new Button("保存", this);
		btnSave.setStyleName("default");
		btnCancel = new Button("取消", this);
		rightLayout.addComponent(btnSave);
		rightLayout.addComponent(btnCancel);
		fullWidthLayout.addComponent(rightLayout);
		fullWidthLayout.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);
		
		return fullWidthLayout;
	}
	
	// 执行保存
	private void executeSave() {
		errorLayout.removeAllComponents();
		String emailAddress=(String)txtEmailAddress.getValue();
		emailAddress=emailAddress==null?"":emailAddress;
		if(!emailAddress.matches("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$")){
			errorLayout.addComponent(new Label("<font color='red'>邮箱格式不正确</font>",Label.CONTENT_XHTML));
			return;
		}
		
		String description=(String)txtDescription.getValue();
		description=description==null?"":description;
		MailContact mailContact=new MailContact();
		mailContact.setDescription(description);
		mailContact.setEmailAddress(emailAddress);
		mailContact.setUser(SpringContextHolder.getLoginUser());
		mailContact.setDomain(SpringContextHolder.getLoginUser().getDomain());
		
		try {
			MailContactService mailContactService=SpringContextHolder.getBean("mailContactService");
			mailContactService.save(mailContact);
			mailContactView.refresh();
			this.getParent().removeWindow(this);
		} catch (Exception e) {
			this.getApplication().getMainWindow().showNotification("保存失败");
			e.printStackTrace();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnCancel){
			errorLayout.removeAllComponents();
			this.getParent().removeWindow(this);
		}else if(source == btnSave){
			errorLayout.removeAllComponents();
			executeSave();
		}
	}
	
}


@SuppressWarnings("serial")
class FillEmailConfig extends Window implements Button.ClickListener {

	private TextField tfEmailUserName;			// 邮箱名称
	private PasswordField tfEmailPassword;		// 邮箱密码
	
	private VerticalLayout errorLayout;	
	private VerticalLayout customViewLayout;
	
	// 按钮
	private Button btnSave;											// 保存按钮
	private Button btnCancel;											// 取消按钮
	
	private EmailSendView emailSendView;										// 邮箱的配置信息
	
	public FillEmailConfig(EmailSendView emailSendView) {
		this.emailSendView = emailSendView;
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setSizeUndefined();
		this.setCaption("配置邮箱");
		this.setDescription("这里的保存，并不会把您的邮箱密码做任何记录保存上的操作，只是用来发送本次邮件，如果您想在下次发送邮件的时候不在弹出此窗口提示输入密码，可以在<font color='blue'>配置邮箱</font>管理界面进行配置邮箱的密码！");
		
		//custom view
		// 添加Widnow内最大的 Layout
		customViewLayout = new VerticalLayout();
		customViewLayout.setSpacing(true);
		customViewLayout.setSizeUndefined();
		customViewLayout.setMargin(false, true, true, true);
		this.setContent(customViewLayout);
		// 创建各组件
		customViewLayout.addComponent(createGridLayout());
		customViewLayout.addComponent(createErrorLayout());
		customViewLayout.addComponent(createButtonsLayout());

	}
	
	// GridLayout
	private GridLayout createGridLayout(){
		GridLayout gridLayout = new GridLayout(2,1);
		gridLayout.setSpacing(true);
		
		int column=-1;
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("邮箱地址："), 0, column);
		
		tfEmailUserName = new TextField();
		tfEmailUserName.setWidth("160px");
		tfEmailUserName.setRequired(true);
		tfEmailUserName.setValue("");
		tfEmailUserName.setNullRepresentation("");
		gridLayout.addComponent(tfEmailUserName, 1, column);
		
		if(emailSendView.getMailConfig().getFromAddress() != null) {
			tfEmailUserName.setValue(emailSendView.getMailConfig().getFromAddress());
			tfEmailUserName.setReadOnly(true);
		} else {
			tfEmailUserName.setReadOnly(false);
		}
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("邮箱密码："), 0, column);
		
		tfEmailPassword = new PasswordField();
		tfEmailPassword.setWidth("160px");
		tfEmailPassword.setImmediate(true);
		tfEmailPassword.setValue("");
		tfEmailPassword.setNullRepresentation("");
		gridLayout.addComponent(tfEmailPassword, 1, column);
		return gridLayout;
	}
	
	private VerticalLayout createErrorLayout() {
		errorLayout=new VerticalLayout();
		return errorLayout;
	}
	
	private HorizontalLayout createButtonsLayout() {
		HorizontalLayout fullWidthLayout=new HorizontalLayout();
		fullWidthLayout.setWidth("100%");

		// LeftLayout
		HorizontalLayout leftLayout = new HorizontalLayout();
		leftLayout.setSpacing(true);
		fullWidthLayout.addComponent(leftLayout);
		
		// RightLayout
		HorizontalLayout rightLayout = new HorizontalLayout();
		rightLayout.setSpacing(true);
		btnSave = new Button("保存", this);
		btnSave.setDescription("这里的保存，并不会把您的邮箱密码做任何记录保存上的操作，只是用来发送本次邮件，如果您想在下次发送邮件的时候不在弹出此窗口提示输入密码，可以在<font color='blue'>配置邮箱</font>管理界面进行配置邮箱的密码！");
		btnSave.setStyleName("default");
		btnCancel = new Button("取消", this);
		rightLayout.addComponent(btnSave);
		rightLayout.addComponent(btnCancel);
		fullWidthLayout.addComponent(rightLayout);
		fullWidthLayout.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);
		
		return fullWidthLayout;
	}
	
	// 执行保存
	private void executeSave() {
		errorLayout.removeAllComponents();
		String emailAddress=(String)tfEmailUserName.getValue();
		emailAddress=emailAddress==null?"":emailAddress;
		if(!emailAddress.matches("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$")){
			errorLayout.addComponent(new Label("<font color='red'>邮箱格式不正确</font>",Label.CONTENT_XHTML));
			return;
		}
		
		if(tfEmailPassword.getValue() == null || "".equals(tfEmailPassword.getValue())) {
			errorLayout.addComponent(new Label("<font color='red'>邮箱密码必须必须填写</font>",Label.CONTENT_XHTML));
			return;
		}
		
		try {
			emailSendView.sendAddPasswordEmail((String)tfEmailPassword.getValue());
			this.getParent().removeWindow(this);
		} catch (Exception e) {
			this.getApplication().getMainWindow().showNotification("保存失败");
			e.printStackTrace();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnCancel){
			errorLayout.removeAllComponents();
			this.getParent().removeWindow(this);
		}else if(source == btnSave){
			executeSave();
		}
	}

	public TextField getTfEmailUserName() {
		return tfEmailUserName;
	}

	public PasswordField getTfEmailPassword() {
		return tfEmailPassword;
	}
	
}




