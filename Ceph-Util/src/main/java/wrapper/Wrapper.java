package wrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmapnode.CephMap;
import cephmapnode.CephNode;
import crush.CrushRunNoFailDisk;
import net.Address;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Wrapper implements Serializable{

	private volatile long epochVal;
	private boolean updating;
	SortedMap<String,FakeServer> map;	//holds all fake servers
	PriorityQueue<LinkedFakeServer> fakeServerLists;	//hold fake server lists belongs to same real server
	private CephMap cephMap;
	HashMap<String,LinkedFakeServer> realServerMap; //read address to fakelist
	
	public Wrapper(SortedMap<String,FakeServer> map,PriorityQueue<LinkedFakeServer> fakeServerLists,CephMap cephMap,HashMap<String,LinkedFakeServer> realServerMap){	//inital ceph map , with only a null real addr
		this.cephMap=cephMap;
		this.fakeServerLists=fakeServerLists;
		this.realServerMap= realServerMap;
		this.map = map;
	}
	
	public Map<String, FakeServer> getMap() {
		return map;
	}

	public void setMap(SortedMap<String, FakeServer> map) {
		this.map = map;
	}

	public PriorityQueue<LinkedFakeServer> getFakeServerLists() {
		return fakeServerLists;
	}

	public void setFakeServerLists(PriorityQueue<LinkedFakeServer> fakeServerLists) {
		this.fakeServerLists = fakeServerLists;
	}

	public CephMap getCephMap() {
		return cephMap;
	}

	public void setCephMap(CephMap cephMap) {
		this.cephMap = cephMap;
	}

	public Map<String, LinkedFakeServer> getRealServerMap() {
		return realServerMap;
	}

	public void setRealServerMap(HashMap<String, LinkedFakeServer> realServerMap) {
		this.realServerMap = realServerMap;
	}

	public void setEpochVal(long epochVal) {
		this.epochVal = epochVal;
	}

	//default constructor for jackson
	public Wrapper(){
		this.cephMap= new CephMap();
		this.fakeServerLists=new PriorityQueue<>();
		this.realServerMap=  new HashMap<>();
		this.map =new TreeMap<>();
	}
	
	
	
	public boolean initWrapper(){
		
		
		List<CephNode> osdList=cephMap.getAvailableOSDList();//get all osd nodes from cephmap;
		if(osdList.size()==0 || fakeServerLists.size()==0)
			return false;
		for(CephNode node:osdList){	//add fake ceph nodes into treemap	
			LinkedFakeServer list=fakeServerLists.poll();
			WrappedAddress tmp=new WrappedAddress(node.getAddress());
			FakeServer server=new FakeServer(tmp,list.getRealServer());
			list.insertHead(server);
			fakeServerLists.offer(list);
			map.put(node.getAddress().getIp()+":"+node.getAddress().getPort(), server);	
		}		
		epochVal=0;//no update epochVal , since it's the inital version
		return true;	
	}
	
    public synchronized void updateEpochVal() {
        this.epochVal = System.currentTimeMillis();

    }

    public long getEpochVal() {
        return this.epochVal;
    }
	
	public Address search(Address fakeServer){	
		WrappedAddress addr = new WrappedAddress(fakeServer);
		return map.get(addr.getServerAddr()).getRealServerAddr();	
	}
	
	public boolean removeRealServer(String IP,int port){
		if(fakeServerLists.size()==1)
			return false;	//cannot remove last real server
		String ipaddr=IP+":"+port;
		LinkedFakeServer tmp=realServerMap.get(ipaddr);
		if(!fakeServerLists.remove(tmp))
			return false;
		if(realServerMap.remove(ipaddr)==null)
			return false;
		while(tmp.getSize()>0){
			FakeServer server=tmp.pop();
			if(server==null)
					return false;
			LinkedFakeServer list=fakeServerLists.poll();
			server.setRealServerAddr(list.getRealServer());
			list.insertHead(server);
			fakeServerLists.offer(list);
			
		}
		updateEpochVal();
		return true;
	}
	
	public boolean removeRealServer(String rawIP){
		String[] ip=rawIP.split(":");
		return removeRealServer(ip[0],Integer.valueOf(ip[1]));
	}

	public synchronized boolean  addRealServer(WrappedAddress realServer){
		LinkedFakeServer list = new LinkedFakeServer(realServer);
		if(realServerMap.containsKey(list))
				return false;	//duplicate realServer;
		fakeServerLists.add(list);
		realServerMap.put(realServer.getServerAddr(), list);	//no rellocation here.		
		updateEpochVal();
		return true;	
	}
	
	public synchronized boolean  addRealServer(String rawIP){
		WrappedAddress realServer = new WrappedAddress(rawIP);
		LinkedFakeServer list = new LinkedFakeServer(realServer);
		if(realServerMap.containsKey(list))
				return false;	//duplicate realServer;
		fakeServerLists.add(list);
		realServerMap.put(realServer.getServerAddr(), list);	//no rellocation here.		
		updateEpochVal();
		return true;	
	}
	
	public boolean setFakeServerListFail(WrappedAddress realServer){
		//set the fake nodes to unreachable;
		LinkedFakeServer list = realServerMap.get(realServer.getServerAddr());
		list.setNodeFail();
		updateEpochVal();
		return true;
	}
	
	public boolean setFakeServerListOk(WrappedAddress realServer){
		LinkedFakeServer list = realServerMap.get(realServer.getServerAddr());
		list.setNodeOK();
		updateEpochVal();
		return true;
	}

	
	public List<Address> searchReadFile(String fileName){		
		String type="read";
		List<Address> result = new ArrayList<>();
		ArrayList<CephNode> res=CrushRunNoFailDisk.getInstance().runCrush(cephMap, fileName, type);//use crush to calculate the target osd server		
		for(CephNode node:res){
			String tmp=node.getAddress().getIp()+":"+node.getAddress().getPort();
			System.out.println("search virtual node:"+tmp);
			FakeServer fakeserver=map.get(tmp);
			result.add(fakeserver.getRealServerAddr());
		}
		return result;	
	}
	
	public List<Address> searchReadFile(String fileName,String type){		
		List<Address> result = new ArrayList<>();
		ArrayList<CephNode> res=CrushRunNoFailDisk.getInstance().runCrush(cephMap, fileName, type);//use crush to calculate the target osd server		
		for(CephNode node:res){
			String tmp=node.getAddress().getIp()+":"+node.getAddress().getPort();
			System.out.println("search virtual node:"+tmp);
			FakeServer fakeserver=map.get(tmp);
			result.add(fakeserver.getRealServerAddr());
		}
		return result;	
	}

	public void printLayOut(){
		System.out.println("******physical node*******************************virtual node*************************");
		for(String str:realServerMap.keySet()){
			LinkedFakeServer linkedserver=realServerMap.get(str);
			FakeServer server=linkedserver.head.next;
			while(server!=null){				
				System.out.println(server.getIdentity());
				server=server.next;
			}
			System.out.println("subtotal"+linkedserver.getSize()+"mappings");
		}
		System.out.println("total "+map.size()+"mappings");
	}



	public boolean isUpdating() {
		return updating;
	}



	public void setUpdating(boolean updating) {
		this.updating = updating;
	}
	

}



