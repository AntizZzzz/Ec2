package com.jiangyifen.ec2.eao;

import com.jiangyifen.ec2.entity.JointLicense;

/**
 * @Description 描述：对接许可证  Eao
 *
 * @author  JRH
 * @date    2014年8月7日 下午3:57:53
 */
public interface JointLicenseEao extends BaseEao {

	/**
	 * @Description 描述：根据访问编号跟访问秘钥获取对接许可对象
	 *
	 * @author  JRH
	 * @date    2014年8月7日 下午4:09:35
	 * @param accessId	访问编号
	 * @param accessKey 访问秘钥
	 * @return JointLicense	对接许可对象
	 */
	public JointLicense getByIdKey(String accessId, String accessKey); 
	
}
