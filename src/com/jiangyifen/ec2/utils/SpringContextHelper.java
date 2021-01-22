package com.jiangyifen.ec2.utils;

import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.Component;

public class SpringContextHelper {

    public static Object getBean(Application application, final String beanRef) {
        ServletContext servletContext = ((WebApplicationContext) application.getContext()).getHttpSession().getServletContext();
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return context.getBean(beanRef);
    }

    public static HttpSession getSession(Application application) {
    	 return ((WebApplicationContext) application.getContext()).getHttpSession();
    }

    @SuppressWarnings("unchecked")
	public static Collection<String> getBusinessModel(Component component) {
    	HttpSession session=((WebApplicationContext) component.getApplication().getContext()).getHttpSession();
    	return (Collection<String>)session.getAttribute("businessModel");
    }

    public static void setBusinessModel(Component component,Collection<String> businessModel) {
    	HttpSession session=((WebApplicationContext) component.getApplication().getContext()).getHttpSession();
    	session.setAttribute("businessModel", businessModel);
    }
    
    public static Long getDomainId(Component component) {
    	HttpSession session=((WebApplicationContext) component.getApplication().getContext()).getHttpSession();
    	User loginUser = (User) session.getAttribute("loginUser");
    	return loginUser.getDomain().getId();
    }

    public static Domain getDomain(Component component) {
    	HttpSession session=((WebApplicationContext) component.getApplication().getContext()).getHttpSession();
    	User loginUser = (User) session.getAttribute("loginUser");
    	return loginUser.getDomain();
    }

    public static User getLoginUser(Component component) {
    	HttpSession session=((WebApplicationContext) component.getApplication().getContext()).getHttpSession();
    	User loginUser = (User) session.getAttribute("loginUser");
    	return loginUser;
    }

}