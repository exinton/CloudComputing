/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iocontrol;

import cephmapnode.CephMap;
import cephmapnode.CephNode;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.ArrayList;

/**
 *
 * @author Avinash
 */
public class GenerateInitMap {
    public static void main(String[] args) {
        GenerateInitMap.sampleClusterMap();
    }
    public static CephMap sampleClusterMap() {
           
     
        CephMap cm = new CephMap();
       
        CephNode row1 = new CephNode();
        row1.setLevelNo(1);
        row1.setId("1");
        row1.setType("row");
        row1.setIsDisk(false);
       
        CephNode cabinet1 = new CephNode();
        cabinet1.setId("11");
        cabinet1.setLevelNo(2);
        cabinet1.setType("cabinet");
        cabinet1.setIsDisk(false);
       
        CephNode cabinet2 = new CephNode();
        cabinet2.setId("12");
        cabinet2.setLevelNo(2);
        cabinet2.setType("cabinet");
        cabinet2.setIsDisk(false);
       
        CephNode cabinet3 = new CephNode();
        cabinet3.setId("13");
        cabinet3.setLevelNo(2);
        cabinet3.setType("cabinet");
        cabinet3.setIsDisk(false);
       
        CephNode cabinet4 = new CephNode();
        cabinet4.setId("14");
        cabinet4.setLevelNo(2);
        cabinet4.setType("cabinet");
        cabinet4.setIsDisk(false);
        
        CephNode disk1 = new CephNode();
        disk1.setLevelNo(3);
        disk1.setId("111");
        disk1.setType("disk");
        disk1.setWeight(1);
        disk1.setAddress("192.168.1.118", 9090);
        disk1.setIsDisk(true);
       
        CephNode disk2 = new CephNode();
        disk2.setLevelNo(3);
        disk2.setId("112");
        disk2.setType("disk");
        disk2.setWeight(1);
        disk2.setAddress("192.168.1.119", 9090);
        disk2.setIsDisk(true);

        CephNode disk3 = new CephNode();
        disk3.setLevelNo(3);
        disk3.setId("113");
        disk3.setType("disk");
        disk3.setWeight(1);
        disk3.setAddress("192.168.1.124", 9090);
        disk3.setIsDisk(true);
       
        CephNode disk4 = new CephNode();
        disk4.setLevelNo(3);
        disk4.setId("114");
        disk4.setType("disk");
        disk4.setWeight(1);
        disk4.setAddress("192.168.1.126", 9090);
        disk4.setIsDisk(true);
       
        CephNode disk5 = new CephNode();
        disk5.setLevelNo(3);
        disk5.setId("121");
        disk5.setType("disk");
        disk5.setWeight(1);
        disk5.setAddress("192.168.1.127", 9090);
        disk5.setIsDisk(true);
       
        CephNode disk6 = new CephNode();
        disk6.setLevelNo(3);
        disk6.setId("122");
        disk6.setType("disk");
        disk6.setWeight(1);
        disk6.setAddress("192.168.1.117", 9090);
        disk6.setIsDisk(true);
       
        CephNode disk7 = new CephNode();
        disk7.setLevelNo(3);
        disk7.setId("123");
        disk7.setType("disk");
        disk7.setWeight(1);
        disk7.setAddress("192.168.1.108", 9090);
        disk7.setIsDisk(true);
       
        CephNode disk8 = new CephNode();
        disk8.setLevelNo(3);
        disk8.setId("124");
        disk8.setType("disk");
        disk8.setWeight(1);
        disk8.setAddress("192.168.1.116", 9090);
        disk8.setIsDisk(true);
       
        CephNode disk9 = new CephNode();
        disk9.setLevelNo(3);
        disk9.setId("131");
        disk9.setType("disk");
        disk9.setWeight(1);
        disk9.setAddress("192.168.1.125", 9090);
        disk9.setIsDisk(true);
       
        CephNode disk10 = new CephNode();
        disk10.setLevelNo(3);
        disk10.setId("132");
        disk10.setType("disk");
        disk10.setWeight(1);
        disk10.setAddress("192.168.1.128", 9090);
        disk10.setIsDisk(true);

        CephNode disk11 = new CephNode();
        disk11.setLevelNo(3);
        disk11.setId("133");
        disk11.setType("disk");
        disk11.setWeight(1);
        disk11.setAddress("192.168.1.106", 9090);
        disk11.setIsDisk(true);
        
        CephNode disk12 = new CephNode();
        disk12.setLevelNo(3);
        disk12.setId("134");
        disk12.setType("disk");
        disk12.setWeight(1);
        disk12.setAddress("192.168.1.107", 9090);
        disk12.setIsDisk(true);
        
        CephNode disk13 = new CephNode();
        disk13.setLevelNo(3);
        disk13.setId("141");
        disk13.setType("disk");
        disk13.setWeight(1);
        disk13.setAddress("192.168.1.109", 9090);
        disk13.setIsDisk(true);
        
        CephNode disk14 = new CephNode();
        disk14.setLevelNo(3);
        disk14.setId("142");
        disk14.setType("disk");
        disk14.setWeight(1);
        disk14.setAddress("192.168.1.110", 9090);
        disk14.setIsDisk(true);
        
        CephNode disk15 = new CephNode();
        disk15.setLevelNo(3);
        disk15.setId("143");
        disk15.setType("disk");
        disk15.setWeight(1);
        disk15.setAddress("192.168.1.113", 9090);
        disk15.setIsDisk(true);
        
        CephNode disk16 = new CephNode();
        disk16.setLevelNo(3);
        disk16.setId("144");
        disk16.setType("disk");
        disk16.setWeight(1);
        disk16.setAddress("192.168.1.114", 9090);
        disk16.setIsDisk(true);
        
        CephNode disk17 = new CephNode();
        disk17.setLevelNo(3);
        disk17.setId("145");
        disk17.setType("disk");
        disk17.setWeight(1);
        disk17.setAddress("192.168.1.129", 9090);
        disk17.setIsDisk(true);
        
        ArrayList<CephNode> cabinet1disks = new ArrayList<>();
        cabinet1disks.add(disk1);
        cabinet1disks.add(disk2);
        cabinet1disks.add(disk3);
        cabinet1disks.add(disk4); 
       
        ArrayList<CephNode> cabinet2disks = new ArrayList<>();
        cabinet2disks.add(disk5);
        cabinet2disks.add(disk6);        
        cabinet2disks.add(disk7);
        cabinet2disks.add(disk8);
       
        ArrayList<CephNode> cabinet3disks = new ArrayList<>();
        cabinet3disks.add(disk9);
        cabinet3disks.add(disk10);
        cabinet3disks.add(disk11);
        cabinet3disks.add(disk12);
               
        ArrayList<CephNode> cabinet4disks = new ArrayList<>();
        cabinet4disks.add(disk13);
        cabinet4disks.add(disk14);
        cabinet4disks.add(disk15);
        cabinet4disks.add(disk16);
        cabinet4disks.add(disk17);
        
        cabinet1.setChildren(cabinet1disks);
        cabinet2.setChildren(cabinet2disks);
        cabinet3.setChildren(cabinet3disks);
        cabinet4.setChildren(cabinet4disks);                
       
        ArrayList<CephNode> row1cabinets = new ArrayList<>();       
        row1cabinets.add(cabinet1);
        row1cabinets.add(cabinet2);
        row1cabinets.add(cabinet3);     
        row1cabinets.add(cabinet4);
        
        row1.setChildren(row1cabinets);
       
        CephNode root = new CephNode();
        root.setLevelNo(0);
        root.setId("0");
        root.setType("root");
        root.setIsDisk(false);
               
        ArrayList<CephNode> rtChildren = new ArrayList<>();
        rtChildren.add(row1);

        root.setChildren(rtChildren);
       
        cm.setEpochVal(System.currentTimeMillis());
        cm.setNode(root);
        cm.updateHashRange();
//        updateWeights(cm.getNode());
       
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.setSerializationInclusion(Include.NON_NULL);
        try {
            String outJson = mapper.writeValueAsString(cm);
            System.out.println(outJson);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
        return cm;
    }
    
    /*
    static Monitor.NodeWeight updateWeights(CephNode n) {
        if (n.getIsDisk()) {
            return (new Monitor.NodeWeight(n.getWeight(),
                    n.getHiddenWeight()));

        } else {
            Monitor.NodeWeight nw = new Monitor.NodeWeight(0, 0);
            n.getChildren().stream().map((i) -> updateWeights(i)).map((childWeight) -> {
                nw.weight += childWeight.weight;
                return childWeight;
            }).forEach((childWeight) -> {
                nw.hiddenWeight += childWeight.hiddenWeight;
            });
            
            n.setWeight(nw.weight);
            n.setHiddenWeight(nw.hiddenWeight);

            return nw;
        }

    }
    */
}
