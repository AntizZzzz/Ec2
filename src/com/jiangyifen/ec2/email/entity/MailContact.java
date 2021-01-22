package com.jiangyifen.ec2.email.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;

/**
 * 邮件联系人实体类
 */
@Entity
@Table(name = "ec2_mail_contact")
public class MailContact {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mail_contact")
	@SequenceGenerator(name = "mail_contact", sequenceName = "seq_mail_contact_id", allocationSize = 1)
	private Long id;

	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String emailOwner;			// 邮件主人昵称
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String emailAddress;		// 邮件地址
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String description;		// 对该条记录的详情说明
	
	/**
	 * 是否是公开的邮件地址
	 * 如果为 true，则表示在对应域下面的所有员工都能看到该条邮件信息
	 * 如果为 false，则表示只有自己可以查看到该条信息
	 * 待拓展
	 */
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String isPublic;			

	public MailContact() {
	}
	// =================对应关系=================//

	// 多个创建的邮件联系人n-->1个用户
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
	private User user;
	
	//域的概念 邮件联系人n-->1域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	// ================= getter 和 setter 方法=================//

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmailOwner() {
		return emailOwner;
	}

	public void setEmailOwner(String emailOwner) {
		this.emailOwner = emailOwner;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(String isPublic) {
		this.isPublic = isPublic;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}

