/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crush;

import cephmapnode.CephMap;
import cephmapnode.CephNode;
import java.util.ArrayList;
import crush.util.CrushUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.Session;
import types.SystemInfoMsgType;
import net.IOControl;

/**
 *
 * @author Avinash
 */
public class CrushRun {

    private final ArrayList<CrushLevel> crushLevels;
    private static CrushRun instance = null;

    public static CrushRun getInstance() {
        if (instance == null) {
            instance = new CrushRun();
        }
        return instance;
    }

    protected CrushRun() {
        //read crush properties and set crush levels
        crushLevels = CrushUtil.setCrushProperty(null);
    }

    public CephNode take(CephMap map) {

        return map.getNode();
    }

//    public ArrayList<CephNode> runCrush(CephMap cephmap, String objectName, boolean ... addnFlags ){
//        
//        System.out.println("Recived Crush Request : Begin");
//        //test if the crush can be run
//        boolean isBalanced = testForUnbalancedCrushRecursive(0,take(cephmap));
//        if(!isBalanced) {
//            try {
//                throw new Exception(" Please check crush map properties , number of child nodes not equal to replication");
//              } catch (Exception ex) {
//                Logger.getLogger(CrushRun.class.getName()).log(Level.SEVERE, null, ex);
//                return new ArrayList<CephNode>();
//            }
//        }
//        
//        boolean includeHiddenNodes = false;
//        if(addnFlags.length > 0){
//            includeHiddenNodes = addnFlags[0];
//        }
//            
//        
//        ArrayList<CephNode> input = new ArrayList<>();
//        input.add(take(cephmap));
//        System.out.println("Running Select on object " + objectName);
//        for (int i = 0; i < getCrushLevels().size(); i++) {
//            
//            ArrayList<CephNode> tempInput = new ArrayList<>(input);
//            tempInput = select(getCrushLevels().get(i), i, input, objectName, i + 1, includeHiddenNodes);
//            input.clear();
//            input = tempInput;
//
//        }
//        
//        return input;
//    }
    public ArrayList<CephNode> runCrush(CephMap cephmap, String objectName, String opType, boolean... addnFlags) {

        System.out.println("Recived Crush Request : Begin");
        //test if the crush can be run
        boolean isBalanced = testForUnbalancedCrushRecursive(0, take(cephmap));//what does balance man here.
        if (!isBalanced) {
            try {
                throw new Exception(" Please check crush map properties , number of child nodes not equal to replication");
            } catch (Exception ex) {
                Logger.getLogger(CrushRun.class.getName()).log(Level.SEVERE, null, ex);
                return new ArrayList<CephNode>();
            }
        }

        boolean includeHiddenNodes = false;
        if (addnFlags.length > 0) {
            includeHiddenNodes = addnFlags[0];
        }

        ArrayList<CephNode> input = new ArrayList<>();
        input.add(take(cephmap));
//        System.out.println("Running Select on object " + objectName);
        for (int i = 0; i < getCrushLevels().size(); i++) {

            ArrayList<CephNode> tempInput = new ArrayList<>(input);
            tempInput = select(getCrushLevels().get(i),
                    i, input,
                    objectName,
                    i + 1,
                    includeHiddenNodes, opType);
            input.clear();
            input = tempInput;

        }

        return input;
    }

//    private ArrayList<CephNode> select(CrushLevel level, int inputLevel,
//                          ArrayList<CephNode> input, 
//                          String fileName, 
//                          int outputLevelNo,
//                          boolean includeHiddenNodes)
//    {
//
//        ArrayList<CephNode> output = new ArrayList<>();
//        for(int i = 0; i < input.size(); i++) {
//            int failure = 0;
//
//            for (int replicaCounter = 1; replicaCounter <= level.getLevelReplica(); replicaCounter++) {
//            
//                int failForThisReplica = 0;
//                boolean retry_decent = false;
//                CephNode bucketOutput = null;
//                do {
//                    retry_decent = false;
//                    boolean retry_bucket = false;
//                    
//                    ArrayList<CephNode> bucket = new ArrayList<>( input.get(i).getChildren());  //start decent at bucket i
//                    
//                    for (CephNode bucket1 : bucket) {
//
//                        if (bucket1.isIsAlreadySelected()) {
//                            bucket.remove(bucket1);
//                        }
//                    }
//
//                    do {
//                        retry_bucket = false;
//                        int rNext = 0;
//                        rNext = replicaCounter + failure;
//                        
////                        if (firstN()) {
////                            rNext = replicaCounter + failure;
////                        } else {
////                            rNext = replicaCounter + (failForThisReplica * level.getLevelReplica());
////                        }
//                       
//                        for (CephNode bucket1 : bucket) {
//                            if (bucket1.isIsAlreadySelected()) {
//                                bucket.remove(bucket1);
//                            }
//                        }
//
//                        bucketOutput = choose(bucket, rNext, fileName, includeHiddenNodes);
//
//                        if (bucketOutput.getLevelNo() != outputLevelNo) {
//                            //b,<- bucket(o)
//                            bucket = bucketOutput.getChildren();
//                        } else if (output.contains(bucketOutput) || failed(bucketOutput) || overload(bucketOutput,op )) {
//                            //fr <- fr + 1 , f <- f +1
//                            failForThisReplica = failForThisReplica + 1;
//                            failure = failure + 1;
//                            
//                            if (output.contains(bucketOutput) && failForThisReplica < 3) {
//                                //bucketOutput.setIsAlreadySelected(true);
//                                retry_bucket = true;  //retry collisons locallly
//                            } else {
//                                retry_decent = true; //other wise descent from i
//                            }
//                        }
//                    } while (retry_bucket);
//
//                } while (retry_decent);
//
//                output.add(bucketOutput);
//            } //end for
//        } //end for
//
//        //copy back input
//        
//        return output;
//    }
    private ArrayList<CephNode> select(CrushLevel level, int inputLevel,
            ArrayList<CephNode> input,
            String fileName,
            int outputLevelNo,
            boolean includeHiddenNodes,
            String opType) {

        ArrayList<CephNode> output = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            int failure = 0;

            for (int replicaCounter = 1; replicaCounter <= level.getLevelReplica(); replicaCounter++) {

                int failForThisReplica = 0;
                boolean retry_decent = false;
                CephNode bucketOutput = null;
                do {
                    retry_decent = false;
                    boolean retry_bucket = false;

                    ArrayList<CephNode> bucket = new ArrayList<>(input.get(i).getChildren());  //start decent at bucket i

                    for (CephNode bucket1 : bucket) {

                        if (bucket1.isIsAlreadySelected()) {
                            bucket.remove(bucket1);
                        }
                    }

                    do {
                        retry_bucket = false;
                        int rNext = 0;
                        rNext = replicaCounter + failure;

//                        if (firstN()) {
//                            rNext = replicaCounter + failure;
//                        } else {
//                            rNext = replicaCounter + (failForThisReplica * level.getLevelReplica());
//                        }
                        for (CephNode bucket1 : bucket) {
                            if (bucket1.isIsAlreadySelected()) {
                                bucket.remove(bucket1);
                            }
                        }

                        bucketOutput = choose(bucket, rNext, fileName, includeHiddenNodes);
//                        System.out.println("select::bucketOutput:"+bucketOutput.getId());

                        if (bucketOutput.getLevelNo() != outputLevelNo) {
                            //b,<- bucket(o)
                            bucket = bucketOutput.getChildren();
                        } else if (output.contains(bucketOutput) || failed(bucketOutput)
                                || overload(bucketOutput, opType, fileName)) {
//                            System.out.println("select:: im inside failed, overload condition");
                            //fr <- fr + 1 , f <- f +1
                            failForThisReplica = failForThisReplica + 1;
                            failure = failure + 1;

                            if (output.contains(bucketOutput) && failForThisReplica < 3) {
                                //bucketOutput.setIsAlreadySelected(true);
                                retry_bucket = true;  //retry collisons locallly
                            } else {
                                retry_decent = true; //other wise descent from i
                            }
                        }
                    } while (retry_bucket);

                } while (retry_decent);

                output.add(bucketOutput);
            } //end for
        } //end for

        //copy back input
        return output;
    }

//private ArrayList<CephNode> select(CrushLevel level, int inputLevel,
//                          ArrayList<CephNode> input, 
//                          String fileName, 
//                          int outputLevelNo,
//                          boolean includeHiddenNodes)
//    {
//
//        ArrayList<CephNode> output = new ArrayList<>();
//        for(int i = 0; i < input.size(); i++) {
//            int failure = 0;
//
//            for (int replicaCounter = 1; replicaCounter <= level.getLevelReplica(); replicaCounter++) {
//            
//                int failForThisReplica = 0;
//                boolean retry_decent = false;
//                CephNode bucketOutput = null;
//                do {
//                    retry_decent = false;
//                    boolean retry_bucket = false;
//                    
//                    ArrayList<CephNode> bucket = new ArrayList<>( input.get(i).getChildren());  //start decent at bucket i
//                    
//                    for (CephNode bucket1 : bucket) {
//
//                        if (bucket1.isIsAlreadySelected()) {
//                            bucket.remove(bucket1);
//                        }
//                    }
//
//                    do {
//                        retry_bucket = false;
//                        int rNext = 0;
//                        rNext = replicaCounter + failure;
//                        
////                        if (firstN()) {
////                            rNext = replicaCounter + failure;
////                        } else {
////                            rNext = replicaCounter + (failForThisReplica * level.getLevelReplica());
////                        }
//                       
//                        for (CephNode bucket1 : bucket) {
//                            if (bucket1.isIsAlreadySelected()) {
//                                bucket.remove(bucket1);
//                            }
//                        }
//
//                        bucketOutput = choose(bucket, rNext, fileName, includeHiddenNodes);
//
//                        if (bucketOutput.getLevelNo() != outputLevelNo) {
//                            //b,<- bucket(o)
//                            bucket = bucketOutput.getChildren();
//                        } else if (output.contains(bucketOutput) || failed(bucketOutput) || overload(bucketOutput)) {
//                            //fr <- fr + 1 , f <- f +1
//                            failForThisReplica = failForThisReplica + 1;
//                            failure = failure + 1;
//                            
//                            if (output.contains(bucketOutput) && failForThisReplica < 3) {
//                                //bucketOutput.setIsAlreadySelected(true);
//                                retry_bucket = true;  //retry collisons locallly
//                            } else {
//                                retry_decent = true; //other wise descent from i
//                            }
//                        }
//                    } while (retry_bucket);
//
//                } while (retry_decent);
//
//                output.add(bucketOutput);
//            } //end for
//        } //end for
//
//        //copy back input
//        
//        return output;
//    }
    private boolean firstN() {
        //need to check implementaion
        return true;
    }

    private boolean failed(CephNode bucketOutput) {
        //implement 
//        System.out.println("************ am failed ?" + bucketOutput.getIsFailed() + "myid :" + bucketOutput.getId());
//        if(bucketOutput.getIsDisk() && bucketOutput.getAddress().getIp() == "192.168.1.119" )
//            System.out.println("************8I am failed ?" + bucketOutput.getIsFailed());
        if (bucketOutput.getIsFailed()) {
            return true;
        }
        return false;
    }

    private boolean overload(CephNode node, String opType, String fileName) {
//      System.out.println("overload::opType:"+opType );

        // System.out.println( " node ip" + node.getAddress().getIp()  );
//       System.out.println( " node id" + node.getId()  );
//        System.out.println("node overloaded ?" + node.getIsOverloaded() );
        if (node.getIsOverloaded()) {
            switch (opType.toUpperCase()) {
                case "READ":
               //check to ping osd

                    return !isFilePresentinOSD(node, fileName);
                case "UPDATE":
                    //check to ping osd
                    return false;
                case "CREATE":
                    return false;
                case "WRITE":
                    return false;
                case "DELETE":
                    return false;
                case "OVERLOAD":
//               System.out.println("Getting overload request");
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public static int calculateWeight(ArrayList<CephNode> bucketList, int index) {
        int totalWeight = 0;
        for (int i = index; i < bucketList.size(); i++) {
            totalWeight += bucketList.get(i).getWeight();
        }
        return totalWeight;
    }

    public static CephNode choose(ArrayList<CephNode> bucketList, int repNum, String filename, boolean includeHiddenNodes) {

        int index = 0;
        double totalWeight = 0;
        //String filename = "Helloorld.txt";
        CrushHash calHash = new CrushHash();
        totalWeight = calculateWeight(bucketList, 0);
        // System.out.println("TotalWeight:" + totalWeight);
        for (int j = 0; j < bucketList.size(); j++) {

//            totalWeight = calculateWeight(bucketList, j);
            double hashValue = calHash.hashFunction(filename, repNum, bucketList.get(j).getId());
//            double nodeHash = bucketList.get(j).getWeight() / totalWeight;

            double nodeHash;
            if (includeHiddenNodes) {
                nodeHash = bucketList.get(j).getHiddenHashRange();
            } else {
                nodeHash = bucketList.get(j).getHashRange();
            }

//            System.out.println("Nodehash:" + j + " " + nodeHash + " " + includeHiddenNodes);
            // System.out.println("Hashvalue:" + j + " " + hashValue);
            if (hashValue < nodeHash) {
                index = j;
                break;
            }

        }
        return bucketList.get(index);
    }

    private int getReplicationFromConfig(ArrayList<CrushLevel> crushLevels) {

        int getReplicationFromConfig = 1;

        for (CrushLevel crushLevel : crushLevels) {
            getReplicationFromConfig = getReplicationFromConfig * crushLevel.getLevelReplica();
        }
        return getReplicationFromConfig;
    }

    private boolean testForUnbalancedCrushRecursive(int crushLevel, CephNode input) {
        if (input.getChildren() == null || input.getChildren().size() == 0) {
            return true;
        }
        if (crushLevel > getCrushLevels().size()) {
            return true;
        }
        CrushLevel nextLevel = getCrushLevels().get(crushLevel);
        int replicaExpected = nextLevel.getLevelReplica();
        int childrenCount = input.getChildren().size();
        if (replicaExpected > childrenCount) {
            return false;
        }
        ArrayList<CephNode> children = input.getChildren();
        boolean testChildren = true;
        for (CephNode children1 : children) {
            testChildren = testForUnbalancedCrushRecursive(crushLevel + 1, children1);
            if (!testChildren) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the crushLevels
     */
    public ArrayList<CrushLevel> getCrushLevels() {
        return crushLevels;
    }

    private boolean isFilePresentinOSD(CephNode node, String fileName) {

        try {
            Session input = new Session(SystemInfoMsgType.FILE_EXIST);
            input.set("fileName", fileName);

            System.out.println("Checking if the file is moved for overloaded node");
            IOControl iocontrol = new IOControl();
            Session response = iocontrol.request(input, node.getAddress());
            System.out.println("Recived reply  " + response.getBoolean("fileExist"));
            return response.getBoolean("fileExist");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

}
