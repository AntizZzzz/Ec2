package com.jiangyifen.ec2.eao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.eao.BusinessModelEao;
import com.jiangyifen.ec2.entity.BusinessModel;

@SuppressWarnings("unchecked")
public class BusinessModelEaoImpl extends BaseEaoImpl implements
		BusinessModelEao {

	@Override
	public List<BusinessModel> getByRoleType(RoleType roleType) {
		String roleTypeStr="";
		if(roleType.equals(RoleType.csr)){
			roleTypeStr="csr";
		}else if(roleType.equals(RoleType.manager)){
			roleTypeStr="manager";
		}else{
			throw new RuntimeException("类型必须为RoleType的csr、manager中的一种！");
		}
		
// JRH - 20141212 修改成原生SQL
		List<BusinessModel> parentBmLs = new ArrayList<BusinessModel>();	// 存储所有父级权限

		StringBuffer nativeSqlStrBf = new StringBuffer();	// 获取所有父级权限
		nativeSqlStrBf.append("select id,application,description,function,module,parent_id from ec2_businessmodel as bm where bm.application='");
		nativeSqlStrBf.append(roleTypeStr);
		nativeSqlStrBf.append("' and bm.module is not null and bm.parent_id is null order by bm.id");
		List<Object[]> resultParentLs = this.getEntityManager().createNativeQuery(nativeSqlStrBf.toString()).getResultList();
		
		for(Object[] parentArr : resultParentLs) {
			Set<BusinessModel> childs = new HashSet<BusinessModel>();
			BusinessModel parentBm = new BusinessModel();
			parentBm.setId((Long)parentArr[0]);
			parentBm.setApplication((String)parentArr[1]);
			parentBm.setDescription((String)parentArr[2]);
			parentBm.setFunction((String)parentArr[3]);
			parentBm.setModule((String)parentArr[4]);
			parentBm.setChilds(childs);
			parentBm.setParent(null);
			parentBmLs.add(parentBm);
			
			StringBuffer nativeSqlChildStrBf = new StringBuffer();	// 获取指定父级下的子级权限
			nativeSqlChildStrBf.append("select id,application,description,function,module,parent_id from ec2_businessmodel as bm where bm.parent_id = ");
			nativeSqlChildStrBf.append(parentArr[0]);
			nativeSqlChildStrBf.append(" and bm.application='");
			nativeSqlChildStrBf.append(roleTypeStr);
			nativeSqlChildStrBf.append("' order by bm.id");
			List<Object[]> resultChildLs = this.getEntityManager().createNativeQuery(nativeSqlChildStrBf.toString()).getResultList();
			for(Object[] childArr : resultChildLs) {
				BusinessModel childBm = new BusinessModel();
				childBm.setId((Long)childArr[0]);
				childBm.setApplication((String)childArr[1]);
				childBm.setDescription((String)childArr[2]);
				childBm.setFunction((String)childArr[3]);
				childBm.setModule((String)childArr[4]);
				childBm.setParent(parentBm);
				childs.add(childBm);
			}
		}
		
		return parentBmLs;
		
//JRH - 原来的JPQL 形式
//		String sql="select bm from BusinessModel as bm where bm.application='"+roleTypeStr+"' and bm.module is not null and bm.parent is null order by bm.id";
//		return getEntityManager().createQuery(sql).getResultList();
	}
	
	/**
	 * chb
	 * 取出所有的BusinessModel对象
	 * @return
	 */
	@Override
	public List<BusinessModel> getAll() {
		String sql="select bm from BusinessModel as bm";
		return getEntityManager().createQuery(sql).getResultList();
	}

	@Override
	public List<BusinessModel> getAllModelsByRoleType(RoleType roleType) {
		String roleTypeStr="";
		if(roleType.equals(RoleType.csr)){
			roleTypeStr="csr";
		}else if(roleType.equals(RoleType.manager)){
			roleTypeStr="manager";
		}else{
			throw new RuntimeException("类型必须为RoleType的csr、manager中的一种！");
		}
		
// JRH - 20141212 修改成原生SQL
		List<BusinessModel> allBmLs = new ArrayList<BusinessModel>();	// 存储所有父级权限

		StringBuffer nativeSqlStrBf = new StringBuffer();	// 获取所有父级权限
		nativeSqlStrBf.append("select id,application,description,function,module,parent_id from ec2_businessmodel as bm where bm.application='");
		nativeSqlStrBf.append(roleTypeStr);
		nativeSqlStrBf.append("' and bm.module is not null and bm.parent_id is null order by bm.id");
		List<Object[]> resultParentLs = this.getEntityManager().createNativeQuery(nativeSqlStrBf.toString()).getResultList();
		
		for(Object[] parentArr : resultParentLs) {
			Set<BusinessModel> childs = new HashSet<BusinessModel>();
			BusinessModel parentBm = new BusinessModel();
			parentBm.setId((Long)parentArr[0]);
			parentBm.setApplication((String)parentArr[1]);
			parentBm.setDescription((String)parentArr[2]);
			parentBm.setFunction((String)parentArr[3]);
			parentBm.setModule((String)parentArr[4]);
			parentBm.setChilds(childs);
			parentBm.setParent(null);
			allBmLs.add(parentBm);
			
			StringBuffer nativeSqlChildStrBf = new StringBuffer();	// 获取指定父级下的子级权限
			nativeSqlChildStrBf.append("select id,application,description,function,module,parent_id from ec2_businessmodel as bm where bm.parent_id = ");
			nativeSqlChildStrBf.append(parentArr[0]);
			nativeSqlChildStrBf.append(" and bm.application='");
			nativeSqlChildStrBf.append(roleTypeStr);
			nativeSqlChildStrBf.append("' order by bm.id");
			List<Object[]> resultChildLs = this.getEntityManager().createNativeQuery(nativeSqlChildStrBf.toString()).getResultList();
			for(Object[] childArr : resultChildLs) {
				BusinessModel childBm = new BusinessModel();
				childBm.setId((Long)childArr[0]);
				childBm.setApplication((String)childArr[1]);
				childBm.setDescription((String)childArr[2]);
				childBm.setFunction((String)childArr[3]);
				childBm.setModule((String)childArr[4]);
				childBm.setParent(parentBm);
				childs.add(childBm);
				allBmLs.add(childBm);
			}
		}
		
		return allBmLs;		

//JRH - 原来的JPQL 形式
//		String sql="select bm from BusinessModel as bm where bm.application='"+roleTypeStr+"' and bm.module is not null order by bm.id";
//		return getEntityManager().createQuery(sql).getResultList();
	}

	@Override
	public List<BusinessModel> getModelsByRoleId(Long roleId) {
		StringBuffer nativeSqlStrBf = new StringBuffer();	// 获取指定角色关联的用户编号
		nativeSqlStrBf.append(" select businessmodel_id from ec2_role_businessmodel_link as rb where rb.role_id = ");
		nativeSqlStrBf.append(roleId);
		nativeSqlStrBf.append(" order by rb.businessmodel_id asc");
		List<Long> bmIdLs = this.getEntityManager().createNativeQuery(nativeSqlStrBf.toString()).getResultList();
		bmIdLs.add(0L);
		String bmIdSql = StringUtils.join(bmIdLs, ",");
		
		String jpql = "select bm from BusinessModel as bm where bm.id in ("+ bmIdSql + ")";
		return this.getEntityManager().createQuery(jpql).getResultList();
	}
	
}
