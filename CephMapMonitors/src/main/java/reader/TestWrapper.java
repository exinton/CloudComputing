package reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmapnode.CephMap;
import net.Address;
import wrapper.WrappedAddress;
import wrapper.Wrapper;

public class TestWrapper {
	
	public static void main(String[] args){
		loadWrapper();
		
		
	}
	
	
	public static void loadWrapper(){
		 final String CEPH_HOME = System.getenv("CEPH_HOME");
	        if (CEPH_HOME == null) {
	            System.out.println("Cannot Find CEPH_HOME, Please set the system property");
	            System.exit(-1);
	        }
	        ObjectMapper mapper = new ObjectMapper();
	        String init_json = CEPH_HOME + File.separator + "conf" + File.separator + "init_map.json";

	        try {
				CephGlobalParameter.setCephMap(mapper.readValue(new File(init_json), CephMap.class));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        CephMap tmp =CephGlobalParameter.getCephMap();
	        
	 
	        List<WrappedAddress> listRealServer= generateRealServer();
	        Wrapper wrapper=new Wrapper(tmp,listRealServer);
	        

	}
	
	public static List<WrappedAddress> generateRealServer(){
		List<WrappedAddress> list = new ArrayList<>();
		for(int i=0;i<3;i++){
			WrappedAddress addr = new WrappedAddress("10.100.1.1",i+2000);
			list.add(addr);
		}
		return list;
		
	}
	

}
