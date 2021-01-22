package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.common.FlipSupportService;


public interface MarketingProjectService extends FlipSupportService<MarketingProject>{
	
	// enhanced method 
	
	
//	 extends CommonTable.TableService
	public MarketingProject get(Object primaryKey);
	@Transactional
	public void save(MarketingProject marketingProject);
	@Transactional
	public MarketingProject update(MarketingProject marketingProject);
	@Transactional
	public void delete(MarketingProject marketingProject);
	@Transactional
	public void deleteById(Object primaryKey);
	
	public List<MarketingProject> loadPageEntities(int start,int length,String sql);
	
	public int getEntityCount(String sql);
	
	/**
	 * chb
	 *  取出所有已经分配到指定项目任务中的Csr集合
	 * @param marketingProject
	 * @param domain
	 * @return
	 */
	@Transactional
	public List<User> getCsrsByProject(MarketingProject marketingProject,
			Domain domain);
	/**
	 * chb
	 * 取出所有的项目
	 */
	public List<MarketingProject> getAll(Domain domain);

	/**
	 * chb
	 * 取出所有域的项目
	 */
	public List<MarketingProject> getAll();
	
	/**
	 * jrh 
	 * 	根据分机（外线），获取所有与之关联的项目
	 * @param outline 外线
	 * @return
	 */
	@Transactional
	public List<MarketingProject> getAllBySip(SipConfig outline);
	
	/**
	 * jrh
	 * 	获取指定域中使用了指定队列的所有项目
	 * @param queueId	队列的Id号
	 * @param domainId	域的Id号
	 * @return List<MarketingProject> 返回的项目集合
	 */
	@Transactional
	public List<MarketingProject> getAllByQueue(Long queueId, Long domainId);

	/**
	 * jrh
	 * 获取指定域下指定部门创建的所有项目
	 * @param deptIds		 所有部门的Id号	
	 * @param domainId  	域的Id号
	 * @return
	 */
	@Transactional
	public List<MarketingProject> getAllByDepartments(List<Long> deptIds, Long domainId);
	
	/**
	 *  jrh
	 *  获取分配给某一用户的所有的项目
	 * @param user 被分配过项目的用户
	 * @return List 存放项目的集合
	 */
	@Transactional
	public List<MarketingProject> getAllProjectsByUser(User user);
	
	/**
	 *  jrh
	 *  获取分配给某一用户的处于指定状态的所有项目
	 * @param user 被分配过项目的用户
	 * @param projectStatus 项目状态
	 * @return List 存放项目的集合
	 */
	@Transactional
	public List<MarketingProject> getProjectsByUserAndStatus(User user, MarketingProjectStatus projectStatus);
	
	/**
	 * @Description 描述：根据外线名称获取对应的外线
	 *
	 * @author  JRH
	 * @date    2014年7月11日 下午2:35:07
	 * @param outlineName	外线名称
	 * @param domainId		域编号
	 * @return MarketingProject
	 */
	@Transactional
	public MarketingProject getAllByOutlineName(String outlineName, Long domainId);

}
