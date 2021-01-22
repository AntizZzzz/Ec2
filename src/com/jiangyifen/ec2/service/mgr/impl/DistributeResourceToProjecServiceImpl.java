package com.jiangyifen.ec2.service.mgr.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.eao.CommonEao;
import com.jiangyifen.ec2.eao.CustomerResourceBatchEao;
import com.jiangyifen.ec2.eao.CustomerResourceEao;
import com.jiangyifen.ec2.eao.MarketingProjectEao;
import com.jiangyifen.ec2.eao.MarketingProjectTaskEao;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.mgr.DistributeResourceToProjecService;
import com.vaadin.ui.ProgressIndicator;

public class DistributeResourceToProjecServiceImpl implements
		DistributeResourceToProjecService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private MarketingProjectEao marketingProjectEao;
	private MarketingProjectTaskEao marketingProjectTaskEao;
	private CustomerResourceBatchEao customerResourceBatchEao;
	private CustomerResourceEao customerResourceEao;
	private CommonEao commonEao;

	/**
	 * chb 根据批次为项目分配资源
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void assignProjectResourceByBatch(MarketingProject marketingProject,
			Collection<CustomerResourceBatch> batches, Domain domain,ProgressIndicator pi) {
		//批次为空直接返回
		if(batches==null||batches.isEmpty()) return;
		Long domainId=domain.getId();
		// 将所有要添加到项目中的批次Id进行组合
		//处理的到按照批次Id的一个 eg：（1,2,3,4）
		StringBuilder sbBatch = new StringBuilder();
		sbBatch.append('(');
		Iterator<CustomerResourceBatch> batchIter = batches.iterator();
		while (batchIter.hasNext()) {
			CustomerResourceBatch batch = batchIter.next();
			sbBatch.append(batch.getId());
			if (batchIter.hasNext())
				sbBatch.append(",");
		}
		sbBatch.append(')');
		String inSql = sbBatch.toString();
		
		String countSql="SELECT count(*) FROM ec2_customer_resource_ec2_customer_resource_batch where  customerresourcebatches_id in "+inSql;
		Long totalLong=(Long)commonEao.excuteNativeSql(countSql, ExecuteType.SINGLE_RESULT);
		
		//记录总共要分配的任务数和任务总数
		float taskNum=0;
		float totalNum=totalLong.floatValue();
		
		// ===========以步长3000取出要分配的 customerresources_id ==================//
		Long recordId = Long.MAX_VALUE;
		int step = 3000;
		// 每次加载step步长条数据
		List<Object[]> records = null;
		for (;;) {
			// batchId 隐含 domain 概念
			String nativeSql = "SELECT customerresources_id,customerresourcebatches_id FROM ec2_customer_resource_ec2_customer_resource_batch where customerresources_id<"+recordId+" and customerresourcebatches_id in "+inSql+" order by customerresources_id desc";

			Long startTime=System.currentTimeMillis();
			records = (List<Object[]>) commonEao.loadStepRows(nativeSql,step);
			Long endTime=System.currentTimeMillis();
			logger.info("加载3000数据耗时:"+(endTime-startTime)/1000+"秒");
			
			//在SQL中不适用distinct对取最小值没有影响
			if (records.size() > 0) {
				// 取得最小的Id值
				recordId = (Long)records.get(records.size() - 1)[0];
			} else {
				break;	// 如果资源加载完，则跳出for循环
			}
			//对于取出来的资源Id去重，由于对所有取出来的项目进行分配处理，顺序没有关系的
			Set<Object[]> recordSet=new HashSet<Object[]>(records);
			//对取出的资源进行分配,此时records.size>0
			Iterator<Object[]> recordsIter = recordSet.iterator();

			Long startTime1=System.currentTimeMillis();
			while (recordsIter.hasNext()) {
				try {
					MarketingProjectTask mpt=new MarketingProjectTask();
					mpt.setIsAnswered(false);
					mpt.setIsFinished(false);
					//一同导入的几个批次的资源不能重复,所以导数据时不会出现先导入的资源重复问题
					Object[] ids = recordsIter.next();
					Long resourceId=(Long)ids[0];
					Long batchId=(Long)ids[1];
					// jrh 此处应该保证一条资源在通一个项目中最多出现一条任务
					CustomerResource customerResource = commonEao.get(CustomerResource.class, resourceId);
					marketingProjectTaskEao.checkThenAddTask(customerResource, batchId, true, marketingProject, domainId);
					
					pi.setValue((taskNum++)/(float)totalNum);
				} catch (Exception e) {
					logger.error("chb: import resource bug");
					e.printStackTrace();
				}
			}
			Long endTime1=System.currentTimeMillis();
			logger.info("存储3000耗时:"+(endTime1-startTime1)/1000+"秒");
			
		}
	}

	// getter and setter
	public MarketingProjectEao getMarketingProjectEao() {
		return marketingProjectEao;
	}

	public void setMarketingProjectEao(MarketingProjectEao marketingProjectEao) {
		this.marketingProjectEao = marketingProjectEao;
	}

	public MarketingProjectTaskEao getMarketingProjectTaskEao() {
		return marketingProjectTaskEao;
	}

	public void setMarketingProjectTaskEao(
			MarketingProjectTaskEao marketingProjectTaskEao) {
		this.marketingProjectTaskEao = marketingProjectTaskEao;
	}

	public CustomerResourceBatchEao getCustomerResourceBatchEao() {
		return customerResourceBatchEao;
	}

	public void setCustomerResourceBatchEao(
			CustomerResourceBatchEao customerResourceBatchEao) {
		this.customerResourceBatchEao = customerResourceBatchEao;
	}

	public CustomerResourceEao getCustomerResourceEao() {
		return customerResourceEao;
	}

	public void setCustomerResourceEao(CustomerResourceEao customerResourceEao) {
		this.customerResourceEao = customerResourceEao;
	}

	public CommonEao getCommonEao() {
		return commonEao;
	}

	public void setCommonEao(CommonEao commonEao) {
		this.commonEao = commonEao;
	}

}
