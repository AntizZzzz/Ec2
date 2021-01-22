package com.jiangyifen.ec2.service.mgr;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.vaadin.ui.ProgressIndicator;

public interface ImportResourceService{
	
	/**
	 * chb
	 * 导数据的方法
	 * @param file
	 * @param batch
	 * @return 返回值为导入的条数，如果返回null表示导入失败
	 * @throws Exception 
	 * 此方法不应该有事务
	 */
	public Map<String, Long> importData(File file,CustomerResourceBatch batch,User user,Boolean isAppend,ProgressIndicator pi,Boolean quChong);

	/**
	 * chb 为导数据提供服务
	 * 
	 * @param sheet
	 * @param headers
	 * @param row
	 * @param isAppend
	 * @param batch
	 * @param user
	 * @param resultMap  用来存储导入的结果，如号码存在几个（是客户忽略，已存在资源更新），导入成功几条
	 * @return
	 */
	@Transactional
	public List<CustomerResource> persistOneRow(jxl.Sheet sheet,
			HashMap<Integer, String> headers, int row,Boolean isAppend,CustomerResourceBatch batch,User user, Map<String, Long> resultMap,Boolean quChong);

	/**
	 * chb 为导数据提供服务
	 * 
	 * @param sheet
	 * @param headers
	 * @param row
	 * @param isAppend
	 * @param batch
	 * @param user
	 * @param resultMap  用来存储导入的结果，如号码存在几个（是客户忽略，已存在资源更新），导入成功几条
	 * @return
	 */
	@Transactional
	public List<CustomerResource> persistOneRow(org.apache.poi.ss.usermodel.Sheet sheet,
			HashMap<Integer, String> headers, int row,Boolean isAppend,CustomerResourceBatch batch,User user, Map<String, Long> resultMap,Boolean quChong);
	//存储Csv文件
	@Transactional
	public List<CustomerResource> persistOneCsvRowToCustomerResourceList(String [] nextLine,
			HashMap<Integer, String> headers, int row,Boolean isAppend,CustomerResourceBatch batch,User user, Map<String, Long> resultMap,Boolean quChong);
	/**
	 * chb 为导数据提供服务
	 * @param batch
	 * @return
	 */
	@Transactional
	public CustomerResourceBatch validatePersistNewBatch(CustomerResourceBatch batch);
	
	/**
	 * chb 为导数据提供服务
	 * @param batch
	 * @param customerResource
	 */
	@Transactional
	public void persistToTask(CustomerResourceBatch batch,CustomerResource customerResource, Domain domain);
	
	@Transactional
	public void deleteBatch(CustomerResourceBatch batch);

	/**
	 * @Description 描述：导入单条资源到指定批次中去，如果该批次已经被项目使用了，则自动给该项目创建人任务
	 * 
	 * @param isAppend			是否为追加资源
	 * @param resourceBatch		批次
	 * @param customer			客户资源
	 * @param descriptionMap	描述信息集合
	 * @param phonenum			电话号码
	 * @param companyName		公司名称
	 * @param addressInfo			地址信息
	 * @return String			返回信息
	 *
	 * @author  JRH
	 * @date    2014年12月9日 下午2:36:01
	 */
	@Transactional
	public String importOneCustomer(boolean isAppend, CustomerResourceBatch resourceBatch, CustomerResource customer, 
			HashMap<String, String> descriptionMap, String phonenum, String companyName, String addressInfo);
	
	/**
	 * @Description 描述：导入单挑资源到指定批次中去
	 *
	 * @param isAppend			是否为追加资源
	 * @param resourceBatch		批次
	 * @param customer			客户资源
	 * @param descriptionMap	描述信息集合
	 * @param telephonesList	电话号码
	 * @param addressesList		地址信息
	 * @param companyName		公司名称
	 * @return
	 * 
	 * @author jinht
	 * @date	2015年6月24日 13:09:16
	 */
	@Transactional
	public String importOneCustomer(boolean isAppend, CustomerResourceBatch resourceBatch, CustomerResource customer, 
			HashMap<String, String> descriptionMap, List<Telephone> telephonesList, List<Address> addressesList, String companyName);
	
}
