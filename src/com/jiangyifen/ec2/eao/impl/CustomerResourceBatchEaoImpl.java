package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.CustomerResourceBatchEao;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;

public class CustomerResourceBatchEaoImpl extends BaseEaoImpl implements
		CustomerResourceBatchEao {
	/**
	 * 级联删除一个批次
	 * <p>
	 * 批次信息中隐含由域的概念,所以不需要域参数
	 * </p>
	 */
	@Override
	public <T> void delete(Class<T> entityClass, final Object primaryKey) {
		// 删除描述
		String sqlDescription = "delete from CustomerResourceDescription crd where crd.customerResource.customerResourceBatch.id="
				+ primaryKey
				+ " and crd.customerResource.isCustomer is not true";
		// 删除记录
		String sqlRecord1 = "delete from CustomerServiceRecord csr where csr.customerResource.customerResourceBatch.id="
				+ primaryKey + " and csr.customerResource.isCustomer=false";
		String sqlRecord2 = "delete from CustomerComplaintRecord ccr where ccr.customerResource.customerResourceBatch.id="
				+ primaryKey + " and ccr.customerResource.isCustomer=false";
		// 删除批次相应的任务
		String sqlTask = "delete from MarketingProjectTask mpt where mpt.customerResource.customerResourceBatch.id="
				+ primaryKey + " and mpt.customerResource.isCustomer=false";
		// 删除批次相应的项目的中间表
		String sqlTaskBatchMiddle = "delete from ec2_markering_project_ec2_customer_resource_batch where batches_id="+primaryKey;
		// 删除相应的地址信息
		String sqlAddress = "delete from Address a where a.customerResource.customerResourceBatch.id="
				+ primaryKey + " and a.customerResource.isCustomer=false";
		// 删除资源
		String sqlResource = "delete from CustomerResource cr where cr.customerResourceBatch.id="
				+ primaryKey + " and cr.isCustomer=false";

		getEntityManager().createQuery(sqlDescription).executeUpdate();
		getEntityManager().createQuery(sqlRecord1).executeUpdate();
		getEntityManager().createQuery(sqlRecord2).executeUpdate();
		getEntityManager().createQuery(sqlTask).executeUpdate();
		getEntityManager().createQuery(sqlAddress).executeUpdate();
		getEntityManager().createNativeQuery(sqlTaskBatchMiddle).executeUpdate();
		getEntityManager().createQuery(sqlResource).executeUpdate();

		// 删除批次
		String sqlResourceCount = "select count(cr) from CustomerResource cr where cr.customerResourceBatch.id="
				+ primaryKey;
		Long resourceCount = (Long) getEntityManager().createQuery(
				sqlResourceCount).getSingleResult();
		// 如果条数大于0，说明有客户资源，不进行批次删除
		if (resourceCount > 0) {
			return;
		} else {
			String sqlBatch = "delete from CustomerResourceBatch crb where crb.id="
					+ primaryKey;
			getEntityManager().createQuery(sqlBatch).executeUpdate();
		}
	}
	/**
	 * chb
	 * 根据域取出所有的批次
	 * @param domain
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerResourceBatch> getAllBatches(Domain domain) {
		String sql="select crb from CustomerResourceBatch crb where crb.domain.id="+domain.getId()+" order by crb.id desc";
		return getEntityManager().createQuery(sql).getResultList();
	}

	/** jrh  根据客户资源的Id号和批次Id号， 检查资源是否已经在指定的批次当中   @param resourceId 客户资源的Id号  @param batchId 批次的Id号	 */
	@Override
	public boolean checkResourceExistedInBatch(Long resourceId, Long batchId) {
		String nativeSql = "select count(rb) from ec2_customer_resource_ec2_customer_resource_batch as rb where rb.customerresources_id = "+resourceId
				+" and rb.customerresourcebatches_id = "+batchId;
		Long count = (Long) getEntityManager().createNativeQuery(nativeSql).getSingleResult();
		if(count > 0) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<CustomerResourceBatch> getByName(String batchName, Domain domain) {
		StringBuffer jpqlStrBf = new StringBuffer();
		jpqlStrBf.append("select crb from CustomerResourceBatch crb where crb.domain.id = ");
		jpqlStrBf.append(domain.getId());
		jpqlStrBf.append(" and crb.batchName = '");
		jpqlStrBf.append(batchName);
		jpqlStrBf.append("' order by crb.id asc");
		
		return this.getEntityManager().createQuery(jpqlStrBf.toString()).getResultList();
	}

}
