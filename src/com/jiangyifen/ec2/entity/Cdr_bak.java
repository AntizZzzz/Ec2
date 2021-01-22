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
import com.jiangyifen.ec2.utils.Config;
/**
 * 0812 更新修改录音文件名只包含  日期-手机、固话号码-uniquid.wav
 * @author chb
 *
 */
@Entity
@Table(name = "cdr")
public class Cdr_bak {
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
	
	@Size(min = 0, max =64)
	@Column(columnDefinition="character varying(64) NOT NULL default ''", nullable = false)
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
	
	
	//用户信息
	@Column(columnDefinition="bigint")
	private Long resourceId;  //目前只有自动外呼有ResourceId的信息
	
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
		
//		//---------------------- 公用部分
//		String dest = destination;
//		if("".equals(destination)) {
//			dest = "UNKNOWN";
//		}
//
//		//---------------------- 公用部分
//		String empNo ="";
//		if(srcEmpNo!=null&&!srcEmpNo.equals("")){
//			empNo=srcEmpNo;
//		}
//		if(destEmpNo!=null&&!destEmpNo.equals("")){
//			empNo=destEmpNo;
//		}
//		
//		if("".equals(empNo)) {
//			empNo = "UNKNOWN";
//		}
//		String phoneNumber="";
//		src=src==null?"":src;
//		if(src.startsWith("01")&&src.length()==12){
//			phoneNumber=src;
//		}else if(src.startsWith("1")&&src.length()==11){
//			phoneNumber=src;
//		}
//		
//		String dest=destination==null?"":destination;
//		if(dest.startsWith("01")&&dest.length()==12){
//			phoneNumber=dest;
//		}else if(dest.startsWith("1")&&dest.length()==11){
//			phoneNumber=dest;
//		}
		
		
		src=src==null?"":src;
		String dest=destination==null?"":destination;
//		//CDR中的呼叫方向
//		e2e("内部", 0), e2o("呼出", 1),o2e("呼入",2),o2o("外转外",3);
		
		
		String phoneNumber="";
		if(cdrDirection.equals(CdrDirection.e2e)){
			phoneNumber="E2E"+src+"("+dest+")";
		}else if(cdrDirection.equals(CdrDirection.e2o)){
			phoneNumber=dest;
		}else if(cdrDirection.equals(CdrDirection.o2e)){
			phoneNumber=src;
		}else if(cdrDirection.equals(CdrDirection.o2o)){
			phoneNumber="O2O"+src+"("+dest+")";
		}
		
		if(phoneNumber.equals("")){
			phoneNumber="UNKNOWN";
		}
		
		//---------------------- 老  的录音文件名存储方式
		String url1=baseUrl+dateStr+"/"+dateStr+"-"+uniqueId+".wav";
		// 检查文件是否存在，存在则返回url ,不存在，则返回null
		String filePath1 = Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+uniqueId+".wav";
		String softlinkUrl=baseUrl+dateStr+"/"+dateStr+"-"+phoneNumber+"-"+uniqueId+".wav";
		String softlinkPath=Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+phoneNumber+"-"+uniqueId+".wav";
		
		//TODO chb 在用户下载录音url时有打开过多文件的风险，还不确定
		File file1 = new File(filePath1);
		File softlinkFile = new File(softlinkPath);
		if(file1.exists()) {
			if(!softlinkFile.exists()){
				try {
					Runtime.getRuntime().exec("ln "+filePath1+" "+softlinkPath);
				} catch (IOException e) {
System.out.println("chb record link exception:"+e);
					//ignore
					return url1;
				}
			}
			return softlinkUrl;
		}

//		//=================================================
//		//======= TODO 如果没有副作用，新的录音文件方式可以暂时删除=======
//		//=================================================
//		//---------------------- 新  的录音文件名存储方式
//		// 检验新的存储方式
//		String url2=Config.props.getProperty(Config.REC_URL_PERFIX)+dateStr+"/"+dateStr+"-"+src+"-"+dest+"-"+uniqueId+".wav";
//		// 检查文件是否存在，存在则返回url ,不存在，则返回null
//		String filePath2 = Config.props.getProperty(Config.REC_DIR_PATH)+dateStr+"/"+dateStr+"-"+src+"-"+dest+"-"+uniqueId+".wav";
//		
//		File file2 = new File(filePath2);
//		if(file2.exists()) {
//			return url2;
//		}
		
		return null;
	}

	// jrh 录音文件名称
	public String getRecordFileName() {
		String dateStr=new SimpleDateFormat("yyyyMMdd").format(startTimeDate);
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
	
}
