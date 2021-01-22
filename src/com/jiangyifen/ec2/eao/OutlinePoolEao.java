package com.jiangyifen.ec2.eao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.OutlinePool;

public interface OutlinePoolEao extends BaseEao {
	
	/**
	 * 重写 BaseEao 中的保存实体方法
	 * @param entity	 需要保存的实体
	 */
	@Transactional
	public void save(OutlinePool outlinePool);
	
	/**
	 * 重写 BaseEao 中的更新实体方法
	 * @param entity	 需要更新的实体
	 */
	@Transactional
	public OutlinePool update(OutlinePool outlinePool);
	
	/**
	 * jrh
	 * 获取指定域中的所有号码池
	 * @param domain 		指定域
	 * @return List<OutlinePool>
	 */
	public List<OutlinePool> getAllByDomain(Domain domai);
	
	public void deleteById(Object primaryKey);
	
	
	public boolean existByName(String name, Domain domain);

}
