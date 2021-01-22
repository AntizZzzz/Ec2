package com.jiangyifen.ec2.email.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.eao.UserEao;
import com.jiangyifen.ec2.email.eao.MailConfigEao;
import com.jiangyifen.ec2.email.eao.MailContactEao;
import com.jiangyifen.ec2.email.eao.MailHistoryEao;
import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.email.entity.MailHistory;
import com.jiangyifen.ec2.email.service.MailService;
import com.jiangyifen.ec2.email.utils.SendMailUtils;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;

public class MailServiceImpl implements MailService{
	
	private UserEao userEao;
	private MailConfigEao mailConfigEao;
	private MailContactEao mailContactEao;
	private MailHistoryEao mailHistoryEao;

	@Override
	public Boolean sendEmail(String subject,String content,List<File> attachFiles,String toAddresses){
		return false;
//		return this.sendEmail(null, subject, content, attachFiles, toAddresses);
	}
	
	@Override
	public Boolean sendEmail(User loginUser, User sendEmailUser,String subject,String content,List<File> attachFiles,String toAddresses, String ccAddresses, String emailPassword){
		if(sendEmailUser==null){
			throw new RuntimeException("发送邮件的用户不能为空");
		}
		
		// Get config
		MailConfig mailConfig=null;
		Boolean isSettingGlobalEmail = ShareData.domainToConfigs.get(sendEmailUser.getDomain().getId()).get("setting_global_email");
		if(isSettingGlobalEmail == null || !isSettingGlobalEmail){
			mailConfig=mailConfigEao.getMailConfigByUser(sendEmailUser);
		}else{ //全局统一邮件smtp配置使用
			// mailConfig=mailConfigEao.getDefaultMailConfig(operateUser.getDomain().getId());
			mailConfig=mailConfigEao.getMailConfigByUser(sendEmailUser);
		}
		
		if(mailConfig==null){
			throw new RuntimeException("请先配置SMTP服务器再尝试发送邮件");
		}

		/**
		 * 邮箱密码没有填写的时候，发送邮件会出现验证多次发送邮件，这里在发送邮件的时候，应该设置一个超时时间
		 */
		/*if(emailPassword != null) {
			mailConfig.setSenderPassword(emailPassword);
		}*/
		if(emailPassword == null || "".equals(emailPassword.trim())) {
			return false;
			/*throw new RuntimeException("邮箱密码没有填写");*/
		}
		
		Boolean isSuccess = SendMailUtils.doSend(mailConfig, subject, content, attachFiles, toAddresses, ccAddresses, emailPassword);

		// Log to history
		MailHistory mailHistory=new MailHistory();
		StringBuilder afs=new StringBuilder();
		if(attachFiles!=null){
			for(File file:attachFiles){
				afs.append(file.getAbsolutePath());
			}
		}
		mailHistory.setAttachFiles(afs.toString());
		mailHistory.setContent(content);
		mailHistory.setFromAddress(mailConfig.getFromAddress());
		mailHistory.setSenderName(mailConfig.getSenderName());
		mailHistory.setSendTimeDate(new Date());
		mailHistory.setSubject(subject);
		mailHistory.setToAddresses(toAddresses);
		mailHistory.setUser(sendEmailUser);
		mailHistory.setDomain(sendEmailUser.getDomain());
		mailHistory.setLoginUser(loginUser);
		mailHistoryEao.save(mailHistory);
		
		//是否正确
		return isSuccess;
	}
	
	@Override
	public MailConfig findMailConfigByUserId(Long userId) {
		User operateUser=userId==null?null:userEao.get(User.class, userId);
		if(operateUser==null){
			throw new RuntimeException("发送邮件的用户不能为空");
		}
		// Get config
		MailConfig mailConfig=null;
		Boolean isSettingGlobalEmail = ShareData.domainToConfigs.get(operateUser.getDomain().getId()).get("setting_global_email");
		if(isSettingGlobalEmail == null || !isSettingGlobalEmail){
			mailConfig=mailConfigEao.getMailConfigByUser(operateUser);
		}else{ //全局统一邮件smtp配置使用
			// mailConfig=mailConfigEao.getDefaultMailConfig(operateUser.getDomain().getId());
			mailConfig=mailConfigEao.getMailConfigByUser(operateUser);
		}
		
		if(mailConfig==null){
			throw new RuntimeException("请先配置SMTP服务器再尝试发送邮件");
		}
		return mailConfig;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MailHistory> loadPageEntities(int start, int length, String sql) {
		return mailHistoryEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return mailHistoryEao.getEntityCount(sql);
	}

	@Override
	@Transactional
	public MailHistory get(Object primaryKey) {
		return mailHistoryEao.get(MailHistory.class, primaryKey);
	}

	@Override
	@Transactional
	public void save(MailHistory mailHistory) {
		mailHistoryEao.save(mailHistory);
		
	}

	@Override
	@Transactional
	public MailHistory update(MailHistory mailHistory) {
		return (MailHistory)mailHistoryEao.update(mailHistory);
	}

	@Override
	@Transactional
	public void delete(MailHistory mailHistory) {
		mailHistoryEao.delete(mailHistory);
		
	}

	@Override
	@Transactional
	public void deleteById(Object primaryKey) {
		mailHistoryEao.delete(MailHistory.class, primaryKey);
		
	}
	
	//xxxxxxxxx
	public MailConfigEao getMailConfigEao() {
		return mailConfigEao;
	}

	public void setMailConfigEao(MailConfigEao mailConfigEao) {
		this.mailConfigEao = mailConfigEao;
	}
	
	public UserEao getUserEao() {
		return userEao;
	}

	public void setUserEao(UserEao userEao) {
		this.userEao = userEao;
	}

	public MailContactEao getMailContactEao() {
		return mailContactEao;
	}

	public void setMailContactEao(MailContactEao mailContactEao) {
		this.mailContactEao = mailContactEao;
	}

	public MailHistoryEao getMailHistoryEao() {
		return mailHistoryEao;
	}

	public void setMailHistoryEao(MailHistoryEao mailHistoryEao) {
		this.mailHistoryEao = mailHistoryEao;
	}

}
