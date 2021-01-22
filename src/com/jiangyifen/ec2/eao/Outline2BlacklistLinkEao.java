package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;

/**
* Eao接口：外线黑名单对应关系管理
* 
* @author jrh
*
*/
public interface Outline2BlacklistLinkEao extends BaseEao {
	
	
	/**
	 * 获得外线黑名单对应关系列表
	 * @param 	jpql jpql语句
	 * @return 	外线黑名单对应关系列表
	 */
	public List<Outline2BlacklistItemLink> loadOutline2BlacklistLinkList(String jpql);
	
}