package com.jiangyifen.ec2.email.utils;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Test;

import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.email.utils.SendMailUtils;

public class TestSendMailUtils {
	
	@Test
	public void sendEmail(){
		MailConfig authInfo=new MailConfig();
		authInfo.setFromAddress("jinhongting@hantiansoft.com");
		authInfo.setSenderName("jinhongting@hantiansoft.com");
		authInfo.setSenderPassword("");
		authInfo.setSmtpAuth("true");
		authInfo.setSmtpHost("smtp.exmail.qq.com");
		authInfo.setSmtpPort("25");
		
		SendMailUtils.doSend(authInfo, "test", "<html><head></head><body><i><b>This is 陈海波 chenhb</b></i><br></body></html>", null, "smackcoffee@vip.qq.com", null, null);
	}
	
	@Test
	public void sendEmailTimeOut() throws AddressException, MessagingException {
		final MailConfig mailConfig=new MailConfig();
		mailConfig.setFromAddress("jinhongting@hantiansoft.com");
		mailConfig.setSenderName("jinhongting@hantiansoft.com");
		mailConfig.setSenderPassword("hantiansoft2015");
		mailConfig.setSmtpAuth("true");
		mailConfig.setSmtpHost("smtp.exmail.qq.com");
		mailConfig.setSmtpPort("25");
		
		// 根据已配置的JavaMail属性创建Session实例
		Authenticator authenticator=new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mailConfig.getSenderName(), mailConfig.getSenderPassword());
			}
		};
		Properties props=new Properties();
		props.put("mail.smtp.port", mailConfig.getSmtpPort());
		props.put("mail.smtp.host", mailConfig.getSmtpHost());
		props.put("mail.smtp.auth", mailConfig.getSmtpAuth());
		props.put("mail.smtp.connectiontimeout", "10000");		// Socket 连接超时值，单位毫秒，缺省值不超时
		props.put("mail.smtp.timeout", "10000");				// Socket I/O 超时值，单位毫秒，缺省值不超时
		
		Session mailSession = Session.getInstance(props,authenticator);
	mailSession.setDebug(true);
		String toAddresses = "smackcoffee@vip.qq.com";
		String subject = "这是一个Java发送的测试邮件";
		
		//邮件信息设定
		MimeMessage msg = new MimeMessage(mailSession);
		msg.setFrom(new InternetAddress(mailConfig.getFromAddress())); // 设置邮件发送者
		InternetAddress[] address = InternetAddress.parse(toAddresses, false);
		msg.setRecipients(Message.RecipientType.TO, address); // 设置邮件接收者
		msg.setSubject(subject);  // 设置邮件主题
		msg.setSentDate(new Date()); // 设置邮件时间
		
		Multipart multipart = new MimeMultipart("alternative");
		BodyPart part = new MimeBodyPart();
		part.setHeader("Content-Type", "text/html;charset=UTF-8");
		part.setHeader("Content-Transfer-Ecoding", "quoted-printable");		// 或者使用 base 64
		part.setContent("欢迎您使用 <b style='color:red'>JavaMail</b> 开发邮件发送功能！", "text/html;charset=UTF-8");
		
		multipart.addBodyPart(part);
		msg.setContent(multipart);
		
		Transport tran = mailSession.getTransport("smtp");
		tran.connect(mailConfig.getSmtpHost(), mailConfig.getSenderName(),mailConfig.getSenderPassword());
		Transport.send(msg);
		
		
	}
	
}
