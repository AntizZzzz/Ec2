package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OutlinePool;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface OutlinePoolService extends FlipSupportService<OutlinePool> {

	@Transactional
	public void save(OutlinePool outlinePool);

	@Transactional
	public OutlinePool update(OutlinePool outlinePool);

	public List<OutlinePool> getAllByDomain(Domain domai);

	public boolean existByName(String name, Domain domain);

	@Transactional
	public void deleteById(Object primaryKey);

}
