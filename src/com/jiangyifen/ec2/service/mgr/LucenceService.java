package com.jiangyifen.ec2.service.mgr;

import java.util.List;

import com.jiangyifen.ec2.entity.Knowledge;

public interface LucenceService {
	/**
	 * 根据数据库knowledge实体生成文件，适用于数据库域实体少于3000
	 * @param domainId
	 * @param knowledgeList
	 * @param isDeleteOld
	 * @return
	 */
	public Boolean generateFile(Long domainId, List<Knowledge> knowledgeList,Boolean isDeleteOld) ;
	/**
	 * 为文件创建索引
	 * @param filePath
	 * @param toPath
	 * @return
	 */
	public Boolean indexFile(String filePath, String toPath) ;
	/**
	 * 根据关键词在指定文件夹执行搜索操作
	 * @param indexDir
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	public List<String> search(String indexDir, String keyword) throws Exception;
}
