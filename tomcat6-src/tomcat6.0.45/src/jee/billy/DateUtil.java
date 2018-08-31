package jee.billy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	private static DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
	
	public static String date2String(Date date) {
		String s = null;
		
		s = df.format(date);
		return s;
	}
	
	public static String now2String() {
		return date2String(new Date());
	}
	
}
