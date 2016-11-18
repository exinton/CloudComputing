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
    
    static CephMap cm;
    
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

    /*
    this is post order traversal of CephMap and updating the weights
    */
    static NodeWeight updateWeights(CephNode n){
        if(n.getIsDisk()){
            return (new NodeWeight(n.getWeight(), 
                                    n.getHiddenWeight()));
                    
        } else {
            NodeWeight nw = new NodeWeight(0, 0);
            for(CephNode i: n.getChildren()){
                NodeWeight childWeight = updateWeights(i);
                nw.weight += childWeight.weight;
                nw.hiddenWeight += childWeight.hiddenWeight;
            }
            
            n.setWeight(nw.weight);
            n.setHiddenWeight(nw.hiddenWeight);

            return nw;
        }
        
    }
    
    static class NodeWeight{
        double weight;
        double hiddenWeight;
        
        NodeWeight(){
            
        }
        
        NodeWeight(double weight, double hiddenWeight){
            this.weight = weight;
            this.hiddenWeight = hiddenWeight;
        }
    }
    
    static void add() {
        Scanner sc = new Scanner(System.in);
        System.out.println("in adding a new node");

        System.out.println("Specify path from root for adding new node");
        ArrayList<CephNode> add_path = new ArrayList<>();
	// EXISTING internal: id
        // NEW internal: id:0; type; isDisk
        // NEW leaf: id:0; weight; type; ip; port; drive_path; isDisk

        boolean keepAdding = true;
        while (keepAdding) {
            CephNode nd = new CephNode();
            System.out.println();
            System.out.print("node id(0 for new): ");
            String id = sc.next();
            nd.setId(id);
            if (id.equals("0")) {
                System.out.print("disk node(0|1)? ");
                int inp = sc.nextInt();
                if (inp == 0) {
                    nd.setIsDisk(false);

                    System.out.println("Enter values for below parameters:");
                    System.out.print("type: ");
                    String type = sc.next();

                    nd.setType(type);
                } else {
                    nd.setIsDisk(true);

                    System.out.println("Enter values for below parameters:");
                    System.out.print("weight: ");
                    double weight = sc.nextDouble();
                    System.out.print("type: ");
                    String type = sc.next();
                    System.out.print("ip: ");
                    String ip = sc.next();
//                    int port = 0;
                    System.out.print("port: ");
                    int port = sc.nextInt();
//                    System.out.print("drive path: ");
//                    String drive_path = sc.next();

                    nd.setWeight(weight);
                    nd.setType(type);
                    nd.setAddress(ip, port);
//                    nd.setDriveInfo(drive_path);
                    
                    keepAdding = false;
                }
            }

            add_path.add(nd);

            if(id.equals("0") && keepAdding){
                System.out.print("add another node in the path?(0|1): ");
                int continueAdding = sc.nextInt();
                if (continueAdding == 0) {
                    keepAdding = false;
                }
            }
        }

//        System.out.println(add_path);
        callMON(add_path, MonitorMsgType.MODIFY_MAP_ADD);
//        sc.close();
    }

    static void delete() {
        System.out.println("in deleting a node");

        System.out.print("Specify the ID of the node to be deleted: ");
        ArrayList<CephNode> delete_path = new ArrayList<>();
        CephNode nd = new CephNode();
        Scanner sc = new Scanner(System.in);
        String id = sc.next();

        nd.setId(id);

        delete_path.add(nd);

        System.out.println();
        callMON(delete_path, MonitorMsgType.MODIFY_MAP_REMOVE);
        
//        sc.close();
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

    static CephMap getClusterMapFromMON() {
        CephMap cm = null;
        try{
            final  String CEPH_HOME = System.getenv("CEPH_HOME");
            
            if(CEPH_HOME == null){
                throw new Exception("CEPH_HOME not set");
            }
            
            final String MANAGER_PROPERTIES_FILE = CEPH_HOME+File.separator+"conf"+File.separator+"manager.properties";
//            System.out.println(MANAGER_PROPERTIES_FILE);
            Properties prop = new Properties();
            InputStream input = new FileInputStream(MANAGER_PROPERTIES_FILE);
            prop.load(input);
            
            IOControl control = new IOControl();
            
            String monitor_addresses = prop.getProperty("CEPH_MONITORS");
            String[] addressList = monitor_addresses.split(","); //return all monitors 
            for (int i = 0; i < addressList.length; i++) {
                String[] tokens = addressList[i].split(":");
                if (tokens.length == 2) {	//if two monitors
                    try {
                        Session updateSession = new Session(MonitorMsgType.CACHE_GET);
//                        System.out.println(tokens[0]+" "+tokens[1]);
                        Session updateResponse = control.request(updateSession, tokens[0], Integer.parseInt(tokens[1]));
//                        System.out.println(tokens[0]+" "+tokens[1]);
                        if (updateResponse.getType() == MonitorMsgType.CACHE_VALID) {
                            String jsonValue = updateResponse.getString("updatedMap");
                            ObjectMapper mapper = new ObjectMapper();
                            cm = mapper.readValue(jsonValue, CephMap.class);
//                            System.out.println(cm);
//                            System.out.println(cm.getNode().getId());
                        }
                    } catch (Exception e) {
                        System.out.println("Monitor " + addressList[i] + " not responding. Trying next Monitor");
                        continue;
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        
        return cm;
    }

    static void operations() {
        boolean acceptInput = true;

        int option;
        Scanner sc = new Scanner(System.in);
        while (acceptInput) {
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
                    add();
                    break;
                case 2:
                    delete();
                    break;
//                case 3:
//                    initiateLoadBalancing();
//                    break;
                case 3:
                    CephMap cm = getClusterMapFromMON();
//                    cm.displayClusterMap();
                    cm.printMap();
                    break;
                default:
                    acceptInput = false;
            }
        }
    }

    public static CephMap sampleClusterMap() {
        if(cm != null){
            return cm;
        }
        
        cm = new CephMap();

        CephNode n9 = new CephNode();
        n9.setId("9");
        n9.setType("disk");
        n9.setWeight(1);
        n9.setAddress("192.168.0.9", 9);
        n9.setDriveInfo("/usr/disk");
        n9.setIsDisk(true);

        CephNode n10 = new CephNode();
        n10.setId("10");
        n10.setType("disk");
        n10.setWeight(1);
        n10.setAddress("192.168.0.10", 10);
        n10.setDriveInfo("/usr/disk");
        n10.setIsDisk(true);

        CephNode n11 = new CephNode();
        n11.setId("11");
        n11.setType("disk");
        n11.setWeight(1);
        n11.setAddress("192.168.0.11", 11);
        n11.setDriveInfo("/usr/disk");
        n11.setIsDisk(true);

        CephNode n12 = new CephNode();
        n12.setId("12");
        n12.setType("disk");
        n12.setWeight(1);
        n12.setAddress("192.168.0.12", 12);
        n12.setDriveInfo("/usr/disk");
        n12.setIsDisk(true);

        CephNode n13 = new CephNode();
        n13.setId("13");
        n13.setType("disk");
        n13.setWeight(1);
        n13.setAddress("192.168.0.13", 13);
        n13.setDriveInfo("/usr/disk");
        n13.setIsDisk(true);

        CephNode n14 = new CephNode();
        n14.setId("14");
        n14.setType("disk");
        n14.setWeight(1);
        n14.setAddress("192.168.0.14", 14);
        n14.setDriveInfo("/usr/disk");
        n14.setIsDisk(true);

        CephNode n15 = new CephNode();
        n15.setId("15");
        n15.setType("disk");
        n15.setWeight(1);
        n15.setAddress("192.168.0.15", 15);
        n15.setDriveInfo("/usr/disk");
        n15.setIsDisk(true);

        CephNode n16 = new CephNode();
        n16.setId("16");
        n16.setType("disk");
        n16.setWeight(1);
        n16.setAddress("192.168.0.16", 16);
        n16.setDriveInfo("/usr/disk");
        n16.setIsDisk(true);

        CephNode n5 = new CephNode();
        n5.setId("5");
        n5.setType("column");
        n5.setIsDisk(false);
        ArrayList<CephNode> al = new ArrayList<CephNode>();
        al.add(n9);
        al.add(n10);
        n5.setChildren(al);

        CephNode n6 = new CephNode();
        n6.setId("6");
        n6.setType("column");
        n6.setIsDisk(false);
        al = new ArrayList<CephNode>();
        al.add(n11);
        al.add(n12);
        n6.setChildren(al);

        CephNode n7 = new CephNode();
        n7.setId("7");
        n7.setType("column");
        n7.setIsDisk(false);
        al = new ArrayList<CephNode>();
        al.add(n13);
        al.add(n14);
        n7.setChildren(al);

        CephNode n8 = new CephNode();
        n8.setId("8");
        n8.setType("column");
        n8.setIsDisk(false);
        al = new ArrayList<CephNode>();
        al.add(n15);
        al.add(n16);
        n8.setChildren(al);

        CephNode n2 = new CephNode();
        n2.setId("2");
        n2.setType("row");
        n2.setIsDisk(false);
        al = new ArrayList<CephNode>();
        al.add(n5);
        al.add(n6);
        n2.setChildren(al);

        CephNode n3 = new CephNode();
        n3.setId("3");
        n3.setType("row");
        n3.setIsDisk(false);
        al = new ArrayList<CephNode>();
        al.add(n7);
        n3.setChildren(al);

        CephNode n4 = new CephNode();
        n4.setId("4");
        n4.setType("row");
        n4.setIsDisk(false);
        al = new ArrayList<CephNode>();
        al.add(n8);
        n4.setChildren(al);

        CephNode n1 = new CephNode();
        n1.setId("1");
        n1.setType("dc");
        n1.setIsDisk(false);
        al = new ArrayList<CephNode>();
        al.add(n2);
        al.add(n3);
        al.add(n4);
        n1.setChildren(al);

        cm.getNode().addChild(n1);
        return cm;
    }

    public static void main(String[] args) {
    	/**String[] command = {
                "/bin/bash",
                "-c",
                "source /etc/profile"
        };
    	try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	*/
        System.out.println("Program started...");
        operations();

        System.out.println("Program completed.");
    }
}