package com.jiangyifen.ec2.ui.csr;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.DoBridgeChannelsService;
import com.jiangyifen.ec2.service.eaoservice.CustomerResourceService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.ui.LoginLayout;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.ui.csr.toolbar.CsrToolBar;
import com.jiangyifen.ec2.ui.csr.workarea.sms.SendSingleMessageWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CsrMainView extends VerticalLayout {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CsrToolBar csrToolBar;
	private CsrWorkArea csrWorkArea;
	private CsrStatusBar csrStatusBar;
	private ProgressIndicator messageIndicator;
	
	// delete -----------------------
	private Button bridge_button;
	private Button send_button;
	private Button addTelephone_button;
	private SendSingleMessageWindow sendSingleMessageWindow;
	
	
	public CsrMainView(LoginLayout loginLayout) {
		this.setSizeFull();
		
		csrToolBar = new CsrToolBar(loginLayout);
		this.addComponent(csrToolBar);

		csrWorkArea = new CsrWorkArea();
		this.addComponent(csrWorkArea);
		this.setExpandRatio(csrWorkArea, 1);

		csrStatusBar = new CsrStatusBar(csrWorkArea);
		this.addComponent(csrStatusBar);
		
		messageIndicator = new ProgressIndicator();
		messageIndicator.setPollingInterval(1000);
		messageIndicator.setStyleName("invisible");
		this.addComponent(messageIndicator);
		
//		TODO jrh
//		testSendMessage();
//		testBridgeChannel();
//		testAddTel();
	}

	@SuppressWarnings("unused")
	private void testAddTel() {
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		this.addComponent(h);
		
		final TextField t1 = new TextField("号码:");
		h.addComponent(t1);
		
		addTelephone_button = new Button("添加手机号");
		h.addComponent(addTelephone_button);
		
		addTelephone_button.setImmediate(true);
		addTelephone_button.setStyleName("default");
		addTelephone_button.setIcon(ResourceDataCsr.dial_16_sidebar_ico);
		addTelephone_button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String number = StringUtils.trimToEmpty((String) t1.getValue());
				
				if("".equals(number)) {
					return;
				}
				
				CustomerResourceService customerResourceService = SpringContextHolder.getBean("customerResourceService");
				TelephoneService telephoneService = SpringContextHolder.getBean("telephoneService");
				
				CustomerResource cr = customerResourceService.get(5L);
				Telephone tel = new Telephone();
				tel.setNumber(number);
				tel.setDomain(cr.getDomain());
				tel.setCustomerResource(cr);
				telephoneService.save(tel);
			}
		});
	}

	@SuppressWarnings("unused")
	private void testBridgeChannel() {
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		this.addComponent(h);
		
		final TextField t1 = new TextField("channel1:");
		h.addComponent(t1);
		final TextField t2 = new TextField("channel2:");
		h.addComponent(t2);
		
		bridge_button = new Button("直接Bridge");
		h.addComponent(bridge_button);
		
		bridge_button.setImmediate(true);
		bridge_button.setStyleName("default");
		bridge_button.setIcon(ResourceDataCsr.dial_16_sidebar_ico);
		bridge_button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Set<String> strs = ShareData.peernameAndChannels.get("800003");
				
				logger.info("");
				DoBridgeChannelsService doBridgeChannelsService = SpringContextHolder.getBean("doBridgeChannelsService");
				String channel = StringUtils.trimToEmpty((String)t1.getValue());
				String bridgedChannel = StringUtils.trimToEmpty((String)t2.getValue());
				logger.info(channel+"            -------        "+ bridgedChannel);
				boolean isSuccess = doBridgeChannelsService.doBridgeChannels(channel, bridgedChannel);
				
				logger.info(isSuccess + "");
			}
		});
	}

	@SuppressWarnings("unused")
	private void testSendMessage() {
		send_button = new Button("发送短信");
		send_button.setImmediate(true);
		send_button.setStyleName("default");
		this.addComponent(send_button);
		send_button.setIcon(ResourceDataCsr.phone_message_send_16_ico);
		send_button.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				send_button.getApplication().getMainWindow().removeWindow(sendSingleMessageWindow);
				testSimple();
				testSpecifiedPhoneNo();
				testSpecifiedCustomer();
				send_button.getApplication().getMainWindow().addWindow(sendSingleMessageWindow);
			}

			private void testSimple() {
				logger.info(" nothing to transfered !!");
				sendSingleMessageWindow.updateWindowComponents();
			}
			
			private void testSpecifiedPhoneNo() {
				String phonoNo = "13816760398";
				sendSingleMessageWindow.updateWindowComponents(phonoNo);
			}

			private void testSpecifiedCustomer() {
				Telephone telephone1 = new Telephone();
				telephone1.setNumber("013867543567");
				Telephone telephone2 = new Telephone();
				telephone2.setNumber("13867543567");
				Telephone telephone3 = new Telephone();
				telephone3.setNumber("010867543");
				Telephone telephone4 = new Telephone();
				telephone4.setNumber("02186754350");
				Set<Telephone> phones = new HashSet<Telephone>();
				phones.add(telephone1);
				phones.add(telephone2);
				phones.add(telephone3);
				phones.add(telephone4);
				
				CustomerResource customer = new CustomerResource();
				customer.setId(1L);
				customer.setTelephones(phones);
				sendSingleMessageWindow.updateWindowComponents(customer);
			}

		});
		
		User loginUser = SpringContextHolder.getLoginUser();
		ArrayList<String> businessModels = SpringContextHolder.getBusinessModel();
		if(sendSingleMessageWindow == null) {
			sendSingleMessageWindow = new SendSingleMessageWindow(loginUser, RoleType.csr, businessModels);
		}
	}

}
