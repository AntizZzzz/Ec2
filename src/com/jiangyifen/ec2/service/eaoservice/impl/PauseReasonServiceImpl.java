package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.PauseReasonEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.PauseReason;
import com.jiangyifen.ec2.service.eaoservice.PauseReasonService;

public class PauseReasonServiceImpl implements PauseReasonService {
	
	private PauseReasonEao pauseReasonEao;
	
	// ========  enhanced method  ========//
	
	public List<PauseReason> getAllByEnabled(Domain domain, boolean enabled) {
		return pauseReasonEao.getAllByEnabled(domain, enabled);
	}

	//========  common method ========//
	
	@Override
	public PauseReason get(Object primaryKey) {
		return pauseReasonEao.get(PauseReason.class, primaryKey);
	}

	@Override
	public void save(PauseReason pauseReason) {
		pauseReasonEao.save(pauseReason);
	}

	@Override
	public void update(PauseReason pauseReason) {
		pauseReasonEao.update(pauseReason);
	}

	@Override
	public void delete(PauseReason pauseReason) {
		pauseReasonEao.delete(pauseReason);
	}

	@Override
	public void deleteById(Object primaryKey) {
		pauseReasonEao.delete(PauseReason.class, primaryKey);
	}

	
	//==========  getter setter  ==========//
	
	public PauseReasonEao getPauseReasonEao() {
		return pauseReasonEao;
	}
	
	public void setPauseReasonEao(PauseReasonEao pauseReasonEao) {
		this.pauseReasonEao = pauseReasonEao;
	}
	
	
	

}
