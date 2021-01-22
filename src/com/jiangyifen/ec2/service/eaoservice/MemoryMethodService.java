package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import com.jiangyifen.ec2.entity.MemoryMethod;

/**
 * Service接口：内存数据信息
 * 
 * @author JHT
 */
public interface MemoryMethodService {

	/**
	 * 根据员工编号或姓名进行查询出员工的ID
	 * @param keyWord 传入的参数
	 * @return 员工的ID
	 */
	public Long findUserIdByEmpnoOrRealname(String keyWord);
	
	/**
	 * 查询出所有的内存方法数据
	 * @return
	 */
	public List<MemoryMethod> findAllMemoryMethods();
	
	/**
	 * 根据名字进行查询数据信息
	 * @param keyName
	 * @return
	 */
	public List<MemoryMethod> findByKeyWord(String keyName);
	
}
