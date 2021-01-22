package com.jiangyifen.ec2.ami;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.QueueCallerAbandonEvent;

public class QueueCallerAbandonEventListener extends
		AbstractManagerEventListener {

//	private QueueRequestDetailService detailService = SpringContextHolder
//			.getBean("queueRequestDetailService");

	// 队列放弃事件
	@Override
	protected void handleEvent(QueueCallerAbandonEvent event) {
//		// 根据event.getUniqueId()从ShareData中取得对应QueueRequestDetail
//		QueueRequestDetail detail = ShareData.queueRequestDetailMap.get(event
//				.getUniqueId());
//		if(detail != null) {
//			// 设置离开队列时间
//			detail.setOutDate(new Date());
//			detail.setIsAnswered(false);
//			
//			// 存到数据库
//			detailService.save(detail);
//		}
//
//		// TODO DELETE
//		seeData(ShareData.queueRequestDetailMap);

	}

//	public void seeData(Map<String, QueueRequestDetail> map) {
//
//		Set<Entry<String, QueueRequestDetail>> set = map.entrySet();
//		Iterator<Entry<String, QueueRequestDetail>> iterator = set.iterator();
//
//		while (iterator.hasNext()) {
//			Entry<String, QueueRequestDetail> entry = iterator.next();
//			System.out.println();
//			System.out.println();
//			System.out.println("entry.getKey() -------> " + entry.getKey());
//			System.out.println("entry.getValue().toString() ---------> "
//					+ entry.getValue().toString());
//			System.out.println();
//			System.out.println();
//		}
//	}
	
}
