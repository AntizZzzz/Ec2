package com.jiangyifen.ec2.eao;

import java.util.List;

public interface ReportEao extends BaseEao {
	public List<Object[]> getMoreRecord(String sql);

	public List<Object> getOneRecord(String sql);

	public List<Object> getObjects(String sql);

	public void delete(String sql);

	/**
	 * jrh 获取满足指定条件的所有 entity
	 * 
	 * @param jpql
	 *            查询条件
	 * @return List
	 */
	public List<Object> getAll(String jpql);

	/**
	 * 取得多条记录,根据Sql语句进行控制返回结果
	 * 
	 * @param sql
	 * @return
	 */
	public List<Object[]> getRecords(String sql);

	/**
	 * 取得一条记录,返回为Object,根据Sql语句进行控制返回结果
	 * 
	 * @param sql
	 * @return
	 */
	public Object getSingleRecord(String sql);

	/**
	 * 根据Sql去执行更新或者删除操作
	 * 
	 * @param sql
	 */
	public int executeUpdate(String sql);
}
