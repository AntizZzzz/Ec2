package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.IvrMenuEao;
import com.jiangyifen.ec2.entity.IVRMenu;
import com.jiangyifen.ec2.entity.IVROption;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;
import com.jiangyifen.ec2.service.eaoservice.IvrMenuService;

public class IvrMenuServiceImpl implements IvrMenuService {

	private IvrMenuEao ivrMenuEao;
	
	public IvrMenuEao getIvrMenuEao() {
		return ivrMenuEao;
	}
	
	public void setIvrMenuEao(IvrMenuEao ivrMenuEao) {
		this.ivrMenuEao = ivrMenuEao;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IVRMenu> loadPageEntities(int start, int length, String sql) {
		 
		return ivrMenuEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return ivrMenuEao.getEntityCount(sql);
	}
	
	@Override
	public void save(IVRMenu ivrMenu) {
		ivrMenuEao.save(ivrMenu);
	}

	@Override
	public void delete(IVRMenu ivrMenu) {

		ivrMenuEao.delete(ivrMenu);

	}

	@Override
	public void deleteById(Long ivrMenuId) {
		ivrMenuEao.deleteById(ivrMenuId);
	}

	@Override
	public void update(IVRMenu ivrMenu) {

		ivrMenuEao.update(ivrMenu);

	}

	@Override
	public IVRMenu get(Object primaryKey) {
		return ivrMenuEao.get(IVRMenu.class, primaryKey);
	}
	
	@Override
	public List<IVRMenu> getAllByDomain(Long domainId) {
		return ivrMenuEao.getAllByDomain(domainId);
	}

	@Override
	public List<IVRMenu> getAllByDomain(Long domainId, IVRMenuType menuType) {
		return ivrMenuEao.getAllByDomain(domainId, menuType);
	}

	@Override
	public <T> List<T> getEntities(Class<T> entityClass, String jpql) {
		return ivrMenuEao.getEntities(entityClass, jpql);
	}

	@Override
	public List<SipConfig> getRelatedOutlineByIVRMenuId(Long ivrMenuId) {
		return ivrMenuEao.getRelatedOutlineByIVRMenuId(ivrMenuId);
	}

	@Override
	public boolean createNewIvrByIvrTemplate(IVRMenu ivrMenu, IVRActionType ivrActionType) {
		return ivrMenuEao.createNewIvrByIvrTemplate(ivrMenu, ivrActionType);
	}

	@Override
	public List<IVROption> sortOptionsByPressKey(List<IVROption> ivrOptions) {
		return ivrMenuEao.sortOptionsByPressKey(ivrOptions);
	}

	@Override
	public IVRMenu getById(Long menuId) {
		return ivrMenuEao.get(IVRMenu.class, menuId);
	}
	
}
