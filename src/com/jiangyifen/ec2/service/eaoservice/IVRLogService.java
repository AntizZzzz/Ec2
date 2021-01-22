package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.IVRLog;

public interface IVRLogService {
	// common method

	@Transactional
	public IVRLog get(Object primaryKey);

	@Transactional
	public void save(IVRLog ivrlog);

	@Transactional
	public void update(IVRLog ivrlog);

	@Transactional
	public void delete(IVRLog ivrlog);

	@Transactional
	public void deleteById(Object primaryKey);

}
