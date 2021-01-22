package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.OutlinePoolOutlineLinkEao;
import com.jiangyifen.ec2.entity.OutlinePoolOutlineLink;
import com.jiangyifen.ec2.service.eaoservice.OutlinePoolOutlineLinkService;

public class OutlinePoolOutlineLinkServiceImpl implements OutlinePoolOutlineLinkService {

	private OutlinePoolOutlineLinkEao outlinePoolOutlineLinkEao;
	
	@Override
	public void save(OutlinePoolOutlineLink outlinePoolOutlineLink) {
		outlinePoolOutlineLinkEao.save(outlinePoolOutlineLink);
		
	}

	@Override
	public List<OutlinePoolOutlineLink> getAllByPoolId(Long poolId) {
		// TODO Auto-generated method stub
		return outlinePoolOutlineLinkEao.getAllByDomain(poolId);
	}

	@Override
	public void delete(Long poolId) {
		// TODO Auto-generated method stub
		outlinePoolOutlineLinkEao.delete(poolId);
	}

	public OutlinePoolOutlineLinkEao getOutlinePoolOutlineLinkEao() {
		return outlinePoolOutlineLinkEao;
	}

	public void setOutlinePoolOutlineLinkEao(OutlinePoolOutlineLinkEao outlinePoolOutlineLinkEao) {
		this.outlinePoolOutlineLinkEao = outlinePoolOutlineLinkEao;
	}

}
