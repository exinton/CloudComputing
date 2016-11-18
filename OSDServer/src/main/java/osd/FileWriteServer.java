package osd;

import net.*;
//import sample.log.Utils;
import types.FileWriteMsgType;
import util.FileHelper;
import util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chunk upload server using GFS scheme
 */
public class FileWriteServer {

    private static final Log log = Log.get();

    static class WriteResult {

        public Address address;
        public FileWriteMsgType result;

        WriteResult(Address address, FileWriteMsgType result) {
            this.address = address;
            this.result = result;
        }
    }

    static class WriteServer implements MsgHandler {

        private IOControl control;
        private Path chunkDir;

        WriteServer(IOControl control, Path chunkDir) throws IOException {
            this.control = control;
            this.chunkDir = chunkDir;
        }

        //  WRITE_CHUNK, WRITE_CHUNK_CACHE
        void proc(Session session, boolean isPrimary, long start) {
            String id = session.getString("id");
            final long size = session.getLong("size");
            long timeout = session.getLong("timeout");
            final long position = session.getLong("position", 0);
            Address primary = session.get("primary", Address.class);   //  nullable

            UUID transID = session.get("transid", UUID.class); //  nullable
            ArrayList<Address> addresses = session.get("address", ArrayList.class);

            final SocketChannel src = session.getSocketChannel();
            File newChunk = new File(chunkDir.toFile(), id);
            Session reply = session.clone();
            reply.setType(isPrimary ? FileWriteMsgType.WRITE_FAIL : FileWriteMsgType.COMMIT_FAIL);
            do {
                if (!newChunk.exists() && position > 0) {
                    log.w("File not exist but position is positive");
                    break;
                }
                if (isPrimary) {
                    primary = addresses.remove(0);
                    transID = UUID.randomUUID();
                    reply.set("primary", primary);
                    reply.set("transid", transID);
                } else {
                    addresses.remove(0);
                }
                FileOutputStream fos = null;
                try {
                    do {
                        if (!newChunk.exists()) {
                            newChunk.createNewFile();
                        }
                        fos = new FileOutputStream(newChunk);
                        final FileChannel dest = fos.getChannel();
                        if (addresses.size() == 0) {
                            //  no more forwarding
                            Future<Object> writeTrans = session.getExecutor().submit(new Callable<Object>() {
                                @Override
                                public Object call() throws Exception {
                                    FileHelper.download(src, dest, size, position);
                                    return null;
                                }
                            });
                            try {
                                writeTrans.get(timeout + start - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                                fos.close();
                                if (newChunk.length() == size) {
                                    reply.setType(isPrimary ? FileWriteMsgType.WRITE_OK : FileWriteMsgType.COMMIT_OK);
                                    log.i("File write to: " + newChunk.getAbsolutePath());
                                }
                            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                log.i(e);
                            }
                        } else {
                            //  do forward
                            Session forward = reply.clone();
                            forward.setType(FileWriteMsgType.WRITE_CHUNK_CACHE);
                            ArrayList<Address> commit_ok = new ArrayList<>();
                            ArrayList<Address> commit_fail = new ArrayList<>();
                            BlockingQueue<WriteResult> results = new LinkedBlockingQueue<>();
                            forwardResult.put(transID, results);
                            forward.set("start", start);
                            control.send(forward, addresses.get(0));
                            FileHelper.pipe(session.getExecutor(),
                                    src, dest, forward.getSocketChannel(), size);
                            fos.close();
                            if (newChunk.length() != size) {
                                break;
                            }
                            log.i("File write to: " + newChunk.getAbsolutePath());
                            long remain;
                            while ((remain = start + timeout - System.currentTimeMillis()) >= 0) {
                                WriteResult r = results.poll(remain, TimeUnit.MILLISECONDS);
                                if (r == null) {
                                    commit_fail.addAll(addresses);
                                    commit_fail.removeAll(commit_ok);
                                    break;
                                } else {
                                    if (r.result == FileWriteMsgType.COMMIT_OK) {
                                        commit_ok.add(r.address);
                                    } else {
                                        commit_fail.add(r.address);
                                    }
                                    if (commit_fail.size() + commit_ok.size() == addresses.size()) {
                                        reply.setType(FileWriteMsgType.WRITE_OK);
                                        break;
                                    }
                                }
                            }
                        }
                    } while (false);
                } catch (Exception e) {
                    log.w(e);
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
            } while (false);
            try {
                if (isPrimary) {
                    control.response(reply, session);
                } else {
                    control.send(reply, primary);
                }
                OSDGlobalParameters.addFile(id, size);
                FileListBackupServer.syncFileList(control, id, size, "add");
            } catch (Exception e) {
                log.w(e);
            }
        }

        static long timeout = 60 * 1000;    //  60 seconds

        boolean upload(String path, Address address) {
            try {
               // IOControl control1 = new IOControl();
                System.out.println("Transferring File." + path);
                File file = new File(path);
                FileInputStream fis = new FileInputStream(file);
                FileChannel src = fis.getChannel();

                Session req = new Session(FileWriteMsgType.WRITE_CHUNK);
                String id = file.getName();
                long size = file.length();
                ArrayList<Address> destList = new ArrayList<Address>();
                destList.add(address);
                req.set("id", id);
                req.set("size", size);
                req.set("timeout", timeout);
                req.set("address", destList);

                control.send(req, address.getIp(), address.getPort());

                SocketChannel dest = req.getSocketChannel();
                System.out.println(" Getting destination socket,before upload");

                FileHelper.upload(src, dest, size);
                System.out.println("upload Succuess");
                fis.close();
                Session result = control.get(req);
                System.out.println(result.getType());
                if(result.getType() == FileWriteMsgType.WRITE_OK){
                    System.out.println("inside WRITE_OK");
                    return true;
                }

            } catch (Exception e) {
                log.w(e);
            }
            return false;
        }
        private Map<UUID, BlockingQueue<WriteResult>> forwardResult = new ConcurrentHashMap<>();

        @Override
        public boolean process(Session session) throws IOException {
            long start = System.currentTimeMillis();
            MsgType type = session.getType();
            if (type == FileWriteMsgType.WRITE_CHUNK || type == FileWriteMsgType.WRITE_CHUNK_CACHE) {
                OSDGlobalParameters.incrementWriteRequestConter(session.getString("id"));
                proc(session, type == FileWriteMsgType.WRITE_CHUNK, start);
                FileReadWriteServer.checkOverLoaded(control);
            } else if (type == FileWriteMsgType.COMMIT_OK || type == FileWriteMsgType.COMMIT_FAIL) {
                UUID transID = session.get("transid", UUID.class);
                BlockingQueue<WriteResult> queue = forwardResult.get(transID);
                if (queue != null) {
                    try {
                        queue.put(new WriteResult(session.getSender(), (FileWriteMsgType) type));
                    } catch (InterruptedException ignored) {
                    }
                }
            } else if (type == FileWriteMsgType.DELETE_FILE) {
                try {
                    String fileName = session.getString("filename");
                    ArrayList<Address> addresses = session.get("addresses", ArrayList.class);
                    for (Address address : addresses) {

                        Session delSession = new Session(FileWriteMsgType.DELETE_FILE);
                        delSession.set("filename", fileName);
                        delSession.set("addresses", new ArrayList<Address>());
                        control.send(delSession, address);

                    }
                    String filePath = chunkDir + File.separator + fileName;
                    File delFile = new File(filePath);
                    delFile.delete();
                    OSDGlobalParameters.deleteFile(fileName);
                    FileListBackupServer.syncFileList(control, fileName, 0L, "delete");

                } catch (Exception ex) {
                    log.w(" Delete Failed " + ex.getMessage());
                }
            } else if (type == FileWriteMsgType.TRANSFER_FILE) {
                String fileName = session.getString("filename");
                final Address destAddress = session.get("address", Address.class);
                final String filePath = chunkDir + File.separator + fileName;
                boolean deleteFile = session.getBoolean("toDelete", true);
                String del_file = chunkDir + File.separator + fileName;

                boolean result = upload(filePath, destAddress);
                if (result == true) {
                    if (deleteFile) {
                        File delFile = new File(del_file);
                        delFile.delete();
                        OSDGlobalParameters.deleteFile(fileName);
                        OSDGlobalParameters.setThisOverLoaded(fileName);
                    }
                    Session responseTransfer = new Session(FileWriteMsgType.TRANSFER_OK);
                    control.response(responseTransfer, session);
                    FileListBackupServer.syncFileList(control, fileName, 0L, "overloaded");
                    return true;
                } else {
                    Session responseTransfer = new Session(FileWriteMsgType.TRANSFER_FAIL);
                    responseTransfer.set("message", "Transfer Failed");
                    control.response(responseTransfer, session);
                }

            }
            return false;
        }
    }

    public static void main(String args[]) {
        try {
            //	Utils.connectToLogServer(log);
            final String CEPH_HOME = System.getenv("CEPH_HOME");

            OSDProperty osd = OSDProperty.getInstance(CEPH_HOME);

            IOControl server = new IOControl();
                        // register file upload handler
            //  modify to your dir
            MsgHandler fileWrite = new WriteServer(server, Paths.get(osd.getCEPH_DATA_DIR()));

            MsgType[] type = FileWriteMsgType.values();
            server.registerMsgHandlerHead(fileWrite, type);
			//server.registerMsgHandlerHead(new SimpleLogger(),type);
            // start server
            server.startServer(osd.getWRITE_SERVER_PORT());
            // blocking until asked to quit (see SimpleEchoClient)
            server.waitForServer();

        } catch (IOException e) {
            log.w(e);
        }
    }
}
