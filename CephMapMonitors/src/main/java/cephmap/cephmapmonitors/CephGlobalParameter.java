/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cephmap.cephmapmonitors;

import cephmapnode.CephMap;

/**
 *
 * @author Avinash
 */
public class CephGlobalParameter {

    private static CephMap cephMap = new CephMap();
    private static String logFile;

    public static CephMap getCephMap() {
        return cephMap;
    }

    public static void setCephMap(CephMap cephMap) {
        cephMap.updateHashRange();
        CephGlobalParameter.cephMap = cephMap;
        CephGlobalParameter.cephMap.populateNodeList();       
    }

    public static long getCurrentVersion() {
        return cephMap.getEpochVal();
    }

	public static String getLogFile() {
		return logFile;
	}

	public static void setLogFile(String logFile) {
		CephGlobalParameter.logFile = logFile;
	}
}
