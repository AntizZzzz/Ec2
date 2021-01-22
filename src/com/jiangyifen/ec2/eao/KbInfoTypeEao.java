package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.KbInfoType;

/**
* Eao接口：知识类型管理
* 
* @author lxy
*
*/
public interface KbInfoTypeEao extends BaseEao {
	
	
	/**
	 * 获得知识类型列表
	 * @param 	jpql jpql语句
	 * @return 	知识类型列表
	 */
	public List<KbInfoType> loadKbInfoTypeList(String jpql);
	
}