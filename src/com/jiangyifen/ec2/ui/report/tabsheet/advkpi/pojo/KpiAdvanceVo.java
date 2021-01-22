package com.jiangyifen.ec2.ui.report.tabsheet.advkpi.pojo;

import java.text.DecimalFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;



/**
 * @Description 描述：高级KPI详情考察字段对应实体
 *
 * @author  jrh
 * @date    2014年4月1日 下午4:37:22
 * @version v1.0.0
 */
public class KpiAdvanceVo {

	private final DecimalFormat DF_TWO_PRECISION = new DecimalFormat("0.00");
	private final DecimalFormat DF_FOUR_PRECISION = new DecimalFormat("0.0000");
	
	/**
	 * cdr的开始日期
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	private Date startDate;	// yyyy-MM-dd 2013-09-05

	/**
	 * cdr的开始小时
	 */
	@Column(columnDefinition="integer NOT NULL", nullable = false)
	private Integer hour;
	
//	// cdr的结束日期
//	@Temporal(TemporalType.TIMESTAMP)
//	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
//	private Date endDate;	// yyyy-MM-dd HH 2013-09-05 13

	/**
	 * 租户编号
	 */
	@Column(columnDefinition="bigint")
	private Long domainId;            
	
	/************** 基础统计详情  ********************/

	/**
	 * 部门编号
	 */
	@Column(columnDefinition="bigint")
	private Long deptId;              
	
	/**
	 * 坐席所属部门
	 */
	private String deptName;          

	/**
	 * 坐席编号
	 */
	@Column(columnDefinition="bigint")
	private Long csrId;

	/**
	 * 坐席姓名
	 */
	private String csrName;
	
	/**
	 * 坐席工号
	 */
	private String empno;
	
	/**
	 * 坐席用户名
	 */
	private String username;

	
	/************** 登陆信息  ********************/
	
	/**
	 * 登陆次数
	 */
	@Column(columnDefinition="bigint")
	private Long loginCt = 0L;

	/**
	 * 登陆时长
	 */
	@Column(columnDefinition="bigint")
	private Long onlineDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String onlineDtStr = "";

	/**
	 * 就绪时长 = 在线时长 - 各种置忙时长 - 总的话后处理时长
	 */
	@Column(columnDefinition="bigint")
	private Long readyDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String readyDtStr = "";

	/**
	 * 有效工作时长   = 呼入时长 + 呼入后处理时长 + 呼出时长 + 呼出后理时长 + 内部呼叫时长
	 */
	@Column(columnDefinition="bigint")
	private Long effectWorkDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String effectWorkDtStr = "";
	
	/**
	 * 有效工作率% = 有效工作时长 / 登录时长
	 */
	private Double effectWorkRt = 0.0000;

	/**
	 * @Description 描述：统计 [ 就绪时长、有效工作时长、 有效工作率  ]
	 *
	 * @author  JRH
	 * @date    2014年5月13日 下午3:57:06 void
	 */
	public void calcLoginInfos() {
		this.readyDt = onlineDt - leavePsDt - meettingPsDt - busyPsDt - restPsDt - dinePsDt - trainPsDt - outAfterDt - inAfterDt;
		if(readyDt < 0) {
			readyDt = 0L;
		}
		this.readyDtStr = DateUtil.getTime(readyDt);
		
		this.effectWorkDt = outDialDt + outAfterDt + innerDialDt + inDialDt + inAfterDt;
		if(effectWorkDt > onlineDt) {
			effectWorkDt = onlineDt;
		}
		this.effectWorkDtStr = DateUtil.getTime(effectWorkDt);
		
		if(onlineDt > 0) {
			this.effectWorkRt = Double.valueOf(DF_FOUR_PRECISION.format((effectWorkDt+0.0) / onlineDt));
		}
	}
	
	/************** 置忙置闲统计详情  ********************/

	/**
	 * 置忙次数 = 其他各种置忙原因对应的置忙次数的总和
	 */
	@Column(columnDefinition="bigint")
	private Long pauseCt = 0L;

	/**
	 * 置忙时长 = 其他各种置忙原因对应的置忙时长的总和
	 */
	@Column(columnDefinition="bigint")
	private Long pauseDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String pauseDtStr = "";

	/**
	 * 离开次数
	 */
	@Column(columnDefinition="bigint")
	private Long leavePsCt = 0L;

	/**
	 * 离开时长
	 */
	@Column(columnDefinition="bigint")
	private Long leavePsDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String leavePsDtStr = "";

	/**
	 * 会议次数
	 */
	@Column(columnDefinition="bigint")
	private Long meettingPsCt = 0L;

	/**
	 * 会议时长
	 */
	@Column(columnDefinition="bigint")
	private Long meettingPsDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String meettingPsDtStr = "";

	/**
	 * 忙碌次数
	 */
	@Column(columnDefinition="bigint")
	private Long busyPsCt = 0L;

	/**
	 * 忙碌时长
	 */
	@Column(columnDefinition="bigint")
	private Long busyPsDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String busyPsDtStr = "";

	/**
	 * 休息次数
	 */
	@Column(columnDefinition="bigint")
	private Long restPsCt = 0L;

	/**
	 * 休息时长
	 */
	@Column(columnDefinition="bigint")
	private Long restPsDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String restPsDtStr = "";

	/**
	 * 就餐次数
	 */
	@Column(columnDefinition="bigint")
	private Long dinePsCt = 0L;

	/**
	 * 就餐时长
	 */
	@Column(columnDefinition="bigint")
	private Long dinePsDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String dinePsDtStr = "";

	/**
	 * 培训次数
	 */
	@Column(columnDefinition="bigint")
	private Long trainPsCt = 0L;

	/**
	 * 培训时长
	 */
	@Column(columnDefinition="bigint")
	private Long trainPsDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String trainPsDtStr = "";
	
	/**
	 * @Description 描述：统计 【 置忙总次数、置忙总时长 】
	 *
	 * @author  JRH
	 * @date    2014年5月13日 下午3:58:14 void
	 */
	public void calcPauseInfos() {
		this.pauseCt = leavePsCt + meettingPsCt + busyPsCt + restPsCt + dinePsCt + trainPsCt;
		this.pauseDt = leavePsDt + meettingPsDt + busyPsDt + restPsDt + dinePsDt + trainPsDt;
		this.pauseDtStr = DateUtil.getTime(pauseDt);
	}
	
	/************** 话务统计详情  ********************/

	/**
	 * 话务总量 = 呼入总量 + 内部呼叫总量 + 呼出总量
	 */
	@Column(columnDefinition="bigint")
	private Long dialCt = 0L;

	/**
	 * 话务接通总量 = 呼入接通总量 + 内部接通总量 + 呼出接通总量
	 */
	@Column(columnDefinition="bigint")
	private Long bridgeCt = 0L;

	/**
	 * 话务接通总量 = 话务总量 -话务接通总量
	 */
	@Column(columnDefinition="bigint")
	private Long unbridgeCt = 0L;
	
	/**
	 * 话务接通率 = 话务接通总量 / 话务总量  * 100%
	 */
	private Double bridgeRt = 0.0000;

	/**
	 * 话务总时长 = 呼入总时长 + 内部呼叫总时长 + 呼出总时长
	 */
	@Column(columnDefinition="bigint")
	private Long dialDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String dialDtStr = "";

	/**
	 * 话务接通时长 = 呼入接通时长 + 内部呼叫接通时长 + 呼出接通时长
	 */
	@Column(columnDefinition="bigint")
	private Long dialBgDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String dialBgDtStr = "";

	/**
	 * 有效话务率 = 话务接通总数 / 话务总数 * 100%
	 */
	private Double effectDialRt = 0.0000;

	/**
	 * 每小时话务量 = 话务总数 / 在线小时数
	 */
	private Double perHourDialCt = 0.00;

	/**
	 * @Description 描述：统计 【 话务总量、话务接通总量、话务接通总量、话务接通率%、话务总时长、话务接通时长、有效话务率%、每小时话务量】
	 *
	 * @author  JRH
	 * @date    2014年5月13日 下午3:58:14 void
	 */
	public void calcAllDialInfos() {
		this.dialCt = outDialCt + innerDialCt + inDialCt;
		this.bridgeCt = outBgCt + innerBgCt + inBgCt;
		this.unbridgeCt = dialCt - bridgeCt;
		if(dialCt > 0) {
			this.bridgeRt = Double.valueOf(DF_FOUR_PRECISION.format((bridgeCt+0.0) / dialCt));
		}
		
		this.dialDt = outDialDt + innerDialDt + inDialDt;
		this.dialDtStr = DateUtil.getTime(dialDt);
		this.dialBgDt = outBgDt + innerBgDt + inBgDt;
		this.dialBgDtStr = DateUtil.getTime(dialBgDt);
		
		if(dialDt > 0) {
			this.effectDialRt = Double.valueOf(DF_FOUR_PRECISION.format((dialBgDt + 0.0) / dialDt));
		}

		long evaluateHour = (onlineDt + 1800) / 3600;
		if(evaluateHour > 0) {
			this.perHourDialCt = Double.valueOf(DF_TWO_PRECISION.format((dialCt + 0.0) / evaluateHour));
		}
	}
	
	/************** 呼出话务统计详情  ********************/

	/**
	 * 呼出总数
	 */
	@Column(columnDefinition="bigint")
	private Long outDialCt = 0L;

	/**
	 * 呼出接通数
	 */
	@Column(columnDefinition="bigint")
	private Long outBgCt = 0L;

	/**
	 * 呼出未接通数 = 呼出总数 - 呼出接通数
	 */
	@Column(columnDefinition="bigint")
	private Long outUnbgCt = 0L;

	/**
	 * 呼出接通率 = 呼出接通数 / 呼出总数 * 100%
	 */
	private Double outBgRt = 0.0000;

	/**
	 * 30 	小于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLowSecCt = 0L;
	
	/**
	 * 30	小于xx秒呼出接通率 = 小于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLowSecRt = 0.0000;
	
	/**
	 * 45 	小于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLowSecCt1 = 0L;
	
	/**
	 * 45	小于xx秒呼出接通率 = 小于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLowSecRt1 = 0.0000;
	
	/**
	 * 45	大于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLargeSecCt1 = 0L;
	
	/**
	 * 45	大于xx秒呼出接通率 = 大于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLargeSecRt1 = 0.0000;
	
	/**
	 * 120	大于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLargeSecCt3 = 0L;
	
	/**
	 * 120	大于xx秒呼出接通率 = 大于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLargeSecRt3 = 0.0000;
	
	/**
	 * 240	大于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLargeSecCt4 = 0L;
	
	/**
	 * 240	大于xx秒呼出接通率 = 大于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLargeSecRt4 = 0.0000;
	
	/**
	 * 600	大于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLargeSecCt5 = 0L;
	
	/**
	 * 600	大于xx秒呼出接通率 = 大于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLargeSecRt5 = 0.0000;
	
	/**
	 * 1200	大于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLargeSecCt6 = 0L;
	
	/**
	 * 1200	大于xx秒呼出接通率 = 大于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLargeSecRt6 = 0.0000;
	
	/**
	 * 1800	大于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLargeSecCt7 = 0L;
	
	/**
	 * 1800	大于xx秒呼出接通率 = 大于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLargeSecRt7 = 0.0000;

	/**
	 * 50 	小于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLowSecCt2 = 0L;
	
	/**
	 * 50	小于xx秒呼出接通率 = 小于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLowSecRt2 = 0.00;

	/**
	 * 300	大于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLargeSecCt = 0L;

	/**
	 * 300	大于xx秒呼出接通率 = 大于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLargeSecRt = 0.0000;

	/**
	 * 500	大于xx秒呼出接通量
	 */
	@Column(columnDefinition="bigint")
	private Long outBgLargeSecCt2 = 0L;

	/**
	 * 500	大于xx秒呼出接通率 = 大于xx秒呼出接通量 / 呼出接通总数
	 */
	private Double outBgLargeSecRt2 = 0.00;

	/**
	 * 最大呼出通话时长 = 呼出的电话当中，坐席跟客户交流时间最长的通话时长
	 */
	@Column(columnDefinition="bigint")
	private Long outMaxBgDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String outMaxBgDtStr = "";

	/**
	 * 呼出总时长
	 */
	@Column(columnDefinition="bigint")
	private Long outDialDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String outDialDtStr = "";

	/**
	 * 呼出等待时长
	 */
	@Column(columnDefinition="bigint")
	private Long outRingDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String outRingDtStr = "";

	/**
	 * 平均呼出等待时长 = 呼出等待时长 / 呼出总数
	 */
	private Double outAvgRingDt = 0.00;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String outAvgRingDtStr = "";

	/**
	 * 呼出接通时长 = 呼出接通时长 / 呼出接通总数
	 */
	@Column(columnDefinition="bigint")
	private Long outBgDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String outBgDtStr = "";
	
	/**
	 * 呼出平均接通时长
	 */
	private Double outAvgBgDt = 0.00;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String outAvgBgDtStr = "";

	/**
	 * 有效呼出时长占有率 = 呼出接通时长 / 呼出总时长 * 100%
	 */
	private Double outEffectDtRt = 0.0000;

	/**
	 * 呼出后处理次数
	 */
	@Column(columnDefinition="bigint")
	private Long outAfterCt = 0L;

	/**
	 * 呼出后处理时长
	 */
	@Column(columnDefinition="bigint")
	private Long outAfterDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String outAfterDtStr = "";

	/**
	 * 每小时呼出量 = 呼出总量 / 在线小时数
	 */
	private Double outPerHourCt = 0.00;

	/**
	 * @Description 描述：统计 【 呼出未接通数、呼出接通率%、呼出通话时长过短比例%-30、呼出通话时长过短比例%-50、呼出通话时长过长比例%-300、呼出通话时长过长比例%-500、平均呼出等待时长、呼出平均接通时长、有效呼出时长占有率%、每小时呼出数】
	 *
	 * @author  JRH
	 * @date    2014年5月13日 下午3:58:14 void
	 */
	public void calcOutDialInfos() {
		this.outUnbgCt = outDialCt - outBgCt;
		
		if(outDialCt > 0) {
			this.outBgRt = Double.valueOf(DF_FOUR_PRECISION.format((outBgCt+0.0) / outDialCt));
			this.outAvgRingDt = Double.valueOf(DF_TWO_PRECISION.format((outRingDt+0.0) / outDialCt));
			this.outAvgRingDtStr = DateUtil.getTime(outAvgRingDt.longValue());
		}

		if(outDialDt > 0) {
			this.outEffectDtRt = Double.valueOf(DF_FOUR_PRECISION.format((outBgDt + 0.0) / outDialDt));
		}
		
		if(outBgCt > 0) {
			this.outBgLowSecRt = Double.valueOf(DF_FOUR_PRECISION.format((outBgLowSecCt+0.0) / outBgCt));
			this.outBgLowSecRt1 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLowSecCt1+0.0) / outBgCt));
			this.outBgLowSecRt2 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLowSecCt2+0.0) / outBgCt));
			this.outBgLargeSecRt1 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLargeSecCt1+0.0) / outBgCt));
			this.outBgLargeSecRt3 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLargeSecCt3+0.0) / outBgCt));
			this.outBgLargeSecRt4 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLargeSecCt4+0.0) / outBgCt));
			this.outBgLargeSecRt5 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLargeSecCt5+0.0) / outBgCt));
			this.outBgLargeSecRt6 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLargeSecCt6+0.0) / outBgCt));
			this.outBgLargeSecRt7 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLargeSecCt7+0.0) / outBgCt));
			this.outBgLargeSecRt = Double.valueOf(DF_FOUR_PRECISION.format((outBgLargeSecCt+0.0) / outBgCt));
			this.outBgLargeSecRt2 = Double.valueOf(DF_FOUR_PRECISION.format((outBgLargeSecCt2+0.0) / outBgCt));
			this.outAvgBgDt = Double.valueOf(DF_TWO_PRECISION.format((outBgDt+0.0) / outBgCt));
			this.outAvgBgDtStr = DateUtil.getTime(outAvgBgDt.longValue());
		}

		long evaluateHour = (onlineDt + 1800) / 3600;
		if(evaluateHour > 0) {
			this.outPerHourCt = Double.valueOf(DF_TWO_PRECISION.format((outDialCt + 0.0) / evaluateHour));
		}
	}

	/************** 内部话务统计详情  ********************/

	/**
	 * 内部呼叫总量
	 */
	@Column(columnDefinition="bigint")
	private Long innerDialCt = 0L;

	/**
	 * 内部呼叫接通数
	 */
	@Column(columnDefinition="bigint")
	private Long innerBgCt = 0L;

	/**
	 * 内部呼叫未接通数
	 */
	@Column(columnDefinition="bigint")
	private Long innerUnbgCt = 0L;
	
	/**
	 * 内部呼叫接通率 = 内部呼叫接通数 / 内部呼叫总量 * 100%
	 */
	private Double innerBgRt = 0.0000;

	/**
	 * 内部呼叫时长
	 */
	@Column(columnDefinition="bigint")
	private Long innerDialDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String innerDialDtStr = "";

	/**
	 * 内部接通时长
	 */
	@Column(columnDefinition="bigint")
	private Long innerBgDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String innerBgDtStr = "";

	/**
	 * 最大内部呼叫时长
	 */
	@Column(columnDefinition="bigint")
	private Long innerMaxBgDt = 0L;
	/**
	 * 格式： 14天 15:00:15
	 */
	private String innerMaxBgDtStr = "";

	/**
	 * 每小时内呼数 = 内部呼叫总数 / 在线小时数
	 */
	private Double innerPerHourCt = 0.00;

	/**
	 * @Description 描述：统计 【未内部接通数、内部接通率、每小时内呼数】
	 *
	 * @author  JRH
	 * @date    2014年5月13日 下午3:58:14 void
	 */
	public void calcInnerDialInfos() {
		this.innerUnbgCt =  innerDialCt - innerBgCt;
		
		if(innerDialCt > 0) {
			this.innerBgRt = Double.valueOf(DF_FOUR_PRECISION.format((innerBgCt+0.0) / innerDialCt));
		}
		
		long evaluateHour = (onlineDt + 1800) / 3600;
		if(evaluateHour > 0) {
			this.innerPerHourCt = Double.valueOf(DF_TWO_PRECISION.format((innerDialCt + 0.0) / evaluateHour));
		}
	}
	
	/************** 呼入话务统计详情  ********************/
	
	/**
	 * 呼入总数
	 */
	@Column(columnDefinition="bigint")
	private Long inDialCt = 0L;

	/**
	 * 呼入接通数
	 */
	@Column(columnDefinition="bigint")
	private Long inBgCt = 0L;

	/**
	 * 呼入未接通数 = 呼入总数 - 呼入接通数
	 */
	@Column(columnDefinition="bigint")
	private Long inUnbgCt = 0L;
	
	/**
	 * 呼入接通率 = 呼入接通数 / 呼入总数 * 100%
	 */
	private Double inBgRt = 0.0000;
	
	@Column(columnDefinition="bigint")
	private Long inMissCt = 0L;

	/**
	 * 5  xx秒服务水平内接通数
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLevelCt = 0L;

	/**
	 * 5  xx秒服务水平% = xx秒服务水平内接通数 / 呼入总量
	 */
	private Double inLevelRt = 0.0000;
	
	/**
	 * 10	xx秒服务水平内接通数
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLevelCt2 = 0L;

	/**
	 * 10  xx秒服务水平% = xx秒服务水平内接通数 / 呼入总量
	 */
	private Double inLevelRt2 = 0.0000;
	
	/**
	 * 5 	(呼入方向，并且接通了，而且双方交流的时长小于 5秒) 小于zz秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLowSecCt = 0L;

	/**
	 * 5 	小于zz秒的呼入接通率 = 小于zz秒的呼入接通量 / 呼入总的接通量
	 */
	private Double inBgLowSecRt = 0.0000;
	
	/**
	 * 10	小于zz秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLowSecCt2 = 0L;
	
	/**
	 * 10	小于zz秒的呼入接通率
	 */
	private Double inBgLowSecRt2 = 0.0000;
	
	/**
	 * 45	小于zz秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLowSecCt1 = 0L;
	
	/**
	 * 45	小于zz秒的呼入接通率
	 */
	private Double inBgLowSecRt1 = 0.0000;
	
	/**
	 * 45	大于ww秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLargeSecCt1 = 0L;

	/**
	 * 45 	大于ww秒的呼入接通率 = 大于ww秒的呼入接通量 / 呼入总的接通量
	 */
	private Double inBgLargeSecRt1 = 0.0000;
	
	/**
	 * 120	大于ww秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLargeSecCt3 = 0L;

	/**
	 * 120 	大于ww秒的呼入接通率 = 大于ww秒的呼入接通量 / 呼入总的接通量
	 */
	private Double inBgLargeSecRt3 = 0.0000;
	
	/**
	 * 240	大于ww秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLargeSecCt4 = 0L;

	/**
	 * 240 	大于ww秒的呼入接通率 = 大于ww秒的呼入接通量 / 呼入总的接通量
	 */
	private Double inBgLargeSecRt4 = 0.0000;
	
	/**
	 * 600	大于ww秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLargeSecCt5 = 0L;

	/**
	 * 600 	大于ww秒的呼入接通率 = 大于ww秒的呼入接通量 / 呼入总的接通量
	 */
	private Double inBgLargeSecRt5 = 0.0000;
	
	/**
	 * 1200	大于ww秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLargeSecCt6 = 0L;

	/**
	 * 1200 	大于ww秒的呼入接通率 = 大于ww秒的呼入接通量 / 呼入总的接通量
	 */
	private Double inBgLargeSecRt6 = 0.0000;

	/**
	 * 1800	大于ww秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLargeSecCt7 = 0L;

	/**
	 * 1800 	大于ww秒的呼入接通率 = 大于ww秒的呼入接通量 / 呼入总的接通量
	 */
	private Double inBgLargeSecRt7 = 0.0000;

	
	/**
	 * 300	大于ww秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLargeSecCt = 0L;

	/**
	 * 300 	大于ww秒的呼入接通率 = 大于ww秒的呼入接通量 / 呼入总的接通量
	 */
	private Double inBgLargeSecRt = 0.0000;
	
	/**
	 * 500	大于ww秒的呼入接通量
	 */
	@Column(columnDefinition="bigint")
	private Long inBgLargeSecCt2 = 0L;
	
	/**
	 * 500	 大于ww秒的呼入接通率
	 */
	private Double inBgLargeSecRt2 = 0.0000;

	/**
	 * 最大呼入通话时长
	 */
	@Column(columnDefinition="bigint")
	private Long inMaxBgDt = 0L;
	/** 格式： 14天 15:00:15 */
	private String inMaxBgDtStr = "";

	/**
	 * 呼入总时长
	 */
	@Column(columnDefinition="bigint")
	private Long inDialDt = 0L;
	/** 格式： 14天 15:00:15 */
	private String inDialDtStr = "";

	/**
	 * 呼入等待时长
	 */
	@Column(columnDefinition="bigint")
	private Long inRingDt = 0L;
	/** 格式： 14天 15:00:15 */
	private String inRingDtStr = "";

	/**
	 * 平均呼入等待时长 = 呼入等待时长 / 呼入总数
	 */
	private Double inAvgRingDt = 0.00;
	/** 格式： 14天 15:00:15 */
	private String inAvgRingDtStr = "";

	/**
	 * 呼入接通时长
	 */
	@Column(columnDefinition="bigint")
	private Long inBgDt = 0L;
	/** 格式： 14天 15:00:15 */
	private String inBgDtStr = "";

	/**
	 * 呼入平均接通时长 = 呼入接通时长 / 呼入接通总数
	 */
	private Double inAvgBgDt = 0.00;
	/** 格式： 14天 15:00:15 */
	private String inAvgBgDtStr = "";

	/**
	 * 有效呼入时长占有率 = 呼入接通时长 / 呼入总时长 * 100%
	 */
	private Double inEffectDtRt = 0.0000;

	/**
	 * 呼入后处理次数
	 */
	@Column(columnDefinition="bigint")
	private Long inAfterCt = 0L;

	/**
	 * 呼入后处理时长
	 */
	@Column(columnDefinition="bigint")
	private Long inAfterDt = 0L;
	/** 格式： 14天 15:00:15 */
	private String inAfterDtStr = "";

	/**
	 * 每小时呼入数 = 呼入总数 / 在线小时数
	 */
	private Double inPerHourCt = 0.00;

	/**
	 * @Description 描述：统计 【呼入未接通数、呼入接通率%、服务水平%-5、服务水平%-10、呼入通话时长过短比例%-5、呼入通话时长过短比例%-10、
	 * 						呼入通话时长过长比例%-300、呼入通话时长过长比例%-500、呼入平均振铃时长、呼入平均接通时长、有效呼入时长占有率%、每小时呼入数】
	 *
	 * @author  JRH
	 * @date    2014年5月13日 下午3:58:14 void
	 */
	public void calcInDialInfos() {
		this.inUnbgCt = inDialCt - inBgCt;
		
		if(inDialCt > 0) {
			this.inBgRt = Double.valueOf(DF_FOUR_PRECISION.format((inBgCt+0.0) / inDialCt));
			this.inLevelRt = Double.valueOf(DF_FOUR_PRECISION.format((inBgLevelCt+0.0) / inDialCt));
			this.inLevelRt2 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLevelCt2+0.0) / inDialCt));
			this.inAvgRingDt = Double.valueOf(DF_TWO_PRECISION.format((inRingDt+0.0) / inDialCt));
			this.inAvgRingDtStr = DateUtil.getTime(inAvgRingDt.longValue());
		}
		
		if(inDialDt > 0) {
			this.inEffectDtRt = Double.valueOf(DF_FOUR_PRECISION.format((inBgDt + 0.0) / inDialDt));
		}
		
		if(inBgCt > 0) {
			this.inBgLowSecRt = Double.valueOf(DF_FOUR_PRECISION.format((inBgLowSecCt+0.0) / inBgCt));
			this.inBgLowSecRt2 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLowSecCt2+0.0) / inBgCt));
			this.inBgLowSecRt1 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLowSecCt1+0.0) / inBgCt));
			
			this.inBgLargeSecRt = Double.valueOf(DF_FOUR_PRECISION.format((inBgLargeSecCt+0.0) / inBgCt));
			this.inBgLargeSecRt1 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLargeSecCt1+0.0) / inBgCt));
			this.inBgLargeSecRt2 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLargeSecCt2+0.0) / inBgCt));
			this.inBgLargeSecRt3 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLargeSecCt3+0.0) / inBgCt));
			this.inBgLargeSecRt4 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLargeSecCt4+0.0) / inBgCt));
			this.inBgLargeSecRt5 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLargeSecCt5+0.0) / inBgCt));
			this.inBgLargeSecRt6 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLargeSecCt6+0.0) / inBgCt));
			this.inBgLargeSecRt7 = Double.valueOf(DF_FOUR_PRECISION.format((inBgLargeSecCt7+0.0) / inBgCt));
			
			this.inAvgBgDt = Double.valueOf(DF_TWO_PRECISION.format((inBgDt+0.0) / inBgCt));
			this.inAvgBgDtStr = DateUtil.getTime(inAvgBgDt.longValue());
		}

		long evaluateHour = (onlineDt + 1800) / 3600;
		if(evaluateHour > 0) {
			this.inPerHourCt = Double.valueOf(DF_TWO_PRECISION.format((inDialCt + 0.0) / evaluateHour));
		}
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Integer getHour() {
		return hour;
	}

	public void setHour(Integer hour) {
		this.hour = hour;
	}

//	public Date getEndDate() {
//		return endDate;
//	}
//
//	public void setEndDate(Date endDate) {
//		this.endDate = endDate;
//	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public Long getCsrId() {
		return csrId;
	}

	public void setCsrId(Long csrId) {
		this.csrId = csrId;
	}

	public String getCsrName() {
		return csrName;
	}

	public void setCsrName(String csrName) {
		this.csrName = csrName;
	}

	public String getEmpno() {
		return empno;
	}

	public void setEmpno(String empno) {
		this.empno = empno;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getLoginCt() {
		return loginCt;
	}

	public void setLoginCt(Long loginCt) {
		this.loginCt = loginCt;
	}

	public Long getOnlineDt() {
		return onlineDt;
	}

	public void setOnlineDt(Long onlineDt) {
		this.onlineDt = onlineDt;
	}

	public String getOnlineDtStr() {
		return onlineDtStr;
	}

	public void setOnlineDtStr(String onlineDtStr) {
		this.onlineDtStr = onlineDtStr;
	}

	public Long getReadyDt() {
		return readyDt;
	}

	public void setReadyDt(Long readyDt) {
		this.readyDt = readyDt;
	}

	public String getReadyDtStr() {
		return readyDtStr;
	}

	public void setReadyDtStr(String readyDtStr) {
		this.readyDtStr = readyDtStr;
	}

	public Long getEffectWorkDt() {
		return effectWorkDt;
	}

	public void setEffectWorkDt(Long effectWorkDt) {
		this.effectWorkDt = effectWorkDt;
	}

	public String getEffectWorkDtStr() {
		return effectWorkDtStr;
	}

	public void setEffectWorkDtStr(String effectWorkDtStr) {
		this.effectWorkDtStr = effectWorkDtStr;
	}

	public Double getEffectWorkRt() {
		return effectWorkRt;
	}

	public void setEffectWorkRt(Double effectWorkRt) {
		this.effectWorkRt = effectWorkRt;
	}

	public Long getPauseCt() {
		return pauseCt;
	}

	public void setPauseCt(Long pauseCt) {
		this.pauseCt = pauseCt;
	}

	public Long getPauseDt() {
		return pauseDt;
	}

	public void setPauseDt(Long pauseDt) {
		this.pauseDt = pauseDt;
	}

	public String getPauseDtStr() {
		return pauseDtStr;
	}

	public void setPauseDtStr(String pauseDtStr) {
		this.pauseDtStr = pauseDtStr;
	}

	public Long getLeavePsCt() {
		return leavePsCt;
	}

	public void setLeavePsCt(Long leavePsCt) {
		this.leavePsCt = leavePsCt;
	}

	public Long getLeavePsDt() {
		return leavePsDt;
	}

	public void setLeavePsDt(Long leavePsDt) {
		this.leavePsDt = leavePsDt;
	}

	public String getLeavePsDtStr() {
		return leavePsDtStr;
	}

	public void setLeavePsDtStr(String leavePsDtStr) {
		this.leavePsDtStr = leavePsDtStr;
	}

	public Long getMeettingPsCt() {
		return meettingPsCt;
	}

	public void setMeettingPsCt(Long meettingPsCt) {
		this.meettingPsCt = meettingPsCt;
	}

	public Long getMeettingPsDt() {
		return meettingPsDt;
	}

	public void setMeettingPsDt(Long meettingPsDt) {
		this.meettingPsDt = meettingPsDt;
	}

	public String getMeettingPsDtStr() {
		return meettingPsDtStr;
	}

	public void setMeettingPsDtStr(String meettingPsDtStr) {
		this.meettingPsDtStr = meettingPsDtStr;
	}

	public Long getBusyPsCt() {
		return busyPsCt;
	}

	public void setBusyPsCt(Long busyPsCt) {
		this.busyPsCt = busyPsCt;
	}

	public Long getBusyPsDt() {
		return busyPsDt;
	}

	public void setBusyPsDt(Long busyPsDt) {
		this.busyPsDt = busyPsDt;
	}

	public String getBusyPsDtStr() {
		return busyPsDtStr;
	}

	public void setBusyPsDtStr(String busyPsDtStr) {
		this.busyPsDtStr = busyPsDtStr;
	}

	public Long getRestPsCt() {
		return restPsCt;
	}

	public void setRestPsCt(Long restPsCt) {
		this.restPsCt = restPsCt;
	}

	public Long getRestPsDt() {
		return restPsDt;
	}

	public void setRestPsDt(Long restPsDt) {
		this.restPsDt = restPsDt;
	}

	public String getRestPsDtStr() {
		return restPsDtStr;
	}

	public void setRestPsDtStr(String restPsDtStr) {
		this.restPsDtStr = restPsDtStr;
	}

	public Long getDinePsCt() {
		return dinePsCt;
	}

	public void setDinePsCt(Long dinePsCt) {
		this.dinePsCt = dinePsCt;
	}

	public Long getDinePsDt() {
		return dinePsDt;
	}

	public void setDinePsDt(Long dinePsDt) {
		this.dinePsDt = dinePsDt;
	}

	public String getDinePsDtStr() {
		return dinePsDtStr;
	}

	public void setDinePsDtStr(String dinePsDtStr) {
		this.dinePsDtStr = dinePsDtStr;
	}

	public Long getTrainPsCt() {
		return trainPsCt;
	}

	public void setTrainPsCt(Long trainPsCt) {
		this.trainPsCt = trainPsCt;
	}

	public Long getTrainPsDt() {
		return trainPsDt;
	}

	public void setTrainPsDt(Long trainPsDt) {
		this.trainPsDt = trainPsDt;
	}

	public String getTrainPsDtStr() {
		return trainPsDtStr;
	}

	public void setTrainPsDtStr(String trainPsDtStr) {
		this.trainPsDtStr = trainPsDtStr;
	}

	public Long getDialCt() {
		return dialCt;
	}

	public void setDialCt(Long dialCt) {
		this.dialCt = dialCt;
	}

	public Long getBridgeCt() {
		return bridgeCt;
	}

	public void setBridgeCt(Long bridgeCt) {
		this.bridgeCt = bridgeCt;
	}

	public Long getUnbridgeCt() {
		return unbridgeCt;
	}

	public void setUnbridgeCt(Long unbridgeCt) {
		this.unbridgeCt = unbridgeCt;
	}

	public Double getBridgeRt() {
		return bridgeRt;
	}

	public void setBridgeRt(Double bridgeRt) {
		this.bridgeRt = bridgeRt;
	}

	public Long getDialDt() {
		return dialDt;
	}

	public void setDialDt(Long dialDt) {
		this.dialDt = dialDt;
	}

	public String getDialDtStr() {
		return dialDtStr;
	}

	public void setDialDtStr(String dialDtStr) {
		this.dialDtStr = dialDtStr;
	}

	public Long getDialBgDt() {
		return dialBgDt;
	}

	public void setDialBgDt(Long dialBgDt) {
		this.dialBgDt = dialBgDt;
	}

	public String getDialBgDtStr() {
		return dialBgDtStr;
	}

	public void setDialBgDtStr(String dialBgDtStr) {
		this.dialBgDtStr = dialBgDtStr;
	}

	public Double getEffectDialRt() {
		return effectDialRt;
	}

	public void setEffectDialRt(Double effectDialRt) {
		this.effectDialRt = effectDialRt;
	}

	public Double getPerHourDialCt() {
		return perHourDialCt;
	}

	public void setPerHourDialCt(Double perHourDialCt) {
		this.perHourDialCt = perHourDialCt;
	}

	public Long getOutDialCt() {
		return outDialCt;
	}

	public void setOutDialCt(Long outDialCt) {
		this.outDialCt = outDialCt;
	}

	public Long getOutBgCt() {
		return outBgCt;
	}

	public void setOutBgCt(Long outBgCt) {
		this.outBgCt = outBgCt;
	}

	public Long getOutUnbgCt() {
		return outUnbgCt;
	}

	public void setOutUnbgCt(Long outUnbgCt) {
		this.outUnbgCt = outUnbgCt;
	}

	public Double getOutBgRt() {
		return outBgRt;
	}

	public void setOutBgRt(Double outBgRt) {
		this.outBgRt = outBgRt;
	}

	public Long getOutBgLowSecCt() {
		return outBgLowSecCt;
	}

	public void setOutBgLowSecCt(Long outBgLowSecCt) {
		this.outBgLowSecCt = outBgLowSecCt;
	}

	public Double getOutBgLowSecRt() {
		return outBgLowSecRt;
	}

	public void setOutBgLowSecRt(Double outBgLowSecRt) {
		this.outBgLowSecRt = outBgLowSecRt;
	}

	public Long getOutBgLowSecCt2() {
		return outBgLowSecCt2;
	}

	public void setOutBgLowSecCt2(Long outBgLowSecCt2) {
		this.outBgLowSecCt2 = outBgLowSecCt2;
	}

	public Double getOutBgLowSecRt2() {
		return outBgLowSecRt2;
	}

	public void setOutBgLowSecRt2(Double outBgLowSecRt2) {
		this.outBgLowSecRt2 = outBgLowSecRt2;
	}

	public Long getOutBgLargeSecCt() {
		return outBgLargeSecCt;
	}

	public void setOutBgLargeSecCt(Long outBgLargeSecCt) {
		this.outBgLargeSecCt = outBgLargeSecCt;
	}

	public Double getOutBgLargeSecRt() {
		return outBgLargeSecRt;
	}

	public void setOutBgLargeSecRt(Double outBgLargeSecRt) {
		this.outBgLargeSecRt = outBgLargeSecRt;
	}

	public Long getOutBgLargeSecCt2() {
		return outBgLargeSecCt2;
	}

	public void setOutBgLargeSecCt2(Long outBgLargeSecCt2) {
		this.outBgLargeSecCt2 = outBgLargeSecCt2;
	}

	public Double getOutBgLargeSecRt2() {
		return outBgLargeSecRt2;
	}

	public void setOutBgLargeSecRt2(Double outBgLargeSecRt2) {
		this.outBgLargeSecRt2 = outBgLargeSecRt2;
	}

	public Long getOutMaxBgDt() {
		return outMaxBgDt;
	}

	public void setOutMaxBgDt(Long outMaxBgDt) {
		this.outMaxBgDt = outMaxBgDt;
	}

	public String getOutMaxBgDtStr() {
		return outMaxBgDtStr;
	}

	public void setOutMaxBgDtStr(String outMaxBgDtStr) {
		this.outMaxBgDtStr = outMaxBgDtStr;
	}

	public Long getOutDialDt() {
		return outDialDt;
	}

	public void setOutDialDt(Long outDialDt) {
		this.outDialDt = outDialDt;
	}

	public String getOutDialDtStr() {
		return outDialDtStr;
	}

	public void setOutDialDtStr(String outDialDtStr) {
		this.outDialDtStr = outDialDtStr;
	}

	public Long getOutRingDt() {
		return outRingDt;
	}

	public void setOutRingDt(Long outRingDt) {
		this.outRingDt = outRingDt;
	}

	public String getOutRingDtStr() {
		return outRingDtStr;
	}

	public void setOutRingDtStr(String outRingDtStr) {
		this.outRingDtStr = outRingDtStr;
	}

	public Double getOutAvgRingDt() {
		return outAvgRingDt;
	}

	public void setOutAvgRingDt(Double outAvgRingDt) {
		this.outAvgRingDt = outAvgRingDt;
	}

	public String getOutAvgRingDtStr() {
		return outAvgRingDtStr;
	}

	public void setOutAvgRingDtStr(String outAvgRingDtStr) {
		this.outAvgRingDtStr = outAvgRingDtStr;
	}

	public Long getOutBgDt() {
		return outBgDt;
	}

	public void setOutBgDt(Long outBgDt) {
		this.outBgDt = outBgDt;
	}

	public String getOutBgDtStr() {
		return outBgDtStr;
	}

	public void setOutBgDtStr(String outBgDtStr) {
		this.outBgDtStr = outBgDtStr;
	}

	public Double getOutAvgBgDt() {
		return outAvgBgDt;
	}

	public void setOutAvgBgDt(Double outAvgBgDt) {
		this.outAvgBgDt = outAvgBgDt;
	}

	public String getOutAvgBgDtStr() {
		return outAvgBgDtStr;
	}

	public void setOutAvgBgDtStr(String outAvgBgDtStr) {
		this.outAvgBgDtStr = outAvgBgDtStr;
	}

	public Double getOutEffectDtRt() {
		return outEffectDtRt;
	}

	public void setOutEffectDtRt(Double outEffectDtRt) {
		this.outEffectDtRt = outEffectDtRt;
	}

	public Long getOutAfterCt() {
		return outAfterCt;
	}

	public void setOutAfterCt(Long outAfterCt) {
		this.outAfterCt = outAfterCt;
	}

	public Long getOutAfterDt() {
		return outAfterDt;
	}

	public void setOutAfterDt(Long outAfterDt) {
		this.outAfterDt = outAfterDt;
	}

	public String getOutAfterDtStr() {
		return outAfterDtStr;
	}

	public void setOutAfterDtStr(String outAfterDtStr) {
		this.outAfterDtStr = outAfterDtStr;
	}

	public Double getOutPerHourCt() {
		return outPerHourCt;
	}

	public void setOutPerHourCt(Double outPerHourCt) {
		this.outPerHourCt = outPerHourCt;
	}

	public Long getInnerDialCt() {
		return innerDialCt;
	}

	public void setInnerDialCt(Long innerDialCt) {
		this.innerDialCt = innerDialCt;
	}

	public Long getInnerBgCt() {
		return innerBgCt;
	}

	public void setInnerBgCt(Long innerBgCt) {
		this.innerBgCt = innerBgCt;
	}

	public Long getInnerUnbgCt() {
		return innerUnbgCt;
	}

	public void setInnerUnbgCt(Long innerUnbgCt) {
		this.innerUnbgCt = innerUnbgCt;
	}

	public Double getInnerBgRt() {
		return innerBgRt;
	}

	public void setInnerBgRt(Double innerBgRt) {
		this.innerBgRt = innerBgRt;
	}

	public Long getInnerDialDt() {
		return innerDialDt;
	}

	public void setInnerDialDt(Long innerDialDt) {
		this.innerDialDt = innerDialDt;
	}

	public String getInnerDialDtStr() {
		return innerDialDtStr;
	}

	public void setInnerDialDtStr(String innerDialDtStr) {
		this.innerDialDtStr = innerDialDtStr;
	}

	public Long getInnerBgDt() {
		return innerBgDt;
	}

	public void setInnerBgDt(Long innerBgDt) {
		this.innerBgDt = innerBgDt;
	}

	public String getInnerBgDtStr() {
		return innerBgDtStr;
	}

	public void setInnerBgDtStr(String innerBgDtStr) {
		this.innerBgDtStr = innerBgDtStr;
	}

	public Long getInnerMaxBgDt() {
		return innerMaxBgDt;
	}

	public void setInnerMaxBgDt(Long innerMaxBgDt) {
		this.innerMaxBgDt = innerMaxBgDt;
	}

	public String getInnerMaxBgDtStr() {
		return innerMaxBgDtStr;
	}

	public void setInnerMaxBgDtStr(String innerMaxBgDtStr) {
		this.innerMaxBgDtStr = innerMaxBgDtStr;
	}

	public Double getInnerPerHourCt() {
		return innerPerHourCt;
	}

	public void setInnerPerHourCt(Double innerPerHourCt) {
		this.innerPerHourCt = innerPerHourCt;
	}

	public Long getInDialCt() {
		return inDialCt;
	}

	public void setInDialCt(Long inDialCt) {
		this.inDialCt = inDialCt;
	}

	public Long getInBgCt() {
		return inBgCt;
	}

	public void setInBgCt(Long inBgCt) {
		this.inBgCt = inBgCt;
	}

	public Long getInUnbgCt() {
		return inUnbgCt;
	}

	public void setInUnbgCt(Long inUnbgCt) {
		this.inUnbgCt = inUnbgCt;
	}

	public Double getInBgRt() {
		return inBgRt;
	}

	public void setInBgRt(Double inBgRt) {
		this.inBgRt = inBgRt;
	}

	public Long getInMissCt() {
		return inMissCt;
	}

	public void setInMissCt(Long inMissCt) {
		this.inMissCt = inMissCt;
	}

	public Long getInBgLevelCt() {
		return inBgLevelCt;
	}

	public void setInBgLevelCt(Long inBgLevelCt) {
		this.inBgLevelCt = inBgLevelCt;
	}

	public Double getInLevelRt() {
		return inLevelRt;
	}

	public void setInLevelRt(Double inLevelRt) {
		this.inLevelRt = inLevelRt;
	}

	public Long getInBgLevelCt2() {
		return inBgLevelCt2;
	}

	public void setInBgLevelCt2(Long inBgLevelCt2) {
		this.inBgLevelCt2 = inBgLevelCt2;
	}

	public Double getInLevelRt2() {
		return inLevelRt2;
	}

	public void setInLevelRt2(Double inLevelRt2) {
		this.inLevelRt2 = inLevelRt2;
	}

	public Long getInBgLowSecCt() {
		return inBgLowSecCt;
	}

	public void setInBgLowSecCt(Long inBgLowSecCt) {
		this.inBgLowSecCt = inBgLowSecCt;
	}

	public Double getInBgLowSecRt() {
		return inBgLowSecRt;
	}

	public void setInBgLowSecRt(Double inBgLowSecRt) {
		this.inBgLowSecRt = inBgLowSecRt;
	}

	public Long getInBgLowSecCt2() {
		return inBgLowSecCt2;
	}

	public void setInBgLowSecCt2(Long inBgLowSecCt2) {
		this.inBgLowSecCt2 = inBgLowSecCt2;
	}

	public Double getInBgLowSecRt2() {
		return inBgLowSecRt2;
	}

	public void setInBgLowSecRt2(Double inBgLowSecRt2) {
		this.inBgLowSecRt2 = inBgLowSecRt2;
	}

	public Long getInBgLargeSecCt() {
		return inBgLargeSecCt;
	}

	public void setInBgLargeSecCt(Long inBgLargeSecCt) {
		this.inBgLargeSecCt = inBgLargeSecCt;
	}

	public Double getInBgLargeSecRt() {
		return inBgLargeSecRt;
	}

	public void setInBgLargeSecRt(Double inBgLargeSecRt) {
		this.inBgLargeSecRt = inBgLargeSecRt;
	}

	public Long getInBgLargeSecCt2() {
		return inBgLargeSecCt2;
	}

	public void setInBgLargeSecCt2(Long inBgLargeSecCt2) {
		this.inBgLargeSecCt2 = inBgLargeSecCt2;
	}

	public Double getInBgLargeSecRt2() {
		return inBgLargeSecRt2;
	}

	public void setInBgLargeSecRt2(Double inBgLargeSecRt2) {
		this.inBgLargeSecRt2 = inBgLargeSecRt2;
	}

	public Long getInMaxBgDt() {
		return inMaxBgDt;
	}

	public void setInMaxBgDt(Long inMaxBgDt) {
		this.inMaxBgDt = inMaxBgDt;
	}

	public String getInMaxBgDtStr() {
		return inMaxBgDtStr;
	}

	public void setInMaxBgDtStr(String inMaxBgDtStr) {
		this.inMaxBgDtStr = inMaxBgDtStr;
	}

	public Long getInDialDt() {
		return inDialDt;
	}

	public void setInDialDt(Long inDialDt) {
		this.inDialDt = inDialDt;
	}

	public String getInDialDtStr() {
		return inDialDtStr;
	}

	public void setInDialDtStr(String inDialDtStr) {
		this.inDialDtStr = inDialDtStr;
	}

	public Long getInRingDt() {
		return inRingDt;
	}

	public void setInRingDt(Long inRingDt) {
		this.inRingDt = inRingDt;
	}

	public String getInRingDtStr() {
		return inRingDtStr;
	}

	public void setInRingDtStr(String inRingDtStr) {
		this.inRingDtStr = inRingDtStr;
	}

	public Double getInAvgRingDt() {
		return inAvgRingDt;
	}

	public void setInAvgRingDt(Double inAvgRingDt) {
		this.inAvgRingDt = inAvgRingDt;
	}

	public String getInAvgRingDtStr() {
		return inAvgRingDtStr;
	}

	public void setInAvgRingDtStr(String inAvgRingDtStr) {
		this.inAvgRingDtStr = inAvgRingDtStr;
	}

	public Long getInBgDt() {
		return inBgDt;
	}

	public void setInBgDt(Long inBgDt) {
		this.inBgDt = inBgDt;
	}

	public String getInBgDtStr() {
		return inBgDtStr;
	}

	public void setInBgDtStr(String inBgDtStr) {
		this.inBgDtStr = inBgDtStr;
	}

	public Double getInAvgBgDt() {
		return inAvgBgDt;
	}

	public void setInAvgBgDt(Double inAvgBgDt) {
		this.inAvgBgDt = inAvgBgDt;
	}

	public String getInAvgBgDtStr() {
		return inAvgBgDtStr;
	}

	public void setInAvgBgDtStr(String inAvgBgDtStr) {
		this.inAvgBgDtStr = inAvgBgDtStr;
	}

	public Double getInEffectDtRt() {
		return inEffectDtRt;
	}

	public void setInEffectDtRt(Double inEffectDtRt) {
		this.inEffectDtRt = inEffectDtRt;
	}

	public Long getInAfterCt() {
		return inAfterCt;
	}

	public void setInAfterCt(Long inAfterCt) {
		this.inAfterCt = inAfterCt;
	}

	public Long getInAfterDt() {
		return inAfterDt;
	}

	public void setInAfterDt(Long inAfterDt) {
		this.inAfterDt = inAfterDt;
	}

	public String getInAfterDtStr() {
		return inAfterDtStr;
	}

	public void setInAfterDtStr(String inAfterDtStr) {
		this.inAfterDtStr = inAfterDtStr;
	}

	public Double getInPerHourCt() {
		return inPerHourCt;
	}

	public void setInPerHourCt(Double inPerHourCt) {
		this.inPerHourCt = inPerHourCt;
	}

	public Long getOutBgLowSecCt1() {
		return outBgLowSecCt1;
	}

	public void setOutBgLowSecCt1(Long outBgLowSecCt1) {
		this.outBgLowSecCt1 = outBgLowSecCt1;
	}

	public Double getOutBgLowSecRt1() {
		return outBgLowSecRt1;
	}

	public void setOutBgLowSecRt1(Double outBgLowSecRt1) {
		this.outBgLowSecRt1 = outBgLowSecRt1;
	}

	public Long getOutBgLargeSecCt1() {
		return outBgLargeSecCt1;
	}

	public void setOutBgLargeSecCt1(Long outBgLargeSecCt1) {
		this.outBgLargeSecCt1 = outBgLargeSecCt1;
	}

	public Double getOutBgLargeSecRt1() {
		return outBgLargeSecRt1;
	}

	public void setOutBgLargeSecRt1(Double outBgLargeSecRt1) {
		this.outBgLargeSecRt1 = outBgLargeSecRt1;
	}

	public Long getOutBgLargeSecCt3() {
		return outBgLargeSecCt3;
	}

	public void setOutBgLargeSecCt3(Long outBgLargeSecCt3) {
		this.outBgLargeSecCt3 = outBgLargeSecCt3;
	}

	public Double getOutBgLargeSecRt3() {
		return outBgLargeSecRt3;
	}

	public void setOutBgLargeSecRt3(Double outBgLargeSecRt3) {
		this.outBgLargeSecRt3 = outBgLargeSecRt3;
	}

	public Long getOutBgLargeSecCt4() {
		return outBgLargeSecCt4;
	}

	public void setOutBgLargeSecCt4(Long outBgLargeSecCt4) {
		this.outBgLargeSecCt4 = outBgLargeSecCt4;
	}

	public Double getOutBgLargeSecRt4() {
		return outBgLargeSecRt4;
	}

	public void setOutBgLargeSecRt4(Double outBgLargeSecRt4) {
		this.outBgLargeSecRt4 = outBgLargeSecRt4;
	}

	public Long getOutBgLargeSecCt5() {
		return outBgLargeSecCt5;
	}

	public void setOutBgLargeSecCt5(Long outBgLargeSecCt5) {
		this.outBgLargeSecCt5 = outBgLargeSecCt5;
	}

	public Double getOutBgLargeSecRt5() {
		return outBgLargeSecRt5;
	}

	public void setOutBgLargeSecRt5(Double outBgLargeSecRt5) {
		this.outBgLargeSecRt5 = outBgLargeSecRt5;
	}

	public Long getOutBgLargeSecCt6() {
		return outBgLargeSecCt6;
	}

	public void setOutBgLargeSecCt6(Long outBgLargeSecCt6) {
		this.outBgLargeSecCt6 = outBgLargeSecCt6;
	}

	public Double getOutBgLargeSecRt6() {
		return outBgLargeSecRt6;
	}

	public void setOutBgLargeSecRt6(Double outBgLargeSecRt6) {
		this.outBgLargeSecRt6 = outBgLargeSecRt6;
	}

	public Long getOutBgLargeSecCt7() {
		return outBgLargeSecCt7;
	}

	public void setOutBgLargeSecCt7(Long outBgLargeSecCt7) {
		this.outBgLargeSecCt7 = outBgLargeSecCt7;
	}

	public Double getOutBgLargeSecRt7() {
		return outBgLargeSecRt7;
	}

	public void setOutBgLargeSecRt7(Double outBgLargeSecRt7) {
		this.outBgLargeSecRt7 = outBgLargeSecRt7;
	}

	public Long getInBgLowSecCt1() {
		return inBgLowSecCt1;
	}

	public void setInBgLowSecCt1(Long inBgLowSecCt1) {
		this.inBgLowSecCt1 = inBgLowSecCt1;
	}

	public Double getInBgLowSecRt1() {
		return inBgLowSecRt1;
	}

	public void setInBgLowSecRt1(Double inBgLowSecRt1) {
		this.inBgLowSecRt1 = inBgLowSecRt1;
	}

	public Long getInBgLargeSecCt1() {
		return inBgLargeSecCt1;
	}

	public void setInBgLargeSecCt1(Long inBgLargeSecCt1) {
		this.inBgLargeSecCt1 = inBgLargeSecCt1;
	}

	public Double getInBgLargeSecRt1() {
		return inBgLargeSecRt1;
	}

	public void setInBgLargeSecRt1(Double inBgLargeSecRt1) {
		this.inBgLargeSecRt1 = inBgLargeSecRt1;
	}

	public Long getInBgLargeSecCt3() {
		return inBgLargeSecCt3;
	}

	public void setInBgLargeSecCt3(Long inBgLargeSecCt3) {
		this.inBgLargeSecCt3 = inBgLargeSecCt3;
	}

	public Double getInBgLargeSecRt3() {
		return inBgLargeSecRt3;
	}

	public void setInBgLargeSecRt3(Double inBgLargeSecRt3) {
		this.inBgLargeSecRt3 = inBgLargeSecRt3;
	}

	public Long getInBgLargeSecCt4() {
		return inBgLargeSecCt4;
	}

	public void setInBgLargeSecCt4(Long inBgLargeSecCt4) {
		this.inBgLargeSecCt4 = inBgLargeSecCt4;
	}

	public Double getInBgLargeSecRt4() {
		return inBgLargeSecRt4;
	}

	public void setInBgLargeSecRt4(Double inBgLargeSecRt4) {
		this.inBgLargeSecRt4 = inBgLargeSecRt4;
	}

	public Long getInBgLargeSecCt5() {
		return inBgLargeSecCt5;
	}

	public void setInBgLargeSecCt5(Long inBgLargeSecCt5) {
		this.inBgLargeSecCt5 = inBgLargeSecCt5;
	}

	public Double getInBgLargeSecRt5() {
		return inBgLargeSecRt5;
	}

	public void setInBgLargeSecRt5(Double inBgLargeSecRt5) {
		this.inBgLargeSecRt5 = inBgLargeSecRt5;
	}

	public Long getInBgLargeSecCt6() {
		return inBgLargeSecCt6;
	}

	public void setInBgLargeSecCt6(Long inBgLargeSecCt6) {
		this.inBgLargeSecCt6 = inBgLargeSecCt6;
	}

	public Double getInBgLargeSecRt6() {
		return inBgLargeSecRt6;
	}

	public void setInBgLargeSecRt6(Double inBgLargeSecRt6) {
		this.inBgLargeSecRt6 = inBgLargeSecRt6;
	}

	public Long getInBgLargeSecCt7() {
		return inBgLargeSecCt7;
	}

	public void setInBgLargeSecCt7(Long inBgLargeSecCt7) {
		this.inBgLargeSecCt7 = inBgLargeSecCt7;
	}

	public Double getInBgLargeSecRt7() {
		return inBgLargeSecRt7;
	}

	public void setInBgLargeSecRt7(Double inBgLargeSecRt7) {
		this.inBgLargeSecRt7 = inBgLargeSecRt7;
	}
	
	
	
}
