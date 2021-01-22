package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ReportEao;

/**
 * @author chb
 */
public class ReportEaoImpl extends BaseEaoImpl implements ReportEao {
	/**
	 * 取得多条记录,根据Sql语句进行控制返回结果
	 * 
	 * @param sql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getRecords(String sql) {
		return getEntityManager().createNativeQuery(sql).getResultList();
	}

	/**
	 * 取得一条记录,返回为Object,根据Sql语句进行控制返回结果
	 * 
	 * @param sql
	 * @return
	 */
	@Override
	public Object getSingleRecord(String sql) {
		return getEntityManager().createNativeQuery(sql).getSingleResult();
	}

	/**
	 * 根据Sql去执行更新或者删除操作
	 * 
	 * @param sql
	 */
	@Override
	public int executeUpdate(String sql) {

		return getEntityManager().createNativeQuery(sql).executeUpdate();

	}

	public void delete(String sql) {
		getEntityManager().createNativeQuery(sql).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getAll(String jpql) {
		return getEntityManager().createQuery(jpql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getMoreRecord(String sql) {

		return getEntityManager().createNativeQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getOneRecord(String sql) {
		return getEntityManager().createNativeQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> getObjects(String sql) {

		return getEntityManager().createNativeQuery(sql).getResultList();
	}

}
