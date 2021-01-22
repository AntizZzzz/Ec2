package com.jiangyifen.ec2.backgroundthread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.mgr.system.realtime.RealTimeSystemStatisticPanel;
import com.jiangyifen.ec2.ui.mgr.system.realtime.pojo.form.SystemStatistic;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemStatus;
import com.jiangyifen.ec2.utils.SnmpInfoTool;

/**
 * 后台获取系统监控数据
 * @author lxy
 *
 */
public class SystemStatisticThread extends Thread {

	private static Logger logger = LoggerFactory.getLogger(SystemStatisticThread.class);
			
	private static long THREAD_SLEEP = 2 * 1000;
	
	public SystemStatisticThread() {
		this.setDaemon(true);
		this.setName("SystemStatisticThread");
		this.start();
 	}
	
	private void updateComponent(){
 		SystemStatistic statistic = SnmpInfoTool.findSystemStatistic();
		 if(null != statistic){
			 for (Long userid : ShareData.systemStatusMap.keySet()) {
				 SystemStatus systemStatus = ShareData.systemStatusMap.get(userid);
				 if(null != systemStatus){
					 RealTimeSystemStatisticPanel  systemStatisticPanel = systemStatus.panelStatistics;
					 if(null != systemStatisticPanel){
						 systemStatisticPanel.updateStatistics(statistic);
					 }
				 }
			 }
		 }
	}
	 
	@Override
	public void run() {
		try {
			while (true) {
 				updateComponent();
				Thread.sleep(THREAD_SLEEP);
			}
		} catch (Exception e) {
			 e.printStackTrace();
			 logger.error("后台获取系统监控失败_SystemStatisticThread_run_LLXXYY", e);
		}
	}

}
