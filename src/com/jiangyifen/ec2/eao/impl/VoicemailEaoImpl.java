package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.VoicemailEao;
import com.jiangyifen.ec2.entity.Voicemail;

/**
 * 
 * 语音留言 Eao 接口实现操作类
 *
 * @author jinht
 *
 * @date 2015-6-15 下午7:52:53 
 *
 */
public class VoicemailEaoImpl extends BaseEaoImpl implements VoicemailEao {

	@SuppressWarnings("unchecked")
	@Override
	public List<Voicemail> findByJpql(String jpql) {
		return this.getEntityManager().createQuery(jpql).getResultList();
	}

	
}
