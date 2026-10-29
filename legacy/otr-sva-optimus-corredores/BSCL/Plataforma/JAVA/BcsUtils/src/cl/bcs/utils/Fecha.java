package cl.bcs.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class Fecha {
    private static Integer offSetLocal=null;
    private final static DateTimeFormatter newFormatterTS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final static DateTimeFormatter newFormatterDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final static DateTimeFormatter newFormatterSD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter newFormatterTT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyy-MM");
	public static final DateTimeFormatter YYYY = DateTimeFormatter.ofPattern("yyyy");
	public static final String DEFAULT_DATE_TIME = "0001-01-01 00:00:00.000";
	public static final String DEFAULT_TIME = "00:00:00.000";
	
    /**
     * Set seconds to Local Time
     * @param offset seconds
     */
    public final static void setLocalOffSet(int offset) {
    	offSetLocal=offset;
    }
    
    /**
     * Get seconds used
     */
    public final static Integer getLocalOffSet() {
    	return offSetLocal;
    }    
    
    
    
    /**
     * Return DateTime as String with the format given
     * offset is used to convert to local
     * @param date
     * @param format
     * @return
     */
    public final static String toLocalFormat(java.sql.Timestamp date,String format) {
    	java.sql.Timestamp localdate=addSeconds(date,offSetLocal);
    	SimpleDateFormat sd = new SimpleDateFormat(format);
    	return sd.format(localdate);    	
    }
    
    
    /**
     * Return DateTime as String with the format given
     * @param date
     * @param format
     * @return
     */
    public final static String toFormat(java.sql.Timestamp date,String format) {
    	SimpleDateFormat sd = new SimpleDateFormat(format);
    	return sd.format(date);    	
    } 
    
    public final static String toFormat(java.sql.Date date,String format) {
    	SimpleDateFormat sd = new SimpleDateFormat(format);
    	return sd.format(date);    	
    } 
    
    public final static String stringToSqlDate(java.util.Date date) {
    	String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		return simpleDateFormat.format(date);
    }
    
    /**
     * 
     * @param date
     * @return
     */
    public final static Timestamp toLocalTimeStamp(java.sql.Timestamp date) {
    	return addSeconds(date,offSetLocal);
    }
    
    public final static Timestamp toUTCTimeStamp(java.sql.Timestamp date) {
    	return substractSeconds(date,offSetLocal);
    }
    
    public final static java.sql.Time toLocalTime(java.sql.Time time) {
    	LocalTime localtime = time.toLocalTime();
    	localtime = localtime.plusSeconds(getLocalOffSet());
    	return Time.valueOf(localtime);
    }
    
    public final static java.sql.Timestamp fechaSistemaHoraLocal_fechaHoraUTC(String fechaSistema,String horaLocal) {
    	return Fecha.toUTCTimeStamp(Fecha.toDateTime(fechaSistema + " " + Fecha.toStringTime(Fecha.toTime(horaLocal))));
    }
    
    public final static java.sql.Timestamp fechaSistemaHoraUTC_fechaHoraUTC(String fechaSistema,java.sql.Time horaUTC) {
    	return Fecha.toUTCTimeStamp(Fecha.toDateTime(fechaSistema + " " + Fecha.toStringTime(Fecha.toLocalTime(horaUTC))));
    }
    
    
    /**
     * Return Timestamp as String yyyy-MM-dd HH:mm:ss.SSS
     * @param Timestamp
     * @return
     */
    public final static String toStringTimeStamp(java.sql.Timestamp Timestamp) {
    	return Timestamp.toLocalDateTime().format(newFormatterTS);
    }
    
    /**
     * Return Datetime as String yyyy-MM-dd HH:mm:ss
     * @param Timestamp
     * @return
     */
    public final static String toStringDatetime(java.sql.Timestamp Timestamp) {
    	return Timestamp.toLocalDateTime().format(newFormatterSD);
    }
    
    
    /**
     * Return Date as String yyyy-MM-dd
     * @param date
     * @return
     */
    public final static String toStringDate(java.sql.Date date) {
    	return (date!=null) ? date.toLocalDate().format(newFormatterDD) : "";
    	
    }
    /**
     * Return Date as String yyyy-MM-dd or default date.
     * @param date
     * @return
     */
    public final static String toStringDate(java.sql.Date date, String defaultDate) {
    	return (date!=null) ? date.toLocalDate().format(newFormatterDD) : defaultDate;
    	
    }

    /**
     * 
     * @param date
     * @return
     */
    public final static String toStringDate(java.sql.Timestamp date) {
    	return date.toLocalDateTime().format(newFormatterDD);
    	
    }

    /**
     * Return Time as String HH:mm:ss
     * @param time
     * @return
     */
    public final static String toStringTime(java.sql.Time time) {
    	return time.toLocalTime().format(newFormatterTT);
    }    
  
    /**
     * 
     * @param time
     * @return
     */
    public final static String toStringTime(java.sql.Timestamp time) {
    	return time.toLocalDateTime().format(newFormatterTT);
    }       
    
    /**
     * 
     * @param date
     * @return
     */
    public final static java.sql.Date toDate(String date) {
    	return date.length() == 19?
    			java.sql.Date.valueOf(LocalDate.parse(date, newFormatterSD)) : java.sql.Date.valueOf(LocalDate.parse(date, newFormatterDD));
    }
    
    public static void main(String[] args) {
    	/*String timestampStr = "2017-08-09 17:32:01";
    	String dateStr = "2017-02-01";
    	
    	Timestamp t1 = Fecha.toDateTime(timestampStr);
    	String st1 = Fecha.toStringDate(t1);
    	
    	Timestamp t2 = Fecha.toDateTime(dateStr);
    	String st2 = Fecha.toStringDate(t2);
    	
    	Date d = Fecha.toDate(dateStr);
    	String d2 = Fecha.toStringDate(d);
    	
    	Date d3 = Fecha.toDate(timestampStr);
    	String d4 = Fecha.toStringDate(d3);
    	*/
    	
    	//String timeDateStr = "2017-08-09";
    	//Date asdf = Fecha.toDate(timeDateStr);
    	//String st = Fecha.toStringDate(asdf);
    }
    
    /**
     * 
     * @param date
     * @return
     */
    public final static java.sql.Date utilDateToSqlDate(java.util.Date date) {
		String pattern = "yyyy-MM-dd";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String dateFormat = simpleDateFormat.format(date);
		return java.sql.Date.valueOf(LocalDate.parse(dateFormat, newFormatterDD));
		
    }
    
     
    
    /**
     * 
     * @param date
     * @return
     */
    public final static java.sql.Timestamp toDateTime(String date) {
    	if (date.length()==19) {
    		return java.sql.Timestamp.valueOf(LocalDateTime.parse(date, newFormatterSD));
    	}
    	else {
    		return java.sql.Timestamp.valueOf(LocalDateTime.parse(date + " 00:00:00", newFormatterSD));
    	}
    		
    	
    }    
    
    /**
     * 
     * @param date
     * @return
     */
    public final static java.sql.Timestamp toTimeStamp(String date) {
		if (date.length()==19) 
			return java.sql.Timestamp.valueOf(LocalDateTime.parse(date, newFormatterSD));
		return java.sql.Timestamp.valueOf(LocalDateTime.parse(date, newFormatterTS));
    }        
    
    /**
     * 
     * @param time
     * @return
     */
    public final static java.sql.Time toTime(String time) {
    	return java.sql.Time.valueOf(LocalTime.parse(time, newFormatterTT));
    }         
    
    /**
     * 
     * @param date
     * @param secs
     * @return
     */
    public final static java.sql.Timestamp addSeconds(java.sql.Timestamp date,int secs) {
    	Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        cal.add(Calendar.SECOND, secs);
        return new java.sql.Timestamp(cal.getTime().getTime());    	
    }
    
    public final static java.sql.Timestamp substractSeconds(java.sql.Timestamp date,int secs) {
    	Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        cal.add(Calendar.SECOND, secs*-1);
        return new java.sql.Timestamp(cal.getTime().getTime());    	
    }
    
    /**
     * 
     * @param date
     * @param hours
     * @return
     */
    public final static java.sql.Timestamp addHours(java.sql.Timestamp date,int hours) {
    	Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        cal.add(Calendar.HOUR, hours);
        return new java.sql.Timestamp(cal.getTime().getTime());    	
    }

    /**
     * 
     * @param date
     * @param millisecs
     * @return
     */
    public final static java.sql.Timestamp addMilliseconds(java.sql.Timestamp date,int millisecs) {
    	Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        cal.add(Calendar.MILLISECOND, millisecs);
        return new java.sql.Timestamp(cal.getTime().getTime());    	
    } 
    
    /**
     * 
     * @param date
     * @param millisecs
     * @return
     */
    public final static java.sql.Date addDays(java.sql.Date date,int days) {
    	Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_WEEK, days);
        return new java.sql.Date(cal.getTime().getTime());    	
    } 
    
    public final static java.sql.Timestamp addDays(java.sql.Timestamp date,int days) {
    	Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_WEEK, days);
        return new java.sql.Timestamp(cal.getTime().getTime());    	
    } 
    
    public final static java.sql.Date substractDays(java.sql.Date date,int days) {
    	Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_WEEK, days*-1);
        return new java.sql.Date(cal.getTime().getTime());    	
    }
    
    public final static java.sql.Timestamp substractDays(java.sql.Timestamp date,int days) {
    	Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_WEEK, days*-1);
        return new java.sql.Timestamp(cal.getTime().getTime());    	
    }
    
}
