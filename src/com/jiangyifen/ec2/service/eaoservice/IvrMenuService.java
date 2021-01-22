package com.jiangyifen.ec2.service.eaoservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;
import com.jiangyifen.ec2.service.common.FlipSupportService;

public interface IvrMenuService extends FlipSupportService<IVRMenu>{
	
	@Transactional
	public void save(IVRMenu ivrMenu);
	
	@Transactional
	public void update(IVRMenu ivrMenu);

	/**
	 * jrh 删除指定的指定的IVR
	 *  注意：删除IVRMenu 时，该方法内部还将与该IVRMenu 相关的 IVRAction、IVROption, 以及OutlineToIvrLink 一同删除
	 * 
	 * @param ivrMenu	待删除的IVR
	 */
	@Transactional
	public void delete(IVRMenu ivrMenu);
	
	/**
	 * jrh 删除指定的指定的IVR, 根据编号删除
	 *  注意：删除IVRMenu 时，该方法内部还将与该IVRMenu 相关的 IVRAction、IVROption, 以及OutlineToIvrLink 一同删除
	 * 
	 * @param ivrMenuId   待删除IVR 的编号
	 */
	@Transactional
	public void deleteById(Long ivrMenuId);

	@Transactional
	public IVRMenu get(Object primaryKey);
	
	/**
	 * 获取指定域下的所有IVRMenu ， 不区分IVRMenuType，如 是否为模板
	 * @author jrh
	 * @param domainId	指定域的编号
	 * @return
	 */
	@Transactional
	public List<IVRMenu> getAllByDomain(Long domainId);
	
	/**
	 * 根据域的编号、以及指定的IVRMenu 的类型【customize("用户自定义IVR", 0),template("模板IVR", 1);】
	 * 	获取所有符合条件的IVRMenu 
	 * @author jrh
	 * @param domainId	指定域的编号
	 * @param menuType	IVR的类型
	 * @return
	 */
	@Transactional
	public List<IVRMenu> getAllByDomain(Long domainId, IVRMenuType menuType);

	@Transactional
	public <T> List<T> getEntities(Class<T> entityClass, String jpql);

	/**
	 * jrh
	 * 	根据指定的IVRMenu编号，获取所有与之关联的外线
	 * 
	 * @param ivrMenuId		IVRMenu对象编号
	 */
	@Transactional
	public List<SipConfig> getRelatedOutlineByIVRMenuId(Long ivrMenuId);

	/**
	 * jrh 根据IVR 模板，创建一个指定类型ivrActionType 的IVR 导航工具（实质是到数据库中复制相应的模板，只是将ID 改变）
	 * 
	 * @param ivrMenu			新建的IVRMenu 对象
	 * @param ivrActionType		指定要创建的IVR 类型
	 * @return boolean
	 */
	@Transactional
	public boolean createNewIvrByIvrTemplate(IVRMenu ivrMenu, IVRActionType ivrActionType);

	/**
	 * jrh 将传入的IVROption 的集合，按 option 的按键进行排序 ，排序的顺序按数字由小到大，如 0、1、2、3、4、5、6、7、8、9、*
	 * 
	 * @param ivrOptions		待排序的IVROption集合
	 * @return List<IVROption>  返回排序后的集合
	 */
	// 不需要事物
	public List<IVROption> sortOptionsByPressKey(List<IVROption> ivrOptions);

	/**
	 * jrh 根据 编号 获取IVRMenu
	 *  
	 * @param menuId  编号
	 * @return
	 */
	@Transactional
	public IVRMenu getById(Long menuId);
	
}
