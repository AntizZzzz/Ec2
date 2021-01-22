package com.jiangyifen.ec2.ui;

import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemInfo;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Admin Manager 三种用户共用的状态栏组件
 * 该组件用于显示一些公告等信息
 * @author Administrator
 */
@SuppressWarnings("serial")
public class StatusBar extends HorizontalLayout {
	
	private Label announcement;
	
	public StatusBar() {
		this.setWidth("100%");
		this.setMargin(false, true, false, true);

		String caption = "<marque direction=left behavior=scroll><font color='blue'><B>" + SystemInfo.getStatusString()+" "+GlobalData.STATUS_BAR_ANNOUNCEMENT+ "</font></marquee>";
		announcement = new Label(caption, Label.CONTENT_XHTML);
		announcement.setSizeFull();
		
		this.addComponent(announcement);
	}

}
