package com.jiangyifen.ec2.ui.mgr;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.sms.SmsUtil;
import com.jiangyifen.ec2.ui.StatusBar;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;

/**
 * Manager的主窗口的 尾部
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class MgrFooter extends HorizontalLayout implements Button.ClickListener{
	//状态组件
	private StatusBar statusBar;
//	private Button sendSms;
	
	public MgrFooter() {
		statusBar=new StatusBar();
		this.addStyleName("mgrstatusbarmargin");
		this.addComponent(statusBar);
		
//		sendSms = new Button("发短信", this);
//		this.addComponent(sendSms);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		User user=SpringContextHolder.getLoginUser();
		String extenNo=ShareData.userToExten.get(user.getId());
		Long domainId=SpringContextHolder.getDomain().getId();
		List<String> toPhones=new ArrayList<String>();
		toPhones.add("150111111111");
		toPhones.add("130222222222");
		SmsUtil.sendSMS(user,extenNo,domainId,toPhones, "测试短信！");
	}
}
