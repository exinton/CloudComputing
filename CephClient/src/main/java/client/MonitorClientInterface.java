package client;

import java.io.File;
import java.io.IOException;

import cephmapnode.CephMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import types.MonitorMsgType;
import net.IOControl;
import net.Session;

/**
 *
 * @author balanivash
 */
public class MonitorClientInterface {
    public static String serverConf;
   // public static int a = 0;
    public static void updateCache(IOControl control) {

        Session session = new Session(MonitorMsgType.CACHE_VALID);
        session.set("epochVal", CephClientGlobalParameters.getCurrentVersion());
            String[] addressList = serverConf.split(",");
            for (int i = 0; i < addressList.length; i++) {
                String[] tokens = addressList[i].split(":");
                if (tokens.length == 2) {
                    try {
                  //     System.out.println("here   "+a);
                  //      a++;
                        Session response = control.request(session, tokens[0], Integer.parseInt(tokens[1]));
                        if (response.getType() == MonitorMsgType.CACHE_VALID) {
                            if (response.getBoolean("isValid")) {
                             //   System.out.println("Cluster Map Cache Valid. Returning");
                                return;
                            } else {
                                Session updateSession = new Session(MonitorMsgType.CACHE_GET);
                                Session updateResponse = control.request(updateSession, tokens[0], Integer.parseInt(tokens[1]));
                                if (updateResponse.getType() == MonitorMsgType.CACHE_VALID) {
                                 //   System.out.println("Updating Cluster Map Cache");
                                    String jsonValue = updateResponse.getString("updatedMap");
                                    ObjectMapper mapper = new ObjectMapper();
                                    CephClientGlobalParameters.setCephMap(mapper.readValue(jsonValue, CephMap.class));
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

    }
//	public static void main(String args[]){
    //	long epochNumber = Long.parseLong("0");
    //MonitorClientInterface.updateCache(new IOControl(), epochNumber);
//	}

}
