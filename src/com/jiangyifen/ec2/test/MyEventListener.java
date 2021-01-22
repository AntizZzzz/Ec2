package com.jiangyifen.ec2.test;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.QueueMemberPausedEvent;
import org.asteriskjava.manager.event.StatusCompleteEvent;
import org.asteriskjava.manager.event.StatusEvent;

public class MyEventListener extends AbstractManagerEventListener {


	protected void handleEvent(QueueMemberPausedEvent event) {
		System.out.println();

		System.out.println(event.getLocation());
		System.out.println(event.getMemberName());
		System.out.println(event.getQueue());
		System.out.println(event.getPaused());
		System.out.println(event.getSource());
		
		System.out.println();
	}
	
	@SuppressWarnings("deprecation")
	protected void hanleEvent(StatusEvent event) {
		System.out.println();
		
		System.out.println("---------------");
	
		System.out.println(event.getAccount() + " <--- account ");
		System.out.println(event.getActionId() + " <--- action id ");
		System.out.println(event.getCallerId() + " <--- caller id ");
		System.out.println(event.getCallerIdName() + " <--- caller id name ");
		System.out.println(event.getCallerIdNum() + " <--- caller id num ");
		System.out.println(event.getChannel() + " <--- channel ");
		System.out.println(event.getContext() + " <--- context ");
		System.out.println(event.getDateReceived() + " <--- date received ");
		System.out.println(event.getExtension() + " <--- extension ");
		System.out.println(event.getInternalActionId() + " <--- internal action id ");
		System.out.println(event.getLink() + " <--- link ");
		System.out.println(event.getPrivilege() + " <--- privilege ");
		System.out.println(event.getPriority() + " <--- priority ");
		System.out.println(event.getSeconds() + " <--- seconds ");
		System.out.println(event.getSource() + " <--- source ");
		System.out.println(event.getState() + " <--- state ");
		System.out.println(event.getUniqueId() + " <--- unique id ");
		System.out.println(event.getTimestamp() + " <--- time stamp");
		System.out.println(event.getClass() + " <--- class ");
		
		System.out.println();
	}
	
	protected void hanleEvent(StatusCompleteEvent statusCompleteEvent) {
		System.out.println();
		
		System.out.println(statusCompleteEvent.getActionId());
		System.out.println(statusCompleteEvent.getInternalActionId());
		System.out.println(statusCompleteEvent.getPrivilege());
		System.out.println(statusCompleteEvent.getSource());
		System.out.println(statusCompleteEvent.getDateReceived());
		System.out.println(statusCompleteEvent.getTimestamp());
		
		System.out.println();
	}


}
