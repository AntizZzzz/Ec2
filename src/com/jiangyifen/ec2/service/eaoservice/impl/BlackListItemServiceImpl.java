package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.BlackListItemEao;
import com.jiangyifen.ec2.entity.BlackListItem;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;
import com.jiangyifen.ec2.service.eaoservice.BlackListItemService;

/**
 * 黑名单服务类的实现
 * @author jrh
 *
 */
public class BlackListItemServiceImpl implements BlackListItemService{
	
	private BlackListItemEao blackListItemEao;

	/** jrh 根据整个域下的所有黑面单  <br/> @param domain 所属域 */
	@Override
	public List<BlackListItem> getAllByDomain(Domain domain) {
		return blackListItemEao.getAllByDomain(domain);
	}
	
	/** 根据呼叫方向取出所有的黑名单名字 */
	@Override
	public List<String> getAllPhoneNumsByDirection(String direction, Domain domain){
		return blackListItemEao.getAllPhoneNumsByDirection(direction, domain);
	}

	/** 指定域下根据外线名称获取其对应的所有的黑名单名字 <br/>* @param outlineId	外线Id <br/> @param direction	   类型(呼入、呼出) <br/> @param domainId	所属域的Id */
	@Override
	public List<String> getAllPhoneNumsByOutlineUsername(Long outlineId, String direction, Long domainId) {
		return blackListItemEao.getAllPhoneNumsByOutlineUsername(outlineId, direction, domainId);
	}

	/** 检查当前黑名单号码是否已经存在 */
	@Override
	public boolean checkExistedByPhoneNo(String phoneNo, String type, Domain domain) {
		return blackListItemEao.checkExistedByPhoneNo(phoneNo, type, domain);
	}
	
	/** 添加外线与黑面单的对应关系 */
	@Override
	public void saveOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistItemLink) {
		blackListItemEao.saveOutline2BlacklistLink(outline2BlacklistItemLink);
	}

	/** 删除外线与黑面单的对应关系  <br/> @param blacklistItemId	黑名单id <br/> @param outlineId	外线  id */
	@Override
	public void deleteOutline2BlacklistLinkByIds(Long blacklistItemId, Long outlineId) {
		blackListItemEao.deleteOutline2BlacklistLinkByIds(blacklistItemId, outlineId);
	}

	/** jrh 获取所有按指定外线对应的黑名单 <br/> @param outlineId	外线id */
	@Override
	public List<BlackListItem> getAllByOutlineId(Long outlineId) {
		return blackListItemEao.getAllByOutlineId(outlineId);
	}
	
	/** jrh 根据制定的黑名单编号，获取所有与只关联的外线的编号  <br/>  @param blacklistItemId			黑名单编号 */
	@Override
	public List<Long> getAllOutlineIdsByBlacklistItemId(Long blacklistItemId) {
		return blackListItemEao.getAllOutlineIdsByBlacklistItemId(blacklistItemId);
	}
	
	// common method

	@Override
	public BlackListItem get(Object primaryKey) {
		return blackListItemEao.get(BlackListItem.class, primaryKey);
	}

	@Override
	public void save(BlackListItem blackListItem) {
		blackListItemEao.save(blackListItem);
	}

	@Override
	public BlackListItem update(BlackListItem blackListItem) {
		return (BlackListItem) blackListItemEao.update(blackListItem);
	}

	@Override
	public void delete(BlackListItem blackListItem) {
		blackListItemEao.delete(blackListItem);
	}

	@Override
	public void deleteById(Object primaryKey) {
		blackListItemEao.deleteById(primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BlackListItem> loadPageEntities(int start, int length, String sql) {
		return blackListItemEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return blackListItemEao.getEntityCount(sql);
	}

	
	public BlackListItemEao getBlackListItemEao() {
		return blackListItemEao;
	}

	public void setBlackListItemEao(BlackListItemEao blackListItemEao) {
		this.blackListItemEao = blackListItemEao;
	}
	
}
