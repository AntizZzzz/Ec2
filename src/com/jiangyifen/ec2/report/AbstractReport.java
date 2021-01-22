package com.jiangyifen.ec2.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.service.mgr.ReportService;

import com.jiangyifen.ec2.utils.SpringContextHolder;

public abstract class AbstractReport implements Report {
	private ReportService reportService = SpringContextHolder
			.getBean("reportService");

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public Object getOneRecord(String sql) {
		if (reportService.getOneRecord(sql).size() == 0) {
			return null;
		} else {
			return reportService.getOneRecord(sql).get(0);
		}
	}

	// 获得中间表数据
	public List<Object[]> getRecord(String sql) {

		return reportService.getMoreRecord(sql);
	}

	public List<Object> getObjects(String sql) {

		return reportService.getObjects(sql);
	}

	public void save(Object object) {
		reportService.save(object);
	}

	public void delete(String sql) {
		reportService.delete(sql);
	}

	public int update(String sql) {
		return reportService.executeUpdate(sql);
	}

	public Object getSingleRecord(String sql) {

		return reportService.getSingleRecord(sql);
	}

	// 得到日期和小时（24小时进制），如 yyyy-MM-dd HH

	public Date getDateHour(String sql) {
		try {

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");

			List<Object[]> list = reportService.getMoreRecord(sql);

			if (list.size() == 0) {
				return null;
			}

			Object[] objects = list.get(0);

			return format.parse(objects[0].toString() + " " + objects[1]);

		} catch (ParseException e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
