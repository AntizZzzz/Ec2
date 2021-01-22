package com.jiangyifen.ec2.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.report.entity.EmployeeLogin;
import com.jiangyifen.ec2.utils.DateTransformFactory;

public class EmployeeLoginMiddleTable extends AbstractReport {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<Object[]> list;// 执行sql得到多条记录
	private String sql;
	private Date employeeDetailMaxDate;// ec2_report_employee_login_logout_detail中最大的日期（yyyy-MM-dd）
	private Date minDate;// ec2_user_login_record中最小的日期(yyyy-MM-dd)
	private Date maxDate;// ec2_user_login_record中最大的日期(yyyy-MM-dd)
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private Object object;
	private List<EmployeeLogin> loginLogoutDetails = new ArrayList<EmployeeLogin>();
	private DateTransformFactory dtf = new DateTransformFactory();

	public EmployeeLoginMiddleTable() {

	}

	// 生成中间表
	@Override
	public void execute() {
		try {

			/*object = this.getSingleRecord("select max(to_char(logindate,'yyyy-MM-dd')) from ec2_report_employee_login");*/
			object = this.getSingleRecord("select max(logindate) from ec2_report_employee_login");

			if (object == null) {
				employeeDetailMaxDate = null;
			} else {
				employeeDetailMaxDate = dateFormat.parse(object.toString());
			}

			// ec2_user_login_record中最小的日期(yyyy-MM-dd)
			/*object = this.getSingleRecord("select min(to_char(logindate,'yyyy-MM-dd')) from ec2_user_login_record ");*/
			object = this.getSingleRecord("select min(logindate) from ec2_user_login_record ");

			if (object == null) {
				minDate = null;
			} else {
				minDate = dateFormat.parse(object.toString());
			}

			// ec2_user_login_record中最大的日期(yyyy-MM-dd)
			/*object = this.getSingleRecord("select max(to_char(logindate,'yyyy-MM-dd')) from ec2_user_login_record ");*/
			object = this.getSingleRecord("select max(logindate) from ec2_user_login_record ");

			if (object == null) {
				maxDate = null;
			} else {
				maxDate = dateFormat.parse(object.toString());
			}

			if (employeeDetailMaxDate == null) {

				if (minDate != null && maxDate != null) {

					if (minDate.toString().equals(maxDate.toString())) {
						insertData(minDate);
					} else {
						createDate(minDate, maxDate);
					}

				}

			} else {
				// 删除中间表中最大日期的数据
				this.delete("delete from ec2_report_employee_login where logindate >= '"
						+ dateFormat.format(employeeDetailMaxDate) + "' and logindate < '" + dtf.getSpecifiedDayAfter(employeeDetailMaxDate, "yyyy-MM-dd") + "'");

				if (employeeDetailMaxDate.toString().equals(maxDate.toString())) {

					insertData(employeeDetailMaxDate);

				} else {
					createDate(employeeDetailMaxDate, maxDate);
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

				// 清空list
				loginLogoutDetails.clear();

				insertData(date);

				calendar.setTime(startDate);
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				date = calendar.getTime();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}

		}

	}

	// 插入数据
	public void insertData(Date date) {

		try {

			// 工号，分机号，ip，登陆时间，登出时间
			baseData(date);

			insertDB();

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 将map中的entity存入到数据库中
	public void insertDB() {
		try {

			// 遍历list

			Iterator<EmployeeLogin> iterable = loginLogoutDetails.iterator();

			while (iterable.hasNext()) {

				EmployeeLogin logoutDetail = iterable.next();

				// 存入数据库
				this.save(logoutDetail);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	// 部门名称，工号，姓名，分机号，ip,登陆时间，登出时间，domain
	public void baseData(Date date) {

		try {

			sql = "select deptname,username,realname,exten,ip,logindate,cast((extract(epoch from logoutdate) - extract(epoch from logindate)) as bigint),domainid,logoutdate from ec2_user_login_record "
					+ "where logindate >= '"
					+ dateFormat.format(date) + "' and logindate < '" + dtf.getSpecifiedDayAfter(date, "yyyy-MM-dd") + "'";

			list = this.getRecord(sql);

			// 创建entity，并赋值
			for (Object[] objects : list) {
				// 创建entity
				EmployeeLogin loginLogoutDetail = new EmployeeLogin();

				// 设置部门
				loginLogoutDetail.setDepartment(objects[0] == null ? null
						: objects[0].toString());
				// 设置工号
				loginLogoutDetail.setUsername(objects[1] == null ? null
						: objects[1].toString());
				// 设置姓名
				loginLogoutDetail.setName(objects[2] == null ? null
						: objects[2].toString());

				// 设置分机号
				loginLogoutDetail.setFenji(objects[3] == null ? null
						: objects[3].toString());

				// 设置ip
				loginLogoutDetail.setIp(objects[4] == null ? null : objects[4]
						.toString());

				// 设置登陆时间
				loginLogoutDetail.setLoginDate(objects[5] == null ? null
						: format.parse(objects[5].toString()));

				// 设置登入时长
				loginLogoutDetail.setLogin_time_length(Integer
						.parseInt((objects[6] == null ? "0" : objects[6]
								.toString())));
				// 设置domainid
				loginLogoutDetail.setDomainId(objects[7] == null ? null
						: objects[7].toString());

				// 设置登出时间
				loginLogoutDetail.setLogoutDate(objects[8] == null ? null
						: format.parse(objects[8].toString()));
				// 存入数据库
				loginLogoutDetails.add(loginLogoutDetail);

			}

		} catch (ParseException e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public String getReportName() {

		return "雇员登陆登出详单中间表";
	}
}
