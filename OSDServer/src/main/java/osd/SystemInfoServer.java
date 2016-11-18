package osd;

import java.io.File;
import java.io.IOException;
import net.IOControl;
import net.MsgHandler;
import net.Session;
import types.SystemInfoMsgType;
import util.Log;

public class SystemInfoServer {
	private static Log log=Log.get();
	static class InfoServer implements MsgHandler{
		private IOControl control;

		public InfoServer(IOControl control){
			this.control = control;
		}
		public static void proc(Session session) throws InterruptedException, IOException{
			
		}
		@Override
		public boolean process(Session session) throws IOException {
			 Session error = new Session(SystemInfoMsgType.SYSTEM_INFO_ERROR);
			 
			 try{
				 SystemInfoMsgType type = (SystemInfoMsgType) session.getType();
				 
				 switch(type){
				 
				 case LIST_FILES:{
					 	final  String CEPH_HOME = System.getenv("CEPH_HOME");
					 	OSDProperty osd = OSDProperty.getInstance(CEPH_HOME);
					 	File folder = new File(osd.getCEPH_DATA_DIR());
					 	File[] listOfFiles = folder.listFiles();
					 	String result = "";
					 	for (File file : listOfFiles) {
					 	    if (file.isFile()) {
					 	        result += file.getName()+",";
					 	    }
					 	}
					 	Session response = new Session(SystemInfoMsgType.LIST_FILES);
					 	response.set("files", result);
					 	control.response(response, session);
					 break;
				 }
				 case GET_READ_COUNTER:{
						 Session response = new Session(SystemInfoMsgType.GET_READ_COUNTER);
						 response.set("readCounter",OSDGlobalParameters.getReadRequestCounter());
						 control.response(response,session);
					 break;
				 }
				 case GET_WRITE_COUNTER:{
							 Session response = new Session(SystemInfoMsgType.GET_WRITE_COUNTER);
							 response.set("writeCounter",OSDGlobalParameters.getWriteRequestCounter());
							 control.response(response,session);
						 break;
					 }
				 case GET_READ_WRITE_LOAD:{
							 Session response = new Session(SystemInfoMsgType.GET_READ_WRITE_LOAD);
							 int total = OSDGlobalParameters.getReadRequestCounter()+OSDGlobalParameters.getWriteRequestCounter();
							 response.set("totalCounter",total);
							 control.response(response,session);
						 break;
					 }
				 case IS_ALIVE:{
					 Session response = new Session(SystemInfoMsgType.IS_ALIVE);
					 response.set("noFiles", OSDGlobalParameters.getFileStats().size());
					 response.set("size", OSDGlobalParameters.getDiskUsed());
					 response.set("noRequests", (OSDGlobalParameters.getReadRequestCounter()+OSDGlobalParameters.getWriteRequestCounter()));
					 control.response(response, session);					 
					 break;
				 }
                                 case FILE_EXIST:{
                                     Session response = new Session(SystemInfoMsgType.FILE_EXIST);
                                     System.out.println( OSDGlobalParameters.getFileStats().keySet() );
                                     if(OSDGlobalParameters.getFileStats().containsKey(session.getString("fileName").trim()))
                                         response.set("fileExist",true);
                                     else
                                         response.set("fileExist",false);
                                     control.response(response, session);
                                     break;
                                 }
			/*	 case GET_SYSTEM_LOAD:{
					 OperatingSystemMXBean os=ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
						Runtime runtime=Runtime.getRuntime();
							 Session response = new Session(SystemInfoMsgType.GET_SYSTEM_LOAD);
							 response.set("writeCounter",OSDGlobalParameters.getWriteRequestCounter());
							 control.response(response,session);
						 break;
					 }
				 case GET_PROCESS_LOAD:{
						 break;
					 }
				 case GET_FREE_MEMORY:{
						 break;
					 }
				 case GET_FREE_SPACE:{
						 break;
					 }*/
						default:{
							Session response = new Session(SystemInfoMsgType.SYSTEM_INFO_ERROR);							 
							 response.set("message","Unknown Command");
							 control.response(response,session);
							break;
						}
						 
				 }
				 
			 } catch (Exception e) {
	                log.w(e);
	                error.set("message", e.getMessage());
	                control.response(error, session);
	            }
			return false;
		}
	}
}
