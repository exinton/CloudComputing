package performance;

import tools.file_size_convertor;

public class PerformanceParser {
	public static void main(String[] args) {
		performance p =new performance("log/Request_DHT_20161217-12-43-48.log");
		file_size_convertor fsc =new file_size_convertor();	
		p.load_request_log();
		try{
			System.out.println("Task Request_HDFS_20161118-16-55-02.log");
			System.out.println("Reading_speed : "+fsc.readable_file_size(p.get_reading_speed()*1000)+"/s");
			System.out.println("Writing_speed : "+fsc.readable_file_size(p.get_writing_speed()*1000)+"/s");
			System.out.println("Lookup_time : "+p.get_lookup_time()+" ms");
			System.out.println("Throughput : "+fsc.readable_file_size(p.get_throughput()*1000)+"/s");
			System.out.println();
			System.out.println("Task log/Request_HDFS_20161118-17-06-57.log");
			performance p2 =new performance("log/fs_propagate_Ceph_20161126-18-12-19.log");
			p2.load_request_log();
			String reading=fsc.readable_file_size(p2.get_reading_speed()*1000)+"/s";
			String writing=fsc.readable_file_size(p2.get_writing_speed()*1000)+"/s";
			String lookup=p2.get_lookup_time()+" ms";
			String throughput=p2.get_lookup_time()+" ms";
			System.out.println("Reading_speed : "+reading);
			System.out.println("Writing_speed : "+writing);
			System.out.println("Lookup_time : "+lookup);
			System.out.println("Throughput : "+throughput);
			System.out.println("final report:"+reading+";"+writing+";"+throughput+";"+lookup);
		}catch (ArithmeticException e){
			e.printStackTrace();
		}
	}
	public static void parse(String performanceLogFile){
		performance p =new performance(performanceLogFile);
		file_size_convertor fsc =new file_size_convertor();	
		p.load_request_log();
		try{
			System.out.println("Task Request_HDFS_20161118-16-55-02.log");
			System.out.println("Reading_speed : "+fsc.readable_file_size(p.get_reading_speed()*1000)+"/s");
			System.out.println("Writing_speed : "+fsc.readable_file_size(p.get_writing_speed()*1000)+"/s");
			System.out.println("Lookup_time : "+p.get_lookup_time()+" ms");
			System.out.println("Throughput : "+fsc.readable_file_size(p.get_throughput()*1000)+"/s");
			System.out.println();
			System.out.println("Task log/Request_HDFS_20161118-17-06-57.log");
			performance p2 =new performance("log/fs_propagate_Ceph_20161126-18-12-19.log");
			p2.load_request_log();
			System.out.println("Reading_speed : "+fsc.readable_file_size(p2.get_reading_speed()*1000)+"/s");
			System.out.println("Writing_speed : "+fsc.readable_file_size(p2.get_writing_speed()*1000)+"/s");
			System.out.println("Lookup_time : "+p2.get_lookup_time()+" ms");
			System.out.println("Throughput : "+fsc.readable_file_size(p2.get_throughput()*1000)+"/s");
		}catch (ArithmeticException e){
			e.printStackTrace();
		}
		
	}
}
