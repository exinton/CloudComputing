/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

/**
 *
 * @author vivek
 */
import java.util.Scanner;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import cephmapnode.CephMap;
import cephmapnode.CephNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import types.MonitorMsgType;
import wrapper.Wrapper;
import wrapper.WrapperUtils;
import net.Address;
import net.IOControl;
import net.Session;
import org.ini4j.Wini;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
@SuppressWarnings("serial")
public class Manager implements java.io.Serializable {
    
    static private Wrapper wrapper;
    
    public synchronized static void setWrapper(Wrapper wrapper) {
		Manager.wrapper = wrapper;
	}
    public  static Wrapper getWrapper() {
 		return wrapper;
 	}

	static void callMON(ArrayList<CephNode> exchange_info, MonitorMsgType msgType) {
        try {
        	
            final  String CEPH_HOME = System.getenv("CEPH_HOME");
            
            if(CEPH_HOME == null){
                throw new Exception("CEPH_HOME not set");
            }
            
            final String MANAGER_PROPERTIES_FILE = CEPH_HOME+File.separator+"conf"+File.separator+"manager.properties";
            Properties prop = new Properties();
            InputStream input = new FileInputStream(MANAGER_PROPERTIES_FILE);
            prop.load(input);
            
            String serverIP;
            int serverPort;
            
            IOControl control = new IOControl();
            Session session = new Session(msgType);
            session.set("exchange_info", exchange_info);
            
            String monitor_addresses = prop.getProperty("CEPH_MONITORS");
            
            String[] addressList = monitor_addresses.split(",");
            for (int i = 0; i < addressList.length; i++) {
                String[] tokens = addressList[i].split(":");
                if (tokens.length == 2) {
                    try {
                        serverIP = tokens[0];
                        serverPort = Integer.parseInt(tokens[1]);
                        
                        Session ping = control.request(session,serverIP,serverPort);
                        String message = ping.getString("message");
                        System.out.println(message);
                        
                    } catch (Exception e) {
                        System.out.println("Monitor " + addressList[i] + " not responding. Trying next Monitor");
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        simulateMON(modify_path, msgType);
    }

    
    
    static synchronized boolean add() {
        Scanner sc = new Scanner(System.in);
        System.out.println("in adding a new node");
            System.out.println();
            System.out.print("new physical node ip:port: ");
            String id = sc.next();
            getWrapper().addRealServer(id);
            WrapperUtils.rebalanceWrapper(wrapper);
            System.out.println("adding physical node and rebalance is done!");
            return WrapperUtils.uploadWrapper(wrapper);	//update wrapper to monitor

    }

    static boolean delete() {
        System.out.println("in deleting a node");

        System.out.print("Specify the Ip and port of the node to be deleted: ");
        Scanner sc = new Scanner(System.in);
        String rawip = sc.next();        
        getWrapper().removeRealServer(rawip);
        WrapperUtils.rebalanceWrapper(getWrapper());
        System.out.println("removing physical node and rebalance is done!");
        return WrapperUtils.uploadWrapper(wrapper);	//update wrapper to monitor

    }
    
    static void initiateLoadBalancing(){
        System.out.println("in initiate load balancing by the manager");
        
        ArrayList<CephNode> modify_path = new ArrayList<>();
        /*
        System.out.print("Specify the ID of the overloaded node: ");
        CephNode nd = new CephNode();
        
        Scanner sc = new Scanner(System.in);
        String id = sc.next();
        nd.setId(id);
        modify_path.add(nd);
        */

        System.out.println();
        callMON(modify_path, MonitorMsgType.INITIATE_LOAD_BALANCING);
    }
    
    static boolean initWrapper(){
    	 Properties prop = new Properties();
    	 String dir = System.getProperty("user.dir");
 		 String confFile=dir+File.separator+"conf"+File.separator+"manager.conf";
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
 		Address monitorAddr= new Address(ip[0],Integer.valueOf(ip[1]));      
 		String fileName=dir+File.separator+"wrapper.json";
 		File file = new File(fileName);
 		WrapperUtils.setMonitorAddr(monitorAddr);	
 		WrapperUtils.setJsonFile(file);
 		setWrapper(WrapperUtils.downloadWrapper());
    	return true;
     }
    

    static void operations() {
        boolean acceptInput = true;
        initWrapper();

        Scanner sc = new Scanner(System.in);
        while (acceptInput) {
            int option;
            System.out.println();
            System.out.println("Choose any operation to perform:");
            System.out.println("1. add a node");
            System.out.println("2. delete a node");
//            System.out.println("3. initiate load balancing");
            System.out.println("3. display cluster map");
            System.out.println("4. exit");

            System.out.println();
            System.out.print("option: ");
            option = sc.nextInt();

            switch (option) {
                case 1:
                    if(add())
                    	System.out.println("upload to monitor successfully!");
                    else
                    	System.out.println("upload to monitor failed!");
                    break;
                case 2:
                    delete();
                    break;
                case 3:                	
                    getWrapper().printLayOut();
                    break;
                default:
                    acceptInput = false;
            }
        }
        sc.close();
    }

  

    public static void main(String[] args) {

        System.out.println("Program started...");
        operations();

        System.out.println("Program completed.");
    }
}