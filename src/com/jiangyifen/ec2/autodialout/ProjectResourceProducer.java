package com.jiangyifen.ec2.autodialout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.service.eaoservice.AutoDialoutTaskService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.utils.AutoDialConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 定期从数据库取资源线程 
 * @author chb
 */
public class ProjectResourceProducer extends Thread{
	private volatile Boolean isLoop;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//容器的资源数量低于下限MIN则取新资源
	private Long MIN=0L;
	
	//取数据的步长值,一般情况下,步长值应该大于最小值
	private Long STEP=0L;
	
	//项目中当前Task的Id值，用来做取数据的标记
	private Long CURSOR=0L;
	
	//线程睡眠时间调整
	private Long THREAD_SLEEP_TIME=0L;
	
	//若取不到资源则将此值设置为true，等待资源消费线程将队列中的资源消耗光，然后停止获取线程和消费线程
	private Boolean waitForStop=false;
	
	//依赖此生产线程的消费线程
	private List<Thread> dependConsumerList=new ArrayList<Thread>();
	
	//盛装资源的容器
	private ConcurrentLinkedQueue<MarketingProjectTask> resourceQueue=new ConcurrentLinkedQueue<MarketingProjectTask>();
	
	//Service
	private Long projectId;
	private AutoDialoutTask autoDialoutTask;
	private MarketingProjectTaskService marketingProjectTaskService;
	private AutoDialoutTaskService autoDialoutTaskService;
	
	private String threadName;
	/**
	 * 以项目Id为参数构造线程
	 * @param projectId
	 */
	public ProjectResourceProducer(AutoDialoutTask autoDialoutTask,Long projectId) {
		//初始化Service
		marketingProjectTaskService=SpringContextHolder.getBean("marketingProjectTaskService");
		autoDialoutTaskService=SpringContextHolder.getBean("autoDialoutTaskService");

		//初始化生产者线程的参数
		MIN=Long.parseLong(AutoDialConfig.props.getProperty(AutoDialConfig.PRODUCER_MIN));
		STEP=Long.parseLong(AutoDialConfig.props.getProperty(AutoDialConfig.PRODUCER_STEP));
		THREAD_SLEEP_TIME=Long.parseLong(AutoDialConfig.props.getProperty(AutoDialConfig.PRODUCER_SLEEP_TIME));
		
		this.threadName=AutoDialHolder.AUTODIAL_PRODUCER_PRE+projectId;
		isLoop=true;
		this.setName(threadName);
		this.projectId=projectId;
		this.autoDialoutTask=autoDialoutTask;
		this.setDaemon(true);
		AutoDialHolder.nameToThread.put(threadName, this);
		this.start();
	}
	
	/**
	 * 取数据线程的运行
	 */
	@Override
	public void run() {
logger.info("启动生产线程："+threadName);
		// 循环判断资源的数量是不是低于最小值，如果低于就去取新的资源
		try {
			while (isLoop) {
				try {
					//如果线程取不到数据,则查看resourceQueue中的资源是不是空,等待依赖于它的消费线程将数据消费光,然后停止线程
					if(waitForStop==true){
						if(resourceQueue.isEmpty()){
							//停止线程，更新实体，PI更新界面状态
							isLoop=false; //停止当前线程和依赖它的消费线程
							autoDialoutTask.setAutoDialoutTaskStatus(AutoDialoutTaskStatus.PAUSE);
							autoDialoutTaskService.update(autoDialoutTask);
logger.info("chenhb--waitForStop--resourceQueueEmpty--"+threadName);
						}else{
							//do nothing but wait!

logger.info("chenhb--waitForStop--doNothing--"+threadName);
						}
					}else{
						if(resourceQueue.size()<MIN){
							// 取数据使得数据数量大于MAX值
							resourceQueue.addAll(getStepResource());
logger.info("chenhb--waitForStop--resourceQueueSizeLT--"+MIN+"--Thread--"+threadName);
						}else{
logger.info("chenhb--waitForStop--resourceQueueSizeGT--"+MIN+"--Thread--"+threadName);							
						}
					}
				} catch (Exception e1) {
					logger.error("chb : 获取数据线程异常！", e1);
				}
				Thread.sleep(THREAD_SLEEP_TIME);
			}
		} catch (InterruptedException e) {
			logger.error("chb : 生产线程睡眠异常,线程结束！", e);
		}
		AutoDialHolder.nameToThread.remove(threadName);
logger.info("停止生产线程:"+threadName);
	}

	/**
	 * 取出步长值数量的数据
	 * @return
	 */
	private List<MarketingProjectTask> getStepResource() {
		
		Long T1 = System.currentTimeMillis();
		List<MarketingProjectTask> resourceList=marketingProjectTaskService.getStepNotDistributedProjectTaskByMinId(projectId,CURSOR,STEP); //里面的资源Id应该由小到大排列
		Long T2 = System.currentTimeMillis();
logger.info(threadName+" GetStepResourceListTime: "+(T2-T1)+" ResourceListSize "+resourceList.size());

logger.info("chenhb--getStepResource--resourceList--"+resourceList.size()+"--Thread--"+threadName);
		if(resourceList.size()>0){
			//更新Cursor值
			CURSOR=resourceList.get(resourceList.size()-1).getId();
		}else{
logger.info("chenhb--getStepResource--resourceNotEnrough--"+resourceList.size()+"--Thread--"+threadName);
			//标记，让线程等待
			waitForStop=true;
		}
		return resourceList;
	}

	//记录依赖此生产者线程的消费者线程数量
	public void addDependConsumer(Thread thread){
		synchronized (dependConsumerList) {
			dependConsumerList.add(thread);
		}
	} 
	
	//记录依赖此生产者线程的消费者线程数量
	public void removeDependConsumer(String threadName){
		synchronized (dependConsumerList) {
			//从依赖此生产线程的消费线程中停止并且移除消费线程
			ProjectResourceConsumer consumerThread=null;
			for(Thread dependThread:dependConsumerList){
				if(dependThread.getName().equals(threadName)){
					consumerThread=(ProjectResourceConsumer)dependThread;
					consumerThread.setIsLoop(false);
				}
			}
			
			if(consumerThread!=null){
				dependConsumerList.remove(consumerThread);
			}
			
			//如果生产线程没有依赖线程，则停止自身
			if(dependConsumerList.isEmpty()){
				this.isLoop=false;
			}
		}
	}
	//============= Getter and Setter ========================//
	public Boolean getIsLoop() {
		return isLoop;
	}

	public void setIsLoop(Boolean isLoop) {
		this.isLoop = isLoop;
	}

	public ConcurrentLinkedQueue<MarketingProjectTask> getResourceQueue() {
		return resourceQueue;
	}

	public void setResourceQueue(
			ConcurrentLinkedQueue<MarketingProjectTask> resourceQueue) {
		this.resourceQueue = resourceQueue;
	}
}
