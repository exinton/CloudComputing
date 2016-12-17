package cephrados;

import RESTAPI.OPENSTACKClientBase;
import api.RequestAdapterAPI;
import req.Request;
import tools.log_writer;
import tools.time_tool;

public class CephRados  implements RequestAdapterAPI{
	log_writer lw;
	RadosClientBase radosClient;
	public CephRados(log_writer lw){
		this.lw=lw;
		radosClient = new RadosClientBase();
	}
	
	public synchronized void transfer(Request request) {
		run(request);
	}
	
	public void run(Request request) {

		
		time_tool tt = new time_tool();
		String start_time = tt.get_time_msec();
		
		try {
			if (request.type==Request.ReqType.SEQ_READ) {
				request.start=0;
				request.end=radosClient.read(request);
			}else if(request.type==Request.ReqType.RANDOM_READ){
				request.end=radosClient.read(request);
			}else if(request.type==Request.ReqType.SEQ_WRITE){
				request.start=0;
				request.end=radosClient.randomWrite(request);				
			}else if(request.type==Request.ReqType.RANDOM_WRITE){
				request.end=radosClient.read(request);
			}else if (request.type==Request.ReqType.DELETE) {
				radosClient.force_delete_file(request);		
			}  else if (request.type==Request.ReqType.CREATE_FILE) {
				radosClient.create_file(request);
	
			} else if (request.type==Request.ReqType.LS) {
				//radosClient.ls(request);
				
			}else if (request.type==Request.ReqType.APPEND) {
				//radosClient.append(request);
				
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
