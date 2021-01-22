package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface SipConfigService extends FlipSupportService<SipConfig> {

	// enhanced method
	
	/**
	 * jrh 
	 * 全局范围内检查是否存在该当前分机或外线
	 * @param 	sipname		分机号
	 * @return	boolean 	如果存在则返回true
	 */
	@Transactional
	public boolean existBySipname(String sipname);
	
	/**
	 * jrh 
	 * 	指定域范围内，检查指定的域中是否存在该当前分机
	 * @param 	sipname		分机号
	 * @param 	domain		指定域
	 * @return	boolean 	如果存在则返回true
	 */
	@Transactional
	public boolean existBySipname(String sipname, Domain domain);
	
	/**
	 * jrh
	 * 获取指定域中的所有分机
	 * @param 	domain		分机所在域
	 * @return List<SipConfig>	存放分机的集合
	 */
	@Transactional
	public List<SipConfig> getAllExtsByDomain(Domain domain);
	
	/**
	 * jrh
	 * 获取指定域中的所有外线
	 * @param 	domain		外线所在域
	 * @return List<SipConfig>	存放外线的集合
	 */
	@Transactional
	public List<SipConfig> getAllOutlinesByDomain(Domain domain);
	
	/**
	 * jrh
	 * 获取指定域的默认外线
	 * @param domain	域
	 * @return SipConfig 默认外线
	 */
	@Transactional
	public SipConfig getDefaultOutlineByDomain(Domain domain);
	
	/**
	 * jrh
	 *  更新当前域拥有的asterisk 的 sip_exten_domainname.conf 文件
	 *  读取DB 中的sip_conf 表，并将其中的分机信息写入asterisk 的sip_exten_domainname 文件中去
	 * @return boolean 如果为true 表示 更新文件成功
	 */
	@Transactional
	public boolean updateAsteriskExtenSipConfigFile(Domain domain);
	
	/**
	 * jrh
	 *  更新当前域拥有的asterisk 的 sip_register_domainname.conf 文件
	 *  读取DB 中的sip_conf 表，并将其中的外线注册信息写入asterisk 的sip_register_domainname 文件中去
	 * @return boolean 如果为true 表示 更新文件成功
	 */
	@Transactional
	public boolean updateAsteriskRegisterSipConfigFile(Domain domain);
	
	/**
	 * jrh
	 *  更新当前域拥有的asterisk 的 sip_outline_domainname.conf 文件
	 *  读取DB 中的sip_conf 表，并将其中的外线信息写入asterisk 的sip_outline_domainname 文件中去
	 * @return boolean 如果为true 表示 更新文件成功
	 */
	@Transactional
	public boolean updateAsteriskOutlineSipConfigFile(Domain domain);

	/**
	 * chb 
	 * 根据外线取得domain
	 * @return
	 */
	public Domain getDomainByOutLine(String outLine);

	// common
	@Transactional
	public SipConfig get(Object primaryKey);
	
	/**
	 * 更保存新建的分机或外线，如果是分机，则自动为其设置分机号
	 * @param sipConfig
	 */
//	@Transactional  // 由于牵涉到同步问题，所以将其事务加到了 Eao 上
	public void save(SipConfig sipConfig);

	/**
	 * 更新或保存新建的分机或外线，如果是分机，并且是新建的，则自动为其设置分机号
	 * @param sipConfig
	 * @return
	 */
//	@Transactional  // 由于牵涉到同步问题，所以将其事务加到了 Eao 上
	public SipConfig update(SipConfig sipConfig);

	@Transactional
	public void delete(SipConfig sipConfig);
	
	@Transactional
	public void deleteById(Object primaryKey);

	/**
	 * chb 
	 * 根据domain 取得所有没有被其他项目使用的外线
	 * @return
	 */
	public List<SipConfig> getAllUseableOutlinesByDomain(
			Domain domain);
	/**
	 * chb 
	 *根据外线的名字取出一条外线
	 * @return
	 */
	public SipConfig getOutlineByOutlineName(String outlineName);

	/**
	 * jrh 根据JPQL 查询所有符合条件的SipConfig
	 * @param searchSql
	 * @return
	 */
	@Transactional
	public List<SipConfig> getAllByJpql(String jpql);

	/**
	 * jrh
	 * 	在指定域下，根据分机或外线名称，检查是否与之关联的IVRAction 对象，如果有，则表示不能删除
	 * 
	 * @param isExten	是否为分机对象
	 * @param sipName	分机或外线名称
	 * @param domainId	域编号
	 * @return boolean  删除与否
	 */
	@Transactional
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
	@Transactional
	public void checkIvrActionAndUpdate(boolean isExten, String sipName, Long domainId);
}
