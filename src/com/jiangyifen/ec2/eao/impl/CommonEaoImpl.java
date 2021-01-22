package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.utils.LoggerUtil;

public class CommonEaoImpl extends BaseEaoImpl implements CommonEao {

	@Override
	public Object excuteNativeSql(String nativeSql, ExecuteType executeType) {
		if(executeType.equals(ExecuteType.SINGLE_RESULT)){
			return getEntityManager().createNativeQuery(nativeSql).getSingleResult();
		} else if (executeType.equals(ExecuteType.RESULT_LIST)) {
			return getEntityManager().createNativeQuery(nativeSql).getResultList();
		} else if (executeType.equals(ExecuteType.UPDATE)) {
			return getEntityManager().createNativeQuery(nativeSql).executeUpdate();
		}else{
			return null;
		}
	}

	@Override
	public List<?> loadStepRows(String nativeSql, int stepSize) {
		return getEntityManager().createNativeQuery(nativeSql).setFirstResult(0).setMaxResults(stepSize).getResultList();
	}

	/**
	 * 执行Sql
	 */
	@Override
	public Object excuteSql(String sql,ExecuteType executeType) {
		if(executeType==ExecuteType.SINGLE_RESULT){
			@SuppressWarnings("unchecked")
			List<Object> results=this.getEntityManager().createQuery(sql).getResultList();
			if(results.size()>1){
				LoggerUtil.logError(this, "chb： 不应该用取单个实例的方法取实例：类-->"+results.get(0).getClass().getName()+" Sql-->"+sql);
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

