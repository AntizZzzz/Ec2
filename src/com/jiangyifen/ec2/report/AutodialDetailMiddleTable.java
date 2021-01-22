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

import com.jiangyifen.ec2.report.entity.AutodialDetail;
import com.jiangyifen.ec2.utils.DateTransformFactory;

public class AutodialDetailMiddleTable extends AbstractReport {

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
	 * 中间表 ec2_report_autodial_detail 中的最大日期 （yyyy-MM-dd）
	 */
	private Date autodialDetailMaxDate;
	/**
	 * ec2_marking_project_task 中 autodialtime 的最小值 （yyyy-MM-dd）
	 */
	private Date minDate;
	/**
	 * ec2_marking_project_task 中 autodialtime 的最大值 （yyyy-MM-dd）
	 */
	private Date maxDate;
	/**
	 * 年-月-日
	 */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * 小时（24小时进制）
	 */
	// private SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
	/**
	 * 年-月-日 时（24小时进制）
	 */
	// private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
	/**
	 * 执行sql时的返回值
	 */
	private Object object;
	/**
	 * key: domainId+username value: AutodialDetail对应的实例
	 */
	private Map<String, AutodialDetail> autodialDetailMap = new HashMap<String, AutodialDetail>();
	/**
	 * 日期转换工具类
	 */
	private DateTransformFactory dtf = new DateTransformFactory();

	public AutodialDetailMiddleTable() {

	}

	// 生成中间表
	public void execute() {
		try {

			// 获得中间表中最大的日期
			object = this.getSingleRecord("select max(date) from ec2_report_autodial_detail");
			if (object != null) {
				autodialDetailMaxDate = dateFormat.parse(object.toString());
			}

			// 获得原始表中最小的日期
			/*object = this.getSingleRecord("select to_char(min(starttimedate),'yyyy-MM-dd') from cdr ");*/
			object = this.getSingleRecord("select min(starttimedate) from cdr ");

			if (object == null) {
				minDate = null;
			} else {
				minDate = dateFormat.parse(object.toString());
			}

			// 获得原始表中最大的日期
			/*object = this.getSingleRecord(" select to_char(max(starttimedate),'yyyy-MM-dd') from cdr ");*/
			object = this.getSingleRecord(" select max(starttimedate) from cdr ");

			if (object == null) {
				maxDate = null;
			} else {
				maxDate = dateFormat.parse(object.toString());
			}

			if (autodialDetailMaxDate == null) {

				if (minDate != null && maxDate != null) {

					if (minDate.toString().equals(maxDate.toString())) {

						saveEntityToMap(minDate);

					} else {
						createDate(minDate, maxDate);
					}

				}

			} else {

				// 删除中间表中最大日期的数据
				this.delete("delete from ec2_report_autodial_detail where date = "
						+ "'" + dateFormat.format(autodialDetailMaxDate) + "'");

				if (autodialDetailMaxDate.toString().equals(maxDate.toString())) {

					saveEntityToMap(autodialDetailMaxDate);

				} else {
					createDate(autodialDetailMaxDate, maxDate);
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		}
	}

	// 生成从startDate到endDate时间段内的日期
	public void createDate(Date startDate, Date endDate) {

		Calendar calendar = Calendar.getInstance();
		Date date = startDate;// 初始值等于startDate

		// 如果startDate和endDate相等，则循环结束
		while (!startDate.equals(endDate)) {

			try {
				startDate = date;

				saveEntityToMap(date);

				calendar.setTime(startDate);
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				date = calendar.getTime();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}

		}

	}

	public void saveEntityToMap(Date date) throws ParseException {

		// 清空map
		autodialDetailMap.clear();

		/*sql = "select t.domainid,d.name,t.destusername,u.realname from ec2_department as d,ec2_user as u, "
		+ "( "
		+ "select domainid,destusername from cdr where destusername !='' group by domainid,destusername "
		+ ") as t "
		+ "where d.id = u.department_id and t.destusername = u.username ";*/
		
		sql = "select t.domainid,d.name,t.destusername,u.realname from ec2_department as d,ec2_user as u, (select domainid,destusername from cdr where destusername !='') as t where d.id = u.department_id and t.destusername = u.username ";

		list = this.getRecord(sql);

		if (list.size() == 0) {
			return;
		}

		for (Object[] objects : list) {

			// 部门名称
			Object deptName = objects[1];

			// 用户名
			Object username = objects[2];

			// 姓名
			Object realName = objects[3];

			AutodialDetail autodialDetail = this.getAutodialDetailEntity(date,
					objects[0], deptName, username, realName, null, null, null,
					null, null);

			// domainid + username + autodialname唯一
			autodialDetailMap.put(objects[0].toString() + username.toString(),
					autodialDetail);

		}

		insertData(date);

	}

	public void insertData(Date date) {
		try {

			// 接通数量
			connect_count(date);

			// 接通时长
			connect_time_length(date);

			// 平均通话时长
			average_call_time_length(date);

			// 接电话用时
			pickup_time_length(date);

			// 平均接电话用时
			average_pickup_time_length(date);

			insertDB();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 将map中的entity存入到数据库中
	public void insertDB() {

		insert(autodialDetailMap);

	}

	public void insert(Map<String, AutodialDetail> map) {

		Iterator<Entry<String, AutodialDetail>> iterator = map.entrySet()
				.iterator();

		while (iterator.hasNext()) {

			Entry<String, AutodialDetail> entry = iterator.next();

			AutodialDetail detail = entry.getValue();

			// 存入数据库
			this.save(detail);
		}
	}

	public void connect_count(Date date) {
		try {
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select domainid,destusername,count(*) from cdr ");
			sbQuery.append("where isautodial = true and destusername !='' and starttimedate >= '");
			sbQuery.append(dateFormat.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayAfter(date, "yyyy-MM-dd"));
			sbQuery.append("' group by domainid,destusername ");
			/*sql = "select domainid,destusername,count(*) from cdr "
					+ "where isautodial = true and destusername !='' and to_char(starttimedate,'yyyy-MM-dd') = "
					+ "'" + dateFormat.format(date) + "'"
					+ " group by domainid,destusername ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[0].toString()
						+ listObjects[1].toString();

				if (autodialDetailMap.containsKey(key)) {

					autodialDetailMap.get(key).setConnect_count(
							Integer.parseInt(listObjects[2].toString()));

				} else {

					logger.error("自动外呼，统计接通数量异常");
					logger.error("domain_id: " + listObjects[0].toString());
					logger.error("username: " + listObjects[1].toString());

					// TODO
					return;

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void connect_time_length(Date date) {
		try {
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select domainid,destusername,sum(billableseconds) from cdr where isautodial = true and destusername !='' and starttimedate >= '");
			sbQuery.append(dateFormat.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayAfter(date, "yyyy-MM-dd"));
			sbQuery.append("' group by domainid,destusername ");
			/*sql = "select domainid,destusername,sum(billableseconds) from cdr "
					+ "where isautodial = true and destusername !='' and to_char(starttimedate,'yyyy-MM-dd') = "
					+ "'" + dateFormat.format(date) + "'"
					+ " group by domainid,destusername ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[0].toString()
						+ listObjects[1].toString();

				if (autodialDetailMap.containsKey(key)) {

					autodialDetailMap.get(key).setConnect_time_length(
							Integer.parseInt(listObjects[2].toString()));

				} else {

					logger.error("自动外呼，统计接通时长时异常");
					logger.error("domain_id: " + listObjects[0].toString());
					logger.error("username: " + listObjects[1].toString());

					// TODO
					return;

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void average_call_time_length(Date date) {
		try {

			Iterator<Entry<String, AutodialDetail>> iterator = autodialDetailMap
					.entrySet().iterator();

			while (iterator.hasNext()) {

				Entry<String, AutodialDetail> entry = iterator.next();

				AutodialDetail detail = entry.getValue();

				if (detail.getConnect_count() != 0) {

					detail.setAverage_call_time_length(detail
							.getConnect_time_length()
							/ detail.getConnect_count());

				}

			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void pickup_time_length(Date date) {
		try {
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select domain_id,user_id,cast(sum(extract(epoch from autodialpickuptime)-extract(epoch from autodialansweredtime)) as bigint) from ec2_marketing_project_task ");
			sbQuery.append("where autodialisanswered = 'TRUE' and autodialiscsrpickup = 'TRUE' and user_id !='1' and autodialtime >= '");
			sbQuery.append(dateFormat.format(date));
			sbQuery.append("' and autodialtime < '");
			sbQuery.append(dtf.getSpecifiedDayAfter(date, "yyyy-MM-dd"));
			sbQuery.append("' group by domain_id,user_id");
			/*sql = "select domain_id,user_id,cast(sum(extract(epoch from autodialpickuptime)-extract(epoch from autodialansweredtime)) as bigint) from ec2_marketing_project_task "
					+ "where autodialisanswered = 'TRUE' and autodialiscsrpickup = 'TRUE' and user_id !='1' and to_char(autodialtime,'yyyy-MM-dd') = "
					+ "'"
					+ dateFormat.format(date)
					+ "'"
					+ " group by domain_id,user_id";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				StringBuffer sbSql = new StringBuffer("select u.username from ec2_user as u,ec2_marketing_project_task as task where task.user_id = u.id and task.user_id = '");
				sbSql.append(listObjects[1]);
				sbSql.append("'");
				
				// 用户名
				Object username = this.getOneRecord(sbSql.toString());

				String key = listObjects[0].toString() + username.toString();

				if (autodialDetailMap.containsKey(key)) {

					autodialDetailMap.get(key).setPickup_time_length(
							Integer.parseInt(listObjects[2].toString()));

				} else {

					logger.error("domain_id: " + listObjects[0].toString());
					logger.error("username: " + username.toString());

					throw new RuntimeException("自动外呼接起用时异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void average_pickup_time_length(Date date) {

		try {

			Iterator<Entry<String, AutodialDetail>> iterator = autodialDetailMap
					.entrySet().iterator();

			while (iterator.hasNext()) {

				Entry<String, AutodialDetail> entry = iterator.next();

				AutodialDetail detail = entry.getValue();

				if (detail.getConnect_count() != 0) {

					detail.setAverage_pickup_time_length(detail
							.getPickup_time_length()
							/ detail.getConnect_count());
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void seeData() {
		Iterator<Entry<String, AutodialDetail>> iterator = autodialDetailMap
				.entrySet().iterator();

		while (iterator.hasNext()) {

			Entry<String, AutodialDetail> entry = iterator.next();

			AutodialDetail detail = entry.getValue();

			logger.info(" detail.getDomainId() ----------> " + detail.getDomainId());
			logger.info("detail.getUsername() ---------> " + detail.getUsername());

		}

	}

	/**
	 * @param date
	 *            日期
	 * @param domainId
	 *            域的id
	 * @param deptName
	 *            部门名称
	 * @param autodialName
	 *            自动外呼名称
	 * @param username
	 *            用户名
	 * @param realName
	 *            姓名
	 * @param connect_count
	 *            接通数量
	 * @param connect_time_length
	 *            接通时长
	 * @param average_call_time_length
	 *            平均接通时长
	 * @param pickup_time_length
	 *            接电话用时
	 * @param average_pickup_time_length
	 *            接电话平均用时
	 * @return
	 * @throws ParseException
	 */
	public AutodialDetail getAutodialDetailEntity(Object date, Object domainId,
			Object deptName, Object username, Object realName,
			Object connect_count, Object connect_time_length,
			Object average_call_time_length, Object pickup_time_length,
			Object average_pickup_time_length) throws ParseException {

		AutodialDetail autodialDetail = new AutodialDetail();

		// 设置日期
		if (date != null) {
			autodialDetail.setDate(dateFormat.parse(dateFormat.format(date)
					.toString()));
		}
		// 设置所在域id
		if (domainId != null) {
			autodialDetail.setDomainId(domainId.toString());
		}
		// 设置部门名称
		if (deptName != null) {
			autodialDetail.setDeptName(deptName.toString());
		}

		// 设置用户名
		if (username != null) {
			autodialDetail.setUsername(username.toString());
		}
		// 设置姓名
		if (realName != null) {
			autodialDetail.setRealName(realName.toString());
		}
		// 设置接通数量
		if (connect_count != null) {
			autodialDetail.setConnect_count(Integer.parseInt(connect_count
					.toString()));
		}
		// 设置接通时长
		if (connect_time_length != null) {
			autodialDetail.setConnect_time_length(Integer
					.parseInt(connect_time_length.toString()));
		}
		// 设置平均通话时长
		if (average_call_time_length != null) {
			autodialDetail.setAverage_call_time_length(Integer
					.parseInt(average_call_time_length.toString()));
		}
		// 设置接电话用时
		if (pickup_time_length != null) {
			autodialDetail.setPickup_time_length(Integer
					.parseInt(pickup_time_length.toString()));
		}
		// 设置平均接电话用时
		if (average_pickup_time_length != null) {
			autodialDetail.setAverage_pickup_time_length(Integer
					.parseInt(average_pickup_time_length.toString()));
		}

		return autodialDetail;

	}

	@Override
	public String getReportName() {

		return "自动外呼详情中间表";
	}

}
