package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Notice;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface NoticeService extends FlipSupportService<Notice> {

	// common method 
	
	@Transactional
	public Notice get(Object primaryKey);
	
	@Transactional
	public void save(Notice notice);

	@Transactional
	public Notice update(Notice notice);

	@Transactional
	public void delete(Notice notice);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	// enhanced method
	
	
}
