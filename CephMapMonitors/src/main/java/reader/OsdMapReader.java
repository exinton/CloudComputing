package reader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.iocontrol.Monitor;
import org.iocontrol.Monitor.ModifyStatus;
import org.iocontrol.MonitorProperties;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmapnode.CephMap;
import cephmapnode.CephNode;
import crush.CrushRunNoFailDisk;
import net.IOControl;
import types.MonitorMsgType;


public class OsdMapReader {
	
	
	public static void main(String[] args) {
		System.out.println("start");
		initialize();
		//delete();
		//initialize();
		lookup("read");
		/**
		try {
			addNode();
			initialize();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	
		

	}
	
	public static ArrayList<CephNode> lookup(String type){
			System.out.println("\n\n Ceph File System lookup: Enter lookup file name");
			 Scanner in = new Scanner(System.in);
			String fileName = in.nextLine();
	        ArrayList<CephNode> res=null;
	        res=CrushRunNoFailDisk.getInstance().runCrush(CephGlobalParameter.getCephMap(), fileName, type);//use crush to calculate the target osd server
	        System.out.println("input file is "+fileName);
	        System.out.println("return disks are: ");
	        for(CephNode o:res)
	        	System.out.println(o.getType()+o.getAddress().getIp()+":"+o.getAddress().getPort());
	        return res;
		
	}
	
	
	public static void initialize(){
		
		 final String CEPH_HOME = System.getenv("CEPH_HOME");
	        if (CEPH_HOME == null) {
	            System.out.println("Cannot Find CEPH_HOME, Please set the system property");
	            System.exit(-1);
	        }
	        ObjectMapper mapper = new ObjectMapper();
	        String init_json = CEPH_HOME + File.separator + "conf" + File.separator + "init_map.json";
	        try {
	            CephGlobalParameter.setCephMap(mapper.readValue(new File(init_json), CephMap.class));
	            CephGlobalParameter.getCephMap().printMap();
	        } catch (IOException ex) {
	            //ex.printStackTrace();
	            //Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
	        }
	        //MonitorProperties monitorProperties = MonitorProperties.getInstance(CEPH_HOME);
	        //CephGlobalParameter.setLogFile(monitorProperties.getLOG_FILE());
		

         mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
         mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
         mapper.setSerializationInclusion(Include.NON_NULL);
         CephMap cephmap=null;
         try {
        	 cephmap=CephGlobalParameter.getCephMap();
			String outJson = mapper.writeValueAsString(cephmap);
			System.out.println(outJson);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	public static void addNode() throws FileNotFoundException, JsonProcessingException{
		ArrayList<CephNode> add_path=createNode();
		ModifyStatus m_status=modify_map_add(add_path);
		  if (m_status.status) {
              CephGlobalParameter.getCephMap().updateHashRange();
          }
          if (m_status.status) {
        	  	saveMap();        	 
          }

	}
	
	public static  void saveMap() throws FileNotFoundException, JsonProcessingException{
		 final String CEPH_HOME = System.getenv("CEPH_HOME");
         PrintWriter pw = new PrintWriter(new FileOutputStream(CEPH_HOME + File.separator + "conf" + File.separator + "init_map.json", false));
         ObjectMapper mapper = new ObjectMapper();
         mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
         mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
         mapper.setSerializationInclusion(Include.NON_NULL);
         String outJson = mapper.writeValueAsString(CephGlobalParameter.getCephMap());
         pw.write(outJson);
         pw.close();
	}
	
	
    public static ModifyStatus modify_map_add(ArrayList<CephNode> modify_path) {
//      boolean returnStatus = true;
      Monitor.ModifyStatus m_status = new Monitor.ModifyStatus();
      m_status.status = true;
      m_status.message = "";

      CephMap cm = CephGlobalParameter.getCephMap();
      CephNode root = cm.getNode();
      boolean errorOccurred = false;
      boolean newPathTaken = false;
      CephNode addToNode = null;
      CephNode newTree = null;
      CephNode parent = root, newTreeParent = null;
      int level = 1;
      for (CephNode node : modify_path) {
          if (node.getId().equals("0")) {
              // new node should be created

              CephNode current = Monitor.instantiateCephNode(node, level);

              if (current == null) {
                  // node not created, some error
                  System.out.println("ERROR: couldn't instantiate new node");
                  m_status.message = "ERROR: Not able to instantiate node, check the details input";
                  m_status.status = false;
                  errorOccurred = true;
                  break;
              }
              if (!newPathTaken) {
                  addToNode = parent;
                  newTree = current;
                  newPathTaken = true;
              } else {
                  newTreeParent.addChild(current);
              }
              newTreeParent = current;
          } else {
              if (newPathTaken) {
                  // error
                  System.out.println("ERROR: node id given after new path");
                  errorOccurred = true;
                  m_status.status = false;
                  m_status.message = "ERROR: Referening existing node after branching out to a new node";
                  break;
              } else {
                  boolean nodeFound = false;
                  for (CephNode i : parent.getChildren()) {
                      if (node.getId().equals(i.getId())) {
                          parent = i;
                          nodeFound = true;
                      }
                  }

                  if (!nodeFound) {
                      // error
                      System.out.println("ERROR: node id could not be found");
                      errorOccurred = true;
                      m_status.status = false;
                      m_status.message = "ERROR: node id input cannot be found";
                      break;
                  }
              }
          }
          level++;
      }

      if (!errorOccurred && addToNode != null && newTree != null) {
          addToNode.addChild(newTree);
          cm.upateEpochVal();
          if (newTree.getIsDisk()) {       	 
             //cm.changeBackup(newTree.getAddress());
          }
      } /*else {
       System.out.println("ERROR OCCURED");
       m_status.status = false;    
       } */

      return m_status;
  }
	
	public static  ArrayList<CephNode> createNode(){
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
//	                    int port = 0;
	                    System.out.print("port: ");
	                    int port = sc.nextInt();
//	                    System.out.print("drive path: ");
//	                    String drive_path = sc.next();

	                    nd.setWeight(weight);
	                    nd.setType(type);
	                    nd.setAddress(ip, port);
//	                    nd.setDriveInfo(drive_path);
	                    
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
	        
//	        System.out.println(add_path);
	        return(add_path);
//	        sc.close();
	}
	
    static void delete() {
        System.out.println("in deleting a node");

        System.out.print("Specify the ID of the node to be deleted: ");
        ArrayList<CephNode> delete_path = new ArrayList<>();
        CephNode nd = new CephNode();
        Scanner sc = new Scanner(System.in);
        String id = sc.next();

        nd.setId(id);

        System.out.println();
        ModifyStatus m_status=modify_map_remove(nd);
        if (m_status.status) {
//          update_hashRange();
          Long epochVal = System.currentTimeMillis();
          CephGlobalParameter.getCephMap().setEpochVal(epochVal);
         
      } else {
      }

      CephGlobalParameter.getCephMap().printMap();
      try {
		saveMap();
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (JsonProcessingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
        
//        sc.close();
    }
    
    static ModifyStatus modify_map_remove(CephNode delete_node) {
        Monitor.ModifyStatus m_status = new Monitor.ModifyStatus();
        m_status.status = true;
        m_status.message = "";
        CephMap cm = CephGlobalParameter.getCephMap();
        CephNode result = Monitor.getCephNodeWithID(delete_node.getId());
        if (result != null) {
            result.setIsFailed(true);
            cm.upateEpochVal();
            if (result.getIsDisk()) {
                //cm.changeBackup(result.getAddress());
            }
        } else {
            System.out.println("ERROR: Node ID not found");
            m_status.status = false;
            m_status.message = "Node ID not found";
        }

        return m_status;
    }
	
	
	

}
