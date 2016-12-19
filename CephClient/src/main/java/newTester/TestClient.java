package newTester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import net.Address;
import net.IOControl;
import net.Session;
import req.Request;
import types.WrapperMsgType;
import wrapper.WrapperHolder;
import wrapper.WrapperUtils;

public class TestClient {
	 public  IOControl control=null;
	 private  Address monitorAddr;
	 private  WrapperHolder wrapperHolder=new WrapperHolder();
	 
	
	
	public TestClient(){
		 Properties prop = new Properties();
	   	   String dir = System.getProperty("user.dir");
			 String confFile=dir+File.separator+"conf"+File.separator+"client.conf";
	        InputStream input = null;
	        try {
				input = new FileInputStream(confFile);
				prop.load(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     
	        String rawIP = prop.getProperty("address");       
			String[] ip=rawIP.split(":");
			this.monitorAddr= new Address(ip[0],Integer.valueOf(ip[1]));     
			WrapperUtils.setMonitorAddr(monitorAddr);	
			long t=100;
			while(wrapperHolder.getWrapper()==null){
			wrapperHolder.setWrapper(WrapperUtils.downloadWrapper());
			try {
				Thread.sleep(t);
				t+=100;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
    public void update(){
    	wrapperHolder.setWrapper(WrapperUtils.downloadWrapper(wrapperHolder.getWrapper()));
    }
    
 public  boolean read(Request	request) throws DHTException{
        
    	if(wrapperHolder.getWrapper()==null){
    		wrapperHolder.setWrapper(WrapperUtils.downloadWrapper());
    		if(wrapperHolder.getWrapper()==null){
    			System.out.println("failed to update wrapper with monitor");   
    			throw new DHTException("no result");
    		
    		}                            			
    	}
    	List<Address> fileLocations=wrapperHolder.getWrapper().searchFile(request.path, "read");       
    	
    	//check version
    	int numberOfWorkingOSD=0;
    	for (Address fileLocation : fileLocations) { 
    		int queryResult=query(request.path,fileLocation);  
    		if(queryResult==-1){
    			System.out.println("can not find file "+request.path+"on osd server"+fileLocation.getIp()+":"+fileLocation.getPort());
            	System.out.println("download the latest wrapper from monitor!"); 
            	numberOfWorkingOSD++;
    		}else if(queryResult==0){
    			wrapperHolder.setWrapper(WrapperUtils.downloadWrapper());
    		}else if(queryResult==1){
    			numberOfWorkingOSD++;
    			return true;
    		}
    	}
    	//if none osd is working, may need to download wrapper from monitor.
        if(numberOfWorkingOSD==0){
        	 throw new DHTException("no result");
        }
        
    	fileLocations=wrapperHolder.getWrapper().searchFile(request.path, "read");
    	int foundFile=0;
        for (Address fileLocation : fileLocations) {                            
            System.out.println("search wrapper structure, find file:" + request.path + " at " + fileLocation.getIp()+":"+fileLocation.getPort());                                                                                                      
            	int queryResult=query(request.path,fileLocation);                                      
                if(queryResult==1){
                	System.out.println("find file:"+request.path+" on osd server"+fileLocation.getIp()+":"+fileLocation.getPort());
                	return true;
                }
        }
        
        throw new DHTException("no result");
        
    }
    
 
 	public  int query(String filename,Address osdAddr){
 	IOControl control = new IOControl(); 
 	Session session = new Session(WrapperMsgType.FILE_VALID);
     session.set("epochVal",wrapperHolder.getWrapper().getEpochVal() );
 	session.set("fileName", filename);
     Session response = null;
		try {
				response = control.request(session, osdAddr);
		} catch (Exception e) {
			System.out.println("Cannot connect to "+osdAddr.getIp()+":"+osdAddr.getPort());
			return 0;
		}
 	return response.getBoolean("isFileValid")?1:-1;

 	}
	public static void main(String[] args){
		
	}
	
}
