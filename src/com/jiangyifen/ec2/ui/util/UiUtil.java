package com.jiangyifen.ec2.ui.util;

import com.vaadin.ui.AbstractField;
/**
 * 界面组件的工具类
 * @author chb
 *
 */
public class UiUtil {
	public static String getComponentValue(AbstractField abstractField){
		return abstractField.getValue()==null?"":abstractField.getValue().toString().trim();
	}
}
