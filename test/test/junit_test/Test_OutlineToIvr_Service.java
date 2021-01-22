package test.junit_test;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.service.eaoservice.IvrMenuService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

public class Test_OutlineToIvr_Service {

	private static IvrMenuService ivrMenuService;

    @SuppressWarnings("static-access")
	@BeforeClass // 注意,这里必须是static...因为方法将在类被装载的时候就被调用(那时候还没创建实例)  
    public static void before() {   
    	SpringContextHolder sch = ApplicationContiextInit.springContextHolder;
    	ivrMenuService = sch.getBean("ivrMenuService");
    	
        System.out.println("jrh ---- 单元测试 ---- 开始！");
        System.out.println();
    }  
  
    @AfterClass  
    public static void after() {  
    	System.out.println();
        System.out.println("jrh ==== 单元测试  ==== 结束！");  
    }  
    
    @Before  
    public void setUp() throws Exception {  
        System.out.println("jrh 一个测试开始......");  
        System.out.println();
    }  
  
    @After  
    public void tearDown() throws Exception {  
    	System.out.println();
        System.out.println("jrh 一个测试结束。。。。。。");  
    }  
    
    @Test
    public void testGetIVR() {
    	Long id = 1L;
    	IVRMenu menu = ivrMenuService.getById(id);
    	if(menu != null) {
    		System.out.println(menu.getId()+"---"+menu.getIvrMenuName()+"---"+menu.getDescription());
    	} else {
    		System.out.println("未找到编号为： "+id+" 的 IVRMenu");
    	}
    }
	
    @Test
    public void checkIVRMenuConflict() {
//    	2909,    	2992,    	12

    	try {
			ArrayList<Long> selectedOutlineIds = new ArrayList<Long>();
			selectedOutlineIds.add(12L);
			
			Long id = 1861L;
			IVRMenu menu = ivrMenuService.getById(id);
			if(menu != null) {
//	    		System.out.println(menu.getId()+"---"+menu.getIvrMenuName()+"---"+menu.getDescription());
//	    		boolean conflict = ivrMenuService.checkIVRMenuConflict(menu, selectedOutlineIds);
//	    		String resultNotice = conflict ? "存在冲突的IVR" : "不存在冲突的IVR";
//    			System.out.println("conflict："+conflict+"------------》"+resultNotice);
	    	} else {
	    		System.out.println("未找到编号为： "+id+" 的 IVRMenu");
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
