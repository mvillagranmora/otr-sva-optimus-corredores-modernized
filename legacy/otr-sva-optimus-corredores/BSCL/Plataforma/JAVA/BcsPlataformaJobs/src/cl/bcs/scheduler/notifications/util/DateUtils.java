package cl.bcs.scheduler.notifications.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
	
	public static String getDateFormat(String pattern)
	{
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

			String date = simpleDateFormat.format(new Date());
	 		return date;
			
		} catch (Exception e) {
			
			 
			return null;
 			// TODO: handle exception
		}
 		
 	}

}
