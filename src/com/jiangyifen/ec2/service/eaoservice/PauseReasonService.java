package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.PauseReason;

public interface PauseReasonService {
	
	// ========  enhanced method  ========//
	
	/**
	 * 根据指定域获取指定域的置忙原因
	 * @param domain  域
	 * @param enabled 可用状态
	 * @return List<PauseReason>
	 */
	public List<PauseReason> getAllByEnabled(Domain domain, boolean enabled);


	// ========  common method  ========//
	
	@Transactional
	public PauseReason get(Object primaryKey);
	
	@Transactional
	public void save(PauseReason pauseReason);

	@Transactional
	public void update(PauseReason pauseReason);

	@Transactional
	public void delete(PauseReason pauseReason);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}
