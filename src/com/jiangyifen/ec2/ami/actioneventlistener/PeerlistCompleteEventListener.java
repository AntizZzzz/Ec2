package com.jiangyifen.ec2.ami.actioneventlistener;

import org.asteriskjava.manager.event.PeerlistCompleteEvent;

public class PeerlistCompleteEventListener  extends AbstractCompleteManagerEventListener {
	@Override
	protected void handleEvent(PeerlistCompleteEvent event) {
		this.setLastEventReceiveTime(System.currentTimeMillis());
		super.handleEvent(event);
	}
}
