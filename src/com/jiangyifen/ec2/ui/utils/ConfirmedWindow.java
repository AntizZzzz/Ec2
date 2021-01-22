package com.jiangyifen.ec2.ui.utils;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 确认是否进行某项操作,并显示相应的提示信息
 * <p>
 * 为了使窗口更具通用性，使用了反射
 * </p>
 * 
 * @author jrh
 * 
 */
@SuppressWarnings("serial")
public class ConfirmedWindow extends Window implements Button.ClickListener {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 窗口的layout
	private VerticalLayout windowContent;
	
	// 消息
	private String warningMsg;
	private String msgPxLen;	// 提示信息，所对应的标签的长度
	private Label msg_lb;
	private Button confirm_bt;
	private Button cancel_bt;
	private Object srcComp;
	private String methodName;

	/**
	 * 
	 * @param warningMsg
	 *            显示的消息
	 * @param msgPxLen
	 *            提示信息，所对应的标签的长度
	 * @param srcComp
	 *            调用此确认窗口的源组件
	 * @param methodName
	 *            回调的方法,此方法必须有一个Boolean类型的参数值 以上三个参数均不能为Null
	 */
	public ConfirmedWindow(String warningMsg, String msgPxLen, Object srcComp, String methodName) {
		this.center();
		this.setCaption("确认信息");
		this.setResizable(false);
		this.setModal(true);
		this.warningMsg = warningMsg;
		this.msgPxLen = msgPxLen;
		this.srcComp = srcComp;
		this.methodName = methodName;

		// 创建一个windowContent的组件
		windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setSpacing(true);
		windowContent.setMargin(false, true, true, true);
		this.setContent(windowContent);

		buildMessageAndButtons();
	}

	private void buildMessageAndButtons() {
		// 消息输出
		VerticalLayout msg_vlo = new VerticalLayout();
		windowContent.addComponent(msg_vlo);

		msg_lb = new Label("", Label.CONTENT_XHTML);
		// messageLabel.setWidth("-1px");
		msg_lb.setValue(warningMsg);
		msg_lb.setWidth(msgPxLen); 
		windowContent.addComponent(msg_lb);

		// 按钮输出
		HorizontalLayout operator_hlo = new HorizontalLayout();
		operator_hlo.setSpacing(true);
		windowContent.addComponent(operator_hlo);

		// 按钮组件
		confirm_bt = new Button("确认");
		confirm_bt.addListener(this);
		operator_hlo.addComponent(confirm_bt);

		cancel_bt = new Button("取消");
		cancel_bt.addListener(this);
		operator_hlo.addComponent(cancel_bt);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == confirm_bt) {
			// 回调特定实例指定方法 ,应该是不同的实例充当监听器,不是方法当监听器,应该没有问题的...
			if (srcComp != null && !methodName.trim().equals("")) {
				try {
					Class objClass = srcComp.getClass();
					Method objMethod = objClass.getMethod(methodName);
					objMethod.invoke(srcComp);
					this.getParent().removeWindow(this);	// 关闭窗口
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("弹屏确认窗口-回调类："+srcComp.getClass()+"的"+methodName+"方法时出现异常："+e.getMessage(), e);
					NotificationUtil.showWarningNotification(this, "执行失败，请稍后重试！");
				}
			}
		} else if(source == cancel_bt) {
			this.getParent().removeWindow(this);
		}
	}
	
	/**
	 * 修改提示信息
	 * @param warningMsg
	 */
	public void setWarningMsg(String warningMsg) {
		this.warningMsg = warningMsg;
		msg_lb.setValue(warningMsg);
	}
	
}
