package com.jiangyifen.ec2.entity;

import java.io.File;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.QcCsr;
import com.jiangyifen.ec2.entity.enumtype.QcMgr;
import com.jiangyifen.ec2.utils.Config;

@Entity
@Table(name = "ec2_customer_service_record")
public class CustomerServiceRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_service_record")
	@SequenceGenerator(name = "customer_service_record", sequenceName = "seq_ec2_customer_service_record_id", allocationSize = 1)
	private Long id;

	// 记录的标题
	@Size(min = 0, max = 128)
	@Column(columnDefinition = "character varying(128) not null default ''")
	private String title = "";

	// 记录的创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	private Date createDate;

	// 记录的内容
	@Column(columnDefinition = "TEXT")
	private String recordContent;

	// 呼叫方向，outgoing 表示呼出，incoming 呼入 默认 ''
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64) not null default ''", nullable = false)
	private String direction;

	@Enumerated
	private QcCsr qcCsr; // 坐席质检

	@Enumerated
	private QcMgr qcMgr; // 管理员质检

	// 记录的内容
	@Column(columnDefinition = "TEXT")
	private String qcReason;

	// --------------------------- jrh 为山西焦炭设定 2013-7-01
	// -----------------------//
	// 预约时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date orderTime;

	// 预约备注
	@Size(min = 0, max = 1024)
	@Column(columnDefinition = "character varying(1024)")
	private String orderNote;

	// --------------------------- jrh 2013-7-19 -----------------------//
	@Size(min = 0, max = 512)
	@Column(columnDefinition = "character varying(512)")
	private String recordFileName; // 该通话对应的录音文件名称

	// =================对应关系=================//

	// 域的概念 批次n-->1域
	// 域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	// --------------------------- jrh 2013-7-19 -----------------------//
	// 记录n-->1输入者 质检人
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User mgrQcCreator;

	// 记录n-->1输入者 记录输入者
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User creator;

	// 记录n-->1资源 针对哪条资源的记录
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerResource.class)
	private CustomerResource customerResource;

	// 记录n-->1项目 针对哪个项目的记录
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = MarketingProject.class)
	@JoinColumn(nullable = true)
	private MarketingProject marketingProject;

	// 记录n-->1状态 针对具体状态的记录
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerServiceRecordStatus.class)
	private CustomerServiceRecordStatus serviceRecordStatus;

	// ================= getter 和 setter 方法=================//

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRecordContent() {
		return recordContent;
	}

	public void setRecordContent(String recordContent) {
		this.recordContent = recordContent;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public User getMgrQcCreator() {
		return mgrQcCreator;
	}

	public void setMgrQcCreator(User mgrQcCreator) {
		this.mgrQcCreator = mgrQcCreator;
	}

	public CustomerResource getCustomerResource() {
		return customerResource;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

	public MarketingProject getMarketingProject() {
		return marketingProject;
	}

	public void setMarketingProject(MarketingProject marketingProject) {
		this.marketingProject = marketingProject;
	}

	public CustomerServiceRecordStatus getServiceRecordStatus() {
		return serviceRecordStatus;
	}

	public void setServiceRecordStatus(
			CustomerServiceRecordStatus serviceRecordStatus) {
		this.serviceRecordStatus = serviceRecordStatus;
	}

	public QcCsr getQcCsr() {
		return qcCsr;
	}

	public void setQcCsr(QcCsr qcCsr) {
		this.qcCsr = qcCsr;
	}

	public QcMgr getQcMgr() {
		return qcMgr;
	}

	public void setQcMgr(QcMgr qcMgr) {
		this.qcMgr = qcMgr;
	}

	public String getQcReason() {
		return qcReason;
	}

	public void setQcReason(String qcReason) {
		this.qcReason = qcReason;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public String getOrderNote() {
		return orderNote;
	}

	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote;
	}

	public String getRecordFileName() {
		return recordFileName;
	}

	public void setRecordFileName(String recordFileName) {
		this.recordFileName = recordFileName;
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
	public String getRecordFileDownloadUrl() {
		return "";
	}
	/**
	 * jrh 2013-07-19 获取客服记录对应的录音下载地址
	 * 
	 * @return
	 */
	public String getRecordFileDownloadUrl(String baseUrl) {
		String recordFileDownloadUrl = null;
		if (recordFileName != null) {
			// 录音文件的上级文件夹
			String dateStr = recordFileName.substring(0,
					recordFileName.indexOf("-"));
			// 录音文件http 访问路径
			String exactUrl = baseUrl
					+ dateStr + "/" + recordFileName;
			// 录音文件存储路径， 检查文件是否存在，存在则返回url ,不存在，则返回null
			String filePath1 = Config.props.getProperty(Config.REC_DIR_PATH)
					+ dateStr + "/" + recordFileName;
			File exactFile = new File(filePath1);
			if (exactFile.exists()) {
				recordFileDownloadUrl = exactUrl;
			}
		}
		return recordFileDownloadUrl;
	}

	@Override
	public String toString() {
		return "CustomerServiceRecord [id=" + id + ", title=" + title
				+ ", createDate=" + createDate + ", recordContent="
				+ recordContent + ", direction=" + direction + ", qcCsr="
				+ qcCsr + ", qcMgr=" + qcMgr + ", qcReason=" + qcReason
				+ ", orderTime=" + orderTime + ", orderNote=" + orderNote
				+ ", domain=" + domain + ", creator=" + creator
				+ ", customerResource=" + customerResource
				+ ", marketingProject=" + marketingProject
				+ ", serviceRecordStatus=" + serviceRecordStatus + "]";
	}

}
