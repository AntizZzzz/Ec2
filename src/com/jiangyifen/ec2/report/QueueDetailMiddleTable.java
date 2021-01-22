package com.jiangyifen.ec2.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.report.entity.QueueDetail;
import com.jiangyifen.ec2.utils.DateTransformFactory;

public class QueueDetailMiddleTable extends AbstractReport {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 执行sql得到多条记录
	 */
	private List<Object[]> list;

	/**
	 * 要执行的sql语句
	 */
	private String sql;
	/**
	 * 中间表 ec2_report_queue_detail 中的最大日期 （yyyy-MM-dd HH）
	 */
	private Date queueDetailkMaxDate;
	/**
	 * ec2_queue_request_detail 中 intodate 的最小值 （yyyy-MM-dd HH）
	 */
	private Date minDate;
	/**
	 * ec2_queue_request_detail 中 intodate 的最大值 （yyyy-MM-dd HH）
	 */
	private Date maxDate;
	/**
	 * 年-月-日
	 */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * 小时（24小时进制）
	 */
	private SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
	/**
	 * 年-月-日 时（24小时进制）
	 */
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
	/**
	 * 执行sql时的返回值
	 */
	private Object object;
	/**
	 * key 队列名 value QueueDetail对应的实例
	 */
	private Map<String, QueueDetail> queueDetailMap = new HashMap<String, QueueDetail>();
	/**
	 * 日期转换工具类
	 */
	private DateTransformFactory dtf = new DateTransformFactory();
	
	public QueueDetailMiddleTable() {

	}

	// 生成中间表
	public void execute() {
		try {

			// 获得中间表中最大的日期
			queueDetailkMaxDate = this.getDateHour("select date,max(hour) from ec2_report_queue_detail where date = (select max(date) from ec2_report_queue_detail) group by date");

			// 获得原始表中最小的日期，精确到时
			/*object = this.getSingleRecord("select min(to_char(intodate,'yyyy-MM-dd hh24')) from ec2_queue_request_detail ");*/
			object = this.getSingleRecord("select min(intodate) from ec2_queue_request_detail ");

			if (object == null) {
				minDate = null;
			} else {
				minDate = format.parse(object.toString());
			}

			// 获得原始表中最大的日期，精确到时
			/*object = this.getSingleRecord("select max(to_char(intodate,'yyyy-MM-dd hh24')) from ec2_queue_request_detail ");*/
			object = this.getSingleRecord("select max(intodate) from ec2_queue_request_detail ");

			if (object == null) {
				maxDate = null;
			} else {
				maxDate = format.parse(object.toString());
			}

			if (queueDetailkMaxDate == null) {

				if (minDate != null && maxDate != null) {

					if (minDate.toString().equals(maxDate.toString())) {

						saveQueueNameAndDate(minDate);

					} else {
						createDate(minDate, maxDate);
					}

				}

			} else {

				// 删除中间表中最大日期的数据
				this.delete("delete from ec2_report_call_check where date = "
						+ "'" + dateFormat.format(queueDetailkMaxDate) + "'"
						+ " and hour = " + "'"
						+ hourFormat.format(queueDetailkMaxDate) + "'");

				if (queueDetailkMaxDate.toString().equals(maxDate.toString())) {

					saveQueueNameAndDate(queueDetailkMaxDate);

				} else {
					createDate(queueDetailkMaxDate, maxDate);
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		}
	}

	// 生成从startDate到endDate时间段内每小时的日期（精确到时）
	public void createDate(Date startDate, Date endDate) {

		Calendar calendar = Calendar.getInstance();
		Date date = startDate;// 初始值等于startDate

		// 如果startDate和endDate相等，则循环结束
		while (!startDate.equals(endDate)) {

			try {
				startDate = date;

				saveQueueNameAndDate(date);

				calendar.setTime(startDate);
				calendar.add(Calendar.HOUR_OF_DAY, 1);
				date = calendar.getTime();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}

		}

	}

	/**
	 * 存 队列名 date hour
	 * 
	 * @param date
	 * @throws ParseException
	 */
	public void saveQueueNameAndDate(Date date) throws ParseException {

		sql = "select domainid,queuename from ec2_queue_request_detail where to_char(intodate,'yyyy-MM-dd hh24') = "
				+ "'"
				+ format.format(date)
				+ "'"
				+ " group by domainid,queuename";

		// 清空map
		queueDetailMap.clear();

		list = this.getRecord(sql);

		for (Object[] objects : list) {

			QueueDetail detail = getQueueDetailEntity(dateFormat.format(date),
					hourFormat.format(date), objects[0], objects[1], null,
					null, null, null);

			queueDetailMap.put(objects[0].toString() + objects[1].toString(),
					detail);

		}

		insertData(date);

	}

	/**
	 * @param date
	 *            格式: yyyy-MM-dd hh24
	 */
	public void insertData(Date date) {
		try {

			// 队列进入次数
			queue_into_count(date);

			// 队列放弃次数
			queue_abandon_count(date);

			// 接通等待时长
			connect_wait_time_length(date);

			// 未接通等待时长
			unconnect_wait_time_length(date);

			insertDB();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 将map中的entity存入到数据库中
	public void insertDB() {

		insert(queueDetailMap);

	}

	public void insert(Map<String, QueueDetail> map) {

		Iterator<Entry<String, QueueDetail>> iterator = map.entrySet()
				.iterator();

		while (iterator.hasNext()) {

			Entry<String, QueueDetail> entry = iterator.next();

			QueueDetail detail = entry.getValue();

			// 存入数据库
			this.save(detail);
		}
	}

	/**
	 * 队列进入次数
	 * 
	 * @param date
	 *            格式:yyyy-MM-dd hh24
	 */
	public void queue_into_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select domainid,queuename,count(*) from ec2_queue_request_detail ");
			sbQuery.append("where intodate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and intodate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by domainid,queuename");
			/*sql = "select domainid,queuename,count(*) from ec2_queue_request_detail "
					+ "where to_char(intodate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by domainid,queuename";*/
			sql = sbQuery.toString();
			// 获得每小时的记录
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				// domainid + queueName 唯一
				String key = listObjects[0].toString()
						+ listObjects[1].toString();

				if (queueDetailMap.containsKey(key)) {

					// 设置队列进入次数
					queueDetailMap.get(key).setInto_queue_count(
							Integer.parseInt(listObjects[2].toString()));

				} else {

					throw new RuntimeException("设置队列进入次数时出错");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 队列放弃次数
	 * 
	 * @param date
	 *            格式：yyyy-MM-dd hh24
	 */
	public void queue_abandon_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select domainid,queuename,count(*) from ec2_queue_request_detail ");
			sbQuery.append("where isanswered = 'false' and intodate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and intodate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by domainid,queuename");
			/*sql = "select domainid,queuename,count(*) from ec2_queue_request_detail "
					+ "where isanswered = 'false' and to_char(intodate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by domainid,queuename";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[0].toString()
						+ listObjects[1].toString();

				if (queueDetailMap.containsKey(key)) {

					// 设置队列放弃次数
					queueDetailMap.get(key).setAbandon_queue_count(
							Integer.parseInt(listObjects[2].toString()));

				} else {

					throw new RuntimeException("设置队列放弃次数时出错");
				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 接通等待时长
	 * 
	 * @param date
	 */
	public void connect_wait_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select domainid,queuename,cast(sum(extract(epoch from outdate)-extract(epoch from intodate)) as bigint)  from ec2_queue_request_detail ");
			sbQuery.append("where isanswered = 'true' and intodate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and intodate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by domainid,queuename");
			/*sql = "select domainid,queuename,cast(sum(extract(epoch from outdate)-extract(epoch from intodate)) as bigint)  from ec2_queue_request_detail "
					+ "where isanswered = 'true' and to_char(intodate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by domainid,queuename";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[0].toString()
						+ listObjects[1].toString();

				if (queueDetailMap.containsKey(key)) {

					// 接通等待时长
					queueDetailMap.get(key).setConnect_wait_time_length(
							Integer.parseInt(listObjects[2].toString()));

				} else {

					throw new RuntimeException("设置接通等待时长时出错");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 未接通等待时长
	 * 
	 * @param date
	 */
	public void unconnect_wait_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select domainid,queuename,cast(sum(extract(epoch from outdate)-extract(epoch from intodate)) as bigint)  from ec2_queue_request_detail ");
			sbQuery.append("where isanswered = 'false' and intodate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and intodate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by domainid,queuename");
			/*sql = "select domainid,queuename,cast(sum(extract(epoch from outdate)-extract(epoch from intodate)) as bigint)  from ec2_queue_request_detail "
					+ "where isanswered = 'false' and to_char(intodate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by domainid,queuename";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[0].toString()
						+ listObjects[1].toString();

				if (queueDetailMap.containsKey(key)) {

					// 设置未接通等待时长
					queueDetailMap.get(key).setUnconnect_wait_time_length(
							Integer.parseInt(listObjects[2].toString()));

				} else {

					throw new RuntimeException("设置未接通等待时长时出错");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @param date
	 *            日期（yyyy-MM-dd）
	 * @param hour
	 *            时段（24小时进制）
	 * @param domainId
	 *            所在域的id
	 * @param queueName
	 *            队列名
	 * @param into_queue_count
	 *            队列进入次数
	 * @param abandon_queue_count
	 *            队列放弃次数
	 * @param connect_wait_time_length
	 *            接通等待时长
	 * @param unconnect_wait_time_length
	 *            未接通等待时长
	 * @return
	 * @throws ParseException
	 */
	public QueueDetail getQueueDetailEntity(Object date, Object hour,
			Object domainId, Object queueName, Object into_queue_count,
			Object abandon_queue_count, Object connect_wait_time_length,
			Object unconnect_wait_time_length) throws ParseException {

		QueueDetail detail = new QueueDetail();

		if (date != null) {
			detail.setDate(dateFormat.parse(date.toString()));
		}
		if (hour != null) {
			detail.setHour(Integer.parseInt(hour.toString()));
		}
		if (domainId != null) {
			detail.setDomainId(domainId.toString());
		}
		if (queueName != null) {
			detail.setQueueName(queueName.toString());
		}
		if (into_queue_count != null) {
			detail.setInto_queue_count(Integer.parseInt(into_queue_count
					.toString()));
		}
		if (abandon_queue_count != null) {
			detail.setAbandon_queue_count(Integer.parseInt(abandon_queue_count
					.toString()));
		}
		if (connect_wait_time_length != null) {
			detail.setConnect_wait_time_length(Integer
					.parseInt(connect_wait_time_length.toString()));
		}
		if (unconnect_wait_time_length != null) {
			detail.setUnconnect_wait_time_length(Integer
					.parseInt(unconnect_wait_time_length.toString()));
		}

		return detail;
	}

	@Override
	public String getReportName() {

		return "队列详情中间表";
	}

}
