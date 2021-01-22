package com.jiangyifen.ec2.ami;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.QueueMemberPausedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
/**
 * 监听QueueMemberPausedEvent事件 
 * @author chb
 */
public class QueueMemberPausedEventListener extends AbstractManagerEventListener {
	// 日志文件
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//队列状态存储，用来更新界面
	private Map<String, Object> queueStatus = new ConcurrentHashMap<String, Object>();
	
	/**
	 * 监听队列成员置忙置闲状态的事件
	 */
	protected void handleEvent(QueueMemberPausedEvent event) {
		String exten = event.getMemberName().substring(event.getMemberName().indexOf("/") + 1);
		String queueName = event.getQueue();
		String reason = event.getReason();
		boolean paused = event.getPaused();
		if(reason == null) {
			reason = GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON;
		} else {
			try {
				reason = URLDecoder.decode(reason, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error("jrh 管理员界面，将置忙原因 UrlDecode 出现异常-->"+e.getMessage(), e);
			}
		}
		
		queueStatus.put("queue", queueName);
		queueStatus.put("reason", reason);
		queueStatus.put("paused", paused);

		//在Map中取出分机状态对象
		ExtenStatus extenStatus=ShareData.extenStatusMap.get(exten);
		if(extenStatus==null){
			extenStatus=new ExtenStatus();
			ShareData.extenStatusMap.put(exten,extenStatus);
		}
		
		//如果是置忙,则在ExtenStatus中添加，否则移除
		if (paused) {
			extenStatus.getPauseQueues2Reason().put(queueName, reason);
			extenStatus.getUnpauseQueues2Reason().remove(queueName);
		} else {
			extenStatus.getUnpauseQueues2Reason().put(queueName, reason);
			extenStatus.getPauseQueues2Reason().remove(queueName);
		}
		
		// 针对指定用户的更新操作
		Set<String> sipnames = ShareData.extenToUser.keySet();
		for (String sipname : sipnames) {
			if (sipname.equals(exten)) {
				Long userId = ShareData.extenToUser.get(sipname);
				CsrStatusBar statusBar = ShareData.csrToStatusBar.get(userId);
				
				// 只有在用户登陆了系统才需要更新界面组件
				if(statusBar != null) {
					statusBar.updateSinglePauseStatus(queueStatus);
				}
			}
		}
	}
}
