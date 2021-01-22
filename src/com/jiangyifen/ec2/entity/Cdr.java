package com.jiangyifen.ec2.entity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.bean.CdrDirection;
import com.jiangyifen.ec2.ui.mgr.util.BaseUrlUtils;
import com.jiangyifen.ec2.utils.Config;

@Entity
@Table(name = "cdr")
public class Cdr {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cdr")
	@SequenceGenerator(name = "cdr", sequenceName = "seq_cdr_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32) NOT NULL default ''", nullable = false)
	private String accountCode="";

	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32) NOT NULL default ''", nullable = false)
	private String amaflags="";

	@Temporal(TemporalType.TIMESTAMP)
	private Date answerTimeDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date bridgeTimeDate;
	
	@Column(columnDefinition="integer NOT NULL default 0", nullable = false)
	private Integer billableSeconds=0;

	@Column(columnDefinition="integer NOT NULL default 0", nullable = false)
	private Integer ec2_billableSeconds=0;

	/** 振铃时长统计 */
	@Column(columnDefinition="integer NOT NULL default 0")
	private Integer ringDuration=0;
	
	// 原来的值为 64 位, 由于在自动外呼的时候, 会做透传操作, 也就是会把这个值设置为 callerid<callerid>, 所以有时候会超过 64 位.
	@Size(min = 0, max =1024)
	@Column(columnDefinition="character varying(1024) NOT NULL default ''", nullable = false)
	private String callerId="";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
	private String channel="";
	
	@Enumerated
	private CdrDirection cdrDirection;
	
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32) NOT NULL default ''", nullable = false)
	private String destination="";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
	private String destinationChannel="";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
	private String destinationContext="";

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
	private String disposition="";

	@Column(columnDefinition="integer NOT NULL default 0", nullable = false)
	private Integer duration=0;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTimeDate;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
	private String lastApplication="";

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128) NOT NULL default ''", nullable = false)
	private String lastData="";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
	private String src="";
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTimeDate;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
	private String uniqueId="";
	
	// TODO jrh 该字段暂时用来做统计语音群发是客户的按键信息
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
	private String userField="";
	
	//客户编号
	@Column(columnDefinition="bigint")
	private Long resourceId;  //目前只有自动外呼有ResourceId的信息
	
	// 客户对应的号码编号  // jrh  新增 2013-11-26
	@Column(columnDefinition="bigint")
	private Long resourceTelephoneId;  
	
	//用户信息
	@Column(columnDefinition="bigint")
	private Long srcUserId;

	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32) default ''")
	private String srcEmpNo ="";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String srcUsername="";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String srcRealName="";

	@Column(columnDefinition="bigint")
	private Long destUserId;
	
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32) default ''")
	private String destEmpNo ="";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String destUsername="";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String destRealName="";
	
	//部门信息
	@Column(columnDefinition="bigint")
	private Long srcDeptId;

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String srcDeptName="";

	@Column(columnDefinition="bigint")
	private Long destDeptId;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String destDeptName="";

	@Column(columnDefinition="bigint", nullable = false)
	private Long domainId=0L;
	
	
	//项目Id
	@Column(columnDefinition="bigint")
	private Long projectId;

	//项目名称
	@Column(columnDefinition="character varying(64) default ''")
	private String projectName="";

//	TODO  如果true 表示自动外呼   、 null 表示人工呼叫  、 false 表示语音群发
//	TODO  这样很不好，以后改成枚举类型或者字符串类型
	private Boolean isAutoDial;

	private Boolean isBridged;
	
	//自动外呼的Id
	@Column(columnDefinition="bigint")
	private Long autoDialId;
	
	//自动外呼的名称
	@Column(columnDefinition="character varying(64) default ''")
	private String autoDialName="";
	
	//录音是否被下载过，默认false 没有被下载过 
	@Column(columnDefinition="boolean default false")
	private Boolean isDownloaded = false;

	//外线名称
	@Column(columnDefinition="character varying(80) default ''")
	private String outlineName="";

	// 队列名称
	@Column(columnDefinition="character varying(80) default ''")
	private String queueName = "";

	@Temporal(TemporalType.TIMESTAMP)
	private Date lastInQueueTimeDate;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}

	public String getAmaflags() {
		return amaflags;
	}

	public void setAmaflags(String amaflags) {
		this.amaflags = amaflags;
	}

	public Date getAnswerTimeDate() {
		return answerTimeDate;
	}

	public void setAnswerTimeDate(Date answerTimeDate) {
		this.answerTimeDate = answerTimeDate;
	}

	public Integer getBillableSeconds() {
		return billableSeconds;
	}

	public void setBillableSeconds(Integer billableSeconds) {
		this.billableSeconds = billableSeconds;
	}

	public String getCallerId() {
		return callerId;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public CdrDirection getCdrDirection() {
		return cdrDirection;
	}

	public void setCdrDirection(CdrDirection cdrDirection) {
		this.cdrDirection = cdrDirection;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDestinationChannel() {
		return destinationChannel;
	}

	public void setDestinationChannel(String destinationChannel) {
		this.destinationChannel = destinationChannel;
	}

	public String getDestinationContext() {
		return destinationContext;
	}

	public void setDestinationContext(String destinationContext) {
		this.destinationContext = destinationContext;
	}

	public String getDisposition() {
		return disposition;
	}

	public void setDisposition(String disposition) {
		this.disposition = disposition;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Date getEndTimeDate() {
		return endTimeDate;
	}

	public void setEndTimeDate(Date endTimeDate) {
		this.endTimeDate = endTimeDate;
	}

	public String getLastApplication() {
		return lastApplication;
	}

	public void setLastApplication(String lastApplication) {
		this.lastApplication = lastApplication;
	}

	public String getLastData() {
		return lastData;
	}

	public void setLastData(String lastData) {
		this.lastData = lastData;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public Date getStartTimeDate() {
		return startTimeDate;
	}

	public void setStartTimeDate(Date startTimeDate) {
		this.startTimeDate = startTimeDate;
	}

	/**
	 * @Description 描述：为了解决下面异常
	 * 
	 * java.lang.IllegalArgumentException: Ids must exist in the Container or as a generated column , missing id: url
	 * 因为现在表格中获取URL的方式，实际是调用 getUrl(String baseUrl) 方法
	 * 
	 * @author  JRH
	 * @date    2014年6月9日 下午5:24:28
	 * @return String
	 */
	public String getUrl() {
		/*return "";*/
		return getUrl(BaseUrlUtils.getBaseUrl());
	}
	
//	TODO jrh 数据库中添加 url 字段 ，这样以后获取url 创建试听按钮时就不需要每次去查询文件是否存在了
	/**
	 * jrh 录音存储地址
	 * @return
	 */
	public String getUrl(String baseUrl) {
//		String dateStr=new SimpleDateFormat("yyyyMMdd").format(startTimeDate);
//		//---------------------- 老  的录音文件名存储方式
//		String url1=Config.props.getProperty(Config.REC_URL_PERFIX)+dateStr+"/"+dateStr+"-"+uniqueId+".wav";
//		// 检查文件是否存在，存在则返回url ,不存在，则返回null
//		String filePath1 = Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+uniqueId+".wav";
//		
//		File file1 = new File(filePath1);
//		if(file1.exists()) {
//			return url1;
//		}
//
//		//---------------------- 新  的录音文件名存储方式
//		// 检验新的存储方式
//		String dest = destination;
//		if("".equals(destination)) {
//			dest = "UNKNOWN";
//		}
//		String url2=Config.props.getProperty(Config.REC_URL_PERFIX)+dateStr+"/"+dateStr+"-"+src+"-"+dest+"-"+uniqueId+".wav";
//		// 检查文件是否存在，存在则返回url ,不存在，则返回null
//		String filePath2 = Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+src+"-"+dest+"-"+uniqueId+".wav";
//		
//		File file2 = new File(filePath2);
//		if(file2.exists()) {
//			return url2;
//		}
//		
//		return null;
		
		String dateStr=new SimpleDateFormat("yyyyMMdd").format(startTimeDate);
		
		//---------------------- 公用部分
		String dest = destination;
		if("".equals(destination)) {
			dest = "UNKNOWN";
		}

		//---------------------- 公用部分
		String empNo ="";
		if(srcEmpNo!=null&&!srcEmpNo.equals("")){
			empNo=srcEmpNo;
		}
		if(destEmpNo!=null&&!destEmpNo.equals("")){
			empNo=destEmpNo;
		}
		
		if("".equals(empNo)) {
			empNo = "UNKNOWN";
		}
		
		//---------------------- 老  的录音文件名存储方式
		String url1=baseUrl+dateStr+"/"+dateStr+"-"+uniqueId+".wav";
		// 检查文件是否存在，存在则返回url ,不存在，则返回null
		String filePath1 = Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+uniqueId+".wav";
		String softlinkUrl=baseUrl+dateStr+"/"+dateStr+"-"+src+"-"+dest+"-"+empNo+"-"+uniqueId+".wav";
		String softlinkPath=Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+src+"-"+dest+"-"+empNo+"-"+uniqueId+".wav";
		
		//TODO chb 在用户下载录音url时有打开过多文件的风险，还不确定
		File file1 = new File(filePath1);
		File softlinkFile = new File(softlinkPath);
		if(file1.exists()) {
			if(!softlinkFile.exists()){
				try {
					Runtime.getRuntime().exec("ln "+filePath1+" "+softlinkPath);
				} catch (IOException e) {
					//ignore
					return url1;
				}
			}
			return softlinkUrl;
		}

		//=================================================
		//======= TODO 如果没有副作用，新的录音文件方式可以暂时删除=======
		//=================================================
		//---------------------- 新  的录音文件名存储方式
		// 检验新的存储方式
		String url2=baseUrl+dateStr+"/"+dateStr+"-"+src+"-"+dest+"-"+uniqueId+".wav";
		// 检查文件是否存在，存在则返回url ,不存在，则返回null
		String filePath2 = Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+src+"-"+dest+"-"+uniqueId+".wav";
		
		File file2 = new File(filePath2);
		if(file2.exists()) {
			return url2;
		}
		
		return null;
	}
	
	public String getRealUrl() {
		return getRealUrl(BaseUrlUtils.getBaseUrl());
	}
	
	/**
	 * jrh 2013-12-18  录音文件的真实路径，为了解决在电话加密的时候显示的录音名称看不到电话号码，
	 * 	也可以通过修改 Embed html 音乐播放器的参数 ShowStatusBar='0'  但这样将看不到录音的时长
	 * 
	 * @return
	 */
	public String getRealUrl(String baseUrl) {
		String dateStr=new SimpleDateFormat("yyyyMMdd").format(startTimeDate);
		String filePath1 = Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+uniqueId+".wav";
		String realUrl = baseUrl+dateStr+"/"+dateStr+"-"+uniqueId+".wav";

		File file2 = new File(filePath1);
		if(file2.exists()) {
			return realUrl;
		}
		
		return null;
	}
	
	// jrh 录音文件名称 目前没用上
	public String getRecordFileName() {
		String dateStr=new SimpleDateFormat("yyyyMMdd").format(startTimeDate);
		String filePath2 = Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+uniqueId+".wav";
		
		File file2 = new File(filePath2);
		if(!file2.exists()) {
			return "无录音";
		}
		
		return dateStr+"-"+uniqueId+".wav";
	}
	
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getUserField() {
		return userField;
	}

	public void setUserField(String userField) {
		this.userField = userField;
	}
	
	public Date getBridgeTimeDate() {
		return bridgeTimeDate;
	}

	public void setBridgeTimeDate(Date bridgeTimeDate) {
		this.bridgeTimeDate = bridgeTimeDate;
	}

	public Long getSrcUserId() {
		return srcUserId;
	}

	public void setSrcUserId(Long srcUserId) {
		this.srcUserId = srcUserId;
	}

	public String getSrcEmpNo() {
		return srcEmpNo;
	}

	public void setSrcEmpNo(String srcEmpNo) {
		this.srcEmpNo = srcEmpNo;
	}

	public String getSrcUsername() {
		return srcUsername;
	}

	public void setSrcUsername(String srcUsername) {
		this.srcUsername = srcUsername;
	}

	public String getSrcRealName() {
		return srcRealName;
	}

	public void setSrcRealName(String srcRealName) {
		this.srcRealName = srcRealName;
	}

	public Long getDestUserId() {
		return destUserId;
	}

	public void setDestUserId(Long destUserId) {
		this.destUserId = destUserId;
	}

	public String getDestEmpNo() {
		return destEmpNo;
	}

	public void setDestEmpNo(String destEmpNo) {
		this.destEmpNo = destEmpNo;
	}

	public String getDestUsername() {
		return destUsername;
	}

	public void setDestUsername(String destUsername) {
		this.destUsername = destUsername;
	}

	public String getDestRealName() {
		return destRealName;
	}

	public void setDestRealName(String destRealName) {
		this.destRealName = destRealName;
	}

	public Long getSrcDeptId() {
		return srcDeptId;
	}

	public void setSrcDeptId(Long srcDeptId) {
		this.srcDeptId = srcDeptId;
	}

	public String getSrcDeptName() {
		return srcDeptName;
	}

	public void setSrcDeptName(String srcDeptName) {
		this.srcDeptName = srcDeptName;
	}

	public Long getDestDeptId() {
		return destDeptId;
	}

	public void setDestDeptId(Long destDeptId) {
		this.destDeptId = destDeptId;
	}

	public String getDestDeptName() {
		return destDeptName;
	}

	public void setDestDeptName(String destDeptName) {
		this.destDeptName = destDeptName;
	}

	public Integer getEc2_billableSeconds() {
		return ec2_billableSeconds;
	}

	public void setEc2_billableSeconds(Integer ec2_billableSeconds) {
		this.ec2_billableSeconds = ec2_billableSeconds;
	}

	public Integer getRingDuration() {
		return ringDuration;
	}

	public void setRingDuration(Integer ringDuration) {
		this.ringDuration = ringDuration;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Boolean getIsAutoDial() {
		return isAutoDial;
	}

	public void setIsAutoDial(Boolean isAutoDial) {
		this.isAutoDial = isAutoDial;
	}

	public Long getAutoDialId() {
		return autoDialId;
	}

	public void setAutoDialId(Long autoDialId) {
		this.autoDialId = autoDialId;
	}

	public String getAutoDialName() {
		return autoDialName;
	}

	public void setAutoDialName(String autoDialName) {
		this.autoDialName = autoDialName;
	}

	public Boolean getIsBridged() {
		return isBridged;
	}

	public void setIsBridged(Boolean isBridged) {
		this.isBridged = isBridged;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public Long getResourceTelephoneId() {
		return resourceTelephoneId;
	}

	public void setResourceTelephoneId(Long resourceTelephoneId) {
		this.resourceTelephoneId = resourceTelephoneId;
	}

	public Boolean getIsDownloaded() {
		return isDownloaded;
	}

	public void setIsDownloaded(Boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
	}

	public String getOutlineName() {
		return outlineName;
	}

	public void setOutlineName(String outlineName) {
		this.outlineName = outlineName;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Date getLastInQueueTimeDate() {
		return lastInQueueTimeDate;
	}

	public void setLastInQueueTimeDate(Date lastInQueueTimeDate) {
		this.lastInQueueTimeDate = lastInQueueTimeDate;
	}
	
}
