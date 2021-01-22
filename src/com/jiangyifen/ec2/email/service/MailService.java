package com.jiangyifen.ec2.email.service;

import java.io.File;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.email.entity.MailHistory;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface MailService extends FlipSupportService<MailHistory> {
	
	// common method 
	@Transactional
	public MailHistory get(Object primaryKey);
	
	@Transactional
	public void save(MailHistory notice);

	@Transactional
	public MailHistory update(MailHistory notice);

	@Transactional
	public void delete(MailHistory notice);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	@Transactional
	public Boolean sendEmail(String subject,String content,List<File> attachFiles,String toAddresses);
	
	/**
	 * 发送邮件的业务处理类
	 * @param loginUser			当前登录的用户
	 * @param sendEmailUser		开启了全局配时，管理员邮箱对应的管理员用户
	 * @param subject			邮件的主题
	 * @param content			邮件的内容
	 * @param attachFiles		附件，多个用","隔开
	 * @param toAddresses		收件人的Email地址, 多个中间用 , 隔开
	 * @param ccAddresses		抄送人的Email地址, 多个中间用 , 隔开
	 * @param emailPassword		发件人的邮箱密码
	 * @return Boolean			是否发送成功
	 */
	@Transactional
	public Boolean sendEmail(User loginUser, User sendEmailUser,String subject,String content,List<File> attachFiles,String toAddresses, String ccAddresses, String emailPassword);

	/**
	 * 根据用户ID进行查询和该用户关联的邮箱配置
	 * @param userId		当前登录的用户
	 * @return mailConfig	邮箱的配置
	 */
	@Transactional
	public MailConfig findMailConfigByUserId(Long userId);
	

}





