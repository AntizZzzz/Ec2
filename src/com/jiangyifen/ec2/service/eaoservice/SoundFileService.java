package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface SoundFileService extends FlipSupportService<SoundFile> {
	// ========  common method  ========//
	
	@Transactional
	public SoundFile get(Object primaryKey);
	
	@Transactional
	public void save(SoundFile soundFile);

	@Transactional
	public void update(SoundFile soundFile);

	@Transactional
	public void delete(SoundFile soundFile);
	
	@Transactional
	public void deleteById(Object primaryKey);

	/**
	 * chb 选择声音文件
	 * @param domain
	 * @return
	 */
	public List<SoundFile> getAll(Domain domain);
	
	/**
	 * jrh 在指定域下，根据语音文件的编号，检查这个语音文件是否存在关联的IVR对象，如IVRMenu、IVRAction
	 * 	
	 * @param soundFileId	待检验的语音对象的编号
	 * @param domainId		指定租户所属域的编号
	 * @return boolean		返回是否可以删除
	 */
	@Transactional
	public boolean checkDeleteAbleByIVR(Long soundFileId, Long domainId);

}
