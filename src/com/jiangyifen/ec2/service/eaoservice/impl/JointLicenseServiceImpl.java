package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.JointLicenseEao;
import com.jiangyifen.ec2.entity.JointLicense;
import com.jiangyifen.ec2.service.eaoservice.JointLicenseService;

/**
 * @Description 描述：对接许可证  服务类Impl
 *
 * @author  JRH
 * @date    2014年8月7日 下午4:29:10
 */
public class JointLicenseServiceImpl implements JointLicenseService {
	
	private JointLicenseEao jointLicenseEao;

	// getter setter
	
	public JointLicenseEao getJointLicenseEao() {
		return jointLicenseEao;
	}

	public void setJointLicenseEao(JointLicenseEao jointLicenseEao) {
		this.jointLicenseEao = jointLicenseEao;
	}

	
	// common method

	@Override
	public JointLicense get(Object primaryKey) {
		return jointLicenseEao.get(JointLicense.class, primaryKey);
	}

	@Override
	public void save(JointLicense jointLicense) {
		jointLicenseEao.save(jointLicense);
	}

	@Override
	public JointLicense update(JointLicense jointLicense) {
		return (JointLicense) jointLicenseEao.update(jointLicense);
	}

	@Override
	public void delete(JointLicense jointLicense) {
		jointLicenseEao.delete(jointLicense);
	}

	@Override
	public void deleteById(Object primaryKey) {
		jointLicenseEao.delete(JointLicense.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<JointLicense> loadPageEntities(int start, int length, String sql) {
		return jointLicenseEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return jointLicenseEao.getEntityCount(sql);
	}

	
	// enhanced method
	
	@Override
	public JointLicense getByIdKey(String accessId, String accessKey) {
		return jointLicenseEao.getByIdKey(accessId, accessKey);
	}
	
	
}
