package performance;

import tools.file_size_convertor;

public class test {
	public static void main(String[] args) {
		performance p = new performance(
				"log/Request_HDFS_20161216-19-16-03.log");
		file_size_convertor fsc = new file_size_convertor();
 
		p.load_request_log();
		System.out.println("Reading_speed : "
				+ fsc.readable_file_size(p.get_reading_speed() * 1000) + "/s");
		System.out.println("Writing_speed : "
				+ fsc.readable_file_size(p.get_writing_speed() * 1000) + "/s");
		System.out.println("Lookup_time : " + p.get_lookup_time() + " ms");
		System.out.println("Throughput : "
				+ fsc.readable_file_size(p.get_throughput() * 1000) + "/s");
		//p.get_request_distribution();
		System.out.println();

		// performance p2 =new
		// performance("log/fs_propagate_HDFS_20161212-10-17-34.log");
		// p2.load_request_log();
		// System.out.println("Reading_speed : "+fsc.readable_file_size(p2.get_reading_speed()*1000)+"/s");
		// System.out.println("Writing_speed : "+fsc.readable_file_size(p2.get_writing_speed()*1000)+"/s");
		// System.out.println("Lookup_time : "+p2.get_lookup_time()+" ms");
		// System.out.println("Throughput : "+fsc.readable_file_size(p2.get_throughput()*1000)+"/s");
	}
}
