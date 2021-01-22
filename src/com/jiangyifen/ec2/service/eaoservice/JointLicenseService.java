package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.JointLicense;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
 * @Description 描述：对接许可证  服务类
 *
 * @author  JRH
 * @date    2014年8月7日 下午4:06:47
 */
public interface JointLicenseService extends FlipSupportService<JointLicense> {
	
	// common method 
	
	@Transactional
	public JointLicense get(Object primaryKey);
	
	@Transactional
	public void save(JointLicense jointLicense);

	@Transactional
	public JointLicense update(JointLicense jointLicense);

	@Transactional
	public void delete(JointLicense jointLicense);
	
	@Transactional
	public void deleteById(Object primaryKey);

	// enhanced method
	
	/**
	 * @Description 描述：根据访问编号跟访问秘钥获取对接许可对象
	 *
	 * @author  JRH
	 * @date    2014年8月7日 下午4:09:35
	 * @param accessId	访问编号
	 * @param accessKey 访问秘钥
	 * @return JointLicense	对接许可对象
	 */
	@Transactional
	public JointLicense getByIdKey(String accessId, String accessKey); 
	
}
