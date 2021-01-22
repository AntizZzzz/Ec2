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

import com.jiangyifen.ec2.report.entity.CallCheck;
import com.jiangyifen.ec2.utils.DateTransformFactory;

public class CallCheckMiddleTable_lc_bak extends AbstractReport {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<Object[]> list;// 执行sql得到多条记
	private List<Object[]> srcBaseList;
	private List<Object[]> destBaseList;

	private String sql;
	private Date callCheckMaxDate;// ec2_report_call_check中最大的日期（yyyy-MM-dd HH）
	private Date minDate;// cdr中最小的日期(yyyy-MM-dd HH)
	private Date maxDate;// cdr中最大的日期(yyyy-MM-dd HH)
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
	private Object object;
	private Map<String, CallCheck> srcMap = new HashMap<String, CallCheck>();
	private Map<String, CallCheck> destMap = new HashMap<String, CallCheck>();
	private DateTransformFactory dtf = new DateTransformFactory();

	public CallCheckMiddleTable_lc_bak() {

	}

	// 生成中间表
	public void execute() {
		try {

			// 获得中间表中最大的日期
			callCheckMaxDate = this
					.getDateHour("select date,max(hour) from ec2_report_call_check where date = (select max(date) from ec2_report_call_check) group by date");

			// 获得cdr表中最小的日期，精确到时
			/*object = this.getSingleRecord("select min(to_char(starttimedate,'yyyy-MM-dd hh24')) from cdr ");*/
			object = this.getSingleRecord("select min(starttimedate) from cdr ");

			if (object == null) {
				minDate = null;
			} else {
				minDate = format.parse(object.toString());
			}

			// 获得cdr表中最大的日期，精确到时
			/*object = this.getSingleRecord("select max(to_char(starttimedate,'yyyy-MM-dd hh24')) from cdr ");*/
			object = this.getSingleRecord("select max(starttimedate) from cdr ");

			if (object == null) {
				maxDate = null;
			} else {
				maxDate = format.parse(object.toString());
			}

			if (callCheckMaxDate == null) {

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
				this.delete("delete from ec2_report_call_check where date = "
						+ "'" + dateFormat.format(callCheckMaxDate) + "'"
						+ " and hour = " + "'"
						+ hourFormat.format(callCheckMaxDate) + "'");

				if (callCheckMaxDate.toString().equals(maxDate.toString())) {

					saveEmpnoAndDate(callCheckMaxDate);

				} else {
					createDate(callCheckMaxDate, maxDate);
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

		sql = "select t.domainid,d.name,t.srcusername,u.realname from ec2_department as d,ec2_user as u, "
				+ "( "
				+ "select domainid,srcusername from cdr where srcusername !='' group by domainid,srcusername "
				+ ") as t "
				+ "where d.id = u.department_id and t.srcusername = u.username";

		if (srcBaseList == null) {

			srcBaseList = this.getRecord(sql);
		}

		insertBaseInfo(srcBaseList, srcMap, date);

		sql = "select t.domainid,d.name,t.destusername,u.realname from ec2_department as d,ec2_user as u, "
				+ "( "
				+ "select domainid,destusername from cdr where destusername !='' group by domainid,destusername "
				+ ") as t "
				+ "where d.id = u.department_id and t.destusername = u.username ";

		if (destBaseList == null) {

			destBaseList = this.getRecord(sql);
		}

		insertBaseInfo(destBaseList, destMap, date);

		insertData(date);

	}

	public void insertBaseInfo(List<Object[]> list, Map<String, CallCheck> map,
			Date date) {

		try {

			// 清空map
			map.clear();

			for (Object[] objects : list) {

				CallCheck callCheck = getCallCheck(dateFormat.format(date),
						hourFormat.format(date), objects[0], null, objects[1],
						objects[2], objects[3], null, null, null, null, null,
						null, null, null, null, null, null, null, null, null,
						null, null, null, null, null, null, null, null);

				// domainid + username 唯一
				map.put(objects[0].toString() + objects[2].toString(),
						callCheck);

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

			// 内部总数
			src_e_to_e_total_count(date);

			// 内部接通数量,时长
			src_e_to_e_connect_count_and_time_length(date);

			// 内部未接通数量,时长
			src_e_to_e_unconnect_count_and_time_length(date);

			// 呼出总数
			src_outgoing_total_count(date);

			// 呼出置闲时长
			src_outgoing_free_time_length(date);

			// 呼出接通数量,时长
			src_outgoing_connect_count_and_time_length(date);

			// 呼出未接通数量,时长
			src_outgoing_unconnect_count_and_time_length(date);

			// 呼入总数
			// src_incoming_total_count(date);

			// 呼入接通数量,时长
			// src_incoming_connect_count_and_time_length(date);

			// 呼入未接通数量,时长
			// src_incoming_unconnect_count_and_time_length(date);

			// 外传外总数
			src_o_to_o_total_count(date);

			// 外传外接通数量,时长
			src_o_to_o_connect_count_and_time_length(date);

			// 外传外未接通数量,时长
			src_o_to_o_unconnect_count_and_time_length(date);

			// --------------------------------------------------------------------//

			// dest_e_to_e_total_count(date);

			// 内部接通数量，时长
			// dest_e_to_e_connect_and_time_length(date);

			// 内部未接通数量，时长
			// dest_e_to_e_unconnect_and_time_length(date);

			// dest_outgoing_total_count(date);

			// 呼出接通数量，时长
			// dest_outgoing_connect_count_and_time_length(date);

			// 呼出未接通数量，时长
			// dest_outgoing_unconnect_count_and_time_length(date);

			dest_incoming_total_count(date);

			// 呼入接通数量，时长
			dest_incoming_connect_count_and_time_length(date);

			// 呼入未接通数量，时长
			dest_incoming_unconnect_count_and_time_length(date);

			// dest_o_to_o_total_count(date);

			// 外传外接通数量，时长
			// dest_o_to_o_connect_count_and_time_length(date);

			// 外传外未接通数量，时长
			// dest_o_to_o_unconnect_count_and_time_length(date);

			insertDB();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 将map中的entity存入到数据库中
	public void insertDB() {

		insert(srcMap);
		insert(destMap);
	}

	public void insert(Map<String, CallCheck> map) {

		Iterator<Entry<String, CallCheck>> iterator = map.entrySet().iterator();

		while (iterator.hasNext()) {

			Entry<String, CallCheck> entry = iterator.next();

			CallCheck callCheck = entry.getValue();

			// 存入数据库
			this.save(callCheck);
		}
	}

	public void src_e_to_e_total_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
					
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '0' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '0' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				// domainid + srcusername 唯一
				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {
						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					callCheck.setE_to_e_total_count(Integer
							.parseInt(listObjects[5].toString()));

				} else {

					logger.error("src内部接通数量，时长异常");

					// throw new RuntimeException("src内部接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 内部接通数量，时长
	 * 
	 * @param date
	 *            格式:yyyy-MM-dd hh24
	 */
	public void src_e_to_e_connect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '0' and disposition = 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '0' and disposition = 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录
			list = this.getRecord(sql);
			sbQuery = null;
			
			for (Object[] listObjects : list) {

				// domainid + srcusername 唯一
				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置内部接通数量
					callCheck.setE_to_e_connect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置内部接通时长
					callCheck.setE_to_e_connect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src内部接通数量，时长异常");
					// throw new RuntimeException("src内部接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void src_e_to_e_unconnect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '0' and disposition != 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '0' and disposition != 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				// domainid + srcusername 唯一
				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置内部接通数量
					callCheck.setE_to_e_unconnect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置内部接通时长
					callCheck.setE_to_e_unconnect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src内部接通数量，时长异常");
					// throw new RuntimeException("src内部接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void src_outgoing_total_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '1' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '1' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录hhhh
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼出数量
					callCheck.setOutgoing_total_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼出时长
					callCheck.setOutgoing_total_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src呼出接通数量，时长异常");
					// throw new RuntimeException("src呼出接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 设置置闲时长（一通电话结束距下通电话开始）
	public void src_outgoing_free_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,extract(epoch from (max(endtimedate)-min(starttimedate)-sum(endtimedate-starttimedate))) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '1' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,extract(epoch from (max(endtimedate)-min(starttimedate)-sum(endtimedate-starttimedate))) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '1' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录hhhh
			list = this.getRecord(sql);
			sbQuery = null;
			
			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置置闲时长
					callCheck.setOutgoing_free_time_length((int) Double
							.parseDouble(listObjects[5].toString()));

				} else {

					logger.error("src呼出置闲时长异常");
					// throw new RuntimeException("src呼出接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 呼出接通数量，时长
	 * 
	 * @param date
	 *            格式：yyyy-MM-dd hh24
	 */
	public void src_outgoing_connect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '1' and disposition = 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '1' and disposition = 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录hhhh
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}
					// 设置呼出接通数量
					callCheck.setOutgoing_connect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼出接通时长
					callCheck.setOutgoing_connect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src呼出接通数量，时长异常");
					// throw new RuntimeException("src呼出接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void src_outgoing_unconnect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '1' and disposition != 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '1' and disposition != 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录hhhh
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼出接通数量
					callCheck.setOutgoing_unconnect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼出接通时长
					callCheck.setOutgoing_unconnect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src呼出接通数量，时长异常");
					// throw new RuntimeException("src呼出接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void src_incoming_total_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '2' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '2' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼入接通数量
					callCheck.setIncoming_total_count(Integer
							.parseInt(listObjects[5].toString()));

				} else {

					logger.error("src呼入接通数量，时长异常");
					// throw new RuntimeException("src呼入接通数量，时长异常");
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	// 呼入数量,时长(接通)
	public void src_incoming_connect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '2' and disposition = 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '2' and disposition = 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼入接通数量
					callCheck.setIncoming_connect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼入接通时长
					callCheck.setIncoming_connect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src呼入接通数量，时长异常");
					// throw new RuntimeException("src呼入接通数量，时长异常");
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void src_incoming_unconnect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '2' and disposition != 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '2' and disposition != 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼入接通数量
					callCheck.setIncoming_unconnect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼入接通时长
					callCheck.setIncoming_unconnect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src呼入接通数量，时长异常");
					// throw new RuntimeException("src呼入接通数量，时长异常");
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void src_o_to_o_total_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '3' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '3' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;
			
			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}
					// 设置呼入数量（接通）
					callCheck.setO_to_o_total_count(Integer
							.parseInt(listObjects[5].toString()));

				} else {

					logger.error("src外传外接通数量，时长异常");
					// throw new RuntimeException("src外传外接通数量，时长异常");
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 外传外接通数量，时长
	public void src_o_to_o_connect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '3' and disposition = 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '3' and disposition = 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼入数量（接通）
					callCheck.setO_to_o_connect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼入时长（接通）
					callCheck.setO_to_o_connect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src外传外接通数量，时长异常");
					// throw new RuntimeException("src外传外接通数量，时长异常");
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void src_o_to_o_unconnect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where srcusername != '' and cdrdirection = '3' and disposition != 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,srcusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,srcusername as srcusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where srcusername != '' and cdrdirection = '3' and disposition != 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,srcusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (srcMap.containsKey(key)) {

					CallCheck callCheck = srcMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}
					// 设置呼入数量（接通）
					callCheck.setO_to_o_unconnect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼入时长（接通）
					callCheck.setO_to_o_unconnect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("src外传外接通数量，时长异常");
					// throw new RuntimeException("src外传外接通数量，时长异常");
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dest_e_to_e_total_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '0' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '0' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					callCheck.setE_to_e_total_count(Integer
							.parseInt(listObjects[5].toString()));

				} else {

					logger.error("dest内部接通数量，时长异常");
					// throw new RuntimeException("dest内部接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// dest 内部接通数量，时长
	public void dest_e_to_e_connect_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '0' and disposition = 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '0' and disposition = 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					callCheck.setE_to_e_connect_count(Integer
							.parseInt(listObjects[5].toString()));

					callCheck.setE_to_e_connect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest内部接通数量，时长异常");
					// throw new RuntimeException("dest内部接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dest_e_to_e_unconnect_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '0' and disposition != 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '0' and disposition != 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					callCheck.setE_to_e_unconnect_count(Integer
							.parseInt(listObjects[5].toString()));

					callCheck.setE_to_e_unconnect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest内部接通数量，时长异常");
					// throw new RuntimeException("dest内部接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dest_outgoing_total_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '1' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '1' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录hhhh
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼出接通数量
					callCheck.setOutgoing_total_count(Integer
							.parseInt(listObjects[5].toString()));

					callCheck.setOutgoing_total_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest呼出接通数量，时长异常");
					// throw new RuntimeException("dest呼出接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 呼出接通数量，时长
	 * 
	 * @param date
	 *            格式：yyyy-MM-dd hh24
	 */
	public void dest_outgoing_connect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '1' and disposition = 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '1' and disposition = 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			// 获得每小时的记录hhhh
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼出接通数量
					callCheck.setOutgoing_connect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼出接通时长
					callCheck.setOutgoing_connect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest呼出接通数量，时长异常");
					// throw new RuntimeException("dest呼出接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dest_outgoing_unconnect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '1' and disposition != 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '1' and disposition != 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/

			// 获得每小时的记录hhhh
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼出接通数量
					callCheck.setOutgoing_unconnect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼出接通时长
					callCheck.setOutgoing_unconnect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest呼出接通数量，时长异常");
					// throw new RuntimeException("dest呼出接通数量，时长异常");

				}

			}

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dest_incoming_total_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '2' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '2' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼入接通数量
					callCheck.setIncoming_total_count(Integer
							.parseInt(listObjects[5].toString()));

				} else {

					logger.error("dest呼入接通数量，时长异常");
					// throw new RuntimeException("dest呼入接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 呼入数量,时长(接通)
	public void dest_incoming_connect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '2' and disposition = 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '2' and disposition = 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}
					// 设置呼入接通数量
					callCheck.setIncoming_connect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼入接通时长
					callCheck.setIncoming_connect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest呼入接通数量，时长异常");
					// throw new RuntimeException("dest呼入接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dest_incoming_unconnect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '2' and disposition != 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '2' and disposition != 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼入接通数量
					callCheck.setIncoming_unconnect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼入接通时长
					callCheck.setIncoming_unconnect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest呼入接通数量，时长异常");
					// throw new RuntimeException("dest呼入接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dest_o_to_o_total_count(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '3' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '3' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}

					// 设置呼入数量（接通）
					callCheck.setO_to_o_total_count(Integer
							.parseInt(listObjects[5].toString()));

				} else {

					logger.error("dest外传外接通数量，时长异常");
					// throw new RuntimeException("dest外传外接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 外传外接通数量，时长
	public void dest_o_to_o_connect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '3' and disposition = 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '3' and disposition = 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}
					// 设置呼入数量（接通）
					callCheck.setO_to_o_connect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼入时长（接通）
					callCheck.setO_to_o_connect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest外传外接通数量，时长异常");
					// throw new RuntimeException("dest外传外接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void dest_o_to_o_unconnect_count_and_time_length(Date date) {
		try {
			String dateFormat = "yyyy-MM-dd HH:00:00";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			
			StringBuffer sbQuery = new StringBuffer();
			sbQuery.append("select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count ");
			sbQuery.append("from cdr where destusername != '' and cdrdirection = '3' and disposition != 'ANSWERED' and starttimedate >= '");
			sbQuery.append(sdf.format(date));
			sbQuery.append("' and starttimedate < '");
			sbQuery.append(dtf.getSpecifiedDayHourAfter(date, dateFormat));
			sbQuery.append("' group by date,hour,destusername,domainid,projectid ");
			/*sql = "select to_char(starttimedate,'yyyy-MM-dd') as date,to_char(starttimedate,'hh24') as hour,destusername as destusername,domainid as domainid,projectid,count(*) as count,sum(billableseconds) as count "
					+ "from cdr "
					+ "where destusername != '' and cdrdirection = '3' and disposition != 'ANSWERED' and to_char(starttimedate,'yyyy-MM-dd hh24') = "
					+ "'"
					+ format.format(date)
					+ "'"
					+ " group by date,hour,destusername,domainid,projectid ";*/
			sql = sbQuery.toString();
			list = this.getRecord(sql);
			sbQuery = null;

			for (Object[] listObjects : list) {

				String key = listObjects[3].toString()
						+ listObjects[2].toString();

				if (destMap.containsKey(key)) {

					CallCheck callCheck = destMap.get(key);

					if (listObjects[4] != null) {

						callCheck.setProjectId(Integer.parseInt(listObjects[4]
								.toString()));
					}
					// 设置呼入数量（接通）
					callCheck.setO_to_o_unconnect_count(Integer
							.parseInt(listObjects[5].toString()));

					// 设置呼入时长（接通）
					callCheck.setO_to_o_unconnect_time_length(Integer
							.parseInt(listObjects[6].toString()));

				} else {

					logger.error("dest外传外接通数量，时长异常");
					// throw new RuntimeException("dest外传外接通数量，时长异常");

				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * @param date
	 *            日期
	 * @param hour
	 *            时段
	 * @param domainId
	 *            域的id
	 * @param deptName
	 *            部门名称
	 * @param username
	 *            用户名
	 * @param realName
	 *            真实姓名
	 * @param e_to_e_total_count
	 *            内部总数
	 * @param e_to_e_connect_count
	 *            内部接通数量
	 * @param e_to_e_connect_time_length
	 *            内部接通时长
	 * @param e_to_e_unconnect_count
	 *            内部未接通数量
	 * @param e_to_e_unconnect_time_length
	 *            内部未接通时长
	 * @param outgoing_total_count
	 *            呼出总数
	 * @param outgoing_connect_count
	 *            呼出接通数量
	 * @param outgoing_connect_time_length
	 *            呼出接通时长
	 * @param outgoing_unconnect_count
	 *            呼出未接通数量
	 * @param outgoing_unconnect_time_length
	 *            呼出未接通时长
	 * @param incoming_total_count
	 *            呼入总数
	 * @param incoming_connect_count
	 *            呼入接通数量
	 * @param incoming_connect_time_length
	 *            呼入接通时长
	 * @param incoming_unconnect_count
	 *            呼入未接通数量
	 * @param incoming_unconnect_time_length
	 *            呼入未接通时长
	 * @param o_to_o_total_count
	 *            外传外总数
	 * @param o_to_o_connect_count
	 *            外传外接通数量
	 * @param o_to_o_connect_time_length
	 *            外传外接通时长
	 * @param o_to_o_unconnect_count
	 *            外传外未接通数量
	 * @param o_to_o_unconnect_time_length
	 *            外传外未接通时长
	 * @return
	 * @throws ParseException
	 */
	public CallCheck getCallCheck(Object date, Object hour, Object domainId,
			Object projectId, Object deptName, Object username,
			Object realName, Object e_to_e_total_count,
			Object e_to_e_connect_count, Object e_to_e_connect_time_length,
			Object e_to_e_unconnect_count, Object e_to_e_unconnect_time_length,
			Object outgoing_total_count, Object outgoing_free_time_length,
			Object outgoing_total_time_length, Object outgoing_connect_count,
			Object outgoing_connect_time_length,
			Object outgoing_unconnect_count,
			Object outgoing_unconnect_time_length, Object incoming_total_count,
			Object incoming_connect_count, Object incoming_connect_time_length,
			Object incoming_unconnect_count,
			Object incoming_unconnect_time_length, Object o_to_o_total_count,
			Object o_to_o_connect_count, Object o_to_o_connect_time_length,
			Object o_to_o_unconnect_count, Object o_to_o_unconnect_time_length)
			throws ParseException {

		CallCheck callCheck = new CallCheck();

		if (date != null) {
			callCheck.setDate(dateFormat.parse(date.toString()));
		}
		if (hour != null) {
			callCheck.setHour(Integer.parseInt(hour.toString()));
		}
		if (domainId != null) {
			callCheck.setDomainId(domainId.toString());
		}
		if (projectId != null) {
			callCheck.setProjectId(Integer.parseInt(projectId.toString()));
		}
		if (deptName != null) {
			callCheck.setDeptName(deptName.toString());
		}
		if (username != null) {
			callCheck.setUsername(username.toString());
		}
		if (realName != null) {
			callCheck.setRealName(realName.toString());
		}
		if (e_to_e_total_count != null) {
			callCheck.setE_to_e_total_count(Integer.parseInt(e_to_e_total_count
					.toString()));
		}
		if (e_to_e_connect_count != null) {
			callCheck.setE_to_e_connect_count(Integer
					.parseInt(e_to_e_connect_count.toString()));
		}
		if (e_to_e_connect_time_length != null) {
			callCheck.setE_to_e_connect_time_length(Integer
					.parseInt(e_to_e_connect_time_length.toString()));
		}
		if (e_to_e_unconnect_count != null) {
			callCheck.setE_to_e_unconnect_count(Integer
					.parseInt(e_to_e_unconnect_count.toString()));
		}
		if (e_to_e_unconnect_time_length != null) {
			callCheck.setE_to_e_unconnect_time_length(Integer
					.parseInt(e_to_e_unconnect_time_length.toString()));
		}
		if (outgoing_total_count != null) {
			callCheck.setOutgoing_total_count(Integer
					.parseInt(outgoing_total_count.toString()));
		}

		if (outgoing_free_time_length != null) {
			callCheck.setOutgoing_free_time_length(Integer
					.parseInt(outgoing_free_time_length.toString()));
		}

		if (outgoing_total_time_length != null) {
			callCheck.setOutgoing_total_time_length(Integer
					.parseInt(outgoing_total_time_length.toString()));
		}

		if (outgoing_connect_count != null) {
			callCheck.setOutgoing_connect_count(Integer
					.parseInt(outgoing_connect_count.toString()));
		}
		if (outgoing_connect_time_length != null) {
			callCheck.setOutgoing_connect_time_length(Integer
					.parseInt(outgoing_connect_time_length.toString()));
		}
		if (outgoing_unconnect_count != null) {
			callCheck.setOutgoing_unconnect_count(Integer
					.parseInt(outgoing_unconnect_count.toString()));
		}
		if (outgoing_unconnect_time_length != null) {
			callCheck.setOutgoing_unconnect_time_length(Integer
					.parseInt(outgoing_unconnect_time_length.toString()));
		}
		if (incoming_total_count != null) {
			callCheck.setIncoming_total_count(Integer
					.parseInt(incoming_total_count.toString()));
		}
		if (incoming_connect_count != null) {
			callCheck.setIncoming_connect_count(Integer
					.parseInt(incoming_connect_count.toString()));
		}
		if (incoming_connect_time_length != null) {
			callCheck.setIncoming_connect_time_length(Integer
					.parseInt(incoming_connect_time_length.toString()));
		}
		if (incoming_unconnect_count != null) {
			callCheck.setIncoming_unconnect_count(Integer
					.parseInt(incoming_unconnect_count.toString()));
		}
		if (incoming_unconnect_time_length != null) {
			callCheck.setIncoming_unconnect_time_length(Integer
					.parseInt(incoming_unconnect_time_length.toString()));
		}
		if (o_to_o_total_count != null) {
			callCheck.setO_to_o_total_count(Integer.parseInt(o_to_o_total_count
					.toString()));
		}
		if (o_to_o_connect_count != null) {
			callCheck.setO_to_o_connect_count(Integer
					.parseInt(o_to_o_connect_count.toString()));
		}
		if (o_to_o_connect_time_length != null) {
			callCheck.setO_to_o_connect_time_length(Integer
					.parseInt(o_to_o_connect_time_length.toString()));
		}
		if (o_to_o_unconnect_count != null) {
			callCheck.setO_to_o_unconnect_count(Integer
					.parseInt(o_to_o_unconnect_count.toString()));
		}
		if (o_to_o_unconnect_time_length != null) {
			callCheck.setO_to_o_unconnect_time_length(Integer
					.parseInt(o_to_o_unconnect_time_length.toString()));
		}

		return callCheck;
	}

	@Override
	public String getReportName() {

		return "话务考核中间表";
	}

}
