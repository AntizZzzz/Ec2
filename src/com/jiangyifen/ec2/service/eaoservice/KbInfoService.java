package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.KbInfo;
import com.jiangyifen.ec2.entity.KbInfoType;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：知识库管理
* 
* @author lxy
*
*/
public interface KbInfoService extends FlipSupportService<KbInfo>  {
	
	/**
	 * 根据主键ID获得知识库
	 * @param id	主键ID 
	 * @return		知识库，一条或null
	 */
	@Transactional
	public KbInfo getKbInfoById(Long id);
	
	/**
	 * 保存知识库
	 * @param kbInfo	知识库
	 * 
	 */
	@Transactional
	public void saveKbInfo(KbInfo kbInfo);
	
	/**
	 * 更新知识库
	 * @param kbInfo	知识库
	 * 
	 */
	@Transactional
	public KbInfo updateKbInfo(KbInfo kbInfo);
	
	/**
	 * 删除知识库
	 * @param kbInfo	知识库
	 * 
	 */
	@Transactional
	public void deleteKbInfo(KbInfo kbInfo);

	/**
	 * 查看给类型下知识的总数
	 * @param id
	 * @return
	 */
	public int getKbInfoCountByTypeId(long id);

	/**
	 * 根据类型编号删除所有该编号下的类型
	 * @param id
	 */
	@Transactional
	public void deleteKbInfoTypeByTypeId(long id);

	/**
	 * 查询 该标题，该类别，该域
	 * @param title
	 * @param infoType
	 * @param domainid
	 * @return
	 */
	public List<KbInfo> getKbInfoCountByTitle(String title,KbInfoType infoType, Long domainid);

	/**
	 * 根据id删除知识对象在service即一下层使用删除
	 * @param id
	 */
	@Transactional
	public void deleteKbInfoById(Long id);
	
	/**
	 * 根据父ID获得该id的所有自idList集合
	 * @param pid
	 * @param ids
	 * @return
	 */
	public List<Long> getTypeIds(Long pid,List<Long> ids);
}