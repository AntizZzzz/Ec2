package com.jiangyifen.ec2.backgroundthread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.asteriskjava.manager.AbstractManagerEventListener;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerConnectionState;
import org.asteriskjava.manager.action.AbstractManagerAction;
import org.asteriskjava.manager.action.QueueStatusAction;
import org.asteriskjava.manager.action.QueueSummaryAction;
import org.asteriskjava.manager.action.SipPeersAction;
import org.asteriskjava.manager.action.StatusAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.actioneventlistener.AbstractCompleteManagerEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.PeerEntryEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.PeerlistCompleteEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.QueueEntryEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.QueueMemberEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.QueueParamsEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.QueueStatusCompleteEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.QueueSummaryCompleteEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.QueueSummaryEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.StatusCompleteEventListener;
import com.jiangyifen.ec2.ami.actioneventlistener.StatusEventListener;

import com.jiangyifen.ec2.utils.Config;
/**
 * 每个需要持续发送的action都有自己的一个独立线程
 * @author chb
 *
 */
public class ActionThreadStarter {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 针对每个Action启动一个单独的线程
	 */
	public void start(){
		//取得action对应Listener的Map
		Map<AbstractManagerAction,List<AbstractManagerEventListener>> actionListenerMap=initActionListenerMap();
		
		//对Map进行轮询，根据Action的不同启动不同时间刷新的线程
		Set<AbstractManagerAction> actions = actionListenerMap.keySet();
		for(AbstractManagerAction action:actions){
			if(action instanceof QueueSummaryAction){
				startThread(action,actionListenerMap.get(action),1000,60*1000);
			}else if(action instanceof QueueStatusAction){
				startThread(action,actionListenerMap.get(action),1000,60*1000);
			}else if(action instanceof SipPeersAction){
				startThread(action,actionListenerMap.get(action),1000,60*1000);
			}else if(action instanceof StatusAction){
				startThread(action,actionListenerMap.get(action),1000,60*1000);
			}else{
				//Do Nothing
			}
		}
	}
	
	/**
	 * 每个CompleteEventListener都必须放在List的最后一位
	 */
	private Map<AbstractManagerAction,List<AbstractManagerEventListener>> initActionListenerMap(){
		//存放发送action和接收event的listener
		Map<AbstractManagerAction,List<AbstractManagerEventListener>> actionListenerMap=new HashMap<AbstractManagerAction, List<AbstractManagerEventListener>>();
		
		//=========队列概况===========//
		QueueSummaryAction queueSummaryAction=new QueueSummaryAction();
		
		List<AbstractManagerEventListener> queueSummaryActionListeners=new ArrayList<AbstractManagerEventListener>();
		QueueSummaryEventListener queueSummaryEventListener=new QueueSummaryEventListener();
		queueSummaryActionListeners.add(queueSummaryEventListener);
		QueueSummaryCompleteEventListener queueSummaryCompleteEventListener=new QueueSummaryCompleteEventListener();
		queueSummaryActionListeners.add(queueSummaryCompleteEventListener);
		
		actionListenerMap.put(queueSummaryAction, queueSummaryActionListeners);
		
		//=========队列状态===========//
		QueueStatusAction queueStatusAction=new QueueStatusAction();
		
		List<AbstractManagerEventListener> queueStatusActionListeners=new ArrayList<AbstractManagerEventListener>();
		QueueParamsEventListener queueParamsEventListener=new QueueParamsEventListener();
		queueStatusActionListeners.add(queueParamsEventListener);
		QueueMemberEventListener queueMemberEventListener=new QueueMemberEventListener();
		queueStatusActionListeners.add(queueMemberEventListener);
		QueueEntryEventListener queueEntryEventListener=new QueueEntryEventListener();
		queueStatusActionListeners.add(queueEntryEventListener);
		QueueStatusCompleteEventListener queueStatusCompleteEventListener=new QueueStatusCompleteEventListener();
		queueStatusActionListeners.add(queueStatusCompleteEventListener);
		
		actionListenerMap.put(queueStatusAction, queueStatusActionListeners);
		
		//========分机状态============//
		SipPeersAction sipPeersAction=new SipPeersAction();
		
		List<AbstractManagerEventListener> sipPeersActionListeners=new ArrayList<AbstractManagerEventListener>();
		PeerEntryEventListener peerEntryEventListener=new PeerEntryEventListener();
		sipPeersActionListeners.add(peerEntryEventListener);
		PeerlistCompleteEventListener peerlistCompleteEventListener=new PeerlistCompleteEventListener();
		sipPeersActionListeners.add(peerlistCompleteEventListener);
		
		actionListenerMap.put(sipPeersAction, sipPeersActionListeners);
		
		//========StatusAction状态============//
		StatusAction statusAction=new StatusAction();

		List<AbstractManagerEventListener> statusActionListeners=new ArrayList<AbstractManagerEventListener>();
		StatusEventListener statusEventListener=new StatusEventListener();
		statusActionListeners.add(statusEventListener);
		StatusCompleteEventListener statusCompleteEventListener=new StatusCompleteEventListener();
		statusActionListeners.add(statusCompleteEventListener);
		
		actionListenerMap.put(statusAction, statusActionListeners);

		return actionListenerMap;
	}


	/**
	 * 根据Action和Action对应的Event启动相应的线程
	 * @param actions 
	 * @param list Action对应的Event的List集合，最后一个Event为CompleteEvent
	 * @param interval 收到CompleteEvent后发送下一个Event的的时间间隔
	 * @param missEventConfirmTime 确认CompleteEvent丢失的时间,在此时间后会再次发出Action
	 */
	private void startThread(final AbstractManagerAction action,final List<AbstractManagerEventListener> listenerList,final long interval,final long missEventConfirmTime) {
		//创建线程，线程名字为Action的名字加时间
		new Thread(action.getClass().getCanonicalName()+System.currentTimeMillis()){
			private ManagerConnection managerConnection;
			private AbstractCompleteManagerEventListener completeListener;
			/**
			 * 线程运行方法
			 */
			@SuppressWarnings("static-access")
			public void run() {
				//设置为后台线程 
//				this.setDaemon(true);
				try {
					this.sleep(30*1000);
				} catch (Exception e1) {
					//do nothing
				}
				 
				// 我们只需初始化第一次连接，之后如果断线Asterisk会自动重连
				while (true) {
					try {
						//创建连接工厂，由于线程不会太多，所以每次创建新的连接工厂
						String ip = Config.props.getProperty(Config.AMI_IP);
						String username = Config.props.getProperty(Config.AMI_USERNAME);
						String password = Config.props.getProperty(Config.AMI_PWD);
						ManagerConnectionFactory factory = new ManagerConnectionFactory(ip,username, password);
						
						managerConnection = factory.createManagerConnection();
						
						for(AbstractManagerEventListener listener:listenerList){
							if(listener instanceof AbstractCompleteManagerEventListener){
								completeListener=(AbstractCompleteManagerEventListener)listener;
							}
							managerConnection.addEventListener(listener);
						}
						managerConnection.login();
//临时调试信息
logger.info("chb: "+this.getName()+" ActionThread connected successful !");
						break;
					} catch (Exception e) {
logger.info("chb: "+this.getName()+" ActionThread connect failed ! will reconnect in 10 seconds !");
						try {
							Thread.sleep(10*1000);
							managerConnection.logoff(); //Logoff 并不关闭连接，必要性不大
						} catch (Exception e1) {
							//do nothing
						}
					}
				}
				
				//不断尝试判断是否要发送Action
				while(true){
					actionSenderLogic();
					//线程睡眠0.5秒
					try {
						Thread.sleep(100);
					} catch (Exception e1) {
						//do nothing
					}
				}

			}
			
			long recordedTime=0; //记录时间（接收到 CompleteEvent 或 发送Action的时间 ）
			long lastCompleteEventReceiveTime=0; //上一次接收到CompleteEvent的时间
			boolean isExpireLogic=true; //记录是否是超时逻辑： true 超时逻辑           false 等待发送逻辑 
			
			/**
			 * 发送Action的处理逻辑
			 */
			private void actionSenderLogic(){
				//第一次发送Action
				if(recordedTime==0){
					sendAction();
					recordedTime=System.currentTimeMillis();
					isExpireLogic=true;
//System.out.println("=======FirstTimeSend 首次发送 ");
					return;
				}
				
				//记录上一次接收到CompleteEvent的时间，更新记录时间，并更改逻辑为等待发送逻辑 (false)
				long receiveTime=completeListener.getLastEventReceiveTime();
				if(receiveTime!=lastCompleteEventReceiveTime){
					lastCompleteEventReceiveTime=receiveTime;
					recordedTime=receiveTime;
					isExpireLogic=false;  //只有当接收到CompleteEvent时是等待逻辑
//System.out.println("=======ReceiveEvent 接收到Event ");
					return;
				}
				
				//超时逻辑和等待发送逻辑的判断处理
				if(isExpireLogic){
					if(System.currentTimeMillis()>recordedTime+missEventConfirmTime){ //已经超时，判断是CompleteEvent丢失
						sendAction();
						recordedTime=System.currentTimeMillis();
						isExpireLogic=true;
//System.out.println("=======ExpireLogic 超时逻辑发送Action ");
					}
				}else{ //等待发送逻辑
					if(System.currentTimeMillis()>lastCompleteEventReceiveTime+interval){ //说明已经到了发送时间
						sendAction();
						recordedTime=System.currentTimeMillis();
						isExpireLogic=true;
//System.out.println("=======WaitLogic 等待逻辑发送Action ");
					}
				}
//				System.out.print("-");
			}
			
			/**
			 * 发送相应的Action
			 * @return
			 */
			private boolean sendAction(){
				if(managerConnection.getState()==ManagerConnectionState.CONNECTED){
					try {
						managerConnection.sendAction(action,null);
						return true;
					} catch (Exception e) {
						logger.info("chb: "+this.getName()+" ActionThread send "+action.getClass().getCanonicalName()+" failed !"+e.getMessage());
						return false;
					}
				}
				return false;
			}
		}.start();
	}
}
