package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.KbInfoType;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：知识类型管理
* 
* @author lxy
*
*/
public interface KbInfoTypeService extends FlipSupportService<KbInfoType>  {
	
	
	/**
	 * 根据主键ID获得知识类型
	 * @param id	主键ID 
	 * @return		知识类型，一条或null
	 */
	@Transactional
	public KbInfoType getKbInfoTypeById(Long id);
	
	/**
	 * 保存知识类型
	 * @param kbInfoType	知识类型
	 * 
	 */
	@Transactional
	public void saveKbInfoType(KbInfoType kbInfoType);
	
	/**
	 * 更新知识类型
	 * @param kbInfoType	知识类型
	 * 
	 */
	@Transactional
	public KbInfoType updateKbInfoType(KbInfoType kbInfoType);
	
	/**
	 * 删除知识类型
	 * @param kbInfoType	知识类型
	 * 
	 */
	@Transactional
	public void deleteKbInfoType(KbInfoType kbInfoType);
	
	/**
	 * 获得该域下面的所有一级类别
	 * @param domainid
	 * @return
	 */
	public List<KbInfoType> getKbInfoTypeListByDomain(Long domainid);
	
	/**
	 * 根据父节点ID获得自对象列表
	 * @param Id
	 * @return
	 */
	@Transactional
	public List<KbInfoType> getKbInfoTypeListByParenetId(Long id,Long domainid);
	
	/**
	 * 根据父节点ID获得自对象总数
	 * @param Id
	 * @return
	 */
	@Transactional
	public int getKbInfoTypeCountByParenetId(Long id,Long domainid);

	/**
	 * 判断该域下，根目录下是否存在该名称的类型
	 * @param name
	 * @param domainid
	 * @return
	 */
	public int getKbInfoTypeCountByName(String name, Long domainid);

	/**
	 * 判断该域下，根目录下,该父类下 是否存在该名称的类型
	 * @param name
	 * @param selectType
	 * @param domainid
	 * @return
	 */
	public int getKbInfoTypeCountByNameAndParent(String name,long selectType, Long domainid);

}