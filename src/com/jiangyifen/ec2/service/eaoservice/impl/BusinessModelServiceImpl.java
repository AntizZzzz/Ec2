package com.jiangyifen.ec2.service.eaoservice.impl;

import java.io.Serializable;
import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.eao.BusinessModelEao;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.service.eaoservice.BusinessModelService;

public class BusinessModelServiceImpl implements BusinessModelService, Serializable {
	
	private static final long serialVersionUID = -4002625686326238513L;
	
	private BusinessModelEao businessModelEao;

	// common method
	
	@Override
	public BusinessModel get(Object primaryKey) {
		return businessModelEao.get(BusinessModel.class, primaryKey);
	}

	@Override
	public void save(BusinessModel businessModel) {
		businessModelEao.save(businessModel);
	}

	@Override
	public void update(BusinessModel businessModel) {
		businessModelEao.update(businessModel);
	}

	@Override
	public void delete(BusinessModel businessModel) {
		businessModelEao.delete(businessModel);
	}

	@Override
	public void deleteById(Object primaryKey) {
		businessModelEao.delete(BusinessModel.class, primaryKey);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BusinessModel> loadPageEntitys(int start, int length, String sql) {
		return businessModelEao.loadPageEntities(start, length, sql);
	}
	
	@Override
	public int getEntityCount(String sql) {
		return businessModelEao.getEntityCount(sql);
	}
	
	// enhance function method
	
//	@Override
//	public Collection<String> getByRoleName(String roleName) {
//		List<BusinessModel> businessModels = businessModelEao.getByRoleName(roleName);
//		return getBusinessModelStringList(businessModels);
//	}
//	
//	@Override
//	public Collection<String> getAll() {
//		List<BusinessModel> businessModels =  businessModelEao.getAll();
//		return getBusinessModelStringList(businessModels);
//	}
//
//	private List<String> getBusinessModelStringList(
//			List<BusinessModel> businessModels) {
//		List<String> list = new ArrayList<String>();
//		for(BusinessModel businessModel : businessModels) {
//			list.add(businessModel.toString());
//		}
//		return list;
//	}
	
	@Override
	public List<BusinessModel> getModelsByRoleType(RoleType roleType) {
		return businessModelEao.getByRoleType(roleType);
	}
	
	@Override
	public List<BusinessModel> getAllModelsByRoleType(RoleType roleType) {
		return businessModelEao.getAllModelsByRoleType(roleType);
	}
	
	/**
	 * chb
	 * 取出所有的BusinessModel对象
	 * @return
	 */
	@Override
	public List<BusinessModel> getAll() {
		List<BusinessModel> businessModels =  businessModelEao.getAll();
		return businessModels;
	}

	@Override
	public List<BusinessModel> getModelsByRoleId(Long roleId) {
		return businessModelEao.getModelsByRoleId(roleId);
	}
	
	//getter and setter


	public BusinessModelEao getBusinessModelEao() {
		return businessModelEao;
	}

	public void setBusinessModelEao(BusinessModelEao businessModelEao) {
		this.businessModelEao = businessModelEao;
	}

}
