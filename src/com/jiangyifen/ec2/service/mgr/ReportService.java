package com.jiangyifen.ec2.service.mgr;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

public interface ReportService {

	// =========== enhanced method ===========//

	@Transactional
	public List<Object[]> getMoreRecord(String sql);

	@Transactional
	public List<Object> getOneRecord(String sql);

	@Transactional
	public Object getSingleRecord(String sql);

	@Transactional
	public void delete(String sql);

	@Transactional
	public int executeUpdate(String sql);

	@Transactional
	public List<Object> getObjects(String sql);

	/**
	 * 获取满足指定条件的所有 entity
	 * 
	 * @param jpql
	 *            查询条件
	 * @return List
	 */
	@Transactional
	public List<Object> getAll(String jpql);

	@Transactional
	public List<Object[]> getRecords(String sql);

	// =========== common method ===========//

	@Transactional
	public void save(Object object);

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
	 * 更新实体
	 * 
	 * @param entity
	 *            需要更新的实体
	 */
	@Transactional
	public void update(Object entity);

	/**
	 * 删除实体
	 * 
	 * @param <T>
	 * @param entityClass
	 *            需要删除实体类
	 * @param pk
	 *            需要删除的实体主键
	 */
	@Transactional
	public <T> void delete(Class<T> entityClass, Object pk);

	/**
	 * 删除实体
	 * 
	 * @param entity
	 */
	@Transactional
	public void delete(Object entity);

}
