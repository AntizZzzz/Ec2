package com.jiangyifen.ec2.ui.mgr.questionnaire.utils.ui;

import com.vaadin.ui.TextField;

/**
 * 
 *	<h2>隐藏域</h2><br>
 *	此类TextField，专门用户隐藏数据
 *	<br>
 *	使用
 * 	this.setData(data)
	this.getData()
 * 
 * @author lxy
 *
 */
public class HiddenTextField extends TextField {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8748659340578198451L;
	
	/**
	 *初始化方法隐藏该对象 
	 */
	public HiddenTextField(){
		this.setVisible(false);
		
	}

}
