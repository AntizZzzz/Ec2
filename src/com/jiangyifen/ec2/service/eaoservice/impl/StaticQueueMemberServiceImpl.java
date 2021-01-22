package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.StaticQueueMemberEao;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;

public class StaticQueueMemberServiceImpl implements StaticQueueMemberService {
	
	private StaticQueueMemberEao staticQueueMemberEao;
	
	// ========  enhanced method  ========//
	
	@Override
	public List<StaticQueueMember> getAllByDomain(Domain domain) {
		return staticQueueMemberEao.getAll();
	}
	
	@Override
	public List<StaticQueueMember> getAllBySipname(Domain domain, String sipname) {
		return staticQueueMemberEao.getAllBySipname(domain, sipname);
	}
	
	/**
	 * 获取指定域中指定队列名的所有分机和队列的对应关系
	 * @param domain  	域
	 * @param queueName	队列名
	 * @return
	 */
	public List<StaticQueueMember> getAllByQueueName(Domain domain, String queueName) {
		return staticQueueMemberEao.getAllByQueueName(domain.getId(), queueName);
	}

	public List<StaticQueueMember> getAllByQueueName(Long domainId, String queueName) {
		return staticQueueMemberEao.getAllByQueueName(domainId, queueName);
	}
	
	//========  common method ========//
	@Override
	public StaticQueueMember get(Object primaryKey) {
		return staticQueueMemberEao.get(StaticQueueMember.class, primaryKey);
	}

	@Override
	public void save(StaticQueueMember queueMember) {
		staticQueueMemberEao.save(queueMember);
	}

	@Override
	public StaticQueueMember update(StaticQueueMember queueMember) {
		return (StaticQueueMember) staticQueueMemberEao.update(queueMember);
	}

	@Override
	public void delete(StaticQueueMember queueMember) {
		staticQueueMemberEao.delete(queueMember);
	}

	@Override
	public void deleteById(Object primaryKey) {
		staticQueueMemberEao.delete(StaticQueueMember.class, primaryKey);
	}

	
	//==========  getter setter  ==========//
	
	public StaticQueueMemberEao getStaticQueueMemberEao() {
		return staticQueueMemberEao;
	}
	
	public void setStaticQueueMemberEao(StaticQueueMemberEao staticQueueMemberEao) {
		this.staticQueueMemberEao = staticQueueMemberEao;
	}
	
}
