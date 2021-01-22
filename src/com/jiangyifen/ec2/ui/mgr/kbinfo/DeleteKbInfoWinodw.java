package com.jiangyifen.ec2.ui.mgr.kbinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.KbInfo;
import com.jiangyifen.ec2.service.eaoservice.KbInfoService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.KbInfoManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

/**
 * 删除 知识窗口
 * @author lxy
 *
 */
public class DeleteKbInfoWinodw extends Window implements  ClickListener{
	
	//静态-非业务
	private static final long serialVersionUID = -1824638065739476620L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
	//静态-业务
 
	//持有UI对象
	private KbInfoManagement kbInfoManagement;
  	
	//页面组件
 	private Label lb_msg;
 	 
	private Button bt_delete;
	private Button bt_cancel;
	
	//业务必须对象
	
	//业务本页对象
	 private KbInfo kbInfo;
	 
	//业务注入对象
	private KbInfoService kbInfoService; 
 	
 	public DeleteKbInfoWinodw(KbInfoManagement kbInfoManagement){
 		this.kbInfoManagement = kbInfoManagement;
		initThis();
		initSpringContext();
		initCompanent();
	}
 	
 	private void initThis() {
 		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("删除知识");
		this.setWidth("400px");
		this.setHeight("120px");
 	}

 	private void initSpringContext() {
		kbInfoService = SpringContextHolder.getBean("kbInfoService");
	}

	private void initCompanent() {
		lb_msg = new Label("",Label.CONTENT_XHTML);
		this.addComponent(lb_msg);
		HorizontalLayout hl_tool = new HorizontalLayout();
		hl_tool.setMargin(true);
		hl_tool.setSpacing(true);
		bt_delete = new Button("确认删除",this);
		hl_tool.addComponent(bt_delete);
		bt_cancel = new Button("取消删除",this);
		hl_tool.addComponent(bt_cancel);
		this.addComponent(hl_tool);
 	}


	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_delete) {
			excuteDelete();
		}else if(source == bt_cancel){
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	private void excuteDelete(){
		try {
			if (null != kbInfo) {
				kbInfoService.deleteKbInfoById(kbInfo.getId());
				showWindowMsgInfo("删除[" + kbInfo.getTitle() + "]成功");
				this.getApplication().getMainWindow().removeWindow(this);
			} else {
				showWindowMsgInfo("知识不存在,请返回选择知识");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除知识异常_excuteDelete_LLXXYY", e);
			showWindowMsgInfo("删除知识异常");
		}
		kbInfoManagement.updateTable(true);
		kbInfoManagement.getTable().setValue(null);
	}
	
	public void refreshComponentInfo(KbInfo kbInfo){
		 this.kbInfo = kbInfo;
		 String showHtml = "<font color='red'>你确定要删除:["+kbInfo.getTitle()+"]?</font>";
		 this.lb_msg.setValue(showHtml);
	}
	
	/**
	 * 显示
	 * @param msg
	 */
	private void showWindowMsgInfo(String msg){
		this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_HUMANIZED_MESSAGE);
	}
}
