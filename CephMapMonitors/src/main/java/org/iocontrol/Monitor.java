/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iocontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.PrintWriter;

import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmapnode.CephMap;
import cephmapnode.CephNode;
import cephmap.loadbalance.RunLoadBalancer;
import net.Address;

import java.util.LinkedList;
import java.util.Queue;

import types.FileBackup;
import types.MonitorMsgType;
import types.SystemInfoMsgType;
import types.FileWriteMsgType;
import net.IOControl;
import net.MsgHandler;
import net.MsgType;
import net.Session;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import crush.CrushRun;
import crush.FileListInfo;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import replicatemap.ReplicateMap;
import message.IOMessageConstants;
import types.ShareFileServerMsgType;

//import sample.EchoMsgType;
//import sample.FileReadEchoServer;
//import sample.FileReadMsgType;
//import sample.RawLogger;
//import sample.log.Utils;
import util.Log;

/**
 *
 * @author Avinash
 */
public class Monitor {

    private static Log log = Log.get();
    public final static ScheduledExecutorService scheduler
            = Executors.newScheduledThreadPool(1);
    /*
     static class Echo implements MsgHandler{
     private IOControl control;
     Echo(IOControl control){
     this.control=control;
     }
     @Override
     public boolean process(Session session) throws IOException{
     control.response(new Session(EchoMsgType.ACK),session);
     if(session.getType()==EchoMsgType.EXIT_SERVER)
     control.quitServer();
     return false;
     }
     }
     */
    static class MonitorServer implements MsgHandler {

        private static final Log log = Log.get();

        private IOControl control;

        MonitorServer(IOControl control) {
            this.control = control;
        }

        @Override
        public boolean process(Session session) throws IOException {

            System.out.println("Starting processing " + session.getType());
            Session error = new Session(MonitorMsgType.MONITOR_ERROR);
            try {
                MsgType type = session.getType();

                if (type == MonitorMsgType.CACHE_VALID) {
                    Long epochVal = session.getLong("epochVal");
                    if (epochVal.equals(CephGlobalParameter.getCephMap().getEpochVal())) {
                        Session reply = new Session(MonitorMsgType.CACHE_VALID);
                        reply.set("isValid", true);                       // System.out.println("sending response");
                        control.response(reply, session);
                                          //      System.out.println("sent response");
                    } else {
                        Session reply = new Session(MonitorMsgType.CACHE_VALID);
                        reply.set("isValid", false);
                        control.response(reply, session);
                    }
                }
                if (type == MonitorMsgType.CACHE_GET) {

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
                    mapper.setSerializationInclusion(Include.NON_NULL);
                    String outJson = mapper.writeValueAsString(CephGlobalParameter.getCephMap());
                    Session reply = new Session(MonitorMsgType.CACHE_VALID);
                    reply.set("updatedMap", outJson);
                    control.response(reply, session);
                }
                if (type == FileBackup.GET_PREV_NEXT) {
//                        System.out.println("prev_NEXT");
//                        System.out.println(session.getSocket().getInetAddress().getHostAddress());
//                        for(Address adr: CephGlobalParameter.getCephMap().getNodeList()){
//                            System.out.println(adr.getIp());
//                        }
                    ArrayList<Address> result = CephGlobalParameter.getCephMap().getAdjacent(session.getSocket().getInetAddress().getHostAddress());
                    Session response = new Session(FileBackup.GET_PREV_NEXT);
                    response.set("prev", result.get(0));
                    response.set("next", result.get(1));
                    response.set("epoch", CephGlobalParameter.getCurrentVersion());
                    control.response(response, session);
                }
                if (type == MonitorMsgType.UPDATE_MAP) {

                    String jsonValue = session.getString("updatedMap");
                    Session response = new Session(MonitorMsgType.UPDATE_MAP_RESPONSE);
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        CephMap tempMap = mapper.readValue(jsonValue, CephMap.class);
                        //begin updating
                        CephGlobalParameter.getCephMap().setUpdating(true);
                        CephGlobalParameter.setCephMap(tempMap);
                        CephGlobalParameter.getCephMap().setUpdating(false);

                        response.set(IOMessageConstants.UPDATE_MAP_RESPONSE_MESSAGE, "updated");
                    } catch (Exception e) {
                        response.set(IOMessageConstants.UPDATE_MAP_RESPONSE_MESSAGE, "failed");
                        response.set(IOMessageConstants.UPDATE_MAP_FAILED, e.getMessage());
                    } finally {
                        control.response(response, session);
                    }

                }

                if (type == MonitorMsgType.MODIFY_MAP_ADD) {
                    waitForNewCephMap();
                    ArrayList<CephNode> modify_path = (ArrayList<CephNode>) session.get("exchange_info");
                    System.out.println(modify_path);
                    Monitor.ModifyStatus m_status = modify_map_add(modify_path);
                    Session returnSession = new Session(MonitorMsgType.ACK_MODIFY);
//                    System.out.println("Message: " + m_status.message + "; status: " + m_status.status);
                    if (m_status.status) {
                        CephGlobalParameter.getCephMap().updateHashRange();
                        returnSession.set("message", "success");
                    } else {
                        returnSession.set("message", m_status.message);
                    }
                    control.response(returnSession, session);

                    if (m_status.status) {
                        reshuffleOnAdd(control);

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
                }
                if (type == MonitorMsgType.MODIFY_MAP_REMOVE) {
                    log.i("Wait till the monitor gets the latest map");
                    waitForNewCephMap();
                    ArrayList<CephNode> modify_path = (ArrayList<CephNode>) session.get("exchange_info");
                    Session returnSession = new Session(MonitorMsgType.ACK_MODIFY);
                    Monitor.ModifyStatus m_status = modify_map_remove(modify_path.get(0));
                    if (m_status.status) {
//                        update_hashRange();
                        Long epochVal = System.currentTimeMillis();
                        CephGlobalParameter.getCephMap().setEpochVal(epochVal);
                        returnSession.set("message", "success");
                    } else {
                        returnSession.set("message", m_status.message);
                    }

                    CephGlobalParameter.getCephMap().printMap();
                    control.response(returnSession, session);
                }
                if (type == MonitorMsgType.INITIATE_LOAD_BALANCING) {
                    log.i("Wait till the monitor gets the latest map");
                    while (CephGlobalParameter.getCephMap().isUpdating());
                    log.i("Start modifying the ceph map");

                    /*
                     ArrayList<CephNode> modify_path = (ArrayList<CephNode>) session.get("modify_path");
                     Session returnSession = new Session(MonitorMsgType.ACK_MODIFY);
                     CephNode overloadedNode = modify_path.get(0);
                    
                     CephNode result = getCephNodeWithID(overloadedNode.getId());
                    
                     if(result != null){
                     if(result.getIsDisk()){
                     result.setIsOverloaded(true);
                     CephMap cm = CephGlobalParameter.getCephMap();
                     cm.upateEpochVal();
                     System.out.println("overload: success");
                     returnSession.set("message", "success");
                     control.response(returnSession, session);
                     // after returning status do the transferring process
                     transferLoad(result);
                     } else {
                     System.out.println("ERROR: only disk nodes can be marked as overloaded");
                     returnSession.set("message", "ERROR: only disk nodes can be marked as overloaded");
                     control.response(returnSession, session);
                     }
                     } else {
                     System.out.println("ERROR: Node ID not present");
                     returnSession.set("message", "ERROR: Node ID not present");
                     control.response(returnSession, session);
                     }
                     */
                    CephGlobalParameter.getCephMap().printMap();
                }
                if (type == MonitorMsgType.OVERLOADED) {
                    log.i("Recieved overloaded Request from an OSD, start maoving overloaded files..");

                    ArrayList<String> overLoadedFiles = (ArrayList<String>) session.get("filesList");
                    String ip = (String) session.get("ip");
                    if (overLoadedFiles != null && overLoadedFiles.size() != 0 && ip != null) {

                    }

                    while (CephGlobalParameter.getCephMap().isUpdating());
                    log.i("Start modifying the ceph map");
                    ArrayList<CephNode> modify_path = (ArrayList<CephNode>) session.get("modify_path");
                    Session returnSession = new Session(MonitorMsgType.ACK_MODIFY);
                    CephNode overloadedNode = modify_path.get(0);
                    CephNode result = getCephNodeWithID(overloadedNode.getId());

                    if (result != null) {
                        if (result.getIsDisk()) {
                            result.setIsOverloaded(true);
                            CephMap cm = CephGlobalParameter.getCephMap();
                            cm.upateEpochVal();
                            System.out.println("overload: success");
                            returnSession.set("message", "success");
                            control.response(returnSession, session);
                            // after returning status do the transferring process
                            transferLoad(result);
                        } else {
                            System.out.println("ERROR: only disk nodes can be marked as overloaded");
                            returnSession.set("message", "ERROR: only disk nodes can be marked as overloaded");
                            control.response(returnSession, session);
                        }
                    } else {
                        System.out.println("ERROR: Node ID not present");
                        returnSession.set("message", "ERROR: Node ID not present");
                        control.response(returnSession, session);
                    }
                    CephGlobalParameter.getCephMap().printMap();
                }

                if (type == MonitorMsgType.OSD_OVERLOADED) {
                    log.i("Recieved overloaded Request from an OSD, start moving overloaded files..");
                    ArrayList<String> overLoadedFiles = (ArrayList<String>) session.get("filesList");
                    String ip = session.getSocket().getInetAddress().getHostAddress();

                    System.out.println("Overload request came from ip:" + ip);

                    CephNode node = CephGlobalParameter.getCephMap().getNodeByIP(ip);

                    if (node == null) {
                        log.i("Node with ip not registered" + ip);
                    }

                    CrushRun cr = CrushRun.getInstance();

                    CephGlobalParameter.getCephMap().setEpochVal(System.currentTimeMillis());
                    if (overLoadedFiles != null && overLoadedFiles.size() != 0) {

                        for (int i = 0; i < overLoadedFiles.size(); i++) {
                            // find extra file location
                            System.out.println("Get Previous locations file: " + overLoadedFiles.get(i));
                            ArrayList<CephNode> prev_locations = cr.runCrush(CephGlobalParameter.getCephMap(), overLoadedFiles.get(i), "READ");

                            ArrayList<String> prev_ips = new ArrayList<String>();
                            for (CephNode prev_location : prev_locations) {
                                System.out.print("location - > " + prev_location.getAddress().getIp());
                                prev_ips.add(prev_location.getAddress().getIp());
                            }

                            node.setIsOverloaded(true);
                            ArrayList<CephNode> dest_locations = cr.runCrush(CephGlobalParameter.getCephMap(), overLoadedFiles.get(i), "OVERLOAD");

                            ArrayList<String> dest_ips = new ArrayList<String>();
                            for (CephNode dest_location : dest_locations) {
                                System.out.print(" new location - > " + dest_location.getAddress().getIp());
                                dest_ips.add(dest_location.getAddress().getIp());
                            }

                            dest_ips.removeAll(prev_ips);

                            if (dest_ips.size() > 0) {
                                CephNode new_location = RunLoadBalancer.getNodeByIP(dest_ips.get(0));
                                FileWriteMsgType fwmt;
                                fwmt = RunLoadBalancer.transferFile(control, node, new_location, overLoadedFiles.get(i));
                                if (fwmt == FileWriteMsgType.TRANSFER_OK) {
                                    System.out.println("Transfer of file:" + overLoadedFiles.get(i) + " successful");
                                } else if (fwmt == FileWriteMsgType.TRANSFER_FAIL) {
                                    System.out.println("Transfer of file:" + overLoadedFiles.get(i) + " failed");
                                }
                            } else {
                                System.out.println("ERROR: OSD_OVERLOAD new node could not be found");
                            }

                            /*
                             dest_locations.removeAll(prev_locations);
                             for (CephNode dest_node_found : dest_locations) {
                             System.out.print(" dest_node_found - > "+dest_node_found.getAddress().getIp());
                             }
                             
                             CephNode new_location = dest_locations.get(0);
                             //                             Iterator<CephNode> iterator = destnodes.iterator();
                             //                             CephNode new_location = iterator.next();
                             */
                        }
                    } else {
                        log.i("Recieved osd request but null file list, exiting overloading ");
                    }
                }

                if (type == MonitorMsgType.NODE_FAIL) {

                    Session returnSession = new Session(MonitorMsgType.NODE_FAIL);
                    log.i("Recieved node Request from an Monitor, start replicating failed files..");
                    waitForNewCephMap();
                    String failedNodeId = session.getString(IOMessageConstants.NODE_FAILED);
                    log.i("Recived Id" + failedNodeId);
                    CephNode failedNode = getCephNodeWithID(failedNodeId);
                    failedNode.setIsFailed(true);
                    CephGlobalParameter.getCephMap().setEpochVal(System.currentTimeMillis());
                    if (failedNode != null) {
                        IOControl fileserver = new IOControl();
                        Session fileServerSession = new Session(ShareFileServerMsgType.GET_OSD_FILES);
                        //Session response = fileserver.request(fileServerSession,);
                        ArrayList<Address> fileServer = (ArrayList<Address>) MonitorProperties.getInstance().getFileServers();
                        int retry = 0;
                        Session response = null;
                        for (Address fileServer1 : fileServer) {
                            if (retry == 1) {
                                break;
                            }
                            try {
                                response = fileserver.request(fileServerSession, fileServer1);
                                if (response != null) {
                                    retry = 0;
                                }
                                retry = 1;
                                break;
                            } catch (Exception e) {
                                e.printStackTrace();
                                retry = 0;
                                log.i("retrying next file server");
                            }

                        }
                        if (response != null) {
                            ArrayList<String> files
                                    = (ArrayList<String>) response.get(IOMessageConstants.NODE_FAILED_FILE_LIST);
                            if (files != null) {
                                //for each of the file run crush and get the new node
                                CrushRun crush = CrushRun.getInstance();

                            } else {
                                log.i("Did Not Recieve any files");
                            }

                        } else {
                            log.i("Error in getting reponse from file servers");
                        }

                    } else {
                        System.out.println("ERROR: Node ID not present");

                        returnSession.set("message", "ERROR: Node ID not present");
                        control.response(returnSession, session);
                    }

                }
            } catch (Exception e) {
                log.w(e);
                error.set("comment", e.getMessage());
                control.response(error, session);
            }
            return false;
        }

        private void waitForNewCephMap() throws InterruptedException {
            while (CephGlobalParameter.getCephMap().isUpdating()) {
                Thread.sleep(1000);
            }
            return;
        }
    }

    public static class ModifyStatus {

        public boolean status;
        public String message;
    }

    public static ModifyStatus modify_map_add(ArrayList<CephNode> modify_path) {
//        boolean returnStatus = true;
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

                CephNode current = instantiateCephNode(node, level);

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
                cm.changeBackup(newTree.getAddress());
            }
        } /*else {
         System.out.println("ERROR OCCURED");
         m_status.status = false;    
         } */

        return m_status;
    }

    static ModifyStatus modify_map_remove(CephNode delete_node) {
        Monitor.ModifyStatus m_status = new Monitor.ModifyStatus();
        m_status.status = true;
        m_status.message = "";
        CephMap cm = CephGlobalParameter.getCephMap();
        CephNode result = getCephNodeWithID(delete_node.getId());
        if (result != null) {
            result.setIsFailed(true);
            cm.upateEpochVal();
            if (result.getIsDisk()) {
                cm.changeBackup(result.getAddress());
            }
        } else {
            System.out.println("ERROR: Node ID not found");
            m_status.status = false;
            m_status.message = "Node ID not found";
        }

        return m_status;
    }

    public static CephNode getCephNodeWithID(String ID) {
        CephNode result = null;

        CephMap cm = CephGlobalParameter.getCephMap();
        Queue<CephNode> q = new LinkedList<>();
        q.add(cm.getNode());

        while (!q.isEmpty()) {
            CephNode n = q.poll();
            if (n.getId().equals(ID)) {
                result = n;
                break;
            }
            for (CephNode cn : n.getChildren()) {
                q.add(cn);
            }
        }

        return result;
    }

    /*
     Input Parameters:
     Output Parameters:
     Decription: reshuffle files when new node is ADDED
    
     1. traverse to all disk nodes and inspect all files
     2. for each file:
     get old locations
     get new locations - consider hidden nodes in CRUSH
     if new_location[get_index(my_location)] == my_location:
     no file movement required
     else:
     move file to new_location; call the transfer file component
     */
    public static boolean reshuffleOnAdd(IOControl control) {
        CephMap cm = CephGlobalParameter.getCephMap();
        ArrayList<String> files_moved = new ArrayList<>();
        Queue<CephNode> q = new LinkedList<>();
        
        q.add(cm.getNode());
        int filesScanned = 0, filesMoved = 0;
        while (!q.isEmpty()) {
            CephNode cn = q.poll();

            // process disk nodes
            if (cn.getIsDisk() && !cn.getIsHidden()) {
                try {
                    // get list of files from OSD daemon
                    Session session = new Session(SystemInfoMsgType.LIST_FILES);

                    String osd_IP = cn.getAddress().getIp();
                    int osd_port = cn.getAddress().getPort();

                    Session ping = control.request(session, osd_IP, osd_port);
                    String file_names = ping.getString("files");
//                    String file_names = "file1.txt,file2.txt,file3.txt";

                    // process each file
                    for (String file_name : new ArrayList<>(Arrays.asList(file_names.split(",")))) {
                        boolean relocationNecessary = true;
                        if (file_name.length() > 0) {
                            CrushRun cr = CrushRun.getInstance();
                            ArrayList<CephNode> old_locations = cr.runCrush(cm, file_name, "READ");
                            ArrayList<CephNode> new_locations = cr.runCrush(cm, file_name, "READ", true);

                            System.out.println("current IP:" + osd_IP + ", file:" + file_name);
                            System.out.println("OLD LOCATIONS");
                            for (CephNode location : old_locations) {
                                System.out.print(location.getAddress().getIp() + " ");
                            }
                            System.out.println();

                            System.out.println("NEW LOCATIONS");
                            for (CephNode location : new_locations) {
                                System.out.print(location.getAddress().getIp() + " ");
                            }
                            System.out.println();

                            HashMap<Integer, String> new_ips = new HashMap<>();
                            int count = 0;
                            for (CephNode itr : new_locations) {
                                String ip = itr.getAddress().getIp();
//                                System.out.println(ip);
                                if (ip.equals(osd_IP)) {
                                    // osd holds the file based on new map
                                    relocationNecessary = false;
                                    break;
                                }
                                new_ips.put(count++, ip);
                            }

//                            System.out.println("new ips");
//                            System.out.println(new_ips);
                            if (relocationNecessary) {
                                int old_location = -1;
                                count = 0;
                                for (CephNode itr : old_locations) {
                                    String ip = itr.getAddress().getIp();
//                                    System.out.println("old location:"+ip);
                                    if (ip.equals(osd_IP)) {
                                        old_location = count;
                                        break;
                                    }
                                    count++;
                                }

//                                System.out.println("old location index:" + old_location);
                                if (old_location >= 0 && new_ips.containsKey(old_location)) {
                                    String to_ip = new_ips.get(old_location);

                                    // copy the file, don't delete it
//                                    System.out.println("Copy file: " + osd_IP + " -> " + new_locations.get(old_location).getAddress().getIp());
                                    MsgType fwmt = RunLoadBalancer.copyFile(control, cn, new_locations.get(old_location), file_name);
                                    System.out.println(fwmt);
                                    if (fwmt == FileWriteMsgType.TRANSFER_OK) {
                                        files_moved.add(osd_IP + "," + osd_port + "," + file_name);
                                    } else if (fwmt == FileWriteMsgType.TRANSFER_FAIL) {
                                        System.out.println("Transfer of file:" + file_name + " failed");
                                    }
                                }

                            }

                        }
                    filesScanned++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                for (CephNode child : cn.getChildren()) {
                    q.add(child);
                }
            }
        }

        // update hidden nodes, mark isHidden as false , update new weights
        cm.makeHiddenNodesActive();

        try {
            // delete the files after cm
            for (String ip_filename : files_moved) {
                String[] info = ip_filename.split(",");

                // code to call delete files in OSD
//                System.out.println(info[0] + " " + info[1]);
                Session del_file = new Session(FileWriteMsgType.DELETE_FILE);
                del_file.set("filename", info[2]);
                del_file.set("addresses", new ArrayList<Address>());
                control.send(del_file, info[0], Integer.parseInt(info[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        filesMoved = files_moved.size();
        
        final String CEPH_HOME = System.getenv("CEPH_HOME");
        String relocatedFilesLog = CEPH_HOME+File.separator+"log"+File.separator+"files_relocated.log";
        
        FileWriter fw = null;
        try{
            fw = new FileWriter(relocatedFilesLog, true);
            String log_line = (new Date()).toString()+": Files Scanned:"+filesScanned+", Files Moved:"+filesMoved+"\n";
            fw.write(log_line);
            fw.close();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }

        return true;
    }

    static void transferLoad(CephNode node) {
        System.out.println("in transferLoad");

        Address addr = node.getAddress();

        String serverIP = addr.getIp();
        int serverPort = addr.getPort();

        try {
            IOControl control = new IOControl();
            Session session = new Session(SystemInfoMsgType.LIST_FILES);
            Session ping = control.request(session, serverIP, serverPort);
            String file_names = ping.getString("files");
            System.out.println(file_names);
            ArrayList<String> files = new ArrayList<>(Arrays.asList(file_names.split(",")));

            for (int i = 0; i < files.size(); i += 3) {
                // find extra file location
                System.out.println("Transferring file: " + files.get(i));
                CrushRun cr = CrushRun.getInstance();
                ArrayList<CephNode> dest_locations = cr.runCrush(CephGlobalParameter.getCephMap(), files.get(i), "READ");
                CephNode new_location = dest_locations.get(dest_locations.size() - 1);
                FileWriteMsgType fwmt = RunLoadBalancer.transferFile(control, node, new_location, files.get(i));
                if (fwmt == FileWriteMsgType.TRANSFER_OK) {
                    System.out.println("Transfer of file:" + files.get(i) + " successful");
                } else if (fwmt == FileWriteMsgType.TRANSFER_FAIL) {
                    System.out.println("Transfer of file:" + files.get(i) + " failed");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean isTypeValid(int level, String type) {
        return true;
    }

    static boolean isValidIP(String ip_address) {
        return true;
    }

    public static CephNode instantiateCephNode(CephNode n, int level) {
        boolean isDisk = n.getIsDisk();
        CephNode newNode = null;

        if (isDisk) {
            while (true) {
                if (!isTypeValid(level, n.getType())) {
                    break;
                }
                if (!isValidIP(n.getAddress().getIp())) {
                    break;
                }

                newNode = new CephNode();

                newNode.setIsDisk(true);
                newNode.setType(n.getType());
                newNode.setLevelNo(level);
                newNode.setAddress(n.getAddress());
                newNode.setDriveInfo(n.getDriveInfo());
                newNode.setIsHidden(true);
                newNode.setHiddenWeight(n.getWeight());

                break;
            }
        } else {
            // check type, maybe later
            while (true) {
                if (!isTypeValid(level, n.getType())) {
                    break;
                }

                newNode = new CephNode();
                newNode.setIsDisk(false);
                newNode.setType(n.getType());
                newNode.setLevelNo(level);
                newNode.setIsHidden(true);

                break;
            }
        }

        return newNode;
    }

    /*
     public static void main(String[] args){
     Monitor.sampleClusterMap();
     }*/
    public static void main(String[] args) {

        //  CephGlobalParameter.setCephMap(sampleClusterMap());
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
            ex.printStackTrace();
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        MonitorProperties monitorProperties = MonitorProperties.getInstance(CEPH_HOME);
        CephGlobalParameter.setLogFile(monitorProperties.getLOG_FILE());
        try {
            IOControl server = new IOControl();
            MsgHandler monitor = new Monitor.MonitorServer(server);
            server.registerMsgHandlerLast(monitor, MonitorMsgType.values());
            server.registerMsgHandlerLast(monitor, new FileBackup[]{FileBackup.GET_PREV_NEXT});
            server.startServer(monitorProperties.getMONITOR_SERVER_PORT());
            scheduler.scheduleAtFixedRate(new HeartBeatScheduler(), 10, 30, SECONDS);
            server.waitForServer();
            scheduler.shutdownNow();
        } catch (IOException e) {
            log.w(e);
        }

    }
}
