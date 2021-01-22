package com.jiangyifen.ec2.ui.mgr.autodialout;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.GetVarAction;
import org.asteriskjava.manager.response.ManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.entity.AutoDialoutTask;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.mgr.tabsheet.AutoDialout;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.ShareDataElementUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 监控自动外呼添状态
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class AutoDialoutMonitor extends Window implements Window.CloseListener{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// Attach初始化
	private Label projectNameLabel = new Label();
	private Label autoDialoutNameLabel = new Label();
	private Label outlineNumberLabel = new Label();
	private Label outlineCapabilityLabel = new Label();
	private Label queueNameLabel = new Label(); // 队列名
	private Label queueDeepthLabel = new Label(); // 队列中的排队数量

	// 线程改变数值
	private String outlineNumber;
	private String queueName;
	private Label outlineConcurrentLabel = new Label(); // 外线并发数
	private Label csrAvailableLabel = new Label(); // CSR空闲数
	private Label csrLoggedInLabel = new Label(); // CSR登陆数
	private Label queueCallersLabel = new Label(); // 队列中的排队数量

	/**
	 * 其他组件
	 */
	private RefreshThread refreshThread;
//	private Domain domain;
//	private AutoDialoutTaskService autoDialoutTaskService;
	// 持有从Table取出来的AutoDialoutTask引用
	private AutoDialoutTask autoDialoutTask;
	// 持有调用它的组件引用AutoDialout,以刷新父组件
	private AutoDialout autoDialout;
	
	public AutoDialoutMonitor(AutoDialout autoDialout) {
		this.initService();
		this.center();
		this.setModal(false);
		this.setResizable(false);
		this.setCaption("自动外呼状态监控");
		this.autoDialout = autoDialout;
		this.addListener((Window.CloseListener)this);

		// 添加Window内最大的Layout
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);

		GridLayout gridLayout = new GridLayout(6, 4);
		gridLayout.setSpacing(true);
		windowContent.addComponent(gridLayout);

		// 第一行
		Label projectLabel=new Label("项目:");
		projectLabel.setDescription("自动外呼使用的项目");
		gridLayout.addComponent(projectLabel, 0, 0);
		gridLayout.addComponent(projectNameLabel, 1, 0);
		
		Label autoDialLabel = new Label("自动外呼:");
		autoDialLabel.setDescription("当前监控的自动外呼");
		gridLayout.addComponent(autoDialLabel, 2, 0);
		gridLayout.addComponent(autoDialoutNameLabel, 3, 0);
//		gridLayout.addComponent(start, 4, 0);
//		gridLayout.addComponent(stop, 5, 0);
		
		// 第二行
		Label outlineLabel = new Label("外线:");
		outlineLabel.setDescription("自动外呼当前使用的外线");
		gridLayout.addComponent(outlineLabel, 0, 1);
		gridLayout.addComponent(outlineNumberLabel, 1, 1);
		Label concurrentLabel = new Label("并发数:");
		concurrentLabel.setDescription("当前这条外线上的并发，该数字为正在振铃和已经建立通话的总数");
		gridLayout.addComponent(concurrentLabel, 2, 1);
		gridLayout.addComponent(outlineConcurrentLabel, 3, 1);
		Label capcityLabel = new Label("外线容量:");
		capcityLabel.setDescription("是当前外线允许的最大并发数,请确认此数字小于等于外线实际支持的并发总数");
		gridLayout.addComponent(capcityLabel, 4, 1);
		gridLayout.addComponent(outlineCapabilityLabel, 5, 1);

		// 第三行
		gridLayout.addComponent(new Label("CSR"), 0, 2);
		gridLayout.addComponent(new Label(""), 1, 2);
		Label csrIdle = new Label("空闲数:");
		csrIdle.setDescription("当前可用坐席的总数");
		gridLayout.addComponent(csrIdle, 2, 2);
		gridLayout.addComponent(csrAvailableLabel, 3, 2);
		Label csrLogin = new Label("登陆数:");
		csrLogin.setDescription("所有加入到队列中的坐席总数");
		gridLayout.addComponent(csrLogin, 4, 2);
		gridLayout.addComponent(csrLoggedInLabel, 5, 2);

		// 第四行
		Label queueLabel = new Label("队列:");
		queueLabel.setDescription("呼入的电话进入到系统中的哪个队列");
		gridLayout.addComponent(queueLabel, 0, 3);
		gridLayout.addComponent(queueNameLabel, 1, 3);
		Label inqueue = new Label("排队数:");
		inqueue.setDescription("在队列中等待坐席接起的客户数量");
		gridLayout.addComponent(inqueue, 2, 3);
		gridLayout.addComponent(queueCallersLabel, 3, 3);
		Label queueLengthLabel = new Label("队列长度:");
		queueLengthLabel.setDescription("队列中最多能容纳多少客户");
		gridLayout.addComponent(queueLengthLabel, 4, 3);
		gridLayout.addComponent(queueDeepthLabel, 5, 3);
	}

	/**
	 * Attach 事件
	 */
	@Override
	public void attach() {
		autoDialoutTask = (AutoDialoutTask) autoDialout.getTable().getValue();
		MarketingProject marketingProject = autoDialoutTask.getMarketingProject();
		projectNameLabel.setValue(marketingProject.getProjectName());
		autoDialoutNameLabel.setValue(autoDialoutTask.getAutoDialoutTaskName());
		//外线 
		// jrh TODO 这一块的机制不好，既然知道项目，就直接可以获取到外线对象或默认外线对象，这样后面的外线容量等信息不再需要查数据库来获取
		// jrh 而当前使用 ShareDataElementUtil 的方式，导致重复查询数据库，而且代码可读性降低了
		outlineNumber=ShareDataElementUtil.getOutlineByProject(marketingProject);
		outlineNumber=(outlineNumber==null?"":outlineNumber);
		outlineNumberLabel.setValue(outlineNumber);
		Integer outlineCapability=ShareDataElementUtil.getOutlineCapabilityByProject(marketingProject);
		outlineCapability=(outlineCapability==null?0:outlineCapability);
		outlineCapabilityLabel.setValue(outlineCapability);
		//队列
		queueName=autoDialoutTask.getQueue().getName();
		queueName=(queueName==null?"":queueName);
		queueNameLabel.setValue(queueName);
		Integer queueDeepth=autoDialoutTask.getQueue().getMaxlen().intValue();
		queueDeepth=(queueDeepth==null?0:queueDeepth);
		queueDeepthLabel.setValue(queueDeepth);
		
		
		/**
		 * 需要线程该变的数值
		 */
		ShareData.mgrToAutoDialoutMonitor.put(SpringContextHolder.getLoginUser().getId(), this);
		if(refreshThread!=null){
			refreshThread.setIsloop(false);
		}
		//启动线程
		refreshThread=new RefreshThread();
		refreshThread.start();
		
	}
	
	/**
	 * 停止自动外呼Monitor中的线程
	 */
	public void stopThread(){
		if(refreshThread!=null){
			refreshThread.setIsloop(false);
		}
	}

	/**
	 * 刷新自动外呼监控页面的相关显示信息
	 * @author chb
	 *
	 */
	private class RefreshThread extends Thread {
		private volatile Boolean isLoop=true;

		// 刷新页面信息线程的运行
		@SuppressWarnings("static-access")
		@Override
		public void run() {
			//线程改变数值
			while(isLoop){
				//获取外线的并发数量
				if(outlineNumber.equals("")||outlineNumber==null){
					outlineConcurrentLabel.setValue("");
				}else{
					//chenhb chenaged 20141113 ***
					ManagerResponse managerResponse = AmiManagerThread.sendResponseAction(new GetVarAction(outlineNumber));
					String concurrentNum = managerResponse.getAttribute("Value");
					if(!StringUtils.isNumeric(concurrentNum)){						
						Set<String> channels = ShareData.peernameAndChannels.get(outlineNumber);
//						Integer current=0;
						if(channels !=null){
							concurrentNum=channels.size()+"";
						}
					}
					outlineConcurrentLabel.setValue(concurrentNum);
					// *** 
				}
				
				//根据队列获取指定队列的CSR空闲数、登陆数、排队数
				if(queueName.equals("")||queueName==null){
					csrAvailableLabel.setValue(0);
					csrLoggedInLabel.setValue(0);
					queueCallersLabel.setValue(0);
				}else{
					//获取CSR空闲数
					Integer available = AutoDialHolder.queueToAvailable.get(queueName);
					if(available!=null){
						csrAvailableLabel.setValue(available);
					}else{
						csrAvailableLabel.setValue(0);
					}
					
					//获取CSR登陆数
					Integer loggedIn =AutoDialHolder.queueToLoggedIn.get(queueName);
					if(loggedIn!=null){
						csrLoggedInLabel.setValue(loggedIn);
					}else{
						csrLoggedInLabel.setValue(0);
					}
					
					//获取队列中的排队数量
					Integer callers =AutoDialHolder.queueToCallers.get(queueName);
					if(callers!=null){
						queueCallersLabel.setValue(callers);
					}else{
						queueCallersLabel.setValue(0);
					}	
				}
				
				//让线程睡眠1s
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					logger.info("chb 线程睡眠异常",e);
					break;
				}
			}
		}
		//设置线程是否运行
		public void setIsloop(Boolean isLoop){
			this.isLoop=isLoop;
		}
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
//		domain = SpringContextHolder.getDomain();
//		autoDialoutTaskService = SpringContextHolder
//				.getBean("autoDialoutTaskService");
	}

	/**
	 * 监听窗口关闭的事件,在窗口关闭时停止线程的运行
	 * @param e
	 */
	@Override
	public void windowClose(CloseEvent e) {
		if(refreshThread!=null){
			refreshThread.setIsloop(false);
			refreshThread=null;
		}
	}

}
