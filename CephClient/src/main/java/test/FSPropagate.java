package test;

import client.CephClientGlobalParameters;
import req.Rand.RandomGenerator;
import req.Rand.UniformGenerator;
import req.Request;
import req.RequestCallback;
import req.StaticTree;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import client.FileReadWriteClient;
import client.MonitorClientInterface;
import net.IOControl;

public class FSPropagate {

    public static IOControl control = new IOControl();

    public static String printLine(List list) {
        String lstring = list.toString();
        return lstring.substring(1, lstring.length() - 1) + "\n";
    }

    public static void parse(String input, String output, RequestCallback call) {
        try (Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(output), "UTF-8"))) {
            StaticTree tree = StaticTree.getStaticTree(input, new UniformGenerator());
            System.out.println(tree.getFileSize()+"  asdf"+input);
            for (int i = 0; i < tree.getFileSize(); ++i) {
                Request req = tree.fileInfo(i);
System.out.println(req);
                req.type = Request.ReqType.CREATE_FILE;
                out.write(printLine(call.call(req)));
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static class NullCall implements RequestCallback {

        List<Integer> order = new ArrayList<>();
        RandomGenerator uniform = new UniformGenerator();

        public NullCall() {
            for (int i = 1; i < 10; ++i) {
                order.add(i);
            }
        }

        @Override
        public List<Integer> call(Request request) {
            StaticTree.plainShuffle(order, uniform);
            /*	int find=uniform.nextInt(6)+1; //  1~6
             System.out.println(request+" : "+find);
             return order.subList(0,find);*/
            System.out.println(request);
            List<Integer> result = FileReadWriteClient.testCreate(control, request.path.trim(), request.end);
            String res = "";
            for (Integer a : result) {
                res += a + "   ";
            }
            System.out.println(request + " : " + res);
            return result;
        }
    }

    public static void main(String args[]) throws IOException {
        InputStream input = null;
        Properties prop = new Properties();
        try {
            FileReadWriteClient.initTest();
            final String CEPH_HOME = System.getenv("CEPH_HOME");
            System.out.println("Reading from " + CEPH_HOME + File.separator + "conf" + File.separator + "test.properties");
            input = new FileInputStream(CEPH_HOME + File.separator + "conf" + File.separator + "test.properties");
            prop.load(input);
            
//            String inputFile = "files/test.txt";//prop.getProperty("INPUT_FILE");
//            String rankFile = "files/rank.txt";//prop.getProperty("RANK_FILE");
            
            String inputFile = prop.getProperty("INPUT_FILE");
            String rankFile = prop.getProperty("RANK_FILE");
            
            CephClientGlobalParameters.setCephMap(null,0);
            MonitorClientInterface.updateCache(control);
            parse(inputFile, rankFile, new NullCall());
         //   FileReadWriteClient.endTest();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
