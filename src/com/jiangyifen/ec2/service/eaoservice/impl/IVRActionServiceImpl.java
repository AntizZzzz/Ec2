package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.IVRActionEao;
import com.jiangyifen.ec2.entity.IVRAction;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.service.eaoservice.IVRActionService;

public class IVRActionServiceImpl implements IVRActionService {

	private IVRActionEao actionEao;

	public IVRActionEao getActionEao() {
		return actionEao;
	}

	public void setActionEao(IVRActionEao actionEao) {
		this.actionEao = actionEao;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IVRAction> loadPageEntities(int start, int length, String sql) {
		return actionEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {

		return actionEao.getEntityCount(sql);
	}
	
	@Override
	public void save(IVRAction ivrAction) {
		actionEao.save(ivrAction);
	}

	@Override
	public void update(IVRAction ivrAction) {
		actionEao.update(ivrAction);
	}
	
	/** jrh 使用递归算法执行删除操作:删除IVRAction，该方法内部还将与IVRAction 相关联的的 IVROption,以及当前Action下的所有子Action 和Option */
	@Override
	public void delete(IVRAction ivrAction) {
		actionEao.delete(ivrAction);
	}

	/** jrh 使用递归算法执行删除操作:删除IVRAction，该方法内部还将与IVRAction 相关联的的 IVROption,以及当前Action下的所有子Action 和Option */
	@Override
	public void deleteById(Long ivrActionId) {
		actionEao.deleteById(ivrActionId);
	}

	@Override
	public List<IVRAction> getAllIVRActions(Long domainId) {

		return actionEao.getAllIVRActions(domainId);
	}

	@Override
	public IVRAction getRootIVRActionByIVRMenu(Long ivrMenuId) {
		return actionEao.getRootIVRActionByIVRMenu(ivrMenuId);
	}

	@Override
	public void updateIvrBranch(IVRAction ivrAction, IVROption ivrOption) {
		actionEao.updateIvrBranch(ivrAction, ivrOption);
	}

	@Override
	public void createBranch(IVRAction newAction, IVROption newOption) {
		actionEao.createBranch(newAction, newOption);
	}


}
