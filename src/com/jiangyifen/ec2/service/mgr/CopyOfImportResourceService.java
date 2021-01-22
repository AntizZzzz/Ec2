package com.jiangyifen.ec2.service.mgr;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import jxl.Sheet;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.User;
import com.vaadin.ui.ProgressIndicator;

public interface CopyOfImportResourceService{
	/**
	 * chb
	 * 导数据的方法
	 * @param file
	 * @param batch
	 * @return 返回值为导入的条数，如果返回null表示导入失败
	 * @throws Exception 
	 * 此方法不应该有事务
	 */
	public Map<String, Long> importData(File file,CustomerResourceBatch batch,User user,Boolean isAppend,ProgressIndicator pi, boolean ignoreReduplicate);

	/**
	 * chb 为导数据提供服务
	 * 
	 * @param sheet
	 * @param headers
	 * @param row
	 * @param isAppend
	 * @param batch
	 * @param user
	 * @param ignoreReduplicate 	是否去重 true ： 不去重
	 * @return
	 */
	@Transactional
	public CustomerResource persistOneRow(Sheet sheet,
			HashMap<Integer, String> headers, int row,Boolean isAppend,CustomerResourceBatch batch,User user, boolean ignoreReduplicate);
	/**
	 * chb 为导数据提供服务
	 * @param batch
	 * @return
	 */
	@Transactional
	public CustomerResourceBatch validatePersistNewBatch(
			CustomerResourceBatch batch);
	/**
	 * chb 为导数据提供服务
	 * @param batch
	 * @param customerResource
	 */
	@Transactional
	public void persistToTask(CustomerResourceBatch batch,CustomerResource customerResource);
	
	@Transactional
	public void deleteBatch();
}
