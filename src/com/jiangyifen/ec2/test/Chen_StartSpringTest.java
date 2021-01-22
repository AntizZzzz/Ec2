package com.jiangyifen.ec2.test;

import javax.persistence.EntityManager;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;

public class Chen_StartSpringTest {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		 String file = "/WebContent/WEB-INF/applicationContext*.xml";
	     ApplicationContext ac = new FileSystemXmlApplicationContext(file);
		EntityManager em=(EntityManager)ac.getBean("entityManager");
		
		CommonService commonService=(CommonService)ac.getBean("commonService");
		
//		select e from CustomerServiceRecord e where (e.creator.department.id=1 or e.creator.department.id=10 or e.creator.department.id=35 or e.creator.department.id=152) and e.domain.id=1 order by e.id desc
//		select customerresource_id,max(id) from ec2_customer_service_record group by customerresource_id;
		
		commonService.excuteSql("delete from SmsPhoneNumber s where s.id=(select max(sp.id) from SmsPhoneNumber sp where sp.domain.id="+1+")", ExecuteType.UPDATE);
		
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
