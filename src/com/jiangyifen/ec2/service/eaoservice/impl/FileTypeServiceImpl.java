package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.FileTypeEao;
import com.jiangyifen.ec2.entity.FileType;

import com.jiangyifen.ec2.service.eaoservice.FileTypeService;
/**
* Service实现类：文件类型
* 
* @author lxy
* 
*/
public class FileTypeServiceImpl implements FileTypeService {

	
	/** 需要注入的Eao */
	private FileTypeEao fileTypeEao; 
	 
	// 根据jpql语句和分页参数实现分页查询
	@Override
	@SuppressWarnings("unchecked")
	public List<FileType> loadPageEntities(int start, int length, String sql) {
		return fileTypeEao.loadPageEntities(start, length, sql);
	}

	// 根据jpql语句实现查询总数与分页查询联合使用
	@Override
	public int getEntityCount(String sql) {
		return fileTypeEao.getEntityCount(sql);
	}
		
	// 根据主键ID获得文件类型
	@Override
	public FileType getFileTypeById(Long id){
		return fileTypeEao.get(FileType.class, id);
	}
	
	// 保存文件类型
	@Override
	public void saveFileType(FileType fileType){
		fileTypeEao.save(fileType);
	}
	
	// 更新文件类型
	@Override
	public void updateFileType(FileType fileType){
		fileTypeEao.update(fileType);
	}
	
	// 删除文件类型
	@Override
	public void deleteFileType(FileType fileType){
		fileTypeEao.delete(fileType);
	}
	
	/** 校验文件是类型是否合法*/
	@Override
	public boolean validateFileType(String fileDotName) {
		boolean vali = false;
		String jpql = "select count(s) from FileType as s where s.dotName  = '" + fileDotName+"'";
	 	int count = this.getEntityCount(jpql);
	 	if(count >0 ){
	 		 vali = true;
	 	}else{
	 		 vali = false;
	 	}
	 	return  vali;
	}
	
	/**
	*
	*	获得总数
	*
	*	String jpql = "select count(s) from FileType as s where s.  = " +id;
	 	return this.getEntityCount(jpql);
	*
	*/
	
	/**
	*
	*	获得列表
	*
	*	String jpql = "select s from FileType as s where s. = "++" order by s.ordernumber asc ";
		List<FileType> list = fileTypeEao.loadFileTypeList(jpql);
		return list;
	*
	*/
	
	
	
	
	//Eao注入
	public FileTypeEao getFileTypeEao() {
		return fileTypeEao;
	}
	
	//Eao注入
	public void setFileTypeEao(FileTypeEao fileTypeEao) {
		this.fileTypeEao = fileTypeEao;
	}

}