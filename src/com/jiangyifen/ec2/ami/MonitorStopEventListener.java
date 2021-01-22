package com.jiangyifen.ec2.ami;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.event.HangupEvent;

/**
 * 监听录音结束事件
 * @author chb
 */
public class MonitorStopEventListener extends AbstractManagerEventListener {
//	private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
	/**
	 * 监听MonitorStopEvent触发的事件
	 */
	@Override
	protected void handleEvent(HangupEvent event) {
//		 String uniqueId=event.getUniqueId();
//		 String fileName=ShareData.recordFileName.get(uniqueId);
//		 ShareData.recordFileName.remove(uniqueId);
//		 if(fileName==null) return;
//		 
//		 try {
//			 String mvShell="mv /dev/shm/"+uniqueId+".wav /var/www/html/monitor/"+sdf.format(new Date())+"/"+fileName;
//			Runtime.getRuntime().exec(mvShell);
//		} catch (IOException e) {
//			LoggerUtil.logWarn(this, "移动录音文件失败！！！");
//			e.printStackTrace();
//		}
	}
}
