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
import wrapper.WrapperUtils;
import net.IOControl;


/**
 *
 * @author balanivash
 */
public class Client {

    private static final Log log = Log.get();
    public static IOControl control=null;
    public static CrushRun crushRun = CrushRun.getInstance();
    public static FileWriter read_log;
    public static FileWriter write_log;
    public static FileWriter lookup_log;
    public static String readLog,writeLog,lookupLog;
    private static Address monitorAddr;
    private static Wrapper wrapper;
 
    public static boolean init(){
    	
   	   Properties prop = new Properties();
   	   String dir = System.getProperty("user.dir");
		 String confFile=dir+File.separator+"conf"+File.separator+"monitor.conf";
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
		wrapper=WrapperUtils.downloadWrapper();
   	 	return true;
    }
    

    


	static List<Address> getLocation(IOControl control, String fileName, String type) {
        MonitorClientInterface.updateCache(control);
       return wrapper.searchReadFile(fileName, type);

    }

    static Address getReadLocation(ArrayList<CephNode> nodeList) {
        return nodeList.get(0).getAddress();
    }


    public static void initTest(){
        CephMap cephMap = new CephMap();
        cephMap.setEpochVal(0L);
        cephMap.setNode(null);
        CephClientGlobalParameters.setCephMap(cephMap);
        Properties prop = new Properties();
        InputStream input = null;
        final String CEPH_HOME = System.getenv("CEPH_HOME");
        try {
            input = new FileInputStream(CEPH_HOME + File.separator + "conf" + File.separator + "client.properties");
            prop.load(input);
            MonitorClientInterface.serverConf = prop.getProperty("CEPH_MONITORS");
            readLog = prop.getProperty("READ_LOG");
            writeLog = prop.getProperty("WRITE_LOG");
            lookupLog = prop.getProperty("LOOKUP_LOG");
           // read_log = new FileWriter(prop.getProperty("READ_LOG"));
           // write_log = new FileWriter(prop.getProperty("WRITE_LOG"));
           // lookup_log = new FileWriter(prop.getProperty("LOOKUP_LOG"));
        }catch(IOException e){
                e.printStackTrace();
        }
    }
    public static void endTest(){
        try{
            read_log.close();
            write_log.close();
            lookup_log.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public static void ClientCMDProcess(){
    	  //   System.out.println("Reading from " + CEPH_HOME + File.separator + "conf" + File.separator + "client.properties");
        System.out.println("Command format : read <filename>");
        System.out.println("Command format : write <filepath>");
        System.out.println("Command format : delete <filename>");
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
                    if (tokens.length > 2) {
                        log.i("Command not valid.");
                        log.i("Valid format : read <filename> \n Valid format : write <filepath>"
                                + "\nValid format : delete <filename>"
                                + "\nValid format : quit");
                    } else {
                        String command = tokens[0].toLowerCase().trim();
                        String file = tokens[1].trim();
                        switch (command) {
                            case "read": {
                            	if(wrapper==null){
                            		if(WrapperUtils.downloadWrapper()==null){
                            			System.out.println("failed to update wrapper with monitor");                            			
                            			log.i("failed to update wrapper with monitor");
                            			break;
                            		}                            			
                            	}
                            	List<Address> fileLocations=wrapper.searchReadFile(file, "read");                                
                                for (Address fileLocation : fileLocations) {
                                    System.out.println("search wrapper structure, find file:" + file + " at " + fileLocation.getIp()+":"+fileLocation.getPort());                                   
                                    log.i("search wrapper, find" + file + " at " + fileLocation.getIp()+":"+fileLocation.getPort());
                                }
                            }
                          
                            case "quit": {
                                break;
                            }
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
