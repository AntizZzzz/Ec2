package com.jiangyifen.ec2.email.eao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jiangyifen.ec2.email.eao.MailConfigEao;
import com.jiangyifen.ec2.email.entity.MailConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"file:WebContent/WEB-INF/applicationContextConfig.xml",
		"file:WebContent/WEB-INF/applicationContextEao.xml",
		"file:WebContent/WEB-INF/applicationContextEaoService.xml"
		})
public class TestMailConfigEao {
	
	@Autowired
	private MailConfigEao mailConfigEao;
	
	@Test
	public void show() throws Exception{
		
//		MailConfig mailConfig=new MailConfig();
//		mailConfig.setFromAddress("chenhb@hantiansoft.com");
//		mailConfig.setSenderName("chenhb@hantiansoft.com");
//		mailConfig.setSenderPassword("123456");
//		mailConfig.setSmtpAuth("true");
//		mailConfig.setSmtpHost("smtp.exmail.qq.com");
//		mailConfig.setIsDefault("true");
//		mailConfig.setSmtpPort("25");
//		mailConfig.setUser(null);
//		mailConfigEao.save(mailConfig);
//		System.err.println("----------------saved-----------------");
		
		//get default
		MailConfig mailConfig=mailConfigEao.getDefaultMailConfig(1L);
		System.err.println("--"+mailConfig.getSenderName());
		Thread.currentThread().sleep(Long.MAX_VALUE);
	}
}


