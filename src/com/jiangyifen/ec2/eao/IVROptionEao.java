package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.IVROption;

public interface IVROptionEao extends BaseEao {

	public List<IVROption> getAllIVROptions(Long domainId);

	/**
	 * jrh 根据编号 ，删除IVR 导航的IVROption
	 * 		注意：使用递归算法执行删除操作:删除IVROption，该方法内部还将与IVROption 相关联的的 IVRAction,以及关联的IVRAction下的所有子Action 和Option
	 * 
	 * @param ivrAction 待删除的IVRAction对象
	 */
	public void delete(IVROption ivrOption);

	/**
	 * jrh 根据编号 ，删除IVR 导航的IVROption
	 * 		注意：使用递归算法执行删除操作:删除IVROption，该方法内部还将与IVROption 相关联的的 IVRAction,以及关联的IVRAction下的所有子Action 和Option
	 * 
	 * @param ivrAction 待删除的IVRAction对象
	 */
	public void deleteById(Long ivrOptionId);
	
	/**
	 * jrh 根据用户按键，及当前客户所在的IVR 层次所对应的Action 的编号，获取Option
	 * @param pressNum			客户按键值
	 * @param currentActionId	当前的ActionId
	 * @return
	 */
	public IVROption getByActionIdAndPressNum(String pressNum, Long currentActionId);

	/**
	 * jrh 根据上级Action 的编号，获取可以使用的按键集合
	 * 	         如果存在需要保留的按键，则从已经用过的按键中移除remainKey
	 *  注意：如果是新建，则remainKey 传一个 null； 如果是编辑非根分支，则需要传递当前编辑的Option 对应的按键 pressNumber
	 * 
	 * @param parentActionId	上级Action 的编号
	 * @param domainId			所在域 的编号
	 * @param remainKey			要保留的按键
	 * @return
	 */
	public List<String> getUseableKeyByActionId(Long parentActionId, Long domainId, String remainKey);

	/**
	 * jrh 获取指定域下 IVROption 中 currentAction 的编号是 actionId 的所有 IVROption 
	 * @param actionId	action 编号
	 * @param domainId  域编号
	 * @return List<IVROption> 
	 */
	public List<IVROption> getAllByActionId(Long actionId, Long domainId);
	
}
