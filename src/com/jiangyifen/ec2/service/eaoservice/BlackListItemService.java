package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.BlackListItem;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
 * 黑名单服务类
 * @author jrh
 *
 */
public interface BlackListItemService extends FlipSupportService<BlackListItem> {

	// enhanced method
	
	/**
	 * jrh
	 * 根据整个域下的所有黑面单
	 * @param domain		所属域
	 */
	@Transactional
	public List<BlackListItem> getAllByDomain(Domain domain);
	
	/**
	 * jrh
	 * 根据呼叫方向（黑名单类型）取出指定域下的所有的黑名单名字
	 * @param direction		类型(呼入、呼出)
	 * @param domain		所属域
	 */
	@Transactional
	public List<String> getAllPhoneNumsByDirection(String direction, Domain domain);
	
	/**
	 * jrh
	 * 指定域下根据外线名称获取其对应的所有的黑名单名字
	 * @param outlineId			外线Id
	 * @param direction			类型(呼入、呼出)
	 * @param domainId			所属域的Id
	 */
	@Transactional
	public List<String> getAllPhoneNumsByOutlineUsername(Long outlineId, String direction, Long domainId);

	/**
	 * jrh 检查当前黑名单号码是否已经存在
	 * @param phoneNo	黑名单号码
	 * @param type		类型(呼入、呼出)
	 * @param domain	所属域
	 * @return
	 */
	@Transactional
	public boolean checkExistedByPhoneNo(String phoneNo, String type, Domain domain);

	/**
	 * jrh
	 * 	添加外线与黑面单的对应关系
	 * @param outline2BlacklistItemLink
	 */
	@Transactional
	public void saveOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistItemLink);
	
	/**
	 * jrh
	 * 	删除外线与黑面单的对应关系
	 * @param blacklistItemId	黑名单id
	 * @param outlineId			外线id
	 */
	@Transactional
	public void deleteOutline2BlacklistLinkByIds(Long blacklistItemId, Long outlineId);
	
	/**
	 * jrh
	 * 	获取所有按指定外线对应的黑名单
	 * @param outlineId			外线id
	 */
	@Transactional
	public List<BlackListItem> getAllByOutlineId(Long outlineId);

	/**
	 * jrh
	 * 	根据制定的黑名单编号，获取所有与之关联的外线的编号
	 * @param blacklistItemId			黑名单编号
	 */
	@Transactional
	public List<Long> getAllOutlineIdsByBlacklistItemId(Long blacklistItemId);
	
	// common method 
	
	@Transactional
	public BlackListItem get(Object primaryKey);
	
	@Transactional
	public void save(BlackListItem blackListItem);

	@Transactional
	public BlackListItem update(BlackListItem blackListItem);

	@Transactional
	public void delete(BlackListItem blackListItem);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
}
