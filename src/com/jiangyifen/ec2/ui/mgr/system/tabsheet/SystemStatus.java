package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.mgr.system.realtime.RealTimeSystemStatisticPanel;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
/**
 * 显示系统状态信息
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class SystemStatus extends VerticalLayout {
	
	private Panel  panel;
	public RealTimeSystemStatisticPanel  panelStatistics;
	
	private User loginUser;		// 当前登陆用户
	
	/**
	 * 构造器
	 */
	public SystemStatus() {
		this.setSizeFull();
		this.setMargin(true);
		
		loginUser = SpringContextHolder.getLoginUser();
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.setWidth("100%");
		
		panel = new Panel("");
		panel.setCaption("布告");
		panel.setWidth("100%");
		layout.addComponent(panel);
		
		panelStatistics = new RealTimeSystemStatisticPanel();
		panelStatistics.setCaption("系统统计");
		panelStatistics.setWidth("100%");
		layout.addComponent(panelStatistics);
		
		this.addComponent(layout);
		
		ShareData.systemStatusMap.put(loginUser.getId(), this);
	}
	
	 

}
