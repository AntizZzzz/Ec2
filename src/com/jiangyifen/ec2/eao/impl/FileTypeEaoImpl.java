package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.FileTypeEao;
import com.jiangyifen.ec2.entity.FileType;

/**
* Eao实现类：文件类型
* 
* 注入：BaseEaoImpl
* 
* @author lxy
*
*/
public class FileTypeEaoImpl extends BaseEaoImpl implements FileTypeEao {
	
	@Override
	@SuppressWarnings("unchecked")
	//获得文件类型列表
	public List<FileType> loadFileTypeList(String jpql) {
		return  this.getEntityManager().createQuery(jpql).getResultList();
	}
}