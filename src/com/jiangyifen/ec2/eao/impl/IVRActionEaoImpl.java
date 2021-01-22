package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import javax.persistence.EntityManager;

import com.jiangyifen.ec2.eao.IVRActionEao;
import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.entity.enumtype.IVROptionType;

public class IVRActionEaoImpl extends BaseEaoImpl implements IVRActionEao {

	@Override
	public void save(IVRAction rootAction) {
		EntityManager em = this.getEntityManager();
		em.persist(rootAction);
		if(rootAction.getIsRootAction()) {	// 这里正常情况下肯定成立
			IVRMenu ivrMenu = rootAction.getIvrMenu();
			ivrMenu.setRootActionType(rootAction.getActionType());
			em.merge(ivrMenu);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IVRAction> getAllIVRActions(Long domainId) {
		return getEntityManager().createQuery("select i from IVRAction as i where i.domain.id = "+domainId).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IVRAction getRootIVRActionByIVRMenu(Long ivrMenuId) {
		String jpql = "select i from IVRAction as i where i.ivrMenu.id = "+ivrMenuId+" and i.isRootAction = true";
		List<IVRAction> actions = this.getEntityManager().createQuery(jpql).getResultList();
		if(actions.size() > 0) {	// 正常情况下 size 不会大于1，否则，就表示数据库存储有问题
			return actions.get(0);
		}
		return null;
	}

	/** jrh 使用递归算法执行删除操作:删除IVRAction，该方法内部还将与IVRAction 相关联的的 IVROption,以及当前Action下的所有子Action 和Option */
	@Override
	public void delete(IVRAction ivrAction) {
		cascadeDeleteIvrBranchByAction(ivrAction);
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
	@SuppressWarnings("unchecked")
	private void cascadeDeleteIvrBranchByAction(IVRAction ivrAction) {
		String jpql = "select o from IVROption as o where o.domain.id = "+ivrAction.getDomain().getId()+" and o.currentIvrAction.id = "+ivrAction.getId();
		List<IVROption> ivrOptions = this.getEntityManager().createQuery(jpql).getResultList();
		
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

	/** jrh 使用递归算法执行删除操作:删除IVRAction，该方法内部还将与IVRAction 相关联的的 IVROption,以及当前Action下的所有子Action 和Option */
	@SuppressWarnings("unchecked")
	@Override
	public void deleteById(Long ivrActionId) {
		String jpql = "select a from IVRAction as a where a.id = "+ivrActionId;
		List<IVRAction> ivrActions = this.getEntityManager().createQuery(jpql).getResultList();
		if(ivrActions.size() > 0) {
			delete(ivrActions.get(0));
		}
	}

	@Override
	public void updateIvrBranch(IVRAction ivrAction, IVROption ivrOption) {
		this.getEntityManager().merge(ivrOption);
		this.getEntityManager().merge(ivrAction);
	}

	@Override
	public void createBranch(IVRAction newAction, IVROption newOption) {
		this.getEntityManager().persist(newAction);
		
		newOption.setNextIvrAction(newAction);
		this.getEntityManager().persist(newOption);
	}

	
}
