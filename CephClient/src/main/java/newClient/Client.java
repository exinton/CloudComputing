package newClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.Address;

import cephmapnode.CephMap;
import cephmapnode.CephNode;
import client.CephClientGlobalParameters;
import client.MonitorClientInterface;
import crush.CrushRun;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Properties;

import util.Log;
import wrapper.Wrapper;
import wrapper.WrapperHolder;
import wrapper.WrapperUtils;
import net.IOControl;
import net.Session;
import types.MonitorMsgType;
import types.WrapperMsgType;



public class Client {

    private static final Log log = Log.get();
    public static IOControl control=null;
    private static Address monitorAddr;
    private static WrapperHolder wrapperHolder=new WrapperHolder();
 
    public static boolean init(){
    	
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
			return false;
		}     
        String rawIP = prop.getProperty("address");       
		String[] ip=rawIP.split(":");
		monitorAddr= new Address(ip[0],Integer.valueOf(ip[1]));      
		String fileName=dir+File.separator+"wrapper.json";
		File file = new File(fileName);
		WrapperUtils.setMonitorAddr(monitorAddr);	
		WrapperUtils.setJsonFile(file);
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
			
		
   	 	return true;
    }
    
    public static void update(){
    	wrapperHolder.setWrapper(WrapperUtils.downloadWrapper(wrapperHolder.getWrapper()));
    }
    
    public static void readFile(String file){
        
    	if(wrapperHolder.getWrapper()==null){
    		wrapperHolder.setWrapper(WrapperUtils.downloadWrapper());
    		if(wrapperHolder.getWrapper()==null){
    			System.out.println("failed to update wrapper with monitor");                            			
    			log.i("failed to update wrapper with monitor");
    			return;
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
        	System.out.println("none of the OSD to file "+file+" is working, need to download wrapper from monitor!");
        	wrapperHolder.setWrapper(WrapperUtils.downloadWrapper());
        }
        
    	fileLocations=wrapperHolder.getWrapper().searchFile(file, "read");
    	
        for (Address fileLocation : fileLocations) {                            
            System.out.println("search wrapper structure, find file:" + file + " at " + fileLocation.getIp()+":"+fileLocation.getPort());                                                                                                      
            	int queryResult=query(file,fileLocation);                                      
                if(queryResult==1){
                	System.out.println("find file:"+file+" on osd server"+fileLocation.getIp()+":"+fileLocation.getPort());                                        	
                }
        }
    }

    public static int query(String filename,Address osdAddr){
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


	static List<Address> getLocation(IOControl control, String fileName, String type) {
        MonitorClientInterface.updateCache(control);
       return wrapperHolder.getWrapper().searchFile(fileName, type);

    }

    static Address getReadLocation(ArrayList<CephNode> nodeList) {
        return nodeList.get(0).getAddress();
    }


    
    public static void ClientCMDProcess(){
    	  //   System.out.println("Reading from " + CEPH_HOME + File.separator + "conf" + File.separator + "client.properties");
        System.out.println("Command format : read <filename>");
        System.out.println("Command format : update  wrapper from monitor");
        System.out.println("Command format : print  print routing table");
        System.out.println("Command format : quit");
        //Utils.connectToLogServer(log);
        try {
        
            Scanner in = new Scanner(System.in);
            for (;;) {
                System.out.println("\n\n Ceph File System. Enter Command");
                String cmd = in.nextLine();
                if (cmd.length() > 0) {
                    String line = cmd.trim();
                    String[] tokens = line.split("\\s");
                    if (tokens.length >3 ) {
                        log.i("Command not valid.");
                        log.i("Valid format : read <filename> \n Valid format : write <filepath>"
                                + "\nValid format : delete <filename>"
                                + "\nValid format : quit");
                    } else {
                        String command = tokens[0].toLowerCase().trim();
 
                        switch (command) {
                            case "read": {
                                String file = tokens[1].trim();
                                readFile(file);
                            }

                            break;
                            case "quit": {
                                break;
                            }
                            case "update":{                
                            	update();
                            }
                            break;
                            case "print":{                
                            	wrapperHolder.getWrapper().printWrapperLayOut();
                            }
                            break;
                            default: {
                                log.i("Command not valid.");
                                log.i("Valid format : read <filename> \n Valid format : write <filepath>"
                                        + "\nValid format : delete <filename>"
                                        + "\nValid format : quit");
                            }
                        }
                        if (command.equalsIgnoreCase("quit")) {
                            break;
                        }
                    }

                }
            }
            in.close();
        } catch (Exception e) {
            log.w(e);
        }
    }

    

    public static void main(String args[]) {
    	if(!init()){
    		return;
    	} 
    	ClientCMDProcess();

//        catch(IOException e){
//		log.w(e);
    }
}
