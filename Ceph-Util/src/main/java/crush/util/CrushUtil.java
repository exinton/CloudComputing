/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crush.util;
import crush.CrushLevel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
/**
 *
 * @author Avinash
 */
public class CrushUtil {
    
    public static ArrayList<CrushLevel> setCrushProperty (String CephHome) {
     //Read the propery file inside
    	String dir = System.getProperty("user.dir");
            
	final String CRUSH_PROPERTIES_FILE = dir+File.separator+"conf"+File.separator+"crushmap.properties";
        Properties prop = new Properties();
	InputStream input = null;

//        crush.level1.level_no = 1;
//crush.lvele1.name = row;
//crush.level1.select_items = 1;
	try {
                ArrayList<CrushLevel> crushLevels = new ArrayList();
		input = new FileInputStream(CRUSH_PROPERTIES_FILE);

		// load a properties file
		prop.load(input);

		// get the property value and print it out
                int storageLevels = Integer.parseInt(prop.getProperty("crush.number_of_storage_levels"));
                for (int i = 1; i <= storageLevels; i++) {
                    
                    String level_no = prop.getProperty("crush.level"+i+".level_no");
                    String level_name = prop.getProperty("crush.level"+i+".level_name");
                    String select_items = prop.getProperty("crush.level"+i+".select_items");
                    
                    CrushLevel cl = new CrushLevel();
                    cl.setLevelno(Integer.parseInt(level_no));
                    cl.setLevelname(level_name);
                    cl.setLevelReplica(Integer.parseInt(select_items));
                    
                    crushLevels.add(cl);
            }
            
            if(crushLevels.size()!=storageLevels) {
               throw new IOException("Properties not correctly set, set property for each level");
            }
            
            return crushLevels;
            
	} catch(IOException e) {
           e.printStackTrace();
        }
        return null;
    }
}
