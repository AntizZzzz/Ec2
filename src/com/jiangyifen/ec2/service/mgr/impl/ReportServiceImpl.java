package com.jiangyifen.ec2.service.mgr.impl;

import java.util.List;

import com.jiangyifen.ec2.eao.ReportEao;
import com.jiangyifen.ec2.service.mgr.ReportService;

/**
 * @author chb
 */

public class ReportServiceImpl implements ReportService {
	private ReportEao reportEao;

	/**
	 * 取得多条记录,根据Sql语句进行控制返回结果
	 * 
	 * @param sql
	 * @return
	 */

	// =========== enhanced method ===========//

	@Override
	public List<Object[]> getMoreRecord(String sql) {

		return reportEao.getMoreRecord(sql);
	}

	public List<Object[]> getRecords(String sql) {
		return reportEao.getRecords(sql);
	}

	/**
	 * 取得一条记录,返回为Object,根据Sql语句进行控制返回结果
	 * 
	 * @param sql
	 * @return
	 */
	@Override
	public List<Object> getOneRecord(String sql) {
		return reportEao.getOneRecord(sql);
	}

	@Override
	public Object getSingleRecord(String sql) {
		return reportEao.getSingleRecord(sql);
	}

	@Override
	public List<Object> getObjects(String sql) {

		return reportEao.getObjects(sql);
	}

	public void delete(String sql) {
		reportEao.delete(sql);
	}

	/**
	 * 根据Sql去执行更新或者删除操作
	 * 
	 * @param sql
	 */
	@Override
	public List<Object> getAll(String jpql) {
		return reportEao.getAll(jpql);
	}

	// =========== common method ===========//

	@Override
	public int executeUpdate(String sql) {
		return reportEao.executeUpdate(sql);
	}

	/**
	 * 执行保存操作
	 * 
	 * @param object
	 */
	@Override
	public void save(Object object) {
		reportEao.save(object);
	}

	@Override
	public <T> T get(Class<T> entityClass, Object pk) {
		return reportEao.get(entityClass, pk);
	}

	@Override
	public void update(Object entity) {
		reportEao.update(entity);
	}

	@Override
	public <T> void delete(Class<T> entityClass, Object pk) {
		reportEao.delete(entityClass, pk);
	}

	@Override
	public void delete(Object entity) {
		reportEao.delete(entity);
	}
	
	// =========== getter setter ===========//

	// Getter and Setter
	public ReportEao getReportEao() {
		return reportEao;
	}

	public void setReportEao(ReportEao reportEao) {
		this.reportEao = reportEao;
	}

}
