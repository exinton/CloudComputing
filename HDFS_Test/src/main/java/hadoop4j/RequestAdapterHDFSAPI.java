package hadoop4j;

import api.RequestAdapterAPI;
import tools.log_writer;
import tools.request_convertor;
import tools.time_tool;

public class RequestAdapterHDFSAPI implements RequestAdapterAPI{
	log_writer lw;
	
	public RequestAdapterHDFSAPI(log_writer lw){
		this.lw=lw;
	}
	public synchronized void transfer(String request) {
		String[] command = commandType(request);
		run(command,request);
	}
	/**
	 * @param originalRequests
	 * @return comand[0] command type comand[1] file_path comand[2] offset start
	 *         comand[3] offset end
	 */
	public String[] commandType(String request) {
		request_convertor rc = new request_convertor();
		String[] comand = rc.convert(request);
		return comand;
	}

	public void run(String[] command,String request) {
		String command_type = command[0];
		String file_path = command[1];
		String offset_start = command[2];
		String offset_end = command[3];
		HDFS_Client hdfs = new HDFS_Client();
		time_tool tt = new time_tool();
		String start_time = tt.get_time_msec();
		try {
			long size = Long.valueOf(offset_end) - Long.valueOf(offset_start);
			if (command_type.startsWith("RANDOM_READ")) {
				hdfs.ran_read(file_path, offset_start, offset_end);
			} else if (command_type.startsWith("RANDOM_WRITE")) {
				hdfs.ran_write(file_path);
			} else if (command_type.startsWith("SEQ_READ")) {
				hdfs.seq_read(file_path, offset_start, offset_end);
			} else if (command_type.startsWith("SEQ_WRITE")) {
				hdfs.seq_write(file_path);
			} else if (command_type.startsWith("DELETE")) {
				hdfs.force_delete(file_path);
			} else if (command_type.startsWith("RMDIR")) {
				hdfs.force_delete(file_path);
			} else if (command_type.startsWith("APPEND")) {
				hdfs.append(file_path, size);
			} else if (command_type.startsWith("CREATE_FILE")) {
				hdfs.create_file(file_path, size);
			} else if (command_type.startsWith("CREATE_DIR")) {
				hdfs.create_dir(file_path);
			} else if (command_type.startsWith("LS")) {
				hdfs.ls(file_path);
			}
			String end_time = tt.get_time_msec();
			lw.write("Success : "+request+","+start_time+","+end_time);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			String end_time = tt.get_time_msec();
			lw.write("Fail : "+request+","+start_time+","+end_time);
			lw.write("Reason : "+e.toString());
			
		}
	}
}
