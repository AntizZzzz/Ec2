package com.jiangyifen.ec2.ami.actioneventlistener;

import java.util.ArrayList;
import java.util.List;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.QueueMemberEvent;

import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 监听队列成员状况
 * jrh
 */
public class QueueMemberEventListener extends AbstractManagerEventListener {
	
//    public static final int AST_DEVICE_NOT_INUSE = 1;
//    public static final int AST_DEVICE_INUSE = 2;
//    public static final int AST_DEVICE_BUSY = 3;
//    public static final int AST_DEVICE_INVALID = 4;
//    public static final int AST_DEVICE_UNAVAILABLE = 5;
//    public static final int AST_DEVICE_RINGING = 6;
//    public static final int AST_DEVICE_RINGINUSE = 7;
//    public static final int AST_DEVICE_ONHOLD = 8;
	
	
	protected void handleEvent(QueueMemberEvent event) {
//		System.out.println();
//		System.out.println("------------------------------------");
//		System.out.println(event);
		
		// jrh 因为队列成员可能包含手机号
		String member = event.getName();
		String queueName = event.getQueue();
		
		// jrh 如果内存中不存在当前队列，则将队列加入内存维护
		if(!ShareData.queue2Members.keySet().contains(queueName)) {
			ShareData.queue2Members.put(queueName, new ArrayList<String>());
		}
		// jrh 如果成员是新加入的，则放入内存
		List<String> members = ShareData.queue2Members.get(queueName);
		if(!members.contains(member)) {
			members.add(member);
		}
	}
}
