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

import com.jiangyifen.ec2.report.entity.CustomerSatisfactionInvestigate;
import com.jiangyifen.ec2.utils.DateTransformFactory;

public class CustomerSatisfactionInvestigateMiddleTable extends AbstractReport {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<Object[]> list;// 执行sql得到多条记
	private List<Object[]> baseList; // 存工号，部门

	private String sql;
	private Date customerSatisfactionMaxDate;// ec2_report_customer_satisfaction_investigate中最大的日期（yyyy-MM-dd）
												// HH）
	private Date minDate;// cdr中最小的日期(yyyy-MM-dd HH)
	private Date maxDate;// cdr中最大的日期(yyyy-MM-dd HH)
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
	private Object object;
	private Map<String, CustomerSatisfactionInvestigate> customer_satisfaction_map = new HashMap<String, CustomerSatisfactionInvestigate>();
	private DateTransformFactory dtf = new DateTransformFactory();
	
	public CustomerSatisfactionInvestigateMiddleTable() {

	}

	// 生成中间表
	public void execute() {

		try {

			// 获得中间表中最大的日期
			customerSatisfactionMaxDate = this
					.getDateHour("select date,max(hour) from ec2_report_customer_satisfaction_investigate where date = (select max(date) from ec2_report_customer_satisfaction_investigate) group by date");

			// 获得原始表中最小的日期，精确到时
			/*object = this.getSingleRecord("select min(to_char(date,'yyyy-MM-dd hh24')) from ec2_customer_satisfaction_investigation_log ");*/
			object = this.getSingleRecord("select min(date) from ec2_customer_satisfaction_investigation_log ");

			if (object == null) {
				minDate = null;
			} else {
				minDate = format.parse(object.toString());
			}

			// 获得原始表中最大的日期，精确到时
			/*object = this.getSingleRecord("select max(to_char(date,'yyyy-MM-dd hh24')) from ec2_customer_satisfaction_investigation_log ");*/
			object = this.getSingleRecord("select max(date) from ec2_customer_satisfaction_investigation_log ");

			if (object == null) {
				maxDate = null;
			} else {
				maxDate = format.parse(object.toString());
			}

			if (customerSatisfactionMaxDate == null) {

				// 如果cdrMinDate,或cdrMaxDate有一个为null，说明原始表中数据为空
				if (minDate != null && maxDate != null) {

					if (minDate.toString().equals(maxDate.toString())) {

						saveEmpnoAndDate(minDate);

					} else {

						createDate(minDate, maxDate);

					}

				}

			} else {

				// 删除中间表中最大日期的数据
				this.delete("delete from ec2_report_customer_satisfaction_investigate where date = "
						+ "'"
						+ dateFormat.format(customerSatisfactionMaxDate)
						+ "'"
						+ " and hour = "
						+ "'"
						+ hourFormat.format(customerSatisfactionMaxDate) + "'");

				if (customerSatisfactionMaxDate.toString().equals(
						maxDate.toString())) {

					saveEmpnoAndDate(customerSatisfactionMaxDate);

				} else {
					createDate(customerSatisfactionMaxDate, maxDate);
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
				calendar.add(Calendar.HOUR_OF_DAY, 1);
				date = calendar.getTime();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}

		}

	}

	// 存工号,日期（date,hour）存到Map中
	public void saveEmpnoAndDate(Date date) {

		sql = "select u.domain_id,d.name,u.username,u.realname from ec2_user as u,ec2_department as d "
				+ "where u.department_id = d.id";

		if (baseList == null) {

			baseList = this.getRecord(sql);
		}

		insertBaseInfo(baseList, customer_satisfaction_map, date);

		insertData(date);

	}

	public void insertBaseInfo(List<Object[]> list,
			Map<String, CustomerSatisfactionInvestigate> map, Date date) {

		try {

			// 清空map
			map.clear();

			for (Object[] objects : list) {

				CustomerSatisfactionInvestigate satisfactionInvestigate = getCustomerSatisfactionInvestigate(
						dateFormat.format(date), hourFormat.format(date),
						objects[0], objects[1], objects[2], objects[3], null,
						null);

				// domainid + username 唯一
				map.put(objects[0].toString() + objects[2].toString(),
						satisfactionInvestigate);

			}

		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	/**
	 * @param date
	 *            格式: yyyy-MM-dd hh24
	 */
	public void insertData(Date date) {
		try {

			sumGradeAndCount(date);

			insertDB();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 将map中的entity存入到数据库中
	public void insertDB() {

		insert(customer_satisfaction_map);

	}

	public void insert(Map<String, CustomerSatisfactionInvestigate> map) {

		Iterator<Entry<String, CustomerSatisfactionInvestigate>> iterator = map
				.entrySet().iterator();

		while (iterator.hasNext()) {

			Entry<String, CustomerSatisfactionInvestigate> entry = iterator
					.next();

			CustomerSatisfactionInvestigate satisfactionInvestigate = entry
					.getValue();

			// 存入数据库
			this.save(satisfactionInvestigate);
		}
	}

	public void sumGradeAndCount(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(date,'yyyy-MM-dd') as date,to_char(date,'hh24') as hour,u.username,domainid,sum(cast(grade as Integer)),count(*) ");
			sbQuery.append("from ec2_customer_satisfaction_investigation_log as c,ec2_user as u ");
			sbQuery.append("where c.csrid = u.id and date >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and date < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,username,domainid ");
			/*sql = "select to_char(date,'yyyy-MM-dd') as date,to_char(date,'hh24') as hour,u.username,domainid,sum(cast(grade as Integer)),count(*) "
					+ "from ec2_customer_satisfaction_investigation_log as c,ec2_user as u "
					+ "where c.csrid = u.id and to_char(date,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,username,domainid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				// domainid + srcusername 唯一
				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (customer_satisfaction_map.containsKey(key)) {

					customer_satisfaction_map.get(key).setSumGrade(
							Integer.parseInt(listObjects[4].toString()));

					customer_satisfaction_map.get(key).setCount(
							Integer.parseInt(listObjects[5].toString()));

				} else {

					logger.error("客户满意调查异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public CustomerSatisfactionInvestigate getCustomerSatisfactionInvestigate(
			Object date, Object hour, Object domainId, Object deptName,
			Object username, Object realName, Object sumGrade, Object count)
			throws ParseException {

		CustomerSatisfactionInvestigate satisfactionInvestigate = new CustomerSatisfactionInvestigate();

		if (date != null) {
			satisfactionInvestigate.setDate(dateFormat.parse(date.toString()));
		}
		if (hour != null) {
			satisfactionInvestigate.setHour(Integer.parseInt(hour.toString()));
		}
		if (domainId != null) {
			satisfactionInvestigate.setDomainId(domainId.toString());
		}
		if (deptName != null) {
			satisfactionInvestigate.setDeptName(deptName.toString());
		}
		if (username != null) {
			satisfactionInvestigate.setUsername(username.toString());
		}
		if (realName != null) {
			satisfactionInvestigate.setRealName(realName.toString());
		}
		if (sumGrade != null) {
			satisfactionInvestigate.setSumGrade(Integer.parseInt(sumGrade
					.toString()));
		}
		if (count != null) {
			satisfactionInvestigate
					.setCount(Integer.parseInt(count.toString()));
		}

		return satisfactionInvestigate;
	}

	@Override
	public String getReportName() {

		return "客户满意度调查中间表";
	}

}
