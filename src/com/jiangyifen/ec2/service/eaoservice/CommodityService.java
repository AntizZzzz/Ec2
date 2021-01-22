package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Commodity;
import com.jiangyifen.ec2.entity.ProjectToCommodityLink;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface CommodityService  extends FlipSupportService<Commodity> {

	// common method 
	
	@Transactional
	public Commodity get(Object primaryKey);
	
	@Transactional
	public void save(Commodity commodity);

	@Transactional
	public Commodity update(Commodity commodity);

	@Transactional
	public void delete(Commodity commodity);
	
	@Transactional
	public void deleteById(Object primaryKey);
	
	// enhanced method

	/**
	 * jrh 
	 * 	获取整个域下的所有商品
	 * @param domainId  租户域的编号
	 * @return
	 */
	@Transactional
	public List<Commodity> getAllByDomainId(Long domainId);
	
	/**
	 * jrh
	 * 	添加项目与商品的对应关系
	 * @param projectToCommodityLink
	 */
	@Transactional
	public void saveProjectToCommodityLink(ProjectToCommodityLink projectToCommodityLink);
	
	/**
	 * jrh
	 * 	删除项目与商品的对应关系
	 * @param commodityId	商品id
	 * @param projectId		项目id
	 */
	@Transactional
	public void deleteProjectToCommodityLinkByIds(Long commodityId, Long projectId);
	
	/**
	 * jrh
	 * 	获取所有按指定项目对应的商品
	 * @param projectId			项目id
	 */
	@Transactional
	public List<Commodity> getAllByProjectId(Long projectId);

	/**
	 * jrh
	 * 	根据制定的商品编号，获取所有与之关联的项目的编号
	 * @param commodityId		商品编号
	 */
	@Transactional
	public List<Long> getAllProjectIdsByBlacklistItemId(Long commodityId);
	
	
}
