package wrapper;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, 
property="realServerTag",scope=LinkedFakeServer.class)
class LinkedFakeServer implements Comparable<LinkedFakeServer> {
	
	public VirtualServer head;
	private int volumeCapacity;
	private int loadCapacity;
	private WrappedAddress realServer;
	private String realServerTag;
	private boolean isFail;
	private boolean isOverLoad;


	public boolean isFail() {
		return isFail;
	}
	public void setFail(boolean isFail) {
		this.isFail = isFail;
		VirtualServer server = head.next;
		while(server!=null){
			server.setFail(isFail);
			server=server.next;
		}
			
	}
	public boolean isOverLoad() {
		return isOverLoad;
	}
	public void setOverLoad(boolean isOverLoad) {
		this.isOverLoad = isOverLoad;
		VirtualServer server = head.next;
		while(server!=null){
			server.setOverload(isOverLoad);
			server=server.next;
		}
	}
	public void setSize(int size) {
		this.volumeCapacity = size;
	}
	public LinkedFakeServer(WrappedAddress realServer){
		head=new VirtualServer(realServer);
		head.setHead(true);
		this.realServer=realServer;
		realServerTag=realServer.getIp()+":"+realServer.getPort();
		volumeCapacity=0;	//tail and head doesn't count;
	}
	public LinkedFakeServer(){
		
		
	}

	public VirtualServer getHead() {
		return head;
	}
	public void setHead(VirtualServer head) {
		this.head = head;
	}
	public WrappedAddress getRealServer() {
		return realServer;
	}
	public void setRealServer(WrappedAddress realServer) {
		this.realServer = realServer;
	}
	public boolean insertHead(VirtualServer server){
		if(head==null)	
				return false;	//if head is null, return false
		server.setRealServerAddr(getRealServer());
		server.next=head.next;
		if(head.next!=null)
			head.next.prev=server;
		head.next=server;
		server.prev=head;
		volumeCapacity++;
		return true;
	}

	
	public boolean delete(VirtualServer server){
		if(server.next==null)
			return false;
		server.next.prev=server.prev;
		server.prev.next=server.next;
		volumeCapacity--;
		return true;
	}
	
	public VirtualServer pop(){
		if(volumeCapacity==0)
			return null;
		VirtualServer tmp=head.next;
		delete(tmp);
		return tmp;
	}
	
	
	public int getSize() {
		return volumeCapacity;
	}


	public int getVolumeCapacity() {
		return volumeCapacity;
	}
	public void setVolumeCapacity(int volumeCapacity) {
		this.volumeCapacity = volumeCapacity;
	}
	public int getLoadCapacity() {
		return loadCapacity;
	}
	public void setLoadCapacity(int loadCapacity) {
		this.loadCapacity = loadCapacity;
		
	}
	public String getRealServerTag() {
		return realServerTag;
	}
	public void setRealServerTag(String realServerTag) {
		this.realServerTag = realServerTag;
	}
	@Override
	public int compareTo(LinkedFakeServer o) {
		return this.volumeCapacity-o.volumeCapacity;	//could be any other factors other than size of fake servers
	}
	

}


