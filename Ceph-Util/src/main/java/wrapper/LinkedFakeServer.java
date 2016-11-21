package wrapper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, 
property="realServerTag",scope=LinkedFakeServer.class)
class LinkedFakeServer implements Comparable<LinkedFakeServer> {
	
	public FakeServer head;
	private int size;
	private WrappedAddress realServer;
	private String realServerTag;


	public void setSize(int size) {
		this.size = size;
	}
	public LinkedFakeServer(WrappedAddress realServer){
		head=new FakeServer(realServer);
		head.setHead(true);
		this.realServer=realServer;
		realServerTag=realServer.getIp()+":"+realServer.getPort();
		size=0;	//tail and head doesn't count;
	}
	public LinkedFakeServer(){
		
		
	}

	public FakeServer getHead() {
		return head;
	}
	public void setHead(FakeServer head) {
		this.head = head;
	}
	public WrappedAddress getRealServer() {
		return realServer;
	}
	public void setRealServer(WrappedAddress realServer) {
		this.realServer = realServer;
	}
	public boolean insertHead(FakeServer server){
		if(head==null)	
				return false;	//if head is null, return false
		server.setRealServerAddr(getRealServer());
		server.next=head.next;
		if(head.next!=null)
			head.next.prev=server;
		head.next=server;
		server.prev=head;
		size++;
		return true;
	}

	
	public boolean delete(FakeServer server){
		if(server.next==null)
			return false;
		server.next.prev=server.prev;
		server.prev.next=server.next;
		size--;
		return true;
	}
	
	public FakeServer pop(){
		if(size==0)
			return null;
		FakeServer tmp=head.next;
		delete(tmp);
		return tmp;
	}
	
	public void setNodeOK(){
		FakeServer tmp = head.next;
		while(!tmp.isHead()){
			tmp.setFail(false);
			tmp=tmp.next;
		}
	}
	
	public void setNodeFail(){
		FakeServer tmp = head.next;
		while(!tmp.isHead()){
			tmp.setFail(true);
			tmp=tmp.next;
		}
	}
	
	public int getSize() {
		return size;
	}


	@Override
	public int compareTo(LinkedFakeServer o) {
		return this.size-o.size;	//could be any other factors other than size of fake servers
	}


}