package com.jiangyifen.ec2.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 描述：业务处理结果信息封装对象
 *
 * @author  JRH
 * @date    2014年6月24日 上午11:10:50
 * @version v1.0.0
 */
public class BusHandle {
	
	/*
	 * 业务操作是否成功
	 */
	private boolean success = false;	
	
	/*
	 * 业务操作返回信息
	 */
	private String notice = "";

	/*
	 * 业务操作返回信息List集合
	 */
	private List<String> resultList = new ArrayList<String>();

	/*
	 * 业务操作返回信息Map集合
	 */
	private Map<String, Object> resultMap = new HashMap<String, Object>();

	/**
	 * 获取业务操作是否成功
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * 设置业务操作是否成功
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * 获取业务操作返回信息
	 */
	public String getNotice() {
		return notice;
	}

	/**
	 * 设置业务操作返回信息
	 */
	public void setNotice(String notice) {
		this.notice = notice;
	}

	/**
	 * 获取业务操作返回信息List集合
	 */
	public List<String> getResultList() {
		return resultList;
	}

	/**
	 * 设置业务操作返回信息List集合
	 */
	public void setResultList(List<String> resultList) {
		this.resultList = resultList;
	}

	/**
	 * 获取业务操作返回信息Map集合
	 */
	public Map<String, Object> getResultMap() {
		return resultMap;
	}

	/**
	 * 设置业务操作返回信息Map集合
	 */
	public void setResultMap(Map<String, Object> resultMap) {
		this.resultMap = resultMap;
	}
	
}
