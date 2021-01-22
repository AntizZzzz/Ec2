package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.eao.MarketingProjectEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;

public class MarketingProjectServiceImpl implements MarketingProjectService,FlipSupportService<MarketingProject> {
	
	private MarketingProjectEao marketingProjectEao;
	
	// enhanced method
	

	// common method
	
	@Override
	public MarketingProject get(Object primaryKey) {
		return marketingProjectEao.get(MarketingProject.class, primaryKey);
	}

	@Override
	public void save(MarketingProject marketingProject) {
		marketingProjectEao.save(marketingProject);
	}

	@Override
	public MarketingProject update(MarketingProject marketingProject) {
		return (MarketingProject)marketingProjectEao.update(marketingProject);
	}

	@Override
	public void delete(MarketingProject marketingProject) {
		// jrh 需删除项目与商品之间的对应关系
		String sql = "delete from ec2_project_to_commodity_link where projectid = " + marketingProject.getId();
		this.getEntityManager().createNativeQuery(sql).executeUpdate();
		marketingProjectEao.delete(marketingProject);
	}

	@Override
	public void deleteById(Object primaryKey) {
		// jrh 需删除项目与商品之间的对应关系
		String sql = "delete from ec2_project_to_commodity_link where projectid = " + primaryKey;
		this.getEntityManager().createNativeQuery(sql).executeUpdate();
		marketingProjectEao.delete(MarketingProject.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProject> loadPageEntities(int start, int length,
			String sql) {
		return marketingProjectEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return marketingProjectEao.getEntityCount(sql);
	}

	//getter and setter

	public MarketingProjectEao getMarketingProjectEao() {
		return marketingProjectEao;
	}

	public void setMarketingProjectEao(MarketingProjectEao marketingProjectEao) {
		this.marketingProjectEao = marketingProjectEao;
	}
	/**
	 * chb
	 *  取出所有已经分配到指定项目任务中的Csr集合
	 * @param marketingProject
	 * @param domain
	 * @return 指派是只能指派CSR，故查处来的结果应该不会有管理员
	 */
	@Override
	public List<User> getCsrsByProject(MarketingProject marketingProject,
			Domain domain) {
		Set<User> csrs= marketingProjectEao.get(MarketingProject.class, marketingProject.getId()).getUsers();
		return new ArrayList<User>(csrs);
	}
	/**
	 * chb
	 * 取出所有的项目
	 */
	@Override
	public List<MarketingProject> getAll(Domain domain) {
		return marketingProjectEao.getAll(domain);
	}
	
	/**
	 * chb
	 * 取出所有域的项目
	 */
	@Override
	public List<MarketingProject> getAll() {
		return marketingProjectEao.getAll();
	}
	
	@Override
	public List<MarketingProject> getAllBySip(SipConfig outline) {
		return marketingProjectEao.getAllBySip(outline);
	}
	
	@Override
	public List<MarketingProject> getAllByQueue(Long queueId, Long domainId) {
		return marketingProjectEao.getAllByQueue(queueId, domainId);
	}
	
	@Override
	public List<MarketingProject> getAllByDepartments(List<Long> deptIds, Long domainId) {
		return marketingProjectEao.getAllByDepartments(deptIds, domainId);
	}
	

	@Override
	public List<MarketingProject> getAllProjectsByUser(User user) {
		return marketingProjectEao.getAllProjectsByUser(user);
	}

	@Override
	public List<MarketingProject> getProjectsByUserAndStatus(User user,
			MarketingProjectStatus projectStatus) {
		return marketingProjectEao.getProjectsByUserAndStatus(user, projectStatus);
	}
	/**
	 * 取得EntityManager
	 */
	public EntityManager getEntityManager(){
		return marketingProjectEao.getEntityManager();
	}

	@Override
	public MarketingProject getAllByOutlineName(String outlineName, Long domainId) {
		return marketingProjectEao.getAllByOutlineName(outlineName, domainId);
	}
	
}
