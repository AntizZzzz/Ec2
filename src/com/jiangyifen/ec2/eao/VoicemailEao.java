package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Voicemail;

/**
 * 
 * 语音留言 Eao 接口操作类
 *
 * @author jinht
 *
 * @date 2015-6-15 下午7:51:32 
 *
 */
public interface VoicemailEao extends BaseEao {
	
	public List<Voicemail> findByJpql(String jpql);
	
}
