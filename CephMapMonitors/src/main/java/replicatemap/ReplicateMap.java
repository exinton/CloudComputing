/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package replicatemap;

import java.util.ArrayList;
import net.Address;
import net.IOControl;
import net.Session;
import types.MonitorMsgType;
import cephmap.cephmapmonitors.CephGlobalParameter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.IOMessageConstants;
import org.iocontrol.MonitorProperties;
import util.Log;
/**
 *
 * @author Avinash
 */
public class ReplicateMap implements Runnable{
     private static Log log = Log.get();
     public static void  replicateMap(ArrayList<Address> addr) {
     
         try {
             
             // get cephmap as string 
             ObjectMapper mapper = new ObjectMapper();
             mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
             mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
             mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
             String outJson = mapper.writeValueAsString(CephGlobalParameter.getCephMap());
             IOControl control = new IOControl();
             Session session = new Session(MonitorMsgType.UPDATE_MAP);
             session.set(IOMessageConstants.UPDATE_MAP_KEY_JSON_MAP, outJson);
             for (Address addr1 : addr) {
                 Session response = control.request(session, addr1);
                 boolean isSuccess = response.getBoolean(IOMessageConstants.UPDATE_MAP_RESPONSE_MESSAGE);
                 if(isSuccess){
                  log.i("Update map success to " + addr1.getIp() + ":"+addr1.getPort());
                 }
                 else {
                    String message = response.getString(IOMessageConstants.UPDATE_MAP_FAILED);
                    //retry updating map
                    response = control.request(session, addr1);
                    isSuccess = response.getBoolean(IOMessageConstants.UPDATE_MAP_RESPONSE_MESSAGE);
                    if(isSuccess){
                     log.i("Update map success to " + addr1.getIp() + ":"+addr1.getPort());
                    }
                    else {
                     log.i(response.getString(IOMessageConstants.UPDATE_MAP_FAILED));
                    }
                 }
             }
             log.i("Finished replicating map to back up monitors");
          } catch (Exception ex) {
             Logger.getLogger(ReplicateMap.class.getName()).log(Level.SEVERE, null, ex);
         }
    }

    @Override
    public void run() {
         log.i("Started replicating map");
        replicateMap((ArrayList<Address>) MonitorProperties.getInstance().getBackupMonitors());
    }
}
