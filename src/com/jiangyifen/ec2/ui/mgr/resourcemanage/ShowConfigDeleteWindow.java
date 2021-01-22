package com.jiangyifen.ec2.ui.mgr.resourcemanage;

import com.jiangyifen.ec2.ui.mgr.tabsheet.ResourceManage;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ShowConfigDeleteWindow extends Window implements Window.CloseListener,ClickListener{
	
	private static final long serialVersionUID = -7859724232325890746L;

	
	private Button btnOk;		//保存关闭
	private Button btnCcl;		//取消按钮
	
	private ResourceManage resourceManage;
	
	public ShowConfigDeleteWindow(ResourceManage resourceManage,String txt,String width,String height){
		this.center();
		this.setModal(true);
		this.setWidth(width);
		this.setHeight(height);
		this.setResizable(false);
		this.setCaption("提示");
		
		this.resourceManage = resourceManage;

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		Label lb_msg = new Label(txt,Label.CONTENT_XHTML);
		lb_msg.setSizeFull();
		mainLayout.addComponent(lb_msg);
		
		HorizontalLayout foolter = new HorizontalLayout();
		foolter.setSpacing(true);
		foolter.setSizeFull();
		btnOk = new Button("确定");
		btnOk.addListener(this);
		btnOk.setStyleName("default");
		foolter.addComponent(btnOk);
		
		btnCcl = new Button("关闭",this);
		btnCcl.addListener(this);
		foolter.addComponent(btnCcl);
		foolter.setComponentAlignment(btnCcl, Alignment.BOTTOM_RIGHT);
		
		mainLayout.addComponent(foolter);
		this.addComponent(mainLayout);
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		closeWindow();
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btnOk){
			resourceManage.doDeleteTask();//执行删除
			closeWindow();
		}
		if(source == btnCcl){
			closeWindow();
		}
		
	}
	
	public void closeWindow(){
		this.getApplication().getMainWindow().removeWindow(this);
	}
}
