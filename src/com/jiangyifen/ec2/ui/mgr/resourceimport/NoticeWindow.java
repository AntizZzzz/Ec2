package com.jiangyifen.ec2.ui.mgr.resourceimport;

import java.util.List;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 *窗口 
 */
@SuppressWarnings("serial")
public class NoticeWindow extends Window implements Button.ClickListener{
	
	//底部按钮
	private Button confirm;
	
	
	public NoticeWindow(String caption,List<String> noteContentList) {
		this.setCaption(caption);
		this.center();
		this.setSizeUndefined();
		this.setModal(true);
		this.setResizable(false);
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(true, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);
		
		Panel panel=new Panel();
		panel.setWidth("20em");
		panel.setHeight("10em");
		//获取文件内容，放入Panel
		for(String noteLine:noteContentList){
			panel.addComponent(new Label(noteLine,Label.CONTENT_XHTML));
		}
		windowContent.addComponent(panel);
		
		//确认按钮
		HorizontalLayout buttonsLayout = buildButtonsLayout();
		windowContent.addComponent(buttonsLayout);
//		windowContent.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);
	}
	
	
	/**
	 * 按钮输出区域
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout fullWidthLayout=new HorizontalLayout();
		fullWidthLayout.setWidth("100%");
//		fullWidthLayout.setWidth("30em");
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		// 确定按钮
		confirm = new Button("确定", this);
		buttonsLayout.addComponent(confirm);
		fullWidthLayout.addComponent(buttonsLayout);
		fullWidthLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);
		
		return fullWidthLayout;
	}

	private void excuteConfirm() {
		this.getParent().removeWindow(this);
	}
	
	/**
	 * 点击保存、取消按钮的事件 
	 * @param event
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==confirm){
			excuteConfirm();
		}
	}
	
}
