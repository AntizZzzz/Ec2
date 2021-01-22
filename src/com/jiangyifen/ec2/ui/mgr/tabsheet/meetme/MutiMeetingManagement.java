package com.jiangyifen.ec2.ui.mgr.tabsheet.meetme;

import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class MutiMeetingManagement extends VerticalLayout implements ClickListener {
	
	private Notification notification;					// 提示信息
	
	private TextField inviteNumber_tf;
	private Button invite_b;
	
	private Long domainId;
	
	private MeetMeInviteAction meetMeInviteAction;
	
	public MutiMeetingManagement() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);

		domainId = SpringContextHolder.getDomain().getId();
		
		meetMeInviteAction = new MeetMeInviteAction();
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		HorizontalLayout invite_hl = new HorizontalLayout();
		invite_hl.setSpacing(true);
		this.addComponent(invite_hl);
		
		Label caption_l = new Label("<B>邀请号码：</B>", Label.CONTENT_XHTML);
		invite_hl.addComponent(caption_l);
		invite_hl.setComponentAlignment(caption_l, Alignment.MIDDLE_LEFT);
		
		inviteNumber_tf = new TextField();
		inviteNumber_tf.setValue("13816760398");
		inviteNumber_tf.addValidator(new RegexpValidator("\\d+", "号码只能是数字"));
		inviteNumber_tf.setValidationVisible(false);
		invite_hl.addComponent(inviteNumber_tf);
		invite_hl.setComponentAlignment(inviteNumber_tf, Alignment.MIDDLE_LEFT);
		
		invite_b = new Button("邀请", this);
		invite_b.setImmediate(true);
		invite_hl.addComponent(invite_b);
		invite_hl.setComponentAlignment(invite_b, Alignment.MIDDLE_LEFT);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == invite_b) {
			String number = (String) inviteNumber_tf.getValue();
			if(!inviteNumber_tf.isValid() || "".endsWith(number)) {
				notification.setCaption("<font color='red'><B>邀请号码不能为空，并且只能是数字</B></font>");
				this.getApplication().getMainWindow().showNotification(notification);
				return;
			}
			
			meetMeInviteAction.execute(domainId, number);
			System.out.println(number);
		}
	}
	
}
