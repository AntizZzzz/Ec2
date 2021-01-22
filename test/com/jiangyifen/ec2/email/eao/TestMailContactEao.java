package com.jiangyifen.ec2.email.eao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jiangyifen.ec2.email.eao.MailContactEao;
import com.jiangyifen.ec2.email.entity.MailContact;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"file:WebContent/WEB-INF/applicationContextConfig.xml",
		"file:WebContent/WEB-INF/applicationContextEao.xml",
		"file:WebContent/WEB-INF/applicationContextEaoService.xml"
		})
public class TestMailContactEao {
	
	@Autowired
	private MailContactEao mailContactEao;
	
	@Test
	public void show() throws Exception{
		
//		MailContact mc1=new MailContact();
//		mc1.setEmailAddress("chen@163.com");
//		mc1.setEmailOwner("chenhb");
//		mc1.setDescription("This is chenhb email");
//		mc1.setIsPublic("true");
//		mailContactEao.save(mc1);
//		
//		MailContact mc2=new MailContact();
//		mc2.setEmailAddress("hai@163.com");
//		mc2.setEmailOwner("hai");
//		mc2.setDescription("This is chen email");
//		mc2.setIsPublic("true");
//		mailContactEao.save(mc2);
		
		
		//get default
		List<MailContact> mailContactList = mailContactEao.getEntitiesByJpql("select m from MailContact m");
System.err.println(mailContactList);
		
		Thread.currentThread().sleep(Long.MAX_VALUE);
	}
}


