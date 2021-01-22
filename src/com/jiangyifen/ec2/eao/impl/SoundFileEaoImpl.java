package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.SoundFileEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SoundFile;

public class SoundFileEaoImpl extends BaseEaoImpl implements SoundFileEao {
	@SuppressWarnings("unchecked")
	@Override
	public List<SoundFile> getAll(Domain domain) {
		String sql="select sf from SoundFile as sf where sf.domain.id="+domain.getId();
		return getEntityManager().createQuery(sql).getResultList();
	}

	@Override
	public boolean checkDeleteAbleByIVR(Long soundFileId, Long domainId) {
		/********  先检验IVRAction，因为这个可能性比较大; 而分开检验IVRAction和IVRMenu，是因为只要有一种于SoundFile关联，就应该返回 false **********/
		// 统计语音所关联的IVRAction 的个数
		String nativeActionSql = "select count(*) from ec2_ivr_action where domain_id = "+domainId+" and soundfile_id = "+soundFileId;
		Long conActionCount = (Long) this.getEntityManager().createNativeQuery(nativeActionSql).getSingleResult();

		if(conActionCount > 0) {
			return false;
		}
		
		// 统计语音所关联的IVRMenu 的个数
		String nativeMenuSql = "select count(*) from ec2_ivr_menu where domain_id = "+domainId
				+" and (welcomesoundfile_id = "+soundFileId+" or closesoundfile_id = "+soundFileId+")";
		Long conMenuCount = (Long) this.getEntityManager().createNativeQuery(nativeMenuSql).getSingleResult();

		if(conMenuCount > 0) {
			return false;
		}
		
		return true;
	}
	
}
