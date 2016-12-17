package cephrados;

import java.io.File;
import com.ceph.rados.IoCTX;
import com.ceph.rados.Rados;
import com.ceph.rados.exceptions.RadosException;
import RESTAPI.NoImplementionException;
import req.Request;

public class RadosClientBase {
	IoCTX io;
	public RadosClientBase(){
		 Rados cluster = new Rados("test1");
         System.out.println("Created cluster handle.");
         try{
        	 File f = new File("/etc/ceph/ceph.conf");
             cluster.confReadFile(f);
             System.out.println("Read the configuration file.");
             cluster.confSet("key", "AQCKA1NYMMgmHxAAME1iu9RUpO5hef8ICAQVbg==");
             cluster.confSet("mon_host", "192.168.1.129");
             cluster.connect();
             System.out.println("Connected to the cluster.");
             io = cluster.ioCtxCreate("data");
         }catch (RadosException e) {
             System.out.println(e.getMessage() + ": " + e.getReturnValue());
         }
         
		
	}
	
	public long read(Request request) throws Exception {
		long len=request.end-request.start;
		byte[] buf = new byte[(int)len];
		try{
			io.read(request.path, (int) len,request.start, buf);
		}catch(Exception e){
			e.printStackTrace();
			throw new NoImplementionException("read failure");
		}	
		return len;		
	}
	
	public long randomWrite(Request request) throws Exception{
		int len=(int) (request.end-request.start);		
		try{
			io.write(request.path, new byte[len], request.start);	
		}catch(Exception e){
			throw new NoImplementionException("random write failure");
		}
		return len;
	}
	
	public long seqWrite(Request request) throws Exception{
		int len=(int) (request.end-request.start);		
		try{
			io.write(request.path, new byte[len]);	
		}catch(Exception e){
			throw new NoImplementionException("random write failure");
		}
		return len;
	}
	
	
	public void create_file(Request request) throws Exception{
		long len=request.end-request.start;
		try{
			io.write(request.path, new byte[(int) len]);
		}catch(Exception e){
			e.printStackTrace();
			throw new NoImplementionException("create fail");
		}
					
	}
	public void append_file(Request request) throws Exception{
		int len=(int) (request.end-request.start);
		try{
			io.append(request.path, new byte[len], len);
		}catch(Exception e){
			throw new NoImplementionException("create fail");
		}
					
	}
	
	public void force_delete_file(Request request) throws Exception {
		try{
			io.remove(request.path);
		}catch(Exception e){
			throw new NoImplementionException("not supported by openstack");
		}
	}
	

	
	public void ls(Request request) throws Exception {
		try{
			String[] dirs = io.listObjects();
		}catch(Exception e){
			throw new NoImplementionException("not supported by openstack");
		}
	}


	
	
}
