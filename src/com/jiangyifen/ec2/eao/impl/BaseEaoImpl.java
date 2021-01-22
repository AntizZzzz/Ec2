package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.eao.BaseEao;

public class BaseEaoImpl implements BaseEao {
	private EntityManager entityManager;

	public <T> T get(Class<T> entityClass, Object primaryKey) {
		return entityManager.find(entityClass, primaryKey);
	}

	public void save(Object entity) {
		entityManager.persist(entity);
	}

	public Object update(Object entity) {
		return entityManager.merge(entity);
	}

	public <T> void delete(Class<T> entityClass, Object primaryKey) {
		entityManager.remove(entityManager.getReference(entityClass, primaryKey));
	}

	public void delete(Object entity) {
		entityManager.remove(entity);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List loadPageEntities(int start, int length, String sql) {
		// 无日期格式,直接用字符串查询
		List result = entityManager.createQuery(sql).setFirstResult(start)
				.setMaxResults(length).getResultList();
		return result;
	}

	@Override
	public int getEntityCount(String sql) {
		Long count = (long) 0;
		try {
			count = (Long) entityManager.createQuery(sql).getSingleResult();
		} catch (NoResultException e) {
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.error(e.getMessage(), e);
		}
		return count.intValue();
	}

	@Override
	public int getEntityCountByNativeSql(String nativeSql) {
		Long count = (Long) entityManager.createNativeQuery(nativeSql).getSingleResult();
		return count.intValue();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List loadPageEntitiesByNativeSql(int start, int length,
			String nativeSql) {
		// 无日期格式,直接用字符串查询
		List result = entityManager.createNativeQuery(nativeSql)
				.setFirstResult(start).setMaxResults(length).getResultList();
		return result;
	}

	// /**
	// * 执行原生sql查询
	// * @param nativeSql
	// * @return
	// */
	// @Override
	// public Object excuteNativeSql(String nativeSql,ExecuteType executeType) {
	// if(executeType.equals(ExecuteType.SINGLE_RESULT)){
	// return entityManager.createNativeQuery(nativeSql).getSingleResult();
	// }else if(executeType.equals(ExecuteType.RESULT_LIST)){
	// return entityManager.createNativeQuery(nativeSql).getResultList();
	// }else if(executeType.equals(ExecuteType.UPDATE)){
	// return entityManager.createNativeQuery(nativeSql).executeUpdate();
	// }else{
	// return null;
	// }
	// }

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> loadPageInfosByNativeSql(int startIndex, int pageSize, String nativeSelectSql) {
		if(nativeSelectSql == null || "".equals(nativeSelectSql)) {
			return null;
		}
		// 无日期格式,直接用字符串查询
		List<Object[]> result = entityManager.createNativeQuery(nativeSelectSql).setFirstResult(startIndex).setMaxResults(pageSize).getResultList();
		return result;
	}

	@Override
	public int getInfoCountByNativeSql(String nativeCountSql) {
		if(nativeCountSql == null || "".equals(nativeCountSql)) {
			return 0;
		}
		Long count = (Long) entityManager.createNativeQuery(nativeCountSql).getSingleResult();
		return count.intValue();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getEntitiesByJpql(String jpql) {
		 
		return entityManager.createQuery(jpql).getResultList();
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}
	
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}


}
