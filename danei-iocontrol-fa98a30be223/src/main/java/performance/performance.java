package performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;

import tools.time_tool;

public class performance {
	String log_file_Path = "";
	LinkedList<String[]> request_log = new LinkedList<String[]>();
	public performance(String log_file_path){
		this.log_file_Path=log_file_path;
	}
	public void load_request_log(){
		File file = new File(log_file_Path);
        BufferedReader reader = null;
        try {
			reader = new BufferedReader(new FileReader(file));
			  String tempString = null;
		        int line = 1;
		        while ((tempString = reader.readLine()) != null) {
		            if(tempString.startsWith("Success")){
		        //	System.out.println("line " + line + ": " + tempString);
		        	String[] tmp = tempString.split(",");
		        	String[] tmp2 = tmp[0].split(":");
		        	String request_type = tmp2[1];
		        	String target_file = tmp2[2];
		        	String[] tmp3 = tmp[1].split(":");
		        	String offset_start = tmp3[0];
		        	String offset_end = tmp3[1];
		        	String start_time = tmp[2];
		        	String end_time = tmp[3];
		        	String[] one_request_record={request_type,target_file,offset_start,offset_end,start_time,end_time};
		        	this.request_log.add(one_request_record);
		            line++;
		            }
		        }
		        reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void print_all_request_record(){
		for (int i = 0; i < request_log.size(); i++) {
			for (int j = 0; j <request_log.get(i).length; j++) {
				String[] one_request_record = request_log.get(i);
				System.out.println(i+" Request type : "+one_request_record[0]);
				System.out.println("    target_file : "+one_request_record[1]);
				System.out.println("    offset_start => offset_end : "+one_request_record[2]+" => "+one_request_record[3]);
				System.out.println("    start_time => end_time : "+one_request_record[4]+" => "+one_request_record[5]);
			}
		}
	}
	public double get_reading_speed(){
		long total_size=0;
		long total_time=0;
		for (int i = 0; i < request_log.size(); i++) {
			time_tool tt =new time_tool();
			String type =request_log.get(i)[0];
			long size = Long.valueOf(request_log.get(i)[3])-0;
			long time = tt.get_time_gap(request_log.get(i)[4], request_log.get(i)[5]);
			if(type.contains("SEQ_READ")||type.contains("RANDOM_READ")){
				//System.out.println(type+" size : "+size+" time : "+time);
				total_size += size;
				total_time += time;
			}
		}
		//System.out.println("total_size : "+total_size+" total_time : "+total_time);
		double reading_speed =  total_size/total_time;
//		System.out.println("total_size : "+total_size);
//		System.out.println("total_time : "+total_time);
		return reading_speed;
	}
	
	public double get_writing_speed(){
		long total_size=0;
		long total_time=0;
		for (int i = 0; i < request_log.size(); i++) {
			time_tool tt =new time_tool();
			String type =request_log.get(i)[0];
			long size = Long.valueOf(request_log.get(i)[3])-Long.valueOf(request_log.get(i)[2]);
			long time = tt.get_time_gap(request_log.get(i)[4], request_log.get(i)[5]);
			if(type.contains("CREATE")||type.contains("APPEND")||type.contains("SEQ_WRITE")||type.contains("RANDOM_WRITE")){
				total_size += size;
				total_time += time;
			}
		}
		double writing_speed =  total_size/total_time;
//		System.out.println("total_size : "+total_size);
//		System.out.println("total_time : "+total_time);
		return writing_speed;
	}
	
	public double get_lookup_time(){
		long total_time=0;
		int total_ls_request=0;
		for (int i = 0; i < request_log.size(); i++) {
			time_tool tt =new time_tool();
			String type =request_log.get(i)[0];
			long time = tt.get_time_gap(request_log.get(i)[4], request_log.get(i)[5]);
			if(type.contains("LS")){
				total_time += time;
				total_ls_request++;
			}
		}
		double lookup_time =  total_time/total_ls_request;
		return lookup_time;
	}
	
	
	public double get_throughput(){
		time_tool tt =new time_tool();
		long total_size=0;
		long start_time=tt.get_time_long(request_log.get(0)[4]);
		long end_time = tt.get_time_long(request_log.get(0)[5]);
		String start_time_string =request_log.get(0)[4];
		String end_time_string =request_log.get(0)[5];
		
		for (int i = 0; i < request_log.size(); i++) {
			long size = Long.valueOf(request_log.get(i)[3])-Long.valueOf(request_log.get(i)[2]);
			if(size>0){
			String type =request_log.get(i)[0];
			if(tt.get_time_long(request_log.get(i)[4])<start_time){
				start_time =tt.get_time_long(request_log.get(i)[4]);
				start_time_string=request_log.get(i)[4];
			}
			if(tt.get_time_long(request_log.get(i)[5])>end_time){
				end_time =tt.get_time_long(request_log.get(i)[5]);
				end_time_string=request_log.get(i)[5];
			}
 			total_size += size;
			}
		}
//		System.out.println("start_time : "+start_time_string+" end_time : "+end_time_string);
//		System.out.println("total_size : "+total_size);
		long total_time = tt.get_time_gap(start_time_string, end_time_string);
//		System.out.println("total_size : "+total_size);
//		System.out.println("total_time : "+total_time);
		double throughput = total_size / total_time;
		return throughput;
	}
	
}
