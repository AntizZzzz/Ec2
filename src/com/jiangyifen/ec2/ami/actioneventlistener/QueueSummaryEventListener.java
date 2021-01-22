package com.jiangyifen.ec2.ami.actioneventlistener;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.QueueSummaryEvent;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;
/**
 * 监听QueueParamsEvent,以统计当前队列中可用CSR情况
 * @author chb
 */
public class QueueSummaryEventListener extends AbstractManagerEventListener {
	/**
	 * 通过发送QueueSummaryAction，监听QueueParamsEvent,以统计当前队列中可用CSR情况
	 */
	protected void handleEvent(QueueSummaryEvent event) {
		String queue=event.getQueue();
		Integer loggedIn=event.getLoggedIn();
		Integer available=event.getAvailable();
		Integer callers=event.getCallers();
		AutoDialHolder.queueToLoggedIn.put(queue, loggedIn);
		AutoDialHolder.queueToAvailable.put(queue, available);
		AutoDialHolder.queueToCallers.put(queue, callers);
	}
}
