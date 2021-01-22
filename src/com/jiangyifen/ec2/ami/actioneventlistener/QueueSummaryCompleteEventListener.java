package com.jiangyifen.ec2.ami.actioneventlistener;

import org.asteriskjava.manager.event.QueueSummaryCompleteEvent;

public class QueueSummaryCompleteEventListener  extends AbstractCompleteManagerEventListener {
	@Override
	protected void handleEvent(QueueSummaryCompleteEvent event) {
		this.setLastEventReceiveTime(System.currentTimeMillis());
		super.handleEvent(event);
	}
}
