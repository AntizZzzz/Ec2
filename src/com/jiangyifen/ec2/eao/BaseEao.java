package com.jiangyifen.ec2.eao;

import java.util.List;

import javax.persistence.EntityManager;

public interface BaseEao {
	/**
	 * 查找实体
	 * 
	 * @param <T>
	 *            动态传入实体类
	 * @param entityClass
	 *            实体类
	 * @param pk
	 *            主键
	 * @return 根据指定主键返回的实体
	 */
	public <T> T get(Class<T> entityClass, Object pk);

	/**
	 * 保存实体
	 * 
	 * @param entity
	 *            需要保存的实体
	 */
	public void save(Object entity);

	/**
	 * 更新实体
	 * 
	 * @param entity
	 *            需要更新的实体
	 */
	public Object update(Object entity);

	/**
	 * 删除实体
	 * @param <T>
	 * 
	 * @param entityClass
	 *            需要删除实体类
	 * @param pk
	 *            需要删除的实体主键
	 */
	public <T> void delete(Class<T>entityClass, Object pk);
	
	/**
	 * 删除实体
	 * @param <Role>
	 * 
	 * @param entity
	 */
	public void delete(Object entity);
	
	/**
	 * 根据SQL语句取得所有实例
	 * @param start 从第几个实力开始取
	 * @param length 取出几个实例
	 * @param sql 输入的SQL语句
	 * @return 查询到的结果集
	 */
	@SuppressWarnings("rawtypes")
	public List loadPageEntities(int start,int length,String sql);
	
	/**
	 * 查询到的实例个数
	 * @param sql 
	 * @return 符合条件的记录条数
	 */
	public int getEntityCount(String sql);
	
	/**
	 * 查询到的实例个数
	 * @param nativeSql 
	 * @return 符合条件的记录条数
	 */
	public int getEntityCountByNativeSql(String nativeSql);
	
	/**
	 * 根据SQL语句取得所有实例
	 * @param start 从第几个实力开始取
	 * @param length 取出几个实例
	 * @param nativeSql 输入的SQL语句
	 * @return 查询到的结果集
	 */
	@SuppressWarnings("rawtypes")
	public List loadPageEntitiesByNativeSql(int start, int length, String nativeSql);
	
//	/**
//	 * 执行原生sql查询
//	 * @param nativeSql
//	 * @return
//	 */
//	public Object excuteNativeSql(String nativeSql,ExecuteType executeType);
	
	/**
	 * 取得EntityManager
	 */
	public EntityManager getEntityManager();
	

	/**
	 * jrh 分页加载
	 * 
	 * @param startIndex
	 *           	 第几个Object[]开始
	 * @param pageSize
	 *           	 查询几个Object[]
	 * @param selectSql
	 *           	 查询语句
	 * @return List<Object[]> 
	 * 				查询出的实体的List集合
	 */
	public List<Object[]> loadPageInfosByNativeSql(int startIndex, int pageSize, String nativeSelectSql);

	/**
	 * jrh 数据库记录条数 (如果查询语句为空字符串 "" ,则查询数据空中对应实体的总记录数；否则按照查询条件进行查询)
	 * 
	 * @param nativeCountSql
	 *            查询条数的语句
	 * @return 查询的记录条数
	 */
	public int getInfoCountByNativeSql(String nativeCountSql);
	
	/**
	 * @param jpql
	 * @return 返回实体集合
	 */
	public <T> List<T> getEntitiesByJpql(String jpql);
	
}