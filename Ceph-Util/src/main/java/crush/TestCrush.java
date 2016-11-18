/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crush;

import cephmapnode.CephMap;
import cephmapnode.CephNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avinash
 */
public class TestCrush {
    
    public static CephMap cephMap;
    public static void main(String[] args) throws InterruptedException, IOException {
        
        
            final  String CEPH_HOME = System.getenv("CEPH_HOME");
            
            final String init_map  = CEPH_HOME+File.separator+"init_map.json";
            ObjectMapper mapper = new ObjectMapper();
            
            System.out.println(" finished reading init map");
            cephMap= mapper.readValue(new File(init_map), CephMap.class);
            cephMap.printMap();
            
            
            String fileName = "##@#$@#$@#$@#$@!#$@#$@#$@";
            
            CrushRun cr = CrushRun.getInstance();
            
            ArrayList<CephNode> nodes = cr.runCrush(cephMap, fileName,"READ");
            
            for (CephNode node : nodes) {
                
                System.out.println(node.getId() + "@" +node.getAddress().getIp());
            }
            
            
//        MultiThreadedCrush m = new MultiThreadedCrush(cephMap, fileName);
//        Thread crushRunner = new Thread(m);
//        crushRunner.start();
//        crushRunner.join(1000);
//        ArrayList<CephNode> nodes2 = m.getThreadOutput();
//       
//        for (CephNode node : nodes2) {
//           System.out.println(node.getId());
//        }
        
        
        
        
    }
    
        
    
}
