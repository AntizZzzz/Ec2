package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.IVROptionEao;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.service.eaoservice.IVROptionService;

public class IVROptionServiceImpl implements IVROptionService {

	private IVROptionEao ivrOptionEao;

	public IVROptionEao getIvrOptionEao() {
		return ivrOptionEao;
	}

	public void setIvrOptionEao(IVROptionEao ivrOptionEao) {
		this.ivrOptionEao = ivrOptionEao;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IVROption> loadPageEntities(int start, int length, String sql) {

		return ivrOptionEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {

		return ivrOptionEao.getEntityCount(sql);
	}
	
	@Override
	public void update(IVROption ivrOption) {
		ivrOptionEao.update(ivrOption);
	}
	
	@Override
	public void save(IVROption ivrOption) {

		ivrOptionEao.save(ivrOption);
	}

	@Override
	public void delete(IVROption ivrOption) {
		ivrOptionEao.delete(ivrOption);
	}

	@Override
	public void deleteById(Long ivrOptionId) {
		ivrOptionEao.deleteById(ivrOptionId);
	}

	@Override
	public List<IVROption> getAllIVROptions(Long domainId) {

		return ivrOptionEao.getAllIVROptions(domainId);
	}

	@Override
	public IVROption getByActionIdAndPressNum(String pressNum,
			Long currentActionId) {
		return ivrOptionEao.getByActionIdAndPressNum(pressNum, currentActionId);
	}

	@Override
	public List<String> getUseableKeyByActionId(Long parentActionId, Long domainId, String remainKey) {
		return ivrOptionEao.getUseableKeyByActionId(parentActionId, domainId, remainKey);
	}

	@Override
	public List<IVROption> getAllByActionId(Long actionId, Long domainId) {
		return ivrOptionEao.getAllByActionId(actionId, domainId);
	}

}
