package com.jiangyifen.ec2.eao.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.eao.TimersEao;
import com.jiangyifen.ec2.entity.Timers;


public class TimersEaoImpl extends BaseEaoImpl implements TimersEao {
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Timers> getTiemrsToCurrentTime() {
		String now = df.format(new Date());
		return getEntityManager().createQuery("select t from Timers as t where t.responseTime = " +
				"(select min(t2.responseTime) from Timers as t2 where t2.responseTime > '" + now +"')").getResultList();
	}

	@Override
	public Date getNearestTimeToCurrentTime() {
		String now = df.format(new Date());
		Date nearestResponseTime = null;
		try {
			nearestResponseTime = (Date) getEntityManager().createQuery("select min(t.responseTime) from Timers as t where t.responseTime > '" + now +"'").getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		}
		return nearestResponseTime;
	}

}
