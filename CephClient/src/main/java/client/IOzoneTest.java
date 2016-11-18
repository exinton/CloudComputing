package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import cephmapnode.CephMap;
import net.IOControl;

public class IOzoneTest {

    public static void main(String[] args) {

        try {

            CephMap cephMap = new CephMap();
            cephMap.setEpochVal(0L);
            cephMap.setNode(null);
            CephClientGlobalParameters.setCephMap(cephMap);
            IOControl control = new IOControl();
            Properties prop = new Properties();
            InputStream input = null;

            final String CEPH_HOME = System.getenv("CEPH_HOME");
            System.out.println("Reading from " + CEPH_HOME + File.separator + "conf" + File.separator + "test.properties");
            input = new FileInputStream(CEPH_HOME + File.separator + "conf" + File.separator + "test.properties");
            prop.load(input);
            String serverConf = prop.getProperty("TEST_HOME");
            String filename = prop.getProperty("TEST_FILE");
            File file = new File(filename);

            String line = null;

            BufferedReader br = new BufferedReader(new FileReader(file));

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                String tokens[] = line.split(":");
                if (tokens[0].trim().equalsIgnoreCase("FileWrite")) {
                    System.out.println(" Writing file ......");
                    FileReadWriteClient.test(control, "write", serverConf + File.separator + (tokens[1]).trim(), 0L);
                }
                if (tokens[0].trim().equalsIgnoreCase("FileRead")) {
                    System.out.println(" reading file ......");
                    FileReadWriteClient.test(control, "read", (tokens[1]).trim(), 0L);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
