package com.jiangyifen.ec2.mail;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JavaMailSender {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(JavaMailSender.class);
	
	// 只用来测试而已
	public static void main(String[] args) {
		Properties props = JavaMailConfig.getProperties();
		String hostName=props.getProperty("mail.smtp.host");
		System.out.println(hostName);
		
		File file = new File("d:/hostsmap.txt");
		JavaMailSender.sendMail(file, "hello jrh 快去找bug 呀!");
	}
	
	
	@SuppressWarnings("static-access")
	public static void sendMail(File file,String content) {
		// 设置JavaMail属性
		Properties props = JavaMailConfig.getProperties();
		String hostName=props.getProperty("mail.smtp.host");
		String fromAddress=props.getProperty("fromAddress");
		String toAddresse=props.getProperty("toAddress");
		final String senderName=props.getProperty("senderName");
		final String senderPassword=props.getProperty("senderPassword");
		if(content==null||content.trim().equals("")){
			content=props.getProperty("content");
		}
		
		try {
			// 根据已配置的JavaMail属性创建Session实例
			Authenticator authenticator=new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(senderName, senderPassword);
				}
			};
			Session mailSession = Session.getInstance(props,authenticator);
			MimeMessage msg = new MimeMessage(mailSession);
			msg.setSubject(props.getProperty("subject"));  // 设置邮件主题
			msg.setSentDate(new Date()); // 设置邮件时间
			msg.setFrom(new InternetAddress(fromAddress)); // 设置邮件发送者
			
			// 设置邮件接收者
			InternetAddress[] address = InternetAddress.parse(toAddresse, false);
			msg.setRecipients(Message.RecipientType.TO, address);
			
			// 加入文本内容
			Multipart multipart = new MimeMultipart();
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setText(content);
			multipart.addBodyPart(mimeBodyPart);

			// 加入附件
			MimeBodyPart bodyPart = new MimeBodyPart();
			// 得到数据源
			FileDataSource fileDataSource = new FileDataSource(file);
			bodyPart.setDataHandler(new DataHandler(fileDataSource));
			bodyPart.setDisposition(Part.ATTACHMENT);
			// 设置文件名
			bodyPart.setFileName(fileDataSource.getName());
			multipart.addBodyPart(bodyPart);
			msg.setContent(multipart);
			
			// 创建Transport对象
			Transport tran = mailSession.getTransport("smtp");
			// 连接邮件服务器
			tran.connect(hostName, senderName,senderPassword);
			// 发送邮件
			tran.send(msg);
			tran.close();
		} catch (Exception e) {
			LOGGER.error("jrh 发送邮件失败:"+e.getMessage(), e);
		}
	}
	
}