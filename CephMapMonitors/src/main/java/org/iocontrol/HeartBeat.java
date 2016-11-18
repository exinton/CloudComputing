package org.iocontrol;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import types.SystemInfoMsgType;
import cephmap.cephmapmonitors.CephGlobalParameter;
import net.IOControl;
import net.Session;

public class HeartBeat {

    static IOControl control = new IOControl();

    public static void checkPulse() {
        Logger logger = Logger.getLogger("Ceph Log");
        FileHandler fh;
        try {
            fh = new FileHandler(CephGlobalParameter.getLogFile());
            logger.setUseParentHandlers(false);
            Session session = new Session(SystemInfoMsgType.IS_ALIVE);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            for (int i = 0; i < CephGlobalParameter.getCephMap().getNodeList().size(); i++) {
                try {
                    Session reply = control.request(session, CephGlobalParameter.getCephMap().getNodeList().get(i));
                    String logInfo = "Load Information from " + reply.getSocket().getInetAddress().getHostAddress() + "\n";
                    logInfo += "\n Total number of files" + reply.getInt("noFiles");
                    logInfo += "\n Total Size of files" + reply.getInt("size");
                    logInfo += "\n Total number of requests" + reply.getInt("noRequests");
                    logger.info(logInfo);

                } catch (Exception ex) {
                    String logInfo = "Unable to get load info from " + CephGlobalParameter.getCephMap().getNodeList().get(i).getIp();
                    logInfo += "\n Marking the node as failed and redistributing the files in the node";
                    Monitor.modify_map_remove(CephGlobalParameter.getCephMap().getNodeByIP(CephGlobalParameter.getCephMap().getNodeList().get(i).getIp()));
                    logger.info(logInfo);
                }
            }
        } catch (Exception ex) {
            System.out.println("Unable to write to log file");
        }
    }
}
