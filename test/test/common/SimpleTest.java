package test.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SimpleTest {
	
	public static void main(String[] args) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("1970-01-01 HH:mm:00");
		Date now = new Date();
		String date = sdf.format(now);
		System.out.println("----------------"+date);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date ct = cal.getTime();
		System.out.println(sdf.format(ct));
		
		cal.set(1970, 1, 1, 17, 14, 0);
		Date ot = cal.getTime();
		System.out.println(sdf.format(ot));
		
		System.out.println(ot.compareTo(ct));
		System.out.println(ot.before(ct));
		
		

		Calendar cal2 = Calendar.getInstance();
		cal2.set(Calendar.HOUR_OF_DAY, 17);
		cal2.set(Calendar.MINUTE, 58);
		System.out.println(sdf.format(cal2.getTime()));
	}

}
