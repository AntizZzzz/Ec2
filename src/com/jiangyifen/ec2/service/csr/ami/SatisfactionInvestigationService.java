package com.jiangyifen.ec2.service.csr.ami;

/**
 * 客户满意度调查服务类
 * @author jrh
 *
 */
public interface SatisfactionInvestigationService {
	
	/**
	 * jrh 
	 * 	客户满意度调查,邀请与当前话务员通话的客户做满意度调查
	 * 	当前只能支持一个分机单路通话的情况
	 * @param csrId  		   话务员id
	 * @param callDirection  调查方向
	 * @return	
	 * 			"success" ： 调查成功 
	 * 			"fail" ： 调查失败 
	 * 			"unbridge" ： 当前坐席没有建立通话
	 */
	public String investigation(Long csrId, String callDirection);

}
