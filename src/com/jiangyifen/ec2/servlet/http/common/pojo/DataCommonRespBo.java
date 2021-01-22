package com.jiangyifen.ec2.servlet.http.common.pojo;

/**
 * 
 * @Description 描述：第三方系统调用 Ec2 系统中的 查询数据接口后返回的信息
 *
 * @author jinht
 *
 * @date 2015-6-4 上午9:23:18 
 *
 */
public class DataCommonRespBo {

	private Integer code;			// 状态码
	private String message;		// 描述信息
	private Integer totalCount;	// 总记录数
	private Object results;		// 返回结果信息
	
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
	public Object getResults() {
		return results;
	}
	public void setResults(Object results) {
		this.results = results;
	}
	
	@Override
	public String toString() {
		return "DataCommonRespBo [code=" + code + ", message=" + message + ", totalCount=" + totalCount + ", results=" + results + "]";
	}
	
}
