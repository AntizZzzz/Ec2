package com.jiangyifen.ec2.email.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.email.entity.MailConfig;

public class SendMailUtils {
	private static Logger logger=LoggerFactory.getLogger(SendMailUtils.class);
	
	/**
	 * 发送邮件，使用认证信息中的信息来发送邮件（如果认证信息为null，使用系统配置的默认账户发送邮件）
	 * @param authInfo 认证信息
	 * @param subject 主题
	 * @param content 内容
	 * @param attachFiles 附件
	 * @param toAddresses 收件人, 多个中间用 , 分割
	 * @param ccAddresses 抄送人, 多个中间用 , 分割
	 * @param emailPassword 邮箱密码
	 * @return 是否成功
	 */
	public static Boolean doSend(final MailConfig mailConfig,String subject,String content,List<File> attachFiles,String toAddresses, String ccAddresses, final String emailPassword){
		if(StringUtils.isEmpty(subject)){
			throw new RuntimeException("subject should not null");
		}
		if(StringUtils.isEmpty(content)){
			throw new RuntimeException("content should not null");
		}
		
		Transport tran = null;
		
		try {
			// 根据已配置的JavaMail属性创建Session实例
			Authenticator authenticator=new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mailConfig.getFromAddress(), emailPassword);
				}
			};
			Properties props=new Properties();
			// 配置邮件的传送协议为 smtp
			// props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.port", mailConfig.getSmtpPort());
			props.put("mail.smtp.host", mailConfig.getSmtpHost());
			props.put("mail.smtp.auth", mailConfig.getSmtpAuth());
			props.put("mail.smtp.connectiontimeout", "10000");		// Socket 连接超时值，单位毫秒，缺省值不超时
			props.put("mail.smtp.timeout", "10000");				// Socket I/O 超时值，单位毫秒，缺省值不超时
			
			Session mailSession = Session.getInstance(props,authenticator);
			
			// 发送者的邮箱以及昵称
			String fromAddress = "";	
			if(mailConfig.getSenderName() != null && !"".equals(mailConfig.getSenderName().trim())) {
				// 这里的 MimeUtility.encodeText 是要对发送者邮件昵称进行转码，否则就会出现发送邮件的标题之类的不正常
				fromAddress = MimeUtility.encodeText(mailConfig.getSenderName())+"<"+mailConfig.getFromAddress()+">";
			} else {
				fromAddress = mailConfig.getFromAddress();
			}
			
			String checkEmail = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

			// 接收者的邮箱
			List<String> toAddressList = new ArrayList<String>();
			for(String addressEmail : Arrays.asList(toAddresses.split(","))) {
				if(addressEmail != null) {
					int firstIndex = addressEmail.indexOf("<");
					int lastIndex = addressEmail.indexOf(">");
					if(firstIndex > 0 && lastIndex > 0) {
						String address = addressEmail.substring(firstIndex+1, lastIndex);
						if(Pattern.compile(checkEmail).matcher(address).matches()) {
							String username = addressEmail.substring(0, addressEmail.indexOf("<"));
							address = "<"+address+">";
							toAddressList.add(MimeUtility.encodeText(username)+address);
						}
					} else if(Pattern.compile(checkEmail).matcher(addressEmail).matches()) {
						toAddressList.add(addressEmail);
					}
				}
			}
			
			// 抄送者的邮箱
			List<String> ccAddressList = new ArrayList<String>();
			for(String addressEmail : Arrays.asList(ccAddresses.split(","))) {
				if(addressEmail != null) {
					int firstIndex = addressEmail.indexOf("<");
					int lastIndex = addressEmail.indexOf(">");
					if(firstIndex > 0 && lastIndex > 0) {
						String address = addressEmail.substring(firstIndex+1, lastIndex);
						if(Pattern.compile(checkEmail).matcher(address).matches()) {
							String username = addressEmail.substring(0, addressEmail.indexOf("<"));
							address = "<"+address+">";
							ccAddressList.add(MimeUtility.encodeText(username)+address);
						}
					} else if(Pattern.compile(checkEmail).matcher(addressEmail).matches()) {
						ccAddressList.add(addressEmail);
					}
				}
			}
			
			//邮件信息设定
			MimeMessage msg = new MimeMessage(mailSession);
			msg.setFrom(new InternetAddress(fromAddress)); 			// 设置邮件发送者
			InternetAddress[] toAddress = InternetAddress.parse(StringUtils.join(toAddressList, ","), false);
			msg.setRecipients(Message.RecipientType.TO, toAddress); // 设置邮件接收者
			InternetAddress[] ccAddress = InternetAddress.parse(StringUtils.join(ccAddressList, ","), false);
			msg.setRecipients(Message.RecipientType.CC, ccAddress);	// 设置邮件的抄送者
			msg.setSubject(subject);  // 设置邮件主题
			msg.setSentDate(new Date()); // 设置邮件时间
			
			// 邮件正文
			Multipart multipart = new MimeMultipart();
			msg.setContent(multipart);
			
			//添加正文文本内容
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(content,"text/html;charset=UTF-8");
			multipart.addBodyPart(mimeBodyPart);

			// 加入附件
			if(attachFiles!=null){
				for(File attachFile:attachFiles){
					MimeBodyPart bodyPart = new MimeBodyPart();
					FileDataSource fileDataSource = new FileDataSource(attachFile);
					bodyPart.setDataHandler(new DataHandler(fileDataSource));
					bodyPart.setDisposition(Part.ATTACHMENT);
					bodyPart.setFileName(fileDataSource.getName()); // 设置文件名
					multipart.addBodyPart(bodyPart);
				}
			}
			
			// 连接邮件服务器 并发送邮件
			tran = mailSession.getTransport("smtp");
			tran.connect(mailConfig.getSmtpHost(), mailConfig.getFromAddress(), emailPassword);
			Transport.send(msg);
			tran.close();
			//记录日志
			logger.info("chb: Send mail "+subject+" from "+mailConfig.getFromAddress()+" to "+toAddresses+" success");
			logger.info("jinht: Real receiver ["+StringUtils.join(toAddressList, ",")+"]");
			return true;
		} catch (AuthenticationFailedException e) {
			logger.error("jinht: Send mail failed : " + e.getMessage() , e);
			throw new RuntimeException("身份验证有误，邮件发送失败！");
		} catch (SendFailedException e) {
			logger.error("jinht: Send mail failed : " + e.getMessage(), e);
			throw new RuntimeException("不是有效的邮件发送地址，邮件发送失败！");
		} catch (AddressException e) {
			logger.error("jinht: Send mail failed : " + e.getMessage(), e);
			throw new RuntimeException("不是有效的邮件发送地址，邮件发送失败！");
		} catch (Exception e) {
			logger.error("jinht: Send mail failed : " + e.getMessage(), e);
			throw new RuntimeException("发送邮件失败！");
		} finally {
			if(tran != null && tran.isConnected()) {	// 判断资源是否还在连接中，避免资源未被释放的情况出现，正常情况下不会出现该问题
				try {
					tran.close();
				} catch (MessagingException e) {
					logger.error("jinht --> 发送邮件后，关闭连接出现异常！" + e.getMessage(), e);
				}
			}
		}

	}
}
