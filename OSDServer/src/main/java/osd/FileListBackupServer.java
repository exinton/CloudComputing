package osd;

import java.io.IOException;
import java.net.InetAddress;

import osd.OSDGlobalParameters;
import crush.FileListInfo;
import net.Address;
import net.IOControl;
import net.MsgHandler;
import net.Session;
import types.FileBackup;
import types.MonitorMsgType;
import util.Log;

public class FileListBackupServer {

    private static final Log log = Log.get();

    public static void syncAdjacentNodes(IOControl control) {
        Session session = new Session(MonitorMsgType.CACHE_VALID);
        session.set("epochVal", OSDGlobalParameters.getEpoch());
        try {
            String[] addressList = OSDGlobalParameters.getCephMonitors().split(",");
            for (int i = 0; i < addressList.length; i++) {
                String[] tokens = addressList[i].split(":");
                if (tokens.length == 2) {
                    try {
                        Session response = control.request(session, tokens[0], Integer.parseInt(tokens[1]));
                         if (response.getType() == MonitorMsgType.CACHE_VALID) {
                            if (response.getBoolean("isValid",false)) {
                               // System.out.println("Cluster Map Cache Valid. Returning");
                                return;
                            } else {
                                Session updateSession = new Session(FileBackup.GET_PREV_NEXT);
                                Session updateResponse = control.request(updateSession, tokens[0], Integer.parseInt(tokens[1]));
                                if (updateResponse.getType() == FileBackup.GET_PREV_NEXT) {
                                   // System.out.println("Updating Prev and Next Pointers");
                                    OSDGlobalParameters.setPrevOSD(updateResponse.get("prev", Address.class));
                                    OSDGlobalParameters.setNextOSD(updateResponse.get("next", Address.class));
                                    OSDGlobalParameters.setEpoch(updateResponse.getLong("epoch"));
                                   // System.out.println(OSDGlobalParameters.getPrevOSD().getIp());
                                  //  System.out.println(OSDGlobalParameters.getNextOSD().getIp());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Monitor " + addressList[i] + " not responding. Trying next Monitor");
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncFileList(IOControl control, String fileName, Long size, String action) {
//        return;
     //   System.out.println("Sync File List");
        syncAdjacentNodes(control);
        try {
            Session prevSession = null;
            Session nextSession = null;
            if (action.equalsIgnoreCase("add")) {
                prevSession = new Session(FileBackup.FILE_ADD);
                nextSession = new Session(FileBackup.FILE_ADD);                
            } else if (action.equalsIgnoreCase("delete")) {
                prevSession = new Session(FileBackup.FILE_DELETE);
                nextSession = new Session(FileBackup.FILE_DELETE);
            } else if (action.equalsIgnoreCase("overloaded")) {
                prevSession = new Session(FileBackup.FILE_OVERLOADED);
                nextSession = new Session(FileBackup.FILE_OVERLOADED);
            }

            prevSession.set("destination", "next");
            prevSession.set("fileName", fileName);
            prevSession.set("size", size);
            nextSession.set("destination", "prev");
            nextSession.set("fileName", fileName);
            nextSession.set("size", size);

            control.send(prevSession, OSDGlobalParameters.getPrevOSD());
            control.send(nextSession, OSDGlobalParameters.getNextOSD());
        } catch (Exception e) {
            log.d("Error syncing files between linked OSD");
        }
    }

    static class FileListServer implements MsgHandler {

        private IOControl control;

        public FileListServer(IOControl control) {
            super();
            this.control = control;
        }

        @Override
        public boolean process(Session session) throws IOException {
            if (session.getType() == FileBackup.TRANSFER_FILE_LIST) {
                String action = session.getString("action");
                String destination = session.getString("location");
                Session reply = new Session(FileBackup.TRANSFER_FILE_LIST);

                if (action.equalsIgnoreCase("get")) {
//                    FileListInfo list = null;
                    FileListInfo list = new FileListInfo();
                    if (destination.equalsIgnoreCase("this")) {
                        list = OSDGlobalParameters.getThisNode();
                    } else if (destination.equalsIgnoreCase("prev")) {
                        list = OSDGlobalParameters.getPrevNode();
                    } else if (destination.equalsIgnoreCase("next")) {
                        list = OSDGlobalParameters.getNextNode();
                    }
                    
                    reply.set("fileList", list);
                    control.response(reply, session);
                }
                if (action.equalsIgnoreCase("set")) {
                    if (destination.equalsIgnoreCase("prev")) {
                        OSDGlobalParameters.setPrevNode(session.get("fileList", FileListInfo.class));
                    } else if (destination.equalsIgnoreCase("next")) {
                        OSDGlobalParameters.setNextNode(session.get("fileList", FileListInfo.class));
                    }
                }

            }
            if (session.getType() == FileBackup.FILE_ADD) {
                String destination = session.getString("destination");
                String filename = session.getString("fileName");
                Long size = session.getLong("size");

                if (destination.equalsIgnoreCase("prev")) {
                    OSDGlobalParameters.addPrevNode(filename, size);
                }
                if (destination.equalsIgnoreCase("next")) {
                    OSDGlobalParameters.addNextNode(filename, size);
                }
            }
            if (session.getType() == FileBackup.FILE_DELETE) {
                String destination = session.getString("destination");
                String filename = session.getString("fileName");

                if (destination.equalsIgnoreCase("prev")) {
                    OSDGlobalParameters.delPrevNode(filename);
                }
                if (destination.equalsIgnoreCase("next")) {
                    OSDGlobalParameters.delNextNode(filename);
                }
            }
            if (session.getType() == FileBackup.FILE_OVERLOADED) {
                String destination = session.getString("destination");
                String filename = session.getString("fileName");

                if (destination.equalsIgnoreCase("prev")) {
                    OSDGlobalParameters.setPrevOverLoaded(filename);
                }
                if (destination.equalsIgnoreCase("next")) {
                    OSDGlobalParameters.setNextOverLoaded(filename);
                }
            }
            return false;
        }
    }
}
