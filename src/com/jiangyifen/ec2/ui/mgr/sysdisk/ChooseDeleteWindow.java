package com.jiangyifen.ec2.ui.mgr.sysdisk;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ChooseDeleteWindow extends Window implements ClickListener{

	private static final long serialVersionUID = 9022685462013515756L;

	private SoundRecordingWindow soundRecordingWindow;
	
	private Button btn_ok;		//保存关闭
	private Button btn_ccl;		//取消按钮
	
	private String methodName;
	private SoundRecording soundRecording;
	
	public void setSoundRecording(SoundRecording soundRecording) {
		this.soundRecording = soundRecording;
	}

	public ChooseDeleteWindow(String title,String methodName,SoundRecordingWindow soundRecordingWindow){
		this.center();
		this.setModal(true);
		this.setWidth("400px");
		this.setHeight("60px");
		this.setResizable(false);
		this.setCaption("系统提示");
		
		this.soundRecordingWindow = soundRecordingWindow;
		this.methodName = methodName;
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		Label lb_msg = new Label(title,Label.CONTENT_XHTML);
		lb_msg.setSizeFull();
		mainLayout.addComponent(lb_msg);
		
		HorizontalLayout foolter = new HorizontalLayout();
		foolter.setSpacing(true);
		
		btn_ok = new Button("确 定");
		btn_ok.addListener(this);
		btn_ok.setStyleName("btn-danger");
		foolter.addComponent(btn_ok);
		
		btn_ccl = new Button("取消",this);
		btn_ccl.addListener(this);
		foolter.addComponent(btn_ccl);
		
		mainLayout.addComponent(foolter);
		this.addComponent(mainLayout);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btn_ok){
			if("SoundRecording".equals(methodName)){
				soundRecordingWindow.deleteOkBack(soundRecording,true);	
			}
		}
		if(source == btn_ccl){
			soundRecordingWindow.deleteOkBack(null,false);	
		}
	}
	
	public void closeWindow(){
		this.getApplication().getMainWindow().removeWindow(this);
	}
}
