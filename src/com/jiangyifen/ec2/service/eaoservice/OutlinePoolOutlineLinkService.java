package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.OutlinePoolOutlineLink;

public interface OutlinePoolOutlineLinkService {
	
	@Transactional
	public void save(OutlinePoolOutlineLink outlinePoolOutlineLink);
	
	public List<OutlinePoolOutlineLink> getAllByPoolId(Long poolId);
	
	@Transactional
	public void delete(Long poolId);

}
