package com.jiangyifen.ec2.ui.mgr.messagesend;

import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.service.eaoservice.NoticeService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.HistoryNotice;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 编辑消息窗口
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class EditNotice extends Window implements Button.ClickListener {
	/**
	 * 主要组件
	 */
	// 消息组件
	private TextField title;
	private RichTextArea content;
	private Button save;
	private Button cancel;

	/**
	 * 其它组件
	 */
	private NoticeService noticeService;
	private HistoryNotice historyMessage;

	/**
	 * 编辑消息窗口
	 */
	public EditNotice(HistoryNotice historyMessage) {
		this.center();
		this.setModal(true);
		this.setCaption("编辑消息");
		this.historyMessage = historyMessage;

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);

		// 标题栏
		HorizontalLayout constraintLayout = new HorizontalLayout();
		constraintLayout.setWidth("100%");
		Label tempLabel1 = new Label("标题:", Label.CONTENT_XHTML);
		tempLabel1.setSizeUndefined();
		constraintLayout.addComponent(tempLabel1);
		title = new TextField();
		title.setWidth("100%");
		constraintLayout.addComponent(title);
		constraintLayout.setExpandRatio(title, 1.0f);
		windowContent.addComponent(constraintLayout);

		// 消息内容
		content = new RichTextArea();
		content.setWidth("35em");
		windowContent.addComponent(content);

		// 按钮
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setWidth("25%");
		
		save = new Button("保存");
		save.addListener((Button.ClickListener) this);
		buttonsLayout.addComponent(save);

		cancel = new Button("取消");
		cancel.addListener((Button.ClickListener) this);
		buttonsLayout.addComponent(cancel);

		windowContent.addComponent(buttonsLayout);
		windowContent.setComponentAlignment(buttonsLayout, Alignment.BOTTOM_RIGHT);
	}
	

	/**
	 * 执行保存操作
	 */
	private void executeSave() {
		// 标题
		String titleStr = "";
		if (title.getValue() != null) {
			titleStr = title.getValue().toString();
		}
		if(titleStr.trim().equals("")){
			NotificationUtil.showWarningNotification(this, "消息标题不能为空");
			return;
		}
		// 内容
		String contentStr = "";
		if (content.getValue() != null) {
			contentStr = content.getValue().toString();
		}
		
		Notice notice = (Notice)historyMessage.getTable().getValue();
		notice.setTitle(titleStr);
		notice.setContent(contentStr);
		if(noticeService==null){
			noticeService=SpringContextHolder.getBean("noticeService");
		}
		noticeService.update(notice);
		historyMessage.updateTable(false);
	}
	
	@Override
	public void attach() {
		super.attach();
		Notice notice = (Notice)historyMessage.getTable().getValue();
		title.setValue(notice.getTitle());
		content.setValue(notice.getContent());
	}
	/**
	 * 保存编辑过的消息
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == save) {
			executeSave();
		}
		this.getParent().removeWindow(this);
	}

}
