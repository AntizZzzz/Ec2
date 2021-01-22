package com.jiangyifen.ec2.service.common;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.eao.BaseEao;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;

@SuppressWarnings("rawtypes")
public interface CommonService extends BaseEao,FlipSupportService{
	@Transactional
	public Object excuteNativeSql(String nativeSql,ExecuteType executeType);
	public List<?> loadStepRows(String nativeSql,int stepSize);
	public EntityManager getEntityManager();
	/**
	 * 执行sql语句
	 * @param sql
	 * @return
	 */
	@Transactional
	public Object excuteSql(String sql,ExecuteType executeType);


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
	@Transactional
	public <T> T get(Class<T> entityClass, Object pk);

	/**
	 * 保存实体
	 * 
	 * @param entity
	 *            需要保存的实体
	 */
	@Transactional
	public void save(Object entity);

	/**
	 * 更新实体
	 * 
	 * @param entity
	 *            需要更新的实体
	 */
	@Transactional
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
	@Transactional
	public <T> void delete(Class<T>entityClass, Object pk);
	
	/**
	 * 删除实体
	 * @param <Role>
	 * 
	 * @param entity
	 */
	@Transactional
	public void delete(Object entity);
	
	/**
	 * 根据SQL语句取得所有实例
	 * @param start 从第几个实力开始取
	 * @param length 取出几个实例
	 * @param sql 输入的SQL语句
	 * @return 查询到的结果集
	 */
	@Transactional
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
	public List loadPageEntitiesByNativeSql(int start, int length, String nativeSql);
	
	 
	//通过jpql获取实体集合
	public <T> List<T> getEntitiesByJpql(String jpql);
	 
}
