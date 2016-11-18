/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iocontrol;

/**
 *
 * @author vivek
 */

import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmap.loadbalance.RunLoadBalancer;
import cephmapnode.CephMap;
import cephmapnode.CephNode;

import java.util.Date;
import net.Address;
import net.IOControl;

public class RelocateFIlesOnFailedNode implements Runnable {
    Thread t;
    public IOControl control = new IOControl();
    CephNode failed_node;
    RelocateFIlesOnFailedNode(CephNode failed_node){
        t = new Thread(this, (new Date()).toString());
        this.failed_node = failed_node;
        System.out.println("Starting new thread to move files on node: "+failed_node.getAddress().getIp());
        t.start();
    }
    
    @Override
    public void run(){
        RunLoadBalancer.moveFilesOnNodeFail(control, failed_node.getAddress());
        Monitor.modify_map_remove(failed_node);
    }
}
