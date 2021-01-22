package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.BlackListItem;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;

/**
 * 黑名单Eao
 * @author jrh
 *
 */
public interface BlackListItemEao extends BaseEao {
	
	/**
	 * jrh
	 * 根据整个域下的所有黑面单
	 * @param domain		所属域
	 */
	public List<BlackListItem> getAllByDomain(Domain domain);
	
	/**
	 * jrh
	 * 根据呼叫方向（黑名单类型）取出指定域下的所有的黑名单名字
	 * @param direction		类型(呼入、呼出)
	 * @param domain		所属域
	 */
	public List<String> getAllPhoneNumsByDirection(String direction, Domain domain);

	/**
	 * jrh
	 * 指定域下根据外线名称获取其对应的所有的黑名单名字
	 * @param outlineId			外线Id
	 * @param direction			类型(呼入、呼出)
	 * @param domainId			所属域的Id
	 */
	public List<String> getAllPhoneNumsByOutlineUsername(Long outlineId, String direction, Long domainId);
	
	/**
	 * jrh 检查当前黑名单号码是否已经存在
	 * @param phoneNo	黑名单号码
	 * @param type		类型(呼入、呼出)
	 * @param domain	所属域
	 * @return
	 */
	public boolean checkExistedByPhoneNo(String phoneNo, String type, Domain domain);

	/**
	 * jrh
	 * 	添加外线与黑面单的对应关系
	 * @param status2DeptLink
	 */
	public void saveOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistItemLink);
	
	/**
	 * jrh
	 * 	删除外线与黑面单的对应关系
	 * @param blacklistItemId	黑名单id
	 * @param outlineId			外线id
	 */
	public void deleteOutline2BlacklistLinkByIds(Long blacklistItemId, Long outlineId);

	/**
	 * jrh
	 * 	获取所有按指定外线对应的黑名单
	 * @param outlineId			外线id
	 */
	public List<BlackListItem> getAllByOutlineId(Long outlineId);

	/**
	 * jrh
	 * 	根据制定的黑名单编号，获取所有与只关联的外线的编号
	 * @param blacklistItemId			黑名单编号
	 */
	public List<Long> getAllOutlineIdsByBlacklistItemId(Long blacklistItemId);
	
	/**
	 * jrh 
	 *  重写删除方法，因为删除的时候不仅要将本身对象删除，还要讲黑名单域外线的对应关系删除
	 * @param blackListItem	黑名单对象
	 */
	public void delete(BlackListItem blackListItem);

	/**
	 * jrh 
	 *  重写删除方法，因为删除的时候不仅要将本身对象删除，还要讲黑名单域外线的对应关系删除
	 * @param primaryKey	黑名单对象对应的Id 号
	 */
	public void deleteById(Object primaryKey);
	
}
