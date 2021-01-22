package com.jiangyifen.ec2.ami.actioneventlistener;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.HangupEvent;

public class QueueParamsEventListener extends AbstractManagerEventListener {
	@Override
	protected void handleEvent(HangupEvent event) {
		super.handleEvent(event);
	}
}
