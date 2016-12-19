package test;

import java.io.IOException;

import performance.PerformanceParser;
import sample.Request.FSPropagate;
import sample.Request.ReqGenerator;
import tools.time_tool;

public class fs_propagate_test {
	public static void main(String[] args) {
		time_tool tt =new time_tool();
		String server_type = "Ceph";
		String request_log = "log/fs_propagate_"+server_type+"_"+tt.get_time_stamp()+".log"; 
		String source_file1 = "files/test.txt";
	 	FSPropagate fs_pro =new FSPropagate();
	 	try {
	 		fs_pro.run_fspropagate_generator(server_type, request_log,source_file1);
	 		 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Test1 finished");
		
	
		String request_log2 = "log/fs_propagate_"+server_type+"_"+tt.get_time_stamp()+".log"; 
		String source_file2 = "files/test2.txt";
	 	try {
	 		fs_pro.run_fspropagate_generator(server_type, request_log2,source_file2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Test2 finished");
		System.out.println(request_log);
		System.out.println(request_log2);
		
		System.out.println(request_log);
		PerformanceParser.parse(request_log);
		
		System.out.println(request_log2);
		PerformanceParser.parse(request_log2);

	}
}
