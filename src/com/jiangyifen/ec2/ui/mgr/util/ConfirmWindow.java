package com.jiangyifen.ec2.ui.mgr.util;

import java.lang.reflect.Method;

import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 * 确认是否进行某项操作,并显示相应的提示信息
 * <p>为了使窗口更具通用性，使用了反射</p>
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class ConfirmWindow extends Window implements Button.ClickListener{
	//窗口的layout
	private VerticalLayout windowContent;
	//消息
	private Label messageLabel;
	private Button confirm;
	private Button cancel;
	private Object obj;
	private String methodName;
	
	/**
	 * 
	 * @param messageLabel 显示的消息
	 * @param obj 对象
	 * @param methodName 回调的方法,此方法必须有一个Boolean类型的参数值
	 * 以上三个参数均不能为Null
	 */
	public ConfirmWindow(Label messageLabel,Object obj,String methodName) {
		this.center();
		this.setCaption("确认信息");
		this.setResizable(false);
		this.setModal(true);
		this.messageLabel=messageLabel;
		this.obj=obj;
		this.methodName=methodName;
		
		//创建一个windowContent的组件
		windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setSpacing(true);
		windowContent.setMargin(false, true, true, true);
		this.setContent(windowContent);
		
		buildMessageAndButtons();
	}

	private void buildMessageAndButtons() {
		//消息输出
		VerticalLayout messageLayout=new VerticalLayout();
		windowContent.addComponent(messageLayout);
		messageLabel.setWidth("25em"); //设置所有的确认信息宽度为25em
		windowContent.addComponent(messageLabel);
		
		//按钮输出
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		windowContent.addComponent(buttonsLayout);
		
		//按钮组件
		confirm=new Button("确认");
		confirm.addListener(this);
		buttonsLayout.addComponent(confirm);
		
		cancel=new Button("取消");
		cancel.addListener(this);
		buttonsLayout.addComponent(cancel);
	}

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buttonClick(ClickEvent event) {
		Boolean state=false;
		if(event.getButton()==confirm){
			state=true;
		}
		//回调特定实例指定方法	,应该是不同的实例充当监听器,不是方法当监听器,应该没有问题的...
		if(obj!=null&&!methodName.trim().equals("")){
			Class objClass=null;
			try{
				objClass=obj.getClass();
				Method objMethod=objClass.getMethod(methodName,Boolean.class);
				objMethod.invoke(obj,state);
				//关闭窗口
				this.getParent().removeWindow(this);
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "系统错误！");
			}
		}
	}
}
