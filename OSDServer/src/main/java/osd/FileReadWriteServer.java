package osd;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.ArrayList;

import cephmapnode.CephMap;
import osd.FileReadEchoServer.FileServer;
import osd.FileWriteServer.WriteServer;
import osd.SystemInfoServer.InfoServer;
import osd.FileListBackupServer.FileListServer;
import net.IOControl;
import net.MsgHandler;
import net.MsgType;
import net.Session;
import types.FileBackup;
import types.FileReadMsgType;
import types.FileWriteMsgType;
import types.MonitorMsgType;
import types.SystemInfoMsgType;
import util.Log;

public class FileReadWriteServer {

    private static final Log log = Log.get();
    private static OSDProperty osd;

    public static void checkOverLoaded(IOControl control) {
        ArrayList<String> overLoadedFiles = new ArrayList<String>();

        for (String fileName : OSDGlobalParameters.getFileStats().keySet()) {
            if (OSDGlobalParameters.getFileStats().get(fileName).requestInfo.size() > OSDGlobalParameters.getThreshold()) {
                overLoadedFiles.add(fileName);
            }
        }
        if (!overLoadedFiles.isEmpty()) {
            try {
                Session session = new Session(MonitorMsgType.OSD_OVERLOADED);
                session.set("filesList", overLoadedFiles);
                String[] addressList = osd.getCEPH_MONITORS().split(",");

                for (int i = 0; i < addressList.length; i++) {
                    String[] tokens = addressList[i].split(":");
                    if (tokens.length == 2) {
                        try {
                            control.send(session, tokens[0], Integer.parseInt(tokens[1]));
                            return;
                        } catch (Exception e) {
                            System.out.println("Monitor " + addressList[i] + " not responding. Trying next Monitor");
                            continue;
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error sending overload information");
            }
        }
    }

    public static void main(String args[]) {
        try {
            //	Utils.connectToLogServer(log);
            final String CEPH_HOME = System.getenv("CEPH_HOME");

            osd = OSDProperty.getInstance(CEPH_HOME);

            IOControl server = new IOControl();
            // register file upload handler
            //  modify to your dir
            OSDGlobalParameters.setTimeout(osd.getSTATS_TIMEOUT());
            OSDGlobalParameters.setThreshold(osd.getOVERLOAD_THRESHOLD());
            OSDGlobalParameters.setCephMonitors(osd.getCEPH_MONITORS());
            MsgHandler fileWrite = new WriteServer(server, Paths.get(osd.getCEPH_DATA_DIR()));
            MsgHandler fileRead = new FileServer(server, osd.getCEPH_DATA_DIR());
            MsgHandler fileBackup = new FileListServer(server);
            MsgHandler systemInfo = new InfoServer(server);
            MsgType[] type = FileWriteMsgType.values();
            MsgType[] systemInfoTypes = SystemInfoMsgType.values();
            MsgType[] fileBackupTypes = FileBackup.values();

            server.registerMsgHandlerHead(fileRead, FileReadMsgType.READ_FILE);
            server.registerMsgHandlerLast(fileWrite, type);
            server.registerMsgHandlerLast(systemInfo, systemInfoTypes);
            server.registerMsgHandlerLast(fileBackup, fileBackupTypes);

            server.startServer(osd.getREAD_WRITE_SERVER_PORT());

            System.out.println("Starting Read / Write Server !!!");

            server.waitForServer();

        } catch (IOException e) {
            log.w(e);
        }
    }

}
