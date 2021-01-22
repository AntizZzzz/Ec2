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

import com.jiangyifen.ec2.report.entity.EmployeeBusy;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
import com.jiangyifen.ec2.utils.DateTransformFactory;

public class EmployeeBusyMiddleTable extends AbstractReport {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<Object[]> list;// 执行sql得到多条记录
	private List<Object[]> domainIdAndUsernameObjects;

	private String sql;
	private Date employeeCheckMaxDate;// ec2_report_employee_check中最大的日期（yyyy-MM-dd）
	private Date minDate;// ec2_queue_member_pause_event_log中最小的日期(yyyy-MM-dd)
	private Date maxDate;// ec2_queue_member_pause_event_log中最大的日期(yyyy-MM-dd)
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat hourFormat = new SimpleDateFormat("HH");// 24小时进制
	private DateTransformFactory dtf = new DateTransformFactory();

	private Object object;
	private Map<String, EmployeeBusy> map = new HashMap<String, EmployeeBusy>();

	public EmployeeBusyMiddleTable() {

	}

	// 生成中间表
	@Override
	public void execute() {

		try {

			// 获得中间表最大的日期
			object = this
					.getDateHour("select date,max(hour) from ec2_report_employee_busy where date = (select max(date) from ec2_report_employee_busy) group by date");

			if (object != null) {
				employeeCheckMaxDate = format.parse(format.format(object));
			}

			// ec2_queue_member_pause_event_log中最小的日期(yyyy-MM-dd hh24)
			/*object = this.getSingleRecord("select min(to_char(pausedate,'yyyy-MM-dd hh24')) from ec2_queue_member_pause_event_log where pausedate is not null and unpausedate is not null ");*/
			object = this.getSingleRecord("select min(pausedate) from ec2_queue_member_pause_event_log where pausedate is not null and unpausedate is not null ");

			if (object == null) {
				minDate = null;
			} else {
				minDate = format.parse(object.toString());
			}

			// ec2_queue_member_pause_event_log中最大的日期(yyyy-MM-dd hh24)
			/*object = this.getSingleRecord("select max(to_char(pausedate,'yyyy-MM-dd hh24')) from ec2_queue_member_pause_event_log where pausedate is not null and unpausedate is not null ");*/
			object = this.getSingleRecord("select max(pausedate) from ec2_queue_member_pause_event_log where pausedate is not null and unpausedate is not null ");

			if (object == null) {
				maxDate = null;
			} else {
				maxDate = format.parse(object.toString());
			}
			
			if (employeeCheckMaxDate == null) {

				if (minDate != null && maxDate != null) {

					if (minDate.toString().equals(maxDate.toString())) {

						saveEmpnoAndDate(minDate);

					} else {

						createDate(minDate, maxDate);
					}

				}

			} else {
				// 删除中间表中最大日期的数据
				this.delete("delete from ec2_report_employee_busy where date = "
						+ "'"
						+ dateFormat.format(employeeCheckMaxDate)
						+ "'"
						+ " and hour = "
						+ "'"
						+ hourFormat.format(employeeCheckMaxDate) + "'");

				if (employeeCheckMaxDate.toString().equals(maxDate.toString())) {

					saveEmpnoAndDate(employeeCheckMaxDate);
				} else {

					createDate(employeeCheckMaxDate, maxDate);
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

				saveEmpnoAndDate(date);

				calendar.setTime(startDate);
				// 精确到时
				calendar.add(Calendar.HOUR_OF_DAY, 1);
				date = calendar.getTime();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}

		}

	}

	// 把domain+username,日期（date,hour）存到Map中
	public void saveEmpnoAndDate(Date date) {

		// 清空map
		map.clear();

		try {

			sql = "select domainid,username from ec2_queue_member_pause_event_log where reason !='话后处理' and pausedate is not null and unpausedate is not null "
					+ "group by domainid,username";

			// 获得ec2_user中所有的工号
			domainIdAndUsernameObjects = this.getRecord(sql);

			for (Object[] objects : domainIdAndUsernameObjects) {

				EmployeeBusy employeeBusy = new EmployeeBusy();

				// 获得姓名
				Object name = this
						.getOneRecord("select realname from ec2_user where username = "
								+ "'" + objects[1] + "'");

				// 获得部门名称
				Object dname = this
						.getOneRecord("select name from ec2_department where id = (select department_id from ec2_user where username = "
								+ "'" + objects[1] + "'" + ")");
				// 设置日期
				employeeBusy.setDate(dateFormat.parse(dateFormat.format(date)));

				// 设置时段
				employeeBusy.setHour(Integer.parseInt(hourFormat.format(date)));
				// 设置domainId
				employeeBusy.setDomainId(objects[0] == null ? null : objects[0]
						.toString());

				// 设置部门
				employeeBusy.setDepartment(dname == null ? null : dname
						.toString());
				// 设置工号
				employeeBusy.setUsername(objects[1].toString());

				// 设置姓名
				employeeBusy.setName(name == null ? null : name.toString());

				// domainid + username 唯一
				map.put(objects[0].toString() + objects[1].toString(),
						employeeBusy);
			}

			insertData(date);

		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	// 插入数据
	public void insertData(Date date) {

		try {

			// 置忙时长
			busyTimelength(date);

			insertDB();

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);

		}

	}

	// 将map中的entity存入到数据库中
	public void insertDB() {
		try {

			// 遍历map
			Iterator<Entry<String, EmployeeBusy>> iterator = map.entrySet()
					.iterator();
			while (iterator.hasNext()) {

				Entry<String, EmployeeBusy> entry = iterator.next();
				EmployeeBusy employeeCheck = entry.getValue();

				// 队列名不为空的存入数据库
				if (employeeCheck.getQueueName() != null)

					this.save(employeeCheck);

			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 置忙时长
	public void busyTimelength(Date date) {

		try {

			// date 下一个小时日期
			String nextHour = DateUtil.getNextHour(date);
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select domainid,username,queue as queue,cast( SUM(extract(epoch from least(unpausedate,'");
			sbQuery.append(nextHour);
			sbQuery.append("')) - extract(epoch from greatest(pausedate,'");
			sbQuery.append(format.format(date));
			sbQuery.append(":00'))) as bigint) as sum from ec2_queue_member_pause_event_log where reason !='话后处理' and pausedate is not null and unpausedate is not null and ");
			sbQuery.append(" ((pausedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and pausedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("') or (unpausedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and unpausedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("')) group by domainid,username,queue ");

			/*sql = "select domainid,username,queue as queue,cast( SUM(extract(epoch from least(unpausedate,"
					+ "'"
					+ nextHour
					+ "'"
					+ ")) - extract(epoch from greatest(pausedate,"
					+ "'"
					+ format.format(date)
					+ ":00"
					+ "'"
					+ "))) as bigint) as sum "
					+ "from ec2_queue_member_pause_event_log "
					+ "where reason !='话后处理' and pausedate is not null and unpausedate is not null and to_char(pausedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " or to_char(unpausedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by domainid,username,queue ";*/

			sql = sbQuery.toString();
			// 获得每小时的记录
			list = this.getRecord(sql);

			if (list.size() == 0) {
				sbQuery.setLength(0);
				sbQuery.append("select domainid,username,queue as queue,cast( SUM(extract(epoch from least(unpausedate, '");
				sbQuery.append(nextHour);
				sbQuery.append("')) - extract(epoch from greatest(pausedate,'");
				sbQuery.append(format.format(date));
				sbQuery.append(":00'))) as bigint) as sum from ec2_queue_member_pause_event_log ");
				sbQuery.append("where reason !='话后处理' and pausedate is not null and unpausedate is not null and ");
				sbQuery.append(" pausedate < '");
				sbQuery.append(sdf.format(date));
				sbQuery.append("' and unpausedate > '");
				sbQuery.append(sdf.format(date));
				sbQuery.append("' group by domainid,username,queue ");
				
				/*sql = "select domainid,username,queue as queue,cast( SUM(extract(epoch from least(unpausedate,"
						+ "'"
						+ nextHour
						+ "'"
						+ ")) - extract(epoch from greatest(pausedate,"
						+ "'"
						+ format.format(date)
						+ ":00"
						+ "'"
						+ "))) as bigint) as sum "
						+ "from ec2_queue_member_pause_event_log "
						+ "where reason !='话后处理' and pausedate is not null and unpausedate is not null and to_char(pausedate,'yyyy-MM-dd hh24') < "
						+ "'"
						+ format.format(date)
						+ "'"
						+ " and to_char(unpausedate,'yyyy-MM-dd hh24') > "
						+ "'"
						+ format.format(date)
						+ "'"
						+ " group by domainid,username,queue ";*/
				sql = sbQuery.toString();
				list = this.getRecord(sql);

				for (Object[] objects : list) {

					String key = objects[0].toString() + objects[1].toString();

					if (map.containsKey(key)) {

						// 设置置忙时长
						map.get(key).setBusyTimeLength(
								Integer.parseInt(objects[3].toString()));

						// 设置队列名
						map.get(key).setQueueName(objects[2].toString());

					} else {

						throw new RuntimeException("置忙时长异常");

					}

				}

			} else {

				for (Object[] listObjects : list) {

					String key = listObjects[0].toString()
							+ listObjects[1].toString();

					if (map.containsKey(key)) {

						// 设置置忙时长
						map.get(key).setBusyTimeLength(
								Integer.parseInt(listObjects[3].toString()));

						// 设置队列名
						map.get(key).setQueueName(listObjects[2].toString());

					} else {

						throw new RuntimeException("置忙时长异常");
					}

				}

			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @param list
	 *            要打印的list
	 */
	public void debugList(List<Object[]> list) {
		logger.info("-------------------------------------------");
		for (Object[] objects : list) {
			for (int i = 0; i < objects.length; i++) {
				System.out.print(objects[i].toString() + "  ");
			}

			logger.info("");
		}
		logger.info("-------------------------------------------");
	}

	/**
	 * @param map
	 *            要打印的map
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void debugMap(Map map) {
		logger.info("=======================================");
		try {

			// 遍历map
			Iterator<Entry<String, EmployeeBusy>> iterator = map.entrySet()
					.iterator();
			while (iterator.hasNext()) {

				Entry<String, EmployeeBusy> entry = iterator.next();
				EmployeeBusy employeeCheck = entry.getValue();

				logger.info(employeeCheck.toString());

			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
		logger.info("=======================================");
	}

	@Override
	public String getReportName() {

		return "雇员考核中间表";
	}

}
