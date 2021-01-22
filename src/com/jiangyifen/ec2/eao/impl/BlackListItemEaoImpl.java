package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.BlackListItemEao;
import com.jiangyifen.ec2.entity.BlackListItem;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;

/**
 * 黑名单Eao 的实现
 * @author jrh
 *
 */
public class BlackListItemEaoImpl extends BaseEaoImpl implements BlackListItemEao {
	

	/** jrh 根据整个域下的所有黑面单  <br/> @param domain 所属域 */
	@SuppressWarnings("unchecked")
	public List<BlackListItem> getAllByDomain(Domain domain) {
		String sql = "select bi from BlackListItem as bi where bi.domain.id = "+domain.getId()+ " order by bi.type desc,bi.id asc";
		return this.getEntityManager().createQuery(sql).getResultList();
	}
	
	/**
	 * jrh 根据呼叫方向取出所有的黑名单名字
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllPhoneNumsByDirection(String direction, Domain domain) {
		String sql = "select phonenumber from ec2_black_list_item where type='"+direction+"' and domain_id="+domain.getId();
		List<String> blackListNums = getEntityManager().createNativeQuery(sql).getResultList();
		return blackListNums;
	}

	/** 指定域下根据外线名称获取其对应的所有的黑名单名字 <br/>* @param outlineId	外线Id <br/> @param direction	   类型(呼入、呼出) <br/> @param domainId	所属域的Id */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getAllPhoneNumsByOutlineUsername(Long outlineId, String direction, Long domainId) {
		String blacklistItemIdsSql = "select blacklistItemId from ec2_outline_2_blacklist_item_link where outlineid = "+outlineId;
		String sql = "select phonenumber from ec2_black_list_item where id in (" +blacklistItemIdsSql+ ") and type='"+direction+"' and domain_id="+domainId;
		return this.getEntityManager().createNativeQuery(sql).getResultList();
	}
	
	/** jrh 检查当前黑名单号码是否已经存在 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean checkExistedByPhoneNo(String phoneNo, String type, Domain domain) {
		String sql = "select bi from BlackListItem as bi where bi.phoneNumber = '"+phoneNo+"' and bi.type='"+type+"' and bi.domain.id = "+domain.getId();
		List<String> blackListNums = getEntityManager().createQuery(sql).getResultList();
		if(blackListNums.size() > 0) {
			return true;
		}
		return false;
	}

	/** jrh 添加外线与黑面单的对应关系 */
	@Override
	public void saveOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistItemLink) {
		this.getEntityManager().persist(outline2BlacklistItemLink);
	}
	
	/** jrh 删除外线与黑面单的对应关系  <br/> @param blacklistItemId	黑名单id <br/> @param outlineId	外线  id */
	@Override
	public void deleteOutline2BlacklistLinkByIds(Long blacklistItemId, Long outlineId) {
		String deleteLinkSql = "delete from ec2_outline_2_blacklist_item_link where blacklistItemId = "+ blacklistItemId +" and outlineid = "+outlineId;
		this.getEntityManager().createNativeQuery(deleteLinkSql).executeUpdate();
	}

	/** jrh 获取所有按指定外线对应的黑名单 <br/> @param outlineId	外线id */
	@SuppressWarnings("unchecked")
	@Override
	public List<BlackListItem> getAllByOutlineId(Long outlineId) {
		String blacklistItemIdsSql = "select blacklistItemId from ec2_outline_2_blacklist_item_link where outlineid = "+outlineId;
		List<Long> blacklistItemIds = this.getEntityManager().createNativeQuery(blacklistItemIdsSql).getResultList();
		String containItemIdSql = "0,";
		for(Long statusId : blacklistItemIds) {
			containItemIdSql += statusId+ ",";
		}
		while(containItemIdSql.endsWith(",")) {
			containItemIdSql = containItemIdSql.substring(0, containItemIdSql.length()-1);
		}
		
		String itemsSql = "select bi from BlackListItem as bi where bi.id in (" +containItemIdSql+ ") order by bi.type desc,bi.id asc";
		return this.getEntityManager().createQuery(itemsSql).getResultList();
	}

	/** jrh 根据制定的黑名单编号，获取所有与只关联的外线的编号  <br/>  @param blacklistItemId			黑名单编号 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getAllOutlineIdsByBlacklistItemId(Long blacklistItemId) {
		String outlineidIdsSql = "select outlineid from ec2_outline_2_blacklist_item_link where blacklistitemid = "+blacklistItemId;
		return this.getEntityManager().createNativeQuery(outlineidIdsSql).getResultList();
	}
	
	/** jrh 重写删除方法，因为删除的时候不仅要将本身对象删除，还要讲黑名单域外线的对应关系删除 <br/> @param blackListItem	黑名单对象  */
	public void delete(BlackListItem blackListItem) {
		// jrh 如果要删除某条黑名单，则需要删除黑名单域外线的对应关系
		String sql = "delete from ec2_outline_2_blacklist_item_link where blacklistitemid = "+ blackListItem.getId();
		this.getEntityManager().createNativeQuery(sql).executeUpdate();
		// 删黑名单
		this.delete(blackListItem);
	}

	/** jrh 重写删除方法，因为删除的时候不仅要将本身对象删除，还要讲黑名单域外线的对应关系删除 <br/> @param primaryKey	黑名单对象对应的Id 号  */
	public void deleteById(Object primaryKey) {
		// jrh 如果要删除某条黑名单，则需要删除黑名单域外线的对应关系
		String sql = "delete from ec2_outline_2_blacklist_item_link where blacklistitemid = "+ primaryKey;
		this.getEntityManager().createNativeQuery(sql).executeUpdate();
		// 删黑名单
		this.delete(BlackListItem.class, primaryKey);
	}
	
}
