package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.OutlineToIvrLink;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
 * 
 * @Description 描述：外线与IVR 的关联关系维护工具
 * 
 * @author  jrh
 * @date    2014年3月5日 下午6:06:22
 * @version v1.0.0
 */
public interface OutlineToIvrLinkService extends FlipSupportService<OutlineToIvrLink> {

	// common method 
	
	@Transactional
	public OutlineToIvrLink get(Object primaryKey);
	
	/**
	 * 新建外线与IVR 的关联 时，还需要从数据库中查出当前最大的优先级值，然后在此值上+1，并付给outlineToIvrLink
	 * @author jrh
	 * @param outlineToIvrLink	待保存的对象
	 */
	@Transactional
	public void save(OutlineToIvrLink outlineToIvrLink);

	@Transactional
	public OutlineToIvrLink update(OutlineToIvrLink outlineToIvrLink);

	@Transactional
	public void delete(OutlineToIvrLink outlineToIvrLink);
	
	@Transactional
	public void deleteById(Long primaryKey);
	
	// enhanced method
	
	/**
	 * 获取整个域下的所有外线与IVR 的关联关系
	 * @author jrh
	 * @param domainId	域的编号
	 * @return
	 */
	@Transactional
	public List<OutlineToIvrLink> getAllByDomain(Long domainId);
	
	/**
	 * 获取整个域下的所有处于指定可用状态的外线与IVR 的关联关系
	 * @author jrh
	 * @param domainId	域的编号
	 * @param isUseable	可用状态
	 * @return
	 */
	@Transactional
	public List<OutlineToIvrLink> getAllByDomain(Long domainId, boolean isUseable);

	/**
	 * 编辑外线与IVR的关联关系 的优先级别
	 * 		【注意：一个Link 的升级，必然导致另一个Link 的降级；同理降级也一样】
	 * @author jrh
	 * @param outlineToIvrLink	带升级或者降级的对象
	 * @param isUpgrade			是否是要升级
	 */
	@Transactional
	public void editOtilPriority(OutlineToIvrLink outlineToIvrLink, boolean isUpgrade);
	
}
