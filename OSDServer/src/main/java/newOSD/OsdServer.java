package newOSD;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import net.Address;
import net.IOControl;
import net.MsgHandler;
import types.WrapperMsgType;
import wrapper.Wrapper;
import wrapper.WrapperHolder;
import wrapper.WrapperUtils;

import wrappernet.WrapperMsgHandler;

public class OsdServer {
    private static Address monitorAddr;
    private static WrapperHolder wrapper=new WrapperHolder();
    private static IOControl control;
	private	static String ipaddr;
	private	static int ipport;
	 
    public static boolean init(){
    	
   	   Properties prop = new Properties();
   	   String dir = System.getProperty("user.dir");
		 String confFile=dir+File.separator+"conf"+File.separator+"osd.conf";
        InputStream input = null;
        try {
			input = new FileInputStream(confFile);
			prop.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}     
        ipport=Integer.valueOf(prop.getProperty("port"));
        ipaddr=prop.getProperty("ip");
        String rawIP = prop.getProperty("address");       
		String[] ip=rawIP.split(":");
		monitorAddr= new Address(ip[0],Integer.valueOf(ip[1]));      
		String fileName=dir+File.separator+"wrapper.json";
		File file = new File(fileName);
		WrapperUtils.setMonitorAddr(monitorAddr);	
		WrapperUtils.setJsonFile(file);
		wrapper.setWrapper(WrapperUtils.downloadWrapper());
		wrapper.setJsonFile(file);
   	 	return true;
   	 	
    }
    
   
    
    public static void main(String[] args){    	
    	if(!init()){
    		return;
    	} 
    	
    	control = new IOControl();
    	MsgHandler wrapperhandler = new WrapperMsgHandler(control,wrapper);
    	control.registerMsgHandlerHead(wrapperhandler,WrapperMsgType.values());
    	try {
			control.startServer(ipport);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}    
    	control.waitForServer();

    }


}
