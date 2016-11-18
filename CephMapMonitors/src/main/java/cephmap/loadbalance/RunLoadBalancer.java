/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cephmap.loadbalance;

/**
 *
 * @author Avinash
 */
import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmapnode.CephMap;
import cephmapnode.CephNode;
import crush.CrushLevel;
import crush.CrushRun;
import crush.FileListInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.IOMessageConstants;
import net.Address;
import net.IOControl;
import net.MsgType;
import types.FileBackup;

import net.Session;
import types.FileWriteMsgType;
import types.MonitorMsgType;
import types.SystemInfoMsgType;
import util.Log;

public class RunLoadBalancer {

    private static final Log log = Log.get();

    public static FileWriteMsgType transferFile(IOControl control, CephNode source, CephNode Destination, String filename) throws Exception {
        System.out.println("in transfer file: source ip:" + source.getAddress().getIp() + "; destination ip:" + Destination.getAddress().getIp());
        System.out.println("filename:" + filename);
        Session session = new Session(FileWriteMsgType.TRANSFER_FILE);
        session.set("filename", filename);
        session.set("address", Destination.getAddress());

        Session response = control.request(session, source.getAddress().getIp(), source.getAddress().getPort());
        if (response.getType() == FileWriteMsgType.TRANSFER_FAIL) {
            return FileWriteMsgType.TRANSFER_FAIL;
        }
        return FileWriteMsgType.TRANSFER_OK;
    }

    // copies file but doesn't delete it
    public static MsgType copyFile(IOControl control, CephNode source, CephNode Destination, String filename) throws Exception {
        System.out.println("COPYING FILE ... file:+"+filename+"; source ip:" + source.getAddress().getIp() + "; destination ip:" + Destination.getAddress().getIp());
        Session session = new Session(FileWriteMsgType.TRANSFER_FILE);
        session.set("filename", filename);
        session.set("address", Destination.getAddress());
        session.set("toDelete", false);

        Session response = control.request(session, source.getAddress().getIp(), source.getAddress().getPort());
//        if (response.getType() == FileWriteMsgType.TRANSFER_FAIL) {
//            return FileWriteMsgType.TRANSFER_FAIL;
//        }
//        return FileWriteMsgType.TRANSFER_OK;
        return response.getType();
    }

    public static CephNode getNodeByIP(String ip) {
        ArrayList<CephNode> osdNodes = getAvailableOSDList(CephGlobalParameter.getCephMap());
        //iterate through each OSD
        CephNode nodeWithIP = null;
        for (CephNode osdNode : osdNodes) {
            if (osdNode.getAddress().getIp() == ip) {
                nodeWithIP = osdNode;
            }
        }
        return nodeWithIP;
    }

    public CephNode runDynamicOSDLoadBalancer(CephMap cephmap) {
        IOControl control = new IOControl();

        ArrayList<CephNode> OSDList = getAvailableOSDList(cephmap);

        ArrayList<CephNode> overLoadedList = new ArrayList<>();
        if (OSDList != null) {
            Session session = new Session(MonitorMsgType.OVERLOADED);
            for (CephNode OSD : OSDList) {
                try {
                    log.i("requesting node " + OSD.getId() + ":" + OSD.getAddress().getIp());
                    Session response = control.request(session, OSD.getAddress().getIp(), OSD.getAddress().getPort());
                    boolean isOverLoaded = response.getBoolean(IOMessageConstants.IS_OVERLOADED_RESPONSE);
                    log.i("response recieved " + isOverLoaded);
                    if (isOverLoaded) {
                        overLoadedList.add(OSD);
                    } else {
                        continue;
                    }
                } catch (Exception ex) {
                    log.i("Exception in getting the resulr from osd 1" + OSD.getId() + ":" + OSD.getAddress().getIp() + " " + ex.getMessage());
                }
            }

            for (CephNode oveloadedNode : overLoadedList) {
               //call file transfer logic

                CephGlobalParameter.getCephMap().getCephNodeWithID(oveloadedNode.getId()).setIsOverloaded(true);
                CephGlobalParameter.getCephMap().setEpochVal(System.currentTimeMillis());
            }
            for (CephNode oveloadedNode : overLoadedList) {
                //call file transfer logic
                transferLoad(oveloadedNode);
            }

        }
        log.w("Error in getting overloaded list");
        return null;
    }

    public static ArrayList<CephNode> getAvailableOSDList(CephMap cephMap) {

        CrushRun cr = CrushRun.getInstance();

        ArrayList<CrushLevel> crushLevels = cr.getCrushLevels();
        CrushLevel crushLevel = crushLevels.get(crushLevels.size() - 1);
        log.i("Get the list of all levels with level" + crushLevel.getLevelno());

        ArrayList<CephNode> osdList = new ArrayList<>();

        getRescursiveOSDList(cephMap.getNode(), osdList, crushLevel.getLevelno());

        return osdList;
    }

    private static void getRescursiveOSDList(CephNode cephNode, ArrayList<CephNode> osdList, int levelno) {

        if (cephNode.getLevelNo() == levelno) {
            osdList.add(cephNode);
            return;
        } else {
            ArrayList<CephNode> children = cephNode.getChildren();
            if (children == null || children.size() == 0) {
                return;
            } else {
                for (CephNode children1 : children) {
                    getRescursiveOSDList(children1, osdList, levelno);
                }
            }
        }
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

            // for now selecting every third file for transferring
            // later can use hash_function and mod to select files
            for (int i = 0; i < files.size(); i += 3) {
                // find extra file location
                System.out.println("Transferring file: " + files.get(i));
                CrushRun cr = CrushRun.getInstance();
                ArrayList<CephNode> dest_locations = cr.runCrush(CephGlobalParameter.getCephMap(), files.get(i), "READ");
                CephNode new_location = dest_locations.get(dest_locations.size() - 1);
                FileWriteMsgType fwmt = transferFile(control, node, new_location, files.get(i));
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

    /**
     * Transfer Load given list of files and node ip
     */
    static void transferLoad(ArrayList<String> files, String ip) {
        System.out.println("in transferLoad");

        CephNode node = getNodeByIP(ip);
        Address addr = node.getAddress();
        String serverIP = addr.getIp();
        int serverPort = addr.getPort();
        log.i("Receied node from map as" + node.toString());
        try {
            log.i("Starting File Transfer..");
            IOControl control = new IOControl();
            for (int i = 0; i < files.size(); i++) {
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

    public static void moveFilesOnNodeFail(IOControl control, Address failedAddress) {

        FileListInfo fileListInfo = new FileListInfo();
        ArrayList<Address> adjacentNodes = CephGlobalParameter.getCephMap().getAdjacent(failedAddress.getIp());

        boolean getFileList = false;

        for (int i = 0; i < adjacentNodes.size(); i++) {
//            boolean nodeAlreadyTried = false;
            if(i==0){
                try {
                    Session send = new Session(FileBackup.TRANSFER_FILE_LIST);
                    send.set("action", "get");
                    send.set("location", "next");
                    Session response = control.request(send, adjacentNodes.get(i));
                    fileListInfo = response.get("fileList", FileListInfo.class);
                    System.out.println("From prev");
                    for (String file : fileListInfo.getList().keySet()) {
                        System.out.println(file);
                    }
                    getFileList = true;
                } catch (Exception ex) {
//                    nodeAlreadyTried = true;
                    ex.printStackTrace();
                }
            } else if(i==1){
                try {
                    Session send = new Session(FileBackup.TRANSFER_FILE_LIST);
                    send.set("action", "get");
                    send.set("location", "prev");
                    Session response = control.request(send, adjacentNodes.get(i));
                    fileListInfo = response.get("fileList", FileListInfo.class);
                    System.out.println("From next");
                    for (String file : fileListInfo.getList().keySet()) {
                        System.out.println(file);
                    }
                    getFileList = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (getFileList) {
                break;
            }
        }
        
        CephNode failedNode = CephGlobalParameter.getCephMap().getNodeByIP(failedAddress.getIp());
        CrushRun cr = CrushRun.getInstance();
        for (String file : fileListInfo.getList().keySet()) {
            try {
                System.out.println("Running crsuh for file" + file);
                CephGlobalParameter.getCephMap().getNodeByIP(failedAddress.getIp()).setIsFailed(false);
                ArrayList<CephNode> prev_locations = cr.runCrush(CephGlobalParameter.getCephMap(), file, "READ");
                
                for (CephNode prev_location : prev_locations) {
                    System.out.println("prev_location" + prev_location.getAddress().getIp());
                }
                
                CephGlobalParameter.getCephMap().getNodeByIP(failedAddress.getIp()).setIsFailed(true);
                ArrayList<CephNode> dest_locations = cr.runCrush(CephGlobalParameter.getCephMap(), file, "READ");
                
                for (CephNode dest_location : dest_locations) {
                    System.out.println("new " + dest_location.getAddress().getIp());
                }
                
                ArrayList<String> dest_ips = new ArrayList<String>();
                for(CephNode dest_location:dest_locations){
                    dest_ips.add(dest_location.getAddress().getIp());
                }
                
                ArrayList<String> prev_ips = new ArrayList<String>();
                for(CephNode prev_location:prev_locations){
                    prev_ips.add(prev_location.getAddress().getIp());
                }
                dest_ips.removeAll(prev_ips);
                System.out.println("dest_ips");
                System.out.println(dest_ips);
                if(dest_ips.size() == 0){
                    System.out.println("Crush cannot find any new nodes");
                }
                CephNode new_location = RunLoadBalancer.getNodeByIP(dest_ips.get(0));
                
                prev_ips.remove(failedAddress.getIp());
//                System.out.println("new after intersection" + prev_ips.get(0));
                CephNode prevLoc = RunLoadBalancer.getNodeByIP(prev_ips.get(0));
                
                /*
                for (CephNode dest_location : dest_locations) {
                    System.out.println("new after intersection" + dest_location.getAddress().getIp()+" "+dest_location.getAddress());
                }
                if(dest_locations.size()==0) {
                    System.out.println("Crush cannot find any new nodes");
                }
                CephNode new_location = dest_locations.get(0);
                
                prev_locations.remove(failedAddress);
                System.out.println("new after intersection" + prev_locations.get(0));
                CephNode prevLoc = prev_locations.get(0);
                */
                
//                System.out.println("Transferring file " + file + " from "
//                        + prevLoc.getAddress().getIp() + " -> " + new_location.getAddress().getIp());
                MsgType copyFile = RunLoadBalancer.copyFile(control, prevLoc, new_location, file);
                System.out.println(copyFile + " successful ");
            } catch (Exception ex) {
                log.i("Error copying file");
                ex.printStackTrace();
            }
        }
        CephGlobalParameter.getCephMap().getNodeByIP(failedAddress.getIp()).setIsFailed(true);
        CephGlobalParameter.getCephMap().setEpochVal(System.currentTimeMillis());
        
        Thread tr = new Thread(new replicatemap.ReplicateMap());
        tr.start();
    }

}
