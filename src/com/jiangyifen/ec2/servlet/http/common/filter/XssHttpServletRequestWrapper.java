package com.jiangyifen.ec2.servlet.http.common.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @Description 描述：在request到达servlet的服务方法之前拦截HttpServletRequest对象，并进行包装处理
 * 
 * @author  jrh
 * @date    2014年3月3日 下午2:30:59
 * @version v1.0.0
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {  
    
	public XssHttpServletRequestWrapper(HttpServletRequest servletRequest) {
		super(servletRequest);
	}

	@Override
	public String[] getParameterValues(String parameter) {
		String[] values = super.getParameterValues(parameter);
		if (values == null) {
			return null;
		}
		int count = values.length;
		String[] encodedValues = new String[count];
		for (int i = 0; i < count; i++) {
			encodedValues[i] = cleanAndTrimXSS(values[i]);
		}
		return encodedValues;
	}

	@Override
	public String getParameter(String parameter) {
		String value = super.getParameter(parameter);
		if (value == null) {
			return null;
		} 
//		System.out.println(value+ "《---前    filter 包装处理     后  ---》" + cleanAndTrimXSS(value));
		return cleanAndTrimXSS(value);
	}

	@Override
	public String getHeader(String name) {
		String value = super.getHeader(name);
		if (value == null)
			return null;
		return cleanAndTrimXSS(value);
	}

	/**
	 * 清理跨站脚本、并且将参数的前后空格去掉 XSS - cross-site scripting
	 * @param value
	 * @return
	 */
	private String cleanAndTrimXSS(String value) {
		value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
		value = value.replaceAll("'", "&#39;");
		value = value.replaceAll("eval\\((.*)\\)", "");
		value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']","\"\"");
		value = value.replaceAll("script", "");
		value = StringUtils.trim(value);
		return value;
	}
	
} 