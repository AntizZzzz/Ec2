package com.jiangyifen.ec2.eao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.OutlinePoolOutlineLink;

public interface OutlinePoolOutlineLinkEao extends BaseEao {

	/**
	 * 重写 BaseEao 中的保存实体方法
	 * @param entity	 需要保存的实体
	 */
	@Transactional
	public void save(OutlinePoolOutlineLink outlinePoolOutlineLink);
	
	/**
	 * 获取指定域中号码池中的所有外线id
	 * @return List<OutlinePool>
	 */
	public List<OutlinePoolOutlineLink> getAllByDomain(Long poolId);
	
	/**
	 * 删除号码池对应关系
	 */
	@Transactional
	public void delete(Long poolId);
	
}
