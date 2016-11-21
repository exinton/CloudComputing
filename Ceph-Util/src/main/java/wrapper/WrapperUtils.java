package wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import cephmapnode.CephMap;
import cephmapnode.CephNode;
import message.IOMessageConstants;
import net.Address;
import net.IOControl;
import net.MsgType;
import net.Session;
import types.MonitorMsgType;

public class WrapperUtils {
	
	private static Address monitorAddr;
	private static File jsonFile;
	static 	ObjectMapper mapper = new ObjectMapper();
	static{	
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	    mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
	    mapper.setSerializationInclusion(Include.NON_NULL);	
	}

	public static Address getMonitorAddr() {
		return monitorAddr;
	}

	public static void  setMonitorAddr(Address monitorAddr) {
		WrapperUtils.monitorAddr = monitorAddr;
	}

	public static boolean saveToJSON(File file,Object obj){
		try {
			mapper.writeValue(file, obj);
		} catch (IOException e) {			
			e.printStackTrace();
			System.out.println("writing wrapper to json file failed");
			return false;
		}
		return true;
	}
	
	public static Wrapper loadFromString(String input){
		Wrapper wrapper=null;
		try {
			wrapper = mapper.readValue(input, Wrapper.class);
		} catch (IOException e) {
			System.out.println("IO exception during deserialization!");
			e.printStackTrace();
			System.out.println("reinitialize a wrapper with default setting");
			return null;
		}
        return wrapper;
	}
	
	public static String saveToString(Wrapper wrapper){
		String res=null;
		try {
			res=mapper.writeValueAsString(wrapper);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("serializing wrapper fail");
			return null;
		}
		return res;
	}
	
	public static Wrapper loadFromJSON(File file){
		Wrapper wrapper=null;
		try {
			wrapper=mapper.readValue(file, Wrapper.class);				
		} catch (IOException e) {
			System.out.println("IO exception during deserialization!");
			e.printStackTrace();
			System.out.println("reinitialize a wrapper with default setting");
			return WrapperUtils.initWrapper();
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
		String dir = System.getProperty("user.dir");		
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(dir + File.separator + "conf" + File.separator + fileName);
			System.out.println("Reading From properties " + dir + File.separator + "conf" + File.separator + "fileName");
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
				
				if(!wrapper.fakeServerLists.remove(linkedserver)){
					throw new NullPointerException();
				}					
				
				//System.out.println("link size is"+linkedserver.getSize());
				while(linkedserver.getSize()>averageFakeNodes){
					FakeServer fakeserver = linkedserver.pop();					
					LinkedFakeServer tmp = wrapper.fakeServerLists.poll();					
					tmp.insertHead(fakeserver);
					wrapper.fakeServerLists.add(tmp);
				}
				wrapper.fakeServerLists.add(linkedserver);	
			}
			
		}
		wrapper.updateEpochVal();
		return wrapper;
		
	}
	
	public static boolean uploadWrapper(Wrapper wrapper){
		IOControl control = new IOControl(); 
 		
 		 Session session = new Session(MonitorMsgType.UPDATE_MAP);
         session.set("epochVal", wrapper.getEpochVal());
         String outJson=null;
         Session response=null;

		try {
            outJson = mapper.writeValueAsString(wrapper); 
	        session.set("updatedMap", outJson);  
	        response = control.request(session, monitorAddr);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		String res= response.getString(IOMessageConstants.UPDATE_MAP_RESPONSE_MESSAGE);
		if(res.equals("updated")){
            saveToJSON(jsonFile,wrapper);
            return true;
		}		
		else 
			return false;

	}
	
	public static Wrapper initWrapper(){
		
		List<WrappedAddress> realServerLists=WrapperUtils.getPhysicalNodes();	
		PriorityQueue<LinkedFakeServer> fakeServerLists=new PriorityQueue<LinkedFakeServer>();
		HashMap<String,LinkedFakeServer>  realServerMap= new HashMap<>();
		for(WrappedAddress addr:realServerLists){
			LinkedFakeServer tmp=new LinkedFakeServer(addr);	
			fakeServerLists.add(tmp);
			realServerMap.put(addr.getServerAddr(),tmp);
		}			
		CephMap cephMap=WrapperUtils.getCephMap();		
		SortedMap<String,FakeServer> map = new TreeMap<>();
		Wrapper wrapper =new Wrapper(map,fakeServerLists,cephMap,realServerMap);
		wrapper.initWrapper();
		return wrapper;

	}
	
	
    public static Wrapper downloadWrapper() {
    	IOControl control = new IOControl(); 
    	Wrapper wrapper=null; 
    	long version=-1;
 		if(jsonFile.isFile()){
			 wrapper= WrapperUtils.loadFromJSON(jsonFile);
			 version=wrapper.getEpochVal();
		 }
			 
    	//only one monitor in addr
        Session session = new Session(MonitorMsgType.CACHE_VALID);
        session.set("epochVal", version);

                  //     System.out.println("here   "+a);
                  //      a++;
                        Session response = null;
						try {
							response = control.request(session, monitorAddr);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        if (response.getType() == MonitorMsgType.CACHE_VALID) {
                            if (!response.getBoolean("isValid")) {
                               System.out.println("the current wrapper version is older than the monitor's,update it!");
                               String jsonValue = response.getString("latestMap");
       
                               try {
								wrapper = mapper.readValue(jsonValue, Wrapper.class);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                               WrapperUtils.saveToJSON(jsonFile, wrapper);
                            }
                        }
                        System.out.println("the current wrapper version is the same with monitor's,no update!");

        return wrapper;

    }

	public static File getJsonFile() {
		return jsonFile;
	}

	public static void setJsonFile(File jsonFile) {
		WrapperUtils.jsonFile = jsonFile;
	}
	

}
