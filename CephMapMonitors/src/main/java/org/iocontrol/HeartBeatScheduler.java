/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iocontrol;

import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmap.loadbalance.RunLoadBalancer;
import cephmapnode.CephNode;
import java.io.FileWriter;
import org.iocontrol.RelocateFIlesOnFailedNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import net.Address;
import net.IOControl;
import net.Session;
import types.SystemInfoMsgType;
import util.Log;

public class HeartBeatScheduler implements Runnable {

    private static Log log = Log.get();
    public IOControl control = new IOControl();

    @Override
    public void run() {
        //executeHeartBeat();
        checkPulse();
    }

    private void executeHeartBeat() {

        ArrayList<Address> nodeList = CephGlobalParameter.getCephMap().getNodeList();
        for (Address node : nodeList) {
            int maxNodeTry = 3;
            int i = 0;
            while (i < maxNodeTry) {
                try {
                    Session heartBeat = new Session(SystemInfoMsgType.GET_SYSTEM_LOAD);
                    Session response = control.request(heartBeat, node.getIp(), node.getPort());
                    break;
                } catch (Exception ex) {
                    log.i("Excpetion " + ex.getMessage());
                    ex.printStackTrace();
                    System.out.println("Failed to contact node, starting load balancer");
                    RunLoadBalancer.moveFilesOnNodeFail(control, node);
                }
                i++;
            }

        }
    }

    private void checkPulse() {
        Logger logger = Logger.getLogger("Ceph Log");
        final String CEPH_HOME = System.getenv("CEPH_HOME");
         
        if (CEPH_HOME == null) {
            System.out.println("Cannot Find CEPH_HOME, Please set the system property");
            System.exit(-1);
        }
        
//        FileHandler fh;
        FileWriter file_write;
        try {
//            fh = new FileHandler(MonitorProperties.getInstance(CEPH_HOME).getLOG_FILE(),true);
//            logger.addHandler(fh);
//            logger.setUseParentHandlers(false);
//            logger.setLevel(Level.INFO);
            
            file_write = new FileWriter(MonitorProperties.getInstance(CEPH_HOME).getLOG_FILE(),true);
            Session session = new Session(SystemInfoMsgType.IS_ALIVE);
            SimpleFormatter formatter = new SimpleFormatter();
//            fh.setFormatter(formatter);
            for (int i = 0; i < CephGlobalParameter.getCephMap().getNodeList().size(); i++) {
                try {
                    Session reply = control.request(session, CephGlobalParameter.getCephMap().getNodeList().get(i));
                    String logInfo = "Load Information from " + reply.getSocket().getInetAddress().getHostAddress() + "\n";
                    logInfo += "\n Total number of files:" + reply.getInt("noFiles");
                    logInfo += "\n Total Size of files:" + reply.getLong("size");
                    logInfo += "\n Total number of requests:" + reply.getInt("noRequests");
//                    logger.info(logInfo);
                    file_write.write("\n\n"+(new Date()).toString()+" checkPulse");
                    file_write.write(logInfo);
                } catch (Exception ex) {
                    CephNode failed_node = RunLoadBalancer.getNodeByIP(CephGlobalParameter.getCephMap().getNodeList().get(i).getIp());
                    
                    String logInfo = "Unable to get load info from " + CephGlobalParameter.getCephMap().getNodeList().get(i).getIp();
                    logInfo += "\n Marking the node as failed and redistributing the files in the node";

                    file_write.write("\n\n"+(new Date()).toString()+" checkPulse");
                    file_write.write(logInfo);
                    System.out.println("Unable to get load info from " + failed_node.getAddress().getIp());
                    
                    // process the node only if under processing is false
                    if(!failed_node.getUnderProcessing()){
                        failed_node.setUnderProcessing(true);

                        new RelocateFIlesOnFailedNode(failed_node);

                        failed_node.setUnderProcessing(false);
                       
                        /*
                        RunLoadBalancer.moveFilesOnNodeFail(control, CephGlobalParameter.getCephMap().getNodeList().get(i));
                        Monitor.modify_map_remove(CephGlobalParameter.getCephMap().getNodeByIP(CephGlobalParameter.getCephMap().getNodeList().get(i).getIp()));
                        */

//                        logger.info(logInfo);
                    }
                }
            }
            file_write.write("\n******************************************************************************************************\n");
            file_write.close();
            
            
        } catch (Exception ex) {
            System.out.println("Unable to write to log file");
        }
    }

}
