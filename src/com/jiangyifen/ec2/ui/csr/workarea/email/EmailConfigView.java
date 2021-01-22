package com.jiangyifen.ec2.ui.csr.workarea.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionSystemException;

import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.email.service.MailConfigService;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * 配置邮箱 UI 界面
 */
@SuppressWarnings("serial")
public class EmailConfigView extends VerticalLayout implements Button.ClickListener{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 输入框
	private TextField txtSmtpHost;											// SMTP 服务器地址
	private TextField txtSmtpPort;											// SMTP 服务器端口
	private TextField txtSenderName;										// 发件人名称
	private TextField txtFromAddress;										// 发件人邮箱帐号
	private PasswordField txtSenderPassword;								// 发件人邮箱密码
	//private TextField txtIsDefault;										// 是否为系统默认邮箱帐号
	private Label lblDescription;											// 详情描述信息的 Label
	
	// 错误输出
	private VerticalLayout errorLayout;	
		
	// 按钮
	private Button btnSave;												// 保存按钮
	private Button btnCancel;												// 取消按钮
	private Button btnEdit;												// 编辑按钮
	
	// 其它
	private User loginUser;
	private MailConfigService mailConfigService;
	
	public EmailConfigView() {
		
		this.initService();
		this.setSizeFull();
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSizeUndefined();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);
		
		constrantLayout.addComponent(createGridLayout());
		constrantLayout.addComponent(createErrorLayout());
		constrantLayout.addComponent(createButtonsLayout());
		
		StringBuffer sbDescription = new StringBuffer();
		sbDescription.append("<br/><h3>说明</h3><b>SMTP 服务器：</b><ul><li>QQ企业邮箱帐号对应的SMTP服务器地址为 <b>smtp.exmail.qq.com</b></li>");
		sbDescription.append("<li>QQ普通邮箱对应的 SMTP 服务器地址为 <b>smtp.qq.com</b></li>");
		sbDescription.append("<li>新浪邮箱对应的 SMTP 服务器地址为 <b>smtp.sina.com</b></li>");
		sbDescription.append("<li>网易163邮箱对应的 SMTP 服务器地址为<b> smtp.163.com</b></li>");
		sbDescription.append("<li>网易126邮箱对应的 SMTP 服务器地址为 <b>smtp.126.com</b></li>");
		sbDescription.append("<li>搜狐邮箱对应的 SMTP 服务器地址为 <b>smtp.sohu.com</b></li></ul>");
		sbDescription.append("<b>SMTP 端口：</b>统一设置成 <b>25</b> ");
		lblDescription = new Label(sbDescription.toString(), Label.CONTENT_XHTML);
		lblDescription.setWidth("-1px");
		constrantLayout.addComponent(lblDescription);
		
	}
	
	@Override
	public void attach() {
		super.attach();
		update();
	}
	
	//ErrorLayout
	private VerticalLayout createErrorLayout() {
		errorLayout=new VerticalLayout();
		return errorLayout;
	}
		
	//ButtonsLayout
	private HorizontalLayout createButtonsLayout() {
		HorizontalLayout fullWidthLayout=new HorizontalLayout();

		// LeftLayout
		HorizontalLayout leftLayout = new HorizontalLayout();
		leftLayout.setSpacing(true);
		fullWidthLayout.addComponent(leftLayout);
		
		// RightLayout
		HorizontalLayout rightLayout = new HorizontalLayout();
		rightLayout.setSpacing(true);
		btnEdit = new Button("编辑", this);
		btnSave = new Button("保存", this);
		btnSave.setStyleName("default");
		btnCancel = new Button("撤销", this);
		Label lblSep = new Label(" ");
		lblSep.setWidth("-1px");
		rightLayout.addComponent(btnEdit);
		rightLayout.addComponent(btnSave);
		rightLayout.addComponent(lblSep);
		rightLayout.addComponent(btnCancel);
		fullWidthLayout.addComponent(rightLayout);
		fullWidthLayout.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);
		
		return fullWidthLayout;
	}
	
	/**
	 * 界面组件布局
	 * @return
	 */
	private GridLayout createGridLayout() {
		GridLayout gridLayout = new GridLayout(4,1);
		gridLayout.setSpacing(true);
		
		int column=-1;
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("SMTP服务器："), 0, column);  
		txtSmtpHost = new TextField();
		txtSmtpHost.setImmediate(true);
		txtSmtpHost.setWidth("240px");
		txtSmtpHost.setRequired(false);
		txtSmtpHost.setValue(null);
		txtSmtpHost.setNullRepresentation("");
		txtSmtpHost.setInputPrompt("SMTP服务器");
		gridLayout.addComponent(txtSmtpHost, 1, column);
		gridLayout.addComponent(new Label("<i>eg:smtp.exmail.qq.com</i>",Label.CONTENT_XHTML), 2, column);
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("SMTP端口："), 0, column);  
		txtSmtpPort = new TextField();
		txtSmtpPort.setImmediate(true);
		txtSmtpPort.setWidth("240px");
		txtSmtpPort.setRequired(false);
		txtSmtpPort.setValue(null);
		txtSmtpPort.setNullRepresentation("");
		txtSmtpPort.setInputPrompt("SMTP端口");
		gridLayout.addComponent(txtSmtpPort, 1, column);
		gridLayout.addComponent(new Label("<i>eg:25</i>",Label.CONTENT_XHTML), 2, column);
		
		// txtSmtpAuth
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("邮箱昵称："), 0, column);
		txtSenderName = new TextField();
		txtSenderName.setImmediate(true);
		txtSenderName.setWidth("240px");
		txtSenderName.setRequired(false);
		txtSenderName.setValue(null);
		txtSenderName.setNullRepresentation("");
		txtSenderName.setInputPrompt("邮箱昵称");
		gridLayout.addComponent(txtSenderName, 1, column);
		gridLayout.addComponent(new Label("<i>eg:小A</i>", Label.CONTENT_XHTML), 2, column);
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("邮箱账号："), 0, column);  
		txtFromAddress = new TextField();
		txtFromAddress.setImmediate(true);
		txtFromAddress.setWidth("240px");
		txtFromAddress.setRequired(false);
		txtFromAddress.setValue(null);
		txtFromAddress.setNullRepresentation("");
		txtFromAddress.setInputPrompt("邮箱账号");
		gridLayout.addComponent(txtFromAddress, 1, column);
		gridLayout.addComponent(new Label("<i>eg:xxx@qq.com</i>",Label.CONTENT_XHTML), 2, column);
		
		column++;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(new Label("邮箱密码："), 0, column);  
		txtSenderPassword = new PasswordField();
		txtSenderPassword.setImmediate(true);
		txtSenderPassword.setWidth("240px");
		txtSenderPassword.setRequired(false);
		txtSenderPassword.setInputPrompt("");	// 邮箱密码
		txtSenderPassword.setNullRepresentation("");
		txtSenderPassword.setValue(null);
		txtSenderPassword.setDescription("如果不填写，则在每次发送邮件的时候都提示要输入密码！");
		gridLayout.addComponent(txtSenderPassword, 1, column);
		gridLayout.addComponent(new Label("<i>eg:123456</i>",Label.CONTENT_XHTML), 2, column);
		
		return gridLayout;
	}

	// 按钮事件处理
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnCancel){
			errorLayout.removeAllComponents();
			update(); //回显
		}else if(source == btnSave){
			errorLayout.removeAllComponents();
			executeSave();
		} else if(source == btnEdit) {
			updateButtons(false);
		}
	}
		
	private void executeSave() {
		errorLayout.removeAllComponents();
		
		// SMTP 服务器
		String txtSmtpHostStr=(String)txtSmtpHost.getValue();
		txtSmtpHostStr=txtSmtpHostStr==null?"":txtSmtpHostStr;
//		if(!txtSmtpHostStr.matches("\\w+")){
//			errorLayout.addComponent(new Label("<font color='red'>SMTP服务器\\w+</font>",Label.CONTENT_XHTML));
//			return;
//		}
		
		// SMTP 端口
		String txtSmtpPortStr=(String)txtSmtpPort.getValue();
		txtSmtpPortStr=txtSmtpPortStr==null?"":txtSmtpPortStr;
		if(!txtSmtpPortStr.matches("\\d{1,5}")){
			errorLayout.addComponent(new Label("<font color='red'>SMTP端口格式不正确</font>",Label.CONTENT_XHTML));
			return;
		}
		
		// 邮箱昵称
		String txtSenderNameStr = (String) txtSenderName.getValue();
		txtSenderNameStr = txtSenderNameStr == null ? "" : txtSenderNameStr;
		
		// 邮箱帐号
		String txtFromAddressStr=(String)txtFromAddress.getValue();
		txtFromAddressStr=txtFromAddressStr==null?"":txtFromAddressStr;
		if(!txtFromAddressStr.matches("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$")){
			errorLayout.addComponent(new Label("<font color='red'>邮箱账号格式不正确</font>",Label.CONTENT_XHTML));
			return;
		}
		
		// 邮箱密码
		String txtSenderPasswordStr=(String)txtSenderPassword.getValue();
		txtSenderPasswordStr=txtSenderPasswordStr==null?"":txtSenderPasswordStr;
		
		// 获取配置
		MailConfig mailConfig = mailConfigService.getMailConfigByUser(SpringContextHolder.getLoginUser());
		Boolean isMailConfigExists=mailConfig==null?false:true;
		mailConfig=mailConfig==null?new MailConfig():mailConfig; //认为两个页面不会同时操作
		
		// 设置配置
		mailConfig.setSmtpHost(txtSmtpHostStr);
		mailConfig.setSmtpPort(txtSmtpPortStr);
		mailConfig.setFromAddress(txtFromAddressStr);
		mailConfig.setSenderPassword(txtSenderPasswordStr);
		mailConfig.setIsDefault("false"); //default
		
		if(!"".equals(txtSenderNameStr.trim())) {
			mailConfig.setSenderName(txtSenderNameStr);
		} else {
			mailConfig.setSenderName(txtFromAddressStr); //same as from address
		}
		
		mailConfig.setSmtpAuth("true"); //default
		mailConfig.setUser(loginUser);
		mailConfig.setDomain(loginUser.getDomain());
		
		// 保存配置
		try {
			if(isMailConfigExists){
				mailConfigService.update(mailConfig);
				NotificationUtil.showWarningNotification(this.getApplication(), "更新成功");
			}else{
				mailConfigService.save(mailConfig);
				NotificationUtil.showWarningNotification(this.getApplication(), "保存成功");
			}
			updateButtons(true);
		} catch (TransactionSystemException e) {
			NotificationUtil.showWarningNotification(this.getApplication(), "该邮箱地址已在系统中存在，保存失败！");
			logger.error("jinht -->> 保存或修改配置邮箱信息的时候失败！"+e.getMessage(), e);
		} catch (Exception e) {
			logger.error("jinht -->> 保存或修改配置邮箱信息的时候失败！"+e.getMessage(), e);
			NotificationUtil.showWarningNotification(this.getApplication(), "配置邮箱信息填写有误，保存失败！");
		}
	}

	/**
	 * 初始化服务类
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		mailConfigService = SpringContextHolder.getBean("mailConfigService");
	}
	
	/**
	 * 回显邮件配置信息
	 */
	public void update() {
		updateButtons(false);
		//回显
		MailConfig mailConfig = mailConfigService.getMailConfigByUser(loginUser);
		if(mailConfig==null){
			txtSmtpHost.setValue(null);
			txtSmtpPort.setValue(null);
			txtFromAddress.setValue(null);
			txtSenderPassword.setValue(null);
			txtSenderName.setValue(null);
		}else{
			txtSmtpHost.setValue(mailConfig.getSmtpHost());
			txtSmtpPort.setValue(mailConfig.getSmtpPort());
			txtFromAddress.setValue(mailConfig.getFromAddress());
			txtSenderPassword.setValue(mailConfig.getSenderPassword());
			txtSenderName.setValue(mailConfig.getSenderName());
		}
		updateButtons(true);
	}
	
	/**
	 * 修改按钮组件显示状态 
	 */
	private void updateButtons(Boolean isVisible) {
		btnEdit.setVisible(isVisible);
		btnSave.setVisible(!isVisible);
		btnCancel.setVisible(!isVisible);
		
		txtSmtpHost.setReadOnly(isVisible);
		txtSmtpPort.setReadOnly(isVisible);
		txtSenderName.setReadOnly(isVisible);
		txtFromAddress.setReadOnly(isVisible);
		txtSenderPassword.setReadOnly(isVisible);
	}
	
}

