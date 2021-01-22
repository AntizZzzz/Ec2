package com.jiangyifen.ec2.service.common.impl;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.eao.impl.BaseEaoImpl;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;

public class CommonServiceImpl extends BaseEaoImpl implements CommonService{
	private CommonEao commonEao;

	@Override
	@Transactional
	public void save(Object object) {
		commonEao.save(object);	
	}
	

	@Override
	@Transactional
	public <T> T get(Class<T> entityClass, Object pk) {
		return commonEao.get(entityClass, pk);
	}


	@Override
	@Transactional
	public Object update(Object entity) {
		return commonEao.update(entity);
	}


	@Override
	@Transactional
	public <T> void delete(Class<T> entityClass, Object pk) {
		commonEao.delete(entityClass,pk);
	}


	@Override
	@Transactional
	public void delete(Object entity) {
		commonEao.delete(entity);
	}


	@SuppressWarnings("rawtypes")
	@Override
	@Transactional
	public List loadPageEntities(int start, int length, String sql) {
		return commonEao.loadPageEntities(start, length, sql);
	}


	@Override
	public int getEntityCount(String sql) {
		return (int)commonEao.getEntityCount(sql);
	}


	@Override
	public int getEntityCountByNativeSql(String nativeSql) {
		return commonEao.getEntityCountByNativeSql(nativeSql);
	}


	@SuppressWarnings("rawtypes")
	@Override
	public List loadPageEntitiesByNativeSql(int start, int length,
			String nativeSql) {
		return commonEao.loadPageEntitiesByNativeSql(start, length, nativeSql);
	}
	
	@Override
	public Object excuteNativeSql(String nativeSql,ExecuteType executeType) {
		return commonEao.excuteNativeSql(nativeSql,executeType);
	}
	
	@Override
	public <T> List<T> getEntitiesByJpql(String jpql){
	
		return commonEao.getEntitiesByJpql(jpql);
	}
	

	/**
	 * 加载指定步长的行
	 * 页数通过nativeSql的Id来控制
	 */
	@Override
	public List<?> loadStepRows(String nativeSql, int stepSize) {
		return commonEao.loadStepRows(nativeSql, stepSize);
	}
	/**
	 * 取得EntityManager
	 */
	public EntityManager getEntityManager(){
		return commonEao.getEntityManager();
	}
	
	//Getter and setter
	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}


	@Override
	public Object excuteSql(String sql, ExecuteType executeType) {
		if(executeType==ExecuteType.SINGLE_RESULT){
			@SuppressWarnings("unchecked")
			List<Object> results=this.getEntityManager().createQuery(sql).getResultList();
			if(results.size()>1){
				return results.get(0);
			}else if(results.size()==1){
				return results.get(0);
			}else{
				return null;
			}
		}else if(executeType==ExecuteType.RESULT_LIST){
			return this.getEntityManager().createQuery(sql).getResultList();
		}else if(executeType==ExecuteType.UPDATE){
			return this.getEntityManager().createQuery(sql).executeUpdate();
		}
		return null;
	}


}
