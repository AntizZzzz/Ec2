package com.jiangyifen.ec2.ui.mgr.questionnaire.utils;

public class WorkUIUtils {

	/**
	 * 判断给组件的value是否存在字符创,前后空格不算值
	 * 
	 * @param strobj
	 * @return 有值返回false
	 */
	public static boolean stringIsEmpty(Object strobj) {
		if ((null != strobj)) {
			String tem = strobj.toString();
			if (	(null != tem) 
					&& (tem.trim() != null)
					&& (tem.trim().length() > 0))
			{
				return false;
			}else{
				return true;
			}
		} else {
			return true;
		}
	}
	
	/**
	 * 
	 * @param info
	 * @return
	 */
	public static String fontColorHtmlString(String info){
		return fontColorHtmlString(info,"red");
	}
	
	/**
	 * 
	 * @param info
	 * @param color
	 * @return
	 */
	public static String fontColorHtmlString(String info,String color){
		StringBuffer fontBuffer = new StringBuffer("");
		if(null == color){
			color = "red";
		}
		fontBuffer.append("<font");
		fontBuffer.append(" color='");
		fontBuffer.append(color);
		fontBuffer.append("'>");
		fontBuffer.append(info);
		fontBuffer.append("</font>");
		
		return fontBuffer.toString();
	}
}
