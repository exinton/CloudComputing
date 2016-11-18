package osd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import util.Log;
import net.Address;
import crush.FileListInfo;

public class OSDGlobalParameters {

    static class fileInfo {

        public long size;
        public ArrayList<Long> requestInfo;

        public fileInfo() {
            size = 0;
            requestInfo = new ArrayList<Long>();
        }
    }
    private static int readRequestCounter = 0;
    private static int writeRequestCounter = 0;
    private static Map<String, fileInfo> fileStats = new HashMap<String, fileInfo>();
    private static int timeout = 300;
    private static int threshold = 50;
    private static long diskUsed = 0;
    private static long epoch = 0;
    private static String cephMonitors = null;

    private static Address nextOSD = new Address("127.0.0.1", 1234);
    private static Address prevOSD = new Address("127.0.0.1", 1234);

    private static FileListInfo nextNode = new FileListInfo();
    private static FileListInfo prevNode = new FileListInfo();
    private static FileListInfo thisNode = new FileListInfo();

    public static Map<String, fileInfo> getFileStats() {
        return fileStats;
    }

    public static void setFileStats(Map<String, fileInfo> fileStats) {
        OSDGlobalParameters.fileStats = fileStats;
    }

    public static long getDiskUsed() {
        return diskUsed;
    }

    public static void setDiskUsed(long diskUsed) {
        OSDGlobalParameters.diskUsed = diskUsed;
    }

    public static int getThreshold() {
        return threshold;
    }

    public static void incrementDiskUsed(long size) {
        diskUsed += size;
    }

    public static void decrementDiskUsed(long size) {
        diskUsed -= size;
    }

    public static void setThreshold(int threshold) {
        OSDGlobalParameters.threshold = threshold;
    }

    public static int getTimeout() {
        return timeout;
    }

    public static void setTimeout(int timeout) {
        OSDGlobalParameters.timeout = timeout;
    }

    public static int getReadRequestCounter() {
        return readRequestCounter;
    }

    public static void setReadRequestCounter(int readRequestCounter) {
        OSDGlobalParameters.readRequestCounter = readRequestCounter;
    }

    public static int getWriteRequestCounter() {
        return writeRequestCounter;
    }

    public static void setWriteRequestCounter(int writeRequestCounter) {
        OSDGlobalParameters.writeRequestCounter = writeRequestCounter;
    }

    public static void incrementReadRequestConter(String file) {
        OSDGlobalParameters.readRequestCounter++;
        computeStats(file);
    }

    public static void incrementWriteRequestConter(String file) {
        OSDGlobalParameters.writeRequestCounter++;
        computeStats(file);
    }

    public static void addFile(String filename, Long size) {
        if (fileStats.containsKey(filename)) {
            thisNode.addFile(filename, size);
            decrementDiskUsed(fileStats.get(filename).size);
            incrementDiskUsed(size);
        } else {
            fileInfo info = new fileInfo();
            info.size = size;
            info.requestInfo.add(System.currentTimeMillis());
            fileStats.put(filename, info);
            thisNode.addFile(filename, size);
            incrementDiskUsed(size);
        }
    }

    public static void deleteFile(String file) {
        fileStats.remove(file);
        thisNode.delFile(file);
    }

    private static void computeStats(String fileName) {
        if (fileStats.containsKey(fileName)) {
            fileInfo presentStats = fileStats.get(fileName);
            presentStats.requestInfo.add(System.currentTimeMillis());
            while (presentStats.requestInfo.get(0) < (System.currentTimeMillis() - timeout)) {
                presentStats.requestInfo.remove(0);
            }
            fileStats.put(fileName, presentStats);
        } else {
            fileInfo presentStats = new fileInfo();
            presentStats.requestInfo.add(System.currentTimeMillis());
            fileStats.put(fileName, presentStats);
        }
    }

    public static FileListInfo getNextNode() {
        return nextNode;
    }

    public static void setNextNode(FileListInfo nextNode) {
        OSDGlobalParameters.nextNode = nextNode;
    }

    public static FileListInfo getPrevNode() {
        return prevNode;
    }

    public static void setPrevNode(FileListInfo prevNode) {
        OSDGlobalParameters.prevNode = prevNode;
    }

    public static FileListInfo getThisNode() {
        return thisNode;
    }

    public static void setThisNode(FileListInfo thisNode) {
        OSDGlobalParameters.thisNode = thisNode;
    }

    public static void addPrevNode(String filename, Long size) {
        prevNode.addFile(filename, size);
    }

    public static void addNextNode(String filename, Long size) {
        nextNode.addFile(filename, size);
    }

    public static void delPrevNode(String filename) {
        prevNode.delFile(filename);
    }

    public static void delNextNode(String filename) {
        nextNode.delFile(filename);
    }

    public static void setPrevOverLoaded(String filename) {
        prevNode.setOverloaded(filename);
    }

    public static void setNextOverLoaded(String filename) {
        nextNode.setOverloaded(filename);
    }

    public static void setThisOverLoaded(String filename) {
        thisNode.setOverloaded(filename);
    }

    public static long getEpoch() {
        return epoch;
    }

    public static void setEpoch(long epoch) {
        OSDGlobalParameters.epoch = epoch;
    }

    public static Address getNextOSD() {
        return nextOSD;
    }

    public static void setNextOSD(Address nextOSD) {
        OSDGlobalParameters.nextOSD = nextOSD;
    }

    public static Address getPrevOSD() {
        return prevOSD;
    }

    public static void setPrevOSD(Address prevOSD) {
        OSDGlobalParameters.prevOSD = prevOSD;
    }

    public static String getCephMonitors() {
        return cephMonitors;
    }

    public static void setCephMonitors(String cephMonitors) {
        OSDGlobalParameters.cephMonitors = cephMonitors;
    }

    public static void printList(FileListInfo list) {
        list.print();
    }
}
