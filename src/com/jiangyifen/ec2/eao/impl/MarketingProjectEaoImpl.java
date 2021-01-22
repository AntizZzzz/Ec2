package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.eao.MarketingProjectEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;

public class MarketingProjectEaoImpl extends BaseEaoImpl implements
		MarketingProjectEao {
	/**
	 * chb 重写删除方法
	 */
	@Override
	public <T> void delete(Class<T> entityClass, Object primaryKey) {
		//删除任务（task）
		String deleteTaskSql="delete from MarketingProjectTask mpt where mpt.marketingProject.id="+primaryKey;
		getEntityManager().createQuery(deleteTaskSql).executeUpdate();

		//删除记录（record）
		String deleteComplaintSql="delete from CustomerComplaintRecord ccr where ccr.marketingProject.id="+primaryKey;
		getEntityManager().createQuery(deleteComplaintSql).executeUpdate();

		String deleteServiceSql="delete from CustomerServiceRecord csr where csr.marketingProject.id="+primaryKey;
		getEntityManager().createQuery(deleteServiceSql).executeUpdate();
		
		//删除任务
		getEntityManager().remove(
				getEntityManager().getReference(entityClass, primaryKey));
	}
	/**
	 * chb
	 * 取出所有的项目
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProject> getAll(Domain domain) {
//		String addFakeDelete="marketingProjectStatus != "+MarketingProjectStatus.class.getName() + ".DELETE";
		String sql="select mp from MarketingProject as mp where mp.domain.id="+domain.getId();
		return getEntityManager().createQuery(sql).getResultList();
	}

	/**
	 * chb
	 * 取出所有域的项目
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProject> getAll() {
		/*String addFakeDelete="marketingProjectStatus != "+MarketingProjectStatus.class.getName() + ".DELETE";*/
		String sql="select mp from MarketingProject as mp";
		return getEntityManager().createQuery(sql).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProject> getAllBySip(SipConfig outline) {
		return getEntityManager().createQuery("select mp from MarketingProject as mp where mp.sip.id = " + outline.getId()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProject> getAllByQueue(Long queueId, Long domainId) {
		return getEntityManager().createQuery("select mp from MarketingProject as mp where mp.queue.id = " +queueId +" and mp.domain.id = " +domainId).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProject> getAllByDepartments(List<Long> deptIds, Long domainId) {
		String deptSql = "0,";
		for(int i = 0; i < deptIds.size(); i++) {
			deptSql += deptIds.get(i) +", ";
		}
		while(deptSql.endsWith(" ") || deptSql.endsWith(",")) {
			deptSql = deptSql.substring(0, deptSql.length() -1);
		}
		String addFakeDelete="marketingProjectStatus != "+MarketingProjectStatus.class.getName() + ".DELETE";
		return getEntityManager().createQuery("select mp from MarketingProject as mp where mp.domain.id = " +domainId+" and mp."+addFakeDelete+" and mp.creater.department.id in ("+ deptSql+")").getResultList();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProject> getAllProjectsByUser(User user) {
		// 先从中间表中查出项目id
		List<Long> projectIds = getEntityManager().createNativeQuery("select marketingproject_id from ec2_markering_project_ec2_user where users_id = " +user.getId()).getResultList();
		String idSql = projectIds.size() > 0 ? "" : "-1";
		for(int i = 0; i < projectIds.size(); i++) {
			if( i == (projectIds.size() -1) ) {
				idSql += projectIds.get(i);
			} else {
				idSql += projectIds.get(i) +", ";
			}
		}
		// 在通过项目id 查出项目集合
		return getEntityManager().createQuery("select p from MarketingProject as p where p.id in ("+ idSql+") order by p.id asc").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketingProject> getProjectsByUserAndStatus(User user, MarketingProjectStatus projectStatus) {
		// 先从中间表中查出项目id
		List<Long> projectIds = getEntityManager().createNativeQuery("select marketingproject_id from ec2_markering_project_ec2_user where users_id = " +user.getId()).getResultList();
		String idSql = projectIds.size() > 0 ? "" : "-1";
		for(int i = 0; i < projectIds.size(); i++) {
			if( i == (projectIds.size() -1) ) {
				idSql += projectIds.get(i);
			} else {
				idSql += projectIds.get(i) +", ";
			}
		}
		
		// 在通过项目id 和项目状态组合查出项目集合
		String statusStr = getProjectStatusSql(projectStatus);
		return getEntityManager().createQuery("select p from MarketingProject as p where p.marketingProjectStatus = "
									+statusStr +" and p.id in ("+ idSql+") order by p.id asc").getResultList();
	}

	/**
	 * jrh 根据项目状态对象，得到用于查询项目状态的字符串语句
	 * 
	 * @param projectStatus
	 *            项目状态
	 * @return
	 */
	private String getProjectStatusSql(MarketingProjectStatus projectStatus) {
		String statuStr = projectStatus.getClass().getName() + ".";
		if (projectStatus.getIndex() == 0) {
			statuStr += "NEW";
		} else if (projectStatus.getIndex() == 1) {
			statuStr += "RUNNING";
		} else if (projectStatus.getIndex() == 2) {
			statuStr += "OVER";
		}
		return statuStr;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public MarketingProject getAllByOutlineName(String outlineName, Long domainId) {
		String outlineNsql = "select id from ec2_sip_conf where domain_id = "+domainId+" and username = '"+outlineName+"'";
		List<Long> olids = this.getEntityManager().createNativeQuery(outlineNsql).getResultList();
		if(olids != null && olids.size() > 0) {
			Long olid = olids.get(0);
			List<MarketingProject> projectLs = this.getEntityManager().createQuery("select mp from MarketingProject as mp where mp.sip.id = " + olid).getResultList();
			if(projectLs.size() > 0) {
				return projectLs.get(0);
			}
		}
		return null;
	}
	
}
