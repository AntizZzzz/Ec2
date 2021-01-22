package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.bean.SipConfigType;

/**
 *	IP 电话配置类
 */

@Entity
@Table(name = "ec2_sip_conf")
public class SipConfig {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sip_config")
	@SequenceGenerator(name = "sip_config", sequenceName = "seq_ec2_sip_config_id", allocationSize = 1)
	private Long id; 
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80) NOT NULL unique", unique = true, nullable = false)
	private String name;	// asterisk 拥有

	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String context;		// asterisk 拥有

	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String secret;		// asterisk 拥有
	
	@Size(min = 0, max = 31)
	@Column(columnDefinition="character varying(31) NOT NULL", nullable = false)
	private String host;	// asterisk 拥有
	
	@Column(columnDefinition="character varying NOT NULL DEFAULT 'friend'", nullable = false)
	private String type = "friend";		// asterisk 拥有
	
	@Size(min = 0, max = 3)
	@Column(columnDefinition="character varying(3) NOT NULL DEFAULT 'yes'", nullable = false)
	private String qualify = "yes";		// asterisk 拥有
	
	@Size(min = 0, max = 3)
	@Column(columnDefinition="character varying(3) NOT NULL DEFAULT 'yes'", nullable = false)
	private String canreinvite = "yes";	// asterisk 拥有
	
	@Size(min = 0, max = 50)
	@Column(columnDefinition="character varying(50) NOT NULL DEFAULT 'yes'", nullable = false)
	private String nat = "yes";			// asterisk 拥有
	
	@Size(min = 0, max = 100)
	@Column(columnDefinition="character varying(100) DEFAULT 'all'")
	private String disallow = "all";	// asterisk 拥有
	
	@Size(min = 0, max = 100)
	@Column(columnDefinition="character varying(100) DEFAULT 'alaw'")
	private String allow = "g729";		// asterisk 拥有
	
	@Column(columnDefinition="smallint NOT NULL", name = "\"call-limit\"", nullable = false)
	private Integer call_limit;		// asterisk 拥有

	@Size(min = 0, max = 5)
	@Column(columnDefinition="character varying(5) NOT NULL DEFAULT '5060'", nullable = false)
	private String port = "5060";		// asterisk 拥有
	
	@Size(min = 0, max = 3)
	@Column(columnDefinition="character varying(3) NOT NULL DEFAULT 'yes'", nullable = false)
	private String cancallforward = "yes";
	
	@Column(columnDefinition="bigint")
	private Long regseconds;		// asterisk 拥有
	
	@Column(columnDefinition="integer")
	private Integer lastms;

	@Size(min = 0, max = 100)
	@Column(columnDefinition="character varying(100)")
	private String pickupgroup;		// asterisk 拥有
	
	@Size(min = 0, max = 10)
	@Column(columnDefinition="character varying(10)")
	private String callgroup;		// asterisk 拥有

	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String fromuser;		// asterisk 拥有
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String fromdomain;		// asterisk 拥有
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String insecure;		// asterisk 拥有
	
	@Size(min = 0, max = 20)
	@Column(columnDefinition="character varying(20)")
	private String accountcode;		// asterisk 拥有
	
	@Size(min = 0, max = 7)
	@Column(columnDefinition="character varying(7)")
	private String amaflags;		// asterisk 拥有
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String callerid;		// asterisk 拥有
	
	@Size(min = 0, max = 15)
	@Column(columnDefinition="character varying(15)")
	private String defaultip;		// asterisk 拥有
	
	@Size(min = 0, max = 7)
	@Column(columnDefinition="character varying(7)")
	private String dtmfmode;		// asterisk 拥有

	@Size(min = 0, max = 2)
	@Column(columnDefinition="character varying(2)")
	private String language;		// asterisk 拥有
	
	@Size(min = 0, max = 50)
	@Column(columnDefinition="character varying(50)")
	private String mailbox;		// asterisk 拥有
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String md5secret;		// asterisk 拥有
	
	@Size(min = 0, max = 95)
	@Column(columnDefinition="character varying(95)")
	private String permit;		// asterisk 拥有
	
	@Size(min = 0, max = 95)
	@Column(columnDefinition="character varying(95)")
	private String deny;		// asterisk 拥有
	
	@Size(min = 0, max = 95)
	@Column(columnDefinition="character varying(95)")
	private String mask;
	
	@Size(min = 0, max = 100)
	@Column(columnDefinition="character varying(100)")
	private String musiconhold;
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String regexten;		// asterisk 拥有
	
	@Size(min = 0, max = 1)
	@Column(columnDefinition="character varying(1)")
	private String restrictcid;
	
	@Size(min = 0, max = 3)
	@Column(columnDefinition="character varying(3)")
	private String rtptimeout;		// asterisk 拥有
	
	@Size(min = 0, max = 3)
	@Column(columnDefinition="character varying(3)")
	private String rtpholdtimeout;		// asterisk 拥有
	
	@Size(min = 0, max = 100)
	@Column(columnDefinition="character varying(100)")
	private String setvar;		// asterisk 拥有
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String fullcontact;
	
	@Size(min = 0, max = 15)
	@Column(columnDefinition="character varying(15)")
	private String ipaddr;
	
	@Size(min = 0, max = 100)
	@Column(columnDefinition="character varying(100)")
	private String regserver;		// asterisk 拥有
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String username;
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String defaultuser;		// asterisk 拥有
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String subscribecontext;		// asterisk 拥有

	/**
	 * chb 添加 ，用来识别外线归属地
	 * 考虑到归属地可能为地址，也可能是区号，所以用字符串，这样用哪种方案都可以
	 */
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String belong="";		

	//Sip 1-->1 sip类型(分机、SIP外线，网关外线) 
	@NotNull
	@Enumerated
	private SipConfigType sipType;

	// 是否为域范围内的默认外线，默认false 表示不是
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isDefaultOutline = false;

	// 使用当前外线（或分机：分机暂时未用）进行呼叫，是否需要弹屏
	@Column(columnDefinition="boolean not null default true", nullable = false)
	private Boolean ispopupWin = true;
	
	//项目1-->1外线     一个项目可以使用一条外线
	@OneToOne(targetEntity = MarketingProject.class,fetch=FetchType.LAZY)
	private MarketingProject marketingProject;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getNat() {
		return nat;
	}

	public void setNat(String nat) {
		this.nat = nat;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAccountcode() {
		return accountcode;
	}

	public void setAccountcode(String accountcode) {
		this.accountcode = accountcode;
	}

	public String getBelong() {
		return belong;
	}

	public void setBelong(String belong) {
		this.belong = belong;
	}

	public String getAmaflags() {
		return amaflags;
	}

	public void setAmaflags(String amaflags) {
		this.amaflags = amaflags;
	}

	public Integer getCall_limit() {
		return call_limit;
	}

	public void setCall_limit(Integer call_limit) {
		this.call_limit = call_limit;
	}

	public String getCallgroup() {
		return callgroup;
	}

	public void setCallgroup(String callgroup) {
		this.callgroup = callgroup;
	}

	public String getCallerid() {
		return callerid;
	}

	public void setCallerid(String callerid) {
		this.callerid = callerid;
	}

	public String getCancallforward() {
		return cancallforward;
	}

	public void setCancallforward(String cancallforward) {
		this.cancallforward = cancallforward;
	}

	public String getCanreinvite() {
		return canreinvite;
	}

	public void setCanreinvite(String canreinvite) {
		this.canreinvite = canreinvite;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getDefaultip() {
		return defaultip;
	}

	public void setDefaultip(String defaultip) {
		this.defaultip = defaultip;
	}

	public String getDtmfmode() {
		return dtmfmode;
	}

	public void setDtmfmode(String dtmfmode) {
		this.dtmfmode = dtmfmode;
	}

	public String getFromuser() {
		return fromuser;
	}

	public void setFromuser(String fromuser) {
		this.fromuser = fromuser;
	}

	public String getFromdomain() {
		return fromdomain;
	}

	public void setFromdomain(String fromdomain) {
		this.fromdomain = fromdomain;
	}

	public String getInsecure() {
		return insecure;
	}

	public void setInsecure(String insecure) {
		this.insecure = insecure;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMailbox() {
		return mailbox;
	}

	public void setMailbox(String mailbox) {
		this.mailbox = mailbox;
	}

	public String getMd5secret() {
		return md5secret;
	}

	public void setMd5secret(String md5secret) {
		this.md5secret = md5secret;
	}

	public String getPermit() {
		return permit;
	}

	public void setPermit(String permit) {
		this.permit = permit;
	}

	public String getDeny() {
		return deny;
	}

	public void setDeny(String deny) {
		this.deny = deny;
	}

	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	public String getMusiconhold() {
		return musiconhold;
	}

	public void setMusiconhold(String musiconhold) {
		this.musiconhold = musiconhold;
	}

	public String getPickupgroup() {
		return pickupgroup;
	}

	public void setPickupgroup(String pickupgroup) {
		this.pickupgroup = pickupgroup;
	}

	public String getQualify() {
		return qualify;
	}

	public void setQualify(String qualify) {
		this.qualify = qualify;
	}

	public String getRegexten() {
		return regexten;
	}

	public void setRegexten(String regexten) {
		this.regexten = regexten;
	}

	public String getRestrictcid() {
		return restrictcid;
	}

	public void setRestrictcid(String restrictcid) {
		this.restrictcid = restrictcid;
	}

	public String getRtptimeout() {
		return rtptimeout;
	}

	public void setRtptimeout(String rtptimeout) {
		this.rtptimeout = rtptimeout;
	}

	public String getRtpholdtimeout() {
		return rtpholdtimeout;
	}

	public void setRtpholdtimeout(String rtpholdtimeout) {
		this.rtpholdtimeout = rtpholdtimeout;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getSetvar() {
		return setvar;
	}

	public void setSetvar(String setvar) {
		this.setvar = setvar;
	}

	public String getDisallow() {
		return disallow;
	}

	public void setDisallow(String disallow) {
		this.disallow = disallow;
	}

	public String getAllow() {
		return allow;
	}

	public void setAllow(String allow) {
		this.allow = allow;
	}

	public String getFullcontact() {
		return fullcontact;
	}

	public void setFullcontact(String fullcontact) {
		this.fullcontact = fullcontact;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getRegserver() {
		return regserver;
	}

	public void setRegserver(String regserver) {
		this.regserver = regserver;
	}

	public Long getRegseconds() {
		return regseconds;
	}

	public void setRegseconds(Long regseconds) {
		this.regseconds = regseconds;
	}

	public Integer getLastms() {
		return lastms;
	}

	public void setLastms(Integer lastms) {
		this.lastms = lastms;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDefaultuser() {
		return defaultuser;
	}

	public void setDefaultuser(String defaultuser) {
		this.defaultuser = defaultuser;
	}

	public String getSubscribecontext() {
		return subscribecontext;
	}

	public void setSubscribecontext(String subscribecontext) {
		this.subscribecontext = subscribecontext;
	}
	
	public SipConfigType getSipType() {
		return sipType;
	}
	
	public void setSipType(SipConfigType sipType) {
		this.sipType = sipType;
	}
	
	public Boolean getIsDefaultOutline() {
		return isDefaultOutline;
	}

	public void setIsDefaultOutline(Boolean isDefaultOutline) {
		this.isDefaultOutline = isDefaultOutline;
	}

	public Boolean getIspopupWin() {
		return ispopupWin;
	}

	public void setIspopupWin(Boolean ispopupWin) {
		this.ispopupWin = ispopupWin;
	}

	public MarketingProject getMarketingProject() {
		return marketingProject;
	}

	public void setMarketingProject(MarketingProject marketingProject) {
		this.marketingProject = marketingProject;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	@Override
	public String toString() {
		return name;
	}
	
}