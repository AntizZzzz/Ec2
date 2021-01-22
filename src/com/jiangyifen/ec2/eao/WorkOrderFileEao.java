package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.WorkOrderFile;

/**
* Eao接口：工单附件管理
* 
* @author lxy
*
*/
public interface WorkOrderFileEao extends BaseEao {
	
	
	/**
	 * 获得工单附件列表
	 * @param 	jpql jpql语句
	 * @return 	工单附件列表
	 */
	public List<WorkOrderFile> loadWorkOrderFileList(String jpql);
	
}