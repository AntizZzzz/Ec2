package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.ProjectToCommodityLink;
import com.jiangyifen.ec2.service.eaoservice.CommodityService;

public class CommodityServiceImpl implements CommodityService {
	
	private CommonEao commonEao ;

	@SuppressWarnings("unchecked")
	public List<Commodity> getAllByDomainId(Long domainId) {
		String sql = "select e from Commodity as e where e.domain.id = "+domainId +" order by e.id desc";
		return commonEao.getEntityManager().createQuery(sql).getResultList();
	}
	
	@Override
	public void saveProjectToCommodityLink(ProjectToCommodityLink projectToCommodityLink) {
		commonEao.getEntityManager().persist(projectToCommodityLink);
	}

	@Override
	public void deleteProjectToCommodityLinkByIds(Long commodityId, Long projectId) {
		String deleteLinkSql = "delete from ec2_project_to_commodity_link where commodityId = "+ commodityId +" and projectid = "+projectId;
		commonEao.getEntityManager().createNativeQuery(deleteLinkSql).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Commodity> getAllByProjectId(Long projectId) {
		String commodityIdsSql = "select commodityId from ec2_project_to_commodity_link where projectid = "+projectId;
		List<Long> commodityIds = commonEao.getEntityManager().createNativeQuery(commodityIdsSql).getResultList();
		commodityIds.add(0L);
		String commodityIdSql = StringUtils.join(commodityIds, ",");
		
		String commoditysSql = "select c from Commodity as c where c.id in (" +commodityIdSql+ ") order by c.id desc";
		return commonEao.getEntityManager().createQuery(commoditysSql).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getAllProjectIdsByBlacklistItemId(Long commodityId) {
		String projectIdsSql = "select projectid from ec2_project_to_commodity_link where commodityid = "+commodityId + " order by projectid desc";
		return commonEao.getEntityManager().createNativeQuery(projectIdsSql).getResultList();
	}

	@Override
	public Commodity get(Object primaryKey) {
		return commonEao.get(Commodity.class, primaryKey);
	}

	@Override
	public void save(Commodity commodity) {
		commonEao.save(commodity);
	}

	@Override
	public Commodity update(Commodity commodity) {
		return (Commodity) commonEao.update(commodity);
	}

	@Override
	public void delete(Commodity commodity) {
		// jrh 如果要删除某个商品，则需要删除商品与项目的对应关系
		String sql = "delete from ec2_project_to_commodity_link where commodityid = "+ commodity.getId();
		commonEao.getEntityManager().createNativeQuery(sql).executeUpdate();
		commonEao.delete(commodity);
	}

	@Override
	public void deleteById(Object primaryKey) {	
		// jrh 如果要删除某个商品，则需要删除商品与项目的对应关系
		String sql = "delete from ec2_project_to_commodity_link where commodityid = "+ primaryKey;
		commonEao.getEntityManager().createNativeQuery(sql).executeUpdate();
		commonEao.delete(Commodity.class, primaryKey);
	}

	// enhance function method

	//flip method
	@Override
	public int getEntityCount(String sql) {
		return commonEao.getEntityCount(sql);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Commodity> loadPageEntities(int startIndex, int pageRecords,
			String selectSql) {
		return commonEao.loadPageEntities(startIndex, pageRecords, selectSql);
	}

	//getter and setter
	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}
	
}
