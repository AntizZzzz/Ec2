package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.SoundFileEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.SoundFile;
import com.jiangyifen.ec2.service.eaoservice.SoundFileService;

public class SoundFileServiceImpl implements SoundFileService {
	private SoundFileEao soundFileEao;
	
	//========  common method ========//
	@Override
	public SoundFile get(Object primaryKey) {
		return soundFileEao.get(SoundFile.class, primaryKey);
	}

	@Override
	public void save(SoundFile soundFile) {
		soundFileEao.save(soundFile);
	}

	@Override
	public void update(SoundFile soundFile) {
		soundFileEao.update(soundFile);
	}

	@Override
	public void delete(SoundFile soundFile) {
		soundFileEao.delete(soundFile);
	}

	@Override
	public void deleteById(Object primaryKey) {
		soundFileEao.delete(SoundFile.class, primaryKey);
	}

	/**
	 * 取得所有的SoundFile
	 */
	@Override
	public List<SoundFile> getAll(Domain domain) {
		return soundFileEao.getAll(domain);
	}
	
	@Override
	public boolean checkDeleteAbleByIVR(Long soundFileId, Long domainId) {
		return soundFileEao.checkDeleteAbleByIVR(soundFileId, domainId);
	}

	//flip method
	@Override
	public int getEntityCount(String sql) {
		return soundFileEao.getEntityCount(sql);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<SoundFile> loadPageEntities(int startIndex, int pageRecords,
			String selectSql) {
		return soundFileEao.loadPageEntities(startIndex, pageRecords, selectSql);
	}
	//==========  getter setter  ==========//
	
	public SoundFileEao getSoundFileEao() {
		return soundFileEao;
	}

	public void setSoundFileEao(SoundFileEao soundFileEao) {
		this.soundFileEao = soundFileEao;
	}

	
}
