package cephrados;

import com.ceph.rados.Rados;
import com.ceph.rados.exceptions.RadosException;

import api.RequestAdapterAPI;
import req.Request;

import java.io.File;
import com.ceph.rados.IoCTX;

public class CephClient{
		
	
	
	
        public static void main (String args[]){

                try {
                	 Rados cluster = new Rados("test1");
                     System.out.println("Created cluster handle.");

                     File f = new File("/etc/ceph/ceph.conf");
                     cluster.confReadFile(f);
                     System.out.println("Read the configuration file.");
                     cluster.confSet("key", "AQABFlZYKL8zIhAAX9MVqhlmg9A49amNi8W/rg==");
                     cluster.confSet("mon_host", "192.168.1.129");

                        cluster.connect();
                        System.out.println("Connected to the cluster.");

                        IoCTX io = cluster.ioCtxCreate("data");

                        String oidone = "hw";
                        String contentone = "Hello World!";
                        io.write(oidone, contentone);

                        String oidtwo = "bm";
                        String contenttwo = "Bonjour tout le monde!";
                        io.write(oidtwo, contenttwo);

                        String[] objects = io.listObjects();
                        for (String object: objects){
                            System.out.println("file"+object.toString());                          
                        	io.remove(object);
                        }
                            
                    
                        io.remove(oidone);
                        io.remove(oidtwo);

                        cluster.ioCtxDestroy(io);

                } catch (RadosException e) {
                        System.out.println(e.getMessage() + ": " + e.getReturnValue());
                }
        }

}