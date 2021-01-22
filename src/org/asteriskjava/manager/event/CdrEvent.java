/*
 *  Copyright 2004-2006 Stefan Reuter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.asteriskjava.manager.event;

import java.util.Date;
import java.util.TimeZone;

import org.asteriskjava.util.DateUtil;

/**
 * A CdrEvent is triggered when a call detail record is generated, usually at the end of a call.<p>
 * To enable CdrEvents you have to add <code>enabled = yes</code> to the general section in
 * <code>cdr_manager.conf</code>.<p>
 * This event is implemented in <code>cdr/cdr_manager.c</code>
 * 
 * @author srt
 * @version $Id$
 */
public class CdrEvent extends ManagerEvent
{
    /**
     * Serializable version identifier
     */
    private static final long serialVersionUID = 2541424315212201670L;
    
    public static final String DISPOSITION_NO_ANSWER = "NO ANSWER";
    public static final String DISPOSITION_FAILED = "FAILED";
    public static final String DISPOSITION_BUSY = "BUSY";
    public static final String DISPOSITION_ANSWERED = "ANSWERED";
    public static final String DISPOSITION_UNKNOWN = "UNKNOWN";
    
    public static final String AMA_FLAG_OMIT = "OMIT";
    public static final String AMA_FLAG_BILLING = "BILLING";
    public static final String AMA_FLAG_DOCUMENTATION = "DOCUMENTATION";
    public static final String AMA_FLAG_UNKNOWN = "Unknown";
    
    private String accountCode;
    private String src;
    private String destination;
    private String destinationContext;
    private String callerId;
    private String channel;
    private String destinationChannel;
    private String lastApplication;
    private String lastData;
    private String startTime;
    private String answerTime;
    private String endTime;
    private Integer duration;
    private Integer billableSeconds;
    private String disposition;
    private String amaFlags;
    private String uniqueId;
    private String userField;

    /**************************** jrh 为实现业务需求，而自定义的信息   *******************************/

    private String srcUserId;			// 主叫坐席【呼出】的编号
    private String srcEmpNo;			// 主叫坐席【呼出】的工号
    private String srcUsername;			// 主叫坐席【呼出】的用户名
    private String srcRealName;			// 主叫坐席【呼出】的真实姓名
    private String srcDeptId;			// 主叫坐席【呼出】的所属部门的编号
    private String srcDeptName;			// 主叫坐席【呼出】的所属部门的名称
    private String projectId;			// 当前执行的项目编号
    private String projectName;			// 当前执行的项目名称
    private String resourceId;			// 当前联系的客户资源编号
    private String taskId;				// 当前呼叫的任务编号
    private String isAutoDial;			// 当前是否执行的是自动外呼任务
    private String autoDialId;			// 自动外呼任务的编号
    private String autoDialName;		// 自动外呼任务的名称
    private String domainId;			// 当前所属
    
    /***************************** jrh 预留字段 **********************************************/
    
    private String customerPressKey;	// 记录被叫客户的按键信息
    private String mySrcNum;			// 我们自定义的主叫号码[真实主叫]
    private String myDstNum;			// 我们自定义的被叫号码[真实被叫]
    private String usedOutlineName;		// 呼叫所使用的外线号码
    private String paramA;				// 预留的字段A
    private String paramB;				// 预留的字段B
    private String paramC;				// 预留的字段C
    
    /*************************************************************************************/
    
    /**
     * @param source
     */
    public CdrEvent(Object source)
    {
        super(source);
    }

    /**
     * Returns the account number that is usually used to identify the party to bill for the call.<p>
     * Corresponds to CDR field <code>accountcode</code>.
     * 
     * @return the account number.
     */
    public String getAccountCode()
    {
        return accountCode;
    }

    /**
     * Sets the account number.
     * 
     * @param accountCode the account number.
     */
    public void setAccountCode(String accountCode)
    {
        this.accountCode = accountCode;
    }

    /**
     * Returns the Caller*ID number.<p>
     * Corresponds to CDR field <code>src</code>.
     * 
     * @return the Caller*ID number.
     */
    public String getSrc()
    {
        return src;
    }

    /**
     * Sets the Caller*ID number.
     * 
     * @param source the Caller*ID number.
     */
    public void setSrc(String source)
    {
        this.src = source;
    }

    /**
     * Returns the destination extension.<p>
     * Corresponds to CDR field <code>dst</code>.
     * 
     * @return the destination extension.
     */
    public String getDestination()
    {
        return destination;
    }

    /**
     * Sets the destination extension.
     * 
     * @param destination the destination extension.
     */
    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    /**
     * Returns the destination context.<p>
     * Corresponds to CDR field <code>dcontext</code>.
     * 
     * @return the destination context.
     */
    public String getDestinationContext()
    {
        return destinationContext;
    }

    /**
     * Sets the destination context.
     * 
     * @param destinationContext the destination context.
     */
    public void setDestinationContext(String destinationContext)
    {
        this.destinationContext = destinationContext;
    }

    /**
     * Returns the Caller*ID with text.<p>
     * Corresponds to CDR field <code>clid</code>.
     * 
     * @return the Caller*ID with text
     */
    public String getCallerId()
    {
        return callerId;
    }

    /**
     * Sets the Caller*ID with text.
     * 
     * @param callerId the Caller*ID with text.
     */
    public void setCallerId(String callerId)
    {
        this.callerId = callerId;
    }

    /**
     * Returns the name of the channel, for example "SIP/1310-asfe".<p>
     * Corresponds to CDR field <code>channel</code>.
     * 
     * @return the name of the channel.
     */
    public String getChannel()
    {
        return channel;
    }

    /**
     * Sets the name of the channel.
     * 
     * @param channel the name of the channel.
     */
    public void setChannel(String channel)
    {
        this.channel = channel;
    }

    /**
     * Returns the name of the destination channel if appropriate.<p>
     * Corresponds to CDR field <code>dstchannel</code>.
     * 
     * @return the name of the destination channel or <code>null</code> if not available.
     */
    public String getDestinationChannel()
    {
        return destinationChannel;
    }

    /**
     * Sets the name of the destination channel.
     * 
     * @param destinationChannel the name of the destination channel.
     */
    public void setDestinationChannel(String destinationChannel)
    {
        this.destinationChannel = destinationChannel;
    }

    /**
     * Returns the last application if appropriate, for example "VoiceMail".<p>
     * Corresponds to CDR field <code>lastapp</code>.
     * 
     * @return the last application or <code>null</code> if not avaialble.
     */
    public String getLastApplication()
    {
        return lastApplication;
    }

    /**
     * Sets the last application.
     * 
     * @param lastApplication the last application.
     */
    public void setLastApplication(String lastApplication)
    {
        this.lastApplication = lastApplication;
    }

    /**
     * Returns the last application's data (arguments), for example "s1234".<p>
     * Corresponds to CDR field <code>lastdata</code>.
     * 
     * @return the last application's data or <code>null</code> if not avaialble.
     */
    public String getLastData()
    {
        return lastData;
    }

    /**
     * Set the last application's data.
     * 
     * @param lastData the last application's data.
     */
    public void setLastData(String lastData)
    {
        this.lastData = lastData;
    }

    /**
     * Returns when the call has started.<p>
     * This corresponds to CDR field <code>start</code>.
     * 
     * @return A string of the format "%Y-%m-%d %T" (strftime(3)) representing the date/time the
     * call has started, for example "2006-05-19 11:54:48".
     */
    public String getStartTime()
    {
        return startTime;
    }
    
    /**
     * Returns the start time as Date object.<p>
     * This method asumes that the Asterisk server's timezone equals the default 
     * timezone of your JVM.
     * 
     * @return the start time as Date object.
     * @since 0.3
     */
    public Date getStartTimeAsDate()
    {
        return DateUtil.parseDateTime(startTime);
    }
    
    /**
     * Returns the start time as Date object.
     * 
     * @param tz the timezone of the Asterisk server.
     * @return the start time as Date object.
     * @since 0.3
     */
    public Date getStartTimeAsDate(TimeZone tz)
    {
        return DateUtil.parseDateTime(startTime, tz);
    }

    /**
     * Sets the date/time when the call has started.
     * 
     * @param startTime the date/time when the call has started.
     */
    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    /**
     * Returns when the call was answered.<p>
     * This corresponds to CDR field <code>answered</code>.
     * 
     * @return A string of the format "%Y-%m-%d %T" (strftime(3)) representing the date/time the
     * call was answered, for example "2006-05-19 11:55:01"
     */
    public String getAnswerTime()
    {
        return answerTime;
    }

    /**
     * Returns the answer time as Date object.<p>
     * This method asumes that the Asterisk server's timezone equals the default 
     * timezone of your JVM.
     * 
     * @return the answer time as Date object.
     * @since 0.3
     */
    public Date getAnswerTimeAsDate()
    {
        return DateUtil.parseDateTime(answerTime);
    }

    /**
     * Returns the answer time as Date object.
     * 
     * @param tz the timezone of the Asterisk server.
     * @return the answer time as Date object.
     * @since 0.3
     */
    public Date getAnswerTimeAsDate(TimeZone tz)
    {
        return DateUtil.parseDateTime(answerTime, tz);
    }

    /**
     * Sets the date/time when the call was answered.
     * 
     * @param answerTime the date/time when the call was answered.
     */
    public void setAnswerTime(String answerTime)
    {
        this.answerTime = answerTime;
    }

    /**
     * Returns when the call has ended.<p>
     * This corresponds to CDR field <code>end</code>.
     * 
     * @return A string of the format "%Y-%m-%d %T" (strftime(3)) representing the date/time the
     * call has ended, for example "2006-05-19 11:58:21"
     */
    public String getEndTime()
    {
        return endTime;
    }

    /**
     * Returns the end time as Date object.<p>
     * This method asumes that the Asterisk server's timezone equals the default 
     * timezone of your JVM.
     * 
     * @return the end time as Date object.
     * @since 0.3
     */
    public Date getEndTimeAsDate()
    {
        return DateUtil.parseDateTime(endTime);
    }
    
    /**
     * Returns the end time as Date object.
     * 
     * @param tz the timezone of the Asterisk server.
     * @return the end time as Date object.
     * @since 0.3
     */
    public Date getEndTimeAsDate(TimeZone tz)
    {
        return DateUtil.parseDateTime(endTime, tz);
    }

    /**
     * Sets the date/time when the call has ended.
     * 
     * @param endTime the date/time when the call has ended.
     */
    public void setEndTime(String endTime)
    {
        this.endTime = endTime;
    }

    /**
     * Returns the total time (in seconds) the caller spent in the system from dial to hangup.<p>
     * Corresponds to CDR field <code>duration</code>.
     * 
     * @return the total time in system in seconds.
     */
    public Integer getDuration()
    {
        return duration;
    }

    /**
     * Sets the total time in system.
     * 
     * @param duration total time in system in seconds.
     */
    public void setDuration(Integer duration)
    {
        this.duration = duration;
    }

    /**
     * Returns the total time (in seconds) the call was up from answer to hangup.<p>
     * Corresponds to CDR field <code>billsec</code>.
     * 
     * @return the total time in call in seconds.
     */
    public Integer getBillableSeconds()
    {
        return billableSeconds;
    }

    /**
     * Sets the total time in call.
     * 
     * @param billableSeconds the total time in call in seconds.
     */
    public void setBillableSeconds(Integer billableSeconds)
    {
        this.billableSeconds = billableSeconds;
    }

    /**
     * Returns what happened to the call.<p>
     * This is one of
     * <ul>
     * <li>{@link #DISPOSITION_NO_ANSWER}
     * <li>{@link #DISPOSITION_FAILED}
     * <li>{@link #DISPOSITION_BUSY}
     * <li>{@link #DISPOSITION_ANSWERED}
     * <li>{@link #DISPOSITION_UNKNOWN}
     * </ul>
     * Corresponds to CDR field <code>disposition</code>.
     * 
     * @return the disposition.
     */
    public String getDisposition()
    {
        return disposition;
    }

    /**
     * Sets the disposition.
     * 
     * @param disposition the disposition.
     */
    public void setDisposition(String disposition)
    {
        this.disposition = disposition;
    }

    /**
     * Returns the AMA (Automated Message Accounting) flags.<p>
     * This is one of
     * <ul>
     * <li>{@link #AMA_FLAG_OMIT}
     * <li>{@link #AMA_FLAG_BILLING}
     * <li>{@link #AMA_FLAG_DOCUMENTATION}
     * <li>{@link #AMA_FLAG_UNKNOWN}
     * </ul>
     * Corresponds to CDR field <code>amaflags</code>.
     * 
     * @return the AMA flags.
     */
    public String getAmaFlags()
    {
        return amaFlags;
    }

    /**
     * Sets the AMA (Automated Message Accounting) flags.
     * 
     * @param amaFlags the AMA (Automated Message Accounting) flags.
     */
    public void setAmaFlags(String amaFlags)
    {
        this.amaFlags = amaFlags;
    }

    /**
     * Returns the unique id of the channel.
     * 
     * @return the unique id of the channel.
     */
    public String getUniqueId()
    {
        return uniqueId;
    }

    /**
     * Sets the unique id of the channel.
     * 
     * @param uniqueId the unique id of the channel.
     */
    public void setUniqueId(String uniqueId)
    {
        this.uniqueId = uniqueId;
    }

    /**
     * Returns the user-defined field as set by <code>Set(CDR(userfield)=Value)</code>.<p>
     * Corresponds to CDR field <code>userfield</code>.
     * 
     * @return the user-defined field.
     */
    public String getUserField()
    {
        return userField;
    }

    /**
     * Sets the user-defined field.
     * 
     * @param userField the user-defined field
     */
    public void setUserField(String userField)
    {
        this.userField = userField;
    }


    /**************************** jrh 为实现业务需求，而自定义的信息   *******************************/

	/**
	 * 获取主叫坐席【呼出】的编号
	 * @return
	 */
	public String getSrcUserId() {
		return srcUserId;
	}

	/**
	 * 设置主叫坐席【呼出】的编号
	 * @param srcUserId
	 */
	public void setSrcUserId(String srcUserId) {
		this.srcUserId = srcUserId;
	}

	/**
	 * 获取主叫坐席【呼出】的工号
	 * @return
	 */
	public String getSrcEmpNo() {
		return srcEmpNo;
	}

	/**
	 * 设置主叫坐席【呼出】的工号
	 * @param srcEmpNo
	 */
	public void setSrcEmpNo(String srcEmpNo) {
		this.srcEmpNo = srcEmpNo;
	}

	/**
	 * 获取主叫坐席【呼出】的用户名
	 * @return
	 */
	public String getSrcUsername() {
		return srcUsername;
	}

	/**
	 * 设置主叫坐席【呼出】的用户名
	 * @param srcUsername
	 */
	public void setSrcUsername(String srcUsername) {
		this.srcUsername = srcUsername;
	}

	/**
	 * 获取主叫坐席【呼出】的真实姓名
	 * @return
	 */
	public String getSrcRealName() {
		return srcRealName;
	}

	/**
	 * 设置主叫坐席【呼出】的真实姓名
	 * @param srcRealName
	 */
	public void setSrcRealName(String srcRealName) {
		this.srcRealName = srcRealName;
	}

	/**
	 * 获取主叫坐席【呼出】所属部门的编号
	 * @return
	 */
	public String getSrcDeptId() {
		return srcDeptId;
	}

	/**
	 * 设置主叫坐席【呼出】所属部门的编号
	 * @param srcDeptId
	 */
	public void setSrcDeptId(String srcDeptId) {
		this.srcDeptId = srcDeptId;
	}

	/**
	 * 获取主叫坐席【呼出】所属部门的名称
	 * @return
	 */
	public String getSrcDeptName() {
		return srcDeptName;
	}

	/**
	 * 设置主叫坐席【呼出】所属部门的名称
	 * @param srcDeptName
	 */
	public void setSrcDeptName(String srcDeptName) {
		this.srcDeptName = srcDeptName;
	}

	/**
	 * 获取当前Cdr对应项目的编号
	 * @return
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * 设置当前Cdr对应项目的编号
	 * @param projectId
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * 获取当前Cdr对应项目的名称
	 * @return
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * 设置当前Cdr对应项目的名称
	 * @param projectName
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * 获取当前Cdr对应资源的编号
	 * @return
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * 设置当前Cdr对应资源的编号
	 * @param resourceId
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * 获取当前Cdr对应任务的编号【自动外呼】
	 * @return
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * 设置当前Cdr对应任务的编号【自动外呼】
	 * @param taskId
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * 获取标示当前Cdr是否为自动外呼产生的信息
	 * @return
	 */
	public String getIsAutoDial() {
		return isAutoDial;
	}

	/**
	 * 设置标示当前Cdr是否为自动外呼产生的信息
	 * @param isAutoDial
	 */
	public void setIsAutoDial(String isAutoDial) {
		this.isAutoDial = isAutoDial;
	}

	/**
	 * 获取自动外呼任务的编号
	 * @return
	 */
	public String getAutoDialId() {
		return autoDialId;
	}

	/**
	 * 设置自动外呼任务的编号
	 * @param autoDialId
	 */
	public void setAutoDialId(String autoDialId) {
		this.autoDialId = autoDialId;
	}

	/**
	 * 获取自动外呼的名称
	 * @return
	 */
	public String getAutoDialName() {
		return autoDialName;
	}

	/**
	 * 设置自动外呼的名称
	 * @param autoDialName
	 */
	public void setAutoDialName(String autoDialName) {
		this.autoDialName = autoDialName;
	}

	/**
	 * 获取租户所属域的编号
	 * @return
	 */
	public String getDomainId() {
		return domainId;
	}

	/**
	 * 设置租户所属域的编号
	 * @param domainId
	 */
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}
	
    /**************************** jrh 为实现业务需求，而自定义的信息   结束  ****************************/

    /***************************** jrh 预留字段  开始 ******************************************/

	/**
	 * 获取 记录被叫客户的按键信息
	 * @return
	 */
	public String getCustomerPressKey() {
		return customerPressKey;
	}

	/**
	 * 设置 记录被叫客户的按键信息
	 * @param customerPressKey
	 */
	public void setCustomerPressKey(String customerPressKey) {
		this.customerPressKey = customerPressKey;
	}

	/**
	 * 获取 我们自定义的主叫号码[真实主叫]
	 * @return
	 */
	public String getMySrcNum() {
		return mySrcNum;
	}

	/**
	 * 设置 我们自定义的主叫号码[真实主叫]
	 * @param mySrcNum
	 */
	public void setMySrcNum(String mySrcNum) {
		this.mySrcNum = mySrcNum;
	}

	/**
	 * 获取 我们自定义的被叫号码[真实被叫]
	 * @return
	 */
	public String getMyDstNum() {
		return myDstNum;
	}

	/**
	 * 设置 我们自定义的被叫号码[真实被叫]
	 * @param myDstNum
	 */
	public void setMyDstNum(String myDstNum) {
		this.myDstNum = myDstNum;
	}

	/**
	 * 获取 呼叫所使用的外线号码
	 * @return
	 */
	public String getUsedOutlineName() {
		return usedOutlineName;
	}

	/**
	 * 设置 呼叫所使用的外线号码
	 * @param usedOutlineName
	 */
	public void setUsedOutlineName(String usedOutlineName) {
		this.usedOutlineName = usedOutlineName;
	}

	/**
	 * 获取 预留的字段A
	 * @return
	 */
	public String getParamA() {
		return paramA;
	}

	/**
	 * 设置 预留参数A
	 * @param paramA
	 */
	public void setParamA(String paramA) {
		this.paramA = paramA;
	}

	/**
	 * 获取 预留的字段B
	 * @return
	 */
	public String getParamB() {
		return paramB;
	}

	/**
	 * 设置 预留参数B
	 * @param paramB
	 */
	public void setParamB(String paramB) {
		this.paramB = paramB;
	}

	/**
	 * 获取 预留的字段C
	 * @return
	 */
	public String getParamC() {
		return paramC;
	}

	/**
	 * 设置 预留参数C
	 * @param paramC
	 */
	public void setParamC(String paramC) {
		this.paramC = paramC;
	}
	
	/***************************** jrh 预留字段  结束 ******************************************/
    
}
