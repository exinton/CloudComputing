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
import types.WrapperMsgType;
import wrapper.WrapperHolder;
import wrapper.WrapperUtils;

public class TestClinet {
	 public  IOControl control=null;
	 private  Address monitorAddr;
	 private  WrapperHolder wrapperHolder=new WrapperHolder();
	 
	
	
	public TestClinet(){
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
    
 public  boolean readFile(String file){
        
    	if(wrapperHolder.getWrapper()==null){
    		wrapperHolder.setWrapper(WrapperUtils.downloadWrapper());
    		if(wrapperHolder.getWrapper()==null){
    			System.out.println("failed to update wrapper with monitor");                            			
    			return false;
    		}                            			
    	}
    	List<Address> fileLocations=wrapperHolder.getWrapper().searchFile(file, "read");       
    	
    	//check version
    	int numberOfWorkingOSD=0;
    	for (Address fileLocation : fileLocations) { 
    		int queryResult=query(file,fileLocation);  
    		if(queryResult==-1){
    			System.out.println("can not find file "+file+"on osd server"+fileLocation.getIp()+":"+fileLocation.getPort());
            	System.out.println("download the latest wrapper from monitor!");
            	wrapperHolder.setWrapper(WrapperUtils.downloadWrapper());
            	numberOfWorkingOSD++;
    		}                            		
    	}
    	//if none osd is working, may need to download wrapper from monitor.
        if(numberOfWorkingOSD==0){
        	return false;
        }
        
    	fileLocations=wrapperHolder.getWrapper().searchFile(file, "read");
    	int foundFile=0;
        for (Address fileLocation : fileLocations) {                            
            System.out.println("search wrapper structure, find file:" + file + " at " + fileLocation.getIp()+":"+fileLocation.getPort());                                                                                                      
            	int queryResult=query(file,fileLocation);                                      
                if(queryResult==1){
                	System.out.println("find file:"+file+" on osd server"+fileLocation.getIp()+":"+fileLocation.getPort());
                	foundFile++;
                }
        }
        
        return foundFile>0;
        
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
