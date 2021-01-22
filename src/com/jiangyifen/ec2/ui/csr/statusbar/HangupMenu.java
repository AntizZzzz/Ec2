package com.jiangyifen.ec2.ui.csr.statusbar;

import java.util.HashSet;
import java.util.Set;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
/**
 * 点击挂断按钮的相应操作组件
 * 按钮和MenuBar互换
 * @author chb
 */
@SuppressWarnings("serial")
public class HangupMenu extends VerticalLayout implements Button.ClickListener {
	// 是哪个分机的HangupMenu
	private String exten;
	private HangupService hangupService;

	// 两个来回互换的组件
	private MenuItem mainItem;
	private MenuBar menubar;
	private Button hangup;
	private Notification notification;		// 提示信息

	/**
	 * 构造器
	 * 
	 * @param exten
	 */
	public HangupMenu(String exten) {
		this.exten = exten;
		this.initService();
		// 用来标示当前布局管理器中存放的是"按钮"还是"菜单"组件, 默认为"按钮"
		this.setData("button");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 创建一个可以挂断的按钮
		hangup = buildHangupButton();
		this.addComponent(hangup);
			
		// 创建一个MenuBar
		menubar = buildMenuBar();
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		hangupService = SpringContextHolder.getBean("hangupService");
	}

	/**
	 * 创建一个MenuBar
	 * 
	 * @return
	 */
	private MenuBar buildMenuBar() {
		// 创建一个可以挂断多个Channel的MenuBar，代码有可改善空间，不用重复创建
		MenuBar menubar = new MenuBar();
		menubar.setAutoOpen(true);
		menubar.addStyleName("nobackground");

		// 点击Menu按钮事件
		mainItem = menubar.addItem("挂断", ResourceDataCsr.hangup_16_sidebar_ico,
				null);
		return menubar;
	}

	/**
	 * 创建一个挂断按钮
	 * 
	 * @return
	 */
	private Button buildHangupButton() {
		// 创建一个可以挂断的按钮
		hangup = new Button("挂断");
		hangup.setIcon(ResourceDataCsr.hangup_16_sidebar_ico);
		hangup.setStyleName("borderless");
		hangup.addListener(this);
		return hangup;
	}

	/**
	 * 取得所有处于Bridged状态的Channel、以及分机自身建立的channel(即：处于呼叫状态，但对方未接起)
	 * 
	 * @return
	 */
	private Set<String> getAllChannels() {
		Set<String> allChannels = new HashSet<String>();
		Set<String> allBridgedChannel = new HashSet<String>();
		Set<String> selfChannels = ShareData.peernameAndChannels.get(exten);
		for (String channel : selfChannels) {
			ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
			if (channelSession != null) {
				String bridgedChannel = channelSession.getBridgedChannel();
				if (bridgedChannel != null && (!"".equals(bridgedChannel))) {
					allBridgedChannel.add(bridgedChannel);
				}
			}
		}
		allChannels.addAll(selfChannels);
		allChannels.addAll(allBridgedChannel);
		return allChannels;
	}
	
	/**
	 * jrh
	 * 根据当前的HangupMenu中的组件情况和当前用户的通话数量
	 * 	更改挂断按钮上的组件
	 * 	供 CsrStatusBar 使用
	 */
	public void updateHangupComponent() {
		String signal = (String) HangupMenu.this.getData();
		if("menubar".equals(signal)) {
			Set<String> allBridgedChannel = getAllChannels();
			int brigedCount = allBridgedChannel.size();
			if(brigedCount == 1) {
				HangupMenu.this.replaceComponent(menubar, hangup);
				HangupMenu.this.setData("button");
			} else if(brigedCount > 1) {
				// 根据通话数量，重新构建MainItem的子组件
				rebuildMenubar(allBridgedChannel);
			}
		}
	}
	
	/**
	 * 挂断按钮单击的事件
	 * 	根据当前通话数量，进行对组件的修改，并修改HangupMenu 的Data 信息
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		// 取得所有Bridged Channel
		Set<String> allChannels = getAllChannels();
		if (allChannels.size() == 0) {
			notification.setCaption("<font color='red'><B>您当前没有通话！</B></font>");
			this.getApplication().getMainWindow().showNotification(notification);
			HangupMenu.this.setData("button");
			return;
		} else if (allChannels.size() <= 2) {
			for(String channel : allChannels) {
				hangupService.hangup(channel);
			}
//			hangupService.hangup(new ArrayList<String>(allChannels).get(0));
			HangupMenu.this.setData("button");
			return;
		} else {
			// 组件互换
			HangupMenu.this.replaceComponent(hangup, menubar);
			HangupMenu.this.setData("menubar");

			// 根据通话数量，重新构建MainItem的子组件
			rebuildMenubar(allChannels);
		}
	}

	/**
	 *  根据通话数量，重新构建MainItem的子组件
	 * @param allBridgedChannel
	 */
	private void rebuildMenubar(Set<String> allBridgedChannel) {
		mainItem.removeChildren();
		for (final String channel : allBridgedChannel) {
			Command hangupCommand = new Command() {
				public void menuSelected(MenuItem selectedItem) {
					hangupService.hangup(channel);
					HangupMenu.this.replaceComponent(menubar, hangup);
				}
			};
			mainItem.addItem(ShareData.channelAndChannelSession
					.get(channel).getCallerIdNum(),
					ResourceDataCsr.hangup_16_sidebar_ico, hangupCommand);
		}
	}

}
