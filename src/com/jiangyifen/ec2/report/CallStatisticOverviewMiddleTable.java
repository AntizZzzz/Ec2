package com.jiangyifen.ec2.report;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.CdrDirection;
import com.jiangyifen.ec2.report.entity.CallStatisticOverview;
import com.jiangyifen.ec2.service.eaoservice.CallStatisticOverviewService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 检测并往【 呼叫统计概览中间表】插入的数据 
 *
 *	【统计方式3：只要domain中产生过Cdr,哪怕只有一条，哪怕之后再也没有用过该呼叫中心，
 *		只要我们呼叫中心在运作，也按24小时制每天添加，但中间表中的最大统计时间就是当前时间点】
 *
 * @author jrh
 *  2013-9-4
 */
public class CallStatisticOverviewMiddleTable extends AbstractReport {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat SDF_YMDH = new SimpleDateFormat("yyyy-MM-dd HH");
	private final SimpleDateFormat SDF_ALL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private final DecimalFormat DF_TWO_PRECISION = new DecimalFormat("0.00");
	private final DecimalFormat DF_FOUR_PRECISION = new DecimalFormat("0.0000");
	
	private final CallStatisticOverviewService callStatisticOverviewService = SpringContextHolder.getBean("callStatisticOverviewService");

	private String startSearchDateStr;			// 开始查询的日期的字符串形式
	private String nextStepSearchDateStr;		// 下一步的起始查询时间的字符串形式
	private Date maxCallStatisticStartDate;		// 报表后台线程上次执行时，保存到‘呼叫统计概览中间表’的最大日期和小时
	private String dynamicScopeSql;				// 起始查询时间用 '>=' 还是用 ‘>’,如果是第一次创建中间表，则用‘>=’,否则用'>'
	private int specifiedSec = 25;				// 查询在指定秒数内接起的通话数或者放弃数量
	private Map<Long,HashMap<Date,CallStatisticOverview>> statistcDomainMap;
	private Map<Long, ArrayList<Date>> startDateDomainMap;		// 各租户下， 针对cdr的记录， 已经创建了统计中间表的时段有哪些【用来补全一些时段下，没有呼叫记录的统计信息】

	private long tc = 1;
	
	@Override
	public void execute() {
		try {
			long count = 0;
			Long st = System.currentTimeMillis();
			logger.info("第"+tc+"次执行----起始时间："+SDF_ALL.format(new Date()));
			
			statistcDomainMap = new HashMap<Long, HashMap<Date,CallStatisticOverview>>();
			startDateDomainMap = new HashMap<Long, ArrayList<Date>>();
			
			// 开始查询的日期
			Date stopSearchDate = new Date();
			Long maxCdrId = (Long) this.getSingleRecord("select max(id) from cdr;");
			if(maxCdrId == null || maxCdrId < 0 ) {
				return;
			}

			/***********************************开始************************************************/
//			// 终止查询的日期，cdr 中的最大时间  [这段代码的作用是：如果租户不再使用我们的系统打电话，我就不再为其统计中间表，也就不再为其在中间表中插入一些空数据]
//			Date stopSearchDate = (Date) this.getSingleRecord("select max(endtimedate) from cdr;");
//			if(stopSearchDate == null) {
//				return;
//			}
			/***********************************结束************************************************/
			
			// 终止查询的日期，cdr 中的最大时间
			Date startSearchDate = (Date) this.getSingleRecord("select max(enddate) from ec2_report_call_statistic_overview");
			if(startSearchDate == null) {
				dynamicScopeSql = ">=";
				startSearchDate = (Date) this.getSingleRecord("select min(endtimedate) from cdr;");
			} else {
				dynamicScopeSql = ">";
			}
			maxCallStatisticStartDate = startSearchDate;
			
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(startSearchDate);
			calendar1.set(Calendar.MINUTE, 0);
			calendar1.set(Calendar.SECOND, 0);
			calendar1.set(Calendar.MILLISECOND, 0);
			
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(stopSearchDate);
			calendar2.set(Calendar.MINUTE, 0);
			calendar2.set(Calendar.SECOND, 0);
			calendar2.set(Calendar.MILLISECOND, 0);
			Date stopDate = calendar2.getTime();
			
			ArrayList<Date> startDateArray = new ArrayList<Date>();
			calendar1.add(Calendar.HOUR_OF_DAY, +1);
			while(!calendar1.getTime().after(stopDate)) {
				startDateArray.add(calendar1.getTime());
				calendar1.add(Calendar.HOUR_OF_DAY, +1);
			}
			
			// 检查当前有多少个域已经被人客户租赁走了
//			List<Object[]> cdrDomainIds = this.getRecord("select distinct(domainid) from cdr");		// JRH 当数据量达到千万级时，这个要耗费10秒以上的时间
			List<Object[]> cdrDomainIds = this.getRecord("select id from ec2_domain order by id");	// JRH 这样只是会给哪些还没启用的域也创建报表，但数量很少，一个域一天就只有24条
			for(Object obj : cdrDomainIds) {
				Long domainid = (Long) obj;
				if(domainid.equals(0L)) {
					continue;
				}
				startDateDomainMap.put(domainid, startDateArray);
			}
			
			while(stopSearchDate.after(startSearchDate)) {
				count++;
				// 初始化单次查询的起始时间和截止时间的字符串参数
				startSearchDateStr = SDF_ALL.format(startSearchDate);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(startSearchDate);
				calendar.add(Calendar.HOUR_OF_DAY, +1);
				nextStepSearchDateStr = SDF_ALL.format(calendar.getTime());

				Date endDate = (Date) this.getSingleRecord("select max(endtimedate) as endDate from cdr where endtimedate >='" 
						+ startSearchDateStr + "' and endtimedate < '" + nextStepSearchDateStr + "'");
				
//				System.out.println("\t startSearchDateStr----------"+startSearchDateStr);
//				System.out.println("\t nextStepSearchDateStr-------"+nextStepSearchDateStr);
				
				// 只查呼入的数据，按 域和时段 进行划分统计：呼入总数、呼入接通数、呼入总通时、呼入平均通时
				this.searchIncomingCdrInfoList1(endDate);

				// 只查呼出的数据，按 域和时段 进行划分统计：呼出总数、呼出接通数、呼出总通时、呼出平均通时
				this.searchOutgoingCdrInfoList1(endDate);

				// 只查呼入的数据，按 域和时段 进行划分统计：在指定时间内总接起数，呼入坐席平均应答速度，在指定时间内接通数
				this.searchIncomingCdrInfoList2(endDate);

				// 只查呼出的数据，按 域和时段 进行划分统计：在指定时间内总接起数，外呼客户平均应答速度，在指定时间内客户接通数
				this.searchOutgoingCdrInfoList2(endDate);

				// 只查呼入的数据，按 域和时段 进行划分统计：在指定时间内总的放弃数，指定时间内客户放弃数
				this.searchIncomingCdrInfoList3(endDate);

				// 只查呼出的数据，按 域和时段 进行划分统计：在指定时间内总的放弃数，指定时间内坐席放弃数
				this.searchOutgoingCdrInfoList3(endDate);
				
				// 保存新建的统计信息，或者更新老的信息
				this.saveToDb();
				
				// 一次查询完成后，重新初始化单次查询的起始时间和截止时间的字符串参数
				calendar.setTime(startSearchDate);
				calendar.add(Calendar.HOUR_OF_DAY, +1);
				startSearchDate = calendar.getTime();
				startSearchDateStr = nextStepSearchDateStr;
				// 往后推一个小时进行查询
				calendar.add(Calendar.HOUR_OF_DAY, +1);
				nextStepSearchDateStr = SDF_ALL.format(calendar.getTime());
			}

			// 统计各个域中存储的在话务总概览中间表里的最大保存时间点
			List<Object[]> maxStartDateDomainList = this.getRecord("select max(enddate),domainid from ec2_report_call_statistic_overview where domainid != 0 group by domainid");
			HashMap<Long, Date> maxStartDateDomainMap = new HashMap<Long, Date>();
			for(Object[] objs : maxStartDateDomainList) {
				maxStartDateDomainMap.put((Long) objs[1], (Date) objs[0]);
			}
			
			// 如果当前时间点[new Date()]比呼叫记录Cdr 最后的创建时间大，则需要根据情况判断是否需要插入新的空记录
			for(long domainId : startDateDomainMap.keySet()) {
				ArrayList<Date> tmpStartDateArray = startDateDomainMap.get(domainId);
				Date maxDomainDate = maxStartDateDomainMap.get(domainId);
				if(maxDomainDate == null) {
					continue;
				}
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(maxDomainDate);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				maxDomainDate = calendar.getTime();

				if(tmpStartDateArray.size() > 0) {
					for (Date dhDate : tmpStartDateArray) {
						// 如果当前域的在话务总概览表中的最大保存时间比tmpStartDateArray中的最小时间还小，则创建一条空记录，否则直接删除tmpStartDateArray 中的最小时间，不做其他操作
						if(maxDomainDate.before(dhDate)) {
							CallStatisticOverview emptyCsoInfo = new CallStatisticOverview();
							Calendar cal = Calendar.getInstance();
							cal.setTime(dhDate);
							int hour = cal.get(Calendar.HOUR_OF_DAY);
							cal.set(Calendar.HOUR_OF_DAY, 0);
							
							emptyCsoInfo.setStartDate(cal.getTime());
							emptyCsoInfo.setHour(hour);
							emptyCsoInfo.setEndDate(dhDate);
							emptyCsoInfo.setDomainId(domainId);
							callStatisticOverviewService.save(emptyCsoInfo);
						}
					}
				}
			}
			
			Long sp = System.currentTimeMillis();
			logger.info("第"+tc+"次执行----截止时间："+SDF_ALL.format(new Date()));
			logger.info("总耗时： "+((sp - st) / 1000) +" 秒");
			logger.info("执行总小时数："+count);
			tc++;
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 检测并往【 呼叫统计概览中间表】插入的数据 时出现异常"+e.getMessage(), e);
		}
	}

	/**
	 * 只查呼入的数据，按 域和时段 进行划分统计：呼入总数、呼入接通数、呼入总通时、呼入平均通时
	 * @throws Exception
	 */
	private void searchIncomingCdrInfoList1(Date endDate) throws Exception {
		String nativeSql = "select to_char(starttimedate, 'yyyy-MM-dd HH24') as startDate,count(*) as callInAmount," +
				" sum(case when isbridged is true then 1 else 0 end) as bridgedInAmount,sum(billableseconds) as totalInCallTime,domainid " +
				" from cdr where cdrdirection = "+CdrDirection.o2e.getIndex()+" and endtimedate "+dynamicScopeSql+"'" + startSearchDateStr + "' and endtimedate < '" + nextStepSearchDateStr + "' " +
				" group by to_char(starttimedate, 'yyyy-MM-dd HH24'),domainid order by to_char(starttimedate, 'yyyy-MM-dd HH24')";
		List<Object[]> incomingCdrInfoList1 = this.getRecord(nativeSql);

		printSqlInfo(true, nativeSql);
		
		for(Object[] objs : incomingCdrInfoList1) {
			// 检查指定时段的话务统计信息是否存在，如果不存在则新建
			String startDateHour = (String) objs[0];
			Date dhDate = SDF_YMDH.parse(startDateHour);
			Long domainId = (Long) objs[4];
			if(domainId.equals(0L)) {
				continue;
			}
			
			HashMap<Date, CallStatisticOverview> statisticMap = statistcDomainMap.get(domainId);
			if(statisticMap == null) {
				statisticMap = new HashMap<Date, CallStatisticOverview>();
				statistcDomainMap.put(domainId, statisticMap);
			}
			
			CallStatisticOverview callStatisticOverview = statisticMap.get(dhDate);
			if(callStatisticOverview == null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dhDate);
				Integer hour = calendar.get(Calendar.HOUR_OF_DAY);

				calendar.set(Calendar.HOUR_OF_DAY, 0);
				Date startDate = calendar.getTime();
				
				callStatisticOverview = new CallStatisticOverview();
				callStatisticOverview.setStartDate(startDate);
				callStatisticOverview.setEndDate(endDate);
				callStatisticOverview.setHour(hour);
				callStatisticOverview.setDomainId(domainId);
				statisticMap.put(dhDate, callStatisticOverview);
			}
			
			// 更新总体信息
			Long callAmount = callStatisticOverview.getCallAmount() + ((Long) objs[1]);
			Long bridgedAmount = callStatisticOverview.getBridgedAmount() + ((Long) objs[2]);
			Long totalCallTime = callStatisticOverview.getTotalCallTime() + ((Long) objs[3]);
			Double avgCallTime = (callAmount > 0L) ? ((totalCallTime+0.0) / callAmount) : 0.0;
			Double bridgedRate = (callAmount > 0L) ? ((bridgedAmount+0.0) / callAmount) : 0.0;
			callStatisticOverview.setCallAmount(callAmount);
			callStatisticOverview.setBridgedAmount(bridgedAmount);
			callStatisticOverview.setTotalCallTime(totalCallTime);
			callStatisticOverview.setAvgCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgCallTime)));
			callStatisticOverview.setBridgedRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedRate)));
			
			// 更新呼入信息
			Long callInAmount = callStatisticOverview.getCallInAmount() + ((Long) objs[1]);
			Long bridgedInAmount = callStatisticOverview.getBridgedInAmount() + ((Long) objs[2]);
			Long totalInCallTime = callStatisticOverview.getTotalInCallTime() + ((Long) objs[3]);
			Double avgInCallTime = (callInAmount > 0L) ? ((totalInCallTime+0.0) / callInAmount) : 0.0;
			Double bridgedInRate = (callInAmount > 0L) ? ((bridgedInAmount+0.0) / callInAmount) : 0.0;
			callStatisticOverview.setCallInAmount(callInAmount);
			callStatisticOverview.setBridgedInAmount(bridgedInAmount);
			callStatisticOverview.setTotalInCallTime(totalInCallTime);
			callStatisticOverview.setAvgInCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgInCallTime)));
			callStatisticOverview.setBridgedInRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedInRate)));
		}
	}

	/**
	 * 只查呼出的数据，按 域和时段 进行划分统计：呼出总数、呼出接通数、呼出总通时、呼出平均通时
	 * @throws ParseException
	 */
	private void searchOutgoingCdrInfoList1(Date endDate) throws Exception {
		String nativeSql = "select to_char(starttimedate, 'yyyy-MM-dd HH24') as startDate,count(*) as callOutAmount," +
				" sum(case when isbridged is true then 1 else 0 end) as bridgedInAmount,sum(billableseconds) as totalOutCallTime,domainid " +
				" from cdr where cdrdirection != "+CdrDirection.o2e.getIndex()+" and endtimedate "+dynamicScopeSql+"'" + startSearchDateStr + "' and endtimedate < '" + nextStepSearchDateStr + "' " +
				" group by to_char(starttimedate, 'yyyy-MM-dd HH24'),domainid order by to_char(starttimedate, 'yyyy-MM-dd HH24')";
		List<Object[]> outgoingCdrInfoList1 = this.getRecord(nativeSql);

		printSqlInfo(true, nativeSql);
		
		for(Object[] objs : outgoingCdrInfoList1) {
			// 检查指定时段的话务统计信息是否存在，如果不存在则新建
			String startDateHour = (String) objs[0];
			Date dhDate = SDF_YMDH.parse(startDateHour);
			Long domainId = (Long) objs[4];
			if(domainId.equals(0L)) {
				continue;
			}
			
			HashMap<Date, CallStatisticOverview> statisticMap = statistcDomainMap.get(domainId);
			if(statisticMap == null) {
				statisticMap = new HashMap<Date, CallStatisticOverview>();
				statistcDomainMap.put(domainId, statisticMap);
			}
			
			CallStatisticOverview callStatisticOverview = statisticMap.get(dhDate);
			if(callStatisticOverview == null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dhDate);
				Integer hour = calendar.get(Calendar.HOUR_OF_DAY);

				calendar.set(Calendar.HOUR_OF_DAY, 0);
				Date startDate = calendar.getTime();
				
				callStatisticOverview = new CallStatisticOverview();
				callStatisticOverview.setStartDate(startDate);
				callStatisticOverview.setEndDate(endDate);
				callStatisticOverview.setHour(hour);
				callStatisticOverview.setDomainId(domainId);
				statisticMap.put(dhDate, callStatisticOverview);
			}
			
			// 更新总体信息
			Long callAmount = callStatisticOverview.getCallAmount() + ((Long) objs[1]);
			Long bridgedAmount = callStatisticOverview.getBridgedAmount() + ((Long) objs[2]);
			Long totalCallTime = callStatisticOverview.getTotalCallTime() + ((Long) objs[3]);
			Double avgCallTime = (callAmount > 0L) ? ((totalCallTime+0.0) / callAmount) : 0.0;
			Double bridgedRate = (callAmount > 0L) ? ((bridgedAmount+0.0) / callAmount) : 0.0;
			callStatisticOverview.setCallAmount(callAmount);
			callStatisticOverview.setBridgedAmount(bridgedAmount);
			callStatisticOverview.setTotalCallTime(totalCallTime);
			callStatisticOverview.setAvgCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgCallTime)));
			callStatisticOverview.setBridgedRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedRate)));
			
			// 更新呼出信息
			Long callOutAmount = callStatisticOverview.getCallOutAmount() + ((Long) objs[1]);
			Long bridgedOutAmount = callStatisticOverview.getBridgedOutAmount() + ((Long) objs[2]);
			Long totalOutCallTime = callStatisticOverview.getTotalOutCallTime() + ((Long) objs[3]);
			Double avgOutCallTime = (callOutAmount > 0L) ? ((totalOutCallTime+0.0) / callOutAmount) : 0.0;
			Double bridgedOutRate = (callOutAmount > 0L) ? ((bridgedOutAmount+0.0) / callOutAmount) : 0.0;
			callStatisticOverview.setCallOutAmount(callOutAmount);
			callStatisticOverview.setBridgedOutAmount(bridgedOutAmount);
			callStatisticOverview.setTotalOutCallTime(totalOutCallTime);
			callStatisticOverview.setAvgOutCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgOutCallTime)));
			callStatisticOverview.setBridgedOutRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedOutRate)));
		}
	}

	/**
	 * 只查呼入的数据，按 域和时段 进行划分统计：在指定时间内总接起数，应答总耗时，总的应答速度，呼入应答总耗时，呼入坐席平均应答速度，在指定时间内接通数
	 * @throws ParseException
	 */
	private void searchIncomingCdrInfoList2(Date endDate) throws Exception {
		String nativeSql = "select to_char(starttimedate, 'yyyy-MM-dd HH24') as startDate,sum(ringduration) as totalInAnswerTime," +
				" sum(case when ringduration <= "+specifiedSec+" then 1 else 0 end) as csrPickupOutSpecifiedSec,domainid " +
				" from cdr where cdrdirection = "+CdrDirection.o2e.getIndex()+" and isbridged = true and endtimedate "+dynamicScopeSql+"'" + startSearchDateStr + "' and endtimedate < '" + nextStepSearchDateStr + "' " + 
				" group by to_char(starttimedate, 'yyyy-MM-dd HH24'),domainid order by to_char(starttimedate, 'yyyy-MM-dd HH24')";
		List<Object[]> incomingCdrInfoList2 = this.getRecord(nativeSql);

		printSqlInfo(true, nativeSql);
		
		for(Object[] objs : incomingCdrInfoList2) {
			// 检查指定时段的话务统计信息是否存在，如果不存在则新建
			String startDateHour = (String) objs[0];
			Date dhDate = SDF_YMDH.parse(startDateHour);
			Long domainId = (Long) objs[3];
			if(domainId.equals(0L)) {
				continue;
			}
			
			HashMap<Date, CallStatisticOverview> statisticMap = statistcDomainMap.get(domainId);
			if(statisticMap == null) {
				statisticMap = new HashMap<Date, CallStatisticOverview>();
				statistcDomainMap.put(domainId, statisticMap);
			}
			
			CallStatisticOverview callStatisticOverview = statisticMap.get(dhDate);
			if(callStatisticOverview == null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dhDate);
				Integer hour = calendar.get(Calendar.HOUR_OF_DAY);

				calendar.set(Calendar.HOUR_OF_DAY, 0);
				Date startDate = calendar.getTime();
				
				callStatisticOverview = new CallStatisticOverview();
				callStatisticOverview.setStartDate(startDate);
				callStatisticOverview.setEndDate(endDate);
				callStatisticOverview.setHour(hour);
				callStatisticOverview.setDomainId(domainId);
				statisticMap.put(dhDate, callStatisticOverview);
			}
			
			// 更新总体信息-在指定时间内总接起数
			Long totalAnswerTime = callStatisticOverview.getTotalAnswerTime() + ((Long) objs[1]);
			Long pickupInSpecifiedSec = callStatisticOverview.getPickupInSpecifiedSec() + ((Long) objs[2]);
			Double avgAnswerTime = (callStatisticOverview.getBridgedAmount() > 0L) ? ((totalAnswerTime+0.0) / callStatisticOverview.getBridgedAmount()) : 0.0;
			callStatisticOverview.setTotalAnswerTime(totalAnswerTime);
			callStatisticOverview.setPickupInSpecifiedSec(pickupInSpecifiedSec);
			callStatisticOverview.setAvgAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgAnswerTime)));
			
			// 更新呼入信息-呼入坐席平均应答速度，在指定时间内接通数
			Long totalInAnswerTime = callStatisticOverview.getTotalInAnswerTime() + ((Long) objs[1]);
			Double avgInAnswerTime = (callStatisticOverview.getBridgedInAmount() > 0L) ? ((totalInAnswerTime+0.0) / callStatisticOverview.getBridgedInAmount()) : 0.0;
			Long csrPickupInSpecifiedSec = callStatisticOverview.getCsrPickupInSpecifiedSec() + ((Long) objs[2]);
			Double incomingServiceLevel = (callStatisticOverview.getCallInAmount() > 0) ? csrPickupInSpecifiedSec / callStatisticOverview.getCallInAmount() : 0.0;
			callStatisticOverview.setTotalInAnswerTime(totalInAnswerTime);
			callStatisticOverview.setAvgInAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgInAnswerTime)));
			callStatisticOverview.setCsrPickupInSpecifiedSec(csrPickupInSpecifiedSec);
			callStatisticOverview.setIncomingServiceLevel(Double.valueOf(DF_FOUR_PRECISION.format(incomingServiceLevel)));
		}
	}

	/**
	 * 只查呼出的数据，按 域和时段 进行划分统计：在指定时间内总接起数，应答总耗时，总的应答速度，呼出应答总耗时，外呼客户平均应答速度，在指定时间内客户接通数
	 * @throws ParseException
	 */
	private void searchOutgoingCdrInfoList2(Date endDate) throws Exception {
		String nativeSql = "select to_char(starttimedate, 'yyyy-MM-dd HH24') as startDate,sum(ringduration) as totalOutAnswerTime," +
				" sum(case when ringduration <= "+specifiedSec+" then 1 else 0 end) as customerPickupInSpecifiedSec,domainid " +
				" from cdr where cdrdirection != "+CdrDirection.o2e.getIndex()+" and isbridged = true and endtimedate "+dynamicScopeSql+"'" + startSearchDateStr + "' and endtimedate < '" + nextStepSearchDateStr + "' " + 
				" group by to_char(starttimedate, 'yyyy-MM-dd HH24'),domainid order by to_char(starttimedate, 'yyyy-MM-dd HH24')";
		List<Object[]> outgoingCdrInfoList2 = this.getRecord(nativeSql);

		printSqlInfo(true, nativeSql);
		
		for(Object[] objs : outgoingCdrInfoList2) {
			// 检查指定时段的话务统计信息是否存在，如果不存在则新建
			String startDateHour = (String) objs[0];
			Date dhDate = SDF_YMDH.parse(startDateHour);
			Long domainId = (Long) objs[3];
			if(domainId.equals(0L)) {
				continue;
			}
			
			HashMap<Date, CallStatisticOverview> statisticMap = statistcDomainMap.get(domainId);
			if(statisticMap == null) {
				statisticMap = new HashMap<Date, CallStatisticOverview>();
				statistcDomainMap.put(domainId, statisticMap);
			}
			
			CallStatisticOverview callStatisticOverview = statisticMap.get(dhDate);
			if(callStatisticOverview == null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dhDate);
				Integer hour = calendar.get(Calendar.HOUR_OF_DAY);

				calendar.set(Calendar.HOUR_OF_DAY, 0);
				Date startDate = calendar.getTime();
				
				callStatisticOverview = new CallStatisticOverview();
				callStatisticOverview.setStartDate(startDate);
				callStatisticOverview.setEndDate(endDate);
				callStatisticOverview.setHour(hour);
				callStatisticOverview.setDomainId(domainId);
				statisticMap.put(dhDate, callStatisticOverview);
			}
			
			// 更新总体信息-在指定时间内接起数
			Long totalAnswerTime = callStatisticOverview.getTotalAnswerTime() + ((Long) objs[1]);
			Long pickupInSpecifiedSec = callStatisticOverview.getPickupInSpecifiedSec() + ((Long) objs[2]);
			Double avgAnswerTime = (callStatisticOverview.getBridgedAmount() > 0L) ? ((totalAnswerTime+0.0) / callStatisticOverview.getBridgedAmount()) : 0.0;
			callStatisticOverview.setTotalAnswerTime(totalAnswerTime);
			callStatisticOverview.setPickupInSpecifiedSec(pickupInSpecifiedSec);
			callStatisticOverview.setAvgAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgAnswerTime)));
			
			// 更新呼入信息-外呼客户平均应答速度，在指定时间内客户接通数
			Long totalOutAnswerTime = callStatisticOverview.getTotalOutAnswerTime() + ((Long) objs[1]);
			Double avgOutAnswerTime = (callStatisticOverview.getBridgedOutAmount() > 0L) ? ((totalOutAnswerTime+0.0) / callStatisticOverview.getBridgedOutAmount()) : 0.0;
			Long customerPickupInSpecifiedSec = callStatisticOverview.getCustomerPickupInSpecifiedSec() + ((Long) objs[2]);
			callStatisticOverview.setTotalOutAnswerTime(totalOutAnswerTime);
			callStatisticOverview.setAvgOutAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgOutAnswerTime)));
			callStatisticOverview.setCustomerPickupInSpecifiedSec(customerPickupInSpecifiedSec);
		}
	}

	/**
	 * 只查呼入的数据，按 域和时段 进行划分统计：在指定时间内总的放弃数，指定时间内客户放弃数
	 * @throws ParseException
	 */
	private void searchIncomingCdrInfoList3(Date endDate) throws Exception {
		String nativeSql = "select to_char(starttimedate, 'yyyy-MM-dd HH24') as startDate," +
				" sum(case when ringduration <= "+specifiedSec+" then 1 else 0 end) as customerAbandonInSpecifiedSec,domainid " +
				" from cdr where cdrdirection = "+CdrDirection.o2e.getIndex()+" and isbridged is null and endtimedate "+dynamicScopeSql+"'" + startSearchDateStr + "' and endtimedate < '" + nextStepSearchDateStr + "' " +
				" group by to_char(starttimedate, 'yyyy-MM-dd HH24'),domainid order by to_char(starttimedate, 'yyyy-MM-dd HH24')";
		List<Object[]> incomingCdrInfoList3 = this.getRecord(nativeSql);

		printSqlInfo(true, nativeSql);
		
		for(Object[] objs : incomingCdrInfoList3) {
			// 检查指定时段的话务统计信息是否存在，如果不存在则新建
			String startDateHour = (String) objs[0];
			Date dhDate = SDF_YMDH.parse(startDateHour);
			Long domainId = (Long) objs[2];
			if(domainId.equals(0L)) {
				continue;
			}
			
			HashMap<Date, CallStatisticOverview> statisticMap = statistcDomainMap.get(domainId);
			if(statisticMap == null) {
				statisticMap = new HashMap<Date, CallStatisticOverview>();
				statistcDomainMap.put(domainId, statisticMap);
			}
			
			CallStatisticOverview callStatisticOverview = statisticMap.get(dhDate);
			if(callStatisticOverview == null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dhDate);
				Integer hour = calendar.get(Calendar.HOUR_OF_DAY);

				calendar.set(Calendar.HOUR_OF_DAY, 0);
				Date startDate = calendar.getTime();
				
				callStatisticOverview = new CallStatisticOverview();
				callStatisticOverview.setStartDate(startDate);
				callStatisticOverview.setEndDate(endDate);
				callStatisticOverview.setHour(hour);
				callStatisticOverview.setDomainId(domainId);
				statisticMap.put(dhDate, callStatisticOverview);
			}
			
			// 更新总体信息-在指定时间内总的放弃数
			Long abandonInSpecifiedSec = callStatisticOverview.getAbandonInSpecifiedSec() + ((Long) objs[1]);
			callStatisticOverview.setAbandonInSpecifiedSec(abandonInSpecifiedSec);
			
			// 更新呼入信息-指定时间内客户放弃数
			Long customerAbandonInSpecifiedSec = callStatisticOverview.getCustomerAbandonInSpecifiedSec() + ((Long) objs[1]);
			callStatisticOverview.setCustomerAbandonInSpecifiedSec(customerAbandonInSpecifiedSec);
		}
	}

	/**
	 * 只查呼出的数据，按 域和时段 进行划分统计：在指定时间内总的放弃数，指定时间内坐席放弃数
	 * @throws ParseException
	 */
	private void searchOutgoingCdrInfoList3(Date endDate) throws Exception {
		String nativeSql = "select to_char(starttimedate, 'yyyy-MM-dd HH24') as startDate," +
				" sum(case when ringduration <= "+specifiedSec+" then 1 else 0 end) as csrAbandonInSpecifiedSec,domainid " +
				" from cdr where cdrdirection != "+CdrDirection.o2e.getIndex()+" and isbridged is null and endtimedate "+dynamicScopeSql+"'" + startSearchDateStr + "' and endtimedate < '" + nextStepSearchDateStr + "' " +
				" group by to_char(starttimedate, 'yyyy-MM-dd HH24'),domainid order by to_char(starttimedate, 'yyyy-MM-dd HH24')";
		List<Object[]> outgoingCdrInfoList3 = this.getRecord(nativeSql);

		printSqlInfo(true, nativeSql);
		
		for(Object[] objs : outgoingCdrInfoList3) {
			// 检查指定时段的话务统计信息是否存在，如果不存在则新建
			String startDateHour = (String) objs[0];
			Date dhDate = SDF_YMDH.parse(startDateHour);
			Long domainId = (Long) objs[2];
			if(domainId.equals(0L)) {
				continue;
			}
			
			HashMap<Date, CallStatisticOverview> statisticMap = statistcDomainMap.get(domainId);
			if(statisticMap == null) {
				statisticMap = new HashMap<Date, CallStatisticOverview>();
				statistcDomainMap.put(domainId, statisticMap);
			}
			
			CallStatisticOverview callStatisticOverview = statisticMap.get(dhDate);
			if(callStatisticOverview == null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dhDate);
				Integer hour = calendar.get(Calendar.HOUR_OF_DAY);

				calendar.set(Calendar.HOUR_OF_DAY, 0);
				Date startDate = calendar.getTime();
				
				callStatisticOverview = new CallStatisticOverview();
				callStatisticOverview.setStartDate(startDate);
				callStatisticOverview.setEndDate(endDate);
				callStatisticOverview.setHour(hour);
				callStatisticOverview.setDomainId(domainId);
				statisticMap.put(dhDate, callStatisticOverview);
			}
			
			// 更新总体信息-在指定时间内总的放弃数
			Long abandonInSpecifiedSec = callStatisticOverview.getAbandonInSpecifiedSec() + ((Long) objs[1]);
			callStatisticOverview.setAbandonInSpecifiedSec(abandonInSpecifiedSec);
			
			// 更新呼入信息-指定时间内坐席放弃数
			Long csrAbandonInSpecifiedSec = callStatisticOverview.getCsrAbandonInSpecifiedSec() + ((Long) objs[1]);
			callStatisticOverview.setCsrAbandonInSpecifiedSec(csrAbandonInSpecifiedSec);
		}
	}

	
//	TODO
	/**
	 * 保存新建的统计信息，或者更新老的信息
	 * @throws Exception
	 */
	private void saveToDb() throws Exception {
		// 用来记录一次查询后，呼叫统计概览中间表中的最大时间
		Date tempMaxStartDate = maxCallStatisticStartDate;

		for(long domainId : statistcDomainMap.keySet()) {
			HashMap<Date,CallStatisticOverview> map = statistcDomainMap.get(domainId);
			if(domainId == 0) {
				map.clear();
				continue;
			}
			
			ArrayList<Date> tmpStartDateArray = startDateDomainMap.get(domainId);
			ArrayList<Date> dhDateList = new ArrayList<Date>(map.keySet());
			// 按时间先后排序
			sortServiceRecordStatusList(dhDateList);
			
			for(Date dhDate : dhDateList) {
				CallStatisticOverview cso = map.get(dhDate);
				
				// 如果当前要保存的cso 的时间比“开始时间数组”中最小的时间要往，则先保存一个空信息的cso 进入数据库
				while(tmpStartDateArray.size() > 0 && dhDate.after(tmpStartDateArray.get(0))) {
					tempMaxStartDate = tmpStartDateArray.get(0);
					CallStatisticOverview emptyCsoInfo = new CallStatisticOverview();
					Calendar cal = Calendar.getInstance();
					cal.setTime(tempMaxStartDate);
					int hour = cal.get(Calendar.HOUR_OF_DAY);
					cal.set(Calendar.HOUR_OF_DAY, 0);
					emptyCsoInfo.setStartDate(cal.getTime());
					emptyCsoInfo.setHour(hour);
					emptyCsoInfo.setEndDate(tempMaxStartDate);
					emptyCsoInfo.setDomainId(domainId);
					callStatisticOverviewService.save(emptyCsoInfo);
//					System.out.println("\t ---> tmp save empty "+SDF_ALL.format(tempMaxStartDate) + " -- : --" +SDF_ALL.format(dhDate));
					
					tmpStartDateArray.remove(0);
				}
				
				if(tmpStartDateArray.size() > 0 && dhDate.equals(tmpStartDateArray.get(0))) {
					tmpStartDateArray.remove(0);
				}

				if(dhDate.after(maxCallStatisticStartDate)) {
					tempMaxStartDate = dhDate;
					callStatisticOverviewService.save(cso);

//					System.out.println("\t\t ---> save new cso "+SDF_ALL.format(dhDate));
				} else {	// 如果比中间表的时间小，表示可能已经存在该信息了，所以查询并更新数据库
					CallStatisticOverview oldCso = callStatisticOverviewService.getByDateAndHour(cso.getStartDate(), cso.getHour(), cso.getDomainId());
					if(oldCso == null) { // 一般不会出现
						callStatisticOverviewService.save(cso);
					} else {
						Long callAmount = cso.getCallAmount() + oldCso.getCallAmount();
						Long bridgedAmount = cso.getBridgedAmount() + oldCso.getBridgedAmount();
						Long totalCallTime = cso.getTotalCallTime() + oldCso.getTotalCallTime();
						Double avgCallTime = (callAmount > 0L) ? ((totalCallTime+0.0) / callAmount) : 0.0;
						
						Long callInAmount = cso.getCallInAmount() + oldCso.getCallInAmount();
						Long bridgedInAmount = cso.getBridgedInAmount() + oldCso.getBridgedInAmount();
						Long totalInCallTime = cso.getTotalInCallTime() + oldCso.getTotalInCallTime();
						Double avgInCallTime = (callInAmount > 0L) ? ((totalInCallTime+0.0) / callInAmount) : 0.0;
						
						Long callOutAmount = cso.getCallOutAmount() + oldCso.getCallOutAmount();
						Long bridgedOutAmount = cso.getBridgedOutAmount() + oldCso.getBridgedOutAmount();
						Long totalOutCallTime = cso.getTotalOutCallTime() + oldCso.getTotalOutCallTime();
						Double avgOutCallTime = (callOutAmount > 0L) ? ((totalOutCallTime+0.0) / callOutAmount) : 0.0;
						
						Long totalAnswerTime = cso.getTotalAnswerTime() + oldCso.getTotalAnswerTime();
						Double avgAnswerTime = (bridgedAmount > 0L) ? ((totalAnswerTime+0.0) / bridgedAmount) : 0.0;
						Long totalInAnswerTime = cso.getTotalInAnswerTime() + oldCso.getTotalInAnswerTime();
						Double avgInAnswerTime = (bridgedInAmount > 0L) ? ((totalInAnswerTime+0.0) / bridgedInAmount) : 0.0;
						
						Long totalOutAnswerTime = cso.getTotalOutAnswerTime() + oldCso.getTotalOutAnswerTime();
						Double avgOutAnswerTime = (bridgedOutAmount > 0L) ? ((totalOutAnswerTime+0.0) / bridgedOutAmount) : 0.0;
						Long pickupInSpecifiedSec = cso.getPickupInSpecifiedSec() + oldCso.getPickupInSpecifiedSec();
						Long csrPickupInSpecifiedSec = cso.getCsrPickupInSpecifiedSec() + oldCso.getCsrPickupInSpecifiedSec();
						
						Long customerPickupInSpecifiedSec = cso.getCustomerPickupInSpecifiedSec() + oldCso.getCustomerPickupInSpecifiedSec();
						Long abandonInSpecifiedSec = cso.getAbandonInSpecifiedSec() + oldCso.getAbandonInSpecifiedSec();
						Long customerAbandonInSpecifiedSec = cso.getCustomerAbandonInSpecifiedSec() + oldCso.getCustomerAbandonInSpecifiedSec();
						Long csrAbandonInSpecifiedSec = cso.getCsrAbandonInSpecifiedSec() + oldCso.getCsrAbandonInSpecifiedSec();
						
						Double bridgedRate = (callAmount > 0) ? ((bridgedAmount+0.0) / callAmount) : 0.0;
						Double bridgedInRate = (callInAmount > 0) ? ((bridgedInAmount+0.0) / callInAmount) : 0.0;
						Double bridgedOutRate = (callOutAmount > 0) ? ((bridgedOutAmount+0.0) / callOutAmount) : 0.0;
						Double incomingServiceLevel = (callInAmount > 0) ? ((csrPickupInSpecifiedSec+0.0) / callInAmount) : 0.0;
						
						oldCso.setEndDate(cso.getEndDate());
						oldCso.setCallAmount(callAmount);
						oldCso.setBridgedAmount(bridgedAmount);
						oldCso.setTotalCallTime(totalCallTime);
						oldCso.setAvgCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgCallTime)));
						
						oldCso.setCallInAmount(callInAmount);
						oldCso.setBridgedInAmount(bridgedInAmount);
						oldCso.setTotalInCallTime(totalInCallTime);
						oldCso.setAvgInCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgInCallTime)));
						
						oldCso.setCallOutAmount(callOutAmount);
						oldCso.setBridgedOutAmount(bridgedOutAmount);
						oldCso.setTotalOutCallTime(totalOutCallTime);
						oldCso.setAvgOutCallTime(Double.valueOf(DF_TWO_PRECISION.format(avgOutCallTime)));
						
						oldCso.setTotalAnswerTime(totalAnswerTime);
						oldCso.setAvgAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgAnswerTime)));
						oldCso.setTotalInAnswerTime(totalInAnswerTime);
						oldCso.setAvgInAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgInAnswerTime)));
						
						oldCso.setTotalOutAnswerTime(totalOutAnswerTime);
						oldCso.setAvgOutAnswerTime(Double.valueOf(DF_TWO_PRECISION.format(avgOutAnswerTime)));
						oldCso.setPickupInSpecifiedSec(pickupInSpecifiedSec);
						oldCso.setCsrPickupInSpecifiedSec(csrPickupInSpecifiedSec);
						
						oldCso.setCustomerPickupInSpecifiedSec(customerPickupInSpecifiedSec);
						oldCso.setAbandonInSpecifiedSec(abandonInSpecifiedSec);
						oldCso.setCustomerAbandonInSpecifiedSec(customerAbandonInSpecifiedSec);
						oldCso.setCsrAbandonInSpecifiedSec(csrAbandonInSpecifiedSec);
						
						oldCso.setBridgedRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedRate)));
						oldCso.setBridgedInRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedInRate)));
						oldCso.setBridgedOutRate(Double.valueOf(DF_FOUR_PRECISION.format(bridgedOutRate)));
						oldCso.setIncomingServiceLevel(Double.valueOf(DF_FOUR_PRECISION.format(incomingServiceLevel)));

						callStatisticOverviewService.update(oldCso);

//						System.out.println("\t\t\t ---> update old cso "+SDF_ALL.format(dhDate));
					}
				}
			}
			// 清空数据
			map.clear();
		}
		maxCallStatisticStartDate = tempMaxStartDate;
	}
	
	/**
	 * 按时间先后排序
	 * @param dateList 乱序的集合
	 */
	private void sortServiceRecordStatusList(ArrayList<Date> dateList) {
		Collections.sort(dateList, new Comparator<Date>() {
			@Override
			public int compare(Date date1, Date date2) {
				return date1.compareTo(date2);
			}
		});
	}
	
//	TODO 在正式上线的时候要将其 isAllStop 设置成 true
	/**
	 * 调试用的方法，用于打印我们调用的sql语句，如果要输出就将isAllStop 设置成 false
	 * 	在正式上线的时候要将其 isAllStop 设置成 true
	 * @param isprint
	 * @param info
	 */
	private void printSqlInfo(boolean isprint, String info) {
		boolean isAllStop = true;		
		if(!isAllStop && isprint) {
			logger.info(info);
			logger.info("");
		}
	}
	
	@Override
	public String getReportName() {
		return "呼叫统计概览中间表";
	}
}
