package com.jiangyifen.ec2.eao.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.MusicOnHoldEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MusicOnHold;

public class MusicOnHoldEaoImpl extends BaseEaoImpl implements MusicOnHoldEao {
	
	@Override
	public void save(MusicOnHold musicOnHold) {
		getEntityManager().persist(musicOnHold);
	}

	@Override
	public MusicOnHold update(MusicOnHold musicOnHold) {
		return getEntityManager().merge(musicOnHold);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean existByDirectory(String directory, Domain domain) {
		List<MusicOnHold> mohs = getEntityManager().createQuery("select moh from MusicOnHold as moh where moh.directory = '" +directory+ "' and moh.domain.id = " +domain.getId()).getResultList();
		if(mohs.size() == 0) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MusicOnHold> getAllByDomain(Domain domain) {
		List<MusicOnHold> mohs = getEntityManager().createQuery("select moh from MusicOnHold as moh where moh.domain.id = " +domain.getId()).getResultList();
		return mohs;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Long getMaxMusicOnHoldName() {
		List<MusicOnHold> mohs = getEntityManager().createQuery("select moh from MusicOnHold as moh where moh.name = " 
				+ "(select max(moh2.name) from MusicOnHold as moh2 where moh2.name != 'default')").getResultList();
		if(mohs.size() > 0) {
			return Long.parseLong(mohs.get(0).getName());
		}
		return 500000L;
	}
}
