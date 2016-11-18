/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crush;

import cephmapnode.CephMap;
import cephmapnode.CephNode;
import java.util.ArrayList;

/**
 *
 * @author Avinash
 */
public class MultiThreadedCrush implements Runnable{
    
    private CephMap cephMap;
    private String fileName;
    
    private ArrayList<CephNode> threadOutput;
    @Override
    public void run() {
        CrushRun crushRun = CrushRun.getInstance();
        ArrayList<CephNode> runCrushOutPutNode = crushRun.runCrush(this.cephMap, this.fileName,"READ");
        this.setThreadOutput(new ArrayList<>(runCrushOutPutNode));
    }

    /**
     * @return the cephMap
     */
    public CephMap getCephMap() {
        return cephMap;
    }

    /**
     * @param cephMap the cephMap to set
     */
    public void setCephMap(CephMap cephMap) {
        this.cephMap = cephMap;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the threadOutput
     */
    public ArrayList<CephNode> getThreadOutput() {
        return threadOutput;
    }

    /**
     * @param threadOutput the threadOutput to set
     */
    public void setThreadOutput(ArrayList<CephNode> threadOutput) {
        this.threadOutput = threadOutput;
    }

    public MultiThreadedCrush(CephMap cephMap, String fileName) {
        this.cephMap = cephMap;
        this.fileName = fileName;
    }
    
    
    
}
