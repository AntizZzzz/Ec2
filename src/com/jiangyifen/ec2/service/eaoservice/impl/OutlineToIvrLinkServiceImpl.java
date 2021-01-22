package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.OutlineToIvrLinkEao;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.service.eaoservice.OutlineToIvrLinkService;

/**
 * 
 * @Description 描述：外线与IVR 的关联关系维护工具
 * 
 * @author  jrh
 * @date    2014年3月5日 下午6:06:22
 * @version v1.0.0
 */
public class OutlineToIvrLinkServiceImpl implements OutlineToIvrLinkService {
	
	private OutlineToIvrLinkEao outlineToIvrLinkEao;

	public OutlineToIvrLinkEao getOutlineToIvrLinkEao() {
		return outlineToIvrLinkEao;
	}

	public void setOutlineToIvrLinkEao(OutlineToIvrLinkEao outlineToIvrLinkEao) {
		this.outlineToIvrLinkEao = outlineToIvrLinkEao;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<OutlineToIvrLink> loadPageEntities(int start, int length, String sql) {
		return outlineToIvrLinkEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return outlineToIvrLinkEao.getEntityCount(sql);
	}

	// enhanced method

	@Override
	public OutlineToIvrLink get(Object primaryKey) {
		return outlineToIvrLinkEao.get(OutlineToIvrLink.class, primaryKey);
	}

	@Override
	public void save(OutlineToIvrLink notice) {
		outlineToIvrLinkEao.save(notice);
	}

	@Override
	public OutlineToIvrLink update(OutlineToIvrLink notice) {
		return (OutlineToIvrLink) outlineToIvrLinkEao.update(notice);
	}

	@Override
	public void delete(OutlineToIvrLink notice) {
		outlineToIvrLinkEao.delete(notice);
	}

	@Override
	public void deleteById(Long primaryKey) {
		outlineToIvrLinkEao.deleteById(primaryKey);
	}
	
	@Override
	public List<OutlineToIvrLink> getAllByDomain(Long domainId) {
		return outlineToIvrLinkEao.getAllByDomain(domainId);
	}

	@Override
	public List<OutlineToIvrLink> getAllByDomain(Long domainId, boolean isUseable) {
		return outlineToIvrLinkEao.getAllByDomain(domainId, isUseable);
	}

	@Override
	public void editOtilPriority(OutlineToIvrLink outlineToIvrLink, boolean isUpgrade) {
		outlineToIvrLinkEao.editOtilPriority(outlineToIvrLink, isUpgrade);
	}

}
