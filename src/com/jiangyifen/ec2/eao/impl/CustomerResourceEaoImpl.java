package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerResourceEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Telephone;

public class CustomerResourceEaoImpl extends BaseEaoImpl implements CustomerResourceEao {
	/**
	 * chb
	 * 根据批次查找批次对应的资源
	 * @param batch
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerResource> getCustomerResourceByBatch(CustomerResourceBatch batch) {
		//隐含了域的概念
		return getEntityManager().createQuery("select cr from CustomerResource as cr where cr.customerResourceBatch.id="+batch.getId()).getResultList();
	}
	/**
	 * 级联删除Resource
	 * chb
	 */
	@Override
	public <T> void delete(Class<T> entityClass,final Object primaryKey) {
		  //隐含域的概念
		 //删除客服记录
		  String sqlRecord1="delete from CustomerServiceRecord csr where csr.customerResource.id="+primaryKey;
		  String sqlRecord2="delete from CustomerComplaintRecord ccr where ccr.customerResource.id="+primaryKey;
		  //删除描述信息
		  String sqlDescription="delete from CustomerResourceDescription crd where crd.customerResource.id="+primaryKey;
		  //删除任务
		  String sqlTask="delete from MarketingProjectTask mpt where mpt.customerResource.id="+primaryKey;
		  //删除资源
		  String sqlResource="delete from CustomerResource cr where cr.id="+primaryKey;
		  getEntityManager().createQuery(sqlRecord1).executeUpdate();
		  getEntityManager().createQuery(sqlRecord2).executeUpdate();
		  getEntityManager().createQuery(sqlDescription).executeUpdate();
		  getEntityManager().createQuery(sqlTask).executeUpdate();
		  getEntityManager().createQuery(sqlResource).executeUpdate();
//用原生SQL删除，EL也能知道，资源条数信息不更新
//		  super.delete(CustomerResource.class, primaryKey);
	}
	
	/**
	 * chb
	 * 根据电话号码查找某个域内对应的资源，如果返回null表示该域中还没有存储此电话号码，如果找到多个电话号码符合条件，则抛出异常
	 * @param batch
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CustomerResource getCustomerResourceByPhoneNumber(
			String phoneNumber, Long domainId) { 
//		jrh 带0与不带0 系属同一电话
		String remoteNum = "";	// 带 0 的号码
		String localNum = "";	// 不带 0 的号码
		if(phoneNumber.startsWith("0")) {
			remoteNum = phoneNumber;
			localNum = phoneNumber.substring(1);
		} else {
			remoteNum = "0" + phoneNumber;
			localNum = phoneNumber;
		}
		
		String sql="select t from Telephone t where (t.number='"+localNum+"' or t.number='"+remoteNum+"') and t.domain.id="+domainId;
		//一个电话号码只能取出一个客户资源，在电话号码表中，表中的域内电话号码唯一
		List<Telephone> telephones=(List<Telephone>)getEntityManager().createQuery(sql).getResultList();
		//此电话号码从来没有被存储过
		if(telephones.size()==0) return null;
//		TODO add it
//		if(telephones.size()>1){
//			throw new RuntimeException("电话号码发生重复！");
//		}
		return telephones.get(0).getCustomerResource();
	}

	/**
	 * chb
	 * 按照步长查找List
	 * @param nativeSql
	 * @param step
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Long> loadStepRows(String nativeSql, int stepSize) {
		return getEntityManager().createNativeQuery(nativeSql).setFirstResult(0).setMaxResults(stepSize).getResultList();
	}
	
	/**
	 * chb
	 * 通过Id来验证是否是客户
	 * @param id
	 * @return
	 */
	@Override
	public Boolean isCustomerById(Long id, Domain domain) {
		String nativeSql="SELECT count(*) FROM ec2_project_customer where customer_resource_id="+id+" and domain_id="+domain.getId();
		Long count=(Long)getEntityManager().createNativeQuery(nativeSql).getSingleResult();
		if(count==0L){
			return false;
		}
		return true;
	}
	
	/**
	 * jrh 
	 * 	根据搜索语句获取相应的所有客户对象
	 * @param searchSql
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerResource> getAllBySql(String searchSql) {
		return getEntityManager().createQuery(searchSql).getResultList();
	}
}
