package wrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cephmap.cephmapmonitors.CephGlobalParameter;
import cephmapnode.CephMap;
import cephmapnode.CephNode;
import crush.CrushRunNoFailDisk;
import net.Address;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Wrapper implements Serializable{
	
	private long epochVal;
	private boolean updating;
	Map<String,FakeServer> map;	//holds all fake servers
	PriorityQueue<LinkedFakeServer> fakeServerLists;	//hold fake server lists belongs to same real server
	CephMap cephMap;
	Map<String,LinkedFakeServer> realServerMap; //read address to fakelist
	
	public Wrapper( ){	//inital ceph map , with only a null real addr
		
		List<WrappedAddress> realServerLists=WrapperUtils.getPhysicalNodes();
		fakeServerLists=new PriorityQueue<LinkedFakeServer>();
		realServerMap= new HashMap<>();
		for(WrappedAddress addr:realServerLists){
			LinkedFakeServer tmp=new LinkedFakeServer(addr);
			fakeServerLists.add(tmp);	//add real server into lists
			realServerMap.put(addr.serverAddr,tmp);
		}			
		this.cephMap=WrapperUtils.getCephMap();		
		map = new TreeMap<>();
		initWrapper();
	}
	
	
	
	public boolean initWrapper(){
		List<CephNode> osdList=cephMap.getAvailableOSDList();//get all osd nodes from cephmap;
		if(osdList.size()==0 || fakeServerLists.size()==0)
			return false;
		for(CephNode node:osdList){	//add fake ceph nodes into treemap
		
			LinkedFakeServer list=fakeServerLists.poll();
			WrappedAddress tmp=new WrappedAddress(node.getAddress());
			FakeServer server=new FakeServer(tmp,list.realServer);
			list.insertHead(server);
			fakeServerLists.offer(list);
			map.put(node.getAddress().getIp()+":"+node.getAddress().getPort(), server);	
		}
		upateEpochVal();
		return true;	
	}
	
    public void upateEpochVal() {
        this.epochVal = System.currentTimeMillis();

    }

    public long getEpochVal() {
        return this.epochVal;
    }
	
	public Address search(Address fakeServer){	
		WrappedAddress addr = new WrappedAddress(fakeServer);
		return map.get(addr.serverAddr).getRealServerAddr();	
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
			server.setRealServerAddr(list.realServer);
			list.insertHead(server);
			fakeServerLists.offer(list);
			
		}
		return true;
	}

	public boolean addRealServer(WrappedAddress realServer){
		LinkedFakeServer list = new LinkedFakeServer(realServer);
		if(realServerMap.containsKey(list))
				return false;	//duplicate realServer;
		fakeServerLists.add(list);
		realServerMap.put(realServer.serverAddr, list);	//no rellocation here.		
		return true;	
	}
	
	public boolean setFakeServerListFail(WrappedAddress realServer){
		//set the fake nodes to unreachable;
		LinkedFakeServer list = realServerMap.get(realServer.serverAddr);
		list.setNodeFail();
		return true;
	}
	
	public boolean setFakeServerListOk(WrappedAddress realServer){
		LinkedFakeServer list = realServerMap.get(realServer.serverAddr);
		list.setNodeOK();
		return true;
	}

	
	public List<Address> searchFile(String fileName){		
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

	public void printLayOut(){
		System.out.println("******physical node*******************************virtual node*************************");
		for(String str:realServerMap.keySet()){
			LinkedFakeServer linkedserver=realServerMap.get(str);
			FakeServer server=linkedserver.head.next;
			while(!server.isHead){				
				System.out.println(server.identity);
				server=server.next;
			}
			System.out.println("subtotal"+linkedserver.getSize()+"mappings");
		}
		System.out.println("total "+map.size()+"mappings");
	}



	public boolean isUpdating() {
		return updating;
	}



	public synchronized void setUpdating(boolean updating) {
		this.updating = updating;
	}
	

}
