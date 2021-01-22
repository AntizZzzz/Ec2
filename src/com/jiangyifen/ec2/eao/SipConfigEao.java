package com.jiangyifen.ec2.eao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;

public interface SipConfigEao extends BaseEao {
	
	/**
	 * 重写 BaseEao 中的保存实体方法
	 * @param entity	 需要保存的实体
	 */
	@Transactional
	public void save(SipConfig sipConfig);
	
	/**
	 * 重写 BaseEao 中的更新实体方法
	 * @param entity	 需要更新的实体
	 */
	@Transactional
	public SipConfig update(SipConfig sipConfig);
	
	/**
	 * jrh
	 * 	重写删除方法，按对象删除
	 * 注意：其中还要操作删除外线与黑名单之间的对应关系、 清理外线与IVR 的关联关系
	 * @param sipConfig
	 */
	public void delete(SipConfig sipConfig);
	
	/**
	 * jrh
	 * 	重写删除方法， 按id删除
	 * 注意：其中还要操作删除外线与黑名单之间的对应关系、 清理外线与IVR 的关联关系
	 * @param primaryKey
	 */
	public void deleteById(Object primaryKey);
	
	
	/**
	 * jrh 
	 * 全局范围内检查是否存在该当前分机或外线
	 * @param 	sipname		分机号
	 * @return	boolean 	如果存在则返回true
	 */
	public boolean existBySipname(String sipname);
	
	/**
	 * jrh 
	 * 	指定域范围内，检查指定的域中是否存在该当前分机
	 * @param 	sipname		分机号
	 * @param 	domain		指定域
	 * @return	boolean 	如果存在则返回true
	 */
	public boolean existBySipname(String sipname, Domain domain);
	
	
	/**
	 * jrh 
	 * 获取指定域中的所有分机
	 * @param 	domain		分机所在域
	 * @return List<SipConfig>	存放分机的集合
	 */
	public List<SipConfig> getAllExtsByDomain(Domain domain);
	
	
	/**
	 * jrh 
	 * 获取指定域中的所有外线
	 * @param 	domain		外线所在域
	 * @return List<SipConfig>	存放外线的集合
	 */
	public List<SipConfig> getAllOutlinesByDomain(Domain domain);
	
	/**
	 * jrh
	 * 获取指定域的默认外线
	 * @param domain	域
	 * @return SipConfig 默认外线
	 */
	public SipConfig getDefaultOutlineByDomain(Domain domain);
	
	/**
	 * chb 
	 * 根据外线取得domain
	 * @return
	 */
	public Domain getDomainByOutLine(String outline);


	/**
	 * jrh
	 * 	获取系统中当前最大的分机名称，并将String 转化成Long 类型返回，不包括外线
	 */
	public Long getMaxSipnameInExt();
	
	/**
	 * jrh
	 * 	获取系统中当前最大的分机名称，并将String 转化成Long 类型返回，不包括外线
	 */
	public Long getMaxSipnameInExt6();

	/**
	 * chb 
	 * 根据domain 取得所有没有被其他项目使用的外线
	 * @return
	 */
	public List<SipConfig> getAllUseableOutlinesByDomain(Domain domain);
	/**
	 * chb 
	 *根据外线的名字取出一条外线
	 * @return
	 */
	public SipConfig getOutlineByOutlineName(String outlineName);

	/**
	 * jrh
	 * 	在指定域下，根据分机或外线名称，检查是否与之关联的IVRAction 对象，如果有，则表示不能删除
	 * 
	 * @param isExten	是否为分机对象
	 * @param sipName	分机或外线名称
	 * @param domainId	域编号
	 * @return boolean  删除与否
	 */
	public boolean checkDeleteAbleByIvrAction(boolean isExten, String sipName, Long domainId);

	/**
	 * jrh
	 * 	在指定域下，根据分机或外线名称，检查是否与之关联的IVRAction 对象，如果有，则需要将对应的Action 进行更新
	 * 
	 * 	用于更改外线或分机的名称时，做判断
	 * 
	 * @param isExten	是否为分机对象
	 * @param sipName	分机或外线名称
	 * @param domainId	域编号
	 * @return boolean  删除与否
	 */
	public void checkIvrActionAndUpdate(boolean isExten, String sipName, Long domainId);

}
