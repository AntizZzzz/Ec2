package com.jiangyifen.ec2.ami.actioneventlistener;

import org.asteriskjava.manager.event.QueueStatusCompleteEvent;

public class QueueStatusCompleteEventListener  extends AbstractCompleteManagerEventListener {
	@Override
	protected void handleEvent(QueueStatusCompleteEvent event) {
		this.setLastEventReceiveTime(System.currentTimeMillis());
		super.handleEvent(event);
	}
}
