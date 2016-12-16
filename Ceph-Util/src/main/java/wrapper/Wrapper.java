package wrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cephmapnode.CephMap;
import cephmapnode.CephNode;
import crush.CrushRunNoFailDisk;
import net.Address;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Wrapper implements Serializable{
	private volatile long epochVal;
	private boolean updating;
	private TreeMap<String,VirtualServer> map=new TreeMap<String,VirtualServer>();	//holds all fake servers
	private PriorityQueue<LinkedFakeServer> fakeServerListsByVolume;
	private ArrayList<LinkedFakeServer> failedLinkedLists;
	private ArrayList<LinkedFakeServer> overloadedLinkedLists;

	private CephMap cephMap;
	private HashMap<String,LinkedFakeServer> realServerMap=new HashMap<String,LinkedFakeServer>();; //read address to fakelist
	
	public Wrapper(SortedMap<String,VirtualServer> map,PriorityQueue<LinkedFakeServer> fakeServerListsByVolume,PriorityQueue<LinkedFakeServer> fakeServerListsByLoad,CephMap cephMap,HashMap<String,LinkedFakeServer> realServerMap){	//inital ceph map , with only a null real addr
		this.cephMap=cephMap;
		this.fakeServerListsByVolume=new PriorityQueue<LinkedFakeServer>(fakeServerListsByVolume);
		this.realServerMap.putAll(realServerMap);
		this.map.putAll(map);
		this.failedLinkedLists=new ArrayList<>();
		this.overloadedLinkedLists=new ArrayList<>();
	}
	
	public Map<String, VirtualServer> getMap() {
		return map;
	}
	
	public LinkedFakeServer getLinkedFakeServerFromMap(String rawip){
		return realServerMap.get(rawip);
	}

	public void setMap(TreeMap<String, VirtualServer> map) {
		this.map=map;
	}


	public CephMap getCephMap() {
		return cephMap;
	}
	public ArrayList<LinkedFakeServer> getFailedLinkedLists() {
		return failedLinkedLists;
	}
	public ArrayList<LinkedFakeServer> getOverloadedLinkedLists() {
		return overloadedLinkedLists;
	}

	public void setOverloadedLinkedLists(ArrayList<LinkedFakeServer> overloadedLinkedLists) {
		this.overloadedLinkedLists = overloadedLinkedLists;
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
	}
	
	public void initPriorityQueue(){
		PriorityQueue<LinkedFakeServer> tmp1=new PriorityQueue<LinkedFakeServer>(fakeServerListsByVolume);		
		this.fakeServerListsByVolume=new  PriorityQueue<LinkedFakeServer>(1, new VolumeComparator());		
		this.fakeServerListsByVolume.addAll(tmp1);
		this.failedLinkedLists=new ArrayList<>();
		this.overloadedLinkedLists=new ArrayList<>();
		
	}
	
	public List<Address> getRealServerList(){
		Set<String> tmp = getRealServerMap().keySet();
		List<Address> res = new ArrayList<>();
		for(String str:tmp){
			String[] rawip = str.split(":");
			res.add(new Address(rawip[0],Integer.valueOf(rawip[1])));
		}
		return res;
	}
	
	public boolean initWrapper(){
		
		
		List<CephNode> osdList=cephMap.getAvailableOSDList();//get all osd nodes from cephmap;
		if(osdList.size()==0 || fakeServerListsByVolume.size()==0)
			return false;
		for(CephNode node:osdList){	//add fake ceph nodes into treemap	
			LinkedFakeServer list=fakeServerListsByVolume.poll();
			WrappedAddress tmp=new WrappedAddress(node.getAddress());
			VirtualServer server=new VirtualServer(tmp,list.getRealServer());
			list.insertHead(server);
			fakeServerListsByVolume.offer(list);			
			
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
		if(fakeServerListsByVolume.size()==1)
			return false;	//cannot remove last real server
		String ipaddr=IP+":"+port;
		LinkedFakeServer tmp=realServerMap.get(ipaddr);
		if(!realServerMap.containsKey(ipaddr))
			return false;
		fakeServerListsByVolume.remove(tmp);
		while(tmp.getSize()>0){
			VirtualServer server=tmp.pop();
			if(server==null)
					return false;
			LinkedFakeServer list=fakeServerListsByVolume.poll();
			server.setRealServerAddr(list.getRealServer());
			list.insertHead(server);
			fakeServerListsByVolume.offer(list);	
		}
		realServerMap.remove(ipaddr);
		updateEpochVal();
		return true;
	}
	

	
	
	public PriorityQueue<LinkedFakeServer> getFakeServerListsByVolume() {
		return fakeServerListsByVolume;
	}

	public void setFakeServerListsByVolume(PriorityQueue<LinkedFakeServer> fakeServerListsByVolume) {
		this.fakeServerListsByVolume = fakeServerListsByVolume;
	}


	public boolean removeRealServer(String rawIP){
		String[] ip=rawIP.split(":");
		return removeRealServer(ip[0],Integer.valueOf(ip[1]));
	}

	public synchronized boolean  addRealServer(WrappedAddress realServer){
		LinkedFakeServer list = new LinkedFakeServer(realServer);
		if(realServerMap.containsKey(list))
				return false;	//duplicate realServer;
		fakeServerListsByVolume.add(list);
		realServerMap.put(realServer.getServerAddr(), list);	//no rellocation here.		
		updateEpochVal();
		return true;	
	}
	
	public synchronized boolean  addRealServer(String rawIP){
		WrappedAddress realServer = new WrappedAddress(rawIP);
		LinkedFakeServer list = new LinkedFakeServer(realServer);
		if(realServerMap.containsKey(list))
				return false;	//duplicate realServer;
		fakeServerListsByVolume.add(list);
		realServerMap.put(realServer.getServerAddr(), list);	//no rellocation here.		
		updateEpochVal();
		return true;	
	}
	
	public boolean setFakeServerListFail(String realServer){
		//set the fake nodes to unreachable;
		LinkedFakeServer list = realServerMap.get(realServer);
		fakeServerListsByVolume.remove(list);
		failedLinkedLists.add(list);
		list.setFail(true);		
		updateEpochVal();
		return true;
	}
	public boolean setFakeServerListOverLoad(String realServer){
		//set the fake nodes to unreachable;
		LinkedFakeServer list = realServerMap.get(realServer);
		fakeServerListsByVolume.remove(list);
		overloadedLinkedLists.add(list);
		list.setOverLoad(true);	
		updateEpochVal();
		return true;
	}
	
	public boolean setFakeServerListNotOverLoad(String realServer){
		//set the fake nodes to unreachable;
		LinkedFakeServer list = realServerMap.get(realServer);
		fakeServerListsByVolume.add(list);
		overloadedLinkedLists.remove(list);
		list.setOverLoad(false);	
		updateEpochVal();
		return true;
	}
	
	
	public boolean setFakeServerListNotFail(String realServer){
		LinkedFakeServer list = realServerMap.get(realServer);
		fakeServerListsByVolume.add(list);
		failedLinkedLists.remove(list);
		list.setFail(false);
		updateEpochVal();
		return true;
	}

	
	public List<Address> searchReadFile(String fileName){		
		String type="read";
		return searchFile(fileName,type);
	}
	/**
	 * return the non-failed physical nodes
	 * @param fileName
	 * @param type
	 * @return
	 */
	public List<Address> searchFile(String fileName,String type){		
		List<Address> result = new ArrayList<>();
		ArrayList<CephNode> res=CrushRunNoFailDisk.getInstance().runCrush(cephMap, fileName, type);//use crush to calculate the target osd server		
		for(CephNode node:res){
			String tmp=node.getAddress().getIp()+":"+node.getAddress().getPort();			
			VirtualServer fakeserver=map.get(tmp);
			LinkedFakeServer linkedserver = realServerMap.get(fakeserver.getRealServerAddr().getServerAddr());
			System.out.println("file "+fileName+" located at "+fakeserver.getIdentity());
			result.add(fakeserver.getRealServerAddr());		
		}
		if(result.size()==0)
			result.add(new Address("0.0.0.0",0));
		return result;	
	}

	public void printWrapperLayOut(){
		System.out.println("***********virtual node**************************physical node*************************");
		for(String str:realServerMap.keySet()){
			LinkedFakeServer linkedserver=realServerMap.get(str);
			VirtualServer server=linkedserver.head.next;
			while(server!=null){				
				System.out.println(server.getIdentity());
				server=server.next;
			}
			System.out.println("subtotal"+linkedserver.getSize()+"mappings");
		}
		System.out.println("version:"+epochVal);
		System.out.println("total "+map.size()+"mappings");
	}

	public void printCephMapLayOut(){
		System.out.println("**********CephMap***********************");
		cephMap.printMap();
		System.out.println("version:"+epochVal);
	}
	
	public void printCephMapLayOut(Wrapper wrapper){
		System.out.println("**********CephMap********************************physical node**************");
		cephMap.printMap(wrapper);
		System.out.println("version:"+epochVal);
	}


	public boolean isUpdating() {
		return updating;
	}



	public void setUpdating(boolean updating) {
		this.updating = updating;
	}
	

}



