package com.jiangyifen.ec2.email.eao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jiangyifen.ec2.email.eao.MailHistoryEao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"file:WebContent/WEB-INF/applicationContextConfig.xml",
		"file:WebContent/WEB-INF/applicationContextEao.xml",
		"file:WebContent/WEB-INF/applicationContextEaoService.xml"
		})
public class TestMailHistoryEao {
	
	@Autowired
	private MailHistoryEao mailHistoryEao;
	
	@SuppressWarnings("static-access")
	@Test
	public void show() throws Exception{
		
		Thread.currentThread().sleep(Long.MAX_VALUE);
	}
}