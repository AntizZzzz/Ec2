package com.jiangyifen.ec2.ami.actioneventlistener;

import org.asteriskjava.manager.AbstractManagerEventListener;
/**
 * 监听的所有事件完成的Listener
 * @author chb
 *
 */
public abstract class AbstractCompleteManagerEventListener extends AbstractManagerEventListener {
	private long lastEventReceiveTime = 0;

	public long getLastEventReceiveTime() {
		return lastEventReceiveTime;
	}

	public void setLastEventReceiveTime(long lastEventReceiveTime) {
		this.lastEventReceiveTime = lastEventReceiveTime;
	}
}
