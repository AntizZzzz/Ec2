package com.jiangyifen.ec2.email.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.email.service.MailService;
import com.jiangyifen.ec2.email.service.impl.MailServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"file:WebContent/WEB-INF/applicationContextConfig.xml",
		"file:WebContent/WEB-INF/applicationContextEao.xml",
		"file:WebContent/WEB-INF/applicationContextEaoService.xml"
		})
public class TestMailService {
	
	@Autowired
	private MailServiceImpl mailService;
	
	@Test
	public void show() throws Exception{
		System.err.println(mailService);
		System.err.println(mailService.getUserEao());
		System.err.println(mailService.getMailConfigEao());
		System.err.println(mailService.getMailContactEao());
		System.err.println(mailService.getMailHistoryEao());
		
		Thread.currentThread().sleep(Long.MAX_VALUE);
	}
}


