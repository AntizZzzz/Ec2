package com.jiangyifen.ec2.service.eaoservice.impl;

import com.jiangyifen.ec2.eao.IVRLogEao;
import com.jiangyifen.ec2.entity.IVRLog;
import com.jiangyifen.ec2.service.eaoservice.IVRLogService;

public class IVRLogServiceImpl implements IVRLogService {
	
	private IVRLogEao ivrLogEao;

	// common method
	
	@Override
	public IVRLog get(Object primaryKey) {
		return ivrLogEao.get(IVRLog.class, primaryKey);
	}

	@Override
	public void save(IVRLog ivrLog) {
		ivrLogEao.save(ivrLog);
	}


	@Override
	public void delete(IVRLog ivrLog) {
		ivrLogEao.delete(ivrLog);
	}

	@Override
	public void update(IVRLog ivrlog) {
		ivrLogEao.update(ivrlog);
		
	}
	public void deleteById(Object primaryKey) {
		ivrLogEao.delete(IVRLog.class, primaryKey);
	}


	//getter and setter
	public IVRLogEao getIvrLogEao() {
		return ivrLogEao;
	}

	public void setIvrLogEao(IVRLogEao ivrLogEao) {
		this.ivrLogEao = ivrLogEao;
	}
	
}
