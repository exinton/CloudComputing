/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iocontrol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.Address;

/**
 *
 * @author Avinash
 */
public class MonitorProperties {

    private int MONITOR_SERVER_PORT;
    private String MONITOR_SERVER_IP;
    private List<Address> backupMonitors;
    private List<Address> FileServers;
    private static MonitorProperties instance = null;
    private String LOG_FILE;

    public static MonitorProperties getInstance(String ceph_home) {
        if (instance == null) {
            instance = new MonitorProperties(ceph_home);
        }
        return instance;
    }

    public static MonitorProperties getInstance() {
        return instance;
    }

    private MonitorProperties(String ceph_home) {
        setMonitorProperties(ceph_home);
    }

    private void setMonitorProperties(String ceph_home) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(ceph_home + File.separator + "conf" + File.separator + "monitor.properties");
            System.out.println("Reading From properties " + ceph_home + File.separator + "conf" + File.separator + "monitor.properties");
            prop.load(input);
            //set monitor IP
            this.setMONITOR_SERVER_IP(prop.getProperty("MONITOR_SERVER_IP"));
            //set the monitor port
            this.setMONITOR_SERVER_PORT(Integer.parseInt(prop.getProperty("MONITOR_SERVER_PORT")));
            this.setLOG_FILE(ceph_home + File.separator + "log" + File.separator + prop.getProperty("LOG_FILE"));

            String monitorList = prop.getProperty("MONITOR_PEER_LIST");

            if (monitorList != null) {
                String tempAddress[] = monitorList.split(",");
                List<Address> tempAddrList = new ArrayList<Address>();
                for (String tempAddres : tempAddress) {

                    String[] ip = tempAddres.split(":");
                    if (ip[0] != MONITOR_SERVER_IP) {
                        Address addr = new Address(ip[0], Integer.parseInt(ip[1]));
                        tempAddrList.add(addr);
                    }
                }
                this.setBackupMonitors(backupMonitors);
            } else {
                throw new IOException("Back Up Monitors not set running on single monitor");
            }

            String fileServerList = prop.getProperty("MONITOR_PEER_LIST");
            if (fileServerList != null) {
                String tempAddress[] = fileServerList.split(",");
                List<Address> tempAddrList = new ArrayList<Address>();
                for (String tempAddres : tempAddress) {
                    String[] ip = tempAddres.split(":");
                    Address addr = new Address(ip[0], Integer.parseInt(ip[1]));
                    tempAddrList.add(addr);
                }

                this.setBackupMonitors(backupMonitors);
            } else {
                throw new IOException("FileServer not set node Fail file recovery Not Possible");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the MONITOR_SERVER_PORT
     */
    public int getMONITOR_SERVER_PORT() {
        return MONITOR_SERVER_PORT;
    }

    /**
     * @param MONITOR_SERVER_PORT the MONITOR_SERVER_PORT to set
     */
    public void setMONITOR_SERVER_PORT(int MONITOR_SERVER_PORT) {
        this.MONITOR_SERVER_PORT = MONITOR_SERVER_PORT;
    }

    /**
     * @return the backupMonitors
     */
    public List<Address> getBackupMonitors() {
        return backupMonitors;
    }

    /**
     * @param backupMonitors the backupMonitors to set
     */
    public void setBackupMonitors(List<Address> backupMonitors) {
        this.backupMonitors = backupMonitors;
    }

    /**
     * @return the MONITOR_SERVER_IP
     */
    public String getMONITOR_SERVER_IP() {
        return MONITOR_SERVER_IP;
    }

    /**
     * @param MONITOR_SERVER_IP the MONITOR_SERVER_IP to set
     */
    public void setMONITOR_SERVER_IP(String MONITOR_SERVER_IP) {
        this.MONITOR_SERVER_IP = MONITOR_SERVER_IP;
    }

    /**
     * @return the FileServers
     */
    public List<Address> getFileServers() {
        return FileServers;
    }

    /**
     * @param FileServers the FileServers to set
     */
    public void setFileServers(List<Address> FileServers) {
        this.FileServers = FileServers;
    }

    public String getLOG_FILE() {
        return LOG_FILE;
    }

    public void setLOG_FILE(String logFile) {
        this.LOG_FILE = logFile;
    }
}
