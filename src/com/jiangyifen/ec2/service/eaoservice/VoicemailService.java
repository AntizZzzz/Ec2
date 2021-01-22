package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Voicemail;
import com.jiangyifen.ec2.service.common.FlipSupportNativeSqlService;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
 * 
 * 语音留言 Service 接口操作类
 *
 * @author jinht
 *
 * @date 2015-6-15 下午7:53:54 
 *
 */
public interface VoicemailService extends FlipSupportNativeSqlService<Voicemail>, FlipSupportService<Voicemail> {

	@Transactional
	public Voicemail get(Object primaryKey);
	
	@Transactional
	public void save(Voicemail voicemail);
	
	@Transactional
	public Voicemail update(Voicemail voicemail);
	
	@Transactional
	public void delete(Voicemail voicemail);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	/**
	 * 查询这个语音留言信息是否已经在数据库中存在
	 * 
	 * @param userId
	 * @param domainId
	 * @param partFilename
	 * @return
	 */
	@Transactional
	public boolean isAlreadyExists(Voicemail voicemail);
	
}
