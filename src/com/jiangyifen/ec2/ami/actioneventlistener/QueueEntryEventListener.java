package com.jiangyifen.ec2.ami.actioneventlistener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.QueueEntryEvent;
import org.asteriskjava.manager.event.QueueStatusCompleteEvent;

import com.jiangyifen.ec2.autodialout.AutoDialHolder;

public class QueueEntryEventListener extends AbstractManagerEventListener {
	
	//存储一组StatusEventMap，用来进行定时比对
	List<QueueEntryEvent> queueEntryEvents = new ArrayList<QueueEntryEvent>();

	@Override
	protected void handleEvent(QueueEntryEvent event) {
//		System.out.println(event);
//		System.out.println("getQueue-->"+event.getQueue());
//		System.out.println("getCallerIdNum-->"+event.getCallerIdNum());
//		System.out.println("getPosition-->"+event.getPosition());
//		System.out.println("getChannel-->"+event.getChannel());
//		System.out.println("getWait-->"+event.getWait());
//		
//		System.out.println("getCallerId-->"+event.getCallerId());
//		System.out.println("getCallerIdName-->"+event.getCallerIdName());
//		System.out.println("getPrivilege-->"+event.getPrivilege());
//		System.out.println("getLine-->"+event.getLine());

		// 将监听到的Event 存入 List
		queueEntryEvents.add(event);
		
		
	}
	
	/**
	 * 在队列状态监听结束时，开始统计每一个队列中的排队人员
	 * 	首先将上一次的统计结果清空（只清空一次）
	 *  然后，将本次的到的排队信息存入到内存中
	 */
	@Override
	protected void handleEvent(QueueStatusCompleteEvent event) {
		Map<String, Boolean> queueToCleared = new HashMap<String, Boolean>();
		for(QueueEntryEvent entryEvent : queueEntryEvents) {
			String queue = entryEvent.getQueue();
			List<QueueEntryEvent> entryEvents = AutoDialHolder.queueToWaiters.get(queue);
			if(entryEvents == null) {
				entryEvents = new ArrayList<QueueEntryEvent>();
				AutoDialHolder.queueToWaiters.put(queue, entryEvents);
			} else {
				Boolean iscleared = queueToCleared.get(queue);
				// 清空上一次得到的各个队列的排队信息
				if(iscleared == null || !iscleared.booleanValue()) {
					entryEvents.clear();
					queueToCleared.put(queue, true);
				}
			}
			entryEvents.add(entryEvent);
		}
		
		// 清空本次的排队集合
		queueEntryEvents.clear();
	}
	
}
