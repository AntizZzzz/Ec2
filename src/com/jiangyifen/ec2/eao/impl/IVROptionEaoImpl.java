package com.jiangyifen.ec2.eao.impl;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.eao.IVROptionEao;
import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.entity.enumtype.IVROptionType;

public class IVROptionEaoImpl extends BaseEaoImpl implements IVROptionEao {
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IVROption> getAllIVROptions(Long domainId) {

		return getEntityManager().createQuery("select i from IVROption i where i.domain.id = "+domainId).getResultList();
	}

	/** 使用递归算法执行删除操作:删除IVROption，该方法内部还将与IVROption 相关联的的 IVRAction,以及关联的IVRAction下的所有子Action 和Option */
	@Override
	public void delete(IVROption ivrOption) {
		cascadeDeleteIvrBranchByOpction(ivrOption);
	}

	/**
	 * jrh 删除IVROption，该方法内部还将与IVROption 相关联的的 IVRAction,以及关联的IVRAction下的所有子Action 和Option， 
	 *      删除次序：层层深入，如果一个IVRAction 有对应的 IVROption(即，有下一层子节点)，则暂时不删除当前IVRAction，继续往下一层走，
	 *      	   如果，当前IVRAction 没有下对应的IVROption，则删除当前 action，并找出按键对象 IVROption的下一步动作是当前Action 的Option，将其删除
	 *  
	 *  所以，这里需要使用递归算法执行删除操作
	 *      
	 * @param ivrOption 待删除的ivrOption对象
	 */
	private void cascadeDeleteIvrBranchByOpction(IVROption ivrOption) {
		IVROptionType ivrOptionType = ivrOption.getIvrOptionType();
		Long ivrOptionId = ivrOption.getId();
		if(IVROptionType.toRepeat.equals(ivrOptionType)  				// 如果当前删除的Option 类型是 “重听、返回上一级、返回主菜单”中的任意一个，则直接删除按键IVROption 对象
				|| IVROptionType.toReturnPre.equals(ivrOptionType) 
				|| IVROptionType.toReturnRoot.equals(ivrOptionType)) {

			String deleteOptSql = "delete from ec2_ivr_option where id = "+ivrOptionId;
			this.getEntityManager().createNativeQuery(deleteOptSql).executeUpdate();
		} else {
			IVRAction nextAction = ivrOption.getNextIvrAction();
			cascadeDeleteIvrBranchByAction(nextAction);
		}
	}

	/**
	 * jrh 删除IVRAction，该方法内部还将与IVRAction 相关联的的 IVROption,以及当前Action下的所有子Action 和Option， 
	 *      删除次序：层层深入，如果一个IVRAction 有对应的 IVROption(即，有下一层子节点)，则暂时不删除当前IVRAction，继续往下一层走，
	 *      	   如果，当前IVRAction 没有下对应的IVROption，则删除当前 action，并找出按键对象 IVROption的下一步动作是当前Action 的Option，将其删除
	 *  
	 *  所以，这里需要使用递归算法执行删除操作
	 *      
	 * @param ivrAction 待删除的IVRAction对象
	 */
	private void cascadeDeleteIvrBranchByAction(IVRAction ivrAction) {
		List<IVROption> ivrOptions = getAllByActionId(ivrAction.getId(), ivrAction.getDomain().getId());
		Long ivrActionId = ivrAction.getId();
		if(ivrOptions.size() > 0) {
			for(IVROption ivrOption : ivrOptions) {
				IVROptionType ivrOptionType = ivrOption.getIvrOptionType();
				Long ivrOptionId = ivrOption.getId();
				
				if(IVROptionType.toRepeat.equals(ivrOptionType) 				// 如果当前删除的Option 类型是 “重听、返回上一级、返回主菜单”中的任意一个，则直接删除按键IVROption 对象
						|| IVROptionType.toReturnPre.equals(ivrOptionType) 
						|| IVROptionType.toReturnRoot.equals(ivrOptionType)) {

					String deleteOptSql = "delete from ec2_ivr_option where id = "+ivrOptionId;
					this.getEntityManager().createNativeQuery(deleteOptSql).executeUpdate();
				} else {	// 如果按键对象 IVROption 的下一步操作时执行一个新建的Action，则继续执行删除Action的递归方法
					IVRAction nextAction = ivrOption.getNextIvrAction();
					cascadeDeleteIvrBranchByAction(nextAction);
				}
			}
		}
		// 最后需要将自身IVRAction 给删除掉
		String optionDeleteSql = "delete from ec2_ivr_option where nextivraction_id = "+ivrActionId;
		String actionDeleteJpql = "delete from IVRAction as a where a.id = "+ivrActionId;

		this.getEntityManager().createNativeQuery(optionDeleteSql).executeUpdate();
		this.getEntityManager().createQuery(actionDeleteJpql).executeUpdate();
	}

	/** 使用递归算法执行删除操作:删除IVROption，该方法内部还将与IVROption 相关联的的 IVRAction,以及关联的IVRAction下的所有子Action 和Option */
	@SuppressWarnings("unchecked")
	@Override
	public void deleteById(Long ivrOptionId) {
		String jpql = "select o from IVROption as o where o.id = "+ivrOptionId;
		List<IVROption> ivrOptions = this.getEntityManager().createQuery(jpql).getResultList();
		if(ivrOptions.size() > 0) {
			delete(ivrOptions.get(0));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IVROption getByActionIdAndPressNum(String pressNum, Long currentActionId) {
		String jpql = "select i from IVROption as i where i.currentIvrAction.id = "+currentActionId+" and i.pressNumber = '"+pressNum+"'";
		List<IVROption> options = this.getEntityManager().createQuery(jpql).getResultList();
		if(options.size() > 0) {	// 正常不会出现 size 大于1 的情况，如果出现则表示数据库存储有问题
			return options.get(0);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getUseableKeyByActionId(Long parentActionId, Long domainId, String remainKey) {
		String nativeSql = "select pressnumber from ec2_ivr_option where domain_id = "+domainId+" and  currentivraction_id = "+parentActionId;
		List<String> usedKeys = this.getEntityManager().createNativeQuery(nativeSql).getResultList();
		
		if(remainKey != null) {			// 如果存在需要保留的按键，则从已经用过的按键中移除remainKey
			usedKeys.remove(remainKey);
		}
		
		ArrayList<String> useableKeys = new ArrayList<String>();
		if(usedKeys.size() == 0) {
			for(String key : IVROption.press_keys) {
				useableKeys.add(key);
			}
		} else {
			for(String key : IVROption.press_keys) {
				if(!usedKeys.contains(key)) {
					useableKeys.add(key);
				}
			}
		}
		
		return useableKeys;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IVROption> getAllByActionId(Long actionId, Long domainId) {
		String jpql = "select o from IVROption as o where o.domain.id = "+domainId+" and o.currentIvrAction.id = "+actionId;
		return this.getEntityManager().createQuery(jpql).getResultList();
	}
	
}
