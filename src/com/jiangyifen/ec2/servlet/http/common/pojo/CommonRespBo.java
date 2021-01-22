package com.jiangyifen.ec2.servlet.http.common.pojo;

/**
 * @Description 描述：第三方系统调用Ec2 系统接口后的返回信息
 *
 * @author  JRH
 * @date    2014年8月8日 上午9:42:07
 */
public class CommonRespBo {
	
	private String code;	// 状态码

	private String message; //描述信息
	
	private Object results; //返回结果集

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getResults() {
		return results;
	}

	public void setResults(Object results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "CommonRespBo [code=" + code + ", message=" + message + ", results=" + results + "]";
	}

}
