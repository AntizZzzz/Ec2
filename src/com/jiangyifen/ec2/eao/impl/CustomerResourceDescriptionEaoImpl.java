package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerResourceDescriptionEao;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.Domain;

public class CustomerResourceDescriptionEaoImpl extends BaseEaoImpl implements CustomerResourceDescriptionEao {
	/**
	 * chb
	 * 取得同一域内所有不同的Key值，以方便整理
	 * DistinctKeys
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getDistinctKeys(Domain domain) {
		return getEntityManager().createQuery("select distinct crd.key from CustomerResourceDescription crd where crd.domain.id="+domain.getId()).getResultList();
	}
	
	/**
	 * chb
	 * @param toUpdateList 待更新的一组Key值
	 * @param updateTo 更新Key值为updateTo
	 * @param domain 域
	 */
	@Override
	public void updateKey(List<String> toUpdateList,final String updateTo,
			final Domain domain) {
		for(String toUpdate:toUpdateList){
//			List<CustomerResourceDescription> descriptionList = this.loadPageEntities(0, Integer.MAX_VALUE, "select crd from CustomerResourceDescription crd where crd.domain.id="+domain.getId());
//			for(int i=0;i<descriptionList.size();i++){
//				CustomerResourceDescription description=descriptionList.get(i);
//				if(toUpdateList.contains(description.getKey())){
//					description.setKey(updateTo);
//					this.update(description);
//				}
//			}
			
			//更新不支持   连接表查询
			final String toUpdateStr=toUpdate;
			String sql="update CustomerResourceDescription crd set crd.key='"+updateTo+"' where crd.domain.id="+domain.getId()+" and crd.key='"+toUpdateStr+"'";
			getEntityManager().createQuery(sql).executeUpdate();
		}
	}
	
	/**
	 * chb
	 * 导数据时对描述信息的查询
	 * @param key
	 * @param value
	 * @param resourceId
	 * @param domain
	 * @return
	 */
	@Override
	public Boolean isExistDescription(String key, String value, Long resourceId, Long domainId) {
		value=value==null?"":value;
		String nativeSql="select count(*) from ec2_customer_resource_description " +
				"where key='"+key+"' and value='"+value+"' and customerresource_id="+resourceId+" and domain_id="+domainId;
		Long count=(Long)getEntityManager().createNativeQuery(nativeSql).getSingleResult();
		//如果数量大于0，则返回true，否则返回false
		return count>0L?true:false;
	}

	/** jrh 	获取某个资源的所有描述信息 <br/> @param customerId	资源编号 <br/> @return */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerResourceDescription> getAllByCustomerId(Long customerId) {
		String sql = "select e from CustomerResourceDescription as e where e.customerResource.id = "+ customerId + " order by e.id asc";
		return this.getEntityManager().createQuery(sql).getResultList();
	}
	
	/**
	 * jinht
	 * 导数据时对描述信息的查询
	 * @param key
	 * @param value
	 * @param resourceId
	 * @param domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CustomerResourceDescription getExistDescription(String key, String value, Long resourceId, Long domainId) {
		if(resourceId == null || domainId == null) {
			return null;
		}
		String sql = "select s from CustomerResourceDescription as s where s.key = '"+key+"' and s.value = '"+value+"' and s.customerResource.id = "+resourceId + " and s.domain.id = "+domainId;
		List<CustomerResourceDescription> crdList = this.getEntityManager().createQuery(sql).getResultList();
		if(crdList != null && crdList.size() > 0) {
			return crdList.get(0);
		}
		return null;
	}
	
}
