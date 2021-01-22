package com.jiangyifen.ec2.ui.mgr;

import com.jiangyifen.ec2.ui.LoginLayout;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;
/**
 * Manager的主窗口
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class MgrMain extends VerticalLayout{
	//头部、中间、状态组件
	private MgrHeader mgrHeader;
	private MgrCenter mgrCenter;
	private MgrFooter mgrFooter;
	
	private ProgressIndicator messageIndicator;
	
	public MgrMain(LoginLayout loginLayout) {
		this.setSizeFull();
		
		//组件添加区 -- 添加头部
		mgrHeader=new MgrHeader(loginLayout);
		mgrHeader.setWidth("100%");
		this.addComponent(mgrHeader);
		
		//输出区 -- Accordin 和  mainLayout 主要信息窗口 
		mgrCenter=new MgrCenter();
		mgrCenter.setSizeFull();
		this.addComponent(mgrCenter);
		this.setExpandRatio(mgrCenter, 1);
		
		//输出区 -- 状态信息
		mgrFooter=new MgrFooter();
		mgrFooter.setWidth("100%");
		this.addComponent(mgrFooter);
		
		messageIndicator = new ProgressIndicator();
		messageIndicator.setPollingInterval(1000);
//		messageIndicator.setPollingInterval(3600 * 1000);
		messageIndicator.setStyleName("invisible");
		this.addComponent(messageIndicator);
	}
}
