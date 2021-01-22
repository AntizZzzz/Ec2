package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.FileType;

/**
* Eao接口：文件类型
* 
* @author lxy
*
*/
public interface FileTypeEao extends BaseEao {
	
	
	/**
	 * 获得文件类型列表
	 * @param 	jpql jpql语句
	 * @return 	文件类型列表
	 */
	public List<FileType> loadFileTypeList(String jpql);
	
}