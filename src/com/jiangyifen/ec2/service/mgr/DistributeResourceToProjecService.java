package com.jiangyifen.ec2.service.mgr;

import java.util.Collection;

import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.vaadin.ui.ProgressIndicator;
public interface DistributeResourceToProjecService{
	/**
	 * chb 根据批次为项目分配资源
	 * @param marketingProject
	 * @param batches
	 * @param domain
	 */
//	@Transactional chb 因为只是去数据，所以此处删除事物，避免事物嵌套
	public void assignProjectResourceByBatch(MarketingProject marketingProject,
			Collection<CustomerResourceBatch> batches, Domain domain,ProgressIndicator pi);
}
