package com.jiangyifen.ec2.ui.mgr.kbinfo;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window;

/**
 * 知识查询展示窗口
 * @author lxy
 *
 */
public class ShowKbInfoViewWindow extends Window  implements  ClickListener{

	 
	private static final long serialVersionUID = -3309074131660882763L;

	private ShowKbInfoView showKbInfoView;
	private int windowWidth = 560;
	private int browserWindowWidth;
	
	public ShowKbInfoViewWindow(int browserWindowWidth){
		this.browserWindowWidth = browserWindowWidth;
		initThis();
		showKbInfoView = new ShowKbInfoView();
		this.setContent(showKbInfoView);
		 
	}
	
	private void initThis() {
		this.setModal(false);
		this.setResizable(true);
		this.setCaption("知识库");
		this.setWidth(windowWidth + "px");
		this.setHeight("560px");
		int resultWidth = browserWindowWidth - windowWidth;
		if(resultWidth < 0){
			resultWidth = 10;
		}
		this.setPositionX(resultWidth);
		this.setPositionY(55);
	}
 
	public void refreshComponentInfo(int browserWindowWidth){
		int resultWidth = browserWindowWidth - windowWidth;
		if(resultWidth < 0){
			resultWidth = 10;
		}
		this.setPositionX(resultWidth);
		showKbInfoView.refreshComponentInfo();
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		
	}
}
