package com.jiangyifen.ec2.utils;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.jiangyifen.ec2.backgroundthread.ActionThreadStarter;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.ui.csr.workarea.marketingtask.MyMarketingTaskTabView;
import com.jiangyifen.ec2.ui.csr.workarea.questionnairetask.QuestionnaireTaskTabView;
import com.vaadin.ui.Window;

/**
 * 
 *以静态变量保存Spring ApplicationContext, 可在任何代码任何地方任何时候中取出ApplicaitonContext.   
 *由于要获取到HttpSession，此类依赖于WebSessionHolder  
 * @author chb                                                                 
 */
public class SpringContextHolder implements ApplicationContextAware{
    private static ApplicationContext applicationContext;

    static{
    	//启动Action对应的Event监听线程
    	new ActionThreadStarter().start();
    }
    
    /**
     * common
     * 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.applicationContext = applicationContext;
    }
   
    /**
     * common
     * 取得存储在静态变量中的ApplicationContext.
     * @return
     */
    public static ApplicationContext getApplicationContext() {
    	return applicationContext;
    }

    /**
     * common
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
    	if(applicationContext!=null){
    		return (T) applicationContext.getBean(name);
    	}else{
    		return null;
    	}
    }
    
    /**
     * common
     * 取得HttpSession
     * @return
     */
    public static HttpSession getHttpSession() {
		WebSessionHolder webSessionHolder=SpringContextHolder.getBean("webSessionHolder");
		return webSessionHolder.getHttpSession();
	}

    /**
     * chb
     * @return
     */
    public static Domain getDomain() {
    	return getLoginUser().getDomain();
    }
    
    /**
     * chb
     * @return
     */
    public static User getLoginUser() {
		return (User) getHttpSession().getAttribute("loginUser");
    }
    
    /**
     * jrh
     * 获取登录用户登录系统所使用的角色类型
     * @return
     */
    public static RoleType getRoleType() {
    	return (RoleType) getHttpSession().getAttribute("roleType");
    }
    
    /**
     * jrh	获取当前登录CSR用户的分机号
     * @return	
     */
    public static String getExten() {
    	return (String) getHttpSession().getAttribute("exten");
    }
    
    /**
     * jrh
     * @return	返回存放功能模块名称的String 集合
     */
    @SuppressWarnings("unchecked")
	public static ArrayList<String> getBusinessModel() {
//System.err.println(getHttpSession().getAttribute("businessModels"));
    	return (ArrayList<String>) getHttpSession().getAttribute("businessModels");
    }
    
    /**
     * jrh	获取当前计算机系统正在使用的屏幕分辨率
     * @return	Map<Integer, Integer>
     */
	public static Integer[] getScreenResolution() {
    	return (Integer[]) getHttpSession().getAttribute("screenResolution");
    }
	
	/**
	 * jrh  获取CSR用户当前用户的“我的任务模块” 的显示界面
	 * @return MyTaskTabView
	 */
	public static MyMarketingTaskTabView getMyTaskTabView() {
		return (MyMarketingTaskTabView) getHttpSession().getAttribute("myTaskTabView");
	}
	
	
	/**
	 * jrh  获取CSR用户当前用户的“我的问卷模块” 的显示界面
	 * @return QuestionnaireTaskTabView
	 */
	public static QuestionnaireTaskTabView getQuestionnaireTaskTabView() {
		return (QuestionnaireTaskTabView) getHttpSession().getAttribute("questionnaireTaskTabView");
	}
	
	/**
	 * chb 获取主窗口，显示Notification信息
	 * @return MyTaskTabView
	 */
	public static Window getMainWindow() {
		return (Window) getHttpSession().getAttribute("mainWindow");
	}
	
	
	
}