package test.junit_test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * @Description 描述：启动Spring容器
 * 
 * @author  jrh
 * @date    2014年3月5日 上午11:49:55
 * @version v1.0.0
 */
public class ApplicationContiextInit {

	
	public static SpringContextHolder springContextHolder = null; 

	static{  
		String[] locations = {"/WebContent/WEB-INF/applicationContextConfig.xml",
                "/WebContent/WEB-INF/applicationContextEao.xml",
                "/WebContent/WEB-INF/applicationContextEaoService.xml"};
		
		// 或者使用实际存储路径
//		String[] locations = {"D:/workspace/ec2/WebContent/WEB-INF/applicationContextConfig.xml",
//				"D:/workspace/ec2/WebContent/WEB-INF/applicationContextEao.xml",
//		"D:/workspace/ec2/WebContent/WEB-INF/applicationContextEaoService.xml"};

		ApplicationContext ac = new FileSystemXmlApplicationContext(locations);
		
		springContextHolder = (SpringContextHolder) ac.getBean("springContextHolder");
		
    } 

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		System.out.println(springContextHolder.getBean("userService"));
	}
	
}
