/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cephmapnode;

import crush.CrushLevel;
import crush.CrushRun;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import crush.FileListInfo;
import types.FileBackup;
import net.Address;
import net.IOControl;
import net.Session;

/**
 *
 * @author Avinash
 */
public class CephMap {

    private long epochVal;
    private CephNode node;
    private ArrayList<Address> nodeList;

    private boolean updating;

    public CephMap() {
        this.epochVal = System.currentTimeMillis();
        this.node = new CephNode();
        this.nodeList = new ArrayList<Address>();
    }

    public void setEpochVal(long epochVal) {
        this.epochVal = epochVal;
    }

    public void upateEpochVal() {
        this.epochVal = System.currentTimeMillis();

    }

    public long getEpochVal() {
        return this.epochVal;
    }

    /**
     * @return the node
     */
    public CephNode getNode() {
        return this.node;
    }

    /**
     * @param node the node to set
     */
    public void setNode(CephNode node) {
        this.node = node;
    }

    public void displayClusterMap() {
        System.out.println("in displayClusterMap");

        CephNode root = this.getNode();
        Queue<InnerClass> q = new LinkedList<>();
        q.add(new InnerClass(root, 0, "-1"));

        int prev_level = -1;
        while (!q.isEmpty()) {
            InnerClass i = q.poll();

            if (i.level != prev_level) {
                System.out.println();
                System.out.print("Level " + i.level + " : ");
            }

            String addr = "";
            if (i.n.getIsDisk()) {
                addr = " IP:" + i.n.getAddress().getIp() + " " + i.n.getAddress().getPort() + " ";
            }

            System.out.print(i.n.getId() + "("
                    + "Pr:" + i.parent_id
                    + "wt:" + i.n.getWeight() + " "
                    + "hwt:" + i.n.getHiddenWeight() + " "
                    + "hr:" + i.n.getHashRange() + " "
                    + "hhr:" + i.n.getHiddenHashRange() + " "
                    + addr
                    + (i.n.getIsHidden() ? " Hdn" : "")
                    + (i.n.getIsFailed() ? " Fld" : "")
                    + (i.n.getIsOverloaded() ? " OvrLd" : "")
                    + ") ");

            for (CephNode cn : i.n.getChildren()) {
                q.add(new InnerClass(cn, i.level + 1, i.n.getId()));
            }

            prev_level = i.level;
        }
        System.out.println();
    }
    
    public void printMap(){
     printMap(this.getNode(),0);
    }
    
    public void printMap(CephNode node , int level){
    
        if(node == null)
                  return;

             for(int i=0; i < level ; i++)
                   System.out.print("| ");
              System.out.print(node.getId()+ " : " );
              if(node.getIsDisk()) {
                  System.out.print( "ip:" + node.getAddress().getIp());
                  System.out.print(" overload:" + node.getIsOverloaded());
                  System.out.print(" failed:" + node.getIsFailed());
              }
              System.out.println();
              if(node.getChildren() !=null) {
                  
                  if(node.getId() == null)
                      System.out.print( node.getId());
                  else {
                  
                      for (CephNode mynode : node.getChildren()) {
                          printMap(mynode,level+1);
                      }
                      
                  }
                      
              }
    }

    /**
     * @return the updating
     */
    public boolean isUpdating() {
        return updating;
    }

    /**
     * @param updating the updating to set
     */
    public void setUpdating(boolean updating) {
        this.updating = updating;
    }

    static class InnerClass {

        CephNode n;
        int level;
        String parent_id;

        InnerClass(CephNode n, int level, String parent_id) {
            this.n = n;
            this.level = level;
            this.parent_id = parent_id;
        }
    }

    public void updateHashRange() {
        CephNode root = this.getNode();
        updateWeights(root);

        Queue<CephNode> q = new LinkedList<>();
        root.setHashRange(1.00D);
        root.setHiddenHashRange(1.00D);
        q.add(root);
        while (!q.isEmpty()) {
            CephNode i = q.poll();
            double totalWeight = i.getWeight();
            double totalHiddenWeight = i.getHiddenWeight();
            for (CephNode child : i.getChildren()) {
                q.add(child);
                double weight = child.getWeight();
                double hiddenWeight = child.getHiddenWeight();
                child.setHashRange(weight / (totalWeight));
                child.setHiddenHashRange((weight + hiddenWeight) / (totalHiddenWeight + totalWeight));

                totalWeight -= weight;
                totalHiddenWeight -= hiddenWeight;
            }
        }
    }

    /*
     this is post order traversal of CephMap and updating the weights
     */
    private NodeWeight updateWeights(CephNode n) {
        if (n.getIsDisk()) {
            return (new NodeWeight(n.getWeight(),
                    n.getHiddenWeight()));

        } else {
            NodeWeight nw = new NodeWeight(0, 0);
            for (CephNode i : n.getChildren()) {
                NodeWeight childWeight = updateWeights(i);
                nw.weight += childWeight.weight;
                nw.hiddenWeight += childWeight.hiddenWeight;
            }

            n.setWeight(nw.weight);
            n.setHiddenWeight(nw.hiddenWeight);

            return nw;
        }

    }

    static class NodeWeight {

        double weight;
        double hiddenWeight;

        NodeWeight() {
        }

        NodeWeight(double weight, double hiddenWeight) {
            this.weight = weight;
            this.hiddenWeight = hiddenWeight;
        }
    }

    public void makeHiddenNodesActive() {
        CephNode root = this.getNode();

        Queue<CephNode> q = new LinkedList<>();

        q.add(root);
        while (!q.isEmpty()) {
            CephNode i = q.poll();

            if (i.getIsHidden()) {
                i.setIsHidden(false);

                if (i.getIsDisk()) {
                    i.setWeight(i.getHiddenWeight());
                    i.setHiddenWeight(0.0D);
                }
            }
            for (CephNode child : i.getChildren()) {
                q.add(child);
            }
        }

        this.updateHashRange();
        this.upateEpochVal();
    }

    public ArrayList<Address> getNodeList() {
        return nodeList;
    }

    public void setNodeList(ArrayList<Address> nodeList) {
        this.nodeList = nodeList;
    }

    public void changeBackup(Address address) {
        System.out.println("in changeBackup");
        IOControl control = new IOControl();
        ArrayList<CephNode> allNode = new ArrayList<CephNode>();
        ArrayList<Address> tempAddress = new ArrayList<Address>();
        allNode.add(this.node);
        while (!allNode.isEmpty()) {
            CephNode tempNode = allNode.remove(0);
            if (tempNode.getIsDisk() && !tempNode.getIsFailed()) {
                tempAddress.add(tempNode.getAddress());
            } else {
                allNode.addAll(tempNode.getChildren());
            }
        }
        /*
        System.out.println("list from cephmap after adding/failing a node:");
        for(Address adr: tempAddress){
            System.out.println(adr.getIp()+" ");
        }
        System.out.println("EOL");
        System.out.println("nodeList structure from CephMap:");
        for(Address adr: nodeList){
            System.out.println(adr.getIp()+" ");
        }
        System.out.println("EOL");
        */
        try {
            if (tempAddress.size() < nodeList.size()) {
                int index = nodeList.lastIndexOf(address);
                
                int prevAddress = (index == 0) ? (nodeList.size() - 1) : index - 1;
                int nextAddress = (index == (nodeList.size() - 1)) ? 0 : index + 1;
                
//                System.out.println("prevAddress:"+prevAddress+"; nextAddress:"+nextAddress);

                Session sessionPrev = new Session(FileBackup.TRANSFER_FILE_LIST);
                sessionPrev.set("action", "get");
                sessionPrev.set("location", "this");
                
                Session sessionNext = new Session(FileBackup.TRANSFER_FILE_LIST);
                sessionNext.set("action", "get");
                sessionNext.set("location", "this");
                
                Session replyPrev = control.request(sessionPrev, nodeList.get(prevAddress));
                Session replyNext = control.request(sessionNext, nodeList.get(nextAddress));

                Session addPrev = new Session(FileBackup.TRANSFER_FILE_LIST);
                addPrev.set("action", "set");
                addPrev.set("location", "prev");
                addPrev.set("fileList", replyPrev.get("fileList", FileListInfo.class));
                control.send(addPrev, nodeList.get(nextAddress));

                Session addNext = new Session(FileBackup.TRANSFER_FILE_LIST);
                addNext.set("action", "set");
//              addNext.set("location", "prev");
                addNext.set("location", "next");
                addNext.set("fileList", replyNext.get("fileList", FileListInfo.class));
                control.send(addNext, nodeList.get(prevAddress));
            }
            if (tempAddress.size() > nodeList.size()) {
                int index = tempAddress.lastIndexOf(address);

                int prevAddress = (index == 0) ? (nodeList.size() - 1) : index - 1;
                int nextAddress = (index == (nodeList.size() - 1)) ? 0 : index;
                
//                System.out.println("prevAddress:"+prevAddress+"; nextAddress:"+nextAddress);

                Session sessionPrev = new Session(FileBackup.TRANSFER_FILE_LIST);
                sessionPrev.set("action", "get");
                sessionPrev.set("location", "this");
                
                Session sessionNext = new Session(FileBackup.TRANSFER_FILE_LIST);
                sessionNext.set("action", "get");
                sessionNext.set("location", "this");
                
//                System.out.println("here1");

                Session replyPrev = control.request(sessionPrev, nodeList.get(prevAddress));
                Session replyNext = control.request(sessionNext, nodeList.get(nextAddress));

                // set next node's previous list as empty, because current (new) node has zero files
                Session addPrev = new Session(FileBackup.TRANSFER_FILE_LIST);
                addPrev.set("action", "set");
                addPrev.set("location", "prev");
                addPrev.set("fileList", new FileListInfo());
                control.send(addPrev, nodeList.get(nextAddress));
                
//                System.out.println("here2");

                // set prev node's next list as empty, because current (new) node has zero files
                Session addNext = new Session(FileBackup.TRANSFER_FILE_LIST);
                addNext.set("action", "set");
                addNext.set("location", "next");
                addNext.set("fileList", new FileListInfo());
                control.send(addNext, nodeList.get(prevAddress));
                
//                System.out.println("here3");

                // set previous node's 'this' list to current (new) node's prev list
                Session addPrevThis = new Session(FileBackup.TRANSFER_FILE_LIST);
                addPrevThis.set("action", "set");
                addPrevThis.set("location", "prev");
                
                addPrevThis.set("fileList", replyPrev.get("fileList", FileListInfo.class));
                control.send(addPrevThis, address);
                
//                System.out.println("here4");

                // set next node's 'this' list to current (new) node's next list
                Session addNextThis = new Session(FileBackup.TRANSFER_FILE_LIST);
                addNextThis.set("action", "set");
                addNextThis.set("location", "next");
                addNextThis.set("fileList", replyNext.get("fileList", FileListInfo.class));
                control.send(addNextThis, address);
                
//                System.out.println("here5");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        nodeList = tempAddress;
        System.out.println("nodeList after changes:");
        for(Address adr: nodeList){
            System.out.println(adr.getIp()+" ");
        }
        System.out.println("EOL");
    }

    public CephNode getCephNodeWithID(String ID) {
        CephNode result = null;
        Queue<CephNode> q = new LinkedList<>();
        q.add(this.getNode());
        while (!q.isEmpty()) {
            CephNode n = q.poll();
            if (n.getId().equals(ID)) {
                result = n;
                break;
            }
            for (CephNode cn : n.getChildren()) {
                q.add(cn);
            }
        }
        return result;
    }

    public void populateNodeList() {
        ArrayList<CephNode> allNode = new ArrayList<CephNode>();
        allNode.add(this.node);
        nodeList.clear();
        while (!allNode.isEmpty()) {
            CephNode tempNode = allNode.remove(0);
            if (tempNode.getIsDisk()) {
                nodeList.add(tempNode.getAddress());
            } else {
                allNode.addAll(tempNode.getChildren());
            }
        }
    }

    public ArrayList<Address> getAdjacent(String ip) {
        ArrayList<Address> result = new ArrayList<Address>();
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).getIp().equals(ip)) {
                if (i == 0) {
                    result.add(nodeList.get(nodeList.size() - 1));
                } else {
                    result.add(nodeList.get(i - 1));
                }
                if (i == nodeList.size() - 1) {
                    result.add(nodeList.get(0));
                } else {
                    result.add(nodeList.get(i + 1));
                }
                return result;
            }
        }
        return result;
    }

    public CephNode getNodeByIP(String ip) {
        ArrayList<CephNode> availableOSDList = getAvailableOSDList();

        for (CephNode osd : availableOSDList) {
            if (osd.getAddress().getIp().equals(ip)) {
                return osd;
            }
        }
        return null;
    }

    public ArrayList<CephNode> getAvailableOSDList() {

        CrushRun cr = CrushRun.getInstance();
        ArrayList<CrushLevel> crushLevels = cr.getCrushLevels();
        CrushLevel crushLevel = crushLevels.get(crushLevels.size() - 1);
        ArrayList<CephNode> osdList = new ArrayList<>();
        getRescursiveOSDList(this.getNode(), osdList, crushLevel.getLevelno());
        return osdList;
    }


    private static void getRescursiveOSDList(CephNode cephNode, ArrayList<CephNode> osdList, int levelno) {

        if (cephNode.getLevelNo() == levelno) {
            osdList.add(cephNode);
            return;
        } else {
            ArrayList<CephNode> children = cephNode.getChildren();
            if (children == null || children.size() == 0) {
                return;
            } else {
                for (CephNode children1 : children) {
                    getRescursiveOSDList(children1, osdList, levelno);
                }
            }
        }
    }

}
