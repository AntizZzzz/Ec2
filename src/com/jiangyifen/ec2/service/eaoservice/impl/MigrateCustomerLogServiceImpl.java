package com.jiangyifen.ec2.service.eaoservice.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.MigrateCustomerLogEao;
import com.jiangyifen.ec2.entity.MigrateCustomerLog;
import com.jiangyifen.ec2.service.eaoservice.MigrateCustomerLogService;

public class MigrateCustomerLogServiceImpl implements MigrateCustomerLogService {
	
	private MigrateCustomerLogEao migrateCustomerLogEao;
	
	// enhanced method

	@SuppressWarnings("unchecked")
	@Override
	public List<MigrateCustomerLog> loadPageEntities(int start, int length, String sql) {
		return migrateCustomerLogEao.loadPageEntities(start, length, sql);
	}

	@Override
	public int getEntityCount(String sql) {
		return migrateCustomerLogEao.getEntityCount(sql);
	}
	
	// common method
	
	@Override
	public MigrateCustomerLog get(Object primaryKey) {
		return migrateCustomerLogEao.get(MigrateCustomerLog.class, primaryKey);
	}

	@Override
	public void save(MigrateCustomerLog migrateCustomerLog) {
		migrateCustomerLogEao.save(migrateCustomerLog);
	}

	@Override
	public MigrateCustomerLog update(MigrateCustomerLog migrateCustomerLog) {
		return (MigrateCustomerLog) migrateCustomerLogEao.update(migrateCustomerLog);
	}

	@Override
	public void delete(MigrateCustomerLog migrateCustomerLog) {
		migrateCustomerLogEao.delete(migrateCustomerLog);
	}

	@Override
	public void deleteById(Object primaryKey) {
		migrateCustomerLogEao.delete(MigrateCustomerLog.class, primaryKey);
	}

	public MigrateCustomerLogEao getMigrateCustomerLogEao() {
		return migrateCustomerLogEao;
	}

	public void setMigrateCustomerLogEao(MigrateCustomerLogEao migrateCustomerLogEao) {
		this.migrateCustomerLogEao = migrateCustomerLogEao;
	}

}
