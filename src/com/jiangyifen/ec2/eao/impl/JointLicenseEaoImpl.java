package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.JointLicenseEao;
import com.jiangyifen.ec2.entity.JointLicense;

/**
 * @Description 描述：对接许可证  EaoImpl
 *
 * @author  JRH
 * @date    2014年8月7日 下午3:57:59
 */
public class JointLicenseEaoImpl extends BaseEaoImpl implements JointLicenseEao {

	@SuppressWarnings("unchecked")
	@Override
	public JointLicense getByIdKey(String accessId, String accessKey) {
		String jpql = "select jl from JointLicense as jl where jl.accessId = '"+accessId+"' and jl.accessKey = '"+accessKey+"'";
		List<JointLicense> jlLs = this.getEntityManager().createQuery(jpql).getResultList();
		
		if(jlLs.size() > 0) {
			return jlLs.get(0);
		}
		
		return null;
	}

}
