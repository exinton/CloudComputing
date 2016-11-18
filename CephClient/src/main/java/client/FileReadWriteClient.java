package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.Address;
import net.Session;
import cephmapnode.CephMap;
import cephmapnode.CephNode;
import crush.CrushRun;
import crush.CrushRunNoFailDisk;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import sample.log.Utils;
import types.FileWriteMsgType;
import util.Log;
import net.IOControl;
import req.Rand.ContentSrc;

/**
 *
 * @author balanivash
 */
public class FileReadWriteClient {

    private static final Log log = Log.get();
    public static IOControl control = new IOControl();
    public static CrushRun crushRun = CrushRun.getInstance();
    public static FileWriter read_log;
    public static FileWriter write_log;
    public static FileWriter lookup_log;
    public static String readLog,writeLog,lookupLog;
    
    static ArrayList<CephNode> getLocation(IOControl control, String fileName, String type) {

        MonitorClientInterface.updateCache(control);
        
       return crushRun.runCrush(CephClientGlobalParameters.getCephMap(), fileName, type);//use crush to calculate the target osd server

    }

    static Address getReadLocation(ArrayList<CephNode> nodeList) {
        return nodeList.get(0).getAddress();
    }

    static ArrayList<Address> getAddress(ArrayList<CephNode> nodeList) {
        ArrayList<Address> addresses = new ArrayList<Address>();
        System.out.println("Host used");
        for (CephNode node : nodeList) {
            addresses.add(node.getAddress());
            System.out.println(node.getAddress().getIp() + ":" + node.getAddress().getPort() + "@" + node.getId());
        }

        return addresses;
    }

    public static void initTest(){
        CephMap cephMap = new CephMap();
        cephMap.setEpochVal(0L);
        cephMap.setNode(null);
        CephClientGlobalParameters.setCephMap(cephMap);
        Properties prop = new Properties();
        InputStream input = null;
        final String CEPH_HOME = System.getenv("CEPH_HOME");
        try {
            input = new FileInputStream(CEPH_HOME + File.separator + "conf" + File.separator + "client.properties");
            prop.load(input);
            MonitorClientInterface.serverConf = prop.getProperty("CEPH_MONITORS");
            readLog = prop.getProperty("READ_LOG");
            writeLog = prop.getProperty("WRITE_LOG");
            lookupLog = prop.getProperty("LOOKUP_LOG");
           // read_log = new FileWriter(prop.getProperty("READ_LOG"));
           // write_log = new FileWriter(prop.getProperty("WRITE_LOG"));
           // lookup_log = new FileWriter(prop.getProperty("LOOKUP_LOG"));
        }catch(IOException e){
                e.printStackTrace();
        }
    }
    public static void endTest(){
        try{
            read_log.close();
            write_log.close();
            lookup_log.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    public static List<Integer> testCreate(IOControl control, String path, Long size) {
        System.out.println("CRUSH LEVELS");
        List<Integer> result = new ArrayList<Integer>();
        try{
            write_log = new FileWriter(writeLog,true);
            lookup_log = new FileWriter(lookupLog,true);
            Long start_time = System.currentTimeMillis();
            ArrayList<Address> addresses = getAddress(getLocation(control, path, "write"));
            Long lookupTime = System.currentTimeMillis() - start_time;            
            FileWriteClient.FileWrite(control, path, addresses, size,true);
            Long writeTime = System.currentTimeMillis() - start_time;
            lookup_log.write(lookupTime.toString()+"\n");
            write_log.write(writeTime.toString()+"\n");
            for (Address address : addresses) {
                result.add(Integer.parseInt(CephClientGlobalParameters.getCephMap().getNodeByIP(address.getIp()).getId()));
            }
            write_log.close();
            lookup_log.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return result;
    }

    public static void test(IOControl control, String command, String path, Long size) {

        
        log.i(" Recieved command " + command + " " + path);
            try{
            switch (command) {
                case "read": {
                     read_log = new FileWriter(readLog,true);
                    lookup_log = new FileWriter(lookupLog,true);
                    Long start_time = System.currentTimeMillis();
                    ArrayList<CephNode> fileLocations = getLocation(control, path, "read");
                    Long lookupTime = System.currentTimeMillis() - start_time;
                    if (fileLocations == null || fileLocations.size() == 0) {
                        log.i(" Failed to get Locations, Error occured in Crush , Please contact the admin");
                        break;
                    }
                    for (CephNode fileLocation : fileLocations) {
                        int retry = FileReadClient.FileRead(control, path, fileLocation.getAddress());
                        if (retry == 0) {
                            break;
                        }
                        log.i(" Failed to read from one of the locations , retry next !!!!! ");
                    }
                    Long readTime = System.currentTimeMillis() - start_time;
                    lookup_log.write(lookupTime.toString()+"\n");
                    read_log.write(readTime.toString()+"\n");
                    read_log.close();
                    lookup_log.close();
                    break;
                }
                case "write": {
                     write_log = new FileWriter(writeLog,true);
                     lookup_log = new FileWriter(lookupLog,true);
                    Long start_time = System.currentTimeMillis();
                    ArrayList<Address> address = getAddress(getLocation(control, path, "write"));
                    Long lookupTime = System.currentTimeMillis() - start_time; 
                    if (address == null || address.isEmpty()) {
                        log.i(" Failed to get Locations, Error occured in Crush , Please contact the admin");
                        break;
                    } else {

                        FileWriteClient.FileWrite(control, path, address, size,true);
                    }
                    Long writeTime = System.currentTimeMillis() - start_time;
                    lookup_log.write(lookupTime.toString()+"\n");
                    write_log.write(writeTime.toString()+"\n");
                    write_log.close();
                    lookup_log.close();
                    break;
                }
                case "delete": {
                     write_log = new FileWriter(writeLog,true);
                    lookup_log = new FileWriter(lookupLog,true);
                    Long start_time = System.currentTimeMillis();
                    ArrayList<Address> addresses = getAddress(getLocation(control, path, "write"));
                    Long lookupTime = System.currentTimeMillis() - start_time;
                    Session delSession = new Session(FileWriteMsgType.DELETE_FILE);
                    Address primaryAddress = addresses.get(0);
                    addresses.remove(0);
                    delSession.set("filename", path);
                    delSession.set("addresses", addresses);
                    try {
                        control.send(delSession, primaryAddress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Long writeTime = System.currentTimeMillis() - start_time;
                    lookup_log.write(lookupTime.toString()+"\n");
                    write_log.write(writeTime.toString()+"\n");
                    write_log.close();
                    lookup_log.close();   
                    break;
                }
                default: {
                    log.i("Invalid       "+command+"    "+path+"    "+size);
                }
            }
            }catch(IOException ex){
                ex.printStackTrace();
            }
    }

    public static void main(String args[]) {

        IOControl control = new IOControl();
        CephMap cephMap = new CephMap();
        cephMap.setEpochVal(0L);
        cephMap.setNode(null);
        CephClientGlobalParameters.setCephMap(cephMap);
        Properties prop = new Properties();
        InputStream input = null;
          final String CEPH_HOME = System.getenv("CEPH_HOME");
         //   System.out.println("Reading from " + CEPH_HOME + File.separator + "conf" + File.separator + "client.properties");
        System.out.println("Command format : read <filename>");
        System.out.println("Command format : write <filepath>");
        System.out.println("Command format : delete <filename>");
        System.out.println("Command format : quit");
        //Utils.connectToLogServer(log);
        try {
            input = new FileInputStream(CEPH_HOME + File.separator + "conf" + File.separator + "client.properties");
            prop.load(input);
            MonitorClientInterface.serverConf = prop.getProperty("CEPH_MONITORS");
            Scanner in = new Scanner(System.in);
            for (;;) {
                System.out.println("\n\n Ceph File System. Enter Command");
                String cmd = in.nextLine();
                if (cmd.length() > 0) {
                    String line = cmd.trim();
                    String[] tokens = line.split("\\s");
                    if (tokens.length > 2) {
                        log.i("Command not valid.");
                        log.i("Valid format : read <filename> \n Valid format : write <filepath>"
                                + "\nValid format : delete <filename>"
                                + "\nValid format : quit");
                    } else {
                        String command = tokens[0].toLowerCase().trim();
                        String file = tokens[1].trim();
                        switch (command) {
                            case "read": {
                                ArrayList<CephNode> fileLocations = getLocation(control, file, "read");
                                for (CephNode fileLocation : fileLocations) {
                                    System.out.println("trying read from " + fileLocation.getId() + " at " + fileLocation.getAddress().getIp());
                                    int retry = FileReadClient.FileRead(control, file, fileLocation.getAddress());
                                    if (retry == 0) {
                                        break;
                                    }
                                    log.i(" Failed to read from one of the locations , retry next !!!!! ");

                                }
                                break;
                            }
                            case "write": {
                                File filePath = new File(file);
                                if (filePath.exists() && !filePath.isDirectory()) {
                                   // ArrayList<Address> newaddress = new ArrayList<Address>();
                                   // Address a = new Address("192.168.1.106", 9090);
                                  //  newaddress.add(a);
                                    FileWriteClient.FileWrite(control, tokens[1], getAddress(getLocation(control, filePath.getName(), "write")), 0L,false);
                                   // FileWriteClient.FileWrite(control, tokens[1], newaddress, 0L,false);
                                } else {
                                    log.i("File does not exist");
                                }
                                break;
                            }
                            case "delete": {
                                Session delSession = new Session(FileWriteMsgType.DELETE_FILE);
                                ArrayList<Address> addresses = getAddress(getLocation(control, file, "write"));
                                Address primaryAddress = addresses.get(0);
                                addresses.remove(0);
                                delSession.set("filename", file);
                                delSession.set("addresses", addresses);
                                control.send(delSession, primaryAddress);
                                break;
                            }
                            case "quit": {
                                break;
                            }
                            default: {
                                log.i("Command not valid.");
                                log.i("Valid format : read <filename> \n Valid format : write <filepath>"
                                        + "\nValid format : delete <filename>"
                                        + "\nValid format : quit");
                            }
                        }
                        if (command.equalsIgnoreCase("quit")) {
                            break;
                        }
                    }

                }
            }
            in.close();
        } catch (Exception e) {
            log.w(e);
        }

//        catch(IOException e){
//		log.w(e);
    }
}
