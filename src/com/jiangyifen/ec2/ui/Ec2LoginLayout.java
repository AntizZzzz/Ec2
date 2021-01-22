package com.jiangyifen.ec2.ui;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

public class Ec2LoginLayout extends VerticalLayout {
	private static final long serialVersionUID = 7069074530031558845L;
	private TabSheet loginTabSheet;
	@SuppressWarnings("unused")
	private Tab tabMgr;
	@SuppressWarnings("unused")
	private Tab tabCsr;
	private Application appli;
	
	public Ec2LoginLayout() {
		this.setMargin(true);
		this.setSizeFull();
		
		loginTabSheet = new TabSheet();
		tabCsr = loginTabSheet.addTab(new LoginLayout(RoleType.csr, this), "用户登录", ResourceDataCsr.user_ico);
		tabMgr = loginTabSheet.addTab(new LoginLayout(RoleType.manager, this), "管理员登录", ResourceDataCsr.manager_ico);
		
		loginTabSheet.setWidth("420px");
//		loginTabSheet.setHeight("310px");
		loginTabSheet.setHeight("-1px");
		
		this.addComponent(loginTabSheet);
		this.setComponentAlignment(loginTabSheet, Alignment.TOP_CENTER);
	}
	
	public void attach() {
//		 TODO Auto-generated method stub
//		LoginLayout loginLayout = null;
//		
//		loginTabSheet.setSelectedTab(tabMgr);
//		loginLayout=(LoginLayout)tabMgr.getComponent();
//		
//		loginTabSheet.setSelectedTab(tabCsr);
//		loginLayout=(LoginLayout)tabCsr.getComponent();
//		
//		loginLayout.loginClick();
	}
	
	public Application getAppli() {
		return appli;
	}

	/**
	 * jrh 20131023
	 * 通过曲折的方式实现按enter 键登陆系统
	 */
	public void loginByShutCut() {
		LoginLayout loginLayout = (LoginLayout) loginTabSheet.getSelectedTab();
		loginLayout.loginClick();
	}

	public TabSheet getLoginTabSheet() {
		return loginTabSheet;
	}

}
