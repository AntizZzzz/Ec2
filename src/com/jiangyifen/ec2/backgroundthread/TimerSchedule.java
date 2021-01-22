package com.jiangyifen.ec2.backgroundthread;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.TimersService;
import com.jiangyifen.ec2.ui.csr.toolbar.CsrToolBar;

/**
 * 该类是定时器 任务调度 
 * 当前方案：方案一
 * 总体思路：
 * 		1、tomcat 启动时，获取响应时间大于当前时间的所有定时器中响应时间最小的定时器 Timers 对象
 * 		2、创建schedule 任务，根据得到的定时器，获取其响应时间，作为 schedule 的延时参数(第二个参数)
 * 		3、在任务响应时，调用run() 方法，首先根据第一步得到的Timers 对象，将其对应的拥有者的界面进行更新(弹出提醒窗口)
 * 		4、重新从数据库中获取响应时间大于当前时间的所有定时器中响应时间最小的定时器 Timers 对象
 * 		5、重复 1-4 步
 * 
 * 需要考虑的环节：如果CSR 用户新增或更新定时器对象，那么就需要判断其响应时间是否大于schedule的响应时间
 * 			如果不大于，则新建 schedule 任务，并且将之前的任务取消
 * 可改进：
 * 			如果CSR 删除一条定时器，同样需要判断其响应时间是否等于schedule的响应时间，如果等于，则需要将其从timersMap 中移除
 * 
 * 当前解决方案：
 * 		TimerSchedule 类 与 TimersService 接口 使用双向依赖注入，使用Spring 注入，彼此都能得到对方的 Bean
 * 		<bean tiemrSchedule > 中加   p:timersService-ref="timersService"
 * 		<bean timersService > 中加   p:timerSchedule-ref="timerSchedule"
 * 		在CSR 用户新增或者更新定时器对象时，在UI 界面首先调用 TimersService.save(timers) 或 TimersService.update(timers) 方法
 * 		然后调用 TimersService.refreshSchedule() 方法
 * 
 * 此方案的缺点：使用了互相注入，使得程序的逻辑性相对较乱
 * 
 * 另一种解决方案：
 * 		只需 TimerSchedule 类注入 到 TimerSchedule 类中，不使用双向注入，并且使用构造注入法 ，如 OldTimerSchedule 所示 。	
 * 		<bean tiemrSchedule > 中加  <constructor-arg ref="timersService"/>
 * 		在CSR 用户新增或者更新定时器对象时，
 * 		在UI 界面首先调用 TimersService.getNearestTimeToCurrentTime() 获取 schedule 即将响应的时间 
 * 			然后再调用TimersService.save(timers) 或 TimersService.update(timers) 方法
 * 		最后调用 TimersService.refreshSchedule() 方法重建 schedule
 * 
 * 方案三：
 * 		直接在后台起一个线程，让其每个一段时间到数据库中进行查询，当时间到直接在线程中更新用户界面
 * 	 此方案的缺点，随着时间的推移，数据库中的闹铃提醒表变得越来越大，每隔一段时间都进行对数据库的查询操作在性能上有所降低
 * 	    另外，如果数据库中有大量或者只有少量的闹铃提醒信息，而这些信息需要提醒的时间都比当前时间小，此时仍然始终对数据库进行查询是在做无用功
 * 
 * jrh
 */
public class TimerSchedule extends TimerTask {
	private Timer timer;					// 定时器工具	
	private Date responseTime;				// 定时器响应时间
	private Map<Long, Timers> timersMap;	// 定时器提醒对象与用户的对应集合
	private TimersService timersService;	// 定时器提醒服务类
	
	public TimerSchedule() {
		
	}
	
	// 调度的任务
	public TimerSchedule(Timer timer, Map<Long, 
			Timers> timersMap, TimersService timersService) {
		this.timer = timer;
		this.timersMap = timersMap;
		this.timersService = timersService;
	}
	
	@Override 
	public void run() { 
		Set<Long> userIdSet = new HashSet<Long>();
		userIdSet.addAll(timersMap.keySet());
		for(Long userId : userIdSet) {
			Timers timers = timersMap.get(userId);
			CsrToolBar csrToolBar = ShareData.csrToToolBar.get(userId); 	// 获取登录用户的系统组件
			if(csrToolBar != null) {	// 根据组件是否为空来判断用户是否已经登录
				csrToolBar.updateTimersResponse(timers.getId());
// TODO delete
//				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//				System.out.println(timers.getCreator().getId() + "****" +timers.getId() + " ---- " + timers.getTitle() + "  ---  " + df.format(timers.getResponseTime()));
			}
			timersMap.remove(userId);
		}
		
		createSchedule();
	}
	
	/**
	 * 创建定时器响应任务
	 */
	private void createSchedule() {
		List<Timers> all = timersService.getTiemrsByCurrentTime();
		for(Timers timers : all) {
			timersMap.put(timers.getCreator().getId(), timers);
			responseTime = timers.getResponseTime();
//	TODO delete 
//			System.out.println("\n\n responseTime -- \t"+responseTime+"\n\n");
		}
		
//		System.out.println("all.size()---"+all.size());
		
		if(responseTime != null) { // 如果存在为响应的定时器提醒，则创建schedule
			timer.schedule(new TimerSchedule(timer, timersMap, timersService), responseTime);
		}
	}
	
	/**
	 * 当有用户新增或更新一个定时器，并且该定时器的响应时间比当前的 schedule 的响应时间早
	 *  那么就将之前的 schedule 任务取消，并清空定时器Map 集合中的值
	 *  然后获取最近将要发生的定时器，并创建 schedule
	 */
	public void refreshSchedule() {
		timer.cancel();
		timer = new Timer();
		Set<Long> userIdSet = new HashSet<Long>();
		userIdSet.addAll(timersMap.keySet());
		for(Long userId : userIdSet) {
			timersMap.remove(userId);
		}
		createSchedule();
//TODO delete
//		System.out.println("timersMap.keySet()------"+timersMap.keySet());
//		System.out.println( "\n\n" + responseTime + " ---refresh---\n\n");
	}

	public TimersService getTimersService() {
		return timersService;
	}

	public void setTimersService(TimersService timersService) {
		this.timersService = timersService;
		timersMap = new HashMap<Long, Timers>();
		timer = new Timer(); 
		// tomcat 启动时，获取响应时间大于当前时间的所有定时器中响应时间最小的定时器
		createSchedule();
	}

}