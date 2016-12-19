package test;

import java.io.IOException;

import sample.Request.ReqGenerator;
import tools.time_tool;

public class request_generator_test {
public static void main(String[] args) {
	time_tool tt =new time_tool(); 
	String server_type = "HDFS";
	String request_log = "log/Request_"+server_type+"_"+tt.get_time_stamp()+".log"; 
 
 	ReqGenerator rg =new ReqGenerator();
	try {
		rg.run_request_generator(server_type, request_log);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println("Finished");
	System.out.println(request_log);
 }
}
 