package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.KbInfo;

/**
* Eao接口：知识库管理
* 
* @author lxy
*
*/
public interface KbInfoEao extends BaseEao {
	
	
	/**
	 * 获得知识库列表
	 * @param 	jpql jpql语句
	 * @return 	知识库列表
	 */
	public List<KbInfo> loadKbInfoList(String jpql);
	
}