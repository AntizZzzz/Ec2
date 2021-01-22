package com.jiangyifen.ec2.service.csr.ami;

/**
 * 呼叫客户服务接口
 */
public interface DialService {
	
	/**
	 * 从指定分机号拨打指定电话
	 * @param exten					分机号
	 * @param connectedLineNum		要拨打的号码
	 */
	public Boolean dial(String exten, String connectedLineNum);
	
	/**
	 * 指定外线池或者外线
	 * @param exten
	 * @param connectedLineNum
	 * @param outline
	 * @param poolNum
	 * @return
	 */
	public Boolean dial(String exten, String connectedLineNum, String outline, String poolNum);
	
	/**
	 * 从指定分机号拨打指定电话
	 * @param exten					分机号
	 * @param connectedLineNum		要拨打的号码
	 * @param nonNumericAble		被叫号码是否可以包含数字 true : 可以包含字母， false： 只能是数字
	 */
	public Boolean dial(String exten, String connectedLineNum, boolean nonNumericAble);
	
	/**
	 * @Description 描述：监听指定的通道
	 *
	 * @author  jrh
	 * @date    2014年4月14日 下午9:02:36
	 * @param exten					分机号
	 * @param connectedLineNum		要拨打的号码，拨号方案匹配的电话号码
	 * @param spiedChannel			被监听的通道
	 * @return boolean
	 */
	public boolean channelSpy(String exten, String connectedLineNum, String spiedChannel);
	
}
