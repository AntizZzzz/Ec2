package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MessageTemplate;
import com.jiangyifen.ec2.entity.SmsPhoneNumber;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.entity.enumtype.SmsPhoneNumberType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.MessageTemplateService;
import com.jiangyifen.ec2.sms.SmsUtil;
import com.jiangyifen.ec2.ui.mgr.messagetemplatemanage.AddContactsWindow;
import com.jiangyifen.ec2.ui.mgr.messagetemplatemanage.EditContactsWindow;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
 

@SuppressWarnings("serial")
public class SendMessageManage extends VerticalLayout implements
		ValueChangeListener, ClickListener, Action.Handler {

	/**
	 * 主要组件
	 */
	private TextField receiver;
	private ComboBox messageTemplate;
	private TextArea content;
	private Button send;
	private Button refresh;
	private TabSheet tabSheet;
	
	private Button addContacts;
	private Button editContacts;
	private Button removeContacts;
	
	private Table tableLeft;
//	private Table tableRight;

	//Serivce
	private CommonService commonService;
	private MessageTemplateService messageTemplateService;
	private Domain domain;
	
	//Window
	private AddContactsWindow addContactsWindow;
	private EditContactsWindow editContactsWindow;

	/**
	 * 右键组件
	 */
	private Action ADD = new Action("添加到接收人");
	private Action[] ACTIONS = new Action[] { ADD};
	
	public SendMessageManage() {
		this.setSizeFull();
		domain = SpringContextHolder.getDomain();
		commonService =SpringContextHolder.getBean("commonService");
		messageTemplateService=SpringContextHolder.getBean("messageTemplateService");
		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setHeight("45%");
		constrantLayout.setMargin(true);
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 创建左侧组件
		VerticalLayout leftLayout = buildLeftLayout();
		constrantLayout.addComponent(leftLayout);
		leftLayout.setHeight("100%");
		constrantLayout.setExpandRatio(leftLayout, 7.0f);
		// 右侧历史短信组件  TODO 以后添加
		@SuppressWarnings("unused")
		TabSheet tabSheet=buildTabSheet();
//		constrantLayout.addComponent(tabSheet);
//		constrantLayout.setExpandRatio(tabSheet, 3.0f);
	}

	@SuppressWarnings("deprecation")
	private VerticalLayout buildLeftLayout() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setWidth("100%");

		// 接收者号码组件
		HorizontalLayout constraintClientLayout = new HorizontalLayout();
		constraintClientLayout.setWidth("100%");
		Label tempLabel = new Label("接收人:");
		tempLabel.setSizeUndefined();
		constraintClientLayout.addComponent(tempLabel);
		receiver = new TextField();
		receiver.setInputPrompt("电话号码格式必须正确并用英文格式的逗号隔开");
		receiver.setRows(4);
		receiver.setWidth("100%");
		constraintClientLayout.addComponent(receiver);
		constraintClientLayout.setExpandRatio(receiver, 1.0f);
		layout.addComponent(constraintClientLayout);

		// 短信模板组件
		HorizontalLayout template = new HorizontalLayout();
		Label label = new Label("短信模板:");
		messageTemplate = new ComboBox();
		messageTemplate.addListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				if(null!=messageTemplate.getValue()){
					MessageTemplate messageT=(MessageTemplate)messageTemplate.getValue();
					content.setValue(messageT.getContent());
					content.setImmediate(true);
				}
			}
		});
		template.addComponent(label);
		template.addComponent(messageTemplate);
		
		refresh=new Button("刷新");
		refresh.addListener((Button.ClickListener)this);
		template.addComponent(refresh);
		layout.addComponent(template);

		// 短信内容
		content = new TextArea();
		content.setSizeFull();
		content.setInputPrompt("请输入要发送的内容");
		layout.addComponent(content);
		layout.setExpandRatio(content, 1.0f);

		// 按钮输出
		HorizontalLayout constraintButtonsLayout = new HorizontalLayout();
		layout.addComponent(constraintButtonsLayout);
		
		send = new Button("发送");
		send.addListener((Button.ClickListener) this);
		constraintButtonsLayout.addComponent(send);
		return layout;
	}

	@Override
	public void attach() {
		this.update();
		this.refreshTabSheet();
		editContacts.setEnabled(false);
		removeContacts.setEnabled(false);
		
		//更新选择的模板的显示
		BeanItemContainer<MessageTemplate> templateContainer=new BeanItemContainer<MessageTemplate>(MessageTemplate.class);
		List<MessageTemplate> templates=messageTemplateService.getAllByDomain(SpringContextHolder.getDomain());
		templateContainer.addAll(templates);
		messageTemplate.setContainerDataSource(templateContainer);
		messageTemplate.setItemCaptionPropertyId("title");
	}
	
//更新相关信息
	public void update() {
		refreshTabSheet();
	}
	
	/**
	 * 显示添加联系人的窗口
	 */
	private void showAddContactsWindow() {
		if (addContactsWindow == null) {
			try {
				
				addContactsWindow = new AddContactsWindow(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
				return;
			}
		}
		this.getApplication().getMainWindow().removeWindow(addContactsWindow);
		this.getApplication().getMainWindow().addWindow(addContactsWindow);
	}

	/**
	 * 显示编辑联系人的窗口
	 */
	private void showEditContactsWindow() {
		if (editContactsWindow == null) {
			try {
				editContactsWindow = new EditContactsWindow(this);
			} catch (Exception e) {
				e.printStackTrace();
				this.getApplication().getMainWindow().showNotification("弹出窗口失败");
				return;
			}
		}
		this.getApplication().getMainWindow().removeWindow(editContactsWindow);
		this.getApplication().getMainWindow().addWindow(editContactsWindow);
	}
	
	//通讯录
	private TabSheet buildTabSheet() {
		tabSheet=new TabSheet();
		tabSheet.setSizeFull();
		
		//添加通讯录和最近联系
		Panel panelLeft=buildPanelLeft();
		tabSheet.addComponent(panelLeft);
//		Panel panelRight=buildPanelRight();
//		tabSheet.addComponent(panelRight);
		
		return tabSheet;
	}

//	//创建右侧
//	private Panel buildPanelRight() {
//		Panel panelRight=new Panel("最近联系");
//		tableRight=new Table();
//		tableRight.setImmediate(true);
//		tableRight.setSelectable(true);
//		tableRight.setWriteThrough(true);
//		tableRight.addListener(this);
//		tableRight.addActionHandler(this);
//		BeanItemContainer<SmsPhoneNumber> container=new BeanItemContainer<SmsPhoneNumber>(SmsPhoneNumber.class);
//		tableRight.setContainerDataSource(container);
//		tableRight.setVisibleColumns(new String[]{"phoneNumber"});
//		tableRight.setColumnHeaders(new String[]{"电话号码"});
//		tableRight.setWidth("100%");
//		tableRight.setPageLength(13);
//		panelRight.addComponent(tableRight);
//		return panelRight;
//	}
	
	//创建左侧
	private Panel buildPanelLeft() {
		Panel panelLeft = new Panel("通讯录");
		
		tableLeft=new Table();
		tableLeft.addListener(this);
		tableLeft.addActionHandler(this);
		tableLeft.setImmediate(true);
		tableLeft.setWriteThrough(true);
		tableLeft.setSelectable(true);
		BeanItemContainer<SmsPhoneNumber> container=new BeanItemContainer<SmsPhoneNumber>(SmsPhoneNumber.class);
		tableLeft.setContainerDataSource(container);
		tableLeft.setVisibleColumns(new String[]{"name","phoneNumber"});
		tableLeft.setColumnHeaders(new String[]{"姓名","电话号码"});
		tableLeft.setWidth("100%");
		tableLeft.setPageLength(13);
		panelLeft.addComponent(tableLeft);
		
		//下面的按钮
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		addContacts=new Button("添加");
		addContacts.addListener((Button.ClickListener)this);
		editContacts=new Button("编辑");
		editContacts.addListener((Button.ClickListener)this);
		removeContacts=new Button("删除");
		removeContacts.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(addContacts);
		buttonsLayout.addComponent(editContacts);
		buttonsLayout.addComponent(removeContacts);
		panelLeft.addComponent(buttonsLayout);
		
		return panelLeft;
	}

	//刷新panel
	@SuppressWarnings("unchecked")
	private void refreshTabSheet(){
		BeanItemContainer<SmsPhoneNumber> leftContainer=(BeanItemContainer<SmsPhoneNumber>)tableLeft.getContainerDataSource();
		leftContainer.removeAllItems();
		String sql="select s from SmsPhoneNumber s where s.domain.id="+SpringContextHolder.getDomain().getId()+" and s.smsPhoneNumberType="+SmsPhoneNumberType.class.getCanonicalName()+".CONTACTS order by s.time desc";
		List<SmsPhoneNumber> list = (List<SmsPhoneNumber>)commonService.excuteSql(sql, ExecuteType.RESULT_LIST);
		leftContainer.addAll(list);
		
		
//		String sql2="select s from SmsPhoneNumber s where s.domain.id="+SpringContextHolder.getDomain().getId()+" and s.smsPhoneNumberType="+SmsPhoneNumberType.class.getCanonicalName()+".HISTORY";
//		List<SmsPhoneNumber> list2 = (List<SmsPhoneNumber>)commonService.excuteSql(sql2, ExecuteType.RESULT_LIST);
//		BeanItemContainer<SmsPhoneNumber> rightContainer=(BeanItemContainer<SmsPhoneNumber>)tableRight.getContainerDataSource();
//		rightContainer.removeAllItems();
//		rightContainer.addAll(list2);
		
		editContacts.setEnabled(false);
		removeContacts.setEnabled(false);
	}

	
	// 进行发送短息的操作
	private void executeSend() {
		String receiverStr=(String)receiver.getValue();
		if(receiverStr==null||receiverStr.trim().equals("")){
			NotificationUtil.showWarningNotification(this,"接收人不能为空");
			return;
		}

		String contentStr=(String)content.getValue();
		if(contentStr==null||contentStr.trim().equals("")){
			NotificationUtil.showWarningNotification(this,"发送内容不能为空");
			return;
		}
		
		String[] receivers = receiverStr.split(",");
		List<String> receiverList=new ArrayList<String>(); 
		for(String re:receivers){
			receiverList.add(re);
			if(!StringUtils.isNumeric(re)){
				NotificationUtil.showWarningNotification(this,"请确认电话号码格式正确并用英文格式的逗号隔开！");
				return;
			}
		}
		
//		for(String phoneNumber:receiverList){
//			SmsPhoneNumber smsPhoneNumber=new SmsPhoneNumber();
//			smsPhoneNumber.setDomain(domain);
//			smsPhoneNumber.setSmsPhoneNumberType(SmsPhoneNumberType.HISTORY);
//			smsPhoneNumber.setPhoneNumber(phoneNumber);
//			smsPhoneNumber.setTime(new Date());
//			String sql2="select count(s) from SmsPhoneNumber s where s.domain.id="+SpringContextHolder.getDomain().getId()+" and s.smsPhoneNumberType="+SmsPhoneNumberType.class.getCanonicalName()+".HISTORY";
//			Long count = (Long)commonService.excuteSql(sql2, ExecuteType.SINGLE_RESULT);
//			if(count>15){
//				commonService.excuteSql("delete from SmsPhoneNumber s where s.id=(select max(sp.id) from SmsPhoneNumber sp where sp.domain.id="+domain.getId()+")", ExecuteType.UPDATE);
//			}
//			commonService.update(smsPhoneNumber);
//		}
		
		//发送短信
		SmsUtil.sendSMS(SpringContextHolder.getLoginUser(), SpringContextHolder.getExten(), domain.getId(), receiverList, (String)content.getValue());
		
		receiver.setValue("");
		messageTemplate.setValue(null);
		content.setValue("");
		
		NotificationUtil.showWarningNotification(this,"发送成功！");
	}

	
	@Override
	public void valueChange(ValueChangeEvent event) {
		if(null!=tableLeft.getValue()){
			editContacts.setEnabled(true);
			removeContacts.setEnabled(true);
		}else{
			editContacts.setEnabled(false);
			removeContacts.setEnabled(false);
		}
	}

	public SmsPhoneNumber getSelect(){
		return (SmsPhoneNumber)tableLeft.getValue();
	}
	
	// 执行删除
	private void executeDelete() {
		Label label = new Label("您确定要删除吗?", Label.CONTENT_XHTML);
		ConfirmWindow confirmWindow = new ConfirmWindow(label, this,
				"confirmDelete");
		this.getApplication().getMainWindow().removeWindow(confirmWindow);
		this.getApplication().getMainWindow().addWindow(confirmWindow);
	}
	
	// 确认删除窗口
	public void confirmDelete(Boolean isConfirmed) {
		if (isConfirmed) {
			SmsPhoneNumber smsPhoneNumber=(SmsPhoneNumber)tableLeft.getValue();
			commonService.delete(SmsPhoneNumber.class,smsPhoneNumber.getId());
			this.update();
		}
	}
	
	
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==refresh){
			BeanItemContainer<MessageTemplate> templateContainer=new BeanItemContainer<MessageTemplate>(MessageTemplate.class);
			List<MessageTemplate> templates=messageTemplateService.getAllByDomain(SpringContextHolder.getDomain());
			templateContainer.addAll(templates);
			messageTemplate.setContainerDataSource(templateContainer);
			messageTemplate.setItemCaptionPropertyId("title");
			
		}else if(event.getButton()==addContacts){
			showAddContactsWindow();
		}else if(event.getButton()==editContacts){
			showEditContactsWindow();
		}else if(event.getButton()==removeContacts){
			executeDelete();
		}else if(event.getButton()==send){
			try {
				executeSend();
				refreshTabSheet();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this,"错误提醒："+e.getMessage());
			}
		}

	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		SmsPhoneNumber smsPhoneNumber=(SmsPhoneNumber)target;
		if (ADD == action) {
			String phoneNumber=smsPhoneNumber.getPhoneNumber();
			String receiverStr=(String)receiver.getValue();
			receiverStr=receiverStr.trim();
			if(StringUtils.isEmpty(receiverStr)||receiverStr.endsWith(",")){
				receiverStr+=phoneNumber;
			}else{
				receiverStr+=","+phoneNumber;
			}
			receiver.setValue(receiverStr);
		}
	}
}
