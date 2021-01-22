package com.jiangyifen.ec2.service.eaoservice;

import org.springframework.transaction.annotation.Transactional;

import com.jiangyifen.ec2.entity.FileType;
import com.jiangyifen.ec2.service.common.FlipSupportService;

/**
* Service接口：文件类型
* 
* @author lxy
*
*/
public interface FileTypeService extends FlipSupportService<FileType>  {
	
	/**
	 * 根据主键ID获得文件类型
	 * @param id	主键ID 
	 * @return		文件类型，一条或null
	 */
	@Transactional
	public FileType getFileTypeById(Long id);
	
	/**
	 * 保存文件类型
	 * @param fileType	文件类型
	 * 
	 */
	@Transactional
	public void saveFileType(FileType fileType);
	
	/**
	 * 更新文件类型
	 * @param fileType	文件类型
	 * 
	 */
	@Transactional
	public void updateFileType(FileType fileType);
	
	/**
	 * 删除文件类型
	 * @param fileType	文件类型
	 * 
	 */
	@Transactional
	public void deleteFileType(FileType fileType);
	
	/**
	 * 校验文件是类型是否合法
	 * @param fileName
	 * @return
	 */
	@Transactional
	public boolean validateFileType(String fileDotName);
	
}