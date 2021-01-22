package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.csr.workarea.myresource.AddTelephonesField;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 为客户添加联系方式的窗口
 * @author jrh
 */
@SuppressWarnings("serial")
public class AddPhoneWindow extends Window implements ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Notification notification;					// 提示信息
	private AddTelephonesField telephonesField;			// 自定义添加电话号码组件
	private DialComponentToForm dialComponentToForm;	// 
	
	//保存按钮和取消按钮
	private Button save;		//	保存按钮
	private Button cancel;		//	取消按钮

	private Domain domain;		//	资源所属域
	private boolean existedCustomer;				// 用于判断客户是否已经存在
	private CustomerResource customerResource;		// 资源对象
	private TelephoneService telephoneService;		// 电话号码服务类
	private CustomerResourceService customerResourceService; // 客户信息源服务类
	
	public AddPhoneWindow(CustomerResource customerResource, DialComponentToForm dialComponentToForm) {
		this.center();
		this.setModal(true);
		this.setImmediate(true);
		this.setDescription("<B><font color='red'>说明：</font>您可以一次添加多个电话，同时所有的输入框不必全部输完，但至少输入一个！</B>");
		this.setCaption("添加联系电话");
		this.customerResource = customerResource;
		this.dialComponentToForm = dialComponentToForm;

		notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		domain = SpringContextHolder.getDomain();
		customerResourceService = SpringContextHolder.getBean("customerResourceService");
		telephoneService = SpringContextHolder.getBean("telephoneService");

		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		// 创建Form 表单
		telephonesField = new AddTelephonesField("180px", domain);
		windowContent.addComponent(telephonesField);
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth("100%");
		windowContent.addComponent(buttonsLayout);

		// 保存按钮
		save = new Button("保存", this);
		save.setStyleName("default");
		buttonsLayout.addComponent(save);

		// 取消按钮
		cancel = new Button("取消", this);
		buttonsLayout.addComponent(cancel);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save){
			if(excuteConfirm() == true) {
				boolean success = excuteSave();
				if(success == true) {
					dialComponentToForm.updateFormPhoneField(customerResource, existedCustomer);
					notification.setCaption("成功添加联系电话！");
					this.getApplication().getMainWindow().showNotification(notification);
					this.getApplication().getMainWindow().removeWindow(this);
				}
			}
		} else if(source == cancel) {
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	/**
	 * 验证电话号输入是否合法
	 * @return
	 */
	private boolean excuteConfirm() {
		try {
			if(!telephonesField.isValid()) {
				notification.setCaption("<font color='red'><B>电话号码格式填写错误！</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			}
		} catch (Exception e) {
			logger.info("CSR 为资源添加新的电话时，电话号码格式填写错误！");
			return false;
		} 
		
		int originalSize = telephonesField.getValue().size();
		if(originalSize == 0) {
			notification.setCaption("<font color='red'><B>电话号不能为空！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return false;
		}
		
		return true;
	}

	/**
	 * 执行保存操作
	 * @return
	 */
	private boolean excuteSave() {
		existedCustomer = true;
		// 处理资源的电话号码
		List<Telephone> useablePhone = new ArrayList<Telephone>();
		for(Telephone telephone : telephonesField.getValue()) {
			Telephone existedTel = telephoneService.getByNumber(telephone.getNumber(), domain.getId());
			// 如果号码存在，则应检查使用号码的人是不是有价值的客户，如果不是则将其覆盖
			if(existedTel != null && existedTel.getCustomerResource() != null) {
				notification.setCaption("电话号"+telephone.getNumber()+" 已被<B>其他客户</B>使用，请确认号码后重试！");
				this.getApplication().getMainWindow().showNotification(notification);
				return false;
			} else {
				useablePhone.add(telephone);
			}
		}
		
		List<Telephone> oldPhones = new ArrayList<Telephone>();
		if(customerResource.getId() == null) {
			existedCustomer = false;
			customerResource = customerResourceService.update(customerResource); 
			for(Telephone ph : customerResource.getTelephones()) {
				ph.setCustomerResource(customerResource);
				ph = telephoneService.update(ph);
				oldPhones.add(ph);
			}
		} else {
			oldPhones.addAll(customerResource.getTelephones());
		}
		
		// 都是新号码，则添加号码
		List<Telephone> newPhones = new ArrayList<Telephone>();
		for(Telephone telephone : useablePhone) {
			telephone.setDomain(customerResource.getDomain());
			telephone.setCustomerResource(customerResource);
			telephone = telephoneService.update(telephone);
			newPhones.add(telephone);
		}
		
		// 更新客户的电话集合（更新缓存）
		Set<Telephone> phoneSet = new HashSet<Telephone>();
		phoneSet.addAll(newPhones);
		phoneSet.addAll(oldPhones);
		customerResource.setTelephones(phoneSet);
		customerResource = customerResourceService.update(customerResource);
		
		return true;
	}
}
