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
import wrapper.Wrapper;
import wrapper.WrapperHolder;
import wrapper.WrapperUtils;
import net.Address;
import net.IOControl;
import net.Session;
import types.MonitorMsgType;
import types.WrapperMsgType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
@SuppressWarnings("serial")
public class Manager implements java.io.Serializable {
    
    static private WrapperHolder wrapperHolder= new WrapperHolder();
    
    public synchronized static void setWrapper(Wrapper wrapper) {
    	wrapper.initPriorityQueue();
    	wrapperHolder.setWrapper(wrapper);
	}
    public  static Wrapper getWrapper() {
 		return wrapperHolder.getWrapper();
 	}


    
    static synchronized boolean add() {
        Scanner sc = new Scanner(System.in);
        System.out.println("in adding a new node");
            System.out.println();
            System.out.print("new physical node ip:port: ");
            String id = sc.next();
            getWrapper().addRealServer(id);
            WrapperUtils.rebalanceWrapperByVolume(wrapperHolder.getWrapper());
            System.out.println("adding physical node and rebalance is done! current version"+wrapperHolder.getWrapper().getEpochVal());
            return WrapperUtils.uploadWrapper(wrapperHolder.getWrapper());	//update wrapper to monitor

    }
    
    static synchronized boolean immediateAdd(){
    	if(add() && multicast())
    		return true;
    	return false;
    }
    
    static synchronized boolean immediateDelete(){
    	if(delete() && multicast())
    		return true;
    	return false;
    }
    
    static boolean multicast(){
    	IOControl control = new IOControl(); 
 		Session session = new Session(WrapperMsgType.MULTI_CAST);
 		Session response=null;
 		try {
 			response = control.request(session,WrapperUtils.getMonitorAddr());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
 		if(response==null)
 			return false;
 		else
 			return true;
    }
    

    static boolean delete() {
        System.out.println("in deleting a node");

        System.out.print("Specify the Ip and port of the node to be deleted: ");
        Scanner sc = new Scanner(System.in);
        String rawip = sc.next();        
        getWrapper().removeRealServer(rawip);
        WrapperUtils.rebalanceWrapperByVolume(getWrapper());
        System.out.println("removing physical node and rebalance is done!");
        return WrapperUtils.uploadWrapper(wrapperHolder.getWrapper());	//update wrapper to monitor

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
            System.out.println("5. print cephmap");
            System.out.println("6. print cephmap to physical mapping");
            System.out.println("7. immediate add node");
            System.out.println("8. immediate delete node");
            System.out.println("11. rebalance by volume");
            System.out.println("12. redistributed virtual nodes from physical nodes by load");
            System.out.println("13. immediate upload wrapper to monitor");
            System.out.println("14. set node fail");
            System.out.println("15. set node not fail");
            System.out.println("16. set node overload");
            System.out.println("17. set node not overload");
            System.out.println("18. immediate broadcasting wrapper");   
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
                    getWrapper().printWrapperLayOut();
                    break;
                case 5:
                	getWrapper().printCephMapLayOut();
                	break;
                case 6:
                	getWrapper().printCephMapLayOut(wrapperHolder.getWrapper());
                	break;
                case 7:
                	if(immediateAdd())
                		System.out.println("immediate added node");
                	break;
                case 8:
                	if(immediateDelete())
                		System.out.println("immediate delete node");
                	break;        
                case 12:
                	if(redistributeVirtualNodes());
                	System.out.println("wrapper rebalance by volume done");
                	break;
                case 11:
                	if(rebalanceByVolume());
                	System.out.println("redistributed virtual nodes from physical nodes by load");
                	break;
                case 13:
                	long start1=System.currentTimeMillis();
                	if(WrapperUtils.uploadWrapper(wrapperHolder.getWrapper()))
                		System.out.println("latest wrapper uploaded to monitor");
                	if(multicast()){
                		long curr1=System.currentTimeMillis();
                		System.out.println("broadcasted! take "+(curr1-start1)+"ms");
                	}else{
                		System.out.println("failed");
                	}
                	break;
                	
                case 14:
                	if(setFailNode());
                	System.out.println("set physical node fail");
                	break;
                case 15:
                	if(setNotFailNode());
                	System.out.println("set physical node not fail");
                	break;
                case 16:
                	if(setOverLoadNode());
                	System.out.println("set physical node overload");
                	break;
                case 17:
                	if(setNodeNotOverLoad());
                	System.out.println("set physical node not overload");
                	break;
                case 18:
                	long start=System.currentTimeMillis();
                	if(multicast()){
                		long curr=System.currentTimeMillis();
                		System.out.println("broadcasted! take "+(curr-start)+"ms");
                	}else{
                		System.out.println("failed");
                	}
                	break;
                default:
                    acceptInput = false;
            }
        }
        sc.close();
    }


	private static boolean setOverLoadNode() {
		Scanner sc = new Scanner(System.in);
        System.out.println("set osd node overload");
        System.out.println();
        System.out.print("overloaded physical node ip:port");
        String id = sc.nextLine();
        return getWrapper().setFakeServerListOverLoad(id);    
	}
	private static boolean setFailNode() {
    	Scanner sc = new Scanner(System.in);
        System.out.println("set osd node fail");
        System.out.println();
        System.out.print("failed physical node ip:port");
        String id = sc.nextLine();
        return getWrapper().setFakeServerListFail(id);        
	}
	
	private static boolean setNotFailNode() {
    	Scanner sc = new Scanner(System.in);
        System.out.println("set osd node fail");
        System.out.println();
        System.out.print("failed physical node ip:port");
        String id = sc.nextLine();
        return getWrapper().setFakeServerListNotFail(id);
	}
    
    private static boolean setNodeNotOverLoad() {
    	Scanner sc = new Scanner(System.in);
        System.out.println("set osd node OK");
        System.out.println();
        System.out.print("back to normal physical node ip:port + load ");
        String id = sc.nextLine();
        return getWrapper().setFakeServerListNotOverLoad(id);
	}
    
	private static boolean rebalanceByVolume() {
		if(!WrapperUtils.rebalanceWrapperByVolume(getWrapper()))
			return false;
		else
			return true;
	}
	
	
	private static boolean redistributeVirtualNodes() {
		Scanner sc = new Scanner(System.in);
        System.out.println("move n nodes from the overloaded server");
        System.out.println();
        System.out.print(" overloaded node rawip numberofvirtualnodes to be removed ");
        String id = sc.nextLine();		
		String[] input = id.split("\\s+");
		return WrapperUtils.rebalanceWrapperByLoad(input[0], getWrapper(),Integer.valueOf(input[1]));
	}
	
    
    
    
	public static void main(String[] args) {

        System.out.println("Program started...");
        operations();

        System.out.println("Program completed.");
    }
}