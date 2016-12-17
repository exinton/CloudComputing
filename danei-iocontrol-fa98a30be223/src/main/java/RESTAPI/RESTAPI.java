package RESTAPI;


import api.RequestAdapterAPI;
import req.Request;
import tools.log_writer;
import tools.request_convertor;
import tools.time_tool;


public class RESTAPI implements RequestAdapterAPI {
	log_writer lw;
	String systype;	
	public RESTAPI(log_writer lw,String systype){
		this.lw=lw;
		this.systype=systype;
		
	}
		
	public synchronized void transfer(Request request) {
		run(request);
	}
	
	public void run(Request request) {
		long start = request.start;
		long end = request.end;
		time_tool tt = new time_tool();
		String start_time = tt.get_time_msec();
		OPENSTACKClientBase openstack = new OPENSTACKClientBase(systype);	
		
		try {
			if (request.type==Request.ReqType.SEQ_READ) {
				request.end=openstack.read(request);
			} else if (request.type==Request.ReqType.DELETE) {
				openstack.force_delete_file(request);
		
			} else if (request.type==Request.ReqType.RMDIR) {
				openstack.force_delete_dir(request);

			}  else if (request.type==Request.ReqType.CREATE_FILE) {
				openstack.create_file(request);
	
			} else if (request.type==Request.ReqType.CREATE_DIR) {
				openstack.create_dir(request);

			} else if (request.type==Request.ReqType.LS) {
				//openstack.ls(request);
				//throw new NoImplementionException("not implemented ls");
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
