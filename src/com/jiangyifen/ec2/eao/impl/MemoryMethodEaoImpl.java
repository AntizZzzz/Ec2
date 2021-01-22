package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.MemoryMethodEao;
import com.jiangyifen.ec2.entity.MemoryMethod;

/**
 * Eao实现类：内存数据信息
 * 
 * @author JHT
 */
public class MemoryMethodEaoImpl extends BaseEaoImpl implements MemoryMethodEao {

	@Override
	public Long getUserIdByEmpnoOrRealname(String sql) {
		Object result = null;
		Long userId = -1L;
		try{
			result = this.getEntityManager().createNativeQuery(sql).getSingleResult();
			userId = Long.parseLong(result.toString());
			return userId;
		} catch (Exception e) {	
			return userId;	
		}
	}

	@Override
	public List<MemoryMethod> getAllMemoryMethods() {
		List<MemoryMethod> allMemoryMethods = getEntityManager().createQuery("select memory from MemoryMethod as memory ", MemoryMethod.class).getResultList();
		return allMemoryMethods;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getSqlByMemoryMethods(String sql){
		return this.getEntityManager().createNativeQuery(sql).getResultList();
	}

}
