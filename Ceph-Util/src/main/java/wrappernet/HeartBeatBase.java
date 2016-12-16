/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrappernet;
import java.util.List;


import net.Address;
import net.IOControl;
import net.Session;
import types.SystemInfoMsgType;
import types.WrapperMsgType;
import util.Log;
import wrapper.Wrapper;
import wrapper.WrapperHolder;

public class HeartBeatBase implements Runnable {

    private static Log log = Log.get();
    public IOControl control =null;
    private WrapperHolder wrapper;
    
    public HeartBeatBase(WrapperHolder wrapper,IOControl control){
    	this.wrapper=wrapper;
    	this.control=control;
    }
    
    @Override
    public void run() {
        executeHeartBeat();
    }

    private void executeHeartBeat() {

        List<Address> nodeList = wrapper.getWrapper().getRealServerList();
        for (Address node : nodeList) {
            int maxNodeTry = 3;
            int i = 0;
            while (i < maxNodeTry) {
                try {
                    Session heartBeat = new Session(WrapperMsgType.HEARTBEAT);  
                    heartBeat.set("epochVal", wrapper.getWrapper().getEpochVal());
                    System.out.println("send heart beat to "+node.getIp()+":"+node.getPort()+"with version"+wrapper.getWrapper().getEpochVal());            
                    control.send(heartBeat, node.getIp(), node.getPort());              
                    break;
                } catch (Exception ex) {
                    log.i("Excpetion " + ex.getMessage());
                    ex.printStackTrace();
                    System.out.println("Failed to contact node, starting load balancer");                   
                }
                i++;
            }

        }
    }



}
