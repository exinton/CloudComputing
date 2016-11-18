/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package osd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Avinash
 */
 class OSDProperty {
    private  int READ_SERVER_PORT;
    private  String CEPH_DATA_DIR;
    private  int WRITE_SERVER_PORT;
    private  int READ_WRITE_SERVER_PORT;
    private  int STATS_TIMEOUT;
	private int OVERLOAD_THRESHOLD;
	private String CEPH_MONITORS;
    

	private static OSDProperty instance = null;

    public static OSDProperty getInstance(String ceph_home) {
        if (instance == null) {
            instance = new OSDProperty(ceph_home);
        }
        return instance;
    }

    private OSDProperty( String ceph_home) {
        setOSDProperties(ceph_home);
    }
    
    
    private  void setOSDProperties(String ceph_home){
        Properties prop = new  Properties();
        InputStream input = null;
         try {
               input = new FileInputStream(ceph_home+File.separator+"conf"+File.separator+"osd.properties");
               System.out.println("Reading From properties " + ceph_home+File.separator+"conf"+File.separator+"osd.properties");
                prop.load(input);
                this.setREAD_SERVER_PORT( Integer.parseInt( prop.getProperty("OSD_READ_SERVER_PORT") ));
                this.setCEPH_DATA_DIR(prop.getProperty("CEPH_DATA_DIR"));
                this.setWRITE_SERVER_PORT(Integer.parseInt(prop.getProperty("OSD_WRITE_SERVER_PORT")));
                this.setREAD_WRITE_SERVER_PORT(Integer.parseInt(prop.getProperty("OSD_READ_WRITE_SERVER_PORT")));
                this.setSTATS_TIMEOUT(Integer.parseInt(prop.getProperty("STATS_TIMEOUT")));
                this.setOVERLOAD_THRESHOLD(Integer.parseInt(prop.getProperty("OVERLOAD_THRESHOLD")));
                this.setCEPH_MONITORS(prop.getProperty("CEPH_MONITORS"));
             }
        catch(IOException e) {
            e.printStackTrace();
        }

    
    } 
     public String getCEPH_MONITORS() {
		return CEPH_MONITORS;
	}

	public void setCEPH_MONITORS(String cEPH_MONITORS) {
		CEPH_MONITORS = cEPH_MONITORS;
	}
 
    /**
     * @return the CEPH_DATA_DIR
     */
    public String getCEPH_DATA_DIR() {
        return CEPH_DATA_DIR;
    }

    /**
     * @param CEPH_DATA_DIR the CEPH_DATA_DIR to set
     */
    public void setCEPH_DATA_DIR(String CEPH_DATA_DIR) {
        this.CEPH_DATA_DIR = CEPH_DATA_DIR;
    }

    /**
     * @return the WRITE_SERVER_PORT
     */
    public int getWRITE_SERVER_PORT() {
        return WRITE_SERVER_PORT;
    }

    /**
     * @param WRITE_SERVER_PORT the WRITE_SERVER_PORT to set
     */
    public void setWRITE_SERVER_PORT(int WRITE_SERVER_PORT) {
        this.WRITE_SERVER_PORT = WRITE_SERVER_PORT;
    }

    /**
     * @return the READ_SERVER_PORT
     */
    public int getREAD_SERVER_PORT() {
        return READ_SERVER_PORT;
    }

    /**
     * @param READ_SERVER_PORT the READ_SERVER_PORT to set
     */
    public void setREAD_SERVER_PORT(int READ_SERVER_PORT) {
        this.READ_SERVER_PORT = READ_SERVER_PORT;
    }


    /**
     * @return the READ_WRITE_SERVER_PORT
     */
    public int getREAD_WRITE_SERVER_PORT() {
        return READ_WRITE_SERVER_PORT;
    }

    /**
     * @param READ_WRITE_SERVER_PORT the READ_WRITE_SERVER_PORT to set
     */
    public void setREAD_WRITE_SERVER_PORT(int READ_WRITE_SERVER_PORT) {
        this.READ_WRITE_SERVER_PORT = READ_WRITE_SERVER_PORT;
    }
    
    public int getSTATS_TIMEOUT() {
        return STATS_TIMEOUT;
    }
    public void setSTATS_TIMEOUT(int STATS_TIMEOUT) {
        this.STATS_TIMEOUT = STATS_TIMEOUT;
    }

    public int getOVERLOAD_THRESHOLD() {
		return OVERLOAD_THRESHOLD;
	}

	public void setOVERLOAD_THRESHOLD(int oVERLOAD_THRESHOLD) {
		OVERLOAD_THRESHOLD = oVERLOAD_THRESHOLD;
	}
    
}
