package dhtAPI;


import api.RequestAdapterAPI;
import req.Request;
import tools.log_writer;
import tools.request_convertor;
import tools.time_tool;
import newTester.TestClient;
public class DHTAPI implements RequestAdapterAPI {
	log_writer lw;
	TestClient client;
	public DHTAPI(log_writer lw){
		this.lw=lw;
		client= new TestClient();
		
	}
		
	public synchronized void transfer(Request request) {
		run(request);
	}
	
	public void run(Request request) {
		time_tool tt = new time_tool();
		String start_time = tt.get_time_msec();
		
		try {
			if (request.type==Request.ReqType.SEQ_READ) {
				//client.read(request);
			} else if (request.type==Request.ReqType.DELETE) {
				//client.read(request);
		
			} else if (request.type==Request.ReqType.RMDIR) {
				
				//client.read(request);
			}  else if (request.type==Request.ReqType.CREATE_FILE) {
				//client.read(request);
	
			} else if (request.type==Request.ReqType.CREATE_DIR) {
				//client.read(request);
			} else if (request.type==Request.ReqType.LS) {
				client.read(request);
			}
			String end_time = tt.get_time_msec();
			lw.write("Success : "+request+","+start_time+","+end_time);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String end_time = tt.get_time_msec();
			lw.write("Fail : "+request+","+start_time+","+end_time);
			lw.write("Reason : "+e.toString());
			
		}

	}
	
	

}
