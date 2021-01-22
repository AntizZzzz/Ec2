package com.jiangyifen.ec2.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.jiangyifen.ec2.entity.MobileLoc;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.mobilebelong.MobileLocUtil;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

public class A_Jrh_Test_Spring {
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
        String[] locations = {"D:/workspace/ec2/WebContent/WEB-INF/applicationContextConfig.xml",
                "D:/workspace/ec2/WebContent/WEB-INF/applicationContextEao.xml",
                "D:/workspace/ec2/WebContent/WEB-INF/applicationContextEaoService.xml"};

		ApplicationContext ac = new FileSystemXmlApplicationContext(locations);
		
		SpringContextHolder sch = (SpringContextHolder) ac.getBean("springContextHolder");
		
//		SipConfigService sipConfigService = sch.getBean("sipConfigService");
		
		User user = new User();
		user.setId(1L);
		
		UserService userService = SpringContextHolder.getBean("userService");
		List<User> userLs = userService.getAllUsersByJpql("select u from User as u where u.domain.id=1 and u.id != "+user.getId());
		System.out.println(userLs.size());
		for(User u : userLs) {
			System.out.println(u.getMigrateCsr());
		}
		
		List<User> userLs2 = userService.getAllUsersByJpql("select u from User as u where u.domain.id=1 and u.id = "+user.getId());
		System.out.println("--------------");
		System.out.println(userLs2.size());
		for(User u : userLs2) {
			System.out.println(u.getMigrateCsr());
		}
		
//		sipConfigService.getEntityCount("from User e");
		
//		System.out.println(sipConfigService.getOutlineByOutlineName("88860847041"));
//		
//		System.out.println(sipConfigService.getDomainByOutLine("88860847041"));
		
//		readAndWrite();
//		
//		
//		IvrMenuService ivrMenuService = sch.getBean("ivrMenuService");
//		
//		IVRMenu tempMenu = ivrMenuService.get(1L);
//		IVRMenu ivrMenu = new IVRMenu();
//		ivrMenu.setDomain(tempMenu.getDomain());
//		ivrMenu.setWelcomeSoundFile(tempMenu.getWelcomeSoundFile());
//		ivrMenu.setCloseSoundFile(tempMenu.getCloseSoundFile());
//		ivrMenu.setIvrMenuName("新建的-----------");
//		ivrMenu.setDescription("这是 新建的IVR 哦----------");
//		ivrMenu.setIvrMenuType(IVRMenuType.customize);
//		
//		boolean result = ivrMenuService.createNewIvrByIvrTemplate(ivrMenu, IVRActionType.toRead);
//		
//		System.out.println("依据 IVR 模板，创建VIR ：  "+result);
//		System.out.println(ivrMenuService);
		
	}

	@SuppressWarnings("unused")
	private static void readAndWrite() {
		try {
			String filename = "D://miss_log_phone.txt";           // 读取成功
			File file = new File(filename); 
			FileReader fr = new FileReader(file); 
			BufferedReader br = new BufferedReader(fr); 
			
			File file2 = new File("D://inqueue_has_bridged_and_localarea.txt");
			FileWriter fw = new FileWriter(file2);
			BufferedWriter bw=new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(fw);

            int i = 0;
			String phoneNum  = null ; 
			while ( (phoneNum = br.readLine()) != null) {
				i++;
	            phoneNum = StringUtils.trimToEmpty(phoneNum);
	            if("".equals(phoneNum)) {
	            	continue;
	            }
	            String mobileLocStr = "号码归属地：未知";
    			MobileLoc mobileLoc = MobileLocUtil.getMobileAreaCode(phoneNum);
    			if(mobileLoc != null) {
    				mobileLocStr = "号码归属地："+mobileLoc.getMobileArea();
    			}
	    		
	    		out.append(phoneNum+"\t"+mobileLocStr+"\r\n");
	    		
	    		System.out.println(phoneNum+"\t"+mobileLocStr+"\r\n");
			} 
			
			System.out.println("执行完成，共处理行数 ："+i);
			
			
			br.close(); 
			fr.close();
			
			out.close();
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
