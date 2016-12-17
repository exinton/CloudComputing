package tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class time_tool {
	//get current time
	public String get_time() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(new Date());
	}
	public String get_time_stamp() {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
		return df.format(new Date());
	}
	public String get_time_msec() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss-SSS");
		return df.format(new Date());
	}
	//print current time 
	public void print_time() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(df.format(new Date()));
	}
	
	//Input "yyyy-MM-dd HH:mm:ss" time string, return a Long-type value (Unit : second)
	public long get_time_gap(String time_start,String time_end){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss-SSS");
		long diff = 0;
		try
		{
		    Date d1 = df.parse(time_end);
		    Date d2 = df.parse(time_start);
		    diff = d1.getTime() - d2.getTime();
		}
		catch (Exception e)
		{
		}
		 return diff;
		
	}
	
	//Input "yyyy-MM-dd HH:mm:ss" time string, return a Long-type value (Unit : second)
	public long get_time_long(String time_string) {
		time_string=time_string.replaceAll("-", "");
		time_string=time_string.replaceAll(":", "");
		time_string=time_string.replaceAll(" ", "");
		return Long.valueOf(time_string);
	}
	

}
