package com.jiangyifen.ec2.servlet.http.common.pojo;

/**
 * @Description 描述信息: 武睿定制开发, 按需求实现功能:
 * 	1. 返回呼入分机振铃弾屏信息
 *  2. 返回呼入呼出接起信息
 *  3. 返回呼入呼出挂断信息 
 *
 * @auther jinht
 *
 * @date 2015-11-30 下午8:30:58
 */
public class TimeInfoVo {

	/** 状态码 */
	private String code;
	
	/** 描述信息 */
	private String message;
	
	/** 分机振铃信息 */
	private Object resultsPhoneIn;
	
	/** 呼入呼出接起信息 */
	private Object resultsBridge;
	
	/** 呼入呼出挂断信息 */
	private Object resultsHangUp;

	// getter and setters
	
	/**
	 * 获取 状态码
	 * @return code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * 设置 状态码
	 * @param code 状态码
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * 获取 描述信息
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 设置 描述信息
	 * @param message 描述信息
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 获取 分机振铃信息
	 * @return resultsPhoneIn
	 */
	public Object getResultsPhoneIn() {
		return resultsPhoneIn;
	}

	/**
	 * 设置 分机振铃信息
	 * @param resultsPhoneIn 分机振铃信息
	 */
	public void setResultsPhoneIn(Object resultsPhoneIn) {
		this.resultsPhoneIn = resultsPhoneIn;
	}

	/**
	 * 获取 呼入呼出接起信息
	 * @return resultsBridge
	 */
	public Object getResultsBridge() {
		return resultsBridge;
	}

	/**
	 * 设置 呼入呼出接起信息
	 * @param resultsBridge 呼入呼出接起信息
	 */
	public void setResultsBridge(Object resultsBridge) {
		this.resultsBridge = resultsBridge;
	}

	/**
	 * 获取 呼入呼出挂断信息
	 * @return resultsHangUp
	 */
	public Object getResultsHangUp() {
		return resultsHangUp;
	}

	/**
	 * 设置 呼入呼出挂断信息
	 * @param resultsHangUp 呼入呼出挂断信息
	 */
	public void setResultsHangUp(Object resultsHangUp) {
		this.resultsHangUp = resultsHangUp;
	}

	// toString method

	@Override
	public String toString() {
		return "TimeInfoVo [code=" + code + ", message=" + message
				+ ", resultsPhoneIn=" + resultsPhoneIn + ", resultsBridge="
				+ resultsBridge + ", resultsHangUp=" + resultsHangUp + "]";
	}
	
}
