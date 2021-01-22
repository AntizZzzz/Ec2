package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：外线黑名单对应关系管理
* 
* @author jrh
*
*/
public interface Outline2BlacklistLinkService extends FlipSupportService<Outline2BlacklistItemLink>  {
	
	/**
	 * 根据主键ID获得外线黑名单对应关系
	 * @param id	主键ID 
	 * @return		外线黑名单对应关系，一条或null
	 */
	@Transactional
	public Outline2BlacklistItemLink getOutline2BlacklistLinkById(Long id);
	
	/**
	 * 保存外线黑名单对应关系
	 * @param outline2BlacklistLink	外线黑名单对应关系
	 * 
	 */
	@Transactional
	public void saveOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistLink);
	
	/**
	 * 更新外线黑名单对应关系
	 * @param outline2BlacklistLink	外线黑名单对应关系
	 * 
	 */
	@Transactional
	public Outline2BlacklistItemLink updateOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistLink);
	
	/**
	 * 删除外线黑名单对应关系
	 * @param outline2BlacklistLink	外线黑名单对应关系
	 * 
	 */
	@Transactional
	public void deleteOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistLink);
	
}