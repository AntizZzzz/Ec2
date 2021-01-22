package com.jiangyifen.ec2.ami.actioneventlistener;

import org.asteriskjava.manager.event.StatusCompleteEvent;

public class StatusCompleteEventListener  extends AbstractCompleteManagerEventListener {
	@Override
	protected void handleEvent(StatusCompleteEvent event) {
		this.setLastEventReceiveTime(System.currentTimeMillis());
		super.handleEvent(event);
	}
}
