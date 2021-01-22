package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.Outline2BlacklistLinkEao;
import com.jiangyifen.ec2.entity.Outline2BlacklistItemLink;

import com.jiangyifen.ec2.service.eaoservice.Outline2BlacklistLinkService;

/**
* Service实现类：外线黑名单对应关系管理
* 
* @author jrh
* 
*/
public class Outline2BlacklistLinkServiceImpl implements Outline2BlacklistLinkService {

	
	/** 需要注入的Eao */
	private Outline2BlacklistLinkEao outline2BlacklistLinkEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<Outline2BlacklistItemLink> loadPageEntities(int start, int length, String sql) {
		return outline2BlacklistLinkEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return outline2BlacklistLinkEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得外线黑名单对应关系
	@Override
	public Outline2BlacklistItemLink getOutline2BlacklistLinkById(Long id){
		return outline2BlacklistLinkEao.get(Outline2BlacklistItemLink.class, id);
	}
	
	// 保存外线黑名单对应关系
	@Override
	public void saveOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistLink){
		outline2BlacklistLinkEao.save(outline2BlacklistLink);
	}
	
	// 更新外线黑名单对应关系
	@Override
	public Outline2BlacklistItemLink updateOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistLink){
		return (Outline2BlacklistItemLink)outline2BlacklistLinkEao.update(outline2BlacklistLink);
	}
	
	// 删除外线黑名单对应关系
	@Override
	public void deleteOutline2BlacklistLink(Outline2BlacklistItemLink outline2BlacklistLink){
		outline2BlacklistLinkEao.delete(outline2BlacklistLink);
	}
	
	//Eao注入
	public Outline2BlacklistLinkEao getOutline2BlacklistLinkEao() {
		return outline2BlacklistLinkEao;
	}
	
	//Eao注入
	public void setOutline2BlacklistLinkEao(Outline2BlacklistLinkEao outline2BlacklistLinkEao) {
		this.outline2BlacklistLinkEao = outline2BlacklistLinkEao;
	}
}