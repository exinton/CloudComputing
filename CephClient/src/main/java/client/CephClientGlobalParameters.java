package client;

import cephmapnode.CephMap;
/**
*
* @author balanivash
*/
public class CephClientGlobalParameters {
	 
	private static CephMap cephMap;

	public static CephMap getCephMap() {
		return cephMap;
	}

	public static void setCephMap(CephMap cephMap) {
		CephClientGlobalParameters.cephMap = cephMap;
               
	}
	
        
        public static void setCephMap(CephMap cephMap,long tempVal) {
            if ( cephMap == null)
                cephMap = new CephMap();
		CephClientGlobalParameters.cephMap = cephMap;
                CephClientGlobalParameters.cephMap.setEpochVal(tempVal);
               
	}
	public static long getCurrentVersion(){
		return cephMap.getEpochVal();
	}

}
