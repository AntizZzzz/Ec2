package com.jiangyifen.ec2.ami.actioneventlistener;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.PeerEntryEvent;

import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.globaldata.ShareData;

/**
 * 监听PeerEntryEvent事件
 * 
 * @author jrh
 */
public class PeerEntryEventListener extends AbstractManagerEventListener {
	/**
	 * 监听PeerEntryEvent触发的事件
	 */
	@Override
	protected void handleEvent(PeerEntryEvent event) {
		String sipName = event.getObjectName();
		String ip = event.getIpAddress();
		Integer port = event.getPort();
		String registerStatus = event.getStatus();

		// 在Map中取出分机状态对象
		ExtenStatus extenStatus = ShareData.extenStatusMap.get(sipName);
		if (extenStatus == null) {
			extenStatus = new ExtenStatus();
			ShareData.extenStatusMap.put(sipName, extenStatus);
		}

		// 为ExtenStatus设置值
		extenStatus.setSipName(sipName);
		extenStatus.setIp(ip);
		extenStatus.setPort(port);
		extenStatus.setRegisterStatus(registerStatus);
	}
}
