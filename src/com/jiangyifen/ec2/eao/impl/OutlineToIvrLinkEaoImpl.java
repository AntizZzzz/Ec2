package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import javax.persistence.EntityManager;

import com.jiangyifen.ec2.eao.OutlineToIvrLinkEao;
import com.jiangyifen.ec2.entity.OutlineToIvrLink;

/**
 * 
 * @Description 描述：外线与IVR 的关联关系维护工具
 * 
 * @author  jrh
 * @date    2014年3月5日 下午6:05:48
 * @version v1.0.0
 */
public class OutlineToIvrLinkEaoImpl extends BaseEaoImpl implements OutlineToIvrLinkEao {

	@Override
	public void save(OutlineToIvrLink outlineToIvrLink) {
		EntityManager em = this.getEntityManager();
		Long outlineId = outlineToIvrLink.getOutlineId();
		
		String prioritySql = "select max(priority) from ec2_outline_to_ivr_link as e where e.outlineid = "+outlineId;
		Integer max_priority = (Integer) em.createNativeQuery(prioritySql).getSingleResult();

		int priority = 1;
		if(max_priority != null) {
			priority += max_priority;
		}
		outlineToIvrLink.setPriority(priority);
		em.persist(outlineToIvrLink);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<OutlineToIvrLink> getAllByDomain(Long domainId) {
		String jpql = "select e from OutlineToIvrLink as e where e.domainId =:domainId order by e.outlineId asc, e.priority asc";
		return this.getEntityManager().createQuery(jpql).setParameter("domainId", domainId).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<OutlineToIvrLink> getAllByDomain(Long domainId, boolean isUseable) {
		String jpql = "select e from OutlineToIvrLink as e where e.domainId =:domainId and e.isUseable =:isUseable order by e.outlineId asc, e.priority asc";
		return this.getEntityManager().createQuery(jpql).setParameter("domainId", domainId).setParameter("isUseable", isUseable).getResultList();
	}

	@Override
	public void delete(OutlineToIvrLink outlineToIvrLink) {
		Long outlineId = outlineToIvrLink.getOutlineId();
		int priority = outlineToIvrLink.getPriority();
		
		EntityManager em = this.getEntityManager();
		em.remove(em.getReference(OutlineToIvrLink.class, outlineToIvrLink.getId()));
		
		// 注意：这里只能用 JPQL 不能使用 原生SQL ，因为使用原生SQL 会导致界面缓存问题
		String jpql = "update OutlineToIvrLink as e set e.priority = (e.priority-1) where e.outlineId = "+outlineId+" and e.priority > "+priority;
		em.createQuery(jpql).executeUpdate();
	}

	@Override
	public void deleteById(Long primaryKey) {
		EntityManager em = this.getEntityManager();
		OutlineToIvrLink outlineToIvrLink = em.find(OutlineToIvrLink.class, primaryKey);
		if(outlineToIvrLink != null) {
			delete(outlineToIvrLink);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void editOtilPriority(OutlineToIvrLink outlineToIvrLink, boolean isUpgrade) {
		Long outlineId = outlineToIvrLink.getOutlineId();
		int priority = outlineToIvrLink.getPriority();
		int cursor = isUpgrade ? -1 : 1;	
		outlineToIvrLink.setPriority(priority +cursor);
		this.getEntityManager().merge(outlineToIvrLink);

		int relatedCursor = isUpgrade ? 1 : -1;
		String jpql = "select e from OutlineToIvrLink as e where e.outlineId = "+outlineId+" and e.priority = "+(priority-1);
		if(!isUpgrade) {
			jpql = "select e from OutlineToIvrLink as e where e.outlineId = "+outlineId+" and e.priority = "+(priority+1);
		}
		
		List<OutlineToIvrLink> links = this.getEntityManager().createQuery(jpql).getResultList();
		if(links.size() > 0) {
			OutlineToIvrLink relatedLink = links.get(0);
			relatedLink.setPriority(relatedLink.getPriority() + relatedCursor);
			this.getEntityManager().merge(relatedLink);
		}
	}
	
}
