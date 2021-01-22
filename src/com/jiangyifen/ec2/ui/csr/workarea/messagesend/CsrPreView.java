package com.jiangyifen.ec2.ui.csr.workarea.messagesend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.entity.User;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 *	添加和编辑窗口，可以添加项目和编辑项目 
 * @author lxy
 */
@SuppressWarnings("serial")
public class CsrPreView extends Window implements Button.ClickListener {
/**
 * 主要组件输出	
 */
	private Label receiverLabel;
	private Label contentLabel;
	private Button close;
	/**
	 *其它组件 
	 */
	private CsrNoticeHistory historyMessage;
	
	/**historyMessage
	 * 构造器
	 * @param historyMessage
	 */
	public CsrPreView(CsrNoticeHistory historyMessage) {
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.historyMessage=historyMessage;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);
		
		//创建内容部分
		windowContent.addComponent(buildContentArea());
		
		close = new Button("关闭", this);
		close.setImmediate(true);
		windowContent.addComponent(close);
	}
	
	/**
	 * 创建内容输出区域
	 * @return
	 */
	private Panel buildContentArea() {
		Panel panel=new Panel();
		panel.setWidth("650px");
		panel.setHeight("330px");
		panel.setScrollable(true);
		receiverLabel = new Label("接收人：", Label.CONTENT_XHTML);
		receiverLabel.setWidth("-1px");
		panel.addComponent(receiverLabel);
		panel.addComponent(new Label("<b>内容:</b>",Label.CONTENT_XHTML));
		contentLabel=new Label("",Label.CONTENT_XHTML);
		panel.addComponent(contentLabel);
		return panel;
	}
	/**
	 * 每当Attach时触发加载Table选中条目数据
	 */
	@Override
	public void attach() {
		super.attach();
		Notice notice=(Notice)historyMessage.getTable().getValue();
		StringBuffer receiverStrs = new StringBuffer();
		List<User> receivers = new ArrayList<User>(notice.getReceivers());
		Collections.sort(receivers, new Comparator<User>() {
			@Override
			public int compare(User o1, User o2) {
				int emp1 = Integer.parseInt(o1.getEmpNo());
				int emp2 = Integer.parseInt(o2.getEmpNo());
				return emp1 - emp2;
			}
		});
		
		for(int i = 1; i <= receivers.size(); i++) {
			User receiver = receivers.get(i-1);
			String name = receiver.getRealName();
			if(name == null) {
				receiverStrs.append(receiver.getEmpNo() +",");
			} else {
				receiverStrs.append(receiver.getEmpNo() +"-"+name+",");
			} 
			if(i%8 == 0 && i != receivers.size()) {
				receiverStrs.append("</br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
			}
		}
		String receiverStr = receiverStrs.toString().substring(0, receiverStrs.toString().length()-1);
		receiverLabel.setValue("<B>接收人：</B><font color='red'>"+receiverStr+"</font>");
		this.setCaption(notice.getTitle());
		contentLabel.setValue(notice.getContent());
	}
	/**
	 * 点击关闭按钮时关闭窗口
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==close){
			this.getParent().removeWindow(this);
		}
	}
}