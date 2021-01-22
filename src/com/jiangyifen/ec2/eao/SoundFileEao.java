package com.jiangyifen.ec2.eao;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SoundFile;


public interface SoundFileEao extends BaseEao {
	/**
	 * 取得所有的SoundFile
	 * @param domain
	 * @return
	 */
	public List<SoundFile> getAll(Domain domain);

	/**
	 * jrh 在指定域下，根据语音文件的编号，检查这个语音文件是否存在关联的IVR对象，如IVRMenu、IVRAction
	 * 	
	 * @param soundFileId	待检验的语音对象的编号
	 * @param domainId		指定租户所属域的编号
	 * @return boolean		返回是否可以删除
	 */
	public boolean checkDeleteAbleByIVR(Long soundFileId, Long domainId);
}
