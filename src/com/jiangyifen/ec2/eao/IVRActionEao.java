package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVROption;

public interface IVRActionEao extends BaseEao {

	/**
	 * 重载BaseEao 的保存方法，该方法用于给IVRMenu 创建根Action 
	 * 	这其中，在创建根Action 时，需要修改IVRMenu 的rootActionType
	 * @author jrh
	 * @param rootAction
	 */
	public void save(IVRAction rootAction);
	
	/**
	 * 获取整个域下的IVRAction 分支
	 * @author jrh
	 * @param domainId
	 * @return
	 */
	public List<IVRAction> getAllIVRActions(Long domainId);
	
	/**
	 * jrh 根据编号 ，删除IVR 导航的IVRAction
	 * 		注意：使用递归算法执行删除操作:删除IVRAction，该方法内部还将与IVRAction 相关联的的 IVROption,以及当前Action下的所有子Action 和Option
	 * 
	 * @param ivrAction 待删除的IVRAction对象
	 */
	public void delete(IVRAction ivrAction);
	
	/**
	 * jrh 根据编号 ，删除IVR 导航的IVRAction
	 * 		注意：使用递归算法执行删除操作:删除IVRAction，该方法内部还将与IVRAction 相关联的的 IVROption,以及当前Action下的所有子Action 和Option
	 * 
	 * @param ivrActionId 待删除的IVRAction的编号
	 */
	public void deleteById(Long ivrActionId);
	
	/**
	 * jrh 根据IVR Menu 的编号，查找出IVR 的第一级Action 
	 * 
	 * @param ivrMenuId	IVRMenu的编号
	 * @return IVRAction 
	 */
	public IVRAction getRootIVRActionByIVRMenu(Long ivrMenuId);

	/**
	 * jrh 更新IVR 的分支，一个分支一般包含一个IVRAction 和一个IVROption 对象
	 * 
	 * @param ivrAction		需要更新的 action
	 * @param ivrOption		需要更新的 option
	 */
	public void updateIvrBranch(IVRAction ivrAction, IVROption ivrOption);

	/**
	 * jrh 根据当前的 IVRAction 和 IVROption 创建IVR 语音导航流程的分支
	 * 
	 * @param newAction	对象 IVRAction 
	 * @param newOption 对象 newOption
	 */
	public void createBranch(IVRAction newAction, IVROption newOption);
	
}
