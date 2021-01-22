package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.MemoryMethod;

/**
 * Eao接口：内存数据信息
 * 
 * @author JHT
 */
public interface MemoryMethodEao  extends BaseEao{

	/**
	 * 根据员工编号或员工姓名进行查询员工的ID
	 * @param sql 传入的sql语句
	 * @return 返回员工的ID
	 */
	public Long getUserIdByEmpnoOrRealname(String sql);
	
	/**
	 * 获取所有的内存方法数据
	 * @return
	 */
	public List<MemoryMethod> getAllMemoryMethods();
	
	/**
	 * 根据Sql语句查询出一些数据信息
	 * @param sql
	 * @return
	 */
	public List<Object[]> getSqlByMemoryMethods(String sql);
}
