package com.jiangyifen.ec2.report.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;



/**
 * 话务统计总概览中间表
 *
 * @author jrh
 *  2013-9-4
 */
@Entity
@Table(name = "ec2_report_call_statistic_overview")
public class CallStatisticOverview {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_call_statistic_overview")
	@SequenceGenerator(name = "report_call_statistic_overview", sequenceName = "seq_ec2_report_call_statistic_overview_id", allocationSize = 1)
	private Long id;

	// cdr的开始日期
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	private Date startDate;	// yyyy-MM-dd 2013-09-05
	
	// cdr的开始小时
	@Column(columnDefinition="integer NOT NULL", nullable = false)
	private Integer hour;
	
	// cdr的结束日期
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date endDate;	// yyyy-MM-dd HH 2013-09-05 13
	
	// 呼叫总数
	@Column(columnDefinition="bigint")
	private Long callAmount  = 0L;
	
	// 呼入总数
	@Column(columnDefinition="bigint")
	private Long callInAmount  = 0L;
	
	// 呼出总数
	@Column(columnDefinition="bigint")
	private Long callOutAmount  = 0L;
	
	// 总的接通数
	@Column(columnDefinition="bigint")
	private Long bridgedAmount  = 0L;
	
	// 呼入接通数
	@Column(columnDefinition="bigint")
	private Long bridgedInAmount  = 0L;
	
	// 呼出接通数
	@Column(columnDefinition="bigint")
	private Long bridgedOutAmount = 0L;

	// 无论呼入呼出，在指定时间内接起数
	@Column(columnDefinition="bigint")
	private Long pickupInSpecifiedSec = 0L;
	
	// 无论呼入呼出，在指定时间内放弃数
	@Column(columnDefinition="bigint")
	private Long abandonInSpecifiedSec = 0L;
	
	// 呼入 坐席 在指定时间内(如20s)接通数
	@Column(columnDefinition="bigint")
	private Long csrPickupInSpecifiedSec = 0L;
	
	// 呼入 客户 在指定时间内(如20s)放弃数
	@Column(columnDefinition="bigint")
	private Long customerAbandonInSpecifiedSec = 0L;
	
	// 呼出 客户 在指定时间内(如30s)接通数
	@Column(columnDefinition="bigint")
	private Long customerPickupInSpecifiedSec = 0L;
	
	// 呼出 坐席 在指定时间内(如30s)放弃数
	@Column(columnDefinition="bigint")
	private Long csrAbandonInSpecifiedSec = 0L;
	
	// 总的总通时
	@Column(columnDefinition="bigint")
	private Long totalCallTime = 0L;
	
	// 呼入总通时
	@Column(columnDefinition="bigint")
	private Long totalInCallTime = 0L;
	
	// 呼出总通时
	@Column(columnDefinition="bigint")
	private Long totalOutCallTime = 0L;
	
	// 平均通时
	@Column(precision=12, scale=2)
	private Double avgCallTime = 0.00;
	
	// 呼入平均通时
	@Column
	private Double avgInCallTime = 0.00;
	
	// 呼出平均通时
	@Column
	private Double avgOutCallTime = 0.00;

	// 总的应答时间
	@Column(columnDefinition="bigint")
	private Long totalAnswerTime = 0L;

	// 呼入应答总时间
	@Column(columnDefinition="bigint")
	private Long totalInAnswerTime = 0L;

	// 呼出应答总时间
	@Column(columnDefinition="bigint")
	private Long totalOutAnswerTime = 0L;
	
	// 平均应答时长
	@Column
	private Double avgAnswerTime = 0.00;
	
	// 呼入平均应答时长
	@Column
	private Double avgInAnswerTime = 0.00;
	
	// 呼出平均应答时长
	@Column
	private Double avgOutAnswerTime = 0.00;
	
	// 话后处理时长
	@Column(columnDefinition="bigint")
	private Long totalAfterCallWorkTime = 0L;
	
	// 呼入话后处理时长
	@Column(columnDefinition="bigint")
	private Long totalInAfterCallWorkTime = 0L;
	
	// 呼出话后处理时长
	@Column(columnDefinition="bigint")
	private Long totalOutAfterCallWorkTime = 0L;
	
	//  平均话后处理时长
	@Column
	private Double avgAfterCallWorkTime = 0.00;
	
	// 呼入平均话后处理时长
	@Column
	private Double avgInAfterCallWorkTime = 0.00;
	
	// 呼出平均话后处理时长
	@Column
	private Double avgOutAfterCallWorkTime = 0.00;
	
	// 接通率
	@Column
	private Double bridgedRate = 0.0000;
	
	// 呼入接通率
	@Column
	private Double bridgedInRate = 0.0000;

	// 呼出接通率
	@Column
	private Double bridgedOutRate = 0.0000;
	
	// 呼入服务水平
	@Column
	private Double incomingServiceLevel = 0.0000;

	// 所属域的编号
	@Column(columnDefinition="bigint", nullable = false)
	private Long domainId;

    /** 获取呼叫统计总览编号 */
    public Long getId() {
          return id;
    }

    /** 设置呼叫统计总览编号 */
    public void setId(Long id) {
          this.id = id;
    }

    /** 获取呼叫记录创建日期，格式：yyyy-MM-dd 2013-09-05 */
    public Date getStartDate() {
		return startDate;
	}

    /** 设置呼叫记录创建日期 ，格式：yyyy-MM-dd 2013-09-05 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/** 获取当前已经统计到的时间点，格式：yyyy-MM-dd HH 2013-09-05 13 */
	public Date getEndDate() {
		return endDate;
	}

	/** 获取当前已经统计到的时间点，格式：yyyy-MM-dd HH 2013-09-05 13 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/** 获取呼叫记录创建小时 */
    public Integer getHour() {
          return hour;
    }

    /** 设置呼叫记录创建小时 */
    public void setHour(Integer hour) {
          this.hour = hour;
    }

    /** 获取呼叫总数 */
    public Long getCallAmount() {
          return callAmount;
    }

    /** 设置呼叫总数 */
    public void setCallAmount(Long callAmount) {
          this.callAmount = callAmount;
    }

    /** 获取呼入总数 */
    public Long getCallInAmount() {
          return callInAmount;
    }

    /** 设置呼入总数 */
    public void setCallInAmount(Long callInAmount) {
          this.callInAmount = callInAmount;
    }

    /** 获取呼出总数 */
    public Long getCallOutAmount() {
          return callOutAmount;
    }

    /** 设置呼出总数 */
    public void setCallOutAmount(Long callOutAmount) {
          this.callOutAmount = callOutAmount;
    }

    /** 获取总的接通数 */
    public Long getBridgedAmount() {
          return bridgedAmount;
    }

    /** 设置总的接通数 */
    public void setBridgedAmount(Long bridgedAmount) {
          this.bridgedAmount = bridgedAmount;
    }

    /** 获取呼入接通数 */
    public Long getBridgedInAmount() {
          return bridgedInAmount;
    }

    /** 设置呼入接通数 */
    public void setBridgedInAmount(Long bridgedInAmount) {
          this.bridgedInAmount = bridgedInAmount;
    }

    /** 获取呼出接通数 */
    public Long getBridgedOutAmount() {
          return bridgedOutAmount;
    }

    /** 设置呼出接通数 */
    public void setBridgedOutAmount(Long bridgedOutAmount) {
          this.bridgedOutAmount = bridgedOutAmount;
    }

    /** 获取 无论呼入呼出，在指定时间内接起数 */
    public Long getPickupInSpecifiedSec() {
		return pickupInSpecifiedSec;
	}

    /** 设置 无论呼入呼出，在指定时间内接起数 */
	public void setPickupInSpecifiedSec(Long pickupInSpecifiedSec) {
		this.pickupInSpecifiedSec = pickupInSpecifiedSec;
	}

	/** 获取 无论呼入呼出，在指定时间内放弃数 */
	public Long getAbandonInSpecifiedSec() {
		return abandonInSpecifiedSec;
	}

	/** 设置 无论呼入呼出，在指定时间内放弃数 */
	public void setAbandonInSpecifiedSec(Long abandonInSpecifiedSec) {
		this.abandonInSpecifiedSec = abandonInSpecifiedSec;
	}

	/** 获取呼入 坐席 在指定时间内(如20s)接通数 */
    public Long getCsrPickupInSpecifiedSec() {
          return csrPickupInSpecifiedSec;
    }

    /** 设置呼入 坐席 在指定时间内(如20s)接通数 */
    public void setCsrPickupInSpecifiedSec(Long csrPickupInSpecifiedSec) {
          this.csrPickupInSpecifiedSec = csrPickupInSpecifiedSec;
    }

    /** 获取呼入 客户 在指定时间内(如20s)放弃数 */
    public Long getCustomerAbandonInSpecifiedSec() {
          return customerAbandonInSpecifiedSec;
    }

    /** 设置呼入 客户 在指定时间内(如20s)放弃数 */
    public void setCustomerAbandonInSpecifiedSec(Long customerAbandonInSpecifiedSec) {
          this.customerAbandonInSpecifiedSec = customerAbandonInSpecifiedSec;
    }

    /** 获取呼出 客户 在指定时间内(如30s)接通数 */
    public Long getCustomerPickupInSpecifiedSec() {
          return customerPickupInSpecifiedSec;
    }

    /** 设置呼出 客户 在指定时间内(如30s)接通数 */
    public void setCustomerPickupInSpecifiedSec(Long customerPickupInSpecifiedSec) {
          this.customerPickupInSpecifiedSec = customerPickupInSpecifiedSec;
    }

    /** 获取呼出 坐席 在指定时间内(如30s)放弃数 */
    public Long getCsrAbandonInSpecifiedSec() {
          return csrAbandonInSpecifiedSec;
    }

    /** 设置呼出 坐席 在指定时间内(如30s)放弃数 */
    public void setCsrAbandonInSpecifiedSec(Long csrAbandonInSpecifiedSec) {
          this.csrAbandonInSpecifiedSec = csrAbandonInSpecifiedSec;
    }

    /** 获取总的总通时 */
    public Long getTotalCallTime() {
          return totalCallTime;
    }

    /** 设置总的总通时 */
    public void setTotalCallTime(Long totalCallTime) {
          this.totalCallTime = totalCallTime;
    }

    /** 获取呼入总通时 */
    public Long getTotalInCallTime() {
          return totalInCallTime;
    }

    /** 设置呼入总通时 */
    public void setTotalInCallTime(Long totalInCallTime) {
          this.totalInCallTime = totalInCallTime;
    }

    /** 获取呼出总通时 */
    public Long getTotalOutCallTime() {
          return totalOutCallTime;
    }

    /** 设置呼出总通时 */
    public void setTotalOutCallTime(Long totalOutCallTime) {
          this.totalOutCallTime = totalOutCallTime;
    }

    /** 获取平均通时 */
    public Double getAvgCallTime() {
          return avgCallTime;
    }

    /** 设置平均通时 */
    public void setAvgCallTime(Double avgCallTime) {
          this.avgCallTime = avgCallTime;
    }

    /** 获取呼入平均通时 */
    public Double getAvgInCallTime() {
          return avgInCallTime;
    }

    /** 设置呼入平均通时 */
    public void setAvgInCallTime(Double avgInCallTime) {
          this.avgInCallTime = avgInCallTime;
    }

    /** 获取呼出平均通时 */
    public Double getAvgOutCallTime() {
          return avgOutCallTime;
    }

    /** 设置呼出平均通时 */
    public void setAvgOutCallTime(Double avgOutCallTime) {
          this.avgOutCallTime = avgOutCallTime;
    }

    /** 获取 总的应答时间 */
    public Long getTotalAnswerTime() {
		return totalAnswerTime;
	}

    /** 设置 总的应答时间 */
	public void setTotalAnswerTime(Long totalAnswerTime) {
		this.totalAnswerTime = totalAnswerTime;
	}

	/** 获取 呼入应答总耗时 */
	public Long getTotalInAnswerTime() {
		return totalInAnswerTime;
	}

	/** 设置 呼入应答总耗时 */
	public void setTotalInAnswerTime(Long totalInAnswerTime) {
		this.totalInAnswerTime = totalInAnswerTime;
	}

	/** 获取 呼出应答总耗时 */
	public Long getTotalOutAnswerTime() {
		return totalOutAnswerTime;
	}

	/** 设置 呼出应答总耗时 */
	public void setTotalOutAnswerTime(Long totalOutAnswerTime) {
		this.totalOutAnswerTime = totalOutAnswerTime;
	}

	/** 获取平均应答时长 */
    public Double getAvgAnswerTime() {
          return avgAnswerTime;
    }

    /** 设置平均应答时长 */
    public void setAvgAnswerTime(Double avgAnswerTime) {
          this.avgAnswerTime = avgAnswerTime;
    }

    /** 获取呼入平均应答时长 */
    public Double getAvgInAnswerTime() {
          return avgInAnswerTime;
    }

    /** 设置呼入平均应答时长 */
    public void setAvgInAnswerTime(Double avgInAnswerTime) {
          this.avgInAnswerTime = avgInAnswerTime;
    }

    /** 获取呼出平均应答时长 */
    public Double getAvgOutAnswerTime() {
          return avgOutAnswerTime;
    }

    /** 设置呼出平均应答时长 */
    public void setAvgOutAnswerTime(Double avgOutAnswerTime) {
          this.avgOutAnswerTime = avgOutAnswerTime;
    }

    /** 获取话后处理时长 */
    public Long getTotalAfterCallWorkTime() {
          return totalAfterCallWorkTime;
    }

    /** 设置话后处理时长 */
    public void setTotalAfterCallWorkTime(Long totalAfterCallWorkTime) {
          this.totalAfterCallWorkTime = totalAfterCallWorkTime;
    }

    /** 获取呼入话后处理时长 */
    public Long getTotalInAfterCallWorkTime() {
          return totalInAfterCallWorkTime;
    }

    /** 设置呼入话后处理时长 */
    public void setTotalInAfterCallWorkTime(Long totalInAfterCallWorkTime) {
          this.totalInAfterCallWorkTime = totalInAfterCallWorkTime;
    }

    /** 获取呼出话后处理时长 */
    public Long getTotalOutAfterCallWorkTime() {
          return totalOutAfterCallWorkTime;
    }

    /** 设置呼出话后处理时长 */
    public void setTotalOutAfterCallWorkTime(Long totalOutAfterCallWorkTime) {
          this.totalOutAfterCallWorkTime = totalOutAfterCallWorkTime;
    }

    /** 获取平均话后处理时长 */
    public Double getAvgAfterCallWorkTime() {
          return avgAfterCallWorkTime;
    }

    /** 设置平均话后处理时长 */
    public void setAvgAfterCallWorkTime(Double avgAfterCallWorkTime) {
          this.avgAfterCallWorkTime = avgAfterCallWorkTime;
    }

    /** 获取 呼入平均话后处理时长 */
    public Double getAvgInAfterCallWorkTime() {
          return avgInAfterCallWorkTime;
    }

    /** 设置 呼入平均话后处理时长 */
    public void setAvgInAfterCallWorkTime(Double avgInAfterCallWorkTime) {
          this.avgInAfterCallWorkTime = avgInAfterCallWorkTime;
    }

    /** 获取 呼出平均话后处理时长 */
    public Double getAvgOutAfterCallWorkTime() {
          return avgOutAfterCallWorkTime;
    }

    /** 设置 呼出平均话后处理时长 */
    public void setAvgOutAfterCallWorkTime(Double avgOutAfterCallWorkTime) {
          this.avgOutAfterCallWorkTime = avgOutAfterCallWorkTime;
    }

    /** 获取 接通率 */
    public Double getBridgedRate() {
        return bridgedRate;
    }

    /** 设置 接通率 */
    public void setBridgedRate(Double bridgedRate) {
        this.bridgedRate = bridgedRate;
    }

    /** 获取 呼入接通率 */
    public Double getBridgedInRate() {
    	return bridgedInRate;
    }

    /** 设置 呼入接通率 */
    public void setBridgedInRate(Double bridgedInRate) {
        this.bridgedInRate = bridgedInRate;
    }

    /** 获取 呼出接通率 */
    public Double getBridgedOutRate() {
        return bridgedOutRate;
    }

    /** 设置 呼出接通率 */
    public void setBridgedOutRate(Double bridgedOutRate) {
          this.bridgedOutRate = bridgedOutRate;
    }

    /** 获取 呼入服务水平 */
    public Double getIncomingServiceLevel() {
    	return incomingServiceLevel;
    }

    /** 设置 获取 呼入服务水平 */
    public void setIncomingServiceLevel(Double incomingServiceLevel) {
          this.incomingServiceLevel = incomingServiceLevel;
    }

    /** 获取 所属域的编号 */
	public Long getDomainId() {
		return domainId;
	}

	/** 设置 所属域的编号 */
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "CallStatisticOverview [id=" + id + ", startDate=" + startDate
				+ ", hour=" + hour + ", endDate=" + endDate + ", callAmount="
				+ callAmount + ", callInAmount=" + callInAmount
				+ ", callOutAmount=" + callOutAmount + ", bridgedAmount="
				+ bridgedAmount + ", bridgedInAmount=" + bridgedInAmount
				+ ", bridgedOutAmount=" + bridgedOutAmount
				+ ", pickupInSpecifiedSec=" + pickupInSpecifiedSec
				+ ", abandonInSpecifiedSec=" + abandonInSpecifiedSec
				+ ", csrPickupInSpecifiedSec=" + csrPickupInSpecifiedSec
				+ ", customerAbandonInSpecifiedSec="
				+ customerAbandonInSpecifiedSec
				+ ", customerPickupInSpecifiedSec="
				+ customerPickupInSpecifiedSec + ", csrAbandonInSpecifiedSec="
				+ csrAbandonInSpecifiedSec + ", totalCallTime=" + totalCallTime
				+ ", totalInCallTime=" + totalInCallTime
				+ ", totalOutCallTime=" + totalOutCallTime + ", avgCallTime="
				+ avgCallTime + ", avgInCallTime=" + avgInCallTime
				+ ", avgOutCallTime=" + avgOutCallTime + ", avgAnswerTime="
				+ avgAnswerTime + ", avgInAnswerTime=" + avgInAnswerTime
				+ ", avgOutAnswerTime=" + avgOutAnswerTime
				+ ", totalAfterCallWorkTime=" + totalAfterCallWorkTime
				+ ", totalInAfterCallWorkTime=" + totalInAfterCallWorkTime
				+ ", totalOutAfterCallWorkTime=" + totalOutAfterCallWorkTime
				+ ", avgAfterCallWorkTime=" + avgAfterCallWorkTime
				+ ", avgInAfterCallWorkTime=" + avgInAfterCallWorkTime
				+ ", avgOutAfterCallWorkTime=" + avgOutAfterCallWorkTime
				+ ", bridgedRate=" + bridgedRate + ", bridgedInRate="
				+ bridgedInRate + ", bridgedOutRate=" + bridgedOutRate
				+ ", incomingServiceLevel=" + incomingServiceLevel
				+ ", domainId=" + domainId + "]";
	}
    
}
