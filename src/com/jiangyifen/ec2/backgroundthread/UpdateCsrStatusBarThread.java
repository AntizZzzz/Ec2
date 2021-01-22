package com.jiangyifen.ec2.backgroundthread;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.CdrService;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：更新坐席的界面状态栏信息，要刷新的数据有：
 * 		坐席：【上次登陆时间，上次登陆时长,今日累计登陆时长,今日累计置忙时长,呼叫总数，接通总数，总通时，平均应答速度】
 * 
 * @author  jrh
 * @date    2013年11月13日 上午8:34:30
 * @version v1.0.0
 */
public class UpdateCsrStatusBarThread extends Thread {

	private final Logger logger = LoggerFactory.getLogger(UpdateCsrStatusBarThread.class);
			
	private final long THREAD_SLEEP = 1000 * 60 * 2;
	
	private final SimpleDateFormat SDF_BY_DAY = new SimpleDateFormat("yyyy-MM-dd 00:00.00");
	private final SimpleDateFormat SDF_BY_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss");
	private final DecimalFormat DF_ONE_PRECISION = new DecimalFormat("0.0");
	
	private CdrService cdrService;
	
	public UpdateCsrStatusBarThread() {
		this.setDaemon(true);
		this.setName("UpdateCsrStatusBarThread");
		this.start();
	}

	@Override
	public void run() {
		try {
			while (true) {
 				try {
// 					TODO
// 					long t1 = System.currentTimeMillis();
					updateCsrStatusBar();
//					long t2 = System.currentTimeMillis();
//					System.out.println("本次更新坐席界面信息耗时："+(t2-t1)+"毫秒");
				} catch (Exception e) {
					logger.error("jrh 后台更新坐席状态栏信息失败_UpdateCsrStatusBarThread_从数据库获取统计数据时出错", e);
				}
				Thread.sleep(THREAD_SLEEP);
			}
		} catch (Exception e) {
			 e.printStackTrace();
			 logger.error("jrh 后台更新坐席状态栏信息失败_UpdateCsrStatusBarThread", e);
		}
	}

	/**
	 * 更新坐席的界面状态栏信息，要刷新的数据有：
	 * 	【上次登陆时间，上次登陆时长,今日累计登陆时长,今日累计置忙时长,呼叫总数，接通总数，总通时，平均应答速度】
	 */
	private void updateCsrStatusBar() throws Exception {
		if(cdrService == null) {
			cdrService = SpringContextHolder.getBean("cdrService");
		}
		
		HashSet<Long> csrIds = new HashSet<Long>(ShareData.csrToStatusBar.keySet());			// 当前在线坐席的编号
		if(csrIds.size() == 0) {	// 如果当前没有在线用户，则直接返回
			return;
		}
		String csrIdsSql = StringUtils.join(csrIds, ",");
		if(csrIdsSql == null || "".equals(csrIdsSql)) {			// 如果这个值为空值或者为空字符串，则将结果设置为 0
			csrIdsSql = "0";
		}
		List<Object[]> csrIdAndNameArr = cdrService.getInfosByNativeSql("select id,username from ec2_user where id in("+csrIdsSql+") order by id asc");

		HashMap<Long, String> csrIdToNameMap = new HashMap<Long, String>();						// 当前在线坐席的编号与用户名的对应关系
		HashMap<String, Object[]> csrStatisticInfosMap = new HashMap<String, Object[]>();		// 用于存储各个坐席的统计信息
		// 针对一个用户【username】要统计的信息有8个 ： nearestLoginTime, nearestLoginDuration, currentLoginDuration, currentPauseDuration, callAmount, birdgedAmount, totalCallTime, avgAnwserSpeed

		for(Object[] obj : csrIdAndNameArr) {
			if(obj.length < 2 || obj[0] == null || obj[1] == null) {	// 一般不行
				continue;
			}
			String username = obj[1].toString();
			csrIdToNameMap.put((Long)obj[0], username);
			
			Object[] statisticInfoArr = new Object[8];
			csrStatisticInfosMap.put(username, statisticInfoArr);
		}

		// 当天时间的零点如 2013-10-10 00:00:00
		String todayStartTime = SDF_BY_DAY.format(new Date());
		String usernameSql = "'"+StringUtils.join(csrIdToNameMap.values(), "','")+"'";
		
		// ==================================================  统计：   上次登陆时间，上次登陆时长： ================================================== //
		List<Object[]> csrInfoArrList1 = cdrService.getInfosByNativeSql("select username, logindate, sum(logoutdate - logindate) as nearestLoginTime from ec2_user_login_record right join"
				+ " (select max(id) as id from ec2_user_login_record where logindate is not null and logoutdate is not null and username in ("+usernameSql+") group by username) as t2 using (id) group by username,logindate");

		for(Object[] infoArr1 : csrInfoArrList1) {
			String username = (String) infoArr1[0];
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			if(statisticInfoArr != null) {
				Date loginDate = (Date)infoArr1[1];
				statisticInfoArr[0] = SDF_BY_SEC.format(loginDate);
				statisticInfoArr[1] = buildDateStr(buildDurationParams(infoArr1[2]+""));
			}
		}

		// ==================================================  统计：   今日累计登陆时长： ================================================== //
		HashMap<String, Integer[]> onlineTimeMap = new HashMap<String, Integer[]>();

		// 第一步：统计在线时长分两步走：第一步统计那些在当前查询时间范围内，登陆时间和退出时间都不为空的记录的时间差值
		List<Object[]> csrInfoArrList2 = cdrService.getInfosByNativeSql("select username, sum(logoutdate - logindate) as oldOnlineTime from ec2_user_login_record where logoutdate is not null"
				+ " and logindate >= '"+todayStartTime+"' and username in ("+usernameSql+") group by username");
		
		for(Object[] infoArr2 : csrInfoArrList2) {
			String username = (String) infoArr2[0];
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			if(statisticInfoArr != null) {
				Integer[] onlineTimeInfoArr = buildDurationParams(infoArr2[1]+"");
				onlineTimeMap.put(username, onlineTimeInfoArr);
			}
		}
		
		// 第二步：如果当前查询的结束时间比当前时间点大，则需要把坐席当前的在线时间也算在内，因为坐席此时没有下线，所以会存在一条只有登陆时间，没有退出时间的记录
		List<Object[]> csrInfoArrList3 = cdrService.getInfosByNativeSql("select username, (now() - max(logindate)) as currentOnlineTime from ec2_user_login_record where logoutdate is null"
				+ " and logindate >= '"+todayStartTime+"' and username in ("+usernameSql+") group by username");
		
		for(Object[] infoArr3 : csrInfoArrList3) {
			String username = (String) infoArr3[0];
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			if(statisticInfoArr != null) {
				Integer[] onlineTimeInfoArr = onlineTimeMap.get(username);
				Integer[] onlineTimeInfoArr2 = buildDurationParams(infoArr3[1]+"");
				if(onlineTimeInfoArr == null) {	// 如果不存在，则将当前得到的数组直接赋值过去
					onlineTimeInfoArr = onlineTimeInfoArr2;
				} else {	// 如果已经存在，则需要将两个值相加后在处理
					for(int i = 0; i < 4; i++) {
						onlineTimeInfoArr[i] += onlineTimeInfoArr2[i];
					}
				}
				onlineTimeMap.put(username, onlineTimeInfoArr);
			}
		}
		
		for(String username : csrStatisticInfosMap.keySet()) {				// 将在线用户的在线时长，编辑成 “10天 10:10:12” 的形式
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			Integer[] onlineTimeArr = onlineTimeMap.get(username);
			if(onlineTimeArr == null) {
				statisticInfoArr[2] = "00:00:00";
			} else {
				statisticInfoArr[2] = buildDateStr(onlineTimeArr);
			}
		}
		
		// ==================================================  统计：   今日累计置忙时长： ================================================== //
		HashMap<String, Integer[]> pauseTimeMap = new HashMap<String, Integer[]>();
		
		
		// 第一步：首先处理有登陆置忙置闲时间的，由于一个坐席可能同时属于多个队列，但对于界面，只能显示一个置忙时间，所以只统计用户属于的所有队列中置忙时间最长的那一个
		List<Object[]> csrInfoArrList4 = cdrService.getInfosByNativeSql("select t1.username, max(t1.pauseTime) as maxPauseTime from (select username,queue, (sum(unpausedate - pausedate)) as pauseTime from ec2_queue_member_pause_event_log"
				+ " where unpausedate is not null and pausedate >= '"+todayStartTime+"' and username in ("+usernameSql+") and reason not in('"+GlobalVariable.AFTER_CALL_IN_PAUSE_EXTEN_REASON+"', '"+GlobalVariable.AFTER_CALL_OUT_PAUSE_EXTEN_REASON+"', '"+GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON+"') group by username,queue) as t1 group by t1.username");

		for(Object[] infoArr4 : csrInfoArrList4) {
			String username = (String) infoArr4[0];
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			if(statisticInfoArr != null) {
				Integer[] pauseTimeInfoArr = buildDurationParams(infoArr4[1]+"");
				pauseTimeMap.put(username, pauseTimeInfoArr);
			}
		}
		
		// 第二步：然后统计每个坐席在各个队列中，那些正处于置忙状态的时长，并且只取其中时间最大的那个
		List<Object[]> csrInfoArrList5 = cdrService.getInfosByNativeSql("select t1.username, max(t1.currentPauseTime) as maxPauseTime from (select username, queue, (now() - max(pausedate)) as currentPauseTime from ec2_queue_member_pause_event_log"
				+ " where unpausedate is null and pausedate >= '"+todayStartTime+"' and username in ("+usernameSql+") and reason not in('"+GlobalVariable.AFTER_CALL_IN_PAUSE_EXTEN_REASON+"', '"+GlobalVariable.AFTER_CALL_OUT_PAUSE_EXTEN_REASON+"', '"+GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON+"') group by username, queue) as t1 group by t1.username");

		for(Object[] infoArr5 : csrInfoArrList5) {
			String username = (String) infoArr5[0];
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			if(statisticInfoArr != null) {
				Integer[] pauseTimeInfoArr = pauseTimeMap.get(username);
				Integer[] pauseTimeInfoArr2 = buildDurationParams(infoArr5[1]+"");
				if(pauseTimeInfoArr == null) {	// 如果不存在，则将当前得到的数组直接赋值过去
					pauseTimeInfoArr = pauseTimeInfoArr2;
				} else {	// 如果已经存在，则需要将两个值相加后在处理
					for(int i = 0; i < 4; i++) {
						pauseTimeInfoArr[i] += pauseTimeInfoArr2[i];
					}
				}
				
				pauseTimeMap.put(username, pauseTimeInfoArr);
			}
		}

		for(String username : csrStatisticInfosMap.keySet()) {				// 将在线用户的置忙时长，编辑成 “10天 10:10:12” 的形式
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			Integer[] pauseTimeArr = pauseTimeMap.get(username);
			if(pauseTimeArr == null) {
				statisticInfoArr[3] = "00:00:00";
			} else {
				statisticInfoArr[3] = buildDateStr(pauseTimeArr);
			}
		}

		// ==================================================  统计：   话务信息[呼叫总数，接通总数，总通时，平均应答速度]： ================================================== //
		HashMap<String, Long[]> callStatisticMap = new HashMap<String, Long[]>();
		
		// 第一步：根据坐席为 主叫 时的话务信息 【平均应答速度是根据总的应答时间跟总的接通数的比值来决定的】
		List<Object[]> csrInfoArrList6 = cdrService.getInfosByNativeSql("select srcusername, count(*) as calloutAmount, count(case when isbridged is true then 't' else null end) as bridgedoutAmount,"
				+ " sum(billableseconds) as totaloutCallTime, sum(case when isbridged is true then ringduration else 0 end) as totaloutAnswerTime from cdr"
				+ " where starttimedate >= '"+todayStartTime+"' and srcusername in ("+usernameSql+") group by srcusername");
		
		for(Object[] infoArr6 : csrInfoArrList6) {
			String username = (String) infoArr6[0];
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			if(statisticInfoArr != null) {
				Long[] outCallInfoArr = new Long[]{0L, 0L, 0L, 0L};
				for(int i = 0; i < 4; i++) {
					outCallInfoArr[i] = (Long) infoArr6[i+1];
				}
				callStatisticMap.put(username, outCallInfoArr);
			}
		}
		
		// 第二步：根据坐席为 被叫 时的话务信息 【平均应答速度是根据总的应答时间跟总的接通数的比值来决定的】
		List<Object[]> csrInfoArrList7 = cdrService.getInfosByNativeSql("select destusername, count(*) as callinAmount, count(case when isbridged is true then 't' else null end) as bridgedinAmount,"
				+ " sum(billableseconds) as totalinCallTime,sum(case when isbridged is true then ringduration else 0 end) as totalinAnswerTime from cdr"
				+ " where starttimedate >= '"+todayStartTime+"' and destusername in ("+usernameSql+") group by destusername");

		for(Object[] infoArr7 : csrInfoArrList7) {
			String username = (String) infoArr7[0];
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			if(statisticInfoArr != null) {
				Long[] callInfoArr = callStatisticMap.get(username);
				Long[] inCallInfoArr = new Long[]{0L, 0L, 0L, 0L};
				for(int i = 0; i < 4; i++) {
					inCallInfoArr[i] = (Long) infoArr7[i+1];
				}
				
				if(callInfoArr == null) {	// 如果不存在，则将当前得到的数组直接赋值过去
					callInfoArr = inCallInfoArr;
				} else {	// 如果已经存在，则需要将两个值相加后在处理
					for(int i = 0; i < 4; i++) {
						callInfoArr[i] += inCallInfoArr[i];
					}
				}
				callStatisticMap.put(username, callInfoArr);
			}
		}

		for(String username : csrStatisticInfosMap.keySet()) {				// 将在线用户的在线时长，编辑成 “10天 10:10:12” 的形式
			Object[] statisticInfoArr = csrStatisticInfosMap.get(username);
			Long[] callStatisticArr = callStatisticMap.get(username);
			if(callStatisticArr == null) {
				statisticInfoArr[4] = 0L;
				statisticInfoArr[5] = 0L;
				statisticInfoArr[6] = 0L;
				statisticInfoArr[7] = 0.00d;
			} else {
				statisticInfoArr[4] = callStatisticArr[0];
				statisticInfoArr[5] = callStatisticArr[1];
				statisticInfoArr[6] = callStatisticArr[2];
				double avgAnswerTime = (callStatisticArr[1] > 0) ? ((callStatisticArr[3] + 0.0) / callStatisticArr[1]) : 0.0000;
				statisticInfoArr[7] = DF_ONE_PRECISION.format(avgAnswerTime);
			}
		}
		
		// 最后：刷新统计信息到坐席界面
		for(Long csrId : csrIdToNameMap.keySet()) {
			CsrStatusBar csrStatusBar = ShareData.csrToStatusBar.get(csrId);
			String username = csrIdToNameMap.get(csrId);
			Object[] statisticInfos = csrStatisticInfosMap.get(username);
			if(csrStatusBar != null && statisticInfos != null) {	// 一般情况下，这两个都不会为空
				csrStatusBar.updateStatisticInfos(statisticInfos);	// 该方法做了异常处理，如果刷新某个人的界面出现异常，则不会影响到其他人
			}
		}
		
	}

	/**
	 * 解析由NativeSql 查出来的时间差，获取其中的天、小时、分钟、秒，对应的值
	 * @param dateInfoStr
	 * @return
	 */
	private Integer[] buildDurationParams(String dateInfoStr) {
		Integer[] params = new Integer[]{0,0,0,0};
		String[] dts = dateInfoStr.split(" ");
		if(dts != null && dts.length > 10) {
			dts[10] = dts[10].substring(0, dts[10].indexOf("."));
			params[0] = Integer.parseInt(dts[4]);
			params[1] = Integer.parseInt(dts[6]);
			params[2] = Integer.parseInt(dts[8]);
			params[3] = Integer.parseInt(dts[10]);
		}
		return params;
	}
	
	/**
	 * 根据天、小时、分钟、秒，对应的值，组装成 7天 10:10:00 这样的字符串
	 * @param dateParams
	 * @return
	 */
	private String buildDateStr(Integer[] dateParams) {
		int day = dateParams[0];
		int hour = dateParams[1];
		int minute = dateParams[2];
		int second = dateParams[3];
		
		int addMin = second / 60;				// 如果秒数大于60，则算出多余的分钟数
		int addHour = (minute + addMin) / 60;	// 如果分钟大于60，则算出多余的小时数
		int addDay = (hour + addHour) / 24;		// 如果小时大于24，则算出多余的天数
		second = second % 60;
		minute = (minute + addMin) % 60;
		hour = (hour + addHour) % 24;
		day = day + addDay;
		
		String onlineTimeStr = "";
		if(day > 0) {
			onlineTimeStr = day+"天 ";
		} 
		if(hour >= 0 && hour < 10) {
			onlineTimeStr += "0"+hour+":";
		} else {
			onlineTimeStr += hour+":";
		}
		if(minute >= 0 && minute < 10) {
			onlineTimeStr += "0"+minute+":";
		} else {
			onlineTimeStr += minute+":";
		}
		if(second >= 0 && second < 10) {
			onlineTimeStr += "0"+second;
		} else {
			onlineTimeStr += second;
		}
		
		return onlineTimeStr;
	}
	
}
