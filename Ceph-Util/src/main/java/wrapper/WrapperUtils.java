package wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmapnode.CephMap;
import cephmapnode.CephNode;

public class WrapperUtils {
	
	public static boolean saveToJSON(File file,Object obj){
		ObjectMapper mapper = new ObjectMapper();	
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        mapper.setSerializationInclusion(Include.NON_NULL);
		try {
			mapper.writeValue(file, obj);
		} catch (IOException e) {			
			e.printStackTrace();
			System.out.println("writing wrapper to json file failed");
			return false;
		}
		return true;
	}
	
	public static Wrapper loadFromJSON(File file){
		ObjectMapper mapper = new ObjectMapper();
		Wrapper wrapper=null;
		try {
			wrapper = mapper.readValue(file, Wrapper.class);
		} catch (IOException e) {
			System.out.println("IO exception during deserialization!");
			e.printStackTrace();
			return null;
		}
        return wrapper;
	}
	
	
	public static CephMap getCephMap(){
		
		CephMap cephMap = new CephMap();
		//construct cephnode then cephmap
		Properties prop = getProperties("init_map.properties");
		
		//construct root
	    CephNode root = new CephNode();
        root.setLevelNo(0);
        root.setId("0");
        root.setType("root");
        root.setIsDisk(false);
        root.setAddress("1.0.0.0", 0);  //port==0 denotes that this is a subnet
        int[] prevId=new int[1];	//store the prev id
		//construnct row
        ArrayList<CephNode> rowList=generateRowCephNode(root,prevId,prop.get("row"),"row",1);
		root.setChildren(rowList);
		//construct cabinet
		for(CephNode node:rowList){		
			ArrayList<CephNode> cabinetList=generateRowCephNode(node,prevId,prop.get("cabinet"),"cabinet",2);
			node.setChildren(cabinetList);
		}		
		//construct disks
		for(CephNode node1:rowList){		
			for(CephNode node2:node1.getChildren()){
				ArrayList<CephNode> diskList=generateRowCephNode(node2,prevId,prop.get("disk"),"disk",3);
				node2.setChildren(diskList);
			}
		}	
		cephMap.setEpochVal(System.currentTimeMillis());
		cephMap.setNode(root);
		cephMap.updateHashRange();
		
		return cephMap;
	}
	
	public static boolean setIP(CephNode parent,CephNode curr,int num){

		String[] subnet=parent.getAddress().getIp().split("\\.");
		if(subnet.length!=4)
			return false;
		String res="";
		for(int i=0;i<4;i++){
			if(!subnet[i].equals("0")){
				res+=subnet[i]+".";
				if(i==3)
					return false;
				continue;
			}else{
				if(i==3){	//if it's the last range, then this is a ip, o/w its subnet 					
					res+=num;
					curr.setAddress(res, 1000);
					return true;
				}else{
					res+=num+".";
					for(int j=i+1;j<4;j++)
						res+="0"+".";
					res=res.substring(0,res.length()-1);
					curr.setAddress(res, 0);
					return true;
				}
			}
			
		}
		return false;
		
	}
	
	public static ArrayList<CephNode> generateRowCephNode(CephNode parent,int[] parentId,Object number,String type,int level){
		int num=Integer.valueOf((String)number);
		ArrayList<CephNode> list = new ArrayList<>();
		for(int i=0;i<num;i++){
			 CephNode tmp = new CephNode();
			 tmp.setLevelNo(level);
		        String newId = String.valueOf(++parentId[0]);
		        tmp.setId(newId);
		        tmp.setType(type);
		        if(type.equals("disk"))
		        	tmp.setIsDisk(true);
		        else
		        	tmp.setIsDisk(false);
		        setIP(parent,tmp,i+1);
		        tmp.setWeight(1);
		        list.add(tmp);
		}
		return list;
	}

	
	public static Properties getProperties(String fileName){
		final String ceph_home = System.getenv("CEPH_HOME");
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(ceph_home + File.separator + "conf" + File.separator + fileName);
			System.out.println("Reading From properties " + ceph_home + File.separator + "conf" + File.separator + "fileName");
	        prop.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        
		return prop;
	}
	
	
	public static List<WrappedAddress> getPhysicalNodes(){
		Properties prop = getProperties("physicalNodeRedundancy.properties");
		List<WrappedAddress> list = new ArrayList<>();
		int numOfNodes=Integer.valueOf(prop.getProperty("numberOfNodes"));
		for(int i=1;i<=numOfNodes;i++){
			String[] tmp = prop.getProperty("node."+i).split(":");
			WrappedAddress addr = new WrappedAddress(tmp[0],Integer.valueOf(tmp[1]));
			list.add(addr);
		}
		return list;
		
	}
	
	public static Wrapper rebalanceWrapper(Wrapper wrapper) {		
		wrapper.setUpdating(true);	//set wrapper status to updating
		int totalNumber=wrapper.map.size();
		int averageFakeNodes=totalNumber/wrapper.realServerMap.size();
		//System.out.println("avg size is"+averageFakeNodes);
		//System.out.println("real server number is"+wrapper.realServerMap.size());
		for(LinkedFakeServer linkedserver:wrapper.realServerMap.values()){
			
			if(linkedserver.getSize()>averageFakeNodes){			
				wrapper.fakeServerLists.remove(linkedserver);
				//System.out.println("link size is"+linkedserver.getSize());
				while(linkedserver.getSize()>averageFakeNodes){
					FakeServer fakeserver = linkedserver.pop();					
					LinkedFakeServer tmp = wrapper.fakeServerLists.poll();					
					fakeserver.setRealServerAddr(tmp.realServer);
					tmp.insertHead(fakeserver);
					wrapper.fakeServerLists.add(tmp);
				}
				wrapper.fakeServerLists.add(linkedserver);	
			}
			
		}
		wrapper.upateEpochVal();
		return wrapper;
		
	}
	

}
